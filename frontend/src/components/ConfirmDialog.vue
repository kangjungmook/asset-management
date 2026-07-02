<script setup>
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
</script>

<template>
  <div v-if="ui.confirmDialog" class="modal-overlay" @click.self="ui.resolveConfirm(false)">
    <div class="modal-panel" style="max-width: 400px">
      <h3 style="margin-bottom: var(--space-3)">{{ ui.confirmDialog.title }}</h3>
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
