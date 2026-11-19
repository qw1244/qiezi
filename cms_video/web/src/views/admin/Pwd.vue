<template>
  <div class="admin-page">
    <div class="admin-panel form-panel">
      <p class="hint">新密码至少 8 位，请勿使用 admin123 / 123456 等默认弱口令。</p>
      <el-form label-width="90px" style="max-width:420px" @submit.prevent="save">
        <el-form-item label="原密码">
          <el-input v-model="form.oldPwd" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPwd" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import http from '@/utils/http'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const admin = useAdminStore()
const form = reactive({ oldPwd: '', newPwd: '' })

async function save() {
  if (!form.oldPwd || !form.newPwd) {
    ElMessage.warning('请填写完整')
    return
  }
  if (form.newPwd.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  await http.post('/admin/pwd', form)
  ElMessage.success('密码已修改，请重新登录')
  admin.logout()
  router.push('/qz853/login')
}
</script>

<style scoped>
.form-panel { padding: 1.4rem 1.3rem 0.4rem; }
.hint { margin: 0 0 1rem; color: #8a6d3b; font-size: 13px; line-height: 1.5; }
</style>
