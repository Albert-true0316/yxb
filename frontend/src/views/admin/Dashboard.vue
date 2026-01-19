<template>
  <div class="dashboard">
    <h1 class="page-title">仪表盘</h1>

    <div class="stats-grid" v-loading="loading">
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待认领</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon reviewing">
          <el-icon><View /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.reviewing }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon progress">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.inProgress }}</span>
          <span class="stat-label">进行中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.completed }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="$router.push('/admin/tasks?action=create')">
          <el-icon><Plus /></el-icon>
          <span>发布新任务</span>
        </div>
        <div class="action-card" @click="$router.push('/admin/review')">
          <el-icon><Checked /></el-icon>
          <span>审核任务</span>
        </div>
        <div class="action-card" @click="$router.push('/')">
          <el-icon><View /></el-icon>
          <span>查看任务榜</span>
        </div>
        <div class="action-card" @click="$router.push('/leaderboard')">
          <el-icon><Trophy /></el-icon>
          <span>查看排行榜</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAdminTasks } from '@/api/task'

const loading = ref(false)
const stats = reactive({
  pending: 0,
  reviewing: 0,
  inProgress: 0,
  completed: 0
})

onMounted(async () => {
  await loadStats()
})

async function loadStats() {
  loading.value = true
  try {
    const res = await getAdminTasks()
    const tasks = (res as any).data || []

    stats.pending = tasks.filter((t: any) => t.status === 0).length
    stats.reviewing = tasks.filter((t: any) => t.status === 1).length
    stats.inProgress = tasks.filter((t: any) => t.status === 2).length
    stats.completed = tasks.filter((t: any) => t.status === 3).length
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 40px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-primary);
  margin-bottom: 32px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 28px;
  margin-bottom: 56px;
}

.stat-card {
  background: white;
  border-radius: 24px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #E5E7EB;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #D1D5DB;
}

.stat-icon {
  width: 68px;
  height: 68px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #F3F4F6, #E5E7EB);
  color: #6B7280;
}

.stat-icon.reviewing {
  background: linear-gradient(135deg, #FFF7ED, #FED7AA);
  color: #EA580C;
}

.stat-icon.progress {
  background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
  color: #2563EB;
}

.stat-icon.completed {
  background: linear-gradient(135deg, #ECFDF5, #D1FAE5);
  color: #059669;
}

.stat-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.stat-value {
  font-size: 44px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 15px;
  color: var(--text-secondary);
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
  margin-top: 8px;
}

.quick-actions h2 {
  font-size: 28px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.action-card {
  background: white;
  border-radius: 20px;
  padding: 36px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #E5E7EB;
}

.action-card:hover {
  transform: translateY(-10px) scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #60A5FA;
}

.action-card .el-icon {
  font-size: 48px;
  color: #3B82F6;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-card:hover .el-icon {
  transform: scale(1.2) rotate(10deg);
}

.action-card span {
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  font-size: 16px;
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .stat-card {
    padding: 24px;
  }

  .stat-value {
    font-size: 32px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    font-size: 24px;
  }

  .page-title {
    font-size: 32px;
  }
}
</style>
