import Icon from "../Icon/Icon"
import styles from "./StatusMessage.module.css"

const VARIANTS = {
  loading: { icon: "loader", iconClassName: styles.spinner, role: "status" },
  error: { icon: "alert", iconClassName: styles.iconError, role: "alert" },
  empty: { icon: "searchOff", iconClassName: styles.icon, role: undefined },
}

export default function StatusMessage({ variant = "empty", children }) {
  const { icon, iconClassName, role } = VARIANTS[variant] ?? VARIANTS.empty

  return (
    <p className={styles.status} role={role}>
      <Icon name={icon} size="2rem" className={iconClassName} />
      {children}
    </p>
  )
}
