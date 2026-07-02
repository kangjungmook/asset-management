import http from './http'

export const listDepartments = () => http.get('/departments').then((r) => r.data.data)
export const createDepartment = (payload) => http.post('/departments', payload).then((r) => r.data.data)
export const updateDepartment = (id, payload) => http.put(`/departments/${id}`, payload).then((r) => r.data.data)
export const deleteDepartment = (id) => http.delete(`/departments/${id}`).then((r) => r.data)
