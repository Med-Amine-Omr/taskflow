package com.taskflow.api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.api.dto.PageResponse;
import com.taskflow.api.dto.TaskResponse;
import com.taskflow.api.dto.UserTaskStatsResponse;
import com.taskflow.api.entity.Task;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.mapper.TaskMapper;
import com.taskflow.api.repository.TaskRepository;
import com.taskflow.api.repository.TaskSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTaskService {

	private final TaskRepository taskRepository;
	private final TaskMapper taskMapper;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<TaskResponse> list(String ownerId, PageRequest pageRequest) {
		Specification<Task> spec = (ownerId == null || ownerId.isBlank()) ? TaskSpecifications.all()
				: TaskSpecifications.ownedBy(ownerId);
		return PageResponse.from(taskRepository.findAll(spec, pageRequest).map(taskMapper::toResponse));
	}

	public void delete(UUID id) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable"));
		taskRepository.delete(task);
	}

	@Transactional(readOnly = true)
	public List<UserTaskStatsResponse> stats() {
		return taskRepository.statsByOwner(LocalDate.now(clock)).stream()
				.map(s -> new UserTaskStatsResponse(s.ownerId(), s.ownerUsername(), s.total(), s.completed(),
						s.total() - s.completed() - s.overdue(), s.overdue()))
				.toList();
	}
}
