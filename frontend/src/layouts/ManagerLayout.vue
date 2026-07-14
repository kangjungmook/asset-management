<script setup>
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import ExpiryBanner from '@/components/ExpiryBanner.vue'
import TopBar from '@/components/TopBar.vue'
import NavIcon from '@/components/NavIcon.vue'

const auth = useAuthStore()
const ui = useUiStore()
</script>

<template>
  <div class="app-shell">
    <div class="body-row">
      <aside class="sidebar" :class="{ 'is-collapsed': ui.sidebarCollapsed }">
        <div class="sidebar__brand">
          <div class="topbar__brand-mark">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round">
              <path d="M12 3l8 4-8 4-8-4 8-4z" />
              <path d="M4 12l8 4 8-4" />
            </svg>
          </div>
          <div class="sidebar__brand-text">
            <div class="sidebar__brand-title">AMS</div>
            <div class="sidebar__brand-sub">{{ auth.dept?.deptName || '부서' }} 관리</div>
          </div>
        </div>

        <nav class="sidebar__nav">
          <div class="sidebar__group">
            <div class="sidebar__group-label">부서 관리</div>
            <RouterLink class="sidebar__link" to="/manage/users"><NavIcon name="users" /><span>사용자 관리</span></RouterLink>
            <RouterLink class="sidebar__link" to="/manage/passwords"><NavIcon name="passwords" /><span>패스워드 관리대장</span></RouterLink>
          </div>

          <div class="sidebar__group">
            <div class="sidebar__group-label">내 계정</div>
            <RouterLink class="sidebar__link" to="/portal"><NavIcon name="home" /><span>내 정보</span></RouterLink>
          </div>
        </nav>

        <div class="sidebar__footer">
          <div class="sidebar__footer-card">
            <div class="topbar__avatar">{{ auth.name?.charAt(0) || '?' }}</div>
            <div class="sidebar__footer-text">
              <div class="topbar__user-name">{{ auth.name }}</div>
              <div class="topbar__user-role">부서 관리자</div>
            </div>
          </div>
        </div>
      </aside>

      <div class="main-content-col">
        <TopBar role-label="부서 관리자" />
        <ExpiryBanner />
        <div class="main-content">
          <RouterView v-slot="{ Component, route }">
            <transition name="page-fade" mode="out-in">
              <component :is="Component" :key="route.fullPath" />
            </transition>
          </RouterView>
        </div>
      </div>
    </div>
  </div>
</template>
