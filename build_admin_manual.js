const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, ImageRun,
  Header, Footer, AlignmentType, HeadingLevel,
  BorderStyle, WidthType, ShadingType,
  Table, TableRow, TableCell, PageNumber,
  TableOfContents, PageBreak, LevelFormat
} = require('docx');

const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');
const OUTPUT = path.join(__dirname, 'docs', '管理平台操作手册.docx');

function img(name) {
  const p = path.join(SCREENSHOT_DIR, name);
  if (fs.existsSync(p)) return fs.readFileSync(p);
  return null;
}

function addImage(name, w, h) {
  const data = img(name);
  if (!data) return new Paragraph({ children: [new TextRun('[图片: ' + name + ']')] });
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120, after: 200 },
    children: [new ImageRun({ type: 'png', data, transformation: { width: w, height: h }, altText: { title: name, description: name, name } })]
  });
}

function caption(text) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 60, after: 300 },
    children: [new TextRun({ text: '▲ ' + text, italics: true, size: 20, color: '666666' })]
  });
}

function tip(text) {
  return new Paragraph({
    spacing: { before: 100, after: 100 },
    border: { left: { style: BorderStyle.SINGLE, size: 12, color: '2E75B6', space: 8 } },
    children: [
      new TextRun({ text: '小贴士：', bold: true, color: '2E75B6', size: 22 }),
      new TextRun({ text, size: 22 })
    ]
  });
}

function step(num, text) {
  return new Paragraph({
    spacing: { before: 160, after: 80 },
    children: [
      new TextRun({ text: '第' + num + '步：', bold: true, size: 24, color: '2E75B6' }),
      new TextRun({ text, size: 24 })
    ]
  });
}

function para(text) {
  return new Paragraph({
    spacing: { before: 80, after: 80 },
    children: [new TextRun({ text, size: 24 })]
  });
}

function heading(text, level) {
  return new Paragraph({
    heading: level === 1 ? HeadingLevel.HEADING_1 : HeadingLevel.HEADING_2,
    spacing: { before: level === 1 ? 400 : 300, after: 200 },
    children: [new TextRun({ text, bold: true, size: level === 1 ? 36 : 30, font: 'Arial' })]
  });
}

function makeTable(headers, widths, rows) {
  const headerRow = new TableRow({
    children: headers.map((h, i) => {
      return new TableCell({
        borders: { top: border, bottom: border, left: border, right: border },
        width: { size: widths[i], type: WidthType.DXA },
        shading: { fill: '2E75B6', type: ShadingType.CLEAR },
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: h, bold: true, color: 'FFFFFF', size: 22 })] })]
      });
    })
  });
  const dataRows = rows.map(row => new TableRow({
    children: row.map((cell, i) => new TableCell({
      borders: { top: border, bottom: border, left: border, right: border },
      width: { size: widths[i], type: WidthType.DXA },
      margins: { top: 60, bottom: 60, left: 100, right: 100 },
      children: [new Paragraph({ children: [new TextRun({ text: cell, size: 22 })] })]
    }))
  }));
  return new Table({
    width: { size: widths.reduce((a, b) => a + b, 0), type: WidthType.DXA },
    columnWidths: widths,
    rows: [headerRow, ...dataRows]
  });
}

const border = { style: BorderStyle.SINGLE, size: 1, color: 'CCCCCC' };

