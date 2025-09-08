package com.configmanager.service;

import com.configmanager.entity.Configuration;
import com.configmanager.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigurationService {
    
    @Autowired
    private ConfigurationRepository configurationRepository;
    
    public List<Configuration> getAllConfigurations() {
        return configurationRepository.findAll();
    }
    
    public List<Configuration> getConfigurationsByEnvironment(String environment) {
        return configurationRepository.findByEnvironmentOrderByKeyAsc(environment);
    }
    
    public Optional<Configuration> getConfiguration(String key, String environment) {
        return configurationRepository.findByKeyAndEnvironment(key, environment);
    }
    
    public Configuration saveConfiguration(Configuration configuration) {
        return configurationRepository.save(configuration);
    }
    
    public Configuration updateConfiguration(String key, String environment, Configuration updatedConfig) {
        Optional<Configuration> existingConfig = configurationRepository.findByKeyAndEnvironment(key, environment);
        
        if (existingConfig.isPresent()) {
            Configuration config = existingConfig.get();
            config.setValue(updatedConfig.getValue());
            config.setDescription(updatedConfig.getDescription());
            config.setIsSensitive(updatedConfig.getIsSensitive());
            config.setIsEncrypted(updatedConfig.getIsEncrypted());
            config.setUpdatedBy(updatedConfig.getUpdatedBy());
            return configurationRepository.save(config);
        } else {
            throw new RuntimeException("Configuration not found: " + key + " in " + environment);
        }
    }
    
    public void deleteConfiguration(String key, String environment) {
        configurationRepository.deleteByKeyAndEnvironment(key, environment);
    }
    
    public List<String> getEnvironments() {
        return configurationRepository.findDistinctEnvironments();
    }
    
    public List<Configuration> searchConfigurations(String environment, String searchTerm) {
        return configurationRepository.findByEnvironmentAndKeyContaining(environment, searchTerm);
    }
    
    public boolean existsConfiguration(String key, String environment) {
        return configurationRepository.existsByKeyAndEnvironment(key, environment);
    }
    
    public Map<String, String> getConfigurationsAsMap(String environment) {
        return getConfigurationsByEnvironment(environment)
                .stream()
                .collect(Collectors.toMap(
                    Configuration::getKey,
                    config -> config.getIsSensitive() ? "***SENSITIVE***" : config.getValue()
                ));
    }
    
    public Map<String, String> getConfigurationsAsMapWithSensitive(String environment) {
        return getConfigurationsByEnvironment(environment)
                .stream()
                .collect(Collectors.toMap(
                    Configuration::getKey,
                    Configuration::getValue
                ));
    }
    
    public String generateDotEnvFormat(String environment) {
        List<Configuration> configs = getConfigurationsByEnvironment(environment);
        StringBuilder envContent = new StringBuilder();
        
        envContent.append("# Environment: ").append(environment).append("\n");
        envContent.append("# Generated on: ").append(java.time.LocalDateTime.now()).append("\n\n");
        
        for (Configuration config : configs) {
            if (config.getDescription() != null && !config.getDescription().isEmpty()) {
                envContent.append("# ").append(config.getDescription()).append("\n");
            }
            envContent.append(config.getKey()).append("=").append(config.getValue()).append("\n");
        }
        
        return envContent.toString();
    }
}
