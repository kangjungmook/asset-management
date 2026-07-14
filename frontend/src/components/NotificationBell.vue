<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  listNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
} from '@/api/notifications'

const router = useRouter()
const open = ref(false)
const root = ref(null)
const notifications = ref([])
const unreadCount = ref(0)
let pollTimer = null

async function refreshUnreadCount() {
  try {
    unreadCount.value = await getUnreadCount()
  } catch {
    // 로그인 전이거나 세션 만료 시 조용히 무시
  }
}

async function loadList() {
  notifications.value = await listNotifications()
}

async function toggle() {
  open.value = !open.value
  if (open.value) {
    await loadList()
  }
}

function close() {
  open.value = false
}

function handleOutsideClick(e) {
  if (root.value && !root.value.contains(e.target)) {
    close()
  }
}

function relativeTime(iso) {
  if (!iso) return ''
  const diffMs = Date.now() - new Date(iso).getTime()
  const min = Math.floor(diffMs / 60000)
  if (min < 1) return '방금 전'
  if (min < 60) return `${min}분 전`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}시간 전`
  return `${Math.floor(hour / 24)}일 전`
}

async function handleClick(notification) {
  if (!notification.isRead) {
    try {
      await markNotificationRead(notification.notificationId)
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {
      // ignore
    }
  }
  close()
  if (notification.link) {
    router.push(notification.link)
  }
}

async function handleMarkAllRead() {
  try {
    await markAllNotificationsRead()
    notifications.value = notifications.value.map((n) => ({ ...n, isRead: true }))
    unreadCount.value = 0
  } catch {
    // ignore
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  refreshUnreadCount()
  pollTimer = setInterval(refreshUnreadCount, 20000)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="notif-bell" ref="root">
    <button class="notif-bell__button" title="알림" @click="toggle">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      <span v-if="unreadCount > 0" class="notif-bell__badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </button>

    <div v-if="open" class="notif-bell__panel">
      <div class="notif-bell__head">
        <span>알림</span>
        <button v-if="unreadCount > 0" class="notif-bell__mark-all" @click="handleMarkAllRead">모두 읽음</button>
      </div>
      <div class="notif-bell__list">
        <button
          v-for="n in notifications"
          :key="n.notificationId"
          class="notif-bell__item"
          :class="{ 'notif-bell__item--unread': !n.isRead }"
          @click="handleClick(n)"
        >
          <span class="notif-bell__dot" v-if="!n.isRead"></span>
          <span class="notif-bell__item-body">
            <span class="notif-bell__item-title">{{ n.title }}</span>
            <span class="notif-bell__item-message">{{ n.message }}</span>
            <span class="notif-bell__item-time">{{ relativeTime(n.createdAt) }}</span>
          </span>
        </button>
        <p v-if="notifications.length === 0" class="notif-bell__empty">알림이 없습니다.</p>
      </div>
    </div>
  </div>
</template>
