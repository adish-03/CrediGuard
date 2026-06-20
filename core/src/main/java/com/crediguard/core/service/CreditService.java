package com.crediguard.core.service;

import com.crediguard.core.model.CreditApplication;
import com.crediguard.core.repository.CreditRepository; // Import the repository
import org.springframework.stereotype.Service;

@Service
public class CreditService {

    // 1. Declare the empty desk for the Vault Manager
    private final CreditRepository creditRepository;

    // 2. Constructor Injection: Spring Boot automatically assigns the Vault Manager
    public CreditService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    public String processApplication(CreditApplication application) {
        
        double debtToIncomeRatio = application.getTotalDebt() / application.getAnnualIncome();
        
        if (debtToIncomeRatio > 0.40) {
            return "Application " + application.getApplicationId() + " DENIED. Debt ratio too high.";
        } else {
            // Using the official JPA save method
            creditRepository.save(application);
            return "Application " + application.getApplicationId() + " APPROVED and SENT TO VAULT.";
        }
    }
    public CreditApplication getApplicationStatus(String id) {
        return creditRepository.findById(id);
    }
}