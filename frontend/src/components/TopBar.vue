<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { refreshTokens } from '@/api/tokenRefresh'
import { decodeJwtExpiryMs } from '@/utils/jwt'

defineProps({
  roleLabel: { type: String, required: true },
})

const auth = useAuthStore()
const ui = useUiStore()
const initial = computed(() => auth.name?.charAt(0) || '?')

const menuOpen = ref(false)
const menuRoot = ref(null)

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

function handleOutsideClick(e) {
  if (menuRoot.value && !menuRoot.value.contains(e.target)) {
    closeMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})

const remainingMs = ref(null)
const extending = ref(false)
let countdownTimer = null

function updateRemaining() {
  const expMs = auth.refreshToken ? decodeJwtExpiryMs(auth.refreshToken) : null
  remainingMs.value = expMs ? Math.max(0, expMs - Date.now()) : null
}

watch(() => auth.refreshToken, updateRemaining, { immediate: true })

onMounted(() => {
  countdownTimer = setInterval(updateRemaining, 1000)
})

onBeforeUnmount(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const remainingLabel = computed(() => {
  if (remainingMs.value == null) return ''
  const totalSec = Math.floor(remainingMs.value / 1000)
  const min = Math.floor(totalSec / 60)
  const sec = totalSec % 60
  return `${min}분 ${String(sec).padStart(2, '0')}초`
})

async function handleExtend() {
  extending.value = true
  try {
    await refreshTokens()
    ui.success('로그인이 연장되었습니다.')
  } catch {
    ui.error('로그인 연장에 실패했습니다.')
  } finally {
    extending.value = false
  }
}
</script>

<template>
  <div class="topbar">
    <div class="topbar__brand">
      <div class="topbar__brand-mark">
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round">
          <path d="M12 3l8 4-8 4-8-4 8-4z" />
          <path d="M4 12l8 4 8-4" />
        </svg>
      </div>
      <span>AMS</span>
    </div>

    <div class="topbar__spacer"></div>

    <div v-if="remainingLabel" class="session-timer">
      <span class="session-timer__badge" :title="'자동 로그아웃까지 남은 시간'">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="9" />
          <path d="M12 7v5l3 3" />
        </svg>
        {{ remainingLabel }} 뒤 로그아웃
      </span>
      <button class="btn btn-ghost btn-sm" :disabled="extending" @click="handleExtend">로그인 연장</button>
    </div>

    <div class="topbar__user-menu" ref="menuRoot">
      <button class="topbar__user" @click="toggleMenu">
        <div class="topbar__avatar">{{ initial }}</div>
        <div>
          <div class="topbar__user-name">{{ auth.name }}</div>
          <div class="topbar__user-role">{{ roleLabel }}</div>
        </div>
      </button>

      <div v-if="menuOpen" class="topbar__dropdown">
        <RouterLink to="/change-password" class="topbar__dropdown-item" @click="closeMenu">비밀번호 변경</RouterLink>
      </div>
    </div>
  </div>
</template>
