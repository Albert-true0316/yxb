<template>
  <div class="email-settings">
    <h2>邮件配置</h2>

    <div class="config-section">
      <h3>邮箱账号配置</h3>
      <form @submit.prevent="saveEmailConfig">
        <div class="form-group">
          <label>邮箱账号</label>
          <input v-model="emailConfig.mailUsername" type="email" required />
        </div>
        <div class="form-group">
          <label>邮箱密码/授权码</label>
          <input v-model="emailConfig.mailPassword" type="password" required />
        </div>
        <button type="submit" class="btn-primary">保存配置</button>
        <button type="button" @click="testEmail" class="btn-secondary">测试发送</button>
      </form>
    </div>

    <div class="rules-section">
      <h3>提醒规则配置</h3>
      <button @click="showAddRule = true" class="btn-primary">添加规则</button>

      <div v-if="showAddRule" class="rule-form">
        <h4>新增提醒规则</h4>
        <form @submit.prevent="saveRule">
          <div class="form-group">
            <label>提前天数 (0表示当天)</label>
            <input v-model.number="newRule.daysBefore" type="number" min="0" required />
          </div>

          <div class="form-group">
            <label>发送时间 (小时 0-23)</label>
            <input v-model.number="newRule.sendHour" type="number" min="0" max="23" required />
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-primary">保存</button>
            <button type="button" @click="showAddRule = false" class="btn-secondary">取消</button>
          </div>
        </form>
      </div>

      <div class="rules-list">
        <div v-for="rule in rules" :key="rule.id" class="rule-item">
          <div class="rule-info">
            <span class="rule-type">{{ getRuleTypeText(rule) }}</span>
            <span class="rule-time">{{ rule.sendHour }}:00 发送</span>
          </div>
          <button @click="deleteRule(rule.id)" class="btn-danger">删除</button>
        </div>
      </div>
    </div>

    <div v-if="testEmailDialog" class="modal" @click.self="testEmailDialog = false">
      <div class="modal-content" @click.stop>
        <h3>测试邮件发送</h3>
        <input
          v-model="testEmailAddress"
          type="email"
          placeholder="输入测试邮箱"
          @click.stop
          @keydown.stop
        />
        <div class="modal-actions">
          <button @click="sendTestEmail" class="btn-primary">发送</button>
          <button @click="testEmailDialog = false" class="btn-secondary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'

const emailConfig = ref({
  mailUsername: '',
  mailPassword: ''
})

const newRule = ref({
  daysBefore: 0,
  sendHour: 9
})

const rules = ref([])
const showAddRule = ref(false)
const testEmailDialog = ref(false)
const testEmailAddress = ref('')

const loadEmailConfig = async () => {
  try {
    const res = await api.get('/admin/email/config')
    if (res.data.data) {
      emailConfig.value = res.data.data
    }
  } catch (error) {
    console.error('加载邮件配置失败', error)
  }
}

const loadRules = async () => {
  try {
    const res = await api.get('/admin/email/rules')
    rules.value = res.data.data || []
  } catch (error) {
    console.error('加载提醒规则失败', error)
  }
}

const saveEmailConfig = async () => {
  try {
    await api.post('/admin/email/config', emailConfig.value)
    alert('邮件配置保存成功')
  } catch (error: any) {
    alert('保存失败: ' + (error.response?.data?.message || error.message))
  }
}

const testEmail = () => {
  testEmailDialog.value = true
}

const sendTestEmail = async () => {
  try {
    await api.post('/admin/email/test', { toEmail: testEmailAddress.value })
    alert('测试邮件发送成功')
    testEmailDialog.value = false
  } catch (error: any) {
    alert('发送失败: ' + (error.response?.data?.message || error.message))
  }
}

const saveRule = async () => {
  try {
    await api.post('/admin/email/rules', newRule.value)
    alert('提醒规则保存成功')
    showAddRule.value = false
    newRule.value = { daysBefore: 0, sendHour: 9 }
    loadRules()
  } catch (error: any) {
    alert('保存失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteRule = async (ruleId: number) => {
  if (!confirm('确定删除此规则?')) return
  try {
    await api.delete(`/admin/email/rules/${ruleId}`)
    alert('删除成功')
    loadRules()
  } catch (error: any) {
    alert('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

const getRuleTypeText = (rule: any) => {
  if (rule.daysBefore === 0) return '截止当天'
  return `提前${rule.daysBefore}天`
}

onMounted(() => {
  loadEmailConfig()
  loadRules()
})
</script>

<style scoped>
.email-settings {
  padding: 20px;
}

.config-section, .rules-section {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-primary, .btn-secondary, .btn-danger {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}

.btn-primary {
  background: #4CAF50;
  color: white;
}

.btn-secondary {
  background: #2196F3;
  color: white;
}

.btn-danger {
  background: #f44336;
  color: white;
}

.rule-form {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.rules-list {
  margin-top: 20px;
}

.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 10px;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-width: 300px;
  z-index: 10000;
}

.modal-content input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 10px 0;
  box-sizing: border-box;
}

.modal-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}
</style>
