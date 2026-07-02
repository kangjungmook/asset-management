<script setup>
import ToastStack from '@/components/ToastStack.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
</script>

<template>
  <RouterView />
  <ToastStack />
  <ConfirmDialog />
</template>
