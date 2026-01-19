import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/views/HeroBoard.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HeroBoard.vue')
      },
      {
        path: 'task/:id',
        name: 'TaskDetail',
        component: () => import('@/views/HeroBoard.vue')
      },
      {
        path: 'leaderboard',
        name: 'Leaderboard',
        component: () => import('@/views/HeroBoard.vue')
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue')
  },
  {
    path: '/admin/register',
    name: 'AdminRegister',
    component: () => import('@/views/admin/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'tasks',
        name: 'AdminTasks',
        component: () => import('@/views/admin/TaskManage.vue')
      },
      {
        path: 'tasks/:id',
        name: 'AdminTaskDetail',
        component: () => import('@/views/admin/TaskDetailAdmin.vue')
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('@/views/admin/TaskReview.vue')
      },
      {
        path: 'email',
        name: 'EmailSettings',
        component: () => import('@/views/admin/EmailSettings.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/admin/login')
  } else {
    next()
  }
})

export default router
