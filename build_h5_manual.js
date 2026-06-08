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
const OUTPUT = path.join(__dirname, 'docs', 'H5用户操作手册.docx');

function img(name) {
  const p = path.join(SCREENSHOT_DIR, name);
  if (fs.existsSync(p)) return fs.readFileSync(p);
  console.warn(`  警告: 截图 ${name} 不存在`);
  return null;
}

function addImage(name, w, h) {
  const data = img(name);
  if (!data) return new Paragraph({ children: [new TextRun(`[图片: ${name}]`)] });
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
    children: [new TextRun({ text: `▲ ${text}`, italics: true, size: 20, color: '666666' })]
  });
}

function tip(text) {
  return new Paragraph({
    spacing: { before: 100, after: 100 },
    border: { left: { style: BorderStyle.SINGLE, size: 12, color: '2E75B6', space: 8 } },
    children: [
      new TextRun({ text: '💡 小贴士：', bold: true, color: '2E75B6', size: 22 }),
      new TextRun({ text, size: 22 })
    ]
  });
}

function step(num, text) {
  return new Paragraph({
    spacing: { before: 160, after: 80 },
    children: [
      new TextRun({ text: `第${num}步：`, bold: true, size: 24, color: '2E75B6' }),
      new TextRun({ text, size: 24 })
    ]
  });
}

function para(text, opts = {}) {
  return new Paragraph({
    spacing: { before: 80, after: 80 },
    ...opts,
    children: [new TextRun({ text, size: 24, ...opts })]
  });
}

function heading(text, level) {
  return new Paragraph({
    heading: level === 1 ? HeadingLevel.HEADING_1 : HeadingLevel.HEADING_2,
    spacing: { before: level === 1 ? 400 : 300, after: 200 },
    children: [new TextRun({ text, bold: true, size: level === 1 ? 36 : 30, font: 'Arial' })]
  });
}

const border = { style: BorderStyle.SINGLE, size: 1, color: 'CCCCCC' };
const borders = { top: border, bottom: border, left: border, right: border };
const headerShading = { fill: '2E75B6', type: ShadingType.CLEAR };
const cellMargins = { top: 60, bottom: 60, left: 100, right: 100 };

function headerCell(text, w) {
  return new TableCell({
    borders, width: { size: w, type: WidthType.DXA }, shading: headerShading, margins: cellMargins,
    children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text, bold: true, color: 'FFFFFF', size: 22 })] })]
  });
}

function cell(text, w, opts = {}) {
  return new TableCell({
    borders, width: { size: w, type: WidthType.DXA }, margins: cellMargins,
    shading: opts.shading ? { fill: opts.shading, type: ShadingType.CLEAR } : undefined,
    children: [new Paragraph({ alignment: opts.center ? AlignmentType.CENTER : AlignmentType.LEFT, children: [new TextRun({ text, size: 22, ...opts })] })]
  });
}

