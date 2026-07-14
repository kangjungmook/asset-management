<script setup>
import { computed, onMounted, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import {
  getUser,
  updateUser,
  deleteUser,
  deactivateUser,
  activateUser,
  grantRole,
  revokeRole,
  grantPermission,
  revokePermission,
} from '@/api/users'
import { listDepartments } from '@/api/departments'
import { listPermissions } from '@/api/permissions'
import { resetPassword as resetPasswordApi } from '@/api/auth'
import {
  listPasswords,
  createPassword,
  updatePassword,
  deletePassword,
  approvePassword,
  rejectPassword,
} from '@/api/passwords'
import { listAccountTypes } from '@/api/accountTypes'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()

const userId = computed(() => Number(route.params.id))

const detail = ref(null)
const detailForm = ref({ name: '', deptId: null })
const detailError = ref('')
const tempPasswordResult = ref(null)

const departments = ref([])
const allPermissions = ref([])
const visiblePermissions = computed(() => (hasRole('DEPT_MANAGER') ? [] : allPermissions.value))

const ALL_ROLES = [{ code: 'DEPT_MANAGER', label: '부서 관리자' }]

const roleForm = ref([])
const permissionForm = ref([])

const isFormDirty = computed(() => {
  if (!detail.value) return false
  if (detailForm.value.name !== detail.value.name) return true
  if (detailForm.value.deptId !== detail.value.deptId) return true
  const origRoles = JSON.stringify([...(detail.value.roles || [])].sort())
  const curRoles = JSON.stringify([...roleForm.value].sort())
  if (origRoles !== curRoles) return true
  const origPerms = JSON.stringify((detail.value.permissions || []).map((p) => p.permId).sort())
  const curPerms = JSON.stringify([...permissionForm.value].sort())
  return origPerms !== curPerms
})

onBeforeRouteLeave(async () => {
  if (!isFormDirty.value) return true
  return ui.confirm({
    title: '저장하지 않은 변경사항이 있습니다',
    message: '수정한 내용을 저장하지 않고 나가면 사라집니다.',
    confirmText: '저장 안 하고 나가기',
    danger: true,
  })
})

const pwEntries = ref([])
const accountTypes = ref([])
const pwLoading = ref(true)
const showPwForm = ref(false)
const editingPwId = ref(null)
const pwError = ref('')
const showMorePwFields = ref(false)
const editingPwRejectedEntry = computed(() => {
  if (!editingPwId.value) return null
  const entry = pwEntries.value.find((e) => e.pwId === editingPwId.value)
  return entry && entry.status === 'REJECTED' ? entry : null
})

function pad2(n) {
  return String(n).padStart(2, '0')
}

function nowDatetimeLocal() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

function emptyPwForm() {
  return {
    typeId: null,
    userId: userId.value,
    requesterName: '',
    accountId: '',
    changedAt: nowDatetimeLocal(),
    expireAt: '',
    changeReason: '',
    note: '',
  }
}
const pwForm = ref(emptyPwForm())

const pwExpireDays = ref(null)

function applyPwExpireDays() {
  const days = Number(pwExpireDays.value)
  if (!days || days <= 0) return
  const base = new Date()
  base.setDate(base.getDate() + days)
  pwForm.value.expireAt = `${base.getFullYear()}-${pad2(base.getMonth() + 1)}-${pad2(base.getDate())}`
}

function pwStatus(expireAt) {
  if (!expireAt) return null
  const diffDays = Math.ceil((new Date(expireAt) - new Date()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return '만료'
  if (diffDays <= 30) return '임박'
  return '정상'
}

function pwStatusClass(expireAt) {
  return { 만료: 'badge-danger', 임박: 'badge-warning', 정상: 'badge-success' }[pwStatus(expireAt)] || ''
}

function pwRowStyle(expireAt) {
  const bg = { 만료: '#FEF6F5', 임박: '#FEFBF2' }[pwStatus(expireAt)]
  return bg ? { background: bg } : {}
}

function pwRemaining(expireAt) {
  if (!expireAt) return null
  const diffDays = Math.ceil((new Date(expireAt) - new Date()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return { text: `${Math.abs(diffDays)}일 경과`, color: 'var(--color-danger)' }
  if (diffDays <= 30) return { text: `${diffDays}일 남음`, color: 'var(--color-warning)' }
  return { text: `${diffDays}일 남음`, color: 'var(--color-success)' }
}

function isPendingPw(entry) {
  return (entry.status || 'PENDING') === 'PENDING'
}

function isApprovedPw(entry) {
  return entry.status === 'APPROVED'
}

function isRejectedPw(entry) {
  return entry.status === 'REJECTED'
}

function canApprovePw(entry) {
  if (!isPendingPw(entry)) return false
  if (auth.isAdmin) return true
  if (auth.hasRole('DEPT_MANAGER') && entry.userDeptId != null && entry.userDeptId === auth.dept?.deptId) return true
  return auth.hasPermission('PASSWORD.APPROVE')
}

async function approvePwEntry(entry) {
  const confirmed = await ui.confirm({
    title: '승인할까요?',
    message: `'${entry.accountId || entry.userName}' 항목의 비밀번호 변경을 승인합니다.`,
    confirmText: '승인하기',
  })
  if (!confirmed) return
  try {
    await approvePassword(entry.pwId)
    ui.success('승인되었습니다.')
    await loadPasswords()
  } catch (err) {
    ui.error(err?.response?.data?.message || '승인에 실패했습니다.')
  }
}

const showRejectPwModal = ref(false)
const rejectPwTarget = ref(null)
const rejectPwReason = ref('')
const rejectPwError = ref('')

function openRejectPwModal(entry) {
  rejectPwTarget.value = entry
  rejectPwReason.value = ''
  rejectPwError.value = ''
  showRejectPwModal.value = true
}

async function submitRejectPw() {
  if (!rejectPwReason.value.trim()) {
    rejectPwError.value = '반려 사유를 입력해주세요.'
    return
  }
  try {
    await rejectPassword(rejectPwTarget.value.pwId, rejectPwReason.value.trim())
    ui.success('반려되었습니다.')
    showRejectPwModal.value = false
    await loadPasswords()
  } catch (err) {
    rejectPwError.value = err?.response?.data?.message || '반려에 실패했습니다.'
  }
}

async function loadDetail() {
  detail.value = await getUser(userId.value)
  detailForm.value = { name: detail.value.name, deptId: detail.value.deptId }
  roleForm.value = [...(detail.value.roles || [])]
  permissionForm.value = (detail.value.permissions || []).map((p) => p.permId)
}

async function loadPasswords() {
  pwLoading.value = true
  try {
    const result = await listPasswords({ userId: userId.value, page: 1, size: 100 })
    pwEntries.value = result.content
  } finally {
    pwLoading.value = false
  }
}

function hasRole(role) {
  return roleForm.value.includes(role)
}

function hasPermission(permId) {
  return permissionForm.value.includes(permId)
}

async function submitDetail() {
  detailError.value = ''
  try {
    await updateUser(userId.value, detailForm.value)

    const currentRoles = detail.value.roles || []
    for (const { code } of ALL_ROLES) {
      const has = currentRoles.includes(code)
      const wants = roleForm.value.includes(code)
      if (wants && !has) await grantRole(userId.value, code)
      if (!wants && has) await revokeRole(userId.value, code)
    }

    const currentPerms = detail.value.permissions || []
    for (const perm of allPermissions.value) {
      const granted = currentPerms.find((p) => p.permId === perm.permId)
      const wants = permissionForm.value.includes(perm.permId)
      if (wants && !granted) await grantPermission(userId.value, perm.permId)
      if (!wants && granted) await revokePermission(userId.value, perm.permId)
    }

    ui.success('저장되었습니다.')
    await loadDetail()
  } catch (err) {
    detailError.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function handleDeactivate() {
  const confirmed = await ui.confirm({
    title: '사용자를 비활성화할까요?',
    message: '비활성화하면 이 사용자는 로그인할 수 없습니다.',
    confirmText: '비활성화하기',
    danger: true,
  })
  if (!confirmed) return
  await deactivateUser(userId.value)
  ui.success('사용자가 비활성화되었습니다.')
  await loadDetail()
}

async function handleActivate() {
  const confirmed = await ui.confirm({
    title: '사용자를 다시 활성화할까요?',
    message: '활성화하면 이 사용자는 다시 로그인할 수 있습니다.',
    confirmText: '활성화하기',
  })
  if (!confirmed) return
  await activateUser(userId.value)
  ui.success('사용자가 활성화되었습니다.')
  await loadDetail()
}

async function handleDelete() {
  const confirmed = await ui.confirm({
    title: '계정을 완전히 삭제할까요?',
    message: `'${detail.value.name}' 계정을 삭제합니다. 이 작업은 되돌릴 수 없습니다.`,
    confirmText: '완전히 삭제하기',
    danger: true,
  })
  if (!confirmed) return
  detailError.value = ''
  try {
    await deleteUser(userId.value)
    ui.success('사용자가 삭제되었습니다.')
    router.push('/manage/users')
  } catch (err) {
    detailError.value = err?.response?.data?.message || '삭제에 실패했습니다.'
  }
}

async function handleResetPassword() {
  const confirmed = await ui.confirm({
    title: '임시 비밀번호를 발급할까요?',
    message: '기존 비밀번호는 더 이상 사용할 수 없게 됩니다.',
    confirmText: '발급하기',
  })
  if (!confirmed) return
  tempPasswordResult.value = await resetPasswordApi(userId.value)
  ui.success('임시 비밀번호가 발급되었습니다.')
}

function toggleRole(role, checked) {
  if (checked) {
    if (!roleForm.value.includes(role)) roleForm.value.push(role)
  } else {
    roleForm.value = roleForm.value.filter((r) => r !== role)
  }
}

function togglePermission(perm, checked) {
  if (checked) {
    if (!permissionForm.value.includes(perm.permId)) permissionForm.value.push(perm.permId)
  } else {
    permissionForm.value = permissionForm.value.filter((id) => id !== perm.permId)
  }
}

function typeName(typeId) {
  return accountTypes.value.find((t) => t.typeId === typeId)?.typeName || '-'
}

function openPwCreate() {
  editingPwId.value = null
  pwForm.value = emptyPwForm()
  pwExpireDays.value = null
  showMorePwFields.value = false
  pwError.value = ''
  showPwForm.value = true
}

function openPwEdit(entry) {
  editingPwId.value = entry.pwId
  pwForm.value = {
    typeId: entry.typeId,
    userId: userId.value,
    requesterName: entry.requesterName || '',
    accountId: entry.accountId,
    changedAt: entry.changedAt ? entry.changedAt.slice(0, 16) : '',
    expireAt: entry.expireAt,
    changeReason: entry.changeReason,
    note: entry.note,
  }
  pwExpireDays.value = null
  showMorePwFields.value = !!(entry.requesterName || entry.changeReason || entry.note)
  pwError.value = ''
  showPwForm.value = true
}

async function submitPwForm() {
  pwError.value = ''
  try {
    if (editingPwId.value) {
      await updatePassword(editingPwId.value, pwForm.value)
      ui.success('패스워드 관리대장이 수정되었습니다.')
    } else {
      await createPassword(pwForm.value)
      ui.success('패스워드 관리대장이 등록되었습니다.')
    }
    showPwForm.value = false
    await loadPasswords()
  } catch (err) {
    pwError.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function removePwEntry(entry) {
  const confirmed = await ui.confirm({
    title: '항목을 삭제할까요?',
    message: '이 패스워드 관리대장 항목을 삭제합니다. 변경 이력이 사라집니다.',
    confirmText: '삭제하기',
    danger: true,
  })
  if (!confirmed) return
  try {
    await deletePassword(entry.pwId)
    ui.success('패스워드 관리대장이 삭제되었습니다.')
    await loadPasswords()
  } catch (err) {
    ui.error(err?.response?.data?.message || '삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  await Promise.all([
    loadDetail(),
    loadPasswords(),
    listDepartments().then((v) => (departments.value = v)),
    listPermissions().then((v) => (allPermissions.value = v)),
    listAccountTypes().then((v) => (accountTypes.value = v)),
  ])
})
</script>

<template>
  <div>
    <RouterLink to="/manage/users" class="text-muted" style="font-size: var(--text-sm); display: inline-block; margin-bottom: var(--space-4)">← 사용자 관리로 돌아가기</RouterLink>

    <template v-if="detail">
      <div class="page-header">
        <div style="display: flex; align-items: center; gap: var(--space-4)">
          <span style="width: 52px; height: 52px; border-radius: 12px; background: var(--color-brand-tint); color: var(--color-brand); font-size: 20px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex: 0 0 auto">{{ detail.name?.charAt(0) }}</span>
          <div>
            <h1>{{ detail.name }}</h1>
            <p class="page-header__desc">
              {{ detail.employeeNo }} · {{ detail.deptName || '미배정' }}
              <span class="badge" :class="detail.isActive ? 'badge-success' : 'badge-danger'" style="margin-left: var(--space-2)">
                {{ detail.isActive ? '활성' : '비활성' }}
              </span>
            </p>
          </div>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1.4fr 1fr; gap: var(--space-4); align-items: start">
        <div class="table-wrap">
          <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border)">
            <h3 style="font-size: var(--text-base)">기본 정보 · 역할 · 권한</h3>
            <p class="page-header__desc" style="font-size: var(--text-xs); margin-top: 3px">아래 항목을 한 번에 저장합니다.</p>
          </div>

          <div style="padding: var(--space-4)">
            <p v-if="detailError" class="form-error">{{ detailError }}</p>

            <div class="form-grid" style="margin-bottom: var(--space-5)">
              <div class="field" style="margin-bottom: 0">
                <label>이름</label>
                <input v-model="detailForm.name" class="input" type="text" />
              </div>
              <div class="field" style="margin-bottom: 0">
                <label>부서</label>
                <select v-model="detailForm.deptId" class="input" :disabled="!auth.isAdmin">
                  <option :value="null">미배정</option>
                  <option v-for="d in departments" :key="d.deptId" :value="d.deptId">{{ d.deptName }}</option>
                </select>
              </div>
            </div>

            <template v-if="auth.isAdmin">
              <div style="height: 1px; background: var(--color-border); margin-bottom: var(--space-5)"></div>
              <label class="field-hint" style="display: block; margin-bottom: var(--space-2)">역할</label>
              <div style="display: flex; gap: var(--space-2); margin-bottom: var(--space-5)">
                <label v-for="role in ALL_ROLES" :key="role.code" class="checkbox-row" style="flex: 1; border: 1px solid var(--color-border-strong); border-radius: var(--radius-xl); padding: var(--space-2) var(--space-3)">
                  <input type="checkbox" :checked="hasRole(role.code)" @change="toggleRole(role.code, $event.target.checked)" />
                  {{ role.label }}
                </label>
              </div>
            </template>

            <template v-if="hasRole('DEPT_MANAGER')">
              <div style="height: 1px; background: var(--color-border); margin-bottom: var(--space-5)"></div>
              <label class="field-hint" style="display: block; margin-bottom: var(--space-2)">개별 권한</label>
              <p style="font-size: var(--text-sm); color: var(--color-text-muted)">부서 관리자는 관련 권한을 모두 자동으로 가지므로 따로 부여할 필요가 없습니다.</p>
            </template>
            <template v-else-if="visiblePermissions.length">
              <div style="height: 1px; background: var(--color-border); margin-bottom: var(--space-5)"></div>
              <label class="field-hint" style="display: block; margin-bottom: var(--space-2)">개별 권한</label>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2)">
                <label
                  v-for="perm in visiblePermissions"
                  :key="perm.permId"
                  class="checkbox-row"
                  style="border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: var(--space-2) var(--space-3); background: var(--color-bg-sunken)"
                >
                  <input
                    type="checkbox"
                    :checked="hasPermission(perm.permId)"
                    @change="togglePermission(perm, $event.target.checked)"
                  />
                  <span>
                    <span style="display: block">{{ perm.permName }}</span>
                    <span class="text-faint" style="display: block; font-size: var(--text-xs); font-family: var(--font-mono)">{{ perm.permCode }}</span>
                  </span>
                </label>
              </div>
            </template>
          </div>

          <div style="display: flex; align-items: center; justify-content: space-between; padding: var(--space-3) var(--space-4); border-top: 1px solid var(--color-border); background: var(--color-bg-sunken)">
            <span v-if="isFormDirty" style="font-size: var(--text-xs); color: var(--color-warning); display: flex; align-items: center; gap: 6px">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 11h14a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1z" /><path d="M8 11V7a4 4 0 0 1 8 0v4" /></svg>
              저장하지 않은 변경사항이 있습니다
            </span>
            <span v-else></span>
            <div style="display: flex; gap: var(--space-2)">
              <button class="btn btn-ghost btn-sm" @click="router.push('/manage/users')">취소</button>
              <button class="btn btn-primary btn-sm" :disabled="!isFormDirty" @click="submitDetail">저장</button>
            </div>
          </div>
        </div>

        <div class="table-wrap">
          <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border)">
            <h3 style="font-size: var(--text-base)">계정 관리</h3>
            <p class="page-header__desc" style="font-size: var(--text-xs); margin-top: 3px">아래 작업은 즉시 실행됩니다.</p>
          </div>
          <div style="padding: var(--space-3); display: flex; flex-direction: column; gap: var(--space-2)">
            <button v-if="detail.isActive" class="action-row" @click="handleDeactivate">
              <span class="action-row__icon" style="background: var(--color-warning-tint); color: var(--color-warning)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0" /><path d="M12 2v10" /></svg>
              </span>
              <span>
                <span class="action-row__title">계정 비활성화</span>
                <span class="action-row__sub">로그인을 즉시 차단</span>
              </span>
            </button>
            <button v-else class="action-row" @click="handleActivate">
              <span class="action-row__icon" style="background: var(--color-success-tint); color: var(--color-success)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0" /><path d="M12 2v10" /></svg>
              </span>
              <span>
                <span class="action-row__title">계정 다시 활성화</span>
                <span class="action-row__sub">로그인을 다시 허용</span>
              </span>
            </button>
            <button class="action-row" @click="handleResetPassword">
              <span class="action-row__icon" style="background: var(--color-brand-tint); color: var(--color-brand)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M23 4v6h-6" /><path d="M1 20v-6h6" /><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10" /><path d="M1 14l4.64 4.36A9 9 0 0 0 20.49 15" /></svg>
              </span>
              <span>
                <span class="action-row__title">임시 비밀번호 발급</span>
                <span class="action-row__sub">다음 로그인 시 변경 강제</span>
              </span>
            </button>
            <button class="action-row action-row--danger" @click="handleDelete">
              <span class="action-row__icon" style="background: var(--color-danger-tint); color: var(--color-danger)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18" /><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /></svg>
              </span>
              <span>
                <span class="action-row__title" style="color: var(--color-danger)">계정 삭제</span>
                <span class="action-row__sub">되돌릴 수 없습니다</span>
              </span>
            </button>
          </div>

          <p v-if="tempPasswordResult" class="field-hint" style="margin: 0 var(--space-4) var(--space-4)">
            임시 비밀번호가 발급되었습니다: <strong>{{ tempPasswordResult.tempPassword }}</strong>
            (해당 직원에게 안전한 방법으로 전달해주세요.)
          </p>
        </div>
      </div>

      <div class="section">
        <div class="page-header">
          <div>
            <h3>패스워드 관리대장</h3>
            <p class="page-header__desc">{{ detail.name }} 님의 계정 비밀번호 변경 이력입니다.</p>
          </div>
          <button class="btn btn-primary btn-sm" @click="openPwCreate">항목 등록하기</button>
        </div>

        <div v-if="showPwForm" class="modal-overlay" @click.self="showPwForm = false">
          <div class="modal-panel modal-panel--form">
            <div class="modal-panel__head">
              <h3>{{ editingPwId ? '항목 수정' : '항목 등록' }}</h3>
              <button class="icon-btn" title="닫기" @click="showPwForm = false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
              </button>
            </div>
            <div class="modal-panel__body">
              <p v-if="pwError" class="form-error">{{ pwError }}</p>
              <p v-if="editingPwRejectedEntry" class="form-error">
                반려됨 · {{ editingPwRejectedEntry.rejectedBy }}: {{ editingPwRejectedEntry.rejectReason }}
                <br />저장하면 다시 승인대기 상태로 등록됩니다.
              </p>

              <div class="form-grid">
                <div class="field" style="margin-bottom: 0">
                  <label>계정유형</label>
                  <select v-model="pwForm.typeId" class="input">
                    <option :value="null" disabled>선택하세요</option>
                    <option v-for="t in accountTypes" :key="t.typeId" :value="t.typeId">{{ t.typeName }}</option>
                  </select>
                </div>
                <div class="field" style="margin-bottom: 0">
                  <label>계정ID (선택)</label>
                  <input v-model="pwForm.accountId" class="input" type="text" />
                </div>
                <div class="field" style="margin-bottom: 0">
                  <label>비밀번호 만료일 (선택)</label>
                  <input v-model="pwForm.expireAt" class="input" type="date" />
                  <div class="form-quick-expire form-quick-expire--nested">
                    <span class="text-faint" style="font-size: var(--text-xs); white-space: nowrap">오늘부터</span>
                    <input
                      v-model.number="pwExpireDays"
                      class="input"
                      type="number"
                      min="1"
                      style="width: 56px"
                      placeholder="일수"
                      @keyup.enter="applyPwExpireDays"
                    />
                    <span class="text-faint" style="font-size: var(--text-xs); white-space: nowrap">일 후</span>
                    <button type="button" class="btn btn-ghost btn-sm" @click="applyPwExpireDays">적용</button>
                  </div>
                </div>
                <div v-if="editingPwId" class="field" style="margin-bottom: 0">
                  <label>비밀번호 변경일</label>
                  <input v-model="pwForm.changedAt" class="input" type="datetime-local" />
                </div>
              </div>

              <button type="button" class="form-more-toggle" @click="showMorePwFields = !showMorePwFields">
                {{ showMorePwFields ? '△ 추가 항목 접기' : '＋ 요청자 · 변경사유 · 비고 추가 (선택)' }}
              </button>

              <div v-if="showMorePwFields" class="form-grid" style="margin-top: var(--space-3)">
                <div class="field" style="margin-bottom: 0">
                  <label>요청자 (선택)</label>
                  <input v-model="pwForm.requesterName" class="input" type="text" placeholder="본인" />
                </div>
                <div class="field" style="margin-bottom: 0">
                  <label>비밀번호 변경사유 (선택)</label>
                  <input v-model="pwForm.changeReason" class="input" type="text" />
                </div>
                <div class="field" style="margin-bottom: 0; grid-column: 1 / -1">
                  <label>비고 (선택)</label>
                  <input v-model="pwForm.note" class="input" type="text" />
                </div>
              </div>
            </div>
            <div class="modal-panel__foot">
              <button class="btn btn-ghost" @click="showPwForm = false">취소</button>
              <button class="btn btn-primary" @click="submitPwForm">저장하기</button>
            </div>
          </div>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>생성일</th>
                <th>갱신일자</th>
                <th>최종수정자</th>
                <th>계정유형</th>
                <th>요청자</th>
                <th>계정ID</th>
                <th>비밀번호 변경일</th>
                <th>비밀번호 만료일</th>
                <th>비밀번호 변경사유</th>
                <th>승인</th>
                <th>비고</th>
                <th>잔여</th>
                <th>상태</th>
                <th style="text-align: right">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in pwEntries" :key="entry.pwId" :style="pwRowStyle(entry.expireAt)">
                <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ entry.createdAt?.slice(0, 10) || '-' }}</td>
                <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ entry.updatedAt?.slice(0, 10) || '-' }}</td>
                <td class="text-muted">{{ entry.lastModifier || '-' }}</td>
                <td><span class="code-tag">{{ entry.typeName || typeName(entry.typeId) }}</span></td>
                <td class="text-muted">{{ entry.requesterName || '-' }}</td>
                <td style="font-family: var(--font-mono); font-weight: 600">{{ entry.accountId || '-' }}</td>
                <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ entry.changedAt?.slice(0, 10) || '-' }}</td>
                <td style="font-family: var(--font-mono); font-weight: 600; color: var(--color-heading-soft)">{{ entry.expireAt || '-' }}</td>
                <td class="text-muted">{{ entry.changeReason || '-' }}</td>
                <td>
                  <span v-if="isApprovedPw(entry)" style="display: inline-flex; align-items: center; gap: 6px" :title="`승인일시: ${entry.approvedAt?.slice(0, 16).replace('T', ' ')}`">
                    <span class="badge badge-success">승인완료</span>
                    <span style="font-weight: 600; color: var(--color-heading-soft); font-size: var(--text-xs)">{{ entry.approverName }}</span>
                  </span>
                  <span v-else-if="isRejectedPw(entry)" style="display: inline-flex; align-items: center; gap: 6px" :title="`반려일시: ${entry.rejectedAt?.slice(0, 16).replace('T', ' ')} · 사유: ${entry.rejectReason}`">
                    <span class="badge badge-danger">반려됨</span>
                    <span style="font-weight: 600; color: var(--color-heading-soft); font-size: var(--text-xs)">{{ entry.rejectedBy }}</span>
                  </span>
                  <span v-else class="badge badge-warning">승인대기</span>
                </td>
                <td class="text-muted">{{ entry.note || '-' }}</td>
                <td>
                  <span v-if="pwRemaining(entry.expireAt)" :style="{ color: pwRemaining(entry.expireAt).color, fontWeight: 600, fontSize: 'var(--text-xs)' }">{{ pwRemaining(entry.expireAt).text }}</span>
                  <span v-else class="text-faint">-</span>
                </td>
                <td>
                  <span v-if="pwStatus(entry.expireAt)" class="badge" :class="pwStatusClass(entry.expireAt)">{{ pwStatus(entry.expireAt) }}</span>
                  <span v-else class="text-faint">-</span>
                </td>
                <td style="text-align: right">
                  <span style="display: inline-flex; gap: 4px">
                    <button v-if="canApprovePw(entry)" class="icon-btn" title="승인하기" @click="approvePwEntry(entry)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5" /></svg>
                    </button>
                    <button v-if="canApprovePw(entry)" class="icon-btn icon-btn--danger" title="반려하기" @click="openRejectPwModal(entry)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
                    </button>
                    <button class="icon-btn" title="수정하기" @click="openPwEdit(entry)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9" /><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z" /></svg>
                    </button>
                    <button class="icon-btn icon-btn--danger" title="삭제하기" @click="removePwEntry(entry)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18" /><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /></svg>
                    </button>
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-if="!pwLoading && pwEntries.length === 0" class="empty-state">등록된 항목이 없습니다.</p>
        </div>
      </div>

      <div v-if="showRejectPwModal" class="modal-overlay" @click.self="showRejectPwModal = false">
        <div class="modal-panel" style="max-width: 420px">
          <div class="modal-panel__header">
            <h3>반려하기</h3>
            <button class="btn btn-ghost btn-sm" @click="showRejectPwModal = false">닫기</button>
          </div>
          <p style="font-size: var(--text-sm); color: var(--color-text-muted); margin-bottom: var(--space-3)">
            '{{ rejectPwTarget?.accountId || rejectPwTarget?.userName }}' 항목을 반려합니다. 사유는 등록자에게 알림으로 전달됩니다.
          </p>
          <p v-if="rejectPwError" class="form-error">{{ rejectPwError }}</p>
          <div class="field">
            <label>반려 사유</label>
            <input v-model="rejectPwReason" class="input" type="text" placeholder="예: 계정ID를 다시 확인해주세요" @keyup.enter="submitRejectPw" />
          </div>
          <div class="form-actions">
            <button class="btn btn-danger" @click="submitRejectPw">반려하기</button>
            <button class="btn btn-ghost" @click="showRejectPwModal = false">취소</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
