<template>
  <div class="content-feed">
    <div v-loading="contentStore.loading && contentStore.timeline.length === 0" class="feed-container">
      <ContentItem
        v-for="item in contentStore.timeline"
        :key="item.id"
        :content="item"
        @like="handleLike"
        @comment="handleComment"
      />
    </div>

    <div v-if="hasMore" class="load-more">
      <el-button
        :loading="contentStore.loading"
        @click="loadMore"
      >
        Load More
      </el-button>
    </div>

    <el-empty v-if="!contentStore.loading && contentStore.timeline.length === 0" description="No content yet" />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useContentStore } from '@/stores/content'
import ContentItem from './ContentItem.vue'

const contentStore = useContentStore()

const hasMore = computed(() =>
  contentStore.currentPage < contentStore.totalPages - 1
)

async function loadMore() {
  await contentStore.fetchGlobalTimeline(contentStore.currentPage + 1)
}

async function handleLike(contentId) {
  await contentStore.likeContent(contentId)
}

async function handleComment({ contentId, text }) {
  await contentStore.addComment(contentId, text)
}

onMounted(() => {
  if (contentStore.timeline.length === 0) {
    contentStore.fetchGlobalTimeline()
  }
})
</script>

<style scoped>
.content-feed {
  min-height: 400px;
}

.feed-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}
</style>
