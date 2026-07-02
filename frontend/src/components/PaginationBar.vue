<script setup>
const props = defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalElements: { type: Number, required: true },
})
const emit = defineEmits(['change'])

function goTo(page) {
  if (page < 1 || page > props.totalPages || page === props.page) return
  emit('change', page)
}
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <span class="pagination__summary">총 {{ totalElements }}건 · {{ page }} / {{ totalPages }} 페이지</span>
    <div class="pagination__controls">
      <button class="btn btn-ghost btn-sm" :disabled="page <= 1" @click="goTo(page - 1)">이전</button>
      <button class="btn btn-ghost btn-sm" :disabled="page >= totalPages" @click="goTo(page + 1)">다음</button>
    </div>
  </div>
</template>
