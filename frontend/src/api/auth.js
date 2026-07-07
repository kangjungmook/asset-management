import axios from 'axios'
import http from './http'

export const login = (payload) => http.post('/auth/login', payload).then((r) => r.data.data)
export const logout = () => http.post('/auth/logout').then((r) => r.data)
export const resetPassword = (userId) =>
  http.post(`/auth/reset-password/${userId}`).then((r) => r.data.data)
export const changePassword = (payload) =>
  http.post('/auth/change-password', payload).then((r) => r.data)
// Uses plain axios (not the `http` instance) to avoid the 401-retry interceptor
// recursing into this same endpoint.
export const refreshToken = (payload) => axios.post('/api/auth/refresh', payload).then((r) => r.data.data)
