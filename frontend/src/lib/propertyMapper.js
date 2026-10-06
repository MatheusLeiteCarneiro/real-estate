export function toPropertyCardProps(dto) {
  return {
    id: dto.id,
    title: dto.title,
    city: dto.city,
    price: dto.price,
    tx: dto.transactionType.toLowerCase(),
    beds: dto.bedrooms,
    baths: dto.bathrooms,
    area: dto.area,
    active: dto.available,
    image: dto.primaryImage?.url,
  }
}
