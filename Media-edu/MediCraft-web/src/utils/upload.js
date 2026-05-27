/**
 * 文件上传工具函数
 */

/**
 * 检查文件类型
 * @param {File} file - 文件对象
 * @param {Array} acceptTypes - 允许的MIME类型
 */
export function checkFileType(file, acceptTypes) {
  return acceptTypes.some((type) => {
    if (type.endsWith('/*')) {
      const prefix = type.slice(0, -2)
      return file.type.startsWith(prefix)
    }
    return file.type === type
  })
}

/**
 * 检查文件大小
 * @param {File} file - 文件对象
 * @param {Number} maxSize - 最大文件大小（MB）
 */
export function checkFileSize(file, maxSize) {
  return file.size / 1024 / 1024 < maxSize
}

/**
 * 图片上传前验证
 * @param {File} file - 图片文件
 * @param {Number} maxSize - 最大大小（MB），默认5MB
 */
export function beforeImageUpload(file, maxSize = 5) {
  const isImage = checkFileType(file, ['image/jpeg', 'image/png', 'image/gif', 'image/webp'])
  const isLt = checkFileSize(file, maxSize)

  if (!isImage) {
    return { valid: false, message: '只能上传 JPG/PNG/GIF/WEBP 格式的图片' }
  }
  if (!isLt) {
    return { valid: false, message: `图片大小不能超过 ${maxSize}MB` }
  }

  return { valid: true }
}

/**
 * 文档上传前验证
 * @param {File} file - 文档文件
 * @param {Number} maxSize - 最大大小（MB），默认20MB
 */
export function beforeDocumentUpload(file, maxSize = 20) {
  const isDoc = checkFileType(file, [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'text/plain',
    'text/markdown'
  ])
  const isLt = checkFileSize(file, maxSize)

  if (!isDoc) {
    return { valid: false, message: '只能上传 PDF/Word/TXT/Markdown 格式的文档' }
  }
  if (!isLt) {
    return { valid: false, message: `文档大小不能超过 ${maxSize}MB` }
  }

  return { valid: true }
}

/**
 * 将File转为Base64
 * @param {File} file - 文件对象
 */
export function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}

/**
 * 创建图片压缩
 * @param {File} file - 图片文件
 * @param {Number} maxWidth - 最大宽度
 * @param {Number} quality - 压缩质量 0-1
 */
export function compressImage(file, maxWidth = 1920, quality = 0.8) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }

        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            const compressedFile = new File([blob], file.name, { type: 'image/jpeg' })
            resolve(compressedFile)
          },
          'image/jpeg',
          quality
        )
      }
      img.onerror = reject
    }
    reader.onerror = reject
  })
}
