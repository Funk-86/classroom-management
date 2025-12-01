# MySQL数据库配置说明

## 问题修复

已修复登录时出现的数据库连接失败问题。主要修改包括：

1. **数据库配置**：将 `application.yml` 从 H2 数据库改为 MySQL
2. **异常处理**：改进了登录和定时任务的异常处理
3. **连接池配置**：优化了 HikariCP 连接池参数

## 配置方式

### 方式1：使用环境变量（推荐）

在启动应用前设置环境变量：

**Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="classroom_management"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:DB_SSL="false"
```

**Windows (CMD):**
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=classroom_management
set DB_USERNAME=root
set DB_PASSWORD=your_password
set DB_SSL=false
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=classroom_management
export DB_USERNAME=root
export DB_PASSWORD=your_password
export DB_SSL=false
```

### 方式2：直接修改 application.yml

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/classroom_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&autoReconnect=true
    username: root
    password: your_password
```

## 数据库准备

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS classroom_management 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 2. 创建必要的表

确保数据库中已创建以下表：
- `users` - 用户表
- `user_session` - 用户会话表
- `classrooms` - 教室表
- `buildings` - 教学楼表
- `campuses` - 校区表
- `reservations` - 预约表
- `announcements` - 公告表
- `courses` - 课程表
- `course_schedules` - 课程安排表
- 等其他业务表

### 3. 检查表结构

确保 `user_session` 表存在且包含以下字段：
- `session_id` (VARCHAR, PRIMARY KEY)
- `user_id` (VARCHAR)
- `login_time` (DATETIME)
- `expire_time` (DATETIME)
- `last_access_time` (DATETIME)
- `create_time` (DATETIME)
- `update_time` (DATETIME)

## 验证配置

### 1. 检查数据库连接

启动应用后，查看日志中是否有以下信息：
- ✅ "Started ClassroomApplication" - 应用启动成功
- ❌ "Failed to obtain JDBC Connection" - 数据库连接失败

### 2. 测试登录

使用微信小程序或Postman测试登录接口：
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password",
  "role": 0
}
```

## 常见问题

### 问题1：连接被拒绝
**错误信息**：`Communications link failure` 或 `Connection refused`

**解决方案**：
1. 检查MySQL服务是否启动
2. 检查端口号是否正确（默认3306）
3. 检查防火墙设置

### 问题2：认证失败
**错误信息**：`Access denied for user`

**解决方案**：
1. 检查用户名和密码是否正确
2. 检查用户是否有访问该数据库的权限
3. 尝试使用MySQL客户端直接连接验证

### 问题3：时区问题
**错误信息**：`The server time zone value 'xxx' is unrecognized`

**解决方案**：
- URL中已包含 `serverTimezone=Asia/Shanghai`，如果仍有问题，检查MySQL时区设置

### 问题4：SSL连接问题
**错误信息**：`SSL connection error`

**解决方案**：
- 确保URL中包含 `useSSL=false`（开发环境）
- 或配置正确的SSL证书（生产环境）

## 连接池配置说明

当前配置的HikariCP连接池参数：
- `maximum-pool-size: 20` - 最大连接数
- `minimum-idle: 5` - 最小空闲连接数
- `connection-timeout: 30000` - 连接超时时间（30秒）
- `idle-timeout: 600000` - 空闲连接超时（10分钟）
- `max-lifetime: 1800000` - 连接最大生存时间（30分钟）

## 生产环境建议

1. **使用环境变量**：不要将数据库密码硬编码在配置文件中
2. **启用SSL**：生产环境应启用SSL连接
3. **连接池调优**：根据实际负载调整连接池大小
4. **监控连接**：添加数据库连接监控和告警
5. **备份策略**：定期备份数据库

## 修复内容总结

1. ✅ 修改数据库配置为MySQL
2. ✅ 添加数据库连接重试机制（autoReconnect=true）
3. ✅ 改进登录方法的异常处理
4. ✅ 改进定时任务的异常处理，避免影响登录功能
5. ✅ 优化连接池配置参数



