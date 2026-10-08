export async function fetchProperties({filters = {}, page = 1, size = 20}){
  const params = new URLSearchParams({...filters, page: page - 1, size})
  const response = await fetch(`/api/properties?${params}`)

  if(!response.ok){
    throw new Error(`Request failed with status ${response.status}`)
  }

  return response.json()

}

export async function fetchSession() {
  const response = await fetch('/api/bff/session')

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }

  return response.json()
}

export function getCsrfToken() {
  const cookie = document.cookie
    .split('; ')
    .find((entry) => entry.startsWith('XSRF-TOKEN='))

  return cookie ? decodeURIComponent(cookie.split('=')[1]) : ''
}
