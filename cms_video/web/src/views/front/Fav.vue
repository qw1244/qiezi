<template>
  <section>
    <h1>我的收藏</h1>
    <p class="tip">收藏的是合集。卡片上会显示第一集观看进度，点进去可接着看。</p>

    <div v-if="!user.token" class="guest">
      <p>登录后可收藏合集，并同步第一集进度。</p>
      <router-link class="cta" to="/login?redirect=/fav">去登录</router-link>
    </div>

    <template v-else>
      <div class="grid">
        <router-link v-for="item in list" :key="item.id" class="card" :to="`/series/${item.id}`">
          <div class="cover" :style="coverStyle(item.cover)">
            <span class="badge">第一集 {{ item.firstProgress || 0 }}%</span>
          </div>
          <div class="meta">
            <h3>{{ item.title }}</h3>
            <p>{{ item.epCount || 0 }} 集</p>
          </div>
        </router-link>
      </div>
      <el-empty v-if="!list.length" description="还没有收藏" />
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
    : { backgroundImage: 'linear-gradient(145deg, #1f6f8b, #d4552a)' }
}

function formatHeat(n: number) {
  n = n || 0
  if (n >= 10000) {
    return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  }
  return String(n)
}

async function loadRecommend() {
  try {
    const res: any = await http.get('/series/list', { params: { sort: 'hot', page: 1, size: 9 } })
    const favIds = new Set((list.value || []).map((s: any) => s.id))
    const items = (res.data.list || []).filter((s: any) => !favIds.has(s.id))
    recommend.value = items.slice(0, 8)
  } catch {
    recommend.value = []
  }
}

onMounted(async () => {
  if (user.token) {
    const res: any = await http.get('/interact/fav', { params: { page: 1, size: 50 } })
    list.value = res.data.list || []
  }
  loadRecommend()
})
</script>

<style scoped lang="scss">
h1 { margin: 0 0 0.4rem; }
.tip { color: var(--muted); margin: 0 0 1.2rem; }
.guest {
  background: var(--card);
  border: 1px dashed var(--line);
  border-radius: var(--radius);
  padding: 2rem 1.2rem;
  text-align: center;
  p { margin: 0 0 1rem; color: var(--muted); }
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
  gap: 1rem;
}
.card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}
.cover {
  aspect-ratio: 3/4;
  background-size: cover;
  background-position: center;
  position: relative;
}
.badge {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(18,20,26,0.75);
  color: #fff;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 12px;
}
.meta { padding: 0.9rem 1rem; }
h3 { margin: 0 0 0.3rem; font-size: 1rem; }
p { margin: 0; color: var(--muted); font-size: 0.86rem; }
.recommend {
  margin-top: 2rem;
}
.sec { margin: 0 0 0.9rem; font-size: 1.15rem; }
.rec-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
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
  aspect-ratio: 3/4;
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
.rec-meta { padding: 0.7rem 0.8rem 0.85rem; }
.rec-meta h3 {
  margin: 0 0 0.25rem;
  font-size: 0.92rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-meta p { margin: 0; color: var(--muted); font-size: 0.8rem; }
@media (max-width: 980px) { .grid { grid-template-columns: repeat(3, 1fr); } .rec-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 720px) { .grid { grid-template-columns: repeat(2, 1fr); } .rec-grid { grid-template-columns: repeat(3, 1fr); } }
</style>
