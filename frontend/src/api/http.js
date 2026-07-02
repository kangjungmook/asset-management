import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let refreshingPromise = null

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const auth = useAuthStore()
    const { response, config } = error

    if (!response) {
      return Promise.reject(error)
    }

    if (response.status === 401 && !config._retried && auth.refreshToken) {
      config._retried = true
      try {
        if (!refreshingPromise) {
          refreshingPromise = axios
            .post('/api/auth/refresh', { refreshToken: auth.refreshToken })
            .then((res) => {
              auth.setAccessToken(res.data.data.accessToken)
              return res.data.data.accessToken
            })
            .finally(() => {
              refreshingPromise = null
            })
        }
        const newToken = await refreshingPromise
        config.headers.Authorization = `Bearer ${newToken}`
        return http(config)
      } catch (refreshError) {
        auth.clear()
        router.push('/login')
        return Promise.reject(refreshError)
      }
    }

    if (response.status === 401) {
      auth.clear()
      router.push('/login')
    }

    if (response.status === 403) {
      router.push('/403')
    }

    return Promise.reject(error)
  },
)

export default http
