<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

defineProps({
  roleLabel: { type: String, required: true },
})

const auth = useAuthStore()
const initial = computed(() => auth.name?.charAt(0) || '?')
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

    <div class="topbar__user">
      <div class="topbar__avatar">{{ initial }}</div>
      <div>
        <div class="topbar__user-name">{{ auth.name }}</div>
        <div class="topbar__user-role">{{ roleLabel }}</div>
      </div>
    </div>
  </div>
</template>
