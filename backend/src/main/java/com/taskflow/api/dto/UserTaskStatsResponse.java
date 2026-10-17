package com.taskflow.api.dto;

public record UserTaskStatsResponse(String ownerId, String ownerUsername,
        long total, long completed, long inProgress, long overdue) {
}
