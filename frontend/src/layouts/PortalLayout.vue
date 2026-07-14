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
            <div class="sidebar__brand-sub">사내 자산 관리</div>
          </div>
        </div>

        <nav class="sidebar__nav">
          <RouterLink class="sidebar__link" to="/portal"><NavIcon name="home" /><span>내 정보</span></RouterLink>
        </nav>

        <div class="sidebar__footer">
          <div class="sidebar__footer-card">
            <div class="topbar__avatar">{{ auth.name?.charAt(0) || '?' }}</div>
            <div class="sidebar__footer-text">
              <div class="topbar__user-name">{{ auth.name }}</div>
              <div class="topbar__user-role">일반 사용자</div>
            </div>
          </div>
        </div>
      </aside>

      <div class="main-content-col">
        <TopBar role-label="일반 사용자" />
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
