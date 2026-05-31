# 地图选点组件说明

## 概述

管理后台课程编辑页的"线下打卡设置"中，经纬度配置已从手动输入升级为**腾讯地图选点**，支持搜索地点、点击地图选点、自动逆地址解析。

## 组件文件

- `admin/src/components/MapPicker.vue` - 地图选点组件
- `admin/src/views/course/CourseEdit.vue` - 课程编辑页（使用方）

## API Key

| 项目 | 值 |
|------|-----|
| Key | `2NMBZ-4XULZ-F2HZM-ZSW4T-TA2JS-Z3FUH` |
| 平台 | 腾讯位置服务 (lbs.qq.com) |
| 免费额度 | 10,000 次/日 |
| 启用服务 | 地图展示、地点搜索、逆地址解析 |

### 如何申请新 Key

1. 打开 https://lbs.qq.com/
2. 注册/登录 → 控制台 → 应用管理 → 创建应用
3. 添加 Key，选择 **WebServiceAPI**
4. 启用服务：地图、地点搜索、逆地址解析
5. 将获取的 Key 替换 `CourseEdit.vue` 中的 `tmapKey`

## 功能说明

| 功能 | 操作方式 | 说明 |
|------|---------|------|
| 搜索地点 | 输入关键词回车或点击搜索 | 支持模糊搜索，显示前10条结果 |
| 点击选点 | 直接点击地图 | 放置标记并获取坐标 |
| 逆地址解析 | 选点后自动触发 | 显示详细地址信息 |
| 坐标回显 | 编辑课程时自动定位 | 根据已保存的经纬度定位地图 |

## 使用流程

1. 进入 **课程管理 → 新增/编辑课程**
2. 课程类型选择 **线下课程**
3. 在"线下打卡设置"区域看到地图
4. 搜索或点击选择打卡地点
5. 设置打卡半径（米）
6. 保存课程

## 技术实现

- **地图 SDK**：腾讯地图 JavaScript API v2 (GL)
- **搜索**：`TMap.service.Search` 地点搜索
- **逆地理编码**：`TMap.service.Geocoder.getAddress`
- **标记**：`TMap.MultiMarker` 可拖拽标记
- **数据流**：MapPicker(lng/lat) → watch → form.longitude/form.latitude

## 降级处理

如果 API Key 未配置或加载失败，组件会显示引导提示，原有的经纬度数据仍然可以正常保存和读取。
