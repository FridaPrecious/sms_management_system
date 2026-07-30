package com.example.smslistener.service;

import com.example.smslistener.dto.SmsDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelReaderService {
    
    public List<SmsDto> readSmsFromExcel(MultipartFile file) throws Exception {
        List<SmsDto> smsList = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            
            // Skip header row (row 0)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                
                try {
                    String phoneNumber = dataFormatter.formatCellValue(row.getCell(0)).trim();
                    String message = dataFormatter.formatCellValue(row.getCell(1)).trim();
                    
                    if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                        smsList.add(new SmsDto(phoneNumber, message));
                    }
                } catch (Exception e) {
                    System.err.println("Error reading row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
        }
        
        return smsList;
    }
    
    public boolean isExcelFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        return fileName.endsWith(".xlsx") || fileName.endsWith(".xls");
    }
}