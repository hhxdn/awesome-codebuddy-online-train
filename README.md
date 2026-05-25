# H5 在线学习平台

全手机自适应H5在线学习系统，集成课程视频学习、章节题库练习、线上正式考试、课程付费购买、双渠道支付、全维度学情&营收数据统计能力。

## 项目架构

```
awesome-codebuddy-online-train/
├── backend/          # Java Spring Boot 后端
├── admin/            # Vue 3 + Element Plus 管理后台
├── h5/               # Vue 3 + Vant 4 H5学员端
└── README.md
```

## 技术栈

### 后端
- Java 8 + Spring Boot 2.7.18
- MyBatis-Plus 3.5.3.1
- MySQL 8.0
- JWT 认证
- Knife4j (Swagger) API文档
- Apache POI (Excel导入)

### 管理后台
- Vue 3 + Vite
- Element Plus + Icons
- ECharts (vue-echarts)
- Axios + Pinia + Vue Router

### H5 学员端
- Vue 3 + Vite
- Vant 4 移动端UI组件库
- Axios + Pinia + Vue Router
- 响应式适配 (max-width: 750px)

## 快速开始

### 1. 数据库初始化

```bash
# 执行SQL脚本创建数据库和表结构
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
# API文档: http://localhost:8080/doc.html
```

### 3. 启动管理后台

```bash
cd admin
npm install
npm run dev
# 管理后台运行在 http://localhost:3000
# 默认管理员: admin / admin123
```

### 4. 启动H5学员端

```bash
cd h5
npm install
npm run dev
# H5学员端运行在 http://localhost:3001
```

## 核心功能

### H5 学员端
- 📱 手机号验证码登录 / 微信快捷登录
- 📚 课程浏览、搜索、分类筛选
- 🎬 视频断点续播、倍速播放
- ✍️ 章节题库练习（单选/多选/判断/简答）
- 📝 错题自动归集、反复重做
- 📋 在线限时考试、自动阅卷、考后复盘
- 💰 课程付费购买（微信/支付宝双支付）
- 📊 个人学情数据看板

### 管理后台
- 📂 课程分类管理
- 🎯 课程管理（视频章节、免费/付费设置）
- 📖 题库管理（单题录入/Excel批量导入）
- 📝 试卷管理（手动组卷/发布考试）
- 👥 学员管理（学习轨迹全掌握）
- 💵 订单管理 & 营收统计
- 📈 全维度学情数据可视化
- 🛡️ 角色权限管理

## 数据库表

| 表名 | 说明 |
|------|------|
| user | 用户表（学员+管理员） |
| course_category | 课程分类表 |
| course | 课程表 |
| chapter | 章节表 |
| question | 题目表 |
| question_option | 题目选项表 |
| exam_paper | 试卷表 |
| exam_paper_question | 试卷题目关联表 |
| exam_record | 考试记录表 |
| exam_answer | 考试答题表 |
| learning_record | 学习记录表 |
| wrong_question | 错题表 |
| orders | 订单表 |
| payment_log | 支付日志表 |

## API 接口

### H5 学员端 API (`/api/h5/`)
- `POST /api/h5/user/login` - 手机号验证码登录
- `POST /api/h5/user/wx-login` - 微信登录
- `GET /api/h5/courses` - 课程列表
- `GET /api/h5/courses/{id}` - 课程详情
- `POST /api/h5/practice/submit/{chapterId}` - 提交章节练习
- `POST /api/h5/exam/start/{paperId}` - 开始考试
- `POST /api/h5/exam/submit/{recordId}` - 提交考试
- `POST /api/h5/orders` - 创建订单
- `POST /api/h5/orders/{id}/pay` - 发起支付
- `POST /api/h5/learning/record` - 保存学习进度

### 管理后台 API (`/api/admin/`)
- `POST /api/admin/auth/login` - 管理员登录
- `CRUD /api/admin/categories` - 分类管理
- `CRUD /api/admin/courses` - 课程管理
- `CRUD /api/admin/questions` - 题目管理
- `POST /api/admin/questions/import` - Excel批量导入
- `CRUD /api/admin/exam-papers` - 试卷管理
- `GET /api/admin/statistics/revenue` - 营收统计
- `GET /api/admin/statistics/learning` - 学情统计
- `GET /api/admin/statistics/exam` - 考试统计

## 项目状态

✅ 后端 Spring Boot 完整实现
✅ 管理后台 Vue 3 + Element Plus 完整实现
✅ H5 学员端 Vue 3 + Vant 4 完整实现
