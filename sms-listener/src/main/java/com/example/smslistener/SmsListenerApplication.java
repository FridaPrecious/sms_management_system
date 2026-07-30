package com.example.smslistener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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