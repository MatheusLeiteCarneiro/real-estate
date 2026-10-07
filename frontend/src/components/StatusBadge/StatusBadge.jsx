import styles from './StatusBadge.module.css'

const kinds = {
  sale : "For sale",
  rent : "For rent",
  unavailable: "Unavailable",
  active : "Active",
  inactive: "Inactive",
  admin: "Admin",
  broker: "Broker",
  cover: "Cover"
}

export default function StatusBadge({kind, children}){
  const className = [
    styles.statusBadge,
    styles[`statusBadge-${kind}`]
  ].filter(Boolean).join(" ");
  return(
    <span className={className}>{children || kinds[kind]}</span>
  )
}
