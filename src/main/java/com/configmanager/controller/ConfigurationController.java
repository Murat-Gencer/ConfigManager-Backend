package com.configmanager.controller;

import com.configmanager.dto.ConfigDTO;
import com.configmanager.dto.CreateConfigRequestDTO;
import com.configmanager.dto.UpdateConfigRequestDTO;
import com.configmanager.entity.Configuration;
import com.configmanager.entity.User;
import com.configmanager.mapper.DTOMapper;
import com.configmanager.service.ConfigurationService;
import jakarta.validation.Valid;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConfigurationController {
    
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DTOMapper dtoMapper;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
    
    @GetMapping
    public ResponseEntity<List<ConfigDTO>> getAllConfigurations() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Configuration> configurations = configurationService.getAllConfigurationsByUser(user);
        List<ConfigDTO> configDTOs = configurations.stream()
            .map(dtoMapper::toConfigDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(configDTOs);
    }
    
    @GetMapping("/environments")
    public ResponseEntity<List<String>> getEnvironments() {
        List<String> environments = configurationService.getEnvironments();
        return ResponseEntity.ok(environments);
    }
    
    @GetMapping("/{environment}")
    public ResponseEntity<List<ConfigDTO>> getConfigurationsByEnvironment(@PathVariable String environment) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Configuration> configurations = configurationService.getConfigurationsByEnvironmentAndUser(environment, user);
        List<ConfigDTO> configDTOs = configurations.stream()
            .map(dtoMapper::toConfigDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(configDTOs);
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
    public ResponseEntity<ConfigDTO> getConfiguration(@PathVariable String environment, @PathVariable String key) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<Configuration> configuration = configurationService.getConfigurationByKeyEnvironmentAndUser(key, environment, user);
        return configuration.map(config -> ResponseEntity.ok(dtoMapper.toConfigDTO(config)))
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ConfigDTO> createConfiguration(@Valid @RequestBody CreateConfigRequestDTO configRequest) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        Configuration configuration = dtoMapper.toConfigEntity(configRequest, user);
        
        if (configurationService.existsConfiguration(configuration.getKey(), configuration.getEnvironment(), user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Configuration savedConfig = configurationService.saveConfiguration(configuration);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toConfigDTO(savedConfig));
    }
    
    @PutMapping("/{environment}/{key}")
    public ResponseEntity<ConfigDTO> updateConfiguration(
            @PathVariable String environment,
            @PathVariable String key,
            @Valid @RequestBody UpdateConfigRequestDTO configRequest) {
        try {
            User user = getCurrentUser();
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
            Optional<Configuration> existingConfigOpt = configurationService.getConfigurationByKeyEnvironmentAndUser(key, environment, user);
            if (existingConfigOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Configuration existingConfig = existingConfigOpt.get();
            if (configRequest.getValue() != null) {
                existingConfig.setValue(configRequest.getValue());
            }
            if (configRequest.getDescription() != null) {
                existingConfig.setDescription(configRequest.getDescription());
            }
            
            Configuration updatedConfig = configurationService.saveConfiguration(existingConfig);
            return ResponseEntity.ok(dtoMapper.toConfigDTO(updatedConfig));
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
    public ResponseEntity<List<ConfigDTO>> searchConfigurations(
            @PathVariable String environment,
            @RequestParam String q) {
        List<Configuration> configurations = configurationService.searchConfigurations(environment, q);
        List<ConfigDTO> configDTOs = configurations.stream()
            .map(dtoMapper::toConfigDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(configDTOs);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<?> createConfigurationsBatch(@Valid @RequestBody com.configmanager.dto.BatchConfigRequestDTO request) {
        try {
            User user = getCurrentUser();
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
            List<Configuration> configs = request.getConfigs().entrySet().stream()
                .map(entry -> {
                    Configuration config = new Configuration();
                    config.setKey(entry.getKey());
                    config.setValue(entry.getValue());
                    config.setEnvironment(request.getEnvironment());
                    config.setUser(user);
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
}
