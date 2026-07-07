<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { listUsers, createUser } from '@/api/users'
import { listDepartments } from '@/api/departments'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()

const users = ref([])
const departments = ref([])
const loading = ref(false)
const keyword = ref('')
let searchTimer = null

const showCreateForm = ref(false)
const createForm = ref({ employeeNo: '', name: '', deptId: null })
const createError = ref('')

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
  }
  createError.value = ''
  showCreateForm.value = true
}

async function submitCreate() {
  createError.value = ''
  try {
    await createUser(createForm.value)
    ui.success('사용자가 생성되었습니다.')
    showCreateForm.value = false
    await loadUsers()
  } catch (err) {
    createError.value = err?.response?.data?.message || '사용자 생성에 실패했습니다.'
  }
}

function openDetail(user) {
  router.push(`/manage/users/${user.userId}`)
}

const pageTitle = computed(() => (auth.isAdmin ? '사용자 관리' : '사용자 관리 (부서)'))

onMounted(async () => {
  await Promise.all([loadUsers(), loadDepartments()])
})
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1>{{ pageTitle }}</h1>
        <p class="page-header__desc">계정 정보, 역할, 개별 권한을 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">사용자 추가하기</button>
    </div>

    <div class="toolbar">
      <label class="search-box">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4-4" /></svg>
        <input v-model="keyword" type="text" placeholder="이름 또는 사번 검색" @input="handleSearchInput" />
      </label>
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
            <input v-model="createForm.employeeNo" class="input" type="text" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>이름</label>
            <input v-model="createForm.name" class="input" type="text" />
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>부서</label>
            <select v-model="createForm.deptId" class="input" :disabled="!auth.isAdmin">
              <option :value="null">미배정</option>
              <option v-for="d in departments" :key="d.deptId" :value="d.deptId">{{ d.deptName }}</option>
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
      <table class="data-table">
        <thead>
          <tr>
            <th>사용자</th>
            <th>사번</th>
            <th>부서</th>
            <th>상태</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.userId" class="data-table__row-link" @click="openDetail(user)">
            <td>
              <div style="display: flex; align-items: center; gap: var(--space-3)">
                <span style="width: 30px; height: 30px; border-radius: 8px; background: var(--color-brand-tint); color: var(--color-brand); font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex: 0 0 auto">{{ user.name?.charAt(0) }}</span>
                {{ user.name }}
              </div>
            </td>
            <td class="text-muted">{{ user.employeeNo }}</td>
            <td class="text-muted">{{ user.deptName || '미배정' }}</td>
            <td>
              <span class="badge" :class="user.isActive ? 'badge-success' : 'badge-danger'">
                {{ user.isActive ? '활성' : '비활성' }}
              </span>
            </td>
            <td>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-text-faint)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6" /></svg>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && users.length === 0" class="empty-state">등록된 사용자가 없습니다.</p>
    </div>
  </div>
</template>
