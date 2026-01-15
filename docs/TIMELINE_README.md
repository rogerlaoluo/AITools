# Timeline App - 时间线社交应用

一个基于时间线的个人记录与社交分享平台，用户可以发布图片、视频、文字等内容，按时间线自动排列，其他人可以浏览、点赞、评论。

## 技术栈

### 后端
- Spring Boot 2.5.5
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Redis
- Java 8

### 前端
- Vue.js 3
- Pinia
- Vue Router
- Element Plus
- Vite
- Axios

## 项目结构

```
AiTools/
├── src/main/java/com/huawei/codearts/
│   ├── timeline/
│   │   ├── entity/          # 实体类
│   │   ├── repository/      # 数据访问层
│   │   ├── service/         # 业务逻辑层
│   │   ├── controller/      # 控制器层
│   │   ├── security/        # 安全配置
│   │   └── dto/             # 数据传输对象
│   └── controller/          # 原有控制器
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 可复用组件
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── router/          # 路由配置
│   │   └── api/             # API 配置
│   ├── package.json
│   └── vite.config.js
├── deploy/                  # 部署配置
│   ├── nginx.conf
│   ├── docker-compose.yml
│   └── Dockerfile.backend
└── docs/                    # 文档
```

## 快速开始

### 环境要求

- JDK 8+
- Node.js 16+
- MySQL 8.0
- Redis 7+
- Maven 3.3+

### 后端启动

1. 配置数据库连接（`src/main/resources/application.properties`）
2. 启动 MySQL 和 Redis
3. 运行后端：

```bash
mvn clean install
mvn spring-boot:run
```

后端将运行在 `http://localhost:8080`

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端将运行在 `http://localhost:3000`

### 使用 Docker Compose

```bash
cd deploy
docker-compose up -d
```

访问 `http://localhost` 即可

## API 文档

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 内容相关
- `POST /api/content` - 发布内容
- `GET /api/content/global` - 获取全局时间线
- `GET /api/content/{id}` - 获取单条内容详情
- `POST /api/content/{id}/like` - 点赞/取消点赞
- `POST /api/content/{id}/comment` - 发表评论
- `GET /api/content/{id}/comments` - 获取评论列表

### 用户相关
- `GET /api/users/{id}` - 获取用户资料
- `PUT /api/users/{id}` - 更新个人资料
- `GET /api/users/{id}/timeline` - 获取用户时间线
- `POST /api/users/{userId}/follow` - 关注/取消关注

### 发现功能
- `GET /api/explore` - 探索页
- `GET /api/search?q=keyword` - 搜索
- `GET /api/tags` - 获取标签列表
- `GET /api/tags/{id}/content` - 标签下的内容

## 主要功能

### Phase 1 - 核心功能
- [x] 用户注册登录
- [x] 发布内容（图片+文字）
- [x] 查看时间线
- [x] 点赞评论

### Phase 2 - 社交功能
- [x] 关注系统
- [x] 个人资料完善
- [ ] 通知系统（前端界面已创建，后端待实现）

### Phase 3 - 发现功能
- [x] 探索页面
- [x] 搜索功能
- [x] 标签系统

### Phase 4 - 推荐
- [ ] 基于标签的推荐算法
- [ ] 个性化首页

## 配置说明

### 数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/timeline_db
spring.datasource.username=root
spring.datasource.password=123456
```

### 文件存储配置
```properties
app.storage.location=./storage
spring.servlet.multipart.max-file-size=100MB
```

### JWT 配置
```properties
app.jwt.secret=your-secret-key
app.jwt.expiration=604800000
```

## 部署

### 使用 Nginx

1. 构建前端：`cd frontend && npm run build`
2. 构建后端：`mvn clean package`
3. 配置 Nginx（参考 `deploy/nginx.conf`）
4. 启动服务

### 使用 Docker

参考 `deploy/docker-compose.yml` 进行容器化部署。

## 许可证

MIT License
