package com.onlinetrain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 首页种子数据初始化器 - 在应用启动后执行
 */
@Slf4j
@Component
@Order(100)
public class HomepageDataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            seedBanners();
            seedNewsArticles();
            log.info("首页种子数据初始化完成");
        } catch (Exception e) {
            log.error("首页种子数据初始化失败: {}", e.getMessage());
        }
    }

    private void seedBanners() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM banner WHERE deleted=0", Long.class);
        if (count != null && count > 0) {
            log.info("Banner数据已存在 ({} 条), 跳过初始化", count);
            return;
        }
        jdbcTemplate.update("DELETE FROM banner");
        String sql = "INSERT INTO banner (title, image_url, link_url, sort_order, status, create_time, update_time) VALUES " +
                "('2026年在线培训春季班火热招生中', 'https://picsum.photos/seed/banner1/750/400', '/courses', 1, 1, NOW(), NOW())," +
                "('AI大模型应用开发实战训练营', 'https://picsum.photos/seed/banner2/750/400', '/courses', 2, 1, NOW(), NOW())," +
                "('Vue3+SpringBoot全栈项目实战', 'https://picsum.photos/seed/banner3/750/400', '/courses', 3, 1, NOW(), NOW())," +
                "('云计算与DevOps工程师认证课程', 'https://picsum.photos/seed/banner4/750/400', '/courses', 4, 1, NOW(), NOW())," +
                "('Python数据分析从入门到精通', 'https://picsum.photos/seed/banner5/750/400', '/courses', 5, 1, NOW(), NOW())";
        jdbcTemplate.update(sql);
        log.info("Banner种子数据已插入 5 条");
    }

    private void seedNewsArticles() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_article WHERE deleted=0", Long.class);
        if (count != null && count > 0) {
            log.info("新闻数据已存在 ({} 条), 跳过初始化", count);
            return;
        }
        jdbcTemplate.update("DELETE FROM news_article");

        // 获取新闻模块ID
        Long newsModuleId = getModuleId("NEWS");
        Long organizerModuleId = getModuleId("ORGANIZER");
        Long styleModuleId = getModuleId("STYLE");
        Long courseIntroModuleId = getModuleId("COURSE_INTRO");
        Long noticeModuleId = getModuleId("NOTICE");
        Long teacherModuleId = getModuleId("TEACHER");

        String sql = "INSERT INTO news_article (title, summary, content, cover, module_id, source, view_count, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW(), NOW())";
        Object[][] articles = {
            {"2026年数字化转型培训全面启动", "为响应国家数字化转型战略，2026年度企业数字化人才培训计划正式启动，覆盖人工智能、大数据、云计算等多个热门领域。", "<h2>培训背景</h2><p>随着数字经济的快速发展，企业对数字化人才的需求日益增长。2026年度培训计划将围绕人工智能、大数据、云计算、物联网等核心技术领域，为企业培养高素质数字化人才。</p><h2>培训内容</h2><p>本次培训涵盖AI大模型应用开发、数据分析与可视化、云原生架构设计、DevOps工程实践等热门方向。</p>", "https://picsum.photos/seed/news1/600/400", newsModuleId, "培训中心", 1523},
            {"最新AI技术在企业培训中的应用", "探讨人工智能技术如何变革传统企业培训模式，提升培训效率和学习体验。", "<p>AI技术正在深刻改变企业培训的方式。从智能推荐学习路径到自动化评估，AI让培训更加个性化和高效。</p>", "https://picsum.photos/seed/news2/600/400", newsModuleId, "科技日报", 892},
            {"在线学习平台助力职业技能提升", "我省在线学习平台上线以来，累计服务学员超过10万人次，培训效果获得广泛好评。", "<p>在线学习平台打破了时间和空间的限制，让更多在职人员能够利用碎片化时间进行学习提升。</p>", "https://picsum.photos/seed/news3/600/400", newsModuleId, "培训中心", 567},
            {"主办单位简介：XX省工业和信息化厅", "XX省工业和信息化厅是本培训项目的主办单位，致力于推动全省工业和信息化领域人才队伍建设。", "<h2>单位概况</h2><p>XX省工业和信息化厅是省政府组成部门，负责全省工业和信息化工作的统筹协调。近年来，积极推动产业数字化转型和人才培养工作。</p>", "https://picsum.photos/seed/org1/600/400", organizerModuleId, "主办方", 645},
            {"承办单位：XX省信息技术职业学院", "XX省信息技术职业学院作为承办单位，将为培训提供优质的教学资源和实训环境。", "<p>学院拥有先进的多媒体教室、云计算实训中心、人工智能实验室等教学设施，为培训提供有力保障。</p>", "https://picsum.photos/seed/org2/600/400", organizerModuleId, "主办方", 423},
            {"2026年第一期大数据培训班精彩回顾", "来自全省各地市的60名学员齐聚一堂，开展了为期5天的大数据技术专题培训，学习氛围浓厚。", "<h2>培训概况</h2><p>2026年3月10日至14日，第一期大数据技术专题培训班成功举办。来自全省各地市的60名学员参加了培训。</p><h2>精彩瞬间</h2><p>培训采用理论授课+实操演练的方式，学员们积极参与课堂讨论和项目实践，学习热情高涨。</p>", "https://picsum.photos/seed/style1/600/400", styleModuleId, "培训中心", 2156},
            {"云计算技术培训班学员圆满完成实训项目", "经过两周的紧张学习，云计算培训班的学员们成功完成了基于Kubernetes的微服务部署实训项目。", "<p>学员们分组完成了从Docker容器化到Kubernetes集群部署的全流程实践，成果显著。</p>", "https://picsum.photos/seed/style2/600/400", styleModuleId, "培训中心", 1234},
            {"《Spring Boot微服务实战》课程全面升级", "课程新增Spring Cloud Alibaba、服务网格、可观测性等前沿内容，帮助学员掌握最新微服务技术栈。", "<h2>课程升级内容</h2><ul><li>Spring Boot 3.x 新特性详解</li><li>Spring Cloud Alibaba 微服务全家桶</li><li>Istio服务网格实战</li><li>Prometheus+Grafana可观测性</li></ul>", "https://picsum.photos/seed/course1/600/400", courseIntroModuleId, "教研组", 1876},
            {"《AI大模型应用开发》新课上线", "涵盖ChatGPT API调用、LangChain框架、RAG检索增强生成、Agent智能体等前沿技术，手把手教你开发AI应用。", "<p>本课程从零基础开始，逐步深入，帮助学员掌握AI大模型应用开发的核心技能。</p>", "https://picsum.photos/seed/course2/600/400", courseIntroModuleId, "教研组", 2345},
            {"关于开展2026年第二期人工智能培训的通知", "定于2026年6月20日-24日在XX省信息技术职业学院举办第二期人工智能技术专题培训班，请各单位积极组织报名。", "<h2>培训通知</h2><p><strong>培训时间：</strong>2026年6月20日-24日（共5天）</p><p><strong>培训地点：</strong>XX省信息技术职业学院</p><p><strong>培训对象：</strong>各单位信息化部门技术人员</p><p><strong>报名截止：</strong>2026年6月15日</p>", "https://picsum.photos/seed/notice1/600/400", noticeModuleId, "培训中心", 3456},
            {"关于举办网络安全技术培训班的预通知", "为提高各单位网络安全防护能力，拟于2026年7月举办网络安全技术专题培训班。", "<p>培训内容涵盖网络安全基础、渗透测试、安全运维、应急响应等，具体时间另行通知。</p>", "https://picsum.photos/seed/notice2/600/400", noticeModuleId, "培训中心", 1567},
            {"张教授 - AI与大模型课程主讲教师", "计算机科学博士，博士生导师，长期从事人工智能、自然语言处理研究，发表SCI论文50余篇。", "<h2>导师简介</h2><p>张教授，XX大学计算机学院教授，博士生导师。研究方向包括人工智能、自然语言处理、大模型训练与优化。</p><h2>学术成果</h2><p>主持国家自然科学基金项目3项，发表SCI/EI论文50余篇，获省部级科技进步奖2项。</p>", "https://picsum.photos/seed/teacher1/600/400", teacherModuleId, "教研组", 987},
            {"李老师 - 云计算与DevOps课程主讲教师", "资深云计算架构师，AWS/阿里云双认证专家，15年IT行业经验，曾主导多个大型企业云原生转型项目。", "<h2>导师简介</h2><p>李老师拥有15年IT行业经验，曾在多家知名互联网公司担任架构师。精通Docker、Kubernetes、Jenkins等技术。</p><h2>项目经验</h2><p>主导过日活千万级应用的容器化改造，熟悉大规模分布式系统的设计与运维。</p>", "https://picsum.photos/seed/teacher2/600/400", teacherModuleId, "教研组", 756},
        };

        for (Object[] row : articles) {
            jdbcTemplate.update(sql, row);
        }
        log.info("新闻种子数据已插入 {} 条", articles.length);

        // 更新课程封面和学习人数
        jdbcTemplate.update("UPDATE course_category SET status=1 WHERE id=1 AND status=0");
        jdbcTemplate.update("UPDATE course SET is_recommend=1 WHERE id IN (1,3,5,7,9,11)");
        jdbcTemplate.update("UPDATE course SET student_count=FLOOR(100+RAND()*5000) WHERE deleted=0");
        String[] covers = {
            "UPDATE course SET cover='https://picsum.photos/seed/springboot/400/300' WHERE id=1",
            "UPDATE course SET cover='https://picsum.photos/seed/springcloud/400/300' WHERE id=2",
            "UPDATE course SET cover='https://picsum.photos/seed/vue3/400/300' WHERE id=3",
            "UPDATE course SET cover='https://picsum.photos/seed/react18/400/300' WHERE id=4",
            "UPDATE course SET cover='https://picsum.photos/seed/python/400/300' WHERE id=5",
            "UPDATE course SET cover='https://picsum.photos/seed/django/400/300' WHERE id=6",
            "UPDATE course SET cover='https://picsum.photos/seed/chatgpt/400/300' WHERE id=7",
            "UPDATE course SET cover='https://picsum.photos/seed/langchain/400/300' WHERE id=8",
            "UPDATE course SET cover='https://picsum.photos/seed/mysql/400/300' WHERE id=9",
            "UPDATE course SET cover='https://picsum.photos/seed/redis/400/300' WHERE id=10",
            "UPDATE course SET cover='https://picsum.photos/seed/docker/400/300' WHERE id=11",
            "UPDATE course SET cover='https://picsum.photos/seed/jenkins/400/300' WHERE id=12",
            "UPDATE course SET cover='https://picsum.photos/seed/checkin/400/300' WHERE id=13",
            "UPDATE course SET cover='https://picsum.photos/seed/vodtest/400/300' WHERE id=14",
            "UPDATE course SET cover='https://picsum.photos/seed/trainer/400/300' WHERE id=15",
        };
        for (String c : covers) {
            jdbcTemplate.update(c);
        }
        log.info("课程封面和学习人数已更新");
    }

    private Long getModuleId(String type) {
        return jdbcTemplate.queryForObject(
            "SELECT id FROM news_module WHERE type=? AND deleted=0 LIMIT 1",
            Long.class, type
        );
    }
}
