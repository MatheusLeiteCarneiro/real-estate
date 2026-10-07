import Icon from "../Icon/Icon"
import styles from "./SpecBadge.module.css"

export default function SpecBadge({icon, value, label, variant = 'inline'}){
  const className = [
    styles.specBadge,
    styles[`specBadge-${variant}`]
  ].filter(Boolean).join(' ')

  const valueClassName = [
    styles.value,
    styles[`value-${variant}`]
  ].filter(Boolean).join(' ')


  return(
    <span className={className}>
      <Icon name={icon} size={variant === 'tile' ? '1.5rem' : '1.125rem'} />
      <span className={valueClassName}>{value}</span>
      {label && <span className={styles.label}>{label}</span>}
    </span>
  )
}
