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
    path: '/change-password',
    name: 'change-password',
    component: () => import('@/views/ChangePasswordView.vue'),
    meta: { title: '비밀번호 변경' },
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: 'manage/dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { roles: ['ADMIN'], title: '대시보드' },
      },
      {
        path: 'manage/passwords',
        name: 'passwords',
        component: () => import('@/views/PasswordsView.vue'),
        meta: { roles: ['ADMIN', 'DEPT_MANAGER'], title: '패스워드 관리대장' },
      },
      {
        path: 'manage/users',
        name: 'users',
        component: () => import('@/views/UsersView.vue'),
        meta: { roles: ['ADMIN', 'DEPT_MANAGER'], title: '사용자 관리' },
      },
      {
        path: 'manage/users/:id',
        name: 'user-detail',
        component: () => import('@/views/UserDetailView.vue'),
        meta: { roles: ['ADMIN', 'DEPT_MANAGER'], title: '사용자 상세' },
      },
      {
        path: 'manage/departments',
        name: 'departments',
        component: () => import('@/views/DepartmentsView.vue'),
        meta: { roles: ['ADMIN'], title: '부서 관리' },
      },
      {
        path: 'manage/permissions',
        name: 'permissions',
        component: () => import('@/views/PermissionsView.vue'),
        meta: { roles: ['ADMIN'], title: '권한 관리' },
      },
      {
        path: 'manage/account-types',
        name: 'account-types',
        component: () => import('@/views/AccountTypesView.vue'),
        meta: { roles: ['ADMIN'], title: '계정유형 관리' },
      },
      {
        path: 'manage/audit-logs',
        name: 'audit-logs',
        component: () => import('@/views/AuditLogsView.vue'),
        meta: { roles: ['ADMIN'], title: '감시 로그' },
      },
      {
        path: 'manage/system-reset',
        name: 'system-reset',
        component: () => import('@/views/SystemResetView.vue'),
        meta: { roles: ['ADMIN'], title: '시스템 초기화' },
      },
      {
        path: 'portal',
        name: 'portal',
        component: () => import('@/views/PortalView.vue'),
        meta: { title: '내 정보' },
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

  if (auth.mustChangePassword && to.name !== 'change-password') {
    return { path: '/change-password' }
  }

  if (to.name === 'change-password') {
    return true
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
