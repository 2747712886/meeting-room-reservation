# 数据库设计

数据库：

```text
meeting_room_reservation
```

本地连接：

```text
host: localhost
port: 3307
username: reservation
password: reservation
```

建表脚本：

```text
reservation-backend/src/main/resources/db/schema.sql
```

初始化数据：

```text
reservation-backend/src/main/resources/db/data.sql
```

## 表清单

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_user_role` | 用户角色关联表 |
| `meeting_room` | 会议室表 |
| `room_schedule` | 会议室开放时间表，已建表，业务待实现 |
| `room_block_time` | 会议室不可预约时间表，已建表，业务待实现 |
| `appointment` | 预约表 |
| `appointment_log` | 预约状态日志表 |
| `notification` | 通知表 |
| `operation_log` | 操作日志表，已建表，业务待实现 |

## 用户和权限

### sys_user

核心字段：

- `id`
- `username`
- `password`
- `real_name`
- `email`
- `enabled`
- `created_at`
- `updated_at`

`username` 有唯一索引。

默认管理员：

```text
admin / 123456
```

### sys_role

默认角色：

| role_code | role_name |
| --- | --- |
| `EMPLOYEE` | 员工 |
| `APPROVER` | 审批员 |
| `ADMIN` | 管理员 |

权限判断使用 `role_code`，不要把 `ADMIN` 等 code 改成中文。

## 会议室

### meeting_room

核心字段：

- `name`
- `floor`
- `capacity`
- `has_projector`
- `has_whiteboard`
- `enabled`

默认会议室：

- A101 小型会议室
- B201 项目会议室
- C301 培训会议室

## 预约

### appointment

核心字段：

- `user_id`
- `room_id`
- `subject`
- `start_time`
- `end_time`
- `status`
- `reject_reason`
- `cancel_reason`

状态枚举：

- `PENDING`
- `APPROVED`
- `REJECTED`
- `CANCELLED`
- `COMPLETED`
- `EXPIRED`

关键索引：

```sql
KEY idx_appointment_room_time (room_id, start_time, end_time)
KEY idx_appointment_user_created (user_id, created_at)
KEY idx_appointment_status_created (status, created_at)
```

冲突检测逻辑：

```sql
room_id = ?
AND status IN ('PENDING', 'APPROVED')
AND start_time < 新预约结束时间
AND end_time > 新预约开始时间
```

### appointment_log

用于记录预约状态流转：

- `appointment_id`
- `old_status`
- `new_status`
- `operator_id`
- `remark`

当前写入场景：

- 创建预约：`null -> PENDING`
- 审批通过：`PENDING -> APPROVED`
- 拒绝预约：`PENDING -> REJECTED`
- 取消预约：`PENDING/APPROVED -> CANCELLED`

## 通知

### notification

核心字段：

- `user_id`
- `appointment_id`
- `event_type`
- `title`
- `content`
- `read_flag`

关键索引：

```sql
KEY idx_notification_user_read (user_id, read_flag)
KEY idx_notification_appointment_id (appointment_id)
```

当前事件类型：

- `APPOINTMENT_APPROVED`
- `APPOINTMENT_REJECTED`
- `APPOINTMENT_CANCELLED`

通知通过 RabbitMQ 异步生成。

## 初始化策略

`data.sql` 对角色和会议室使用 `ON DUPLICATE KEY UPDATE`。

这意味着应用启动执行 SQL 初始化时，如果数据已存在，会更新中文名称、楼层、设备等字段。
