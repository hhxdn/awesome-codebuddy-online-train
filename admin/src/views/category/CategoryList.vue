<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>课程分类管理（多级树形）</h3>
        <el-button type="primary" @click="handleAdd()">新增一级分类</el-button>
      </div>
      <el-table :data="tableData" border stripe row-key="id" default-expand-all>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="160">
          <template #default="{ row }">
            <span :style="{ paddingLeft: ((row.level || 1) - 1) * 24 + 'px' }">
              <el-tag v-if="row.level === 1" type="primary" size="small" effect="plain">一级</el-tag>
              <el-tag v-else-if="row.level === 2" type="success" size="small" effect="plain">二级</el-tag>
              <el-tag v-else type="warning" size="small" effect="plain">三级</el-tag>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="父分类" width="120">
          <template #default="{ row }">
            <span v-if="row.parentId">{{ getParentName(row.parentId) }}</span>
            <span v-else style="color:var(--el-text-color-placeholder)">-</span>
          </template>
        </el-table-column>
        <el-table-column label="价格设置" width="140">
          <template #default="{ row }">
            <span v-if="row.isFree === 1" style="color:#00A870;">免费</span>
            <span v-else style="color:#E34D59;">¥{{ row.price || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleToggleStatus(row, val)"
              active-text="启用"
              inactive-text="禁用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" link @click="handleAdd(row)">添加子分类</el-button>
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="父分类" v-if="form.parentId">
          <el-input :model-value="getParentName(form.parentId)" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.cover" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="购买页展示的分类描述" />
        </el-form-item>
        <el-form-item label="是否免费">
          <el-radio-group v-model="form.isFree" :disabled="form.parentId == null">
            <el-radio :value="1">免费</el-radio>
            <el-radio :value="0">付费</el-radio>
          </el-radio-group>
          <div v-if="form.parentId == null" style="font-size:12px;color:#909399;">一级分类不支持设置价格，请在末级分类设置</div>
        </el-form-item>
        <el-form-item v-if="form.isFree === 0 && form.parentId != null" label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.01" />
          <span style="margin-left:8px;color:#909399;">元</span>
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

const loading = ref(false)
const tableData = ref([])
const flatList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

const form = reactive({
  parentId: null,
  name: '',
  cover: '',
  description: '',
  isFree: 0,
  price: 0,
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  price: [{
    validator: (rule, value, callback) => {
      if (form.isFree === 0 && form.parentId != null && (!value || value <= 0)) {
        callback(new Error('请输入售价'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
}

function getParentName(pid) {
  const p = flatList.value.find(c => c.id === pid)
  return p ? p.name : '未知'
}

function buildTree(list) {
  flatList.value = list
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else if (!item.parentId) {
      roots.push(map[item.id])
    }
  })
  return roots
}

function flattenTree(roots) {
  const result = []
  function walk(nodes, level) {
    nodes.forEach(n => {
      result.push(n)
      if (n.children && n.children.length > 0) walk(n.children, level + 1)
    })
  }
  walk(roots, 0)
  return result
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/categories')
    const list = res.data || []
    const roots = buildTree(list)
    tableData.value = roots
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.parentId = null
  form.name = ''
  form.cover = ''
  form.description = ''
  form.isFree = 0
  form.price = 0
  form.sortOrder = 0
  isEdit.value = false
  editId.value = null
}

function handleAdd(parent) {
  resetForm()
  if (parent) {
    form.parentId = parent.id
    dialogTitle.value = '新增子分类（父分类：' + parent.name + '）'
  } else {
    dialogTitle.value = '新增一级分类'
  }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑分类'
  form.parentId = row.parentId || null
  form.name = row.name || ''
  form.cover = row.cover || ''
  form.description = row.description || ''
  form.isFree = row.isFree || 0
  form.price = row.price || 0
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function handleToggleStatus(row, val) {
  try {
    await put(`/admin/categories/${row.id}/status`, { status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success('状态更新成功')
  } catch {
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该分类吗？如有子分类请先删除。', '提示', { type: 'warning' })
    await del(`/admin/categories/${row.id}`)
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
      parentId: form.parentId,
      name: form.name,
      cover: form.cover,
      description: form.description,
      isFree: form.parentId ? form.isFree : 0,
      price: form.parentId ? form.price : 0,
      sortOrder: form.sortOrder
    }
    if (isEdit.value) {
      await put(`/admin/categories/${editId.value}`, data)
    } else {
      await post('/admin/categories', data)
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
