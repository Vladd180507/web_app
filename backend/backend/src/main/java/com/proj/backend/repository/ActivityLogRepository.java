package com.proj.backend.repository;

import com.proj.backend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // ✅ ГОЛОВНИЙ ЗАПИТ:
    // Вибрати логи, де group_id є в списку моїх груп
    // АБО group_id IS NULL (глобальні події)
    @Query("SELECT l FROM ActivityLog l WHERE (l.groupId IN :groupIds) OR (l.groupId IS NULL) ORDER BY l.timestamp DESC")
    List<ActivityLog> findMyLogs(@Param("groupIds") List<Long> groupIds);
}