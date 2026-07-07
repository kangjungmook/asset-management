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
    <TopBar role-label="ADMIN" />
    <ExpiryBanner />

    <div class="body-row">
      <aside class="sidebar" :class="{ 'is-collapsed': ui.sidebarCollapsed }">
        <button class="sidebar__toggle" :aria-label="ui.sidebarCollapsed ? '사이드바 펼치기' : '사이드바 접기'" @click="ui.toggleSidebar()">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 6l-6 6 6 6" /></svg>
          <span>접기</span>
        </button>

        <nav class="sidebar__nav">
          <div class="sidebar__section-label">개요</div>
          <RouterLink class="sidebar__link" to="/manage/dashboard"><NavIcon name="dashboard" /><span>대시보드</span></RouterLink>

          <div class="sidebar__section-label">조직·계정</div>
          <RouterLink class="sidebar__link" to="/manage/users"><NavIcon name="users" /><span>사용자 관리</span></RouterLink>
          <RouterLink class="sidebar__link" to="/manage/departments"><NavIcon name="departments" /><span>부서 관리</span></RouterLink>
          <RouterLink class="sidebar__link" to="/manage/permissions"><NavIcon name="permissions" /><span>권한 관리</span></RouterLink>

          <div class="sidebar__section-label">패스워드</div>
          <RouterLink class="sidebar__link" to="/manage/passwords"><NavIcon name="passwords" /><span>패스워드 관리</span></RouterLink>
          <RouterLink class="sidebar__link" to="/manage/account-types"><NavIcon name="account-types" /><span>계정유형 관리</span></RouterLink>

          <div class="sidebar__section-label">로그</div>
          <RouterLink class="sidebar__link" to="/manage/audit-logs"><NavIcon name="audit-log" /><span>감시 로그</span></RouterLink>
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
