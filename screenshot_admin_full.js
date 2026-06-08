const { chromium } = require('playwright');
const path = require('path');

const ADMIN_URL = 'http://localhost:18088';
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
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });

  try {
    // ====== 1. 截取未填写的登录页 ======
    console.log('admin-01-login');
    let lp = await context.newPage();
    await lp.goto(`${ADMIN_URL}/#/login`, { waitUntil: 'networkidle' });
    await sleep(1500);
    await lp.screenshot({ path: path.join(SCREENSHOT_DIR, 'admin-01-login.png') });

    // ====== 2. 截取已填写的登录页 ======
    console.log('admin-02-login-filled');
    await lp.fill('input[placeholder="请输入用户名"]', 'admin');
    await lp.fill('input[placeholder="请输入密码"]', 'admin123');
    await sleep(500);
    await lp.screenshot({ path: path.join(SCREENSHOT_DIR, 'admin-02-login-filled.png') });
    await lp.close();

    // ====== 3. 执行登录 ======
    console.log('执行管理员登录...');
    let page = await context.newPage();
    await page.goto(`${ADMIN_URL}/#/login`, { waitUntil: 'networkidle' });
    await sleep(1000);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await sleep(500);
    await page.click('button:has-text("登 录")');
    await sleep(3000);

    // 管理后台所有页面
    const pages = [
      { name: 'admin-03-dashboard', url: '/#/dashboard', desc: '仪表盘' },
      { name: 'admin-04-category-list', url: '/#/categories', desc: '课程分类' },
      { name: 'admin-05-course-list', url: '/#/courses', desc: '课程管理' },
      { name: 'admin-06-course-edit', url: '/#/courses/edit/1', desc: '课程编辑' },
      { name: 'admin-07-banner-list', url: '/#/banners', desc: 'Banner管理' },
      { name: 'admin-08-news-modules', url: '/#/news-modules', desc: '新闻模块' },
      { name: 'admin-09-news-list', url: '/#/news', desc: '新闻资讯' },
      { name: 'admin-10-system-config', url: '/#/config', desc: '系统配置' },
      { name: 'admin-11-question-list', url: '/#/questions', desc: '题库管理' },
      { name: 'admin-12-question-import', url: '/#/questions/import', desc: '题目导入' },
      { name: 'admin-13-exam-list', url: '/#/exams', desc: '试卷管理' },
      { name: 'admin-14-exam-edit', url: '/#/exams/edit/1', desc: '试卷编辑' },
      { name: 'admin-15-random-exam', url: '/#/exams/random', desc: '随机组卷' },
      { name: 'admin-16-exam-records', url: '/#/exams/records', desc: '考试记录' },
      { name: 'admin-17-student-list', url: '/#/students', desc: '学员管理' },
      { name: 'admin-18-student-detail', url: '/#/students/1', desc: '学员详情' },
      { name: 'admin-19-order-list', url: '/#/orders', desc: '订单管理' },
      { name: 'admin-20-revenue-stats', url: '/#/statistics/revenue', desc: '营收统计' },
      { name: 'admin-21-learning-stats', url: '/#/statistics/learning', desc: '学情统计' },
      { name: 'admin-22-exam-stats', url: '/#/statistics/exam', desc: '考试统计' },
      { name: 'admin-23-reservation-list', url: '/#/reservations', desc: '考试预约' },
      { name: 'admin-24-course-reservation', url: '/#/course-reservations', desc: '课程预约' },
      { name: 'admin-25-checkins', url: '/#/checkins', desc: '线下打卡' },
      { name: 'admin-26-certificates', url: '/#/certificates', desc: '结业证书' },
      { name: 'admin-27-qa-list', url: '/#/qa-questions', desc: '答疑解惑' },
      { name: 'admin-28-system-users', url: '/#/system/users', desc: '系统用户' },
      { name: 'admin-29-role-management', url: '/#/system/roles', desc: '角色管理' },
      { name: 'admin-30-menu-management', url: '/#/system/menus', desc: '菜单管理' },
    ];

    for (const p of pages) {
      console.log(`${p.name} (${p.desc})`);
      try {
        await page.goto(`${ADMIN_URL}${p.url}`, { waitUntil: 'networkidle', timeout: 15000 });
        await sleep(2000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${p.name}.png`), fullPage: p.desc.includes('列表') });
      } catch (e) {
        console.log(`  警告: ${p.name} 失败 - ${e.message}`);
        // 尝试重新登录
        try {
          await page.goto(`${ADMIN_URL}/#/login`, { waitUntil: 'networkidle', timeout: 10000 });
          await sleep(1000);
          await page.fill('input[placeholder="请输入用户名"]', 'admin');
          await page.fill('input[placeholder="请输入密码"]', 'admin123');
          await page.click('button:has-text("登 录")');
          await sleep(3000);
          await page.goto(`${ADMIN_URL}${p.url}`, { waitUntil: 'networkidle', timeout: 15000 });
          await sleep(2000);
          await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${p.name}.png`) });
        } catch (e2) {
          console.log(`  重试也失败: ${e2.message}`);
        }
      }
    }

    await page.close();
  } finally {
    await browser.close();
  }

  console.log('✅ 管理后台截图全部完成！共30张');
}

main().catch(console.error);
