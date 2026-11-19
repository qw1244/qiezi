<template>
  <span class="brand-lockup" :class="[tone, sizeClass]">
    <img class="mark" :src="mark" alt="" />
    <span class="copy">
      <span class="title">
        茄子短剧
        <i class="accent" aria-hidden="true"></i>
      </span>
      <span class="en">
        <i class="tick" aria-hidden="true"></i>
        QIEZI SHORT DRAMA
      </span>
    </span>
  </span>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import mark from '@/assets/logo-mark.svg'

withDefaults(defineProps<{ tone?: 'light' | 'dark' }>(), {
  tone: 'dark',
})

const attrs = useAttrs()
const sizeClass = computed(() => {
  const cls = String(attrs.class || '')
  if (cls.includes('lg')) return 'lg'
  if (cls.includes('sm')) return 'sm'
  return ''
})
</script>

<style scoped lang="scss">
.brand-lockup {
  display: inline-flex !important;
  flex-direction: row !important;
  flex-wrap: nowrap !important;
  align-items: center;
  gap: 0.7rem;
  max-width: 100%;
}
.mark {
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  display: block;
  object-fit: contain;
  filter: drop-shadow(0 4px 10px rgba(110, 50, 220, 0.35));
}
.copy {
  display: grid;
  gap: 0.2rem;
  line-height: 1;
  min-width: 0;
}
.title {
  position: relative;
  display: inline-block;
  font-size: 1.12rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  white-space: nowrap;
  font-family: 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.accent {
  position: absolute;
  right: 1.15em;
  top: 0.18em;
  width: 0.34em;
  height: 0.34em;
  border-radius: 0.08em;
  background: #a78bfa;
}
.en {
  position: relative;
  padding-left: 0.55rem;
  font-size: 0.58rem;
  font-weight: 600;
  letter-spacing: 0.14em;
  white-space: nowrap;
  font-family: Outfit, 'Segoe UI', sans-serif;
  text-transform: uppercase;
}
.tick {
  position: absolute;
  left: 0;
  bottom: 0.05rem;
  width: 0.42rem;
  height: 0.14rem;
  border-radius: 999px;
  background: #a78bfa;
  transform: rotate(-28deg);
  transform-origin: left center;
}
.brand-lockup.dark .title { color: #fffef9; }
.brand-lockup.dark .en { color: rgba(255, 254, 249, 0.62); }
.brand-lockup.light .title { color: #1f1630; }
.brand-lockup.light .en { color: rgba(31, 22, 48, 0.55); }

.brand-lockup.sm .mark { width: 30px; height: 30px; }
.brand-lockup.sm .title { font-size: 0.98rem; }
.brand-lockup.sm .en { font-size: 0.52rem; letter-spacing: 0.12em; }

.brand-lockup.lg .mark { width: 46px; height: 46px; }
.brand-lockup.lg .title { font-size: 1.4rem; }
.brand-lockup.lg .en { font-size: 0.68rem; }
</style>
