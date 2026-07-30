package com.example.sms_sender.controller;

import com.example.sms_sender.dto.SmsDto;
import com.example.sms_sender.model.SmsLog;
import com.example.sms_sender.repository.SmsLogRepository;
import com.example.sms_sender.service.SmsSenderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsSenderController {

    private final SmsSenderService smsSenderService;
    private final SmsLogRepository smsLogRepository;

    // Constructor Injection - Best Practice
    public SmsSenderController(SmsSenderService smsSenderService,
                               SmsLogRepository smsLogRepository) {
        this.smsSenderService = smsSenderService;
        this.smsLogRepository = smsLogRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<SmsLog> sendSms(@RequestBody SmsDto smsDto) {
        try {
            SmsLog log = smsSenderService.sendSmsDirectly(smsDto);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            SmsLog log = new SmsLog();
            log.setPhoneNumber(smsDto.getPhoneNumber());
            log.setMessage(smsDto.getMessage());
            log.setStatus("FAILED");
            log.setErrorMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(log);
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<List<SmsLog>> getAllLogs() {
        return ResponseEntity.ok(smsLogRepository.findAll());
    }

    @GetMapping("/logs/status/{status}")
    public ResponseEntity<List<SmsLog>> getLogsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(smsLogRepository.findByStatus(status));
    }

    @GetMapping("/logs/phone/{phoneNumber}")
    public ResponseEntity<List<SmsLog>> getLogsByPhoneNumber(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(smsLogRepository.findByPhoneNumber(phoneNumber));
    }

    @GetMapping("/logs/count")
    public ResponseEntity<Map<String, Long>> getLogsCount() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", smsLogRepository.count());
        counts.put("sent", smsLogRepository.findByStatus("SENT").stream().count());
        counts.put("failed", smsLogRepository.findByStatus("FAILED").stream().count());
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "SMS Sender");
        return ResponseEntity.ok(status);
    }
}