package com.proj.backend.controller;

import com.proj.backend.dto.GroupDto;
import com.proj.backend.model.User; // <--- Импорт User
import com.proj.backend.repository.UserRepository; // <--- Импорт репозитория
import com.proj.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // <--- Импорт для Токена
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository; // <--- 1. Добавили репозиторий

    @GetMapping
    public ResponseEntity<List<GroupDto>> getAll() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    // ✅ БЕЗОПАСНОЕ СОЗДАНИЕ ГРУППЫ
    @PostMapping
    public ResponseEntity<GroupDto> create(
            @RequestBody Map<String, Object> request,
            Principal principal // <--- 2. Берем пользователя из токена
    ) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");

        // 3. Узнаем email того, кто пришел (из токена)
        String email = principal.getName();

        // 4. Находим его в базе
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 5. Передаем его НАСТОЯЩИЙ ID
        // Поле "createdBy" из JSON мы теперь просто игнорируем!
        GroupDto created = groupService.createGroup(name, description, user.getUserId());

        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupDto> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");

        GroupDto updated = groupService.updateGroup(id, name, description);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}