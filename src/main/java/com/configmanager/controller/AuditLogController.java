package com.configmanager.controller;

import com.configmanager.dto.AuditLogPageDTO;
import com.configmanager.dto.ErrorResponseDTO;
import com.configmanager.entity.User;
import com.configmanager.repository.UserRepository;
import com.configmanager.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Kullanıcının kendi audit loglarını getir (pagination)
     * GET /api/audit-logs?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<?> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Sadece kullanıcının kendi logları
        AuditLogPageDTO logs = auditLogService.getLogsByUser(user, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Kullanıcının kendi loglarını getir
     * GET /api/audit-logs/my?page=0&size=20
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        AuditLogPageDTO logs = auditLogService.getLogsByUser(user, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Tarih aralığına göre logları getir
     * GET /api/audit-logs/date-range?startDate=2026-01-01T00:00:00&endDate=2026-01-31T23:59:59&page=0&size=20
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Sadece kullanıcının kendi logları
        AuditLogPageDTO logs = auditLogService.getLogsByDateRangeAndUser(user, startDate, endDate, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Filtrelere göre logları getir
     * GET /api/audit-logs/filter?userId=1&action=LOGIN&resourceType=USER&startDate=...&endDate=...&page=0&size=20
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getLogsByFilters(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Sadece kullanıcının kendi logları - userId parametresini override et
        AuditLogPageDTO logs = auditLogService.getLogsByFilters(
            user.getId(), action, resourceType, startDate, endDate, page, size
        );
        return ResponseEntity.ok(logs);
    }

    /**
     * Son işlemler (son 10)
     * GET /api/audit-logs/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentLogs() {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        return ResponseEntity.ok(auditLogService.getRecentLogs(user));
    }
}
