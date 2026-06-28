# Java 工程师项目方案：企业会议室预约系统

## 项目定位

本项目是一个企业内部会议室预约系统，采用前后端分离架构。后端围绕预约业务中的认证鉴权、资源管理、冲突检测、并发控制、审批流、状态日志和异步通知展开；前端提供一个可直接联调的预约工作台。

项目适合作为 Java 后端求职项目展示，覆盖企业开发中常见的能力点：

- RESTful API 设计
- Spring Security + JWT 登录认证
- RBAC 角色权限控制
- MyBatis-Plus 数据访问和分页
- MySQL 表结构和索引设计
- 预约冲突检测
- Redis / Redisson 分布式锁
- RabbitMQ 异步消息
- 统一异常处理
- 单元测试
- Docker Compose 本地基础设施
- Vue 3 前端联调

## 技术选型

### 后端

| 技术 | 用途 |
| --- | --- |
| Java 17 | 后端开发语言 |
| Spring Boot 3 | 后端应用框架 |
| Spring MVC | REST API |
| Spring Security | 认证和授权 |
| JWT | 无状态登录凭证 |
| MyBatis-Plus | 数据访问、分页、基础 CRUD |
| MySQL 8 | 关系型数据库 |
| Redis | Redisson 分布式锁依赖 |
| Redisson | 并发预约锁 |
| RabbitMQ | 异步通知消息 |
| Knife4j / Swagger | 接口文档 |
| JUnit 5 / Mockito | 单元测试 |

### 前端

| 技术 | 用途 |
| --- | --- |
| Vue 3 | 前端框架 |
| TypeScript | 类型约束 |
| Vite | 前端构建工具 |
| Element Plus | 管理台 UI 组件 |
| Axios | HTTP 请求 |

### 基础设施

| 技术 | 用途 |
| --- | --- |
| Docker Compose | 本地启动 MySQL、Redis、RabbitMQ |
| MySQL 容器 | 数据存储 |
| Redis 容器 | 分布式锁 |
| RabbitMQ 容器 | 异步通知 |

## 角色和权限

当前角色：

- `EMPLOYEE`：普通员工
- `APPROVER`：审批员，预留角色
- `ADMIN`：管理员

已实现权限：

- 未登录只能访问健康检查和登录接口。
- 普通用户只能查看自己的预约和通知。
- 管理员可以管理会议室、查看全部预约、审批或拒绝预约。

## 已实现功能

### 登录认证

- `POST /api/auth/login`
- `GET /api/auth/me`
- JWT 生成和解析
- 自定义 JWT 过滤器
- BCrypt 密码校验
- 默认管理员：`admin / 123456`

### 会议室管理

- 会议室分页查询
- 会议室详情
- 管理员新增会议室
- 管理员编辑会议室
- 管理员禁用会议室
- 默认中文会议室初始化数据

### 预约流程

- 创建预约
- 查询预约列表
- 查询预约详情
- 取消预约
- 管理员审批通过
- 管理员拒绝
- 普通用户只允许访问自己的预约
- 管理员可查询全部预约

### 冲突检测

冲突规则：

```text
同一会议室
状态为 PENDING 或 APPROVED
已有预约 start_time < 新预约结束时间
已有预约 end_time > 新预约开始时间
```

冲突时返回 `409 CONFLICT`。

### 分布式锁

创建预约时使用 Redisson 锁保护冲突检测和插入：

```text
lock:appointment:room:{roomId}:{yyyy-MM-dd}
```

锁粒度是会议室和日期，避免同一会议室同一天的并发预约同时通过冲突检测。

### 状态日志

写入 `appointment_log`：

- 创建预约：`null -> PENDING`
- 审批通过：`PENDING -> APPROVED`
- 拒绝预约：`PENDING -> REJECTED`
- 取消预约：`PENDING/APPROVED -> CANCELLED`

### 通知

- 通知分页查询
- 单条标记已读
- 全部标记已读
- 审批、拒绝、取消时生成通知
- 使用 RabbitMQ 异步投递通知消息

RabbitMQ：

```text
exchange: reservation.notification.exchange
queue: reservation.notification.queue
routing key: reservation.notification.created
```

### 前端工作台

已实现：

- 登录页
- 会议室列表
- 创建预约
- 我的预约
- 管理员审批
- 通知中心
- JWT 请求头自动携带

## 当前项目结构

```text
java-1
├─ reservation-backend
│  ├─ src/main/java/com/example/reservation
│  │  ├─ auth
│  │  ├─ appointment
│  │  ├─ meetingroom
│  │  ├─ notification
│  │  ├─ security
│  │  ├─ mapper
│  │  ├─ domain
│  │  ├─ config
│  │  ├─ common
│  │  ├─ exception
│  │  └─ bootstrap
│  └─ src/main/resources/db
├─ reservation-frontend
│  └─ src
├─ docs
└─ docker-compose.yml
```

## 本地运行

启动基础设施：

```bash
docker compose up -d
```

启动后端：

```bash
cd reservation-backend
mvn spring-boot:run
```

启动前端：

```bash
cd reservation-frontend
npm install
npm run dev
```

访问：

```text
前端: http://localhost:5173
后端: http://localhost:8080
Knife4j: http://localhost:8080/doc.html
RabbitMQ 管理后台: http://localhost:15672
```

## 测试

后端：

```bash
cd reservation-backend
mvn test
```

前端：

```bash
cd reservation-frontend
npm run build
```

## 后续可扩展方向

优先级较高：

- 会议室开放时间 `room_schedule`
- 会议室不可预约时间段 `room_block_time`
- 预约日志查询接口
- 操作日志 `operation_log`
- 预约过期自动处理

前端可继续完善：

- 会议室新增、编辑、禁用页面
- 日历式预约视图
- 预约日志展示
- 通知轮询或 WebSocket 实时提醒

工程化可继续完善：

- GitHub Actions CI
- k6/JMeter 压测脚本
- Testcontainers 集成测试
- 生产环境配置拆分
