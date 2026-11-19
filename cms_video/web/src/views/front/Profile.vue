<template>
  <section class="page">
    <div class="glow a" aria-hidden="true"></div>
    <div class="glow b" aria-hidden="true"></div>

    <header class="intro">
      <button
        type="button"
        class="avatar"
        :class="{ uploading: avatarLoading }"
        :aria-label="avatarUrl ? '更换头像' : '上传头像'"
        :disabled="avatarLoading"
        @click="pickAvatar"
      >
        <img v-if="avatarUrl" :src="avatarUrl" alt="" />
        <span v-else>{{ avatarLetter }}</span>
        <em class="hint">{{ avatarLoading ? '上传中' : '换头像' }}</em>
      </button>
      <input
        ref="fileRef"
        type="file"
        accept="image/jpeg,image/png,image/webp,image/gif"
        hidden
        @change="onAvatarChange"
      />
      <div class="who">
        <p class="eyebrow">我的账号</p>
        <h1>{{ displayName }}</h1>
        <p class="sub">@{{ user.profile?.username || '—' }} · 点击头像可更换</p>
      </div>
      <div class="quick">
        <router-link to="/fav">收藏</router-link>
        <router-link to="/history">足迹</router-link>
        <button type="button" @click="onLogout">退出</button>
      </div>
    </header>

    <form class="sheet" @submit.prevent="onPwd">
      <div class="sheet-head">
        <h2>修改密码</h2>
        <p>保存后需重新登录，收藏与观看进度不受影响。</p>
      </div>

      <label>
        <span>原密码</span>
        <input
          v-model="form.oldPwd"
          type="password"
          autocomplete="current-password"
          placeholder="输入当前密码"
        />
      </label>
      <label>
        <span>新密码</span>
        <input
          v-model="form.newPwd"
          type="password"
          autocomplete="new-password"
          placeholder="至少 8 位，建议字母 + 数字"
        />
      </label>

      <div class="actions">
        <button class="submit" type="submit" :disabled="loading">
          {{ loading ? '保存中…' : '保存新密码' }}
        </button>
        <router-link class="back" to="/">返回发现</router-link>
      </div>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import http from '@/utils/http'
import { uploadUserAvatar } from '@/utils/cosUpload'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const router = useRouter()
const loading = ref(false)
const avatarLoading = ref(false)
const fileRef = ref<HTMLInputElement | null>(null)
const form = reactive({ oldPwd: '', newPwd: '' })

const displayName = computed(() => user.profile?.nickname || user.profile?.username || '我的账号')
const avatarLetter = computed(() => (displayName.value.trim().charAt(0) || '茄').toUpperCase())
const avatarUrl = computed(() => (user.profile?.avatar as string) || '')

onMounted(() => user.fetchProfile())

function pickAvatar() {
  if (avatarLoading.value) return
  fileRef.value?.click()
}

async function onAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/') || file.type.includes('svg')) {
    ElMessage.warning('请选择 JPG / PNG / WEBP / GIF 图片')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像请小于 2MB')
    return
  }
  avatarLoading.value = true
  try {
    const { url } = await uploadUserAvatar(file)
    if (!url) throw new Error('未获取到访问地址，请检查 R2 公开访问域名')
    const res: any = await http.post('/user/avatar', { avatar: url })
    user.setProfile(res.data)
    ElMessage.success('头像已更新')
  } catch (err: any) {
    if (!err?.msg) {
      ElMessage.error(err?.message || '头像上传失败')
    }
  } finally {
    avatarLoading.value = false
  }
}

