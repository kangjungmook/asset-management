<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { refreshTokens } from '@/api/tokenRefresh'
import { decodeJwtExpiryMs } from '@/utils/jwt'

const WARNING_LEAD_MS = 10 * 60 * 1000 // show the warning 10 minutes before expiry

const auth = useAuthStore()
const ui = useUiStore()

let warningTimer = null
let countdownTimer = null
const remainingMs = ref(null)

function clearTimer() {
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function updateRemaining() {
  const expMs = auth.refreshToken ? decodeJwtExpiryMs(auth.refreshToken) : null
  remainingMs.value = expMs ? Math.max(0, expMs - Date.now()) : null
}

function scheduleWarning() {
  clearTimer()
  ui.sessionExpiryVisible = false

  if (!auth.refreshToken) return
  const expMs = decodeJwtExpiryMs(auth.refreshToken)
  if (!expMs) return

  const delay = expMs - Date.now() - WARNING_LEAD_MS
  if (delay <= 0) {
    showWarning()
    return
  }
  warningTimer = setTimeout(showWarning, delay)
}

function showWarning() {
  ui.sessionExpiryVisible = true
  updateRemaining()
  countdownTimer = setInterval(updateRemaining, 1000)
}

watch(() => auth.refreshToken, scheduleWarning, { immediate: true })

onBeforeUnmount(clearTimer)

const remainingLabel = computed(() => {
  if (remainingMs.value == null) return ''
  const totalSec = Math.floor(remainingMs.value / 1000)
  const min = Math.floor(totalSec / 60)
  const sec = totalSec % 60
  return `${min}분 ${String(sec).padStart(2, '0')}초`
})

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
    <div class="modal-panel">
      <div class="modal-panel__lead">
        <div class="modal-panel__icon" style="background: var(--color-warning-tint); color: var(--color-warning)">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 3" /></svg>
        </div>
        <div>
          <h3>곧 자동으로 로그아웃됩니다</h3>
          <p class="text-muted" style="font-size: var(--text-xs); margin-top: 2px">
            세션 만료까지 <span style="font-family: var(--font-mono); font-weight: 700; color: var(--color-warning)">{{ remainingLabel }}</span>
          </p>
        </div>
      </div>
      <p class="text-muted" style="font-size: var(--text-sm); margin-top: var(--space-4); margin-bottom: var(--space-6)">
        보안을 위해 일정 시간 동안 활동이 없으면 자동으로 로그아웃됩니다. 계속 사용하시려면 로그인 상태를 연장해주세요.
      </p>
      <div class="form-actions">
        <button class="btn btn-ghost" @click="handleDismiss">닫기</button>
        <button class="btn btn-primary" style="flex: 1" :disabled="extending" @click="handleExtend">로그인 연장하기</button>
      </div>
    </div>
  </div>
</template>
