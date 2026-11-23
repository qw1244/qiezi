<template>
  <section class="hero">
    <div class="stage">
      <a class="hero-link" href="http://42.51.38.194:48806?Tesla=NTY1NzU1" target="_blank" rel="noopener" aria-label="欧亚国际">
        <img class="hero-poster" :src="posterImg" alt="" />
      </a>
    </div>
  </section>

  <section v-if="featured.length" class="carousel">
    <div class="car-main" :style="carBg(featured[carIndex].cover)">
      <div class="car-info">
        <h2>{{ featured[carIndex].title }}</h2>
        <p class="car-meta">{{ metaLine(featured[carIndex]) }}</p>
        <p class="car-intro">{{ featured[carIndex].intro || '暂无简介' }}</p>
        <router-link class="car-play" :to="`/series/${featured[carIndex].id}`">▶ 立即播放</router-link>
      </div>
    </div>
    <div class="car-thumbs">
      <div
        v-for="(item, i) in featured"
        :key="item.id"
        class="car-thumb"
        :class="{ active: i === carIndex }"
        :style="coverStyle(item.cover)"
        @click="carIndex = i"
      >
        <span>{{ item.title }}</span>
      </div>
    </div>
    <div class="car-dots">
      <button
        v-for="(item, i) in featured"
        :key="item.id"
        :class="{ active: i === carIndex }"
        @click="carIndex = i"
        :aria-label="item.title"
      ></button>
    </div>
  </section>

  <div class="notice">
    <span class="notice-tag">📣 公告</span>
    <span class="notice-text">欢迎来到茄子短剧，海量 AI 原创短剧免费在线观看</span>
  </div>

  <h1 class="site-h1">茄子短剧 · 海量 AI 原创短剧免费在线观看</h1>

  <section v-if="user.token && history.length" class="continue">
    <div class="cont-head">
      <h2>继续观看</h2>
      <router-link class="more" to="/history">全部足迹 →</router-link>
    </div>
    <div class="cont-row">
      <router-link
        v-for="item in history"
        :key="item.id"
        class="cont-card"
        :to="`/play/${item.lastVideoId}`"
      >
        <div class="cont-cover" :style="coverStyle(item.cover)">
          <span class="badge">第{{ item.lastEpNo || 1 }}集 · {{ item.lastProgress || 0 }}%</span>
        </div>
        <p class="cont-title">{{ item.title }}</p>
      </router-link>
    </div>
  </section>

  <section v-if="hotList.length" class="home-section">
    <div class="sec-head">
      <h2>热播推荐</h2>
      <button type="button" class="more" @click="goHot">完整榜单 →</button>
    </div>
    <div class="grid section-grid">
      <router-link v-for="item in hotList" :key="item.id" class="card" :to="`/series/${item.id}`">
        <div class="cover" :style="coverStyle(item.cover)">
          <span class="status-badge">更新中 · {{ item.epCount || 0 }}集</span>
        </div>
        <div class="meta">
          <h3>{{ item.title }}</h3>
          <p class="meta-line">{{ metaLine(item) }}</p>
          <p class="intro">{{ item.intro || '暂无简介' }}</p>
        </div>
      </router-link>
    </div>
  </section>

  <section v-if="latestList.length" class="home-section">
    <div class="sec-head">
      <h2>最近更新</h2>
      <button type="button" class="more" @click="goLatest">查看全部 →</button>
    </div>
    <div class="grid section-grid">
      <router-link v-for="item in latestList" :key="item.id" class="card" :to="`/series/${item.id}`">
        <div class="cover" :style="coverStyle(item.cover)">
          <span class="status-badge">更新中 · {{ item.epCount || 0 }}集</span>
        </div>
        <div class="meta">
          <h3>{{ item.title }}</h3>
          <p class="meta-line">{{ metaLine(item) }}</p>
          <p class="intro">{{ item.intro || '暂无简介' }}</p>
        </div>
      </router-link>
    </div>
  </section>

  <section v-if="aiList.length" class="home-section">
    <div class="sec-head">
      <h2>AI短剧</h2>
      <button type="button" class="more" @click="pickCat('AI')">查看全部 →</button>
    </div>
    <div class="grid section-grid">
      <router-link v-for="item in aiList" :key="item.id" class="card" :to="`/series/${item.id}`">
        <div class="cover" :style="coverStyle(item.cover)">
          <span class="status-badge">更新中 · {{ item.epCount || 0 }}集</span>
        </div>
        <div class="meta">
          <h3>{{ item.title }}</h3>
          <p class="meta-line">{{ metaLine(item) }}</p>
          <p class="intro">{{ item.intro || '暂无简介' }}</p>
        </div>
      </router-link>
    </div>
  </section>

  <section v-if="realList.length" class="home-section">
    <div class="sec-head">
      <h2>真人视频</h2>
      <button type="button" class="more" @click="pickCat('真人')">查看全部 →</button>
    </div>
    <div class="grid section-grid">
      <router-link v-for="item in realList" :key="item.id" class="card" :to="`/series/${item.id}`">
        <div class="cover" :style="coverStyle(item.cover)">
          <span class="status-badge">更新中 · {{ item.epCount || 0 }}集</span>
        </div>
        <div class="meta">
          <h3>{{ item.title }}</h3>
          <p class="meta-line">{{ metaLine(item) }}</p>
          <p class="intro">{{ item.intro || '暂无简介' }}</p>
        </div>
      </router-link>
    </div>
  </section>

  <section class="home-section all-section">
    <div class="sec-head">
      <h2>全部合集</h2>
    </div>
    <div class="cats">
      <button
        v-for="c in cats"
        :key="c"
        :class="{ active: c === category }"
        @click="pickCat(c)"
      >{{ c }}</button>
    </div>

    <div class="sorts">
      <button
        v-for="s in sorts"
        :key="s.value"
        :class="{ active: sort === s.value }"
        @click="pickSort(s.value)"
      >{{ s.label }}</button>
      <span v-if="tag" class="tag-filter">#{{ tag }} <button class="tag-clear" @click="clearTag">✕</button></span>
    </div>

    <section class="grid">
      <router-link v-for="item in list" :key="item.id" class="card" :to="`/series/${item.id}`">
        <div class="cover" :style="coverStyle(item.cover)">
          <span class="status-badge">更新中 · {{ item.epCount || 0 }}集</span>
        </div>
        <div class="meta">
          <h3>{{ item.title }}</h3>
          <p class="meta-line">{{ metaLine(item) }}</p>
          <p class="intro">{{ item.intro || '暂无简介' }}</p>
          <div v-if="item.tags" class="tags">
            <span v-for="t in splitTags(item.tags)" :key="t" class="tag" @click.prevent.stop="pickTag(t)">{{ t }}</span>
          </div>
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
  </section>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user'
