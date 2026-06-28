# 系统架构

## 总体结构

```text
Browser
  |
  | HTTP / JSON
  v
Vue 3 + Element Plus 前端
  |
  | /api 代理到 8080
  v
Spring Boot 后端
  |
  |-- MySQL: 用户、角色、会议室、预约、日志、通知
  |-- Redis: Redisson 分布式锁
  |-- RabbitMQ: 异步通知消息
```

## 后端分层

```text
controller  接收 HTTP 请求，做参数校验和权限入口
service     承载业务流程和事务边界
mapper      MyBatis-Plus 数据访问
domain      实体和枚举
config      MyBatis、RabbitMQ、分页等基础配置
security    JWT 和 Spring Security
common      统一响应和错误码
exception   全局异常处理
```

## 核心链路

### 登录链路

1. 前端调用 `POST /api/auth/login`。
2. 后端校验用户名、密码和用户启用状态。
3. 登录成功后生成 JWT。
4. 前端保存 token，后续请求携带 `Authorization: Bearer <token>`。
5. `JwtAuthenticationFilter` 解析 token 并写入 Spring Security 上下文。

### 创建预约链路

1. 前端提交会议室、主题、开始时间、结束时间。
2. 后端校验时间范围和会议室状态。
3. 根据 `roomId + 日期` 获取 Redisson 分布式锁。
4. 锁内执行预约冲突检测。
5. 写入 `appointment`，状态为 `PENDING`。
6. 写入 `appointment_log` 状态日志。
7. 释放锁。

锁 key：

```text
lock:appointment:room:{roomId}:{yyyy-MM-dd}
```

### 审批和通知链路

1. 管理员审批或拒绝预约。
2. 后端校验预约必须为 `PENDING`。
3. 审批通过前再次执行冲突检测。
4. 更新预约状态。
5. 写入 `appointment_log`。
6. 发布 RabbitMQ 通知消息。
7. `NotificationListener` 消费消息。
8. `NotificationService` 写入 `notification` 表。

RabbitMQ 配置：

```text
exchange:    reservation.notification.exchange
queue:       reservation.notification.queue
routing key: reservation.notification.created
```

## 权限模型

当前角色：

- `EMPLOYEE`
- `APPROVER`
- `ADMIN`

当前已实现的管理权限主要使用 `ADMIN`：

- 新增、编辑、禁用会议室
- 审批、拒绝预约
- 查询全部预约

普通用户只能：

- 查询会议室
- 创建预约
- 查询自己的预约
- 取消自己的预约
- 查询和处理自己的通知

## 前端架构

前端目前是一个轻量工作台，核心逻辑集中在 `src/App.vue`：

- 登录
- 会议室列表
- 创建预约
- 我的预约
- 管理员审批
- 通知中心

`vite.config.ts` 配置了代理：

```text
/api -> http://localhost:8080
```

## 当前边界

已有数据库表但暂未实现完整业务：

- `room_schedule`
- `room_block_time`
- `operation_log`

后续可以继续补会议室开放时间、不可预约时间段、操作审计、预约过期任务等能力。
