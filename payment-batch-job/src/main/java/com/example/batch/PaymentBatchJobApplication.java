package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for Payment Batch Job
 * This is a short-lived process that runs maintenance tasks
 */
@SpringBootApplication
@EnableScheduling
public class PaymentBatchJobApplication {

    public static void main(String[] args) {
        // Run the Spring Boot application
        SpringApplication.run(PaymentBatchJobApplication.class, args);
        
        // The job will run via CommandLineRunner and then exit
    }
}
