<script setup>
import { onMounted, ref } from 'vue'
import {
  listAccountTypes,
  createAccountType,
  updateAccountType,
  deleteAccountType,
} from '@/api/accountTypes'
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
const accountTypes = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const form = ref({ typeName: '' })
const errorMessage = ref('')

async function load() {
  loading.value = true
  try {
    accountTypes.value = await listAccountTypes()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { typeName: '' }
  errorMessage.value = ''
  showForm.value = true
}

function openEdit(type) {
  editingId.value = type.typeId
  form.value = { typeName: type.typeName }
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
      await updateAccountType(editingId.value, form.value)
      ui.success('계정유형이 수정되었습니다.')
    } else {
      await createAccountType(form.value)
      ui.success('계정유형이 생성되었습니다.')
    }
    showForm.value = false
    await load()
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function removeType(type) {
  const confirmed = await ui.confirm({
    title: '계정유형을 삭제할까요?',
    message: `'${type.typeName}' 계정유형을 삭제합니다. 이 유형을 사용하는 패스워드 관리대장 항목이 있다면 삭제에 실패할 수 있습니다.`,
    confirmText: '삭제하기',
    danger: true,
  })
  if (!confirmed) return
  try {
    await deleteAccountType(type.typeId)
    ui.success('계정유형이 삭제되었습니다.')
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
        <h1>계정유형 관리</h1>
        <p class="page-header__desc">패스워드 관리대장에서 사용할 계정유형(서버, NAS, SVN 등)을 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">계정유형 추가하기</button>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-panel" style="max-width: 420px">
        <div class="modal-panel__header">
          <h3>{{ editingId ? '계정유형 수정' : '계정유형 추가' }}</h3>
          <button class="btn btn-ghost btn-sm" @click="closeForm">닫기</button>
        </div>
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <div class="field">
          <label for="typeName">계정유형명</label>
          <input id="typeName" v-model="form.typeName" class="input" type="text" placeholder="예: 서버, NAS, SVN, GitLab" />
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
            <th>계정유형명</th>
            <th>등록일</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="type in accountTypes" :key="type.typeId">
            <td>{{ type.typeName }}</td>
            <td class="text-muted">{{ type.createdAt?.slice(0, 10) }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="openEdit(type)">수정하기</button>
              <button class="btn btn-danger btn-sm" @click="removeType(type)">삭제하기</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && accountTypes.length === 0" class="empty-state">등록된 계정유형이 없습니다.</p>
    </div>
  </div>
</template>
