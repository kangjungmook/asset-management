import { defineStore } from 'pinia'

let toastSeq = 0
let confirmResolver = null
const SIDEBAR_KEY = 'ams.sidebarCollapsed'

export const useUiStore = defineStore('ui', {
  state: () => ({
    toasts: [],
    confirmDialog: null,
    sidebarCollapsed: localStorage.getItem(SIDEBAR_KEY) === '1',
    sessionExpiryVisible: false,
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      localStorage.setItem(SIDEBAR_KEY, this.sidebarCollapsed ? '1' : '0')
    },
    pushToast(message, type = 'brand') {
      const id = ++toastSeq
      this.toasts.push({ id, message, type })
      setTimeout(() => this.dismissToast(id), 3200)
    },
    success(message) {
      this.pushToast(message, 'success')
    },
    error(message) {
      this.pushToast(message, 'danger')
    },
    dismissToast(id) {
      this.toasts = this.toasts.filter((t) => t.id !== id)
    },
    confirm(options) {
      const config = typeof options === 'string' ? { message: options } : options
      return new Promise((resolve) => {
        confirmResolver = resolve
        this.confirmDialog = {
          title: config.title || '이 작업을 진행할까요?',
          message: config.message || '',
          confirmText: config.confirmText || '진행하기',
          cancelText: config.cancelText || '취소하기',
          danger: !!config.danger,
        }
      })
    },
    resolveConfirm(result) {
      this.confirmDialog = null
      if (confirmResolver) {
        confirmResolver(result)
        confirmResolver = null
      }
    },
  },
})
