package com.proj.backend.service;


import com.proj.backend.dto.ActivityLogResponseDto;
import com.proj.backend.model.*;
import com.proj.backend.dto.GroupDto;
import com.proj.backend.repository.GroupRepository;
import com.proj.backend.repository.UserRepository;
import com.proj.backend.repository.MembershipRepository;
import com.proj.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public void logActivity(Long userId, String action, String details) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Používateľ neexistuje"));

        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        // timestamp устанавливается автоматически

        activityLogRepository.save(log);
    }

    public List<ActivityLogResponseDto> getLogsByUser(Long userId) {
        return activityLogRepository.findByUser_UserId(userId)
                .stream()
                .map(ActivityLogResponseDto::new)
                .toList();
    }

    public List<ActivityLogResponseDto> getAllLogs() {
        return activityLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(ActivityLogResponseDto::new)
                .toList();
    }

    // Для аналитики: например, count by action или по пользователю
    public long countActivitiesByUser(Long userId) {
        return activityLogRepository.findByUser_UserId(userId).size();
    }
}