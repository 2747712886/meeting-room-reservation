# Java 工程师求职项目方案

## 项目名称

企业会议室预约与智能排班系统

## 项目定位

这是一个面向企业内部办公场景的会议室预约系统。项目不只做基础 CRUD，而是围绕真实后端业务中的预约冲突、并发控制、权限认证、缓存一致性、异步通知、定时任务、接口文档、自动化测试和容器化部署展开。

这个项目适合放在 Java 后端简历中，因为它能覆盖企业后端开发中常见的核心能力：

- RESTful API 设计
- 用户认证与角色权限控制
- 复杂业务状态流转
- 数据库表结构与索引设计
- 高并发预约冲突处理
- Redis 缓存与分布式锁
- RabbitMQ 异步解耦
- Spring Task 定时任务
- Docker Compose 一键启动
- 单元测试、接口文档、CI 流程

简历项目描述可以写成：

> 基于 Spring Boot 3、MySQL、Redis、Redisson、RabbitMQ 和 Vue 3 实现企业会议室预约与智能排班系统，支持会议室管理、排班配置、预约申请、预约审核、冲突检测、并发预约控制、异步通知、权限管理、操作日志和定时任务处理，并通过 Docker Compose 提供本地一键启动能力。

## 推荐技术栈

### 后端

| 技术 | 作用 |
| --- | --- |
| Java 17 | 项目基础语言版本，匹配 Spring Boot 3 要求 |
| Spring Boot 3.x | 后端应用框架，负责快速搭建 Web 服务 |
| Spring MVC | 提供 RESTful API 接口 |
| Spring Security | 认证、鉴权、接口访问控制 |
| JWT | 无状态登录凭证 |
| MyBatis-Plus | 数据访问层，简化 CRUD 和分页查询 |
| MySQL 8 | 关系型数据库，存储用户、会议室、预约等核心数据 |
| Redis | 缓存会议室信息、可预约时间段、用户权限等数据 |
| Redisson | 实现分布式锁，解决并发预约问题 |
| RabbitMQ | 异步通知，解耦预约主流程和通知流程 |
| Spring Task | 定时关闭过期预约、发送会议提醒 |
| Knife4j / Swagger | 生成接口文档，便于联调和展示 |
| Maven | 项目构建、依赖管理 |

### 前端

| 技术 | 作用 |
| --- | --- |
| Vue 3 | 前端主框架 |
| TypeScript | 提升前端代码类型约束 |
| Vite | 前端构建工具 |
| Element Plus | 后台管理页面 UI 组件库 |
| Axios | HTTP 请求封装 |
| Pinia | 前端状态管理 |

### 工程化、测试与部署

| 技术 | 作用 |
| --- | --- |
| Docker | 容器化部署应用 |
| Docker Compose | 一键启动 MySQL、Redis、RabbitMQ、后端、前端 |
| GitHub Actions | 自动化构建和测试 |
| JUnit 5 | Java 单元测试框架 |
| Mockito | Mock 外部依赖，测试业务逻辑 |
| Testcontainers | 可选，用真实容器测试 MySQL、Redis、RabbitMQ |
| JMeter / k6 | 可选，用于预约接口压测 |

## 核心业务角色

### 普通员工

- 查看会议室列表
- 查看会议室可预约时间
- 发起预约
- 取消预约
- 查看自己的预约记录
- 接收预约结果和会议提醒

### 审核员

- 查看待审核预约
- 审核预约申请
- 查看预约状态变更记录

### 管理员

- 管理用户
- 管理角色权限
- 管理会议室
- 配置会议室开放时间
- 配置不可预约时间段
- 查看预约统计
- 查看操作日志

## 核心功能模块

## 1. 用户与权限模块

### 功能

- 用户注册
- 用户登录
- JWT 鉴权
- 角色权限控制
- 用户信息维护
- 密码加密存储
- 登录失败统一处理

### 关键技术点

- 使用 Spring Security 构建认证和授权流程
- 登录成功后生成 JWT Token
- 请求进入系统时通过过滤器解析 Token
- 使用角色控制接口访问权限
- 使用 BCrypt 存储密码，避免明文密码入库

### 面试讲法

> 我在项目中使用 Spring Security + JWT 实现无状态登录。用户登录成功后后端生成 Token，前端后续请求在请求头中携带 Token。后端通过自定义过滤器解析 Token，获取用户身份和权限信息，再交给 Spring Security 判断当前用户是否有权限访问接口。

## 2. 会议室管理模块

### 功能

- 新增会议室
- 编辑会议室
- 禁用会议室
- 查询会议室
- 维护会议室容量、位置、设备标签

### 会议室字段示例

- 名称
- 楼层
- 容量
- 是否有投影
- 是否有白板
- 是否启用
- 创建时间
- 更新时间

### 关键技术点

- 使用 MyBatis-Plus 完成分页查询
- 使用 Redis 缓存会议室详情
- 修改会议室信息后删除缓存，下一次查询重新加载

## 3. 排班配置模块

### 功能

