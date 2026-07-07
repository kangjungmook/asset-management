<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const REMEMBER_KEY = 'ams.rememberedEmployeeNo'

const employeeNo = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')
const loading = ref(false)

const auth = useAuthStore()
const router = useRouter()

onMounted(() => {
  const remembered = localStorage.getItem(REMEMBER_KEY)
  if (remembered) {
    employeeNo.value = remembered
    rememberMe.value = true
  }
})

async function handleSubmit() {
  if (loading.value) return
  errorMessage.value = ''
  loading.value = true
  try {
    const data = await loginApi({ employeeNo: employeeNo.value, password: password.value })
    if (rememberMe.value) {
      localStorage.setItem(REMEMBER_KEY, employeeNo.value)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
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
            autofocus
          />
        </div>

        <div class="field">
          <label for="password">비밀번호</label>
          <div style="position: relative">
            <input
              id="password"
              v-model="password"
              class="input"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              required
              style="width: 100%; padding-right: 44px"
            />
            <button
              type="button"
              class="password-toggle"
              :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 표시'"
              @click="showPassword = !showPassword"
            >
              <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" /><circle cx="12" cy="12" r="3" /></svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3l18 18" /><path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" /><path d="M9.9 5.1A9.4 9.4 0 0 1 12 5c6.5 0 10 7 10 7a13.2 13.2 0 0 1-3.1 3.9M6.2 6.2C4 7.6 2 12 2 12a13.4 13.4 0 0 0 5 5.6" /></svg>
            </button>
          </div>
        </div>

        <label class="checkbox-row" style="margin-bottom: var(--space-5)">
          <input v-model="rememberMe" type="checkbox" />
          사번 기억하기
        </label>

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
