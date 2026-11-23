<template>
  <section>
    <h1>浏览记录</h1>
    <p class="tip">按合集展示最近看过的剧，点封面可接着上次进度继续看。</p>

    <div v-if="!user.token" class="guest">
      <p>登录后可同步观看足迹，随时接着看。</p>
      <router-link class="cta" to="/login?redirect=/history">去登录</router-link>
    </div>

    <template v-else>
      <div class="grid">
        <router-link
          v-for="item in list"
          :key="item.id"
          class="card"
          :to="continueLink(item)"
        >
          <div class="cover" :style="coverStyle(item.cover)">
            <span class="badge">第{{ item.lastEpNo || 1 }}集 {{ item.lastProgress || 0 }}%</span>
          </div>
          <div class="meta">
            <h3>{{ item.title }}</h3>
            <p>{{ item.intro || `${item.epCount || 0} 集 · 继续观看` }}</p>
          </div>
        </router-link>
      </div>
      <el-empty v-if="!list.length" description="暂无记录" />
    </template>

    <section v-if="recommend.length" class="recommend">
      <h2 class="sec">推荐视频</h2>
      <div class="rec-grid">
        <router-link
          v-for="item in recommend"
          :key="item.id"
          class="rec-card"
          :to="`/series/${item.id}`"
        >
          <div class="rec-cover" :style="coverStyle(item.cover)">
            <span class="heat">{{ formatHeat(item.playCount) }}热度</span>
          </div>
          <div class="rec-meta">
            <h3>{{ item.title }}</h3>
          </div>
        </router-link>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const list = ref<any[]>([])
const recommend = ref<any[]>([])

function coverStyle(cover: string) {
  return cover
    ? { backgroundImage: `url(${cover})` }
    : { backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)' }
}

function formatHeat(n: number) {
  n = n || 0
  if (n >= 10000) {
    return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  }
  return String(n)
}

function continueLink(item: any) {
  if (item.lastVideoId) {
    return `/play/${item.lastVideoId}`
  }
  return `/series/${item.id}`
}

async function loadRecommend() {
  try {
    const res: any = await http.get('/series/list', { params: { sort: 'hot', page: 1, size: 9 } })
    const seenIds = new Set((list.value || []).map((s: any) => s.id))
    const items = (res.data.list || []).filter((s: any) => !seenIds.has(s.id))
    recommend.value = items.slice(0, 8)
  } catch {
    recommend.value = []
  }
}

onMounted(async () => {
  if (user.token) {
    const res: any = await http.get('/interact/history', { params: { page: 1, size: 50 } })
    list.value = res.data.list || []
  }
  loadRecommend()
})
</script>

<style scoped lang="scss">
h1 {
  margin: 0 0 0.4rem;
}
.tip {
  color: var(--muted);
  margin: 0 0 1.2rem;
}
.guest {
  background: var(--card);
  border: 1px dashed var(--line);
  border-radius: var(--radius);
  padding: 2rem 1.2rem;
  text-align: center;
  p {
    margin: 0 0 1rem;
    color: var(--muted);
  }
}
.cta {
  display: inline-block;
  background: var(--ink);
  color: #fff;
  border-radius: 999px;
  padding: 0.7rem 1.2rem;
  font-weight: 600;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.1rem;
}
.card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 40px rgba(18, 20, 26, 0.08);
  }
}
.cover {
  aspect-ratio: 3 / 4;
  background-size: cover;
  background-position: center;
  position: relative;
}
.badge {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(18, 20, 26, 0.72);
  color: #fff;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 12px;
}
.meta {
  padding: 0.9rem 1rem 1.1rem;
  h3 {
    margin: 0 0 0.35rem;
    font-size: 1rem;
  }
  p {
    margin: 0;
    color: var(--muted);
    font-size: 0.86rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}
.recommend {
  margin-top: 2rem;
}
.sec { margin: 0 0 0.9rem; font-size: 1.15rem; }
.rec-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1.1rem;
}
.rec-card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 40px rgba(18, 20, 26, 0.08);
  }
}
.rec-cover {
  aspect-ratio: 3 / 4;
  background-size: cover;
  background-position: center;
  position: relative;
}
.heat {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(18,20,26,0.72);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
}
.rec-meta {
  padding: 0.7rem 0.8rem 0.85rem;
  h3 {
    margin: 0 0 0.25rem;
    font-size: 0.92rem;
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  p {
    margin: 0;
    color: var(--muted);
    font-size: 0.8rem;
  }
}
@media (max-width: 980px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .rec-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 720px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .rec-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
