package com.configmanager.controller;

import com.configmanager.entity.Configuration;
import com.configmanager.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConfigurationController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    @GetMapping
    public ResponseEntity<List<Configuration>> getAllConfigurations() {
        List<Configuration> configurations = configurationService.getAllConfigurations();
        return ResponseEntity.ok(configurations);
    }
    
    @GetMapping("/environments")
    public ResponseEntity<List<String>> getEnvironments() {
        List<String> environments = configurationService.getEnvironments();
        return ResponseEntity.ok(environments);
    }
    
    @GetMapping("/{environment}")
    public ResponseEntity<List<Configuration>> getConfigurationsByEnvironment(@PathVariable String environment) {
        List<Configuration> configurations = configurationService.getConfigurationsByEnvironment(environment);
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
        Optional<Configuration> configuration = configurationService.getConfiguration(key, environment);
        return configuration.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Configuration> createConfiguration(@RequestBody Configuration configuration) {
        try {
            if (configurationService.existsConfiguration(configuration.getKey(), configuration.getEnvironment())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            Configuration savedConfig = configurationService.saveConfiguration(configuration);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedConfig);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
            if (!configurationService.existsConfiguration(key, environment)) {
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
}
