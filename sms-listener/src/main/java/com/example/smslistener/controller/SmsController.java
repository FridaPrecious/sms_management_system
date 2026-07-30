package com.example.smslistener.controller;

import com.example.smslistener.dto.SmsDto;
import com.example.smslistener.dto.UploadResponseDto;
import com.example.smslistener.model.SmsRequest;
import com.example.smslistener.repository.SmsRequestRepository;
import com.example.smslistener.service.ExcelReaderService;
import com.example.smslistener.service.PhoneNumberValidationService;
import com.example.smslistener.service.SmsProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsController {

    private final ExcelReaderService excelReaderService;
    private final SmsProcessingService smsProcessingService;
    private final PhoneNumberValidationService validationService;
    private final SmsRequestRepository smsRequestRepository;

    // Constructor Injection - Best Practice
    public SmsController(ExcelReaderService excelReaderService,
                         SmsProcessingService smsProcessingService,
                         PhoneNumberValidationService validationService,
                         SmsRequestRepository smsRequestRepository) {
        this.excelReaderService = excelReaderService;
        this.smsProcessingService = smsProcessingService;
        this.validationService = validationService;
        this.smsRequestRepository = smsRequestRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                UploadResponseDto response = new UploadResponseDto();
                response.setMessage("File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (!excelReaderService.isExcelFile(file)) {
                UploadResponseDto response = new UploadResponseDto();
                response.setMessage("Please upload an Excel file (.xlsx or .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            // Parse Excel file
            List<SmsDto> smsDtos = excelReaderService.readSmsFromExcel(file);

            if (smsDtos.isEmpty()) {
                UploadResponseDto response = new UploadResponseDto();
                response.setMessage("No valid data found in Excel file");
                return ResponseEntity.badRequest().body(response);
            }

            // Process SMS requests (validates and sends to queue)
            List<SmsRequest> processed = smsProcessingService.processSmsRequests(smsDtos);

            // Count results
            long validCount = processed.stream().filter(r -> "VALID".equals(r.getStatus())).count();
            long invalidCount = processed.stream().filter(r -> "INVALID".equals(r.getStatus())).count();

            // Build response
            UploadResponseDto response = new UploadResponseDto();
            response.setTotalRecords(processed.size());
            response.setValidRecords((int) validCount);
            response.setInvalidRecords((int) invalidCount);
            response.setMessage(String.format("Processed %d records: %d valid, %d invalid",
                    processed.size(), validCount, invalidCount));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            UploadResponseDto response = new UploadResponseDto();
            response.setMessage("Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/validate/{phoneNumber}")
    public ResponseEntity<String> validatePhoneNumber(@PathVariable String phoneNumber) {
        boolean isValid = validationService.isValidKenyanNumber(phoneNumber);
        String message = validationService.getValidationMessage(phoneNumber);
        return ResponseEntity.ok(String.format("Number: %s - %s", phoneNumber, message));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<SmsRequest>> getAllRequests() {
        return ResponseEntity.ok(smsRequestRepository.findAll());
    }

    @GetMapping("/requests/status/{status}")
    public ResponseEntity<List<SmsRequest>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(smsRequestRepository.findByStatus(status));
    }

    @GetMapping("/requests/count")
    public ResponseEntity<Long> getTotalRequests() {
        return ResponseEntity.ok(smsRequestRepository.count());
    }

    @GetMapping("/requests/status/{status}/count")
    public ResponseEntity<Long> getCountByStatus(@PathVariable String status) {
        return ResponseEntity.ok(smsRequestRepository.findByStatus(status).stream().count());
    }
}