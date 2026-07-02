import http from './http'

export const listAuditLogs = (params) => http.get('/audit-logs', { params }).then((r) => r.data.data)
