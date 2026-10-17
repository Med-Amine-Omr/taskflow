package com.taskflow.api.dto;

public record TaskStatsResponse(long completed, long inProgress, long overdue, long total) {
}
