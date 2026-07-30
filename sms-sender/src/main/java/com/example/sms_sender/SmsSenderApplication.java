package com.example.sms_sender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmsSenderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsSenderApplication.class, args);
        System.out.println("=============================");
        System.out.println("SMS SENDER SERVICE STARTED");
        System.out.println("Port: 8082");
        System.out.println("=============================");
    }
}