import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { BellRing, ClipboardList, Cloud, Eye, EyeOff, History, MessageSquare } from 'lucide-react'
import Alert from '@/components/alert'
import { useUser } from '@/contexts/userContext'
import '../auth.css'
import './Login.css'

export default function Login() {
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [emailError, setEmailError] = useState('')
  const [senhaError, setSenhaError] = useState('')
  const [alertMessage, setAlertMessage] = useState('')
  const [alertType, setAlertType] = useState<'success' | 'error' | 'info'>('info')
  const { login, isLoading } = useUser()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from || '/solicitacoes'

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!email) setEmailError('Este campo é obrigatório.')
    if (!senha) setSenhaError('Este campo é obrigatório.')

    if (email && senha) {
      const result = await login(email, senha)
      const isSuccess = result.toLowerCase().includes('sucesso')

      setAlertType(isSuccess ? 'success' : 'error')
      setAlertMessage(result)

      if (isSuccess) navigate(from)
    }
  }

  return (
    <div className="auth-page login-page">
      <Alert message={alertMessage} onClose={() => setAlertMessage('')} type={alertType} />

      <header className="auth-brand">
        <div className="auth-brand-content">
          <span className="auth-brand-logo"><Cloud aria-hidden="true" /></span>
          <span>
            <strong>Help System</strong>
            <small>Specialisterne + Salesforce</small>
          </span>
        </div>
      </header>

      <main className="auth-main">
        <section className="auth-intro">
          <span className="auth-intro-mark"><MessageSquare aria-hidden="true" /></span>
          <p className="auth-intro-eyebrow">Central de serviços internos</p>
          <h2>Controle cada solicitação com contexto, prioridade e histórico.</h2>
          <p>Centralize o atendimento entre colaboradores e equipes responsáveis, com visibilidade de cada etapa até a resolução.</p>
          <div className="auth-feature-list">
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><ClipboardList aria-hidden="true" /></span>
              <span><strong>Solicitações organizadas</strong><small>Categoria, prioridade e status em um único fluxo.</small></span>
            </div>
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><BellRing aria-hidden="true" /></span>
              <span><strong>Acompanhamento contínuo</strong><small>Notificações sobre respostas e atualizações importantes.</small></span>
            </div>
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><History aria-hidden="true" /></span>
              <span><strong>Histórico centralizado</strong><small>Conversas e resoluções preservadas para consulta.</small></span>
            </div>
          </div>
        </section>

        <section className="auth-card login-card">
          <p className="auth-eyebrow">Bem-vindo</p>
          <h1>Entrar</h1>
          <p className="auth-card-subtitle">Use seu e-mail corporativo para continuar.</p>

          <form className="login-form" onSubmit={handleSubmit}>
            <label className="login-field">
              <span>E-mail <strong>*</strong></span>
              <input
                type="email"
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value)
                  if (emailError) setEmailError('')
                }}
                placeholder="nome@empresa.com"
                className={emailError ? 'error' : ''}
              />
              {emailError && <small>{emailError}</small>}
            </label>

            <label className="login-field">
              <span>Senha <strong>*</strong></span>
              <div className={`auth-password-input ${senhaError ? 'error' : ''}`}>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={senha}
                  onChange={(event) => {
                    setSenha(event.target.value)
                    if (senhaError) setSenhaError('')
                  }}
                  placeholder="Sua senha"
                />
                <button
                  type="button"
                  aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                  aria-pressed={showPassword}
                  onClick={() => setShowPassword((prev) => !prev)}
                >
                  {showPassword ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}
                </button>
              </div>
              {senhaError && <small>{senhaError}</small>}
            </label>

            <a className="forgot-password" href="#recuperar-senha">Esqueci minha senha</a>

            <button className="login-submit" type="submit" disabled={isLoading}>
              {isLoading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>

          <p className="auth-card-footer">Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link></p>
        </section>
      </main>
    </div>
  )
}
