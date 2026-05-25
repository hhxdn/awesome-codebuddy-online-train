-- ============================================
-- 在线学习平台 测试数据
-- ============================================

-- 1. 用户（保留已有admin，新增学员）
INSERT INTO `user` (`phone`, `nickname`, `avatar`, `password`, `role`, `status`, `register_time`, `total_study_duration`) VALUES
('13800001001', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-01-15 09:30:00', 36000),
('13800001002', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-02-10 14:20:00', 28000),
('13800001003', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-03-05 10:00:00', 15000),
('13800001004', '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-03-20 16:45:00', 8000),
('13800001005', '小红', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohong', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-04-01 08:00:00', 45000),
('13800001006', '小明', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-04-15 11:30:00', 32000),
('13800001007', 'Tom', 'https://api.dicebear.com/7.x/avataaars/svg?seed=tom', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-04-20 13:15:00', 22000),
('13800001008', 'Jerry', 'https://api.dicebear.com/7.x/avataaars/svg?seed=jerry', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1, '2026-05-01 09:00:00', 5000);

-- 2. 课程分类
INSERT INTO `course_category` (`name`, `cover`, `sort_order`, `status`) VALUES
('Java开发', 'https://img.alicdn.com/imgextra/i3/O1CN01Kj5T1U1qGvqJqVqXc_!!6000000005472-2-tps-240-240.png', 1, 1),
('前端开发', 'https://img.alicdn.com/imgextra/i2/O1CN01QZxJ5R1qGvqJqVqXc_!!6000000005472-2-tps-240-240.png', 2, 1),
('Python开发', 'https://img.alicdn.com/imgextra/i4/O1CN01XZxJ5R1qGvqJqVqXc_!!6000000005472-2-tps-240-240.png', 3, 1),
('AI与大模型', 'https://img.alicdn.com/imgextra/i1/O1CN01YZxJ5R1qGvqJqVqXc_!!6000000005472-2-tps-240-240.png', 4, 1),
('数据库', 'https://img.alicdn.com/imgextra/i3/O1CN01ZZxJ5R1qGvqJqVqXc_!!6000000005472-2-tps-240-240.png', 5, 1),
('云计算与DevOps', 'https://img.alicdn.com/imgextra/i2/O1CN01AZxJ5R1qGvqJqVqXc_!!6000000005472-2-tps-240-240.png', 6, 1);

-- 3. 课程
INSERT INTO `course` (`title`, `cover`, `description`, `category_id`, `price`, `is_free`, `sort_order`, `is_recommend`, `status`, `student_count`, `update_status`) VALUES
('Spring Boot 从入门到精通', 'https://img.alicdn.com/imgextra/i1/O1CN01course001_!!6000000005472-2-tps-360-200.png', '全面讲解Spring Boot框架的使用，包括自动配置、Web开发、数据访问、安全认证等核心知识点', 1, 0.00, 1, 1, 1, 'UP', 1250, '已完结'),
('Spring Cloud 微服务实战', 'https://img.alicdn.com/imgextra/i2/O1CN01course002_!!6000000005472-2-tps-360-200.png', '深入讲解Spring Cloud Alibaba微服务架构，涵盖Nacos、Sentinel、Seata、Gateway等组件', 1, 199.00, 0, 2, 1, 'UP', 680, '更新中'),
('Vue3 + TypeScript 实战', 'https://img.alicdn.com/imgextra/i3/O1CN01course003_!!6000000005472-2-tps-360-200.png', '从零到一掌握Vue3+TypeScript开发，包含Composition API、Pinia状态管理、Vite构建工具', 2, 0.00, 1, 1, 1, 'UP', 2100, '已完结'),
('React18 全家桶实战', 'https://img.alicdn.com/imgextra/i4/O1CN01course004_!!6000000005472-2-tps-360-200.png', 'React18新特性全面解析，Hooks、Redux Toolkit、Next.js、React Router全覆盖', 2, 149.00, 0, 2, 1, 'UP', 890, '已完结'),
('Python 数据分析与可视化', 'https://img.alicdn.com/imgextra/i1/O1CN01course005_!!6000000005472-2-tps-360-200.png', '使用Pandas、NumPy、Matplotlib、Seaborn进行数据分析和可视化展示', 3, 0.00, 1, 1, 1, 'UP', 1520, '已完结'),
('Django 企业级开发', 'https://img.alicdn.com/imgextra/i2/O1CN01course006_!!6000000005472-2-tps-360-200.png', 'Django REST Framework + Celery + Redis + Docker企业级项目实战', 3, 129.00, 0, 2, 0, 'UP', 340, '已完结'),
('ChatGPT 应用开发实战', 'https://img.alicdn.com/imgextra/i3/O1CN01course007_!!6000000005472-2-tps-360-200.png', '学习如何调用OpenAI API，开发智能聊天机器人、AI写作助手、代码生成器等应用', 4, 299.00, 0, 1, 1, 'UP', 1850, '更新中'),
('LangChain 框架深度解析', 'https://img.alicdn.com/imgextra/i4/O1CN01course008_!!6000000005472-2-tps-360-200.png', '全面掌握LangChain框架，构建RAG应用、Agent智能体、多模态AI应用', 4, 399.00, 0, 2, 1, 'UP', 760, '更新中'),
('MySQL 性能优化实战', 'https://img.alicdn.com/imgextra/i1/O1CN01course009_!!6000000005472-2-tps-360-200.png', '索引优化、SQL调优、分库分表、主从复制、高可用架构一网打尽', 5, 0.00, 1, 1, 1, 'UP', 980, '已完结'),
('Redis 深度剖析', 'https://img.alicdn.com/imgextra/i2/O1CN01course010_!!6000000005472-2-tps-360-200.png', 'Redis数据结构、持久化、哨兵、集群、缓存穿透/击穿/雪崩解决方案', 5, 99.00, 0, 2, 0, 'UP', 520, '已完结'),
('Docker + K8s 云原生入门', 'https://img.alicdn.com/imgextra/i3/O1CN01course011_!!6000000005472-2-tps-360-200.png', '从Docker基础到Kubernetes编排，手把手教你构建云原生应用', 6, 0.00, 1, 1, 1, 'UP', 1100, '已完结'),
('Jenkins + GitLab CI/CD 实战', 'https://img.alicdn.com/imgextra/i4/O1CN01course012_!!6000000005472-2-tps-360-200.png', '搭建企业级CI/CD流水线，自动化构建、测试、部署全流程', 6, 89.00, 0, 2, 1, 'UP', 430, '已完结');

-- 4. 章节（每个课程4-6个章节）
-- 课程1: Spring Boot从入门到精通 (course_id=1)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(1, 'Spring Boot 概述与环境搭建', 'https://example.com/video/springboot-01.mp4', 1200, 1),
(1, 'Spring Boot 自动配置原理', 'https://example.com/video/springboot-02.mp4', 1800, 2),
(1, 'Spring Boot Web开发', 'https://example.com/video/springboot-03.mp4', 2400, 3),
(1, 'Spring Boot 数据访问层', 'https://example.com/video/springboot-04.mp4', 2100, 4),
(1, 'Spring Boot 安全与认证', 'https://example.com/video/springboot-05.mp4', 2700, 5),
(1, 'Spring Boot 项目实战', 'https://example.com/video/springboot-06.mp4', 3600, 6);

-- 课程2: Spring Cloud微服务实战 (course_id=2)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(2, '微服务架构概述', 'https://example.com/video/springcloud-01.mp4', 1500, 1),
(2, 'Nacos 注册与配置中心', 'https://example.com/video/springcloud-02.mp4', 2200, 2),
(2, 'Sentinel 流量治理', 'https://example.com/video/springcloud-03.mp4', 1800, 3),
(2, 'Gateway 网关实战', 'https://example.com/video/springcloud-04.mp4', 2000, 4),
(2, 'Seata 分布式事务', 'https://example.com/video/springcloud-05.mp4', 2400, 5);

-- 课程3: Vue3+TypeScript实战 (course_id=3)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(3, 'Vue3 新特性概览', 'https://example.com/video/vue3-01.mp4', 1300, 1),
(3, 'Composition API 深入', 'https://example.com/video/vue3-02.mp4', 2100, 2),
(3, 'TypeScript 类型体操', 'https://example.com/video/vue3-03.mp4', 1900, 3),
(3, 'Pinia 状态管理', 'https://example.com/video/vue3-04.mp4', 1600, 4),
(3, 'Vite 构建与优化', 'https://example.com/video/vue3-05.mp4', 1200, 5),
(3, '实战：电商管理后台', 'https://example.com/video/vue3-06.mp4', 3000, 6);

-- 课程4: React18全家桶实战 (course_id=4)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(4, 'React18 新特性', 'https://example.com/video/react-01.mp4', 1400, 1),
(4, 'Hooks 深度解析', 'https://example.com/video/react-02.mp4', 2200, 2),
(4, 'Redux Toolkit 入门', 'https://example.com/video/react-03.mp4', 1800, 3),
(4, 'React Router v6', 'https://example.com/video/react-04.mp4', 1500, 4),
(4, 'Next.js SSR开发', 'https://example.com/video/react-05.mp4', 2500, 5);

-- 课程5: Python数据分析与可视化 (course_id=5)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(5, 'Python 数据分析基础', 'https://example.com/video/py-01.mp4', 1100, 1),
(5, 'NumPy 科学计算', 'https://example.com/video/py-02.mp4', 2000, 2),
(5, 'Pandas 数据处理', 'https://example.com/video/py-03.mp4', 2400, 3),
(5, 'Matplotlib 数据可视化', 'https://example.com/video/py-04.mp4', 1800, 4),
(5, 'Seaborn 高级可视化', 'https://example.com/video/py-05.mp4', 1500, 5);

-- 课程6: Django企业级开发 (course_id=6)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(6, 'Django 框架基础', 'https://example.com/video/django-01.mp4', 1500, 1),
(6, 'DRF API 开发', 'https://example.com/video/django-02.mp4', 2200, 2),
(6, 'Celery 异步任务', 'https://example.com/video/django-03.mp4', 1800, 3),
(6, 'Docker 部署实战', 'https://example.com/video/django-04.mp4', 2000, 4);

-- 课程7: ChatGPT应用开发实战 (course_id=7)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(7, 'OpenAI API 入门', 'https://example.com/video/chatgpt-01.mp4', 1600, 1),
(7, 'Prompt 工程实践', 'https://example.com/video/chatgpt-02.mp4', 2000, 2),
(7, 'Function Calling', 'https://example.com/video/chatgpt-03.mp4', 1900, 3),
(7, '聊天机器人开发', 'https://example.com/video/chatgpt-04.mp4', 2400, 4),
(7, 'AI 工作流自动化', 'https://example.com/video/chatgpt-05.mp4', 2100, 5);

-- 课程8: LangChain深度解析 (course_id=8)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(8, 'LangChain 核心概念', 'https://example.com/video/langchain-01.mp4', 1700, 1),
(8, 'RAG 检索增强生成', 'https://example.com/video/langchain-02.mp4', 2300, 2),
(8, 'Agent 智能体开发', 'https://example.com/video/langchain-03.mp4', 2500, 3),
(8, '多模态 AI 应用', 'https://example.com/video/langchain-04.mp4', 2100, 4);

-- 课程9: MySQL性能优化实战 (course_id=9)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(9, 'MySQL 架构与存储引擎', 'https://example.com/video/mysql-01.mp4', 1400, 1),
(9, '索引原理与优化', 'https://example.com/video/mysql-02.mp4', 2200, 2),
(9, 'SQL 语句优化技巧', 'https://example.com/video/mysql-03.mp4', 2000, 3),
(9, '高可用架构设计', 'https://example.com/video/mysql-04.mp4', 2500, 4);

-- 课程10: Redis深度剖析 (course_id=10)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(10, 'Redis 数据结构详解', 'https://example.com/video/redis-01.mp4', 1800, 1),
(10, '持久化机制 RDB vs AOF', 'https://example.com/video/redis-02.mp4', 1600, 2),
(10, '哨兵与集群模式', 'https://example.com/video/redis-03.mp4', 2200, 3),
(10, '缓存问题解决方案', 'https://example.com/video/redis-04.mp4', 1900, 4);

-- 课程11: Docker+K8s云原生入门 (course_id=11)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(11, 'Docker 容器基础', 'https://example.com/video/docker-01.mp4', 1600, 1),
(11, 'Dockerfile 与镜像构建', 'https://example.com/video/docker-02.mp4', 1900, 2),
(11, 'Docker Compose 编排', 'https://example.com/video/docker-03.mp4', 1500, 3),
(11, 'Kubernetes 核心概念', 'https://example.com/video/k8s-01.mp4', 2400, 4),
(11, 'K8s 部署应用实战', 'https://example.com/video/k8s-02.mp4', 2800, 5);

-- 课程12: Jenkins+GitLab CI/CD实战 (course_id=12)
INSERT INTO `chapter` (`course_id`, `title`, `video_url`, `video_duration`, `sort_order`) VALUES
(12, 'CI/CD 概念与流程', 'https://example.com/video/cicd-01.mp4', 1300, 1),
(12, 'Jenkins 安装与配置', 'https://example.com/video/cicd-02.mp4', 1700, 2),
(12, 'GitLab CI 配置解析', 'https://example.com/video/cicd-03.mp4', 2000, 3),
(12, '自动化部署流水线', 'https://example.com/video/cicd-04.mp4', 2300, 4);

-- 5. 题目数据（每个课程5-8道题）
-- 课程1: Spring Boot (course_id=1)
INSERT INTO `question` (`course_id`, `chapter_id`, `type`, `content`, `score`, `answer`, `analysis`, `status`) VALUES
(1, 1, 'SINGLE', 'Spring Boot 的核心特性不包括以下哪项？', 2, 'D', 'Spring Boot 核心特性包括自动配置、起步依赖、Actuator监控和内嵌服务器，不包括ORM框架，ORM框架是独立的', 1),
(1, 2, 'SINGLE', '@SpringBootApplication 注解包含以下哪些注解？', 2, 'A', '@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan', 1),
(1, 3, 'SINGLE', 'Spring Boot 默认内嵌的Web服务器是？', 2, 'B', 'Spring Boot默认内嵌Tomcat，也支持Jetty和Undertow', 1),
(1, 4, 'SINGLE', 'MyBatis-Plus 中逻辑删除使用的注解是？', 2, 'C', '@TableLogic 是MyBatis-Plus的逻辑删除注解', 1),
(1, 5, 'MULTIPLE', 'Spring Security 支持以下哪些认证方式？', 3, 'ABCD', 'Spring Security支持表单登录、JWT、OAuth2和LDAP认证', 1),
(1, 6, 'JUDGE', 'Spring Boot 2.x 默认使用 Java 8 作为最低版本要求', 1, 'T', 'Spring Boot 2.x 要求 Java 8 及以上版本', 1);

-- 课程3: Vue3+TypeScript (course_id=3)
INSERT INTO `question` (`course_id`, `chapter_id`, `type`, `content`, `score`, `answer`, `analysis`, `status`) VALUES
(3, 1, 'SINGLE', 'Vue3 中 Composition API 的入口函数是？', 2, 'B', 'setup() 是Composition API的入口函数，在组件创建之前执行', 1),
(3, 2, 'SINGLE', 'ref 和 reactive 的主要区别是？', 2, 'C', 'ref用于基本类型和对象，reactive仅用于对象类型。ref需要通过.value访问', 1),
(3, 3, 'MULTIPLE', 'TypeScript 支持以下哪些类型？', 3, 'ABCD', 'TypeScript支持元组、枚举、联合类型和交叉类型等高级类型', 1),
(3, 4, 'SINGLE', 'Vue3 官方推荐的状态管理库是？', 2, 'D', 'Pinia 是 Vue3 官方推荐的状态管理库，是Vuex 5的替代方案', 1),
(3, 5, 'JUDGE', 'Vite 在生产环境中使用 Rollup 进行打包', 1, 'T', 'Vite 开发环境使用esbuild预构建，生产环境使用Rollup打包', 1),
(3, 6, 'ESSAY', '请简述 Vue3 相比 Vue2 的主要改进点', 5, 'Composition API、性能提升、TypeScript支持、Teleport、Suspense、Fragment、Tree Shaking等', '主要改进包括：更小的包体积、更快的渲染速度、更好的TS支持、新增组合式API等', 1);

-- 课程5: Python数据分析 (course_id=5)
INSERT INTO `question` (`course_id`, `chapter_id`, `type`, `content`, `score`, `answer`, `analysis`, `status`) VALUES
(5, 1, 'SINGLE', 'Python 中用于科学计算的基础库是？', 2, 'A', 'NumPy 是 Python 科学计算的基础库，提供多维数组对象和数学函数', 1),
(5, 2, 'SINGLE', '以下哪个方法可以创建 NumPy 全零数组？', 2, 'C', 'np.zeros() 用于创建全零数组，np.ones()创建全一数组，np.eye()创建单位矩阵', 1),
(5, 3, 'MULTIPLE', 'Pandas 中读取数据的常用方法包括？', 3, 'ABC', 'read_csv、read_excel、read_json 是Pandas常用读取方法，read_sql需要数据库连接', 1),
(5, 4, 'SINGLE', 'Matplotlib 中用于创建画布的函数是？', 2, 'B', 'plt.figure() 用于创建画布对象，subplot()用于创建子图区域', 1),
(5, 5, 'JUDGE', 'Seaborn 是基于 Matplotlib 的高级可视化库', 1, 'T', 'Seaborn 是基于 matplotlib 的统计数据可视化库，提供更美观的默认样式', 1),
(5, 1, 'ESSAY', '请简述数据清洗的常用步骤', 5, '缺失值处理、重复值处理、异常值检测与处理、数据类型转换、数据标准化', '数据清洗是数据分析的前提，包括处理缺失值、重复值、异常值等步骤', 1);

-- 课程7: ChatGPT应用开发 (course_id=7)
INSERT INTO `question` (`course_id`, `chapter_id`, `type`, `content`, `score`, `answer`, `analysis`, `status`) VALUES
(7, 1, 'SINGLE', 'OpenAI API 中控制输出随机性的参数是？', 2, 'C', 'temperature 控制输出的随机性，值越高越随机（0-2），越低越确定', 1),
(7, 1, 'SINGLE', 'GPT-4 的上下文窗口最大是多少 tokens？', 2, 'B', 'GPT-4 Turbo 的上下文窗口为 128K tokens', 1),
(7, 2, 'MULTIPLE', 'Prompt 工程中常用技巧包括？', 3, 'ABCD', 'few-shot、chain-of-thought、role-playing、zero-shot 都是Prompt工程常用技巧', 1),
(7, 3, 'JUDGE', 'Function Calling 功能可以让 GPT 调用外部API', 1, 'T', 'Function Calling 允许 GPT 根据对话内容决定是否调用预定义的函数/API', 1),
(7, 4, 'ESSAY', '请描述如何构建一个基于RAG的智能知识问答系统', 8, '需要包含文档加载、文本分割、向量化存储、检索增强生成等步骤', 'RAG系统核心组件：文档解析->文本分割->向量嵌入->向量数据库->检索->LLM生成', 1);

-- 课程9: MySQL优化 (course_id=9)
INSERT INTO `question` (`course_id`, `chapter_id`, `type`, `content`, `score`, `answer`, `analysis`, `status`) VALUES
(9, 1, 'SINGLE', 'MySQL InnoDB 存储引擎的默认索引结构是？', 2, 'B', 'InnoDB 使用 B+Tree 作为默认索引结构', 1),
(9, 2, 'MULTIPLE', '以下哪些操作会导致索引失效？', 3, 'ABCD', '在索引列上使用函数、LIKE前模糊匹配、隐式类型转换、OR条件（非索引列）都会导致索引失效', 1),
(9, 3, 'SINGLE', 'EXPLAIN 命令中 type 字段的哪个值表示最优的查询类型？', 2, 'A', 'EXPLAIN中type字段从好到差：system > const > eq_ref > ref > range > index > ALL', 1),
(9, 4, 'JUDGE', 'MySQL主从复制可以同时提高读写性能', 1, 'T', '主从复制可以提高读性能（读写分离），但写操作只能在主库进行', 1),
(9, 1, 'ESSAY', '请简述数据库分库分表的常见方案', 5, '水平分表、垂直分表、水平分库、垂直分库，以及常用的中间件如ShardingSphere', '分库分表是解决大数据量性能瓶颈的常用方案，需根据业务场景选择', 1);

-- 6. 题目选项
INSERT INTO `question_option` (`question_id`, `option_label`, `content`, `is_correct`) VALUES
-- 题目1选项
(1, 'A', '自动配置', 0),
(1, 'B', '起步依赖', 0),
(1, 'C', 'Actuator 监控', 0),
(1, 'D', 'ORM 框架', 1),
-- 题目2选项
(2, 'A', '@Configuration + @EnableAutoConfiguration + @ComponentScan', 1),
(2, 'B', '@Controller + @Service + @Repository', 0),
(2, 'C', '@Bean + @Autowired + @Component', 0),
(2, 'D', '@Entity + @Table + @Column', 0),
-- 题目3选项
(3, 'A', 'Jetty', 0),
(3, 'B', 'Tomcat', 1),
(3, 'C', 'Netty', 0),
(3, 'D', 'Undertow', 0),
-- 题目4选项
(4, 'A', '@LogicDelete', 0),
(4, 'B', '@Deleted', 0),
(4, 'C', '@TableLogic', 1),
(4, 'D', '@TableField', 0),
-- 题目5选项
(5, 'A', '表单登录', 1),
(5, 'B', 'JWT Token', 1),
(5, 'C', 'OAuth2', 1),
(5, 'D', 'LDAP', 1);

INSERT INTO `question_option` (`question_id`, `option_label`, `content`, `is_correct`) VALUES
-- 题目6 (判断题，不需要选项，但题目7开始需要)
(7, 'A', 'mounted()', 0),
(7, 'B', 'setup()', 1),
(7, 'C', 'created()', 0),
(7, 'D', 'onMounted()', 0),
-- 题目8选项
(8, 'A', 'ref 只能用于对象，reactive 只能用于基本类型', 0),
(8, 'B', '两者完全一样，可以互换使用', 0),
(8, 'C', 'ref 用于基本类型和对象，reactive 仅用于对象。ref 需 .value 访问', 1),
(8, 'D', 'reactive 是 ref 的底层实现', 0),
-- 题目9选项
(9, 'A', '元组(Tuple)', 1),
(9, 'B', '枚举(Enum)', 1),
(9, 'C', '联合类型(Union)', 1),
(9, 'D', '交叉类型(Intersection)', 1),
-- 题目10选项
(10, 'A', 'Vuex', 0),
(10, 'B', 'Redux', 0),
(10, 'C', 'MobX', 0),
(10, 'D', 'Pinia', 1);

INSERT INTO `question_option` (`question_id`, `option_label`, `content`, `is_correct`) VALUES
-- 题目13 (Python题库)
(13, 'A', 'NumPy', 1),
(13, 'B', 'Django', 0),
(13, 'C', 'Flask', 0),
(13, 'D', 'Requests', 0),
-- 题目14选项
(14, 'A', 'np.ones()', 0),
(14, 'B', 'np.eye()', 0),
(14, 'C', 'np.zeros()', 1),
(14, 'D', 'np.arange()', 0),
-- 题目15选项
(15, 'A', 'read_csv()', 1),
(15, 'B', 'read_excel()', 1),
(15, 'C', 'read_json()', 1),
(15, 'D', 'read_sql()', 0),
-- 题目16选项
(16, 'A', 'plt.plot()', 0),
(16, 'B', 'plt.figure()', 1),
(16, 'C', 'plt.show()', 0),
(16, 'D', 'plt.subplot()', 0);

INSERT INTO `question_option` (`question_id`, `option_label`, `content`, `is_correct`) VALUES
-- ChatGPT题库
(19, 'A', 'max_tokens', 0),
(19, 'B', 'top_p', 0),
(19, 'C', 'temperature', 1),
(19, 'D', 'frequency_penalty', 0),
-- 题目20选项
(20, 'A', '4096', 0),
(20, 'B', '128000', 1),
(20, 'C', '8192', 0),
(20, 'D', '32768', 0),
-- 题目21选项
(21, 'A', 'few-shot', 1),
(21, 'B', 'chain-of-thought', 1),
(21, 'C', 'role-playing', 1),
(21, 'D', 'zero-shot', 1);

INSERT INTO `question_option` (`question_id`, `option_label`, `content`, `is_correct`) VALUES
-- MySQL题库
(24, 'A', 'Hash', 0),
(24, 'B', 'B+Tree', 1),
(24, 'C', 'R-Tree', 0),
(24, 'D', 'FullText', 0),
-- 题目25选项
(25, 'A', '在索引列上使用函数', 1),
(25, 'B', 'LIKE 前模糊匹配', 1),
(25, 'C', '隐式类型转换', 1),
(25, 'D', 'OR 条件中索引列和非索引列混合', 1),
-- 题目26选项
(26, 'A', 'const', 1),
(26, 'B', 'ref', 0),
(26, 'C', 'range', 0),
(26, 'D', 'index', 0);

-- 7. 试卷
INSERT INTO `exam_paper` (`course_id`, `title`, `duration_minutes`, `total_score`, `pass_score`, `max_attempts`, `status`) VALUES
(1, 'Spring Boot 基础知识测试', 60, 100, 60, 3, 'PUBLISHED'),
(3, 'Vue3 + TypeScript 阶段测试', 90, 100, 60, 2, 'PUBLISHED'),
(5, 'Python 数据分析考核', 60, 100, 70, 3, 'PUBLISHED'),
(7, 'ChatGPT 应用开发考核', 120, 100, 60, 2, 'PUBLISHED'),
(9, 'MySQL 性能优化考核', 90, 100, 60, 3, 'PUBLISHED');

-- 8. 试卷-题目关联
-- 试卷1: Spring Boot (题目1-6)
INSERT INTO `exam_paper_question` (`exam_paper_id`, `question_id`, `sort_order`) VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5), (1, 6, 6);
-- 试卷2: Vue3 (题目7-12)
INSERT INTO `exam_paper_question` (`exam_paper_id`, `question_id`, `sort_order`) VALUES
(2, 7, 1), (2, 8, 2), (2, 9, 3), (2, 10, 4), (2, 11, 5), (2, 12, 6);
-- 试卷3: Python (题目13-18)
INSERT INTO `exam_paper_question` (`exam_paper_id`, `question_id`, `sort_order`) VALUES
(3, 13, 1), (3, 14, 2), (3, 15, 3), (3, 16, 4), (3, 17, 5), (3, 18, 6);
-- 试卷4: ChatGPT (题目19-23)
INSERT INTO `exam_paper_question` (`exam_paper_id`, `question_id`, `sort_order`) VALUES
(4, 19, 1), (4, 20, 2), (4, 21, 3), (4, 22, 4), (4, 23, 5);
-- 试卷5: MySQL (题目24-28)
INSERT INTO `exam_paper_question` (`exam_paper_id`, `question_id`, `sort_order`) VALUES
(5, 24, 1), (5, 25, 2), (5, 26, 3), (5, 27, 4), (5, 28, 5);

-- 9. 考试记录
INSERT INTO `exam_record` (`user_id`, `exam_paper_id`, `score`, `is_pass`, `start_time`, `end_time`, `submit_time`, `status`) VALUES
(2, 1, 85.0, 1, '2026-03-15 10:00:00', '2026-03-15 10:50:00', '2026-03-15 10:50:00', 'SUBMITTED'),
(3, 1, 72.0, 1, '2026-03-16 14:00:00', '2026-03-16 14:45:00', '2026-03-16 14:45:00', 'SUBMITTED'),
(4, 1, 55.0, 0, '2026-03-20 09:00:00', '2026-03-20 09:55:00', '2026-03-20 09:55:00', 'SUBMITTED'),
(2, 2, 92.0, 1, '2026-04-10 15:00:00', '2026-04-10 15:40:00', '2026-04-10 15:40:00', 'SUBMITTED'),
(5, 2, 88.0, 1, '2026-04-12 10:00:00', '2026-04-12 10:45:00', '2026-04-12 10:45:00', 'SUBMITTED'),
(6, 3, 78.0, 1, '2026-04-20 13:00:00', '2026-04-20 13:50:00', '2026-04-20 13:50:00', 'SUBMITTED'),
(3, 3, 45.0, 0, '2026-04-22 09:30:00', '2026-04-22 10:20:00', '2026-04-22 10:20:00', 'SUBMITTED'),
(6, 4, 95.0, 1, '2026-05-05 14:00:00', '2026-05-05 15:30:00', '2026-05-05 15:30:00', 'SUBMITTED'),
(7, 4, 68.0, 1, '2026-05-08 10:00:00', '2026-05-08 11:40:00', '2026-05-08 11:40:00', 'SUBMITTED'),
(2, 5, 82.0, 1, '2026-05-10 16:00:00', '2026-05-10 17:20:00', '2026-05-10 17:20:00', 'SUBMITTED');

-- 10. 考试答题（模拟部分答题）
INSERT INTO `exam_answer` (`exam_record_id`, `question_id`, `user_answer`, `is_correct`, `score`) VALUES
-- 张三的Spring Boot考试（85分）
(1, 1, 'D', 1, 2),
(1, 2, 'A', 1, 2),
(1, 3, 'B', 1, 2),
(1, 4, 'C', 1, 2),
(1, 5, 'ABC', 0, 0),
(1, 6, 'T', 1, 1),
-- 李四的Spring Boot考试（72分）
(2, 1, 'B', 0, 0),
(2, 2, 'A', 1, 2),
(2, 3, 'B', 1, 2),
(2, 4, 'C', 1, 2),
(2, 5, 'ABCD', 1, 3),
(2, 6, 'T', 1, 1),
-- 张三的Vue3考试（92分）
(4, 7, 'B', 1, 2),
(4, 8, 'C', 1, 2),
(4, 9, 'ABCD', 1, 3),
(4, 10, 'D', 1, 2),
(4, 11, 'T', 1, 1),
(4, 12, 'Composition API、性能提升、TypeScript支持', 1, 5);

-- 11. 学习记录
INSERT INTO `learning_record` (`user_id`, `course_id`, `chapter_id`, `watch_duration`, `watch_percent`, `is_finished`, `last_position`) VALUES
(2, 1, 1, 1200, 100.00, 1, 1200),
(2, 1, 2, 1800, 100.00, 1, 1800),
(2, 1, 3, 1500, 62.50, 0, 1500),
(3, 1, 1, 1200, 100.00, 1, 1200),
(3, 1, 2, 900, 50.00, 0, 900),
(2, 3, 1, 1300, 100.00, 1, 1300),
(2, 3, 2, 2100, 100.00, 1, 2100),
(2, 3, 3, 1900, 100.00, 1, 1900),
(5, 3, 1, 1300, 100.00, 1, 1300),
(5, 3, 2, 1800, 85.71, 0, 1800),
(6, 5, 1, 1100, 100.00, 1, 1100),
(6, 5, 2, 2000, 100.00, 1, 2000),
(6, 5, 3, 2400, 100.00, 1, 2400),
(6, 5, 4, 1200, 66.67, 0, 1200),
(7, 7, 1, 1600, 100.00, 1, 1600),
(7, 7, 2, 2000, 100.00, 1, 2000),
(8, 9, 1, 1400, 100.00, 1, 1400),
(8, 9, 2, 1500, 68.18, 0, 1500);

-- 12. 错题记录
INSERT INTO `wrong_question` (`user_id`, `question_id`, `wrong_count`, `last_wrong_time`) VALUES
(2, 5, 1, '2026-03-15 10:50:00'),
(3, 1, 1, '2026-03-16 14:45:00'),
(4, 1, 1, '2026-03-20 09:55:00'),
(4, 3, 1, '2026-03-20 09:55:00'),
(7, 14, 1, '2026-04-22 10:20:00'),
(7, 16, 1, '2026-04-22 10:20:00');

-- 13. 订单
INSERT INTO `orders` (`order_no`, `user_id`, `course_id`, `amount`, `pay_method`, `status`, `create_time`, `pay_time`) VALUES
('OT20260315001', 2, 2, 199.00, 'WECHAT', 'PAID', '2026-03-15 11:00:00', '2026-03-15 11:05:00'),
('OT20260410001', 2, 4, 149.00, 'ALIPAY', 'PAID', '2026-04-10 16:00:00', '2026-04-10 16:03:00'),
('OT20260420001', 6, 6, 129.00, 'WECHAT', 'PAID', '2026-04-20 11:00:00', '2026-04-20 11:02:00'),
('OT20260505001', 7, 7, 299.00, 'ALIPAY', 'PAID', '2026-05-05 16:00:00', '2026-05-05 16:05:00'),
('OT20260510001', 5, 8, 399.00, 'WECHAT', 'PAID', '2026-05-10 09:00:00', '2026-05-10 09:03:00'),
('OT20260515001', 3, 10, 99.00, 'ALIPAY', 'PENDING', '2026-05-20 14:00:00', NULL),
('OT20260520001', 8, 12, 89.00, 'WECHAT', 'CANCELLED', '2026-05-22 10:00:00', NULL);

-- 14. 支付日志
INSERT INTO `payment_log` (`order_id`, `order_no`, `transaction_id`, `pay_method`, `amount`, `status`, `callback_data`) VALUES
(1, 'OT20260315001', 'WX20260315110500001', 'WECHAT', 199.00, 'SUCCESS', '{"return_code":"SUCCESS","result_code":"SUCCESS"}'),
(2, 'OT20260410001', 'ALI20260410160300001', 'ALIPAY', 149.00, 'SUCCESS', '{"code":"10000","msg":"Success"}'),
(3, 'OT20260420001', 'WX20260420110200001', 'WECHAT', 129.00, 'SUCCESS', '{"return_code":"SUCCESS","result_code":"SUCCESS"}'),
(4, 'OT20260505001', 'ALI20260505160500001', 'ALIPAY', 299.00, 'SUCCESS', '{"code":"10000","msg":"Success"}'),
(5, 'OT20260510001', 'WX20260510090300001', 'WECHAT', 399.00, 'SUCCESS', '{"return_code":"SUCCESS","result_code":"SUCCESS"}');
