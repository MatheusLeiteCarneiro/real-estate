import { useId, useState } from "react"
import Button from "./Button"
import FormField from "./FormField"
import IconButton from "./IconButton"
import Segmented from "./Segmented"
import styles from "./FilterBar.module.css"

const transactionOptions = [
  { value: "", label: "Both" },
  { value: "SALE", label: "Sale" },
  { value: "RENT", label: "Rent" },
]

const categoryOptions = [
  { value: "", label: "Any" },
  { value: "APARTMENT", label: "Apartment" },
  { value: "HOUSE", label: "House" },
  { value: "COMMERCIAL", label: "Commercial" },
  { value: "LAND", label: "Land" },
  { value: "STUDIO", label: "Studio" },
  { value: "FARM", label: "Farm" },
]

const bedroomOptions = [
  { value: "", label: "Any" },
  { value: "1", label: "1+" },
  { value: "2", label: "2+" },
  { value: "3", label: "3+" },
  { value: "4", label: "4+" },
]

export default function FilterBar({ onSubmit }) {
  const [open, setOpen] = useState(false)
  const panelId = useId()

  function handleSubmit(event) {
    event.preventDefault()
    const values = Object.fromEntries(new FormData(event.currentTarget))
    const filters = Object.fromEntries(
      Object.entries(values).filter(([, value]) => value !== "")
    )
    onSubmit?.(filters)
  }

  return (
    <form className={styles.filterBar} role="search" onSubmit={handleSubmit}>
      <div className={styles.searchRow}>
        <div className={styles.searchField}>
          <FormField
            label="Search"
            name="search"
            type="search"
            placeholder="City, neighborhood or keyword"
          />
        </div>
        <IconButton
          icon="filter"
          label="Filters"
          aria-expanded={open}
          aria-controls={panelId}
          onClick={() => setOpen((current) => !current)}
        />
        <Button type="submit" iconBefore="search">Search</Button>
      </div>

      <div id={panelId} className={styles.panel} hidden={!open}>
        <div className={styles.transaction}>
          <Segmented
            label="Transaction type"
            name="transactionType"
            options={transactionOptions}
            defaultValue=""
          />
        </div>
        <div className={styles.short}>
          <FormField label="Category" name="category" type="select" options={categoryOptions} />
        </div>
        <div className={styles.short}>
          <FormField label="Bedrooms" name="minBedrooms" type="select" options={bedroomOptions} />
        </div>
        <div className={styles.price}>
          <FormField label="Min price" name="minPrice" type="number" prefix="$" min={0} placeholder="0" />
        </div>
        <div className={styles.price}>
          <FormField label="Max price" name="maxPrice" type="number" prefix="$" min={0} placeholder="Any" />
        </div>
      </div>
    </form>
  )
}
