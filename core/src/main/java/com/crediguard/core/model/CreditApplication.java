package com.crediguard.core.model;

import java.util.UUID; // Import standard Java UUID

// No more @Entity! Just a pure POJO.
public class CreditApplication {
    
    private String applicationId;
    private String applicantName;
    private double annualIncome;
    private double totalDebt;

    // Constructor to generate the ID automatically when created
    public CreditApplication() {
        this.applicationId = UUID.randomUUID().toString();
    }

    // ... KEEP ALL YOUR GETTERS AND SETTERS BELOW THIS LINE ...

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(double annualIncome) { this.annualIncome = annualIncome; }

    public double getTotalDebt() { return totalDebt; }
    public void setTotalDebt(double totalDebt) { this.totalDebt = totalDebt; }
}