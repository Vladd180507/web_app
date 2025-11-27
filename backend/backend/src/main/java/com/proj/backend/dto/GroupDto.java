package com.proj.backend.dto;

import lombok.Data;
import com.proj.backend.model.Group;

@Data
public class GroupDto {
    private Long groupId;
    private String name;
    private String description;
    private String createdByName;
    private String createdAt;

    public static GroupDto fromEntity(Group group) {
        GroupDto dto = new GroupDto();
        dto.setGroupId(group.getGroupId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setCreatedByName(group.getCreatedBy().getName());
        dto.setCreatedAt(group.getCreatedAt().toString());
        return dto;
    }
}