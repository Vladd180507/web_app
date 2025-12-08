package com.proj.backend.controller;

import com.proj.backend.config.JwtCore;
import com.proj.backend.service.UserService;
import com.proj.backend.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    // ✅ ЛОГИН - POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        System.out.println(">>> ATTEMPTING LOGIN FOR: " + email);
        System.out.println(">>> PASSWORD LENGTH: " + (password != null ? password.length() : "null"));

        try {
            // 1. Спроба аутентифікації (тут перевіряється хеш пароля)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 2. Якщо все ок - встановлюємо контекст
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Генеруємо токен
            String jwt = jwtCore.generateToken(authentication);

            System.out.println(">>> LOGIN SUCCESS! Token generated.");
            return ResponseEntity.ok(jwt);

        } catch (BadCredentialsException e) {
            System.err.println("!!! BAD CREDENTIALS ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невірний логін або пароль");
        } catch (Exception e) {
            System.err.println("!!! LOGIN ERROR: " + e.getClass().getName() + " -> " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Помилка сервера: " + e.getMessage());
        }
    }

    // ✅ РЕЄСТРАЦІЯ
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            UserDto created = userService.register(
                    request.get("name"),
                    request.get("email"),
                    request.get("password")
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ... (інші методи залиш без змін або видали, якщо вони не використовуються для тесту)

    // ✅ ОТРИМАТИ ПОЛЬЗОВАТЕЛЯ ПО EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}