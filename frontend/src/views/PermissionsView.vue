<script setup>
import { onMounted, ref } from 'vue'
import {
  listPermissions,
  createPermission,
  updatePermission,
  deletePermission,
} from '@/api/permissions'
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
const permissions = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const form = ref({ permCode: '', permName: '', description: '' })
const errorMessage = ref('')

async function load() {
  loading.value = true
  try {
    permissions.value = await listPermissions()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  editingSystem.value = false
  form.value = { permCode: '', permName: '', description: '' }
  errorMessage.value = ''
  showForm.value = true
}

const editingSystem = ref(false)

function openEdit(perm) {
  editingId.value = perm.permId
  editingSystem.value = !!perm.system
  form.value = { permCode: perm.permCode, permName: perm.permName, description: perm.description }
  errorMessage.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function submitForm() {
  errorMessage.value = ''
  try {
    if (editingId.value) {
      await updatePermission(editingId.value, form.value)
      ui.success('권한이 수정되었습니다.')
    } else {
      await createPermission(form.value)
      ui.success('권한이 생성되었습니다.')
    }
    showForm.value = false
    await load()
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function removePerm(perm) {
  const confirmed = await ui.confirm({
    title: '권한을 삭제할까요?',
    message: `'${perm.permName}' 권한을 삭제합니다. 이 권한을 부여받은 사용자에게서도 사라집니다.`,
    confirmText: '삭제하기',
    danger: true,
  })
  if (!confirmed) return
  try {
    await deletePermission(perm.permId)
    ui.success('권한이 삭제되었습니다.')
    await load()
  } catch (err) {
    ui.error(err?.response?.data?.message || '삭제에 실패했습니다.')
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1>권한 관리</h1>
        <p class="page-header__desc">개별 권한 코드를 정의하고 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">권한 추가</button>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-panel" style="max-width: 420px">
        <div class="modal-panel__header">
          <h3>{{ editingId ? '권한 수정' : '권한 추가' }}</h3>
          <button class="btn btn-ghost btn-sm" @click="closeForm">닫기</button>
        </div>
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <p v-if="editingSystem" class="field-hint">
          시스템에서 실제로 사용 중인 권한이라 코드는 바꿀 수 없습니다. 이름·설명만 수정 가능합니다.
        </p>
        <div class="field">
          <label for="permCode">권한 코드</label>
          <input id="permCode" v-model="form.permCode" class="input" type="text" :disabled="editingSystem" />
        </div>
        <div class="field">
          <label for="permName">이름</label>
          <input id="permName" v-model="form.permName" class="input" type="text" />
        </div>
        <div class="field">
          <label for="description">설명</label>
          <input id="description" v-model="form.description" class="input" type="text" />
        </div>
        <div class="form-actions">
          <button class="btn btn-primary" @click="submitForm">저장하기</button>
          <button class="btn btn-ghost" @click="closeForm">닫기</button>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>권한 코드</th>
            <th>이름</th>
            <th>설명</th>
            <th>보유자</th>
            <th style="text-align: right">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="perm in permissions" :key="perm.permId">
            <td>
              <span style="display: inline-flex; align-items: center; gap: 6px">
                <span class="code-tag">{{ perm.permCode }}</span>
                <span v-if="perm.system" class="badge badge-brand" title="시스템에서 실제로 사용 중인 권한">시스템</span>
              </span>
            </td>
            <td style="font-weight: 600; color: var(--color-heading-soft)">{{ perm.permName }}</td>
            <td class="text-muted">{{ perm.description || '-' }}</td>
            <td class="text-muted">{{ perm.holderCount ?? 0 }}명</td>
            <td style="text-align: right">
              <span style="display: inline-flex; gap: 4px">
                <button class="icon-btn" title="수정하기" @click="openEdit(perm)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9" /><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z" /></svg>
                </button>
                <button
                  class="icon-btn icon-btn--danger"
                  :title="perm.system ? '시스템 권한은 삭제할 수 없습니다' : '삭제하기'"
                  :disabled="perm.system"
                  @click="removePerm(perm)"
                >
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18" /><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /></svg>
                </button>
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && permissions.length === 0" class="empty-state">등록된 권한이 없습니다.</p>
    </div>
  </div>
</template>
