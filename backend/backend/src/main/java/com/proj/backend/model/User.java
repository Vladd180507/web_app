package com.proj.backend.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

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
}
