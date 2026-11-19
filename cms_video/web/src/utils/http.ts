import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

function isAdminApi(url = '') {
  return url.startsWith('/admin')
}

function isGuestApi(url = '') {
  return url.startsWith('/series') || url.startsWith('/video')
}

http.interceptors.request.use((config) => {
  const adminToken = localStorage.getItem('admin_token')
  const userToken = localStorage.getItem('user_token')
  const path = config.url || ''
  const token = isAdminApi(path) ? adminToken : userToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      const url = res.config.url || ''
      if (body.code === 401) {
        if (isAdminApi(url)) {
          localStorage.removeItem('admin_token')
          if (router.currentRoute.value.path !== '/qz853/login') {
            router.push('/qz853/login')
          }
        } else {
          localStorage.removeItem('user_token')
          if (!isGuestApi(url) && router.currentRoute.value.path !== '/login') {
            router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
          }
        }
      }
      const quietAuth =
        body.code === 401 &&
        (isGuestApi(url) ||
          router.currentRoute.value.path === '/qz853/login' ||
          router.currentRoute.value.path === '/login')
      if (!quietAuth) {
        ElMessage.error(body.msg || '请求失败')
      }
      return Promise.reject(body)
    }
    return body
  },
  (err) => {
    ElMessage.error(err.message || '网络异常')
    return Promise.reject(err)
  },
)

export default http
