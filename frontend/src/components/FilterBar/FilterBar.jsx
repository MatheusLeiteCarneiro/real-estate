import { useId, useState } from "react"
import Button from "../Button/Button"
import FormField from "../FormField/FormField"
import IconButton from "../IconButton/IconButton"
import Segmented from "../Segmented/Segmented"
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

const minCountOptions = [
  { value: "", label: "Any" },
  { value: "1", label: "1+" },
  { value: "2", label: "2+" },
  { value: "3", label: "3+" },
  { value: "4", label: "4+" },
]

const sortOptions = [
  { value: "", label: "Recently listed" },
  { value: "createdAt,asc", label: "Listed longest ago" },
  { value: "price,asc", label: "Price: low to high" },
  { value: "price,desc", label: "Price: high to low" },
  { value: "area,desc", label: "Area: largest first" },
  { value: "area,asc", label: "Area: smallest first" },
]

const maxBedroomOptions = [
  { value: "", label: "Any" },
  { value: "1", label: "1" },
  { value: "2", label: "2" },
  { value: "3", label: "3" },
  { value: "4", label: "4" },
  { value: "5", label: "5" },
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
        <div className={styles.wide}>
          <Segmented
            label="Transaction type"
            showLabel
            name="transactionType"
            options={transactionOptions}
            defaultValue=""
          />
        </div>
        <div className={styles.wide}>
          <FormField label="Category" name="category" type="select" options={categoryOptions} />
        </div>
        <FormField label="Min price" name="minPrice" type="number" prefix="$" min={0} placeholder="0" />
        <FormField label="Max price" name="maxPrice" type="number" prefix="$" min={0} placeholder="Any" />
        <FormField label="Min area" name="minArea" type="number" suffix="m²" min={0} placeholder="0" />
        <FormField label="Max area" name="maxArea" type="number" suffix="m²" min={0} placeholder="Any" />
        <FormField label="Min bedrooms" name="minBedrooms" type="select" options={minCountOptions} />
        <FormField label="Max bedrooms" name="maxBedrooms" type="select" options={maxBedroomOptions} />
        <FormField label="Min bathrooms" name="minBathrooms" type="select" options={minCountOptions} />
        <FormField label="Min suites" name="minSuites" type="select" options={minCountOptions} />
        <FormField label="Min parking spots" name="minParkingSpots" type="select" options={minCountOptions} />
        <FormField label="Sort by" name="sort" type="select" options={sortOptions} />
      </div>
    </form>
  )
}
