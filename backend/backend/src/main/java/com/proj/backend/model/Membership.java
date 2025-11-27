package com.proj.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "memberships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "group_id"}) // один человек в одной группе только один раз
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long membershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}