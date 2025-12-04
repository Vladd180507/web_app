package com.proj.backend.controller;

import com.proj.backend.dto.ActivityLogResponseDto;
import com.proj.backend.model.User;
import com.proj.backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.proj.backend.repository.UserRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    // ✅ ПОЛУЧИТЬ ЛОГИ КОНКРЕТНОГО ПОЛЬЗОВАТЕЛЯ
    @GetMapping("/user/{userId}")
    public List<ActivityLogResponseDto> getByUser(@PathVariable Long userId, Principal principal) {
        // 1. Узнаем, кто делает запрос (из токена)
        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Проверка прав:
        // Пользователь может смотреть СВОИ логи
        // ИЛИ Админ может смотреть ЧУЖИЕ
        boolean isSelf = currentUser.getUserId().equals(userId);
        boolean isAdmin = currentUser.isGroupAdmin();

        if (!isSelf && !isAdmin) {
            throw new RuntimeException("Недостаточно прав для просмотра логов другого пользователя");
        }

        return activityLogService.getLogsByUser(userId);
    }

    // ✅ ПОЛУЧИТЬ ВООБЩЕ ВСЕ ЛОГИ (Только для Админов)
    @GetMapping
    public List<ActivityLogResponseDto> getAll(Principal principal) {
        // 1. Узнаем, кто делает запрос
        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Если он не админ - запрещаем
        if (!currentUser.isGroupAdmin()) {
            throw new RuntimeException("Недостаточно прав. Только для администраторов.");
        }

        return activityLogService.getAllLogs();
    }
}