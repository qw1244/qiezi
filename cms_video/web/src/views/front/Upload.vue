<template>
  <section class="page">
    <header class="head">
      <h1>投稿</h1>
      <p>选一个合集，把视频传上来。上传后需管理员审核，通过后才会在合集里显示。</p>
    </header>

    <div class="panel">
      <div class="row">
        <span class="label">合集</span>
        <el-select v-model="seriesId" placeholder="选择要投稿的合集" style="width: 320px" :disabled="uploading">
          <el-option
            v-for="s in seriesList"
            :key="s.id"
            :label="s.title"
            :value="s.id"
          />
        </el-select>
      </div>

      <div class="row">
        <span class="label">视频</span>
        <input ref="fileRef" type="file" multiple accept="video/*" style="display: none" @change="onPickFiles" />
        <el-button :disabled="uploading" @click="fileRef?.click()">选择视频文件</el-button>
        <span class="muted">已选 {{ rows.length }} 个</span>
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
        <el-button type="primary" :loading="uploading" :disabled="!rows.length || !seriesId" @click="startUpload">
          开始上传
        </el-button>
      </div>
    </div>

    <div class="panel">
      <h2>我的投稿</h2>
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
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'
import { putToCos } from '@/utils/cosUpload'
import { captureThumbFromFile } from '@/utils/videoThumb'

const seriesList = ref<any[]>([])
const seriesId = ref<number | null>(null)
const rows = ref<{ file: File; title: string }[]>([])
const fileRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const tip = ref('')

const jobs = ref<any[]>([])
const jobPage = ref(1)
const jobTotal = ref(0)
const drawer = ref(false)
const items = ref<any[]>([])

async function loadSeries() {
  const res: any = await http.get('/series/list', { params: { page: 1, size: 100 } })
  seriesList.value = res.data?.list || []
}

function onPickFiles(e: Event) {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  for (const file of files) {
    rows.value.push({ file, title: '' })
  }
}

async function startUpload() {
  if (!seriesId.value || !rows.value.length) return
  const list = [...rows.value]
  uploading.value = true
  tip.value = `正在创建上传任务（${list.length} 个文件）...`
  try {
    const createRes: any = await http.post('/upload/job', {
      seriesId: seriesId.value,
      files: list.map((r) => ({
        fileName: r.file.name,
        title: (r.title || '').trim(),
      })),
    })
    const job = createRes.data
    const itemsRes = job.items || []
    let ok = 0
    let fail = 0
    tip.value = `任务 #${job.id} 上传中，请勿关闭页面...`
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
      tip.value = `任务 #${job.id}：成功 ${ok} / 失败 ${fail} / 合计 ${itemsRes.length}`
    }))
    ElMessage.success(`已提交审核：成功 ${ok}，失败 ${fail}`)
    rows.value = []
    loadJobs()
  } catch (err: any) {
    if (!err?.msg) {
      ElMessage.error(err?.message || '创建上传任务失败')
    }
  } finally {
    uploading.value = false
  }
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
  loadSeries()
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
