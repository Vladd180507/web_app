package com.proj.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceDTO {
    private Long resourceId;
    private Long groupId;
    private String groupName;
    private Long uploadedBy;
    private String uploaderName;
    private String uploaderEmail;
    private String title;
    private String type;
    private String pathOrUrl;
    private LocalDateTime uploadedAt;
}