<template>
  <div class="auth-page">
    <div class="container">
      <div class="auth-card card">
        <h1 class="auth-title">Welcome Back</h1>
        <p class="auth-subtitle">Login to your account</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="Username" prop="username">
            <el-input v-model="form.username" placeholder="Enter your username" />
          </el-form-item>

          <el-form-item label="Password" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="Enter your password"
              show-password
              @keydown.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              style="width: 100%"
              @click="handleLogin"
            >
              Login
            </el-button>
          </el-form-item>

          <div class="auth-footer">
            Don't have an account?
            <router-link to="/register">Sign up</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true

    await authStore.login(form.username, form.password)

    ElMessage.success('Login successful')

    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    if (error.errors) {
      // Validation error
      return
    }
    ElMessage.error(error.response?.data?.message || 'Login failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
}

.auth-title {
  font-size: 28px;
  font-weight: bold;
  text-align: center;
  margin: 0 0 8px;
}

.auth-subtitle {
  text-align: center;
  color: #666;
  margin: 0 0 32px;
}

.auth-footer {
  text-align: center;
  color: #666;
}

.auth-footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
