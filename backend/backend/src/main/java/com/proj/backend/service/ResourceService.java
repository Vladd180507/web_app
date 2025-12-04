package com.proj.backend.service;

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

        activityLogService.logActivity(
                user.getUserId(),
                "RESOURCE_UPLOADED",
                "Загружен материал: " + dto.getTitle()
        );

        Resource saved = resourceRepository.save(resource);
        return convertToDTO(saved);
    }

    public List<ResourceDTO> getResourcesByGroup(Long groupId) {
        //  ZMENENÉ: findByGroupGroupId → findByGroupId
        return resourceRepository.findByGroupId(groupId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ResourceDTO> getResourcesByGroupAndType(Long groupId, String type) {
        //  ZMENENÉ: findByGroupGroupIdAndType → findByGroupIdAndType
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
        //  ZMENENÉ: countByGroupGroupId → countByGroupId
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
}