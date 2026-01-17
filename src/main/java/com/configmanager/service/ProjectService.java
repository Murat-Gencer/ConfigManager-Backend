package com.configmanager.service;

import com.configmanager.entity.Project;
import com.configmanager.entity.User;
import com.configmanager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> getProjectsByUser(User user) {
        return projectRepository.findByUser(user);
    }

    public Project getProjectByIdAndUser(Long id, User user) {
        return projectRepository.findByIdAndUser(id, user).orElse(null);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void delete(Project project) {
        projectRepository.delete(project);
    }

}