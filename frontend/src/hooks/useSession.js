import { useEffect, useState } from 'react'
import { fetchSession } from '../lib/api'

function toSessionUser(session) {
  if (!session.authenticated) return null

  const authorities = session.authorities ?? []
  let role = null
  if (authorities.includes('ROLE_ADMIN')) role = 'admin'
  else if (authorities.includes('ROLE_BROKER')) role = 'broker'

  return { name: session.username, role }
}

export function useSession() {
  const [state, setState] = useState({ loading: true, user: null })

  useEffect(() => {
    let ignore = false

    fetchSession()
      .then((session) => {
        if (!ignore) setState({ loading: false, user: toSessionUser(session) })
      })
      .catch(() => {
        if (!ignore) setState({ loading: false, user: null })
      })

    return () => {
      ignore = true
    }
  }, [])

  return state
}
