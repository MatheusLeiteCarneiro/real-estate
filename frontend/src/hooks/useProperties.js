import { useEffect, useState } from 'react'
import { fetchProperties } from '../lib/api'

export function useProperties({ filters, page, size }) {
  const [result, setResult] = useState({ request: null, data: null, error: null })

  useEffect(() => {
    let ignore = false
    const request = { filters, page, size }

    fetchProperties(request)
      .then((data) => {
        if (!ignore) setResult({ request, data, error: null })
      })
      .catch((error) => {
        if (!ignore) setResult({ request, data: null, error })
      })

    return () => {
      ignore = true
    }
  }, [filters, page, size])

  const loading =
    result.request?.filters !== filters ||
    result.request?.page !== page ||
    result.request?.size !== size

  return { data: result.data, loading, error: result.error }
}
