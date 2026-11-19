<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div></div>
      <div class="actions">
        <el-input v-model="keyword" placeholder="搜索标题" style="width:220px" clearable @change="load" />
        <el-button type="primary" @click="openEdit()">新增合集</el-button>
      </div>
    </div>
    <div class="admin-panel">
      <el-table :data="list">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="88">
          <template #default="{ row }">
            <div class="thumb" :style="thumbStyle(row.cover)"></div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="epCount" label="集数" width="80" />
        <el-table-column prop="playCount" label="播放" width="90" />
        <el-table-column prop="favCount" label="收藏" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/admin/series/${row.id}`)">查看详情</el-button>
            <el-button link @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="prev, pager, next" :total="total" v-model:current-page="page" @current-change="load" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑合集' : '新增合集'" width="560px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="封面">
          <CosImageUpload v-model="form.cover" folder="cover" />
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择分类" style="width:180px">
            <el-option label="真人" value="真人" />
            <el-option label="AI" value="AI" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import http from '@/utils/http'
import CosImageUpload from '@/components/admin/CosImageUpload.vue'

const list = ref<any[]>([])
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const visible = ref(false)
const form = reactive<any>({ id: null, title: '', cover: '', intro: '', category: '真人', status: 1 })

function thumbStyle(cover: string) {
  return cover
    ? { backgroundImage: `url(${cover})` }
    : { backgroundImage: 'linear-gradient(145deg, #6e3b72, #3f7a5c)' }
}

async function load() {
  const res: any = await http.get('/admin/series', { params: { keyword: keyword.value, page: page.value, size: 10 } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

function openEdit(row?: any) {
  Object.assign(form, { id: null, title: '', cover: '', intro: '', category: '真人', status: 1 }, row || {})
  visible.value = true
}

async function save() {
  await http.post('/admin/series', form)
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm('删除合集将同步尝试删除云端视频，确认？', '提示')
  await http.delete(`/admin/series/${row.id}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.actions { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.pager { margin-top: 16px; justify-content: flex-end; }
.thumb {
  width: 42px;
  height: 56px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
}
</style>
