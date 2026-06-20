package com.crediguard.core.repository;

import com.crediguard.core.model.CreditApplication;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CreditRepository {

    // 1. We inject Spring's core JDBC tool
    private final JdbcTemplate jdbcTemplate;

    public CreditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 2. We write our own manual save method
    public void save(CreditApplication application) {
        // 3. We write RAW SQL. The ? symbols protect against SQL Injection hackers.
        String sql = "INSERT INTO credit_application (application_id, applicant_name, annual_income, total_debt) VALUES (?, ?, ?, ?)";
        
        // 4. We execute the SQL, mapping our POJO variables exactly to the ? symbols
        jdbcTemplate.update(sql, 
            application.getApplicationId(),
            application.getApplicantName(),
            application.getAnnualIncome(),
            application.getTotalDebt()
        );
        
        System.out.println("JDBC: Raw SQL Insert executed successfully.");
    }
        public CreditApplication findById(String id) {
        // 1. The SQL instruction: "Find the folder where the ID matches"
        String sql = "SELECT * FROM credit_application WHERE application_id = ?";
        
        try {
            // 2. The Armored Car (JdbcTemplate) goes to the vault, gets the row, and maps it back to a Java POJO
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                CreditApplication app = new CreditApplication();
                app.setApplicationId(rs.getString("application_id"));
                app.setApplicantName(rs.getString("applicant_name"));
                app.setAnnualIncome(rs.getDouble("annual_income"));
                app.setTotalDebt(rs.getDouble("total_debt"));
                return app;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            // 3. If the folder is missing, don't crash. Just return nothing.
            return null; 
        }
    }
}