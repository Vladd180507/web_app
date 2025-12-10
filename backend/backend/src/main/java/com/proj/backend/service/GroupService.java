package com.proj.backend.service;

import com.proj.backend.dto.GroupDto;
import com.proj.backend.model.*;
import com.proj.backend.repository.*;
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
    // Репозиторії для каскадного видалення
    private final com.proj.backend.repository.ResourceRepository resourceRepository;
    private final TaskRepository taskRepository;

    // Отримати тільки групи користувача
    public List<GroupDto> getUserGroups(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return groupRepository.findAllByUserId(user.getUserId())
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

        // ✅ ВИПРАВЛЕНО: Додано savedGroup.getGroupId() (2-й аргумент)
        activityLogService.logActivity(
                creator.getUserId(),
                savedGroup.getGroupId(),
                "GROUP_CREATED",
                "Created group: " + name
        );

        return GroupDto.fromEntity(savedGroup);
    }

    @Transactional
    public GroupDto updateGroup(Long id, String name, String description, String editorEmail) {
        verifyGroupAdmin(id, editorEmail);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldName = group.getName();
        group.setName(name);
        group.setDescription(description);

        Group updated = groupRepository.save(group);

        // ✅ ВИПРАВЛЕНО: Додано updated.getGroupId()
        activityLogService.logActivity(
                editor.getUserId(),
                updated.getGroupId(),
                "GROUP_UPDATED",
                "Updated group details. Old name: " + oldName + ", New name: " + name
        );

        return GroupDto.fromEntity(updated);
    }

    @Transactional
    public void deleteGroup(Long id, String editorEmail) {
        verifyGroupAdmin(id, editorEmail);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String groupName = group.getName();

        // Каскадне видалення
        var tasks = taskRepository.findByGroupGroupId(id);
        taskRepository.deleteAll(tasks);

        var resources = resourceRepository.findByGroupId(id);
        resourceRepository.deleteAll(resources);

        var memberships = membershipRepository.findByGroupGroupId(id);
        membershipRepository.deleteAll(memberships);

        groupRepository.delete(group);

        // ✅ ВИПРАВЛЕНО: Додано id (ID видаленої групи)
        // Лог залишиться, хоча група видалена (бо ми зберігаємо просто ID типу Long)
        activityLogService.logActivity(
                editor.getUserId(),
                id,
                "GROUP_DELETED",
                "Deleted group: " + groupName
        );
    }

    private void verifyGroupAdmin(Long groupId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership membership = membershipRepository.findByUserUserIdAndGroupGroupId(user.getUserId(), groupId)
                .orElseThrow(() -> new RuntimeException("Access Denied: You are not a member of this group"));

        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new RuntimeException("Access Denied: Only group ADMIN can perform this action");
        }
    }
}