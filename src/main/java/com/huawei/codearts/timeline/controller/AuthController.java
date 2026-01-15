package com.huawei.codearts.timeline.controller;

import com.huawei.codearts.timeline.dto.ApiResponse;
import com.huawei.codearts.timeline.dto.AuthResponse;
import com.huawei.codearts.timeline.dto.UserDto;
import com.huawei.codearts.timeline.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto userDto = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
        return ApiResponse.success("Registration successful", userDto);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success("Login successful", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        // Extract user ID from JWT token
        String token = authorization.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
        UserDto userDto = authService.getCurrentUser(userId);
        return ApiResponse.success(userDto);
    }

    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }
}