async function main() {
  const children = [];

  // ============ 封面 ============
  children.push(new Paragraph({ spacing: { before: 3000 } }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 }, children: [new TextRun({ text: '在线学习平台', size: 56, bold: true, color: '2E75B6', font: 'Arial' })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: '—— 管理平台操作手册 ——', size: 36, color: '555555', font: 'Arial' })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 600 }, children: [new TextRun({ text: '管理员专用 · 包教包会版', size: 28, color: '888888', italics: true })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: '适用版本：v2.0', size: 24, color: '999999' })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: '文档更新日期：2026年6月', size: 24, color: '999999' })] }));
  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 目录 ============
  children.push(heading('目  录', 1));
  children.push(new TableOfContents('目录', { hyperlink: true, headingStyleRange: '1-2' }));
  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第一章：登录 ============
  children.push(heading('第一章  登录管理后台', 1));
  children.push(para('管理后台是给平台管理员使用的，用来管理课程、学员、考试等所有内容。需要用电脑浏览器打开。'));

  children.push(heading('1.1  打开管理后台', 2));
  children.push(step('1', '打开电脑上的浏览器（推荐使用 Chrome 浏览器）'));
  children.push(step('2', '在地址栏输入管理后台的网址'));
  children.push(step('3', '你会看到登录页面'));

  children.push(addImage('admin-01-login.png', 480, 300));
  caption('管理后台登录页面');

  children.push(heading('1.2  登录系统', 2));
  children.push(addImage('admin-02-login-filled.png', 480, 300));
  caption('填写用户名和密码');
  children.push(step('1', '在用户名输入框里输入你的管理员账号'));
  children.push(step('2', '在密码输入框里输入密码'));
  children.push(step('3', '点蓝色的「登 录」按钮'));
  children.push(step('4', '登录成功后就进入管理后台的主页面了'));
  children.push(tip('管理员账号和密码由超级管理员分配，如果忘记了请联系超级管理员重置。'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第二章：仪表盘 ============
  children.push(heading('第二章  仪表盘 —— 登录后第一眼看到的地方', 1));
  children.push(addImage('admin-03-dashboard.png', 480, 300));
  caption('仪表盘首页');
  children.push(para('仪表盘是管理后台的首页，用来看平台的整体运营情况。'));
  children.push(para('从上到下可以看到：'));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '四个统计卡片：总学员数、总课程数、今日营收、本月新增学员', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '营收趋势图：折线图展示最近30天的收入变化', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '新增学员图：柱状图展示最近7天的新增学员数量', size: 24 })] }));
  children.push(tip('每天打开后台第一件事就是看仪表盘，了解平台运营情况！'));

  children.push(heading('2.1  侧边栏菜单', 2));
  children.push(para('页面左边是菜单栏，列出所有管理功能。点击菜单可以进入对应的管理页面。'));
  children.push(para('主要菜单分组如下：'));
  children.push(makeTable(
    ['菜单分组', '包含的功能'],
    [3000, 6400],
    [
      ['仪表盘', '查看平台运营数据'],
      ['课程管理', '课程分类、课程列表、课程编辑'],
      ['内容管理', 'Banner轮播图、新闻资讯、系统配置'],
      ['题库管理', '题目列表、批量导入题目'],
      ['试卷管理', '试卷列表、组卷、考试记录、随机组卷'],
      ['学员管理', '学员列表、学员详情'],
      ['订单管理', '查看所有订单'],
      ['统计分析', '营收统计、学情统计、考试统计'],
      ['预约管理', '考试预约、课程预约'],
      ['打卡管理', '线下打卡记录'],
      ['证书管理', '学员结业证书'],
      ['答疑管理', '学员提交的问题'],
      ['系统管理', '管理员账号、角色权限、菜单配置'],
    ]
  ));
  children.push(tip('点击顶部Logo旁边的折叠按钮，可以把菜单收起来，给内容区让出更多空间。'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第三章：课程管理 ============
  children.push(heading('第三章  课程管理 —— 创建和管理课程', 1));
  children.push(para('课程管理是整个平台最核心的功能，包含三个子功能：课程分类、课程列表、课程编辑。'));

  children.push(heading('3.1  课程分类管理', 2));
  children.push(addImage('admin-04-category-list.png', 480, 300));
  caption('课程分类管理页面');
  children.push(para('课程分类是用来给课程分组的，比如分成「Java开发」「前端开发」「Python开发」等。'));
  children.push(step('1', '点击「新增分类」按钮'));
  children.push(step('2', '填写分类名称、选择上级分类（可以多级嵌套）'));
  children.push(step('3', '填好排序号（数字越小越靠前）'));
  children.push(step('4', '点击「确定」保存'));
  children.push(tip('分类支持多级结构，比如「IT技术 > Java开发 > Spring Boot」，最多支持三级。'));

  children.push(heading('3.2  课程列表管理', 2));
  children.push(addImage('admin-05-course-list.png', 480, 300));
  caption('课程管理列表页面');
  children.push(para('这里展示所有课程，可以搜索、筛选、新增、编辑、删除课程。'));
  children.push(step('1', '点击「新增课程」添加新课程'));
  children.push(step('2', '搜索框可以按课程名称搜索'));
  children.push(step('3', '操作列可以编辑或删除课程'));

  children.push(heading('3.3  新增/编辑课程', 2));
  children.push(addImage('admin-06-course-edit.png', 480, 300));
  caption('课程编辑页面');
  children.push(para('新增或编辑课程时需要填写以下信息：'));
  children.push(makeTable(
    ['字段', '说明'],
    [2500, 6900],
    [
      ['课程标题', '课程的名称，学员能看到'],
      ['课程分类', '选择这个课程属于哪个分类'],
      ['课程类型', '线上课程 / 线下课程'],
      ['课程封面', '上传一张好看的封面图'],
      ['课程价格', '设置价格，0 表示免费'],
      ['课程介绍', '详细描述课程内容'],
      ['是否上架', '上架后学员才能看到'],
    ]
  ));
  children.push(tip('课程创建后，还需要添加章节（视频+练习题），学员才能真正开始学习！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第四章：内容管理 ============
  children.push(heading('第四章  内容管理 —— Banner、资讯、系统配置', 1));
  children.push(para('内容管理负责平台的前端展示内容，包括首页轮播图、新闻资讯、系统参数配置等。'));

  children.push(heading('4.1  Banner 管理', 2));
  children.push(addImage('admin-07-banner-list.png', 480, 300));
  caption('Banner轮播图管理');
  children.push(para('Banner 是首页顶部的轮播图，用来展示重要通知或推荐课程。'));
  children.push(step('1', '点击「新增Banner」'));
  children.push(step('2', '上传一张图片（建议尺寸 750x350）'));
  children.push(step('3', '填写跳转链接（点Banner后跳到哪里）'));
  children.push(step('4', '设置排序号'));
  children.push(step('5', '点击保存'));

  children.push(heading('4.2  新闻模块管理', 2));
  children.push(addImage('admin-08-news-modules.png', 480, 300));
  caption('新闻模块管理');
  children.push(para('新闻模块是资讯的分类，比如「平台公告」「行业动态」「学习资料」等。'));
  children.push(step('1', '点击「新增模块」'));
  children.push(step('2', '填写模块名称和排序'));
  children.push(step('3', '保存后就可以在新闻资讯中使用这个分类了'));

  children.push(heading('4.3  新闻资讯管理', 2));
  children.push(addImage('admin-09-news-list.png', 480, 300));
  caption('新闻资讯管理');
  children.push(para('发布和管理新闻资讯文章，学员在 H5 端的「最新资讯」可以看到。'));
  children.push(step('1', '点击「新增资讯」'));
  children.push(step('2', '填写标题、选择新闻模块、上传封面图'));
  children.push(step('3', '在富文本编辑器里写正文内容'));
  children.push(step('4', '保存发布'));

  children.push(heading('4.4  系统配置', 2));
  children.push(addImage('admin-10-system-config.png', 480, 300));
  caption('系统配置管理');
  children.push(para('管理系统级别的配置参数，比如报考条件说明、关于我们内容等。'));
  children.push(step('1', '选择要编辑的配置项'));
  children.push(step('2', '修改配置内容（支持富文本）'));
  children.push(step('3', '保存'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第五章：题库管理 ============
  children.push(heading('第五章  题库管理 —— 管理练习题和考试题', 1));
  
  children.push(heading('5.1  题目列表', 2));
  children.push(addImage('admin-11-question-list.png', 480, 300));
  caption('题库管理列表');
  children.push(para('这里管理所有题目，可以通过筛选条件快速找到需要的题目。'));
  children.push(para('题目类型包含：'));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '单选题：一个正确答案', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '多选题：多个正确答案', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '判断题：对或错', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '简答题：需要人工批改', size: 24 })] }));
  children.push(step('1', '点击「新增题目」手动添加'));
  children.push(step('2', '选择题目类型、填写题目内容'));
  children.push(step('3', '设置正确答案和解析'));
  children.push(step('4', '关联到具体章节'));
  children.push(step('5', '保存题目'));

  children.push(heading('5.2  批量导入题目', 2));
  children.push(addImage('admin-12-question-import.png', 480, 300));
  caption('批量导入题目');
  children.push(para('如果有很多题目要添加，可以用 Excel 批量导入：'));
  children.push(step('1', '先下载导入模板（Excel文件）'));
  children.push(step('2', '按模板格式填写题目'));
  children.push(step('3', '上传 Excel 文件'));
  children.push(step('4', '系统会自动导入所有题目'));
  children.push(tip('批量导入是添加大量题目最快捷的方式，建议先准备好题目数据再操作！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第六章：试卷管理 ============
  children.push(heading('第六章  试卷管理 —— 组卷和考试管理', 1));

  children.push(heading('6.1  试卷列表', 2));
  children.push(addImage('admin-13-exam-list.png', 480, 300));
  caption('试卷管理列表');
  children.push(para('这里管理所有考试试卷，可以新增、编辑、删除。'));
  children.push(step('1', '点击「新增试卷」创建新试卷'));

  children.push(heading('6.2  编辑试卷（组卷）', 2));
  children.push(addImage('admin-14-exam-edit.png', 480, 300));
  caption('试卷编辑页面');
  children.push(para('创建或编辑试卷时需要设置：'));
  children.push(makeTable(
    ['配置项', '说明'],
    [3000, 6400],
    [
      ['试卷名称', '考试的标题'],
      ['考试时长', '以分钟为单位，比如 60 表示考试60分钟'],
      ['总分', '所有题目的分数总和'],
      ['及格分数', '达到这个分数才算通过'],
      ['关联课程', '这个考试属于哪个课程'],
      ['题目列表', '从题库中选择题目加入试卷'],
    ]
  ));
  children.push(tip('组卷时要注意题目难度搭配，建议基础题和难题按 7:3 比例分配。'));

  children.push(heading('6.3  随机组卷', 2));
  children.push(addImage('admin-15-random-exam.png', 480, 300));
  caption('随机组卷功能');
  children.push(para('不想手工选题？用随机组卷功能，设置规则后系统自动帮你抽题！'));
  children.push(step('1', '选择题目来源（按课程、按分类）'));
  children.push(step('2', '设置每种题型的数量'));
  children.push(step('3', '设置每题分值'));
  children.push(step('4', '点击生成试卷'));

  children.push(heading('6.4  考试记录', 2));
  children.push(addImage('admin-16-exam-records.png', 480, 300));
  caption('考试记录查看');
  children.push(para('查看所有学员的考试记录：谁考了、考了多少分、过没过、用了多长时间。'));
  children.push(step('1', '可以通过学员姓名、试卷名称筛选'));
  children.push(step('2', '点击某条记录可以查看详细答题情况'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第七章：学员管理 ============
  children.push(heading('第七章  学员管理', 1));
  
  children.push(heading('7.1  学员列表', 2));
  children.push(addImage('admin-17-student-list.png', 480, 300));
  caption('学员管理列表');
  children.push(para('查看和管理所有注册学员。'));
  children.push(step('1', '搜索学员姓名或手机号'));
  children.push(step('2', '查看学员信息'));
  children.push(step('3', '审核新注册的学员（批准或驳回）'));

  children.push(heading('7.2  学员详情', 2));
  children.push(addImage('admin-18-student-detail.png', 480, 300));
  caption('学员详细信息');
  children.push(para('查看某个学员的详细信息，包括：'));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '基本信息：姓名、手机号、性别、学历等', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '学习记录：学了哪些课程、学习时长', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '考试记录：参加了哪些考试、成绩如何', size: 24 })] }));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第八章：订单管理 ============
  children.push(heading('第八章  订单管理', 1));
  children.push(addImage('admin-19-order-list.png', 480, 300));
  caption('订单管理列表');
  children.push(para('查看所有学员的购买记录和支付状态。'));
  children.push(step('1', '筛选：按订单状态（全部/待支付/已支付/已取消）筛选'));
  children.push(step('2', '搜索：按学员姓名或手机号搜索'));
  children.push(step('3', '查看：点击查看订单详情'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第九章：统计分析 ============
  children.push(heading('第九章  统计分析 —— 数据报表', 1));
  
  children.push(heading('9.1  营收统计', 2));
  children.push(addImage('admin-20-revenue-stats.png', 480, 300));
  caption('营收统计页面');
  children.push(para('查看平台的收入数据，支持按时间范围筛选：'));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '总营收金额', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '各课程收入排行', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '收入趋势图表', size: 24 })] }));

  children.push(heading('9.2  学情统计', 2));
  children.push(addImage('admin-21-learning-stats.png', 480, 300));
  caption('学情统计页面');
  children.push(para('查看学员的学习情况数据：'));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '各课程学习人数和完成率', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '学员活跃度统计', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '学习时长分布', size: 24 })] }));

  children.push(heading('9.3  考试统计', 2));
  children.push(addImage('admin-22-exam-stats.png', 480, 300));
  caption('考试统计页面');
  children.push(para('查看考试相关数据：'));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '各场考试的参加人数和通过率', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '分数段分布', size: 24 })] }));
  children.push(new Paragraph({ numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '各题目正确率分析（找出难题）', size: 24 })] }));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第十章：预约与打卡 ============
  children.push(heading('第十章  预约与打卡管理', 1));

  children.push(heading('10.1  考试预约管理', 2));
  children.push(addImage('admin-23-reservation-list.png', 480, 300));
  caption('考试预约管理');
  children.push(para('管理学员的线下考试预约，可以确认或取消预约。'));
  children.push(step('1', '查看待确认的预约'));
  children.push(step('2', '点击「确认」批准预约'));
  children.push(step('3', '或者点击「取消」拒绝预约'));

  children.push(heading('10.2  课程预约管理', 2));
  children.push(addImage('admin-24-course-reservation.png', 480, 300));
  caption('课程预约管理');
  children.push(para('管理学员的线下课程预约，操作方式与考试预约相同。'));

  children.push(heading('10.3  线下打卡管理', 2));
  children.push(addImage('admin-25-checkins.png', 480, 300));
  caption('线下打卡记录');
  children.push(para('查看学员的线下课程打卡记录，包括打卡时间、打卡地点等信息。'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第十一章：证书与答疑 ============
  children.push(heading('第十一章  证书和答疑管理', 1));

  children.push(heading('11.1  结业证书管理', 2));
  children.push(addImage('admin-26-certificates.png', 480, 300));
  caption('结业证书管理');
  children.push(para('管理学员的结业证书：'));
  children.push(step('1', '查看所有已颁发的证书'));
  children.push(step('2', '点击「颁发证书」给通过考试的学员发证'));
  children.push(step('3', '可以查看证书详情和下载'));

  children.push(heading('11.2  答疑解惑管理', 2));
  children.push(addImage('admin-27-qa-list.png', 480, 300));
  caption('答疑解惑管理');
  children.push(para('查看和回复学员提交的问题：'));
  children.push(step('1', '查看问题列表和详情'));
  children.push(step('2', '回复学员的问题'));
  children.push(step('3', '标记问题状态（已回复/已解决）'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第十二章：系统管理 ============
  children.push(heading('第十二章  系统管理 —— 管理员和权限', 1));
  children.push(para('系统管理是最敏感的功能，请谨慎操作。'));

  children.push(heading('12.1  管理员账号管理', 2));
  children.push(addImage('admin-28-system-users.png', 480, 300));
  caption('系统用户管理');
  children.push(para('管理系统管理员账号：'));
  children.push(step('1', '新增管理员：填写用户名、密码、分配角色'));
  children.push(step('2', '编辑管理员：修改信息或角色'));
  children.push(step('3', '禁用/启用管理员账号'));
  children.push(tip('不要轻易删除管理员账号，建议先禁用而不是删除！'));

  children.push(heading('12.2  角色管理', 2));
  children.push(addImage('admin-29-role-management.png', 480, 300));
  caption('角色权限管理');
  children.push(para('角色管理用于控制不同管理员能看到哪些菜单、操作哪些功能。'));
  children.push(step('1', '新增角色：填写角色名称'));
  children.push(step('2', '勾选这个角色能看到的菜单和权限'));
  children.push(step('3', '保存后，拥有这个角色的管理员就只能看到被勾选的功能'));
  children.push(tip('建议按职责分配角色，比如「课程管理员」只管课程相关，「财务」只管订单和营收。'));

  children.push(heading('12.3  菜单管理', 2));
  children.push(addImage('admin-30-menu-management.png', 480, 300));
  caption('菜单管理');
  children.push(para('管理后台左侧菜单的配置（高级功能，一般不需要修改）：'));
  children.push(step('1', '新增菜单项：填写菜单名称、路径、图标'));
  children.push(step('2', '调整菜单层级和排序'));
  children.push(step('3', '设置菜单权限标识'));
  children.push(tip('菜单配置错误可能导致后台无法正常显示，修改前请确认清楚！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 附录 ============
  children.push(heading('附录  常见问题（FAQ）', 1));

  children.push(heading('Q1: 忘记管理员密码怎么办？', 2));
  children.push(para('联系超级管理员重置密码。超级管理员可以通过系统用户管理页面重置任何管理员的密码。'));

  children.push(heading('Q2: 课程创建后学员看不到？', 2));
  children.push(para('请检查：1）课程是否设为「上架」状态；2）课程是否关联了分类；3）课程是否有章节内容。'));

  children.push(heading('Q3: 学员注册后无法登录学习？', 2));
  children.push(para('新学员注册后需要管理员审核通过才能登录学习。请到「学员管理」中查看是否有待审核的学员。'));

  children.push(heading('Q4: 视频上传后播放不了？', 2));
  children.push(para('视频上传后需要等待转码完成才能播放。请检查视频转码状态，如果长时间未完成可能转码失败需要重新上传。'));

  children.push(heading('Q5: 如何批量添加题目？', 2));
  children.push(para('使用「题库管理」→「题目导入」功能，下载模板，按格式填写后上传 Excel 文件即可批量导入。'));

  children.push(heading('Q6: 考试时间到了系统会自动交卷吗？', 2));
  children.push(para('是的，系统会自动计时，时间一到无论学员是否答完都会自动提交试卷。'));

  // ============ 构建文档 ============
  const doc = new Document({
    styles: {
      default: { document: { run: { font: 'Arial', size: 24 } } },
      paragraphStyles: [
        { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: 36, bold: true, font: 'Arial', color: '2E75B6' },
          paragraph: { spacing: { before: 400, after: 200 }, outlineLevel: 0 } },
        { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: 30, bold: true, font: 'Arial', color: '2E75B6' },
          paragraph: { spacing: { before: 300, after: 180 }, outlineLevel: 1 } },
      ]
    },
    numbering: {
      config: [
        { reference: 'bullets', levels: [{ level: 0, format: LevelFormat.BULLET, text: '\u2022', alignment: AlignmentType.LEFT,
            style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
      ]
    },
    sections: [{
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
        }
      },
      headers: {
        default: new Header({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: '2E75B6', space: 4 } },
            children: [new TextRun({ text: '在线学习平台 · 管理平台操作手册', size: 20, color: '999999', italics: true })]
          })]
        })
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
              new TextRun({ text: '第 ', size: 20, color: '999999' }),
              new TextRun({ children: [PageNumber.CURRENT], size: 20, color: '999999' }),
              new TextRun({ text: ' 页', size: 20, color: '999999' })
            ]
          })]
        })
      },
      children
    }]
  });

  const buffer = await Packer.toBuffer(doc);
  fs.mkdirSync(path.dirname(OUTPUT), { recursive: true });
  fs.writeFileSync(OUTPUT, buffer);
  console.log('管理平台操作手册已生成: ' + OUTPUT);
}

main().catch(e => { console.error(e); process.exit(1); });
