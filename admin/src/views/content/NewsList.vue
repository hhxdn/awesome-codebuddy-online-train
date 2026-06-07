<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <div style="display: flex; align-items: center; gap: 12px;">
          <h3>新闻资讯管理</h3>
          <el-select v-model="filterModuleId" placeholder="全部模块" clearable style="width: 150px;" @change="fetchData">
            <el-option v-for="m in modules" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd">新增资讯</el-button>
      </div>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="row.cover"
              style="width: 100px; height: 56px; border-radius: 4px;"
              fit="cover"
              :preview-src-list="[row.cover]"
              preview-teleported
            />
            <span v-else style="color: #c0c4cc;">无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="所属模块" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.moduleId" size="small" type="primary">
              {{ getModuleName(row.moduleId) }}
            </el-tag>
            <span v-else style="color: #c0c4cc;">未分类</span>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="120" show-overflow-tooltip />
        <el-table-column prop="viewCount" label="阅读量" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleToggleStatus(row, val)"
              active-text="发布"
              inactive-text="草稿"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1000px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入新闻标题" />
        </el-form-item>
        <el-form-item label="所属模块">
          <el-select v-model="form.moduleId" placeholder="请选择模块" clearable>
            <el-option v-for="m in modules" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            placeholder="请输入摘要（可选，留空自动截取正文前200字）"
          />
        </el-form-item>
        <el-form-item label="封面图">
          <ImageUpload v-model="form.cover" tip="支持 jpg/png/gif/webp，建议 16:9 比例" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="form.source" placeholder="如：官方博客、技术社区等" />
        </el-form-item>
        <el-form-item label="正文内容" prop="content" class="editor-form-item">
          <RichTextEditor v-model="form.content" :height="450" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

const loading = ref(false)
const tableData = ref([])
const modules = ref([])
const filterModuleId = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增资讯')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const form = reactive({
  title: '',
  summary: '',
  cover: '',
  content: '',
  source: '',
  moduleId: null,
  sortOrder: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

function resetForm() {
  formRef.value?.resetFields()
  form.title = ''
  form.summary = ''
  form.cover = ''
  form.content = ''
  form.source = ''
  form.sortOrder = 0
  isEdit.value = false
  editId.value = null
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/news')
    tableData.value = res.data || []
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增资讯'
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑资讯'
  form.title = row.title || ''
  form.summary = row.summary || ''
  form.cover = row.cover || ''
  form.content = row.content || ''
  form.source = row.source || ''
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function handleToggleStatus(row, val) {
  try {
    await put(`/admin/news/${row.id}/status`, { status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success('状态更新成功')
  } catch {
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该资讯吗？', '提示', { type: 'warning' })
    await del(`/admin/news/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      title: form.title,
      summary: form.summary,
      cover: form.cover,
      content: form.content,
      source: form.source,
      sortOrder: form.sortOrder
    }
    if (isEdit.value) {
      await put(`/admin/news/${editId.value}`, data)
    } else {
      await post('/admin/news', data)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchModules()
  fetchData()
})
</script>

<style scoped>
.editor-form-item :deep(.el-form-item__content) {
  display: block;
}
</style>
