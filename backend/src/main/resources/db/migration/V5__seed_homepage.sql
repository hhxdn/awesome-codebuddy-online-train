-- V5: 首页种子数据
-- 修复Java分类状态
UPDATE course_category SET status=1 WHERE id=1;

-- 设置推荐课程
UPDATE course SET is_recommend=1 WHERE id IN (1,3,5,7,9,11);

-- 更新学习人数
UPDATE course SET student_count=FLOOR(100+RAND()*5000) WHERE deleted=0;

-- 清空旧Banner
DELETE FROM banner;

-- 插入Banner
INSERT INTO banner (title, image_url, link_url, sort_order, status, create_time, update_time) VALUES
('2026年在线培训春季班火热招生中', 'https://picsum.photos/seed/banner1/750/400', '/courses', 1, 1, NOW(), NOW()),
('AI大模型应用开发实战训练营', 'https://picsum.photos/seed/banner2/750/400', '/courses', 2, 1, NOW(), NOW()),
('Vue3+SpringBoot全栈项目实战', 'https://picsum.photos/seed/banner3/750/400', '/courses', 3, 1, NOW(), NOW()),
('云计算与DevOps工程师认证课程', 'https://picsum.photos/seed/banner4/750/400', '/courses', 4, 1, NOW(), NOW()),
('Python数据分析从入门到精通', 'https://picsum.photos/seed/banner5/750/400', '/courses', 5, 1, NOW(), NOW());

-- 清空旧新闻
DELETE FROM news_article;

-- 资讯模块新闻 (module news type='NEWS')
INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '2026年数字化转型培训全面启动', '为响应国家数字化转型战略，2026年度企业数字化人才培训计划正式启动，覆盖人工智能、大数据、云计算等多个热门领域。', '<h2>培训背景</h2><p>随着数字经济的快速发展，企业对数字化人才的需求日益增长。</p>', 'https://picsum.photos/seed/news1/600/400', id, '培训中心', 1523, 1, NOW(), NOW()
FROM news_module WHERE type='NEWS';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '最新AI技术在企业培训中的应用', '探讨人工智能技术如何变革传统企业培训模式。', '<p>AI技术正在深刻改变企业培训的方式。</p>', 'https://picsum.photos/seed/news2/600/400', id, '科技日报', 892, 1, NOW(), NOW()
FROM news_module WHERE type='NEWS';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '在线学习平台助力职业技能提升', '我省在线学习平台上线以来，累计服务学员超过10万人次。', '<p>在线学习平台打破了时间和空间的限制。</p>', 'https://picsum.photos/seed/news3/600/400', id, '培训中心', 567, 1, NOW(), NOW()
FROM news_module WHERE type='NEWS';

-- 主办单位
INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '主办单位简介：XX省工业和信息化厅', 'XX省工业和信息化厅是本培训项目的主办单位。', '<h2>单位概况</h2><p>负责全省工业和信息化工作。</p>', 'https://picsum.photos/seed/org1/600/400', id, '主办方', 645, 1, NOW(), NOW()
FROM news_module WHERE type='ORGANIZER';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '承办单位：XX省信息技术职业学院', 'XX省信息技术职业学院作为承办单位。', '<p>学院拥有先进的教学设施。</p>', 'https://picsum.photos/seed/org2/600/400', id, '主办方', 423, 1, NOW(), NOW()
FROM news_module WHERE type='ORGANIZER';

-- 培训风采
INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '2026年第一期大数据培训班精彩回顾', '来自全省各地的60名学员参加了培训。', '<h2>培训概况</h2><p>培训采用理论授课+实操演练的方式。</p>', 'https://picsum.photos/seed/style1/600/400', id, '培训中心', 2156, 1, NOW(), NOW()
FROM news_module WHERE type='STYLE';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '云计算技术培训班学员圆满完成实训项目', '学员们成功完成了基于Kubernetes的微服务部署实训项目。', '<p>从Docker容器化到Kubernetes集群部署全流程实践。</p>', 'https://picsum.photos/seed/style2/600/400', id, '培训中心', 1234, 1, NOW(), NOW()
FROM news_module WHERE type='STYLE';

