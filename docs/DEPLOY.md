# 在线学习平台 — 部署配置指南

## 一、基础环境

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 8+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0 | 数据库 |
| Node.js | 16+ | 前端构建工具 |
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

使用微信开发者工具打开 `miniapp/` 目录，修改 `app.js` 中 `baseUrl` 为后端实际地址。

---

## 八、生产环境部署建议

| 服务 | 部署方式 |
|------|----------|
| 后端 JAR | `nohup java -jar` 或 systemd 服务 |
| 前端静态文件 | Nginx + gzip 压缩 |
| 数据库 | MySQL 8.0，配置定时备份 |
| 图片 | 腾讯云 COS + CDN 加速 |
| 视频 | 腾讯云 VOD + 防盗链 |
| 支付 | 微信支付 + 外网可达回调地址 |
| 域名 | 建议配置 HTTPS 证书 |
