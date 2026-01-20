package com.configmanager.repository;

import com.configmanager.entity.Configuration;
import com.configmanager.entity.Project;
import com.configmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    List<Configuration> findByEnvironment(String environment);

    List<Configuration> findByEnvironmentOrderByKeyAsc(String environment);

    Optional<Configuration> findByKeyAndEnvironment(String key, String environment);

    @Query("SELECT DISTINCT c.environment FROM Configuration c ORDER BY c.environment")
    List<String> findDistinctEnvironments();

    @Query("SELECT c FROM Configuration c WHERE c.environment = :environment AND c.key LIKE %:searchTerm%")
    List<Configuration> findByEnvironmentAndKeyContaining(@Param("environment") String environment,
            @Param("searchTerm") String searchTerm);

    boolean existsByKeyAndEnvironment(String key, String environment);

    void deleteByIdAndEnvironment(Long id, String environment);

    List<Configuration> findByUser(User user);

    List<Configuration> findByEnvironmentAndUserOrderByKeyAsc(String environment, User user);

    Optional<Configuration> findByKeyAndEnvironmentAndUserAndProjectId(String key, String environment, User user , Long projectId);

    boolean existsByKeyAndEnvironmentAndUser(String key, String environment, User user);
    boolean existsByIdAndEnvironmentAndUser(Long id, String environment, User user);

    Optional<Configuration> findByKeyAndEnvironmentAndProject(String key, String environment, Project project);

    List<Configuration> findByEnvironmentAndUserAndProjectIdOrderByKeyAsc(String environment, User user, Long projectId);

    List<Configuration> findByProject(Project project);

    List<Configuration> findByProjectAndEnvironment(Project project, String environment);

    @Query("SELECT DISTINCT c.environment FROM Configuration c WHERE c.project = :project")
    List<String> findDistinctEnvironmentsByProject(@Param("project") Project project);
}
