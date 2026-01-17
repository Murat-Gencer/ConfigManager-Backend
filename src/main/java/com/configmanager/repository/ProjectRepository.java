package com.configmanager.repository;

import com.configmanager.entity.Project;
import com.configmanager.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String name);
    Optional<Project> findById(Long id);

    boolean existsByName(String name);
    boolean existsById(Long id);
    List<Project> findByUser(User user);
    Optional<Project> findByIdAndUser(Long id, User user);
}
