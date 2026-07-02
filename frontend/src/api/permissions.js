import http from './http'

export const listPermissions = () => http.get('/permissions').then((r) => r.data.data)
export const createPermission = (payload) => http.post('/permissions', payload).then((r) => r.data.data)
export const updatePermission = (id, payload) => http.put(`/permissions/${id}`, payload).then((r) => r.data.data)
export const deletePermission = (id) => http.delete(`/permissions/${id}`).then((r) => r.data)
