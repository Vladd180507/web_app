package com.proj.backend.repository;

import com.proj.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByGroupGroupId(Long groupId);

    // все задачи, где пользователь — создатель или в группе (потом улучшим)
    List<Task> findByGroupGroupIdOrCreatedByUserId(Long groupId, Long userId);
}