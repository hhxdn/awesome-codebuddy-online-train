<template>
  <div class="my-wrong-page">
    <van-nav-bar title="我的错题" left-text="返回" left-arrow @click-left="$router.back()">
      <template #right>
        <van-button size="small" type="danger" plain @click="clearAll">清空全部</van-button>
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-for="group in groupedQuestions" :key="group.courseName" class="wrong-group">
        <div class="group-title">{{ group.courseName }}</div>
        <div v-for="q in group.questions" :key="q.id" class="wrong-item" @click="redoQuestion(q)">
          <div class="wrong-content">{{ q.content }}</div>
          <div class="wrong-meta">
            <van-tag type="danger" size="small">错题</van-tag>
            <span v-if="q.wrongCount">错{{ q.wrongCount }}次</span>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <EmptyState v-if="!refreshing && groupedQuestions.length === 0" description="暂无错题" />
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
      { id: 1, courseName: 'Spring Boot实战', content: 'Spring Boot的默认配置文件是什么？', wrongCount: 2 },
      { id: 2, courseName: 'Spring Boot实战', content: '@SpringBootApplication注解包含哪些注解？', wrongCount: 1 }
    ]
  }
}

function redoQuestion(q) {
  router.push('/practice/' + q.chapterId + '/do')
}

function clearAll() {
  showConfirmDialog({
    title: '清空错题',
    message: '确定清空所有错题记录吗？'
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

.group-title {
  font-size: 15px;
  font-weight: 600;
  padding: 8px 4px;
  color: var(--text-color);
}

.wrong-item {
  background: #fff;
  padding: 14px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.wrong-content {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.wrong-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
