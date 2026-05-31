-- Banner轮播图表
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) COMMENT '标题',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `link_url` VARCHAR(500) COMMENT '跳转链接',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner轮播图表';

-- 新闻资讯表
DROP TABLE IF EXISTS `news_article`;
CREATE TABLE `news_article` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) COMMENT '摘要',
    `cover` VARCHAR(500) COMMENT '封面图',
    `content` LONGTEXT COMMENT '图文内容(HTML)',
    `source` VARCHAR(100) COMMENT '来源',
    `view_count` INT DEFAULT 0 COMMENT '阅读量',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1发布 0草稿',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻资讯表';

-- 插入默认Banner数据
INSERT INTO `banner` (`title`, `image_url`, `link_url`, `sort_order`, `status`) VALUES
('Spring Boot 从入门到精通', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&h=300&fit=crop', '/course/1', 1, 1),
('Vue3 + TypeScript 实战', 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800&h=300&fit=crop', '/course/3', 2, 1),
('Python 数据分析与可视化', 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800&h=300&fit=crop', '/course/5', 3, 1),
('ChatGPT 应用开发实战', 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800&h=300&fit=crop', '/course/7', 4, 1);

-- 插入默认新闻资讯数据
INSERT INTO `news_article` (`title`, `summary`, `cover`, `content`, `source`, `view_count`, `sort_order`, `status`) VALUES
('Spring Boot 3.0 正式发布，带来多项重大更新', 'Spring Boot 3.0 正式版已发布，基于 Spring Framework 6.0，要求 Java 17 及以上版本，带来了原生镜像支持、可观测性增强等新特性。', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&h=200&fit=crop', '<p>Spring Boot 3.0 正式版终于发布了！这是一个重大的版本更新，带来了许多令人兴奋的新特性：</p><h3>1. 基线升级</h3><p>基于 Spring Framework 6.0，要求 Java 17 及以上版本。这意味着开发者需要升级 JDK 版本。</p><h3>2. 原生镜像支持</h3><p>通过 GraalVM 原生镜像技术，Spring Boot 应用可以编译为原生可执行文件，启动速度大幅提升，内存占用显著减少。</p><h3>3. 可观测性增强</h3><p>集成了 Micrometer 和 Micrometer Tracing，提供更好的 metrics、tracing 和 logging 支持。</p><h3>4. Jakarta EE 迁移</h3><p>从 javax.* 迁移到 jakarta.* 命名空间，这是一次重要的生态迁移。</p>', 'Spring官方博客', 1280, 1, 1),
('Vue 3.4 发布：响应式系统性能大幅提升', 'Vue 3.4 版本正式发布，代号"🏀 Slam Dunk"，响应式系统重构带来显著性能提升，同时增强了 TypeScript 支持。', 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=200&fit=crop', '<p>Vue 3.4 正式发布了！这个版本的代号是"🏀 Slam Dunk"，带来了许多令人兴奋的改进：</p><h3>1. 响应式系统重构</h3><p>响应式系统进行了重大重构，性能提升了约 2 倍，特别是在大量响应式数据的场景下表现更加出色。</p><h3>2. defineModel 稳定化</h3><p>defineModel 宏从实验性功能转为稳定功能，简化了 v-model 双向绑定的实现。</p><h3>3. v-bind 同名简写</h3><p>支持 v-bind 的同名简写语法，让模板代码更加简洁。</p><h3>4. 改进的 TypeScript 支持</h3><p>更好的泛型组件支持和类型推断。</p>', 'Vue官方博客', 960, 2, 1),
('AI 大模型技术2026年发展趋势', '2026年AI大模型技术持续演进，多模态融合、Agent智能体、端侧部署成为年度热点方向，各大厂商纷纷发布新一代模型。', 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=400&h=200&fit=crop', '<p>2026年，AI大模型技术继续快速发展，以下是几个重要趋势：</p><h3>1. 多模态深度融合</h3><p>文本、图像、音频、视频的统一理解和生成能力越来越强，GPT-4o、Gemini 等模型引领多模态浪潮。</p><h3>2. Agent 智能体爆发</h3><p>基于大模型的自主智能体（Agent）技术成为热点，能够自主规划、执行复杂任务，代表框架有 LangChain、AutoGPT 等。</p><h3>3. 端侧部署加速</h3><p>通过量化、蒸馏等技术，大模型可以在手机、PC等终端设备上运行，如 Apple Intelligence、高通 AI Engine 等。</p><h3>4. 开源生态繁荣</h3><p>Llama 3、Mistral、Qwen 等开源模型性能不断提升，缩小了与闭源模型的差距。</p>', 'AI科技评论', 2340, 3, 1),
('Docker + Kubernetes 云原生技术最新实践', '云原生技术在企业中的应用越来越广泛，本文总结了 Docker 和 Kubernetes 的最新实践经验和最佳架构模式。', 'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=400&h=200&fit=crop', '<p>云原生技术已经成为现代软件开发的基础设施，以下是当前的最佳实践：</p><h3>1. 容器化最佳实践</h3><p>使用多阶段构建减小镜像体积、合理使用 Docker Compose 进行本地开发、镜像安全扫描等。</p><h3>2. Kubernetes 集群管理</h3><p>使用 Helm Charts 管理应用部署、Ingress Controller 进行流量管理、HPA 实现自动扩缩容。</p><h3>3. 服务网格</h3><p>Istio、Linkerd 等服务网格技术为微服务通信提供了可观测性、流量管理和安全能力。</p><h3>4. GitOps 工作流</h3><p>使用 ArgoCD 或 Flux 实现 GitOps，将 Git 仓库作为单一事实来源进行持续部署。</p>', '云原生技术社区', 890, 4, 1);
