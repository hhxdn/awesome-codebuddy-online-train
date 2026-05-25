import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue'),
    meta: { title: '登录' }
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
    path: '/order/confirm/:courseId',
    name: 'OrderConfirm',
    component: () => import('../views/order/OrderConfirm.vue'),
    meta: { title: '确认订单' }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/order/OrderList.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: '/my-courses',
    name: 'MyCourses',
    component: () => import('../views/user/MyCourses.vue'),
    meta: { title: '我的课程' }
  },
  {
    path: '/my-orders',
    name: 'MyOrders',
    component: () => import('../views/order/OrderList.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: '/my-wrong',
    name: 'MyWrongQuestions',
    component: () => import('../views/user/MyWrongQuestions.vue'),
    meta: { title: '我的错题' }
  },
  {
    path: '/my-exams',
    name: 'MyExamRecords',
    component: () => import('../views/user/MyExamRecords.vue'),
    meta: { title: '考试记录' }
  },
  {
    path: '/my-learning',
    name: 'MyLearningRecords',
    component: () => import('../views/user/MyLearningRecords.vue'),
    meta: { title: '学习记录' }
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
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
