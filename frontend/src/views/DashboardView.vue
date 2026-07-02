<script setup>
import { onMounted, ref } from 'vue'
import { listUsers } from '@/api/users'
import { listDepartments } from '@/api/departments'
import { listExpiringPasswords } from '@/api/passwords'
import { listAuditLogs } from '@/api/auditLogs'

const userCount = ref(0)
const deptCount = ref(0)
const expiringCount = ref(0)
const recentLogs = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [users, depts, expiring, logPage] = await Promise.all([
      listUsers(),
      listDepartments(),
      listExpiringPasswords(),
      listAuditLogs({ page: 1, size: 8 }),
    ])
    userCount.value = users.length
    deptCount.value = depts.length
    expiringCount.value = expiring.length
    recentLogs.value = logPage.content
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h1>대시보드</h1>
        <p class="page-header__desc">사용자·부서·패스워드 관리대장 현황을 한눈에 확인합니다.</p>
      </div>
    </div>

    <div class="stat-grid" style="margin-bottom: var(--space-6)">
      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">전체 사용자</span>
          <span class="stat-card__icon" style="background: var(--color-brand-tint); color: var(--color-brand)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 19v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1" />
              <circle cx="9.5" cy="7.5" r="3.5" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ userCount }}명</div>
        <div class="stat-card__sub">등록된 전체 사용자 수</div>
      </div>

      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">전체 부서</span>
          <span class="stat-card__icon" style="background: var(--color-success-tint); color: var(--color-success)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 21V7l7-4 7 4v14" />
              <path d="M10 21v-4h4v4" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ deptCount }}개</div>
        <div class="stat-card__sub">조직 부서 수</div>
      </div>

      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">만료 임박 계정</span>
          <span class="stat-card__icon" style="background: var(--color-warning-tint); color: var(--color-warning)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="9" />
              <path d="M12 7v5l3 3" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ expiringCount }}건</div>
        <div class="stat-card__sub">30일 이내 만료 예정</div>
      </div>
    </div>

    <h3 style="margin-bottom: var(--space-3)">최근 활동</h3>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>일시</th>
            <th>사용자</th>
            <th>액션</th>
            <th>대상</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in recentLogs" :key="log.logId">
            <td class="text-muted">{{ log.createdAt?.replace('T', ' ').slice(0, 19) }}</td>
            <td>{{ log.userName || '-' }}</td>
            <td><span class="badge badge-brand">{{ log.action }}</span></td>
            <td class="text-muted">{{ log.target }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && recentLogs.length === 0" class="empty-state">최근 활동 내역이 없습니다.</p>
    </div>
  </div>
</template>
