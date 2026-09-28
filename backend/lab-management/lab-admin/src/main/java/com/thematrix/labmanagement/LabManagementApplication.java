package com.thematrix.labmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.thematrix.labmanagement")
@EnableTransactionManagement
public class LabManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(LabManagementApplication.class, args);
    }
}
