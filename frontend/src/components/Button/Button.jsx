import { Link } from "react-router";
import Icon from "../Icon/Icon";
import styles from "./Button.module.css"

export default function Button({children, variant = 'primary', href, to, disabled, iconBefore, iconAfter, size, block, onClick, type = 'button'}){
const className = [
  styles.btn,
  styles[`btn-${variant}`],
  size && styles[`btn-${size}`],
  block && styles.btnBlock,
].filter(Boolean).join(' ');


  if(to && !disabled){
    return <Link to={to} className={className} onClick={onClick}>
      {iconBefore && <Icon name={iconBefore} />}
      {children}
      {iconAfter && <Icon name={iconAfter} />}</Link>
  }
  else if(href && !disabled){
    return <a href={href} className={className} onClick={onClick}>
      {iconBefore && <Icon name={iconBefore} />}
      {children}
      {iconAfter && <Icon name={iconAfter} />}</a>
  }
  else{
    return <button type={type} className={className} disabled={disabled} onClick={onClick}>
      {iconBefore && <Icon name={iconBefore} />}
      {children}
      {iconAfter && <Icon name={iconAfter} />}
    </button>
  }
}
