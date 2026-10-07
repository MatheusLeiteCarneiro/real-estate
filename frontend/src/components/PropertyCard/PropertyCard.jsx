import StatusBadge from "../StatusBadge/StatusBadge"
import SpecBadge from "../SpecBadge/SpecBadge"
import styles from "./PropertyCard.module.css"

const priceFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
  maximumFractionDigits: 0,
})

export default function PropertyCard({ property }) {
  const { id, title, city, price, tx: type, beds, baths, area, active = true, image } = property

  const mediaClassName = [
    styles.media,
    !active && styles.mediaInactive,
  ].filter(Boolean).join(" ")

  return (
    <article className={styles.card}>
      <div className={mediaClassName}>
        {image && <img className={styles.image} src={image} alt="" loading="lazy" />}
        <div className={styles.tag}>
          <StatusBadge kind={active ? type : "unavailable"} />
        </div>
      </div>

      <div className={styles.body}>
        <p className={styles.price}>
          {priceFormatter.format(price)}
          {active && type === "rent" && <span className={styles.period}>/mo</span>}
        </p>
        <h3 className={styles.title}>
          <a href={`/properties/${id}`} className={styles.link}>{title}</a>
        </h3>
        <p className={styles.city}>{city}</p>

        <div className={styles.specs}>
          {beds > 0 && <SpecBadge icon="bed" value={beds} />}
          {baths > 0 && <SpecBadge icon="bath" value={baths} />}
          {area > 0 && <SpecBadge icon="area" value={`${area} m²`} />}
        </div>
      </div>
    </article>
  )
}
