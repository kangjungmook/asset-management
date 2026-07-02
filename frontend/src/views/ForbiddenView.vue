<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function goHome() {
  router.push(auth.isLoggedIn ? auth.homePath : '/login')
}
</script>

<template>
  <div class="auth-shell">
    <div class="auth-panel">
      <div class="auth-panel__icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
          <rect x="5" y="11" width="14" height="9" rx="2" />
          <path d="M8 11V8a4 4 0 0 1 8 0v3" />
          <circle cx="12" cy="15.5" r="1.2" />
        </svg>
      </div>
      <h1 class="auth-panel__title">접근할 수 없는 페이지예요</h1>
      <p class="auth-panel__desc">이 화면에 대한 권한이 없습니다. 필요한 경우 관리자에게 권한을 요청하세요.</p>
      <button class="btn btn-primary" @click="goHome">이전으로 돌아가기</button>
    </div>
  </div>
</template>
