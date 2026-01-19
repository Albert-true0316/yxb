<template>
  <div class="login-page">
    <div class="login-container">
      <div class="brand-area">
        <el-icon class="brand-icon"><Trophy /></el-icon>
        <h2>管理后台</h2>
      </div>

      <div class="login-card">
        <h1 class="welcome-text">欢迎回来</h1>
        <p class="subtitle">请输入您的账号密码登录管理后台</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
          size="large"
        >
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit" class="full-btn">
            登录
          </el-button>
        </el-form>

        <div class="footer-links">
          <router-link to="/">← 返回首页</router-link>
          <router-link to="/admin/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.loginAction(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-color);
  background-image: radial-gradient(circle at 100% 0%, #FECACA 0%, transparent 25%), radial-gradient(circle at 0% 100%, #FED7AA 0%, transparent 25%);
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.brand-area {
  text-align: center;
  margin-bottom: 24px;
  color: var(--primary-color);
}
.brand-icon { font-size: 40px; margin-bottom: 8px; }
.brand-area h2 { font-size: 16px; font-weight: 700; text-transform: uppercase; letter-spacing: 2px; }

.login-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  padding: 40px 32px;
  border-radius: 32px;
  box-shadow: 0 20px 60px -10px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.welcome-text { font-size: 24px; font-weight: 800; color: var(--text-primary); text-align: center; margin-bottom: 8px; }
.subtitle { text-align: center; color: var(--text-secondary); margin-bottom: 32px; font-size: 14px; }

.login-form { margin-bottom: 24px; }
.full-btn { width: 100%; height: 48px; font-size: 16px; margin-top: 8px; }

.footer-links {
  display: flex; justify-content: space-between; font-size: 13px;
}
.footer-links a { color: var(--text-secondary); text-decoration: none; transition: color 0.2s; }
.footer-links a:hover { color: var(--primary-color); }
</style>
