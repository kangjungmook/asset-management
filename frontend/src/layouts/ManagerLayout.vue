<script setup>
import { useRouter } from 'vue-router'
import { logout as logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import ExpiryBanner from '@/components/ExpiryBanner.vue'
import TopBar from '@/components/TopBar.vue'
import NavIcon from '@/components/NavIcon.vue'

const auth = useAuthStore()
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
      <aside class="sidebar">
        <div class="sidebar__section-label">{{ auth.dept?.deptName || '부서' }} 메뉴</div>
        <nav class="sidebar__nav">
          <RouterLink class="sidebar__link" to="/manage/users"><NavIcon name="users" />사용자 관리</RouterLink>
          <RouterLink class="sidebar__link" to="/manage/passwords"><NavIcon name="passwords" />패스워드 관리</RouterLink>
          <RouterLink class="sidebar__link" to="/portal"><NavIcon name="home" />내 정보</RouterLink>
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
