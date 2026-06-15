<template>
  <div class="page-container">
    <div class="card-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3>角色管理</h3>
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="角色名称" width="160" />
        <el-table-column prop="code" label="角色编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="handlePermission(row)">权限</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="如 SUPER_ADMIN" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="角色描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配弹窗 -->
    <el-dialog v-model="permVisible" title="分配权限" width="550px">
      <el-tree
        ref="treeRef"
        :data="allMenus"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedMenuIds"
        :props="{ label: 'name', children: 'children' }"
        style="max-height: 50vh; overflow-y: auto;"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitting" @click="handleSavePerm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/api'

const loading = ref(false)
const tableData = ref([])
const allMenus = ref([])
const checkedMenuIds = ref([])
const permRoleId = ref(null)

const dialogVisible = ref(false)
const permVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const permSubmitting = ref(false)
const formRef = ref(null)
const treeRef = ref(null)

const form = reactive({
  name: '',
  code: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

function resetForm() {
  formRef.value?.resetFields()
  form.name = ''
  form.code = ''
  form.description = ''
  form.status = 1
  isEdit.value = false
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/roles', { size: 999 })
    tableData.value = res.data?.records || []
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd() {
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description || '',
    status: row.status
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
    await del(`/admin/roles/${row.id}`)
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
    if (isEdit.value) {
      await put(`/admin/roles/${form.id}`, {
        name: form.name, code: form.code,
        description: form.description, status: form.status
      })
    } else {
      await post('/admin/roles', { ...form })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('操作失败') } finally { submitting.value = false }
}

async function handlePermission(row) {
  permRoleId.value = row.id
  checkedMenuIds.value = []
  try {
    const [menusRes, menuIdsRes] = await Promise.all([
      get('/admin/menus'),
      get(`/admin/roles/${row.id}/menus`)
    ])
    allMenus.value = menusRes.data || []
    checkedMenuIds.value = menuIdsRes.data || []
  } catch { allMenus.value = [] }
  permVisible.value = true
}

async function handleSavePerm() {
  permSubmitting.value = true
  try {
    const checked = treeRef.value.getCheckedKeys()
    const halfChecked = treeRef.value.getHalfCheckedKeys()
    const allIds = [...checked, ...halfChecked]
    await put(`/admin/roles/${permRoleId.value}/menus`, { menuIds: allIds })
    ElMessage.success('权限保存成功')
    permVisible.value = false
  } catch { ElMessage.error('保存失败') } finally { permSubmitting.value = false }
}

onMounted(fetchData)
</script>
