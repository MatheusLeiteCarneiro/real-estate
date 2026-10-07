import styles from "./Hero.module.css"

export default function Hero({ eyebrow, title, children }) {
  return (
    <section className={styles.hero}>
      <div className={styles.content}>
        {eyebrow && <p className={styles.eyebrow}>{eyebrow}</p>}
        <h1 className={styles.title}>{title}</h1>
        {children && <p className={styles.lead}>{children}</p>}
      </div>
    </section>
  )
}
