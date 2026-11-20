<template>
  <div v-if="video" class="theater" :class="orient">
    <div class="workspace">
      <section class="screen">
        <div class="stage-slot">
          <Transition :name="slideName">
            <div :key="String(video.id)" class="slide-layer">
              <video
                :ref="bindPlayer"
                class="player"
                controls
                controlslist="nodownload"
                playsinline
                autoplay
                :src="video.playUrl"
                @timeupdate="onTime"
                @loadedmetadata="onMeta"
                @ended="onEnded"
                @contextmenu.prevent
              />
              <div v-if="tip" class="toast">{{ tip }}</div>
              <div class="watermark">免费看qz53.com</div>
            </div>
          </Transition>

          <div class="top-tools">
            <button type="button" class="tool" @click="toggleOrient">
              {{ orient === 'portrait' ? '横屏' : '竖屏' }}
            </button>
            <button type="button" class="tool" :class="{ on: autoplay }" @click="autoplay = !autoplay">
              {{ autoplay ? '连播' : '单集' }}
            </button>
          </div>

          <div class="rail">
            <button type="button" class="rail-btn" :class="{ liked: video.liked }" @click="toggleLike">
              <span class="rail-icon">♥</span>
              <span class="rail-count">{{ video.likeCount || 0 }}</span>
            </button>
            <button type="button" class="rail-btn" :class="{ liked: fav }" @click="toggleFav">
              <span class="rail-icon">★</span>
              <span class="rail-count">{{ favCount }}</span>
            </button>
            <button type="button" class="rail-btn" @click="scrollToComments">
              <span class="rail-icon">💬</span>
              <span class="rail-count">{{ commentTotal }}</span>
            </button>
            <button type="button" class="rail-btn" @click="shareVideo">
              <span class="rail-icon">↗</span>
              <span class="rail-count">分享</span>
            </button>
          </div>
        </div>
      </section>

      <aside class="panel">
        <div class="panel-head">
          <p class="eyebrow">正在播放</p>
          <h1>{{ displayTitle }}</h1>
          <p class="meta">
            第 {{ video.epNo }} 集
            <span>·</span>
            {{ episodes.length }} 集全
            <span>·</span>
            {{ video.likeCount || 0 }} 赞
            <span>·</span>
            {{ favCount }} 收藏
            <span>·</span>
            {{ commentTotal }} 评论
          </p>
          <p v-if="seriesIntro" class="intro">{{ seriesIntro }}</p>
        </div>

        <div class="actions">
          <button type="button" class="act" @click="$router.push(`/series/${video.seriesId}`)">详情</button>
          <button type="button" class="act ghost" @click="playPrev" :disabled="!prevEp || animating">上一集</button>
          <button type="button" class="act primary" @click="playNext(false)" :disabled="animating">下一集</button>
        </div>

        <div class="ep-block">
          <div class="ep-head">
            <strong>选集</strong>
            <span>↑↓ 切集 · 空格暂停</span>
          </div>
          <div class="ep-grid">
            <button
              v-for="ep in episodes"
              :key="ep.id"
              type="button"
              class="ep"
              :class="{ active: ep.id === video.id }"
              :disabled="animating"
              @click="goEp(ep.id)"
            >
              {{ ep.epNo }}
            </button>
          </div>
        </div>
      </aside>
    </div>

    <section class="comments">
      <div class="comments-inner">
        <div class="cmt-head">
          <strong>评论</strong>
          <span>{{ commentTotal }} 条</span>
        </div>
        <div class="cmt-post">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="写下你的评论…"
          />
          <el-button type="primary" :loading="posting" :disabled="!commentText.trim()" @click="postComment">
            发布
          </el-button>
        </div>
        <div v-if="!comments.length" class="cmt-empty">暂无评论，快来抢沙发</div>
        <ul v-else class="cmt-list">
          <li v-for="c in comments" :key="c.id" class="cmt">
            <span class="cmt-avatar">{{ (c.nickname || '匿').slice(0, 1) }}</span>
            <div class="cmt-body">
              <p class="cmt-name">
                {{ c.nickname || '匿名用户' }}
                <span class="cmt-time">{{ formatTime(c.createdAt) }}</span>
              </p>
              <p class="cmt-text">{{ c.content }}</p>
            </div>
            <el-button v-if="c.mine" link type="danger" size="small" @click="deleteComment(c)">删除</el-button>
          </li>
        </ul>
        <div v-if="commentTotal > comments.length" class="cmt-more">
          <el-button link @click="loadComments(commentPage + 1)">加载更多</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const video = ref<any>(null)
