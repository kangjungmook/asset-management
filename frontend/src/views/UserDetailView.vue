<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const ALL_ROLES = ['DEPT_MANAGER']

const pwEntries = ref([])
const accountTypes = ref([])
const pwLoading = ref(true)
const showPwForm = ref(false)
const editingPwId = ref(null)
const pwError = ref('')

function emptyPwForm() {
  return {
    typeId: null,
    userId: userId.value,
    approverId: null,
    accountId: '',
    changedAt: '',
    expireAt: '',
    changeReason: '',
    note: '',
  }
}
const pwForm = ref(emptyPwForm())

async function loadDetail() {
  detail.value = await getUser(userId.value)
  detailForm.value = { name: detail.value.name, deptId: detail.value.deptId }
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
  return detail.value?.roles?.includes(role)
}

function hasPermission(permId) {
  return detail.value?.permissions?.some((p) => p.permId === permId)
}

async function submitDetail() {
  detailError.value = ''
  try {
    await updateUser(userId.value, detailForm.value)
    ui.success('사용자 정보가 저장되었습니다.')
    await loadDetail()
  } catch (err) {
    detailError.value = err?.response?.data?.message || '수정에 실패했습니다.'
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

async function toggleRole(role, checked) {
  if (checked) {
    await grantRole(userId.value, role)
  } else {
    await revokeRole(userId.value, role)
  }
  await loadDetail()
}

async function togglePermission(perm, checked) {
  if (checked) {
    await grantPermission(userId.value, perm.permId)
  } else {
    const granted = detail.value.permissions.find((p) => p.permId === perm.permId)
    await revokePermission(userId.value, granted?.id ?? perm.permId)
  }
  await loadDetail()
}

function typeName(typeId) {
  return accountTypes.value.find((t) => t.typeId === typeId)?.typeName || '-'
}

function isExpiringSoon(expireAt) {
  if (!expireAt) return false
  const diffDays = (new Date(expireAt) - new Date()) / (1000 * 60 * 60 * 24)
  return diffDays <= 30
}

function openPwCreate() {
  editingPwId.value = null
  pwForm.value = emptyPwForm()
  pwError.value = ''
  showPwForm.value = true
}

function openPwEdit(entry) {
  editingPwId.value = entry.pwId
  pwForm.value = {
    typeId: entry.typeId,
    userId: userId.value,
    approverId: entry.approverId,
    accountId: entry.accountId,
    changedAt: entry.changedAt ? entry.changedAt.slice(0, 16) : '',
    expireAt: entry.expireAt,
    changeReason: entry.changeReason,
    note: entry.note,
  }
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
    auth.isAdmin ? listDepartments().then((v) => (departments.value = v)) : Promise.resolve(),
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

      <div class="section" style="padding-top: 0">
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

        <div class="form-actions" style="margin-bottom: var(--space-3); flex-wrap: wrap">
          <button class="btn btn-primary btn-sm" @click="submitDetail">정보 저장하기</button>
          <button v-if="detail.isActive" class="btn btn-ghost btn-sm" @click="handleDeactivate">비활성화하기</button>
          <button v-else class="btn btn-ghost btn-sm" @click="handleActivate">다시 활성화하기</button>
          <button class="btn btn-ghost btn-sm" @click="handleResetPassword">임시 비밀번호 발급하기</button>
        </div>
        <div class="form-actions">
          <button class="btn btn-danger btn-sm" @click="handleDelete">계정 완전 삭제하기</button>
        </div>

        <p v-if="tempPasswordResult" class="field-hint" style="margin-top: var(--space-4)">
          임시 비밀번호가 발급되었습니다: <strong>{{ tempPasswordResult.tempPassword }}</strong>
          (해당 직원에게 안전한 방법으로 전달해주세요.)
        </p>
      </div>

      <div class="section">
        <template v-if="auth.isAdmin">
          <h3 style="margin-bottom: var(--space-3)">역할</h3>
          <div style="display: flex; gap: var(--space-6); margin-bottom: var(--space-2)">
            <label v-for="role in ALL_ROLES" :key="role" class="checkbox-row">
              <input type="checkbox" :checked="hasRole(role)" @change="toggleRole(role, $event.target.checked)" />
              부서 관리자
            </label>
          </div>
          <p class="field-hint" style="margin-bottom: var(--space-6)">체크 해제 시 기본값인 일반 사용자로 취급됩니다.</p>
        </template>

        <h3 style="margin-bottom: var(--space-3)">개별 권한</h3>
        <div style="display: flex; flex-direction: column; gap: var(--space-2)">
          <label v-for="perm in allPermissions" :key="perm.permId" class="checkbox-row">
            <input type="checkbox" :checked="hasPermission(perm.permId)" @change="togglePermission(perm, $event.target.checked)" />
            {{ perm.permName }} <span class="text-faint" style="font-size: var(--text-xs)">({{ perm.permCode }})</span>
          </label>
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

        <p v-if="pwError" class="form-error">{{ pwError }}</p>

        <div v-if="showPwForm" class="modal-overlay" @click.self="showPwForm = false">
          <div class="modal-panel" style="max-width: 480px">
            <div class="modal-panel__header">
              <h3>{{ editingPwId ? '항목 수정' : '항목 등록' }}</h3>
              <button class="btn btn-ghost btn-sm" @click="showPwForm = false">닫기</button>
            </div>
            <div class="form-grid">
              <div class="field" style="margin-bottom: 0">
                <label>계정유형</label>
                <select v-model="pwForm.typeId" class="input">
                  <option :value="null" disabled>선택하세요</option>
                  <option v-for="t in accountTypes" :key="t.typeId" :value="t.typeId">{{ t.typeName }}</option>
                </select>
              </div>
              <div class="field" style="margin-bottom: 0">
                <label>계정 ID</label>
                <input v-model="pwForm.accountId" class="input" type="text" />
              </div>
              <div class="field" style="margin-bottom: 0">
                <label>변경일시</label>
                <input v-model="pwForm.changedAt" class="input" type="datetime-local" />
              </div>
              <div class="field" style="margin-bottom: 0">
                <label>만료일</label>
                <input v-model="pwForm.expireAt" class="input" type="date" />
              </div>
              <div class="field" style="margin-bottom: 0">
                <label>변경 사유</label>
                <input v-model="pwForm.changeReason" class="input" type="text" />
              </div>
              <div class="field" style="margin-bottom: 0">
                <label>비고</label>
                <input v-model="pwForm.note" class="input" type="text" />
              </div>
            </div>
            <div class="form-actions">
              <button class="btn btn-primary" @click="submitPwForm">저장하기</button>
              <button class="btn btn-ghost" @click="showPwForm = false">닫기</button>
            </div>
          </div>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>계정유형</th>
                <th>계정 ID</th>
                <th>요청자</th>
                <th>승인자</th>
                <th>변경일</th>
                <th>만료일</th>
                <th>변경 사유</th>
                <th>비고</th>
                <th>최종수정자</th>
                <th>생성일</th>
                <th>갱신일자</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in pwEntries" :key="entry.pwId">
                <td>{{ entry.typeName || typeName(entry.typeId) }}</td>
                <td>{{ entry.accountId || '-' }}</td>
                <td class="text-muted">{{ entry.requesterName || '-' }}</td>
                <td class="text-muted">{{ entry.approverName || '-' }}</td>
                <td class="text-muted">{{ entry.changedAt?.slice(0, 10) || '-' }}</td>
                <td>
                  <span :class="['badge', isExpiringSoon(entry.expireAt) ? 'badge-warning' : '']">
                    {{ entry.expireAt || '-' }}
                  </span>
                </td>
                <td class="text-muted">{{ entry.changeReason || '-' }}</td>
                <td class="text-muted">{{ entry.note || '-' }}</td>
                <td class="text-muted">{{ entry.lastModifier || '-' }}</td>
                <td class="text-muted">{{ entry.createdAt?.slice(0, 10) || '-' }}</td>
                <td class="text-muted">{{ entry.updatedAt?.slice(0, 10) || '-' }}</td>
                <td>
                  <button class="btn btn-ghost btn-sm" @click="openPwEdit(entry)">수정하기</button>
                  <button class="btn btn-danger btn-sm" @click="removePwEntry(entry)">삭제하기</button>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-if="!pwLoading && pwEntries.length === 0" class="empty-state">등록된 항목이 없습니다.</p>
        </div>
      </div>
    </template>
  </div>
</template>
