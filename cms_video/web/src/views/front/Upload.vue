<template>
  <section class="page">
    <header class="head">
      <h1>投稿</h1>
      <p>新建一个合集并上传视频，合集需管理员审核通过后才会在网站上显示。合集通过后，还可以继续往里面传视频。</p>
    </header>

    <div class="panel">
      <h2>新建合集</h2>
      <div class="row">
        <span class="label">标题</span>
        <el-input v-model="form.title" placeholder="合集标题，比如：重生之我在深圳打工" style="width: 100%; max-width: 320px" :disabled="uploading" />
      </div>
      <div class="row">
        <span class="label">分类</span>
        <el-select v-model="form.category" style="width: 160px" :disabled="uploading">
          <el-option label="真人" value="真人" />
          <el-option label="AI" value="AI" />
        </el-select>
      </div>
      <div class="row">
        <span class="label">简介</span>
        <el-input
          v-model="form.intro"
          type="textarea"
          :rows="2"
          placeholder="简单介绍这个合集（可不填）"
          style="width: 100%; max-width: 420px"
          :disabled="uploading"
        />
      </div>
      <div class="row">
        <span class="label">封面</span>
        <input ref="coverRef" type="file" accept="image/*" style="display: none" @change="onPickCover" />
        <el-button :disabled="uploading" @click="coverRef?.click()">选择封面图片</el-button>
        <img v-if="coverPreview" :src="coverPreview" class="cover-preview" alt="" />
        <span v-else class="muted">可不选，审核通过后管理员可以补</span>
      </div>
    </div>

    <div class="panel">
      <h2>视频</h2>
      <div class="row">
        <input ref="fileRef" type="file" multiple accept="video/*" style="display: none" @change="onPickFiles" />
        <el-button :disabled="uploading" @click="fileRef?.click()">选择视频文件</el-button>
        <span class="muted">已选 {{ rows.length }} 个，按选择顺序排集数</span>
      </div>

      <el-table v-if="rows.length" :data="rows" max-height="360">
        <el-table-column label="文件" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.file.name }}</template>
        </el-table-column>
        <el-table-column label="标题" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.title" :disabled="uploading" placeholder="不填则按集数自动命名" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" :disabled="uploading" @click="rows.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <p v-if="tip" class="tip">{{ tip }}</p>

      <div class="actions">
        <el-button type="primary" :loading="uploading" :disabled="!rows.length || !form.title.trim()" @click="startUpload">
          提交审核
        </el-button>
      </div>
    </div>

    <div class="panel" v-if="approvedSeries.length">
      <h2>传到我的合集</h2>
      <div class="row">
        <span class="label">合集</span>
        <el-select v-model="existingSeriesId" placeholder="选择已通过的合集" style="width: 100%; max-width: 320px" :disabled="uploading">
          <el-option
            v-for="s in approvedSeries"
            :key="s.id"
            :label="s.title"
            :value="s.id"
          />
        </el-select>
      </div>
      <div class="row">
        <input ref="fileRef2" type="file" multiple accept="video/*" style="display: none" @change="onPickFiles2" />
        <el-button :disabled="uploading" @click="fileRef2?.click()">选择视频文件</el-button>
        <span class="muted">已选 {{ rows2.length }} 个，新传的视频也要审核，通过后才显示</span>
      </div>

      <el-table v-if="rows2.length" :data="rows2" max-height="360">
        <el-table-column label="文件" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.file.name }}</template>
        </el-table-column>
        <el-table-column label="标题" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.title" :disabled="uploading" placeholder="不填则按集数自动命名" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" :disabled="uploading" @click="rows2.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <p v-if="tip2" class="tip">{{ tip2 }}</p>

      <div class="actions">
        <el-button type="primary" :loading="uploading" :disabled="!rows2.length || !existingSeriesId" @click="startUploadExisting">
          上传到该合集
        </el-button>
      </div>
    </div>

    <div class="panel">
      <h2>我的合集</h2>
      <el-table :data="mySeries">
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="70" />
        <el-table-column prop="epCount" label="集数" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '已上架' : '待审核' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
      </el-table>
      <el-pagination
        class="pager"
        background
        layout="prev, pager, next"
        :total="seriesTotal"
        v-model:current-page="seriesPage"
        @current-change="loadMySeries"
      />
    </div>

    <div class="panel">
      <h2>我的投稿任务</h2>
      <el-table :data="jobs">
        <el-table-column prop="id" label="任务ID" width="90" />
        <el-table-column prop="seriesId" label="合集ID" width="90" />
        <el-table-column prop="total" label="总数" width="70" />
        <el-table-column prop="okNum" label="成功" width="70" />
        <el-table-column prop="failNum" label="失败" width="70" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        background
        layout="prev, pager, next"
        :total="jobTotal"
        v-model:current-page="jobPage"
        @current-change="loadJobs"
      />
    </div>

    <el-drawer v-model="drawer" title="投稿明细" size="520px">
      <el-table :data="items">
        <el-table-column prop="epNo" label="集" width="60" />
        <el-table-column prop="fileName" label="文件" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column prop="errMsg" label="失败详情" min-width="160" show-overflow-tooltip />
      </el-table>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'
