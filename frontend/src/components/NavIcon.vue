<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: Number, default: 18 },
})

const ICONS = {
  dashboard: ['M3 3h7v7H3z', 'M14 3h7v7h-7z', 'M14 14h7v7h-7z', 'M3 14h7v7H3z'],
  passwords: ['M6 11h12v9H6z', 'M9 11V8a3 3 0 0 1 6 0v3'],
  users: [
    'M16 19v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1',
    'M9.5 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7',
    'M21 19v-1a4 4 0 0 0-3-3.8',
  ],
  departments: ['M3 21h18', 'M5 21V7l7-4 7 4v14', 'M9 9h1', 'M9 13h1', 'M14 9h1', 'M14 13h1', 'M10 21v-4h4v4'],
  permissions: ['M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6z', 'M9 12l2 2 4-4'],
  'account-types': ['M4 7h16v10H4z', 'M4 11h16', 'M8 15h.01', 'M12 15h4'],
  'audit-log': ['M7 4h10l3 3v13H7z', 'M14 4v4h4', 'M10 13h6', 'M10 17h4'],
  home: ['M4 11l8-7 8 7', 'M6 10v9h12v-9'],
  'system-reset': ['M3 12a9 9 0 1 1 2.64 6.36', 'M3 21v-6h6'],
}

const paths = computed(() => ICONS[props.name] || [])
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <path v-for="d in paths" :key="d" :d="d" />
  </svg>
</template>
