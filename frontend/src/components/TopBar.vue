<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { logout as logoutApi } from '@/api/auth'
import { refreshTokens } from '@/api/tokenRefresh'
import { decodeJwtExpiryMs } from '@/utils/jwt'
import NotificationBell from '@/components/NotificationBell.vue'

defineProps({
  roleLabel: { type: String, required: true },
})

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()
const initial = computed(() => auth.name?.charAt(0) || '?')
const pageTitle = computed(() => route.meta?.title || 'AMS')

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

async function handleLogout() {
  closeMenu()
  try {
    await logoutApi()
  } catch {
    // ignore network errors on logout
  }
  auth.clear()
  router.push('/login')
}

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

const sessionWarning = computed(() => remainingMs.value != null && remainingMs.value <= 10 * 60 * 1000)

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
  <header class="topbar">
    <button class="topbar__toggle" title="사이드바 접기/펼치기" @click="ui.toggleSidebar()">
      <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M4 6h16M4 12h16M4 18h16" /></svg>
    </button>

    <div class="topbar__title">{{ pageTitle }}</div>

    <div class="topbar__spacer"></div>

    <div v-if="remainingLabel" class="session-timer">
      <button class="session-timer__badge" :class="{ 'session-timer__badge--warn': sessionWarning }" :disabled="extending" @click="handleExtend">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="9" />
          <path d="M12 7v5l3 3" />
        </svg>
        <span class="session-timer__time">{{ remainingLabel }}</span>
        <span class="session-timer__divider"></span>
        <span>연장</span>
      </button>
    </div>

    <NotificationBell />

    <div class="topbar__divider"></div>

    <div class="topbar__user-menu" ref="menuRoot">
      <button class="topbar__user" @click="toggleMenu">
        <div class="topbar__avatar">{{ initial }}</div>
        <div class="topbar__user-text">
          <div class="topbar__user-name">{{ auth.name }}</div>
          <div class="topbar__user-role">{{ roleLabel }}</div>
        </div>
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="topbar__chevron"><path d="m6 9 6 6 6-6" /></svg>
      </button>

      <div v-if="menuOpen" class="topbar__dropdown">
        <RouterLink to="/portal" class="topbar__dropdown-item" @click="closeMenu">내 정보</RouterLink>
        <RouterLink to="/change-password" class="topbar__dropdown-item" @click="closeMenu">비밀번호 변경</RouterLink>
        <button class="topbar__dropdown-item topbar__dropdown-item--danger" @click="handleLogout">로그아웃</button>
      </div>
    </div>
  </header>
</template>
