package com.configmanager.service;

import com.configmanager.entity.Configuration;
import com.configmanager.entity.User;
import com.configmanager.repository.ConfigurationRepository;
import com.configmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User dummyUser = new User();
            dummyUser.setUsername("testUser");
            dummyUser.setEmail("testUser@example.com");
            dummyUser.setPassword(passwordEncoder.encode("test123"));
            dummyUser.setFullName("Test User");
            userRepository.save(dummyUser);
        }
        // if (configurationRepository.count() == 0) {
        //     loadSampleData();
        // }
    }
    
    private void loadSampleData() {
        // // Development environment configs
        // createConfig("DATABASE_URL", "jdbc:postgresql://localhost:5432/myapp_dev", "development", 
        //             "Database connection URL for development environment", false, false);
        // createConfig("API_KEY", "dev_api_key_12345", "development", 
        //             "API key for external services", true, false);
        // createConfig("DEBUG_MODE", "true", "development", 
        //             "Enable debug mode for development", false, false);
        // createConfig("REDIS_URL", "redis://localhost:6379", "development", 
        //             "Redis connection URL", false, false);
        
        // // Staging environment configs
        // createConfig("DATABASE_URL", "jdbc:postgresql://staging-db:5432/myapp_staging", "staging", 
        //             "Database connection URL for staging environment", false, false);
        // createConfig("API_KEY", "staging_api_key_67890", "staging", 
        //             "API key for external services", true, false);
        // createConfig("DEBUG_MODE", "false", "staging", 
        //             "Debug mode disabled for staging", false, false);
        // createConfig("REDIS_URL", "redis://staging-redis:6379", "staging", 
        //             "Redis connection URL for staging", false, false);
        
        // // Production environment configs
        // createConfig("DATABASE_URL", "jdbc:postgresql://prod-db:5432/myapp_prod", "production", 
        //             "Database connection URL for production environment", false, false);
        // createConfig("API_KEY", "prod_api_key_secret", "production", 
        //             "API key for external services", true, true);
        // createConfig("DEBUG_MODE", "false", "production", 
        //             "Debug mode disabled for production", false, false);
        // createConfig("REDIS_URL", "redis://prod-redis:6379", "production", 
        //             "Redis connection URL for production", false, false);
        // createConfig("JWT_SECRET", "super_secret_jwt_key_production", "production", 
        //             "JWT secret key for authentication", true, true);
        
        System.out.println("Sample configuration data loaded successfully!");
    }
    
    private void createConfig(String key, String value, String environment, 
                            String description, boolean isSensitive, boolean isEncrypted) {
        Configuration config = new Configuration();
        config.setKey(key);
        config.setValue(value);
        config.setEnvironment(environment);
        config.setDescription(description);
        config.setIsSensitive(isSensitive);
        config.setIsEncrypted(isEncrypted);
        config.setCreatedBy("system");
        config.setUpdatedBy("system");
        
        configurationRepository.save(config);
    }
}
