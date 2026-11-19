import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '@/utils/http'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const profile = ref<any>(null)

  async function login(username: string, password: string) {
    const res: any = await http.post('/admin/login', { username, password })
    token.value = res.data.token
    profile.value = res.data.admin
    localStorage.setItem('admin_token', token.value)
    return res.data as { mustChangePwd?: boolean }
  }

  async function fetchProfile() {
    if (!token.value) return
    const res: any = await http.get('/admin/profile')
    profile.value = res.data
  }

  function logout() {
    http.post('/admin/logout').catch(() => undefined)
    token.value = ''
    profile.value = null
    localStorage.removeItem('admin_token')
  }

  return { token, profile, login, fetchProfile, logout }
})
