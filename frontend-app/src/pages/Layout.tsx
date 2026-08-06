import { useEffect, useState } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router'
import { Bell, Cloud, LogOut, X } from 'lucide-react'
import './Layout.css'
import api, { type ApiResponse } from '@/services/api'
import { useUser } from '@contexts/userContext'
import { formatDateTime } from '@/utils/datetime'

type Notificacao = {
  id: number
  mensagem: string
  lida: boolean
  dataCriacao: string
  solicitacaoId: number
  solicitacaoTitulo: string
}

type TotalNaoLidas = {
  total: number
}

export default function Layout() {
  const { user, logout, isLoading } = useUser()
  const navigate = useNavigate()
  const location = useLocation()
  const isAuthPage =
    location.pathname === '/login' ||
    location.pathname === '/cadastro' ||
    location.pathname === '/recuperar-senha'
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false)
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([])
  const [totalNaoLidas, setTotalNaoLidas] = useState(0)
  const [isLoadingNotifications, setIsLoadingNotifications] = useState(false)
  const [notificationError, setNotificationError] = useState('')

  const navClass = (path: string) => {
    return location.pathname === path ? 'layout-link active' : 'layout-link'
  }

  async function carregarTotalNaoLidas() {
    if (!user) {
      setTotalNaoLidas(0)
      return
    }

    try {
      const response = await api.get<ApiResponse<TotalNaoLidas>>('/notificacoes/nao-lidas/total')

      if (response.data.sucesso) {
        setTotalNaoLidas(response.data.dado.total)
      }
    } catch (error) {
      console.error('Erro ao carregar total de notificações:', error)
      setNotificationError('Não foi possível carregar o total de notificações.')
    }
  }

  async function carregarNotificacoes() {
    if (!user) return

    setIsLoadingNotifications(true)
    setNotificationError('')

    try {
      const response = await api.get<ApiResponse<Notificacao[]>>('/notificacoes')

      if (response.data.sucesso) {
        setNotificacoes(response.data.dado)
      } else {
        setNotificationError(response.data.mensagem)
      }
    } catch (error) {
      console.error('Erro ao carregar notificações:', error)
      setNotificationError('Não foi possível carregar as notificações. Confira se o login ainda está válido.')
    } finally {
      setIsLoadingNotifications(false)
    }
  }

  async function abrirNotificacoes() {
    setIsNotificationsOpen(true)
    await carregarNotificacoes()
    await carregarTotalNaoLidas()
  }

  async function marcarComoLida(id: number) {
    try {
      const response = await api.patch<ApiResponse<Notificacao>>(`/notificacoes/${id}/lida`)

      if (response.data.sucesso) {
        setNotificacoes((prev) => prev.map((item) => (item.id === id ? { ...item, lida: true } : item)))
        setTotalNaoLidas((prev) => Math.max(prev - 1, 0))
      }
    } catch (error) {
      console.error('Erro ao marcar notificação como lida:', error)
      setNotificationError('Não foi possível marcar a notificação como lida.')
    }
  }

  async function abrirNotificacao(notificacao: Notificacao) {
    if (!notificacao.lida) {
      await marcarComoLida(notificacao.id)
    }
    setIsNotificationsOpen(false)
    navigate(`/solicitacoes?abrir=${notificacao.solicitacaoId}`)
  }

  async function handleLogout() {
    await logout()
    setNotificacoes([])
    setTotalNaoLidas(0)
    setIsNotificationsOpen(false)
    navigate('/login')
  }

  // Verifica novas notificações periodicamente, sem precisar recarregar a página.
  useEffect(() => {
    if (!user) {
      setTotalNaoLidas(0)
      return
    }

    carregarTotalNaoLidas()

    const intervalo = setInterval(() => {
      carregarTotalNaoLidas()
      // se o painel estiver aberto, atualiza também a lista em tempo real
      if (isNotificationsOpen) {
        carregarNotificacoes()
      }
    }, 20000)

    return () => clearInterval(intervalo)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, isNotificationsOpen])

  return (
    <div className={isAuthPage ? 'layout-container auth-layout' : 'layout-container'}>
      {!isAuthPage && (
        <nav className="layout-nav">
          <Link className="layout-brand" to="/">
            <span className="layout-mark"><Cloud aria-hidden="true" /></span>
            <span>
              <strong>Help System</strong>
              <small>Specialisterne + Salesforce</small>
            </span>
          </Link>

          <div className="layout-nav-center">
            {user ? (
              user.admin ? (
                <>
                  <Link className={navClass('/solicitacoes')} to="/solicitacoes">
                    Solicitações
                  </Link>
                  <Link className={navClass('/admin')} to="/admin">
                    Admin
                  </Link>
                </>
              ) : null
            ) : (
              <>
                <Link className={navClass('/cadastro')} to="/cadastro">
                  Cadastro
                </Link>
                <Link className={navClass('/login')} to="/login">
                  Login
                </Link>
              </>
            )}
          </div>

          <div className="layout-nav-actions">
            {user ? (
              <>
                <span className="layout-user-label">
                  <strong>{user.nome}</strong>
                  <small>
                    {user.admin ? 'Administrador' : 'Colaborador'}
                    {user.departamento ? ` · ${user.departamento}` : ''}
                  </small>
                </span>
                <button
                  type="button"
                  className="layout-button notification-trigger"
                  aria-label="Notificações"
                  onClick={abrirNotificacoes}
                  disabled={isLoadingNotifications}
                >
                  <Bell aria-hidden="true" />
                  {totalNaoLidas > 0 && <span className="notification-badge">{totalNaoLidas}</span>}
                </button>
                <button type="button" className="layout-button" onClick={handleLogout} disabled={isLoading}>
                  <LogOut aria-hidden="true" />
                  Sair
                </button>
              </>
            ) : (
              <Link className="layout-button dark" to="/login">
                Entrar
              </Link>
            )}
          </div>
        </nav>
      )}

      <section className="layout-workspace">
        <main className="layout-main">
          <Outlet />
        </main>
      </section>

      {isNotificationsOpen && (
        <div className="layout-modal-backdrop" onClick={() => setIsNotificationsOpen(false)}>
          <div className="notifications-modal" onClick={(event) => event.stopPropagation()}>
            <div className="notifications-header">
              <div>
                <h2>Notificações</h2>
                <p>Acompanhe atualizações das suas solicitações.</p>
              </div>
              <button type="button" className="modal-close-button" aria-label="Fechar" onClick={() => setIsNotificationsOpen(false)}>
                <X aria-hidden="true" />
              </button>
            </div>

            <div className="notifications-list">
              {notificationError && <p className="notification-error">{notificationError}</p>}
              {isLoadingNotifications && <p className="notification-empty">Carregando notificações...</p>}
              {!isLoadingNotifications && notificacoes.length === 0 && (
                <p className="notification-empty">Nenhuma notificação encontrada.</p>
              )}
              {!isLoadingNotifications &&
                notificacoes.map((notificacao) => (
                  <article
                    className={`notification-item ${notificacao.lida ? 'read' : 'unread'}`}
                    key={notificacao.id}
                    role="button"
                    tabIndex={0}
                    title="Abrir a solicitação"
                    onClick={() => abrirNotificacao(notificacao)}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault()
                        abrirNotificacao(notificacao)
                      }
                    }}
                  >
                    <div>
                      <strong>{notificacao.solicitacaoTitulo}</strong>
                      <p>{notificacao.mensagem}</p>
                      <span>{formatDateTime(notificacao.dataCriacao)}</span>
                    </div>
                    {!notificacao.lida && (
                      <button
                        type="button"
                        className="mark-read-button"
                        onClick={(event) => {
                          event.stopPropagation()
                          marcarComoLida(notificacao.id)
                        }}
                      >
                        Marcar como lida
                      </button>
                    )}
                  </article>
                ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
