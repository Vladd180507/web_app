package com.proj.backend.service;

import com.proj.backend.dto.NotificationDto; // ✅ Додано імпорт
import com.proj.backend.dto.ResourceDTO;
import com.proj.backend.model.Group;
import com.proj.backend.model.Resource;
import com.proj.backend.model.User;
import com.proj.backend.repository.GroupRepository;
import com.proj.backend.repository.ResourceRepository;
import com.proj.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ActivityLogService activityLogService;
    private final ResourceRepository resourceRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ResourceDTO createResource(ResourceDTO dto) {
        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(dto.getUploadedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = Resource.builder()
                .group(group)
                .uploadedBy(user)
                .title(dto.getTitle())
                .type(dto.getType())
                .pathOrUrl(dto.getPathOrUrl())
                .uploadedAt(LocalDateTime.now())
                .build();

        Resource updated = resourceRepository.save(resource);

        // ✅ ВИПРАВЛЕНО ЛОГ: Додано group.getGroupId() (2-й аргумент)
        activityLogService.logActivity(
                user.getUserId(),
                group.getGroupId(), // ID групи
                "RESOURCE_ADDED",
                "Added: " + dto.getTitle()
        );

        // ✅ ВИПРАВЛЕНО СПОВІЩЕННЯ: Відправляємо NotificationDto
        try {
            String msg = "New resource in group " + group.getName() + ": " + dto.getTitle();

            NotificationDto notification = new NotificationDto(
                    msg,
                    user.getUserId(),
                    group.getGroupId() // ID групи для фільтрації
            );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            System.err.println("WebSocket error: " + e.getMessage());
        }

        return convertToDTO(updated);
    }

    public List<ResourceDTO> getResourcesByGroup(Long groupId) {
        return resourceRepository.findByGroupId(groupId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ResourceDTO> getResourcesByGroupAndType(Long groupId, String type) {
        return resourceRepository.findByGroupIdAndType(groupId, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ResourceDTO getResourceById(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        return convertToDTO(resource);
    }

    @Transactional
    public ResourceDTO updateResource(Long resourceId, ResourceDTO dto) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resource.setTitle(dto.getTitle());
        resource.setType(dto.getType());
        resource.setPathOrUrl(dto.getPathOrUrl());

        Resource updated = resourceRepository.save(resource);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new RuntimeException("Resource not found");
        }
        resourceRepository.deleteById(resourceId);
    }

    public Long countResourcesByGroup(Long groupId) {
        return resourceRepository.countByGroupId(groupId);
    }

    public List<ResourceDTO> searchResources(Long groupId, String keyword) {
        return resourceRepository.searchByTitle(groupId, keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ResourceDTO convertToDTO(Resource resource) {
        return ResourceDTO.builder()
                .resourceId(resource.getResourceId())
                .groupId(resource.getGroup().getGroupId())
                .groupName(resource.getGroup().getName())
                .uploadedBy(resource.getUploadedBy().getUserId())
                .uploaderName(resource.getUploadedBy().getName())
                .uploaderEmail(resource.getUploadedBy().getEmail())
                .title(resource.getTitle())
                .type(resource.getType())
                .pathOrUrl(resource.getPathOrUrl())
                .uploadedAt(resource.getUploadedAt())
                .build();
    }

    public List<ResourceDTO> getAllResources() {
        return resourceRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
}