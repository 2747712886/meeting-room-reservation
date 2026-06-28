INSERT INTO sys_role (id, role_code, role_name, created_at, updated_at)
VALUES
    (1, 'EMPLOYEE', 'Employee', NOW(), NOW()),
    (2, 'APPROVER', 'Approver', NOW(), NOW()),
    (3, 'ADMIN', 'Admin', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    updated_at = NOW();

INSERT INTO meeting_room (id, name, floor, capacity, has_projector, has_whiteboard, enabled, created_at, updated_at)
VALUES
    (1001, 'A101 Small Meeting Room', '1F', 6, 1, 1, 1, NOW(), NOW()),
    (1002, 'B201 Project Room', '2F', 12, 1, 1, 1, NOW(), NOW()),
    (1003, 'C301 Training Room', '3F', 30, 1, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    floor = VALUES(floor),
    capacity = VALUES(capacity),
    has_projector = VALUES(has_projector),
    has_whiteboard = VALUES(has_whiteboard),
    enabled = VALUES(enabled),
    updated_at = NOW();
