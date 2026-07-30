package com.example.smslistener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@SpringBootApplication
@EnableBatchProcessing
public class SmsListenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsListenerApplication.class, args);
        System.out.println("=========================================");
        System.out.println("SMS LISTENER SERVICE STARTED");
        System.out.println("Port: 8081");
        System.out.println("H2 Console: http://localhost:8081/h2-console");
        System.out.println("=========================================");
    }
}