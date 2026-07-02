<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listExpiringPasswords } from '@/api/passwords'

const router = useRouter()
const count = ref(0)
const loaded = ref(false)

onMounted(async () => {
  try {
    const items = await listExpiringPasswords()
    count.value = items.length
  } catch {
    count.value = 0
  } finally {
    loaded.value = true
  }
})

function goToPasswords() {
  router.push('/manage/passwords')
}
</script>

<template>
  <div v-if="loaded && count > 0" class="banner" role="button" tabindex="0" @click="goToPasswords" @keyup.enter="goToPasswords">
    <span>만료 30일 이내로 다가온 계정이 {{ count }}건 있습니다. 클릭해서 확인하세요.</span>
    <span aria-hidden="true">›</span>
  </div>
</template>
