<script setup>
import { onMounted, ref } from 'vue'
import {
  listPasswords,
  createPassword,
  updatePassword,
  deletePassword,
} from '@/api/passwords'
import { listAccountTypes } from '@/api/accountTypes'
import { listUsers } from '@/api/users'
import { useUiStore } from '@/stores/ui'
import PaginationBar from '@/components/PaginationBar.vue'

const ui = useUiStore()
const entries = ref([])
const accountTypes = ref([])
const users = ref([])
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const keyword = ref('')
const PAGE_SIZE = 20
let searchTimer = null

const showForm = ref(false)
const editingId = ref(null)
const errorMessage = ref('')

function emptyForm() {
  return {
    typeId: null,
    userId: null,
    approverId: null,
    accountId: '',
    changedAt: '',
    expireAt: '',
    changeReason: '',
    note: '',
  }
}
const form = ref(emptyForm())

function isExpiringSoon(expireAt) {
  if (!expireAt) return false
  const diffDays = (new Date(expireAt) - new Date()) / (1000 * 60 * 60 * 24)
  return diffDays <= 30
}

async function load() {
  loading.value = true
  try {
    const result = await listPasswords({
      keyword: keyword.value || undefined,
      page: page.value,
      size: PAGE_SIZE,
    })
    entries.value = result.content
    totalPages.value = result.totalPages
    totalElements.value = result.totalElements
  } finally {
    loading.value = false
  }
}

function changePage(newPage) {
  page.value = newPage
  load()
}

function handleSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    load()
  }, 300)
}

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  errorMessage.value = ''
  showForm.value = true
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
  showForm.value = true
}

async function submitForm() {
  errorMessage.value = ''
  try {
    if (editingId.value) {
      await updatePassword(editingId.value, form.value)
      ui.success('패스워드 관리대장이 수정되었습니다.')
    } else {
      await createPassword(form.value)
      ui.success('패스워드 관리대장이 등록되었습니다.')
    }
    showForm.value = false
    await load()
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function removeEntry(entry) {
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
    await load()
  } catch (err) {
    ui.error(err?.response?.data?.message || '삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  await Promise.all([
    load(),
    listAccountTypes().then((v) => (accountTypes.value = v)),
    listUsers().then((v) => (users.value = v)),
  ])
})
</script>

<template>
  <div> 
    <div class="page-header">
      <div>
        <h1>패스워드 관리대장</h1>
        <p class="page-header__desc">계정 비밀번호 변경 이력을 기록합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">항목 등록하기</button>
    </div>

    <div class="toolbar">
      <label class="search-box">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4-4" /></svg>
        <input v-model="keyword" type="text" placeholder="계정 ID 또는 대상자 검색" @input="handleSearchInput" />
      </label>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-panel" style="max-width: 560px">
        <div class="modal-panel__header">
          <h3>{{ editingId ? '항목 수정' : '항목 등록' }}</h3>
          <button class="btn btn-ghost btn-sm" @click="showForm = false">닫기</button>
        </div>
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <div class="form-grid">
          <div class="field" style="margin-bottom: 0">
            <label>계정유형</label>
            <select v-model="form.typeId" class="input">
              <option :value="null" disabled>선택하세요</option>
              <option v-for="t in accountTypes" :key="t.typeId" :value="t.typeId">{{ t.typeName }}</option>
            </select>
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>대상 사용자</label>
            <select v-model="form.userId" class="input">
              <option :value="null" disabled>선택하세요</option>
              <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.name }} ({{ u.employeeNo }})</option>
            </select>
          </div>
          <div class="field" style="margin-bottom: 0">
            <label>승인자</label>
            <select v-model="form.approverId" class="input">
              <option :value="null">없음</option>
              <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.name }} ({{ u.employeeNo }})</option>
            </select>
          </div>
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
          <button class="btn btn-primary" @click="submitForm">저장하기</button>
          <button class="btn btn-ghost" @click="showForm = false">닫기</button>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>계정유형</th>
            <th>계정 ID</th>
            <th>대상자</th>
            <th>부서</th>
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
          <tr v-for="entry in entries" :key="entry.pwId">
            <td>{{ entry.typeName }}</td>
            <td>{{ entry.accountId || '-' }}</td>
            <td>{{ entry.userName }}</td>
            <td class="text-muted">{{ entry.userDeptName || '-' }}</td>
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
              <button class="btn btn-ghost btn-sm" @click="openEdit(entry)">수정하기</button>
              <button class="btn btn-danger btn-sm" @click="removeEntry(entry)">삭제하기</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && entries.length === 0" class="empty-state">등록된 항목이 없습니다.</p>
      <PaginationBar :page="page" :total-pages="totalPages" :total-elements="totalElements" @change="changePage" />
    </div>
  </div>
</template>
