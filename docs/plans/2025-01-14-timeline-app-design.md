# 时间线应用设计方案

**日期：** 2025-01-14
**状态：** 待实施

## 概述

一个基于时间线的个人记录与社交分享平台，用户可以发布图片、视频、文字等内容，按时间线自动排列，其他人可以浏览、点赞、评论。应用支持多用户注册，提供探索、搜索、推荐等功能。

## 技术栈

- **后端：** Spring Boot + Spring Security + JWT
- **前端：** Vue.js 3 + Pinia + Vue Router + Element Plus
- **数据库：** MySQL 8.0
- **缓存：** Redis
- **存储：** 本地文件系统
- **部署：** Nginx + Spring Boot Jar

## 整体架构

前后端分离架构，RESTful API 通信。

### 后端模块

- **用户模块：** 注册、登录、个人资料
- **内容模块：** 发布、查看时间线、媒体管理
- **社交模块：** 点赞、评论、关注
- **发现模块：** 探索、搜索、推荐
- **标签模块：** 标签管理、相关内容

### 前端结构

- 单页应用（SPA），移动优先响应式设计
- Pinia 状态管理
- Axios 调用后端 API
- Element Plus UI 组件库

## 数据模型

### 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR | 用户名（唯一） |
| email | VARCHAR | 邮箱（唯一） |
| password_hash | VARCHAR | 密码哈希 |
| avatar_url | VARCHAR | 头像路径 |
| bio | TEXT | 个人简介 |
| created_at | DATETIME | 注册时间 |
| updated_at | DATETIME | 更新时间 |

### 内容表 (contents)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 作者ID |
| content_type | VARCHAR | 类型（image/video/text） |
| text_content | TEXT | 文字内容 |
| media_paths | JSON | 媒体文件路径数组 |
| location | VARCHAR | 地理位置（可选） |
| created_at | DATETIME | 发布时间 |

### 标签表 (tags)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 标签名（唯一） |
| usage_count | INT | 使用次数 |

### 内容标签关联表 (content_tags)

| 字段 | 类型 | 说明 |
|------|------|------|
| content_id | BIGINT | 内容ID |
| tag_id | BIGINT | 标签ID |

### 互动表 (interactions)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| target_type | VARCHAR | 目标类型（content/comment） |
| target_id | BIGINT | 目标ID |
| type | VARCHAR | 类型（like/comment） |
| content | TEXT | 评论内容 |
| created_at | DATETIME | 时间 |

### 关注表 (follows)

| 字段 | 类型 | 说明 |
|------|------|------|
| follower_id | BIGINT | 关注者ID |
| following_id | BIGINT | 被关注者ID |

## API 设计

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录，返回 JWT

### 用户相关
- `GET /api/users/{id}` - 获取用户资料
- `PUT /api/users/{id}` - 更新个人资料
- `GET /api/users/{id}/timeline` - 获取用户时间线
- `GET /api/users/{id}/followers` - 获取粉丝列表
- `GET /api/users/{id}/following` - 获取关注列表
- `POST /api/follow/{userId}` - 关注/取消关注

### 内容相关
- `POST /api/content` - 发布内容（multipart/form-data）
- `GET /api/content/{id}` - 获取单条内容详情
- `GET /api/content/global` - 全局内容流
- `POST /api/content/{id}/like` - 点赞/取消点赞
- `POST /api/content/{id}/comment` - 发表评论
- `GET /api/content/{id}/comments` - 获取评论列表

### 发现与推荐
- `GET /api/explore` - 探索页
- `GET /api/search` - 搜索
- `GET /api/recommend/timelines` - 推荐时间线
- `GET /api/tags` - 获取标签列表
- `GET /api/tags/{id}/content` - 标签下的内容

### 通知相关
- `GET /api/notifications` - 获取通知列表

## 前端页面

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 推荐时间线流 |
| `/login` | 登录 | 用户登录 |
| `/register` | 注册 | 用户注册 |
| `/user/:id` | 用户时间线 | 查看用户内容 |
| `/post` | 发布 | 发布新内容 |
| `/explore` | 探索 | 热门内容和标签 |
| `/search` | 搜索 | 搜索用户和内容 |
| `/notifications` | 通知 | 消息通知 |
| `/settings` | 设置 | 个人设置 |

## 推荐系统

基于标签相似度的推荐算法：

1. 获取用户最近浏览/点赞的内容标签
2. 统计标签频率，得出用户兴趣标签
3. 查找包含这些标签的其他时间线
4. 按标签匹配度和活跃度排序

缓存策略：
- 用户兴趣标签缓存 1 小时
- 推荐结果缓存 30 分钟

## 媒体存储

### 目录结构

```
/storage/
├── avatars/           # 用户头像
│   └── {user_id}/
├── media/             # 内容媒体
│   └── {year}/{month}/{content_id}/
│       └── thumbnail/
└── temp/              # 临时文件
    └── {session_id}/
```

### 文件限制

- 图片：JPG、PNG、WEBP，最大 10MB
- 视频：MP4，最大 100MB
- 自动生成缩略图

## 安全措施

- JWT Token 认证（7天有效期）
- BCrypt 密码加密
- 文件上传验证（类型、大小）
- XSS/CSRF 防护
- 权限控制（内容只能作者修改）

## 部署方案

### Nginx 配置

```nginx
server {
    listen 80;
    server_name timeline.example.com;

    # 前端静态文件
    location / {
        root /var/www/timeline-frontend;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8080;
    }

    # 媒体文件
    location /media/ {
        root /storage;
    }
}
```

### Docker 支持（可选）

- 前端：Nginx 多阶段构建
- 后端：Spring Boot 分层构建
- Docker Compose 编排服务

## 测试策略

- 后端：JUnit 5 + Mockito，目标覆盖率 80%+
- 前端：Vue Test Utils + Cypress E2E

## 开发优先级

### Phase 1 - 核心功能
- 用户注册登录
- 发布内容（图片+文字）
- 查看时间线
- 点赞评论

### Phase 2 - 社交功能
- 关注系统
- 个人资料完善
- 通知系统

### Phase 3 - 发现功能
- 探索页面
- 搜索功能
- 标签系统

### Phase 4 - 推荐
- 基于标签的推荐算法
- 个性化首页
