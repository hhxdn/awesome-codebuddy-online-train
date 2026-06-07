<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>新闻模块管理</h3>
        <el-button type="primary" @click="handleAdd">新增模块</el-button>
      </div>
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="模块名称" min-width="150" />
        <el-table-column prop="type" label="类型标识" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
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
      width="500px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模块名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="类型标识" prop="type">
          <el-input v-model="form.type" placeholder="如：NEWS, ORGANIZER" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
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

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增模块')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const form = reactive({
  name: '',
  type: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入模块名称', trigger: 'blur' }],
  type: [{ required: true, message: '请输入类型标识', trigger: 'blur' }]
}

function resetForm() {
  formRef.value?.resetFields()
  form.name = ''
  form.type = ''
  form.sortOrder = 0
  form.status = 1
  isEdit.value = false
  editId.value = null
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/news-modules')
    tableData.value = res.data || []
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增模块'
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑模块'
  form.name = row.name || ''
  form.type = row.type || ''
  form.sortOrder = row.sortOrder || 0
  form.status = row.status || 1
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该模块吗？', '提示', { type: 'warning' })
    await del(`/admin/news-modules/${row.id}`)
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
      name: form.name,
      type: form.type,
      sortOrder: form.sortOrder,
      status: form.status
    }
    if (isEdit.value) {
      await put(`/admin/news-modules/${editId.value}`, data)
    } else {
      await post('/admin/news-modules', data)
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
  fetchData()
})
</script>
