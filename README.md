# 企业会议室预约系统

一个前后端分离的会议室预约项目，面向企业内部办公场景。项目已经实现登录认证、会议室管理、预约创建、冲突检测、并发控制、管理员审批、状态日志、站内通知和 RabbitMQ 异步通知。

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Security
- JWT
- MyBatis-Plus
- MySQL 8
- Redis / Redisson
- RabbitMQ
- Knife4j / Swagger
- Maven
- JUnit 5 / Mockito

前端：

- Vue 3
- TypeScript
- Vite
- Element Plus
- Axios

基础设施：

- Docker
- Docker Compose
- MySQL
- Redis
- RabbitMQ

## 项目结构

```text
java-1
├─ reservation-backend       Spring Boot 后端
├─ reservation-frontend      Vue 3 前端
├─ docs                      项目说明文档
├─ docker-compose.yml        MySQL / Redis / RabbitMQ
├─ java-engineer-project-plan.md
└─ README.md
```

## 当前功能

- JWT 登录认证
- 当前用户信息查询
- 会议室分页查询、详情、新增、编辑、禁用
- 创建预约
- 我的预约查询
- 管理员查询全部预约
- 预约详情查询
- 取消预约
- 预约时间冲突检测
- Redisson 分布式锁防并发重复预约
- 管理员审批预约
- 管理员拒绝预约
- 预约状态流转日志
- 通知查询、单条已读、全部已读
- RabbitMQ 异步生成通知
- 前端工作台页面

## 本地启动

### 1. 启动基础设施

```bash
docker compose up -d
```

端口：

```text
MySQL:    localhost:3307
Redis:    localhost:6379
RabbitMQ: localhost:5672
RabbitMQ 管理后台: http://localhost:15672
```

RabbitMQ 账号：

```text
admin / 123456
```

MySQL 连接信息：

```text
database: meeting_room_reservation
username: reservation
password: reservation
```

### 2. 启动后端

```bash
cd reservation-backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

健康检查：

```http
GET /api/health
```

Swagger / Knife4j：

```text
http://localhost:8080/doc.html
http://localhost:8080/swagger-ui.html
```

### 3. 启动前端

```bash
cd reservation-frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

Vite 已配置代理，前端请求 `/api` 会转发到 `http://localhost:8080`。

## 默认账号

```text
用户名: admin
密码: 123456
角色: ADMIN
```

默认初始化数据：

- A101 小型会议室
- B201 项目会议室
- C301 培训会议室

## 核心业务规则

创建预约时会进行冲突检测：

```text
同一会议室
状态为 PENDING 或 APPROVED
已有预约 start_time < 新预约 endTime
已有预约 end_time > 新预约 startTime
```

为了防止并发请求同时通过冲突检测，创建预约时会使用 Redisson 分布式锁：

```text
lock:appointment:room:{roomId}:{yyyy-MM-dd}
```

预约状态变化后会写入 `appointment_log`，审批、拒绝、取消会通过 RabbitMQ 发布通知消息，再由消费者写入 `notification` 表。

## 常用命令

后端测试：

```bash
cd reservation-backend
mvn test
```

前端构建：

```bash
cd reservation-frontend
npm run build
```

查看容器：

```bash
docker compose ps
```

## 文档

- [接口说明](docs/api.md)
- [系统架构](docs/architecture.md)
- [数据库设计](docs/database-design.md)
- [压测说明](docs/pressure-test.md)
- [项目方案](java-engineer-project-plan.md)
