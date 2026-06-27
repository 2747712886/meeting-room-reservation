# 接口文档

## 健康检查

```http
GET /api/health
```

响应示例：

```json
{
  "status": "UP",
  "service": "reservation-backend",
  "time": "2026-06-27T20:00:00"
}
```

## 后续接口规划

- `POST /api/auth/login`：用户登录
- `GET /api/meeting-rooms`：会议室列表
- `POST /api/appointments`：创建预约
- `POST /api/appointments/{id}/approve`：审核通过
- `POST /api/appointments/{id}/reject`：审核拒绝

