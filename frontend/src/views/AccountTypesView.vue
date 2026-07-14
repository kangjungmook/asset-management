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
const form = ref({ typeName: '', description: '' })
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
  form.value = { typeName: '', description: '' }
  errorMessage.value = ''
  showForm.value = true
}

function openEdit(type) {
  editingId.value = type.typeId
  form.value = { typeName: type.typeName, description: type.description }
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
          <input id="typeName" v-model="form.typeName" class="input" type="text" />
        </div>
        <div class="field">
          <label for="typeDesc">설명</label>
          <input id="typeDesc" v-model="form.description" class="input" type="text" />
        </div>
        <div class="form-actions">
          <button class="btn btn-primary" @click="submitForm">저장하기</button>
          <button class="btn btn-ghost" @click="closeForm">닫기</button>
        </div>
      </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: var(--space-4)">
      <div v-for="type in accountTypes" :key="type.typeId" class="stat-card">
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span class="stat-card__icon" style="width: 38px; height: 38px; background: var(--color-brand-tint); color: var(--color-brand)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 8c4.42 0 8-1.34 8-3s-3.58-3-8-3-8 1.34-8 3 3.58 3 8 3z" /><path d="M4 5v6c0 1.66 3.58 3 8 3s8-1.34 8-3V5" /><path d="M4 11v6c0 1.66 3.58 3 8 3s8-1.34 8-3v-6" /></svg>
          </span>
          <span style="display: flex; gap: 4px">
            <button class="btn btn-ghost btn-sm" @click="openEdit(type)">수정하기</button>
            <button class="btn btn-danger btn-sm" @click="removeType(type)">삭제하기</button>
          </span>
        </div>
        <div style="display: flex; align-items: baseline; gap: var(--space-2); margin-top: var(--space-3)">
          <div style="font-size: 15.5px; font-weight: 700; color: var(--color-heading)">{{ type.typeName }}</div>
        </div>
        <div class="stat-card__sub" style="margin-top: var(--space-2); line-height: 1.5">{{ type.description || '설명 없음' }}</div>
        <div style="font-size: var(--text-xs); color: var(--color-text-faint); margin-top: var(--space-3); padding-top: var(--space-3); border-top: 1px solid var(--color-border)">
          등록 계정 <strong style="font-family: var(--font-mono); color: var(--color-brand-hover)">{{ type.accountCount ?? 0 }}</strong>개
        </div>
      </div>
      <p v-if="!loading && accountTypes.length === 0" class="empty-state">등록된 계정유형이 없습니다.</p>
    </div>
  </div>
</template>
