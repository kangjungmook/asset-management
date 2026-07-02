<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const employeeNo = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

const auth = useAuthStore()
const router = useRouter()

async function handleSubmit() {
  if (loading.value) return
  errorMessage.value = ''
  loading.value = true
  try {
    const data = await loginApi({ employeeNo: employeeNo.value, password: password.value })
    auth.setFromLogin(data)
    router.push(auth.homePath)
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '로그인에 실패했습니다.'
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
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round">
            <path d="M12 3l8 4-8 4-8-4 8-4z" />
            <path d="M4 12l8 4 8-4" />
            <path d="M4 17l8 4 8-4" />
          </svg>
        </div>
      </div>

      <h1 style="text-align: center">AMS 로그인</h1>
      <p class="page-header__desc" style="text-align: center; margin-bottom: var(--space-8)">
        사내 계정 정보로 로그인하세요.
      </p>

      <form @submit.prevent="handleSubmit">
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <div class="field">
          <label for="employeeNo">사번</label>
          <input
            id="employeeNo"
            v-model="employeeNo"
            class="input"
            type="text"
            autocomplete="username"
            required
          />
        </div>

        <div class="field">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            required
          />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" style="width: 100%; height: 46px" :disabled="loading">
            {{ loading ? '로그인 중...' : '로그인' }}
          </button>
        </div>
      </form>

      <p class="auth-hint">비밀번호를 잊으셨나요? 관리자에게 문의하세요.</p>
    </div>

    <p class="login-solo-footer">© 2026 Company · 사내 전용 시스템 · v1.0</p>
  </div>
</template>
