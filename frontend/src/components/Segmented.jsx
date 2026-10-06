import { useId } from "react"
import styles from "./Segmented.module.css"

export default function Segmented({ options, defaultValue, onChange, label, name }) {
  const generatedName = useId()
  const groupName = name ?? generatedName

  return (
    <fieldset className={styles.segmented}>
      <legend className={styles.legend}>{label}</legend>
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
    </fieldset>
  )
}
