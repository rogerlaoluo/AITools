import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
    } else {
      localStorage.removeItem('token')
      delete api.defaults.headers.common['Authorization']
    }
  }

  function setUser(newUser) {
    user.value = newUser
    if (newUser) {
      localStorage.setItem('user', JSON.stringify(newUser))
    } else {
      localStorage.removeItem('user')
    }
  }

  async function login(username, password) {
    try {
      const response = await api.post('/api/auth/login', { username, password })
      setToken(response.data.data.token)
      setUser(response.data.data.user)
      return response.data
    } catch (error) {
      throw error
    }
  }

  async function register(username, email, password) {
    try {
      const response = await api.post('/api/auth/register', { username, email, password })
      return response.data
    } catch (error) {
      throw error
    }
  }

  async function getCurrentUser() {
    try {
      const response = await api.get('/api/auth/me')
      setUser(response.data.data)
      return response.data
    } catch (error) {
      logout()
      throw error
    }
  }

  function logout() {
    setToken(null)
    setUser(null)
  }

  // Initialize
  if (token.value) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }

  return {
    token,
    user,
    isAuthenticated,
    setToken,
    setUser,
    login,
    register,
    getCurrentUser,
    logout
  }
})
