# 数据库设计

## 核心表

- `sys_user`：用户表
- `sys_role`：角色表
- `sys_user_role`：用户角色关联表
- `meeting_room`：会议室表
- `room_schedule`：会议室开放时间表
- `room_block_time`：会议室不可预约时间表
- `appointment`：预约表
- `appointment_log`：预约状态变更日志表
- `notification`：通知表
- `operation_log`：操作日志表

## 预约表

```sql
CREATE TABLE appointment (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    reject_reason VARCHAR(255),
    cancel_reason VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

