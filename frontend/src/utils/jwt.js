export function decodeJwtExpiryMs(token) {
  try {
    const payload = token.split('.')[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    )
    const { exp } = JSON.parse(json)
    return exp ? exp * 1000 : null
  } catch {
    return null
  }
}
