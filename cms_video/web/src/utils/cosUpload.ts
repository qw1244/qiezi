import http from '@/utils/http'

export async function putToCos(putUrl: string, file: File, contentType = 'application/octet-stream') {
  const res = await fetch(putUrl, {
    method: 'PUT',
    headers: {
      'Content-Type': contentType,
    },
    body: file,
  })
  if (!res.ok) {
    throw new Error(`R2 上传失败 HTTP ${res.status}`)
  }
}

export async function uploadToCos(file: File, opts?: { folder?: string; seriesId?: number | string }) {
  const contentType = file.type || 'application/octet-stream'
  const res: any = await http.post('/admin/cos/cred', {
    fileName: file.name,
    contentType,
    folder: opts?.folder || 'cover',
    seriesId: opts?.seriesId,
  })
  const cred = res.data
  await putToCos(cred.putUrl, file, cred.contentType || contentType)
  return {
    key: cred.key as string,
    url: (cred.url as string) || '',
  }
}

export async function uploadUserAvatar(file: File) {
  const contentType = file.type || 'image/jpeg'
  const res: any = await http.post('/user/avatar/cred', {
    fileName: file.name,
    contentType,
  })
  const cred = res.data
  await putToCos(cred.putUrl, file, cred.contentType || contentType)
  return {
    key: cred.key as string,
    url: (cred.url as string) || '',
  }
}
