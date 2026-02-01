package com.configmanager.repository;

import com.configmanager.entity.AuditLog;
import com.configmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Pagination ile tüm audit logları getir
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Kullanıcıya göre pagination
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Tarih aralığına göre
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Kullanıcı + tarih aralığı
    Page<AuditLog> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
        User user,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    // Action'a göre
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    // Resource type'a göre
    Page<AuditLog> findByResourceTypeOrderByCreatedAtDesc(String resourceType, Pageable pageable);
    
    // Karmaşık sorgular için
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(
        @Param("userId") Long userId,
        @Param("action") String action,
        @Param("resourceType") String resourceType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Son N adet log
    List<AuditLog> findTop10ByUserOrderByCreatedAtDesc(User user);
}
