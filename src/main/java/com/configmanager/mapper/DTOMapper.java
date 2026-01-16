package com.configmanager.mapper;

import com.configmanager.dto.ConfigDTO;
import com.configmanager.dto.CreateConfigRequestDTO;
import com.configmanager.dto.LoginResponseDTO;
import com.configmanager.entity.Configuration;
import com.configmanager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DTOMapper {
    
    public LoginResponseDTO toLoginResponseDTO(User user, String token) {
        return new LoginResponseDTO(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }
    
    public ConfigDTO toConfigDTO(Configuration config) {
        return new ConfigDTO(
            config.getId(),
            config.getKey(),
            config.getValue(),
            config.getDescription(),
            config.getCreatedAt(),
            config.getUpdatedAt()
        );
    }
    
    public Configuration toConfigEntity(CreateConfigRequestDTO dto, User user) {
        Configuration config = new Configuration();
        config.setKey(dto.getKey());
        config.setValue(dto.getValue());
        config.setDescription(dto.getDescription());
        config.setEnvironment(dto.getEnvironment());
        config.setUser(user);
        return config;
    }
}
