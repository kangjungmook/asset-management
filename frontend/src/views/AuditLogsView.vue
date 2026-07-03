<script setup>
import { onMounted, ref } from 'vue'
import { listAuditLogs } from '@/api/auditLogs'
import PaginationBar from '@/components/PaginationBar.vue'

const logs = ref([])
const loading = ref(false)
const filters = ref({ action: '', from: '', to: '' })
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const PAGE_SIZE = 20

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: PAGE_SIZE }
    if (filters.value.action) params.action = filters.value.action
    if (filters.value.from) params.from = filters.value.from
    if (filters.value.to) params.to = filters.value.to
    const result = await listAuditLogs(params)
    logs.value = result.content
    totalPages.value = result.totalPages
    totalElements.value = result.totalElements
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function changePage(newPage) {
  page.value = newPage
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1>감사 로그</h1>
        <p class="page-header__desc">로그인, 계정 변경, 권한 변경 등 시스템 전반의 활동 기록입니다.</p>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-grid">
        <div class="field" style="margin-bottom: 0">
          <label for="action">액션</label>
          <input id="action" v-model="filters.action" class="input" type="text" placeholder="예: LOGIN" />
        </div>
        <div class="field" style="margin-bottom: 0">
          <label for="from">시작일</label>
          <input id="from" v-model="filters.from" class="input" type="date" />
        </div>
        <div class="field" style="margin-bottom: 0">
          <label for="to">종료일</label>
          <input id="to" v-model="filters.to" class="input" type="date" />
        </div>
      </div>
      <div class="form-actions">
        <button class="btn btn-primary" @click="search">조회하기</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>일시</th>
            <th>사용자</th>
            <th>액션</th>
            <th>대상</th>
            <th>상세</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.logId">
            <td class="text-muted">{{ log.createdAt?.replace('T', ' ').slice(0, 19) }}</td>
            <td>{{ log.userName || '-' }}</td>
            <td><span class="code-tag">{{ log.action }}</span></td>
            <td class="text-muted">{{ log.target }}</td>
            <td class="text-muted">{{ log.detail }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && logs.length === 0" class="empty-state">조회된 로그가 없습니다.</p>
      <PaginationBar :page="page" :total-pages="totalPages" :total-elements="totalElements" @change="changePage" />
    </div>
  </div>
</template>
