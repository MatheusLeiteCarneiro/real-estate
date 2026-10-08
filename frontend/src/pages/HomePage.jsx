import { useState } from 'react'
import FilterBar from '../components/FilterBar/FilterBar'
import Hero from '../components/Hero/Hero'
import PropertyCard from '../components/PropertyCard/PropertyCard'
import Pagination from '../components/Pagination/Pagination'
import StatusMessage from '../components/StatusMessage/StatusMessage'
import { toPropertyCardProps } from '../lib/propertyMapper'
import { useProperties } from '../hooks/useProperties'
import styles from './HomePage.module.css'

export default function HomePage() {
  const [filters, setFilters] = useState({})
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const { data, loading, error } = useProperties({ filters, page, size: pageSize })


  function handleSearch(newFilters) {
    setFilters(newFilters)
    setPage(1)
  }

  function handlePageSizeChange(size) {
    setPageSize(size)
    setPage(1)
  }

  return (
    <main className={styles.page}>
      <Hero eyebrow="Homes, land and commercial" title="Find a place that feels like yours">
        Search homes, apartments and land for sale or rent, and find the one that fits your life.
      </Hero>

      <div className={`${styles.container} ${styles.filterWrap}`}>
          <FilterBar onSubmit={handleSearch} />
      </div>

      <section className={styles.results} aria-labelledby="results-title">
        <div className={styles.container}>
          <div className={styles.resultsHead}>
            <h2 id="results-title" className={styles.resultsTitle}>Explore properties</h2>
          </div>

          {error && (
            <StatusMessage variant="error">
              Something went wrong while loading the properties.
            </StatusMessage>
          )}
          {loading && !error && <StatusMessage variant="loading">Loading…</StatusMessage>}
          {!loading && !error && data?.content.length === 0 && (
            <StatusMessage variant="empty">No properties found.</StatusMessage>
          )}

          {!loading && !error && data?.content.length > 0 && (
            <div className={styles.grid}>
              {data.content.map((dto) => (
                <PropertyCard key={dto.id} property={toPropertyCardProps(dto)} />
              ))}
            </div>
          )}

          {data?.content.length > 0 && (
            <div className={styles.pager}>
              <Pagination
                page={page}
                pages={data.page.totalPages}
                onChange={setPage}
                pageSize={pageSize}
                onPageSizeChange={handlePageSizeChange}
              />
            </div>
          )}
        </div>
      </section>
    </main>
  )
}
