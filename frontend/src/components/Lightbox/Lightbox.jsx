import { useEffect, useRef } from "react"
import IconButton from "../IconButton/IconButton"
import Thumbnails from "../Thumbnails/Thumbnails"
import { IMAGE_WIDTH, resizeImage } from "../../lib/image"
import styles from "./Lightbox.module.css"

export default function Lightbox({ open, images, index, title, onSelect, onPrevious, onNext, onClose }) {
  const dialogRef = useRef(null)
  const currentImage = images[index]
  const hasMany = images.length > 1

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  useEffect(() => {
    if (!open || !hasMany) return

    function handleKeyDown(event) {
      if (event.key === "ArrowLeft") onPrevious()
      if (event.key === "ArrowRight") onNext()
    }

    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [open, hasMany, onPrevious, onNext])

  function handleStageClick(event) {
    if (event.target === event.currentTarget) onClose()
  }

  return (
    <dialog ref={dialogRef} className={styles.dialog} aria-label={`Photos of ${title}`} onClose={onClose}>
      {open && currentImage && (
        <div className={styles.layout}>
          <div className={styles.stage} onClick={handleStageClick}>
            <div className={styles.topBar}>
              {hasMany && (
                <span className={styles.counter}>
                  {index + 1} / {images.length}
                </span>
              )}
              <div className={styles.close}>
                <IconButton icon="close" label="Close photos" onClick={onClose} />
              </div>
            </div>

            <img
              key={currentImage.id}
              className={styles.image}
              src={resizeImage(currentImage.url, IMAGE_WIDTH.fullscreen)}
              alt={`${title}, photo ${index + 1} of ${images.length}`}
            />

            {hasMany && (
              <>
                <div className={styles.previous}>
                  <IconButton icon="arrowLeft" label="Previous photo" onClick={onPrevious} />
                </div>
                <div className={styles.next}>
                  <IconButton icon="arrowRight" label="Next photo" onClick={onNext} />
                </div>
              </>
            )}
          </div>

          {hasMany && (
            <Thumbnails images={images} activeIndex={index} onSelect={onSelect} variant="lightbox" />
          )}
        </div>
      )}
    </dialog>
  )
}
