import request from './request'

/**
 * 用户相关API接口
 * 对接后端sys_user表相关业务
 */
export const userApi = {
  /**
   * 用户登录
   * @param {Object} data - { username, password }
   */
  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   * @param {Object} data - { username, password, email, nickname }
   */
  register(data) {
    return request({
      url: '/user/register',
      method: 'post',
      data
    })
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    })
  },

  /**
   * 更新用户信息
   * @param {Object} data - { nickname, email, avatar, phone }
   */
  updateUserInfo(data) {
    return request({
      url: '/user/info',
      method: 'put',
      data
    })
  },

  /**
   * 修改密码
   * @param {Object} data - { oldPassword, newPassword }
   */
  changePassword(data) {
    return request({
      url: '/user/password',
      method: 'put',
      data
    })
  },

  /**
   * 退出登录
   */
  logout() {
    return request({
      url: '/user/logout',
      method: 'post'
    })
  }
}
