package com.example.smslistener.batch;

import com.example.smslistener.config.RabbitMQConfig;
import com.example.smslistener.dto.SmsDto;
import com.example.smslistener.model.SmsRequest;
import com.example.smslistener.repository.SmsRequestRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SmsItemWriter implements ItemWriter<SmsRequest> {

    @Autowired
    private SmsRequestRepository smsRequestRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void write(Chunk<? extends SmsRequest> chunk) {
        List<SmsRequest> items = new ArrayList<>(chunk.getItems());

        // Persist the whole chunk (valid + invalid) in one go
        smsRequestRepository.saveAll(items);

        // Build the batch of valid records to push to RabbitMQ
        List<SmsDto> validBatch = new ArrayList<>();
        for (SmsRequest request : items) {
            if ("VALID".equals(request.getStatus())) {
                SmsDto dto = new SmsDto(request.getPhoneNumber(), request.getMessage());
                validBatch.add(dto);
            }
        }

        if (validBatch.isEmpty()) {
            System.out.println("Chunk of " + items.size() + " had no valid records to queue");
            return;
        }

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.SMS_QUEUE, validBatch);
            System.out.println("Sent chunk of " + validBatch.size() + " valid messages to RabbitMQ");
        } catch (Exception e) {
            System.err.println("Failed to send chunk to RabbitMQ: " + e.getMessage());
            for (SmsRequest request : items) {
                if ("VALID".equals(request.getStatus())) {
                    request.setStatus("FAILED");
                    request.setValidationMessage("Queue error: " + e.getMessage());
                    request.setProcessedAt(LocalDateTime.now());
                }
            }
            smsRequestRepository.saveAll(items);
        }
    }
}