<template>
  <div class="qa-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable filterable style="width: 130px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="PROCESSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="搜索内容/手机号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <div v-if="getImages(row).length" class="image-list">
              <el-image
                v-for="(img, idx) in getImages(row).slice(0, 2)"
                :key="idx"
                :src="img"
                :preview-src-list="getImages(row)"
                fit="cover"
                style="width:40px;height:40px;margin-right:4px;border-radius:4px"
              />
              <span v-if="getImages(row).length > 2" class="more-text">+{{ getImages(row).length - 2 }}</span>
            </div>
            <span v-else class="no-data">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'" size="small">
              {{ row.status === 'PENDING' ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回复" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply">{{ row.reply }}</span>
            <span v-else class="no-data">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" size="small" link @click="handleProcess(row)">
              标记已处理
            </el-button>
            <span v-else class="processed-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理答疑" width="500px">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="问题描述">
          <div class="content-preview">{{ currentRow?.content }}</div>
        </el-form-item>
        <el-form-item v-if="getImages(currentRow).length" label="图片">
          <el-image
            v-for="(img, idx) in getImages(currentRow)"
            :key="idx"
            :src="img"
            :preview-src-list="getImages(currentRow)"
            fit="cover"
            style="width:80px;height:80px;margin-right:8px;border-radius:6px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <span>{{ currentRow?.phone }}</span>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="processForm.reply" type="textarea" rows="3" placeholder="可选，填写回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="processing" @click="confirmProcess">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get, put } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const query = reactive({ status: '', keyword: '' })

const dialogVisible = ref(false)
const processing = ref(false)
const currentRow = ref(null)
const processForm = reactive({ reply: '' })

function getImages(row) {
  if (!row || !row.images) return []
  return row.images.split(',').filter(Boolean)
}

async function fetchData() {
  loading.value = true
  try {
    const res = await get('/admin/qa-questions', {
      page: page.value,
      pageSize: pageSize.value,
      status: query.status || undefined,
      keyword: query.keyword || undefined
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  query.status = ''
  query.keyword = ''
  page.value = 1
  fetchData()
}

function handleProcess(row) {
  currentRow.value = row
  processForm.reply = ''
  dialogVisible.value = true
}

async function confirmProcess() {
  processing.value = true
  try {
    await put(`/admin/qa-questions/${currentRow.value.id}/process`, {
      reply: processForm.reply
    })
    ElMessage.success('已处理')
    dialogVisible.value = false
    fetchData()
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.qa-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 16px;
}
.table-card {
  min-height: 400px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.image-list {
  display: flex;
  align-items: center;
}
.more-text {
  font-size: 12px;
  color: #909399;
}
.no-data {
  color: #c0c4cc;
  font-size: 13px;
}
.processed-text {
  color: #67c23a;
  font-size: 13px;
}
.content-preview {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
