<template>
  <div class="search-page">
    <div class="container">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          size="large"
          placeholder="Search for content, tags..."
          @keydown.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <div v-if="hasSearched" class="search-results">
        <h3>Search Results for "{{ rawQuery }}"</h3>
        <div v-loading="loading" class="results-grid">
          <ContentItem
            v-for="item in results"
            :key="item.id"
            :content="item"
            @like="handleLike"
          />
        </div>
        <el-empty v-if="!loading && results.length === 0" description="No results found" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import ContentItem from '@/components/ContentItem.vue'
import { useContentStore } from '@/stores/content'
import { ElMessage } from 'element-plus'

const route = useRoute()
const contentStore = useContentStore()

const searchQuery = ref('')
const rawQuery = ref('')
const results = ref([])
const loading = ref(false)
const hasSearched = ref(false)

async function handleSearch() {
  if (!searchQuery.value.trim()) return

  rawQuery.value = searchQuery.value
  hasSearched.value = true
  loading.value = true

  try {
    const response = await api.get('/api/search', {
      params: {
        q: searchQuery.value.replace('#', ''),
        page: 0,
        size: 20
      }
    })
    results.value = response.data.data.content
  } catch (error) {
    ElMessage.error('Search failed')
  } finally {
    loading.value = false
  }
}

async function handleLike(contentId) {
  await contentStore.likeContent(contentId)
  const item = results.value.find(r => r.id === contentId)
  if (item) {
    item.likedByCurrentUser = !item.likedByCurrentUser
    item.likeCount += item.likedByCurrentUser ? 1 : -1
  }
}

// Initialize from query params
if (route.query.q) {
  searchQuery.value = route.query.q
  handleSearch()
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  padding: 20px 0;
}

.search-bar {
  max-width: 600px;
  margin: 0 auto 24px;
}

.search-results h3 {
  font-size: 18px;
  margin-bottom: 16px;
}

.results-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
