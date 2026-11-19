<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div class="tip">待审核合集和视频都不会在前台显示，通过后才上架；拒绝会删除合集/视频和云端文件。</div>
      <el-button @click="loadAll">刷新</el-button>
    </div>

    <div class="admin-panel">
      <h3>待审核合集</h3>
      <el-table :data="seriesList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="70" />
        <el-table-column prop="intro" label="简介" min-width="180" show-overflow-tooltip />
        <el-table-column prop="epCount" label="集数" width="70" />
        <el-table-column prop="createdAt" label="投稿时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <router-link class="link" :to="`/admin/series/${row.id}`">详情</router-link>
            <el-button link type="primary" @click="approveSeries(row)">通过</el-button>
            <el-button link type="danger" @click="rejectSeries(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="prev, pager, next" :total="seriesTotal" v-model:current-page="seriesPage" @current-change="loadSeries" />
    </div>

    <div class="admin-panel">
      <h3>待审核视频（已上架合集里新传的）</h3>
      <el-table :data="videoList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="seriesTitle" label="合集" min-width="140" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="epNo" label="集" width="70" />
        <el-table-column prop="duration" label="时长(秒)" width="90" />
        <el-table-column prop="createdAt" label="投稿时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="approveVideo(row)">通过</el-button>
            <el-button link type="danger" @click="rejectVideo(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="prev, pager, next" :total="videoTotal" v-model:current-page="videoPage" @current-change="loadVideos" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const seriesList = ref<any[]>([])
const seriesPage = ref(1)
const seriesTotal = ref(0)
const videoList = ref<any[]>([])
const videoPage = ref(1)
const videoTotal = ref(0)

async function loadSeries() {
  const res: any = await http.get('/admin/review/series', { params: { page: seriesPage.value, size: 10 } })
  seriesList.value = res.data?.list || []
  seriesTotal.value = res.data?.total || 0
}

async function loadVideos() {
  const res: any = await http.get('/admin/review/videos', { params: { page: videoPage.value, size: 10 } })
  videoList.value = res.data?.list || []
  videoTotal.value = res.data?.total || 0
}

function loadAll() {
  loadSeries()
  loadVideos()
}

async function approveSeries(row: any) {
  await http.post(`/admin/review/series/${row.id}/approve`)
  ElMessage.success('已通过上架')
  loadAll()
}

async function rejectSeries(row: any) {
  await ElMessageBox.confirm(`拒绝后会删除《${row.title}》和里面的视频、云端文件，确认？`, '提示')
  await http.post(`/admin/review/series/${row.id}/reject`)
  ElMessage.success('已拒绝并删除')
  loadAll()
}

async function approveVideo(row: any) {
  await http.post(`/admin/review/videos/${row.id}/approve`)
  ElMessage.success('已通过上架')
  loadAll()
}

async function rejectVideo(row: any) {
  await ElMessageBox.confirm(`拒绝后会删除《${row.title}》和云端文件，确认？`, '提示')
  await http.post(`/admin/review/videos/${row.id}/reject`)
  ElMessage.success('已拒绝并删除')
  loadAll()
}

onMounted(loadAll)
</script>

<style scoped>
.tip { color: var(--admin-muted); font-size: 0.86rem; }
.link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 0.86rem;
  margin-right: 8px;
}
.admin-panel h3 { margin: 0 0 12px; font-size: 1rem; }
.admin-panel + .admin-panel { margin-top: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
