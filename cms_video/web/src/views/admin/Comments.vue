<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div class="tip">评论在播放页直接显示，不用审核；这里可以删除不当评论。删除后前台立即消失。</div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="admin-panel">
      <el-table :data="list">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="用户" width="120" show-overflow-tooltip />
        <el-table-column prop="videoTitle" label="视频" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <router-link v-if="row.videoId" class="link" :to="`/play/${row.videoId}`">
              {{ row.videoTitle || `视频 #${row.videoId}` }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const list = ref<any[]>([])
const page = ref(1)
const total = ref(0)

async function load(p = 1) {
  page.value = p
  const res: any = await http.get('/admin/comments', { params: { page: p, size: 10 } })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

async function remove(row: any) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/admin/comments/${row.id}`)
  ElMessage.success('已删除')
  load(page.value)
}

onMounted(() => load())
</script>
