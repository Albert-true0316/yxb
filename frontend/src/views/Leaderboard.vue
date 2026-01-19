<template>
  <div class="leaderboard-page">
    <div class="leaderboard container">
      <section class="hero">
        <div class="hero-badge">Hero Quest Board</div>
        <h1 class="page-title">英雄排行榜</h1>
        <p class="page-subtitle">以积分排序的任务荣耀榜单，积累越多，名次越靠前。</p>
        <div class="hero-stats">
          <div class="stat-pill">
            <span class="stat-label">上榜人数</span>
            <span class="stat-value">{{ totalMembers }}</span>
          </div>
          <div class="stat-pill" v-if="topPoints > 0">
            <span class="stat-label">当前最高积分</span>
            <span class="stat-value">{{ topPoints }}</span>
          </div>
        </div>
      </section>

      <section class="board" v-loading="loading">
        <div class="board-head">
          <div>
            <h2 class="board-title">当前排行</h2>
            <p class="board-subtitle">按积分从高到低排列</p>
          </div>
          <div class="board-legend">
            <span class="legend-dot"></span>
            实时更新
          </div>
        </div>

        <div class="board-list" v-if="sortedLeaderboard.length > 0">
          <article
            v-for="(entry, index) in sortedLeaderboard"
            :key="entry.memberEmail"
            class="board-row"
          >
            <div class="rank-pill">{{ index + 1 }}</div>
            <div class="member">
              <div class="avatar">{{ entry.memberName.charAt(0) }}</div>
              <div class="member-info">
                <div class="member-name">{{ entry.memberName }}</div>
                <div class="member-meta">完成 {{ entry.completedTasks }} 个任务</div>
              </div>
            </div>
            <div class="points">
              <div class="points-value">{{ entry.totalPoints }}</div>
              <div class="points-label">积分</div>
            </div>
          </article>
        </div>

        <el-empty v-if="!loading && sortedLeaderboard.length === 0" description="暂无排行数据" />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getLeaderboard, LeaderboardEntry } from '@/api/task'

const loading = ref(false)
const leaderboard = ref<LeaderboardEntry[]>([])

const sortedLeaderboard = computed(() =>
  [...leaderboard.value].sort((a, b) => b.totalPoints - a.totalPoints)
)
const totalMembers = computed(() => sortedLeaderboard.value.length)
const topPoints = computed(() => sortedLeaderboard.value[0]?.totalPoints ?? 0)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getLeaderboard(100)
    leaderboard.value = (res as any).data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fredoka:wght@600;700;800&display=swap');

.leaderboard-page {
  background:
    radial-gradient(circle at 20% 15%, rgba(220, 38, 38, 0.12), transparent 45%),
    radial-gradient(circle at 80% 10%, rgba(249, 115, 22, 0.1), transparent 50%),
    radial-gradient(circle at 50% 85%, rgba(234, 88, 12, 0.08), transparent 50%),
    #FEF2F2;
  padding: 64px 0 100px;
}

.leaderboard {
  max-width: 1000px;
  font-family: 'Nunito', sans-serif;
  color: #450A0A;
  position: relative;
}

.hero {
  text-align: center;
  padding: 40px 32px 48px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.85);
  border: 3px solid rgba(220, 38, 38, 0.15);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(220, 38, 38, 0.05), transparent 50%);
  animation: rotate 20s linear infinite;
  pointer-events: none;
  will-change: transform;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  border-radius: 999px;
  background: var(--primary-gradient);
  font-size: 13px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: white;
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.4);
  position: relative;
  z-index: 1;
  animation: badgeBounce 2s infinite;
}

@keyframes badgeBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.page-title {
  font-family: 'Fredoka', serif;
  font-size: 52px;
  font-weight: 800;
  letter-spacing: -1px;
  margin: 24px 0 14px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
  z-index: 1;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 17px;
  font-weight: 600;
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-stats {
  display: flex;
  gap: 20px;
  margin-top: 32px;
  flex-wrap: wrap;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.stat-pill {
  padding: 14px 24px;
  border-radius: 999px;
  background: white;
  border: 2px solid rgba(220, 38, 38, 0.2);
  display: inline-flex;
  gap: 12px;
  align-items: center;
  font-size: 14px;
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-secondary);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.15);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-pill:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 32px rgba(220, 38, 38, 0.25);
}

.stat-value {
  font-weight: 800;
  font-size: 18px;
  color: var(--primary-color);
}

.board {
  margin-top: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 32px;
  padding: 36px;
  box-shadow: var(--shadow-lg);
  border: 3px solid rgba(220, 38, 38, 0.15);
  backdrop-filter: blur(20px);
}

.board-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.board-title {
  font-size: 28px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-primary);
}

.board-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
  margin-top: 4px;
}

.board-legend {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 700;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-secondary);
  padding: 8px 16px;
  background: linear-gradient(135deg, #FEF2F2, #FFF7ED);
  border-radius: 999px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--primary-gradient);
  box-shadow: 0 0 0 8px rgba(220, 38, 38, 0.2);
  animation: pulse 2s infinite;
}

.board-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.board-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  border-radius: 24px;
  background: white;
  border: 3px solid transparent;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 4px 16px rgba(220, 38, 38, 0.1);
}

.board-row:hover {
  transform: translateX(8px) scale(1.02);
  box-shadow: var(--shadow-md);
  border-color: rgba(220, 38, 38, 0.2);
}

.rank-pill {
  min-width: 48px;
  height: 48px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  font-size: 20px;
  color: white;
  background: var(--primary-gradient);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.3);
}

.member {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: var(--accent-gradient);
  color: white;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  font-size: 22px;
  box-shadow: 0 8px 24px rgba(146, 64, 14, 0.3);
}

.member-name {
  font-size: 18px;
  font-weight: 800;
  font-family: 'Fredoka', sans-serif;
  color: var(--text-primary);
}

.member-meta {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
  margin-top: 4px;
}

.points {
  text-align: right;
  min-width: 120px;
}

.points-value {
  font-family: 'Fredoka', sans-serif;
  font-size: 32px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.points-label {
  font-size: 12px;
  color: var(--text-light);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 720px) {
  .leaderboard-page {
    padding: 48px 0 80px;
  }

  .page-title {
    font-size: 36px;
  }

  .board {
    padding: 24px;
  }

  .board-row {
    grid-template-columns: 1fr;
    text-align: left;
    gap: 16px;
  }

  .points {
    text-align: left;
  }

  .rank-pill {
    position: absolute;
    top: 20px;
    right: 20px;
    min-width: 40px;
    height: 40px;
    font-size: 16px;
  }
}
</style>
