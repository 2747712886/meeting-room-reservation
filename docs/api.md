# 接口说明

所有业务接口默认返回统一结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-28T18:00:00"
}
```

除健康检查和登录外，其余接口需要请求头：

```http
Authorization: Bearer <accessToken>
```

## 健康检查

```http
GET /api/health
```

## 登录认证

### 登录

```http
POST /api/auth/login
Content-Type: application/json
```

请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应 `data`：

```json
{
  "tokenType": "Bearer",
  "accessToken": "jwt-token",
  "userId": 1,
  "username": "admin",
  "realName": "系统管理员",
  "roles": ["ADMIN"]
}
```

### 当前用户

```http
GET /api/auth/me
```

## 会议室

### 分页查询会议室

```http
GET /api/meeting-rooms?page=1&size=10
```

可选参数：

- `name`
- `floor`
- `enabled`
- `page`
- `size`

### 会议室详情

```http
GET /api/meeting-rooms/{id}
```

### 新增会议室

需要 `ADMIN`。

```http
POST /api/meeting-rooms
Content-Type: application/json
```

```json
{
  "name": "D401 董事会议室",
  "floor": "4楼",
  "capacity": 20,
  "hasProjector": true,
  "hasWhiteboard": true
}
```

### 编辑会议室

需要 `ADMIN`。

```http
PUT /api/meeting-rooms/{id}
```

### 禁用会议室

需要 `ADMIN`。

```http
POST /api/meeting-rooms/{id}/disable
```

## 预约

### 创建预约

```http
POST /api/appointments
Content-Type: application/json
```

```json
{
  "roomId": 1001,
  "subject": "项目周会",
  "startTime": "2026-06-29T09:00:00",
  "endTime": "2026-06-29T10:00:00"
}
```

创建后状态为 `PENDING`。

### 分页查询预约

```http
GET /api/appointments?page=1&size=10
```

可选参数：

- `roomId`
- `userId`
- `status`
- `startFrom`
- `startTo`
- `page`
- `size`

普通用户只能查询自己的预约；管理员可以查询全部预约。

### 预约详情

```http
GET /api/appointments/{id}
```

普通用户只能查看自己的预约；管理员可以查看全部预约。

### 取消预约

```http
POST /api/appointments/{id}/cancel
Content-Type: application/json
```

```json
{
  "cancelReason": "会议改期"
}
```

只有 `PENDING` 或 `APPROVED` 状态可以取消。

### 审批通过

需要 `ADMIN`。

```http
POST /api/appointments/{id}/approve
```

只有 `PENDING` 状态可以审批。审批前会再次执行冲突检测。

### 拒绝预约

需要 `ADMIN`。

```http
POST /api/appointments/{id}/reject
Content-Type: application/json
```

```json
{
  "rejectReason": "该时间段不可用"
}
```

## 通知

### 分页查询通知

```http
GET /api/notifications?page=1&size=10
```

可选参数：

- `readFlag`
- `page`
- `size`

只返回当前登录用户自己的通知。

### 标记单条已读

```http
POST /api/notifications/{id}/read
```

### 全部标记已读

```http
POST /api/notifications/read-all
```

## 状态码约定

业务错误通过 HTTP 状态码和统一响应体返回：

- `400`：请求参数错误
- `401`：未登录或用户名密码错误
- `403`：无权限
- `404`：资源不存在
- `409`：业务冲突，例如预约时间冲突
- `500`：服务端异常
