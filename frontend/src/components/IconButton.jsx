import Icon from "./Icon";
import styles from "./IconButton.module.css"
export default function IconButton({icon, label, variant = 'default', onClick, ...props}){
  const className = [
    styles.iconBtn,
    variant != 'default' && styles[`iconBtn-${variant}`],
  ].filter(Boolean).join(' ');


  return (
    <button type="button" className={className} onClick={onClick} aria-label={label} {...props}>
      <Icon name={icon}/>
    </button>
  );
}
