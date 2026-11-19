<template>
  <div class="admin-page" v-loading="loading">
    <div class="admin-toolbar">
      <div class="left">
        <el-button @click="$router.back()">返回</el-button>
        <div>
          <h2>{{ detail?.title || '合集详情' }}</h2>
          <p class="sub">管理分集内容，支持批量直传 R2；封面从视频自动截取</p>
        </div>
      </div>
      <div class="actions">
        <el-button :loading="renumbering" @click="renumberEps">整理集数</el-button>
        <el-button :loading="fillingCover" :disabled="!needCoverCount" @click="fillMissingCovers">
          补齐封面{{ needCoverCount ? ` (${needCoverCount})` : '' }}
        </el-button>
        <el-button type="primary" :disabled="uploading" @click="openUpload">上传视频</el-button>
      </div>
    </div>

    <el-alert
      v-if="jobTip"
      :title="jobTip"
      type="info"
      show-icon
      :closable="false"
    />

    <div class="admin-panel">
      <el-table :data="detail?.videos || []">
        <el-table-column label="集数" width="140">
          <template #default="{ row }">
            <div class="ep-cell">
              <el-button
                class="ep-btn"
                size="small"
                :disabled="savingId === row.id || row.epNo <= 1"
                @click="moveEp(row, -1)"
              >↑</el-button>
              <span class="ep-no">{{ row.epNo }}</span>
              <el-button
                class="ep-btn"
                size="small"
                :disabled="savingId === row.id"
                @click="moveEp(row, 1)"
              >↓</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="88">
          <template #default="{ row }">
            <div class="thumb" :style="thumbStyle(row.cover)" title="视频截帧封面"></div>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(秒)" width="100" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="playCount" label="播放" width="80" />
        <el-table-column prop="cosKey" label="对象 Key" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link @click="editVideo(row)">编辑</el-button>
            <el-button link type="danger" @click="removeVideo(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="uploadVisible"
      title="上传视频"
      width="720px"
      :close-on-click-modal="!uploading"
      :close-on-press-escape="!uploading"
      @closed="resetUploadForm"
    >
      <p class="hint top-hint">选择视频后可改标题、集数；封面仍会自动从视频截取。</p>
      <div class="upload-toolbar">
        <input ref="fileRef" type="file" multiple accept="video/*" style="display:none" @change="onPickFiles" />
        <el-button :disabled="uploading" @click="fileRef?.click()">选择视频文件</el-button>
        <span class="muted">已选 {{ uploadRows.length }} 个</span>
      </div>
      <el-table v-if="uploadRows.length" :data="uploadRows" max-height="360">
        <el-table-column label="文件" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.file.name }}</template>
        </el-table-column>
        <el-table-column label="标题" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.title" :disabled="uploading" placeholder="如：第1集" />
          </template>
        </el-table-column>
        <el-table-column label="集数" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.epNo" :min="1" :disabled="uploading" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="" width="70" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" :disabled="uploading" @click="uploadRows.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="请先选择视频文件" :image-size="72" />
      <template #footer>
        <el-button :disabled="uploading" @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!uploadRows.length" @click="startUpload">
          开始上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑视频" width="480px">
      <el-form label-width="80px">
        <el-form-item label="封面">
          <div class="cover-preview" :style="thumbStyle(editForm.cover)"></div>
          <p class="hint">上传时自动从视频截取，无需手动设置</p>
        </el-form-item>
        <el-form-item label="集数"><el-input-number v-model="editForm.epNo" :min="1" /></el-form-item>
        <el-form-item label="时长"><el-input-number v-model="editForm.duration" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="saveVideo">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'
import { putToCos, uploadToCos } from '@/utils/cosUpload'
import { captureThumbFromFile, captureThumbFromUrl } from '@/utils/videoThumb'

const route = useRoute()
const loading = ref(false)
const uploading = ref(false)
const fillingCover = ref(false)
const renumbering = ref(false)
const detail = ref<any>(null)
const fileRef = ref<HTMLInputElement | null>(null)
const jobTip = ref('')
const editVisible = ref(false)
const editForm = reactive<any>({})
const savingId = ref<number | null>(null)
const uploadVisible = ref(false)
const uploadRows = ref<{ file: File; title: string; epNo: number }[]>([])

const needCoverCount = computed(() =>
  (detail.value?.videos || []).filter((v: any) => !v.cover && v.playUrl).length,
)

const nextEpNo = computed(() => {
  const eps = (detail.value?.videos || []).map((v: any) => Number(v.epNo) || 0)
  return (eps.length ? Math.max(...eps) : 0) + 1
})

function openUpload() {
  resetUploadForm()
  uploadVisible.value = true
}

function resetUploadForm() {
  if (uploading.value) return
  uploadRows.value = []
  if (fileRef.value) fileRef.value.value = ''
}

function onPickFiles(e: Event) {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  if (!files.length) return
  let ep = nextEpNo.value
  const occupied = new Set(uploadRows.value.map((r) => r.epNo))
  for (const file of files) {
    while (occupied.has(ep)) ep += 1
    occupied.add(ep)
    uploadRows.value.push({
      file,
      title: `第${ep}集`,
      epNo: ep,
    })
    ep += 1
  }
}

function thumbStyle(cover: string) {
  return cover
    ? { backgroundImage: `url(${cover})` }
    : { backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)' }
}

