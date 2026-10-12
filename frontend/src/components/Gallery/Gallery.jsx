import { useState } from "react"
import Icon from "../Icon/Icon"
import Lightbox from "../Lightbox/Lightbox"
import Thumbnails from "../Thumbnails/Thumbnails"
import { IMAGE_WIDTH, resizeImage } from "../../lib/image"
import styles from "./Gallery.module.css"

export default function Gallery({ images = [], title }) {
  const primary = images.find((image) => image.isPrimary)
  const ordered = primary
    ? [primary, ...images.filter((image) => image !== primary)]
    : images

  const [index, setIndex] = useState(0)
  const [lightboxOpen, setLightboxOpen] = useState(false)
  const currentImage = ordered[index]
  const hasMany = ordered.length > 1

  function showPrevious() {
    setIndex((previous) => (previous - 1 + ordered.length) % ordered.length)
  }

  function showNext() {
    setIndex((previous) => (previous + 1) % ordered.length)
  }

  if (!currentImage) {
    return (
      <div className={styles.gallery}>
        <div className={styles.photo}>
          <div className={styles.placeholder}>
            <Icon name="imageOff" size="3rem" />
            <span>No photo available</span>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.gallery}>
      <button
        type="button"
        className={`${styles.photo} ${styles.photoButton}`}
        aria-label={`Open photo ${index + 1} of ${ordered.length} in full screen`}
        onClick={() => setLightboxOpen(true)}
      >
        <img key={currentImage.id} className={styles.photoImage} src={resizeImage(currentImage.url, IMAGE_WIDTH.photo)} alt={title} />
        <span className={styles.expand}>
          <Icon name="expand" size="1.125rem" />
        </span>
        {hasMany && (
          <span className={styles.counter}>
            {index + 1} / {ordered.length}
          </span>
        )}
      </button>

      {hasMany && <Thumbnails images={ordered} activeIndex={index} onSelect={setIndex} />}

      <Lightbox
        open={lightboxOpen}
        images={ordered}
        index={index}
        title={title}
        onSelect={setIndex}
        onPrevious={showPrevious}
        onNext={showNext}
        onClose={() => setLightboxOpen(false)}
      />
    </div>
  )
}
