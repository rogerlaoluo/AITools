<template>
  <div class="layout">
    <header class="header">
      <div class="container">
        <div class="header-inner">
          <router-link to="/" class="logo">Timeline</router-link>
          <nav class="nav">
            <router-link to="/" class="nav-item">
              <el-icon><HomeFilled /></el-icon>
              <span>Home</span>
            </router-link>
            <router-link to="/explore" class="nav-item">
              <el-icon><Compass /></el-icon>
              <span>Explore</span>
            </router-link>
            <router-link v-if="authStore.isAuthenticated" to="/post" class="nav-item">
              <el-icon><Plus /></el-icon>
              <span>Post</span>
            </router-link>
            <router-link to="/search" class="nav-item">
              <el-icon><Search /></el-icon>
              <span>Search</span>
            </router-link>
          </nav>
          <div class="header-right">
            <template v-if="authStore.isAuthenticated">
              <router-link :to="`/user/${authStore.user.id}`" class="user-link">
                <el-avatar :src="authStore.user.avatarUrl" :size="32">
                  {{ authStore.user.username?.charAt(0) }}
                </el-avatar>
              </router-link>
              <el-dropdown>
                <span class="dropdown-link">
                  <el-icon><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="router.push('/settings')">Settings</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">Logout</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <router-link to="/login" class="btn-link">Login</router-link>
              <router-link to="/register" class="btn btn-primary">Sign Up</router-link>
            </template>
          </div>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { HomeFilled, Compass, Plus, Search, MoreFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('Are you sure you want to logout?', 'Logout', {
      type: 'warning'
    })
    authStore.logout()
    router.push({ name: 'Login' })
  } catch {
    // User cancelled
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: white;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 60px;
  gap: 24px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
}

.nav {
  display: flex;
  gap: 8px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 6px;
  text-decoration: none;
  color: #666;
  transition: all 0.2s;
}

.nav-item:hover,
.nav-item.router-link-active {
  background: #f0f0f0;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-link {
  text-decoration: none;
}

.dropdown-link {
  cursor: pointer;
  padding: 8px;
}

.btn-link {
  color: #409eff;
  text-decoration: none;
  padding: 8px 16px;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.main {
  padding: 20px 0;
}
</style>
