import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { Cloud, KeyRound, LifeBuoy, Mail, ShieldCheck } from 'lucide-react'
import Alert from '@/components/alert'
import '../auth.css'
import '../login/Login.css'
import './RecuperarSenha.css'

export default function RecuperarSenha() {
  const [email, setEmail] = useState('')
  const [emailError, setEmailError] = useState('')
  const [enviado, setEnviado] = useState(false)
  const [alertMessage, setAlertMessage] = useState('')
  const [alertType, setAlertType] = useState<'success' | 'error' | 'info'>('info')

  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!email) {
      setEmailError('Este campo é obrigatório.')
      return
    }
    if (!emailRegex.test(email)) {
      setEmailError('Insira um e-mail válido.')
      return
    }
    if (!email.trim().toLowerCase().includes('@helpsystem.')) {
      setEmailError('Use um e-mail corporativo do domínio @helpsystem.')
      return
    }

    // Fluxo simulado: não há envio real de e-mail. Sempre respondemos de
    // forma neutra, sem revelar se o e-mail existe na base.
    setEnviado(true)
    setAlertType('success')
    setAlertMessage('Se este e-mail estiver cadastrado, enviaremos as instruções de recuperação em instantes.')
    setEmail('')
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
          <span className="auth-intro-mark"><LifeBuoy aria-hidden="true" /></span>
          <p className="auth-intro-eyebrow">Recuperação de acesso</p>
          <h2>Perdeu o acesso? A gente ajuda a recuperar.</h2>
          <p>Informe seu e-mail corporativo e enviaremos as instruções para você voltar ao fórum com segurança.</p>
          <div className="auth-feature-list">
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><Mail aria-hidden="true" /></span>
              <span><strong>Instruções por e-mail</strong><small>Enviadas apenas para contas @helpsystem.</small></span>
            </div>
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><ShieldCheck aria-hidden="true" /></span>
              <span><strong>Sem expor sua conta</strong><small>Respondemos sempre da mesma forma, por segurança.</small></span>
            </div>
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><KeyRound aria-hidden="true" /></span>
              <span><strong>Nova senha protegida</strong><small>Mínimo de 8 caracteres ao redefinir.</small></span>
            </div>
          </div>
        </section>

        <section className="auth-card login-card">
          <p className="auth-eyebrow">Recuperação</p>
          <h1>Esqueci minha senha</h1>
          <p className="auth-card-subtitle">Informe seu e-mail corporativo para receber as instruções.</p>

          {enviado ? (
            <div className="recuperar-confirmacao">
              <span className="recuperar-confirmacao-icon"><Mail aria-hidden="true" /></span>
              <p>Se este e-mail estiver cadastrado, enviaremos as instruções de recuperação em instantes.</p>
              <p className="recuperar-confirmacao-hint">Não recebeu? Verifique a caixa de spam ou tente novamente.</p>
              <button type="button" className="login-submit" onClick={() => setEnviado(false)}>
                Enviar para outro e-mail
              </button>
            </div>
          ) : (
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
                  placeholder="nome@helpsystem.com"
                  className={emailError ? 'error' : ''}
                />
                {emailError && <small>{emailError}</small>}
              </label>

              <button className="login-submit" type="submit">Enviar instruções</button>
            </form>
          )}

          <p className="auth-card-footer">Lembrou a senha? <Link to="/login">Entrar</Link></p>
        </section>
      </main>
    </div>
  )
}
