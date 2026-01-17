package com.configmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProjectDTO {
    private Long id; // Create'te null, Update/Response'ta var

    @NotBlank
    private String name;

    private String description;

    private LocalDateTime createdAt; // Sadece response'ta dolu
    private LocalDateTime updatedAt;

}