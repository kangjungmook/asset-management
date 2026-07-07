<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { refreshTokens } from '@/api/tokenRefresh'
import { decodeJwtExpiryMs } from '@/utils/jwt'

const WARNING_LEAD_MS = 10 * 60 * 1000 // show the warning 10 minutes before expiry

const auth = useAuthStore()
const ui = useUiStore()

let warningTimer = null

function clearTimer() {
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }
}

function scheduleWarning() {
  clearTimer()
  ui.sessionExpiryVisible = false

  if (!auth.refreshToken) return
  const expMs = decodeJwtExpiryMs(auth.refreshToken)
  if (!expMs) return

  const delay = expMs - Date.now() - WARNING_LEAD_MS
  if (delay <= 0) {
    ui.sessionExpiryVisible = true
    return
  }
  warningTimer = setTimeout(() => {
    ui.sessionExpiryVisible = true
  }, delay)
}

watch(() => auth.refreshToken, scheduleWarning, { immediate: true })

onBeforeUnmount(clearTimer)

const extending = ref(false)

async function handleExtend() {
  extending.value = true
  try {
    await refreshTokens()
    ui.sessionExpiryVisible = false
    ui.success('로그인이 연장되었습니다.')
  } catch {
    ui.error('로그인 연장에 실패했습니다. 다시 로그인해주세요.')
  } finally {
    extending.value = false
  }
}

function handleDismiss() {
  ui.sessionExpiryVisible = false
}
</script>

<template>
  <div v-if="ui.sessionExpiryVisible" class="modal-overlay" @click.self="handleDismiss">
    <div class="modal-panel" style="max-width: 400px">
      <h3 style="margin-bottom: var(--space-3)">곧 자동으로 로그아웃됩니다</h3>
      <p class="text-muted" style="font-size: var(--text-sm); margin-bottom: var(--space-6)">
        보안을 위해 일정 시간 동안 활동이 없으면 자동으로 로그아웃됩니다. 계속 사용하시려면 로그인 상태를 연장해주세요.
      </p>
      <div class="form-actions" style="justify-content: flex-end">
        <button class="btn btn-ghost" @click="handleDismiss">닫기</button>
        <button class="btn btn-primary" :disabled="extending" @click="handleExtend">로그인 연장하기</button>
      </div>
    </div>
  </div>
</template>
