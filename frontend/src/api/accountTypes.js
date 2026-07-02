import http from './http'

export const listAccountTypes = () => http.get('/account-types').then((r) => r.data.data)
export const createAccountType = (payload) => http.post('/account-types', payload).then((r) => r.data.data)
export const updateAccountType = (id, payload) => http.put(`/account-types/${id}`, payload).then((r) => r.data.data)
export const deleteAccountType = (id) => http.delete(`/account-types/${id}`).then((r) => r.data)
