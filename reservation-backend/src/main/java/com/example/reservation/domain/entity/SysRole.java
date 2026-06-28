package com.example.reservation.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String roleCode;
    private String roleName;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