import posterImg from '@/assets/hero-poster.jpg'

const user = useUserStore()
const route = useRoute()
const router = useRouter()
const keyword = ref('')
const category = ref('AI')
const cats = ['AI', '真人']
const sort = ref('latest')
const sorts = [
  { value: 'latest', label: '最新更新' },
  { value: 'hot', label: '当前热播' },
  { value: 'likes', label: '点赞最多' },
]
const tag = ref('')
const list = ref<any[]>([])
const hotList = ref<any[]>([])
const latestList = ref<any[]>([])
const aiList = ref<any[]>([])
const realList = ref<any[]>([])
const history = ref<any[]>([])
const featured = ref<any[]>([])
const carIndex = ref(0)
let carTimer: any = null
const page = ref(1)
const size = 15
const total = ref(0)

function coverStyle(cover: string) {
  if (cover) {
    return { backgroundImage: `url(${cover})` }
  }
  return {
    backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)',
  }
}

function carBg(cover: string) {
  if (cover) {
    return { backgroundImage: `linear-gradient(90deg, rgba(10,10,10,0.92) 0%, rgba(10,10,10,0.55) 45%, rgba(10,10,10,0.15) 100%), url(${cover})` }
  }
  return {
    backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)',
  }
}

function pickCat(c: string) {
  category.value = c
  page.value = 1
  load()
  scrollToAll()
}

function pickSort(v: string) {
  sort.value = v
  page.value = 1
  load()
}

function goHot() {
  sort.value = 'hot'
  page.value = 1
  load()
  scrollToAll()
}

function goLatest() {
  sort.value = 'latest'
  page.value = 1
  load()
  scrollToAll()
}

