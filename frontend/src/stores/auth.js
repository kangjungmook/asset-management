import { defineStore } from 'pinia'

const STORAGE_KEY = 'ams.auth'

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function emptyState() {
  return {
    userId: null,
    name: '',
    employeeNo: '',
    isAdmin: false,
    dept: null, // { deptId, deptName }
    roles: [],
    permissions: [],
    accessToken: '',
    refreshToken: '',
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const saved = loadFromStorage()
    return saved ? { ...emptyState(), ...saved } : emptyState()
  },
  getters: {
    isLoggedIn: (state) => !!state.accessToken,
    hasRole: (state) => (role) => state.roles.includes(role),
    hasPermission: (state) => (permCode) => state.permissions.includes(permCode),
    homePath: (state) => {
      if (state.isAdmin) return '/manage/dashboard'
      if (state.roles.includes('DEPT_MANAGER')) return '/manage/users'
      return '/portal'
    },
  },
  actions: {
    setFromLogin(data) {
      this.userId = data.userId
      this.name = data.name
      this.employeeNo = data.employeeNo
      this.isAdmin = !!data.isAdmin
      this.dept = data.deptId ? { deptId: data.deptId, deptName: data.deptName } : null
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.persist()
    },
    setAccessToken(token) {
      this.accessToken = token
      this.persist()
    },
    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
    },
    clear() {
      Object.assign(this, emptyState())
      localStorage.removeItem(STORAGE_KEY)
    },
  },
})
