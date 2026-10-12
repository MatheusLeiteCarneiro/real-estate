import {Link} from "react-router"
import { useState } from "react"
import Icon from "../Icon/Icon"
import StatusBadge from "../StatusBadge/StatusBadge"
import SpecBadge from "../SpecBadge/SpecBadge"
import { formatPrice } from "../../lib/format"
import { IMAGE_WIDTH, resizeImage } from "../../lib/image"
import styles from "./PropertyCard.module.css"

export default function PropertyCard({ property }) {
  const { id, title, city, price, tx: type, beds, baths, area, active = true, image } = property

  const [imageFailed, setImageFailed] = useState(false)
  const showImage = image && !imageFailed

  const mediaClassName = [
    styles.media,
    !active && styles.mediaInactive,
  ].filter(Boolean).join(" ")

  return (
    <article className={styles.card}>
      <div className={mediaClassName}>
        {showImage ? (
          <img
            className={styles.image}
            src={resizeImage(image, IMAGE_WIDTH.card)}
            alt=""
            loading="lazy"
            onError={() => setImageFailed(true)}
          />
        ) : (
          <div className={styles.placeholder}>
            <Icon name="imageOff" size="2.5rem" />
            <span className={styles.placeholderText}>No photo available</span>
          </div>
        )}
        <div className={styles.tag}>
          <StatusBadge kind={active ? type : "unavailable"} />
        </div>
      </div>

      <div className={styles.body}>
        <p className={styles.price}>
          {formatPrice(price)}
          {active && type === "rent" && <span className={styles.period}>/mo</span>}
        </p>
        <h3 className={styles.title}>
          <Link to={`/properties/${id}`} className={styles.link}>{title}</Link>
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
