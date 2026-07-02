import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/layouts/AppLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('@/views/ForbiddenView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: 'manage/dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { roles: ['ADMIN'] },
      },
      {
        path: 'manage/passwords',
        name: 'passwords',
        component: () => import('@/views/PasswordsView.vue'),
        meta: { roles: ['ADMIN', 'DEPT_MANAGER'] },
      },
      {
        path: 'manage/users',
        name: 'users',
        component: () => import('@/views/UsersView.vue'),
        meta: { roles: ['ADMIN', 'DEPT_MANAGER'] },
      },
      {
        path: 'manage/users/:id',
        name: 'user-detail',
        component: () => import('@/views/UserDetailView.vue'),
        meta: { roles: ['ADMIN', 'DEPT_MANAGER'] },
      },
      {
        path: 'manage/departments',
        name: 'departments',
        component: () => import('@/views/DepartmentsView.vue'),
        meta: { roles: ['ADMIN'] },
      },
      {
        path: 'manage/permissions',
        name: 'permissions',
        component: () => import('@/views/PermissionsView.vue'),
        meta: { roles: ['ADMIN'] },
      },
      {
        path: 'manage/audit-logs',
        name: 'audit-logs',
        component: () => import('@/views/AuditLogsView.vue'),
        meta: { roles: ['ADMIN'] },
      },
      {
        path: 'portal',
        name: 'portal',
        component: () => import('@/views/PortalView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    return true
  }

  if (!auth.isLoggedIn) {
    return { path: '/login' }
  }

  if (to.path === '/') {
    return { path: auth.homePath }
  }

  const requiredRoles = to.meta.roles
  if (!requiredRoles) {
    return true
  }

  if (auth.isAdmin) {
    return true
  }

  if (requiredRoles.some((role) => auth.hasRole(role))) {
    return true
  }

  return { path: '/403' }
})

export default router
