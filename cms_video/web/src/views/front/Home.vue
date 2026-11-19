<template>
  <section class="hero">
    <div class="copy">
      <p class="eyebrow">SHORT DRAMA</p>
      <h1>QIEZI</h1>
      <p class="desc">竖屏短剧，一口气看到爽。收藏合集，接着上次进度继续看。</p>
      <form class="search" @submit.prevent="load">
        <span class="search-ico" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" stroke-linecap="round" />
          </svg>
        </span>
        <input
          v-model="keyword"
          type="search"
          placeholder="搜索合集名称"
          autocomplete="off"
        />
        <button type="submit">搜索</button>
      </form>
    </div>

    <div class="stage" aria-hidden="true">
      <img class="hero-poster" :src="posterImg" alt="" />
    </div>
  </section>

  <div class="cats">
    <button
      v-for="c in cats"
      :key="c"
      :class="{ active: (c === '全部' ? '' : c) === category }"
      @click="pickCat(c)"
    >{{ c }}</button>
  </div>

  <section class="grid">
    <router-link v-for="item in list" :key="item.id" class="card" :to="`/series/${item.id}`">
      <div class="cover" :style="coverStyle(item.cover)">
        <span v-if="item.epCount" class="badge">{{ item.epCount }} 集</span>
      </div>
      <div class="meta">
        <h3>{{ item.title }}</h3>
        <p>{{ item.intro || '暂无简介' }}</p>
      </div>
    </router-link>
  </section>

  <div class="pager" v-if="total > size">
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="size"
      v-model:current-page="page"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/utils/http'
import posterImg from '@/assets/hero-poster.jpg'

const keyword = ref('')
const category = ref('')
const cats = ['全部', '真人', 'AI']
const list = ref<any[]>([])
const page = ref(1)
const size = 12
const total = ref(0)

function coverStyle(cover: string) {
  if (cover) {
    return { backgroundImage: `url(${cover})` }
  }
  return {
    backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)',
  }
}

function pickCat(c: string) {
  category.value = c === '全部' ? '' : c
  page.value = 1
  load()
}

async function load() {
  const res: any = await http.get('/series/list', {
    params: { keyword: keyword.value, category: category.value || undefined, page: page.value, size },
  })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

onMounted(load)
</script>

<style scoped lang="scss">
.hero {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 1.5rem;
  align-items: center;
  padding: 0.8rem 0 2.2rem;
  min-height: min(58vh, 520px);
}
.copy {
  display: grid;
  gap: 0.95rem;
  align-content: center;
}
.eyebrow {
  margin: 0;
  letter-spacing: 0.18em;
  color: var(--accent);
  font-size: 0.78rem;
  font-weight: 700;
}
h1 {
  margin: 0;
  font-family: Outfit, 'Noto Sans SC', sans-serif;
  font-size: clamp(3rem, 9vw, 5rem);
  line-height: 0.95;
  letter-spacing: 0.08em;
}
.desc {
  margin: 0;
  max-width: 28rem;
  color: var(--muted);
  font-size: 1.05rem;
  line-height: 1.7;
}
.search {
  max-width: 440px;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.35rem 0.35rem 0.95rem;
  border-radius: 999px;
  background: rgba(255, 253, 249, 0.92);
  border: 1px solid rgba(110, 59, 114, 0.12);
  box-shadow:
    0 10px 28px rgba(43, 24, 51, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.search:focus-within {
  border-color: rgba(110, 59, 114, 0.35);
  box-shadow:
    0 12px 32px rgba(110, 59, 114, 0.12),
    0 0 0 4px rgba(110, 59, 114, 0.08);
}
.search-ico {
  display: grid;
  place-items: center;
  color: var(--accent);
  flex: 0 0 auto;
}
.search input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 0.98rem;
  color: var(--ink);
  padding: 0.55rem 0.35rem;
}
.search input::placeholder {
  color: #9a92a4;
}
.search button {
  border: 0;
  border-radius: 999px;
  padding: 0.72rem 1.15rem;
  background: linear-gradient(135deg, #6e3b72, #4a2952);
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: 0.04em;
  transition: transform 0.18s ease, opacity 0.18s ease;
}
.search button:hover {
  transform: translateY(-1px);
  opacity: 0.95;
}

.stage {
  position: relative;
  display: grid;
  place-items: center;
}
.hero-poster {
  width: 100%;
  display: block;
  border-radius: 18px;
  box-shadow: 0 24px 50px rgba(28, 20, 36, 0.18);
}

.cats {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
  margin-bottom: 1.4rem;
}
.cats button {
  border: 1px solid var(--line);
  background: var(--card);
  color: var(--ink);
  border-radius: 999px;
  padding: 0.5rem 1.2rem;
  font: inherit;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.18s ease;
}
.cats button:hover {
  border-color: rgba(110, 59, 114, 0.4);
}
.cats button.active {
  background: linear-gradient(135deg, #6e3b72, #4a2952);
  border-color: transparent;
  color: #fff;
  font-weight: 600;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.1rem;
}
.card {
  background: var(--card);
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--line);
  transition: transform .2s ease, box-shadow .2s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 40px rgba(18,20,26,0.08);
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
  background: rgba(18,20,26,0.72);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
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
.pager {
  display: flex;
  justify-content: center;
  margin-top: 1.8rem;
}
@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 720px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
