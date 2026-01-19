<template>
  <Teleport to="body">
    <div class="modal-overlay" :class="{ active: modelValue }" @click.self="close">
      <div class="modal-card" v-if="task">
        <div class="modal-header">
          <div class="modal-title">任务详情 #{{ task.id }}</div>
          <div class="modal-close" @click="close">×</div>
        </div>
        
        <div class="modal-body">
          <h2 style="color:#fff; margin-bottom:20px;">{{ task.title }}</h2>
          <div class="modal-desc-box" v-html="formatContent(task.content)"></div>
          
          <div v-if="canClaim" style="margin-top:20px;">
            <div class="modal-label">认领人信息</div>
            <div style="display:flex; gap:10px; margin-top:10px;">
              <input v-model="form.name" class="cyber-input" placeholder="姓名" />
              <input v-model="form.email" class="cyber-input" placeholder="邮箱" />
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-cancel" @click="close">关闭</button>
          <button v-if="canClaim" class="btn btn-confirm" @click="submitClaim" :disabled="submitting">
            {{ submitting ? '提交中...' : '立即认领' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import type { Task } from '@/api/task'
import { claimTask } from '@/api/task'
import { ElMessage } from 'element-plus'
import { getStatusConfig } from '@/utils/format'

// 1. 允许 task 为 null
const props = defineProps<{ modelValue: boolean; task: Task | null }>()
const emit = defineEmits(['update:modelValue', 'refresh', 'closed'])

const form = reactive({ name: '', email: '' })
const submitting = ref(false)

// 2. 关键修复：先判断 props.task 是否存在，再读取 status
const canClaim = computed(() => {
  if (!props.task) return false
  return getStatusConfig(props.task.status).value === 'todo'
})

const formatContent = (content: string) => {
  return content ? content.replace(/\n/g, '<br>') : ''
}

const close = () => {
  emit('update:modelValue', false)
  emit('closed')
}

const submitClaim = async () => {
  if (!props.task) return
  if (!form.name || !form.email) return ElMessage.warning('请补全信息')
  
  submitting.value = true
  try {
    await claimTask(props.task.id, {
        memberName: form.name,
        memberEmail: form.email,
        deadline: props.task.deadline,
        expectedPoints: props.task.reward
    })
    ElMessage.success('认领成功')
    emit('refresh')
    close()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.85); backdrop-filter: blur(10px); z-index: 3000; display: none; justify-content: center; align-items: center; }
.modal-overlay.active { display: flex; animation: fadeIn 0.3s ease; }
.modal-card { background: #111827; border: 1px solid #38BDF8; width: 600px; max-width: 90%; border-radius: 20px; box-shadow: 0 0 60px rgba(56, 189, 248, 0.25); padding: 0; overflow: hidden; }
.modal-header { padding: 25px; background: rgba(56, 189, 248, 0.1); display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(56, 189, 248, 0.2); }
.modal-title { font-size: 20px; font-weight: 700; color: #fff; }
.modal-close { cursor: pointer; font-size: 28px; color: #94A3B8; }
.modal-body { padding: 30px; }
.modal-label { font-size: 13px; color: #38BDF8; font-weight: 700; margin-bottom: 8px; display: block; letter-spacing: 1px; }
.modal-desc-box { background: rgba(255,255,255,0.05); padding: 20px; border-radius: 10px; font-size: 16px; line-height: 1.7; color: #cbd5e1; border: 1px solid rgba(255,255,255,0.1); }
.modal-actions { padding: 20px 30px; border-top: 1px solid rgba(255,255,255,0.1); display: flex; justify-content: flex-end; gap: 15px; }
.btn { padding: 12px 24px; border-radius: 8px; border: none; cursor: pointer; font-weight: 700; font-size: 15px; }
.btn-cancel { background: transparent; color: #94A3B8; border: 1px solid #334155; }
.btn-confirm { background: #38BDF8; color: #0F172A; }
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }
.cyber-input { background: rgba(255,255,255,0.05); border: 1px solid #38BDF8; color: #fff; padding: 10px; border-radius: 4px; flex: 1; }
</style>