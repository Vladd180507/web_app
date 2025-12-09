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
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final UserService userService;

    // ✅ ЛОГИН
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невірний логін або пароль");
        } catch (Exception e) {
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

    // ✅ ОТРИМАТИ КОРИСТУВАЧА ПО EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================================================================
    // 👇 ОСЬ ТУТ БУЛА ПОМИЛКА. ОСЬ ПРАВИЛЬНИЙ КОД ДЛЯ БЕКЕНДУ:
    // =========================================================================

    // ✅ ОНОВЛЕННЯ ПРОФІЛЮ (Endpoint)
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @RequestBody Map<String, String> request,
            Principal principal // <--- Хто стукає? (з токена)
    ) {
        // 1. Отримуємо email поточного юзера з токена
        String currentEmail = principal.getName();

        // 2. Знаходимо його в базі
        UserDto currentUser = userService.getUserByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Беремо нові дані
        String newName = request.get("name");
        String newEmail = request.get("email");

        // 4. Оновлюємо
        UserDto updatedUser = userService.updateUser(currentUser.getUserId(), newName, newEmail);

        return ResponseEntity.ok(updatedUser);
    }
}