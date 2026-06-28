package com.example.reservation.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record JwtUser(
        Long userId,
        String username,
        Collection<? extends GrantedAuthority> authorities
) {
}