const episodes = ref<any[]>([])
const seriesIntro = ref('')
const player = ref<HTMLVideoElement | null>(null)
const orient = ref<'landscape' | 'portrait'>('portrait')
const autoplay = ref(localStorage.getItem('qiezi_autoplay') !== '0')
const tip = ref('')
const fav = ref(false)
const favCount = ref(0)
const comments = ref<any[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentText = ref('')
const posting = ref(false)
const slideName = ref<'slide-next' | 'slide-prev'>('slide-next')
const animating = ref(false)
let timer: number | undefined
let lastSent = 0
let tipTimer: number | undefined
let switching = false

const SLIDE_MS = 480

const displayTitle = computed(() => {
  if (!video.value) return ''
  return video.value.seriesTitle || video.value.title || `第${video.value.epNo}集`
})

const currentIndex = computed(() =>
  episodes.value.findIndex((e) => e.id === video.value?.id),
)

const prevEp = computed(() =>
  currentIndex.value > 0 ? episodes.value[currentIndex.value - 1] : null,
)

const nextEp = computed(() =>
  currentIndex.value >= 0 && currentIndex.value < episodes.value.length - 1
    ? episodes.value[currentIndex.value + 1]
    : null,
)

watch(autoplay, (v) => localStorage.setItem('qiezi_autoplay', v ? '1' : '0'))
watch(() => route.params.id, () => load())

function bindPlayer(el: Element | null) {
  player.value = (el as HTMLVideoElement) || null
}

function toggleOrient() {
  orient.value = orient.value === 'portrait' ? 'landscape' : 'portrait'
}

function togglePlay() {
  const el = player.value
  if (!el) return
  if (el.paused) el.play().catch(() => undefined)
  else el.pause()
}

function showTip(text: string) {
  tip.value = text
  if (tipTimer) window.clearTimeout(tipTimer)
  tipTimer = window.setTimeout(() => {
    tip.value = ''
  }, 1400)
}

function setSlideDir(dir: 'next' | 'prev') {
  slideName.value = dir === 'next' ? 'slide-next' : 'slide-prev'
}

function goEp(id: number | string, dir?: 'next' | 'prev') {
  if (!id || String(id) === String(route.params.id) || animating.value) return
  if (!dir && video.value) {
    const target = episodes.value.find((e) => String(e.id) === String(id))
    if (target && target.epNo > (video.value.epNo || 0)) dir = 'next'
    else dir = 'prev'
  }
  setSlideDir(dir || 'next')
  animating.value = true
  window.setTimeout(() => {
    animating.value = false
  }, SLIDE_MS + 40)
  router.push(`/play/${id}`)
}

async function load() {
  switching = false
  lastSent = 0
  const res: any = await http.get(`/video/play/${route.params.id}`)
  video.value = res.data
  await loadEpisodes(res.data.seriesId)
  await loadComments(1)
  await nextTick()
  if (player.value) {
    if (video.value.progress > 0) {
      player.value.currentTime = video.value.progress
    }
    player.value.play().catch(() => undefined)
  }
}

async function loadEpisodes(seriesId: number) {
  if (!seriesId) {
    episodes.value = []
    seriesIntro.value = ''
    return
  }
  const res: any = await http.get(`/series/${seriesId}`)
  episodes.value = res.data?.videos || []
  seriesIntro.value = res.data?.intro || ''
  fav.value = !!res.data?.fav
  favCount.value = res.data?.favCount || 0
}

function onMeta() {
  if (player.value && video.value?.progress > 3) {
    player.value.currentTime = video.value.progress
  }
}

function onTime() {
  if (!user.token || !player.value || !video.value) return
  const now = Math.floor(player.value.currentTime)
  if (Math.abs(now - lastSent) < 5) return
  lastSent = now
  http.post('/interact/watch', {
    videoId: video.value.id,
    progress: now,
    duration: Math.floor(player.value.duration || video.value.duration || 0),
  }).catch(() => undefined)
}

async function onEnded() {
  if (!autoplay.value) return
  await playNext(true)
}

async function playPrev() {
  if (animating.value) return
  if (!prevEp.value) {
    showTip('已经是第一集')
    return
  }
  showTip(`上一集 · 第${prevEp.value.epNo}集`)
  goEp(prevEp.value.id, 'prev')
}

async function playNext(fromAuto: boolean) {
  if (switching || animating.value) return
  if (nextEp.value) {
    showTip(`下一集 · 第${nextEp.value.epNo}集`)
    goEp(nextEp.value.id, 'next')
    return
  }
  if (!fromAuto && !autoplay.value) {
    showTip('已经是最后一集')
    return
  }
  await playOtherSeries()
}

async function playOtherSeries() {
  if (switching || animating.value) return
  switching = true
  try {
    showTip('本剧已看完，切换其他剧…')
    const listRes: any = await http.get('/series/list', { params: { page: 1, size: 30 } })
    const list = (listRes.data?.list || []).filter((s: any) => s.id !== video.value.seriesId)
    if (!list.length) {
      showTip('暂无其他可播合集')
      return
    }
    const pick = list[Math.floor(Math.random() * Math.min(list.length, 8))]
    const detail: any = await http.get(`/series/${pick.id}`)
    const first = detail.data?.videos?.[0]
    if (!first?.id) {
      showTip('合集暂无分集')
      return
    }
    showTip(`开始播放《${pick.title}》`)
    goEp(first.id, 'next')
  } finally {
    switching = false
  }
}

async function toggleLike() {
  if (!user.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (video.value.liked) {
    await http.delete(`/interact/like/${video.value.id}`)
    video.value.liked = false
    video.value.likeCount = Math.max(0, (video.value.likeCount || 1) - 1)
  } else {
    await http.post(`/interact/like/${video.value.id}`)
    video.value.liked = true
    video.value.likeCount = (video.value.likeCount || 0) + 1
  }
}

async function toggleFav() {
  if (!user.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const seriesId = video.value.seriesId
  if (!seriesId) return
  if (fav.value) {
    await http.delete(`/interact/fav/${seriesId}`)
    fav.value = false
    favCount.value = Math.max(0, favCount.value - 1)
  } else {
    await http.post(`/interact/fav/${seriesId}`)
    fav.value = true
    favCount.value = favCount.value + 1
  }
}

async function shareVideo() {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
    showTip('链接已复制，快去分享吧')
  } catch {
    const ta = document.createElement('textarea')
    ta.value = url
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    try {
      document.execCommand('copy')
      showTip('链接已复制，快去分享吧')
    } catch {
      showTip(url)
    }
    document.body.removeChild(ta)
  }
}

function scrollToComments() {
  const el = document.querySelector('.comments')
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  const ta = document.querySelector('.cmt-post textarea') as HTMLTextAreaElement | null
  if (ta) window.setTimeout(() => ta.focus(), 500)
}

async function loadComments(page: number) {
  if (!video.value?.id) return
  const res: any = await http.get(`/comments/video/${video.value.id}`, {
    params: { page, size: 10 },
  })
  commentPage.value = page
  commentTotal.value = res.data?.total || 0
  const list = res.data?.list || []
  comments.value = page === 1 ? list : [...comments.value, ...list]
}

async function postComment() {
  if (!user.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const text = commentText.value.trim()
  if (!text || posting.value) return
  posting.value = true
  try {
    const res: any = await http.post('/comments', { videoId: video.value.id, content: text })
    if (res.data) {
      comments.value = [res.data, ...comments.value]
      commentTotal.value = commentTotal.value + 1
    } else {
      await loadComments(1)
    }
    commentText.value = ''
  } finally {
    posting.value = false
  }
}

async function deleteComment(c: any) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await http.delete(`/comments/${c.id}`)
  comments.value = comments.value.filter((item: any) => item.id !== c.id)
  commentTotal.value = Math.max(0, commentTotal.value - 1)
}

function formatTime(t: string) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

function onKey(e: KeyboardEvent) {
  const tag = (e.target as HTMLElement)?.tagName
  if (tag === 'INPUT' || tag === 'TEXTAREA') return
  if (e.key === 'ArrowDown' || e.key === 'PageDown') {
    e.preventDefault()
    e.stopPropagation()
    playNext(false)
  } else if (e.key === 'ArrowUp' || e.key === 'PageUp') {
    e.preventDefault()
    e.stopPropagation()
    playPrev()
  } else if (e.key === ' ' || e.key === 'k' || e.key === 'K') {
    
    e.preventDefault()
    e.stopPropagation()
    togglePlay()
  }
}

function onWheel(e: WheelEvent) {
  if (animating.value) return
  if (Math.abs(e.deltaY) < 40) return
  e.preventDefault()
  if (e.deltaY > 0) playNext(false)
  else playPrev()
}

onMounted(() => {
  load()
  timer = window.setInterval(() => onTime(), 8000)
  window.addEventListener('keydown', onKey, true)
  window.addEventListener('wheel', onWheel, { passive: false })
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  if (tipTimer) clearTimeout(tipTimer)
  window.removeEventListener('keydown', onKey, true)
  window.removeEventListener('wheel', onWheel)
  onTime()
})
</script>

<style scoped lang="scss">
.theater {
  min-height: calc(100vh - 64px);
  color: #f4f1ea;
  background:
    radial-gradient(ellipse at 20% 0%, rgba(254, 44, 85, 0.12), transparent 42%),
    radial-gradient(ellipse at 90% 20%, rgba(110, 59, 114, 0.18), transparent 36%),
    #0b0b0f;
}
.workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 0;
  min-height: calc(100vh - 64px);
  max-width: 1440px;
  margin: 0 auto;
}
.screen {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.25rem 1rem 1.5rem 1.5rem;
  min-width: 0;
}
.stage-slot {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: 12px;
  background: #000;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
}
.theater.portrait .stage-slot {
  aspect-ratio: 9 / 16;
  max-width: min(480px, 100%);
  max-height: calc(100vh - 110px);
  margin: 0 auto;
}
.theater.landscape .stage-slot {
  aspect-ratio: 16 / 9;
  max-height: calc(100vh - 110px);
}
.slide-layer {
  position: absolute;
  inset: 0;
  background: #000;
  will-change: transform;
}
.player {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  background: #000;
}

.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
  transition:
    transform 0.48s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.48s cubic-bezier(0.22, 1, 0.36, 1);
}
.slide-next-enter-from {
  transform: translateY(100%);
  opacity: 0.55;
}
.slide-next-enter-to,
.slide-next-leave-from {
  transform: translateY(0);
  opacity: 1;
}
.slide-next-leave-to {
  transform: translateY(-100%);
  opacity: 0.35;
}

