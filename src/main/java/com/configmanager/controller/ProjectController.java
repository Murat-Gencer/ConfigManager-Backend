package com.configmanager.controller;

import com.configmanager.dto.ConfigDTO;
import com.configmanager.dto.CreateProjectRequestDTO;
import com.configmanager.dto.ErrorResponseDTO;
import com.configmanager.dto.ProjectDTO;
import com.configmanager.dto.UpdateProjectRequestDTO;
import com.configmanager.entity.Project;
import com.configmanager.entity.User;
import com.configmanager.entity.Configuration;
import com.configmanager.mapper.DTOMapper;
import com.configmanager.repository.UserRepository;
import com.configmanager.service.ProjectService;
import com.configmanager.service.ConfigurationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

    @Autowired
    private ProjectService projectService;

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

    // 1. Kullanıcının projelerini listele
    @GetMapping
    public ResponseEntity<?> getUserProjects() {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        List<Project> projects = projectService.getProjectsByUser(user);
        List<ProjectDTO> projectDTOs = projects.stream()
                .map(project -> {
                    var apiKey = projectService.getApiKeyByProject(project);
                    return dtoMapper.toProjectDTO(project, apiKey != null ? apiKey.getKey() : null);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDTOs);
    }

    // 2. Yeni proje oluştur
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody CreateProjectRequestDTO request) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUser(user);

        Project savedProject = projectService.save(project);
        var apiKey = projectService.getApiKeyByProject(savedProject);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoMapper.toProjectDTO(savedProject, apiKey != null ? apiKey.getKey() : null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(id, user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Proje bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        var apiKey = projectService.getApiKeyByProject(project);
        return ResponseEntity.ok(dtoMapper.toProjectDTO(project, apiKey != null ? apiKey.getKey() : null));
    }

    // 3. Proje güncelle
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequestDTO request) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(id, user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Proje bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project updatedProject = projectService.save(project);
        return ResponseEntity.ok(dtoMapper.toProjectDTO(updatedProject));
    }

    // 4. Proje sil
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(id, user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Proje bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        projectService.delete(project);
        return ResponseEntity.noContent().build();
    }

    // 5. Projenin config'lerini listele (environment'a göre)
    @GetMapping("/{projectId}/configs")
    public ResponseEntity<?> getProjectConfigs(
            @PathVariable Long projectId,
            @RequestParam(required = false) String environment) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(projectId, user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Proje bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<Configuration> configs;
        if (environment != null) {
            configs = configurationService.getConfigurationsByProjectAndEnvironment(project, environment);
        } else {
            configs = configurationService.getConfigurationsByProject(project);
        }

        List<ConfigDTO> configDTOs = configs.stream()
                .map(dtoMapper::toConfigDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(configDTOs);
    }

    // 6. Projenin environment'larını listele
    @GetMapping("/{projectId}/environments")
    public ResponseEntity<?> getProjectEnvironments(@PathVariable Long projectId) {
        User user = getCurrentUser();
        if (user == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Oturum geçersiz"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Project project = projectService.getProjectByIdAndUser(projectId, user);
        if (project == null) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Proje bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<String> environments = configurationService.getEnvironmentsByProject(project);
        return ResponseEntity.ok(environments);
    }
}