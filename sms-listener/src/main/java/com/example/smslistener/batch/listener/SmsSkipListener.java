package com.example.smslistener.batch.listener;

import com.example.smslistener.model.SmsRequest;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class SmsSkipListener implements SkipListener<SmsRequest, SmsRequest> {

    @Override
    public void onSkipInProcess(SmsRequest item, Throwable t) {
        System.err.println("Skipped during processing: " + item.getPhoneNumber() + " - " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(SmsRequest item, Throwable t) {
        System.err.println("Skipped during write: " + item.getPhoneNumber() + " - " + t.getMessage());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        System.err.println("Skipped during read: " + t.getMessage());
    }
}