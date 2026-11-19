<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div></div>
      <div class="actions">
        <el-button type="danger" plain @click="clearLogs">清理记录</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>
    <div class="admin-panel">
      <el-table :data="list">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="adminId" label="管理员" width="90" />
        <el-table-column prop="action" label="动作" width="140" />
        <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        v-model:current-page="page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const list = ref<any[]>([])
const page = ref(1)
const size = 20
const total = ref(0)

async function load() {
  const res: any = await http.get('/admin/oplog', { params: { page: page.value, size } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

async function clearLogs() {
  await ElMessageBox.confirm('确定清空全部操作记录？此操作不可恢复。', '清理记录', {
    type: 'warning',
    confirmButtonText: '清空',
    cancelButtonText: '取消',
  })
  await http.delete('/admin/oplog')
  ElMessage.success('已清空操作记录')
  page.value = 1
  await load()
}

onMounted(load)
</script>

<style scoped>
.actions { display: flex; gap: 8px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
