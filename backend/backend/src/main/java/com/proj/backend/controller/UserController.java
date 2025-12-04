package com.proj.backend.controller;

import com.proj.backend.config.JwtCore;
import com.proj.backend.service.ActivityLogService;
import com.proj.backend.service.UserService;
import com.proj.backend.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.proj.backend.model.User;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final UserService userService;
    private final ActivityLogService activityLogService;


    // ✅ ЛОГИН - POST /api/users/login

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        // 1. Аутентификация через Spring Security (проверит пароль сама)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 2. Если успех - ставим контекст
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        // 3. Генерируем токен
        String jwt = jwtCore.generateToken(authentication);
        //  4. ЗАПИСЫВАЕМ ЛОГ ЗДЕСЬ (Вместо сервиса)
        activityLogService.logActivity(
                user.getUserId(),
                "USER_LOGIN",
                "Пользователь вошел в систему"
        );

        // 4. Возвращаем токен клиенту
        return ResponseEntity.ok(jwt); // Или верни JSON { "token": "...", "user": ... }
    }

    // ✅ РЕГИСТРАЦИЯ - POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");

        UserDto registered = userService.register(name, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    // ✅ ЛОГИН - POST /api/users/login
//    @PostMapping("/login")
//    public ResponseEntity<UserDto> login(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        String password = request.get("password");
//
//        UserDto user = userService.login(email, password);
//        return ResponseEntity.ok(user);
//    }

    // ✅ ПОЛУЧИТЬ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ - GET /api/users
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ✅ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ ПО ID - GET /api/users/1
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ✅ ПОЛУЧИТЬ ПОЛЬЗОВАТЕЛЯ ПО EMAIL - GET /api/users/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ ОБНОВИТЬ ПОЛЬЗОВАТЕЛЯ - PUT /api/users/1
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");

        UserDto updated = userService.updateUser(id, name, email);
        return ResponseEntity.ok(updated);
    }

    // ✅ УДАЛИТЬ ПОЛЬЗОВАТЕЛЯ - DELETE /api/users/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}