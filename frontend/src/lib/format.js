const priceFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 0,
})

export function formatPrice(value) {
  return priceFormatter.format(value)
}

export function formatCategory(category) {
  if (!category) return ''
  return category.charAt(0) + category.slice(1).toLowerCase()
}

export function formatZipCode(zipCode) {
  if (!zipCode || zipCode.length !== 8) return zipCode ?? ''
  return `${zipCode.slice(0, 5)}-${zipCode.slice(5)}`
}
