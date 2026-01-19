<template>
  <div class="lb-overlay" :class="{ active: visible }">
    <div class="lb-container-inner">
      <div class="lb-header">
        <div class="lb-back-btn" @click="$emit('close')">← 返回控制台</div>
        <div class="lb-title-lg">H.E.R.O. PROTOCOL</div>
      </div>
      
      <div class="lb-content">
        <div class="lb-table-section">
           <div v-for="(user, idx) in leaders" :key="idx" class="lb-list-item" :class="{top3: idx < 3}">
              <div class="lb-rank">{{ idx + 1 }}</div>
              <div class="lb-agent">
                 <div class="avatar">{{ user.memberName[0] }}</div>
                 <div class="name" style="margin-left:10px;">{{ user.memberName }}</div>
              </div>
              <div class="lb-score">{{ user.totalPoints }}</div>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { LeaderboardEntry } from '@/api/task'
defineProps<{ visible: boolean; leaders: LeaderboardEntry[] }>()
defineEmits(['close'])
</script>