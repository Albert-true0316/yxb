<template>
  <div class="public-layout">
    <header class="cyber-header">
      <div class="header-content">
        <div class="brand-section" @click="$router.push('/')">
          <div class="brand-logo">IMHIC</div>
          <div class="brand-divider">//</div>
          <div class="brand-name">心理健康和智能计算研究院</div>
        </div>

        <nav class="header-nav">
          <div class="status-item">
            <span class="label">SYSTEM:</span>
            <span class="value">ONLINE</span>
            <span class="blink-dot"></span>
          </div>
          <div class="status-item">
            <span class="label">AGENT:</span>
            <span class="value">英雄</span>
          </div>
        </nav>
      </div>
      <div class="header-scanline"></div>
    </header>

    <main class="main-viewport">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <div class="global-scanline"></div>
  </div>
</template>

<style scoped>
.public-layout {
  min-height: 100vh;
  background-color: #0B101A;
  color: #E2E8F0;
  font-family: 'JetBrains Mono', monospace;
  position: relative;
  overflow: hidden;
}

.cyber-header {
  height: 64px;
  background: rgba(23, 31, 46, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(56, 189, 248, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.brand-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.brand-logo {
  font-size: 24px;
  font-weight: 800;
  color: #38BDF8;
  letter-spacing: 2px;
}

.brand-divider {
  color: #64748B;
  font-weight: 300;
}

.brand-name {
  font-size: 14px;
  color: #94A3B8;
  letter-spacing: 1px;
}

.header-nav {
  display: flex;
  gap: 32px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.status-item .label {
  color: #64748B;
}

.status-item .value {
  color: #38BDF8;
  font-weight: 700;
}

.blink-dot {
  width: 6px;
  height: 6px;
  background: #38BDF8;
  border-radius: 50%;
  animation: blink 1s infinite;
  box-shadow: 0 0 8px #38BDF8;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.main-viewport {
  padding-top: 20px;
}

/* 页面切换动画 */
.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity 0.3s ease;
}
.page-fade-enter-from, .page-fade-leave-to {
  opacity: 0;
}
</style>