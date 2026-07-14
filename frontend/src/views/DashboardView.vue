<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listUsers } from '@/api/users'
import { listDepartments } from '@/api/departments'
import { listExpiringPasswords } from '@/api/passwords'
import { listAuditLogs } from '@/api/auditLogs'
import { actionLabel, actionBadgeClass, targetLabel } from '@/utils/auditLog'

const router = useRouter()

const userCount = ref(0)
const inactiveCount = ref(0)
const deptCount = ref(0)
const expiringCount = ref(0)
const totalLogCount = ref(0)
const recentLogs = ref([])
const loading = ref(true)

const today = new Date().toLocaleDateString('ko-KR', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  weekday: 'short',
})

onMounted(async () => {
  try {
    const [users, depts, expiring, logPage] = await Promise.all([
      listUsers(),
      listDepartments(),
      listExpiringPasswords(),
      listAuditLogs({ page: 1, size: 8 }),
    ])
    userCount.value = users.length
    inactiveCount.value = users.filter((u) => !u.isActive).length
    deptCount.value = depts.length
    expiringCount.value = expiring.length
    recentLogs.value = logPage.content
    totalLogCount.value = logPage.totalElements
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
        <p class="page-header__desc">{{ today }} · 계정 자산 현황 요약</p>
      </div>
      <button class="btn btn-primary" @click="router.push('/manage/users')">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>사용자 관리
      </button>
    </div>

    <div class="stat-grid" style="margin-bottom: var(--space-8)">
      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">전체 사용자</span>
          <span class="stat-card__icon" style="background: var(--color-brand-tint); color: var(--color-brand)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ userCount }}명</div>
        <div class="stat-card__sub">비활성 {{ inactiveCount }}명 포함</div>
      </div>

      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">부서</span>
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
              <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ expiringCount }}건</div>
        <div class="stat-card__sub">30일 이내 만료 예정</div>
      </div>

      <div class="stat-card">
        <div class="stat-card__head">
          <span class="stat-card__label">오늘 활동 기록</span>
          <span class="stat-card__icon" style="background: var(--color-info-tint); color: var(--color-info)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
            </svg>
          </span>
        </div>
        <div class="stat-card__value">{{ totalLogCount }}건</div>
        <div class="stat-card__sub">누적 감사 로그</div>
      </div>
    </div>

    <div class="table-wrap">
      <div class="page-header" style="padding: 15px 18px; margin-bottom: 0; border-bottom: 1px solid var(--color-border)">
        <h3 style="font-size: 14px">최근 활동</h3>
        <button style="display: flex; align-items: center; gap: 3px; font-size: 12px; color: var(--color-brand); font-weight: 600; background: none; border: none; cursor: pointer; padding: 0" @click="router.push('/manage/audit-logs')">
          감사 로그 전체
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6" /></svg>
        </button>
      </div>
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
          <tr v-for="log in recentLogs" :key="log.logId">
            <td class="text-muted">{{ log.createdAt?.replace('T', ' ').slice(0, 19) }}</td>
            <td><span style="font-weight: 600; color: var(--color-heading-soft)">{{ log.userName || '-' }}</span></td>
            <td><span class="badge" :class="actionBadgeClass(log.action)">{{ actionLabel(log.action) }}</span></td>
            <td class="text-muted">{{ targetLabel(log.target) }}</td>
            <td class="text-muted">{{ log.detail || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && recentLogs.length === 0" class="empty-state">최근 활동 내역이 없습니다.</p>
    </div>
  </div>
</template>
