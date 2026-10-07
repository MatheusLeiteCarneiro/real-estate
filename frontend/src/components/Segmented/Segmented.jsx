import { useId } from "react"
import styles from "./Segmented.module.css"

export default function Segmented({ options, defaultValue, onChange, label, name, showLabel = false }) {
  const generatedName = useId()
  const groupName = name ?? generatedName

  const legendClassName = [
    styles.legend,
    !showLabel && styles.legendHidden,
  ].filter(Boolean).join(" ")

  return (
    <fieldset className={styles.segmented}>
      <legend className={legendClassName}>{label}</legend>
      <div className={styles.options}>
        {options.map((option) => (
          <label key={option.value} className={styles.option}>
            <input
              type="radio"
              name={groupName}
              value={option.value}
              defaultChecked={option.value === defaultValue}
              onChange={onChange}
              className={styles.input}
            />
            <span className={styles.text}>{option.label}</span>
          </label>
        ))}
      </div>
    </fieldset>
  )
}
