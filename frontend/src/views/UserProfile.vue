<template>
  <div class="profile-page">
    <div class="container">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <template v-else>
        <div class="profile-header card">
          <div class="profile-avatar">
            <el-avatar :src="user.avatarUrl" :size="100">
              {{ user.username?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="profile-info">
            <h1>{{ user.username }}</h1>
            <p class="bio">{{ user.bio || 'No bio yet' }}</p>
            <div class="stats">
              <div class="stat-item">
                <strong>{{ user.followerCount || 0 }}</strong>
                <span>Followers</span>
              </div>
              <div class="stat-item">
                <strong>{{ user.followingCount || 0 }}</strong>
                <span>Following</span>
              </div>
            </div>
            <el-button
              v-if="authStore.isAuthenticated && authStore.user.id !== user.id"
              :type="user.isFollowing ? 'default' : 'primary'"
              @click="handleFollow"
            >
              {{ user.isFollowing ? 'Unfollow' : 'Follow' }}
            </el-button>
          </div>
        </div>

        <h2 class="section-title">Timeline</h2>
        <div v-loading="timelineLoading" class="timeline">
          <ContentItem
            v-for="item in timeline"
            :key="item.id"
            :content="item"
            @like="handleLike"
          />
          <el-empty v-if="!timelineLoading && timeline.length === 0" description="No posts yet" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useContentStore } from '@/stores/content'
import api from '@/api'
import ContentItem from '@/components/ContentItem.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()
const contentStore = useContentStore()

const loading = ref(true)
const timelineLoading = ref(false)
const user = ref({})
const timeline = ref([])

async function fetchUserProfile() {
  loading.value = true
  try {
    const response = await api.get(`/api/users/${route.params.id}`)
    user.value = response.data.data
  } catch (error) {
    ElMessage.error('Failed to load profile')
  } finally {
    loading.value = false
  }
}

async function fetchTimeline() {
  timelineLoading.value = true
  try {
    const response = await api.get(`/api/users/${route.params.id}/timeline`)
    timeline.value = response.data.data.content
  } catch (error) {
    ElMessage.error('Failed to load timeline')
  } finally {
    timelineLoading.value = false
  }
}

async function handleFollow() {
  try {
    await api.post(`/api/users/${user.value.id}/follow`)
    user.value.isFollowing = !user.value.isFollowing
    user.value.followerCount += user.value.isFollowing ? 1 : -1
    ElMessage.success(user.value.isFollowing ? 'Followed' : 'Unfollowed')
  } catch (error) {
    ElMessage.error('Action failed')
  }
}

async function handleLike(contentId) {
  await contentStore.likeContent(contentId)
  const item = timeline.value.find(t => t.id === contentId)
  if (item) {
    item.likedByCurrentUser = !item.likedByCurrentUser
    item.likeCount += item.likedByCurrentUser ? 1 : -1
  }
}

onMounted(() => {
  fetchUserProfile()
  fetchTimeline()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 20px 0;
}

.loading {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  gap: 24px;
  padding: 24px;
  margin-bottom: 24px;
}

.profile-avatar {
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
}

.profile-info h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.bio {
  color: #666;
  margin: 0 0 16px;
}

.stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  gap: 4px;
}

.stat-item strong {
  font-size: 18px;
}

.stat-item span {
  color: #666;
}

.section-title {
  font-size: 20px;
  margin-bottom: 16px;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .stats {
    justify-content: center;
  }
}
</style>
