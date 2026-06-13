<template>
  <div class="exam-start-page">
    <van-nav-bar title="考试确认" left-text="返回" left-arrow @click-left="$router.back()" />

    <div class="exam-hero">
      <div class="hero-icon">
        <van-icon name="certificate" size="40" color="#fff" />
      </div>
      <h1 class="hero-title">{{ exam.name }}</h1>
      <div class="hero-tags">
        <span class="hero-tag"><van-icon name="clock-o" size="14" /> {{ exam.duration }}分钟</span>
        <span class="hero-tag"><van-icon name="gold-coin-o" size="14" /> 满分{{ exam.totalScore }}分</span>
        <span class="hero-tag"><van-icon name="passed" size="14" /> 及格{{ exam.passScore }}分</span>
        <span class="hero-tag"><van-icon name="description" size="14" /> 共{{ exam.questionCount }}题</span>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">考试须知</div>
      <div class="rules-list">
        <div class="rule-item">
          <span class="rule-num">1</span>
          <span>考试开始后计时开始，时间到自动交卷</span>
        </div>
        <div class="rule-item">
          <span class="rule-num">2</span>
          <span>切换窗口或后台3次以上将记为作弊</span>
        </div>
        <div class="rule-item">
          <span class="rule-num">3</span>
          <span>确认答案后点击下一题即保存，交卷前可以检查修改</span>
        </div>
        <div class="rule-item">
          <span class="rule-num">4</span>
          <span>考试期间请保持网络畅通</span>
        </div>
      </div>
    </div>

    <div class="exam-actions">
      <van-button type="primary" block round size="large" @click="startExam" class="start-btn">
        开始考试
      </van-button>
      <p class="start-tip">请认真作答，祝您取得好成绩</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const paperId = route.params.paperId

const exam = ref({})

async function fetchExamInfo() {
  try {
    const res = await get('/exams/' + paperId)
    if (res.data) exam.value = res.data
  } catch (e) {
    exam.value = {
      id: paperId,
      name: 'Java基础考试',
      duration: 60,
      totalScore: 100,
      passScore: 60,
      questionCount: 20
    }
  }
}

async function startExam() {
  try {
    const res = await post('/exam/start', { paperId: parseInt(paperId) })
    if (res.data?.recordId) {
      router.push('/exam/do/' + res.data.recordId)
    } else {
      showToast('开始考试失败，请重试')
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e.message || '开始考试失败'
    showToast(msg)
  }
}

onMounted(() => {
  fetchExamInfo()
})
</script>

<style scoped>
.exam-start-page {
  background: var(--bg-color);
  min-height: 100vh;
}

.exam-hero {
  background: linear-gradient(160deg, var(--primary-dark), var(--primary), var(--primary-light));
  padding-top: 24px; padding-left: 20px; padding-right: 20px; padding-bottom: 28px;
  text-align: center;
  border-radius: 0 0 24px 24px;
}

.hero-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.hero-tag {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.12);
  padding: 5px 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  margin: 12px;
  padding: 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.rule-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e8f4ff;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  margin-top: 1px;
}

.exam-actions {
  padding: 24px 16px;
}

.start-btn {
  height: 48px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  box-shadow: 0 4px 14px rgba(79, 110, 247, 0.4);
}

.start-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 14px;
}
</style>
