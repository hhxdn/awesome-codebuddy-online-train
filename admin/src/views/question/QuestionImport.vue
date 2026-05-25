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

      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
        <el-upload
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
          style="display: inline-block; margin-left: 10px;"
        >
          <el-button type="success">选择Excel文件</el-button>
        </el-upload>
      </div>

      <el-alert
        title="模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px;"
      >
        <div>模板包含以下列：题型(SINGLE/MULTIPLE/JUDGE/ESSAY)、题目内容、分值、选项A、选项A是否正确、选项B、选项B是否正确... 选项H、选项H是否正确、正确答案(判断/简答)、解析</div>
        <div>题型说明：SINGLE=单选 MULTIPLE=多选 JUDGE=判断 ESSAY=简答</div>
      </el-alert>

      <div v-if="previewData.length > 0" class="card-container">
        <div class="card-title">
          预览数据（共 {{ previewData.length }} 条）
          <el-button type="primary" size="small" style="float: right;" :loading="importing" @click="handleImport">确认导入</el-button>
        </div>
        <el-table :data="previewData" border stripe max-height="400">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="题型" width="70">
            <template #default="{ row }">
              <el-tag size="small" :type="tagType(row.type)">{{ row.type }}</el-tag>
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
        </el-table>
      </div>
    </el-card>
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

const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

function tagType(type) {
  const map = { SINGLE: '', MULTIPLE: 'success', JUDGE: 'warning', ESSAY: 'danger' }
  return map[type] || 'info'
}

async function fetchCourses() {
  try {
    const res = await get('/admin/courses', { pageSize: 999 })
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

async function handleImport() {
  if (!courseId.value || !chapterId.value) {
    ElMessage.warning('请先选择课程和章节')
    return
  }
  importing.value = true
  try {
    const data = previewData.value.map(item => ({
      ...item,
      courseId: courseId.value,
      chapterId: chapterId.value
    }))
    await post('/admin/questions/batch', { questions: data })
    ElMessage.success(`成功导入 ${data.length} 道题目`)
    previewData.value = []
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
