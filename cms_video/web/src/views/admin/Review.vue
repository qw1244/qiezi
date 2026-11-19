<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div class="tip">待审核视频不会在前台显示，通过后才上架；拒绝会删除视频和云端文件。</div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="admin-panel">
      <el-table :data="list">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="seriesTitle" label="合集" min-width="140" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="epNo" label="集" width="70" />
        <el-table-column prop="duration" label="时长(秒)" width="90" />
        <el-table-column prop="createdAt" label="投稿时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="approve(row)">通过</el-button>
            <el-button link type="danger" @click="reject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="prev, pager, next" :total="total" v-model:current-page="page" @current-change="load" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const list = ref<any[]>([])
const page = ref(1)
const total = ref(0)

async function load() {
  const res: any = await http.get('/admin/review/videos', { params: { page: page.value, size: 10 } })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

async function approve(row: any) {
  await http.post(`/admin/review/videos/${row.id}/approve`)
  ElMessage.success('已通过上架')
  load()
}

async function reject(row: any) {
  await ElMessageBox.confirm(`拒绝后会删除《${row.title}》和云端文件，确认？`, '提示')
  await http.post(`/admin/review/videos/${row.id}/reject`)
  ElMessage.success('已拒绝并删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.tip { color: var(--admin-muted); font-size: 0.86rem; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
