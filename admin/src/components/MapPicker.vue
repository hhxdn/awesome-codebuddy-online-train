<template>
  <div class="map-picker">
    <div class="map-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索地点（如：天安门广场、腾讯大厦）"
        clearable
        @keyup.enter="searchPlace"
      >
        <template #append>
          <el-button @click="searchPlace" :loading="searching">搜索</el-button>
        </template>
      </el-input>
      <div v-if="searchMsg" class="search-msg">{{ searchMsg }}</div>
    </div>
    <div class="map-container" ref="mapContainer"></div>
    <div v-if="currentLng && currentLat" class="coord-display">
      <el-tag type="primary">经度: {{ currentLng }}</el-tag>
      <el-tag type="success">纬度: {{ currentLat }}</el-tag>
      <el-tag type="warning" v-if="currentAddress">{{ currentAddress }}</el-tag>
    </div>
    <div v-if="!apiKey" class="map-tip">
      <el-alert
        title="请配置腾讯地图 API Key"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <div>1. 前往 <a href="https://lbs.qq.com/" target="_blank">腾讯位置服务</a> 注册并创建应用</div>
          <div>2. 获取 WebService API 的 Key（需启用地图、地点搜索、逆地址解析）</div>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: Object, default: () => ({ lng: '', lat: '' }) },
  apiKey: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const mapContainer = ref(null)
const searchKeyword = ref('')
const searching = ref(false)
const searchMsg = ref('')
const currentLng = ref('')
const currentLat = ref('')
const currentAddress = ref('')
const sdkReady = ref(false)
const sdkError = ref('')

let map = null
let marker = null
let geocoder = null

// Load Tencent Maps JS API (GL版)
function loadMapSDK() {
  return new Promise((resolve, reject) => {
    if (window.TMap) { resolve(); return }
    const script = document.createElement('script')
    script.src = `https://map.qq.com/api/gljs?v=1.exp&key=${props.apiKey}&libraries=service`
    script.onload = () => {
      // 等待 TMap 完全就绪
      let retries = 0
      const check = () => {
        if (window.TMap && window.TMap.Map) {
          resolve()
        } else if (retries < 20) {
          retries++
          setTimeout(check, 200)
        } else {
          reject(new Error('地图 SDK 初始化超时'))
        }
      }
      check()
    }
    script.onerror = () => reject(new Error('地图 SDK 加载失败，请检查 API Key 是否正确'))
    document.head.appendChild(script)
  })
}

// 获取用户当前城市（优先 HTML5 定位，回退默认北京）
async function getUserLocation() {
  // 1. 尝试浏览器 HTML5 定位
  try {
    const pos = await new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject(new Error('不支持定位'))
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        timeout: 5000,
        maximumAge: 600000
      })
    })
    return { lat: pos.coords.latitude, lng: pos.coords.longitude }
  } catch {
    // HTML5 定位失败
  }

  // 2. 最终默认：北京
  return { lat: 39.90923, lng: 116.397428 }
}

async function initMap() {
  if (!props.apiKey || !mapContainer.value) return

  // Ensure container has dimensions
  const container = mapContainer.value
  if (container.offsetWidth === 0 || container.offsetHeight === 0) {
    setTimeout(initMap, 100)
    return
  }

  // 已有坐标就用已有的，否则根据 IP 定位当前城市
  let defaultLat, defaultLng
  if (props.modelValue.lat && props.modelValue.lng) {
    defaultLat = Number(props.modelValue.lat)
    defaultLng = Number(props.modelValue.lng)
  } else {
    const loc = await getUserLocation()
    defaultLat = loc.lat
    defaultLng = loc.lng
  }

  try {
    map = new window.TMap.Map(container, {
      center: new window.TMap.LatLng(defaultLat, defaultLng),
      zoom: 12,
      viewMode: '2D'
    })

    geocoder = new window.TMap.service.Geocoder()

    // Click to place marker
    map.on('click', (evt) => {
      placeMarker(evt.latLng)
    })

    // If initial coords exist, place marker
    if (props.modelValue.lat && props.modelValue.lng) {
      const latLng = new window.TMap.LatLng(
        Number(props.modelValue.lat),
        Number(props.modelValue.lng)
      )
      placeMarker(latLng)
    }

    sdkReady.value = true
  } catch (e) {
    sdkError.value = '地图初始化失败: ' + e.message
    console.error('Map init error:', e)
  }
}

function placeMarker(latLng) {
  if (marker) {
    marker.setGeometries([{ id: 'pick', styleId: 'marker', position: latLng }])
  } else {
    marker = new window.TMap.MultiMarker({
      map,
      styles: {
        marker: new window.TMap.MarkerStyle({
          width: 32,
          height: 42,
          anchor: { x: 16, y: 42 },
          src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/markerDefault.png'
        })
      },
      geometries: [{ id: 'pick', styleId: 'marker', position: latLng }]
    })
  }

  const lng = latLng.getLng().toFixed(6)
  const lat = latLng.getLat().toFixed(6)
  currentLng.value = lng
  currentLat.value = lat
  emit('update:modelValue', { lng, lat })

  // Reverse geocode
  geocoder.getAddress({ location: latLng }).then((result) => {
    if (result && result.result && result.result.address) {
      currentAddress.value = result.result.address
    }
  }).catch(() => {})
}

async function searchPlace() {
  if (!searchKeyword.value.trim() || !sdkReady.value) return
  searching.value = true
  searchMsg.value = ''

  const keyword = searchKeyword.value.trim()
  try {
    // 使用 TMap SDK 内置 geocoder 做地址解析（走 JavaScript API 配额，无需 WebService）
    const geoResult = await geocoder.getLocation({ address: keyword })
    if (geoResult && geoResult.status === 0 && geoResult.result) {
      const loc = geoResult.result.location
      if (loc) {
        const latLng = new window.TMap.LatLng(loc.lat, loc.lng)
        map.setCenter(latLng)
        map.setZoom(17)
        placeMarker(latLng)
        currentAddress.value = geoResult.result.address || keyword
        searchMsg.value = ''
        return
      }
    }
    searchMsg.value = '未找到该地点，请尝试更精确的关键词'
  } catch (e) {
    console.error('搜索失败:', e)
    searchMsg.value = '搜索失败，SDK 地址解析异常'
  } finally {
    searching.value = false
  }
}

watch(() => props.apiKey, async (key) => {
  if (key) {
    try {
      await loadMapSDK()
      await nextTick()
      initMap()
    } catch (e) {
      sdkError.value = e.message
      console.error(e.message)
    }
  }
})

watch(() => props.modelValue, (val) => {
  if (sdkReady.value && val.lat && val.lng && map) {
    const latLng = new window.TMap.LatLng(Number(val.lat), Number(val.lng))
    map.setCenter(latLng)
    placeMarker(latLng)
  }
}, { deep: true })

onMounted(async () => {
  if (props.apiKey) {
    try {
      await loadMapSDK()
      await nextTick()
      // 给 DOM 一点时间渲染
      setTimeout(initMap, 300)
    } catch (e) {
      sdkError.value = e.message
      console.error(e.message)
    }
  }
})

onBeforeUnmount(() => {
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped>
.map-picker {
  width: 100%;
}

.map-search {
  position: relative;
  margin-bottom: 10px;
  z-index: 10;
}

.search-msg {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
}

.map-container {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.coord-display {
  margin-top: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.map-tip {
  margin-top: 12px;
}

.map-tip a {
  color: var(--el-color-primary);
}
</style>
