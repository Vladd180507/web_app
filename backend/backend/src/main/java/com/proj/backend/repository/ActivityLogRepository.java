package com.proj.backend.repository;

import com.proj.backend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUser_UserId(Long userId);

    List<ActivityLog> findAllByOrderByTimestampDesc(); // Для общих логов

    // Опционально: по группе, если добавим group_id позже
}