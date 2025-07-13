package com.medisummarize.controller;

import com.medisummarize.DTO.AuthRequestDTO;
import com.medisummarize.DTO.RegisterAuthResponseDTO;
import com.medisummarize.enums.AuthStatus;
import com.medisummarize.model.User;
import com.medisummarize.service.AuthService;
import com.medisummarize.service.UserService;
import com.medisummarize.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequestDTO authRequestDto, HttpServletResponse response)
    {
        try {
            String token = authService.login(authRequestDto.email(), authRequestDto.password());
            User user = userService.getUserByEmail(authRequestDto.email());
            long cookieMaxAge = 60 * 60;
            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(cookieMaxAge)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            Map<String, String> responseBody = Map.of(
                    "userId", String.valueOf(user.getId()),
                    "role", user.getRole().name(),
                    "name", user.getName(),
                    "email", user.getEmail()
            );
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterAuthResponseDTO> register(@RequestBody AuthRequestDTO authRequestDto, HttpServletResponse response) {
        try {
            var user = authService.register(authRequestDto.name(), authRequestDto.email(),authRequestDto.password(),authRequestDto.role());
            var authResponse = new RegisterAuthResponseDTO(user, AuthStatus.USER_CREATED_SUCCESSFULLY);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

        } catch (Exception e) {
            var authResponse = new RegisterAuthResponseDTO(null, AuthStatus.USER_ALREADY_EXITS);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(authResponse);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/refetch")
    public ResponseEntity<?> refetch(@CookieValue(name = "token", required=false) String token){
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token found");
        }
        try {
            Optional<String> email = JwtUtils.getEmailFromToken(token);
            User user = userService.getUserByEmail(email.get());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
        }
    }
}
