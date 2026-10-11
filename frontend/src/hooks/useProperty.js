import { useEffect, useState } from 'react'
import { fetchProperty } from '../lib/api'

export function useProperty(id) {
  const [result, setResult] = useState({ id: null, data: null, error: null })

  useEffect(() => {
    let ignore = false

    fetchProperty(id)
      .then((data) => {
        if (!ignore) setResult({ id, data, error: null })
      })
      .catch((error) => {
        if (!ignore) setResult({ id, data: null, error })
      })

    return () => {
      ignore = true
    }
  }, [id])

  const loading = result.id !== id


  return { data: result.data, loading, error: result.error }
}
