<template>
  <div class="auth">
    <div class="panel">
      <div class="visual">
        <div class="decor" aria-hidden="true">
          <div class="orb o1"></div>
          <div class="orb o2"></div>
          <div class="phone">
            <div class="screen">
              <div class="bar"></div>
              <div class="lines">
                <i></i><i></i><i></i>
              </div>
              <div class="chip">续看中</div>
            </div>
          </div>
          <div class="card c1"></div>
          <div class="card c2"></div>
        </div>
        <div class="text">
          <p class="eyebrow">QIEZI</p>
          <h1>回来接着看</h1>
          <p class="copy">登录后同步收藏、足迹和续看进度。未登录也能直接刷剧。</p>
        </div>
      </div>
      <form class="form" @submit.prevent="onSubmit">
        <h2>登录</h2>
        <label>
          <span>账号</span>
          <input v-model="form.username" autocomplete="username" placeholder="输入账号" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="current-password" placeholder="输入密码" />
        </label>
        <button class="submit" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '进入茄子' }}
        </button>
        <p class="switch">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
          <span class="dot">·</span>
          <router-link to="/">先去看看</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onSubmit() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    await user.login(form.username, form.password)
    router.replace((route.query.redirect as string) || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth {
  min-height: calc(100vh - 68px);
  display: grid;
  place-items: center;
  padding: 1.2rem 0 2.4rem;
}
.panel {
  width: min(100%, 920px);
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid var(--line);
  background: rgba(255, 253, 248, 0.92);
  box-shadow: 0 24px 60px rgba(43, 24, 51, 0.1);
}
.visual {
  position: relative;
  padding: clamp(1.4rem, 3vw, 2.2rem);
  color: #fff;
  min-height: 460px;
  display: grid;
  grid-template-rows: 1fr auto;
  gap: 1rem;
  background:
    radial-gradient(circle at 18% 18%, rgba(155, 94, 160, 0.45), transparent 36%),
    radial-gradient(circle at 86% 78%, rgba(63, 122, 92, 0.35), transparent 34%),
    linear-gradient(155deg, #4a2952 0%, #2b1833 48%, #1f3d32 120%);
  overflow: hidden;
}
.decor {
  position: relative;
  min-height: 220px;
}
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
}
.orb.o1 {
  width: 140px;
  height: 140px;
  left: -20px;
  top: 10px;
  background: rgba(255, 255, 255, 0.12);
  animation: pulse 5s ease-in-out infinite;
}
.orb.o2 {
  width: 90px;
  height: 90px;
  right: 18px;
  top: 36px;
  background: rgba(63, 122, 92, 0.35);
  animation: pulse 6s ease-in-out infinite reverse;
}
.phone {
  position: absolute;
  left: 50%;
  top: 18px;
  transform: translateX(-50%);
  width: 118px;
  aspect-ratio: 9 / 16;
  border-radius: 22px;
  padding: 7px;
  background: linear-gradient(160deg, #5f3a66, #1c1424);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.28);
  animation: float 5.2s ease-in-out infinite;
  z-index: 2;
}
.screen {
  height: 100%;
  border-radius: 16px;
  background:
    linear-gradient(180deg, transparent 50%, rgba(0,0,0,0.55)),
    linear-gradient(145deg, #8b4d8c, #3f7a5c);
  position: relative;
  overflow: hidden;
}
.bar {
  width: 36%;
  height: 5px;
  border-radius: 999px;
  background: rgba(255,255,255,0.35);
  margin: 10px auto 0;
}
.lines {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 42px;
  display: grid;
  gap: 6px;
  i {
    display: block;
    height: 6px;
    border-radius: 999px;
    background: rgba(255,255,255,0.35);
  }
  i:nth-child(1) { width: 78%; }
  i:nth-child(2) { width: 58%; }
  i:nth-child(3) { width: 42%; }
}
.chip {
  position: absolute;
  left: 10px;
  bottom: 12px;
  font-size: 0.68rem;
  padding: 0.28rem 0.5rem;
  border-radius: 999px;
  background: rgba(0,0,0,0.35);
}
.card {
  position: absolute;
  width: 78px;
  aspect-ratio: 3 / 4;
  border-radius: 14px;
  opacity: 0.7;
  box-shadow: 0 12px 28px rgba(0,0,0,0.18);
}
.card.c1 {
  left: 18px;
  top: 56px;
  transform: rotate(-14deg);
  background: linear-gradient(145deg, rgba(255,255,255,0.22), rgba(110,59,114,0.55));
  animation: tilt 6s ease-in-out infinite;
}
.card.c2 {
  right: 16px;
  bottom: 8px;
  transform: rotate(12deg);
  background: linear-gradient(145deg, rgba(63,122,92,0.55), rgba(255,255,255,0.18));
  animation: tilt 6.8s ease-in-out infinite reverse;
}
.text {
  position: relative;
  z-index: 2;
}
.eyebrow {
  margin: 0;
  letter-spacing: 0.22em;
  font-family: Outfit, sans-serif;
  font-size: 0.78rem;
  opacity: 0.9;
}
.visual h1 {
  margin: 0.55rem 0 0.55rem;
  font-family: 'Noto Sans SC', Outfit, sans-serif;
  font-size: clamp(1.9rem, 4vw, 2.6rem);
  line-height: 1.15;
}
.copy {
  margin: 0;
  max-width: 18rem;
  line-height: 1.7;
  color: rgba(255,255,255,0.86);
  font-size: 0.92rem;
}
.form {
  padding: clamp(1.6rem, 4vw, 2.6rem);
  display: grid;
  gap: 1rem;
  align-content: center;
}
.form h2 {
  margin: 0 0 0.4rem;
  font-size: 1.55rem;
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
    border: 1px solid var(--line);
    background: #fff;
    border-radius: 14px;
    padding: 0.9rem 1rem;
    outline: none;
    transition: border-color .2s ease, box-shadow .2s ease;
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
  transition: transform .18s ease, opacity .18s ease;
}
.submit:hover:not(:disabled) {
  transform: translateY(-1px);
}
.submit:disabled {
  opacity: 0.65;
  cursor: default;
}
.switch {
  margin: 0.4rem 0 0;
  color: var(--muted);
  font-size: 0.92rem;
  a {
    color: var(--accent);
    font-weight: 600;
  }
  .dot {
    margin: 0 0.35rem;
    opacity: 0.5;
  }
}
@keyframes float {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(-8px); }
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.85; }
  50% { transform: scale(1.08); opacity: 1; }
}
@keyframes tilt {
  0%, 100% { transform: rotate(-14deg) translateY(0); }
  50% { transform: rotate(-10deg) translateY(6px); }
}
@media (max-width: 820px) {
  .panel { grid-template-columns: 1fr; }
  .visual { min-height: 300px; }
  .decor { min-height: 180px; }
}
</style>
