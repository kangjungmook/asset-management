<script setup>
import { computed, ref } from 'vue'
import {
  resetSystem,
  resetUsers,
  resetDepartments,
  resetPermissions,
  resetAccountTypes,
  resetPasswords,
  resetAuditLogs,
} from '@/api/system'
import { useUiStore } from '@/stores/ui'

const CONFIRM_PHRASE = '시스템 초기화'

const ui = useUiStore()
const confirmInput = ref('')
const loading = ref(false)
const errorMessage = ref('')
const done = ref(false)
const itemLoading = ref('')

const canSubmit = computed(() => confirmInput.value.trim() === CONFIRM_PHRASE)

const RESET_ITEMS = [
  {
    key: 'auditLogs',
    title: '감사 로그 초기화',
    description: '전체 감사 로그 이력을 삭제합니다.',
    action: resetAuditLogs,
  },
  {
    key: 'passwords',
    title: '패스워드 관리대장 초기화',
    description: '등록된 패스워드 관리대장 항목을 전부 삭제합니다.',
    action: resetPasswords,
  },
  {
    key: 'accountTypes',
    title: '계정유형 초기화',
    description: '계정유형을 기본값(서버·NAS·SVN·GitLab)으로 되돌립니다.',
    warning: '계정유형을 참조 중인 패스워드 관리대장 항목도 함께 삭제됩니다.',
    action: resetAccountTypes,
  },
  {
    key: 'permissions',
    title: '권한 초기화',
    description: '개별 권한 항목을 기본값으로 되돌립니다.',
    warning: '사용자에게 부여된 권한 이력이 전부 삭제됩니다.',
    action: resetPermissions,
  },
  {
    key: 'departments',
    title: '부서 초기화',
    description: '부서를 기본값(개발부·IT사업부)으로 되돌립니다.',
    warning: '기존 사용자의 부서 배정이 전부 해제됩니다.',
    action: resetDepartments,
  },
  {
    key: 'users',
    title: '사용자 초기화',
    description: '관리자를 제외한 모든 사용자 계정을 삭제합니다.',
    warning: '해당 사용자의 패스워드 관리대장·권한·역할도 함께 삭제됩니다.',
    action: resetUsers,
  },
]

async function handleItemReset(item) {
  const confirmed = await ui.confirm({
    title: `${item.title}를 진행할까요?`,
    message: item.warning ? `${item.description} ${item.warning}` : item.description,
    confirmText: '초기화하기',
    danger: true,
  })
  if (!confirmed) return
  itemLoading.value = item.key
  try {
    await item.action()
    ui.success(`${item.title}가 완료되었습니다.`)
  } catch (err) {
    ui.error(err?.response?.data?.message || '초기화에 실패했습니다.')
  } finally {
    itemLoading.value = ''
  }
}

async function handleReset() {
  if (!canSubmit.value || loading.value) return
  errorMessage.value = ''
  loading.value = true
  try {
    await resetSystem(confirmInput.value.trim())
    done.value = true
    confirmInput.value = ''
    ui.success('시스템 데이터가 초기화되었습니다.')
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '초기화에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1>시스템 초기화</h1>
        <p class="page-header__desc">항목별로 골라서 초기화하거나, 관리자 계정을 제외한 전체를 한 번에 초기화할 수 있습니다. 되돌릴 수 없는 작업입니다.</p>
      </div>
    </div>

    <div class="form-section-label" style="margin-top: 0">항목별 초기화</div>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--space-3); margin-bottom: var(--space-7)">
      <div v-for="item in RESET_ITEMS" :key="item.key" class="stat-card" style="display: flex; flex-direction: column; gap: var(--space-3)">
        <div>
          <div style="font-weight: 700; color: var(--color-heading)">{{ item.title }}</div>
          <p style="font-size: var(--text-xs); color: var(--color-text-muted); line-height: 1.5; margin-top: 4px">{{ item.description }}</p>
          <p v-if="item.warning" style="font-size: var(--text-xs); color: var(--color-danger); line-height: 1.5; margin-top: 4px">{{ item.warning }}</p>
        </div>
        <button
          class="btn btn-ghost btn-sm"
          style="align-self: flex-start"
          :disabled="itemLoading === item.key"
          @click="handleItemReset(item)"
        >
          {{ itemLoading === item.key ? '초기화 중...' : '초기화하기' }}
        </button>
      </div>
    </div>

    <div class="form-section-label">전체 초기화</div>
    <div class="stat-card" style="max-width: 640px; border-color: var(--color-danger)">
      <h3 style="color: var(--color-danger); display: flex; align-items: center; gap: 8px; margin-top: 0">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 9v4" /><path d="M12 17h.01" />
          <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z" />
        </svg>
        위험한 작업입니다
      </h3>

      <p style="line-height: 1.7; color: var(--color-text-muted)">
        위 항목별 초기화를 전부 한 번에 실행합니다.
      </p>
      <ul style="line-height: 1.9; color: var(--color-text-muted); padding-left: 20px; margin: 0 0 var(--space-5)">
        <li>패스워드 관리대장 전체 항목</li>
        <li>감사 로그 전체 이력</li>
        <li>관리자를 제외한 모든 사용자 계정 및 부여된 역할·권한</li>
        <li>부서 / 권한 / 계정유형 마스터 데이터 (기본값으로 재생성)</li>
      </ul>
      <p style="line-height: 1.7; color: var(--color-text-muted)">
        <strong style="color: var(--color-heading)">관리자 계정 자체는 삭제되지 않습니다.</strong>
        진행하려면 아래 입력창에 <strong style="color: var(--color-danger)">{{ CONFIRM_PHRASE }}</strong>를 정확히 입력해주세요.
      </p>

      <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
      <p v-if="done" style="color: var(--color-success, #16a34a); font-weight: 600">초기화가 완료되었습니다.</p>

      <div class="field">
        <label for="confirmPhrase">확인 문구</label>
        <input
          id="confirmPhrase"
          v-model="confirmInput"
          class="input"
          type="text"
          :placeholder="CONFIRM_PHRASE"
          autocomplete="off"
        />
      </div>

      <div class="form-actions">
        <button class="btn btn-danger" :disabled="!canSubmit || loading" @click="handleReset">
          {{ loading ? '초기화 중...' : '전체 초기화 실행' }}
        </button>
      </div>
    </div>
  </div>
</template>