async function main() {
  const children = [];

  // ============ 封面 ============
  children.push(new Paragraph({ spacing: { before: 3000 } }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 }, children: [new TextRun({ text: '在线学习平台', size: 56, bold: true, color: '2E75B6', font: 'Arial' })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: '—— 学员操作手册 ——', size: 36, color: '555555', font: 'Arial' })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 600 }, children: [new TextRun({ text: '包教包会版 · 奶奶也能看懂', size: 28, color: '888888', italics: true })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: '适用版本：v2.0', size: 24, color: '999999' })] }));
  children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: '文档更新日期：2026年6月', size: 24, color: '999999' })] }));
  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 目录 ============
  children.push(heading('目  录', 1));
  children.push(new TableOfContents('目录', { hyperlink: true, headingStyleRange: '1-2' }));
  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第一章：快速开始 ============
  children.push(heading('第一章  快速开始 —— 怎么打开这个学习平台', 1));
  children.push(para('您只需要一部能上网的手机（智能手机），打开微信或者手机自带的浏览器就可以开始学习了。'));
  children.push(tip('如果您用的是电脑，用浏览器打开也能学习，屏幕更大更舒服！'));

  children.push(heading('1.1  方式一：用微信打开（推荐）', 2));
  children.push(step('1', '打开微信，在聊天界面向下滑动，会出现「小程序」入口'));
  children.push(step('2', '在搜索框里输入学习平台的名字，点进去就能看到啦'));
  children.push(step('3', '第一次用的话，先点底部的「我的」注册账号'));
  children.push(tip('微信小程序打开速度最快，而且下次直接在微信首页下拉就能找到，最方便！'));

  children.push(heading('1.2  方式二：用手机浏览器打开', 2));
  children.push(step('1', '打开手机自带的浏览器（比如 Safari 或 Chrome）'));
  children.push(step('2', '在地址栏输入学习平台的网址'));
  children.push(step('3', '打开后就可以开始学习了'));
  children.push(tip('用浏览器打开的话，可以把这个网页「添加到主屏幕」，以后就像普通 App 一样点开就能用！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第二章：注册和登录 ============
  children.push(heading('第二章  注册和登录 —— 怎么进入自己的账号', 1));
  children.push(para('第一次使用需要先注册一个账号，注册过以后每次直接登录就行。'));

  children.push(heading('2.1  注册新账号', 2));
  children.push(addImage('01-login.png', 220, 470));
  caption('登录页面 —— 点"注册"切换');
  children.push(step('1', '打开学习平台，会看到登录/注册页面'));
  children.push(step('2', '点一下上面「注册」两个字'));
  children.push(step('3', '输入您的手机号码'));
  children.push(step('4', '设置一个密码（至少6位数字或字母，要记好哦）'));
  children.push(step('5', '再输入一遍密码（防止手滑输错）'));
  children.push(step('6', '点底部的「注册」大按钮'));

  children.push(heading('2.2  完善个人资料', 2));
  children.push(para('注册成功后，系统会要求您填写一些基本信息。别紧张，就是姓名、性别、年龄等简单信息。'));
  children.push(addImage('01b-register-profile.png', 220, 470));
  caption('完善个人资料页面');
  children.push(step('1', '输入您的真实姓名'));
  children.push(step('2', '选择性别（男/女）'));
  children.push(step('3', '输入年龄'));
  children.push(step('4', '选择学历和专业（方便推荐课程）'));
  children.push(step('5', '输入联系电话（方便管理员联系您）'));
  children.push(step('6', '点「提交」按钮'));
  children.push(tip('填完资料后可能需要管理员审核，审核通过才能开始学习。别着急，一般很快的！'));

  children.push(heading('2.3  登录已有账号', 2));
  children.push(addImage('02-login-filled.png', 220, 470));
  caption('登录页面 —— 填写手机号和密码');
  children.push(step('1', '在登录页面，输入您的手机号码'));
  children.push(step('2', '输入您之前设置的密码'));
  children.push(step('3', '点底部蓝色的「登录」按钮'));
  children.push(tip('登录过一次后，下次打开会自动登录，不需要每次都输入密码，省心多了！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第三章：首页 ============
  children.push(heading('第三章  首页 —— 打开后第一眼看到的地方', 1));
  children.push(para('登录成功后，您就进入学习平台的主页面了，我们叫它「首页」。'));
  children.push(addImage('03-home.png', 280, 600));
  caption('首页全貌');
  
  children.push(heading('3.1  首页都有什么？', 2));
  children.push(para('从上到下，首页分成这几个部分：'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 },
    spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '顶部轮播图：展示最新的课程推荐和学习活动', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 },
    spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '最新资讯：看看平台有什么新消息', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 },
    spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '课程分类栏：按类别浏览课程，比如「Java开发」「前端开发」等', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 },
    spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '课程列表：往下滑能看到所有可以学习的课程', size: 24 })]
  }));

  children.push(heading('3.2  底部导航栏 —— 五个功能入口', 2));
  children.push(para('页面最底部有五个按钮，这是整个学习平台最重要的导航：'));
  
  children.push(new Table({
    width: { size: 9000, type: WidthType.DXA },
    columnWidths: [1800, 7200],
    rows: [
      new TableRow({ children: [headerCell('按钮', 1800), headerCell('干什么用的', 7200)] }),
      new TableRow({ children: [cell('首页', 1800, { center: true }), cell('回到主页面，看最新课程和资讯', 7200)] }),
      new TableRow({ children: [cell('课程', 1800, { center: true }), cell('浏览所有课程，搜索想学的课程', 7200)] }),
      new TableRow({ children: [cell('练习', 1800, { center: true }), cell('做课后练习题，巩固知识', 7200)] }),
      new TableRow({ children: [cell('考试', 1800, { center: true }), cell('参加考试，检验学习成果', 7200)] }),
      new TableRow({ children: [cell('我的', 1800, { center: true }), cell('查看个人信息、学习记录、订单等', 7200)] }),
    ]
  }));
  
  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第四章：浏览和选择课程 ============
  children.push(heading('第四章  找课程 —— 找到您想学的内容', 1));
  
  children.push(heading('4.1  课程列表页', 2));
  children.push(addImage('04-course-list.png', 280, 600));
  caption('课程列表页面');
  children.push(step('1', '点底部「课程」按钮，进入课程列表'));
  children.push(step('2', '顶部有搜索框，输入关键词可以搜课程'));
  children.push(step('3', '下方是分类标签，点一下可以只看某一类的课程'));
  children.push(step('4', '往下滑动可以看到更多课程，滑到底会自动加载下一页'));
  children.push(tip('每个课程卡片上会显示课程标题、封面图、价格（免费会写「免费」）、有多少人在学等信息。'));

  children.push(heading('4.2  课程详情页', 2));
  children.push(para('在课程列表里看到感兴趣的课程，点进去就能看到详细信息。'));
  children.push(addImage('05-course-detail.png', 280, 600));
  caption('课程详情页面');
  children.push(para('课程详情页从上往下依次是：'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '课程封面大图：显示课程标题、价格、分类', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '课程介绍：描述这个课程讲什么，可以点「展开」看更多', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '课程目录：列出所有章节，每节有视频和练习入口', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '底部操作栏：购买课程 / 开始学习 / 预约线下课 / 线下打卡', size: 24 })]
  }));

  children.push(heading('4.3  练习课程列表', 2));
  children.push(addImage('08-practice-courses.png', 280, 600));
  caption('练习课程列表');
  children.push(para('点底部「练习」按钮，可以看到有课后练习题的课程。选择一个课程点进去，就可以做练习啦！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第五章：学习课程 ============
  children.push(heading('第五章  学课程 —— 看视频学知识', 1));
  
  children.push(heading('5.1  课程目录（章节列表）', 2));
  children.push(addImage('06-chapter-list.png', 280, 600));
  caption('课程目录 / 章节列表');
  children.push(para('进入课程目录后，可以看到：'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '顶部进度条：显示您学了多少，一目了然', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '章节列表：每一节前面有个图标，✅ 表示学完了，🔄 表示学习中，⬜ 表示还没学', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '每节有两个按钮：「视频」看视频、「练习」做题目', size: 24 })]
  }));

  children.push(heading('5.2  看视频', 2));
  children.push(addImage('07-video-player.png', 280, 600));
  caption('视频播放页面');
  children.push(para('点「视频」按钮就进入播放页面：'));
  children.push(step('1', '视频会自动开始播放'));
  children.push(step('2', '点右下角「倍速」可以调整播放速度（0.5倍慢放 / 1.5倍快放 / 2倍速等）'));
  children.push(step('3', '视频会自动记住您看到哪里了，下次进来接着看'));
  children.push(step('4', '看完90%以上系统会自动标记为「已完成」'));
  children.push(step('5', '看完一节后，点「下一节」继续学习'));
  children.push(tip('建议一口气看完一整节，中途退出的话也不用担心，下次进来会从断点继续播放！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第六章：课后练习 ============
  children.push(heading('第六章  做练习 —— 学完就练记得牢', 1));
  
  children.push(heading('6.1  章节练习首页', 2));
  children.push(addImage('09-practice-home.png', 280, 600));
  caption('章节练习首页');
  children.push(para('在章节列表点「练习」按钮，进入练习页面：'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '显示本章节有多少道题、您的最高得分', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '点「开始练题」开始做题', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '点「错题本」可以只看之前做错的题目', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '下方是练习记录，能看到每次练习的成绩', size: 24 })]
  }));

  children.push(heading('6.2  练习答题', 2));
  children.push(addImage('10-practice-question.png', 280, 600));
  caption('练习答题页面');
  children.push(para('进入答题页面后：'));
  children.push(step('1', '顶部显示当前是第几题 / 共几题'));
  children.push(step('2', '题目类型有单选、多选、判断、简答'));
  children.push(step('3', '选择题直接点选项就可以了'));
  children.push(step('4', '判断题选「对」或「错」'));
  children.push(step('5', '简答题需要自己输入答案'));
  children.push(step('6', '做完所有题目后提交，会显示成绩和每道题的对错'));
  children.push(tip('练习可以反复做，每次题目顺序可能不一样，帮助您真正掌握知识点！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第七章：参加考试 ============
  children.push(heading('第七章  考 试 —— 检验学习成果', 1));
  
  children.push(heading('7.1  考试列表', 2));
  children.push(addImage('11-exam-list.png', 280, 600));
  caption('考试列表页面');
  children.push(para('点底部「考试」按钮进入考试列表，可以看到所有可以参加的考试：'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '按分类筛选考试', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '每个考试会显示：名称、时长、总分、及格分数、题目数', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '状态标签：✅已通过 / ❌未通过 / ⬜未参加', size: 24 })]
  }));

  children.push(heading('7.2  考试确认', 2));
  children.push(addImage('12-exam-start.png', 280, 600));
  caption('考试确认页面');
  children.push(para('选择一场考试后，会先看到考试确认页：'));
  children.push(step('1', '确认考试信息：时长、总分、及格分、题目数'));
  children.push(step('2', '仔细阅读考试须知（重要！）'));
  children.push(step('3', '注意：考试一旦开始计时就不会停'));
  children.push(step('4', '中途切到其他 App 会被记录，切出超过3次自动交卷'));
  children.push(step('5', '准备好后，点「开始考试」按钮'));

  children.push(heading('7.3  考试答题', 2));
  children.push(addImage('13-exam-question.png', 280, 600));
  caption('考试答题页面');
  children.push(para('答题页面与练习类似，但有几个不同点：'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '顶部有倒计时，剩余时间用完了会自动交卷', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '答完所有题后点「交卷」提交', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '考试结束后会立即显示成绩', size: 24 })]
  }));
  children.push(tip('考试前建议先做好练习，把错题都复习一遍！考试机会有限，认真对待哦！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第八章：个人中心 ============
  children.push(heading('第八章  我的 —— 个人中心', 1));
  children.push(addImage('15-profile.png', 280, 600));
  caption('个人中心（我的）页面');
  children.push(para('点底部「我的」按钮进入个人中心，这里啥都有：'));
  
  children.push(heading('8.1  我的课程', 2));
  children.push(addImage('16-my-courses.png', 280, 600));
  caption('我的课程列表');
  children.push(para('显示您购买或正在学习的课程。每个课程会显示封面、标题、学习进度条。点进去就能继续学习。'));

  children.push(heading('8.2  我的订单', 2));
  children.push(addImage('17-my-orders.png', 280, 600));
  caption('我的订单列表');
  children.push(para('查看购买记录。顶部可以筛选：全部 / 待支付 / 已支付 / 已取消。'));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '待支付的订单可以点「去支付」或「取消」', size: 24 })]
  }));
  children.push(new Paragraph({
    numbering: { reference: 'bullets', level: 0 }, spacing: { before: 60, after: 60 },
    children: [new TextRun({ text: '已支付的订单可以点「查看课程」去学习', size: 24 })]
  }));

  children.push(heading('8.3  我的错题', 2));
  children.push(addImage('18-wrong-questions.png', 280, 600));
  caption('我的错题列表');
  children.push(para('所有做错的题目都在这里，按课程分组。每道错题显示错误次数，可以点「重做」再练一遍，也可以「清空全部」错题。'));

  children.push(heading('8.4  考试记录', 2));
  children.push(addImage('19-exam-records.png', 280, 600));
  caption('考试记录列表');
  children.push(para('显示您参加过的所有考试，包含试卷名、得分、是否通过、用时等信息。点进去可以查看详细的答题回顾。'));

  children.push(heading('8.5  学习记录', 2));
  children.push(addImage('20-learning-records.png', 280, 600));
  caption('学习记录页面');
  children.push(para('按课程展示您的学习进度：已经学了多少节、总共有多少节、总共学了多长时间。'));

  children.push(heading('8.6  我的证书', 2));
  children.push(addImage('24-certificates.png', 280, 600));
  caption('我的证书列表');
  children.push(para('通过考试后可以获得结业证书。点证书可以看到详细信息，还能下载附件哦！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第九章：购买课程 ============
  children.push(heading('第九章  购买课程 —— 付费课程怎么买', 1));
  
  children.push(heading('9.1  确认订单', 2));
  children.push(addImage('21-order-confirm.png', 280, 600));
  caption('确认订单页面');
  children.push(step('1', '在课程详情页点「立即购买」进入订单页'));
  children.push(step('2', '确认课程信息（标题、价格）'));
  children.push(step('3', '选择支付方式：微信支付 或 支付宝'));
  children.push(step('4', '点「确认支付」完成购买'));
  children.push(tip('购买成功后，课程就会出现在「我的课程」里面，随时可以开始学习！'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 第十章：其他功能 ============
  children.push(heading('第十章  其他实用功能', 1));
  
  children.push(heading('10.1  最新资讯', 2));
  children.push(addImage('14-news-list.png', 280, 600));
  caption('最新资讯列表');
  children.push(addImage('30-news-detail.png', 280, 600));
  caption('资讯详情');
  children.push(para('在首页或底部可以找到「最新资讯」，查看平台发布的学习资料、通知公告等。'));

  children.push(heading('10.2  答疑解惑', 2));
  children.push(addImage('22-qa-submit.png', 280, 600));
  caption('答疑解惑提交页面');
  children.push(para('学习中遇到问题？来这里提交问题给老师：'));
  children.push(step('1', '描述您的问题'));
  children.push(step('2', '可以上传相关截图或照片'));
  children.push(step('3', '留下您的手机号方便老师联系'));
  children.push(step('4', '点「提交」'));

  children.push(heading('10.3  报名学习', 2));
  children.push(addImage('23-enroll.png', 280, 600));
  caption('报名学习页面');
  children.push(para('想报名课程的同学来这里，按步骤填写信息提交即可。'));

  children.push(heading('10.4  预约线下课程', 2));
  children.push(addImage('27-course-reservation.png', 280, 600));
  caption('预约线下课程');
  children.push(para('有些课程有线下授课，可以预约参加：选择时间、填写备注后提交。'));
  children.push(addImage('28-my-course-reservations.png', 280, 600));
  caption('我的课程预约记录');
  children.push(para('在「我的课程预约」中可以查看预约状态，待确认的预约可以取消。'));

  children.push(heading('10.5  线下打卡', 2));
  children.push(addImage('26-checkin.png', 280, 600));
  caption('线下打卡页面');
  children.push(para('参加线下课时，需要在指定地点范围内打卡签到。GPS 会自动定位，在范围内点「确认打卡」就行。'));

  children.push(heading('10.6  关于我们', 2));
  children.push(addImage('25-about.png', 280, 600));
  caption('关于我们页面');
  children.push(para('想了解更多平台信息？点「关于我们」查看详细介绍。'));

  children.push(new Paragraph({ children: [new PageBreak()] }));

  // ============ 附录 ============
  children.push(heading('附录  常见问题（FAQ）', 1));
  
  children.push(heading('Q1: 忘记密码怎么办？', 2));
  children.push(para('请联系平台管理员帮您重置密码。'));

  children.push(heading('Q2: 视频加载不出来/卡顿怎么办？', 2));
  children.push(para('请检查网络连接是否正常，可以尝试切换到 WiFi 网络。如果仍然不行，联系管理员。'));

  children.push(heading('Q3: 考试中途退出了还能继续吗？', 2));
  children.push(para('如果是不小心退出，请尽快回到考试页面。计时仍在继续，请注意剩余时间。切出超过3次会自动交卷。'));

  children.push(heading('Q4: 购买课程后没看到怎么办？', 2));
  children.push(para('请到「我的」→「我的课程」里看看。如果还是没有，到「我的订单」确认是否支付成功。'));

  children.push(heading('Q5: 怎么获得证书？', 2));
  children.push(para('通过课程对应的考试后，系统会自动生成结业证书。去「我的」→「我的证书」就能看到。'));

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
            children: [new TextRun({ text: '在线学习平台 · 学员操作手册', size: 20, color: '999999', italics: true })]
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
  console.log('✅ H5用户操作手册已生成: ' + OUTPUT);
}

main().catch(e => { console.error(e); process.exit(1); });
