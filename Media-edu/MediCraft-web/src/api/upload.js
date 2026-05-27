import request from './request'

/**
 * 文件上传相关API接口
 */
export const uploadApi = {
  /**
   * 上传图片
   * @param {File} file - 图片文件
   */
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'image')

    return request({
      url: '/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传文档
   * @param {File} file - 文档文件
   */
  uploadDocument(file) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'document')

    return request({
      url: '/upload/document',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 通用文件上传
   * @param {File} file - 文件
   * @param {String} type - 文件类型
   */
  uploadFile(file, type = 'general') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    return request({
      url: '/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