.slide-prev-enter-from {
  transform: translateY(-100%);
  opacity: 0.55;
}
.slide-prev-enter-to,
.slide-prev-leave-from {
  transform: translateY(0);
  opacity: 1;
}
.slide-prev-leave-to {
  transform: translateY(100%);
  opacity: 0.35;
}

.top-tools {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 5;
  display: flex;
  gap: 0.4rem;
}
.tool {
  border: 0;
  border-radius: 999px;
  padding: 0.38rem 0.75rem;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 0.78rem;
  cursor: pointer;
  backdrop-filter: blur(8px);
}
.tool.on {
  background: rgba(254, 44, 85, 0.92);
}
.rail {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 6;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}
.rail-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  background: none;
  border: 0;
  padding: 0;
  cursor: pointer;
  color: #fff;
}
.rail-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  line-height: 1;
}
.rail-btn.liked .rail-icon {
  color: #fe2c55;
}
.rail-count {
  font-size: 0.72rem;
  font-weight: 600;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.8);
}
.watermark {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 4;
  pointer-events: none;
  user-select: none;
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.7);
}
.toast {
  position: absolute;
  left: 50%;
  bottom: 72px;
  transform: translateX(-50%);
  z-index: 3;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.72);
  color: #fff;
  font-size: 0.86rem;
  white-space: nowrap;
}
.panel {
  border-left: 1px solid rgba(255, 255, 255, 0.06);
  background: linear-gradient(180deg, #141418 0%, #101014 100%);
  padding: 1.35rem 1.2rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
  min-height: calc(100vh - 64px);
}
.eyebrow {
  margin: 0 0 0.35rem;
  color: #fe2c55;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}
.panel-head h1 {
  margin: 0;
  font-size: 1.35rem;
  line-height: 1.35;
  font-weight: 700;
}
.meta {
  margin: 0.55rem 0 0;
  color: rgba(244, 241, 234, 0.55);
  font-size: 0.86rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}
.intro {
  margin: 0.75rem 0 0;
  color: rgba(244, 241, 234, 0.68);
  font-size: 0.86rem;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}
.act {
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  color: #f4f1ea;
  border-radius: 10px;
  padding: 0.65rem 0.5rem;
  cursor: pointer;
  font-size: 0.88rem;
}
.act:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.act.liked,
.act.primary {
  background: #fe2c55;
  border-color: #fe2c55;
  color: #fff;
}
.act.ghost:hover:not(:disabled),
.act:hover:not(:disabled):not(.primary):not(.liked) {
  background: rgba(255, 255, 255, 0.1);
}
.ep-block {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.ep-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  strong { font-size: 0.95rem; }
  span {
    color: rgba(244, 241, 234, 0.4);
    font-size: 0.75rem;
  }
}
.ep-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.45rem;
  overflow: auto;
  padding-bottom: 0.5rem;
}
.ep {
  aspect-ratio: 1;
  border: 0;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(244, 241, 234, 0.85);
  font-weight: 700;
  cursor: pointer;
}
.ep:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.12);
}
.ep:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.ep.active {
  background: #fe2c55;
  color: #fff;
  box-shadow: 0 0 0 1px rgba(254, 44, 85, 0.45);
}
.comments {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 1.5rem 1.5rem 2rem;
}
.comments-inner {
  max-width: 860px;
  margin: 0 auto;
}
.cmt-head {
  display: flex;
  align-items: baseline;
  gap: 0.6rem;
  strong { font-size: 1.05rem; }
  span {
    color: rgba(244, 241, 234, 0.5);
    font-size: 0.82rem;
  }
}
.cmt-post {
  display: flex;
  gap: 0.6rem;
  margin-top: 1rem;
  align-items: flex-start;
}
.cmt-post .el-textarea {
  flex: 1;
}
.cmt-empty {
  margin-top: 1.2rem;
  padding: 1.4rem 0;
  text-align: center;
  color: rgba(244, 241, 234, 0.45);
  font-size: 0.88rem;
}
.cmt-list {
  list-style: none;
  margin: 1rem 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.cmt {
  display: flex;
  gap: 0.7rem;
  align-items: flex-start;
}
.cmt-avatar {
  flex: none;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fe2c55, #6e3b72);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: 700;
}
.cmt-body {
  flex: 1;
  min-width: 0;
}
.cmt-name {
  margin: 0;
  color: rgba(244, 241, 234, 0.9);
  font-size: 0.86rem;
  font-weight: 600;
}
.cmt-time {
  margin-left: 0.5rem;
  color: rgba(244, 241, 234, 0.4);
  font-size: 0.75rem;
  font-weight: 400;
}
.cmt-text {
  margin: 0.3rem 0 0;
  color: rgba(244, 241, 234, 0.85);
  font-size: 0.9rem;
  line-height: 1.55;
  word-break: break-word;
}
.cmt-more {
  margin-top: 1rem;
  text-align: center;
}
@media (max-width: 980px) {
  .workspace {
    grid-template-columns: 1fr;
  }
  .panel {
    border-left: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
    min-height: auto;
  }
  .screen {
    padding: 1rem;
  }
  .theater.portrait .stage-slot {
    max-height: 68vh;
  }
}
</style>
