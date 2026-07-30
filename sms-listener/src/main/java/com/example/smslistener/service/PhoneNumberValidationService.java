package com.example.smslistener.service;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class PhoneNumberValidationService {
    
    private static final Pattern KENYAN_PHONE_PATTERN = Pattern.compile("^254[0-9]{9}$");
    
    public boolean isValidKenyanNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // Remove spaces, dashes, plus signs
        String cleaned = phoneNumber.replaceAll("[\\s\\-+]", "");
        
        // Check if it matches Kenyan pattern
        return KENYAN_PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    public String getValidationMessage(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "Phone number is empty";
        }
        
        String cleaned = phoneNumber.replaceAll("[\\s\\-+]", "");
        
        if (!cleaned.startsWith("254")) {
            return "Phone number must start with 254 (Kenyan code)";
        }
        
        if (cleaned.length() != 12) {
            return "Phone number must be exactly 12 digits (254 + 9 digits)";
        }
        
        if (!cleaned.matches("[0-9]+")) {
            return "Phone number contains invalid characters";
        }
        
        return "Valid Kenyan phone number";
    }
}