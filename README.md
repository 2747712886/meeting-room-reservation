# 企业会议室预约与智能排班系统

一个面向 Java 后端求职的企业级会议室预约项目，覆盖登录认证、角色权限、会议室管理、排班配置、预约冲突检测、并发控制、缓存、异步通知、定时任务、接口文档、测试和容器化部署。

## 技术栈

### 后端

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Security
- JWT
- MyBatis-Plus
- MySQL 8
- Redis
- Redisson
- RabbitMQ
- Spring Task
- Knife4j / Swagger
- Maven

### 前端

- Vue 3
- TypeScript
- Vite
- Element Plus
- Axios
- Pinia

### 工程化

- Docker
- Docker Compose
- GitHub Actions
- JUnit 5
- Mockito

## 项目结构

```text
meeting-room-reservation
├── reservation-backend
├── reservation-frontend
├── docs
├── docker-compose.yml
├── java-engineer-project-plan.md
└── README.md
```

## 当前进度

- [x] 初始化 Git 仓库
- [x] 创建后端基础工程
- [x] 创建前端基础工程
- [x] 创建基础文档目录
- [ ] 实现用户登录与 JWT 鉴权
- [ ] 实现会议室管理
- [ ] 实现预约冲突检测
- [ ] 接入 Redis、Redisson、RabbitMQ
- [ ] 补充 Docker Compose 可运行环境

## 本地启动

### 后端

```bash
cd reservation-backend
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

健康检查：

```text
GET /api/health
```

### 前端

```bash
cd reservation-frontend
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

