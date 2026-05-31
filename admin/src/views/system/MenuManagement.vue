<template>
  <div class="page-container">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>菜单管理</h3>
        <div style="display: flex; gap: 10px;">
          <el-button @click="expandAll">展开全部</el-button>
          <el-button @click="collapseAll">折叠全部</el-button>
          <el-button type="primary" @click="handleAdd(null)">新增顶级菜单</el-button>
        </div>
      </div>
      <el-table
        :data="tableData"
        border
        stripe
        row-key="id"
        v-loading="loading"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'MENU' ? 'primary' : 'warning'" size="small">
              {{ row.type === 'MENU' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="160" show-overflow-tooltip />
        <el-table-column prop="permissionCode" label="权限标识" width="180" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="可见" width="70">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleAdd(row)">新增子项</el-button>
            <el-button size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="580px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="父菜单">
          <el-input :model-value="parentName" disabled />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="MENU">菜单</el-radio>
            <el-radio value="BUTTON">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="form.type === 'MENU'" label="路由路径">
          <el-input v-model="form.path" placeholder="如 /system/roles" />
        </el-form-item>
        <el-form-item v-if="form.type === 'MENU'" label="组件路径">
          <el-input v-model="form.component" placeholder="如 system/RoleManagement" />
        </el-form-item>
        <el-form-item v-if="form.type === 'MENU'" label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.permissionCode" placeholder="如 system:role:list" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
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
const dialogTitle = ref('')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const parentName = ref('顶级')

const form = reactive({
  parentId: 0,
  type: 'MENU',
  name: '',
  path: '',
  component: '',
  icon: '',
  permissionCode: '',
  sortOrder: 0,
  visible: 1
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function resetForm() {
  formRef.value?.resetFields()
  form.parentId = 0
  form.type = 'MENU'
  form.name = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.permissionCode = ''
  form.sortOrder = 0
  form.visible = 1
  parentName.value = '顶级'
  isEdit.value = false
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/menus')
    tableData.value = res.data || []
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd(parent) {
  resetForm()
  if (parent) {
    dialogTitle.value = `新增子菜单（父：${parent.name}）`
    form.parentId = parent.id
    parentName.value = parent.name
  } else {
    dialogTitle.value = '新增顶级菜单'
    parentName.value = '顶级'
  }
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || 0,
    type: row.type,
    name: row.name,
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    permissionCode: row.permissionCode || '',
    sortOrder: row.sortOrder || 0,
    visible: row.visible
  })
  parentName.value = row.parentId ? '父菜单(ID:' + row.parentId + ')' : '顶级'
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该菜单吗？子菜单也会被级联删除。', '提示', { type: 'warning' })
    await del(`/admin/menus/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error('删除失败')
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = { ...form, id: undefined }
    if (isEdit.value) {
      await put(`/admin/menus/${form.id}`, data)
    } else {
      await post('/admin/menus', data)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('操作失败') } finally { submitting.value = false }
}

function expandAll() {
  const tableEl = document.querySelector('.el-table')
  if (tableEl) {
    tableEl.__vue__?.store?.states?.data?.forEach(row => {
      tableEl.__vue__?.store?.toggleTreeExpansion?.(row, true)
    })
  }
}

function collapseAll() {
  const tableEl = document.querySelector('.el-table')
  if (tableEl) {
    tableEl.__vue__?.store?.states?.data?.forEach(row => {
      tableEl.__vue__?.store?.toggleTreeExpansion?.(row, false)
    })
  }
}

onMounted(fetchData)
</script>
