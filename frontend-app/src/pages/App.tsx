import { Navigate } from 'react-router'
import { useUser } from '@/contexts/userContext'
import './App.css'

function App() {
  const { user, isLoading } = useUser()

  if (isLoading) {
    return <p className="app-loading">Carregando...</p>
  }

  return <Navigate to={user ? '/solicitacoes' : '/login'} replace />
}

export default App
