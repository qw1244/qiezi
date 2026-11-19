<template>
  <div class="admin-page">
    <div class="admin-panel form-panel">
      <p class="hint">
        当前对接 Cloudflare R2（S3 兼容）。请先在 R2 创建存储桶与 API 令牌，并配置桶 CORS 允许前端直传；
        「公开访问域名」填 r2.dev 公共域名或自定义域名（播放/封面必须）。
      </p>
      <el-form label-width="130px" style="max-width:680px">
        <el-form-item label="Access Key ID">
          <el-input v-model="form.secretId" placeholder="R2 API 令牌 Access Key ID，留空则不修改" />
        </el-form-item>
        <el-form-item label="Secret Access Key">
          <el-input v-model="form.secretKey" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="Account ID">
          <el-input v-model="form.region" placeholder="Cloudflare 账户 ID，如 c78ec5f3f8168a38..." />
        </el-form-item>
        <el-form-item label="Bucket">
          <el-input v-model="form.bucket" placeholder="R2 存储桶名称" />
        </el-form-item>
        <el-form-item label="公开访问域名">
          <el-input v-model="form.cdnHost" placeholder="https://pub-xxxx.r2.dev 或自定义域名" />
        </el-form-item>
        <el-form-item label="路径前缀">
          <el-input v-model="form.pathPrefix" placeholder="video/" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="enabled" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
      <div class="tips">
        <p>S3 API 端点形态：</p>
        <code>https://&lt;AccountID&gt;.r2.cloudflarestorage.com</code>
        <p class="muted">桶 CORS 建议允许 PUT / GET / HEAD，Headers 含 Content-Type，Origin 填你的前台域名或 *（仅调试）。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'

const saving = ref(false)
const form = reactive<any>({
  secretId: '',
  secretKey: '',
  region: '',
  bucket: '',
  cdnHost: '',
  pathPrefix: 'video/',
  enabled: 0,
})

const enabled = computed({
  get: () => form.enabled === 1,
  set: (v: boolean) => { form.enabled = v ? 1 : 0 },
})

onMounted(async () => {
  const res: any = await http.get('/admin/cos/cfg')
  const data = res.data || {}
  form.region = data.region || ''
  form.bucket = data.bucket || ''
  form.cdnHost = data.cdnHost || ''
  form.pathPrefix = data.pathPrefix || 'video/'
  form.enabled = data.enabled || 0
  form.secretId = ''
  form.secretKey = ''
})

async function save() {
  if (form.enabled === 1 && !form.cdnHost) {
    ElMessage.warning('启用 R2 时请填写公开访问域名')
    return
  }
  const accessKey = (form.secretId || '').trim()
  const secretKey = (form.secretKey || '').trim()
  if (accessKey.startsWith('http') || accessKey.startsWith('cfat_')) {
    ElMessage.warning('Access Key ID 请填「访问密钥 ID」，不要填网址或 cfat_ 令牌')
    return
  }
  if (!accessKey || !secretKey) {
    ElMessage.warning('请同时重新粘贴 Access Key ID 与 Secret Access Key 后再保存')
    return
  }
  saving.value = true
  try {
    const payload: any = {
      region: (form.region || '').trim(),
      bucket: (form.bucket || '').trim(),
      cdnHost: (form.cdnHost || '').trim(),
      pathPrefix: form.pathPrefix,
      enabled: form.enabled,
      secretId: accessKey,
      secretKey,
    }
    await http.post('/admin/cos/cfg', payload)
    form.secretId = ''
    form.secretKey = ''
    ElMessage.success('已保存，可到「R2 资源」页验证能否列出对象')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.form-panel { padding: 1.4rem 1.3rem 0.8rem; }
.hint {
  margin: 0 0 1.1rem;
  padding: 0.85rem 1rem;
  border-radius: 12px;
  background: #f6f1f8;
  color: #5a4a62;
  font-size: 0.9rem;
  line-height: 1.6;
}
.tips {
  margin-top: 0.6rem;
  padding-top: 0.8rem;
  border-top: 1px dashed var(--admin-line);
  p { margin: 0 0 0.35rem; color: var(--admin-muted); font-size: 0.86rem; }
  code {
    display: inline-block;
    margin-bottom: 0.55rem;
    padding: 0.35rem 0.55rem;
    border-radius: 8px;
    background: #f3eef5;
    font-size: 0.82rem;
  }
  .muted { color: var(--admin-muted); }
}
</style>
