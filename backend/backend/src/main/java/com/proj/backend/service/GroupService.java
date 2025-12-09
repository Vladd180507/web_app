package com.proj.backend.service;

import com.proj.backend.dto.GroupDto;
import com.proj.backend.model.Group;
import com.proj.backend.model.Membership;
import com.proj.backend.model.MembershipRole;
import com.proj.backend.model.User;
import com.proj.backend.repository.GroupRepository;
import com.proj.backend.repository.MembershipRepository;
import com.proj.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final ActivityLogService activityLogService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final com.proj.backend.repository.ResourceRepository resourceRepository;

    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll()
                .stream()
                .map(GroupDto::fromEntity)
                .toList();
    }

    public GroupDto getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(GroupDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Transactional
    public GroupDto createGroup(String name, String description, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = Group.builder()
                .name(name)
                .description(description)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())
                .build();

        Group savedGroup = groupRepository.save(group);

        Membership adminMembership = Membership.builder()
                .user(creator)
                .group(savedGroup)
                .role(MembershipRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();
        membershipRepository.save(adminMembership);

        // LOG
        activityLogService.logActivity(
                creator.getUserId(),
                "GROUP_CREATED",
                "Created group: " + name
        );

        return GroupDto.fromEntity(savedGroup);
    }

    // ✅ ОНОВЛЕННЯ ГРУПИ (+ ЛОГ)
    @Transactional
    public GroupDto updateGroup(Long id, String name, String description, String editorEmail) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldName = group.getName();
        group.setName(name);
        group.setDescription(description);

        Group updated = groupRepository.save(group);

        // LOG
        activityLogService.logActivity(
                editor.getUserId(),
                "GROUP_UPDATED",
                "Updated group details. Old name: " + oldName + ", New name: " + name
        );

        return GroupDto.fromEntity(updated);
    }

    @Transactional
    public void deleteGroup(Long id, String editorEmail) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String groupName = group.getName();

        // 👇 2. ВАЖЛИВО: Спочатку видаляємо всі ресурси цієї групи!
        // (Припускаємо, що у ResourceRepository є метод deleteByGroupGroupId або ми дістаємо і видаляємо)
        var resources = resourceRepository.findByGroupId(id);
        resourceRepository.deleteAll(resources);

        // Також видаляємо учасників (якщо каскад не налаштований)
        // membershipRepository.deleteByGroupGroupId(id); // якщо треба

        groupRepository.delete(group);

        activityLogService.logActivity(
                editor.getUserId(),
                "GROUP_DELETED",
                "Deleted group: " + groupName
        );
    }
}