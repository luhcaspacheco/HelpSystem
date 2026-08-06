import { createBrowserRouter } from 'react-router'
import Layout from './pages/Layout'
import Cadastro from './pages/cadastro/Cadastro.tsx'
import Login from './pages/login/Login.tsx'
import RecuperarSenha from './pages/recuperar/RecuperarSenha.tsx'
import App from './pages/App.tsx'
import Solicitacoes from './pages/solicitacoes/Solicitacoes.tsx'
import Admin from './pages/admin/Admin.tsx'
import ProtectedRoute from './components/protectedRoute'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      {
        path: '/',
        element: <App />
      },
      {
        path: '/cadastro',
        element: <Cadastro />
      },
      {
        path: '/login',
        element: <Login />
      },
      {
        path: '/recuperar-senha',
        element: <RecuperarSenha />
      },
      {
        path: '/solicitacoes',
        element: (
          <ProtectedRoute>
            <Solicitacoes />
          </ProtectedRoute>
        )
      },
      {
        path: '/admin',
        element: (
          <ProtectedRoute>
            <Admin />
          </ProtectedRoute>
        )
      }
    ]
  }
])
