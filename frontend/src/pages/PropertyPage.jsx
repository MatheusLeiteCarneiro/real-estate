import { Link, useParams } from "react-router"
import Icon from "../components/Icon/Icon"
import SpecBadge from "../components/SpecBadge/SpecBadge"
import StatusBadge from "../components/StatusBadge/StatusBadge"
import { formatCategory, formatPrice, formatZipCode } from "../lib/format"
import { useProperty } from "../hooks/useProperty"
import StatusMessage from "../components/StatusMessage/StatusMessage"
import NotFoundPage from "./NotFoundPage"
import styles from "./PropertyPage.module.css"

export default function PropertyPage() {
  const { id } = useParams()
  const { data, loading, error } = useProperty(id)
  const notFound = error?.status === 404 || error?.status === 400

 if (notFound) {
  return (
      <NotFoundPage
        title="Property not found"
        text="This property may have been sold, removed, or the link may be mistyped."
      />
    )
  }

  if(error){
    return(
      <main className={styles.page}>
        <StatusMessage variant="error">Something went wrong while loading the property.</StatusMessage>
      </main>
    )
  }

  if(loading){
    return(
      <main className={styles.page}>
        <StatusMessage variant="loading">Loading...</StatusMessage>
      </main>
    )
  }

  const {
    title,
    description,
    price,
    transactionType,
    category,
    available,
    bedrooms,
    bathrooms,
    suites,
    parkingSpots,
    area,
    address,
    images,
  } = data

  const mainImage = images.find((image) => image.isPrimary) ?? images[0]
  const streetLine = [`${address.street}, ${address.number}`, address.complement].filter(Boolean).join(" · ")
  const cityLine = `${address.neighborhood}, ${address.city} - ${address.state}`

  return (
    <main className={styles.page}>
      <div className={styles.inner}>
        <Link to="/" className={styles.back}>
          <Icon name="arrowLeft" size="1rem" />
          Back to listings
        </Link>

        <div className={styles.photo}>
          {mainImage ? (
            <img className={styles.photoImage} src={mainImage.url} alt={title} />
          ) : (
            <div className={styles.photoPlaceholder}>
              <Icon name="imageOff" size="3rem" />
              <span>No photo available</span>
            </div>
          )}
        </div>

        <div className={styles.head}>
          <div>
            <StatusBadge kind={available ? transactionType.toLowerCase() : "unavailable"} />
            <h1 className={styles.title}>{title}</h1>
          </div>
          <p className={styles.price}>
            {formatPrice(price)}
            {transactionType === "RENT" && <span className={styles.period}>/mo</span>}
          </p>
        </div>

        <div className={styles.specs}>
          {bedrooms > 0 && <SpecBadge variant="tile" icon="bed" value={bedrooms} label="Bedrooms" />}
          {bathrooms > 0 && <SpecBadge variant="tile" icon="bath" value={bathrooms} label="Bathrooms" />}
          {suites > 0 && <SpecBadge variant="tile" icon="suite" value={suites} label="Suites" />}
          {parkingSpots > 0 && (
            <SpecBadge variant="tile" icon="car" value={parkingSpots} label="Parking spots" />
          )}
          <SpecBadge variant="tile" icon="area" value={`${area} m²`} label="Area" />
          <SpecBadge variant="tile" icon="building" value={formatCategory(category)} label="Category" />
        </div>

        <p className={styles.address}>
          <Icon name="location" size="1.375rem" />
          <span>
            <span className={styles.addressStreet}>{streetLine}</span>
            <span className={styles.addressCity}>
              {" · "}{cityLine} · {formatZipCode(address.zipCode)}
            </span>
          </span>
        </p>

        <section className={styles.about}>
          <h2 className={styles.sectionTitle}>Property description</h2>
          <p className={styles.description}>{description}</p>
        </section>
      </div>
    </main>
  )
}
