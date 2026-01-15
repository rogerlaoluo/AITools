<template>
  <div class="home-page">
    <div class="container">
      <div class="content-wrapper">
        <div class="main-content">
          <ContentFeed />
        </div>
        <aside class="sidebar">
          <div v-if="authStore.isAuthenticated" class="sidebar-card">
            <div class="user-card">
              <el-avatar :src="authStore.user.avatarUrl" :size="64">
                {{ authStore.user.username?.charAt(0) }}
              </el-avatar>
              <h3>{{ authStore.user.username }}</h3>
              <p>{{ authStore.user.bio || 'No bio yet' }}</p>
              <router-link :to="`/user/${authStore.user.id}`" class="btn-view-profile">
                View Profile
              </router-link>
            </div>
          </div>
          <div class="sidebar-card">
            <h3>Trending Tags</h3>
            <div v-loading="tagsLoading" class="tags-list">
              <router-link
                v-for="tag in trendingTags"
                :key="tag.id"
                :to="`/search?q=%23${tag.name}`"
                class="tag-item"
              >
                #{{ tag.name }}
                <span>{{ tag.usageCount }}</span>
              </router-link>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import ContentFeed from '@/components/ContentFeed.vue'
import api from '@/api'

const authStore = useAuthStore()
const trendingTags = ref([])
const tagsLoading = ref(false)

async function fetchTrendingTags() {
  tagsLoading.value = true
  try {
    const response = await api.get('/api/tags?limit=10')
    trendingTags.value = response.data.data
  } finally {
    tagsLoading.value = false
  }
}

onMounted(() => {
  fetchTrendingTags()
})
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 100px);
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.main-content {
  min-width: 0;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.sidebar-card h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.user-card {
  text-align: center;
}

.user-card h3 {
  margin: 12px 0 4px;
}

.user-card p {
  color: #666;
  font-size: 14px;
  margin: 0 0 12px;
}

.btn-view-profile {
  display: inline-block;
  padding: 8px 16px;
  background: #409eff;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-size: 14px;
}

.tags-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  text-decoration: none;
  color: #333;
  transition: all 0.2s;
}

.tag-item:hover {
  background: #e8f4ff;
  color: #409eff;
}

@media (max-width: 768px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }
}
</style>
