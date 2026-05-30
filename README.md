# 在线学习平台

全终端在线学习系统（H5 + 微信小程序），集成课程视频学习、章节题库练习、线上正式考试、课程付费购买、双渠道支付、全维度学情&营收数据统计能力。

## 项目架构

```
awesome-codebuddy-online-train/
├── backend/          # Java Spring Boot 后端
├── admin/            # Vue 3 + Element Plus 管理后台
├── h5/               # Vue 3 + Vant 4 H5学员端
├── miniapp/          # 微信小程序学员端
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

### 微信小程序学员端
- 原生微信小程序框架
- 微信开发者工具
- 与 H5 端共用同一后端 API

## H5 学员端页面展示

### 登录与首页
| 登录 | 首页 |
|------|------|
| ![登录](screenshots/01-login.png) | ![首页](screenshots/03-home.png) |

### 课程学习
| 课程列表 | 课程详情 | 课程目录 |
|----------|----------|----------|
| ![课程列表](screenshots/04-course-list.png) | ![课程详情](screenshots/05-course-detail.png) | ![课程目录](screenshots/06-chapter-list.png) |

| 视频播放 | 章节练习 | 练习答题 |
|----------|----------|----------|
| ![视频播放](screenshots/07-video-player.png) | ![章节练习](screenshots/08-practice-home.png) | ![练习答题](screenshots/09-practice-question.png) |

### 在线考试
| 考试列表 | 考试确认 |
|----------|----------|
| ![考试列表](screenshots/10-exam-list.png) | ![考试确认](screenshots/11-exam-start.png) |

### 个人中心
| 我的 | 我的课程 | 我的订单 |
|------|----------|----------|
| ![我的](screenshots/12-profile.png) | ![我的课程](screenshots/13-my-courses.png) | ![我的订单](screenshots/14-orders.png) |

| 我的错题 | 考试记录 | 学习记录 |
|----------|----------|----------|
| ![我的错题](screenshots/15-wrong-questions.png) | ![考试记录](screenshots/16-exam-records.png) | ![学习记录](screenshots/17-learning-records.png) |

### 付费购买
| 确认订单 |
|----------|
| ![确认订单](screenshots/18-order-confirm.png) |

## 管理后台页面展示

### 登录与仪表盘
| 登录 | 仪表盘 |
|------|--------|
| ![登录](screenshots/admin-01-login.png) | ![仪表盘](screenshots/admin-03-dashboard.png) |

### 内容管理
| 课程分类 | 课程管理 | 课程编辑 |
|----------|----------|----------|
| ![课程分类](screenshots/admin-04-category-list.png) | ![课程管理](screenshots/admin-05-course-list.png) | ![课程编辑](screenshots/admin-06-course-edit.png) |

### 题库与试卷
| 题库管理 | 题目导入 | 试卷管理 |
|----------|----------|----------|
| ![题库管理](screenshots/admin-07-question-list.png) | ![题目导入](screenshots/admin-08-question-import.png) | ![试卷管理](screenshots/admin-09-exam-list.png) |

| 试卷编辑 | 考试记录 |
|----------|----------|
| ![试卷编辑](screenshots/admin-10-exam-edit.png) | ![考试记录](screenshots/admin-11-exam-records.png) |

### 学员与订单
| 学员管理 | 学员详情 | 订单管理 |
|----------|----------|----------|
| ![学员管理](screenshots/admin-12-student-list.png) | ![学员详情](screenshots/admin-13-student-detail.png) | ![订单管理](screenshots/admin-14-order-list.png) |

### 数据统计
| 营收统计 | 学情统计 | 考试统计 |
|----------|----------|----------|
| ![营收统计](screenshots/admin-15-revenue-stats.png) | ![学情统计](screenshots/admin-16-learning-stats.png) | ![考试统计](screenshots/admin-17-exam-stats.png) |

## 快速开始

### 1. 数据库初始化

```bash
# 一键执行：建库 + 建表 + 管理员账号 + 测试数据（课程/章节/题目/试卷/订单等）
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8088
# API文档: http://localhost:8088/doc.html
```

### 3. 启动管理后台

```bash
cd admin
npm install
npm run dev
# 管理后台运行在 http://localhost:3000
```

### 4. 启动H5学员端

```bash
cd h5
npm install
npm run dev
# H5学员端运行在 http://localhost:3001
```

### 5. 启动微信小程序学员端

```bash
# 使用微信开发者工具打开 miniapp 目录
# 或命令行：
/Applications/wechatwebdevtools.app/Contents/MacOS/cli open --project miniapp
# 注意：真机调试时需修改 app.js 中 globalData.baseUrl 为局域网IP
```

## 测试账号

### 管理员
| 账号 | 密码 | 说明 |
|------|------|------|
| admin | admin123 | 管理后台登录 |

### 学员
| 账号（手机号） | 密码 | 昵称 |
|------|------|------|
| 13800001001 | 123456 | 张三 |
| 13800001002 | 123456 | 李四 |
| 13800001003 | 123456 | 王五 |
| 13800001004 | 123456 | 赵六 |
| 13800001005 | 123456 | 小红 |
| 13800001006 | 123456 | 小明 |
| 13800001007 | 123456 | Tom |
| 13800001008 | 123456 | Jerry |

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
✅ 微信小程序学员端 原生开发完整实现
