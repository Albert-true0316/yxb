# 英雄任务榜 - Hero Quest Board

一个现代化的任务管理平台，融合游戏化元素，支持任务发布、多人协作、审核流程、积分系统和排行榜功能。

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.4.0-green.svg)

## 📋 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [环境配置](#环境配置)
- [API 接口](#api-接口)
- [数据库设计](#数据库设计)
- [开发指南](#开发指南)
- [常见问题](#常见问题)

---

## 🎯 功能特性

### 用户端
- **📋 任务展示板**：公开任务列表，支持按状态筛选（待认领/待审核/进行中/已完成）
- **🎴 响应式布局**：超宽屏4列、宽屏3列、平板2列、手机1列自适应布局
- **⏰ 过期任务标识**：自动识别并标红过期任务，排序优化
- **👥 多人认领**：一个任务可被多人同时认领，自行分配积分
- **📝 任务详情**：富文本任务描述、截止日期、悬赏积分、认领成员列表
- **🏆 积分排行榜**：实时展示成员总积分和完成任务数
- **🎨 现代化 UI**：渐变配色、圆角设计、悬浮动效、响应式交互

### 管理后台
- **🔐 JWT 认证**：管理员注册登录，Token 自动续期
- **📝 任务管理**：创建/查看/编辑任务，富文本编辑器（精简版，无图片上传）
- **✅ 分离式审核流程**：
  - 成员申请 → 审核通过（仅通过审核）
  - 所有成员审核完毕 → 点击"开始任务"→ 任务进入进行中
  - 支持单个通过/拒绝，灵活控制任务开始时机
- **📊 仪表盘**：任务统计、成员排行、快速操作
- **📧 邮件提醒**：截止日当天早上8点自动发送邮件提醒
- **🎯 任务状态管理**：完整的状态流转（待认领 → 待审核 → 进行中 → 已完成）

### 系统特性
- **📱 响应式设计**：完美支持桌面/平板/手机端
- **⚡ 高性能**：Vite 构建、MyBatis-Plus 查询优化
- **🐳 Docker 部署**：一键启动，开箱即用
- **🔒 安全加固**：密码加密、JWT 鉴权、CORS 配置
- **📈 可扩展**：模块化设计，易于二次开发

---

## 🛠 技术栈

### 前端
- **框架**：Vue 3.4.0 (Composition API)
- **语言**：TypeScript 5.2.2
- **UI 组件库**：Element Plus 2.4.4
- **状态管理**：Pinia 2.1.7
- **路由**：Vue Router 4.2.5
- **富文本编辑器**：Wangeditor 5.1.23
- **HTTP 客户端**：Axios 1.6.2
- **构建工具**：Vite 5.0.10

### 后端
- **框架**：Spring Boot 3.2.1
- **语言**：Java 17
- **ORM**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0
- **安全**：Spring Security + JWT (jjwt 0.12.3)
- **邮件**：Spring Boot Starter Mail
- **工具**：Lombok, Validation

### 部署
- **容器化**：Docker + Docker Compose
- **Web 服务器**：Nginx (前端静态文件)
- **反向代理**：Nginx (可选，用于 HTTPS)

---

## 📁 项目结构

```
hero-quest-board/
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 接口封装
│   │   │   ├── index.ts        # Axios 实例配置
│   │   │   ├── auth.ts         # 认证接口
│   │   │   └── task.ts         # 任务接口
│   │   ├── components/         # 公共组件
│   │   │   ├── TaskCard.vue    # 任务卡片
│   │   │   ├── TaskModal.vue   # 任务详情弹窗
│   │   │   └── LeaderboardOverlay.vue # 排行榜全屏覆盖层
│   │   ├── views/              # 页面组件
│   │   │   ├── HeroBoard.vue   # 核心英雄榜主页面
│   │   │   ├── TaskBoard.vue   # 任务展示板
│   │   │   ├── TaskDetail.vue  # 任务详情
│   │   │   ├── Leaderboard.vue # 排行榜页面
│   │   │   └── admin/          # 管理后台
│   │   │       ├── AdminLayout.vue   # 后台布局
│   │   │       ├── Login.vue         # 登录页
│   │   │       ├── Register.vue      # 注册页
│   │   │       ├── Dashboard.vue     # 仪表盘
│   │   │       ├── TaskManage.vue    # 任务管理
│   │   │       ├── TaskReview.vue    # 审核中心
│   │   │       ├── EmailSettings.vue # 邮件提醒设置
│   │   │       └── TaskDetailAdmin.vue # 任务详情(管理员看)
│   │   ├── stores/             # Pinia 状态管理
│   │   │   └── auth.ts         # 认证状态与 Token 管理
│   │   ├── router/             # 路由配置
│   │   │   └── index.ts        # 页面路由定义
│   │   ├── styles/             # 样式文件
│   │   │   └── heroboard.css   # 赛博朋克风核心样式
│   │   ├── utils/              # 工具函数
│   │   │   └── format.ts       # 状态映射与标签生成逻辑
│   │   ├── App.vue             # 根组件
│   │   ├── main.ts             # 入口文件
│   │   └── vite-env.d.ts       # TS 类型声明
│   ├── Dockerfile              # 前端 Docker 构建配置
│   ├── nginx.conf              # Nginx 生产环境配置
│   └── vite.config.ts          # Vite 构建与代理配置
│
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/hero/quest/
│   │   ├── HeroQuestApplication.java  # 后端启动类
│   │   ├── config/                    # 安全与中间件配置
│   │   │   ├── SecurityConfig.java    # Spring Security 权限配置
│   │   │   ├── JwtTokenProvider.java  # JWT 生成与验证
│   │   │   └── GlobalExceptionHandler.java # 统一异常处理
│   │   ├── controller/                # API 控制器层
│   │   ├── service/                   # 业务逻辑层
│   │   ├── entity/                    # 数据库实体类
│   │   ├── mapper/                    # MyBatis 数据库映射
│   │   ├── dto/                       # 数据传输对象
│   │   └── scheduler/                 # 定时任务
│   │       └── EmailReminderScheduler.java # 截止日期邮件提醒
│   ├── Dockerfile                         # 后端 Docker 构建配置
│   └── pom.xml                            # Maven 依赖管理
│
├── mysql/                       # 数据库初始化
│   └── init/
│       └── init.sql             # MySQL 初始化脚本
│
├── docker-compose.yml           # 生产环境一键部署编排
├── docker-compose.dev.yml       # 开发环境热更新编排
└── .env.example                 # 环境变量模板
```

---

## 🚀 快速开始

### 前置要求

- **Docker Desktop**：已安装并运行
  - Windows: [下载链接](https://www.docker.com/products/docker-desktop/)
  - macOS: [下载链接](https://www.docker.com/products/docker-desktop/)
  - Linux: `curl -fsSL https://get.docker.com | sh`
- **Git**：用于克隆项目

### 一键启动（推荐）

1. **克隆项目**
```bash
git clone <your-repo-url>
cd hero-quest-board
```

2. **配置环境变量**
```bash
# Windows
copy .env.example .env

# macOS/Linux
cp .env.example .env
```

编辑 `.env` 文件，填写你的 QQ 邮箱信息：
```env
MAIL_USERNAME=你的QQ邮箱@qq.com
MAIL_PASSWORD=你的QQ邮箱授权码
JWT_SECRET=HeroQuestBoardSecretKey2024VeryLongAndSecureKeyForJWTSigning
```

> **获取 QQ 邮箱授权码**：
> 1. 登录 [QQ 邮箱](https://mail.qq.com)
> 2. 进入「设置」→「账户」
> 3. 找到「POP3/IMAP/SMTP 服务」
> 4. 开启「SMTP 服务」
> 5. 按提示发送短信获取授权码

3. **启动服务**
```bash
docker-compose up -d --build
```

首次构建需要 5-10 分钟，请耐心等待。

4. **访问应用**
- **用户端**：http://localhost
- **管理后台登录**：http://localhost/admin/login
- **管理后台注册**：http://localhost/admin/register
- **后端 API**：http://localhost:8080

5. **注册管理员并开始使用**
- 访问 http://localhost/admin/register 注册第一个管理员账号
- 登录后即可发布任务

### 开发模式（热更新）

如果需要修改代码并实时预览：

```bash
docker-compose -f docker-compose.dev.yml up -d
```

- **前端开发服务器**：http://localhost:3000 (Vite 热更新)
- **后端 API**：http://localhost:8080 (Spring Boot DevTools)
- **数据库**：localhost:3306

修改前端代码会自动刷新，修改后端代码会自动重启。

---

## ⚙️ 环境配置

### 环境变量说明

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `MAIL_USERNAME` | ✅ | 无 | QQ 邮箱地址 |
| `MAIL_PASSWORD` | ✅ | 无 | QQ 邮箱授权码（非登录密码） |
| `JWT_SECRET` | ⚠️ | 默认密钥 | JWT 签名密钥（生产环境务必修改） |
| `MYSQL_ROOT_PASSWORD` | ❌ | hero123456 | MySQL root 密码 |
| `MYSQL_HOST` | ❌ | mysql | MySQL 主机地址（容器内使用） |
| `MYSQL_PORT` | ❌ | 3306 | MySQL 端口 |

### 应用配置文件

**后端配置**：`backend/src/main/resources/application.yml`

```yaml
# 服务端口
server:
  port: 8080

# 数据库连接
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/hero_quest
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:hero123456}

# 邮件服务
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

# JWT 配置
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 小时

# 邮件定时提醒
email:
  reminder:
    cron: "0 0 8 * * ?"  # 每天早上 8:00
    enabled: true
```

---

## 📡 API 接口

### 公开接口（无需认证）

#### 任务相关
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/public/tasks` | 获取任务列表（含成员名称） |
| GET | `/api/public/tasks/{id}` | 获取任务详情（含成员列表） |
| POST | `/api/public/tasks/{id}/claim` | 认领任务 |
| PUT | `/api/public/tasks/{id}/claim` | 更新认领信息 |
| GET | `/api/public/leaderboard` | 获取积分排行榜 |

**认领任务请求示例**：
```json
POST /api/public/tasks/1/claim
{
  "memberName": "张三",
  "memberEmail": "zhangsan@example.com",
  "expectedPoints": 50,
  "deadline": "2024-12-31",
  "title": "更新后的任务标题",  // 可选
  "content": "更新后的任务内容"  // 可选
}
```

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册管理员 |
| POST | `/api/auth/login` | 登录 |

**注册请求示例**：
```json
POST /api/auth/register
{
  "username": "admin",
  "password": "password123",
  "email": "admin@example.com"
}
```

**登录响应示例**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin"
  }
}
```

### 管理员接口（需 JWT Token）

在请求头中添加：`Authorization: Bearer <your-token>`

#### 任务管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/tasks` | 创建任务 |
| GET | `/api/admin/tasks` | 获取任务列表（可按状态筛选） |
| GET | `/api/admin/tasks/{id}` | 获取任务详情（含成员审核状态） |
| POST | `/api/admin/tasks/{id}/complete` | 完成任务并分配积分 |

#### 审核管理（新）
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/tasks/{id}/members/{mid}/approve` | 通过审核并开始任务（旧接口） |
| POST | `/api/admin/tasks/{id}/members/{mid}/approve-only` | 仅通过审核（不改变任务状态） |
| POST | `/api/admin/tasks/{id}/start` | 开始任务（将状态改为进行中） |
| POST | `/api/admin/tasks/{id}/members/{mid}/reject` | 拒绝成员 |
| DELETE | `/api/admin/tasks/{id}/members/{mid}` | 移除成员 |
| POST | `/api/admin/tasks/{id}/assign` | 管理员直接分配成员 |

---

## 🗄️ 数据库设计

### 表结构

#### 1. admin（管理员表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt 加密） |
| email | VARCHAR(100) | 邮箱 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### 2. task（任务表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(200) | 任务名称 |
| content | TEXT | 任务内容（富文本） |
| deadline | DATE | 截止日期 |
| reward | INT | 悬赏积分 |
| status | TINYINT | 状态：0-待认领，1-待审核，2-进行中，3-已完成 |
| created_by | BIGINT | 创建者（管理员ID） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### 3. task_member（任务成员关系表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 任务ID |
| member_name | VARCHAR(50) | 成员姓名 |
| member_email | VARCHAR(100) | 成员邮箱 |
| earned_points | INT | 获得积分 |
| status | TINYINT | 状态：0-待审核，1-已通过，2-已退出 |
| created_at | DATETIME | 认领时间 |
| updated_at | DATETIME | 更新时间 |

#### 4. email_log（邮件发送记录表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 任务ID |
| member_email | VARCHAR(100) | 收件人邮箱 |
| email_type | TINYINT | 邮件类型：0-截止提醒 |
| status | TINYINT | 发送状态：0-待发送，1-已发送，2-发送失败 |
| sent_at | DATETIME | 发送时间 |
| error_msg | VARCHAR(500) | 错误信息 |
| created_at | DATETIME | 创建时间 |

### 状态流转图

#### 任务状态流转
```
待认领(0) → 待审核(1) → 进行中(2) → 已完成(3)
    ↑           ↓
    └─── 全部拒绝 ──┘
```

#### 成员状态流转
```
待审核(0) → 已通过(1)
    ↓
  已退出(2)
```

---

## 💻 开发指南

### 本地开发环境搭建

#### 方式一：Docker 开发模式（推荐）

```bash
# 启动开发环境
docker-compose -f docker-compose.dev.yml up -d

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f

# 停止服务
docker-compose -f docker-compose.dev.yml down
```

#### 方式二：原生开发

**前端开发**：
```bash
cd frontend
npm install
npm run dev
```
访问 http://localhost:5173

**后端开发**：
```bash
cd backend

# 方式 1：使用 Maven
./mvnw spring-boot:run

# 方式 2：使用 IDE
# 直接运行 HeroQuestApplication.java
```

**数据库准备**：
```bash
# 启动 MySQL
docker run -d \
  --name hero-quest-mysql \
  -e MYSQL_ROOT_PASSWORD=hero123456 \
  -e MYSQL_DATABASE=hero_quest \
  -p 3306:3306 \
  -v ./mysql/init:/docker-entrypoint-initdb.d \
  mysql:8.0
```

### 代码规范

#### 前端
- 使用 Composition API
- TypeScript 严格模式
- 组件命名：PascalCase
- 文件命名：PascalCase (组件) / camelCase (工具函数)
- CSS：使用 scoped 样式，避免全局污染

#### 后端
- 遵循 Spring Boot 最佳实践
- 使用 Lombok 简化代码
- Controller → Service → Mapper 分层架构
- 统一异常处理（GlobalExceptionHandler）
- 统一响应格式（ApiResponse）

### 新增功能示例

**1. 新增 API 接口**

后端 Controller：
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PostMapping("/tasks/{taskId}/members/{memberId}/approve-only")
    public ApiResponse<Void> approveMemberOnly(
        @PathVariable Long taskId,
        @PathVariable Long memberId
    ) {
        taskService.approveMemberOnly(taskId, memberId);
        return ApiResponse.success("审核通过", null);
    }
}
```

前端 API 封装：
```typescript
// src/api/task.ts
export function approveMemberOnly(taskId: number, memberId: number) {
  return api.post(`/admin/tasks/${taskId}/members/${memberId}/approve-only`)
}
```

前端调用：
```typescript
// src/views/admin/TaskReview.vue
import { approveMemberOnly } from '@/api/task'

async function handleApprove(taskId: number, memberId: number) {
  await approveMemberOnly(taskId, memberId)
  ElMessage.success('审核通过')
}
```

---

## ❓ 常见问题

### Q1: 邮件发不出去？
**A**: 检查以下几点：
1. `.env` 文件中的 `MAIL_PASSWORD` 是授权码，不是邮箱登录密码
2. 确认已在 QQ 邮箱中开启 SMTP 服务
3. 查看后端日志：`docker-compose logs backend | grep -i mail`
4. 确认服务器能访问 smtp.qq.com:465

### Q2: 端口被占用怎么办？
**A**: 修改 `docker-compose.yml` 中的端口映射：
```yaml
services:
  frontend:
    ports:
      - "8000:80"  # 将 80 改为 8000
```

### Q3: 如何重置数据库？
**A**:
```bash
# 停止服务并删除数据卷
docker-compose down -v

# 重新启动
docker-compose up -d --build
```

### Q4: JWT Token 过期怎么办？
**A**: Token 默认有效期 24 小时。过期后：
1. 前端会自动跳转到登录页
2. 重新登录即可获取新 Token

### Q5: 如何修改邮件发送时间？
**A**: 编辑 `backend/src/main/resources/application.yml`：
```yaml
email:
  reminder:
    cron: "0 0 8 * * ?"  # 改为你想要的时间
```
Cron 表达式格式：秒 分 时 日 月 周

### Q6: 数据库连接失败？
**A**:
```bash
# 检查 MySQL 容器状态
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 重启后端（等待 MySQL 完全启动）
docker-compose restart backend
```

### Q7: 前端打包后页面空白？
**A**: 检查 `vite.config.ts` 中的 `base` 配置是否正确：
```typescript
export default defineConfig({
  base: '/',  // 部署在根路径
  // base: '/hero-quest/',  // 部署在子路径
})
```

---

## 📚 更多文档

- [部署文档](./DEPLOY.md) - 云服务器部署完整教程
- [API 文档](https://your-domain.com/api-docs) - Swagger API 文档（可选）

---

## 📄 License

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📧 联系方式

如有问题或建议，请通过以下方式联系：

- Issue: [GitHub Issues](https://github.com/your-username/hero-quest-board/issues)
- Email: your-email@example.com

---

**⚡ 快速开始命令回顾**：

```bash
# 1. 克隆项目
git clone <your-repo-url> && cd hero-quest-board

# 2. 配置环境变量
cp .env.example .env  # 然后编辑 .env 填写邮箱信息

# 3. 启动服务
docker-compose up -d --build

# 4. 访问应用
# 用户端: http://localhost
# 管理后台: http://localhost/admin/register
```

祝使用愉快！🎉
