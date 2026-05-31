<template>
  <div class="exam-list-page page-fade-in">
    <van-nav-bar title="考试列表" :border="false" />

    <!-- Category Bar: 多级分类 -->
    <div class="category-bar">
      <div class="category-scroll">
        <div class="cat-chip" :class="{ active: activeL1 === 0 }" @click="selectL1(0)">全部</div>
        <div v-for="cat in categories" :key="cat.id" class="cat-chip" :class="{ active: activeL1 === cat.id }" @click="selectL1(cat.id)">{{ cat.name }}</div>
      </div>
      <div v-if="activeL2List.length > 0" class="sub-category-bar">
        <div class="sub-cat-chip" :class="{ active: activeL2 === 0 }" @click="selectL2(0)">全部</div>
        <div v-for="sub in activeL2List" :key="sub.id" class="sub-cat-chip" :class="{ active: activeL2 === sub.id }" @click="selectL2(sub.id)">{{ sub.name }}</div>
      </div>
      <div v-if="activeL3List.length > 0" class="sub-category-bar level3-bar">
        <div class="sub-cat-chip" :class="{ active: activeL3 === 0 }" @click="selectL3(0)">全部</div>
        <div v-for="sub in activeL3List" :key="sub.id" class="sub-cat-chip" :class="{ active: activeL3 === sub.id }" @click="selectL3(sub.id)">{{ sub.name }}</div>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="examList.length > 0">
        <div v-for="exam in examList" :key="exam.id" class="exam-card" @click="goExam(exam)">
          <div class="exam-top">
            <div class="exam-icon" :class="{ offline: exam.examType === 'OFFLINE' }">
              <van-icon :name="exam.examType === 'OFFLINE' ? 'location-o' : 'certificate'" size="20" color="#fff" />
            </div>
            <div class="exam-main">
              <h3 class="exam-name">{{ exam.name }}</h3>
              <div style="display: flex; gap: 4px; flex-shrink: 0;">
                <span v-if="exam.examType === 'OFFLINE'" class="exam-tag offline-tag">线下</span>
                <span class="exam-status" :class="'s-' + getStatusType(exam)">{{ getStatusText(exam) }}</span>
              </div>
            </div>
          </div>
          <div class="exam-meta">
            <div class="meta-item"><van-icon name="clock-o" size="14" /> {{ exam.duration || 60 }}分钟</div>
            <div class="meta-item"><van-icon name="gold-coin-o" size="14" /> 满分{{ exam.totalScore || 100 }}分</div>
            <div class="meta-item"><van-icon name="passed" size="14" /> 及格{{ exam.passScore || 60 }}分</div>
          </div>
          <div class="exam-bottom">
            <span v-if="exam.questionCount">共{{ exam.questionCount }}题</span>
            <span v-else-if="exam.examType === 'OFFLINE'">线下考试</span>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span v-if="exam.examType === 'OFFLINE'" style="color: #E37318;">需预约参加</span>
              <van-icon name="arrow" size="14" />
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="!loading && !refreshing" class="empty-wrap">
        <EmptyState description="暂无考试" />
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId || route.params.id || ''

const examList = ref([])
const refreshing = ref(false)
const loading = ref(false)
const categories = ref([])
const activeL1 = ref(0)
const activeL2 = ref(0)
const activeL3 = ref(0)

const activeL2List = computed(() => {
  if (activeL1.value === 0) return []
  const cat = categories.value.find(c => c.id === activeL1.value)
  return cat?.children || []
})

const activeL3List = computed(() => {
  if (activeL2.value === 0) return []
  for (const cat of categories.value) {
    const l2 = cat.children?.find(c => c.id === activeL2.value)
    if (l2) return l2.children || []
  }
  return []
})

async function fetchCategories() {
  try {
    const res = await get('/categories/tree')
    if (res.data && res.data.length > 0) categories.value = res.data
  } catch (e) { categories.value = [] }
}

