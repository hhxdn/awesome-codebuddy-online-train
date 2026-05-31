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
      <el-tag>经度: {{ currentLng }}</el-tag>
      <el-tag type="success" style="margin-left: 8px;">纬度: {{ currentLat }}</el-tag>
      <el-tag type="warning" style="margin-left: 8px;">{{ currentAddress }}</el-tag>
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

let map = null
let marker = null
let geocoder = null
let searchService = null
let mapLoaded = false

// Load Tencent Maps JS API
function loadMapSDK() {
  return new Promise((resolve, reject) => {
    if (window.TMap) { resolve(); return }
    const script = document.createElement('script')
    script.src = `https://map.qq.com/api/js?v=2.exp&key=${props.apiKey}&libraries=place`
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('地图 SDK 加载失败，请检查 API Key 是否正确'))
    document.head.appendChild(script)
  })
}

function initMap() {
  if (!props.apiKey || !mapContainer.value) return
  const center = new window.TMap.LatLng(
    props.modelValue.lat || 39.90923,
    props.modelValue.lng || 116.397428
  )
  map = new window.TMap.Map(mapContainer.value, {
    center,
    zoom: 15,
    mapStyleId: 'style1'
  })
  geocoder = new window.TMap.service.Geocoder()
  searchService = new window.TMap.service.Search()

  // Click to place marker
  map.on('click', (evt) => {
    placeMarker(evt.latLng)
  })

  // If initial coords exist, place marker
  if (props.modelValue.lat && props.modelValue.lng) {
    placeMarker(center)
  }

  mapLoaded = true
}

function placeMarker(latLng) {
  if (marker) {
    marker.setPosition(latLng)
  } else {
    marker = new window.TMap.MultiMarker({
      map,
      styles: {
        default: new window.TMap.MarkerStyle({
          width: 32,
          height: 42,
          anchor: { x: 16, y: 42 },
          src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/markerDefault.png'
        })
      },
      geometries: [{ id: 'pick', styleId: 'default', position: latLng }]
    })
  }

  const lng = latLng.getLng().toFixed(6)
  const lat = latLng.getLat().toFixed(6)
  currentLng.value = lng
  currentLat.value = lat
  emit('update:modelValue', { lng, lat })

  // Reverse geocode
  geocoder.getAddress(latLng).then((result) => {
    if (result && result.result && result.result.address) {
      currentAddress.value = result.result.address
    }
  }).catch(() => {})
}

function searchPlace() {
  if (!searchKeyword.value.trim() || !mapLoaded) return
  searching.value = true
  searchResults.value = []
  selectedIdx.value = -1

  searchService.search({
    keyword: searchKeyword.value,
    location: map.getCenter(),
    pageSize: 10
  }).then((result) => {
    if (result && result.data && result.data.length > 0) {
      searchResults.value = result.data.map(item => ({
        id: item.id,
        title: item.title,
        address: item.address || '',
        location: item.location
      }))
    } else {
      searchResults.value = []
    }
  }).catch(() => {
    searchResults.value = []
  }).finally(() => {
    searching.value = false
  })
}

function selectPlace(item, idx) {
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
      console.error(e.message)
    }
  }
})

watch(() => props.modelValue, (val) => {
  if (mapLoaded && val.lat && val.lng) {
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
      initMap()
    } catch (e) {
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
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
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
  height: 380px;
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
  gap: 4px;
}

.map-tip {
  margin-top: 12px;
}

.map-tip a {
  color: var(--el-color-primary);
}
</style>
