import { useEffect, useId, useRef, useState } from "react"
import Icon from "../Icon/Icon"
import Logo from "../Logo/Logo"
import StatusBadge from "../StatusBadge/StatusBadge"
import { getCsrfToken } from "../../lib/api"
import styles from "./Header.module.css"

export default function Header({ user }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const menuId = useId()
  const menuRef = useRef(null)

  useEffect(() => {
    if (!menuOpen) return

    function handlePointerDown(event) {
      if (!menuRef.current?.contains(event.target)) setMenuOpen(false)
    }
    function handleKeyDown(event) {
      if (event.key === "Escape") setMenuOpen(false)
    }

    document.addEventListener("pointerdown", handlePointerDown)
    document.addEventListener("keydown", handleKeyDown)
    return () => {
      document.removeEventListener("pointerdown", handlePointerDown)
      document.removeEventListener("keydown", handleKeyDown)
    }
  }, [menuOpen])

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        <Logo />

        {user && (
          <div className={styles.userMenu} ref={menuRef}>
            <button
              type="button"
              className={styles.userButton}
              aria-expanded={menuOpen}
              aria-controls={menuId}
              onClick={() => setMenuOpen((current) => !current)}
            >
              <Icon name="user" size="1.25rem" />
              <span className={styles.userButtonName}>{user.name}</span>
              <Icon name="chevronDown" size="1rem" />
            </button>

            <div id={menuId} className={styles.dropdown} hidden={!menuOpen}>
              <div className={styles.userSummary}>
                <span className={styles.userSummaryName}>{user.name}</span>
                {user.role && <StatusBadge kind={user.role} />}
              </div>

              <a href="/profile" className={styles.menuItem}>
                <Icon name="user" size="1.125rem" />
                My profile
              </a>
              {user.role === "admin" && (
                <a href="/admin" className={styles.menuItem}>
                  <Icon name="shield" size="1.125rem" />
                  Admin panel
                </a>
              )}

              <form method="post" action="/logout" className={styles.logoutForm}>
                <input type="hidden" name="_csrf" value={getCsrfToken()} />
                <button type="submit" className={styles.menuItem}>
                  <Icon name="logOut" size="1.125rem" />
                  Log out
                </button>
              </form>
            </div>
          </div>
        )}
      </div>
    </header>
  )
}
