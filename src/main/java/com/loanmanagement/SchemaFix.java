package com.loanmanagement;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaFix {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixSchema() {
        // Fix users.role column
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(50)");
            System.out.println("SCHEMA FIXED: users.role modified to VARCHAR(50)");
        } catch(Exception e) {
            System.out.println("SCHEMA FIX (users.role): " + e.getMessage());
        }
        // Fix loans.status column
        try {
            jdbcTemplate.execute("ALTER TABLE loans MODIFY COLUMN status VARCHAR(50)");
            System.out.println("SCHEMA FIXED: loans.status modified to VARCHAR(50)");
        } catch(Exception e) {
            System.out.println("SCHEMA FIX (loans.status): " + e.getMessage());
        }
        // Fix loans.purpose column
        try {
            jdbcTemplate.execute("ALTER TABLE loans MODIFY COLUMN purpose VARCHAR(100)");
            System.out.println("SCHEMA FIXED: loans.purpose modified to VARCHAR(100)");
        } catch(Exception e) {
            System.out.println("SCHEMA FIX (loans.purpose): " + e.getMessage());
        }
        // Fix audit_logs columns
        try {
            jdbcTemplate.execute("ALTER TABLE audit_logs MODIFY COLUMN action VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE audit_logs MODIFY COLUMN ip_address VARCHAR(100)");
            jdbcTemplate.execute("ALTER TABLE audit_logs MODIFY COLUMN user_email VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE audit_logs MODIFY COLUMN details TEXT");
            System.out.println("SCHEMA FIXED: audit_logs columns widened");
        } catch(Exception e) {
            System.out.println("SCHEMA FIX (audit_logs): " + e.getMessage());
        }
    }
}
