import Icon from "../Icon/Icon";
import styles from './Logo.module.css'

export default function Logo(){
  return(
    <a href="/" className={styles.logo}>
      <Icon name="home" className={styles.logoIcon} />
      <span className={styles.logoText}>Real Estate</span>
    </a>
  )
}
