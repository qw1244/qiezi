<template>
  <div v-if="detail" class="page">
    <div class="head">
      <div class="cover" :style="coverStyle(detail.cover)"></div>
      <div class="info">
        <h1>{{ detail.title }}</h1>
        <p class="intro">{{ detail.intro || '暂无简介' }}</p>
        <div class="stats">
          <span>{{ detail.epCount || 0 }} 集</span>
          <span>{{ detail.playCount || 0 }} 播放</span>
          <span>{{ detail.favCount || 0 }} 收藏</span>
        </div>
        <div class="ops">
          <el-button type="primary" @click="goPlay">
            {{ detail.resumeVideoId ? '继续观看' : '开始观看' }}
          </el-button>
          <el-button @click="toggleFav">{{ detail.fav ? '已收藏' : '收藏合集' }}</el-button>
        </div>
        <p v-if="detail.firstProgress" class="progress">第一集进度 {{ detail.firstProgress }}%</p>
      </div>
    </div>

    <h2 class="sec">选集</h2>
    <div class="eps">
      <button
        v-for="v in detail.videos || []"
        :key="v.id"
        class="ep"
        @click="$router.push(`/play/${v.id}`)"
      >
        <span>第{{ v.epNo }}集</span>
        <small v-if="v.progress">{{ pct(v) }}%</small>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const detail = ref<any>(null)

function coverStyle(cover: string) {
  return cover
    ? { backgroundImage: `url(${cover})` }
    : { backgroundImage: 'linear-gradient(145deg, #1f6f8b, #d4552a)' }
}

function pct(v: any) {
  if (!v.duration) return 0
  return Math.min(100, Math.round((v.progress || 0) * 100 / v.duration))
}

async function load() {
  const res: any = await http.get(`/series/${route.params.id}`)
  detail.value = res.data
}

function goPlay() {
  if (!detail.value) return
  const id = detail.value.resumeVideoId || detail.value.videos?.[0]?.id
  if (!id) {
    ElMessage.warning('暂无剧集')
    return
  }
  router.push(`/play/${id}`)
}

async function toggleFav() {
  if (!user.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (detail.value.fav) {
    await http.delete(`/interact/fav/${detail.value.id}`)
    detail.value.fav = false
    detail.value.favCount = Math.max(0, (detail.value.favCount || 1) - 1)
  } else {
    await http.post(`/interact/fav/${detail.value.id}`)
    detail.value.fav = true
    detail.value.favCount = (detail.value.favCount || 0) + 1
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.page { display: grid; gap: 1.4rem; }
.head {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 1.4rem;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 1rem;
}
.cover {
  border-radius: 14px;
  aspect-ratio: 3/4;
  background-size: cover;
  background-position: center;
}
.info {
  h1 { margin: 0.2rem 0 0.8rem; font-size: clamp(1.6rem, 4vw, 2.4rem); }
  .intro { color: var(--muted); line-height: 1.7; }
}
.stats {
  display: flex;
  gap: 1rem;
  margin: 1rem 0;
  color: var(--muted);
  font-size: 0.92rem;
}
.ops { display: flex; gap: 0.6rem; flex-wrap: wrap; }
.progress { color: var(--accent); font-weight: 600; }
.sec { margin: 0.4rem 0; font-size: 1.15rem; }
.eps {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 0.7rem;
}
.ep {
  border: 1px solid var(--line);
  background: var(--card);
  border-radius: 12px;
  padding: 0.85rem 0.7rem;
  cursor: pointer;
  display: grid;
  gap: 0.25rem;
  text-align: left;
  small { color: var(--accent); }
}
@media (max-width: 720px) {
  .head { grid-template-columns: 1fr; }
  .cover { max-width: 220px; }
}
</style>
