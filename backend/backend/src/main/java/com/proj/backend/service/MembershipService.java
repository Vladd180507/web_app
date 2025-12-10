package com.proj.backend.service;

import com.proj.backend.dto.MemberDto;
import com.proj.backend.dto.NotificationDto; // ✅ Додано імпорт DTO
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<MemberDto> getMembers(Long groupId) {
        return membershipRepository.findByGroupGroupId(groupId)
                .stream()
                .map(MemberDto::fromEntity)
                .toList();
    }

    @Transactional
    public MemberDto joinGroupById(Long groupId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (membershipRepository.existsByUserUserIdAndGroupGroupId(userId, groupId)) {
            throw new RuntimeException("User is already in the group");
        }

        Membership membership = Membership.builder()
                .user(user)
                .group(group)
                .role(MembershipRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();

        Membership saved = membershipRepository.save(membership);

        // ✅ ВИПРАВЛЕНО ЛОГ: Додано group.getGroupId()
        activityLogService.logActivity(
                userId,
                group.getGroupId(), // ID групи
                "GROUP_JOIN",
                "User " + user.getName() + " joined group: " + group.getName()
        );

        // ✅ ВИПРАВЛЕНО СПОВІЩЕННЯ: Відправляємо NotificationDto
        try {
            String msg = "User " + user.getName() + " joined group " + group.getName() + "!";

            NotificationDto notification = new NotificationDto(
                    msg,
                    userId,
                    group.getGroupId() // ID групи для фільтрації
            );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            System.err.println("WebSocket error: " + e.getMessage());
        }

        return MemberDto.fromEntity(saved);
    }

    @Transactional
    public void leaveGroupById(Long groupId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership membership = membershipRepository
                .findByUserUserIdAndGroupGroupId(userId, groupId)
                .orElseThrow(() -> new RuntimeException("User is not in this group"));

        String groupName = membership.getGroup().getName();

        if (membership.getRole() == MembershipRole.ADMIN &&
                membershipRepository.findByGroupGroupId(groupId).size() == 1) {
            throw new RuntimeException("Cannot leave group as the only admin");
        }

        membershipRepository.delete(membership);

        // ✅ ВИПРАВЛЕНО ЛОГ: Додано groupId
        activityLogService.logActivity(
                userId,
                groupId, // ID групи
                "GROUP_LEAVE",
                "User " + user.getName() + " left group: " + groupName
        );
    }
}