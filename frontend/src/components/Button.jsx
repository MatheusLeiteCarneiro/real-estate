import Icon from "./Icon";
import styles from "./Button.module.css"

export default function Button({children, variant = 'primary', href, disabled, iconBefore, iconAfter, size, block}){
const className = [
  styles.btn,
  styles[`btn-${variant}`],
  size && styles[`btn-${size}`],
  block && styles.btnBlock,
].filter(Boolean).join(' ');


  if(href && !disabled){
    return <a href={href} className={className}>
      {iconBefore && <Icon name={iconBefore} />}
      {children}
      {iconAfter && <Icon name={iconAfter} />}</a>
  }
  else{
    return <button type="button" className={className} disabled={disabled}>
      {iconBefore && <Icon name={iconBefore} />}
      {children}
      {iconAfter && <Icon name={iconAfter} />}
    </button>
  }
}