async function onPwd() {
  if (!form.oldPwd || !form.newPwd) {
    ElMessage.warning('请填写完整')
    return
  }
  if (form.newPwd.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  loading.value = true
  try {
    await http.post('/user/pwd', form)
    ElMessage.success('密码已修改，请重新登录')
    user.logout()
    router.push('/login')
  } finally {
    loading.value = false
  }
}

function onLogout() {
  user.logout()
  router.push('/')
}
</script>

<style scoped lang="scss">
.page {
  position: relative;
  max-width: 640px;
  margin: 0 auto;
  padding: 0.4rem 0 2.4rem;
  isolation: isolate;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(42px);
  pointer-events: none;
  z-index: -1;
}
.glow.a {
  width: 220px;
  height: 220px;
  top: -40px;
  left: -60px;
  background: rgba(110, 59, 114, 0.16);
  animation: drift 9s ease-in-out infinite;
}
.glow.b {
  width: 180px;
  height: 180px;
  right: -40px;
  top: 120px;
  background: rgba(47, 107, 87, 0.14);
  animation: drift 11s ease-in-out infinite reverse;
}

.intro {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 1rem 1.1rem;
  margin-bottom: 1.35rem;
  animation: rise 0.45s ease both;
}

.avatar {
  position: relative;
  width: 72px;
  height: 72px;
  border: 0;
  padding: 0;
  border-radius: 22px;
  overflow: hidden;
  display: grid;
  place-items: center;
  font-family: Outfit, 'Noto Sans SC', sans-serif;
  font-size: 1.55rem;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.28), transparent 42%),
    linear-gradient(145deg, #6e3b72, #2f6b57);
  box-shadow: 0 14px 28px rgba(43, 24, 51, 0.18);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
  .hint {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    margin: 0;
    padding: 0.28rem 0 0.32rem;
    font-style: normal;
    font-size: 0.68rem;
    font-weight: 600;
    letter-spacing: 0.02em;
    background: rgba(20, 12, 28, 0.55);
    color: #fff;
    opacity: 0;
    transform: translateY(4px);
    transition: opacity 0.18s ease, transform 0.18s ease;
  }
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 16px 32px rgba(43, 24, 51, 0.22);
    .hint {
      opacity: 1;
      transform: translateY(0);
    }
  }
  &:disabled {
    cursor: wait;
  }
  &.uploading .hint {
    opacity: 1;
    transform: translateY(0);
  }
}

.who {
  min-width: 0;
}
.eyebrow {
  margin: 0 0 0.2rem;
  font-family: Outfit, sans-serif;
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--muted);
}
.who h1 {
  margin: 0;
  font-size: clamp(1.45rem, 3vw, 1.85rem);
  line-height: 1.2;
  letter-spacing: -0.02em;
}
.sub {
  margin: 0.28rem 0 0;
  color: var(--muted);
  font-size: 0.92rem;
}

.quick {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  justify-content: flex-end;
  a,
  button {
    border: 1px solid var(--line);
    background: rgba(255, 253, 248, 0.72);
    color: var(--text, #2b1833);
    border-radius: 999px;
    padding: 0.42rem 0.85rem;
    font-size: 0.84rem;
    text-decoration: none;
    cursor: pointer;
    transition: border-color 0.18s ease, transform 0.18s ease, background 0.18s ease;
  }
  a:hover,
  button:hover {
    border-color: rgba(110, 59, 114, 0.35);
    background: #fff;
    transform: translateY(-1px);
  }
  button {
    font: inherit;
  }
}

.sheet {
  display: grid;
  gap: 1rem;
  padding: 1.45rem 1.5rem 1.35rem;
  border-radius: 24px;
  border: 1px solid var(--line);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(255, 253, 248, 0.96));
  box-shadow: 0 18px 40px rgba(43, 24, 51, 0.06);
  animation: rise 0.55s ease 0.06s both;
}

.sheet-head h2 {
  margin: 0 0 0.35rem;
  font-size: 1.15rem;
}
.sheet-head p {
  margin: 0 0 0.2rem;
  color: var(--muted);
  font-size: 0.9rem;
  line-height: 1.55;
}

label {
  display: grid;
  gap: 0.42rem;
  span {
    font-size: 0.84rem;
    color: var(--muted);
  }
  input {
    width: 100%;
    border: 1px solid var(--line);
    background: #fff;
    border-radius: 14px;
    padding: 0.92rem 1rem;
    outline: none;
    font: inherit;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }
  input:focus {
    border-color: rgba(110, 59, 114, 0.45);
    box-shadow: 0 0 0 4px rgba(110, 59, 114, 0.1);
  }
}

.actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.85rem;
  margin-top: 0.25rem;
}

.submit {
  border: 0;
  border-radius: 999px;
  padding: 0.88rem 1.45rem;
  background: #2b1833;
  color: #fff;
  cursor: pointer;
  font: inherit;
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

.back {
  color: var(--muted);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.18s ease;
}
.back:hover {
  color: var(--accent);
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes drift {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(12px, 8px);
  }
}

@media (max-width: 640px) {
  .intro {
    grid-template-columns: auto 1fr;
  }
  .quick {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }
  .sheet {
    padding: 1.2rem 1.1rem 1.15rem;
  }
  .actions {
    flex-direction: column;
    align-items: stretch;
  }
  .submit {
    width: 100%;
  }
  .back {
    text-align: center;
  }
}
</style>
