# 在线学习平台 — 部署前准备清单（极简版）

> 按优先级排列，标"可选"的项目初期暂不需要。

| 序号 | 需要什么 | 去哪里 |
|:--:|------|------|
| 1 | **服务器**（4核8G，建议腾讯云/阿里云） | 腾讯云 [buy.cloud.tencent.com/cvm](https://buy.cloud.tencent.com/cvm) / 阿里云 [ecs.console.aliyun.com](https://ecs.console.aliyun.com) |
| 2 | **域名**（.com/.cn 均可，购买后需 ICP 备案） | 腾讯云 [buy.cloud.tencent.com/domain](https://buy.cloud.tencent.com/domain) / 阿里云 [wanwang.aliyun.com](https://wanwang.aliyun.com) |
| 3 | **SSL 证书**（小程序要求 HTTPS，初期可用免费证书） | 腾讯云 [console.cloud.tencent.com/ssl](https://console.cloud.tencent.com/ssl) / 阿里云 SSL 证书 |
| 4 | **腾讯云 COS**（对象存储，存课程封面/Banner/头像等图片） | [console.cloud.tencent.com/cos5](https://console.cloud.tencent.com/cos5) |
| 5 | **微信支付**（课程付费购买，需注册商户号） | 商户平台 [pay.weixin.qq.com](https://pay.weixin.qq.com) / 公众平台 [mp.weixin.qq.com](https://mp.weixin.qq.com) |
| 6 | **腾讯云 VOD**（云点播，视频上传转码播放） | [console.cloud.tencent.com/vod](https://console.cloud.tencent.com/vod) |
| 7 | **微信公众号**（服务号，用于微信登录+微信支付） | [mp.weixin.qq.com](https://mp.weixin.qq.com) → 注册服务号 |
| 8 | **微信小程序**（注册+认证，学员端） | [mp.weixin.qq.com](https://mp.weixin.qq.com) → 注册小程序 |
| 9 | **小程序备案**（工信部要求，未备案无法上架） | 小程序后台 → 设置 → 备案 |
| 10 | **腾讯位置服务**（地图选点，可选） | [lbs.qq.com](https://lbs.qq.com) |
| 11 | **腾讯云短信**（手机验证码登录，可选，当前用 log 模拟） | [console.cloud.tencent.com/smsv2](https://console.cloud.tencent.com/smsv2) |

---

## 注册完成后，填入配置

以下信息注册后填入 `backend/src/main/resources/application.yml`：

| 配置项 | 来源 |
|------|------|
| 数据库连接（url/username/password） | 服务器安装 MySQL 后自行设定 |
| `tencent.cloud.secret-id` / `secret-key` | 腾讯云控制台 → API密钥管理 |
| `tencent.cos.bucket` | COS 控制台 → 存储桶名称 |
| `tencent.vod.sub-app-id` | VOD 控制台 → 子应用 ID |
| `wx.pay.app-id` / `app-secret` | 微信公众平台 → 服务号 AppID |
| `wx.pay.mch-id` / `api-v3-key` / 证书 | 微信支付商户平台 → API安全 |
| `tmapKey`（管理后台地图选点） | [lbs.qq.com](https://lbs.qq.com) → Key 管理 |
| 小程序 `miniapp/app.js` baseUrl | 改为 `https://你的域名/api` |
