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
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const list = ref<any[]>([])

function coverStyle(cover: string) {
  return cover
    ? { backgroundImage: `url(${cover})` }
    : { backgroundImage: 'linear-gradient(145deg, #1f6f8b, #d4552a)' }
}

onMounted(async () => {
  if (!user.token) return
  const res: any = await http.get('/interact/fav', { params: { page: 1, size: 50 } })
  list.value = res.data.list || []
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
@media (max-width: 980px) { .grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 720px) { .grid { grid-template-columns: repeat(2, 1fr); } }
</style>
