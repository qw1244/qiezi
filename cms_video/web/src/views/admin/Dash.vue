<template>
  <div class="admin-page dash">
    <section class="bento">
      <article class="cell hero">
        <div class="hero-copy">
          <p class="eyebrow">茄子 · 今日看板</p>
          <h2>内容与互动总览</h2>
          <p class="desc">一眼看清用户规模、内容库存和播放互动，方便安排上架与运营。</p>
        </div>
        <div class="hero-play">
          <span>总播放</span>
          <strong>{{ data.playSum || 0 }}</strong>
        </div>
      </article>

      <article class="cell wide">
        <div class="row">
          <div>
            <p class="k">用户</p>
            <p class="v">{{ data.userCount || 0 }}</p>
          </div>
          <div>
            <p class="k">合集</p>
            <p class="v">{{ data.seriesCount || 0 }}</p>
          </div>
          <div>
            <p class="k">视频</p>
            <p class="v">{{ data.videoCount || 0 }}</p>
          </div>
        </div>
        <div class="strip">
          <i :style="{ width: contentWidth }"></i>
        </div>
        <p class="note">内容库存健康度（合集 + 视频相对规模）</p>
      </article>

      <article class="cell">
        <p class="k">点赞</p>
        <p class="v accent">{{ data.likeSum || 0 }}</p>
        <p class="note">单视频互动累计</p>
      </article>

      <article class="cell">
        <p class="k">收藏</p>
        <p class="v leaf">{{ data.favSum || 0 }}</p>
        <p class="note">合集收藏累计</p>
      </article>

      <article class="cell">
        <p class="k">观看记录</p>
        <p class="v">{{ data.watchCount || 0 }}</p>
        <p class="note">用户续看足迹条数</p>
      </article>
    </section>

    <section class="charts">
      <article class="chart-card">
        <div class="chart-head">
          <h3>近 7 日观看足迹</h3>
          <p>按用户观看记录更新时间统计</p>
        </div>
        <div ref="trendRef" class="chart"></div>
      </article>

      <article class="chart-card">
        <div class="chart-head">
          <h3>合集播放 Top</h3>
          <p>播放量最高的合集</p>
        </div>
        <div ref="topRef" class="chart"></div>
      </article>

      <article class="chart-card">
        <div class="chart-head">
          <h3>互动构成</h3>
          <p>播放 / 点赞 / 收藏 / 足迹占比</p>
        </div>
        <div ref="mixRef" class="chart"></div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import http from '@/utils/http'

const data = ref<any>({})
const trendRef = ref<HTMLDivElement | null>(null)
const topRef = ref<HTMLDivElement | null>(null)
const mixRef = ref<HTMLDivElement | null>(null)

let trendChart: echarts.ECharts | null = null
let topChart: echarts.ECharts | null = null
let mixChart: echarts.ECharts | null = null

const contentWidth = computed(() => {
  const series = data.value.seriesCount || 0
  const video = data.value.videoCount || 0
  const total = series + video
  if (!total) return '18%'
  return `${Math.min(100, 24 + total * 8)}%`
})

function names(list: any[] = []) {
  return list.map((i) => i.name)
}
function values(list: any[] = []) {
  return list.map((i) => Number(i.value) || 0)
}

function renderCharts() {
  const trend = data.value.watchTrend || []
  const top = data.value.topSeries || []
  const mix = data.value.interactMix || []

  if (trendRef.value) {
    trendChart ||= echarts.init(trendRef.value)
    trendChart.setOption({
      color: ['#6e3b72'],
      grid: { left: 36, right: 16, top: 28, bottom: 28 },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: names(trend),
        axisLine: { lineStyle: { color: '#d8d0dc' } },
        axisLabel: { color: '#6f6678' },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        splitLine: { lineStyle: { color: '#efe8f1' } },
        axisLabel: { color: '#6f6678' },
      },
      series: [
        {
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(110,59,114,0.35)' },
              { offset: 1, color: 'rgba(110,59,114,0.02)' },
            ]),
          },
          data: values(trend),
        },
      ],
    })
  }

  if (topRef.value) {
    topChart ||= echarts.init(topRef.value)
    topChart.setOption({
      color: ['#3f7a5c'],
      grid: { left: 88, right: 24, top: 16, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'value',
        minInterval: 1,
        splitLine: { lineStyle: { color: '#efe8f1' } },
        axisLabel: { color: '#6f6678' },
      },
      yAxis: {
        type: 'category',
        data: names(top).reverse(),
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#2b1833' },
      },
      series: [
        {
          type: 'bar',
          barWidth: 16,
          itemStyle: {
            borderRadius: [0, 8, 8, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#6e3b72' },
              { offset: 1, color: '#3f7a5c' },
            ]),
          },
          data: values(top).reverse(),
        },
      ],
    })
  }

  if (mixRef.value) {
    mixChart ||= echarts.init(mixRef.value)
    const pieData = (mix as any[]).map((i) => ({ name: i.name, value: Number(i.value) || 0 }))
    mixChart.setOption({
      color: ['#6e3b72', '#8b4d8c', '#3f7a5c', '#c4a35a'],
      tooltip: { trigger: 'item' },
      legend: {
        bottom: 0,
        icon: 'circle',
        textStyle: { color: '#6f6678' },
      },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '46%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { color: '#2b1833' },
          data: pieData,
        },
      ],
    })
  }
}

