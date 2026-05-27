/**
 * 表单校验工具函数
 * Element Plus表单验证规则
 */

/**
 * 用户名验证规则
 */
export const usernameRules = [
  { required: true, message: '请输入用户名', trigger: 'blur' },
  { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
  { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
]

/**
 * 密码验证规则
 */
export const passwordRules = [
  { required: true, message: '请输入密码', trigger: 'blur' },
  { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
]

/**
 * 确认密码验证规则
 * @param {Function} getPassword - 获取密码值的函数
 */
export function createConfirmPasswordRules(getPassword) {
  return [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== getPassword()) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 邮箱验证规则
 */
export const emailRules = [
  { required: true, message: '请输入邮箱', trigger: 'blur' },
  { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
]

/**
 * 手机号验证规则
 */
export const phoneRules = [
  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
]

/**
 * 必填验证规则
 * @param {String} fieldName - 字段名称
 */
export function requiredRule(fieldName) {
  return { required: true, message: `请输入${fieldName}`, trigger: 'blur' }
}

/**
 * 长度验证规则
 * @param {Number} min - 最小长度
 * @param {Number} max - 最大长度
 * @param {String} fieldName - 字段名称
 */
export function lengthRule(min, max, fieldName) {
  return {
    min,
    max,
    message: `${fieldName}长度为${min}-${max}个字符`,
    trigger: 'blur'
  }
}
