INSERT INTO sys_role (id, role_code, role_name, created_at, updated_at)
VALUES
    (1, 'EMPLOYEE', '员工', NOW(), NOW()),
    (2, 'APPROVER', '审批员', NOW(), NOW()),
    (3, 'ADMIN', '管理员', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    updated_at = NOW();

INSERT INTO meeting_room (id, name, floor, capacity, has_projector, has_whiteboard, enabled, created_at, updated_at)
VALUES
    (1001, 'A101 小型会议室', '1楼', 6, 1, 1, 1, NOW(), NOW()),
    (1002, 'B201 项目会议室', '2楼', 12, 1, 1, 1, NOW(), NOW()),
    (1003, 'C301 培训会议室', '3楼', 30, 1, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    floor = VALUES(floor),
    capacity = VALUES(capacity),
    has_projector = VALUES(has_projector),
    has_whiteboard = VALUES(has_whiteboard),
    enabled = VALUES(enabled),
    updated_at = NOW();
