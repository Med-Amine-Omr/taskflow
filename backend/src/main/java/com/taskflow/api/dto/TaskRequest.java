package com.taskflow.api.dto;

import java.time.LocalDate;

import com.taskflow.api.entity.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
        String title,

        String description,

        @NotNull(message = "La priorité est obligatoire")
        TaskPriority priority,

        LocalDate dueDate) {
	
}
