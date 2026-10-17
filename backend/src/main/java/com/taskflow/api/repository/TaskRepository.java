package com.taskflow.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taskflow.api.entity.Task;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

	Optional<Task> findByIdAndOwnerId(UUID id, String ownerId);

	long countByOwnerId(String ownerId);

	long countByOwnerIdAndCompletedTrue(String ownerId);

	long countByOwnerIdAndCompletedFalseAndDueDateBefore(String ownerId, LocalDate date);

	@Query("""
			select new com.taskflow.api.repository.UserTaskStats(
			    t.ownerId, t.ownerUsername,
			    count(t),
			    sum(case when t.completed = true then 1 else 0 end),
			    sum(case when t.completed = false and t.dueDate < :today then 1 else 0 end))
			from Task t
			group by t.ownerId, t.ownerUsername
			order by t.ownerUsername
			""")
	List<UserTaskStats> statsByOwner(@Param("today") LocalDate today);
}
