package com.example.reservation.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reservation.domain.entity.SysRole;
import com.example.reservation.domain.entity.SysUser;
import com.example.reservation.domain.entity.SysUserRole;
import com.example.reservation.mapper.SysRoleMapper;
import com.example.reservation.mapper.SysUserMapper;
import com.example.reservation.mapper.SysUserRoleMapper;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultDataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public DefaultDataInitializer(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));
        if (admin == null) {
            LocalDateTime now = LocalDateTime.now();
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("System Admin");
            admin.setEmail("admin@example.com");
            admin.setEnabled(true);
            admin.setCreatedAt(now);
            admin.setUpdatedAt(now);
            userMapper.insert(admin);
        }

        SysRole adminRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "ADMIN"));
        if (adminRole == null) {
            return;
        }

        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, admin.getId())
                .eq(SysUserRole::getRoleId, adminRole.getId()));
        if (count == 0) {
            LocalDateTime now = LocalDateTime.now();
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRole.setCreatedAt(now);
            userRole.setUpdatedAt(now);
            userRoleMapper.insert(userRole);
        }
    }
}

