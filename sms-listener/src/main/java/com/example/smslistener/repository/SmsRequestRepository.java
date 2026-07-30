package com.example.smslistener.repository;

import com.example.smslistener.model.SmsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest, Long> {
    List<SmsRequest> findByStatus(String status);
    List<SmsRequest> findByPhoneNumber(String phoneNumber);

    // Used by the Spring Batch ItemReader to page through PENDING records
    Page<SmsRequest> findByStatus(String status, Pageable pageable);
}