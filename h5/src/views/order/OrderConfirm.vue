<template>
  <div class="order-confirm-page page-fade-in">
    <van-nav-bar title="确认订单" left-text="返回" left-arrow @click-left="$router.back()" :border="false" />

    <!-- 微信环境下需先授权 -->
    <div v-if="needWechatAuth" class="auth-section">
      <div class="auth-card">
        <van-icon name="wechat" size="40" color="#07C160" />
        <h3>需要微信授权</h3>
        <p>微信支付需要获取您的微信身份信息</p>
        <van-button type="primary" block round :loading="oauthLoading" @click="doOAuth">
          授权微信信息
        </van-button>
      </div>
    </div>

    <template v-else>
      <div class="course-card" v-if="course.title">
        <div class="course-cover">
          <van-image :src="course.cover" width="88" height="60" fit="cover" radius="8">
            <template #error>
              <div class="cover-ph">{{ course.title?.charAt(0) }}</div>
            </template>
          </van-image>
        </div>
        <div class="course-info">
          <h4>{{ course.title }}</h4>
          <span class="cat-tag">{{ course.categoryName }}</span>
          <span class="price-text">¥{{ course.price }}</span>
        </div>
      </div>

      <van-cell-group inset style="margin-top:12px;">
        <van-cell title="课程价格" :value="'¥' + (course.price || 0)" />
        <van-cell title="优惠" value="暂无优惠" />
        <van-cell title="实付金额" class="total-cell">
          <template #value><span class="total-price">¥{{ course.price || 0 }}</span></template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset title="支付方式" style="margin-top:12px;">
        <van-cell title="微信支付" clickable @click="payMethod = 'wechat'" center>
          <template #icon>
            <div class="pay-icon pay-wx"><van-icon name="wechat" size="18" color="#fff" /></div>
          </template>
          <template #right-icon>
            <van-radio name="wechat" v-model="payMethod" checked-color="#0052D9" />
          </template>
        </van-cell>
        <van-cell title="支付宝" clickable @click="payMethod = 'alipay'" center>
          <template #icon>
            <div class="pay-icon pay-ali"><span style="font-size:15px;font-weight:700;color:#fff;">支</span></div>
          </template>
          <template #right-icon>
            <van-radio name="alipay" v-model="payMethod" checked-color="#0052D9" />
          </template>
        </van-cell>
      </van-cell-group>

      <div class="pay-action">
        <van-button type="primary" block round size="large" :loading="paying" @click="pay" class="pay-btn">
          确认支付 <span class="btn-price">¥{{ course.price || 0 }}</span>
        </van-button>
        <p class="pay-legal">支付即表示同意 <a href="#">《付费服务协议》</a></p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { get, post } from '../../api'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId
const course = ref({})
const payMethod = ref('wechat')
const paying = ref(false)

// 微信授权相关
const needWechatAuth = ref(false)
const oauthLoading = ref(false)
const isWechatBrowser = ref(false)

/** 检测是否微信浏览器 */
function checkWechatBrowser() {
  const ua = navigator.userAgent.toLowerCase()
  return ua.includes('micromessenger')
}

/** 检查用户是否有openid */
async function checkOpenid() {
  try {
    const res = await get('/wx/openid')
    return res.data?.hasOpenid === 'true'
  } catch (e) {
    return false
  }
}

/** 执行微信OAuth授权 */
async function doOAuth() {
  oauthLoading.value = true
  try {
    const currentPath = route.fullPath || '/order/confirm/' + courseId
    const res = await post('/wx/oauth-prepare', { redirect: currentPath })
    if (res.data?.oauthUrl) {
      window.location.href = res.data.oauthUrl
    } else {
      showFailToast('获取授权链接失败')
    }
  } catch (e) {
    showFailToast('授权失败')
  } finally {
    oauthLoading.value = false
  }
}

/** 获取课程信息 */
async function fetchCourse() {
  try {
    const res = await get('/courses/' + courseId)
    if (res.data) course.value = res.data
  } catch (e) {
    course.value = { id: courseId, title: '加载中...', price: 0 }
  }
}

