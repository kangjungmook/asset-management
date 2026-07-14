import http from './http'

export const listNotifications = () => http.get('/notifications').then((r) => r.data.data)
export const getUnreadCount = () => http.get('/notifications/unread-count').then((r) => r.data.data)
export const markNotificationRead = (id) => http.put(`/notifications/${id}/read`).then((r) => r.data)
export const markAllNotificationsRead = () => http.put('/notifications/read-all').then((r) => r.data)
