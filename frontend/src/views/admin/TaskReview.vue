<template>
  <div class="task-review">
    <h1 class="page-title">审核中心</h1>
    <p class="page-desc">审核成员的任务认领申请</p>

    <div class="review-list" v-loading="loading">
      <div v-for="task in reviewTasks" :key="task.id" class="review-card">
        <div class="task-info">
          <h3>{{ task.title }}</h3>
          <div class="task-meta">
            <span class="reward"><el-icon><Coin /></el-icon> {{ task.reward }} 积分</span>
            <span><el-icon><Calendar /></el-icon> 截止：{{ formatDate(task.deadline) }}</span>
          </div>
        </div>

        <div class="pending-members" v-if="getPendingMembers(task).length > 0">
          <div v-for="member in getPendingMembers(task)" :key="member.id" class="member-row">
            <el-avatar :size="40">{{ member.memberName.charAt(0) }}</el-avatar>
            <div class="member-info">
              <span class="member-name">{{ member.memberName }}</span>
              <span class="member-email">{{ member.memberEmail }}</span>
            </div>
            <div class="member-actions">
              <el-button type="success" size="small" @click="handleApproveOnly(task.id, member.id)">
                <el-icon><Check /></el-icon> 通过审核
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(task.id, member.id)">
                <el-icon><Close /></el-icon> 拒绝
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="getApprovedMembers(task).length > 0 && getPendingMembers(task).length === 0" class="approved-notice">
          <el-icon color="#10b981" :size="20"><CircleCheck /></el-icon>
          <span>所有成员已审核完毕，可开始任务</span>
        </div>

        <div v-if="hasApprovedMembers(task)" class="start-task-section">
          <el-button type="primary" size="large" @click="handleStartTask(task.id)">
            <el-icon><VideoPlay /></el-icon> 开始任务
          </el-button>
          <span class="hint-text">点击后任务将进入进行中状态</span>
        </div>
      </div>

      <el-empty v-if="!loading && reviewTasks.length === 0" description="暂无待审核任务" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminTasks, getAdminTaskDetail, approveMemberOnly, rejectMember, startTask, TaskDetail } from '@/api/task'

const loading = ref(false)
const reviewTasks = ref<TaskDetail[]>([])

onMounted(async () => {
  await loadReviewTasks()
})

async function loadReviewTasks() {
  loading.value = true
  try {
    const res = await getAdminTasks(1)
    const tasks = (res as any).data || []

    const details: TaskDetail[] = []
    for (const task of tasks) {
      const detail = await getAdminTaskDetail(task.id)
      const taskDetail = (detail as any).data
      if (taskDetail && (getPendingMembers(taskDetail).length > 0 || getApprovedMembers(taskDetail).length > 0)) {
        details.push(taskDetail)
      }
    }
    reviewTasks.value = details
  } finally {
    loading.value = false
  }
}

function getPendingMembers(task: TaskDetail) {
  return task.members?.filter(m => m.status === 0) || []
}

function getApprovedMembers(task: TaskDetail) {
  return task.members?.filter(m => m.status === 1) || []
}

function hasApprovedMembers(task: TaskDetail) {
  return getApprovedMembers(task).length > 0
}

async function handleApproveOnly(taskId: number, memberId: number) {
  await ElMessageBox.confirm('确认通过该成员的认领申请？', '确认审核')
  await approveMemberOnly(taskId, memberId)
  ElMessage.success('审核通过，可在所有成员审核完毕后点击"开始任务"')
  await loadReviewTasks()
}

async function handleStartTask(taskId: number) {
  await ElMessageBox.confirm('确认开始该任务？任务将进入进行中状态。', '确认开始')
  await startTask(taskId)
  ElMessage.success('任务已开始')
  await loadReviewTasks()
}

async function handleReject(taskId: number, memberId: number) {
  await ElMessageBox.confirm('确认拒绝该成员的认领申请？', '确认拒绝')
  await rejectMember(taskId, memberId)
  ElMessage.success('已拒绝')
  await loadReviewTasks()
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.task-review {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 53px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  margin-bottom: 12px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-desc {
  color: var(--text-secondary);
  margin-bottom: 40px;
  font-size: 23px;
  font-weight: 600;
}

.review-card {
  background: white;
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #E5E7EB;
  transition: all 0.3s ease;
}

.review-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
  border-color: #D1D5DB;
}

.task-info h3 {
  font-size: 32px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.task-meta {
  display: flex;
  gap: 24px;
  color: var(--text-secondary);
  font-size: 20px;
  font-weight: 600;
}

.task-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.reward {
  color: #f59e0b;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  font-size: 21px;
}

.pending-members {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #E5E7EB;
}

.approved-notice {
  margin-top: 24px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #ECFDF5, #D1FAE5);
  border-radius: 16px;
  border: 2px solid #10b981;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
  color: #047857;
}

.member-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: #F9FAFB;
  border-radius: 16px;
  margin-bottom: 12px;
  border: 1px solid #E5E7EB;
  transition: all 0.3s ease;
}

.member-row:hover {
  transform: translateX(8px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #D1D5DB;
}

.member-row:last-child {
  margin-bottom: 0;
}

.start-task-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #E5E7EB;
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: center;
}

.hint-text {
  color: var(--text-secondary);
  font-size: 19px;
  font-weight: 600;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-name {
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  font-size: 21px;
  color: var(--text-primary);
}

.member-email {
  font-size: 17px;
  color: var(--text-secondary);
  font-weight: 600;
}

.member-actions {
  display: flex;
  gap: 12px;
}

:deep(.el-avatar) {
  background: var(--primary-gradient);
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

:deep(.el-button) {
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 43px;
  }

  .member-row {
    flex-wrap: wrap;
  }

  .member-actions {
    width: 100%;
    justify-content: flex-end;
    margin-top: 8px;
  }
}
</style>
