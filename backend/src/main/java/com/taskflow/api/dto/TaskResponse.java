package com.taskflow.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.taskflow.api.entity.TaskPriority;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate,
        boolean completed,
        boolean overdue,
        String ownerId,
        String ownerUsername,
        Instant createdAt,
        Instant updatedAt) {
	
}
