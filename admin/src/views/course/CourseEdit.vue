<template>
  <div class="page-container" v-loading="loading">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ isEdit ? '编辑课程' : '新增课程' }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="card-title">基本信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>

        <div class="card-title">价格设置</div>
        <el-form-item label="是否免费" prop="isFree">
          <el-radio-group v-model="form.isFree">
            <el-radio :value="1">免费</el-radio>
            <el-radio :value="0">付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.isFree === 0" label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.01" />
        </el-form-item>

        <div class="card-title">推荐设置</div>
        <el-form-item label="是否推荐">
          <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <div class="card-title">
          章节管理
          <el-button size="small" type="primary" style="margin-left: 10px;" @click="addChapter">新增章节</el-button>
        </div>
        <el-table :data="form.chapters" border stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column label="章节标题" min-width="200">
            <template #default="{ row, $index }">
              <el-input v-model="row.title" placeholder="章节标题" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="视频URL" min-width="250">
            <template #default="{ row }">
              <el-input v-model="row.videoUrl" placeholder="视频URL" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="排序" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.sortOrder" :min="0" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button size="small" type="danger" link @click="removeChapter($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="text-align: center; margin-top: 30px;">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, post, put } from '@/api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.params.id && route.params.id !== 'new')
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const categories = ref([])

const form = reactive({
  title: '',
  categoryId: null,
  coverUrl: '',
  description: '',
  sortOrder: 0,
  isFree: 0,
  price: 0,
  isRecommend: 0,
  chapters: []
})

const rules = {
  title: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  isFree: [{ required: true, message: '请选择是否免费', trigger: 'change' }],
  price: [
    {
      validator: (rule, value, callback) => {
        if (form.isFree === 0 && (!value || value <= 0)) {
          callback(new Error('请输入价格'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function addChapter() {
  form.chapters.push({
    title: '',
    videoUrl: '',
    sortOrder: form.chapters.length + 1
  })
}

function removeChapter(index) {
  form.chapters.splice(index, 1)
}

async function fetchCategories() {
  try {
    const res = await get('/admin/categories', { pageSize: 999 })
    categories.value = res.data?.records || res.data?.list || []
  } catch {
    categories.value = []
  }
}

async function fetchCourse() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await get(`/admin/courses/${route.params.id}`)
    const data = res.data
    form.title = data.title || ''
    form.categoryId = data.categoryId
    form.coverUrl = data.coverUrl || ''
    form.description = data.description || ''
    form.sortOrder = data.sortOrder || 0
    form.isFree = data.isFree || 0
    form.price = data.price || 0
    form.isRecommend = data.isRecommend || 0
    form.chapters = data.chapters || []
  } catch {
    ElMessage.error('获取课程信息失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await put(`/admin/courses/${route.params.id}`, data)
    } else {
      await post('/admin/courses', data)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    router.push('/courses')
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchCourse()
})
</script>

<style scoped>
.card-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
</style>
