<template>
  <div class="post-page">
    <div class="container">
      <div class="post-container card">
        <h2>Create New Post</h2>

        <el-form :model="form" label-width="80px">
          <el-form-item label="Content">
            <el-input
              v-model="form.textContent"
              type="textarea"
              :rows="5"
              placeholder="What's on your mind?"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="Media">
            <el-upload
              v-model:file-list="fileList"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="9"
              accept="image/*,video/*"
              list-type="picture-card"
              multiple
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item label="Location">
            <el-input v-model="form.location" placeholder="Add location (optional)" />
          </el-form-item>

          <el-form-item label="Tags">
            <el-select
              v-model="form.tags"
              multiple
              filterable
              allow-create
              placeholder="Add tags"
              style="width: 100%"
            >
              <el-option
                v-for="tag in popularTags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit">
              Publish
            </el-button>
            <el-button @click="handleCancel">Cancel</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useContentStore } from '@/stores/content'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const contentStore = useContentStore()

const loading = ref(false)
const fileList = ref([])
const popularTags = ref([])

const form = reactive({
  textContent: '',
  location: '',
  tags: []
})

function handleFileChange(file) {
  form.media = fileList.value.map(f => f.raw).filter(Boolean)
}

async function fetchPopularTags() {
  try {
    const response = await fetch('/api/tags?limit=20')
    const data = await response.json()
    popularTags.value = data.data
  } catch (error) {
    console.error('Failed to fetch tags:', error)
  }
}

async function handleSubmit() {
  if (!form.textContent && !form.media) {
    ElMessage.warning('Please add content or media')
    return
  }

  loading.value = true
  try {
    await contentStore.createContent(form)
    ElMessage.success('Published successfully!')
    router.push({ name: 'Home' })
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to publish')
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.back()
}

onMounted(() => {
  fetchPopularTags()
})
</script>

<style scoped>
.post-page {
  min-height: 100vh;
  padding: 20px 0;
}

.post-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
}

.post-container h2 {
  margin: 0 0 24px;
  font-size: 24px;
}
</style>
