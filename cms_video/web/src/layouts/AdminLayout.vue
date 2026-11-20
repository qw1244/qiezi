<template>
  <div class="admin-shell">
    <aside class="side">
      <router-link to="/admin" class="brand" aria-label="茄子短剧管理台">
        <BrandLogo tone="dark" />
        <span class="sub">内容管理台</span>
      </router-link>

      <div class="group-label">业务</div>
      <nav class="menu">
        <router-link
          v-for="item in businessMenus"
          :key="item.path"
          :to="item.path"
          class="item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="ico"><el-icon><component :is="item.icon" /></el-icon></span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="group-label">系统</div>
      <nav class="menu">
        <router-link
          v-for="item in systemMenus"
          :key="item.path"
          :to="item.path"
          class="item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="ico"><el-icon><component :is="item.icon" /></el-icon></span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="side-card">
        <strong>茄子短剧</strong>
        <p>合集上架、直传上传、资源清理一处完成。</p>
      </div>
    </aside>

    <div class="main">
      <header class="top">
        <div class="title-block">
          <span class="pill">ADMIN</span>
          <h1>{{ currentLabel }}</h1>
        </div>
        <div class="user">
          <div class="avatar">{{ avatarText }}</div>
          <div class="meta">
            <strong>{{ admin.profile?.nickname || admin.profile?.username || '管理员' }}</strong>
            <span>已登录</span>
          </div>
          <button class="logout" @click="onLogout">退出</button>
        </div>
      </header>
      <section class="content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Box,
  ChatDotRound,
  Collection,
  DataAnalysis,
  Document,
  FolderOpened,
  Key,
  Notebook,
  Upload,
  User,
} from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'
import BrandLogo from '@/components/BrandLogo.vue'

const route = useRoute()
const router = useRouter()
const admin = useAdminStore()

const businessMenus = [
  { path: '/admin/dash', label: '数据统计', icon: DataAnalysis },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/series', label: '合集管理', icon: Collection },
  { path: '/admin/upload', label: '上传记录', icon: Upload },
  { path: '/admin/review', label: '投稿审核', icon: Document },
  { path: '/admin/comments', label: '评论管理', icon: ChatDotRound },
]

const systemMenus = [
  { path: '/admin/cos', label: 'R2 配置', icon: Box },
  { path: '/admin/cos/files', label: 'R2 资源', icon: FolderOpened },
  { path: '/admin/oplog', label: '操作记录', icon: Notebook },
  { path: '/admin/pwd', label: '修改密码', icon: Key },
]

const allMenus = [...businessMenus, ...systemMenus]

const currentLabel = computed(() => {
  const hit = allMenus.find((m) => isActive(m.path))
  return hit?.label || '控制台'
})

const avatarText = computed(() => {
  const name = admin.profile?.nickname || admin.profile?.username || '管'
  return String(name).slice(0, 1)
})

function isActive(path: string) {
  if (path === '/admin/series') {
    return route.path === path || route.path.startsWith('/admin/series/')
  }
  if (path === '/admin/cos') {
    return route.path === '/admin/cos'
  }
  return route.path === path || route.path.startsWith(path + '/')
}

onMounted(() => {
  admin.fetchProfile().catch(() => undefined)
})

function onLogout() {
  admin.logout()
  router.push('/qz853/login')
}
</script>

<style scoped lang="scss">
.admin-shell {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
}
.side {
  margin: 0.85rem 0 0.85rem 0.85rem;
  border-radius: 28px;
  background:
    radial-gradient(circle at 18% 12%, rgba(139, 77, 140, 0.35), transparent 42%),
    linear-gradient(165deg, #321f3c 0%, #24162c 55%, #1b1222 100%);
  color: #fff;
  padding: 1.15rem 0.95rem 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  position: sticky;
  top: 0.85rem;
  height: calc(100vh - 1.7rem);
  box-shadow: 0 18px 50px rgba(36, 22, 44, 0.28);
}
.brand {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.45rem;
  padding: 0.2rem 0.2rem 0.95rem;
}
.sub {
  margin-left: 0.2rem;
  font-size: 0.75rem;
  color: rgba(255,255,255,0.5);
}
.group-label {
  margin: 0.55rem 0.55rem 0.15rem;
  font-size: 0.7rem;
  letter-spacing: 0.14em;
  color: rgba(255,255,255,0.38);
}
.menu {
  display: grid;
  gap: 0.28rem;
}
.item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.72rem 0.8rem;
  border-radius: 14px;
  color: rgba(255,255,255,0.72);
  transition: 0.18s ease;
}
.item:hover {
  background: rgba(255,255,255,0.06);
  color: #fff;
}
.item.active {
  background: rgba(255,255,255,0.1);
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.08);
}
.ico {
  width: 26px;
  height: 26px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  background: rgba(255,255,255,0.08);
  font-size: 14px;
}
.item.active .ico {
  background: linear-gradient(145deg, #a66bab, #6e3b72);
}
.side-card {
  margin-top: auto;
  padding: 0.95rem;
  border-radius: 18px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  strong {
    display: block;
    margin-bottom: 0.35rem;
  }
  p {
    margin: 0;
    font-size: 0.8rem;
    line-height: 1.55;
    color: rgba(255,255,255,0.55);
  }
}
.main {
  min-width: 0;
  display: grid;
  grid-template-rows: auto 1fr;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 1.2rem 1.45rem 0.55rem;
}
.title-block {
  display: flex;
  align-items: center;
  gap: 0.7rem;
}
.pill {
  font-size: 0.68rem;
  letter-spacing: 0.14em;
  padding: 0.35rem 0.55rem;
  border-radius: 999px;
  background: var(--egg-soft);
  color: var(--egg);
  font-family: Outfit, sans-serif;
  font-weight: 700;
}
.top h1 {
  margin: 0;
  font-size: 1.45rem;
}
.user {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  background: rgba(255,255,255,0.78);
  border: 1px solid var(--admin-line);
  border-radius: 999px;
  padding: 0.32rem 0.4rem 0.32rem 0.32rem;
  backdrop-filter: blur(10px);
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #8b4d8c, #3f7a5c);
  color: #fff;
  font-weight: 700;
}
.meta {
  display: grid;
  line-height: 1.15;
  strong { font-size: 0.88rem; }
  span {
    font-size: 0.7rem;
    color: var(--admin-muted);
  }
}
.logout {
  border: 0;
  background: var(--egg-deep);
  color: #fff;
  border-radius: 999px;
  padding: 0.55rem 0.9rem;
  cursor: pointer;
  font-size: 0.84rem;
}
.content {
  padding: 0.55rem 1.45rem 1.7rem;
}
@media (max-width: 980px) {
  .admin-shell { grid-template-columns: 1fr; }
  .side {
    position: relative;
    top: 0;
    height: auto;
    margin: 0.75rem;
  }
  .menu { grid-template-columns: 1fr 1fr; }
}
</style>
