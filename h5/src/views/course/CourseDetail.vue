<template>
  <div class="course-detail-page page-fade-in">
    <!-- Top Banner -->
    <div class="detail-banner">
      <van-nav-bar
        left-arrow
        @click-left="$router.back()"
        :style="{ backgroundColor: 'transparent' }"
      >
        <template #title>
          <span style="color:#fff;font-weight:600;font-size:17px;">课程详情</span>
        </template>
      </van-nav-bar>
      <div class="banner-body">
        <h1 class="banner-title">{{ course.title }}</h1>
        <div class="banner-tags">
          <span class="banner-tag" :class="(course.price || 0) === 0 ? 'tag-free' : 'tag-paid'">
            {{ (course.price || 0) === 0 ? '免费' : '¥' + course.price }}
          </span>
          <span class="banner-tag tag-cat">{{ course.categoryPath || course.categoryName }}</span>
          <span v-if="course.courseType === 'OFFLINE'" class="banner-tag tag-offline">线下课程</span>
        </div>
        <div class="banner-stats">
          <span>{{ course.studentCount || 0 }}人在学</span>
        </div>
      </div>
    </div>

    <!-- Description -->
    <div class="section-card">
      <div class="section-title">课程介绍</div>
      <div class="desc-text" :class="{ expanded: descExpanded }">
        {{ course.description || '暂无介绍' }}
      </div>
      <div
        v-if="(course.description || '').length > 120"
        class="desc-toggle"
        @click="descExpanded = !descExpanded"
      >
        {{ descExpanded ? '收起' : '展开全部' }}
        <van-icon :name="descExpanded ? 'arrow-up' : 'arrow-down'" size="14" />
      </div>
    </div>

    <!-- Chapters -->
    <div class="section-card">
      <div class="section-title">
        课程目录
        <span class="chapter-total">共{{ chapters.length }}节</span>
      </div>
      <div v-if="chapters.length > 0">
        <div
          v-for="(ch, i) in chapters"
          :key="ch.id"
          class="chapter-item"
          @click="goChapter(ch)"
        >
          <div class="chapter-num">{{ String(i + 1).padStart(2, '0') }}</div>
          <div class="chapter-info">
            <div class="chapter-name text-ellipsis-2">{{ ch.title }}</div>
            <div class="chapter-bottom">
              <span class="chapter-dur">
                <van-icon name="play-circle-o" size="13" /> {{ ch.duration || '视频' }}
              </span>
              <span v-if="ch.completed" class="chapter-done">
                <van-icon name="success" size="13" color="#00A870" /> 已学完
              </span>
            </div>
          </div>
          <div class="chapter-right">
            <van-button
              v-if="(!isPaid || purchased) && hasExerciseAccess"
              size="small"
              round
              plain
              type="primary"
              @click.stop="goPractice(ch)"
            >练习</van-button>
            <van-icon v-else name="lock" color="#C9CDD4" size="16" />
          </div>
        </div>
      </div>
      <EmptyState v-else description="暂无章节" />
    </div>

    <!-- Bottom Bar -->
    <div v-if="accessChecked" class="bottom-bar safe-bottom">
      <!-- 线下课程：预约 + 打卡按钮 -->
      <template v-if="course.courseType === 'OFFLINE'">
        <div v-if="!checkedIn" style="display: flex; gap: 10px; width: 100%;">
          <van-button v-if="!reservationInfo || reservationInfo.status === 'CANCELLED'" type="warning" block round size="large" @click="goReserve" class="bottom-btn reserve-btn" style="flex: 1;">
            <van-icon name="bookmark-o" size="18" />
            预约课程
          </van-button>
          <van-button v-else-if="reservationInfo.status === 'PENDING'" type="warning" block round size="large" disabled style="flex: 1;">
            <van-icon name="clock-o" size="18" />
            预约待确认
          </van-button>
          <van-button v-else-if="reservationInfo.status === 'CONFIRMED'" type="warning" block round size="large" @click="goCheckin" class="bottom-btn checkin-btn" style="flex: 1;">
            <van-icon name="location-o" size="18" />
            线下打卡
          </van-button>
        </div>
        <van-button v-else type="success" block round size="large" disabled class="bottom-btn">
          <van-icon name="success" size="18" />
          已打卡
        </van-button>
      </template>
      <!-- 线上课程 -->
      <template v-else-if="(course.price || 0) === 0">
        <van-button type="primary" block round size="large" @click="startLearn" class="bottom-btn">
          开始学习
        </van-button>
      </template>
      <template v-else-if="!purchased">
        <div class="bottom-left">
          <span class="price-label">价格</span>
          <span class="price-value">¥{{ course.price }}</span>
        </div>
        <van-button type="primary" round @click="buyNow" class="bottom-btn-half">购买课程</van-button>
        <van-button v-if="belongCategory" type="warning" round plain @click="buyCategory" class="bottom-btn-half">购买分类 ¥{{ belongCategory.price }}</van-button>
      </template>
      <template v-else>
        <van-button type="primary" block round size="large" @click="startLearn" class="bottom-btn">
          继续学习
        </van-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const courseId = route.params.id