-- 课程介绍
INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '《Spring Boot微服务实战》课程全面升级', '课程新增Spring Cloud Alibaba、服务网格等前沿内容。', '<h2>课程升级内容</h2><ul><li>Spring Boot 3.x 新特性</li><li>Spring Cloud Alibaba</li></ul>', 'https://picsum.photos/seed/course1/600/400', id, '教研组', 1876, 1, NOW(), NOW()
FROM news_module WHERE type='COURSE_INTRO';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '《AI大模型应用开发》新课上线', '涵盖ChatGPT API调用、LangChain框架等前沿技术。', '<p>从零基础开始，逐步深入掌握AI大模型应用开发。</p>', 'https://picsum.photos/seed/course2/600/400', id, '教研组', 2345, 1, NOW(), NOW()
FROM news_module WHERE type='COURSE_INTRO';

-- 培训通知
INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '关于开展2026年第二期人工智能培训的通知', '定于2026年6月20日-24日举办第二期人工智能技术专题培训班。', '<h2>培训通知</h2><p>培训时间：2026年6月20日-24日</p><p>培训地点：XX省信息技术职业学院</p><p>报名截止：2026年6月15日</p>', 'https://picsum.photos/seed/notice1/600/400', id, '培训中心', 3456, 1, NOW(), NOW()
FROM news_module WHERE type='NOTICE';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '关于举办网络安全技术培训班的预通知', '为提供各单位网络安全防护能力，拟于2026年7月举办培训班。', '<p>培训内容涵盖网络安全基础、渗透测试、安全运维等。</p>', 'https://picsum.photos/seed/notice2/600/400', id, '培训中心', 1567, 1, NOW(), NOW()
FROM news_module WHERE type='NOTICE';

-- 师资介绍
INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '张教授 - AI与大模型课程主讲教师', '计算机科学博士，博士生导师，发表SCI论文50余篇。', '<h2>导师简介</h2><p>研究方向：人工智能、自然语言处理、大模型。</p>', 'https://picsum.photos/seed/teacher1/600/400', id, '教研组', 987, 1, NOW(), NOW()
FROM news_module WHERE type='TEACHER';

INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time)
SELECT '李老师 - 云计算与DevOps课程主讲教师', '资深云计算架构师，AWS/阿里云双认证专家，15年IT行业经验。', '<h2>导师简介</h2><p>精通Docker、Kubernetes、Jenkins等技术。</p>', 'https://picsum.photos/seed/teacher2/600/400', id, '教研组', 756, 1, NOW(), NOW()
FROM news_module WHERE type='TEACHER';

-- 更新课程封面图
UPDATE course SET cover='https://picsum.photos/seed/springboot/400/300' WHERE id=1;
UPDATE course SET cover='https://picsum.photos/seed/springcloud/400/300' WHERE id=2;
UPDATE course SET cover='https://picsum.photos/seed/vue3/400/300' WHERE id=3;
UPDATE course SET cover='https://picsum.photos/seed/react18/400/300' WHERE id=4;
UPDATE course SET cover='https://picsum.photos/seed/python/400/300' WHERE id=5;
UPDATE course SET cover='https://picsum.photos/seed/django/400/300' WHERE id=6;
UPDATE course SET cover='https://picsum.photos/seed/chatgpt/400/300' WHERE id=7;
UPDATE course SET cover='https://picsum.photos/seed/langchain/400/300' WHERE id=8;
UPDATE course SET cover='https://picsum.photos/seed/mysql/400/300' WHERE id=9;
UPDATE course SET cover='https://picsum.photos/seed/redis/400/300' WHERE id=10;
UPDATE course SET cover='https://picsum.photos/seed/docker/400/300' WHERE id=11;
UPDATE course SET cover='https://picsum.photos/seed/jenkins/400/300' WHERE id=12;
UPDATE course SET cover='https://picsum.photos/seed/checkin/400/300' WHERE id=13;
UPDATE course SET cover='https://picsum.photos/seed/vodtest/400/300' WHERE id=14;
UPDATE course SET cover='https://picsum.photos/seed/trainer/400/300' WHERE id=15;
