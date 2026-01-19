<template>
  <div class="task-detail-admin" v-loading="loading">
    <div class="back-link">
      <el-button link @click="$router.push('/admin/tasks')">
        <el-icon><ArrowLeft /></el-icon>
        返回任务列表
      </el-button>
    </div>

    <div class="detail-card" v-if="task">
      <div class="detail-header">
        <div>
          <el-tag :type="getStatusType(task.status)" size="large">
            {{ task.statusText }}
          </el-tag>
          <h1 class="task-title">{{ task.title }}</h1>
        </div>
        <div class="reward-badge">
          <span class="reward-value">{{ task.reward }}</span>
          <span class="reward-label">积分</span>
        </div>
      </div>

      <div class="detail-meta">
        <span><el-icon><Calendar /></el-icon> 截止：{{ formatDate(task.deadline) }}</span>
      </div>

      <div class="detail-content">
        <h3>任务描述</h3>
        <div class="content-html" v-html="task.content || '暂无描述'"></div>
      </div>

      <!-- 成员管理 -->
      <div class="members-section">
        <div class="section-header">
          <h3>参与成员</h3>
          <el-button v-if="task.status !== 3" size="small" @click="showAssignDialog = true">
            <el-icon><Plus /></el-icon>
            直接分配
          </el-button>
        </div>

        <el-table :data="task.members" v-if="task.members?.length > 0">
          <el-table-column prop="memberName" label="姓名" width="120" />
          <el-table-column prop="memberEmail" label="邮箱" min-width="200" />
          <el-table-column prop="statusText" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getMemberStatusType(row.status)">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="earnedPoints" label="获得积分" width="100">
            <template #default="{ row }">
              <span v-if="row.earnedPoints > 0" class="points">+{{ row.earnedPoints }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button link type="success" @click="handleApprove(row.id)">通过</el-button>
                <el-button link type="danger" @click="handleReject(row.id)">拒绝</el-button>
              </template>
              <template v-else-if="row.status === 1 && task.status === 2">
                <el-button link type="danger" @click="handleRemove(row.id)">移除</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无成员" />
      </div>

      <!-- 操作按钮 -->
      <div class="actions" v-if="task.status === 2">
        <el-button type="success" size="large" @click="handleComplete">
          <el-icon><CircleCheck /></el-icon>
          完成任务
        </el-button>
      </div>
    </div>

    <!-- 直接分配对话框 -->
    <el-dialog v-model="showAssignDialog" title="直接分配成员" width="500px">
      <p class="dialog-tip">任务悬赏：<strong>{{ task?.reward }}</strong> 积分，已分配：<strong>{{ allocatedPoints }}</strong> 积分，剩余：<strong>{{ task ? task.reward - allocatedPoints : 0 }}</strong> 积分</p>
      <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" label-width="80px">
        <el-form-item label="姓名" prop="memberName">
          <el-input v-model="assignForm.memberName" placeholder="成员姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="memberEmail">
          <el-input v-model="assignForm.memberEmail" placeholder="成员邮箱" />
        </el-form-item>
        <el-form-item label="积分" prop="earnedPoints">
          <el-input-number v-model="assignForm.earnedPoints" :min="0" :max="task ? task.reward - allocatedPoints : 0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitting">分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminTaskDetail, approveMember, rejectMember, removeMember,
  assignMember, completeTask, TaskDetail
} from '@/api/task'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const task = ref<TaskDetail | null>(null)
const showAssignDialog = ref(false)
const submitting = ref(false)
const assignFormRef = ref()

const assignForm = reactive({ memberName: '', memberEmail: '', earnedPoints: 0 })

const allocatedPoints = computed(() => {
  if (!task.value?.members) return 0
  return task.value.members.reduce((sum, m) => sum + (m.earnedPoints || 0), 0)
})

const assignRules = {
  memberName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  memberEmail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadTask()
})

async function loadTask() {
  loading.value = true
  try {
    const taskId = Number(route.params.id)
    const res = await getAdminTaskDetail(taskId)
    task.value = (res as any).data
  } finally {
    loading.value = false
  }
}

async function handleApprove(memberId: number) {
  await ElMessageBox.confirm('确认通过该成员的认领申请？', '确认')
  await approveMember(Number(route.params.id), memberId)
  ElMessage.success('已通过')
  await loadTask()
}

async function handleReject(memberId: number) {
  await ElMessageBox.confirm('确认拒绝该成员的认领申请？', '确认')
  await rejectMember(Number(route.params.id), memberId)
  ElMessage.success('已拒绝')
  await loadTask()
}

async function handleRemove(memberId: number) {
  await ElMessageBox.confirm('确认移除该成员？', '确认', { type: 'warning' })
  await removeMember(Number(route.params.id), memberId)
  ElMessage.success('已移除')
  await loadTask()
}

async function submitAssign() {
  const valid = await assignFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await assignMember(Number(route.params.id), {
      memberName: assignForm.memberName,
      memberEmail: assignForm.memberEmail,
      earnedPoints: assignForm.earnedPoints
    })
    ElMessage.success('分配成功')
    showAssignDialog.value = false
    assignForm.memberName = ''
    assignForm.memberEmail = ''
    assignForm.earnedPoints = 0
    await loadTask()
  } finally {
    submitting.value = false
  }
}

async function handleComplete() {
  await ElMessageBox.confirm('确认完成该任务？', '确认')
  submitting.value = true
  try {
    await completeTask(Number(route.params.id))
    ElMessage.success('任务已完成')
    await loadTask()
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function getStatusType(status: number) {
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return types[status] || 'info'
}

function getMemberStatusType(status: number) {
  const types: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}
</script>

<style scoped>
.task-detail-admin { max-width: 900px; margin: 0 auto; }
.back-link { margin-bottom: 24px; }
.detail-card { background: white; border-radius: 16px; padding: 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
.detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.task-title { font-size: 24px; font-weight: 700; margin-top: 12px; }
.reward-badge { background: linear-gradient(135deg, #f59e0b, #fbbf24); color: white; padding: 16px 24px; border-radius: 12px; text-align: center; }
.reward-value { font-size: 28px; font-weight: 700; display: block; }
.reward-label { font-size: 12px; }
.detail-meta { color: var(--text-secondary); margin-bottom: 24px; display: flex; align-items: center; gap: 8px; }
.detail-content h3, .members-section h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.content-html { color: var(--text-secondary); line-height: 1.8; }
.members-section { margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--border-color); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.points { font-weight: 600; color: #f59e0b; }
.actions { margin-top: 32px; text-align: center; }
.dialog-tip { margin-bottom: 16px; color: var(--text-secondary); }
.dialog-tip strong { color: #f59e0b; }
</style>
