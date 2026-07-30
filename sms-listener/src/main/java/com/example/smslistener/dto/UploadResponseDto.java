package com.example.smslistener.dto;

import java.util.List;

public class UploadResponseDto {
    private int totalRecords;
    private int validRecords;
    private int invalidRecords;
    private List<String> invalidPhoneNumbers;
    private String message;
    private int batchesQueued;
    
    public UploadResponseDto() {}
    
    public int getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public int getValidRecords() {
        return validRecords;
    }
    
    public void setValidRecords(int validRecords) {
        this.validRecords = validRecords;
    }
    
    public int getInvalidRecords() {
        return invalidRecords;
    }
    
    public void setInvalidRecords(int invalidRecords) {
        this.invalidRecords = invalidRecords;
    }
    
    public List<String> getInvalidPhoneNumbers() {
        return invalidPhoneNumbers;
    }
    
    public void setInvalidPhoneNumbers(List<String> invalidPhoneNumbers) {
        this.invalidPhoneNumbers = invalidPhoneNumbers;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getBatchesQueued() {
        return batchesQueued;
    }
    
    public void setBatchesQueued(int batchesQueued) {
        this.batchesQueued = batchesQueued;
    }
}