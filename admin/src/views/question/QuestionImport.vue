<template>
  <div class="page-container" v-loading="loading">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>批量导入题目</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form label-width="100px">
        <el-form-item label="选择课程">
          <el-select v-model="courseId" placeholder="请选择课程" style="width: 300px;" @change="onCourseChange">
            <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择章节">
          <el-select v-model="chapterId" placeholder="请选择章节" style="width: 300px;" :disabled="!courseId">
            <el-option v-for="ch in chapters" :key="ch.id" :label="ch.title" :value="ch.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="import-actions">
        <el-button type="primary" @click="downloadTemplate">下载Excel模板</el-button>
        <el-upload
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
          class="upload-inline"
        >
          <el-button type="success">选择Excel文件</el-button>
        </el-upload>
        <el-upload
          :auto-upload="false"
          :on-change="handleWordFileChange"
          :limit="1"
          accept=".docx"
          class="upload-inline"
        >
          <el-button type="warning">选择Word文件</el-button>
        </el-upload>
        <el-tag v-if="importMode" size="small" effect="dark" type="warning">
          {{ importMode === 'excel' ? 'Excel模式' : 'Word模式' }}
        </el-tag>
      </div>

      <el-alert
        title="导入说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px;"
      >
        <div><strong>Excel导入：</strong>题型(SINGLE/MULTIPLE/JUDGE/ESSAY)、题目内容、分值、选项A-H及是否正确、正确答案、解析</div>
        <div><strong>Word导入：</strong>支持以下格式（题目间空行分隔）：</div>
        <div style="background:#f5f7fa;padding:8px 12px;margin-top:4px;border-radius:4px;font-family:monospace;white-space:pre-wrap;line-height:1.6;">10. (知识点) 题目内容？（ ）
