import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { ChevronLeft, ChevronRight, Plus, RefreshCw, Trash2, UserMinus, UserPlus, UserX, X } from 'lucide-react'
import Alert from '@components/alert'
import api, { type ApiResponse } from '@/services/api'
import { useUser } from '@/contexts/userContext'
import './Admin.css'

type TipoUsuario = 'ADMIN' | 'COMUM'

type UsuarioAdmin = {
  id: number
  nome: string
  email: string
  tipo: TipoUsuario
  admin: boolean
  ativo: boolean
}

type Categoria = {
  id: number
  nome: string
}

type Departamento = {
  id: number
  nome: string
}

export default function Admin() {
  const { user } = useUser()
  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([])
  const [categorias, setCategorias] = useState<Categoria[]>([])
  const [departamentos, setDepartamentos] = useState<Departamento[]>([])
  const [categoriaNome, setCategoriaNome] = useState('')
  const [departamentoNome, setDepartamentoNome] = useState('')
  const [buscaUsuario, setBuscaUsuario] = useState('')
  const [paginaUsuarios, setPaginaUsuarios] = useState(1)
  const [usuariosPorPagina, setUsuariosPorPagina] = useState(10)
  const [isLoading, setIsLoading] = useState(false)
  const [isCatalogLoading, setIsCatalogLoading] = useState(false)
  const [isCatalogSaving, setIsCatalogSaving] = useState(false)
  const [savingId, setSavingId] = useState<number | null>(null)
  const [alertMessage, setAlertMessage] = useState('')
  const [alertType, setAlertType] = useState<'success' | 'error' | 'info'>('info')

  const stats = useMemo(() => {
    return {
      total: usuarios.length,
      ativos: usuarios.filter((item) => item.ativo).length,
      admins: usuarios.filter((item) => item.tipo === 'ADMIN' && item.ativo).length,
      inativos: usuarios.filter((item) => !item.ativo).length
    }
  }, [usuarios])

  const usuariosFiltrados = useMemo(() => {
    const termo = buscaUsuario.trim().toLowerCase()
    if (!termo) return usuarios

    return usuarios.filter((item) =>
      item.nome.toLowerCase().includes(termo) || item.email.toLowerCase().includes(termo)
    )
  }, [buscaUsuario, usuarios])

  const totalPaginasUsuarios = Math.max(1, Math.ceil(usuariosFiltrados.length / usuariosPorPagina))
  const inicioUsuarios = (paginaUsuarios - 1) * usuariosPorPagina
  const usuariosPaginados = usuariosFiltrados.slice(inicioUsuarios, inicioUsuarios + usuariosPorPagina)

  const atualizarUsuario = (usuarioAtualizado: UsuarioAdmin) => {
    setUsuarios((prev) => prev.map((item) => (item.id === usuarioAtualizado.id ? usuarioAtualizado : item)))
  }

  const carregarUsuarios = useCallback(async () => {
    if (!user?.admin) return

    setIsLoading(true)
    try {
      const response = await api.get<ApiResponse<UsuarioAdmin[]>>('/usuarios')
      if (response.data.sucesso) {
        setUsuarios(response.data.dado)
      } else {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
      }
    } catch (error) {
      console.error('Erro ao carregar usuários:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível carregar os usuários.')
    } finally {
      setIsLoading(false)
    }
  }, [user?.admin])

  const carregarCadastros = useCallback(async () => {
    if (!user?.admin) return

    setIsCatalogLoading(true)
    try {
      const [categoriasResponse, departamentosResponse] = await Promise.all([
        api.get<ApiResponse<Categoria[]>>('/categorias'),
        api.get<ApiResponse<Departamento[]>>('/departamentos')
      ])

      if (categoriasResponse.data.sucesso) {
        setCategorias(categoriasResponse.data.dado)
      }
      if (departamentosResponse.data.sucesso) {
        setDepartamentos(departamentosResponse.data.dado)
      }
    } catch (error) {
      console.error('Erro ao carregar cadastros administrativos:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível carregar categorias e departamentos.')
    } finally {
      setIsCatalogLoading(false)
    }
  }, [user?.admin])

  useEffect(() => {
    carregarUsuarios()
    carregarCadastros()
  }, [carregarUsuarios, carregarCadastros])

  useEffect(() => {
    setPaginaUsuarios(1)
  }, [buscaUsuario, usuariosPorPagina])

  useEffect(() => {
    if (paginaUsuarios > totalPaginasUsuarios) {
      setPaginaUsuarios(totalPaginasUsuarios)
    }
  }, [paginaUsuarios, totalPaginasUsuarios])

  const atualizarTela = () => {
    carregarUsuarios()
    carregarCadastros()
  }

  const criarCategoria = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!categoriaNome.trim()) return

    setIsCatalogSaving(true)
    try {
      const response = await api.post<ApiResponse<Categoria>>('/categorias', { nome: categoriaNome.trim() })
      if (response.data.sucesso) {
        setCategorias((prev) => [...prev, response.data.dado].sort((a, b) => a.nome.localeCompare(b.nome)))
        setCategoriaNome('')
        setAlertType('success')
      } else {
        setAlertType('error')
      }
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao criar categoria:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível criar a categoria.')
    } finally {
      setIsCatalogSaving(false)
    }
  }

  const criarDepartamento = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!departamentoNome.trim()) return

    setIsCatalogSaving(true)
    try {
      const response = await api.post<ApiResponse<Departamento>>('/departamentos', { nome: departamentoNome.trim() })
      if (response.data.sucesso) {
        setDepartamentos((prev) => [...prev, response.data.dado].sort((a, b) => a.nome.localeCompare(b.nome)))
        setDepartamentoNome('')
        setAlertType('success')
      } else {
        setAlertType('error')
      }
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao criar departamento:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível criar o departamento.')
    } finally {
      setIsCatalogSaving(false)
    }
  }

  const excluirCategoria = async (categoria: Categoria) => {
    if (!window.confirm(`Deseja excluir a categoria "${categoria.nome}"?`)) return

    setIsCatalogSaving(true)
    try {
      const response = await api.delete<ApiResponse<null>>(`/categorias/${categoria.id}`)
      if (response.data.sucesso) {
        setCategorias((prev) => prev.filter((item) => item.id !== categoria.id))
        setAlertType('success')
      } else {
        setAlertType('error')
      }
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao excluir categoria:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível excluir a categoria.')
    } finally {
      setIsCatalogSaving(false)
    }
  }

  const excluirDepartamento = async (departamento: Departamento) => {
    if (!window.confirm(`Deseja excluir o departamento "${departamento.nome}"?`)) return

    setIsCatalogSaving(true)
    try {
      const response = await api.delete<ApiResponse<null>>(`/departamentos/${departamento.id}`)
      if (response.data.sucesso) {
        setDepartamentos((prev) => prev.filter((item) => item.id !== departamento.id))
        setAlertType('success')
      } else {
        setAlertType('error')
      }
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao excluir departamento:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível excluir o departamento.')
    } finally {
      setIsCatalogSaving(false)
    }
  }

  const alterarTipo = async (usuarioId: number, tipo: TipoUsuario) => {
    setSavingId(usuarioId)
    try {
      const response = await api.patch<ApiResponse<UsuarioAdmin>>(`/usuarios/${usuarioId}/tipo`, { tipo })
      if (response.data.sucesso) {
        atualizarUsuario(response.data.dado)
        setAlertType('success')
      } else {
        setAlertType('error')
      }
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao alterar tipo de usuário:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível alterar o tipo do usuário.')
    } finally {
      setSavingId(null)
    }
  }

  const excluirUsuario = async (usuarioId: number) => {
    if (!window.confirm('Deseja desativar este usuário?')) return

    setSavingId(usuarioId)
    try {
      const response = await api.delete<ApiResponse<UsuarioAdmin>>(`/usuarios/${usuarioId}`)
      if (response.data.sucesso) {
        atualizarUsuario(response.data.dado)
        setAlertType('success')
      } else {
        setAlertType('error')
      }
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao desativar usuário:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível desativar o usuário.')
    } finally {
      setSavingId(null)
    }
  }

  if (!user?.admin) {
    return (
      <main className="admin-page">
        <Alert message={alertMessage} onClose={() => setAlertMessage('')} type={alertType} />
        <section className="admin-header">
          <p className="eyebrow">Configurações</p>
          <h1>Acesso restrito</h1>
          <p>Somente administradores podem acessar esta área.</p>
        </section>
      </main>
    )
  }

  return (
    <main className="admin-page">
      <Alert message={alertMessage} onClose={() => setAlertMessage('')} type={alertType} />

      <section className="admin-header">
        <div>
          <p className="eyebrow">Configurações</p>
          <h1>Painel administrativo</h1>
          <p>Gerencie usuários, categorias e departamentos.</p>
        </div>
        <button type="button" className="admin-refresh" onClick={atualizarTela} disabled={isLoading || isCatalogLoading}>
          <RefreshCw aria-hidden="true" />
          Atualizar
        </button>
      </section>

      <section className="admin-stats">
        <div>
          <span>Total</span>
          <strong>{stats.total}</strong>
        </div>
        <div>
          <span>Ativos</span>
          <strong>{stats.ativos}</strong>
        </div>
        <div>
          <span>Administradores</span>
          <strong>{stats.admins}</strong>
        </div>
        <div>
          <span>Inativos</span>
          <strong>{stats.inativos}</strong>
        </div>
      </section>

      <div className="admin-content-grid">
      <section className="admin-management">
        <div className="admin-panel">
          <div className="admin-panel-header">
            <h2>Categorias</h2>
            <span>{categorias.length}</span>
          </div>
          <form className="admin-form" onSubmit={criarCategoria}>
            <input
              type="text"
              value={categoriaNome}
              onChange={(event) => setCategoriaNome(event.target.value)}
              placeholder="Nova categoria"
            />
            <button type="submit" disabled={isCatalogSaving}>
              <Plus aria-hidden="true" />
              Salvar
            </button>
          </form>
          <div className="admin-list">
            {isCatalogLoading && <p>Carregando...</p>}
            {!isCatalogLoading &&
              categorias.map((item) => (
                <div className="admin-list-item" key={item.id}>
                  <span>{item.nome}</span>
                  <button type="button" onClick={() => excluirCategoria(item)} disabled={isCatalogSaving}>
                    <Trash2 aria-hidden="true" />
                    Excluir
                  </button>
                </div>
              ))}
          </div>
        </div>

        <div className="admin-panel">
          <div className="admin-panel-header">
            <h2>Departamentos</h2>
            <span>{departamentos.length}</span>
          </div>
          <form className="admin-form" onSubmit={criarDepartamento}>
            <input
              type="text"
              value={departamentoNome}
              onChange={(event) => setDepartamentoNome(event.target.value)}
              placeholder="Novo departamento"
            />
            <button type="submit" disabled={isCatalogSaving}>
              <Plus aria-hidden="true" />
              Salvar
            </button>
          </form>
          <div className="admin-list">
            {isCatalogLoading && <p>Carregando...</p>}
            {!isCatalogLoading &&
              departamentos.map((item) => (
                <div className="admin-list-item" key={item.id}>
                  <span>{item.nome}</span>
                  <button type="button" onClick={() => excluirDepartamento(item)} disabled={isCatalogSaving}>
                    <Trash2 aria-hidden="true" />
                    Excluir
                  </button>
                </div>
              ))}
          </div>
        </div>
      </section>

      <div className="admin-users-column">
      <section className="admin-user-filter">
        <label>
          <span>Buscar usuário</span>
          <input
            type="search"
            value={buscaUsuario}
            onChange={(event) => setBuscaUsuario(event.target.value)}
            placeholder="Nome ou e-mail"
          />
        </label>
        {buscaUsuario && (
          <button type="button" onClick={() => setBuscaUsuario('')}>
            <X aria-hidden="true" />
            Limpar busca
          </button>
        )}
      </section>

      <section className="admin-table-wrap">
        {isLoading && <p className="admin-empty">Carregando usuários...</p>}
        {!isLoading && usuarios.length === 0 && <p className="admin-empty">Nenhum usuário encontrado.</p>}
        {!isLoading && usuarios.length > 0 && usuariosFiltrados.length === 0 && (
          <p className="admin-empty">Nenhum usuário encontrado para esta busca.</p>
        )}
        {!isLoading && usuariosFiltrados.length > 0 && (
          <table className="admin-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Tipo</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {usuariosPaginados.map((item) => {
                const isSelf = item.id === user.id
                const isSaving = savingId === item.id

                return (
                  <tr key={item.id}>
                    <td>{item.nome}</td>
                    <td>{item.email}</td>
                    <td>
                      <span className={`admin-badge ${item.tipo.toLowerCase()}`}>{item.tipo}</span>
                    </td>
                    <td>
                      <span className={`admin-badge ${item.ativo ? 'ativo' : 'inativo'}`}>
                        {item.ativo ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td>
                      <div className="admin-actions">
                        {item.tipo === 'COMUM' ? (
                          <button
                            type="button"
                            onClick={() => alterarTipo(item.id, 'ADMIN')}
                            disabled={isSaving || !item.ativo}
                          >
                            <UserPlus aria-hidden="true" />
                            Promover
                          </button>
                        ) : (
                          <button
                            type="button"
                            onClick={() => alterarTipo(item.id, 'COMUM')}
                            disabled={isSaving || !item.ativo || isSelf}
                          >
                            <UserMinus aria-hidden="true" />
                            Rebaixar
                          </button>
                        )}
                        <button
                          type="button"
                          className="danger"
                          onClick={() => excluirUsuario(item.id)}
                          disabled={isSaving || !item.ativo || isSelf}
                        >
                          <UserX aria-hidden="true" />
                          Desativar
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </section>

      {!isLoading && usuariosFiltrados.length > 0 && (
        <section className="admin-pagination-bar">
          <span className="admin-pagination-info">
            Mostrando {inicioUsuarios + 1}-{Math.min(inicioUsuarios + usuariosPorPagina, usuariosFiltrados.length)} de{' '}
            {usuariosFiltrados.length}
          </span>

          <label className="admin-pagination-size">
            <span>Por página</span>
            <select value={usuariosPorPagina} onChange={(event) => setUsuariosPorPagina(Number(event.target.value))}>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={30}>30</option>
            </select>
          </label>

          <div className="admin-pagination-actions">
            <button
              type="button"
              onClick={() => setPaginaUsuarios((prev) => Math.max(prev - 1, 1))}
              disabled={paginaUsuarios === 1}
            >
              <ChevronLeft aria-hidden="true" />
              Anterior
            </button>
            <span>
              Página {paginaUsuarios} de {totalPaginasUsuarios}
            </span>
            <button
              type="button"
              onClick={() => setPaginaUsuarios((prev) => Math.min(prev + 1, totalPaginasUsuarios))}
              disabled={paginaUsuarios === totalPaginasUsuarios}
            >
              Próxima
              <ChevronRight aria-hidden="true" />
            </button>
          </div>
        </section>
      )}
      </div>
      </div>
    </main>
  )
}