async function fetchExams() {
  loading.value = true
  try {
    const params = {}
    if (courseId) params.courseId = courseId
    const cid = activeL3.value > 0 ? activeL3.value : (activeL2.value > 0 ? activeL2.value : activeL1.value)
    if (cid > 0) params.categoryId = cid
    const res = await get('/exams', params)
    if (res.data) examList.value = res.data.records || res.data || []
  } catch (e) { examList.value = [] }
  loading.value = false
}

function selectL1(id) { activeL1.value = id; activeL2.value = 0; activeL3.value = 0; fetchExams() }
function selectL2(id) { activeL2.value = id; activeL3.value = 0; fetchExams() }
function selectL3(id) { activeL3.value = id; fetchExams() }

function getStatusType(exam) {
  if (exam.userScore >= exam.passScore) return 'pass'
  if (exam.userScore !== undefined) return 'fail'
  return 'new'
}
function getStatusText(exam) {
  if (exam.userScore !== undefined && exam.userScore >= exam.passScore) return '已通过'
  if (exam.userScore !== undefined) return '未通过'
  return '未参加'
}
function goExam(exam) {
  if (exam.examType === 'OFFLINE') {
    router.push('/exam/reservation/' + exam.id)
    return
  }
  router.push('/exam/start/' + exam.id)
}
function onRefresh() { refreshing.value = true; fetchExams().finally(() => { refreshing.value = false }) }

onMounted(() => { fetchCategories(); fetchExams() })
</script>

<style scoped>
.exam-list-page { background: var(--bg-color); min-height: 100vh; }

/* Category Bar */
.category-bar { background: #fff; position: sticky; top: 46px; z-index: 98; }
.category-scroll { display: flex; gap: 4px; padding: 8px 14px; overflow-x: auto; scrollbar-width: none; }
.category-scroll::-webkit-scrollbar { display: none; }
.cat-chip { flex-shrink: 0; padding: 6px 14px; border-radius: 6px; background: var(--bg-color); font-size: 13px; color: var(--text-secondary); cursor: pointer; transition: all var(--transition); white-space: nowrap; }
.cat-chip:active { transform: scale(0.96); }
.cat-chip.active { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
.sub-category-bar { display: flex; gap: 4px; padding: 0 14px 10px; overflow-x: auto; scrollbar-width: none; border-top: 1px solid var(--border-light); }
.level3-bar { background: #FAFBFC; }
.sub-category-bar::-webkit-scrollbar { display: none; }
.sub-cat-chip { flex-shrink: 0; padding: 4px 12px; border-radius: 14px; background: var(--bg-color); font-size: 12px; color: var(--text-secondary); cursor: pointer; white-space: nowrap; }
.sub-cat-chip.active { background: var(--primary-bg); color: var(--primary); font-weight: 500; }

.exam-card {
  background: #fff; margin: 8px 12px; padding: 18px;
  border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  cursor: pointer; transition: all var(--transition);
}
.exam-card:active { transform: scale(0.985); }

.exam-top { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.exam-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, #0052D9, #366EF4);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.exam-icon.offline {
  background: linear-gradient(135deg, #E37318, #FF9800);
}
.exam-tag {
  font-size: 10px; font-weight: 500; padding: 1px 6px; border-radius: 3px;
}
.offline-tag {
  background: #FFF3E0; color: #E37318; border: 1px solid #FFCC80;
}
.exam-main { flex: 1; display: flex; align-items: center; justify-content: space-between; min-width: 0; }
.exam-name { font-size: 16px; font-weight: 600; color: var(--text-color); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.exam-status { font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 4px; flex-shrink: 0; margin-left: 8px; }
.s-pass { background: var(--success-light); color: #00A870; }
.s-fail { background: var(--danger-light); color: #E34D59; }
.s-new { background: var(--primary-bg); color: var(--primary); }

.exam-meta { display: flex; gap: 20px; margin-bottom: 12px; }
.meta-item { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 4px; }

.exam-bottom {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 12px; border-top: 1px solid var(--border-light);
  font-size: 12px; color: var(--text-muted);
}
</style>
