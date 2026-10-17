package com.taskflow.api.mapper;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.taskflow.api.dto.TaskRequest;
import com.taskflow.api.dto.TaskResponse;
import com.taskflow.api.entity.Task;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskMapper {

	private final Clock clock;

	public TaskResponse toResponse(Task task) {
		boolean overdue = !task.isCompleted() && task.getDueDate() != null
				&& task.getDueDate().isBefore(LocalDate.now(clock));
		return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getPriority(),
				task.getDueDate(), task.isCompleted(), overdue, task.getOwnerId(), task.getOwnerUsername(),
				task.getCreatedAt(), task.getUpdatedAt());
	}

	public void apply(TaskRequest request, Task task) {
		task.setTitle(request.title().trim());
		task.setDescription(
				request.description() == null || request.description().isBlank() ? null : request.description().trim());
		task.setPriority(request.priority());
		task.setDueDate(request.dueDate());
	}
}
