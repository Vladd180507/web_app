package com.proj.backend.controller;

import com.proj.backend.dto.GroupDto;
import com.proj.backend.model.User;
import com.proj.backend.repository.UserRepository;
import com.proj.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    // ✅ ОТРИМАТИ ТІЛЬКИ МОЇ ГРУПИ
    @GetMapping
    public ResponseEntity<List<GroupDto>> getUserGroups(Principal principal) {
        // principal.getName() - це email з токена
        return ResponseEntity.ok(groupService.getUserGroups(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping
    public ResponseEntity<GroupDto> create(
            @RequestBody Map<String, Object> request,
            Principal principal
    ) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupDto created = groupService.createGroup(name, description, user.getUserId());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupDto> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Principal principal
    ) {
        String name = request.get("name");
        String description = request.get("description");
        String email = principal.getName();

        GroupDto updated = groupService.updateGroup(id, name, description, email);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal
    ) {
        String email = principal.getName();
        groupService.deleteGroup(id, email);
        return ResponseEntity.noContent().build();
    }
}