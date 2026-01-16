package com.configmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateConfigRequestDTO {
    
    @NotBlank(message = "Key is required")
    private String key;
    
    @NotBlank(message = "Value is required")
    private String value;
    
    private String description;
    
    private String environment;

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
