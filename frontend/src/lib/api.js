export async function fetchProperties({filters = {}, page = 1, size = 20}){
  const params = new URLSearchParams({...filters, page: page - 1, size})
  const response = await fetch(`/api/properties?${params}`)

  if(!response.ok){
    throw new Error(`Request failed with status ${response.status}`)
  }

  return response.json()

}
