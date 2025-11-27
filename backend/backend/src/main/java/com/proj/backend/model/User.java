package com.proj.backend.model;

import com.proj.backend.model.Membership;
import com.proj.backend.model.MembershipRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Override
    public String toString() {
        return "User{id=" + userId + ", name=" + name + ", email=" + email + "}";
    }

    @OneToMany(mappedBy = "user")
    private List<Membership> memberships;

    public boolean isGroupAdmin() {
        if (memberships == null) {
            return false;
        }
        return memberships.stream().anyMatch(m -> m.getRole() == MembershipRole.ADMIN);
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // Add roles if needed later
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}