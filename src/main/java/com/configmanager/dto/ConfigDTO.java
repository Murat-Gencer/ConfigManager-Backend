package com.configmanager.dto;

import java.time.LocalDateTime;

public class ConfigDTO {
    
    private Long id;
    private String key;
    private String value;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConfigDTO() {
    }

    public ConfigDTO(Long id, String key, String value, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
