export type VideoThumbResult = {
  file: File
  duration: number
  width: number
  height: number
}

function loadVideo(src: string, crossOrigin = false): Promise<HTMLVideoElement> {
  return new Promise((resolve, reject) => {
    const video = document.createElement('video')
    video.preload = 'auto'
    video.muted = true
    video.playsInline = true
    if (crossOrigin) {
      video.crossOrigin = 'anonymous'
    }
    const cleanup = () => {
      video.onloadedmetadata = null
      video.onseeked = null
      video.onerror = null
    }
    video.onerror = () => {
      cleanup()
      reject(new Error('视频加载失败，无法截取封面'))
    }
    video.onloadedmetadata = () => {
      const duration = Number.isFinite(video.duration) ? video.duration : 0
      const seekTo = duration > 1.2 ? 1 : Math.max(0.05, duration * 0.1)
      const onSeeked = () => {
        cleanup()
        resolve(video)
      }
      video.onseeked = onSeeked
      try {
        video.currentTime = seekTo
      } catch {
        cleanup()
        resolve(video)
      }
    }
    video.src = src
  })
}

function drawThumb(video: HTMLVideoElement, name: string): Promise<VideoThumbResult> {
  const width = video.videoWidth || 720
  const height = video.videoHeight || 1280
  const maxSide = 720
  const scale = Math.min(1, maxSide / Math.max(width, height))
  const w = Math.max(1, Math.round(width * scale))
  const h = Math.max(1, Math.round(height * scale))
  const canvas = document.createElement('canvas')
  canvas.width = w
  canvas.height = h
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    return Promise.reject(new Error('无法创建画布'))
  }
  ctx.drawImage(video, 0, 0, w, h)
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (!blob) {
          reject(new Error('封面生成失败'))
          return
        }
        const base = name.replace(/\.[^.]+$/, '') || 'cover'
        resolve({
          file: new File([blob], `${base}.jpg`, { type: 'image/jpeg' }),
          duration: Math.max(0, Math.round(video.duration || 0)),
          width,
          height,
        })
      },
      'image/jpeg',
      0.86,
    )
  })
}

export async function captureThumbFromFile(file: File): Promise<VideoThumbResult> {
  const url = URL.createObjectURL(file)
  try {
    const video = await loadVideo(url, false)
    return await drawThumb(video, file.name)
  } finally {
    URL.revokeObjectURL(url)
  }
}

export async function captureThumbFromUrl(url: string, name = 'cover.mp4'): Promise<VideoThumbResult> {
  const video = await loadVideo(url, true)
  return drawThumb(video, name)
}
