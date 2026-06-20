CREATE TABLE IF NOT EXISTS credit_application (
    application_id VARCHAR(255) PRIMARY KEY,
    applicant_name VARCHAR(255),
    annual_income DOUBLE PRECISION,
    total_debt DOUBLE PRECISION
);