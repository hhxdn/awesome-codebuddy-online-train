const { chromium } = require('playwright');
const path = require('path');

const ADMIN_URL = 'http://localhost:18088';
const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

// 管理员所有需要截图的页面
const pages = [
  { name: 'admin-01-login', url: '/#/login', desc: '登录' },
  { name: 'admin-02-login-filled', url: '/#/login', desc: '登录-已填', filled: true },
  { name: 'admin-03-dashboard', url: '/#/dashboard', desc: '仪表盘' },
  { name: 'admin-04-category-list', url: '/#/categories', desc: '课程分类' },
  { name: 'admin-05-course-list', url: '/#/courses', desc: '课程管理' },
  { name: 'admin-06-course-edit', url: '/#/courses/edit/1', desc: '课程编辑' },
  { name: 'admin-07-question-list', url: '/#/questions', desc: '题库管理' },
  { name: 'admin-08-question-import', url: '/#/questions/import', desc: '题目导入' },
  { name: 'admin-09-exam-list', url: '/#/exams', desc: '试卷管理' },
  { name: 'admin-10-exam-edit', url: '/#/exams/edit/1', desc: '试卷编辑' },
  { name: 'admin-11-exam-records', url: '/#/exams/records', desc: '考试记录' },
  { name: 'admin-12-student-list', url: '/#/students', desc: '学员管理' },
  { name: 'admin-13-student-detail', url: '/#/students/1', desc: '学员详情' },
  { name: 'admin-14-order-list', url: '/#/orders', desc: '订单管理' },
  { name: 'admin-15-revenue-stats', url: '/#/statistics/revenue', desc: '营收统计' },
  { name: 'admin-16-learning-stats', url: '/#/statistics/learning', desc: '学情统计' },
  { name: 'admin-17-exam-stats', url: '/#/statistics/exam', desc: '考试统计' },
];

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function doLogin(page) {
  await page.goto(`${ADMIN_URL}/#/login`, { waitUntil: 'networkidle', timeout: 15000 });
  await sleep(1500);
  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await sleep(500);
  await page.click('button:has-text("登 录")');
  await sleep(3000);
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
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });

  try {
    // 1. 截图未填写的登录页
    console.log('1/17 admin-01-login (登录)');
    let lp = await context.newPage();
    await lp.goto(`${ADMIN_URL}/#/login`, { waitUntil: 'networkidle' });
    await sleep(1500);
    await lp.screenshot({ path: path.join(SCREENSHOT_DIR, 'admin-01-login.png') });

    // 2. 截图已填写的登录页
    console.log('2/17 admin-02-login-filled (登录-已填)');
    await lp.fill('input[placeholder="请输入用户名"]', 'admin');
    await lp.fill('input[placeholder="请输入密码"]', 'admin123');
    await sleep(500);
    await lp.screenshot({ path: path.join(SCREENSHOT_DIR, 'admin-02-login-filled.png') });
    await lp.close();

    // 3. 正式登录
    console.log('执行登录...');
    let page = await context.newPage();
    await doLogin(page);

    // 4. 逐个页面截图
    let num = 3;
    for (const p of pages) {
      if (p.name === 'admin-01-login' || p.name === 'admin-02-login-filled') continue;
      console.log(`${num++}/17 ${p.name} (${p.desc})`);
      try {
        await page.goto(`${ADMIN_URL}${p.url}`, { waitUntil: 'networkidle', timeout: 15000 });
        await sleep(2000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${p.name}.png`) });
      } catch (e) {
        console.log(`  警告: ${p.name} 失败 - ${e.message}`);
      }
    }

    await page.close();
  } finally {
    await browser.close();
  }

  console.log('✅ 管理后台截图完成！');
}

main().catch(console.error);
