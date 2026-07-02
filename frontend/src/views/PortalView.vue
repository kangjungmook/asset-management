<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe } from '@/api/users'
import { listPasswords, updatePassword } from '@/api/passwords'
import { listAccountTypes } from '@/api/accountTypes'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const ui = useUiStore()
const me = ref(null)
const passwordEntries = ref([])
const accountTypes = ref([])
const loadingPasswords = ref(true)

const editingId = ref(null)
const form = ref({})
const errorMessage = ref('')

const canEditPassword = computed(() => auth.hasPermission('PASSWORD.EDIT'))
const initial = computed(() => me.value?.name?.charAt(0) || '?')

const expiringCount = computed(() =>
  passwordEntries.value.filter((e) => {
    const days = daysUntil(e.expireAt)
    return days !== null && days <= 30
  }).length,
)

function typeName(typeId) {
  return accountTypes.value.find((t) => t.typeId === typeId)?.typeName || '기타'
}

function daysUntil(dateStr) {
  if (!dateStr) return null
  return Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24))
}

function expiryStatus(entry) {
  const days = daysUntil(entry.expireAt)
  if (days === null) return null
  if (days < 0) return { label: '만료됨', tone: 'badge-danger' }
  if (days <= 30) return { label: `D-${days}`, tone: 'badge-warning' }
  return { label: `만료일 ${entry.expireAt}`, tone: 'badge-success' }
}

function openEdit(entry) {
  editingId.value = entry.pwId
  form.value = {
    typeId: entry.typeId,
    userId: entry.userId,
    approverId: entry.approverId,
    accountId: entry.accountId,
    changedAt: entry.changedAt ? entry.changedAt.slice(0, 16) : '',
    expireAt: entry.expireAt,
    changeReason: entry.changeReason,
    note: entry.note,
  }
  errorMessage.value = ''
}

async function submitEdit() {
  errorMessage.value = ''
  try {
    await updatePassword(editingId.value, form.value)
    ui.success('패스워드 관리대장이 수정되었습니다.')
    editingId.value = null
    passwordEntries.value = (await listPasswords({ page: 1, size: 100 })).content
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

onMounted(async () => {
  me.value = await getMe()
  try {
    const [entryPage, types] = await Promise.all([
      listPasswords({ page: 1, size: 100 }),
      listAccountTypes(),
    ])
    passwordEntries.value = entryPage.content
    accountTypes.value = types
  } finally {
    loadingPasswords.value = false
  }
})
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1 v-if="me">안녕하세요, {{ me.name }}님</h1>
        <p class="page-header__desc">내 계정 정보와 패스워드 변경 이력을 확인하세요.</p>
      </div>
    </div>

    <div v-if="me" class="profile-card">
      <div class="profile-card__avatar">{{ initial }}</div>
      <div class="profile-card__body">
        <div class="profile-card__name">{{ me.name }}</div>
        <div class="profile-card__meta">사번 {{ me.employeeNo }} · {{ me.deptName || '미배정' }}</div>
      </div>
    </div>

    <div class="stat-grid" style="margin-top: var(--space-5)">
      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">소속 부서</span>
          <span class="stat-card__icon" style="background: var(--color-brand-tint); color: var(--color-brand)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 21V7l7-4 7 4v14" />
              <path d="M10 21v-4h4v4" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value" style="font-size: var(--text-xl)">{{ me?.deptName || '미배정' }}</div>
        <div class="stat-card__sub">현재 소속</div>
      </div>

      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">보유 개별 권한</span>
          <span class="stat-card__icon" style="background: var(--color-purple-tint); color: var(--color-purple)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6z" />
              <path d="M9 12l2 2 4-4" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ me?.permissions?.length ?? 0 }}개</div>
        <div class="stat-card__sub">부여된 권한 수</div>
      </div>

      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">만료 임박 항목</span>
          <span class="stat-card__icon" style="background: var(--color-warning-tint); color: var(--color-warning)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="9" />
              <path d="M12 7v5l3 3" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ expiringCount }}건</div>
        <div class="stat-card__sub">30일 이내 만료 예정</div>
      </div>
    </div>

    <div class="section">
      <h3 style="margin-bottom: var(--space-1)">내 패스워드 관리대장</h3>
      <p class="text-muted" style="font-size: var(--text-sm); margin-bottom: var(--space-4)">
        실제 비밀번호는 저장하지 않으며, 변경 이력만 관리합니다.
      </p>

      <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

      <div v-if="editingId" class="form-panel">
        <div class="form-grid">
          <div class="field" style="margin-bottom: 0">
            <label>계정 ID</label>
            <input v-model="form.accountId" class="input" type="text" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>변경일시</label>
            <input v-model="form.changedAt" class="input" type="datetime-local" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>만료일</label>
            <input v-model="form.expireAt" class="input" type="date" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>변경 사유</label>
            <input v-model="form.changeReason" class="input" type="text" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>비고</label>
            <input v-model="form.note" class="input" type="text" />
          </div>
        </div>
        <div class="form-actions">
          <button class="btn btn-primary" @click="submitEdit">저장하기</button>
          <button class="btn btn-ghost" @click="editingId = null">닫기</button>
        </div>
      </div>

      <div v-if="!loadingPasswords && passwordEntries.length === 0" class="empty-state">
        등록된 항목이 없습니다.
      </div>

      <div class="pw-list">
        <div v-for="entry in passwordEntries" :key="entry.pwId" class="pw-list__item">
          <div class="pw-list__icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="5" y="11" width="14" height="9" rx="2" />
              <path d="M8 11V8a4 4 0 0 1 8 0v3" />
            </svg>
          </div>
          <div class="pw-list__body">
            <div class="pw-list__title">{{ typeName(entry.typeId) }} <span class="text-faint">· {{ entry.accountId || '계정 미지정' }}</span></div>
            <div class="pw-list__sub">
              마지막 변경 {{ entry.changedAt?.slice(0, 10) || '기록 없음' }}
              <span v-if="entry.changeReason"> · {{ entry.changeReason }}</span>
            </div>
          </div>
          <span v-if="expiryStatus(entry)" class="badge" :class="expiryStatus(entry).tone">
            {{ expiryStatus(entry).label }}
          </span>
          <button v-if="canEditPassword" class="btn btn-ghost btn-sm" @click="openEdit(entry)">수정하기</button>
        </div>
      </div>

      <p v-if="!canEditPassword" class="field-hint" style="margin-top: var(--space-3)">
        수정 권한이 없어 조회만 가능합니다. 수정이 필요하면 관리자에게 권한을 요청하세요.
      </p>
    </div>
  </div>
</template>
