package com.crediguard.core.model;

import java.util.UUID;

public class CreditApplication {
    private String applicationId;
    private String applicantName;
    private Double annualIncome;
    private Double totalDebt;
    private Double riskScore;
    private String complianceNote;
    private Integer dependents;
    private Double loanAmount;

    
    // NEW: Strict Enterprise Boolean status (True = Approved, False = Denied)
    private Boolean isApproved;

    public CreditApplication() {
        this.applicationId = UUID.randomUUID().toString();
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public Double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Double annualIncome) { this.annualIncome = annualIncome; }

    public Double getTotalDebt() { return totalDebt; }
    public void setTotalDebt(Double totalDebt) { this.totalDebt = totalDebt; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getComplianceNote() { return complianceNote; }
    public void setComplianceNote(String complianceNote) { this.complianceNote = complianceNote; }

    // NEW Getter and Setter for the Boolean Status
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }

    public Integer getDependents() { return dependents; }
    public void setDependents(Integer dependents) { this.dependents = dependents; }

    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }
}