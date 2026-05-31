import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: '/categories',
        name: 'CategoryList',
        component: () => import('@/views/category/CategoryList.vue'),
        meta: { title: '课程分类' }
      },
      {
        path: '/courses',
        name: 'CourseList',
        component: () => import('@/views/course/CourseList.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: '/courses/edit/:id?',
        name: 'CourseEdit',
        component: () => import('@/views/course/CourseEdit.vue'),
        meta: { title: '课程编辑' }
      },
      {
        path: '/banners',
        name: 'BannerList',
        component: () => import('@/views/content/BannerList.vue'),
        meta: { title: 'Banner管理' }
      },
      {
        path: '/news',
        name: 'NewsList',
        component: () => import('@/views/content/NewsList.vue'),
        meta: { title: '新闻资讯' }
      },
      {
        path: '/questions',
        name: 'QuestionList',
        component: () => import('@/views/question/QuestionList.vue'),
        meta: { title: '题库管理' }
      },
      {
        path: '/questions/import',
        name: 'QuestionImport',
        component: () => import('@/views/question/QuestionImport.vue'),
        meta: { title: '题目导入' }
      },
      {
        path: '/exams',
        name: 'ExamList',
        component: () => import('@/views/exam/ExamList.vue'),
        meta: { title: '试卷管理' }
      },
      {
        path: '/exams/edit/:id?',
        name: 'ExamEdit',
        component: () => import('@/views/exam/ExamEdit.vue'),
        meta: { title: '试卷编辑' }
      },
      {
        path: '/exams/records',
        name: 'ExamRecord',
        component: () => import('@/views/exam/ExamRecord.vue'),
        meta: { title: '考试记录' }
      },
      {
        path: '/students',
        name: 'StudentList',
        component: () => import('@/views/student/StudentList.vue'),
        meta: { title: '学员管理' }
      },
      {
        path: '/students/:id',
        name: 'StudentDetail',
        component: () => import('@/views/student/StudentDetail.vue'),
        meta: { title: '学员详情' }
      },
      {
        path: '/orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: '/statistics/revenue',
        name: 'RevenueStats',
        component: () => import('@/views/statistics/RevenueStats.vue'),
        meta: { title: '营收统计' }
      },
      {
        path: '/statistics/learning',
        name: 'LearningStats',
        component: () => import('@/views/statistics/LearningStats.vue'),
        meta: { title: '学情统计' }
      },
      {
        path: '/statistics/exam',
        name: 'ExamStats',
        component: () => import('@/views/statistics/ExamStats.vue'),
        meta: { title: '考试统计' }
      },
      {
        path: '/reservations',
        name: 'ReservationList',
        component: () => import('@/views/exam/ReservationList.vue'),
        meta: { title: '考试预约' }
      },
      {
        path: '/course-reservations',
        name: 'CourseReservationList',
        component: () => import('@/views/course/CourseReservationList.vue'),
        meta: { title: '课程预约' }
      },
      {
        path: '/checkins',
        name: 'OfflineCheckinList',
        component: () => import('@/views/checkin/OfflineCheckinList.vue'),
        meta: { title: '线下打卡' }
      },
      {
        path: '/certificates',
        name: 'CertificateList',
        component: () => import('@/views/certificate/CertificateList.vue'),
        meta: { title: '结业证书' }
      },
      {
        path: '/exams/random',
        name: 'RandomExam',
        component: () => import('@/views/exam/RandomExam.vue'),
        meta: { title: '随机组卷' }
      },
      {
        path: '/system/users',
        name: 'SystemUserList',
        component: () => import('@/views/system/SystemUserList.vue'),
        meta: { title: '系统用户管理' }
      },
      {
        path: '/system/roles',
        name: 'RoleManagement',
        component: () => import('@/views/system/RoleManagement.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: '/system/menus',
        name: 'MenuManagement',
        component: () => import('@/views/system/MenuManagement.vue'),
        meta: { title: '菜单管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
