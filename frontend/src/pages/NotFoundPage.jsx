import Button from '../components/Button/Button'
import styles from './NotFoundPage.module.css'

export default function NotFoundPage({
  title = 'We could not find that page',
  text = 'The page may have been removed, or the link may be mistyped.',
}) {
  return (
    <main className={styles.page}>
      <section className={styles.content}>
        <span className={styles.code} aria-hidden="true">404</span>
        <h1 className={styles.title}>{title}</h1>
        <p className={styles.text}>{text}</p>
        <div className={styles.actions}>
          <Button to="/" iconBefore="home">Back to listings</Button>
        </div>
      </section>
    </main>
  )
}
