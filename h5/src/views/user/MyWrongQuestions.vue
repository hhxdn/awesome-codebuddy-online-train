<template>
  <div class="my-wrong-page">
    <van-nav-bar title="我的错题" left-text="返回" left-arrow @click-left="$router.back()">
      <template #right>
        <van-button size="small" round plain type="danger" @click="clearAll">清空全部</van-button>
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-for="group in groupedQuestions" :key="group.courseName" class="wrong-group">
        <div class="group-header">
          <van-icon name="bookmark-o" size="16" color="var(--primary)" />
          <span class="group-title">{{ group.courseName }}</span>
          <span class="group-count">{{ group.questions.length }}题</span>
        </div>
        <div class="group-cards">
          <div v-for="q in group.questions" :key="q.id" class="wrong-card" @click="redoQuestion(q)">
            <div class="wrong-content text-ellipsis-2">{{ q.content }}</div>
            <div class="wrong-footer">
              <van-tag type="danger" size="small" round>错题</van-tag>
              <span v-if="q.wrongCount" class="wrong-count">错了{{ q.wrongCount }}次</span>
              <van-icon name="replay" size="14" color="var(--primary)" class="redo-icon" />
            </div>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <EmptyState v-if="!refreshing && groupedQuestions.length === 0" description="暂无错题，继续保持！" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { get, del } from '../../api'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const wrongQuestions = ref([])
const refreshing = ref(false)

const groupedQuestions = computed(() => {
  const groups = {}
  wrongQuestions.value.forEach(q => {
    const courseName = q.courseName || '未知课程'
    if (!groups[courseName]) groups[courseName] = []
    groups[courseName].push(q)
  })
  return Object.keys(groups).map(name => ({
    courseName: name,
    questions: groups[name]
  }))
})

async function fetchWrongQuestions() {
  try {
    const res = await get('/user/wrong-questions')
    if (res.data) wrongQuestions.value = res.data.records || res.data || []
  } catch (e) {
    wrongQuestions.value = [
      { id: 1, courseName: 'Spring Boot实战', content: 'Spring Boot的默认配置文件是什么？', wrongCount: 2, chapterId: 1 },
      { id: 2, courseName: 'Spring Boot实战', content: '@SpringBootApplication注解包含哪些注解？', wrongCount: 1, chapterId: 1 }
    ]
  }
}

function redoQuestion(q) {
  router.push('/practice/' + q.chapterId + '/do')
}

function clearAll() {
  showConfirmDialog({
    title: '清空错题',
    message: '确定清空所有错题记录吗？此操作不可恢复。'
  }).then(async () => {
    try {
      await del('/user/wrong-questions')
    } catch (e) {}
    wrongQuestions.value = []
    showToast('已清空')
  }).catch(() => {})
}

function onRefresh() {
  refreshing.value = true
  fetchWrongQuestions().finally(() => { refreshing.value = false })
}

onMounted(() => {
  fetchWrongQuestions()
})
</script>

<style scoped>
.my-wrong-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.wrong-group {
  margin: 12px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
  flex: 1;
}

.group-count {
  font-size: 12px;
  color: var(--text-muted);
}

.group-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.wrong-card {
  background: #fff;
  padding: 14px 16px;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.15s;
}

.wrong-card:active {
  transform: scale(0.98);
}

.wrong-content {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
  color: var(--text-color);
}

.wrong-footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wrong-count {
  font-size: 12px;
  color: var(--text-muted);
}

.redo-icon {
  margin-left: auto;
}
</style>
