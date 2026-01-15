<template>
  <div class="auth-page">
    <div class="container">
      <div class="auth-card card">
        <h1 class="auth-title">Create Account</h1>
        <p class="auth-subtitle">Join Timeline today</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          @submit.prevent="handleRegister"
        >
          <el-form-item label="Username" prop="username">
            <el-input v-model="form.username" placeholder="Choose a username" />
          </el-form-item>

          <el-form-item label="Email" prop="email">
            <el-input v-model="form.email" type="email" placeholder="Enter your email" />
          </el-form-item>

          <el-form-item label="Password" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="Choose a password"
              show-password
            />
          </el-form-item>

          <el-form-item label="Confirm" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="Confirm your password"
              show-password
              @keydown.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              style="width: 100%"
              @click="handleRegister"
            >
              Sign Up
            </el-button>
          </el-form-item>

          <div class="auth-footer">
            Already have an account?
            <router-link to="/login">Login</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter email', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
    loading.value = true

    await authStore.register(form.username, form.email, form.password)

    ElMessage.success('Registration successful! Please login.')

    router.push({ name: 'Login' })
  } catch (error) {
    if (error.errors) {
      // Validation error
      return
    }
    ElMessage.error(error.response?.data?.message || 'Registration failed')
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
