import http from './http'

export const login = (payload) => http.post('/auth/login', payload).then((r) => r.data.data)
export const logout = () => http.post('/auth/logout').then((r) => r.data)
export const resetPassword = (userId) =>
  http.post(`/auth/reset-password/${userId}`).then((r) => r.data.data)
export const changePassword = (payload) =>
  http.post('/auth/change-password', payload).then((r) => r.data)