async function load() {
  loading.value = true
  try {
    const res: any = await http.get(`/admin/series/${route.params.id}`)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

async function startUpload() {
  if (!detail.value || !uploadRows.value.length) return
  const rows = [...uploadRows.value]
  const epSet = new Set<number>()
  for (const row of rows) {
    if (!row.epNo || row.epNo < 1) {
      ElMessage.warning('请为每个视频设置有效集数')
      return
    }
    if (epSet.has(row.epNo)) {
      ElMessage.warning(`集数 ${row.epNo} 重复，请调整后再上传`)
      return
    }
    epSet.add(row.epNo)
  }

  uploading.value = true
  jobTip.value = `正在创建上传任务（${rows.length} 个文件）...`
  try {
    const createRes: any = await http.post('/admin/upload/job', {
      seriesId: detail.value.id,
      files: rows.map((r) => ({
        fileName: r.file.name,
        title: (r.title || '').trim() || `第${r.epNo}集`,
        epNo: r.epNo,
        duration: 0,
      })),
    })
    const job = createRes.data
    const items = job.items || []
    let ok = 0
    let fail = 0
    jobTip.value = `任务 #${job.id} 直传中，可离开页面，后台会继续汇总...`

    await Promise.all(items.map(async (item: any, i: number) => {
      const file = rows[i]?.file
      if (!file) {
        fail += 1
        return
      }
      try {
        let cover = ''
        let duration = 0
        try {
          const thumb = await captureThumbFromFile(file)
          duration = thumb.duration
          const uploaded = await uploadToCos(thumb.file, { folder: 'video-cover' })
          cover = uploaded.url
        } catch {
          
        }
        await putToCos(item.putUrl, file)
        await http.post('/admin/upload/confirm', {
          itemId: item.id,
          cosKey: item.cosKey,
          ok: true,
          duration,
          cover,
        })
        ok += 1
      } catch (err: any) {
        await http.post('/admin/upload/confirm', {
          itemId: item.id,
          ok: false,
          errMsg: err?.message || '上传失败',
        })
        fail += 1
      }
      jobTip.value = `任务 #${job.id}：成功 ${ok} / 失败 ${fail} / 合计 ${items.length}`
    }))

    ElMessage.success(`上传完成：成功 ${ok}，失败 ${fail}`)
    uploadVisible.value = false
    uploadRows.value = []
    await load()
  } finally {
    uploading.value = false
  }
}

async function renumberEps() {
  if (!detail.value?.id) return
  renumbering.value = true
  try {
    await http.post(`/admin/video/renumber/${detail.value.id}`)
    ElMessage.success('已整理为连续集数')
    await load()
  } finally {
    renumbering.value = false
  }
}

async function fillMissingCovers() {
  const list = (detail.value?.videos || []).filter((v: any) => !v.cover && v.playUrl)
  if (!list.length) {
    ElMessage.info('没有需要补齐的封面')
    return
  }
  fillingCover.value = true
  let ok = 0
  try {
    for (const row of list) {
      try {
        const thumb = await captureThumbFromUrl(row.playUrl, row.cosKey || `ep-${row.epNo}.mp4`)
        const uploaded = await uploadToCos(thumb.file, { folder: 'video-cover' })
        await http.post('/admin/video', {
          id: row.id,
          seriesId: row.seriesId,
          epNo: row.epNo,
          duration: row.duration || thumb.duration,
          status: row.status,
          cosKey: row.cosKey,
          cover: uploaded.url,
        })
        ok += 1
        jobTip.value = `补齐封面：${ok} / ${list.length}`
      } catch {
        
      }
    }
    ElMessage.success(`已补齐 ${ok} 个封面`)
    await load()
  } finally {
    fillingCover.value = false
  }
}

function editVideo(row: any) {
  Object.assign(editForm, {
    id: row.id,
    seriesId: row.seriesId,
    epNo: row.epNo,
    duration: row.duration,
    status: row.status,
    cosKey: row.cosKey,
    cover: row.cover,
  })
  editVisible.value = true
}

async function saveVideo() {
  await http.post('/admin/video', editForm)
  editVisible.value = false
  load()
}

async function moveEp(row: any, delta: number) {
  const next = (row.epNo || 1) + delta
  if (next < 1) return
  const videos = detail.value?.videos || []
  const other = videos.find((v: any) => v.id !== row.id && v.epNo === next)
  if (!other && delta > 0) {
    
    return
  }
  savingId.value = row.id
  try {
    
    await http.post('/admin/video', {
      id: row.id,
      seriesId: row.seriesId,
      epNo: next,
      duration: row.duration,
      status: row.status,
      cosKey: row.cosKey,
      cover: row.cover,
    })
    await load()
  } finally {
    savingId.value = null
  }
}

async function removeVideo(row: any) {
  await ElMessageBox.confirm('删除后会同步删除云端对象，确认？', '提示')
  await http.delete(`/admin/video/${row.id}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.left { display:flex; align-items:center; gap:12px; }
.actions { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.sub { margin:4px 0 0; color: var(--admin-muted); font-size: 0.86rem; }
.ep-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}
.ep-no {
  min-width: 28px;
  text-align: center;
  font-weight: 700;
}
.ep-btn {
  padding: 4px 8px;
}
.thumb, .cover-preview {
  width: 42px;
  height: 56px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  border: 1px solid rgba(90, 54, 110, 0.12);
}
.cover-preview {
  width: 72px;
  height: 96px;
}
.hint {
  margin: 8px 0 0;
  color: var(--admin-muted);
  font-size: 0.8rem;
}
.top-hint { margin: 0 0 12px; }
.upload-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.muted { color: var(--admin-muted); font-size: 0.86rem; }
</style>
