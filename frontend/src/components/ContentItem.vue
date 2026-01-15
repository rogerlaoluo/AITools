<template>
  <div class="content-item card">
    <div class="card-body">
      <div class="content-header">
        <router-link :to="`/user/${content.userId}`" class="content-avatar-link">
          <el-avatar :src="content.userAvatar" :size="40">
            {{ content.username?.charAt(0) }}
          </el-avatar>
        </router-link>
        <div class="content-info">
          <router-link :to="`/user/${content.userId}`" class="content-username">
            {{ content.username }}
          </router-link>
          <div class="content-time">{{ formatTime(content.createdAt) }}</div>
        </div>
      </div>

      <div v-if="content.textContent" class="content-text">
        {{ content.textContent }}
      </div>

      <div v-if="content.mediaPaths && content.mediaPaths.length > 0" :class="mediaClass">
        <div v-for="(media, index) in content.mediaPaths" :key="index" class="media-item">
          <img :src="media" :alt="`Content ${index + 1}`" @click="previewImage(index)" />
        </div>
      </div>

      <div v-if="content.location" class="content-location">
        <el-icon><Location /></el-icon>
        {{ content.location }}
      </div>

      <div v-if="content.tags && content.tags.length > 0" class="content-tags">
        <router-link
          v-for="tag in content.tags"
          :key="tag"
          :to="`/search?q=%23${tag}`"
          class="tag-link"
        >
          #{{ tag }}
        </router-link>
      </div>

      <div class="content-actions">
        <button
          class="action-btn"
          :class="{ liked: content.likedByCurrentUser }"
          @click="handleLike"
        >
          <el-icon><component :is="content.likedByCurrentUser ? 'StarFilled' : 'Star'" /></el-icon>
          <span>{{ content.likeCount || 0 }}</span>
        </button>
        <button class="action-btn" @click="showCommentDialog = true">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ content.commentCount || 0 }}</span>
        </button>
      </div>
    </div>

    <el-dialog v-model="showCommentDialog" title="Comments" width="500px">
      <CommentList :content-id="content.id" @comment="$emit('comment', { contentId: content.id, text: $event })" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Location, Star, StarFilled, ChatDotRound } from '@element-plus/icons-vue'
import { ElImageViewer } from 'element-plus'
import CommentList from './CommentList.vue'

const props = defineProps({
  content: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['like', 'comment'])

const showCommentDialog = ref(false)

const mediaClass = computed(() => {
  const count = props.content.mediaPaths?.length || 0
  if (count === 1) return 'content-media grid-1'
  if (count === 2) return 'content-media grid-2'
  return 'content-media grid-3'
})

function formatTime(time) {
  const date = new Date(time)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000)

  if (diff < 60) return 'just now'
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`

  return date.toLocaleDateString()
}

function handleLike() {
  emit('like', props.content.id)
}

function previewImage(index) {
  ElImageViewer(props.content.mediaPaths, index)
}
</script>

<style scoped>
.content-item {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.content-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.content-avatar-link {
  text-decoration: none;
}

.content-info {
  flex: 1;
  margin-left: 12px;
}

.content-username {
  font-weight: 600;
  color: #333;
  text-decoration: none;
}

.content-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.content-text {
  margin-bottom: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.content-media {
  display: grid;
  gap: 4px;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.content-media.grid-1 {
  grid-template-columns: 1fr;
}

.content-media.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.content-media.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.media-item {
  aspect-ratio: 1;
  overflow: hidden;
  background: #f0f0f0;
  cursor: pointer;
}

.media-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.media-item img:hover {
  transform: scale(1.05);
}

.content-location {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.content-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.content-actions {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #666;
  background: none;
  border: none;
  font-size: 14px;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #409eff;
}

.action-btn.liked {
  color: #f56c6c;
}
</style>