function onResize() {
  trendChart?.resize()
  topChart?.resize()
  mixChart?.resize()
}

onMounted(async () => {
  const res: any = await http.get('/admin/stats/dash')
  data.value = res.data || {}
  await nextTick()
  renderCharts()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  topChart?.dispose()
  mixChart?.dispose()
  trendChart = null
  topChart = null
  mixChart = null
})
</script>

<style scoped lang="scss">
.bento {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  grid-auto-rows: minmax(140px, auto);
  gap: 1rem;
}
.cell {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 24px;
  padding: 1.2rem 1.25rem;
  box-shadow: 0 14px 40px rgba(43, 24, 51, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 150px;
}
.hero {
  grid-column: 1 / 3;
  grid-row: span 2;
  min-height: 280px;
  color: #fff;
  background:
    radial-gradient(circle at 88% 18%, rgba(255,255,255,0.18), transparent 28%),
    linear-gradient(145deg, #5a3062 0%, #2b1833 48%, #3f7a5c 140%);
  border: 0;
  position: relative;
  overflow: hidden;
}
.hero-copy, .hero-play { position: relative; z-index: 1; }
.eyebrow {
  margin: 0;
  letter-spacing: 0.12em;
  font-size: 0.78rem;
  opacity: 0.8;
}
.hero h2 {
  margin: 0.7rem 0 0.7rem;
  font-size: clamp(1.8rem, 3vw, 2.5rem);
  line-height: 1.15;
}
.desc {
  margin: 0;
  max-width: 26rem;
  line-height: 1.7;
  color: rgba(255,255,255,0.78);
}
.hero-play {
  margin-top: 1.6rem;
  display: inline-flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.85rem 1rem;
  border-radius: 18px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.12);
  width: fit-content;
  span {
    font-size: 0.8rem;
    opacity: 0.75;
  }
  strong {
    font-size: 2rem;
    font-family: Outfit, 'Noto Sans SC', sans-serif;
    line-height: 1;
  }
}
.wide {
  grid-column: 3;
  grid-row: span 2;
}
.row {
  display: grid;
  gap: 1rem;
}
.k {
  margin: 0;
  color: var(--admin-muted);
  font-size: 0.9rem;
}
.v {
  margin: 0.35rem 0 0;
  font-size: 2rem;
  font-weight: 700;
  font-family: Outfit, 'Noto Sans SC', sans-serif;
  line-height: 1;
}
.v.accent { color: var(--egg); }
.v.leaf { color: var(--leaf); }
.strip {
  margin: 1.1rem 0 0.55rem;
  height: 8px;
  border-radius: 999px;
  background: #efe8f1;
  overflow: hidden;
  i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, #6e3b72, #3f7a5c);
  }
}
.note {
  margin: 0;
  color: var(--admin-muted);
  font-size: 0.8rem;
}

.charts {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 1rem;
}
.chart-card {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 24px;
  padding: 1.1rem 1.15rem 0.6rem;
  box-shadow: 0 14px 40px rgba(43, 24, 51, 0.05);
}
.chart-head {
  margin-bottom: 0.35rem;
  h3 {
    margin: 0 0 0.25rem;
    font-size: 1rem;
  }
  p {
    margin: 0;
    color: var(--admin-muted);
    font-size: 0.8rem;
  }
}
.chart {
  width: 100%;
  height: 280px;
}

@media (max-width: 1100px) {
  .bento {
    grid-template-columns: 1fr 1fr;
  }
  .hero, .wide {
    grid-column: 1 / -1;
    grid-row: auto;
  }
  .charts {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 720px) {
  .bento { grid-template-columns: 1fr; }
}
</style>
