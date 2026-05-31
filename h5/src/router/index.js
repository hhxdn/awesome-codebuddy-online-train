import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import { get } from '../api'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register-profile',
    name: 'RegisterProfile',
    component: () => import('../views/user/RegisterProfile.vue'),
    meta: { title: '完善资料' }
  },
  {
    path: '/pending-approval',
    name: 'PendingApproval',
    component: () => import('../views/user/PendingApproval.vue'),
    meta: { title: '审核中' }
  },
  {
    path: '/',
    name: 'TabbarLayout',
    component: () => import('../views/layout/TabbarLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('../views/course/CourseList.vue'),
        meta: { title: '课程' }
      },
      {
        path: 'exam',
        name: 'Exam',
        component: () => import('../views/exam/ExamList.vue'),
        meta: { title: '考试' }
      },
      {
        path: 'mine',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue'),
        meta: { title: '我的' }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('../views/user/MyCourses.vue'),
        meta: { title: '我的课程' }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('../views/order/OrderList.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'my-wrong',
        name: 'MyWrongQuestions',
        component: () => import('../views/user/MyWrongQuestions.vue'),
        meta: { title: '我的错题' }
      },
      {
        path: 'my-exams',
        name: 'MyExamRecords',
        component: () => import('../views/user/MyExamRecords.vue'),
        meta: { title: '考试记录' }
      },
      {
        path: 'my-learning',
        name: 'MyLearningRecords',
        component: () => import('../views/user/MyLearningRecords.vue'),
        meta: { title: '学习记录' }
      },
      {
        path: 'my-certificates',
        name: 'MyCertificates',
        component: () => import('../views/user/MyCertificates.vue'),
        meta: { title: '我的证书' }
      }
    ]
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: () => import('../views/course/CourseDetail.vue'),
    meta: { title: '课程详情' }
  },
  {
    path: '/course/:id/chapters',
    name: 'ChapterList',
    component: () => import('../views/course/ChapterList.vue'),
    meta: { title: '课程目录' }
  },
  {
    path: '/video/:chapterId',
    name: 'VideoPlayer',
    component: () => import('../views/course/VideoPlayer.vue'),
    meta: { title: '视频播放' }
  },
  {
    path: '/practice/:chapterId',
    name: 'PracticeHome',
    component: () => import('../views/practice/PracticeHome.vue'),
    meta: { title: '章节练习' }
  },
  {
    path: '/practice/:chapterId/do',
    name: 'PracticeQuestion',
    component: () => import('../views/practice/PracticeQuestion.vue'),
    meta: { title: '练习答题' }
  },
  {
    path: '/practice/:chapterId/result',
    name: 'PracticeResult',
    component: () => import('../views/practice/PracticeResult.vue'),
    meta: { title: '练习结果' }
  },
  {
    path: '/exam/:courseId',
    name: 'ExamCourseList',
    component: () => import('../views/exam/ExamList.vue'),
    meta: { title: '考试列表' }
  },
  {
    path: '/exam/start/:paperId',
    name: 'ExamStart',
    component: () => import('../views/exam/ExamStart.vue'),
    meta: { title: '考试确认' }
  },
  {
    path: '/exam/do/:recordId',
    name: 'ExamQuestion',
    component: () => import('../views/exam/ExamQuestion.vue'),
    meta: { title: '考试中' }
  },
  {
    path: '/exam/result/:recordId',
    name: 'ExamResult',
    component: () => import('../views/exam/ExamResult.vue'),
    meta: { title: '考试结果' }
  },
  {
    path: '/exam/reservation/:paperId',
    name: 'ExamReservation',
    component: () => import('../views/exam/ExamReservation.vue'),
    meta: { title: '预约线下考试' }
  },
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: () => import('../views/exam/MyReservations.vue'),
    meta: { title: '我的预约' }
  },
  {
    path: '/course/reservation/:courseId',
    name: 'CourseReservation',
    component: () => import('../views/course/CourseReservation.vue'),
    meta: { title: '预约线下课程' }
  },
  {
    path: '/my-course-reservations',
    name: 'MyCourseReservations',
    component: () => import('../views/course/MyCourseReservations.vue'),
    meta: { title: '我的课程预约' }
  },
  {
    path: '/order/confirm/:courseId',
    name: 'OrderConfirm',
    component: () => import('../views/order/OrderConfirm.vue'),
    meta: { title: '确认订单' }
  },
  {
    path: '/order/confirm-category/:categoryId',
    name: 'OrderConfirmCategory',
    component: () => import('../views/order/OrderConfirm.vue'),
    meta: { title: '购买分类' }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/order/OrderList.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: '/checkin/:courseId',
    name: 'OfflineCheckin',
    component: () => import('../views/course/OfflineCheckin.vue'),
    meta: { title: '线下打卡' }
  },
  {
    path: '/news',
    name: 'NewsList',
    component: () => import('../views/news/NewsList.vue'),
    meta: { title: '最新资讯', noAuth: true }
  },
  {
    path: '/news/:id',
    name: 'NewsDetail',
    component: () => import('../views/news/NewsDetail.vue'),
    meta: { title: '资讯详情', noAuth: true }
  },
  {
    path: '/about',
    name: 'AboutUs',
    component: () => import('../views/user/AboutUs.vue'),
    meta: { title: '关于我们' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// Navigation guard
let approvalChecked = false
let currentApprovalStatus = null
let hasProfileFlag = false

router.beforeEach(async (to, from, next) => {
  const token = getToken()

  // 放行无需登录的页面
  if (to.meta.noAuth) {
    return next()
  }

  // 放行不需要拦截的页面
  if (to.path === '/login' || to.path === '/register-profile' || to.path === '/pending-approval') {
    if (to.path === '/login' && token) {
      // 已登录但去登录页，检查状态后决定
      if (!approvalChecked) {
        try {
          const res = await get('/user/check-status')
          currentApprovalStatus = res.data?.approvalStatus
          hasProfileFlag = res.data?.hasProfile
          approvalChecked = true
        } catch (e) { /* ignore */ }
      }
      if (currentApprovalStatus === 'PENDING') {
        if (hasProfileFlag) {
          return next('/pending-approval')
        }
        return next('/register-profile')
      }
      return next('/')
    }
    return next()
  }

  // 未登录 → 跳转登录
  if (!token) {
    return next('/login')
  }

  // 已登录，检查审核状态
  if (!approvalChecked) {
    try {
      const res = await get('/user/check-status')
      currentApprovalStatus = res.data?.approvalStatus
      hasProfileFlag = res.data?.hasProfile
      approvalChecked = true

      if (currentApprovalStatus === 'PENDING') {
        if (hasProfileFlag) {
          return next('/pending-approval')
        }
        return next('/register-profile')
      }
      if (currentApprovalStatus === 'REJECTED') {
        return next('/pending-approval')
      }
    } catch (e) {
      // 网络错误时放行
    }
  } else {
    if (currentApprovalStatus === 'PENDING') {
      if (hasProfileFlag) {
        return next('/pending-approval')
      }
      return next('/register-profile')
    }
    if (currentApprovalStatus === 'REJECTED') {
      return next('/pending-approval')
    }
  }

  next()
})

export default router
