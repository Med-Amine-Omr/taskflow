package com.taskflow.api.support;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.taskflow.api.dto.PageResponse;
import com.taskflow.api.dto.TaskResponse;
import com.taskflow.api.entity.TaskPriority;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TestData {

    public static TaskResponse task(UUID id, String title) {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        return new TaskResponse(id, title, null, TaskPriority.HIGH, null,
                false, false, TestJwts.ALICE_ID, "alice", now, now);
    }

    public static PageResponse<TaskResponse> emptyPage() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0);
    }
}
