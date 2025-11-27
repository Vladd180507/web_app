package com.proj.backend.controller;

import com.proj.backend.dto.MemberDto;
import com.proj.backend.model.MembershipRole;
import com.proj.backend.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // Добавить пользователя в группу

    // Получить всех участников группы
    @GetMapping("/groups/{groupId}/members")
    public List<MemberDto> getMembers(@PathVariable Long groupId) {
        return membershipService.getMembers(groupId);
    }

    // Выйти из группы / удалить пользователя


    @PostMapping
    public MemberDto addUser(@RequestBody AddMembershipRequest req) {
        return membershipService.joinGroupById(req.groupId(), req.userId());
    }

    @DeleteMapping("/leave")
    public void leave(@RequestBody AddMembershipRequest req) {
        membershipService.leaveGroupById(req.groupId(), req.userId());
    }


    // DTO для запроса
    public static record AddMembershipRequest(Long userId, Long groupId, MembershipRole role) {}
}
