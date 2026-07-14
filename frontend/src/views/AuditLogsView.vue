<script setup>
import { onMounted, ref } from 'vue'
import { listAuditLogs } from '@/api/auditLogs'
import { actionLabel, actionBadgeClass, targetLabel } from '@/utils/auditLog'
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

    <div class="table-wrap">
      <div class="toolbar" style="padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--color-border); margin-bottom: 0">
        <span style="font-size: var(--text-xs); font-weight: 600; color: var(--color-text-faint); display: flex; align-items: center; gap: 6px">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 3H2l8 9.46V19l4 2v-8.54z" /></svg>필터
        </span>
        <input v-model="filters.action" class="input" type="text" placeholder="액션" style="max-width: 180px" @keyup.enter="search" />
        <input v-model="filters.from" class="input" type="date" style="max-width: 160px" />
        <span class="text-faint">~</span>
        <input v-model="filters.to" class="input" type="date" style="max-width: 160px" />
        <button class="btn btn-ghost btn-sm" @click="search">조회하기</button>
        <div style="flex: 1"></div>
        <div style="font-size: var(--text-xs); color: var(--color-text-faint)">
          총 <strong style="color: var(--color-heading); font-family: var(--font-mono)">{{ totalElements }}</strong>건
        </div>
      </div>  
      <table class="data-table">  
        <thead>
          <tr>  
            <th>일시</th>
            <th>사용자</th>
            <th>액션</th>
            <th>대상</th>
            <th>IP</th>
            <th>결과</th>
            <th>상세</th> 
          </tr>
        </thead>    
         <tbody>
          <tr v-for="log in logs" :key="log.logId">
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ log.createdAt?.replace('T', ' ').slice(0, 19) }}</td>
            <td style="font-weight: 600; color: var(--color-heading)">{{ log.userName || '-' }}</td>
            <td><span class="badge" :class="actionBadgeClass(log.action)">{{ actionLabel(log.action) }}</span></td>
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ targetLabel(log.target) }}</td>  
            <td class="text-muted" style="font-family: var(--font-mono); font-size: var(--text-xs)">{{ log.ipAddress || '-' }}</td>
            <td>
              <span 
                class="badge"
                :class="{ 성공: 'badge-success', 실패: 'badge-danger', 경고: 'badge-warning' }[log.result] || ''"
              >{{ log.result || '-' }}</span>
            </td>
            <td class="text-muted">{{ log.detail || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && logs.length === 0" class="empty-state">조회된 로그가 없습니다.</p>
      <PaginationBar :page="page" :total-pages="totalPages" :total-elements="totalElements" @change="changePage" />
    </div>
  </div>
</template>
