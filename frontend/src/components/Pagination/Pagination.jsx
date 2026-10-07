import Button from "../Button/Button"
import styles from "./Pagination.module.css"

function getPageItems(page, pages) {
  if (pages <= 7) {
    return Array.from({ length: pages }, (_, index) => index + 1)
  }

  const numbers = [...new Set([1, pages, page - 1, page, page + 1])]
    .filter((number) => number >= 1 && number <= pages)
    .sort((a, b) => a - b)

  const items = []
  numbers.forEach((number, index) => {
    const previous = numbers[index - 1]
    if (previous !== undefined) {
      if (number - previous === 2) {
        items.push(previous + 1)
      } else if (number - previous > 2) {
        items.push("ellipsis")
      }
    }
    items.push(number)
  })
  return items
}

const DEFAULT_PAGE_SIZE_OPTIONS = [10, 20, 30, 40, 50]

export default function Pagination({
  page,
  pages,
  onChange,
  pageSize,
  onPageSizeChange,
  pageSizeOptions = DEFAULT_PAGE_SIZE_OPTIONS,
}) {
  const showNavigation = pages >= 1
  const showPageSize = Boolean(onPageSizeChange)

  if (!showNavigation && !showPageSize) return null

  const items = showNavigation ? getPageItems(page, pages) : []

  function goTo(target) {
    if (target !== page) onChange?.(target)
  }

  return (
    <div className={styles.container}>
      {showNavigation && (
        <nav className={styles.pagination} aria-label="Pagination">
          <div className={styles.navButton}>
            <Button variant="secondary" iconBefore="arrowLeft" block disabled={page <= 1} onClick={() => goTo(page - 1)}>
              Previous
            </Button>
          </div>

          <p className={styles.summary}>Page {page} of {pages}</p>

          <ul className={styles.pages}>
            {items.map((item, index) => {
              if (item === "ellipsis") {
                return (
                  <li key={`ellipsis-${index}`} className={styles.ellipsis} aria-hidden="true">…</li>
                )
              }
              const className = [
                styles.pageButton,
                item === page && styles.current,
              ].filter(Boolean).join(" ")
              return (
                <li key={item}>
                  <button
                    type="button"
                    className={className}
                    aria-label={`Page ${item}`}
                    aria-current={item === page ? "page" : undefined}
                    onClick={() => goTo(item)}
                  >
                    {item}
                  </button>
                </li>
              )
            })}
          </ul>

          <div className={styles.navButton}>
            <Button variant="secondary" iconAfter="arrowRight" block disabled={page >= pages} onClick={() => goTo(page + 1)}>
              Next
            </Button>
          </div>
        </nav>
      )}
      {showPageSize && (
        <label className={styles.pageSize}>
          <span className={styles.pageSizeLabel}>Per page</span>
          <select
            className={styles.pageSizeSelect}
            value={pageSize}
            onChange={(event) => onPageSizeChange(Number(event.target.value))}
          >
            {pageSizeOptions.map((option) => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
        </label>
      )}
    </div>
  )
}
