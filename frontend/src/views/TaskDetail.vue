<template>
  <div class="task-detail container" v-loading="loading">
    <div class="nav-back" @click="$router.push('/')">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回任务榜</span>
    </div>

    <div class="layout-grid" v-if="task">
      <div class="content-panel">
        <div class="panel-card">
          <div class="task-header">
            <span class="status-pill" :class="getStatusClass(task.status)">
              {{ task.statusText }}
            </span>
            <h1 class="task-h1">{{ task.title }}</h1>
            <div class="task-meta-row">
              <span class="meta-tag">
                <el-icon><User /></el-icon> {{ task.members?.length || 0 }} 位参与者
              </span>
              <span class="meta-tag">
                <el-icon><Calendar /></el-icon> {{ formatDate(task.deadline) }}
              </span>
            </div>
          </div>

          <div class="divider"></div>

          <div class="task-body">
            <h3 class="section-title">任务描述</h3>
            <div class="rich-text" v-html="task.content || '暂无描述'"></div>
          </div>
        </div>

        <div class="panel-card members-section" v-if="task.members && task.members.length > 0">
          <h3 class="section-title">参与成员</h3>
          <div class="members-grid">
            <div v-for="member in task.members" :key="member.id" class="member-chip">
              <div class="m-avatar">{{ member.memberName.charAt(0) }}</div>
              <div class="m-info">
                <div class="m-name">{{ member.memberName }}</div>
                <div class="m-points" v-if="member.earnedPoints > 0">{{ member.earnedPoints }} 积分</div>
                <div class="m-status" :class="'s-'+member.status">{{ member.statusText }}</div>
              </div>
              <el-button
                v-if="member.status === 0 && task.status < 2"
                size="small"
                type="primary"
                link
                @click="openEditDialog(member)"
              >修改</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="side-panel">
        <div class="stat-card highlight-card">
          <div class="xp-label">奖励</div>
          <div class="xp-val">{{ task.reward }}</div>
          <div class="xp-unit">积分</div>
        </div>

        <div class="stat-card countdown-card">
          <div class="cd-label">剩余时间</div>
          <div class="cd-val" :class="getDaysLeftClass(task.deadline)">
            {{ getDaysLeft(task.deadline) }}
          </div>
        </div>

        <div class="action-card" v-if="task.status === 0 || task.status === 1">
          <el-button type="primary" class="claim-btn" @click="openClaimDialog">
            认领任务
          </el-button>
          <p class="action-note">认领后需等待管理员审核</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="showClaimDialog" :title="isEditMode ? '修改申请' : '认领任务'" width="500px" align-center @close="resetForm">
      <p class="dialog-desc">{{ isEditMode ? '修改您的认领申请信息' : '请填写您的信息以认领此任务' }}</p>
      <el-form :model="claimForm" :rules="claimRules" ref="claimFormRef" label-position="top">
        <el-form-item label="截止时间" prop="deadline" v-if="task && task.status < 2">
          <el-date-picker v-model="claimForm.deadline" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="您的姓名" prop="memberName">
          <el-input v-model="claimForm.memberName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱地址" prop="memberEmail">
          <el-input v-model="claimForm.memberEmail" placeholder="用于接收通知" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item label="积分" prop="expectedPoints">
          <el-input-number v-model="claimForm.expectedPoints" :min="0" :max="availablePoints" placeholder="填写积分" style="width: 100%" />
          <div class="field-hint" v-if="task">剩余可用积分 {{ availablePoints }} / 任务总积分 {{ task.reward }}</div>
        </el-form-item>

        <el-form-item label="修改任务标题" v-if="task && task.status < 2">
          <el-input v-model="claimForm.title" :placeholder="task?.title" />
        </el-form-item>
        <el-form-item label="修改任务描述" v-if="task && task.status < 2">
          <el-input v-model="claimForm.content" type="textarea" placeholder="可选填写" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showClaimDialog = false">取消</el-button>
        <el-button type="primary" @click="submitClaim" :loading="submitting">
          {{ isEditMode ? '保存修改' : '确认认领' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTaskDetail, claimTask, updateClaim, TaskDetail, TaskMember } from '@/api/task'

const route = useRoute()
const loading = ref(false)
const task = ref<TaskDetail | null>(null)
const showClaimDialog = ref(false)
const submitting = ref(false)
const claimFormRef = ref()
const isEditMode = ref(false)

interface ClaimForm {
  memberName: string
  memberEmail: string
  title: string
  content: string
  deadline: string
  expectedPoints: number | null
}

const getDefaultClaimForm = (): ClaimForm => ({
  memberName: '',
  memberEmail: '',
  title: '',
  content: '',
  deadline: task.value?.deadline || '',
  expectedPoints: null
})

const claimForm = ref<ClaimForm>(getDefaultClaimForm())

const allocatedPoints = computed(() => {
  if (!task.value?.members) return 0
  return task.value.members
    .filter(m => m.status !== 2 && (!isEditMode.value || m.memberEmail !== claimForm.value.memberEmail))
    .reduce((sum, m) => sum + (m.earnedPoints || 0), 0)
})

const availablePoints = computed(() => {
  if (!task.value) return 0
  return task.value.reward - allocatedPoints.value
})

const claimRules = computed(() => ({
  memberName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  memberEmail: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  expectedPoints: [
    { required: true, message: '请填写积分', trigger: 'change' },
    {
      validator: (_rule: any, value: number | null, callback: any) => {
        if (value !== null && value > availablePoints.value) {
          callback(new Error(`积分不能超过剩余可用积分 ${availablePoints.value}`))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}))

onMounted(() => loadTask())

async function loadTask() {
  loading.value = true
  try {
    const res = await getTaskDetail(Number(route.params.id))
    task.value = (res as any).data
  } finally {
    loading.value = false
  }
}

async function submitClaim() {
  const valid = await claimFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      memberName: claimForm.value.memberName,
      memberEmail: claimForm.value.memberEmail,
      deadline: claimForm.value.deadline,
      expectedPoints: claimForm.value.expectedPoints as number,
      title: claimForm.value.title || undefined,
      content: claimForm.value.content || undefined
    }

    if (isEditMode.value) {
      await updateClaim(Number(route.params.id), payload)
      ElMessage.success('修改成功')
    } else {
      await claimTask(Number(route.params.id), payload)
      ElMessage.success('认领成功，请等待管理员审核')
    }
    showClaimDialog.value = false
    await loadTask()
  } finally {
    submitting.value = false
  }
}

function openClaimDialog() {
  isEditMode.value = false
  claimForm.value = getDefaultClaimForm()
  claimFormRef.value?.clearValidate()
  showClaimDialog.value = true
}

function openEditDialog(member: TaskMember) {
  isEditMode.value = true
  claimForm.value = {
    memberName: member.memberName,
    memberEmail: member.memberEmail,
    expectedPoints: typeof member.earnedPoints === 'number' ? member.earnedPoints : 0,
    deadline: task.value?.deadline || '',
    title: task.value?.title || '',
    content: task.value?.content || ''
  }
  claimFormRef.value?.clearValidate()
  showClaimDialog.value = true
}

function resetForm() {
  isEditMode.value = false
  claimFormRef.value?.resetFields()
  claimForm.value = getDefaultClaimForm()
}

function formatDate(d: string) { return new Date(d).toLocaleDateString() }
function getStatusClass(s: number) { return ['s-pending', 's-review', 's-progress', 's-done'][s] }
function getDaysLeft(d: string) {
  const deadline = new Date(d)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  deadline.setHours(0, 0, 0, 0)
  const diff = Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  if (diff < 0) return '已过期'
  if (diff === 0) return '今天截止'
  return `还剩 ${diff} 天`
}
function getDaysLeftClass(d: string) {
  const deadline = new Date(d)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  deadline.setHours(0, 0, 0, 0)
  const diff = Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  if (diff <= 0) return 'urgent'
  if (diff <= 3) return 'warning'
  return 'normal'
}
</script>

<style scoped>
.task-detail { max-width: 1100px; padding-bottom: 80px; }

.nav-back {
  display: inline-flex; align-items: center; gap: 10px;
  margin: 32px 0; cursor: pointer; color: var(--text-secondary);
  font-weight: 700; font-family: 'Fredoka', sans-serif;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  padding: 12px 20px; border-radius: 999px;
  background: white; box-shadow: 0 4px 16px rgba(220, 38, 38, 0.1);
}
.nav-back:hover {
  color: white;
  background: var(--primary-gradient);
  transform: translateX(-8px);
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.3);
}

.layout-grid {
  display: grid; grid-template-columns: 1fr 340px; gap: 40px;
}

.panel-card {
  background: white; border-radius: 28px;
  padding: 48px; box-shadow: var(--shadow-md); margin-bottom: 32px;
  border: 3px solid rgba(220, 38, 38, 0.1);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.panel-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.task-h1 {
  font-size: 40px; font-weight: 800; line-height: 1.2; margin: 20px 0;
  color: var(--text-primary); font-family: 'Fredoka', sans-serif;
}

.status-pill {
  display: inline-block; padding: 10px 20px; border-radius: 999px;
  font-size: 13px; font-weight: 800; text-transform: uppercase;
  font-family: 'Fredoka', sans-serif; letter-spacing: 0.5px;
}
.s-pending { background: linear-gradient(135deg, #F3F4F6, #E5E7EB); color: #6B7280; }
.s-review { background: linear-gradient(135deg, #FFF7ED, #FED7AA); color: #EA580C; }
.s-progress { background: linear-gradient(135deg, #EFF6FF, #DBEAFE); color: #2563EB; }
.s-done { background: linear-gradient(135deg, #ECFDF5, #D1FAE5); color: #059669; }

.task-meta-row {
  display: flex; gap: 28px; color: var(--text-secondary);
  font-weight: 700; font-family: 'Fredoka', sans-serif; font-size: 15px;
}
.meta-tag {
  display: flex; align-items: center; gap: 8px;
}

.divider { height: 2px; background: linear-gradient(90deg, rgba(220, 38, 38, 0.1), transparent); margin: 40px 0; }
.section-title {
  font-size: 16px; text-transform: uppercase; letter-spacing: 1.5px;
  color: var(--primary-color); margin-bottom: 20px; font-weight: 800;
  font-family: 'Fredoka', sans-serif;
}

.rich-text { color: var(--text-secondary); line-height: 1.9; font-size: 17px; font-weight: 500; }

.stat-card {
  background: white; border-radius: 24px;
  padding: 32px; text-align: center; box-shadow: var(--shadow-md); margin-bottom: 24px;
  border: 3px solid rgba(220, 38, 38, 0.1);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: var(--shadow-lg);
}

.highlight-card {
  background: var(--primary-gradient); color: white;
  box-shadow: 0 12px 40px rgba(220, 38, 38, 0.4);
  position: relative; overflow: hidden;
}

.highlight-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1), transparent 50%);
  animation: rotate 15s linear infinite;
  pointer-events: none;
  will-change: transform;
}

.xp-label { font-size: 14px; font-weight: 700; opacity: 0.9; font-family: 'Fredoka', sans-serif; text-transform: uppercase; letter-spacing: 1px; position: relative; z-index: 1; }
.xp-val { font-size: 64px; font-weight: 800; line-height: 1; margin: 12px 0; font-family: 'Fredoka', sans-serif; position: relative; z-index: 1; }
.xp-unit { opacity: 0.85; font-size: 16px; margin-top: 8px; font-weight: 700; position: relative; z-index: 1; }

.countdown-card {
  background: linear-gradient(135deg, #FEF2F2, #FFF7ED);
}

.cd-label { font-size: 14px; font-weight: 700; color: var(--text-secondary); font-family: 'Fredoka', sans-serif; text-transform: uppercase; letter-spacing: 1px; }
.cd-val { font-size: 32px; font-weight: 800; color: var(--text-primary); margin-top: 12px; font-family: 'Fredoka', sans-serif; }
.cd-val.urgent { color: #DC2626; animation: urgentPulse 1.5s infinite; }
.cd-val.warning { color: #F59E0B; }

@keyframes urgentPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.action-card {
  background: white; border-radius: 24px; padding: 32px;
  box-shadow: var(--shadow-md); border: 3px solid rgba(220, 38, 38, 0.1);
}

.claim-btn {
  width: 100%; height: 56px; font-size: 18px; font-weight: 800;
  font-family: 'Fredoka', sans-serif;
}
.action-note {
  font-size: 13px; color: var(--text-light); text-align: center;
  margin-top: 16px; font-weight: 600;
}

.members-section {
  background: linear-gradient(135deg, #FEF2F2, #FFF7ED);
}

.members-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.member-chip {
  display: flex; align-items: center; gap: 14px;
  background: white; padding: 16px; border-radius: 20px;
  border: 2px solid rgba(220, 38, 38, 0.1);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.member-chip:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.2);
  border-color: rgba(220, 38, 38, 0.3);
}

.m-avatar {
  width: 44px; height: 44px; background: var(--primary-gradient); border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-family: 'Fredoka', sans-serif; font-size: 18px;
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.3); color: white;
}
.m-name { font-weight: 800; font-size: 15px; font-family: 'Fredoka', sans-serif; }
.m-points { font-size: 13px; color: var(--primary-color); font-weight: 700; }
.m-status { font-size: 12px; font-weight: 700; }

.dialog-desc {
  color: var(--text-secondary);
  margin-bottom: 24px;
  font-weight: 600;
  font-size: 15px;
}

.field-hint {
  font-size: 13px;
  color: var(--text-light);
  margin-top: 8px;
  font-weight: 600;
}

@media (max-width: 800px) {
  .layout-grid { grid-template-columns: 1fr; }
  .task-h1 { font-size: 28px; }
  .panel-card { padding: 32px; }
}
</style>
