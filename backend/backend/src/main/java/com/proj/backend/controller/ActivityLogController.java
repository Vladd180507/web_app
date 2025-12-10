package com.proj.backend.controller;

import com.proj.backend.dto.ActivityLogResponseDto;
import com.proj.backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLogResponseDto>> getMyLogs(Principal principal) {
        return ResponseEntity.ok(activityLogService.getLogsForUser(principal.getName()));
    }
}