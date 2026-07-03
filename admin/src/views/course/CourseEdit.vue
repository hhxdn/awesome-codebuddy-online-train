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
              <el-cascader
                v-model="form.categoryId"
                :options="categoryOptions"
                :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
                placeholder="请选择末级分类"
                style="width: 100%;"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程类型" prop="courseType">
          <el-radio-group v-model="form.courseType">
            <el-radio value="ONLINE">线上课程</el-radio>
            <el-radio value="OFFLINE">线下课程</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封面图片" prop="coverUrl">
          <ImageUpload v-model="form.coverUrl" tip="支持 jpg/png/gif/webp，建议 283:388 比例，如 283×388" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>

        <!-- 线下课程设置 -->
        <template v-if="form.courseType === 'OFFLINE'">
          <div class="card-title">线下打卡设置</div>
          <el-form-item label="打卡位置">
            <MapPicker
              v-model="checkinLocation"
              :api-key="tmapKey"
            />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="打卡半径(米)">
                <el-input-number v-model="form.checkinRadius" :min="100" :max="10000" :step="100" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="前置线上课程">
                <el-select v-model="form.prerequisiteCourseId" placeholder="不限制" clearable filterable style="width: 100%;">
                  <el-option
                    v-for="item in onlineCourses"
                    :key="item.id"
                    :label="item.title"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

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
        <el-form-item v-if="form.isFree === 0" label="免费试看节数">
          <el-input-number v-model="form.freeChapterCount" :min="0" :max="99" />
          <span style="margin-left: 8px; font-size: 12px; color: #909399;">设为0则不免费，按 sort_order 排序的前N节免费试看</span>
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
          <el-table-column label="视频上传" min-width="280">
            <template #default="{ row }">
              <VideoUpload v-model="row.videoUrl" tip="支持 mp4/flv/mov，上传后将提交到腾讯云点播处理" />
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, post, put } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'
import VideoUpload from '@/components/VideoUpload.vue'
import MapPicker from '@/components/MapPicker.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.params.id && route.params.id !== 'new')
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const categories = ref([])
const categoryOptions = ref([])
const onlineCourses = ref([])

const form = reactive({
  title: '',
  categoryId: null,
  courseType: 'ONLINE',
  coverUrl: '',
  description: '',
  sortOrder: 0,
  isFree: 0,
  price: 0,
  freeChapterCount: 0,
  isRecommend: 0,
  longitude: '',
  latitude: '',
  checkinRadius: 3000,
  prerequisiteCourseId: null,
  chapters: []
})

// 腾讯地图 API Key
const tmapKey = ref('2NMBZ-4XULZ-F2HXM-ZSW4T-TA2JS-Z3FUH')

// 地图选点双向绑定（lng/lat 对象 ↔ form.longitude/form.latitude）
const checkinLocation = reactive({
  lng: form.longitude || '',
  lat: form.latitude || ''
})

// 监听地图选点变化，同步到 form
watch(() => checkinLocation.lng, (val) => { form.longitude = val })
watch(() => checkinLocation.lat, (val) => { form.latitude = val })

// 监听 form 经纬度变化（编辑回显时），同步到地图
watch([() => form.longitude, () => form.latitude], ([lng, lat]) => {
  if (lng && lat && (lng !== checkinLocation.lng || lat !== checkinLocation.lat)) {
    checkinLocation.lng = lng
    checkinLocation.lat = lat
  }
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
    const res = await get('/admin/categories/tree')
    categoryOptions.value = res.data || []
    // 扁平化用于验证
    const flat = []
    function walk(nodes) {
      nodes.forEach(n => {
        flat.push(n)
        if (n.children && n.children.length) walk(n.children)
      })
    }
    walk(categoryOptions.value)
    categories.value = flat
  } catch {
    categoryOptions.value = []
    categories.value = []
  }
}

async function fetchOnlineCourses() {
  try {
    const res = await get('/admin/courses', { size: 999, status: 'UP' })
    const all = res.data?.records || res.data?.list || []
    onlineCourses.value = all.filter(c => c.courseType === 'ONLINE' || !c.courseType)
  } catch {
    onlineCourses.value = []
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
    form.courseType = data.courseType || 'ONLINE'
    form.coverUrl = data.coverUrl || ''
    form.description = data.description || ''
    form.sortOrder = data.sortOrder || 0
    form.isFree = data.isFree || 0
    form.price = data.price || 0
    form.freeChapterCount = data.freeChapterCount || 0
    form.isRecommend = data.isRecommend || 0
    form.longitude = data.longitude || ''
    form.latitude = data.latitude || ''
    form.checkinRadius = data.checkinRadius || 3000
    form.prerequisiteCourseId = data.prerequisiteCourseId || null
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
    // 只发送后端需要的字段，过滤掉纯前端状态字段
    const data = {
      title: form.title,
      categoryId: form.categoryId,
      courseType: form.courseType,
      coverUrl: form.coverUrl,
      description: form.description,
      sortOrder: form.sortOrder,
      isFree: form.isFree,
      price: form.price,
      freeChapterCount: form.freeChapterCount,
      isRecommend: form.isRecommend,
      longitude: form.longitude,
      latitude: form.latitude,
      checkinRadius: form.checkinRadius,
      prerequisiteCourseId: form.prerequisiteCourseId,
      chapters: form.chapters.map((ch, i) => ({
        title: ch.title || '',
        videoUrl: ch.videoUrl || '',
        sortOrder: ch.sortOrder || i + 1
      }))
    }
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
  fetchOnlineCourses()
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
