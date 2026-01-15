# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

这是一个**时间线应用（Timeline Application）** - 一个社交媒体平台，用户可以在时间线动态中分享图片、视频和文字内容。它最初是用于展示华为 CodeArts CI/CD 能力的 Spring Boot 演示模板，后来演变成一个功能完整的社交应用。

**核心技术栈：**
- **后端：** Spring Boot 2.7.18, Java 17, Spring Data JPA, Spring Security, JWT
- **前端：** Vue 3 (Composition API), Pinia, Vue Router, Element Plus, Vite
- **数据库：** MySQL 8.0 with Hibernate ORM
- **缓存：** Redis 用于会话和缓存
- **部署：** Docker Compose (MySQL + Redis + Backend + Nginx)

**项目坐标：**
- GroupId: `com.huawei.codearts`
- ArtifactId: `demoapp`
- Packaging: `jar`

## 构建命令

### 后端 (Maven)
```bash
# 完整清理构建（运行测试并打包）
mvn clean install

# 仅编译
mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassName

# 打包（跳过测试）
mvn package -DskipTests

# 运行 Spring Boot 应用
mvn spring-boot:run

# 运行打包后的 JAR
java -jar ./target/demoapp.jar
```

**构建输出：** `target/demoapp.jar`（通过 `<finalName>${project.artifactId}</finalName>` 配置）

### 前端 (NPM)
```bash
cd frontend

# 开发服务器 (http://localhost:3000)
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### Docker (全栈)
```bash
cd deploy

# 启动所有服务 (MySQL, Redis, Backend, Frontend)
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除卷
docker-compose down -v
```

## 架构设计

### 后端结构

后端采用**分层架构**模式：

```
com.huawei.codearts.timeline/
├── controller/          # REST API 端点
│   ├── AuthController      # POST /api/auth/register, /api/auth/login
│   ├── ContentController   # 内容的 CRUD、点赞、评论
│   ├── UserController      # 用户资料、关注/取消关注
│   ├── ExploreController   # 探索页面、热门内容
│   └── MediaController     # 文件上传处理
├── service/            # 业务逻辑层
│   ├── AuthService        # 认证逻辑
│   ├── ContentService     # 内容管理
│   ├── UserService        # 用户管理
│   └── ExploreService     # 发现和推荐
├── repository/         # Spring Data JPA 仓储
├── entity/             # JPA 实体（数据库表）
├── dto/                # 数据传输对象（API 契约）
└── security/           # JWT 认证、Spring Security 配置
    ├── SecurityConfig           # 主安全配置
    ├── JwtTokenProvider         # JWT 生成/验证
    ├── JwtAuthenticationFilter  # JWT 令牌处理过滤器
    ├── CustomUserDetailsService # 认证用户加载
    └── UserPrincipal            # 认证用户主体
