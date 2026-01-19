<template>
  <div class="task-manage">
    <div class="page-header">
      <h1 class="page-title">任务管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        发布任务
      </el-button>
    </div>

    <div class="filters">
      <el-radio-group v-model="statusFilter" @change="loadTasks" size="large">
        <el-radio-button :value="-1">全部</el-radio-button>
        <el-radio-button :value="0">待认领</el-radio-button>
        <el-radio-button :value="1">待审核</el-radio-button>
        <el-radio-button :value="2">进行中</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="tasks" v-loading="loading" stripe :row-class-name="getRowClassName">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="任务名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="viewTask(row.id)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="reward" label="积分" width="100">
        <template #default="{ row }">
          <span class="reward">{{ row.reward }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止日期" width="160">
        <template #default="{ row }">
          <span :class="{ 'expired-date': isExpired(row) }">
            {{ formatDate(row.deadline) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status, row.deadline)">
            {{ getStatusText(row.status, row.deadline) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" class-name="action-column">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewTask(row.id)" class="detail-btn">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="showCreateDialog" title="发布新任务" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="任务名称" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="悬赏积分" prop="reward">
          <el-input-number v-model="createForm.reward" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="createForm.deadline"
            type="date"
            placeholder="选择截止日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disablePastDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="任务描述">
          <div class="editor-container">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="simple"
            />
            <Editor
              style="height: 250px; overflow-y: hidden"
              v-model="createForm.content"
              :defaultConfig="{ placeholder: '请输入任务描述...' }"
              mode="simple"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitting">
          发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminTasks, createTask, Task } from '@/api/task'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const router = useRouter()
const loading = ref(false)
const tasks = ref<Task[]>([])
const statusFilter = ref(-1)
const showCreateDialog = ref(false)
const submitting = ref(false)
const createFormRef = ref()
const editorRef = shallowRef()

const toolbarConfig = {
  excludeKeys: [
    'uploadImage',
    'insertImage',
    'deleteImage',
    'editImage',
    'viewImageLink',
    'imageWidth30',
    'imageWidth50',
    'imageWidth100',
    'divider',
    'emotion',
    'insertVideo',
    'uploadVideo',
    'codeBlock',
    'group-image',
    'group-video'
  ]
}

const createForm = reactive({
  title: '',
  content: '',
  deadline: '',
  reward: 100
})

const createRules = {
  title: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  reward: [{ required: true, message: '请设置悬赏积分', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }]
}

onMounted(async () => {
  await loadTasks()
})

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) editor.destroy()
})

function handleCreated(editor: any) {
  editorRef.value = editor
}

async function loadTasks() {
  loading.value = true
  try {
    const status = statusFilter.value === -1 ? undefined : statusFilter.value
    const res = await getAdminTasks(status)
    tasks.value = sortByDeadline((res as any).data || [])
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createTask({
      title: createForm.title,
      content: createForm.content,
      deadline: createForm.deadline,
      reward: createForm.reward
    })
    ElMessage.success('任务发布成功')
    showCreateDialog.value = false
    createForm.title = ''
    createForm.content = ''
    createForm.deadline = ''
    createForm.reward = 100
    await loadTasks()
  } finally {
    submitting.value = false
  }
}

function viewTask(taskId: number) {
  router.push(`/admin/tasks/${taskId}`)
}

function disablePastDate(date: Date) {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function isExpired(task: Task) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(task.deadline)
  deadline.setHours(0, 0, 0, 0)
  return deadline < today && task.status !== 3
}

function getStatusText(status: number, deadline?: string) {
  if (deadline && isExpired({ deadline, status } as Task)) {
    return '已过期'
  }
  const texts: Record<number, string> = { 0: '待认领', 1: '待审核', 2: '进行中', 3: '已完成' }
  return texts[status] || '未知'
}

function getStatusType(status: number, deadline?: string) {
  if (deadline && isExpired({ deadline, status } as Task)) {
    return 'danger'
  }
  const types: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return types[status] || 'info'
}

function getRowClassName({ row }: { row: Task }) {
  return isExpired(row) ? 'expired-row' : ''
}

function sortByDeadline(list: Task[]) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return [...list].sort((a, b) => {
    const aCompleted = a.status === 3
    const bCompleted = b.status === 3
    const aExpired = new Date(a.deadline) < today && a.status !== 3
    const bExpired = new Date(b.deadline) < today && b.status !== 3
    const aIsEnd = aCompleted || aExpired
    const bIsEnd = bCompleted || bExpired
    if (aIsEnd !== bIsEnd) return aIsEnd ? 1 : -1
    return new Date(a.deadline).getTime() - new Date(b.deadline).getTime()
  })
}
</script>

<style scoped>
.task-manage {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 53px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.filters {
  margin-bottom: 32px;
}

.reward {
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  color: #f59e0b;
  font-size: 21px;
}

.editor-container {
  border: 1px solid #ccc;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  font-family: 'Nunito', sans-serif;
  border: 1px solid #E5E7EB;
}

:deep(.el-table th) {
  background: #F9FAFB !important;
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
  color: #374151;
  font-size: 20px;
}

:deep(.el-table td) {
  font-size: 20px;
  font-weight: 500;
}

/* 修复固定列操作按钮样式 */
:deep(.el-table__fixed-right .el-table__cell) {
  background-color: inherit !important;
}

/* 按钮悬停效果 */
.detail-btn:hover {
  background: rgba(37, 99, 235, 0.08) !important;
  transform: translateY(-1px) !important;
}

:deep(.el-radio-button__inner) {
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
  padding: 12px 24px;
  font-size: 20px;
}

:deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: var(--primary-gradient) !important;
  border-color: var(--primary-color) !important;
}

.detail-btn {
  font-weight: 700 !important;
  font-family: 'Fredoka', sans-serif !important;
  font-size: 14px !important;
  padding: 6px 16px !important;
  border-radius: 8px !important;
  background: transparent !important;
  color: #2563EB !important;
  transition: all 0.3s ease !important;
  box-shadow: none !important; /* 移除全局 primary 按钮的红色阴影 */
  border: none !important; /* 确保无边框 */
}

.expired-date {
  color: #DC2626;
  font-weight: 800;
}

:deep(.expired-row) {
  background-color: #FEF2F2 !important;
  opacity: 0.8;
}

:deep(.expired-row:hover) {
  background-color: #FEE2E2 !important;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .page-title {
    font-size: 43px;
  }
}
</style>
