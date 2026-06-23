package com.crediguard.core.service;

import com.crediguard.core.model.CreditApplication;
import com.crediguard.core.repository.CreditRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
public class CreditService {

    private final CreditRepository creditRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; 

    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper(); 
    }

    public String processApplication(CreditApplication application) {
        System.out.println("Service: Holding application. Calling Python AI Compliance Engine...");

        String pythonApiUrl = "http://localhost:8000/api/v1/compliance/analyze/";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Manually convert the POJO to a String so Java doesn't use chunks
            String jsonBody = objectMapper.writeValueAsString(application);

            // Package the String and the Headers together
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            // SEND THE REQUEST TO PYTHON!
            Map<String, Object> aiResponse = restTemplate.postForObject(pythonApiUrl, requestEntity, Map.class);

            // Read the Python response
            boolean isCompliant = (boolean) aiResponse.get("is_compliant");
            String complianceNote = (String) aiResponse.get("compliance_note");
            
            // Safely parse the risk score
            double riskScore = ((Number) aiResponse.get("risk_score")).doubleValue(); 

            // Attach the AI's math and compliance decision to our Java POJO
            application.setRiskScore(riskScore);
            application.setComplianceNote(complianceNote);
            
            // NEW: Set the boolean approved status!
            application.setIsApproved(isCompliant);

            System.out.println("Python AI responded with Risk Score: " + riskScore + "%");

            // We save the application to the vault IMMEDIATELY for legal auditing, 
            // before we even check if they are approved or denied!
            creditRepository.save(application);

            if (!isCompliant) {
                return "Application DENIED by AI. Reason: " + complianceNote;
            } else {
                return "Application " + application.getApplicationId() + " APPROVED! AI Note: " + complianceNote;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "SYSTEM CRASH REASON: " + e.getMessage();
        }
    }

    public CreditApplication getApplicationStatus(String id) {
        return creditRepository.findById(id);
    }
}