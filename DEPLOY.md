# 云服务器部署教程

本文档详细介绍如何将英雄任务榜部署到生产环境（云服务器或自托管服务器）。

## 📋 目录

- [准备工作](#准备工作)
- [服务器连接](#服务器连接)
- [安装 Docker](#安装-docker)
- [部署应用](#部署应用)
- [配置域名和 HTTPS](#配置域名和-https)
- [日常运维](#日常运维)
- [监控与日志](#监控与日志)
- [备份与恢复](#备份与恢复)
- [更新部署](#更新部署)
- [回滚操作](#回滚操作)
- [安全加固](#安全加固)
- [故障排查](#故障排查)

---

## 🔧 准备工作

### 服务器要求

#### 最低配置
- **CPU**: 2 核
- **内存**: 2GB
- **存储**: 40GB
- **操作系统**: CentOS 7/8、Ubuntu 18.04/20.04/22.04、Debian 10/11
- **带宽**: 1Mbps（支持 10-20 人同时在线）

#### 推荐配置
- **CPU**: 2-4 核
- **内存**: 4GB
- **存储**: 50GB SSD
- **带宽**: 3-5Mbps（支持 50-100 人同时在线）

### 云服务商推荐

| 服务商 | 优势 | 推荐配置 | 价格参考 |
|--------|------|----------|----------|
| 阿里云 | 国内访问快，稳定性高 | ECS 2核4G | ¥100/月 |
| 腾讯云 | 学生优惠，性价比高 | 轻量应用服务器 2核2G | ¥50/月 |
| 华为云 | 企业级服务 | 云耀云服务器 2核4G | ¥80/月 |
| 国外 VPS | DigitalOcean, Linode, Vultr | 2GB RAM | $10/月 |

### 域名准备

1. **购买域名**：阿里云、腾讯云、GoDaddy 等
2. **ICP 备案**（国内服务器必须）：
   - 阿里云备案系统：https://beian.aliyun.com
   - 腾讯云备案系统：https://cloud.tencent.com/product/ba
   - 备案周期：7-20 个工作日
3. **DNS 解析**：将域名 A 记录指向服务器 IP

---

## 🔗 服务器连接

### Windows 用户

**方式一：Windows Terminal（推荐）**

```powershell
# Windows 11 自带，Windows 10 需从 Microsoft Store 下载
ssh root@你的服务器IP
```

**方式二：MobaXterm**

1. 下载：https://mobaxterm.mobatek.net/
2. 打开软件，点击 `Session` → `SSH`
3. 输入服务器 IP 和端口（默认 22）
4. 点击 OK 并输入密码

### macOS/Linux 用户

```bash
# 使用内置终端
ssh root@你的服务器IP

# 如果使用密钥登录
ssh -i ~/.ssh/your_private_key root@你的服务器IP
```

### 首次登录安全配置

```bash
# 1. 修改 root 密码（建议）
passwd

# 2. 创建新用户（可选但推荐）
adduser heroquest
usermod -aG sudo heroquest  # Ubuntu/Debian
usermod -aG wheel heroquest  # CentOS

# 3. 配置 SSH 密钥登录（推荐）
mkdir -p ~/.ssh
chmod 700 ~/.ssh
# 将本地公钥内容追加到 ~/.ssh/authorized_keys
```

---

## 🐳 安装 Docker

### 一键安装脚本（推荐）

```bash
# 使用官方安装脚本
curl -fsSL https://get.docker.com | sh

# 启动 Docker 服务
systemctl start docker
systemctl enable docker

# 验证安装
docker --version
docker ps
```

### 手动安装（CentOS）

```bash
# 安装依赖
yum install -y yum-utils device-mapper-persistent-data lvm2

# 添加 Docker 仓库
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装 Docker
yum install -y docker-ce docker-ce-cli containerd.io

# 启动服务
systemctl start docker
systemctl enable docker
```

### 手动安装（Ubuntu/Debian）

```bash
# 更新包索引
apt update

# 安装依赖
apt install -y ca-certificates curl gnupg lsb-release

# 添加 Docker GPG 密钥
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 添加 Docker 仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
apt update
apt install -y docker-ce docker-ce-cli containerd.io

# 启动服务
systemctl start docker
systemctl enable docker
```

### 安装 Docker Compose

```bash
# 下载最新版本
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 创建软链接（可选）
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# 验证安装
docker-compose --version
```

---

## 🚀 部署应用

### 方法一：使用 Git（推荐）

```bash
# 1. 安装 Git
yum install -y git  # CentOS
apt install -y git  # Ubuntu/Debian

# 2. 克隆项目到 /opt 目录
cd /opt
git clone https://github.com/your-username/hero-quest-board.git
cd hero-quest-board

# 3. 配置环境变量
cp .env.example .env
nano .env  # 或使用 vim .env
```

### 方法二：手动上传

```bash
# 在本地打包项目
tar -czf hero-quest-board.tar.gz hero-quest-board/

# 上传到服务器
scp hero-quest-board.tar.gz root@你的服务器IP:/opt/

# 在服务器上解压
cd /opt
tar -xzf hero-quest-board.tar.gz
cd hero-quest-board
```

### 配置环境变量

编辑 `.env` 文件：

```bash
nano .env
```

**生产环境配置示例**：

```env
# ========== 邮件配置（必填） ==========
MAIL_USERNAME=你的QQ邮箱@qq.com
MAIL_PASSWORD=你的QQ邮箱授权码

# ========== JWT 密钥（生产环境必须修改） ==========
# 使用以下命令生成随机密钥：
# openssl rand -base64 64
JWT_SECRET=请生成一个至少64字符的随机字符串作为密钥

# ========== MySQL 配置 ==========
MYSQL_ROOT_PASSWORD=请修改为强密码
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_USER=root
```

**生成安全的 JWT 密钥**：

```bash
# 方式1：使用 openssl
openssl rand -base64 64

# 方式2：使用 Python
python3 -c "import secrets; print(secrets.token_urlsafe(64))"

# 方式3：在线生成
# 访问 https://www.grc.com/passwords.htm
```

保存文件：按 `Ctrl+X`，然后按 `Y`，再按 `Enter`

### 启动应用

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看启动日志
docker-compose logs -f

# 等待所有服务启动完成（大约 3-5 分钟）
# 当看到 "Started HeroQuestApplication" 表示后端启动成功
```

### 验证部署

```bash
# 检查服务状态
docker-compose ps

# 应该看到三个服务都是 Up 状态：
# NAME                    STATUS
# hero-quest-mysql        Up (healthy)
# hero-quest-backend      Up
# hero-quest-frontend     Up

# 测试前端访问
curl http://localhost

# 测试后端 API
curl http://localhost:8080/api/public/tasks
```

### 注册第一个管理员

1. 访问 `http://你的服务器IP/admin/register`
2. 填写管理员信息并注册
3. 登录后即可开始使用

---

## 🌐 配置域名和 HTTPS

### 方式一：使用 Nginx 反向代理（推荐）

#### 1. 安装 Nginx

```bash
# CentOS
yum install -y nginx

# Ubuntu/Debian
apt install -y nginx

# 启动 Nginx
systemctl start nginx
systemctl enable nginx
```

#### 2. 配置 HTTP 访问

创建 Nginx 配置文件：

```bash
nano /etc/nginx/conf.d/hero-quest.conf
```

添加以下内容：

```nginx
server {
    listen 80;
    server_name 你的域名.com;

    # 客户端最大上传大小（用于富文本编辑器，虽然已禁用图片上传）
    client_max_body_size 10M;

    # 前端静态文件
    location / {
        proxy_pass http://127.0.0.1:8080;  # Docker 容器映射的前端端口
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
    }

    # 日志
    access_log /var/log/nginx/hero-quest-access.log;
    error_log /var/log/nginx/hero-quest-error.log;
}
```

**注意**：如果使用 Docker 默认配置，前端和后端都通过容器端口暴露，需要修改 `docker-compose.yml`：

```yaml
services:
  frontend:
    ports:
      - "3000:80"  # 改为内部端口，不暴露在外
  backend:
    ports:
      - "8080:8080"
```

然后修改 Nginx 配置：

```nginx
location / {
    proxy_pass http://127.0.0.1:3000;  # 前端端口
}

location /api/ {
    proxy_pass http://127.0.0.1:8080;  # 后端端口
}
```

#### 3. 测试并重启 Nginx

```bash
# 测试配置文件语法
nginx -t

# 重启 Nginx
systemctl restart nginx

# 检查状态
systemctl status nginx
```

#### 4. 配置 HTTPS（Let's Encrypt）

**安装 Certbot**：

```bash
# CentOS 7
yum install -y epel-release
yum install -y certbot python3-certbot-nginx

# CentOS 8
dnf install -y epel-release
dnf install -y certbot python3-certbot-nginx

# Ubuntu/Debian
apt install -y certbot python3-certbot-nginx
```

**获取 SSL 证书**：

```bash
# 自动配置 HTTPS（推荐）
certbot --nginx -d 你的域名.com

# 或手动获取证书
certbot certonly --nginx -d 你的域名.com

# 按照提示完成：
# 1. 输入邮箱地址（用于证书过期提醒）
# 2. 同意服务条款
# 3. 选择是否重定向 HTTP 到 HTTPS（推荐选择 2 - Redirect）
```

**自动续期**：

```bash
# Certbot 会自动配置定时任务，测试续期
certbot renew --dry-run

# 查看自动续期任务
systemctl list-timers | grep certbot

# 或查看 crontab
crontab -l | grep certbot
```

**手动续期证书**（如果需要）：

```bash
# 续期所有即将过期的证书
certbot renew

# 续期后重启 Nginx
systemctl restart nginx
```

### 方式二：使用 Cloudflare（零配置 HTTPS）

1. 注册 Cloudflare 账号：https://dash.cloudflare.com/sign-up
2. 添加你的域名
3. 将域名 NS 服务器改为 Cloudflare 提供的地址
4. 在 Cloudflare 控制台设置：
   - SSL/TLS → 加密模式 → 完全（严格）
   - DNS → 添加 A 记录指向服务器 IP
   - 防火墙 → 根据需要配置规则
5. 完成！Cloudflare 自动提供 HTTPS

---

## 🔥 防火墙配置

### CentOS/RHEL (firewalld)

```bash
# 查看防火墙状态
systemctl status firewalld

# 开放 HTTP 和 HTTPS 端口
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https

# 或使用端口号
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp

# 重载防火墙
firewall-cmd --reload

# 查看已开放端口
firewall-cmd --list-all
```

### Ubuntu/Debian (ufw)

```bash
# 启用防火墙
ufw enable

# 允许 SSH（防止被锁定）
ufw allow 22/tcp

# 允许 HTTP 和 HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# 查看状态
ufw status
```

### 云服务商安全组

**阿里云/腾讯云/华为云**：
1. 进入控制台 → 实例 → 安全组
2. 添加入站规则：
   - 端口 80/TCP，源地址 0.0.0.0/0
   - 端口 443/TCP，源地址 0.0.0.0/0
   - 端口 22/TCP，源地址 0.0.0.0/0（SSH）

---

## 📊 日常运维

### 查看服务状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看容器资源使用
docker stats

# 查看磁盘使用
df -h
du -sh /var/lib/docker
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql

# 查看最近 100 行日志
docker-compose logs --tail=100 backend

# 查看带时间戳的日志
docker-compose logs -f --timestamps backend

# 查看特定时间段日志
docker-compose logs --since 2024-01-01T00:00:00 backend
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart backend

# 停止所有服务
docker-compose stop

# 启动所有服务
docker-compose start

# 停止并删除容器（保留数据）
docker-compose down

# 停止并删除容器和数据卷（危险！会丢失数据）
docker-compose down -v
```

---

## 💾 备份与恢复

### 数据库备份

#### 自动备份脚本

创建备份脚本：

```bash
nano /opt/backup-hero-quest.sh
```

添加以下内容：

```bash
#!/bin/bash
# Hero Quest Board 数据库自动备份脚本

# 配置
BACKUP_DIR="/opt/backups/hero-quest"
KEEP_DAYS=7  # 保留最近 7 天的备份
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/hero_quest_$DATE.sql"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
docker exec hero-quest-mysql mysqldump -uroot -phero123456 hero_quest > $BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_FILE

# 删除旧备份
find $BACKUP_DIR -name "hero_quest_*.sql.gz" -mtime +$KEEP_DAYS -delete

# 输出结果
if [ $? -eq 0 ]; then
    echo "[$(date)] 备份成功: $BACKUP_FILE.gz" >> $BACKUP_DIR/backup.log
else
    echo "[$(date)] 备份失败" >> $BACKUP_DIR/backup.log
fi
```

添加执行权限并测试：

```bash
chmod +x /opt/backup-hero-quest.sh
/opt/backup-hero-quest.sh
```

#### 配置定时备份

```bash
# 编辑 crontab
crontab -e

# 添加定时任务（每天凌晨 2 点备份）
0 2 * * * /opt/backup-hero-quest.sh

# 查看 crontab
crontab -l
```

#### 手动备份

```bash
# 备份数据库
docker exec hero-quest-mysql mysqldump -uroot -phero123456 hero_quest > \
  /opt/backups/hero_quest_manual_$(date +%Y%m%d).sql

# 备份整个项目目录
tar -czf /opt/backups/hero-quest-full_$(date +%Y%m%d).tar.gz \
  -C /opt hero-quest-board

# 备份 .env 配置文件
cp /opt/hero-quest-board/.env /opt/backups/.env_$(date +%Y%m%d)
```

### 数据恢复

```bash
# 1. 停止服务
docker-compose down

# 2. 恢复数据库
docker-compose up -d mysql
sleep 10  # 等待 MySQL 启动
docker exec -i hero-quest-mysql mysql -uroot -phero123456 hero_quest < backup_20240101.sql

# 3. 启动所有服务
docker-compose up -d

# 4. 验证恢复
docker-compose logs -f backend
```

---

## 🔄 更新部署

### 使用 Git 更新（推荐）

```bash
cd /opt/hero-quest-board

# 1. 拉取最新代码
git pull origin main  # 或 master

# 2. 备份数据库
/opt/backup-hero-quest.sh

# 3. 重新构建并启动
docker-compose up -d --build

# 4. 查看日志确认更新成功
docker-compose logs -f

# 5. 清理旧镜像（可选）
docker image prune -f
```

### 手动更新

```bash
# 1. 备份当前版本
cd /opt
cp -r hero-quest-board hero-quest-board.backup

# 2. 上传新版本文件
scp -r ./hero-quest-board root@服务器IP:/opt/

# 3. 重新部署
cd /opt/hero-quest-board
docker-compose down
docker-compose up -d --build
```

### 零停机更新（滚动更新）

如果需要零停机更新：

```bash
# 1. 启动第二套环境（使用不同端口）
docker-compose -f docker-compose.new.yml up -d

# 2. 验证新环境正常
curl http://localhost:8081

# 3. 切换 Nginx 到新环境
nano /etc/nginx/conf.d/hero-quest.conf
# 修改 proxy_pass 指向新端口

# 4. 重载 Nginx
nginx -t && nginx -s reload

# 5. 停止旧环境
docker-compose down
```

---

## ⏮️ 回滚操作

### 快速回滚

```bash
# 1. 停止当前版本
cd /opt/hero-quest-board
docker-compose down

# 2. 恢复备份版本
cd /opt
rm -rf hero-quest-board
cp -r hero-quest-board.backup hero-quest-board

# 3. 恢复数据库
cd hero-quest-board
docker-compose up -d mysql
sleep 10
docker exec -i hero-quest-mysql mysql -uroot -phero123456 hero_quest < \
  /opt/backups/hero_quest_YYYYMMDD.sql

# 4. 启动服务
docker-compose up -d

# 5. 验证回滚
docker-compose logs -f
```

### Git 回滚

```bash
cd /opt/hero-quest-board

# 查看提交历史
git log --oneline

# 回滚到指定提交
git reset --hard <commit-hash>

# 重新部署
docker-compose up -d --build
```

---

## 🔒 安全加固

### 1. 修改默认密码

```env
# .env 文件
MYSQL_ROOT_PASSWORD=使用强密码替换
JWT_SECRET=使用64+字符的随机密钥
```

### 2. 限制端口暴露

修改 `docker-compose.yml`，仅暴露必要端口：

```yaml
services:
  mysql:
    ports:
      - "127.0.0.1:3306:3306"  # 仅本地访问
  backend:
    ports:
      - "127.0.0.1:8080:8080"  # 仅本地访问
  frontend:
    ports:
      - "127.0.0.1:80:80"  # 通过 Nginx 代理
```

### 3. 配置防火墙

```bash
# 只开放必要端口
firewall-cmd --permanent --remove-port=8080/tcp
firewall-cmd --permanent --remove-port=3306/tcp
firewall-cmd --reload
```

### 4. 启用 Fail2ban

防止暴力破解攻击：

```bash
# 安装 Fail2ban
yum install -y fail2ban  # CentOS
apt install -y fail2ban  # Ubuntu

# 配置 SSH 保护
cat > /etc/fail2ban/jail.local <<EOF
[sshd]
enabled = true
port = ssh
logpath = /var/log/secure
maxretry = 5
bantime = 3600
EOF

# 启动服务
systemctl start fail2ban
systemctl enable fail2ban

# 查看状态
fail2ban-client status sshd
```

### 5. 定期更新系统

```bash
# CentOS
yum update -y

# Ubuntu/Debian
apt update && apt upgrade -y

# 重启（如果需要）
reboot
```

### 6. 启用 Docker 日志限制

修改 `docker-compose.yml`：

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

---

## 🔍 监控与日志

### 系统监控

#### 使用 htop

```bash
# 安装 htop
yum install -y htop  # CentOS
apt install -y htop  # Ubuntu

# 运行
htop
```

#### 使用 ctop（Docker 容器监控）

```bash
# 安装 ctop
wget https://github.com/bcicen/ctop/releases/download/v0.7.7/ctop-0.7.7-linux-amd64 \
  -O /usr/local/bin/ctop
chmod +x /usr/local/bin/ctop

# 运行
ctop
```

### 日志管理

```bash
# 查看 Nginx 访问日志
tail -f /var/log/nginx/hero-quest-access.log

# 查看 Nginx 错误日志
tail -f /var/log/nginx/hero-quest-error.log

# 清理 Docker 日志
du -sh /var/lib/docker/containers/*/*-json.log
find /var/lib/docker/containers/ -name "*-json.log" -exec truncate -s 0 {} \;
```

---

## ❓ 故障排查

### 问题 1：数据库连接失败

**症状**：后端日志显示 "Unable to connect to MySQL"

**解决方案**：

```bash
# 1. 检查 MySQL 容器状态
docker-compose ps mysql

# 2. 查看 MySQL 日志
docker-compose logs mysql

# 3. 验证 MySQL 是否健康
docker exec hero-quest-mysql mysqladmin ping -h localhost -uroot -phero123456

# 4. 重启 MySQL
docker-compose restart mysql
sleep 10
docker-compose restart backend
```

### 问题 2：邮件发送失败

**症状**：邮件提醒不工作

**解决方案**：

```bash
# 1. 检查邮箱配置
cat .env | grep MAIL

# 2. 查看后端日志
docker-compose logs backend | grep -i mail

# 3. 测试 SMTP 连接
telnet smtp.qq.com 465

# 4. 确认授权码正确
# 重新获取 QQ 邮箱授权码并更新 .env
```

### 问题 3：端口冲突

**症状**："port is already allocated"

**解决方案**：

```bash
# 查看端口占用
netstat -tlnp | grep :80
lsof -i :80

# 停止占用端口的进程
kill -9 <PID>

# 或修改 docker-compose.yml 端口映射
ports:
  - "8000:80"  # 改为其他端口
```

### 问题 4：磁盘空间不足

**症状**："no space left on device"

**解决方案**：

```bash
# 检查磁盘使用
df -h

# 清理 Docker 未使用资源
docker system prune -a -f

# 清理日志文件
journalctl --vacuum-time=7d

# 删除旧备份
find /opt/backups -mtime +30 -delete
```

### 问题 5：容器启动失败

**症状**：`docker-compose ps` 显示容器 Exit

**解决方案**：

```bash
# 查看容器退出原因
docker-compose logs <service-name>

# 检查配置文件语法
docker-compose config

# 重新构建镜像
docker-compose build --no-cache
docker-compose up -d
```

---

## 📞 技术支持

如遇到无法解决的问题，请提供以下信息：

1. 操作系统版本：`cat /etc/os-release`
2. Docker 版本：`docker --version`
3. 服务状态：`docker-compose ps`
4. 错误日志：`docker-compose logs`
5. 配置文件：`.env`（隐藏敏感信息）

联系方式：
- GitHub Issues: https://github.com/your-username/hero-quest-board/issues
- Email: your-email@example.com

---

## 📝 检查清单

部署完成后，请确认以下事项：

- [ ] 所有容器状态为 Up
- [ ] 可以访问前端页面
- [ ] 可以注册管理员账号
- [ ] 可以登录管理后台
- [ ] 可以创建并发布任务
- [ ] 可以认领任务
- [ ] 邮件提醒功能正常
- [ ] 配置了 HTTPS
- [ ] 配置了防火墙
- [ ] 配置了数据库自动备份
- [ ] 修改了默认密码
- [ ] 生成了强 JWT 密钥

---

**🎉 恭喜！部署完成！**

如有问题或建议，欢迎提 Issue。祝使用愉快！
