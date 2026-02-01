package com.configmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_action", columnList = "action")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "action", nullable = false)
    private String action; // LOGIN, CREATE_CONFIG, UPDATE_CONFIG, DELETE_CONFIG, EXPORT_CONFIG, CREATE_PROJECT, etc.
    
    @Column(name = "resource_type")
    private String resourceType; // PROJECT, CONFIGURATION, USER
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(name = "resource_name")
    private String resourceName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "status")
    private String status; // SUCCESS, FAILURE
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Custom constructor for easier log creation
    public AuditLog(User user, String action, String resourceType, Long resourceId, String resourceName) {
        this.user = user;
        this.username = user != null ? user.getUsername() : null;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.status = "SUCCESS";
    }
}
