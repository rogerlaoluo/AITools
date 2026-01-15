<template>
  <div class="comment-list">
    <div v-loading="loading" class="comments">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :src="comment.userAvatar" :size="32">
          {{ comment.username?.charAt(0) }}
        </el-avatar>
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-username">{{ comment.username }}</span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-text">{{ comment.content }}</div>
        </div>
      </div>
      <el-empty v-if="!loading && comments.length === 0" description="No comments yet" />
    </div>

    <div class="comment-form">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="Write a comment..."
        @keydown.ctrl.enter="submitComment"
      />
      <div class="comment-form-actions">
        <el-button type="primary" :loading="submitting" @click="submitComment">
          Send (Ctrl+Enter)
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useContentStore } from '@/stores/content'

const props = defineProps({
  contentId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['comment'])

const contentStore = useContentStore()
const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const submitting = ref(false)

async function fetchComments() {
  loading.value = true
  try {
    comments.value = await contentStore.getComments(props.contentId)
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!newComment.value.trim() || submitting.value) return

  submitting.value = true
  try {
    await contentStore.addComment(props.contentId, newComment.value)
    emit('comment', newComment.value)
    newComment.value = ''
    await fetchComments()
  } finally {
    submitting.value = false
  }
}

function formatTime(time) {
  const date = new Date(time)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000)

  if (diff < 60) return 'just now'
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  max-height: 500px;
}

.comments {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-username {
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  color: #666;
  line-height: 1.5;
}

.comment-form {
  border-top: 1px solid #eee;
  padding: 16px;
}

.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
