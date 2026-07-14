<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { listUsers, createUser } from '@/api/users'
import { listDepartments, createDepartment } from '@/api/departments'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()

const users = ref([])
const departments = ref([])
const loading = ref(false)
const keyword = ref('')
const deptFilter = ref('')
const statusFilter = ref('')
let searchTimer = null

const filteredUsers = computed(() =>
  users.value.filter((u) => {
    if (deptFilter.value && String(u.deptId) !== deptFilter.value) return false
    if (statusFilter.value === 'active' && !u.isActive) return false
    if (statusFilter.value === 'inactive' && u.isActive) return false
    return true
  }),
)

function roleLabel(user) {
  if (user.isAdmin) return '관리자'
  if (user.isDeptManager) return '부서 관리자'
  return '일반 사용자'
}

const showCreateForm = ref(false)
const createForm = ref({ employeeNo: '', name: '', deptId: null, role: '' })
const createError = ref('')
const employeeNoInput = ref(null)
const nameInput = ref(null)
const deptSelect = ref(null)
const roleSelect = ref(null)
const showQuickDept = ref(false)
const quickDeptName = ref('')
const quickDeptLoading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    users.value = await listUsers({ keyword: keyword.value || undefined })
  } finally {
    loading.value = false
  }
}

function handleSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(loadUsers, 300)
}

async function loadDepartments() {
  departments.value = await listDepartments()
}

function openCreate() {
  createForm.value = {
    employeeNo: '',
    name: '',
    deptId: auth.isAdmin ? null : auth.dept?.deptId,
    role: '',
  }
  createError.value = ''
  showQuickDept.value = false
  quickDeptName.value = ''
  showCreateForm.value = true
}

async function submitQuickDept() {
  const name = quickDeptName.value.trim()
  if (!name || quickDeptLoading.value) return
  quickDeptLoading.value = true
  try {
    const dept = await createDepartment({ deptName: name })
    await loadDepartments()
    createForm.value.deptId = dept.deptId
    quickDeptName.value = ''
    showQuickDept.value = false
    ui.success('부서가 추가되었습니다.')
  } catch (err) {
    ui.error(err?.response?.data?.message || '부서 추가에 실패했습니다.')
  } finally {
    quickDeptLoading.value = false
  }
}

function validateCreate() {
  if (!createForm.value.employeeNo.trim()) {
    createError.value = '사번을 입력해주세요.'
    employeeNoInput.value?.focus()
    return false
  }
  if (!createForm.value.name.trim()) {
    createError.value = '이름을 입력해주세요.'
    nameInput.value?.focus()
    return false
  }
  if (createForm.value.role === 'DEPT_MANAGER' && !createForm.value.deptId) {
    createError.value = '부서 관리자는 부서를 지정해야 합니다.'
    deptSelect.value?.focus()
    return false
  }
  return true
}

async function submitCreate() {
  createError.value = ''
  if (!validateCreate()) return
  try {
    await createUser(createForm.value)
    ui.success('사용자가 생성되었습니다.')
    showCreateForm.value = false
    await loadUsers()
  } catch (err) {
    createError.value = err?.response?.data?.message || '사용자 생성에 실패했습니다.'
    if (createError.value.includes('부서')) {
      deptSelect.value?.focus()
    } else if (createError.value.includes('역할')) {
      roleSelect.value?.focus()
    } else if (createError.value.includes('사번')) {
      employeeNoInput.value?.focus()
    }
  }
}

function openDetail(user) {
  router.push(`/manage/users/${user.userId}`)
}

const pageTitle = computed(() => (auth.isAdmin ? '사용자 관리' : '사용자 관리 (부서)'))

