<script setup>
import { onMounted, ref } from 'vue'
import {
  listDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment,
} from '@/api/departments'
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
const departments = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const form = ref({ deptName: '' })
const errorMessage = ref('')

async function load() {
  loading.value = true
  try {
    departments.value = await listDepartments()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { deptName: '' }
  errorMessage.value = ''
  showForm.value = true
}

function openEdit(dept) {
  editingId.value = dept.deptId
  form.value = { deptName: dept.deptName }
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
      await updateDepartment(editingId.value, form.value)
      ui.success('부서가 수정되었습니다.')
    } else {
      await createDepartment(form.value)
      ui.success('부서가 생성되었습니다.')
    }
    showForm.value = false
    await load()
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function removeDept(dept) {
  const confirmed = await ui.confirm({
    title: '부서를 삭제할까요?',
    message: `'${dept.deptName}' 부서를 삭제합니다. 이 작업은 되돌릴 수 없습니다.`,
    confirmText: '삭제하기',
    danger: true,
  })
  if (!confirmed) return
  try {
    await deleteDepartment(dept.deptId)
    ui.success('부서가 삭제되었습니다.')
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
        <h1>부서 관리</h1>
        <p class="page-header__desc">회사 조직 부서를 등록하고 관리합니다.</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">부서 추가하기</button>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-panel" style="max-width: 420px">
        <div class="modal-panel__header">
          <h3>{{ editingId ? '부서 수정' : '부서 추가' }}</h3>
          <button class="btn btn-ghost btn-sm" @click="closeForm">닫기</button>
        </div>
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <div class="field">
          <label for="deptName">부서명</label>
          <input id="deptName" v-model="form.deptName" class="input" type="text" />
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
            <th>부서명</th>
            <th>등록일</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dept in departments" :key="dept.deptId">
            <td>{{ dept.deptName }}</td>
            <td class="text-muted">{{ dept.createdAt?.slice(0, 10) }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="openEdit(dept)">수정하기</button>
              <button class="btn btn-danger btn-sm" @click="removeDept(dept)">삭제하기</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && departments.length === 0" class="empty-state">등록된 부서가 없습니다.</p>
    </div>
  </div>
</template>
