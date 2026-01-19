<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <el-icon class="logo-icon"><Trophy /></el-icon>
        <span class="logo-text" v-show="!sidebarCollapsed">管理后台</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item">
          <el-icon><DataAnalysis /></el-icon>
          <span v-show="!sidebarCollapsed">仪表盘</span>
        </router-link>
        <router-link to="/admin/tasks" class="nav-item">
          <el-icon><List /></el-icon>
          <span v-show="!sidebarCollapsed">任务管理</span>
        </router-link>
        <router-link to="/admin/review" class="nav-item">
          <el-icon><Checked /></el-icon>
          <span v-show="!sidebarCollapsed">审核中心</span>
        </router-link>
        <router-link to="/admin/email" class="nav-item">
          <el-icon><Message /></el-icon>
          <span v-show="!sidebarCollapsed">邮件配置</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <router-link to="/" class="nav-item">
          <el-icon><HomeFilled /></el-icon>
          <span v-show="!sidebarCollapsed">返回首页</span>
        </router-link>
      </div>
    </aside>

    <div class="main-container">
      <header class="top-bar">
        <button class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </button>

        <div class="top-bar-right">
          <span class="welcome" v-if="authStore.admin">
            欢迎，{{ authStore.admin.username }}
          </span>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="36">
              {{ authStore.admin?.username?.charAt(0) || 'A' }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const sidebarCollapsed = ref(false)

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchAdmin()
  }
})

function handleCommand(command: string) {
  if (command === 'logout') {
    authStore.logout()
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-color);
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #7C2D12 0%, #450A0A 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid rgba(254, 202, 202, 0.2);
}

.logo-icon {
  font-size: 28px;
  color: #FCD34D;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: white;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #FEF2F2;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(254, 202, 202, 0.15);
  color: white;
}

.nav-item.router-link-active {
  background: var(--primary-color);
  color: white;
}

.nav-item .el-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 16px 8px;
  border-top: 1px solid rgba(254, 202, 202, 0.2);
}

.main-container {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed + .main-container {
  margin-left: 64px;
}

.top-bar {
  height: 64px;
  background: white;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.toggle-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
}

.toggle-btn:hover {
  background: var(--bg-color);
  color: var(--text-primary);
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome {
  color: var(--text-secondary);
  font-size: 14px;
}

.main-content {
  flex: 1;
  padding: 24px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }

  .sidebar .logo-text,
  .sidebar .nav-item span {
    display: none;
  }

  .main-container {
    margin-left: 64px;
  }

  .welcome {
    display: none;
  }
}
</style>