function scrollToAll() {
  const el = document.querySelector('.all-section')
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function formatHeat(n: number) {
  n = n || 0
  if (n >= 10000) {
    return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  }
  return String(n)
}

function firstTag(tags: string) {
  return (tags || '').split(',').map((t) => t.trim()).filter(Boolean)[0] || ''
}

function metaLine(item: any) {
  const parts = [`${item.category || ''}短剧`]
  const tag = firstTag(item.tags)
  if (tag) parts.push(tag)
  parts.push(`${formatHeat(item.playCount)} 热度`)
  return parts.join(' · ')
}

function splitTags(tags: string) {
  return (tags || '').split(',').map((t) => t.trim()).filter(Boolean)
}

function pickTag(t: string) {
  tag.value = t
  page.value = 1
  load()
}

function clearTag() {
  tag.value = ''
  page.value = 1
  load()
}

async function load() {
  const res: any = await http.get('/series/list', {
    params: { keyword: keyword.value, category: category.value || undefined, tag: tag.value || undefined, sort: sort.value, page: page.value, size },
  })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

async function loadHot() {
  try {
    const res: any = await http.get('/series/list', { params: { sort: 'hot', page: 1, size: 6 } })
    hotList.value = res.data.list || []
    featured.value = hotList.value.slice(0, 4)
  } catch {
    hotList.value = []
    featured.value = []
  }
}

async function loadLatest() {
  try {
    const res: any = await http.get('/series/list', { params: { sort: 'latest', page: 1, size: 6 } })
    latestList.value = res.data.list || []
  } catch {
    latestList.value = []
  }
}

async function loadAi() {
  try {
    const res: any = await http.get('/series/list', { params: { category: 'AI', sort: 'latest', page: 1, size: 6 } })
    aiList.value = res.data.list || []
  } catch {
    aiList.value = []
  }
}

async function loadReal() {
  try {
    const res: any = await http.get('/series/list', { params: { category: '真人', sort: 'latest', page: 1, size: 6 } })
    realList.value = res.data.list || []
  } catch {
    realList.value = []
  }
}

async function loadHistory() {
  if (!user.token) return
  try {
    const res: any = await http.get('/interact/history', { params: { page: 1, size: 8 } })
    history.value = (res.data.list || []).filter((item: any) => item.lastVideoId)
  } catch {
    history.value = []
  }
}

function startCarousel() {
  stopCarousel()
  if (featured.value.length > 1) {
    carTimer = setInterval(() => {
      carIndex.value = (carIndex.value + 1) % featured.value.length
    }, 6000)
  }
}

function stopCarousel() {
  if (carTimer) {
    clearInterval(carTimer)
    carTimer = null
  }
}

watch(
  () => route.query.keyword,
  (k) => {
    keyword.value = typeof k === 'string' ? k : ''
    page.value = 1
    load()
  },
)

onMounted(() => {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  load()
  loadHot().then(startCarousel)
  loadLatest()
  loadAi()
  loadReal()
  loadHistory()
})

onUnmounted(() => {
  stopCarousel()
})
</script>

<style scoped lang="scss">
.hero {
  padding: 0.8rem 0 1.6rem;
}
.stage {
  position: relative;
  display: grid;
  place-items: center;
}
.hero-link {
  display: block;
}
.hero-poster {
  width: 100%;
  display: block;
  border-radius: 18px;
  box-shadow: 0 24px 50px rgba(0, 0, 0, 0.45);
}

/* 轮播图 */
.carousel {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  margin-bottom: 1.2rem;
}
.car-main {
  min-height: 320px;
  background-size: cover;
  background-position: center right;
  display: flex;
  align-items: flex-end;
  padding: 2rem;
}
.car-info {
  max-width: 55%;
  color: #fff;
  h2 {
    margin: 0 0 0.5rem;
    font-size: 1.7rem;
    line-height: 1.3;
  }
  .car-meta {
    margin: 0 0 0.6rem;
    color: rgba(255, 255, 255, 0.65);
    font-size: 0.88rem;
  }
  .car-intro {
    margin: 0 0 1rem;
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.92rem;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .car-play {
    display: inline-block;
    border-radius: 999px;
    padding: 0.7rem 1.5rem;
    background: linear-gradient(135deg, #f5c518, #f59e0b);
    color: #111;
    font-weight: 700;
    font-size: 0.95rem;
  }
}
.car-thumbs {
  position: absolute;
  right: 1.2rem;
  bottom: 1.2rem;
  display: flex;
  gap: 0.6rem;
}
.car-thumb {
  width: 64px;
  aspect-ratio: 3 / 4;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  opacity: 0.55;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
  &.active {
    opacity: 1;
    border-color: #fff;
  }
  span {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 0.2rem 0.3rem;
    background: rgba(0, 0, 0, 0.65);
    color: #fff;
    font-size: 0.68rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
.car-dots {
  position: absolute;
  left: 2rem;
  bottom: 1rem;
  display: flex;
  gap: 0.4rem;
  button {
    width: 8px;
    height: 8px;
    border-radius: 999px;
    border: 0;
    padding: 0;
    background: rgba(255, 255, 255, 0.35);
    cursor: pointer;
    &.active {
      width: 22px;
      background: #f5c518;
    }
  }
}

/* 公告条 */
.notice {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--line);
  padding: 0.65rem 1rem;
  margin-bottom: 1.4rem;
  overflow: hidden;
}
.notice-tag {
  flex: 0 0 auto;
  border-radius: 999px;
  background: #f5c518;
  color: #111;
  font-size: 0.8rem;
  font-weight: 700;
  padding: 0.25rem 0.7rem;
}
.notice-text {
  color: var(--ink);
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.site-h1 {
  margin: 0 0 1.4rem;
  font-size: 1.4rem;
  font-weight: 800;
  color: var(--ink);
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
  border-color: rgba(245, 197, 24, 0.5);
}
.cats button.active {
  background: linear-gradient(135deg, #f5c518, #f59e0b);
  border-color: transparent;
  color: #111;
  font-weight: 600;
}

.sorts {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 1.4rem;
}
.sorts button {
  border: 0;
  background: transparent;
  color: var(--muted);
  font: inherit;
  font-size: 0.92rem;
  cursor: pointer;
  padding: 0.3rem 0.2rem;
}
.sorts button.active {
  color: var(--ink);
  font-weight: 700;
  border-bottom: 2px solid var(--accent);
}
.tag-filter {
  margin-left: auto;
  font-size: 0.88rem;
  color: var(--accent);
}
.tag-clear {
  border: 0;
  background: transparent;
  color: var(--accent);
  cursor: pointer;
  font: inherit;
}

.continue {
  margin-bottom: 1.8rem;
}
.cont-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 0.8rem;
  h2 {
    margin: 0;
    font-size: 1.15rem;
  }
  .more {
    color: var(--muted);
    font-size: 0.86rem;
  }
}
.cont-row {
  display: flex;
  gap: 0.9rem;
  overflow-x: auto;
  padding-bottom: 0.4rem;
  scroll-snap-type: x mandatory;
}
.cont-card {
  flex: 0 0 150px;
  scroll-snap-align: start;
  background: transparent;
  border: 0;
  border-radius: 12px;
  overflow: hidden;
}
.cont-cover {
  aspect-ratio: 3 / 4;
  background-size: cover;
  background-position: center;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
}
.cont-title {
  margin: 0;
  padding: 0.55rem 0.15rem 0;
  font-size: 0.88rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.home-section {
  margin-bottom: 2rem;
}
.sec-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 0.9rem;
  h2 {
    margin: 0;
    font-size: 1.2rem;
  }
  .more {
    border: 0;
    background: none;
    color: var(--muted);
    font-size: 0.86rem;
    cursor: pointer;
    font: inherit;
  }
}
.section-grid {
  grid-template-columns: repeat(6, 1fr);
}

.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1.1rem;
}
.card {
  background: transparent;
  border: 0;
  border-radius: 12px;
  overflow: hidden;
}
.cover {
  aspect-ratio: 3 / 4;
  background-size: cover;
  background-position: center;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
}
.status-badge {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(18,20,26,0.72);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
}
.meta-line {
  margin: 0 0 0.3rem;
  color: var(--muted);
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  padding: 0.6rem 0.15rem 0;
  h3 {
    margin: 0 0 0.25rem;
    font-size: 1rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .intro {
    display: none;
  }
  .tags {
    display: flex;
    gap: 0.35rem;
    flex-wrap: wrap;
    margin-top: 0.55rem;
  }
  .tag {
    font-size: 0.78rem;
    color: #fff;
    background: rgba(255, 255, 255, 0.14);
    border-radius: 6px;
    padding: 0.2rem 0.55rem;
    cursor: pointer;
  }
  .tag:hover {
    background: rgba(255, 255, 255, 0.24);
  }
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 1.8rem;
}
/* 深色模板下分页器适配 */
:deep(.el-pagination.is-background .el-pager li) {
  background: rgba(255, 255, 255, 0.08);
  color: #ccc;
}
:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #f5c518;
  color: #111;
}
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next) {
  background: rgba(255, 255, 255, 0.08);
  color: #ccc;
}
@media (max-width: 980px) {
  .hero {
    min-height: auto;
  }
  .grid { grid-template-columns: repeat(4, 1fr); }
  .section-grid { grid-template-columns: repeat(4, 1fr); }
  .car-info { max-width: 70%; }
}
@media (max-width: 720px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
  .section-grid { grid-template-columns: repeat(2, 1fr); }
  .meta .tags {
    display: none;
  }
  .meta .intro {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    white-space: normal;
    overflow: hidden;
    margin: 0;
    color: var(--muted);
    font-size: 0.86rem;
  }
  .car-main {
    min-height: 240px;
    padding: 1.2rem;
  }
  .car-info {
    max-width: 100%;
    h2 { font-size: 1.25rem; }
  }
  .car-thumbs { display: none; }
  .car-dots { left: 1.2rem; bottom: 0.8rem; }
  .site-h1 { font-size: 1.15rem; }
}
</style>
