# 在线学习平台 — 部署配置指南

## 一、基础环境

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 8+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0 | 数据库 |
| Node.js | 18+ | 前端构建工具（Vite 5 要求） |
| npm | 8+ | 前端包管理 |

---

## 二、数据库配置

### 2.1 创建数据库并导入表结构

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

### 2.2 修改 application.yml 数据库连接

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_train?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root        # 替换为实际数据库用户名
    password: root        # 替换为实际数据库密码
```

---

## 三、文件上传（腾讯云 COS）

### 3.1 功能说明

课程封面图、Banner图、新闻图片等均通过**腾讯云 COS（对象存储）**上传和托管。

### 3.2 开通步骤

1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 **对象存储 COS** → 创建存储桶
3. 进入 **访问管理 → API密钥管理**，创建 API 密钥，获取 `SecretId` 和 `SecretKey`
4. （可选）配置 **CDN 加速域名**，加速图片访问

### 3.3 配置项（application.yml）

```yaml
tencent:
  cloud:
    secret-id: AKIDxxxxxxxxxxxxxxxxxx        # API密钥 SecretId
    secret-key: xxxxxxxxxxxxxxxxxxxxxxxx     # API密钥 SecretKey
  cos:
    region: ap-guangzhou                     # 存储桶所在地域
    bucket: your-bucket-1234567890           # 存储桶名称（格式：bucket名-appid）
    cdn-domain:                              # （可选）CDN加速域名，如 https://cdn.example.com
```

### 3.4 存储桶权限

建议将存储桶设置为**公有读、私有写**，这样上传的图片可以直接通过 URL 访问。

### 3.5 费用说明

| 项目 | 计费方式 | 新用户优惠 |
|------|----------|-----------|
| 存储容量 | 按实际存储量计费 | 50GB 免费额度（6个月） |
| 外网下行流量 | 按流量计费 | 有一定免费额度 |
| CDN 加速 | 按流量计费 | 可选，不计费则走 COS 源站 |

> **提示：** 如暂时不需要 COS，可以将课程封面改为本地静态文件。项目已内置 SVG 占位封面在 `backend/uploads/covers/` 目录下，通过 `http://localhost:8088/uploads/covers/` 访问。

---

## 四、视频上传（腾讯云 VOD）

### 4.1 功能说明

课程视频通过**腾讯云 VOD（云点播）**托管和播放，支持自适应码率、防盗链等功能。

**上传流程：** 视频文件 → 腾讯云 COS（中转存储） → 腾讯云 VOD PullUpload → 转码 → 播放

### 4.2 开通步骤

1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 **云点播 VOD** → 开通服务
3. 在 VOD 控制台记录地域信息

### 4.3 配置项（application.yml）

```yaml
tencent:
  vod:
    sub-app-id: 0                            # 点播子应用ID（默认0为主应用）
    region: ap-guangzhou                     # 点播服务地域（建议与COS同地域）
```

### 4.4 费用说明

| 项目 | 计费方式 |
|------|----------|
| 视频存储 | 按存储容量计费 |
| 视频转码 | 按转码时长计费 |
| 加速播放 | 按 CDN 流量计费 |

> **提示：** VOD 仅在上传课程视频时需要，如果暂时用不到视频上传功能，可不配置。

---

## 五、微信支付配置

### 5.1 功能说明

学员购买付费课程时使用微信支付 Native 支付（扫码支付）。

### 5.2 开通步骤

