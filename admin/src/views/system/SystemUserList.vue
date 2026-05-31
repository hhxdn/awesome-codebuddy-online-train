<template>
  <div class="system-user-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="手机号/昵称" clearable style="width: 220px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>系统用户列表</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="120">
          <template #default="{ row }">
            <span>{{ row.realName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role.id"
              size="small"
              style="margin-right: 4px"
              :type="role.code === 'ADMIN' ? 'danger' : ''"
            >
              {{ role.name }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0" style="color: #909399">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" size="small" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-popconfirm title="确认删除该用户?" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item :label="`用户: ${roleAssignUser.nickname || ''}`">
          <el-checkbox-group v-model="selectedRoleIds">
            <div v-for="role in allRoles" :key="role.id" style="margin-bottom: 10px">
              <el-checkbox :label="role.id">
                {{ role.name }}
                <el-tag size="small" style="margin-left: 6px" :type="role.code === 'ADMIN' ? 'danger' : 'info'">{{ role.code }}</el-tag>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <div v-if="allRoles.length === 0" style="color: #909399; padding: 20px 0">暂无可用角色</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

// 角色分配相关
const roleDialogVisible = ref(false)
const roleSubmitLoading = ref(false)
const roleAssignUser = reactive({ id: null, nickname: '' })
const selectedRoleIds = ref([])
const allRoles = ref([])

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  phone: '',
  nickname: '',
  realName: '',
  password: '',
  status: 1
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const tableData = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

function handleSearch() {
  loading.value = true
  get('/admin/users', {
    keyword: searchForm.keyword,
    page: pagination.page,
    size: pagination.size
  }).then(res => {
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      // 加载每个用户的角色信息
      tableData.value.forEach(row => {
        row.roles = []
        loadUserRoles(row)
      })
    }
  }).finally(() => {
    loading.value = false
  })
}

function loadUserRoles(row) {
  get(`/admin/users/${row.id}/roles`).then(res => {
    if (res.data && res.data.length > 0) {
      // roleIds -> role objects, need to fetch role names
      get('/admin/roles/all').then(roleRes => {
        const roleMap = {}
        if (roleRes.data) {
          roleRes.data.forEach(r => { roleMap[r.id] = r })
        }
        row.roles = res.data.map(id => roleMap[id] || { id, name: '未知' })
      })
    }
  })
}

function handleReset() {
  searchForm.keyword = ''
  pagination.page = 1
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.phone = row.phone
  form.nickname = row.nickname
  form.realName = row.realName || ''
  form.password = ''
  form.status = row.status
  dialogVisible.value = true
}

function handleDialogClosed() {
  resetForm()
  formRef.value?.resetFields()
}

function resetForm() {
  form.phone = ''
  form.nickname = ''
  form.realName = ''
  form.password = ''
  form.status = 1
}

function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true

    const data = {
      phone: form.phone,
      nickname: form.nickname,
      realName: form.realName,
      status: form.status
    }
    if (form.password) {
      data.password = form.password
    }

    const request = isEdit.value
      ? put(`/admin/users/${editId.value}`, data)
      : post('/admin/users', data)

    request.then(() => {
      ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
      dialogVisible.value = false
      handleSearch()
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleStatusChange(row, val) {
  put(`/admin/users/${row.id}`, { status: val ? 1 : 0 }).then(() => {
    ElMessage.success('状态更新成功')
    row.status = val ? 1 : 0
  })
}

function handleResetPwd(row) {
  ElMessageBox.confirm(`确认将 "${row.nickname}" 的密码重置为 123456？`, '重置密码', {
    type: 'warning'
  }).then(() => {
    return put(`/admin/users/${row.id}/reset-password`)
  }).then(() => {
    ElMessage.success('密码已重置为123456')
  }).catch(() => {})
}

function handleDelete(row) {
  del(`/admin/users/${row.id}`).then(() => {
    ElMessage.success('删除成功')
    handleSearch()
  })
}

// 角色分配
function handleAssignRole(row) {
  roleAssignUser.id = row.id
  roleAssignUser.nickname = row.nickname
  selectedRoleIds.value = []
  roleSubmitLoading.value = false

  // 加载所有可用角色
  get('/admin/roles/all').then(res => {
    allRoles.value = res.data || []
  })

  // 加载当前用户已有角色
  get(`/admin/users/${row.id}/roles`).then(res => {
    selectedRoleIds.value = res.data || []
  })

  roleDialogVisible.value = true
}

function handleRoleSubmit() {
  roleSubmitLoading.value = true
  put(`/admin/users/${roleAssignUser.id}/roles`, {
    roleIds: selectedRoleIds.value
  }).then(() => {
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    handleSearch()
  }).finally(() => {
    roleSubmitLoading.value = false
  })
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.system-user-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