import { putToCos } from '@/utils/cosUpload'
import { captureThumbFromFile } from '@/utils/videoThumb'

const form = reactive({ title: '', category: 'AI', intro: '' })
const coverFile = ref<File | null>(null)
const coverPreview = ref('')
const coverRef = ref<HTMLInputElement | null>(null)
const rows = ref<{ file: File; title: string }[]>([])
const fileRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const tip = ref('')

const existingSeriesId = ref<number | null>(null)
const rows2 = ref<{ file: File; title: string }[]>([])
const fileRef2 = ref<HTMLInputElement | null>(null)
const tip2 = ref('')

const mySeries = ref<any[]>([])
const seriesPage = ref(1)
const seriesTotal = ref(0)
const approvedSeries = computed(() => mySeries.value.filter((s: any) => s.status === 1))
const jobs = ref<any[]>([])
const jobPage = ref(1)
const jobTotal = ref(0)
const drawer = ref(false)
const items = ref<any[]>([])

function onPickCover(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  coverFile.value = file
  coverPreview.value = URL.createObjectURL(file)
}

function onPickFiles(e: Event) {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  for (const file of files) {
    rows.value.push({ file, title: '' })
  }
}

function onPickFiles2(e: Event) {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  for (const file of files) {
    rows2.value.push({ file, title: '' })
  }
}

async function uploadToSeries(seriesId: number, list: { file: File; title: string }[], setTip: (t: string) => void) {
  setTip(`正在创建上传任务（${list.length} 个文件）...`)
  const createRes: any = await http.post('/upload/job', {
    seriesId,
    files: list.map((r) => ({
      fileName: r.file.name,
      title: (r.title || '').trim(),
    })),
  })
  const job = createRes.data
  const itemsRes = job.items || []
  let ok = 0
  let fail = 0
  setTip(`任务 #${job.id} 上传中，请勿关闭页面...`)
  await Promise.all(itemsRes.map(async (item: any, i: number) => {
    const file = list[i]?.file
    if (!file) {
      fail += 1
      return
    }
    try {
      let duration = 0
      try {
        const thumb = await captureThumbFromFile(file)
        duration = thumb.duration
      } catch {
        // 取不到时长也不影响上传
      }
      await putToCos(item.putUrl, file)
      await http.post('/upload/confirm', {
        itemId: item.id,
        cosKey: item.cosKey,
        ok: true,
        duration,
      })
      ok += 1
    } catch (err: any) {
      await http.post('/upload/confirm', {
        itemId: item.id,
        ok: false,
        errMsg: err?.message || '上传失败',
      }).catch(() => undefined)
      fail += 1
    }
    setTip(`任务 #${job.id}：成功 ${ok} / 失败 ${fail} / 合计 ${itemsRes.length}`)
  }))
  return { ok, fail }
}

