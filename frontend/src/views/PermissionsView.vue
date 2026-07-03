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
const form = ref({ permCode: '', permName: '' })
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
  form.value = { permCode: '', permName: '' }
  errorMessage.value = ''
  showForm.value = true
}

function openEdit(perm) {
  editingId.value = perm.permId
  form.value = { permCode: perm.permCode, permName: perm.permName }
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
        <p class="page-header__desc">사용자에게 개별로 부여할 수 있는 권한 항목을 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">권한 추가하기</button>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-panel" style="max-width: 420px">
        <div class="modal-panel__header">
          <h3>{{ editingId ? '권한 수정' : '권한 추가' }}</h3>
          <button class="btn btn-ghost btn-sm" @click="closeForm">닫기</button>
        </div>
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <div class="field">
          <label for="permCode">권한 코드</label>
          <input id="permCode" v-model="form.permCode" class="input" type="text" placeholder="예: PASSWORD.EDIT" />
        </div>
        <div class="field">
          <label for="permName">권한명</label>
          <input id="permName" v-model="form.permName" class="input" type="text" />
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
            <th>권한명</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="perm in permissions" :key="perm.permId">
            <td><span class="code-tag">{{ perm.permCode }}</span></td>
            <td>{{ perm.permName }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="openEdit(perm)">수정하기</button>
              <button class="btn btn-danger btn-sm" @click="removePerm(perm)">삭제하기</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && permissions.length === 0" class="empty-state">등록된 권한이 없습니다.</p>
    </div>
  </div>
</template>
