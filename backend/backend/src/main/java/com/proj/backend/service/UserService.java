package com.proj.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.proj.backend.dto.UserDto;
import com.proj.backend.model.User;
import com.proj.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;


    // ✅ РЕГИСТРАЦИЯ
    @Builder
    @Transactional
    public UserDto register(String name, String email, String password) {
        // Проверка на существующего пользователя
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with email " + email + " already exists");
        }

        // Хэшируем пароль
        String hashedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(hashedPassword)
                .build();

        User saved = userRepository.save(user);
        activityLogService.logActivity(user.getUserId(), "USER_REGISTERED", "Новый пользователь зарегистрирован: " + name);
        return UserDto.fromEntity(saved);
    }

    // ✅ ЛОГИН
    public UserDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Проверяем пароль
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return UserDto.fromEntity(user);
    }

    // ✅ ПОЛУЧИТЬ ВСЕХ
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ ПОЛУЧИТЬ ПО ID
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDto.fromEntity(user);
    }

    // ✅ ПОЛУЧИТЬ ПО EMAIL
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::fromEntity);
    }

    // ✅ ОБНОВИТЬ
    @Transactional
    public UserDto updateUser(Long id, String name, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setEmail(email);

        User updated = userRepository.save(user);
        return UserDto.fromEntity(updated);
    }

    // ✅ УДАЛИТЬ
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

}