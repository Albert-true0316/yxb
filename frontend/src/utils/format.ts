// src/utils/format.ts
import type { Task } from '@/api/task'

// 状态映射表
export const STATUS_MAP: Record<number, { text: string; value: string; class: string }> = {
  0: { text: '待认领', value: 'todo', class: 'bar-todo' },
  1: { text: '进行中', value: 'active', class: 'bar-active' },
  2: { text: '审核中', value: 'review', class: 'bar-review' },
  3: { text: '已完成', value: 'done', class: 'bar-done' }
}

export const getStatusConfig = (status: number) => {
  return STATUS_MAP[status] || STATUS_MAP[0]
}

/**
 * 🏷️ 核心逻辑：根据任务标题和内容自动生成标签
 */
export const deriveTags = (task: Task) => {
  if (!task) return []
  
  const tags = []
  // 将标题和内容合并转为小写，方便关键词匹配
  const text = ((task.title || '') + (task.content || '')).toLowerCase()
  
  // --- 关键词匹配规则 ---
  if (text.includes('ai') || text.includes('模型') || text.includes('算法') || text.includes('python')) 
    tags.push({ label: 'AI 编程', class: 'tag-ai' })
  
  if (text.includes('数据') || text.includes('清洗') || text.includes('分析') || text.includes('excel')) 
    tags.push({ label: '数据分析', class: 'tag-data' })
  
  if (text.includes('ppt') || text.includes('视频') || text.includes('剪辑') || text.includes('海报') || text.includes('设计')) 
    tags.push({ label: '多媒体', class: 'tag-media' })
  
  if (text.includes('论文') || text.includes('综述') || text.includes('写作') || text.includes('文献')) 
    tags.push({ label: '学术写作', class: 'tag-paper' })

  // --- 兜底规则 ---
  // 如果一个标签都没匹配到，给一个默认标签，防止 UI 空白
  if (tags.length === 0) {
    tags.push({ label: '综合事务', class: 'tag-misc' })
  }
  
  return tags
}