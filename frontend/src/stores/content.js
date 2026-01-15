import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export const useContentStore = defineStore('content', () => {
  const timeline = ref([])
  const currentContent = ref(null)
  const loading = ref(false)
  const currentPage = ref(0)
  const totalPages = ref(0)

  async function fetchGlobalTimeline(page = 0) {
    loading.value = true
    try {
      const response = await api.get('/api/content/global', {
        params: { page, size: 20 }
      })
      if (page === 0) {
        timeline.value = response.data.data.content
      } else {
        timeline.value.push(...response.data.data.content)
      }
      currentPage.value = response.data.data.currentPage
      totalPages.value = response.data.data.totalPages
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchUserTimeline(userId, page = 0) {
    loading.value = true
    try {
      const response = await api.get(`/api/users/${userId}/timeline`, {
        params: { page, size: 20 }
      })
      return response.data.data
    } finally {
      loading.value = false
    }
  }

  async function fetchContent(id) {
    loading.value = true
    try {
      const response = await api.get(`/api/content/${id}`)
      currentContent.value = response.data.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function createContent(data) {
    const formData = new FormData()
    if (data.textContent) {
      formData.append('textContent', data.textContent)
    }
    if (data.location) {
      formData.append('location', data.location)
    }
    if (data.tags && data.tags.length > 0) {
      data.tags.forEach(tag => formData.append('tags', tag))
    }
    if (data.media && data.media.length > 0) {
      data.media.forEach(file => formData.append('media', file))
    }

    // Don't set Content-Type header - let browser set it with boundary
    const response = await api.post('/api/content', formData)
    return response.data
  }

  async function likeContent(id) {
    const response = await api.post(`/api/content/${id}/like`)
    const content = timeline.value.find(c => c.id === id)
    if (content) {
      content.likedByCurrentUser = !content.likedByCurrentUser
      content.likeCount += content.likedByCurrentUser ? 1 : -1
    }
    return response.data
  }

  async function addComment(id, content) {
    const response = await api.post(`/api/content/${id}/comment`, { content })
    const item = timeline.value.find(c => c.id === id)
    if (item) {
      item.commentCount++
    }
    return response.data
  }

  async function getComments(id) {
    const response = await api.get(`/api/content/${id}/comments`)
    return response.data.data
  }

  return {
    timeline,
    currentContent,
    loading,
    currentPage,
    totalPages,
    fetchGlobalTimeline,
    fetchUserTimeline,
    fetchContent,
    createContent,
    likeContent,
    addComment,
    getComments
  }
})
