<template>
  <div class="hero-wrapper">
    <div class="container">
      <header>
        <div class="brand-area">
          <img src="https://jsyx.wmu.edu.cn/assets2024/images/logo.png" class="logo-img" style="height: 50px; width: auto;" />
          <div class="brand-text-group">
            <div class="institute-name">心理健康和智能计算研究院</div>
            <div class="board-name">英雄任务榜</div>
          </div>
        </div>
        <div class="data-stream">
          <div><span style="color:var(--status-done-fg)">●</span> 系统在线</div>
          <div>悬赏池: <span style="color:var(--points-gold)">{{ totalReward.toLocaleString() }} 积分</span></div>
        </div>
      </header>

      <div class="dashboard">
        <section class="glass-panel task-section">
          <div class="panel-header-primary">
            <div class="panel-title">战术任务板 // TASK BOARD</div>
            <div class="tabs">
              <div v-for="t in tabs" :key="t.val" :class="['tab', { active: currentFilter === t.val }]" @click="currentFilter = t.val">
                {{ t.label }}
              </div>
            </div>
          </div>

          <div class="filter-bar">
            <div 
              v-for="tag in tagFilters" 
              :key="tag.val"
              :class="['filter-chip', { active: currentTagFilter === tag.val }]"
              :style="currentTagFilter !== tag.val ? { color: tag.color } : {}"
              @click="currentTagFilter = tag.val"
            >
              {{ tag.label }}
            </div>
          </div>

          <div class="task-grid" v-loading="loading" element-loading-background="rgba(0, 0, 0, 0.5)">
            <TaskCard 
              v-for="task in filteredTasks" 
              :key="task.id" 
              :task="task"
              @click="handleTaskClick(task)"
            />
            <div class="empty-state" v-if="filteredTasks.length === 0 && !loading" style="display:block">
               <h3>暂无任务</h3>
               <p>当前筛选条件下暂无数据</p>
            </div>
          </div>
        </section>

        <section class="glass-panel leaderboard-section" @click="openLeaderboard">
          <div class="panel-header-lb">
             <div class="panel-title" style="justify-content: center;">先锋榜</div>
          </div>
          <div class="rank-list" style="padding: 20px;">
             <div style="text-align:center; color: #94A3B8;">点击查看完整榜单</div>
          </div>
        </section>
      </div>

      <LeaderboardOverlay 
        :visible="isLeaderboardOpen"
        :leaders="leaderboardData"
        @close="closeLeaderboard"
      />

      <TaskModal 
        v-if="currentTask"
        v-model="isTaskModalOpen"
        :task="currentTask"
        @refresh="loadData"
        @closed="handleModalClose"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicTasks, getLeaderboard } from '@/api/task'
import type { Task, LeaderboardEntry } from '@/api/task'
// ✅ 引入 deriveTags 用于筛选逻辑
import { getStatusConfig, deriveTags } from '@/utils/format'
import TaskCard from '@/components/TaskCard.vue'
import LeaderboardOverlay from '@/components/LeaderboardOverlay.vue'
import TaskModal from '@/components/TaskModal.vue'

const route = useRoute()
const router = useRouter()

const tasks = ref<Task[]>([])
const leaderboardData = ref<LeaderboardEntry[]>([])
const loading = ref(false)

// --- 状态筛选 ---
const currentFilter = ref('all')
const tabs = [
  { label: '全部', val: 'all' },
  { label: '待认领', val: 'todo' },
  { label: '进行中', val: 'active' },
  { label: '审核中', val: 'review' }
]

// --- ✅ 修复：标签筛选配置 ---
const currentTagFilter = ref('all')
const tagFilters = [
  { label: '全部', val: 'all', color: '' },
  { label: 'AI 编程', val: 'tag-ai', color: 'var(--tag-ai-fg)' },
  { label: '数据分析', val: 'tag-data', color: 'var(--tag-data-fg)' },
  { label: '多媒体', val: 'tag-media', color: 'var(--tag-media-fg)' },
  { label: '学术写作', val: 'tag-paper', color: 'var(--tag-paper-fg)' },
  { label: '综合事务', val: 'tag-misc', color: 'var(--tag-misc-fg)' }
]

const isLeaderboardOpen = ref(false)
const isTaskModalOpen = ref(false)
const currentTask = ref<Task | null>(null)

const totalReward = computed(() => tasks.value.reduce((sum, t) => sum + t.reward, 0))

// --- ✅ 修复：筛选逻辑 (同时支持状态和标签) ---
const filteredTasks = computed(() => {
  return tasks.value.filter(t => {
    // 1. 状态匹配
    const statusMatch = currentFilter.value === 'all' || getStatusConfig(t.status).value === currentFilter.value
    
    // 2. 标签匹配
    // 获取该任务的所有标签类名 (例如 ['tag-ai', 'tag-paper'])
    const taskTagClasses = deriveTags(t).map(tag => tag.class)
    const tagMatch = currentTagFilter.value === 'all' || taskTagClasses.includes(currentTagFilter.value)
    
    return statusMatch && tagMatch
  })
})

// --- 路由联动逻辑 ---
watch(() => route.path, async (newPath) => {
  if (route.name === 'Leaderboard') {
    isLeaderboardOpen.value = true
    isTaskModalOpen.value = false
  } 
  else if (route.name === 'TaskDetail' && route.params.id) {
    isLeaderboardOpen.value = false
    const taskId = Number(route.params.id)
    if (tasks.value.length === 0) await loadData()
    const target = tasks.value.find(t => t.id === taskId)
    if (target) {
      currentTask.value = target
      isTaskModalOpen.value = true
    }
  } 
  else {
    isLeaderboardOpen.value = false
    isTaskModalOpen.value = false
  }
}, { immediate: true })

const handleTaskClick = (task: Task) => {
  router.push({ name: 'TaskDetail', params: { id: task.id } })
}

const openLeaderboard = () => {
  router.push({ name: 'Leaderboard' })
}

const closeLeaderboard = () => {
  router.push({ name: 'Home' })
}

const handleModalClose = () => {
  if (route.name === 'TaskDetail') {
    router.push({ name: 'Home' })
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [tRes, lRes] = await Promise.all([getPublicTasks(), getLeaderboard()])
    // @ts-ignore
    tasks.value = tRes.data || tRes 
    // @ts-ignore
    leaderboardData.value = lRes.data || lRes
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style src="@/styles/heroboard.css"></style>