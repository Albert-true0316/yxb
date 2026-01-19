<template>
  <div class="container" id="mainContainer">
    <section class="glass-panel task-section" v-show="!showLeaderboard">
      <div class="panel-header-primary">
        <div class="panel-title">TASK ARRAY // 任务列表</div>
        <div class="header-actions">
          <button class="btn-tech-primary" @click="showCreateDialog = true">
            <span class="brand-prefix">+</span> PUBLISH_TASK
          </button>
          <div class="tabs">
            <div 
              v-for="opt in filterOptions" :key="opt.value"
              class="tab" :class="{ active: statusFilter === opt.value }"
              @click="statusFilter = opt.value"
            >
              {{ opt.label }}
            </div>
          </div>
        </div>
      </div>

      <div class="filter-bar">
        <span class="filter-label">TAG_FILTER:</span>
        <div class="filter-chip" :class="{ active: tagFilter === 'all' }" @click="tagFilter = 'all'">全部</div>
        <div class="filter-chip" v-for="tag in ['AI编程', '数据分析', '学术写作']" 
             :key="tag" :class="{ active: tagFilter === tag }" @click="tagFilter = tag">
          {{ tag }}
        </div>
      </div>
      
      <div class="task-grid" v-loading="loading">
        <div 
          v-for="task in filteredTasks" :key="task.id" 
          class="task-card is-breathing"
          @click="viewTask(task.id)"
        >
          <div class="task-bar" :class="getBarClass(task.status)"></div>
          <div class="task-header-row">
            <span class="task-id">T-{{ task.id }}</span>
            <span class="task-points">{{ task.reward }} PTS</span>
          </div>
          <div class="task-name">{{ task.title }}</div>
          <div class="task-footer">
            <div class="deadline">{{ getDaysLeft(task.deadline) }}</div>
            <div class="avatar">{{ task.memberNames?.[0]?.charAt(0) || '?' }}</div>
          </div>
        </div>
        
        <div class="leaderboard-trigger-card" @click="toggleLeaderboard(true)">
          <div class="podium-mini">👑 查看全球先锋榜 ↗</div>
        </div>
      </div>
    </section>

    <Transition name="fs-slide">
      <div v-if="showLeaderboard" class="fs-leaderboard">
        <div class="fs-header">
          <button class="btn-back" @click="toggleLeaderboard(false)">← BACK_TO_TERMINAL</button>
          <div class="brand"><span class="brand-prefix">GLOBAL RANKING //</span> 英雄积分榜</div>
        </div>
        <div class="fs-content">
          <div class="list-panel-fs">
             <div v-for="(hero, index) in leaderboardData" :key="index" class="list-item">
                <div class="rank-num">{{ index + 1 }}</div>
                <div class="row-user">{{ hero.memberName }}</div>
                <div class="row-score">{{ hero.totalPoints }}</div>
             </div>
          </div>
        </div>
      </div>
    </Transition>

    <el-dialog v-model="showCreateDialog" title="NEW_TASK_INPUT" width="500px" custom-class="cyber-dialog">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="TASK_TITLE">
          <el-input v-model="createForm.title" placeholder="输入任务名称..." />
        </el-form-item>
        <el-form-item label="REWARD_POINTS">
          <el-input-number v-model="createForm.reward" :min="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="DEADLINE">
          <el-date-picker v-model="createForm.deadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-tech-secondary" @click="showCreateDialog = false">CANCEL</button>
        <button class="btn-tech-primary" @click="handleCreateTask">CONFIRM_PUBLISH</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicTasks, getLeaderboard, createTask, Task, LeaderboardEntry } from '@/api/task'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tasks = ref<Task[]>([])
const leaderboardData = ref<LeaderboardEntry[]>([])
const statusFilter = ref(-1)
const tagFilter = ref('all')
const showLeaderboard = ref(false)
const showCreateDialog = ref(false)

const createForm = ref({ title: '', reward: 100, deadline: '', content: '系统自动生成描述' })
const filterOptions = [
  { label: '全部', value: -1 }, { label: '待认领', value: 0 },
  { label: '进行中', value: 2 }, { label: '已完成', value: 3 }
]

const loadData = async () => {
  loading.value = true
  try {
    const [tRes, lRes] = await Promise.all([getPublicTasks(), getLeaderboard(50)])
    tasks.value = (tRes as any).data || []
    leaderboardData.value = (lRes as any).data || []
  } finally {
    loading.value = false
  }
}

async function handleCreateTask() {
  try {
    await createTask(createForm.value)
    ElMessage.success('任务成功同步至区块链(数据库)')
    showCreateDialog.value = false
    loadData()
  } catch (e) { ElMessage.error('发布失败') }
}

function toggleLeaderboard(state: boolean) {
  showLeaderboard.value = state
  const container = document.getElementById('mainContainer')
  if (container) container.style.filter = state ? "blur(15px) brightness(0.6)" : "none"
}

const filteredTasks = computed(() => {
  let list = tasks.value
  if (statusFilter.value !== -1) list = list.filter(t => t.status === statusFilter.value)
  return list
})

onMounted(loadData)
const viewTask = (id: number) => router.push(`/task/${id}`)
const getBarClass = (s: number) => ['bar-todo', 'bar-review', 'bar-active', 'bar-done'][s] || 'bar-todo'
const getDaysLeft = (d: string) => d // 简略处理，可替换为你的日期函数
</script>

<style scoped>
/* 核心容器 */
.task-board-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  color: #E2E8F0;
  font-family: 'JetBrains Mono', monospace;
}

/* 玻璃拟态面板 */
.glass-panel {
  background: rgba(23, 31, 46, 0.8);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(56, 189, 248, 0.2);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部区域布局 */
.panel-header-primary {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(56, 189, 248, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

/* 选项卡 (Tabs) */
.tabs {
  display: flex;
  gap: 10px;
}

.tab {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: #94A3B8;
  border-radius: 8px;
  transition: all 0.3s;
}

.tab.active {
  background: rgba(56, 189, 248, 0.15);
  color: #38BDF8;
  box-shadow: inset 0 0 10px rgba(56, 189, 248, 0.2);
}

/* 过滤器条 */
.filter-bar {
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 任务网格 - 关键布局修复 */
.task-grid {
  padding: 24px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); /* 强制网格化 */
  gap: 20px;
  min-height: 400px;
}

/* 任务卡片样式 */
.task-card {
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 12px;
  padding: 20px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.task-card:hover {
  border-color: #38BDF8;
  transform: translateY(-5px);
  background: rgba(30, 41, 59, 0.8);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
}

/* 任务卡片内部布局 */
.task-header-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.task-id { color: #64748B; font-size: 12px; }
.task-points { color: #F59E0B; font-weight: bold; }
.task-name { font-size: 16px; font-weight: 600; margin-bottom: 15px; }

/* 侧边色条 */
.task-bar {
  position: absolute;
  left: 0;
  top: 15%;
  bottom: 15%;
  width: 3px;
  border-radius: 0 4px 4px 0;
}
.bar-active { background: #38BDF8; box-shadow: 0 0 10px #38BDF8; }
.bar-todo { background: #64748B; }

/* 全屏排行榜覆盖层样式 */
.fs-leaderboard {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: #0B101A;
  z-index: 2000;
  padding: 40px;
}
</style>