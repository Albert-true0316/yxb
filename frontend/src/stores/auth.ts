import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getCurrentAdmin } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const admin = ref<{ id: number; username: string; email: string } | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  async function loginAction(username: string, password: string) {
    const res = await login(username, password)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchAdmin()
  }

  async function registerAction(username: string, password: string, email: string) {
    const res = await register(username, password, email)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchAdmin()
  }

  async function fetchAdmin() {
    if (!token.value) return
    try {
      const res = await getCurrentAdmin()
      admin.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    admin.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    admin,
    isAuthenticated,
    loginAction,
    registerAction,
    fetchAdmin,
    logout
  }
})