async function startUpload() {
  if (!form.title.trim() || !rows.value.length) return
  const list = [...rows.value]
  uploading.value = true
  tip.value = '正在新建合集...'
  try {
    const seriesRes: any = await http.post('/user/series', {
      title: form.title.trim(),
      category: form.category,
      intro: (form.intro || '').trim(),
    })
    const seriesId = seriesRes.data
    if (coverFile.value) {
      try {
        tip.value = '正在上传封面...'
        const credRes: any = await http.post('/user/series/cover-cred', {
          fileName: coverFile.value.name,
          contentType: coverFile.value.type || 'image/jpeg',
        })
        const cred = credRes.data
        await putToCos(cred.putUrl, coverFile.value, cred.contentType || coverFile.value.type)
        await http.post(`/user/series/${seriesId}/cover`, { cover: cred.url })
      } catch {
        // 封面失败不影响投稿，管理员可以后补
      }
    }
    const { ok, fail } = await uploadToSeries(seriesId, list, (t) => { tip.value = t })
    ElMessage.success(`合集已提交审核：成功 ${ok}，失败 ${fail}，通过后才会在网站上显示`)
    form.title = ''
    form.intro = ''
    coverFile.value = null
    coverPreview.value = ''
    rows.value = []
    loadMySeries()
    loadJobs()
  } catch (err: any) {
    if (!err?.msg) {
      ElMessage.error(err?.message || '提交失败')
    }
  } finally {
    uploading.value = false
  }
}

async function startUploadExisting() {
  if (!existingSeriesId.value || !rows2.value.length) return
  const list = [...rows2.value]
  const seriesId = existingSeriesId.value
  uploading.value = true
  try {
    const { ok, fail } = await uploadToSeries(seriesId, list, (t) => { tip2.value = t })
    ElMessage.success(`上传完成：成功 ${ok}，失败 ${fail}，已提交审核，通过后才显示`)
    rows2.value = []
    loadMySeries()
    loadJobs()
  } catch (err: any) {
    if (!err?.msg) {
      ElMessage.error(err?.message || '上传失败')
    }
  } finally {
    uploading.value = false
  }
}

async function loadMySeries() {
  const res: any = await http.get('/user/series/mine', { params: { page: seriesPage.value, size: 10 } })
  mySeries.value = res.data?.list || []
  seriesTotal.value = res.data?.total || 0
}

async function loadJobs() {
  const res: any = await http.get('/upload/job', { params: { page: jobPage.value, size: 10 } })
  jobs.value = res.data?.list || []
  jobTotal.value = res.data?.total || 0
}

async function showDetail(id: number) {
  const res: any = await http.get(`/upload/job/${id}`)
  items.value = res.data?.items || []
  drawer.value = true
}

onMounted(() => {
  loadMySeries()
  loadJobs()
})
</script>

<style scoped lang="scss">
.page {
  max-width: 880px;
  margin: 0 auto;
}
.head {
  margin-bottom: 1.2rem;
  h1 {
    margin: 0 0 0.4rem;
    font-size: 1.5rem;
  }
  p {
    margin: 0;
    color: var(--muted);
    font-size: 0.92rem;
    line-height: 1.6;
  }
}
.panel {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 1.2rem 1.3rem;
  margin-bottom: 1.2rem;
  display: grid;
  gap: 1rem;
  h2 {
    margin: 0;
    font-size: 1.1rem;
  }
}
.row {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  flex-wrap: wrap;
}
.label {
  width: 3rem;
  color: var(--muted);
  font-size: 0.9rem;
}
.muted {
  color: var(--muted);
  font-size: 0.88rem;
}
.cover-preview {
  width: 64px;
  height: 88px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}
.tip {
  margin: 0;
  color: var(--muted);
  font-size: 0.9rem;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
