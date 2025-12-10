package com.proj.backend.service;

import com.proj.backend.dto.ActivityLogResponseDto;
import com.proj.backend.model.ActivityLog;
import com.proj.backend.model.User;
import com.proj.backend.repository.ActivityLogRepository;
import com.proj.backend.repository.MembershipRepository;
import com.proj.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    // ✅ ОНОВЛЕНИЙ МЕТОД ЗАПИСУ
    public void logActivity(Long userId, Long groupId, String action, String details) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setGroupId(groupId); // <-- Зберігаємо ID групи (або null)
        log.setAction(action);
        log.setDetails(details);

        activityLogRepository.save(log);
    }

    // ✅ ОНОВЛЕНИЙ МЕТОД ОТРИМАННЯ
    public List<ActivityLogResponseDto> getLogsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Знаходимо всі ID груп, де юзер є учасником
        List<Long> myGroupIds = membershipRepository.findByUserUserId(user.getUserId())
                .stream()
                .map(m -> m.getGroup().getGroupId())
                .toList();

        // 2. Хак для новачків: якщо списку немає, додаємо -1, щоб SQL не впав
        if (myGroupIds.isEmpty()) {
            myGroupIds = List.of(-1L);
        }

        // 3. Виконуємо запит
        return activityLogRepository.findMyLogs(myGroupIds)
                .stream()
                .map(ActivityLogResponseDto::new)
                .toList();
    }
}