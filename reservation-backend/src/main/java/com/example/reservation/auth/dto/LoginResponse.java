package com.example.reservation.auth.dto;

import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        Long userId,
        String username,
        String realName,
        List<String> roles
) {
}

