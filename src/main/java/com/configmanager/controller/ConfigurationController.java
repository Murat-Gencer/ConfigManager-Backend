package com.configmanager.controller;

import com.configmanager.dto.BatchConfigRequestDTO;
import com.configmanager.dto.ConfigDTO;
import com.configmanager.dto.CreateConfigRequestDTO;
import com.configmanager.dto.ErrorResponseDTO;
import com.configmanager.entity.Configuration;
import com.configmanager.entity.Project;
import com.configmanager.entity.User;
import com.configmanager.mapper.DTOMapper;
import com.configmanager.service.AuditLogService;
import com.configmanager.service.ConfigurationService;
import com.configmanager.service.ProjectService;

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

import java.util.ArrayList;
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
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DTOMapper dtoMapper;
    
    @Autowired
    private AuditLogService auditLogService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getAllConfigurations() {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
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

    @GetMapping("/{environment}/{projectId}")
    public ResponseEntity<?> getConfigurationsByEnvironment(@PathVariable String environment, @PathVariable Long projectId) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        List<Configuration> configurations = configurationService.getConfigurationsByEnvironmentAndUserAndProjectID(environment,
                user, projectId);
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
        User user = getCurrentUser();
        String envContent = configurationService.generateDotEnvFormat(environment);

        // Audit log - Export işlemi
        if (user != null) {
            auditLogService.createLog(user, "EXPORT_CONFIG", "CONFIGURATION", null, environment, 
                "Environment export edildi: " + environment);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", environment + ".env");

        return new ResponseEntity<>(envContent, headers, HttpStatus.OK);
    }

    @GetMapping("/{environment}/{projectId}/{key}")
    public ResponseEntity<?> getConfiguration(@PathVariable String environment, @PathVariable Long projectId, @PathVariable String key) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Configuration> configuration = configurationService.getConfigurationByKeyEnvironmentAndUser(key,
                environment, user, projectId);
        return configuration.map(config -> ResponseEntity.ok(dtoMapper.toConfigDTO(config)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createConfiguration(@Valid @RequestBody CreateConfigRequestDTO configRequest) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(configRequest.getProjectId(), user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Bu projeye erişim yetkiniz yok"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Varsa güncelle, yoksa yeni oluştur
        Optional<Configuration> existingConfig = configurationService
                .getConfigurationByKeyEnvironmentAndProject(
                        configRequest.getKey(),
                        configRequest.getEnvironment(),
                        project);

        Configuration configuration;
        boolean isUpdate = false;
        if (existingConfig.isPresent()) {
            // Güncelle
            isUpdate = true;
            configuration = existingConfig.get();
            configuration.setValue(configRequest.getValue());
            configuration.setDescription(configRequest.getDescription());
            configuration.setIsEncrypted(configRequest.isSecret());
            configuration.setIsSensitive(configRequest.isSensitive());
        } else {
            // Yeni oluştur
            configuration = dtoMapper.toConfigEntity(configRequest, user , project);
        }

        Configuration savedConfig = configurationService.saveConfiguration(configuration);
        
        // Audit log
        String action = isUpdate ? "UPDATE_CONFIG" : "CREATE_CONFIG";
        String description = isUpdate ? "Konfigürasyon güncellendi" : "Yeni konfigürasyon oluşturuldu";
        auditLogService.createLog(user, action, "CONFIGURATION", savedConfig.getId(), 
            savedConfig.getKey(), description + ": " + savedConfig.getKey() + " (" + savedConfig.getEnvironment() + ")");
        
        return ResponseEntity.ok(dtoMapper.toConfigDTO(savedConfig));
    }


    @DeleteMapping("/{environment}/{id}")
    public ResponseEntity<?> deleteConfiguration(@PathVariable String environment, @PathVariable Long id) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                ErrorResponseDTO error = new ErrorResponseDTO(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "Oturum geçersiz"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            if (!configurationService.existsConfiguration(id, environment, user)) {
                ErrorResponseDTO error = new ErrorResponseDTO(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    "Konfigürasyon bulunamadı"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Audit log için config bilgisini al
            Optional<Configuration> configOpt = configurationService.getConfigurationById(id);
            
            configurationService.deleteConfiguration(id, environment);
            
            // Audit log
            if (configOpt.isPresent()) {
                Configuration config = configOpt.get();
                auditLogService.createLog(user, "DELETE_CONFIG", "CONFIGURATION", id, 
                    config.getKey(), "Konfigürasyon silindi: " + config.getKey() + " (" + environment + ")");
            }
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Silme işlemi sırasında hata oluştu"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
    public ResponseEntity<?> createConfigurationsBatch(
            @Valid @RequestBody BatchConfigRequestDTO batchRequest) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(batchRequest.getProjectId(), user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Bu projeye erişim yetkiniz yok"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<Configuration> savedConfigs = new ArrayList<>();

        for (Map.Entry<String, String> entry : batchRequest.getConfigs().entrySet()) {
            // Varsa güncelle
            Optional<Configuration> existingConfig = configurationService
                    .getConfigurationByKeyEnvironmentAndProject(
                            entry.getKey(),
                            batchRequest.getEnvironment(),
                            project);

            Configuration config;
            if (existingConfig.isPresent()) {
                config = existingConfig.get();
                config.setValue(entry.getValue()); // Sadece value güncelle
            } else {
                config = new Configuration();
                config.setKey(entry.getKey());
                config.setValue(entry.getValue());
                config.setEnvironment(batchRequest.getEnvironment());
                config.setProject(project);
            }

            savedConfigs.add(configurationService.saveConfiguration(config));
        }

        List<ConfigDTO> configDTOs = savedConfigs.stream()
                .map(dtoMapper::toConfigDTO)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(configDTOs);
    }
}
