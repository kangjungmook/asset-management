<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  listPasswords,
  getPasswordSummary,
  createPassword,
  updatePassword,
  deletePassword,
  approvePassword,
  rejectPassword,
} from '@/api/passwords'
import { listAccountTypes } from '@/api/accountTypes'
import { listUsers } from '@/api/users'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import PaginationBar from '@/components/PaginationBar.vue'
import SearchableSelect from '@/components/SearchableSelect.vue'

const ui = useUiStore()
const auth = useAuthStore()
const entries = ref([])
const accountTypes = ref([])
const users = ref([])
const userOptions = computed(() =>
  users.value.map((u) => ({ value: u.userId, label: u.name, sublabel: u.employeeNo })),
)
const editingRejectedEntry = computed(() => {
  if (!editingId.value) return null
  const entry = entries.value.find((e) => e.pwId === editingId.value)
  return entry && entry.status === 'REJECTED' ? entry : null
})
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const summary = ref({ expired: 0, expiringSoon: 0, normal: 0 })
const PAGE_SIZE = 20
let searchTimer = null

const showForm = ref(false)
const editingId = ref(null)
const errorMessage = ref('')
const showMoreFields = ref(false)

function pad2(n) {
  return String(n).padStart(2, '0')
}

function nowDatetimeLocal() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

function emptyForm() {
  return {
    typeId: null,
    userId: null,
    requesterName: '',
    accountId: '',
    changedAt: nowDatetimeLocal(),
    expireAt: '',
    changeReason: '',
    note: '',
  }
}
const form = ref(emptyForm())

const expireDays = ref(null)

