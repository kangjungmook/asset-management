import http from './http'

export const listPasswords = (params) => http.get('/passwords', { params }).then((r) => r.data.data)
export const listExpiringPasswords = () => http.get('/passwords/expiring').then((r) => r.data.data)
export const getPasswordSummary = () => http.get('/passwords/summary').then((r) => r.data.data)
export const createPassword = (payload) => http.post('/passwords', payload).then((r) => r.data.data)
export const updatePassword = (id, payload) => http.put(`/passwords/${id}`, payload).then((r) => r.data.data)
export const deletePassword = (id) => http.delete(`/passwords/${id}`).then((r) => r.data)
export const approvePassword = (id) => http.post(`/passwords/${id}/approve`).then((r) => r.data)
export const rejectPassword = (id, reason) => http.post(`/passwords/${id}/reject`, { reason }).then((r) => r.data)