1. 登录 [微信支付商户平台](https://pay.weixin.qq.com/)
2. 注册商户号，完成入驻
3. 在 **API安全** 中：
   - 设置 APIv3 密钥
   - 下载商户证书（apiclient_key.pem）
   - 获取证书序列号
4. 登录 [微信公众平台](https://mp.weixin.qq.com/) 获取 AppID 和 AppSecret

### 5.3 配置项（application.yml）

```yaml
wx:
  pay:
    app-id: wx0000000000000000              # 微信 AppID
    app-secret: xxxxxxxxxxxxxxxxxxxxxxxx    # 微信 AppSecret
    mch-id: 1234567890                      # 商户号
    api-v3-key: xxxxxxxxxxxxxxxxxxxxxxxx    # APIv3 密钥（32位）
    private-key-path: classpath:apiclient_key.pem  # 商户私钥证书路径
    serial-no: xxxxxxxxxxxxxxxxxxxxxxxx     # 商户证书序列号
    notify-url: http://你的域名:8088/api/payment/callback/wechat  # 支付回调地址（外网可达）
```

### 5.4 注意事项

- `notify-url` 必须是**外网可访问**的地址，本地开发时支付回调无法到达
- `apiclient_key.pem` 放在 `backend/src/main/resources/` 目录下
- 如果是免费课程平台，可不配置微信支付

---

## 六、JWT 与安全配置

```yaml
jwt:
  secret: online-train-jwt-secret-key-2024   # JWT签名密钥（生产环境请修改为复杂随机字符串）
  expiration: 86400000                        # Token过期时间（毫秒，默认24小时）
```

---

## 七、快速启动

### 后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/online-train-*.jar
# 服务运行在 http://localhost:8088
# API文档：http://localhost:8088/doc.html
# 管理后台默认账号：admin / admin123
```

### 管理后台

```bash
cd admin
npm install
npm run build          # 生产构建
# 静态文件在 admin/dist/，可部署到 Nginx
# 开发模式：npm run dev → http://localhost:18088
```

### H5 学员端

```bash
cd h5
npm install
npm run build          # 生产构建
# 静态文件在 h5/dist/，可部署到 Nginx
# 开发模式：npm run dev → http://localhost:3001
```

### 微信小程序

使用微信开发者工具打开 `miniapp/` 目录，修改 `app.js` 中 `baseUrl` 为后端实际地址（详见 8.10 节）。

---

## 八、生产环境部署

### 8.1 部署架构

```
用户 → Nginx (80/443)
        ├── /admin/*      → admin/dist/ (管理后台)
        ├── /h5/*         → h5/dist/ (学员端H5)
        ├── /api/*        → 127.0.0.1:8088 (Spring Boot 后端)
        └── /uploads/*    → 静态文件上传目录
```

### 8.2 环境要求

| 组件 | 版本要求 | 用途 |
|------|----------|------|
| JDK | 1.8+ | 后端运行环境 |
| MySQL | 8.0+ | 数据库 |
| Nginx | 1.18+ | 反向代理 + 静态文件托管 |
| Node.js | >= 18 | 前端构建（仅构建时需要） |
| Maven | 3.6+ | 后端构建（仅构建时需要） |

### 8.3 构建项目

```bash
# 1. 构建后端 JAR
cd backend
mvn clean package -DskipTests
# 产物: target/online-train-backend-1.0.0.jar

# 2. 构建管理后台
cd ../admin
npm install && npm run build
# 产物: dist/

# 3. 构建H5学员端
cd ../h5
npm install && npm run build
# 产物: dist/
```

### 8.4 上传到服务器

```bash
# 创建目录结构
ssh user@server "mkdir -p /opt/online-train /data/online-train/uploads"

# 上传 JAR
scp backend/target/online-train-backend-1.0.0.jar user@server:/opt/online-train/

# 上传前端构建产物
scp -r admin/dist/* user@server:/opt/online-train/admin/
scp -r h5/dist/* user@server:/opt/online-train/h5/

# 上传数据库初始化脚本
scp backend/src/main/resources/db/schema.sql user@server:/opt/online-train/
```

### 8.5 初始化数据库

在服务器上执行：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS online_train DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p online_train < /opt/online-train/schema.sql
```

### 8.6 生产配置修改

部署前需修改 `application.yml` 以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/online_train?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root          # 替换为生产数据库用户名
    password: your_password # 替换为生产数据库密码

jwt:
  secret: 生成一个随机字符串  # 替换默认值

upload:
  path: /data/online-train/uploads/  # 使用绝对路径

wx:
  pay:
    notify-url: https://你的域名/api/payment/callback/wechat  # 使用真实域名
```

### 8.7 Nginx 配置

创建 `/etc/nginx/conf.d/online-train.conf`：

```nginx
server {
    listen 80;
    server_name 你的域名;

    # gzip 压缩
    gzip on;
    gzip_min_length 1k;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # 根路径重定向到 H5 学员端（否则因无 index.html 会 403）
    location = / {
        return 301 /h5/;
    }

    # 管理后台
    location /admin {
        alias /opt/online-train/admin/dist;
        index index.html;
        try_files $uri $uri/ @admin;
    }
    location @admin {
        root /opt/online-train/admin/dist;
        try_files /index.html =404;
    }

    # H5学员端
    location /h5 {
        alias /opt/online-train/h5/dist;
        index index.html;
        try_files $uri $uri/ @h5;
    }
    location @h5 {
        root /opt/online-train/h5/dist;
        try_files /index.html =404;
    }

    # 上传文件
    location /uploads/ {
        alias /data/online-train/uploads/;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://127.0.0.1:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 8.8 更新到服务器（每次打包后）

```bash
# 1. 上传 zip 文件到服务器 /opt/online-train/
scp h5-dist.zip admin-dist.zip user@server:/opt/online-train/

# 2. SSH 到服务器解压
ssh user@server
cd /opt/online-train
unzip -o admin-dist.zip    # 解压到 admin/dist/
unzip -o h5-dist.zip       # 解压到 h5/dist/

# 3. 修复文件权限（宝塔 Nginx 以 www 用户运行）
chown -R www:www /opt/online-train/

# 4. 确认文件存在
ls -la /opt/online-train/h5/dist/assets/index-*.js
ls -la /opt/online-train/admin/dist/assets/index-*.js

# 5. 重载 Nginx
nginx -t && nginx -s reload
```
chmod -R 755 /opt/online-train/
```

重载 Nginx：

```bash
nginx -t && nginx -s reload
```

### 8.8 Systemd 服务（后端守护进程）

创建 `/etc/systemd/system/online-train.service`：

```ini
[Unit]
Description=Online Train Backend
After=network.target mysql.service

[Service]
User=root
WorkingDirectory=/opt/online-train
ExecStart=/usr/bin/java -jar /opt/online-train/online-train-backend-1.0.0.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
systemctl daemon-reload
systemctl enable online-train
systemctl start online-train

# 查看状态和日志
systemctl status online-train
journalctl -u online-train -f
```

### 8.9 防火墙配置

```bash
# 开放 HTTP/HTTPS 端口
firewall-cmd --add-port=80/tcp --permanent
firewall-cmd --add-port=443/tcp --permanent
firewall-cmd --reload
```

> **注意：** 不要开放 8088 端口给外网，后端只通过 Nginx 反向代理访问。

### 8.10 各端接口地址切换

三个前端默认指向本地开发环境，部署到服务器时需要切换为真实域名。

#### 8.10.1 管理后台 (admin) 和 H5 学员端 (h5)

这两个前端使用 **相对路径** `/api` 作为请求前缀，请求封装文件：

| 前端 | 文件 | 关键行 |
|------|------|--------|
| 管理后台 | `admin/src/api/index.js` | 第 5 行 `baseURL: '/api'` |
| H5学员端 | `h5/src/api/index.js` | 第 6 行 `baseURL: '/api'` |

**开发环境：** Vite 代理将 `/api` 转发到 `http://127.0.0.1:8088`（见 `vite.config.js`）。

**生产环境：** 无需修改代码，由 Nginx 反向代理将 `/api` 转发给后端（见 8.7 Nginx 配置）。如果后端端口或地址改变，只需修改 Nginx 的 `proxy_pass`。

#### 8.10.2 微信小程序 (miniapp)

小程序**不支持相对路径**，必须使用完整 URL。修改文件：

**文件：** `miniapp/app.js` 第 4 行

```javascript
// 开发环境
baseUrl: 'http://127.0.0.1:8088/api'

// 生产环境（改为真实域名）
baseUrl: 'https://你的域名/api'
```

**注意事项：**
1. 域名必须是 **HTTPS**（微信小程序要求）
2. 域名需在 **微信小程序后台 → 开发管理 → 服务器域名** 中配置 request 合法域名
3. 开发阶段可将 `project.config.json` 中 `urlCheck` 设为 `false` 跳过域名校验

#### 8.10.3 各端配置汇总

| 前端 | 请求封装文件 | baseURL 值 | 切换方式 |
|------|-------------|-----------|---------|
| **admin** 管理后台 | `admin/src/api/index.js:5` | `/api`（相对路径） | 生产环境由 Nginx 代理，无需改代码 |
| **h5** 学员端 | `h5/src/api/index.js:6` | `/api`（相对路径） | 同上 |
| **miniapp** 小程序 | `miniapp/app.js:4` | 绝对路径 | 直接修改 `app.js` 中的 `baseUrl` |

### 8.11 服务汇总

| 服务 | 部署方式 | 端口 |
|------|----------|------|
| 后端 JAR | systemd 服务 | 8088（仅本地） |
| 前端静态文件 | Nginx 托管 | 80/443 |
| 数据库 | MySQL 8.0，配置定时备份 | 3306 |
| 图片 | 腾讯云 COS + CDN 加速 | - |
| 视频 | 腾讯云 VOD + 防盗链 | - |
| 支付 | 微信支付 + 外网可达回调地址 | - |
