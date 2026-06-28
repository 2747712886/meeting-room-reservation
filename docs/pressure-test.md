# 压测说明

## 目标

压测重点是预约创建接口：

```http
POST /api/appointments
```

需要验证在高并发情况下，同一会议室同一时间段不会被重复预约。

## 关键业务点

创建预约时后端会执行：

1. 参数校验。
2. 会议室存在且启用校验。
3. 根据 `roomId + 日期` 获取 Redisson 分布式锁。
4. 锁内执行冲突检测。
5. 锁内插入预约。
6. finally 释放锁。

锁 key：

```text
lock:appointment:room:{roomId}:{yyyy-MM-dd}
```

冲突条件：

```text
同一会议室
状态为 PENDING 或 APPROVED
已有预约 start_time < 新预约 endTime
已有预约 end_time > 新预约 startTime
```

## 建议压测场景

### 场景 1：同一会议室同一时间段

并发请求全部使用相同参数：

```json
{
  "roomId": 1001,
  "subject": "并发预约测试",
  "startTime": "2026-06-29T09:00:00",
  "endTime": "2026-06-29T10:00:00"
}
```

预期结果：

- 最多只有 1 条成功创建。
- 其他请求返回 `409 CONFLICT`。
- 数据库中同一会议室同一时间段不会出现重复占用。

### 场景 2：同一会议室不同时间段

请求使用同一 `roomId`，但时间段不重叠。

预期结果：

- 不冲突的预约可以成功。
- 同一天同会议室会串行进入锁内逻辑，但不会误判冲突。

### 场景 3：不同会议室同一时间段

请求使用不同 `roomId`，时间段相同。

预期结果：

- 不同会议室互不冲突。
- 锁 key 不同，可以并行处理。

## 关注指标

- 并发用户数
- 总请求数
- 成功数
- 409 冲突数
- 500 错误数
- 平均响应时间
- P95 响应时间
- 数据库最终预约记录数

## 准备步骤

1. 启动基础设施：

```bash
docker compose up -d
```

2. 启动后端：

```bash
cd reservation-backend
mvn spring-boot:run
```

3. 登录获取 token：

```http
POST /api/auth/login
```

4. 压测请求携带：

```http
Authorization: Bearer <accessToken>
```

## 验证 SQL

压测后可以检查预约记录：

```sql
SELECT room_id, start_time, end_time, status, COUNT(*) AS count
FROM appointment
WHERE status IN ('PENDING', 'APPROVED')
GROUP BY room_id, start_time, end_time, status
HAVING COUNT(*) > 1;
```

如果返回空结果，说明没有重复占用。

## 工具建议

可以使用：

- JMeter
- k6
- ApacheBench
- Postman Runner

当前仓库还没有内置压测脚本，后续可以补充 k6 脚本并纳入 `docs`。
