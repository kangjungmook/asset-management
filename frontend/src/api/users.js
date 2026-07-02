import http from './http'

export const listUsers = (params) => http.get('/users', { params }).then((r) => r.data.data)
export const getMe = () => http.get('/users/me').then((r) => r.data.data)
export const getUser = (id) => http.get(`/users/${id}`).then((r) => r.data.data)
export const createUser = (payload) => http.post('/users', payload).then((r) => r.data.data)
export const updateUser = (id, payload) => http.put(`/users/${id}`, payload).then((r) => r.data.data)
export const deleteUser = (id) => http.delete(`/users/${id}`).then((r) => r.data)
export const deactivateUser = (id) => http.put(`/users/${id}/deactivate`).then((r) => r.data)
export const activateUser = (id) => http.put(`/users/${id}/activate`).then((r) => r.data)
export const grantRole = (id, role) => http.post(`/users/${id}/roles`, { role }).then((r) => r.data)
export const revokeRole = (id, role) => http.delete(`/users/${id}/roles/${role}`).then((r) => r.data)
export const grantPermission = (id, permId) =>
  http.post(`/users/${id}/permissions`, { permId }).then((r) => r.data)
export const revokePermission = (id, permId) =>
  http.delete(`/users/${id}/permissions/${permId}`).then((r) => r.data)
