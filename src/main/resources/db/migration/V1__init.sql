-- V1__create_reports_table.sql
-- Initial migration to create the reports table

CREATE TABLE reports (
     id SERIAL PRIMARY KEY,                         -- Auto-incrementing numeric ID
     reporting_time TIMESTAMP NOT NULL,             -- Timestamp for when the report was made
     reporting_client_id VARCHAR(100) NOT NULL,     -- Client ID associated with the report
     type VARCHAR(50) NOT NULL,                     -- Type of the report
     data JSONB NOT NULL,                           -- Generic map to store dynamic data in JSON format
     contact JSONB NOT NULL,                           -- Generic map to store contact details in JSON format
     status VARCHAR(50) NOT NULL                    -- Status of the report
);

-- Indexes to improve query performance on frequently searched fields
CREATE INDEX idx_reports_type ON reports (type);
CREATE INDEX idx_reports_status ON reports (status);
