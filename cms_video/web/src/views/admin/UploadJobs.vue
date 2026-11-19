<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div></div>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="admin-panel">
      <el-table :data="list">
        <el-table-column prop="id" label="任务ID" width="90" />
        <el-table-column prop="seriesId" label="合集ID" width="90" />
        <el-table-column prop="total" label="总数" width="80" />
        <el-table-column prop="okNum" label="成功" width="80" />
        <el-table-column prop="failNum" label="失败" width="80" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="page"
        @current-change="load"
      />
    </div>

    <el-drawer v-model="drawer" title="失败/明细" size="520px">
      <el-table :data="items">
        <el-table-column prop="epNo" label="集" width="60" />
        <el-table-column prop="fileName" label="文件" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column prop="errMsg" label="失败详情" min-width="160" show-overflow-tooltip />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/utils/http'

const list = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const drawer = ref(false)
const items = ref<any[]>([])

async function load() {
  const res: any = await http.get('/admin/upload/job', { params: { page: page.value, size: 10 } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

async function showDetail(id: number) {
  const res: any = await http.get(`/admin/upload/job/${id}`)
  items.value = res.data.items || []
  drawer.value = true
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