```

**入口点：** `JavaWebDemoApplication.java`

**关键架构模式：**
- **DTO 模式：** 将 API 请求/响应的 DTO 与 JPA 实体分离
- **服务层：** 业务逻辑与控制器隔离
- **仓储模式：** 使用 Spring Data JPA 进行数据访问
- **JWT 无状态：** 无服务端会话，JWT 令牌存储在 Redis 中
- **级联删除：** JPA 实体配置了 `CascadeType.DELETE`

### 安全架构

**JWT 认证流程：**
1. 用户登录 → `AuthService` 验证凭据
2. `JwtTokenProvider` 生成 JWT（7天有效期）
3. 客户端在 `Authorization: Bearer <token>` 请求头中发送 JWT
4. `JwtAuthenticationFilter` 拦截请求，验证令牌
5. `SecurityConfig` 对公开端点放行，其他端点需要认证

**公开端点（无需认证）：**
- `/api/auth/**` - 注册和登录
- `/api/explore` - 探索页面
- `/api/search` - 搜索
- `/api/tags` - 标签列表
- `/api/content/global` - 全局动态流
- `/api/content/{id}` - 查看单条内容
- `/api/users/{id}` - 查看用户资料
- `/api/users/{id}/timeline` - 查看用户时间线
- `/api/content/{id}/comments` - 查看评论
- `/media/**` - 媒体文件

**受保护端点（需要认证）：**
- 内容的创建、更新、删除
- 关注/取消关注操作
- 点赞、评论操作
- 通知访问
- 设置更新

**CORS：** 配置允许 `http://localhost:3000` 和 `http://localhost:8080`

### 数据库设计

**数据库：** `timeline_app`（首次运行时自动创建）

**核心表：**
- `users` - 用户账户 (id, username, email, password_hash, avatar_url, bio)
- `contents` - 内容动态 (id, user_id, content_type, text_content, media_paths, location)
- `tags` - 内容标签 (id, name, usage_count)
- `content_tags` - 内容与标签的多对多关系
- `interactions` - 点赞和评论 (id, user_id, target_type, target_id, type, content)
- `follows` - 社交关系图 (follower_id, following_id)
- `notifications` - 用户通知

**数据库结构文件：** `docs/sql/timeline_schema.sql`

### 前端结构

```
frontend/src/
├── components/      # 可复用的 Vue 组件
├── views/          # 页面级组件（路由）
├── stores/         # Pinia 状态存储
├── router/         # Vue Router 配置
└── api/            # Axios API 客户端（带拦截器）
```

**路由：** 带有懒加载视图的客户端 SPA

**状态管理：** Pinia 存储用于用户认证、内容、通知

## 配置文件

### 后端配置

**文件：** `src/main/resources/application.properties`

**重要配置：**
- 数据库：MySQL `localhost:3306/timeline_app`（自动创建数据库）
- JPA DDL：`update`（启动时自动更新架构 - **仅限开发环境**）
- 文件上传：最大 100MB，存储在 `./storage/`
- JWT 密钥：`app.jwt.secret`（生产环境请修改！）
- JWT 有效期：604800000ms（7 天）
- 日志：`com.huawei.codearts.timeline` 为 DEBUG 级别

**Docker 环境变量**（参见 `deploy/docker-compose.yml`）：
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`

### 媒体存储

**目录结构：**
```
./storage/
├── avatars/{user_id}/      # 用户头像文件
├── media/{year}/{month}/{content_id}/  # 内容媒体文件
└── temp/{session_id}/      # 临时上传文件
```

**媒体控制器：** 处理 multipart 文件上传，验证文件类型/大小

## CI/CD 流水线

**为华为 CodeArts 配置**，包含三个阶段：

1. **源码阶段** - 源码仓库集成
2. **构建阶段** - Maven 构建 + 代码检查任务
3. **部署阶段** - 部署 + API 测试（通过 `apitest.yaml`）

**API 测试：** `apitest.yaml` 定义了流水线的 API 契约测试

## 开发注意事项

- **语言：** 代码使用英文，文档使用中文
- **Java 版本：** Java 17（在 `pom.xml` 中配置）
- **Spring Boot 版本：** 2.7.18
- **Lombok：** 广泛使用 - 需要在 Maven 中配置注解处理器
- **懒加载：** 安全模块使用 `@Lazy` 避免与 JWT 过滤器的循环依赖
- **测试覆盖：** 目前没有单元测试（仅 `apitest.yaml` 用于 API 契约测试）
- **遗留代码：** 包含原始演示模板中的 ZXing 二维码库（未使用）

## 默认凭据

**数据库（本地开发）：**
- 用户名：`root`
- 密码：`Huawei@123`（application.properties）
- 密码：`123456`（docker-compose.yml - 不一样！）

**测试用户**（参见 `docs/sql/timeline_schema.sql`）：
- 用户名：`admin`，邮箱：`admin@timeline.com`，密码：`password123`
- 用户名：`testuser`，邮箱：`test@example.com`，密码：`password123`

## API 端点参考

**认证相关：**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录，返回 JWT

**内容相关：**
- `POST /api/content` - 创建内容 (multipart/form-data)
- `GET /api/content/{id}` - 获取内容详情
- `GET /api/content/global` - 全局内容流
- `POST /api/content/{id}/like` - 切换点赞
- `POST /api/content/{id}/comment` - 添加评论
- `GET /api/content/{id}/comments` - 获取评论

**用户相关：**
- `GET /api/users/{id}` - 获取用户资料
- `PUT /api/users/{id}` - 更新资料
- `GET /api/users/{id}/timeline` - 获取用户的时间线
- `POST /api/follow/{userId}` - 关注/取消关注用户

**发现相关：**
- `GET /api/explore` - 探索热门内容
- `GET /api/search` - 搜索用户和内容
- `GET /api/recommend/timelines` - 推荐用户（计划中）
- `GET /api/tags` - 列出所有标签
- `GET /api/tags/{id}/content` - 按标签获取内容

**媒体相关：**
- `POST /api/media/upload` - 上传媒体文件

## 实现新功能时的注意事项

实现新功能时：

1. **新增 API 端点：** 创建 DTO → Controller 方法 → Service 方法 → Repository（如需要）
2. **JPA 实体：** 使用 Lombok 注解，适当配置级联删除
3. **安全配置：** 如果无需认证，将公开端点添加到 `SecurityConfig.permitAll()`
4. **文件上传：** 使用 `MediaController` 模式，验证文件类型/大小
5. **参数验证：** 在 Controller 的 DTO 上使用 `@Valid` 注解
6. **错误处理：** 返回结构一致的 `ApiResponse<T>` 包装器