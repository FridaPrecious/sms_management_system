package com.example.sms_sender.repository;

import com.example.sms_sender.model.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
    
    // ========== Basic Find Methods ==========
    
    List<SmsLog> findByStatus(String status);
    
    List<SmsLog> findByPhoneNumber(String phoneNumber);
    
    List<SmsLog> findByRequestId(Long requestId);
    
    List<SmsLog> findByPhoneNumberAndStatus(String phoneNumber, String status);
    
    // ========== Date Range Queries ==========
    
    List<SmsLog> findBySentAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<SmsLog> findByStatusAndSentAtBetween(String status, LocalDateTime start, LocalDateTime end);
    
    // ========== Count Methods ==========
    
    long countByStatus(String status);
    
    long countByPhoneNumber(String phoneNumber);
    
    // ========== Statistics Queries ==========
    
    @Query("SELECT COUNT(l) FROM SmsLog l WHERE l.status = :status")
    long countByStatusQuery(@Param("status") String status);
    
    @Query("SELECT (COUNT(CASE WHEN l.status = 'SENT' THEN 1 END) * 100.0 / COUNT(l)) FROM SmsLog l")
    Double getSuccessRate();
    
    @Query("SELECT (COUNT(CASE WHEN l.status = 'SENT' THEN 1 END) * 100.0 / COUNT(l)) FROM SmsLog l WHERE l.phoneNumber = :phoneNumber")
    Double getSuccessRateByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    // ========== Recent Queries ==========
    
    List<SmsLog> findTop10ByOrderBySentAtDesc();
    
    List<SmsLog> findTop5ByPhoneNumberOrderBySentAtDesc(String phoneNumber);
    
    // ========== Cleanup Methods ==========
    
    void deleteBySentAtBefore(LocalDateTime date);
    
    // ========== Advanced Statistics ==========
    
    @Query("SELECT l.status, COUNT(l) FROM SmsLog l GROUP BY l.status")
    List<Object[]> getStatusCounts();
    
    @Query("SELECT l.phoneNumber, COUNT(l) FROM SmsLog l GROUP BY l.phoneNumber ORDER BY COUNT(l) DESC")
    List<Object[]> getTopPhoneNumbers();
    
    @Query("SELECT l.status, COUNT(l) FROM SmsLog l WHERE l.sentAt BETWEEN :start AND :end GROUP BY l.status")
    List<Object[]> getStatusCountsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}