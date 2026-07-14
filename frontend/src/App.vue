<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { refreshTokens } from '@/api/tokenRefresh'
import ToastStack from '@/components/ToastStack.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import SessionExpiryModal from '@/components/SessionExpiryModal.vue'

const auth = useAuthStore()
const route = useRoute()

const ACTIVITY_EXTEND_THROTTLE_MS = 5 * 60 * 1000
let lastActivityExtend = 0

function handleActivity() {
  if (route.meta.public) return
  if (!auth.isLoggedIn) return
  const now = Date.now()
  if (now - lastActivityExtend < ACTIVITY_EXTEND_THROTTLE_MS) return
  lastActivityExtend = now
  refreshTokens().catch(() => {})
}

onMounted(() => {
  document.addEventListener('click', handleActivity)
  document.addEventListener('keydown', handleActivity)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleActivity)
  document.removeEventListener('keydown', handleActivity)
})
</script>

<template>
  <RouterView />
  <ToastStack />
  <ConfirmDialog />
  <SessionExpiryModal />
</template>
