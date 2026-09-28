<template>
  <div class="page-bg">
    <div class="register-card">
      <!-- 标题区域 -->
      <div class="form-header">
        <h3 class="form-title">创建账户</h3>
        <p class="form-subtitle">注册实验室管理系统账户</p>
      </div>

      <!-- 表单区域 -->
      <el-form
          ref="registerFormRef"
          :rules="rules"
          :model="registerData"
          class="register-form"
          label-width="0"
      >
        <el-form-item prop="name">
          <el-input
              v-model="registerData.name"
              placeholder="请输入昵称"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="account">
          <el-input
              v-model="registerData.account"
              placeholder="请输入10位数字账户名"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="registerData.password"
              type="password"
              placeholder="请输入密码（8-12位，含字母和数字）"
              size="large"
              show-password
              v-password-tooltip
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="registerData.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              v-password-tooltip
              @keyup.enter="submitRegister"
          />
        </el-form-item>

        <el-form-item>
          <div class="password-tips">
            <span :class="{ 'tip-active': hasLetter, 'tip-done': passwordStrong.letter }">✓ 包含字母</span>
            <span :class="{ 'tip-active': hasNumber, 'tip-done': passwordStrong.number }">✓ 包含数字</span>
            <span :class="{ 'tip-active': hasLength, 'tip-done': passwordStrong.length }">✓ 8-12位</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              style="width:100%"
              :loading="loading"
              @click="submitRegister"
          >注 册</el-button>
        </el-form-item>

        <el-form-item>
          <div class="form-footer">
            <span>已有账户？</span>
            <el-button link type="primary" @click="goToLogin">返回登录</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import JSEncrypt from 'jsencrypt'
import { userApi } from '@/services/api'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const publicKey = 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJXjjN54zuYgR9Xl/VxQu63X9PgrCENf8C9j7WcyB/+f8cy3zQmIW3h0/auw1oKrcxeNz8rctaFsBiNI7BZlTuMCAwEAAQ=='

const registerData = reactive({
  account: '',
  name: '',
  password: '',
  confirmPassword: ''
})

// 密码强度实时校验（仅用于提示显示，不替代表单校验）
const hasLetter = computed(() => /[a-zA-Z]/.test(registerData.password))
const hasNumber = computed(() => /\d/.test(registerData.password))
const hasLength = computed(() => registerData.password.length >= 8 && registerData.password.length <= 12)
const passwordStrong = computed(() => ({
  letter: hasLetter.value,
  number: hasNumber.value,
  length: hasLength.value
}))

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  account: [
    { required: true, message: '请输入账户', trigger: 'blur' },
    { pattern: /^\d{10}$/, message: '账户名必须是10位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 12, message: '密码长度必须在8到12位之间', trigger: 'blur' },
    { pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z]*$/, message: '密码必须包含至少一个数字和一个字母', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const submitRegister = async () => {
  if (!registerFormRef.value) return

  registerFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请填入所有信息')
      return
    }

    loading.value = true
    try {
      // RSA加密密码
      const encrypt = new JSEncrypt()
      encrypt.setPublicKey(publicKey)
      const encryptedPassword = encrypt.encrypt(registerData.password)

      const registerRequest = {
        account: registerData.account,
        name: registerData.name,
        password: encryptedPassword
      }

      const resp = await userApi.register(registerRequest)
      ElMessage.success(resp.msg || '注册成功！即将跳转登录...')
      setTimeout(() => router.push('/login'), 1500)
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error(error.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<script>
export default {
  name: 'Register'
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

.register-card {
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
  margin-bottom: 28px;
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

.register-form {
  margin-top: 8px;
}

.register-form :deep(.el-input--large .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.register-form :deep(.el-input--large .el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.password-tips {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #c0c4cc;
}

.password-tips span {
  transition: color 0.2s;
}

.tip-active {
  color: #e6a23c;
}

.tip-done {
  color: #67c23a;
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
  .register-card {
    width: 90%;
    padding: 28px 20px 24px;
    margin: 16px;
  }

  .form-title {
    font-size: 20px;
  }

  .password-tips {
    gap: 8px;
  }
}
</style>
