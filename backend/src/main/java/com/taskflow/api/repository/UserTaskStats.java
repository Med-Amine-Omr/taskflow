package com.taskflow.api.repository;

public record UserTaskStats(String ownerId, String ownerUsername, Long total, Long completed, Long overdue) {
}
