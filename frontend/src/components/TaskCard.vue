<template>
  <div class="task-card" :class="{ 'is-breathing': status.value === 'active' }">
    <div class="task-bar" :class="status.class"></div>
    <div class="task-header-row">
      <span class="task-id">T-{{ task.id }}</span>
      <span class="task-points" :class="{'done-bg': status.value === 'done'}">{{ task.reward }} 积分</span>
    </div>
    <div class="task-name">{{ task.title }}</div>
    
    <div class="task-tags">
      <span v-for="(tag, i) in tags" :key="i" :class="['chip', tag.class]">
        {{ tag.label }}
      </span>
    </div>
    
    <div class="task-footer">
      <div class="deadline">{{ status.value === 'done' ? '已完成' : task.deadline }}</div>
      <div class="avatar">{{ task.memberNames?.[0]?.[0] || '?' }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Task } from '@/api/task'
// ⚠️ 关键点：必须引入 deriveTags
import { getStatusConfig, deriveTags } from '@/utils/format'

const props = defineProps<{ task: Task }>()

const status = computed(() => getStatusConfig(props.task.status))

// ⚠️ 关键点：使用 computed 自动计算标签
const tags = computed(() => deriveTags(props.task))
</script>

<style scoped>
/* 局部样式微调 */
.done-bg { background: #065F46 !important; color: #34D399 !important; box-shadow: none; }
</style>