- 配置会议室可预约日期
- 配置每日开放时间
- 配置不可预约时间段
- 支持节假日关闭
- 支持临时维护时间段

### 关键技术点

- 校验开始时间必须早于结束时间
- 校验不可预约时间段不能重叠
- 按日期范围查询会议室开放配置
- 为预约模块提供可预约时间计算基础

## 4. 预约模块

### 功能

- 查询会议室可预约时间
- 创建预约
- 取消预约
- 审核预约
- 查询预约记录
- 维护预约状态流转

### 预约状态建议

```text
PENDING    待审核
APPROVED   已通过
REJECTED   已拒绝
CANCELLED  已取消
COMPLETED  已完成
EXPIRED    已过期
```

### 状态流转建议

```text
PENDING -> APPROVED
PENDING -> REJECTED
PENDING -> CANCELLED
APPROVED -> CANCELLED
APPROVED -> COMPLETED
PENDING -> EXPIRED
```

### 关键技术点

- 预约时间冲突检测
- 数据库唯一约束或事务兜底
- Redisson 分布式锁控制并发预约
- 状态机式业务校验，避免非法状态变更

### 面试讲法

> 预约模块的难点不是新增一条记录，而是避免同一个会议室在同一时间段被重复预约。我先用 Redisson 根据 roomId 和日期加分布式锁，然后在事务中查询数据库判断时间段是否冲突，确认无冲突后才写入预约记录。这样可以同时控制多实例部署下的并发问题和数据库最终一致性问题。

## 5. 异步通知模块

### 功能

- 预约成功通知
- 审核结果通知
- 预约取消通知
- 会议即将开始提醒

### 实现方式

- 预约主流程只负责写入数据库
- 主流程成功后发送 RabbitMQ 消息
- 消费者异步生成通知记录
- 后续可扩展短信、邮件、企业微信通知

### 消息类型

```text
APPOINTMENT_CREATED
APPOINTMENT_APPROVED
APPOINTMENT_REJECTED
APPOINTMENT_CANCELLED
APPOINTMENT_REMINDER
```

### 消息体示例

```json
{
  "eventType": "APPOINTMENT_APPROVED",
  "appointmentId": 10001,
  "userId": 20001,
  "roomId": 30001,
  "occurredAt": "2026-06-27T10:00:00"
}
```

## 6. 定时任务模块

### 功能

- 自动关闭过期未审核预约
- 自动完成已结束预约
- 提前提醒即将开始的会议
- 定期清理无效 Token，作为可选功能

### 关键技术点

- 使用 Spring Task 定时扫描数据
- 分页处理，避免一次性加载大量数据
- 使用幂等逻辑，避免重复发送通知
- 如果后期部署多实例，可升级为 XXL-JOB 或 ShedLock

## 7. 日志与审计模块

### 功能

- 操作日志
- 登录日志
- 预约状态变更日志
- 异常日志

### 关键技术点

- 使用 AOP 记录操作日志
- 使用全局异常处理统一返回错误信息
- 使用参数校验减少脏数据进入业务层
- 记录关键业务操作，方便问题追踪

## 数据库设计

### 核心表

| 表名 | 说明 |
| --- | --- |
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_user_role | 用户角色关联表 |
| meeting_room | 会议室表 |
| room_schedule | 会议室开放时间表 |
| room_block_time | 会议室不可预约时间表 |
| appointment | 预约表 |
| appointment_log | 预约状态变更日志表 |
| notification | 通知表 |
| operation_log | 操作日志表 |

### appointment 表建议

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

### 建议索引

```sql
CREATE INDEX idx_appointment_room_time
ON appointment(room_id, start_time, end_time);

CREATE INDEX idx_appointment_user_created
ON appointment(user_id, created_at);

CREATE INDEX idx_appointment_status_created
ON appointment(status, created_at);
```

## 核心难点设计

## 1. 预约冲突检测

判断一个新预约是否与已有预约冲突：

```sql
SELECT COUNT(1)
FROM appointment
WHERE room_id = ?
  AND status IN ('PENDING', 'APPROVED')
  AND start_time < ?
  AND end_time > ?;
```

参数含义：

- 第一个时间参数是新预约的结束时间
- 第二个时间参数是新预约的开始时间

判断逻辑：

- 已有预约开始时间小于新预约结束时间
- 已有预约结束时间大于新预约开始时间
- 两个条件同时满足，说明时间段有重叠

## 2. 并发预约控制

推荐流程：

```text
1. 根据 room_id + 日期生成锁 key
2. 获取 Redisson 分布式锁
3. 开启数据库事务
4. 查询数据库确认时间段是否冲突
5. 无冲突则创建预约
6. 提交事务
7. 释放锁
8. 发送 RabbitMQ 通知消息
```

锁 key 示例：

```text
lock:appointment:room:1001:2026-06-27
```

## 3. 缓存设计

适合缓存的数据：

- 会议室基础信息
- 热门会议室可预约时间段
- 用户权限信息

缓存 key 示例：

