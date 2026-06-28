package com.example.reservation.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reservation.auth.dto.LoginRequest;
import com.example.reservation.auth.dto.LoginResponse;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.SysRole;
import com.example.reservation.domain.entity.SysUser;
import com.example.reservation.domain.entity.SysUserRole;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.SysRoleMapper;
import com.example.reservation.mapper.SysUserMapper;
import com.example.reservation.mapper.SysUserRoleMapper;
import com.example.reservation.security.JwtService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username()));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "username or password is incorrect");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "username or password is incorrect");
        }

        List<String> roles = findRoleCodes(user.getId());
        String token = jwtService.generateToken(user.getId(), user.getUsername(), roles);
        return new LoginResponse(
                "Bearer",
                token,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                roles
        );
    }

    public List<String> findRoleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds)
                .stream()
                .map(SysRole::getRoleCode)
                .toList();
    }
}