onMounted(async () => {
  try {
    await Promise.all([loadUsers(), loadDepartments()])
  } catch {
    // session-expiry / 401 redirect is already handled by the http interceptor
  }
})
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1>{{ pageTitle }}</h1>
        <p class="page-header__desc">계정 정보, 역할, 개별 권한을 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
        사용자 추가
      </button>
    </div>

    <div v-if="showCreateForm" class="modal-overlay" @click.self="showCreateForm = false">
      <div class="modal-panel" style="max-width: 480px">
        <div class="modal-panel__header">
          <h3>사용자 추가</h3>
          <button class="btn btn-ghost btn-sm" @click="showCreateForm = false">닫기</button>
        </div>
        <p v-if="createError" class="form-error">{{ createError }}</p>
        <div class="form-grid">
          <div class="field" style="margin-bottom: 0">
            <label>사번</label>
            <input ref="employeeNoInput" v-model="createForm.employeeNo" class="input" type="text" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>이름</label>
            <input ref="nameInput" v-model="createForm.name" class="input" type="text" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label style="display: flex; align-items: center; justify-content: space-between">
              <span>부서</span>
              <button
                v-if="auth.isAdmin"
                type="button"
                class="icon-btn"
                title="새 부서 추가"
                style="width: 20px; height: 20px"
                @click="showQuickDept = !showQuickDept"
              >
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
              </button>
            </label>
            <select ref="deptSelect" v-model="createForm.deptId" class="input" :disabled="!auth.isAdmin">
              <option :value="null">미배정</option>
              <option v-for="d in departments" :key="d.deptId" :value="d.deptId">{{ d.deptName }}</option>
            </select>
            <div v-if="showQuickDept" class="form-quick-expire" style="margin-top: 8px">
              <input
                v-model="quickDeptName"
                class="input"
                type="text"
                placeholder="새 부서명"
                @keyup.enter="submitQuickDept"
              />
              <button type="button" class="btn btn-ghost btn-sm" :disabled="quickDeptLoading" @click="submitQuickDept">추가</button>
            </div>
          </div>
          <div v-if="auth.isAdmin" class="field" style="margin-bottom: 0">
            <label>역할</label>
            <select ref="roleSelect" v-model="createForm.role" class="input">
              <option value="">일반 사용자</option>
              <option value="DEPT_MANAGER">부서 관리자</option>
            </select>
          </div>
        </div>
        <p class="field-hint" style="margin-top: var(--space-1)">
          초기 비밀번호는 <strong>1234</strong>로 자동 설정됩니다. 최초 로그인 시 새 비밀번호로 변경하라는 안내가 표시됩니다.
        </p>
        <div class="form-actions">
          <button class="btn btn-primary" @click="submitCreate">저장하기</button>
          <button class="btn btn-ghost" @click="showCreateForm = false">닫기</button>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <div class="toolbar" style="padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--color-border); margin-bottom: 0">
        <label class="search-box" style="max-width: 320px">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4-4" /></svg>
          <input v-model="keyword" type="text" placeholder="이름·사번·부서 검색" @input="handleSearchInput" />
        </label>
        <select v-model="deptFilter" class="input" style="max-width: 150px">
          <option value="">전체 부서</option>
          <option v-for="d in departments" :key="d.deptId" :value="String(d.deptId)">{{ d.deptName }}</option>
        </select>
        <select v-model="statusFilter" class="input" style="max-width: 150px">
          <option value="">전체 상태</option>
          <option value="active">활성</option>
          <option value="inactive">비활성</option>
        </select>
        <div style="flex: 1"></div>
        <div style="font-size: var(--text-xs); color: var(--color-text-faint)">
          총 <strong style="color: var(--color-heading-soft); font-family: var(--font-mono)">{{ filteredUsers.length }}</strong>명
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>사번</th>
            <th>이름</th>
            <th>부서</th>
            <th>역할</th>
            <th>상태</th>
            <th>마지막 접속</th>
            <th style="text-align: right">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.userId" class="data-table__row-link" @click="openDetail(user)">
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ user.employeeNo }}</td>
            <td style="font-weight: 600; color: var(--color-heading-soft)">{{ user.name }}</td>
            <td class="text-muted">{{ user.deptName || '미배정' }}</td>
            <td class="text-muted">{{ roleLabel(user) }}</td>
            <td>
              <span class="badge" :class="user.isActive ? 'badge-success' : 'badge-danger'" style="gap: 5px">
                <span style="width: 5px; height: 5px; border-radius: 50%; background: currentColor"></span>
                {{ user.isActive ? '활성' : '비활성' }}
              </span>
            </td>
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ user.lastLoginAt ? user.lastLoginAt.replace('T', ' ').slice(0, 16) : '-' }}</td>
            <td style="text-align: right">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-text-faint)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6" /></svg>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && filteredUsers.length === 0" class="empty-state">등록된 사용자가 없습니다.</p>
    </div>
  </div>
</template>
