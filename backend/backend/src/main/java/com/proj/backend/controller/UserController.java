package com.proj.backend.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // получить список всех пользователей
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // получить конкретного по имени
    @GetMapping("/{name}")
    public ResponseEntity<UserDto> getUserByName(@PathVariable String name) {
        return userRepository.findByName(name)
                .map(user -> ResponseEntity.ok(UserDto.fromEntity(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
