package com.configmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigDTO {
    private Long id;
    private String key;
    private String value;
    private String description;
    private String environment;
    private Boolean isEncrypted;
    private Boolean isSensitive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long projectId;
    private String projectName;

}