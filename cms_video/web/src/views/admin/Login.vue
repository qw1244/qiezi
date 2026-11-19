<template>
  <div class="page">
    <div class="glow a"></div>
    <div class="glow b"></div>
    <div class="panel">
      <aside class="side">
        <p class="eyebrow">管理后台</p>
        <BrandLogo class="lg" tone="dark" />
        <p class="desc">合集、视频、R2 存储与数据看板，都在这里管理。</p>
        <ul>
          <li>合集与分集</li>
          <li>前端直传 R2</li>
          <li>上传任务追踪</li>
        </ul>
      </aside>
      <form class="form" @submit.prevent="onSubmit">
        <div class="head">
          <h2>后台登录</h2>
          <p>使用管理员账号进入控制台</p>
        </div>
        <label>
          <span>账号</span>
          <input v-model="form.username" autocomplete="username" placeholder="管理员账号" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="current-password" placeholder="登录密码" />
        </label>
        <button class="submit" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '进入后台' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import BrandLogo from '@/components/BrandLogo.vue'

const admin = useAdminStore()
const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

onMounted(() => {
  if (admin.token) {
    admin.fetchProfile()
      .then(() => router.replace('/admin/dash'))
      .catch(() => {
        admin.token = ''
        localStorage.removeItem('admin_token')
      })
  }
})

async function onSubmit() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const data = await admin.login(form.username, form.password)
    if (data?.mustChangePwd) {
      ElMessage.warning('当前为默认弱密码，请先修改后再使用后台')
      router.replace('/admin/pwd')
    } else {
      router.replace('/admin/dash')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background:
    radial-gradient(circle at 12% 18%, rgba(110, 59, 114, 0.28), transparent 34%),
    radial-gradient(circle at 88% 12%, rgba(63, 122, 92, 0.18), transparent 32%),
    linear-gradient(155deg, #1b1222 0%, #2b1833 48%, #17101d 100%);
}
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
}
.glow.a {
  width: 280px;
  height: 280px;
  left: 8%;
  bottom: 12%;
  background: rgba(110, 59, 114, 0.22);
}
.glow.b {
  width: 240px;
  height: 240px;
  right: 10%;
  top: 18%;
  background: rgba(63, 122, 92, 0.16);
}
.panel {
  position: relative;
  z-index: 1;
  width: min(100%, 880px);
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 253, 248, 0.96);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.35);
}
.side {
  padding: clamp(1.8rem, 4vw, 2.8rem);
  color: #fff;
  background:
    linear-gradient(155deg, rgba(43, 24, 51, 0.98), rgba(74, 41, 82, 0.88)),
    radial-gradient(circle at 20% 20%, rgba(63, 122, 92, 0.35), transparent 48%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  min-height: 460px;
}
.eyebrow {
  margin: 0;
  letter-spacing: 0.2em;
  font-size: 0.75rem;
  opacity: 0.75;
  font-family: Outfit, sans-serif;
}
:deep(.brand-lockup) {
  margin: 0.85rem 0 1rem;
}
.desc {
  margin: 0 0 1.4rem;
  max-width: 16rem;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}
.side ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 0.55rem;
}
.side li {
  font-size: 0.92rem;
  color: rgba(255, 255, 255, 0.9);
  padding-left: 1rem;
  position: relative;
}
.side li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.55rem;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9b5ea0;
}
.form {
  padding: clamp(1.8rem, 4vw, 2.8rem);
  display: grid;
  gap: 1rem;
  align-content: center;
  background: #fffdf8;
}
.head h2 {
  margin: 0 0 0.35rem;
  font-size: 1.55rem;
}
.head p {
  margin: 0 0 0.4rem;
  color: var(--muted);
  font-size: 0.92rem;
}
label {
  display: grid;
  gap: 0.45rem;
  span {
    font-size: 0.86rem;
    color: var(--muted);
  }
  input {
    width: 100%;
    border: 1px solid rgba(18, 20, 26, 0.12);
    background: #fff;
    border-radius: 14px;
    padding: 0.92rem 1rem;
    outline: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }
  input:focus {
    border-color: rgba(110, 59, 114, 0.55);
    box-shadow: 0 0 0 4px rgba(110, 59, 114, 0.12);
  }
}
.submit {
  margin-top: 0.35rem;
  border: 0;
  border-radius: 999px;
  padding: 0.95rem 1.2rem;
  background: #2b1833;
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  transition: transform 0.18s ease, opacity 0.18s ease;
}
.submit:hover:not(:disabled) {
  transform: translateY(-1px);
}
.submit:disabled {
  opacity: 0.65;
  cursor: default;
}
@media (max-width: 820px) {
  .panel {
    grid-template-columns: 1fr;
  }
  .side {
    min-height: 220px;
  }
}
</style>