const course = ref({})
const chapters = ref([])
const purchased = ref(false)
const accessChecked = ref(false)
const isPaid = ref(false)
const descExpanded = ref(false)
const checkedIn = ref(false)
const reservationInfo = ref(null)
const belongCategory = ref(null)
const hasExerciseAccess = ref(false)

let _allCats = null

async function fetchAllCategories() {
  if (_allCats) return _allCats
  try {
    const res = await get('/categories')
    _allCats = res.data || []
    return _allCats
  } catch (e) { return [] }
}

function buildCategoryPath(allCats, catId, path = []) {
  const cat = allCats.find(c => c.id === catId)
  if (!cat) return path
  path.unshift(cat.name)
  if (cat.parentId) {
    return buildCategoryPath(allCats, cat.parentId, path)
  }
  return path
}

function findPricedAncestor(allCats, parentId) {
  const parent = allCats.find(c => c.id === parentId)
  if (!parent) return null
  if (parent.price > 0 || parent.isFree === 0) return parent
  if (parent.parentId) return findPricedAncestor(allCats, parent.parentId)
  return null
}

async function fetchDetail() {
  try {
    const res = await get('/courses/' + courseId)
    if (res.data) course.value = res.data
    isPaid.value = (course.value.price || 0) > 0
    // 获取课程所属分类的完整路径
    if (course.value.categoryId) {
      try {
        const allCats = await fetchAllCategories()
        const path = buildCategoryPath(allCats, course.value.categoryId)
        course.value.categoryPath = path.join(' > ')
        // 查找付费祖先分类
        const cat = allCats.find(c => c.id === course.value.categoryId)
        if (cat && cat.parentId != null) {
          const ancestor = findPricedAncestor(allCats, cat.parentId)
          belongCategory.value = ancestor || (cat.price > 0 || cat.isFree === 0 ? cat : null)
        }
      } catch (e) {}
    }
  } catch (e) {
    course.value = { id: courseId, title: '加载中...', price: 0, categoryName: '', studentCount: 0, description: '' }
  }
  try {
    const res = await get('/courses/' + courseId + '/chapters')
    if (res.data) chapters.value = res.data
  } catch (e) { chapters.value = [] }
  try {
    const res = await get('/courses/' + courseId + '/exercise-access')
    if (res.data) hasExerciseAccess.value = res.data.hasExerciseAccess || false
  } catch (e) { hasExerciseAccess.value = false }
  try {
    const res = await get('/courses/' + courseId + '/access')
    if (res.data) purchased.value = res.data.accessible
  } catch (e) { purchased.value = false }
  accessChecked.value = true
  if (course.value.courseType === 'OFFLINE') {
    try {
      const res = await get('/checkin/status/' + courseId)
      if (res.data) checkedIn.value = res.data.checkedIn
    } catch (e) { checkedIn.value = false }
    try {
      const res = await get('/course/reservations/status/' + courseId)
      if (res.data) reservationInfo.value = res.data
    } catch (e) { reservationInfo.value = null }
  }
}

