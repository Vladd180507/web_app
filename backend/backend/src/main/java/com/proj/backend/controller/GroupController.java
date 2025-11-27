package com.proj.backend.controller;

import com.proj.backend.dto.GroupDto;
import com.proj.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupDto>> getAll() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping
    public ResponseEntity<GroupDto> create(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");

        // ✅ Получаем createdBy как Long (Integer преобразуется в Long)
        Long creatorId = ((Number) request.get("createdBy")).longValue();

        GroupDto created = groupService.createGroup(name, description, creatorId);
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