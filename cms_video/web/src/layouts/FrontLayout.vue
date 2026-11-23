<template>
  <div class="front-shell" :class="{ theater: isPlay }">
    <header class="nav">
      <div class="front-wrap nav-inner">
        <router-link to="/" class="brand-logo-link" aria-label="茄子短剧">
          <BrandLogo class="sm" tone="light" />
        </router-link>
        <nav class="links">
          <router-link to="/">发现</router-link>
          <router-link to="/fav">收藏</router-link>
          <router-link to="/history">足迹</router-link>
          <router-link to="/upload">投稿</router-link>
        </nav>
        <form class="nav-search" @submit.prevent="onSearch">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" stroke-linecap="round" />
          </svg>
          <input v-model="navKeyword" type="search" placeholder="搜索合集名称" autocomplete="off" />
          <button type="submit">搜索</button>
        </form>
        <div class="actions">
          <template v-if="user.token">
            <router-link to="/profile" class="user">
              <img v-if="user.profile?.avatar" class="nav-avatar" :src="user.profile.avatar" alt="" />
              <span v-else class="nav-avatar letter">{{ navAvatarLetter }}</span>
              {{ user.profile?.nickname || '我的' }}
            </router-link>
            <button class="ghost" @click="onLogout">退出</button>
          </template>
          <template v-else>
            <router-link class="ghost" to="/login">登录</router-link>
            <router-link class="solid" to="/register">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    <main :class="isPlay ? 'main theater-main' : 'front-wrap main'">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import BrandLogo from '@/components/BrandLogo.vue'

const user = useUserStore()
const router = useRouter()
const route = useRoute()
const isPlay = computed(() => route.name === 'play')
const navKeyword = ref('')
function onSearch() {
  const q = navKeyword.value.trim()
  router.push({ name: 'home', query: q ? { keyword: q } : {} })
}
watch(
  () => route.query.keyword,
  (k) => { navKeyword.value = typeof k === 'string' ? k : '' },
  { immediate: true },
)
const navAvatarLetter = computed(() => {
  const name = user.profile?.nickname || user.profile?.username || '我'
  return String(name).trim().charAt(0).toUpperCase() || '我'
})

onMounted(() => {
  user.fetchProfile().catch(() => undefined)
})

function onLogout() {
  user.logout()
  router.push('/')
}
</script>

<style scoped lang="scss">
.nav {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(14px);
  background: rgba(10, 10, 10, 0.92);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  transition: background 0.25s ease, border-color 0.25s ease;
}
.theater .nav {
  background: rgba(12, 12, 16, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.06);
}
.theater .user,
.theater .ghost {
  color: #f2efe8;
}
.theater .links a {
  color: rgba(242, 239, 232, 0.62);
}
.theater .links a.router-link-active {
  color: #fff;
}
.theater .solid {
  background: #fe2c55;
  color: #fff;
}
.nav-inner {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  min-height: 64px;
}
.brand-logo-link {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
}
.brand-logo {
  height: 38px;
  width: auto;
  max-width: 176px;
  display: block;
  object-fit: contain;
  background: transparent;
}
.links {
  display: flex;
  gap: 1rem;
  flex: 1;
  a {
    color: var(--muted);
    font-weight: 500;
  }
  a.router-link-active {
    color: var(--ink);
  }
}
.nav-search {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.25rem 0.25rem 0.8rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  width: min(260px, 34vw);
  color: var(--muted);
  flex: 0 0 auto;
  svg { flex: 0 0 auto; }
  input {
    flex: 1;
    min-width: 0;
    border: 0;
    outline: none;
    background: transparent;
    font: inherit;
    font-size: 0.88rem;
    color: var(--ink);
    padding: 0.3rem 0;
  }
  input::placeholder { color: #666; }
  button {
    border: 0;
    border-radius: 999px;
    padding: 0.42rem 0.8rem;
    background: linear-gradient(135deg, #f5c518, #f59e0b);
    color: #111;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    flex: 0 0 auto;
  }
}
.actions {
  display: flex;
  gap: 0.6rem;
  align-items: center;
}
.user {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-weight: 600;
}
.nav-avatar {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  object-fit: cover;
  flex-shrink: 0;
  &.letter {
    display: grid;
    place-items: center;
    font-size: 0.72rem;
    font-weight: 700;
    color: #fff;
    background: linear-gradient(145deg, #6e3b72, #2f6b57);
  }
}
.ghost, .solid {
  border: 0;
  border-radius: 999px;
  padding: 0.45rem 0.95rem;
  cursor: pointer;
}
.ghost {
  background: transparent;
  color: var(--ink);
}
.solid {
  background: #f5c518;
  color: #111;
}
.main {
  padding: 1.5rem 0 3rem;
}
.theater-main {
  padding: 0;
  width: 100%;
  max-width: none;
}
.front-shell.theater {
  background: #0b0b0f;
}
@media (max-width: 720px) {
  .nav-inner { gap: 0.75rem; flex-wrap: wrap; }
  .links { display: flex; order: 3; flex-basis: 100%; padding-bottom: 0.6rem; overflow-x: auto; }
  .nav-search { order: 4; flex-basis: 100%; width: 100%; }
}
</style>
