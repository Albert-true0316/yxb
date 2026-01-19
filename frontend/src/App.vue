<template>
  <router-view />
</template>

<script setup lang="ts">
</script>
<style>
/* 全局重置：去除浏览器默认边距 */
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden; /* 防止出现双重滚动条 */
}

/* 字体设置 */
body {
  font-family: 'Inter', "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
}
</style>