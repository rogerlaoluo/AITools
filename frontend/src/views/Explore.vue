<template>
  <div class="explore-page">
    <div class="container">
      <h2 class="page-title">Explore</h2>
      <ContentFeed />
    </div>
  </div>
</template>

<script setup>
import ContentFeed from '@/components/ContentFeed.vue'
</script>

<style scoped>
.explore-page {
  min-height: 100vh;
  padding: 20px 0;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
}
</style>
