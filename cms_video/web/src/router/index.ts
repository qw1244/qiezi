import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/FrontLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/front/Home.vue') },
        { path: 'series/:id', name: 'series', component: () => import('@/views/front/Series.vue') },
        { path: 'play/:id', name: 'play', component: () => import('@/views/front/Play.vue') },
        { path: 'fav', name: 'fav', component: () => import('@/views/front/Fav.vue') },
        { path: 'history', name: 'history', component: () => import('@/views/front/History.vue') },
        { path: 'login', name: 'login', component: () => import('@/views/front/Login.vue'), meta: { bare: true } },
        { path: 'register', name: 'register', component: () => import('@/views/front/Register.vue'), meta: { bare: true } },
        { path: 'profile', name: 'profile', component: () => import('@/views/front/Profile.vue'), meta: { auth: true } },
      ],
    },
    {
      path: '/qz853/login',
      name: 'admin-login',
      component: () => import('@/views/admin/Login.vue'),
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { admin: true },
      children: [
        { path: '', redirect: '/admin/dash' },
        { path: 'dash', name: 'admin-dash', component: () => import('@/views/admin/Dash.vue') },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/Users.vue') },
        { path: 'series', name: 'admin-series', component: () => import('@/views/admin/Series.vue') },
        { path: 'series/:id', name: 'admin-series-detail', component: () => import('@/views/admin/SeriesDetail.vue') },
        { path: 'cos', name: 'admin-cos', component: () => import('@/views/admin/CosCfg.vue') },
        { path: 'cos/files', name: 'admin-cos-files', component: () => import('@/views/admin/CosFiles.vue') },
        { path: 'upload', name: 'admin-upload', component: () => import('@/views/admin/UploadJobs.vue') },
        { path: 'oplog', name: 'admin-oplog', component: () => import('@/views/admin/OpLog.vue') },
        { path: 'pwd', name: 'admin-pwd', component: () => import('@/views/admin/Pwd.vue') },
      ],
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  if (to.meta.auth && !localStorage.getItem('user_token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.admin && !localStorage.getItem('admin_token')) {
    return { path: '/qz853/login' }
  }
  return true
})

export default router
