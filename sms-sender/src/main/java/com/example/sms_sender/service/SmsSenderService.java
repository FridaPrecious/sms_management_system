package com.example.sms_sender.service;

import com.example.sms_sender.dto.SmsDto;
import com.example.sms_sender.model.SmsLog;
import com.example.sms_sender.repository.SmsLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class SmsSenderService {
    
    @Autowired
    private SmsLogRepository smsLogRepository;
    
    private final Random random = new Random();
    
    /**
     * Single entry point for all messages from sms.queue
     * Handles both batch (List<SmsDto>) and individual (SmsDto) messages
     */
    @RabbitListener(queues = "sms.queue")
    public void handleMessage(Object message) {
        System.out.println("=========================================");
        System.out.println("Message received from queue");
        System.out.println("Message type: " + message.getClass().getSimpleName());
        System.out.println("=========================================");
        
        try {
            if (message instanceof List) {
                List<?> list = (List<?>) message;
                if (!list.isEmpty() && list.get(0) instanceof SmsDto) {
                    @SuppressWarnings("unchecked")
                    List<SmsDto> batch = (List<SmsDto>) list;
                    processBatch(batch);
                    return;
                }
            }
            
            if (message instanceof SmsDto) {
                SmsDto smsDto = (SmsDto) message;
                processIndividual(smsDto);
                return;
            }
            
            System.err.println("Unknown message type: " + message.getClass().getName());
            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Process a batch of SMS messages
     */
    private void processBatch(List<SmsDto> batch) {
        System.out.println("Processing BATCH with " + batch.size() + " messages");
        
        int successCount = 0;
        int failCount = 0;
        
        for (SmsDto smsDto : batch) {
            System.out.println("  -> " + smsDto.getPhoneNumber());
            if (processSms(smsDto)) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        System.out.println("Batch complete: " + successCount + " sent, " + failCount + " failed");
    }
    
    /**
     * Process an individual SMS message
     */
    private void processIndividual(SmsDto smsDto) {
        System.out.println("Processing INDIVIDUAL message");
        System.out.println("  Phone: " + smsDto.getPhoneNumber());
        System.out.println("  Message: " + smsDto.getMessage());
        
        boolean processed = processSms(smsDto);
        System.out.println("Individual complete: " + (processed ? "SENT" : "FAILED"));
    }
    
    /**
     * Core SMS processing logic
     * Returns true if successful, false if failed
     */
    private boolean processSms(SmsDto smsDto) {
        try {
            boolean success = simulateSmsDelivery();
            
            SmsLog log = new SmsLog();
            log.setPhoneNumber(smsDto.getPhoneNumber());
            log.setMessage(smsDto.getMessage());
            
            if (success) {
                log.setStatus("SENT");
                System.out.println("  [SUCCESS] SMS sent to: " + smsDto.getPhoneNumber());
            } else {
                log.setStatus("FAILED");
                log.setErrorMessage("Simulated delivery failure");
                System.err.println("  [FAILED] SMS failed for: " + smsDto.getPhoneNumber());
            }
            
            smsLogRepository.save(log);
            System.out.println("  Log saved for: " + smsDto.getPhoneNumber());
            
            return success;
            
        } catch (Exception e) {
            System.err.println("  [ERROR] Processing failed for: " + smsDto.getPhoneNumber());
            
            SmsLog log = new SmsLog();
            log.setPhoneNumber(smsDto.getPhoneNumber());
            log.setMessage(smsDto.getMessage());
            log.setStatus("FAILED");
            log.setErrorMessage("Error: " + e.getMessage());
            smsLogRepository.save(log);
            
            return false;
        }
    }
    
    /**
     * Simulate SMS delivery with 90% success rate
     */
    private boolean simulateSmsDelivery() {
        return random.nextInt(100) < 90;
    }
    
    /**
     * Direct API endpoint for sending SMS
     */
    @Transactional
    public SmsLog sendSmsDirectly(SmsDto smsDto) {
        System.out.println("Direct API request for: " + smsDto.getPhoneNumber());
        
        boolean success = simulateSmsDelivery();
        
        SmsLog log = new SmsLog();
        log.setPhoneNumber(smsDto.getPhoneNumber());
        log.setMessage(smsDto.getMessage());
        
        if (success) {
            log.setStatus("SENT");
            System.out.println("[SUCCESS] Direct SMS sent to: " + smsDto.getPhoneNumber());
        } else {
            log.setStatus("FAILED");
            log.setErrorMessage("Simulated delivery failure");
            System.err.println("[FAILED] Direct SMS failed for: " + smsDto.getPhoneNumber());
        }
        
        return smsLogRepository.save(log);
    }
}