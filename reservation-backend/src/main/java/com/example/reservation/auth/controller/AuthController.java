package com.example.reservation.auth.controller;

import com.example.reservation.auth.dto.CurrentUserResponse;
import com.example.reservation.auth.dto.LoginRequest;
import com.example.reservation.auth.dto.LoginResponse;
import com.example.reservation.auth.service.AuthService;
import com.example.reservation.common.ApiResponse;
import com.example.reservation.security.JwtUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal JwtUser user) {
        List<String> roles = user.authorities()
                .stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .toList();
        return ApiResponse.success(new CurrentUserResponse(user.userId(), user.username(), roles));
    }
}

