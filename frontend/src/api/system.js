import http from './http'

export const resetSystem = (confirm) => http.post('/system/reset', { confirm }).then((r) => r.data)
export const resetUsers = () => http.post('/system/reset/users').then((r) => r.data)
export const resetDepartments = () => http.post('/system/reset/departments').then((r) => r.data)
export const resetPermissions = () => http.post('/system/reset/permissions').then((r) => r.data)
export const resetAccountTypes = () => http.post('/system/reset/account-types').then((r) => r.data)
export const resetPasswords = () => http.post('/system/reset/passwords').then((r) => r.data)
export const resetAuditLogs = () => http.post('/system/reset/audit-logs').then((r) => r.data)
