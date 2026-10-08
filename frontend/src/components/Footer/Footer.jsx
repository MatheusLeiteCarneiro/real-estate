import Logo from "../Logo/Logo"
import styles from "./Footer.module.css"

export default function Footer() {
  const year = new Date().getFullYear()

  return (
    <footer className={styles.footer}>
      <div className={styles.brand}>
        <Logo />
        <p className={styles.tagline}>
          Homes, apartments, land and commercial spaces for sale or rent.
        </p>
      </div>
      <p className={styles.note}>© {year} Real Estate. All rights reserved.</p>
    </footer>
  )
}
