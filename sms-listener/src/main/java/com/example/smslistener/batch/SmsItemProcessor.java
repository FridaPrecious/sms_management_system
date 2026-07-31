package com.example.smslistener.batch;

import com.example.smslistener.model.SmsRequest;
import com.example.smslistener.service.PhoneNumberValidationService;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SmsItemProcessor implements ItemProcessor<SmsRequest, SmsRequest> {

    @Autowired
    private PhoneNumberValidationService validationService;

    @Override
    public SmsRequest process(SmsRequest request) {
        boolean isValid = validationService.isValidKenyanNumber(request.getPhoneNumber());
        String validationMsg = validationService.getValidationMessage(request.getPhoneNumber());

        request.setValidationMessage(validationMsg);

        if (isValid) {
            request.setStatus("VALID");
        } else {
            request.setStatus("INVALID");
            request.setProcessedAt(LocalDateTime.now());
        }

        System.out.println("Validating: " + request.getPhoneNumber() + " -> " + request.getStatus());
        return request;
    }
}