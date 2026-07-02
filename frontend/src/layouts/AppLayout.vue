<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from './AdminLayout.vue'
import ManagerLayout from './ManagerLayout.vue'
import PortalLayout from './PortalLayout.vue'

const auth = useAuthStore()

const layout = computed(() => {
  if (auth.isAdmin) return AdminLayout
  if (auth.hasRole('DEPT_MANAGER')) return ManagerLayout
  return PortalLayout
})
</script>

<template>
  <component :is="layout" />
</template>
