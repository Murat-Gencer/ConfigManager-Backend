package com.configmanager.controller;

import com.configmanager.dto.ErrorResponseDTO;
import com.configmanager.entity.ApiKey;
import com.configmanager.entity.Configuration;
import com.configmanager.entity.Project;
import com.configmanager.repository.ApiKeyRepository;
import com.configmanager.service.ConfigurationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicApiController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ConfigurationService configurationService;

    /**
     * Public endpoint to fetch configurations using API key
     * 
     * @param apiKey      The API key for authentication
     * @param environment The environment (development, staging, production, etc.)
     * @return Map of configuration key-value pairs
     */
    @GetMapping("/configs")
    public ResponseEntity<?> getConfigsByApiKey(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestParam String environment) {

        // Validate API key
        Optional<ApiKey> apiKeyEntity = apiKeyRepository.findByKeyAndIsActiveTrue(apiKey);
        
        if (apiKeyEntity.isEmpty()) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Geçersiz veya aktif olmayan API key"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        ApiKey validApiKey = apiKeyEntity.get();
        Project project = validApiKey.getProject();

        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Bu API key ile ilişkili proje bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // Update last used timestamp
        validApiKey.setLastUsed(LocalDateTime.now());
        apiKeyRepository.save(validApiKey);

        // Fetch configurations for the project and environment
        List<Configuration> configurations = configurationService
                .getConfigurationsByProjectAndEnvironment(project, environment);

        // Convert to key-value map
        Map<String, String> configMap = new HashMap<>();
        for (Configuration config : configurations) {
            configMap.put(config.getKey(), config.getValue());
        }

        return ResponseEntity.ok(configMap);
    }

    /**
     * Health check endpoint for API key validation
     * 
     * @param apiKey The API key to validate
     * @return Status of the API key
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateApiKey(@RequestHeader("X-API-Key") String apiKey) {
        Optional<ApiKey> apiKeyEntity = apiKeyRepository.findByKeyAndIsActiveTrue(apiKey);

        if (apiKeyEntity.isEmpty()) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Geçersiz veya aktif olmayan API key"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Map<String, Object> response = new HashMap<>();

        ApiKey validApiKey = apiKeyEntity.get();
        response.put("valid", true);
        response.put("projectName", validApiKey.getProject().getName());
        response.put("keyName", validApiKey.getName());
        
        return ResponseEntity.ok(response);
    }
}
