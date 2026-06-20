package com.crediguard.core.controller;

import com.crediguard.core.model.CreditApplication;
import com.crediguard.core.service.CreditService; // Importing the Service

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {

    // 1. Declare the desk for the Loan Officer
    private final CreditService creditService;

    // 2. Dependency Injection: Spring Boot assigns the Service here
    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping("/apply")
    public String receiveApplication(@RequestBody CreditApplication application) {
        
        System.out.println("Receptionist: Handing application to the Service layer...");
        
        // 3. The Receptionist stops doing the math and hands it to the Service!
        return creditService.processApplication(application);
    }
    @GetMapping("/status/{id}")
    public CreditApplication checkStatus(@PathVariable String id) {
        System.out.println("Receptionist: Checking status for ID - " + id);
        return creditService.getApplicationStatus(id);
    }
}