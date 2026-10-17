package com.taskflow.api.repository;

import org.springframework.data.jpa.domain.Specification;

import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.TaskPriority;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TaskSpecifications {

    public static Specification<Task> all() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Task> ownedBy(String ownerId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    public static Specification<Task> hasCompleted(boolean completed) {
        return (root, query, cb) -> cb.equal(root.get("completed"), completed);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }
}
