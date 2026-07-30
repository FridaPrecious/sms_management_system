package com.example.smslistener.service;

import com.example.smslistener.config.RabbitMQConfig;
import com.example.smslistener.dto.SmsDto;
import com.example.smslistener.model.SmsRequest;
import com.example.smslistener.repository.SmsRequestRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmsProcessingService {
    
    @Autowired
    private SmsRequestRepository smsRequestRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private PhoneNumberValidationService validationService;
    
    // Configuring batch size 
    private static final int BATCH_SIZE = 15;
    
    @Transactional
    public List<SmsRequest> processSmsRequests(List<SmsDto> smsDtos) {
        List<SmsRequest> processedRequests = new ArrayList<>();
        List<SmsDto> validSmsDtos = new ArrayList<>();
        
        System.out.println("=========================================");
        System.out.println("Processing " + smsDtos.size() + " SMS records");
        System.out.println("=========================================");
        
        // First pass: Validate all numbers and save to database
        for (SmsDto dto : smsDtos) {
            SmsRequest request = new SmsRequest();
            request.setPhoneNumber(dto.getPhoneNumber());
            request.setMessage(dto.getMessage());
            
            // Validate phone number
            boolean isValid = validationService.isValidKenyanNumber(dto.getPhoneNumber());
            String validationMsg = validationService.getValidationMessage(dto.getPhoneNumber());
            
            request.setValidationMessage(validationMsg);
            
            System.out.println("Validating: " + dto.getPhoneNumber() + " → " + (isValid ? "VALID" : "INVALID"));
            
            if (isValid) {
                request.setStatus("VALID");
                validSmsDtos.add(dto);  // Store for batching
            } else {
                request.setStatus("INVALID");
                request.setProcessedAt(LocalDateTime.now());
            }
            
            SmsRequest saved = smsRequestRepository.save(request);
            processedRequests.add(saved);
        }
        
        System.out.println("=========================================");
        System.out.println("Validation complete: " + validSmsDtos.size() + " valid, " + 
                          (smsDtos.size() - validSmsDtos.size()) + " invalid");
        System.out.println("=========================================");
        
        // Second pass: Send valid records to queue in batches
        if (!validSmsDtos.isEmpty()) {
            sendToQueueInBatches(validSmsDtos);
        } else {
            System.out.println("No valid records to send to queue");
        }
        
        return processedRequests;
    }
    
    /**
     * Send valid SMS records to RabbitMQ queue in batches
     */
    private void sendToQueueInBatches(List<SmsDto> validSmsDtos) {
        // Create batches
        List<List<SmsDto>> batches = createBatches(validSmsDtos, BATCH_SIZE);
        
        System.out.println("=========================================");
        System.out.println("Creating batches for queuing");
        System.out.println("Total records: " + validSmsDtos.size());
        System.out.println("Batch size: " + BATCH_SIZE);
        System.out.println("Number of batches: " + batches.size());
        System.out.println("=========================================");
        
        // Send each batch to the queue
        int batchNumber = 1;
        for (List<SmsDto> batch : batches) {
            try {
                System.out.println("Sending batch " + batchNumber + " with " + batch.size() + " messages");
                
                // Log each message in the batch
                for (SmsDto dto : batch) {
                    System.out.println("  → " + dto.getPhoneNumber());
                }
                
                // Send the entire batch to RabbitMQ
                rabbitTemplate.convertAndSend(RabbitMQConfig.SMS_QUEUE, batch);
                System.out.println(" Batch " + batchNumber + " sent to RabbitMQ queue successfully");
                
            } catch (Exception e) {
                System.err.println(" Failed to send batch " + batchNumber + ": " + e.getMessage());
                // Update status for failed batch messages
                for (SmsDto dto : batch) {
                    // Find and update the corresponding request in database
                    List<SmsRequest> requests = smsRequestRepository.findByPhoneNumber(dto.getPhoneNumber());
                    for (SmsRequest request : requests) {
                        if ("VALID".equals(request.getStatus())) {
                            request.setStatus("FAILED");
                            request.setValidationMessage("Queue error: " + e.getMessage());
                            request.setProcessedAt(LocalDateTime.now());
                            smsRequestRepository.save(request);
                        }
                    }
                }
            }
            batchNumber++;
        }
        
        System.out.println("=========================================");
        System.out.println("All " + validSmsDtos.size() + " messages sent to queue in " + batches.size() + " batches");
        System.out.println("=========================================");
    }
    
    /**
     * Create batches from the list of SMS DTOs
     */
    private List<List<SmsDto>> createBatches(List<SmsDto> smsDtos, int batchSize) {
        List<List<SmsDto>> batches = new ArrayList<>();
        for (int i = 0; i < smsDtos.size(); i += batchSize) {
            int end = Math.min(i + batchSize, smsDtos.size());
            batches.add(smsDtos.subList(i, end));
        }
        return batches;
    }
    
    /**
     * Alternative: Send individual messages (kept for reference)
     */
    private void sendToQueueIndividually(List<SmsDto> validSmsDtos) {
        System.out.println("Sending individual messages to queue (no batching)");
        for (SmsDto dto : validSmsDtos) {
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.SMS_QUEUE, dto);
                System.out.println("Sent to queue individually: " + dto.getPhoneNumber());
            } catch (Exception e) {
                System.err.println("Failed to send: " + dto.getPhoneNumber() + " - " + e.getMessage());
            }
        }
    }
}