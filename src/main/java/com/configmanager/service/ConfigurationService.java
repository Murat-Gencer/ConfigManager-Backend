package com.configmanager.service;

import com.configmanager.entity.Configuration;
import com.configmanager.entity.Project;
import com.configmanager.entity.User;
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

    public List<Configuration> saveAll(List<Configuration> configs) {
        return configurationRepository.saveAll(configs);
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

    public List<Configuration> getAllConfigurationsByUser(User user) {
        return configurationRepository.findByUser(user);
    }

    public List<Configuration> getConfigurationsByEnvironmentAndUser(String environment, User user) {
        return configurationRepository.findByEnvironmentAndUserOrderByKeyAsc(environment, user);
    }

    public Optional<Configuration> getConfigurationByKeyEnvironmentAndUser(String key, String environment, User user) {
        return configurationRepository.findByKeyAndEnvironmentAndUser(key, environment, user);
    }

    public boolean existsConfiguration(String key, String environment, User user) {
        return configurationRepository.existsByKeyAndEnvironmentAndUser(key, environment, user);
    }

    public Map<String, String> getConfigurationsAsMap(String environment) {
        return getConfigurationsByEnvironment(environment)
                .stream()
                .collect(Collectors.toMap(
                        Configuration::getKey,
                        config -> config.getIsSensitive() ? "***SENSITIVE***" : config.getValue()));
    }

    public Map<String, String> getConfigurationsAsMapWithSensitive(String environment) {
        return getConfigurationsByEnvironment(environment)
                .stream()
                .collect(Collectors.toMap(
                        Configuration::getKey,
                        Configuration::getValue));
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

    public List<Configuration> getConfigurationsByProject(Project project) {
        return configurationRepository.findByProject(project);
    }

    public List<Configuration> getConfigurationsByProjectAndEnvironment(Project project, String environment) {
        return configurationRepository.findByProjectAndEnvironment(project, environment);
    }

    public List<String> getEnvironmentsByProject(Project project) {
        return configurationRepository.findDistinctEnvironmentsByProject(project);
    }

    public Optional<Configuration> getConfigurationByKeyEnvironmentAndProject(String key, String environment,
            Project project) {
        return configurationRepository.findByKeyAndEnvironmentAndProject(key, environment, project);
    }
}
