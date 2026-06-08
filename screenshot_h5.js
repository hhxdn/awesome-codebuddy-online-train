const { chromium } = require('playwright');
const path = require('path');

const H5_URL = 'http://localhost:3001';
const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function main() {
  const browser = await chromium.launch({
    headless: true,
    channel: 'chrome',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  }).catch(async () => {
    return await chromium.launch({ headless: true });
  });

  const context = await browser.newContext({
    viewport: { width: 375, height: 812, deviceScaleFactor: 2, isMobile: true, hasTouch: true },
  });

  const page = await context.newPage();

  try {
    // ====== 1. 登录页 ======
    console.log('01-login');
    await page.goto(`${H5_URL}/#/login`, { waitUntil: 'networkidle' });
    await sleep(1500);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01-login.png') });

    // ====== 2. 登录页-已填写 ======
    console.log('02-login-filled');
    await page.fill('input[placeholder="手机号"]', '13800138000');
    await page.fill('input[placeholder="密码"]', '123456');
    await sleep(500);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '02-login-filled.png') });

    // ====== 3. 执行登录 ======
    console.log('执行登录...');
    await page.click('button:has-text("登录")');
    await sleep(3000);
    
    // 检查是否需要完善资料或审核
    const currentUrl = page.url();
    if (currentUrl.includes('register-profile')) {
      console.log('需要完善资料，先截图完善资料页');
      await sleep(1000);
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01b-register-profile.png') });
      // 填写资料
      try {
        await page.fill('input[placeholder="请输入姓名"]', '张三');
        const selects = await page.$$('.van-field');
        // 简单填写
        await sleep(500);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01c-register-profile-filled.png') });
        // 提交
        const submitBtn = await page.$('button:has-text("提交")');
        if (submitBtn) await submitBtn.click();
        await sleep(2000);
      } catch(e) { console.log('资料填写跳过:', e.message); }
    }
    if (page.url().includes('pending-approval')) {
      console.log('审核中页面');
      await sleep(1000);
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01d-pending-approval.png') });
    }

    // ====== 4. 首页 ======
    console.log('03-home');
    await page.goto(`${H5_URL}/#/home`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '03-home.png') });

    // ====== 5. 课程列表 ======
    console.log('04-course-list');
    await page.goto(`${H5_URL}/#/courses`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '04-course-list.png') });

    // ====== 6. 课程详情 ======
    console.log('05-course-detail');
    await page.goto(`${H5_URL}/#/course/1`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '05-course-detail.png') });

    // ====== 7. 章节列表 ======
    console.log('06-chapter-list');
    await page.goto(`${H5_URL}/#/course/1/chapters`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '06-chapter-list.png') });

    // ====== 8. 视频播放 ======
    console.log('07-video-player');
    // 先获取第一个章节ID
    try {
      const resp = await page.evaluate(async () => {
        const r = await fetch('/api/courses/1/chapters', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('h5_token') }
        });
        return await r.json();
      });
      if (resp.data && resp.data.length > 0) {
        const firstVideo = resp.data.find(c => c.videoUrl);
        if (firstVideo) {
          await page.goto(`${H5_URL}/#/video/${firstVideo.id}`, { waitUntil: 'networkidle' });
          await sleep(2500);
          await page.screenshot({ path: path.join(SCREENSHOT_DIR, '07-video-player.png') });
        }
      }
    } catch(e) { console.log('视频页截图跳过:', e.message); }

    // ====== 9. 练习课程列表 ======
    console.log('08-practice-courses');
    await page.goto(`${H5_URL}/#/practice-courses`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '08-practice-courses.png') });

    // ====== 10. 章节练习首页 ======
    console.log('09-practice-home');
    // 尝试获取有练习题的章节
    try {
      const resp = await page.evaluate(async () => {
        const r = await fetch('/api/courses/1/chapters', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('h5_token') }
        });
        return await r.json();
      });
      if (resp.data) {
        const ch = resp.data.find(c => c.hasPractice);
        if (ch) {
          await page.goto(`${H5_URL}/#/practice/${ch.id}`, { waitUntil: 'networkidle' });
          await sleep(2000);
          await page.screenshot({ path: path.join(SCREENSHOT_DIR, '09-practice-home.png') });
        }
      }
    } catch(e) { console.log('练习首页截图跳过:', e.message); }

    // ====== 11. 练习答题 ======
    console.log('10-practice-question');
    // 找到开始练习按钮并点击
    try {
      const startBtn = await page.$('button:has-text("开始练题")');
      if (startBtn) {
        await startBtn.click();
        await sleep(2000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, '10-practice-question.png') });
      }
    } catch(e) { console.log('练习答题截图跳过:', e.message); }

    // ====== 12. 考试列表 ======
    console.log('11-exam-list');
    await page.goto(`${H5_URL}/#/exam`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '11-exam-list.png') });

    // ====== 13. 考试开始确认 ======
    console.log('12-exam-start');
    try {
      const resp = await page.evaluate(async () => {
        const r = await fetch('/api/h5/exams', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('h5_token') }
        });
        return await r.json();
      });
      if (resp.data && resp.data.length > 0) {
        await page.goto(`${H5_URL}/#/exam/start/${resp.data[0].id}`, { waitUntil: 'networkidle' });
        await sleep(2000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, '12-exam-start.png') });
      }
    } catch(e) { console.log('考试开始页截图跳过:', e.message); }

    // ====== 14. 考试答题 ======
    console.log('13-exam-question');
    try {
      const startExamBtn = await page.$('button:has-text("开始考试")');
      if (startExamBtn) {
        await startExamBtn.click();
        await sleep(3000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, '13-exam-question.png') });
      }
    } catch(e) { console.log('考试答题截图跳过:', e.message); }

    // ====== 15. 最新资讯 ======
    console.log('14-news-list');
    await page.goto(`${H5_URL}/#/news`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '14-news-list.png') });

    // ====== 16. 我的 ======
    console.log('15-profile');
    await page.goto(`${H5_URL}/#/mine`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '15-profile.png') });

    // ====== 17. 我的课程 ======
    console.log('16-my-courses');
    await page.goto(`${H5_URL}/#/my-courses`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '16-my-courses.png') });

    // ====== 18. 我的订单 ======
    console.log('17-my-orders');
    await page.goto(`${H5_URL}/#/my-orders`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '17-my-orders.png') });

    // ====== 19. 我的错题 ======
    console.log('18-wrong-questions');
    await page.goto(`${H5_URL}/#/my-wrong`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '18-wrong-questions.png') });

    // ====== 20. 考试记录 ======
    console.log('19-exam-records');
    await page.goto(`${H5_URL}/#/my-exams`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '19-exam-records.png') });

    // ====== 21. 学习记录 ======
    console.log('20-learning-records');
    await page.goto(`${H5_URL}/#/my-learning`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '20-learning-records.png') });

    // ====== 22. 订单确认 ======
    console.log('21-order-confirm');
    await page.goto(`${H5_URL}/#/order/confirm/1`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '21-order-confirm.png') });

    // ====== 23. 答疑解惑 ======
    console.log('22-qa-submit');
    await page.goto(`${H5_URL}/#/qa-submit`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '22-qa-submit.png') });

    // ====== 24. 报名学习 ======
    console.log('23-enroll');
    await page.goto(`${H5_URL}/#/enroll`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '23-enroll.png') });

    // ====== 25. 我的证书 ======
    console.log('24-certificates');
    await page.goto(`${H5_URL}/#/my-certificates`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '24-certificates.png') });

    // ====== 26. 关于我们 ======
    console.log('25-about');
    await page.goto(`${H5_URL}/#/about`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '25-about.png') });

    // ====== 27. 线下打卡 ======
    console.log('26-checkin');
    await page.goto(`${H5_URL}/#/checkin/1`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '26-checkin.png') });

    // ====== 28. 课程预约 ======
    console.log('27-course-reservation');
    await page.goto(`${H5_URL}/#/course/reservation/1`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '27-course-reservation.png') });

    // ====== 29. 我的课程预约 ======
    console.log('28-my-course-reservations');
    await page.goto(`${H5_URL}/#/my-course-reservations`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '28-my-course-reservations.png') });

    // ====== 30. 我的考试预约 ======
    console.log('29-my-reservations');
    await page.goto(`${H5_URL}/#/my-reservations`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '29-my-reservations.png') });

    // ====== 31. 资讯详情 ======
    console.log('30-news-detail');
    await page.goto(`${H5_URL}/#/news/1`, { waitUntil: 'networkidle' });
    await sleep(2000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '30-news-detail.png') });

  } finally {
    await browser.close();
  }

  console.log('✅ H5用户端截图全部完成！共30张');
}

main().catch(console.error);
