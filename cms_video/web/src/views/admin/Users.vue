<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div></div>
      <el-input v-model="keyword" placeholder="搜索账号/昵称" style="width:260px" clearable @change="load" />
    </div>
    <div class="admin-panel">
      <el-table :data="list">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="72">
          <template #default="{ row }">
            <div class="table-avatar">
              <img v-if="row.avatar" :src="row.avatar" alt="" />
              <span v-else>{{ (row.nickname || row.username || '?').slice(0, 1) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="查看详情" placement="top">
              <el-button link type="primary" :icon="View" @click="openDetail(row)" />
            </el-tooltip>
            <el-tooltip :content="row.status === 1 ? '禁用' : '启用'" placement="top">
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                :icon="row.status === 1 ? Lock : Unlock"
                @click="setStatus(row, row.status === 1 ? 0 : 1)"
              />
            </el-tooltip>
            <el-tooltip content="重置密码" placement="top">
              <el-button link type="danger" :icon="Key" @click="resetPwd(row)" />
            </el-tooltip>
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

    <el-drawer v-model="drawer" size="640px" :title="detailTitle">
      <div v-loading="detailLoading" class="detail">
        <template v-if="detail">
          <div class="profile">
            <div class="avatar">
              <img v-if="detail.user.avatar" :src="detail.user.avatar" alt="" />
              <span v-else>{{ (detail.user.nickname || detail.user.username || '?').slice(0, 1) }}</span>
            </div>
            <div>
              <h3>{{ detail.user.nickname || detail.user.username }}</h3>
              <p>账号：{{ detail.user.username }} · ID {{ detail.user.id }}</p>
              <el-tag :type="detail.user.status === 1 ? 'success' : 'info'" effect="light" round size="small">
                {{ detail.user.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </div>

          <div class="stats">
            <div><strong>{{ detail.watchCount || 0 }}</strong><span>足迹</span></div>
            <div><strong>{{ detail.likeCount || 0 }}</strong><span>点赞</span></div>
            <div><strong>{{ detail.favCount || 0 }}</strong><span>收藏</span></div>
          </div>

          <el-tabs v-model="tab">
            <el-tab-pane :label="`浏览足迹 (${detail.watchCount || 0})`" name="history">
              <div v-if="detail.history?.length" class="list">
                <div v-for="item in detail.history" :key="'h' + item.id" class="row">
                  <div>
                    <strong>{{ item.title }}</strong>
                    <p>看到第 {{ item.lastEpNo || 1 }} 集 · 进度 {{ item.lastProgress || 0 }}%</p>
                  </div>
                  <span>{{ item.epCount || 0 }} 集</span>
                </div>
              </div>
              <el-empty v-else description="暂无足迹" />
            </el-tab-pane>
            <el-tab-pane :label="`点赞 (${detail.likeCount || 0})`" name="likes">
              <div v-if="detail.likes?.length" class="list">
                <div v-for="item in detail.likes" :key="'l' + item.id" class="row">
                  <div>
                    <strong>{{ item.title || `第${item.epNo}集` }}</strong>
                    <p>第 {{ item.epNo }} 集 · 合集 #{{ item.seriesId }}</p>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无点赞" />
            </el-tab-pane>
            <el-tab-pane :label="`收藏 (${detail.favCount || 0})`" name="favs">
              <div v-if="detail.favs?.length" class="list">
                <div v-for="item in detail.favs" :key="'f' + item.id" class="row">
                  <div>
                    <strong>{{ item.title }}</strong>
                    <p>{{ item.epCount || 0 }} 集 · 第一集进度 {{ item.firstProgress || 0 }}%</p>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无收藏" />
            </el-tab-pane>
          </el-tabs>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Key, Lock, Unlock, View } from '@element-plus/icons-vue'
import http from '@/utils/http'

const list = ref<any[]>([])
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const drawer = ref(false)
const detailLoading = ref(false)
const detail = ref<any>(null)
const tab = ref('history')

const detailTitle = computed(() => {
  if (!detail.value?.user) return '用户详情'
  return `用户详情 · ${detail.value.user.nickname || detail.value.user.username}`
})

async function load() {
  const res: any = await http.get('/admin/users', { params: { keyword: keyword.value, page: page.value, size: 10 } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

async function openDetail(row: any) {
  drawer.value = true
  tab.value = 'history'
  detailLoading.value = true
  detail.value = null
  try {
    const res: any = await http.get(`/admin/users/${row.id}`)
    detail.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function setStatus(row: any, status: number) {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}用户「${row.nickname || row.username}」？`, '提示')
  await http.post(`/admin/users/${row.id}/status`, { status })
  row.status = status
  ElMessage.success(`已${action}`)
}

async function resetPwd(row: any) {
  await ElMessageBox.confirm(`确认重置「${row.nickname || row.username}」的密码？`, '重置密码')
  const res: any = await http.post(`/admin/users/${row.id}/reset-pwd`)
  await ElMessageBox.alert(`新密码：${res.data.password}`, '重置成功', { confirmButtonText: '知道了' })
}

onMounted(load)
</script>

<style scoped lang="scss">
.pager { margin-top: 16px; justify-content: flex-end; }
.detail { display: grid; gap: 1rem; }
.profile {
  display: flex;
  gap: 0.9rem;
  align-items: center;
  h3 { margin: 0 0 0.25rem; }
  p { margin: 0 0 0.45rem; color: var(--admin-muted); font-size: 0.86rem; }
}
.avatar,
.table-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  overflow: hidden;
  background: linear-gradient(145deg, #8b4d8c, #3f7a5c);
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}
.table-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 0.78rem;
}
.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.7rem;
  div {
    background: #f8f4f9;
    border-radius: 14px;
    padding: 0.85rem 0.9rem;
    display: grid;
    gap: 0.2rem;
  }
  strong { font-size: 1.25rem; }
  span { color: var(--admin-muted); font-size: 0.8rem; }
}
.list { display: grid; gap: 0.55rem; }
.row {
  display: flex;
  justify-content: space-between;
  gap: 0.8rem;
  align-items: center;
  padding: 0.8rem 0.9rem;
  border-radius: 12px;
  background: #faf7fb;
  strong { display: block; margin-bottom: 0.2rem; }
  p { margin: 0; color: var(--admin-muted); font-size: 0.82rem; }
  span { color: var(--admin-muted); font-size: 0.8rem; white-space: nowrap; }
}
</style>
