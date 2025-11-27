package com.proj.backend.controller;

import com.proj.backend.dto.ActivityLogResponseDto;
import com.proj.backend.model.User;
import com.proj.backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.proj.backend.repository.ActivityLogRepository;
import com.proj.backend.repository.MembershipRepository;
import com.proj.backend.repository.UserRepository;

import java.util.List;
@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {
    private final UserRepository userRepository;

    private final ActivityLogService activityLogService;
    private final MembershipRepository membershipRepository; // Pridaj toto, ak treba checkovať spoločné skupiny

    @GetMapping("/user/{userId}")
    public List<ActivityLogResponseDto> getByUser(@PathVariable Long userId,
                                                  @AuthenticationPrincipal User currentUser) {
        // Základný check: vlastný user vždy môže vidieť svoje logs
        if (currentUser.getUserId().equals(userId)) {
            return activityLogService.getLogsByUser(userId);
        }

        // Ak nie je vlastný, checkuj či currentUser je admin v aspoň jednej skupine
        if (!currentUser.isGroupAdmin()) {
            throw new RuntimeException("Nedostatočné práva");
        }

        // Voliteľne: prísnejší check – či je currentUser admin v skupine, kde je target user členom
        // boolean isAdminOverUser = membershipRepository.findByUser_UserId(userId)
        //     .stream()
        //     .anyMatch(m -> m.getGroup().getMemberships().stream()
        //         .anyMatch(cm -> cm.getUser().equals(currentUser) && cm.getRole() == MembershipRole.ADMIN));
        // if (!isAdminOverUser) {
        //     throw new RuntimeException("Nedostatočné práva na zobrazenie logov tohto používateľa");
        // }

        return activityLogService.getLogsByUser(userId);
    }

//    @GetMapping
//    public List<ActivityLogResponseDto> getAll(@AuthenticationPrincipal User currentUser) {
//        // Len pre group admins
//        if (!currentUser.isGroupAdmin()) {
//            throw new RuntimeException("Nedostatočné práva");
//        }
//        return activityLogService.getAllLogs();
//    }

    @GetMapping
    public List<ActivityLogResponseDto> getAll() {
        // TEMP: вместо @AuthenticationPrincipal
        User currentUser = userRepository.findById(1L).orElseThrow(); // ID тестового пользователя
        if (!currentUser.isGroupAdmin()) {
            throw new RuntimeException("Недостаточно прав");
        }
        return activityLogService.getAllLogs();
    }

}