<template>
  <div class="reservation-page page-fade-in">
    <van-nav-bar title="预约线下课程" left-text="返回" left-arrow @click-left="$router.back()" :border="false" />

    <div v-if="loading" class="loading-wrap">
      <van-loading size="32" text="加载中..." vertical />
    </div>

    <template v-else-if="course">
      <div class="course-info-card">
        <div class="course-cover">
          <img v-if="course.cover" :src="course.cover" alt="cover" />
          <div v-else class="cover-placeholder">
            <van-icon name="bookmark-o" size="32" color="#fff" />
          </div>
        </div>
        <h2 class="course-title">{{ course.title }}</h2>
        <div class="course-desc">{{ course.description || '暂无介绍' }}</div>
        <div class="course-notice">
          <van-icon name="info-o" size="14" />
          <span>此为线下课程，需预约后到场参加，签到由GPS打卡完成</span>
        </div>
      </div>

      <div v-if="myReservation && myReservation.status !== 'CANCELLED'" class="reservation-status-card">
        <div class="status-header">
          <span class="status-dot" :class="'dot-' + myReservation.status.toLowerCase()"></span>
          <span class="status-label">{{ statusLabel(myReservation.status) }}</span>
        </div>
        <div class="status-detail" v-if="myReservation.reservationTime">
          <span>预约时间：{{ myReservation.reservationTime }}</span>
        </div>
        <div class="status-detail">
          <span>提交时间：{{ myReservation.createTime }}</span>
        </div>
        <div v-if="myReservation.status === 'PENDING'" style="margin-top: 12px;">
          <van-button type="default" size="small" round @click="handleCancel">取消预约</van-button>
        </div>
      </div>

      <div v-else class="reservation-form">
        <van-cell-group inset title="预约信息">
          <van-field
            v-model="form.reservationTime"
            readonly
            clickable
            label="预约时间"
            placeholder="选择上课时间（选填）"
            @click="showDatetimePicker = true"
            right-icon="clock-o"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            placeholder="如有特殊需求可在此说明（选填）"
            type="textarea"
            rows="2"
            autosize
          />
        </van-cell-group>

        <div style="padding: 24px 16px;">
          <van-button type="primary" block round :loading="submitting" @click="handleReserve">
            立即预约
          </van-button>
        </div>
      </div>
    </template>

    <div v-else class="empty-wrap">
      <van-empty description="课程信息不存在" />
    </div>

    <van-popup v-model:show="showDatetimePicker" position="bottom" round>
      <van-date-picker
        v-model="currentDate"
        type="datetime"
        title="选择预约时间"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onConfirmTime"
        @cancel="showDatetimePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put } from '../../api'
import { showToast, showDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId

const loading = ref(true)
const submitting = ref(false)
const course = ref(null)
const myReservation = ref(null)
const showDatetimePicker = ref(false)
const currentDate = ref([new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate(), new Date().getHours(), new Date().getMinutes()])

const minDate = new Date()
const maxDate = new Date()
maxDate.setMonth(maxDate.getMonth() + 3)

const form = reactive({
  reservationTime: '',
  remark: ''
})

function statusLabel(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
  return map[status] || status
}

function onConfirmTime({ selectedValues }) {
  const [year, month, day, hour, minute] = selectedValues
  form.reservationTime = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  showDatetimePicker.value = false
}

async function fetchCourse() {
  try {
    const res = await get(`/courses/${courseId}`)
    course.value = res.data
  } catch {
    course.value = null
  }
}

async function fetchMyReservation() {
  try {
    const res = await get('/course/reservations/my')
    const list = res.data || []
    myReservation.value = list.find(r => r.courseId === Number(courseId)) || null
  } catch {
    myReservation.value = null
  }
}

async function handleReserve() {
  submitting.value = true
  try {
    const params = { courseId: Number(courseId) }
    if (form.reservationTime) {
      params.reservationTime = form.reservationTime + ':00'
    }
    if (form.remark) {
      params.remark = form.remark
    }
    const res = await post('/course/reservations', params)
    showToast(res.data?.message || '预约成功')
    await fetchMyReservation()
  } catch (e) {
    showToast(e.response?.data?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

async function handleCancel() {
  try {
    await showDialog({
      title: '取消预约',
      message: '确定要取消本次预约吗？',
      showCancelButton: true
    })
    await put(`/course/reservations/${myReservation.value.id}/cancel`)
    showToast('预约已取消')
    myReservation.value.status = 'CANCELLED'
  } catch {
    // cancelled
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([fetchCourse(), fetchMyReservation()])
  loading.value = false
})
</script>

<style scoped>
.reservation-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding-top: 120px;
}

.course-info-card {
  background: #fff;
  margin: 12px 14px;
  padding: 24px 18px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.course-cover {
  width: 100%;
  height: 160px;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-title {
  font-size: 18px; font-weight: 600;
  color: var(--text-color);
  margin-bottom: 8px;
}

.course-desc {
  font-size: 13px; color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  text-align: left;
}

.course-notice {
  background: #FFF3E0;
  border-radius: 8px; padding: 10px 14px;
  font-size: 13px; color: #E37318;
  display: flex; align-items: flex-start; gap: 6px;
  text-align: left;
}

.reservation-status-card {
  background: #fff;
  margin: 12px 14px;
  padding: 18px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.status-header {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 10px;
}

.status-dot {
  width: 8px; height: 8px; border-radius: 50%;
}
.dot-pENDING { background: #E6A23C; }
.dot-confirmed { background: #67C23A; }
.dot-completed { background: #409EFF; }
.dot-cancelled { background: #C0C4CC; }

.status-label {
  font-size: 16px; font-weight: 600;
  color: var(--text-color);
}

.status-detail {
  font-size: 13px; color: var(--text-secondary);
  margin-top: 4px;
}

.reservation-form {
  margin-top: 12px;
}

.empty-wrap {
  padding-top: 80px;
}
</style>
