<template>
  <div class="uploader">
    <div class="preview" :class="{ empty: !modelValue }" @click="pick">
      <img v-if="modelValue" :src="modelValue" alt="封面" />
      <div v-else class="placeholder">
        <span>+</span>
        <p>上传封面</p>
      </div>
      <div v-if="loading" class="mask">上传中...</div>
    </div>
    <div class="ops">
      <el-button size="small" @click="pick" :loading="loading">选择图片</el-button>
      <el-button v-if="modelValue" size="small" link type="danger" @click="clear">清除</el-button>
    </div>
    <input ref="inputRef" type="file" accept="image/*" style="display:none" @change="onChange" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadToCos } from '@/utils/cosUpload'

const props = defineProps<{
  modelValue?: string
  folder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [string]
}>()

const loading = ref(false)
const inputRef = ref<HTMLInputElement | null>(null)

function pick() {
  inputRef.value?.click()
}

function clear() {
  emit('update:modelValue', '')
}

async function onChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  loading.value = true
  try {
    const { url } = await uploadToCos(file, { folder: props.folder || 'cover' })
    if (!url) {
      throw new Error('未获取到访问地址，请检查 R2 公开访问域名')
    }
    emit('update:modelValue', url)
    ElMessage.success('封面上传成功')
  } catch (err: any) {
    ElMessage.error(err?.msg || err?.message || '上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.uploader {
  display: grid;
  gap: 0.55rem;
}
.preview {
  width: 148px;
  height: 198px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px dashed rgba(43, 24, 51, 0.18);
  background: #f8f4f9;
  cursor: pointer;
  position: relative;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}
.placeholder {
  height: 100%;
  display: grid;
  place-content: center;
  gap: 0.25rem;
  color: var(--admin-muted);
  span {
    font-size: 1.6rem;
    line-height: 1;
  }
  p { margin: 0; font-size: 0.82rem; }
}
.mask {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(28, 20, 36, 0.45);
  color: #fff;
  font-size: 0.86rem;
}
.ops {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}
</style>
