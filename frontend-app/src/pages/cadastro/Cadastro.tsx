import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { BellRing, ClipboardList, Cloud, Eye, EyeOff, History, UserPlus } from 'lucide-react'
import './Cadastro.css'
import '../auth.css'
import Input from '@components/input'
import Button from '@components/button'
import Alert from '@components/alert'
import { useUser } from '@contexts/userContext'
import api, { type ApiResponse } from '@/services/api'

type Departamento = {
  id: number
  nome: string
}

export default function Cadastro() {
  const [nome, setNome] = useState('')
  const [nomeError, setNomeError] = useState('')
  const [email, setEmail] = useState('')
  const [emailError, setEmailError] = useState('')
  const [senha, setSenha] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [senhaError, setSenhaError] = useState('')
  const [dep, setDep] = useState('')
  const [depError, setDepError] = useState('')
  const [departamentos, setDepartamentos] = useState<Departamento[]>([])
  const [isLoadingDepartamentos, setIsLoadingDepartamentos] = useState(false)
  const [alertMessage, setAlertMessage] = useState('')
  const [alertType, setAlertType] = useState<'success' | 'error' | 'info'>('info')
  const { addUser, isLoading } = useUser()
  const navigate = useNavigate()

  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

  useEffect(() => {
    async function carregarDepartamentos() {
      setIsLoadingDepartamentos(true)

      try {
        const response = await api.get<ApiResponse<Departamento[]>>('/departamentos')

        if (response.data.sucesso) {
          setDepartamentos(response.data.dado)
        } else {
          setAlertType('error')
          setAlertMessage(response.data.mensagem)
        }
      } catch (error) {
        console.error('Erro ao carregar departamentos:', error)
        setAlertType('error')
        setAlertMessage('Não foi possível carregar os departamentos.')
      } finally {
        setIsLoadingDepartamentos(false)
      }
    }

    carregarDepartamentos()
  }, [])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const isEmailValid = emailRegex.test(email)

    const isSenhaValid = senha.length >= 8

    if (!nome) setNomeError('Este campo é obrigatório.')
    if (!email) setEmailError('Este campo é obrigatório.')
    if (!senha) setSenhaError('Este campo é obrigatório.')
    else if (!isSenhaValid) setSenhaError('A senha deve ter no mínimo 8 caracteres.')
    if (!dep) setDepError('Selecione um departamento válido.')

    if (email && !isEmailValid) {
      setEmailError('Insira um e-mail válido.')
      return
    }

    if (email && senha && isSenhaValid && nome && dep && isEmailValid) {
      const result = await addUser(nome, email, senha, Number(dep))
      const isSuccess = result.toLowerCase().includes('sucesso')

      setAlertType(isSuccess ? 'success' : 'error')
      setAlertMessage(isSuccess ? `${result} Faça login para continuar.` : result)

      if (isSuccess) {
        setNome('')
        setEmail('')
        setSenha('')
        setDep('')
        setTimeout(() => navigate('/login'), 1200)
      }
    }
  }

  return (
    <div className="auth-page cadastro-page">
      <Alert message={alertMessage} onClose={() => setAlertMessage('')} type={alertType} />

      <header className="auth-brand">
        <div className="auth-brand-content">
          <span className="auth-brand-logo"><Cloud aria-hidden="true" /></span>
          <span><strong>Help System</strong><small>Specialisterne + Salesforce</small></span>
        </div>
      </header>

      <main className="auth-main cadastro-main">
        <section className="auth-intro">
          <span className="auth-intro-mark"><UserPlus aria-hidden="true" /></span>
          <p className="auth-intro-eyebrow">Faça parte do fórum</p>
          <h2>Um espaço para tirar dúvidas e ajudar colegas.</h2>
          <p>Crie seu acesso para publicar perguntas, responder outros colaboradores e acompanhar cada solução até o fim.</p>
          <div className="auth-feature-list">
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><ClipboardList aria-hidden="true" /></span>
              <span><strong>Abra sua dúvida</strong><small>Publique um problema em poucos passos.</small></span>
            </div>
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><BellRing aria-hidden="true" /></span>
              <span><strong>Ajude quem precisa</strong><small>Responda colegas e compartilhe o que sabe.</small></span>
            </div>
            <div className="auth-feature-item">
              <span className="auth-feature-icon"><History aria-hidden="true" /></span>
              <span><strong>Fique por dentro</strong><small>Notificações quando responderem você.</small></span>
            </div>
          </div>
        </section>

        <section className="auth-card cadastro-card">
          <p className="auth-eyebrow">Nova conta</p>
          <h1>Cadastre-se</h1>
          <p className="auth-card-subtitle">Preencha seus dados para começar.</p>

          <form className="cadastro-form" onSubmit={handleSubmit}>
            <label className="cadastro-field">
              <span>Nome completo <strong>*</strong></span>
              <Input type="text" value={nome} error={nomeError} placeholder="Seu nome" onChange={(event) => { setNome(event.target.value); if (nomeError) setNomeError('') }} />
            </label>

            <label className="cadastro-field">
              <span>E-mail <strong>*</strong></span>
              <Input type="email" value={email} error={emailError} placeholder="nome@empresa.com" onChange={(event) => { setEmail(event.target.value); if (emailError) setEmailError('') }} />
            </label>

            <div className="cadastro-form-row">
              <label className="cadastro-field">
                <span>Departamento <strong>*</strong></span>
                <select className={`cadastro-select ${depError ? 'error' : ''}`} value={dep} disabled={isLoadingDepartamentos} onChange={(event) => { setDep(event.target.value); if (depError) setDepError('') }}>
                  <option value="">{isLoadingDepartamentos ? 'Carregando...' : 'Selecione...'}</option>
                  {departamentos.map((departamento) => <option key={departamento.id} value={departamento.id}>{departamento.nome}</option>)}
                </select>
                {depError && <span className="select-error">{depError}</span>}
              </label>

              <label className="cadastro-field">
                <span>Senha <strong>*</strong></span>
                <div className={`auth-password-input ${senhaError ? 'error' : ''}`}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={senha}
                    placeholder="Mínimo de 8 caracteres"
                    onChange={(event) => {
                      setSenha(event.target.value)
                      if (senhaError) setSenhaError('')
                    }}
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
                {senhaError && <span className="select-error">{senhaError}</span>}
              </label>
            </div>

            <div className="cadastro-actions">
              <Button type="submit" disabled={isLoading || isLoadingDepartamentos}>{isLoading ? 'Criando conta...' : 'Criar conta'}</Button>
            </div>
          </form>

          <p className="auth-card-footer">Já tem uma conta? <Link to="/login">Entrar</Link></p>
        </section>
      </main>
    </div>
  )
}
