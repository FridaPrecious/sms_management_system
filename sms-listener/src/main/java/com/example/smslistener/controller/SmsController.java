package com.example.smslistener.controller;

import com.example.smslistener.dto.SmsDto;
import com.example.smslistener.dto.UploadResponseDto;
import com.example.smslistener.model.SmsRequest;
import com.example.smslistener.repository.SmsRequestRepository;
import com.example.smslistener.service.ExcelReaderService;
import com.example.smslistener.service.PhoneNumberValidationService;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsController {

    private final ExcelReaderService excelReaderService;
    private final PhoneNumberValidationService validationService;
    private final SmsRequestRepository smsRequestRepository;
    private final JobLauncher jobLauncher;
    private final Job smsProcessingJob;

    public SmsController(ExcelReaderService excelReaderService,
                         PhoneNumberValidationService validationService,
                         SmsRequestRepository smsRequestRepository,
                         JobLauncher jobLauncher,
                         Job smsProcessingJob) {
        this.excelReaderService = excelReaderService;
        this.validationService = validationService;
        this.smsRequestRepository = smsRequestRepository;
        this.jobLauncher = jobLauncher;
        this.smsProcessingJob = smsProcessingJob;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try {
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

            List<SmsDto> smsDtos = excelReaderService.readSmsFromExcel(file);

            if (smsDtos.isEmpty()) {
                UploadResponseDto response = new UploadResponseDto();
                response.setMessage("No valid data found in Excel file");
                return ResponseEntity.badRequest().body(response);
            }

            // Stage every row as PENDING - the batch job's reader will pick these up
            List<Long> stagedIds = new ArrayList<>();
            for (SmsDto dto : smsDtos) {
                SmsRequest request = new SmsRequest();
                request.setPhoneNumber(dto.getPhoneNumber());
                request.setMessage(dto.getMessage());
                SmsRequest saved = smsRequestRepository.save(request);
                stagedIds.add(saved.getId());
            }

            var jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(smsProcessingJob, jobParameters);

            // Re-read just this upload's records to report accurate counts
            List<SmsRequest> processed = smsRequestRepository.findAllById(stagedIds);
            long validCount = processed.stream().filter(r -> "VALID".equals(r.getStatus())).count();
            long invalidCount = processed.stream().filter(r -> "INVALID".equals(r.getStatus())).count();

            UploadResponseDto response = new UploadResponseDto();
            response.setTotalRecords(processed.size());
            response.setValidRecords((int) validCount);
            response.setInvalidRecords((int) invalidCount);
            response.setMessage(String.format("Job %d (%s): %d records - %d valid, %d invalid",
                    execution.getId(), execution.getStatus(), processed.size(), validCount, invalidCount));

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