function goChapter(ch) {
  if (isPaid.value && !purchased.value) { showToast('请先购买课程'); return }
  router.push('/video/' + ch.id)
}
function goPractice(ch) {
  // 未购买付费课程 → 跳转报名学习
  if (isPaid.value && !purchased.value) {
    router.push('/enroll')
    return
  }
  // 已购买但无练习权限 → 醒目提示
  if (!hasExerciseAccess.value) {
    showDialog({
      title: '暂无练习权限',
      message: '您还没有开通该课程的练习题权限。\n\n请联系管理员为您开通练习权限后再进入练习。',
      confirmButtonText: '我知道了',
      confirmButtonColor: '#0052D9',
      theme: 'round-button'
    })
    return
  }
  router.push('/practice/' + ch.id)
}
function startLearnOrEnroll() {
  // 免费课程也走报名学习流程
  router.push('/enroll')
}
function startLearn() {
  if (chapters.value.length > 0) router.push('/video/' + chapters.value[0].id)
  else showToast('暂无章节内容')
}
function buyNow() { router.push('/order/confirm/' + courseId) }
function buyCategory() {
  if (belongCategory.value) {
    router.push({ path: '/order/confirm-category/' + belongCategory.value.id })
  }
}
function goCheckin() { router.push('/checkin/' + courseId) }
function goReserve() { router.push('/course/reservation/' + courseId) }

onMounted(() => fetchDetail())
</script>

<style scoped>
.course-detail-page {
  background: var(--bg-color);
  min-height: 100vh;
  padding-bottom: 80px;
}

/* Banner */
.detail-banner {
  background: linear-gradient(160deg, #003CAB, #0052D9 50%, #366EF4 100%);
  padding-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.banner-body {
  padding-top: 4px; padding-left: 20px; padding-right: 20px; padding-bottom: 0;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
  margin-bottom: 12px;
}

.banner-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.banner-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 4px;
  backdrop-filter: blur(4px);
  color: #fff;
}

.tag-free { background: rgba(0, 168, 112, 0.3); }
.tag-paid { background: rgba(227, 77, 89, 0.3); }
.tag-cat { background: rgba(255, 255, 255, 0.12); }
.tag-offline { background: rgba(237, 123, 47, 0.35); }

.banner-stats {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

/* Description */
.desc-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  max-height: 80px;
  overflow: hidden;
  transition: max-height 0.35s ease;
}
.desc-text.expanded { max-height: none; }
.desc-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 10px;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
}
.desc-toggle:active { background: var(--primary-bg); }

/* Chapter */
.chapter-total {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 400;
  background: var(--bg-color);
  padding: 2px 10px;
  border-radius: 4px;
  margin-left: 4px;
}

.chapter-item {
  display: flex;
  align-items: center;
  padding: 13px 0;
  border-bottom: 1px solid var(--border-light);
  gap: 12px;
  cursor: pointer;
  transition: background var(--transition);
}
.chapter-item:last-child { border-bottom: none; }
.chapter-item:active { background: var(--bg-color); margin: 0 -18px; padding: 13px 18px; border-radius: 8px; }

.chapter-num {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--primary-bg);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  font-family: 'SF Mono', Menlo, monospace;
}

.chapter-info { flex: 1; min-width: 0; }
.chapter-name { font-size: 14px; color: var(--text-color); margin-bottom: 4px; }
.chapter-bottom { display: flex; align-items: center; gap: 12px; }
.chapter-dur { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
.chapter-done { font-size: 12px; color: #00A870; display: flex; align-items: center; gap: 3px; }
.chapter-right { flex-shrink: 0; }
.chapter-right :deep(.van-button) { font-size: 12px; height: 28px; }

/* Bottom Bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  background: #fff;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 -1px 12px rgba(0, 0, 0, 0.04);
  z-index: 100;
}
.bottom-btn {
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  background: var(--primary) !important;
  border: none !important;
}
.bottom-btn-half {
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  padding: 0 28px;
  background: var(--primary) !important;
  border: none !important;
}
.checkin-btn {
  background: linear-gradient(135deg, #ED7B2F, #E37318) !important;
  border: none !important;
}
.reserve-btn {
  background: linear-gradient(135deg, #FF9800, #F57C00) !important;
  border: none !important;
}
.bottom-left { flex: 1; }
.price-label { font-size: 12px; color: var(--text-muted); display: block; }
.price-value { font-size: 22px; font-weight: 700; color: var(--danger); }

:deep(.van-nav-bar) { background: transparent !important; }
:deep(.van-nav-bar .van-icon) { color: #fff !important; }
</style>
