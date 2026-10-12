import { useEffect, useRef } from "react"
import { IMAGE_WIDTH, resizeImage } from "../../lib/image"
import styles from "./Thumbnails.module.css"

export default function Thumbnails({ images, activeIndex, onSelect, variant = "page" }) {
  const listRef = useRef(null)
  const hasScrolledRef = useRef(false)

  useEffect(() => {
    const frame = requestAnimationFrame(() => {
      const list = listRef.current
      const item = list?.children[activeIndex]
      if (!list || !item) return

      const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches
      list.scrollTo({
        left: item.offsetLeft - (list.clientWidth - item.offsetWidth) / 2,
        top: item.offsetTop - (list.clientHeight - item.offsetHeight) / 2,
        behavior: hasScrolledRef.current && !reducedMotion ? "smooth" : "instant",
      })
      hasScrolledRef.current = true
    })

    return () => cancelAnimationFrame(frame)
  }, [activeIndex])

  const listClassName = [styles.list, styles[`list-${variant}`]].filter(Boolean).join(" ")

  return (
    <ul ref={listRef} className={listClassName} aria-label="Photos">
      {images.map((image, imageIndex) => {
        const active = imageIndex === activeIndex
        const thumbClassName = [styles.thumb, active && styles.active].filter(Boolean).join(" ")

        return (
          <li key={image.id} className={styles.item}>
            <button
              type="button"
              className={thumbClassName}
              aria-label={`Show photo ${imageIndex + 1} of ${images.length}`}
              aria-current={active ? "true" : undefined}
              onClick={() => onSelect(imageIndex)}
            >
              <img className={styles.image} src={resizeImage(image.url, IMAGE_WIDTH.thumbnail)} alt="" loading="lazy" />
            </button>
          </li>
        )
      })}
    </ul>
  )
}
