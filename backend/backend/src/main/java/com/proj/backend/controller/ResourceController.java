package com.proj.backend.controller;

import com.proj.backend.dto.ResourceDTO;
import com.proj.backend.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(@RequestBody ResourceDTO dto) {
        ResourceDTO created = resourceService.createResource(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ResourceDTO>> getResourcesByGroup(@PathVariable Long groupId) {
        List<ResourceDTO> resources = resourceService.getResourcesByGroup(groupId);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/group/{groupId}/type/{type}")
    public ResponseEntity<List<ResourceDTO>> getResourcesByGroupAndType(
            @PathVariable Long groupId,
            @PathVariable String type) {
        List<ResourceDTO> resources = resourceService.getResourcesByGroupAndType(groupId, type);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable Long resourceId) {
        ResourceDTO resource = resourceService.getResourceById(resourceId);
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<ResourceDTO> updateResource(
            @PathVariable Long resourceId,
            @RequestBody ResourceDTO dto) {
        ResourceDTO updated = resourceService.updateResource(resourceId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/group/{groupId}/count")
    public ResponseEntity<Long> countResourcesByGroup(@PathVariable Long groupId) {
        Long count = resourceService.countResourcesByGroup(groupId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/group/{groupId}/search")
    public ResponseEntity<List<ResourceDTO>> searchResources(
            @PathVariable Long groupId,
            @RequestParam String keyword
    ) {
        List<ResourceDTO> result = resourceService.searchResources(groupId, keyword);
        return ResponseEntity.ok(result);
    }

    // ✅ Endpoint для отримання всіх ресурсів
    @GetMapping("/all")
    public ResponseEntity<List<ResourceDTO>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }
}