```text
room:detail:{roomId}
room:available-slots:{roomId}:{date}
user:permission:{userId}
```

缓存一致性策略：

- 更新会议室配置时删除会议室详情缓存
- 创建或取消预约后删除对应日期的可预约时间缓存
- 下一次查询时重新加载缓存
- 对热点数据设置合理过期时间，避免长期脏数据

## 4. RabbitMQ 消息设计

推荐使用 Topic Exchange，根据事件类型路由到不同队列：

```text
appointment.created
appointment.approved
appointment.rejected
appointment.cancelled
appointment.reminder
```

消费者处理要求：

- 消费消息时先判断通知记录是否已存在，保证幂等
- 消费失败时允许重试
- 对持续失败的消息可以进入死信队列，便于后续排查

## 推荐项目目录

```text
meeting-room-reservation
├── reservation-backend
│   ├── src/main/java
│   ├── src/main/resources
│   ├── src/test/java
│   └── pom.xml
├── reservation-frontend
│   ├── src
│   └── package.json
├── docs
│   ├── architecture.md
│   ├── database-design.md
│   ├── api.md
│   └── pressure-test.md
├── docker-compose.yml
└── README.md
```

## GitHub README 必须展示的内容

- 项目介绍
- 技术栈
- 系统架构图
- 核心功能截图
- 数据库 ER 图
- 本地启动方式
- Docker Compose 启动方式
- 核心接口文档地址
- 核心难点说明
- 测试说明
- 压测结果，可选
- 未来优化方向

## 简历写法

### 项目名称

企业会议室预约与智能排班系统

### 项目描述

基于 Spring Boot 3、MySQL、Redis、Redisson、RabbitMQ 和 Vue 3 实现企业会议室预约系统，支持会议室管理、排班配置、预约审核、冲突检测、并发预约控制、异步通知和定时任务处理。

### 项目职责

- 负责后端整体架构设计，基于 Spring Boot 3 搭建 RESTful API 服务
- 使用 Spring Security + JWT 实现登录认证和角色权限控制
- 设计预约状态流转模型，规范处理待审核、已通过、已拒绝、已取消、已完成等状态
- 使用 Redisson 分布式锁解决同一会议室同一时间段并发预约导致的重复占用问题
- 使用 Redis 缓存会议室详情和可预约时间段，降低数据库查询压力
- 使用 RabbitMQ 解耦预约通知流程，提高预约接口响应速度
- 使用 Spring Task 实现过期预约自动关闭和会议开始前提醒
- 编写核心预约逻辑单元测试，并通过 Docker Compose 实现 MySQL、Redis、RabbitMQ 一键启动

## 开发 To Do List

- [ ] 初始化 Git 仓库
- [ ] 搭建 Spring Boot 后端工程
- [ ] 搭建 Vue 3 前端工程
- [ ] 设计数据库表结构
- [ ] 实现用户登录与 JWT 鉴权
- [ ] 实现角色权限控制
- [ ] 实现会议室管理
- [ ] 实现排班配置
- [ ] 实现预约创建、取消、审核
- [ ] 实现预约冲突检测
- [ ] 接入 Redis 缓存
- [ ] 接入 Redisson 分布式锁
- [ ] 接入 RabbitMQ 异步通知
- [ ] 实现定时任务
- [ ] 实现操作日志
- [ ] 编写单元测试
- [ ] 编写接口文档
- [ ] 编写 Docker Compose
- [ ] 整理 README
- [ ] 上传 GitHub

## 推荐开发顺序

### 第一阶段：基础闭环

目标：先让项目能跑起来。

1. 用户登录
2. 会议室管理
3. 预约创建
4. 预约查询
5. 预约取消

### 第二阶段：后端深度

目标：做出简历亮点。

1. 预约冲突检测
2. Redisson 分布式锁
3. Redis 缓存
4. RabbitMQ 异步通知
5. Spring Task 定时任务

### 第三阶段：工程化

目标：让 GitHub 项目看起来像真实项目。

1. 单元测试
2. 接口文档
3. Docker Compose
4. GitHub Actions
5. README
6. 架构图与数据库图

## 最终交付效果

一个合格的 GitHub 求职项目应该具备：

- 能本地启动
- 能通过 Docker Compose 一键启动
- 有清晰 README
- 有接口文档
- 有数据库脚本
- 有核心业务截图
- 有核心测试用例
- 有清晰的 commit 记录
- 能在面试中讲清楚冲突检测、分布式锁、缓存一致性和异步消息

## 面试重点准备

面试前至少要能讲清楚以下问题：

1. 为什么预约会出现并发冲突？
2. 为什么只靠前端校验不够？
3. 为什么需要 Redisson 分布式锁？
4. Redis 缓存更新时如何保证数据一致性？
5. RabbitMQ 在项目中解决了什么问题？
6. 定时任务如何避免重复处理？
7. 数据库索引为什么这样设计？
8. Spring Security + JWT 的认证流程是什么？
9. Docker Compose 启动了哪些服务？
10. 这个项目如果上线，还有哪些可以优化的地方？
