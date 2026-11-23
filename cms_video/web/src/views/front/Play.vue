<template>
  <div v-if="video" class="play-page" :class="{ 'theater-mode': theaterMode }">
    <nav class="crumbs">
      <router-link to="/">首页</router-link>
      <span class="sep">›</span>
      <router-link to="/">{{ seriesCategory }}短剧</router-link>
      <span class="sep">›</span>
      <router-link :to="`/series/${video.seriesId}`">{{ displayTitle }}</router-link>
      <span class="sep">›</span>
      <span class="cur">第 {{ video.epNo }} 集</span>
    </nav>

    <h1 class="play-title">{{ displayTitle }} 第 {{ video.epNo }} 集</h1>

    <div class="play-layout">
      <div class="play-main">
        <div class="stage-slot" :class="orient">
          <Transition :name="slideName">
            <div :key="String(video.id)" class="slide-layer">
              <video
                :ref="bindPlayer"
                class="player"
                controlslist="nodownload"
                playsinline
                autoplay
                :src="video.playUrl"
                @timeupdate="onTime"
                @loadedmetadata="onMeta"
                @ended="onEnded"
                @play="isPlaying = true; flashCenterIcon('play')"
                @pause="isPlaying = false; flashCenterIcon('pause')"
                @click="togglePlay"
                @contextmenu.prevent
              />
              <div v-if="tip" class="toast">{{ tip }}</div>
              <div class="watermark">免费看qz53.com</div>

              <Transition name="center-icon">
                <div v-if="centerIcon" class="center-icon" :class="centerIcon">
                  <span>{{ centerIcon === 'play' ? '▶' : '❚❚' }}</span>
                </div>
              </Transition>

              <div class="player-controls" :class="{ hidden: !showControls }">
                <div class="pc-time">{{ fmtTime(currentTime) }} / {{ fmtTime(duration) }}</div>
                <div class="pc-progress" @click="seekByClick">
                  <div class="pc-buffered" :style="{ width: bufferedPct + '%' }"></div>
                  <div class="pc-played" :style="{ width: playedPct + '%' }"></div>
                  <div class="pc-dot" :style="{ left: playedPct + '%' }"></div>
                </div>
                <div class="pc-row">
                  <div class="pc-left">
                    <button type="button" class="pc-btn skip-btn" @click="skip(-5)" title="后退 5 秒">⏪<span>5</span></button>
                    <button type="button" class="pc-btn" @click="togglePlay">{{ isPlaying ? '❚❚' : '▶' }}</button>
                    <button type="button" class="pc-btn skip-btn" @click="skip(5)" title="前进 5 秒"><span>5</span>⏩</button>
                    <button type="button" class="pc-btn" @click="playNext(false)" :disabled="animating">⏭</button>
                    <label class="pc-auto">
                      <input type="checkbox" v-model="autoplay" />
                      <span>自动连播</span>
                    </label>
                  </div>
                  <div class="pc-right">
                    <div class="pc-speed">
                      <button type="button" class="pc-btn speed-btn" @click="speedMenu = !speedMenu">◷ {{ speed }}x</button>
                      <div v-if="speedMenu" class="speed-menu">
                        <button
                          v-for="s in speeds"
                          :key="s"
                          type="button"
                          :class="{ active: s === speed }"
                          @click="setSpeed(s)"
                        >{{ s }}x</button>
                      </div>
                    </div>
                    <button type="button" class="pc-btn quality-btn">高清</button>
                    <div class="pc-volume">
                      <button type="button" class="pc-btn" @click="toggleMute">{{ muted || volume === 0 ? '🔇' : '🔊' }}</button>
                      <input
                        type="range"
                        min="0"
                        max="100"
                        :value="Math.round(volume * 100)"
                        class="vol-slider"
                        @input="setVolume(($event.target as HTMLInputElement).valueAsNumber / 100)"
                      />
                    </div>
                    <button type="button" class="pc-btn" @click="toggleTheater" :title="theaterMode ? '退出剧场' : '剧场模式'">🎬</button>
                    <button type="button" class="pc-btn" @click="toggleFullscreen" title="全屏">⛶</button>
                  </div>
                </div>
              </div>
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
        </div>

        <div class="play-actions">
          <button type="button" class="pact" @click="playPrev" :disabled="!prevEp || animating">‹ 上一集</button>
          <button type="button" class="pact" @click="playNext(false)" :disabled="animating">下一集 ›</button>
          <button type="button" class="pact" :class="{ liked: video.liked }" @click="toggleLike">♥ {{ video.likeCount || 0 }}</button>
          <button type="button" class="pact" :class="{ liked: fav }" @click="toggleFav">★ {{ favCount }}</button>
          <button type="button" class="pact" @click="shareVideo">↗ 分享</button>
        </div>

        <section class="ep-info">
          <div class="ep-info-head">
            <h2>{{ displayTitle }} 第{{ video.epNo }}集</h2>
            <router-link class="detail-link" :to="`/series/${video.seriesId}`">查看作品详情 ›</router-link>
          </div>
          <p v-if="uploadDate" class="ep-date">{{ uploadDate }} 上线</p>
          <div v-if="seriesTags.length" class="ep-tags">
            <span class="ep-tag cat">{{ seriesCategory }}短剧</span>
            <span v-for="t in seriesTags" :key="t" class="ep-tag">{{ t }}</span>
            <span class="ep-tag">{{ epCount }}集</span>
            <span class="ep-tag">更新中 · 已更新至 {{ epCount }} 集</span>
          </div>
          <p v-if="seriesIntro" class="ep-intro" :class="{ expanded: introExpanded }">{{ seriesIntro }}</p>
          <button v-if="seriesIntro" type="button" class="intro-toggle" @click="introExpanded = !introExpanded">
            {{ introExpanded ? '收起简介 ∧' : '展开简介 ∨' }}
          </button>
        </section>

        <section class="comments">
          <h2>本剧评论 {{ commentTotal }}</h2>
          <div class="cmt-post">
            <span class="cmt-avatar me">{{ (user.profile?.nickname || '我').slice(0, 1) }}</span>
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="友善讨论剧情，避免无提示剧透…"
            />
            <el-button type="primary" :loading="posting" :disabled="!commentText.trim()" @click="postComment">
              提交审核
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
        </section>
      </div>

      <aside class="play-side">
        <section class="series-card">
          <div class="series-card-head">
            <h2>{{ displayTitle }}</h2>
            <router-link class="intro-link" :to="`/series/${video.seriesId}`">简介 ›</router-link>
          </div>
          <p class="series-meta">{{ seriesMetaLine }}</p>
          <p class="series-eps">第 {{ video.epNo }} 集 · 已更新至 {{ epCount }} 集</p>
          <button type="button" class="follow-btn" :class="{ on: fav }" @click="toggleFav">
            {{ fav ? '✓ 已追剧' : '🔔 追剧' }}
          </button>
        </section>

        <section class="ep-list">
          <div class="ep-list-head">
            <h2>播放列表</h2>
            <button type="button" class="reverse-btn" @click="reverseEp = !reverseEp">{{ reverseEp ? '正序' : '倒序' }}</button>
          </div>
          <div v-if="epRanges.length > 1" class="ep-ranges">
            <button
              v-for="(r, i) in epRanges"
              :key="i"
              type="button"
              class="ep-range"
              :class="{ active: i === epRange }"
              @click="epRange = i"
            >{{ r.label }}</button>
          </div>
          <div class="ep-grid">
            <button
              v-for="ep in visibleEps"
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
        </section>

        <section v-if="recommend.length" class="rel-rec">
          <h2>相关推荐</h2>
          <router-link
            v-for="item in recommend"
            :key="item.id"
            class="rel-card"
            :to="`/series/${item.id}`"
          >
            <div class="rel-cover" :style="coverStyle(item.cover)"></div>
            <div class="rel-meta">
              <p class="rel-title">{{ item.title }}</p>
              <p class="rel-sub">{{ item.intro || '全集在线观看' }}</p>
              <p class="rel-info">{{ item.category || '' }} · {{ formatHeat(item.playCount) }} 热度</p>
            </div>
            <span class="rel-play">▶</span>
          </router-link>
        </section>
      </aside>
    </div>
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
const seriesCategory = ref('AI')
const seriesTags = ref<string[]>([])
const seriesDetail = ref<any>(null)
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
const recommend = ref<any[]>([])
const introExpanded = ref(false)
const epRange = ref(0)
const reverseEp = ref(false)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const buffered = ref(0)
const volume = ref(1)
const muted = ref(false)
const speed = ref(1)
const speeds = [0.5, 0.75, 1, 1.25, 1.5, 2]
const speedMenu = ref(false)
const showControls = ref(true)
const theaterMode = ref(false)
const centerIcon = ref<'play' | 'pause' | null>(null)
let centerIconTimer: number | undefined
let controlsTimer: number | undefined

