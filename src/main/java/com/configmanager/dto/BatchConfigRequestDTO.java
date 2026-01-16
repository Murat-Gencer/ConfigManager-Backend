package com.configmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public class BatchConfigRequestDTO {
    
    @NotEmpty(message = "Configs map cannot be empty")
    private Map<String, String> configs;
    
    @NotBlank(message = "Environment is required")
    private String environment;
    
    private String createdBy;
    private String updatedBy;

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
