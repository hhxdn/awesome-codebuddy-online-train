<template>
  <div class="rich-text-editor">
    <div ref="toolbarRef" class="editor-toolbar" style="border: 1px solid #dcdfe6; border-bottom: none;"></div>
    <div ref="editorRef" class="editor-body" :style="{ height: height + 'px', border: '1px solid #dcdfe6' }"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import '@wangeditor/editor/dist/css/style.css'
import { createEditor, createToolbar, Boot } from '@wangeditor/editor'

const props = defineProps({
  modelValue: { type: String, default: '' },
  height: { type: Number, default: 500 },
  placeholder: { type: String, default: '请输入正文内容...' }
})

const emit = defineEmits(['update:modelValue'])

const toolbarRef = ref(null)
const editorRef = ref(null)
let editor = null
let toolbar = null
let isInternalChange = false

// 注册图片上传菜单（如果未注册）
const MENU_CONF_KEY = 'uploadImage'
if (!Boot.plugins?.some(p => p.key === MENU_CONF_KEY)) {
  // wangEditor 默认包含图片上传菜单，只需配置
}

onMounted(() => {
  nextTick(() => {
    if (!editorRef.value) return

    const editorConfig = {
      placeholder: props.placeholder,
      autoFocus: false,
      MENU_CONF: {
        uploadImage: {
          server: '/api/admin/upload/image',
          fieldName: 'file',
          maxFileSize: 10 * 1024 * 1024, // 10MB
          maxNumberOfFiles: 10,
          allowedFileTypes: ['image/*'],
          customInsert(res, insertFn) {
            if (res && res.code === 200 && res.data && res.data.url) {
              insertFn(res.data.url, res.data.name || 'image', res.data.url)
            }
          },
        },
        uploadVideo: {
          server: '/api/admin/upload/video',
          fieldName: 'file',
          maxFileSize: 500 * 1024 * 1024,
          allowedFileTypes: ['video/*'],
          customInsert(res, insertFn) {
            if (res && res.code === 200 && res.data && res.data.playbackUrl) {
              insertFn(res.data.playbackUrl, res.data.playbackUrl)
            }
          },
        },
      },
      onChange(editorInstance) {
        if (isInternalChange) return
        emit('update:modelValue', editorInstance.getHtml())
      }
    }

    editor = createEditor({
      selector: editorRef.value,
      config: editorConfig,
      html: props.modelValue || '',
      mode: 'default'
    })

    if (toolbarRef.value) {
      toolbar = createToolbar({
        editor,
        selector: toolbarRef.value,
        config: {},
        mode: 'default'
      })
    }
  })
})

// 外部值变化时同步到编辑器
watch(() => props.modelValue, (newVal) => {
  if (editor && newVal !== editor.getHtml()) {
    isInternalChange = true
    editor.setHtml(newVal || '')
    nextTick(() => { isInternalChange = false })
  }
})

onBeforeUnmount(() => {
  if (toolbar) {
    toolbar.destroy()
    toolbar = null
  }
  if (editor) {
    editor.destroy()
    editor = null
  }
})
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
}
.editor-body {
  overflow-y: auto;
  background: #fff;
}
</style>