const playedPct = computed(() => {
  if (!duration.value) return 0
  return Math.min(100, (currentTime.value / duration.value) * 100)
})

const bufferedPct = computed(() => {
  if (!duration.value) return 0
  return Math.min(100, (buffered.value / duration.value) * 100)
})

function fmtTime(s: number) {
  if (!s || !isFinite(s)) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${String(sec).padStart(2, '0')}`
}

function flashCenterIcon(icon: 'play' | 'pause') {
  centerIcon.value = icon
  if (centerIconTimer) window.clearTimeout(centerIconTimer)
  if (icon === 'play') {
    centerIconTimer = window.setTimeout(() => {
      centerIcon.value = null
    }, 600)
  }
}

function pokeControls() {
  showControls.value = true
  if (controlsTimer) window.clearTimeout(controlsTimer)
  controlsTimer = window.setTimeout(() => {
    if (isPlaying.value) showControls.value = false
  }, 3000)
}

function seekByClick(e: MouseEvent) {
  const el = e.currentTarget as HTMLElement
  const rect = el.getBoundingClientRect()
  const ratio = Math.min(1, Math.max(0, (e.clientX - rect.left) / rect.width))
  if (player.value && duration.value) {
    player.value.currentTime = ratio * duration.value
    currentTime.value = player.value.currentTime
  }
}

function skip(seconds: number) {
  if (!player.value) return
  const target = (player.value.currentTime || 0) + seconds
  player.value.currentTime = Math.min(
    Math.max(0, target),
    duration.value || player.value.duration || target,
  )
  currentTime.value = player.value.currentTime
}

function setVolume(v: number) {
  volume.value = Math.min(1, Math.max(0, v))
  muted.value = volume.value === 0
  if (player.value) {
    player.value.volume = volume.value
    player.value.muted = muted.value
  }
}

function toggleMute() {
  muted.value = !muted.value
  if (player.value) player.value.muted = muted.value
}

function setSpeed(s: number) {
  speed.value = s
  speedMenu.value = false
  if (player.value) player.value.playbackRate = s
}

function toggleTheater() {
  theaterMode.value = !theaterMode.value
}

function toggleFullscreen() {
  const el = document.querySelector('.stage-slot') as HTMLElement | null
  if (!el) return
  if (document.fullscreenElement) {
    document.exitFullscreen().catch(() => undefined)
  } else {
    el.requestFullscreen().catch(() => undefined)
  }
}

function coverStyle(cover: string) {
  if (cover) {
    return { backgroundImage: `url(${cover})` }
  }
  return {
    backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)',
  }
}

function formatHeat(n: number) {
  n = n || 0
  if (n >= 10000) {
    return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  }
  return String(n)
}
const posting = ref(false)
const slideName = ref<'slide-next' | 'slide-prev'>('slide-next')
const animating = ref(false)
let timer: number | undefined
let lastSent = 0
let tipTimer: number | undefined
let switching = false

const SLIDE_MS = 480
const EP_PAGE = 30

const displayTitle = computed(() => {
  if (!video.value) return ''
  return video.value.seriesTitle || video.value.title || `第${video.value.epNo}集`
})

const epCount = computed(() => episodes.value.length)

const uploadDate = computed(() => {
  const t = video.value?.createdAt
  if (!t) return ''
  return String(t).slice(0, 10)
})

const seriesMetaLine = computed(() => {
  const parts: string[] = []
  const y = uploadDate.value ? uploadDate.value.slice(0, 4) : ''
  if (y) parts.push(y)
  parts.push(`${seriesCategory.value}短剧`)
  if (seriesTags.value.length) parts.push(seriesTags.value.join(' '))
  return parts.join(' · ')
})

const epRanges = computed(() => {
  const total = episodes.value.length
  const ranges: { label: string; start: number; end: number }[] = []
  for (let s = 0; s < total; s += EP_PAGE) {
    const e = Math.min(s + EP_PAGE, total)
    ranges.push({ label: `${s + 1}-${e}`, start: s, end: e })
  }
  return ranges
})

const visibleEps = computed(() => {
  const r = epRanges.value[epRange.value]
  if (!r) return episodes.value
  const list = episodes.value.slice(r.start, r.end)
  return reverseEp.value ? [...list].reverse() : list
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
  introExpanded.value = false
  const res: any = await http.get(`/video/play/${route.params.id}`)
  video.value = res.data
  await loadEpisodes(res.data.seriesId)
  await loadComments(1)
  await loadRecommend()
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
  seriesDetail.value = res.data
  episodes.value = res.data?.videos || []
  seriesIntro.value = res.data?.intro || ''
  seriesCategory.value = res.data?.category || 'AI'
  seriesTags.value = (res.data?.tags || '').split(',').map((t: string) => t.trim()).filter(Boolean)
  fav.value = !!res.data?.fav
  favCount.value = res.data?.favCount || 0
  const idx = Math.floor(((video.value?.epNo || 1) - 1) / EP_PAGE)
  epRange.value = idx
}

function onMeta() {
  if (player.value) {
    duration.value = player.value.duration || 0
    if (video.value?.progress > 3) {
      player.value.currentTime = video.value.progress
    }
  }
}

function onTime() {
  if (!player.value) return
  currentTime.value = player.value.currentTime || 0
  duration.value = player.value.duration || duration.value
  try {
    const b = player.value.buffered
    if (b && b.length) buffered.value = b.end(b.length - 1)
  } catch {
    buffered.value = 0
  }
  if (!user.token || !video.value) return
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

async function loadRecommend() {
  try {
    const res: any = await http.get('/series/list', { params: { sort: 'hot', page: 1, size: 9 } })
    const list = (res.data.list || []).filter((s: any) => s.id !== video.value?.seriesId)
    recommend.value = list.slice(0, 8)
  } catch {
    recommend.value = []
  }
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

onMounted(() => {
  load()
  timer = window.setInterval(() => onTime(), 8000)
  window.addEventListener('keydown', onKey, true)
  const stage = document.querySelector('.stage-slot')
  if (stage) {
    stage.addEventListener('mousemove', pokeControls)
    stage.addEventListener('mouseleave', () => {
      if (isPlaying.value) showControls.value = false
    })
  }
  pokeControls()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  if (tipTimer) clearTimeout(tipTimer)
  if (controlsTimer) clearTimeout(controlsTimer)
  if (centerIconTimer) clearTimeout(centerIconTimer)
  window.removeEventListener('keydown', onKey, true)
  onTime()
})
</script>

<style scoped lang="scss">
.play-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 1.2rem 1rem 3rem;
  color: #f4f1ea;
}
.crumbs {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  flex-wrap: wrap;
  font-size: 0.86rem;
  color: rgba(244, 241, 234, 0.55);
  margin-bottom: 0.7rem;
  a {
    color: rgba(244, 241, 234, 0.55);
  }
  a:hover { color: #fff; }
  .sep { color: rgba(244, 241, 234, 0.35); }
  .cur {
    color: #fff;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 6px;
    padding: 0.15rem 0.5rem;
  }
}
.play-title {
  margin: 0 0 1rem;
  font-size: 1.5rem;
  font-weight: 800;
  color: #fff;
}
.play-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 1.4rem;
  align-items: start;
}
.play-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}
.stage-slot {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: 12px;
  background: #000;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
}
.stage-slot.portrait {
  aspect-ratio: 9 / 16;
  max-width: min(480px, 100%);
  max-height: calc(100vh - 110px);
  margin: 0 auto;
}
.stage-slot.landscape {
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
.watermark {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 4;
  pointer-events: none;
  user-select: none;
  color: rgba(255, 255, 255, 0.12);
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
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
/* 播放/暂停中间大图标 */
.center-icon {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 5;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.center-icon span {
  color: #fff;
  font-size: 2rem;
  line-height: 1;
  margin-left: 0.1rem;
}
.center-icon.pause span {
  font-size: 1.7rem;
  margin-left: 0;
}
.center-icon-enter-active,
.center-icon-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.center-icon-enter-from,
.center-icon-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.85);
}
/* 自定义播放器控制条 */
.player-controls {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 6;
  padding: 0.5rem 0.9rem 0.7rem;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.75));
  transition: opacity 0.3s ease;
}
.player-controls.hidden {
  opacity: 0;
  pointer-events: none;
}
.pc-time {
  color: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  margin-bottom: 0.35rem;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
}
.pc-progress {
  position: relative;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.25);
  cursor: pointer;
  margin-bottom: 0.55rem;
}
.pc-buffered {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.35);
}
.pc-played {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: 999px;
  background: #f5c518;
}
.pc-dot {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #f5c518;
  transform: translate(-50%, -50%);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
  pointer-events: none;
}
.pc-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}
.pc-left,
.pc-right {
  display: flex;
  align-items: center;
  gap: 0.55rem;
}
.pc-btn {
  border: 0;
  background: none;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  padding: 0.25rem;
  line-height: 1;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
}
.pc-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.skip-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.1rem;
  font-size: 0.95rem;
}
.skip-btn span {
  font-size: 0.68rem;
  font-weight: 700;
  line-height: 1;
}
.pc-auto {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  color: #fff;
  font-size: 0.82rem;
  cursor: pointer;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  white-space: nowrap;
}
.pc-auto input {
  accent-color: #f5c518;
  cursor: pointer;
}
.pc-speed {
  position: relative;
}
.speed-btn {
  font-size: 0.86rem;
  font-weight: 600;
  white-space: nowrap;
}
.speed-menu {
  position: absolute;
  bottom: 2rem;
  right: 0;
  display: flex;
  flex-direction: column;
  background: rgba(20, 20, 24, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  overflow: hidden;
  min-width: 76px;
  button {
    border: 0;
    background: none;
    color: rgba(255, 255, 255, 0.85);
    padding: 0.5rem 0.8rem;
    cursor: pointer;
    font-size: 0.86rem;
    text-align: center;
  }
  button:hover { background: rgba(255, 255, 255, 0.1); }
  button.active {
    background: #f5c518;
    color: #111;
    font-weight: 700;
  }
}
.quality-btn {
  font-size: 0.84rem;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  padding: 0.2rem 0.45rem;
}
.pc-volume {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}
.vol-slider {
  width: 64px;
  accent-color: #f5c518;
  cursor: pointer;
}
/* 剧场模式：隐藏侧栏，播放器占满 */
.play-page.theater-mode .play-layout {
  grid-template-columns: 1fr;
}
.play-page.theater-mode .play-side {
  display: none;
}
.play-page.theater-mode .stage-slot.portrait {
  max-width: min(720px, 100%);
}
.play-actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}
.pact {
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: #f4f1ea;
  border-radius: 999px;
  padding: 0.55rem 1.1rem;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 600;
}
.pact:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.pact:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.12);
}
.pact.liked {
  color: #fe2c55;
  border-color: rgba(254, 44, 85, 0.5);
}
.ep-info {
  background: #141418;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
}
.ep-info-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 1rem;
  h2 {
    margin: 0;
    font-size: 1.15rem;
  }
  .detail-link {
    flex: none;
    color: rgba(244, 241, 234, 0.55);
    font-size: 0.86rem;
    white-space: nowrap;
  }
  .detail-link:hover { color: #fff; }
}
.ep-date {
  margin: 0.5rem 0 0;
  color: rgba(244, 241, 234, 0.5);
  font-size: 0.84rem;
}
.ep-tags {
  display: flex;
  gap: 0.45rem;
  flex-wrap: wrap;
  margin-top: 0.8rem;
}
.ep-tag {
  font-size: 0.8rem;
  color: rgba(244, 241, 234, 0.85);
  background: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 0.3rem 0.65rem;
}
.ep-tag.cat {
  background: rgba(245, 197, 24, 0.9);
  color: #111;
  font-weight: 700;
}
.ep-intro {
  margin: 0.9rem 0 0;
  color: rgba(244, 241, 234, 0.75);
  font-size: 0.9rem;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ep-intro.expanded {
  display: block;
}
.intro-toggle {
  margin-top: 0.6rem;
  border: 0;
  background: none;
  color: rgba(244, 241, 234, 0.55);
  font-size: 0.86rem;
  cursor: pointer;
  padding: 0;
}
.comments {
  background: #141418;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
  h2 {
    margin: 0 0 1rem;
    font-size: 1.1rem;
  }
}
.cmt-post {
  display: flex;
  gap: 0.7rem;
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
  margin: 1.2rem 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
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
  background: linear-gradient(135deg, #f5c518, #f59e0b);
  color: #111;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: 700;
}
.cmt-avatar.me {
  background: linear-gradient(135deg, #fe2c55, #6e3b72);
  color: #fff;
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
.play-side {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  min-width: 0;
}
.series-card {
  background: #141418;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
}
.series-card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 0.8rem;
  h2 {
    margin: 0;
    font-size: 1.15rem;
  }
  .intro-link {
    flex: none;
    color: rgba(244, 241, 234, 0.55);
    font-size: 0.86rem;
    white-space: nowrap;
  }
  .intro-link:hover { color: #fff; }
}
.series-meta {
  margin: 0.6rem 0 0;
  color: rgba(244, 241, 234, 0.55);
  font-size: 0.86rem;
}
.series-eps {
  margin: 0.5rem 0 0;
  color: rgba(244, 241, 234, 0.85);
  font-size: 0.9rem;
}
.follow-btn {
  width: 100%;
  margin-top: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.05);
  color: #f4f1ea;
  border-radius: 10px;
  padding: 0.7rem;
  cursor: pointer;
  font-size: 0.92rem;
  font-weight: 600;
}
.follow-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}
.follow-btn.on {
  background: #f5c518;
  border-color: #f5c518;
  color: #111;
}
.ep-list {
  background: #141418;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
}
.ep-list-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.9rem;
  h2 {
    margin: 0;
    font-size: 1.1rem;
    border-left: 3px solid #f5c518;
    padding-left: 0.6rem;
  }
}
.reverse-btn {
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(244, 241, 234, 0.8);
  border-radius: 8px;
  padding: 0.35rem 0.7rem;
  cursor: pointer;
  font-size: 0.82rem;
}
.ep-ranges {
  display: flex;
  gap: 0.45rem;
  flex-wrap: wrap;
  margin-bottom: 0.8rem;
}
.ep-range {
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(244, 241, 234, 0.8);
  border-radius: 8px;
  padding: 0.35rem 0.8rem;
  cursor: pointer;
  font-size: 0.84rem;
}
.ep-range.active {
  background: rgba(245, 197, 24, 0.9);
  border-color: transparent;
  color: #111;
  font-weight: 700;
}
.ep-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.45rem;
  max-height: 420px;
  overflow-y: auto;
  padding-right: 0.2rem;
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
  background: #22c55e;
  color: #fff;
  box-shadow: 0 0 0 1px rgba(34, 197, 94, 0.45);
}
.rel-rec {
  background: #141418;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
  h2 {
    margin: 0 0 1rem;
    font-size: 1.1rem;
  }
}
.rel-card {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  padding: 0.6rem 0;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
  &:first-of-type { border-top: 0; padding-top: 0; }
}
.rel-cover {
  flex: none;
  width: 64px;
  aspect-ratio: 3 / 4;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
}
.rel-meta {
  flex: 1;
  min-width: 0;
}
.rel-title {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rel-sub {
  margin: 0.25rem 0 0;
  color: rgba(244, 241, 234, 0.55);
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rel-info {
  margin: 0.25rem 0 0;
  color: rgba(244, 241, 234, 0.5);
  font-size: 0.78rem;
}
.rel-play {
  flex: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
}
@media (max-width: 980px) {
  .play-layout {
    grid-template-columns: 1fr;
    display: flex;
    flex-direction: column;
  }
  /* 手机上把左右两栏打散，按这个顺序排：视频 → 操作按钮 → 播放列表 → 简介 → 评论 → 合集卡片 → 相关推荐 */
  .play-main,
  .play-side {
    display: contents;
  }
  .stage-slot { order: 0; }
  .play-actions { order: 1; }
  .ep-list { order: 2; }
  .ep-info { order: 3; }
  .comments { order: 4; }
  .series-card { order: 5; }
  .rel-rec { order: 6; }
  .play-title { font-size: 1.25rem; }
  /* 手机上播放列表调大：每行 4 个、字更大、间距更大 */
  .ep-list {
    padding: 1.4rem 1.2rem;
  }
  .ep-list-head h2 {
    font-size: 1.25rem;
  }
  .ep-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 0.7rem;
    max-height: 480px;
  }
  .ep {
    font-size: 1.05rem;
    border-radius: 10px;
  }
}
</style>
