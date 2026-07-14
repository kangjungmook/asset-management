<script setup>
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
</script>

<template>
  <div v-if="ui.confirmDialog" class="modal-overlay" @click.self="ui.resolveConfirm(false)">
    <div class="modal-panel">
      <div class="modal-panel__lead" style="margin-bottom: var(--space-4)">
        <div
          class="modal-panel__icon"
          :style="ui.confirmDialog.danger
            ? 'background: var(--color-danger-tint); color: var(--color-danger)'
            : 'background: var(--color-brand-tint); color: var(--color-brand)'"
        >
          <svg v-if="ui.confirmDialog.danger" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18" /><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /></svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><path d="M12 9v4" /><path d="M12 17h.01" /></svg>
        </div>
        <h3>{{ ui.confirmDialog.title }}</h3>
      </div>
      <p class="text-muted" style="font-size: var(--text-sm); margin-bottom: var(--space-6)">{{ ui.confirmDialog.message }}</p>
      <div class="form-actions" style="justify-content: flex-end">
        <button class="btn btn-ghost" @click="ui.resolveConfirm(false)">{{ ui.confirmDialog.cancelText }}</button>
        <button
          class="btn"
          :class="ui.confirmDialog.danger ? 'btn-danger' : 'btn-primary'"
          @click="ui.resolveConfirm(true)"
        >
          {{ ui.confirmDialog.confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>
