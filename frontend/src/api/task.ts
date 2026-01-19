import api from './index'

export interface Task {
  id: number
  title: string
  content: string
  deadline: string
  reward: number
  status: number
  createdAt: string
  memberNames?: string[]
}

export interface TaskMember {
  id: number
  memberName: string
  memberEmail: string
  earnedPoints: number
  status: number
  statusText: string
  createdAt: string
}

export interface TaskDetail extends Task {
  statusText: string
  members: TaskMember[]
}

export interface LeaderboardEntry {
  memberEmail: string
  memberName: string
  totalPoints: number
  completedTasks: number
  rank: number
}

export interface ClaimData {
  memberName: string
  memberEmail: string
  deadline: string
  expectedPoints: number
  title?: string
  content?: string
}

// 公开接口
export function getPublicTasks() {
  return api.get<Task[]>('/public/tasks')
}

export function getTaskDetail(taskId: number) {
  return api.get<TaskDetail>(`/public/tasks/${taskId}`)
}

export function claimTask(taskId: number, data: ClaimData) {
  return api.post(`/public/tasks/${taskId}/claim`, data)
}

export function updateClaim(taskId: number, data: ClaimData) {
  return api.put(`/public/tasks/${taskId}/claim`, data)
}

export function getLeaderboard(limit = 100) {
  return api.get<LeaderboardEntry[]>('/public/leaderboard', { params: { limit } })
}

// 管理员接口
export function createTask(data: {
  title: string
  content: string
  deadline: string
  reward: number
}) {
  return api.post('/admin/tasks', data)
}

export function getAdminTasks(status?: number) {
  return api.get<Task[]>('/admin/tasks', { params: { status } })
}

export function getAdminTaskDetail(taskId: number) {
  return api.get<TaskDetail>(`/admin/tasks/${taskId}`)
}

export function approveMember(taskId: number, memberId: number) {
  return api.post(`/admin/tasks/${taskId}/members/${memberId}/approve`)
}

export function approveMemberOnly(taskId: number, memberId: number) {
  return api.post(`/admin/tasks/${taskId}/members/${memberId}/approve-only`)
}

export function startTask(taskId: number) {
  return api.post(`/admin/tasks/${taskId}/start`)
}

export function rejectMember(taskId: number, memberId: number) {
  return api.post(`/admin/tasks/${taskId}/members/${memberId}/reject`)
}

export function removeMember(taskId: number, memberId: number) {
  return api.delete(`/admin/tasks/${taskId}/members/${memberId}`)
}

export function assignMember(taskId: number, data: {
  memberName: string
  memberEmail: string
  earnedPoints?: number
  title?: string
  content?: string
  deadline?: string
}) {
  return api.post(`/admin/tasks/${taskId}/assign`, { ...data, expectedPoints: data.earnedPoints })
}

export function completeTask(taskId: number) {
  return api.post(`/admin/tasks/${taskId}/complete`)
}
