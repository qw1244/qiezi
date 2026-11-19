<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div></div>
      <div class="actions">
        <el-input v-model="prefix" placeholder="前缀" style="width:260px" clearable />
        <el-button @click="load()">刷新</el-button>
      </div>
    </div>
    <div class="admin-panel">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="key" label="Key" min-width="320" show-overflow-tooltip />
        <el-table-column label="大小" width="120">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="lastModified" label="修改时间" width="220" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-button :disabled="!nextMarker" @click="load(nextMarker)">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const loading = ref(false)
const list = ref<any[]>([])
const prefix = ref('')
const nextMarker = ref('')

function formatSize(n: number) {
  if (!n) return '0 B'
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  return `${(n / 1024 / 1024).toFixed(1)} MB`
}

async function load(marker?: string) {
  loading.value = true
  try {
    const res: any = await http.get('/admin/cos/objects', {
      params: { prefix: prefix.value || undefined, marker, limit: 50 },
    })
    const rows = res.data?.list || []
    list.value = marker ? list.value.concat(rows) : rows
    nextMarker.value = res.data?.truncated ? (res.data.nextMarker || '') : ''
  } finally {
    loading.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除 ${row.key}？`, '提示')
  await http.post('/admin/cos/objects/delete', { key: row.key })
  list.value = list.value.filter((x) => x.key !== row.key)
}

onMounted(() => load())
</script>

<style scoped>
.actions { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.pager { margin-top: 16px; }
</style>
