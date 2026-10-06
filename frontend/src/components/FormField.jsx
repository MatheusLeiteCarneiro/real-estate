import { useId } from "react"
import Icon from "./Icon"
import styles from "./FormField.module.css"

export default function FormField({
  label,
  type = "text",
  options = [],
  prefix,
  suffix,
  hint,
  error,
  required,
  id,
  name,
  ...props
}) {
  const generatedId = useId()
  const fieldId = id ?? generatedId
  const messageId = `${fieldId}-message`
  const message = error || hint

  const controlProps = {
    id: fieldId,
    name,
    required,
    "aria-invalid": error ? true : undefined,
    "aria-describedby": message ? messageId : undefined,
    className: styles.input,
    ...props,
  }

  const controlClassName = [
    styles.control,
    error && styles.controlError,
  ].filter(Boolean).join(" ")

  let control
  if (type === "select") {
    control = (
      <select {...controlProps}>
        {options.map((option) => (
          <option key={option.value} value={option.value}>{option.label}</option>
        ))}
      </select>
    )
  } else if (type === "textarea") {
    control = <textarea {...controlProps} />
  } else {
    control = <input type={type} {...controlProps} />
  }

  return (
    <div className={styles.field}>
      <label htmlFor={fieldId} className={styles.label}>
        {label}
        {required && <span aria-hidden="true" className={styles.required}> *</span>}
      </label>

      <div className={controlClassName}>
        {prefix && <span className={styles.affix}>{prefix}</span>}
        {control}
        {suffix && <span className={styles.affix}>{suffix}</span>}
      </div>

      {message && (
        <p id={messageId} className={error ? styles.error : styles.hint}>
          {error && <Icon name="alert" size="1rem" />}
          {message}
        </p>
      )}
    </div>
  )
}
