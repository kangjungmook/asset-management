<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const errorMessage = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (loading.value) return
  errorMessage.value = ''

  if (newPassword.value.length < 4) {
    errorMessage.value = '새 비밀번호는 4자 이상이어야 합니다.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = '새 비밀번호가 일치하지 않습니다.'
    return  
  }

  loading.value = true
  try {
    await changePassword({
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
    })
    auth.clearMustChangePassword()
    ui.success('비밀번호가 변경되었습니다.')
    router.push(auth.homePath)
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-shell">
    <div class="login-solo-card">
      <div class="login-solo-card__mark">
        <div class="topbar__brand-mark" style="width: 44px; height: 44px; border-radius: 12px">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="5" y="11" width="14" height="9" rx="2" />
            <path d="M8 11V8a4 4 0 0 1 8 0v3" />
          </svg>
        </div>  
      </div>

      <h1 style="text-align: center">비밀번호를 변경해주세요</h1>
      <p class="page-header__desc" style="text-align: center; margin-bottom: var(--space-8)">
        {{ auth.mustChangePassword ? '초기 비밀번호를 그대로 사용할 수 없습니다. 새 비밀번호로 변경해주세요.' : '보안을 위해 새 비밀번호로 변경합니다.' }}
      </p>

      <form @submit.prevent="handleSubmit">
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <div class="field">
          <label for="currentPassword">현재 비밀번호</label>
          <input
            id="currentPassword"
            v-model="currentPassword"
            class="input"
            type="password"   
            autocomplete="current-password"
            required
          />
        </div>

        <div class="field">   
          <label for="newPassword">새 비밀번호</label>
          <input
            id="newPassword"
            v-model="newPassword"
            class="input"
            type="password"
            autocomplete="new-password"
            required
          />
        </div>

        <div class="field">
          <label for="confirmPassword">새 비밀번호 확인</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            class="input"
            type="password"
            autocomplete="new-password"
            required
          />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" style="width: 100%; height: 46px" :disabled="loading">
            {{ loading ? '변경 중...' : '비밀번호 변경하기' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
