import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '@/utils/http'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const profile = ref<any>(null)

  async function login(username: string, password: string) {
    const res: any = await http.post('/user/login', { username, password })
    token.value = res.data.token
    profile.value = res.data.user
    localStorage.setItem('user_token', token.value)
  }

  async function register(username: string, password: string, nickname?: string) {
    const res: any = await http.post('/user/register', { username, password, nickname })
    token.value = res.data.token
    profile.value = res.data.user
    localStorage.setItem('user_token', token.value)
  }

  async function fetchProfile() {
    if (!token.value) return
    const res: any = await http.get('/user/profile')
    profile.value = res.data
  }

  function setProfile(next: any) {
    profile.value = next
  }

  function logout() {
    http.post('/user/logout').catch(() => undefined)
    token.value = ''
    profile.value = null
    localStorage.removeItem('user_token')
  }

  return { token, profile, login, register, fetchProfile, setProfile, logout }
})
