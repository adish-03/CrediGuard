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

    // The Bank Manager wires the Repository and gives us a RestTemplate and ObjectMapper
    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper(); 
    }

    public String processApplication(CreditApplication application) {
        System.out.println("Service: Holding application. Calling Python AI Compliance Engine...");

        // 1. The exact address of our Python Django department
        String pythonApiUrl = "http://localhost:8000/api/v1/compliance/analyze/";

        try {
            // BULLETPROOF FIX 2: Create an official "Envelope" (Headers) telling Python this is strict JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // BULLETPROOF FIX 3: Manually convert the POJO to a raw String to prevent Python 'chunked' hex errors!
            String jsonBody = objectMapper.writeValueAsString(application);

            // Put the raw String and the Envelope together into an HttpEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            // 2. SEND THE REQUEST TO PYTHON! 
            Map<String, Object> aiResponse = restTemplate.postForObject(pythonApiUrl, requestEntity, Map.class);

            // 3. Read the Python response
            // THE FIX: Check for both the old key and the new key so the contract never breaks!
            boolean isCompliant = false;
            if (aiResponse.containsKey("compliant")) {
                isCompliant = (boolean) aiResponse.get("compliant");
            } else if (aiResponse.containsKey("is_compliant")) {
                isCompliant = (boolean) aiResponse.get("is_compliant");
            }
            
            // BULLETPROOF FIX 4: Safety nets for missing text fields!
            String complianceNote = "AI Decision recorded, but no note was provided.";
            if (aiResponse.containsKey("compliance_note") && aiResponse.get("compliance_note") != null) {
                complianceNote = (String) aiResponse.get("compliance_note");
            } else if (aiResponse.containsKey("prompt_sent_to_ai") && aiResponse.get("prompt_sent_to_ai") != null) {
                complianceNote = "AI OFFLINE. Mockup: " + aiResponse.get("prompt_sent_to_ai");
            }
            
            // BULLETPROOF FIX 5: Safely parse the number whether it's an Integer, Double, or entirely missing!
            double riskScore = 0.0;
            if (aiResponse.containsKey("risk_score") && aiResponse.get("risk_score") != null) {
                riskScore = ((Number) aiResponse.get("risk_score")).doubleValue(); 
            }

            // NEW: Attach the AI's math to our Java POJO!
            application.setRiskScore(riskScore);
            application.setComplianceNote(complianceNote);
            application.setIsApproved(isCompliant); // Ensure the boolean vault field gets updated!

            System.out.println("Python AI responded with Risk Score: " + riskScore + "%");

            // 4. Make the final banking decision based on the AI's ruling
            if (!isCompliant) {
                return "Application DENIED by AI. Reason: " + complianceNote;
            } else {
                // If AI approves, save it to the Postgres Vault
                creditRepository.save(application);
                return "Application " + application.getApplicationId() + " APPROVED! AI Note: " + complianceNote;
            }

        } catch (Exception e) {
            // We added e.printStackTrace() so if it crashes again, the terminal will tell us exactly why!
            e.printStackTrace();
            return "SYSTEM ERROR: The AI Compliance Engine is currently offline. Please try again later.";
        }
    }

    // Customer asking for their specific folder (Read Feature)
    public CreditApplication getApplicationStatus(String id) {
        return creditRepository.findById(id);
    }
}