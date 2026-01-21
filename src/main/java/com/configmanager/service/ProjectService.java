package com.configmanager.service;

import com.configmanager.entity.ApiKey;
import com.configmanager.entity.Project;
import com.configmanager.entity.User;
import com.configmanager.repository.ApiKeyRepository;
import com.configmanager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public List<Project> getProjectsByUser(User user) {
        return projectRepository.findByUser(user);
    }

    public Project getProjectByIdAndUser(Long id, User user) {
        return projectRepository.findByIdAndUser(id, user).orElse(null);
    }

    public Project save(Project project) {
        boolean isNewProject = (project.getId() == null);
        Project savedProject = projectRepository.save(project);
        
        // Proje kaydedildikten sonra otomatik olarak API key oluştur
        if (isNewProject) { // Yeni proje ise
            generateApiKeyForProject(savedProject);
        }
        
        return savedProject;
    }

    public void delete(Project project) {
        projectRepository.delete(project);
    }
    
    private void generateApiKeyForProject(Project project) {
        String uniqueKey = "pk_" + UUID.randomUUID().toString().replace("-", "");
        
        ApiKey apiKey = new ApiKey();
        apiKey.setName(project.getName() + " API Key");
        apiKey.setKey(uniqueKey);
        apiKey.setUser(project.getUser());
        apiKey.setProject(project);
        apiKey.setIsActive(true);
        apiKey.setDescription("Auto-generated API key for project: " + project.getName());
        
        apiKeyRepository.save(apiKey);
    }
    
    public ApiKey getApiKeyByProject(Project project) {
        return apiKeyRepository.findByProject(project).orElse(null);
    }

}