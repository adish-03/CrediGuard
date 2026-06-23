package com.crediguard.core.repository;

import com.crediguard.core.model.CreditApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

@Repository
public class CreditRepository {

    private final JdbcTemplate jdbcTemplate;

    public CreditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(CreditApplication application) {
        // DEBUGGING METAL DETECTOR: Check what the Service layer actually handed us!
        System.out.println("JDBC: Attempting to save folder to the vault...");
        System.out.println("JDBC: The isApproved boolean status is currently: " + application.getIsApproved());

        // UPDATED: Added is_approved to the INSERT statement (Now 7 columns and 7 question marks)
        String sql = "INSERT INTO credit_application (application_id, applicant_name, annual_income, total_debt, risk_score, compliance_note, is_approved) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql, 
            application.getApplicationId(), 
            application.getApplicantName(), 
            application.getAnnualIncome(), 
            application.getTotalDebt(),
            application.getRiskScore(),
            application.getComplianceNote(),
            application.getIsApproved() // NEW
        );
        System.out.println("JDBC: Raw SQL Insert with Boolean Status executed successfully.");
    }

    public CreditApplication findById(String id) {
        String sql = "SELECT * FROM credit_application WHERE application_id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                CreditApplication app = new CreditApplication();
                app.setApplicationId(rs.getString("application_id"));
                app.setApplicantName(rs.getString("applicant_name"));
                app.setAnnualIncome(rs.getDouble("annual_income"));
                app.setTotalDebt(rs.getDouble("total_debt"));
                app.setRiskScore(rs.getDouble("risk_score"));
                app.setComplianceNote(rs.getString("compliance_note"));
                app.setIsApproved(rs.getBoolean("is_approved")); // NEW
                return app;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            return null; 
        }
    }
}