A. 选项A
B. 选项B
答案：ABC
解析：解析内容
难度：困难</div>
      </el-alert>

      <div v-if="previewData.length > 0" class="card-container">
        <div class="card-title">
          预览数据（共 {{ previewData.length }} 条）
          <el-button type="primary" size="small" style="float: right;" :loading="importing" @click="handleImport">确认导入</el-button>
        </div>
        <el-table :data="previewData" border stripe max-height="400">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="题型" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="tagType(row.type)">{{ typeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="score" label="分值" width="60" />
          <el-table-column label="选项" width="80">
            <template #default="{ row }">
              <span v-if="row.options && row.options.length > 0">
                {{ row.options.length }}个选项
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="答案" width="80" show-overflow-tooltip>
            <template #default="{ row }">
              {{ answerLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="70">
            <template #default="{ row }">
              <el-tag v-if="row.difficulty" size="small" :type="difficultyTag(row.difficulty)">{{ row.difficulty }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" fixed="right">
            <template #default="{ row, $index }">
              <el-button type="primary" link size="small" @click="previewQuestion(row, $index)">预览</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 题目预览弹窗 -->
    <el-dialog v-model="previewVisible" title="题目预览" width="650px" destroy-on-close>
      <template v-if="previewItem">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="题型">
            <el-tag size="small" :type="tagType(previewItem.type)">{{ typeLabel(previewItem.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分值">{{ previewItem.score }}</el-descriptions-item>
          <el-descriptions-item label="难度" v-if="previewItem.difficulty">
            <el-tag size="small" :type="difficultyTag(previewItem.difficulty)">{{ previewItem.difficulty }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="题目内容">{{ previewItem.content }}</el-descriptions-item>
          <el-descriptions-item label="选项" v-if="previewItem.options && previewItem.options.length > 0">
            <div v-for="opt in previewItem.options" :key="opt.optionLabel" style="margin-bottom: 4px;">
              <el-tag size="small" :type="opt.isCorrect ? 'success' : 'info'" style="margin-right: 6px;">{{ opt.optionLabel }}</el-tag>
              {{ opt.content }}
              <el-tag v-if="opt.isCorrect" size="small" type="success" style="margin-left: 6px;">正确</el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="答案">{{ answerLabel(previewItem) }}</el-descriptions-item>
          <el-descriptions-item label="解析" v-if="previewItem.analysis">{{ previewItem.analysis }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '@/api'
import * as XLSX from 'xlsx'

const loading = ref(false)
const importing = ref(false)
const courses = ref([])
const chapters = ref([])
const courseId = ref(null)
const chapterId = ref(null)
const previewData = ref([])
const importMode = ref('')  // 'excel' or 'word'
const previewVisible = ref(false)
const previewItem = ref(null)

const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

function typeLabel(type) {
  const map = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', ESSAY: '简答' }
  return map[type] || type
}

function tagType(type) {
  const map = { SINGLE: '', MULTIPLE: 'success', JUDGE: 'warning', ESSAY: 'danger' }
  return map[type] || 'info'
}

function difficultyTag(d) {
  const map = { '简单': 'success', '中等': 'warning', '困难': 'danger' }
  return map[d] || 'info'
}

function answerLabel(row) {
  if (row.type === 'JUDGE') {
    return row.answer === true || row.answer === 'true' ? '正确' : '错误'
  }
  if (row.answer === true) return '正确'
  if (row.answer === false) return '错误'
  return row.answer || '-'
}

function previewQuestion(row, index) {
  previewItem.value = { ...row }
  previewVisible.value = true
}

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { size: 999 })
    courses.value = res.data?.records || res.data?.list || []
  } catch { courses.value = [] }
}

async function onCourseChange(courseId) {
  chapterId.value = null
  chapters.value = []
  if (courseId) {
    try {
      const res = await get(`/admin/courses/${courseId}`)
      chapters.value = res.data?.chapters || []
    } catch { chapters.value = [] }
  }
}

function downloadTemplate() {
  const headers = ['题型', '题目内容', '分值', '选项A', 'A是否正确', '选项B', 'B是否正确', '选项C', 'C是否正确', '选项D', 'D是否正确', '正确答案', '解析']
  const example = ['SINGLE', '以下哪个是编程语言？', '5', 'Java', '是', 'Python', '否', 'Excel', '否', 'Word', '否', '', 'Java是Sun公司推出的编程语言']
  const ws = XLSX.utils.aoa_to_sheet([headers, example])
  ws['!cols'] = headers.map(() => ({ wch: 20 }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '题目导入模板')
  XLSX.writeFile(wb, 'question_import_template.xlsx')
  ElMessage.success('模板下载成功')
}

function handleFileChange(file) {
  importMode.value = 'excel'
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const wb = XLSX.read(data, { type: 'array' })
      const sheet = wb.Sheets[wb.SheetNames[0]]
      const rows = XLSX.utils.sheet_to_json(sheet, { header: 1 })
      if (rows.length < 2) {
        ElMessage.warning('文件内容为空')
        return
      }
      // Skip header
      previewData.value = rows.slice(1).map(row => {
        const type = (row[0] || 'SINGLE').toUpperCase()
        const item = {
          type,
          content: row[1] || '',
          score: Number(row[2]) || 5,
          analysis: row[12] || ''
        }

        if (type === 'JUDGE') {
          item.answer = String(row[11]) === '正确' || String(row[11]) === 'true' || String(row[11]) === 'TRUE'
        } else if (type === 'ESSAY') {
          item.answer = row[11] || ''
        } else {
          item.options = []
          for (let i = 0; i < 8; i++) {
            const content = row[3 + i * 2]
            const isCorrect = row[4 + i * 2]
            if (content) {
              item.options.push({
                optionLabel: optionLabels[i],
                content: String(content),
                isCorrect: String(isCorrect) === '是' || String(isCorrect) === 'true' || String(isCorrect) === 'TRUE'
              })
            }
          }
        }
        return item
      }).filter(item => item.content)
      ElMessage.success(`解析成功，共 ${previewData.value.length} 条数据`)
    } catch (err) {
      ElMessage.error('文件解析失败')
    }
  }
  reader.readAsArrayBuffer(file.raw)
}

async function handleWordFileChange(file) {
  importMode.value = 'word'
  if (!courseId.value || !chapterId.value) {
    ElMessage.warning('Word导入需要先选择课程和章节')
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    if (courseId.value) formData.append('courseId', courseId.value)
    if (chapterId.value) formData.append('chapterId', chapterId.value)
    formData.append('previewOnly', 'true')

    const res = await post('/admin/questions/import-word', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const data = res.data
    previewData.value = (data.preview || []).map(item => ({
      type: item.type,
      content: item.content,
      score: item.score || 5,
      answer: item.answer,
      analysis: item.analysis,
      difficulty: item.difficulty,
      options: (item.options || []).map(opt => ({
        optionLabel: opt.label,
        content: opt.content,
        isCorrect: item.answer && item.answer.includes(opt.label)
      }))
    }))
    ElMessage.success(`解析成功，共 ${previewData.value.length} 条数据`)
  } catch {
    previewData.value = []
    ElMessage.error('Word文件解析失败')
  } finally {
    loading.value = false
  }
}

async function handleImport() {
  if (!courseId.value || !chapterId.value) {
    ElMessage.warning('请先选择课程和章节')
    return
  }
  importing.value = true
  try {
    if (importMode.value === 'word') {
      // Word模式：直接调用导入接口（已在preview时上传解析过，这里用batch方式回传）
      const data = previewData.value.map(item => ({
        ...item,
        courseId: courseId.value,
        chapterId: chapterId.value
      }))
      await post('/admin/questions/batch', { questions: data })
    } else {
      // Excel模式
      const data = previewData.value.map(item => ({
        ...item,
        courseId: courseId.value,
        chapterId: chapterId.value
      }))
      await post('/admin/questions/batch', { questions: data })
    }
    ElMessage.success(`成功导入 ${previewData.value.length} 道题目`)
    previewData.value = []
    importMode.value = ''
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.import-actions {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.upload-inline {
  display: inline-flex;
  align-items: center;
}
.upload-inline :deep(.el-upload) {
  display: inline-flex;
  align-items: center;
}
</style>
