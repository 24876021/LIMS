<template>
  <div class="page-bg">
    <div class="login-card">
      <!-- 标题区域 -->
      <div class="form-header">
        <h3 class="form-title">实验室管理系统</h3>
        <p class="form-subtitle">欢迎回来，请登录您的账户</p>
      </div>

      <!-- 表单区域 -->
      <el-form
          ref="loginFormRef"
          :rules="rules"
          :model="loginData"
          class="login-form"
          label-width="0"
      >
        <el-form-item prop="account">
          <el-input
              v-model="loginData.account"
              placeholder="请输入账户名"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              v-password-tooltip
              @keyup.enter="submitLogin"
          />
        </el-form-item>

        <el-form-item prop="code" class="captcha-form-item">
          <div class="captcha-row">
            <el-input
                v-model="loginData.code"
                placeholder="验证码"
                size="large"
                style="flex:1"
                @keyup.enter="submitLogin"
            />
            <img
                v-if="captchaImg"
                :src="captchaImg"
                alt="验证码"
                class="captcha-img"
                title="点击刷新"
                @click="getCaptcha"
            />
            <el-button v-else link type="primary" @click="getCaptcha">获取验证码</el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="checked" label="记住我" size="small"/>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              style="width:100%"
              :loading="loading"
              @click="submitLogin"
          >登 录</el-button>
        </el-form-item>

        <el-form-item>
          <div class="form-footer">
            <span>还没有账户？</span>
            <el-button link type="primary" @click="goToRegister">立即注册</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import JSEncrypt from 'jsencrypt'
import { loginApi, userApi } from '@/services/api'
import { setUserPermissions } from '@/utils/permission'
import labBg from '@/assets/生物实验室.png'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const checked = ref(false)
const captchaImg = ref('')

const publicKey = 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJXjjN54zuYgR9Xl/VxQu63X9PgrCENf8C9j7WcyB/+f8cy3zQmIW3h0/auw1oKrcxeNz8rctaFsBiNI7BZlTuMCAwEAAQ=='

const loginData = reactive({
  account: '',
  password: '',
  code: '',
  userKey: ''
})

const rules = {
  account: [{ required: true, message: '请输入账户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

onMounted(() => {
  const token = sessionStorage.getItem('jwtToken') || localStorage.getItem('jwtToken')
  const userId = sessionStorage.getItem('userId') || localStorage.getItem('userId')
  if (token && userId) {
    router.replace('/')
  }
  getCaptcha()
})

// 获取验证码
const getCaptcha = async () => {
  try {
    const data = await loginApi.getCaptcha()
    captchaImg.value = data.captcherImg
    loginData.userKey = data.userKey
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  }
}

// 提交登录
const submitLogin = async () => {
  if (!loginFormRef.value) return

  loginFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请填写所有信息')
      return
    }

    loading.value = true
    try {
      // RSA加密密码
      const encrypt = new JSEncrypt()
      encrypt.setPublicKey(publicKey)
      const encryptedPassword = encrypt.encrypt(loginData.password)

      const loginRequest = {
        account: loginData.account,
        password: encryptedPassword,
        code: loginData.code,
        userKey: loginData.userKey
      }

      const resp = await loginApi.login(loginRequest)

      const token = resp.data.jwt
      const userId = resp.data.userId
      const rememberMe = checked.value

      // 根据记住我选择存储位置
      const storage = rememberMe ? localStorage : sessionStorage
      storage.setItem('jwtToken', token)
      storage.setItem('userId', userId)
      storage.setItem('token', token)
      localStorage.setItem('rememberMe', rememberMe ? 'true' : 'false')

      // 获取用户角色权限（携带JWT token）
      try {
        const data = await userApi.getRoleAndAuthority(userId)
        if (data) {
          storage.setItem('userAccount', JSON.stringify(data.account))
          storage.setItem('userRoles', JSON.stringify(data.role))
          storage.setItem('userAuthorities', JSON.stringify(data.authority))

          // 兼容现有权限系统：直接使用后端返回的原始权限标识
          const authorities = data.authority || []
          setUserPermissions(authorities)

          // 获取用户详细信息（包含真实姓名）
          let userName = data.account
          let userDetail = null
          try {
            userDetail = await userApi.getInfo(userId)
            if (userDetail) {
              userName = userDetail.name || userDetail.nickname || userDetail.realName || data.account
            }
          } catch (e) {
            console.error('获取用户详情失败', e)
          }

          // 存储用户信息（兼容现有系统）
          const modulePerms = extractModules(authorities)
          const userInfo = {
            id: userId,
            username: data.account,
            name: userName,
            avatar: userDetail?.avatar || '',
            sex: userDetail?.sex || '',
            departmentId: userDetail?.departmentId || null,
            role: data.role,
            permissions: modulePerms
          }
          localStorage.setItem('labUser', JSON.stringify(userInfo))

          ElMessage.success('欢迎 ' + data.account + ' 登录!')
          router.replace('/')
        } else {
          ElMessage.error('获取用户权限失败')
        }
      } catch (permError) {
        console.error('获取用户权限失败:', permError)
        router.replace('/')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('网络异常，请重试')
      getCaptcha()
    } finally {
      loading.value = false
    }
  })
}

// 从权限列表中提取模块名
const extractModules = (authorities) => {
  const modules = new Set()
  authorities.forEach(auth => {
    if (auth && auth.startsWith('resource:')) {
      modules.add('equipment')
      modules.add('resource')
      modules.add('reservation')
    }
    if (auth && auth.includes('equipment')) modules.add('equipment')
    if (auth && auth.includes('personnel')) modules.add('personnel')
    if (auth && auth.includes('safety')) modules.add('safety')
    if (auth && auth.includes('reservation')) modules.add('reservation')
    if (auth && auth.includes('resource')) modules.add('resource')
    if (auth && auth.includes('report')) modules.add('report')
  })
  if (modules.size === 0) {
    return ['equipment', 'personnel', 'resource', 'safety', 'reservation', 'report']
  }
  return [...modules]
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<script>
export default {
  name: 'Login'
}
</script>

<style scoped>
.page-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-image: url('@/assets/生物实验室.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
}

.page-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px 36px 32px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-input--large .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.login-form :deep(.el-input--large .el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.captcha-form-item {
  margin-bottom: 16px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.captcha-img {
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  flex-shrink: 0;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.form-footer {
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.form-footer .el-button {
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 28px 20px 24px;
    margin: 16px;
  }

  .form-title {
    font-size: 20px;
  }
}
</style>
