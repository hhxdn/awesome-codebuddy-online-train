<template>
  <div class="register-profile-page">
    <div class="page-header">
      <van-icon name="arrow-left" size="20" @click="handleBack" class="back-btn" />
      <h2>完善个人资料</h2>
      <p class="subtitle">提交资料后，我们会尽快审核开通您的账号</p>
    </div>

    <div class="form-container">
      <van-form @submit="onSubmit" ref="formRef">
        <van-cell-group inset>
          <van-field
            v-model="form.realName"
            name="realName"
            label="姓名"
            placeholder="请输入真实姓名"
            :rules="[{ required: true, message: '请输入姓名' }]"
          />
          <van-field
            v-model="form.gender"
            name="gender"
            label="性别"
            placeholder="请选择性别"
            :rules="[{ required: true, message: '请选择性别' }]"
            is-link
            readonly
            @click="showGenderPicker = true"
          />
          <van-field
            v-model="form.age"
            name="age"
            label="年龄"
            type="digit"
            placeholder="请输入年龄"
            :rules="[{ required: true, message: '请输入年龄' }]"
          />
          <van-field
            v-model="form.education"
            name="education"
            label="学历"
            placeholder="请选择学历"
            :rules="[{ required: true, message: '请选择学历' }]"
            is-link
            readonly
            @click="showEducationPicker = true"
          />
          <van-field
            v-model="form.major"
            name="major"
            label="专业"
            placeholder="请输入所学专业"
            :rules="[{ required: true, message: '请输入专业' }]"
          />
          <van-field
            v-model="form.contactPhone"
            name="contactPhone"
            label="联系电话"
            type="tel"
            maxlength="11"
            placeholder="请输入联系电话"
            :rules="[{ required: true, message: '请输入联系电话' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
          />
        </van-cell-group>

        <div class="form-action">
          <van-button round block type="primary" native-type="submit" :loading="loading" class="submit-btn">
            提交资料
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 性别选择器 -->
    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker
        :columns="genderColumns"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
      />
    </van-popup>

    <!-- 学历选择器 -->
    <van-popup v-model:show="showEducationPicker" position="bottom" round>
      <van-picker
        :columns="educationColumns"
        @confirm="onEducationConfirm"
        @cancel="showEducationPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { post } from '../../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const showGenderPicker = ref(false)
const showEducationPicker = ref(false)

const form = reactive({
  realName: '',
  gender: '',
  age: '',
  education: '',
  major: '',
  contactPhone: ''
})

const genderColumns = ['男', '女']
const educationColumns = ['高中', '中专', '大专', '本科', '硕士', '博士', '其他']

function onGenderConfirm({ selectedOptions }) {
  form.gender = selectedOptions[0].text
  showGenderPicker.value = false
}

function onEducationConfirm({ selectedOptions }) {
  form.education = selectedOptions[0].text
  showEducationPicker.value = false
}

function handleBack() {
  router.back()
}

async function onSubmit() {
  loading.value = true
  try {
    await post('/user/submit-profile', {
      realName: form.realName,
      gender: form.gender,
      age: form.age,
      education: form.education,
      major: form.major,
      contactPhone: form.contactPhone
    })
    await showDialog({
      title: '提交成功',
      message: '已经提交，我们会尽快联系您，后台人员审核后开通账号，可以使用小程序',
      confirmButtonText: '我知道了',
      allowHtml: false
    })
    router.replace('/pending-approval')
  } catch (e) {
    showToast(e.message || '提交失败')
  }
  loading.value = false
}
</script>

<style scoped>
.register-profile-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

.page-header {
  background: linear-gradient(160deg, #003CAB 0%, #0052D9 40%, #366EF4 100%);
  padding: 24px 20px 28px;
  color: #fff;
  position: relative;
}

.back-btn {
  position: absolute;
  top: 24px;
  left: 16px;
  color: #fff;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  padding-left: 20px;
  padding-top: 4px;
}

.subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin: 8px 0 0 20px;
}

.form-container {
  padding-top: 16px;
}

.form-action {
  padding: 28px 20px;
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  background: var(--primary, #0052D9) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 82, 217, 0.3);
}

::deep(.van-cell-group--inset) {
  margin: 0 16px;
}
</style>
