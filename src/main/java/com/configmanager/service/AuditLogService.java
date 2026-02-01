package com.configmanager.service;

import com.configmanager.dto.AuditLogDTO;
import com.configmanager.dto.AuditLogPageDTO;
import com.configmanager.entity.AuditLog;
import com.configmanager.entity.User;
import com.configmanager.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Audit log kaydı oluştur
     */
    public AuditLog createLog(User user, String action, String resourceType, Long resourceId, String resourceName, String description) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setUsername(user != null ? user.getUsername() : "anonymous");
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setResourceName(resourceName);
        log.setDescription(description);
        log.setStatus("SUCCESS");
        
        // Request bilgilerini al
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Request context yoksa devam et
        }
        
        return auditLogRepository.save(log);
    }

    /**
     * Başarısız işlem için audit log
     */
    public AuditLog createFailureLog(User user, String action, String resourceType, String errorMessage) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setUsername(user != null ? user.getUsername() : "anonymous");
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setStatus("FAILURE");
        log.setErrorMessage(errorMessage);
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return auditLogRepository.save(log);
    }

    /**
     * Tüm logları pagination ile getir
     */
    public AuditLogPageDTO getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return convertToPageDTO(logPage);
    }

    /**
     * Kullanıcıya göre logları getir
     */
    public AuditLogPageDTO getLogsByUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return convertToPageDTO(logPage);
    }

    /**
     * Tarih aralığına göre logları getir
     */
    public AuditLogPageDTO getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
            startDate, endDate, pageable
        );
        return convertToPageDTO(logPage);
    }
    
    /**
     * Kullanıcı ve tarih aralığına göre logları getir
     */
    public AuditLogPageDTO getLogsByDateRangeAndUser(User user, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            user, startDate, endDate, pageable
        );
        return convertToPageDTO(logPage);
    }

    /**
     * Filtrelere göre logları getir
     */
    public AuditLogPageDTO getLogsByFilters(
        Long userId, 
        String action, 
        String resourceType,
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        int page, 
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findByFilters(
            userId, action, resourceType, startDate, endDate, pageable
        );
        return convertToPageDTO(logPage);
    }

    /**
     * Son 10 logu getir
     */
    public List<AuditLogDTO> getRecentLogs(User user) {
        List<AuditLog> logs = auditLogRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * AuditLog -> AuditLogDTO
     */
    private AuditLogDTO convertToDTO(AuditLog log) {
        return AuditLogDTO.builder()
            .id(log.getId())
            .username(log.getUsername())
            .action(log.getAction())
            .resourceType(log.getResourceType())
            .resourceId(log.getResourceId())
            .resourceName(log.getResourceName())
            .description(log.getDescription())
            .ipAddress(log.getIpAddress())
            .status(log.getStatus())
            .errorMessage(log.getErrorMessage())
            .createdAt(log.getCreatedAt())
            .build();
    }

    /**
     * Page<AuditLog> -> AuditLogPageDTO
     */
    private AuditLogPageDTO convertToPageDTO(Page<AuditLog> page) {
        List<AuditLogDTO> content = page.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new AuditLogPageDTO(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.isFirst()
        );
    }

    /**
     * Client IP adresini al
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