/** 调起微信支付 */
function invokeWxPay(payParams) {
  return new Promise((resolve, reject) => {
    if (typeof WeixinJSBridge === 'undefined') {
      // JSAPI 可能未加载，需要等待
      if (document.addEventListener) {
        document.addEventListener('WeixinJSBridgeReady', () => {
          doInvoke()
        }, false)
      } else if (document.attachEvent) {
        document.attachEvent('WeixinJSBridgeReady', () => {
          doInvoke()
        })
        document.attachEvent('onWeixinJSBridgeReady', () => {
          doInvoke()
        })
      }
      // 直接尝试调用（可能已加载）
      setTimeout(() => {
        if (typeof WeixinJSBridge !== 'undefined') {
          doInvoke()
        } else {
          reject(new Error('微信支付环境未就绪'))
        }
      }, 500)
    } else {
      doInvoke()
    }

    function doInvoke() {
      WeixinJSBridge.invoke('getBrandWCPayRequest', {
        appId: payParams.appId,
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType || 'RSA',
        paySign: payParams.paySign
      }, (res) => {
        if (res.err_msg === 'get_brand_wcpay_request:ok') {
          resolve(res)
        } else if (res.err_msg === 'get_brand_wcpay_request:cancel') {
          reject(new Error('cancel'))
        } else {
          reject(new Error(res.err_msg || '支付失败'))
        }
      })
    }
  })
}

/** 微信支付流程 */
async function payByWechat(orderId) {
  const payRes = await post('/orders/' + orderId + '/pay')
  if (!payRes.data || !payRes.data.package) {
    throw new Error(payRes.message || '获取支付参数失败')
  }
  // 调起微信支付
  await invokeWxPay(payRes.data)
}

/** 支付入口 */
async function pay() {
  if (payMethod.value === 'alipay') {
    showToast('支付宝支付暂未开放，请使用微信支付')
    return
  }

  paying.value = true
  try {
    // 1. 创建订单
    const orderRes = await post('/orders', {
      courseId: parseInt(courseId),
      payMethod: payMethod.value
    })
    const order = orderRes.data
    if (!order || !order.id) {
      showFailToast('创建订单失败')
      return
    }

    // 2. 微信支付流程
    if (payMethod.value === 'wechat') {
      await payByWechat(order.id)
    }

    // 3. 支付成功
    showToast('支付成功')
    setTimeout(() => router.replace('/course/' + courseId), 800)

  } catch (e) {
    if (e.message === 'cancel') {
      showToast('已取消支付')
    } else {
      showFailToast(e.message || '支付失败，请重试')
    }
  } finally {
    paying.value = false
  }
}

onMounted(async () => {
  isWechatBrowser.value = checkWechatBrowser()
  await fetchCourse()

  // 微信环境下检查是否已授权
  if (isWechatBrowser.value && payMethod.value === 'wechat') {
    const hasOpenid = await checkOpenid()
    if (!hasOpenid) {
      needWechatAuth.value = true
    }
  }
})
</script>

<style scoped>
.order-confirm-page { background: var(--bg-color); min-height: 100vh; }

/* 微信授权区域 */
.auth-section {
  display: flex; justify-content: center; padding: 60px 20px;
}
.auth-card {
  background: #fff; border-radius: 16px; padding: 40px 30px;
  text-align: center; width: 100%; max-width: 340px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}
.auth-card h3 { font-size: 18px; font-weight: 600; color: var(--text-color); margin: 16px 0 8px; }
.auth-card p { font-size: 13px; color: var(--text-muted); margin-bottom: 24px; line-height: 1.5; }

.course-card {
  background: #fff; margin: 8px 12px; padding: 16px;
  border-radius: 10px; display: flex; gap: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.cover-ph {
  width: 88px; height: 60px; border-radius: 8px;
  background: linear-gradient(135deg, #0052D9, #366EF4);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; font-weight: 700;
}
.course-info { flex: 1; min-width: 0; }
.course-info h4 { font-size: 15px; font-weight: 500; color: var(--text-color); margin-bottom: 6px; }
.cat-tag {
  display: inline-block; font-size: 11px; color: var(--primary);
  background: var(--primary-bg); padding: 2px 8px; border-radius: 4px;
  margin-bottom: 6px;
}
.price-text { font-size: 20px; font-weight: 700; color: var(--danger); display: block; }

.total-cell :deep(.van-cell__value) { font-weight: 600; }
.total-price { font-size: 20px; font-weight: 700; color: var(--danger); }

.pay-icon {
  width: 34px; height: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  margin-right: 12px;
}
.pay-wx { background: #07C160; }
.pay-ali { background: #1677FF; }

.pay-action { padding: 32px 16px; text-align: center; }
.pay-btn {
  height: 50px; font-size: 16px; font-weight: 600; letter-spacing: 1px;
  background: var(--primary) !important; border: none !important;
  box-shadow: 0 6px 20px rgba(0,82,217,0.35);
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-price { font-size: 18px; font-weight: 700; }
.pay-btn:active { transform: scale(0.97); }
.pay-legal { margin-top: 16px; font-size: 12px; color: var(--text-muted); }
.pay-legal a { color: var(--primary); text-decoration: none; }
</style>
