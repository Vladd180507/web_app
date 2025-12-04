package com.proj.backend.service;

import com.proj.backend.model.MembershipRole;
import com.proj.backend.model.Membership;
import com.proj.backend.dto.GroupDto;
import com.proj.backend.model.Group;
import com.proj.backend.model.User;
import com.proj.backend.repository.GroupRepository;
import com.proj.backend.repository.UserRepository;
import com.proj.backend.repository.MembershipRepository;
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

    @Transactional  // ✅ Добавь транзакцию
    public GroupDto createGroup(String name, String description, Long creatorId) {  // ✅ Изменил на Long creatorId
        // ✅ Ищем по ID
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + creatorId));

        Group group = Group.builder()
                .name(name)
                .description(description)
                .createdBy(creator)
                .createdAt(LocalDateTime.now())  // ✅ Добавь created_at
                .build();

        Group savedGroup = groupRepository.save(group);

        // Автоматически делаем создателя ADMIN
        Membership adminMembership = Membership.builder()
                .user(creator)
                .group(savedGroup)
                .role(MembershipRole.ADMIN)
                .joinedAt(LocalDateTime.now())  // ✅ Добавь joined_at
                .build();

        activityLogService.logActivity(
                creator.getUserId(),
                "GROUP_CREATED",
                "Создана новая группа: " + name
        );

        membershipRepository.save(adminMembership);

        return GroupDto.fromEntity(savedGroup);  // ✅ Возвращаем savedGroup, а не group
    }

    @Transactional
    public GroupDto updateGroup(Long id, String name, String description) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setName(name);
        group.setDescription(description);

        Group updated = groupRepository.save(group);
        return GroupDto.fromEntity(updated);
    }

    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new RuntimeException("Group not found");
        }
        groupRepository.deleteById(id);
    }
}