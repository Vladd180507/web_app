package com.proj.backend.dto;

import com.proj.backend.model.MembershipRole;
import com.proj.backend.model.Membership;
import lombok.Data;

@Data
public class MemberDto {
    private Long userId;
    private String name;
    private String email;
    private MembershipRole role;
    private String joinedAt;

    public static MemberDto fromEntity(Membership m) {
        MemberDto dto = new MemberDto();
        dto.setUserId(m.getUser().getUserId());
        dto.setName(m.getUser().getName());
        dto.setEmail(m.getUser().getEmail());
        dto.setRole(m.getRole());
        dto.setJoinedAt(m.getJoinedAt().toString());
        return dto;
    }
}