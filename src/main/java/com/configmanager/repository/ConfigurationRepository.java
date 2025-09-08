package com.configmanager.repository;

import com.configmanager.entity.Configuration;
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
    
    void deleteByKeyAndEnvironment(String key, String environment);
}
