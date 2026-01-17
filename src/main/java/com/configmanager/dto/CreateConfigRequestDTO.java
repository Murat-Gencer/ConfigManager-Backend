package com.configmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateConfigRequestDTO {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Key is required")
    private String key;

    @NotBlank(message = "Value is required")
    private String value;

    private String description;

    private String environment;

    private boolean isSecret;

    private boolean isSensitive;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean isSecret) {
        this.isSecret = isSecret;
    }

    public boolean isSensitive() {
        return isSensitive;
    }

    public void setSensitive(boolean isSensitive) {
        this.isSensitive = isSensitive;
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

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
