package com.configmanager.mapper;

import com.configmanager.dto.ConfigDTO;
import com.configmanager.dto.CreateConfigRequestDTO;
import com.configmanager.dto.LoginResponseDTO;
import com.configmanager.dto.ProjectDTO;
import com.configmanager.entity.Configuration;
import com.configmanager.entity.Project;
import com.configmanager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DTOMapper {

    public LoginResponseDTO toLoginResponseDTO(User user, String token) {
        return new LoginResponseDTO(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail());
    }

    public ConfigDTO toConfigDTO(Configuration config) {
        return ConfigDTO.builder()
                .id(config.getId())
                .key(config.getKey())
                .value(config.getValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
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

    public ProjectDTO toProjectDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
