<script setup>
import { useRouter } from 'vue-router'
import { logout as logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import ExpiryBanner from '@/components/ExpiryBanner.vue'
import TopBar from '@/components/TopBar.vue'
import NavIcon from '@/components/NavIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()

async function handleLogout() {
  try {
    await logoutApi()
  } catch {
    // ignore network errors on logout
  }
  auth.clear()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <TopBar role-label="부서 관리자" />
    <ExpiryBanner />

    <div class="body-row">
      <aside class="sidebar" :class="{ 'is-collapsed': ui.sidebarCollapsed }">
        <button class="sidebar__toggle" :aria-label="ui.sidebarCollapsed ? '사이드바 펼치기' : '사이드바 접기'" @click="ui.toggleSidebar()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 6l-6 6 6 6" /></svg>
          <span>접기</span>
        </button>

        <div class="sidebar__section-label">{{ auth.dept?.deptName || '부서' }} 메뉴</div>
        <nav class="sidebar__nav">
          <RouterLink class="sidebar__link" to="/manage/users"><NavIcon name="users" /><span>사용자 관리</span></RouterLink>
          <RouterLink class="sidebar__link" to="/manage/passwords"><NavIcon name="passwords" /><span>패스워드 관리</span></RouterLink>
          <RouterLink class="sidebar__link" to="/portal"><NavIcon name="home" /><span>내 정보</span></RouterLink>
        </nav>
        <div class="sidebar__footer">
          <button class="btn btn-ghost btn-sm" style="width: 100%" @click="handleLogout">로그아웃</button>
        </div>
      </aside>

      <div class="main-content">
        <RouterView />
      </div>
    </div>
  </div>
</template>
