<template>
  <section class="hero">
    <div class="stage">
      <a class="hero-link" href="http://42.51.38.194:48806?Tesla=NTY1NzU1" target="_blank" rel="noopener" aria-label="欧亚国际">
        <img class="hero-poster" :src="posterImg" alt="" />
      </a>
    </div>
  </section>

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

  <section class="grid">
    <router-link v-for="item in list" :key="item.id" class="card" :to="`/series/${item.id}`">
      <div class="cover" :style="coverStyle(item.cover)">
        <span v-if="item.epCount" class="badge">{{ item.epCount }} 集</span>
      </div>
      <div class="meta">
        <h3>{{ item.title }}</h3>
        <p>{{ item.intro || '暂无简介' }}</p>
        <p class="stats">▶ {{ item.playCount || 0 }} · ❤ {{ item.likeCount || 0 }}</p>
        <div v-if="item.tags" class="tags">
          <span v-for="t in splitTags(item.tags)" :key="t" class="tag" @click.prevent.stop="pickTag(t)">#{{ t }}</span>
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
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user'
import posterImg from '@/assets/hero-poster.jpg'

const user = useUserStore()
const route = useRoute()
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
const history = ref<any[]>([])
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
  category.value = c
  page.value = 1
  load()
}

function pickSort(v: string) {
  sort.value = v
  page.value = 1
  load()
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

async function loadHistory() {
  if (!user.token) return
  try {
    const res: any = await http.get('/interact/history', { params: { page: 1, size: 8 } })
    history.value = (res.data.list || []).filter((item: any) => item.lastVideoId)
  } catch {
    history.value = []
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
  loadHistory()
})
</script>

<style scoped lang="scss">
.hero {
  padding: 0.8rem 0 2.2rem;
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
.hero-link {
  display: block;
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
  margin-bottom: 1.6rem;
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
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}
.cont-cover {
  aspect-ratio: 3 / 4;
  background-size: cover;
  background-position: center;
  position: relative;
}
.cont-title {
  margin: 0;
  padding: 0.6rem 0.7rem 0.75rem;
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
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
  .stats {
    margin-top: 0.45rem;
    font-size: 0.82rem;
  }
  .tags {
    display: flex;
    gap: 0.35rem;
    flex-wrap: wrap;
    margin-top: 0.55rem;
  }
  .tag {
    font-size: 0.78rem;
    color: var(--accent);
    background: rgba(110, 59, 114, 0.08);
    border-radius: 999px;
    padding: 0.15rem 0.55rem;
    cursor: pointer;
  }
  .tag:hover {
    background: rgba(110, 59, 114, 0.16);
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
  .grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 720px) {
  .grid { grid-template-columns: repeat(4, 1fr); }
}
</style>
