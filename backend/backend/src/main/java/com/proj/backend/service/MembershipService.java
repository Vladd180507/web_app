package com.proj.backend.service;

import com.proj.backend.dto.MemberDto;
import com.proj.backend.model.Group;
import com.proj.backend.model.Membership;
import com.proj.backend.model.MembershipRole;
import com.proj.backend.model.User;
import com.proj.backend.repository.GroupRepository;
import com.proj.backend.repository.MembershipRepository;
import com.proj.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // Добавить пользователя в группу
    public MemberDto joinGroup(Long groupId, Long userId) {
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
                .build();

        return MemberDto.fromEntity(membershipRepository.save(membership));
    }

    // Получить всех участников группы
    public List<MemberDto> getMembers(Long groupId) {
        return membershipRepository.findByGroupGroupId(groupId)
                .stream()
                .map(MemberDto::fromEntity)
                .toList();
    }

    public MemberDto joinGroupById(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (membershipRepository.existsByUserUserIdAndGroupGroupId(user.getUserId(), groupId)) {
            throw new RuntimeException("User is already in this group");
        }

        Membership membership = Membership.builder()
                .user(user)
                .group(group)
                .role(MembershipRole.MEMBER)
                .build();

        return MemberDto.fromEntity(membershipRepository.save(membership));
    }

    public void leaveGroupById(Long groupId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership membership = membershipRepository
                .findByUserUserIdAndGroupGroupId(user.getUserId(), groupId)
                .orElseThrow(() -> new RuntimeException("User is not in this group"));

        if (membership.getRole() == MembershipRole.ADMIN &&
                membershipRepository.findByGroupGroupId(groupId).size() == 1) {
            throw new RuntimeException("Cannot leave group as the only admin");
        }

        membershipRepository.delete(membership);
    }


    // Выйти из группы / удалить пользователя
    public void leaveGroup(Long groupId, Long userId) {
        Membership membership = membershipRepository
                .findByUserUserIdAndGroupGroupId(userId, groupId)
                .orElseThrow(() -> new RuntimeException("User is not in the group"));

        membershipRepository.delete(membership);
    }
}
