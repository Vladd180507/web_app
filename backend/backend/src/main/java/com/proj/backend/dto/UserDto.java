package com.proj.backend.dto;



import com.proj.backend.model.User;
import lombok.Data;

@Data
public class UserDto {
    private Long userId;
    private String name;
    private String email;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
