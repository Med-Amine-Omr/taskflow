package com.taskflow.api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.api.dto.PageResponse;
import com.taskflow.api.dto.TaskRequest;
import com.taskflow.api.dto.TaskResponse;
import com.taskflow.api.dto.TaskStatsResponse;
import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.TaskPriority;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.mapper.TaskMapper;
import com.taskflow.api.repository.TaskRepository;
import com.taskflow.api.repository.TaskSpecifications;
import com.taskflow.api.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

	private final TaskRepository taskRepository;
	private final TaskMapper taskMapper;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<TaskResponse> list(AuthenticatedUser user, Boolean completed, TaskPriority priority,
			PageRequest pageRequest) {
		Specification<Task> spec = TaskSpecifications.ownedBy(user.id());
		if (completed != null) {
			spec = spec.and(TaskSpecifications.hasCompleted(completed));
		}
		if (priority != null) {
			spec = spec.and(TaskSpecifications.hasPriority(priority));
		}
		return PageResponse.from(taskRepository.findAll(spec, pageRequest).map(taskMapper::toResponse));
	}

	@Transactional(readOnly = true)
	public TaskResponse get(AuthenticatedUser user, UUID id) {
		return taskMapper.toResponse(findOwned(user, id));
	}

	public TaskResponse create(AuthenticatedUser user, TaskRequest request) {
		Task task = new Task();
		taskMapper.apply(request, task);
		task.setCompleted(false);
		task.setOwnerId(user.id());
		task.setOwnerUsername(user.username());
		return taskMapper.toResponse(taskRepository.saveAndFlush(task));
	}

	public TaskResponse update(AuthenticatedUser user, UUID id, TaskRequest request) {
		Task task = findOwned(user, id);
		taskMapper.apply(request, task);
		task.setOwnerUsername(user.username());
		return taskMapper.toResponse(taskRepository.saveAndFlush(task));
	}

	public TaskResponse toggle(AuthenticatedUser user, UUID id) {
		Task task = findOwned(user, id);
		task.setCompleted(!task.isCompleted());
		return taskMapper.toResponse(taskRepository.saveAndFlush(task));
	}

	public void delete(AuthenticatedUser user, UUID id) {
		taskRepository.delete(findOwned(user, id));
	}

	@Transactional(readOnly = true)
	public TaskStatsResponse stats(AuthenticatedUser user) {
		LocalDate today = LocalDate.now(clock);
		long total = taskRepository.countByOwnerId(user.id());
		long completed = taskRepository.countByOwnerIdAndCompletedTrue(user.id());
		long overdue = taskRepository.countByOwnerIdAndCompletedFalseAndDueDateBefore(user.id(), today);
		return new TaskStatsResponse(completed, total - completed - overdue, overdue, total);
	}

	private Task findOwned(AuthenticatedUser user, UUID id) {
		return taskRepository.findByIdAndOwnerId(id, user.id())
				.orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable"));
	}
}
