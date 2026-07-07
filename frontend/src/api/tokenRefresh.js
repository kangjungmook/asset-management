import { refreshToken as refreshTokenApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

let refreshingPromise = null

export function refreshTokens() {
  if (!refreshingPromise) {
    const auth = useAuthStore()
    refreshingPromise = refreshTokenApi({ refreshToken: auth.refreshToken })
      .then((data) => {
        auth.setTokens(data.accessToken, data.refreshToken)
        return data
      })
      .finally(() => {
        refreshingPromise = null
      })
  }
  return refreshingPromise
}
