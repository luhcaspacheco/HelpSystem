import { useEffect, useState } from 'react'
import './styles.css'

interface AlertProps {
  message?: string
  onClose?: () => void
  type?: 'success' | 'error' | 'info'
}

export default function Alert({ message, onClose, type = 'info' }: AlertProps) {
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    if (message) {
      setIsVisible(true)
    }
  }, [message])

  useEffect(() => {
    if (isVisible) {
      const timer = setTimeout(() => {
        setIsVisible(false)
        onClose?.()
      }, 5000)

      return () => clearTimeout(timer)
    }
  }, [isVisible, onClose])

  if (!isVisible || !message) {
    return null
  }

  return (
    <div className="alert-overlay" role="alert">
      <div className={`alert-box ${type}`}>
        <span>{message}</span>
        <button
          type="button"
          className="alert-close"
          onClick={() => {
            setIsVisible(false)
            onClose?.()
          }}
          aria-label="Fechar alerta"
        >
          ×
        </button>
      </div>
    </div>
  )
}
