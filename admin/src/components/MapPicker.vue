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
      <div v-if="searchResults.length > 0" class="search-results">
        <div
          v-for="(item, idx) in searchResults"
          :key="idx"
          class="search-result-item"
          :class="{ active: selectedIdx === idx }"
          @click="selectPlace(item, idx)"
        >
          <div class="result-title">{{ item.title }}</div>
          <div class="result-addr">{{ item.address }}</div>
        </div>
      </div>
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
const searchResults = ref([])
const selectedIdx = ref(-1)
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

// 获取用户当前城市（优先 HTML5 定位，回退 IP 定位）
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
    // HTML5 定位失败，回退 IP 定位
  }

  // 2. 回退：腾讯地图 IP 定位 API
  try {
    const res = await fetch(
      `https://apis.map.qq.com/ws/location/v1/ip?key=${props.apiKey}&output=json`
    )
    const data = await res.json()
    if (data.status === 0 && data.result && data.result.location) {
      return {
        lat: data.result.location.lat,
        lng: data.result.location.lng
      }
    }
  } catch {
    // IP 定位也失败
  }

  // 3. 最终默认：北京
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
  searchResults.value = []
  selectedIdx.value = -1

  try {
    // Use Geocoder for location search
    const result = await geocoder.getLocation({ address: searchKeyword.value })
    if (result && result.status === 0 && result.result) {
      const loc = result.result.location
      if (loc) {
        // Single result: center map on it
        const latLng = new window.TMap.LatLng(loc.lat, loc.lng)
        map.setCenter(latLng)
        map.setZoom(17)
        placeMarker(latLng)
        currentAddress.value = result.result.address || searchKeyword.value
        searchResults.value = []
      } else {
        searchResults.value = [{ title: '未找到该地点', address: '请尝试更精确的关键词', location: null }]
      }
    } else {
      // Try suggestion search
      const suggestResult = await geocoder.getSuggestion({ keyword: searchKeyword.value, region: '全国' })
      if (suggestResult && suggestResult.status === 0 && suggestResult.data && suggestResult.data.length > 0) {
        searchResults.value = suggestResult.data.map(item => ({
          id: item.id,
          title: item.title,
          address: item.address || '',
          location: item.location
        }))
      } else {
        searchResults.value = [{ title: '未找到相关地点', address: '请尝试更精确的关键词', location: null }]
      }
    }
  } catch (e) {
    console.error('Search error:', e)
    searchResults.value = [{ title: '搜索失败', address: '网络错误或 API 异常', location: null }]
  } finally {
    searching.value = false
  }
}

function selectPlace(item, idx) {
  if (!item.location) return
  selectedIdx.value = idx
  const latLng = new window.TMap.LatLng(item.location.lat, item.location.lng)
  map.setCenter(latLng)
  map.setZoom(17)
  placeMarker(latLng)
  searchResults.value = []
  searchKeyword.value = item.title
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

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 0 0 4px 4px;
  max-height: 280px;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.search-result-item {
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}

.search-result-item:hover,
.search-result-item.active {
  background: #ecf5ff;
}

.search-result-item:last-child {
  border-bottom: none;
}

.result-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.result-addr {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
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
