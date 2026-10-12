import {Link} from "react-router"
import Icon from "../Icon/Icon";
import styles from './Logo.module.css'

export default function Logo(){
  return(
    <Link to="/" className={styles.logo}>
      <Icon name="home" className={styles.logoIcon} />
      <span className={styles.logoText}>Real Estate</span>
    </Link>
  )
}
