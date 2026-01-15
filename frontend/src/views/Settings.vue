<template>
  <div class="settings-page">
    <div class="container">
      <h2 class="page-title">Settings</h2>
      <div class="settings-container card">
        <el-form :model="form" label-width="100px">
          <el-form-item label="Avatar">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              accept="image/*"
            >
              <el-avatar :src="form.avatarUrl || authStore.user.avatarUrl" :size="80">
                {{ authStore.user.username?.charAt(0) }}
              </el-avatar>
            </el-upload>
          </el-form-item>

          <el-form-item label="Bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              placeholder="Tell us about yourself"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSave">
              Save Changes
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  bio: '',
  avatarUrl: ''
})

function handleAvatarChange(file) {
  form.avatarFile = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    form.avatarUrl = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

async function handleSave() {
  loading.value = true
  try {
    const formData = new FormData()
    if (form.bio) formData.append('bio', form.bio)
    if (form.avatarFile) formData.append('avatar', form.avatarFile)

    await api.put(`/api/users/${authStore.user.id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    await authStore.getCurrentUser()
    ElMessage.success('Settings saved')
  } catch (error) {
    ElMessage.error('Failed to save settings')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  form.bio = authStore.user.bio || ''
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  padding: 20px 0;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
}

.settings-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
}
</style>
