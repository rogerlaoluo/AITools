<template>
  <div class="notifications-page">
    <div class="container">
      <h2 class="page-title">Notifications</h2>
      <div class="notifications-list card">
        <el-empty description="No notifications yet" />
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.notifications-page {
  min-height: 100vh;
  padding: 20px 0;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
}

.notifications-list {
  padding: 24px;
}
</style>
