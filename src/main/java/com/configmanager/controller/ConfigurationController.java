package com.configmanager.controller;

import com.configmanager.entity.Configuration;
import com.configmanager.entity.User;
import com.configmanager.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.configmanager.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConfigurationController {
    
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
    
    @GetMapping
    public ResponseEntity<List<Configuration>> getAllConfigurations() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Configuration> configurations = configurationService.getAllConfigurationsByUser(user);
        return ResponseEntity.ok(configurations);
    }
    
    @GetMapping("/environments")
    public ResponseEntity<List<String>> getEnvironments() {
        List<String> environments = configurationService.getEnvironments();
        return ResponseEntity.ok(environments);
    }
    
    @GetMapping("/{environment}")
    public ResponseEntity<List<Configuration>> getConfigurationsByEnvironment(@PathVariable String environment) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Configuration> configurations = configurationService.getConfigurationsByEnvironmentAndUser(environment, user);
        return ResponseEntity.ok(configurations);
    }
    
    @GetMapping("/{environment}/map")
    public ResponseEntity<Map<String, String>> getConfigurationsAsMap(@PathVariable String environment) {
        Map<String, String> configurations = configurationService.getConfigurationsAsMap(environment);
        return ResponseEntity.ok(configurations);
    }
    
    @GetMapping("/{environment}/export/env")
    public ResponseEntity<String> exportAsDotEnv(@PathVariable String environment) {
        String envContent = configurationService.generateDotEnvFormat(environment);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", environment + ".env");
        
        return new ResponseEntity<>(envContent, headers, HttpStatus.OK);
    }
    
    @GetMapping("/{environment}/{key}")
    public ResponseEntity<Configuration> getConfiguration(@PathVariable String environment, @PathVariable String key) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<Configuration> configuration = configurationService.getConfigurationByKeyEnvironmentAndUser(key, environment, user);
        return configuration.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Configuration> createConfiguration(@RequestBody Configuration configuration) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        configuration.setUser(user);
        if (configurationService.existsConfiguration(configuration.getKey(), configuration.getEnvironment(), user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Configuration savedConfig = configurationService.saveConfiguration(configuration);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedConfig);
    }
    
    @PutMapping("/{environment}/{key}")
    public ResponseEntity<Configuration> updateConfiguration(
            @PathVariable String environment,
            @PathVariable String key,
            @RequestBody Configuration configuration) {
        try {
            Configuration updatedConfig = configurationService.updateConfiguration(key, environment, configuration);
            return ResponseEntity.ok(updatedConfig);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{environment}/{key}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String environment, @PathVariable String key) {
        try {
            User user = getCurrentUser();
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            if (!configurationService.existsConfiguration(key, environment, user)) {
                return ResponseEntity.notFound().build();
            }
            configurationService.deleteConfiguration(key, environment);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{environment}/search")
    public ResponseEntity<List<Configuration>> searchConfigurations(
            @PathVariable String environment,
            @RequestParam String q) {
        List<Configuration> configurations = configurationService.searchConfigurations(environment, q);
        return ResponseEntity.ok(configurations);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<?> createConfigurationsBatch(@RequestBody BatchConfigRequest request) {
        try {
            List<Configuration> configs = request.getConfigs().entrySet().stream()
                .map(entry -> {
                    Configuration config = new Configuration();
                    config.setKey(entry.getKey());
                    config.setValue(entry.getValue());
                    config.setEnvironment(request.getEnvironment());
                    config.setUser(request.getUser()); // User nesnesi frontend'den veya backend'den alınmalı
                    config.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "batch");
                    config.setUpdatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : "batch");
                    return config;
                })
                .toList();
            configurationService.saveAll(configs);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // DTO for batch insert
    class BatchConfigRequest {
        private Map<String, String> configs;
        private String environment;
        private com.configmanager.entity.User user;
        private String createdBy;
        private String updatedBy;
        // getters and setters
        public Map<String, String> getConfigs() { return configs; }
        public void setConfigs(Map<String, String> configs) { this.configs = configs; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public com.configmanager.entity.User getUser() { return user; }
        public void setUser(com.configmanager.entity.User user) { this.user = user; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }
}