function applyExpireDays() {
  const days = Number(expireDays.value)
  if (!days || days <= 0) return
  const base = new Date()
  base.setDate(base.getDate() + days)
  form.value.expireAt = `${base.getFullYear()}-${pad2(base.getMonth() + 1)}-${pad2(base.getDate())}`
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

function isPending(entry) {
  return (entry.status || 'PENDING') === 'PENDING'
}

function isApproved(entry) {
  return entry.status === 'APPROVED'
}

function isRejected(entry) {
  return entry.status === 'REJECTED'
}

function canApprove(entry) {
  if (!isPending(entry)) return false
  if (auth.isAdmin) return true
  if (auth.hasRole('DEPT_MANAGER') && entry.userDeptId != null && entry.userDeptId === auth.dept?.deptId) return true
  return auth.hasPermission('PASSWORD.APPROVE')
}

async function approveEntry(entry) {
  const confirmed = await ui.confirm({
    title: '승인할까요?',
    message: `'${entry.accountId || entry.userName}' 항목의 비밀번호 변경을 승인합니다.`,
    confirmText: '승인하기',
  })
  if (!confirmed) return
  try {
    await approvePassword(entry.pwId)
    ui.success('승인되었습니다.')
    await load()
  } catch (err) {
    ui.error(err?.response?.data?.message || '승인에 실패했습니다.')
  }
}

const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectReason = ref('')
const rejectError = ref('')

function openRejectModal(entry) {
  rejectTarget.value = entry
  rejectReason.value = ''
  rejectError.value = ''
  showRejectModal.value = true
}

async function submitReject() {
  if (!rejectReason.value.trim()) {
    rejectError.value = '반려 사유를 입력해주세요.'
    return
  }
  try {
    await rejectPassword(rejectTarget.value.pwId, rejectReason.value.trim())
    ui.success('반려되었습니다.')
    showRejectModal.value = false
    await load()
  } catch (err) {
    rejectError.value = err?.response?.data?.message || '반려에 실패했습니다.'
  }
}

function pwRemaining(expireAt) {
  if (!expireAt) return null
  const diffDays = Math.ceil((new Date(expireAt) - new Date()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return { text: `${Math.abs(diffDays)}일 경과`, color: 'var(--color-danger)' }
  if (diffDays <= 30) return { text: `${diffDays}일 남음`, color: 'var(--color-warning)' }
  return { text: `${diffDays}일 남음`, color: 'var(--color-success)' }
}

async function loadSummary() {
  summary.value = await getPasswordSummary()
}

async function load() {
  loading.value = true
  try {
    const result = await listPasswords({
      keyword: keyword.value || undefined,
      typeId: typeFilter.value || undefined,
      status: statusFilter.value || undefined,
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

function handleFilterChange() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  expireDays.value = null
  showMoreFields.value = false
  errorMessage.value = ''
  showForm.value = true
}

function openEdit(entry) {
  editingId.value = entry.pwId
  form.value = {
    typeId: entry.typeId,
    userId: entry.userId,
    requesterName: entry.requesterName || '',
    accountId: entry.accountId,
    changedAt: entry.changedAt ? entry.changedAt.slice(0, 16) : '',
    expireAt: entry.expireAt,
    changeReason: entry.changeReason,
    note: entry.note,
  }
  expireDays.value = null
  showMoreFields.value = !!(entry.requesterName || entry.changeReason || entry.note)
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
    await Promise.all([load(), loadSummary()])
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
    await Promise.all([load(), loadSummary()])
  } catch (err) {
    ui.error(err?.response?.data?.message || '삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  await Promise.all([
    load(),
    loadSummary(),
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
        <p class="page-header__desc">자산 계정의 비밀번호 변경 이력과 만료를 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">항목 등록하기</button>
    </div>

    <div style="display: flex; gap: var(--space-3); margin-bottom: var(--space-4); flex-wrap: wrap">
      <div style="flex: 1; min-width: 200px; background: #FEF6F5; border: 1px solid #F0DCD9; border-radius: 10px; padding: 12px 15px; display: flex; align-items: center; gap: 11px">
        <span class="stat-card__icon" style="width: 34px; height: 34px; border-radius: 9px; background: #FBE9E7; color: var(--color-danger)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="9" rx="2" /><path d="M8 11V8a4 4 0 0 1 8 0v3" /></svg>
        </span>
        <div>
          <div style="font-size: 20px; font-weight: 700; color: var(--color-danger); font-family: var(--font-mono)">{{ summary.expired }}</div>
          <div style="font-size: 11.5px; color: #8A6A66; font-weight: 500">만료됨 · 즉시 조치 필요</div>
        </div>
      </div>
      <div style="flex: 1; min-width: 200px; background: #FEFBF2; border: 1px solid #F0E3C4; border-radius: 10px; padding: 12px 15px; display: flex; align-items: center; gap: 11px">
        <span class="stat-card__icon" style="width: 34px; height: 34px; border-radius: 9px; background: #FBF0DA; color: var(--color-warning)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="9" rx="2" /><path d="M8 11V8a4 4 0 0 1 8 0v3" /></svg>
        </span>
        <div>
          <div style="font-size: 20px; font-weight: 700; color: var(--color-warning); font-family: var(--font-mono)">{{ summary.expiringSoon }}</div>
          <div style="font-size: 11.5px; color: #8A7A55; font-weight: 500">30일 이내 만료 임박</div>
        </div>
      </div>
      <div style="flex: 1; min-width: 200px; background: #F3F8F7; border: 1px solid #DCEAE7; border-radius: 10px; padding: 12px 15px; display: flex; align-items: center; gap: 11px">
        <span class="stat-card__icon" style="width: 34px; height: 34px; border-radius: 9px; background: #E4F3EC; color: var(--color-success)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="11" width="14" height="9" rx="2" /><path d="M8 11V8a4 4 0 0 1 8 0v3" /></svg>
        </span>
        <div>
          <div style="font-size: 20px; font-weight: 700; color: var(--color-success); font-family: var(--font-mono)">{{ summary.normal }}</div>
          <div style="font-size: 11.5px; color: #5C7A70; font-weight: 500">정상</div>
        </div>
      </div>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-panel modal-panel--form">
        <div class="modal-panel__head">
          <h3>{{ editingId ? '항목 수정' : '항목 등록' }}</h3>
          <button class="icon-btn" title="닫기" @click="showForm = false">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
          </button>
        </div>
        <div class="modal-panel__body">
          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
          <p v-if="editingRejectedEntry" class="form-error">
            반려됨 · {{ editingRejectedEntry.rejectedBy }}: {{ editingRejectedEntry.rejectReason }}
            <br />저장하면 다시 승인대기 상태로 등록됩니다.
          </p>

          <div class="form-grid">
            <div class="field" style="margin-bottom: 0">
              <label>계정유형</label>
              <select v-model="form.typeId" class="input">
                <option :value="null" disabled>선택하세요</option>
                <option v-for="t in accountTypes" :key="t.typeId" :value="t.typeId">{{ t.typeName }}</option>
              </select>
            </div>
            <div class="field" style="margin-bottom: 0">
              <label>계정소유자</label>
              <SearchableSelect v-model="form.userId" :options="userOptions" placeholder="이름 또는 사번으로 검색" />
            </div>
            <div class="field" style="margin-bottom: 0">
              <label>계정ID (선택)</label>
              <input v-model="form.accountId" class="input" type="text" />
            </div>
            <div class="field" style="margin-bottom: 0">
              <label>비밀번호 만료일 (선택)</label>
              <input v-model="form.expireAt" class="input" type="date" />
              <div class="form-quick-expire form-quick-expire--nested">
                <span class="text-faint" style="font-size: var(--text-xs); white-space: nowrap">오늘부터</span>
                <input
                  v-model.number="expireDays"
                  class="input"
                  type="number"
                  min="1"
                  style="width: 56px"
                  placeholder="일수"
                  @keyup.enter="applyExpireDays"
                />
                <span class="text-faint" style="font-size: var(--text-xs); white-space: nowrap">일 후</span>
                <button type="button" class="btn btn-ghost btn-sm" @click="applyExpireDays">적용</button>
              </div>
            </div>
            <div v-if="editingId" class="field" style="margin-bottom: 0">
              <label>비밀번호 변경일</label>
              <input v-model="form.changedAt" class="input" type="datetime-local" />
            </div>
          </div>

          <button type="button" class="form-more-toggle" @click="showMoreFields = !showMoreFields">
            {{ showMoreFields ? '△ 추가 항목 접기' : '＋ 요청자 · 변경사유 · 비고 추가 (선택)' }}
          </button>

          <div v-if="showMoreFields" class="form-grid" style="margin-top: var(--space-3)">
            <div class="field" style="margin-bottom: 0">
              <label>요청자 (선택)</label>
              <input v-model="form.requesterName" class="input" type="text" placeholder="본인" />
            </div>
            <div class="field" style="margin-bottom: 0">
              <label>비밀번호 변경사유 (선택)</label>
              <input v-model="form.changeReason" class="input" type="text" />
            </div>
            <div class="field" style="margin-bottom: 0; grid-column: 1 / -1">
              <label>비고 (선택)</label>
              <input v-model="form.note" class="input" type="text" placeholder="추가로 남길 메모가 있다면 입력하세요" />
            </div>
          </div>
        </div>
        <div class="modal-panel__foot">
          <button class="btn btn-ghost" @click="showForm = false">취소</button>
          <button class="btn btn-primary" @click="submitForm">저장하기</button>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <div class="toolbar" style="padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--color-border); margin-bottom: 0">
        <label class="search-box" style="max-width: 260px">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4-4" /></svg>
          <input v-model="keyword" type="text" placeholder="계정 검색" @input="handleSearchInput" />
        </label>
        <select v-model="typeFilter" class="input" style="max-width: 150px" @change="handleFilterChange">
          <option value="">전체 유형</option>
          <option v-for="t in accountTypes" :key="t.typeId" :value="t.typeId">{{ t.typeName }}</option>
        </select>
        <select v-model="statusFilter" class="input" style="max-width: 150px" @change="handleFilterChange">
          <option value="">전체 상태</option>
          <option value="만료">만료</option>
          <option value="임박">임박</option>
          <option value="정상">정상</option>
        </select>
        <div style="flex: 1"></div>
        <div style="font-size: var(--text-xs); color: var(--color-text-faint)">
          총 <strong style="color: var(--color-heading-soft); font-family: var(--font-mono)">{{ totalElements }}</strong>건
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>생성일</th>
            <th>갱신일자</th>
            <th>최종수정자</th>
            <th>계정유형</th>
            <th>요청자</th>
            <th>계정소유자</th>
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
          <tr v-for="entry in entries" :key="entry.pwId" :style="pwRowStyle(entry.expireAt)">
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ entry.createdAt?.slice(0, 10) || '-' }}</td>
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ entry.updatedAt?.slice(0, 10) || '-' }}</td>
            <td class="text-muted">{{ entry.lastModifier || '-' }}</td>
            <td><span class="code-tag">{{ entry.typeName }}</span></td>
            <td class="text-muted">{{ entry.requesterName || '-' }}</td>
            <td style="font-weight: 600; color: var(--color-heading-soft)">{{ entry.userName }}</td>
            <td style="font-family: var(--font-mono); font-weight: 600">{{ entry.accountId || '-' }}</td>
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ entry.changedAt?.slice(0, 10) || '-' }}</td>
            <td style="font-family: var(--font-mono); font-weight: 600; color: var(--color-heading-soft)">{{ entry.expireAt || '-' }}</td>
            <td class="text-muted">{{ entry.changeReason || '-' }}</td>
            <td>
              <span v-if="isApproved(entry)" style="display: inline-flex; align-items: center; gap: 6px" :title="`승인일시: ${entry.approvedAt?.slice(0, 16).replace('T', ' ')}`">
                <span class="badge badge-success">승인완료</span>
                <span style="font-weight: 600; color: var(--color-heading-soft); font-size: var(--text-xs)">{{ entry.approverName }}</span>
              </span>
              <span v-else-if="isRejected(entry)" style="display: inline-flex; align-items: center; gap: 6px" :title="`반려일시: ${entry.rejectedAt?.slice(0, 16).replace('T', ' ')} · 사유: ${entry.rejectReason}`">
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
                <button v-if="canApprove(entry)" class="icon-btn" title="승인하기" @click="approveEntry(entry)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5" /></svg>
                </button>
                <button v-if="canApprove(entry)" class="icon-btn icon-btn--danger" title="반려하기" @click="openRejectModal(entry)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
                </button>
                <button class="icon-btn" title="수정하기" @click="openEdit(entry)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9" /><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z" /></svg>
                </button>
                <button class="icon-btn icon-btn--danger" title="삭제하기" @click="removeEntry(entry)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18" /><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /></svg>
                </button>
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && entries.length === 0" class="empty-state">등록된 항목이 없습니다.</p>
      <PaginationBar :page="page" :total-pages="totalPages" :total-elements="totalElements" @change="changePage" />
    </div>

    <div v-if="showRejectModal" class="modal-overlay" @click.self="showRejectModal = false">
      <div class="modal-panel" style="max-width: 420px">
        <div class="modal-panel__header">
          <h3>반려하기</h3>
          <button class="btn btn-ghost btn-sm" @click="showRejectModal = false">닫기</button>
        </div>
        <p style="font-size: var(--text-sm); color: var(--color-text-muted); margin-bottom: var(--space-3)">
          '{{ rejectTarget?.accountId || rejectTarget?.userName }}' 항목을 반려합니다. 사유는 등록자에게 알림으로 전달됩니다.
        </p>
        <p v-if="rejectError" class="form-error">{{ rejectError }}</p>
        <div class="field">
          <label>반려 사유</label>
          <input v-model="rejectReason" class="input" type="text" placeholder="예: 계정ID를 다시 확인해주세요" @keyup.enter="submitReject" />
        </div>
        <div class="form-actions">
          <button class="btn btn-danger" @click="submitReject">반려하기</button>
          <button class="btn btn-ghost" @click="showRejectModal = false">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>
