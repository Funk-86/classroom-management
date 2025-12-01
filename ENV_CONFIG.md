# 环境变量配置说明

## 数据库配置

项目已改为使用环境变量配置敏感信息，请设置以下环境变量：

### Windows (PowerShell)
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="classroom_management"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:DB_SSL="false"
```

### Linux/Mac
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=classroom_management
export DB_USERNAME=root
export DB_PASSWORD=your_password
export DB_SSL=false
```

### 使用 .env 文件（推荐）

在项目根目录创建 `.env` 文件：
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=classroom_management
DB_USERNAME=root
DB_PASSWORD=your_password
DB_SSL=false
```

## CORS 配置

在 `application.yml` 中配置允许的前端域名：
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:8080
```

## 默认值

如果未设置环境变量，将使用以下默认值：
- DB_HOST: localhost
- DB_PORT: 3306
- DB_NAME: classroom_management
- DB_USERNAME: root
- DB_PASSWORD: (空)
- DB_SSL: false

## 安全建议

1. **生产环境**：必须使用环境变量或配置中心管理敏感信息
2. **不要**将 `.env` 文件提交到版本控制系统
3. 使用强密码，定期更换
4. 限制数据库访问权限

