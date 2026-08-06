import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { CheckCircle2, ChevronLeft, ChevronRight, Pencil, Plus, Send, Trash2, X } from 'lucide-react'
import './Solicitacoes.css'
import Button from '@components/button'
import Alert from '@components/alert'
import api, { type ApiResponse } from '@/services/api'
import { useUser } from '@/contexts/userContext'
import { formatDate, formatDateTime } from '@/utils/datetime'

type Categoria = { id: number; nome: string }
type Prioridade = 'BAIXA' | 'MEDIA' | 'ALTA'
type StatusSolicitacao = 'ABERTA' | 'RESPONDIDA' | 'RESOLVIDA'
type OrdenacaoSolicitacao = 'data' | 'prioridade'

type Solicitacao = {
  id: number
  titulo: string
  descricao: string
  prioridade: Prioridade
  status: StatusSolicitacao
  dataCriacao: string
  dataResolucao: string | null
  autorId: number
  autorNome: string
  autorDepartamento: string | null
  categoriaId: number
  categoriaNome: string
}

type Resposta = {
  id: number
  solicitacaoId: number
  texto: string
  dataCriacao: string
  autorId: number
  autorNome: string
}

type FormData = {
  titulo: string
  categoriaId: string
  prioridade: Prioridade
  descricao: string
}

type FiltrosData = {
  termo: string
  status: '' | StatusSolicitacao
  categoriaId: string
  somenteMinhas: boolean
  ordenarPor: OrdenacaoSolicitacao
}

const statusLabels: Record<StatusSolicitacao, string> = {
  ABERTA: 'Aberta',
  RESPONDIDA: 'Respondida',
  RESOLVIDA: 'Resolvida'
}

const prioridadeLabels: Record<Prioridade, string> = {
  BAIXA: 'Baixa',
  MEDIA: 'Média',
  ALTA: 'Alta'
}

const initialFormData: FormData = {
  titulo: '',
  categoriaId: '',
  prioridade: 'MEDIA',
  descricao: ''
}

const initialFiltrosData: FiltrosData = {
  termo: '',
  status: '',
  categoriaId: '',
  somenteMinhas: false,
  ordenarPor: 'data'
}

// formatDate e formatDateTime vêm de '@/utils/datetime' (exibem em horário de Brasília).

export default function Solicitacoes() {
  const { token, user } = useUser()
  const [solicitacoes, setSolicitacoes] = useState<Solicitacao[]>([])
  const [categorias, setCategorias] = useState<Categoria[]>([])
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [selectedSolicitacao, setSelectedSolicitacao] = useState<Solicitacao | null>(null)
  const [respostas, setRespostas] = useState<Resposta[]>([])
  const [respostaTexto, setRespostaTexto] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isLoadingRespostas, setIsLoadingRespostas] = useState(false)
  const [isSendingResposta, setIsSendingResposta] = useState(false)
  const [alertMessage, setAlertMessage] = useState('')
  const [alertType, setAlertType] = useState<'success' | 'error' | 'info'>('info')
  const [formData, setFormData] = useState<FormData>(initialFormData)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [filtros, setFiltros] = useState<FiltrosData>(initialFiltrosData)
  const [paginaAtual, setPaginaAtual] = useState(1)
  const [itensPorPagina, setItensPorPagina] = useState(5)

  const carregarCategorias = useCallback(async () => {
    try {
      const response = await api.get<ApiResponse<Categoria[]>>('/categorias')
      if (response.data.sucesso) {
        setCategorias(response.data.dado)
      } else {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
      }
    } catch (error) {
      console.error('Erro ao carregar categorias:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível carregar as categorias.')
    }
  }, [])

  const carregarSolicitacoes = useCallback(async () => {
    if (!token) {
      setSolicitacoes([])
      setAlertType('error')
      setAlertMessage('Faça login para visualizar as solicitações.')
      return
    }

    setIsLoading(true)
    try {
      const params = new URLSearchParams()
      if (filtros.termo.trim()) params.set('termo', filtros.termo.trim())
      if (filtros.status) params.set('status', filtros.status)
      if (filtros.categoriaId) params.set('categoriaId', filtros.categoriaId)
      if (filtros.somenteMinhas && user?.id) params.set('autorId', String(user.id))
      params.set('ordenarPor', filtros.ordenarPor)

      const query = params.toString()
      const response = await api.get<ApiResponse<Solicitacao[]>>(`/solicitacoes${query ? `?${query}` : ''}`)
      if (response.data.sucesso) {
        setSolicitacoes(response.data.dado)
      } else {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
      }
    } catch (error) {
      console.error('Erro ao carregar solicitações:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível carregar as solicitações.')
    } finally {
      setIsLoading(false)
    }
  }, [filtros, token, user?.id])

  useEffect(() => {
    carregarCategorias()
  }, [carregarCategorias])

  useEffect(() => {
    carregarSolicitacoes()
  }, [carregarSolicitacoes])

  const stats = useMemo(() => {
    return {
      total: solicitacoes.length,
      abertas: solicitacoes.filter((item) => item.status === 'ABERTA').length,
      respondidas: solicitacoes.filter((item) => item.status === 'RESPONDIDA').length,
      resolvidas: solicitacoes.filter((item) => item.status === 'RESOLVIDA').length
    }
  }, [solicitacoes])

  const totalPaginas = Math.max(1, Math.ceil(solicitacoes.length / itensPorPagina))
  const inicioPagina = (paginaAtual - 1) * itensPorPagina
  const solicitacoesPaginadas = solicitacoes.slice(inicioPagina, inicioPagina + itensPorPagina)

  useEffect(() => {
    setPaginaAtual(1)
  }, [filtros, itensPorPagina])

  useEffect(() => {
    if (paginaAtual > totalPaginas) {
      setPaginaAtual(totalPaginas)
    }
  }, [paginaAtual, totalPaginas])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!token) {
      setAlertType('error')
      setAlertMessage('Faça login antes de criar uma solicitação.')
      return
    }

    // Opção B: a validação dos campos fica a cargo do backend, que retorna
    // a mensagem exata (somente os campos realmente vazios).
    setIsSaving(true)
    try {
      const payload = {
        titulo: formData.titulo,
        descricao: formData.descricao,
        categoriaId: Number(formData.categoriaId),
        prioridade: formData.prioridade
      }

      // Editando uma solicitação existente (PUT) ou criando uma nova (POST).
      // Em ambos, o status não é alterado por aqui.
      const response = editingId
        ? await api.put<ApiResponse<Solicitacao>>(`/solicitacoes/${editingId}`, payload)
        : await api.post<ApiResponse<Solicitacao>>('/solicitacoes', payload)

      if (!response.data.sucesso) {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
        return
      }

      await carregarSolicitacoes()
      setFormData(initialFormData)
      setEditingId(null)
      setIsFormOpen(false)
      setAlertType('success')
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao salvar solicitação:', error)
      setAlertType('error')
      const mensagem =
        (error as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem
        ?? (editingId ? 'Não foi possível editar a solicitação.' : 'Não foi possível criar a solicitação.')
      setAlertMessage(mensagem)
    } finally {
      setIsSaving(false)
    }
  }

  const carregarRespostas = async (solicitacao: Solicitacao) => {
    setSelectedSolicitacao(solicitacao)
    setRespostas([])
    setRespostaTexto('')
    setIsLoadingRespostas(true)

    try {
      const response = await api.get<ApiResponse<Resposta[]>>(`/solicitacoes/${solicitacao.id}/respostas`)
      if (response.data.sucesso) {
        setRespostas(response.data.dado)
      } else {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
      }
    } catch (error) {
      console.error('Erro ao carregar respostas:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível carregar as respostas.')
    } finally {
      setIsLoadingRespostas(false)
    }
  }

  const handleRespostaSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!selectedSolicitacao) return

    if (!respostaTexto.trim()) {
      setAlertType('error')
      setAlertMessage('Digite uma resposta antes de enviar.')
      return
    }

    setIsSendingResposta(true)
    try {
      const response = await api.post<ApiResponse<Resposta>>(`/solicitacoes/${selectedSolicitacao.id}/respostas`, {
        texto: respostaTexto.trim()
      })

      if (!response.data.sucesso) {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
        return
      }

      setRespostas((prev) => [...prev, response.data.dado])
      setRespostaTexto('')
      setAlertType('success')
      setAlertMessage(response.data.mensagem)

      if (selectedSolicitacao.status === 'ABERTA') {
        const solicitacaoAtualizada = { ...selectedSolicitacao, status: 'RESPONDIDA' as StatusSolicitacao }
        setSelectedSolicitacao(solicitacaoAtualizada)
        await carregarSolicitacoes()
      }
    } catch (error) {
      console.error('Erro ao enviar resposta:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível enviar a resposta.')
    } finally {
      setIsSendingResposta(false)
    }
  }

  const podeFinalizar = (solicitacao: Solicitacao) => {
    return solicitacao.status !== 'RESOLVIDA' && (user?.admin || user?.id === solicitacao.autorId)
  }

  const podeExcluir = (solicitacao: Solicitacao) => {
    return user?.id === solicitacao.autorId
  }

  const podeEditar = (solicitacao: Solicitacao) => {
    return user?.id === solicitacao.autorId && solicitacao.status !== 'RESOLVIDA'
  }

  const abrirEdicao = (solicitacao: Solicitacao) => {
    setEditingId(solicitacao.id)
    setFormData({
      titulo: solicitacao.titulo,
      categoriaId: String(solicitacao.categoriaId),
      prioridade: solicitacao.prioridade,
      descricao: solicitacao.descricao
    })
    setSelectedSolicitacao(null)
    setIsFormOpen(true)
  }

  const finalizarSolicitacao = async (solicitacao: Solicitacao) => {
    try {
      const response = await api.patch<ApiResponse<Solicitacao>>(`/solicitacoes/${solicitacao.id}/resolver`)

      if (!response.data.sucesso) {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
        return
      }

      const solicitacaoResolvida = response.data.dado
      setSelectedSolicitacao((prev) => (prev?.id === solicitacaoResolvida.id ? solicitacaoResolvida : prev))
      await carregarSolicitacoes()
      setAlertType('success')
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao finalizar solicitação:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível finalizar a solicitação.')
    }
  }

  const excluirSolicitacao = async (solicitacao: Solicitacao) => {
    if (!window.confirm('Deseja excluir esta solicitação?')) return

    try {
      const response = await api.delete<ApiResponse<null>>(`/solicitacoes/${solicitacao.id}`)

      if (!response.data.sucesso) {
        setAlertType('error')
        setAlertMessage(response.data.mensagem)
        return
      }

      setSelectedSolicitacao((prev) => (prev?.id === solicitacao.id ? null : prev))
      await carregarSolicitacoes()
      setAlertType('success')
      setAlertMessage(response.data.mensagem)
    } catch (error) {
      console.error('Erro ao excluir solicitação:', error)
      setAlertType('error')
      setAlertMessage('Não foi possível excluir a solicitação.')
    }
  }

  return (
    <div className="solicitacoes-page">
      <Alert message={alertMessage} onClose={() => setAlertMessage('')} type={alertType} />

      <div className="solicitacoes-header">
        <div>
          <p className="eyebrow">Central de atendimento</p>
          <h1>Solicitações</h1>
          <p className="subtitle">Acompanhe o status de cada pedido de forma rápida e organizada.</p>
        </div>
        <Button type="button" onClick={() => { setEditingId(null); setFormData(initialFormData); setIsFormOpen(true) }} disabled={!token}>
          <Plus aria-hidden="true" />
          Nova solicitação
        </Button>
      </div>

      <div className="filters-bar">
        <label className="filter-field search-field">
          <span>Buscar</span>
          <input
            type="search"
            value={filtros.termo}
            onChange={(event) => setFiltros((prev) => ({ ...prev, termo: event.target.value }))}
            placeholder="Título ou descrição"
          />
        </label>

        <label className="filter-field">
          <span>Status</span>
          <select
            value={filtros.status}
            onChange={(event) =>
              setFiltros((prev) => ({ ...prev, status: event.target.value as FiltrosData['status'] }))
            }
          >
            <option value="">Todos</option>
            <option value="ABERTA">Abertas</option>
            <option value="RESPONDIDA">Respondidas</option>
            <option value="RESOLVIDA">Resolvidas</option>
          </select>
        </label>

        <label className="filter-field">
          <span>Categoria</span>
          <select
            value={filtros.categoriaId}
            onChange={(event) => setFiltros((prev) => ({ ...prev, categoriaId: event.target.value }))}
          >
            <option value="">Todas</option>
            {categorias.map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nome}
              </option>
            ))}
          </select>
        </label>

        <label className="filter-field">
          <span>Ordenar por</span>
          <select
            value={filtros.ordenarPor}
            onChange={(event) =>
              setFiltros((prev) => ({ ...prev, ordenarPor: event.target.value as OrdenacaoSolicitacao }))
            }
          >
            <option value="data">Data</option>
            <option value="prioridade">Prioridade</option>
          </select>
        </label>

        <label className="filter-check">
          <input
            type="checkbox"
            checked={filtros.somenteMinhas}
            onChange={(event) => setFiltros((prev) => ({ ...prev, somenteMinhas: event.target.checked }))}
          />
          <span>Minhas solicitações</span>
        </label>

        <button type="button" className="secondary-button filter-clear" onClick={() => setFiltros(initialFiltrosData)}>
          <X aria-hidden="true" />
          Limpar filtros
        </button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <span>Total</span>
          <strong>{stats.total}</strong>
        </div>
        <div className="stat-card">
          <span>Abertas</span>
          <strong>{stats.abertas}</strong>
        </div>
        <div className="stat-card">
          <span>Respondidas</span>
          <strong>{stats.respondidas}</strong>
        </div>
        <div className="stat-card">
          <span>Resolvidas</span>
          <strong>{stats.resolvidas}</strong>
        </div>
      </div>

      <div className="requests-list">
        <div className="requests-list-header">
          <strong>Pedidos recentes</strong>
          <span>{solicitacoes.length} resultados</span>
        </div>
        {isLoading && <p className="empty-state">Carregando solicitações...</p>}
        {!isLoading && solicitacoes.length === 0 && <p className="empty-state">Nenhuma solicitação encontrada.</p>}
        {!isLoading &&
          solicitacoesPaginadas.map((item) => (
            <article
              className={`request-card priority-${item.prioridade.toLowerCase()} status-${item.status.toLowerCase()}`}
              key={item.id}
              role="button"
              tabIndex={0}
              onClick={() => carregarRespostas(item)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                  event.preventDefault()
                  carregarRespostas(item)
                }
              }}
            >
              <div className="request-main">
                <div className="request-top">
                  <h2>{item.titulo}</h2>
                  <span className={`status-badge ${item.status.toLowerCase()}`}>{statusLabels[item.status]}</span>
                </div>
                <p className="request-description">{item.descricao}</p>
              </div>
              <div className="request-meta">
                <span>{item.categoriaNome}</span>
                <span>{prioridadeLabels[item.prioridade]}</span>
                <span>{formatDate(item.dataCriacao)}</span>
              </div>
            </article>
          ))}
      </div>

      {!isLoading && solicitacoes.length > 0 && (
        <div className="pagination-bar">
          <div className="pagination-info">
            Mostrando {inicioPagina + 1}-{Math.min(inicioPagina + itensPorPagina, solicitacoes.length)} de{' '}
            {solicitacoes.length}
          </div>

          <label className="pagination-size">
            <span>Por página</span>
            <select
              value={itensPorPagina}
              onChange={(event) => setItensPorPagina(Number(event.target.value))}
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
            </select>
          </label>

          <div className="pagination-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={() => setPaginaAtual((prev) => Math.max(prev - 1, 1))}
              disabled={paginaAtual === 1}
            >
              <ChevronLeft aria-hidden="true" />
              Anterior
            </button>
            <span>
              Página {paginaAtual} de {totalPaginas}
            </span>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setPaginaAtual((prev) => Math.min(prev + 1, totalPaginas))}
              disabled={paginaAtual === totalPaginas}
            >
              Próxima
              <ChevronRight aria-hidden="true" />
            </button>
          </div>
        </div>
      )}

      {isFormOpen && (
        <div className="modal-backdrop" onClick={() => { setIsFormOpen(false); setEditingId(null) }}>
          <div className="modal-card" onClick={(event) => event.stopPropagation()}>
            <h2>{editingId ? 'Editar solicitação' : 'Nova solicitação'}</h2>
            <p>{editingId ? 'Atualize os dados da solicitação.' : 'Preencha os detalhes para criar um novo pedido.'}</p>
            <form className="request-form" onSubmit={handleSubmit}>
              <label className="form-group">
                <span>Título</span>
                <input
                  type="text"
                  value={formData.titulo}
                  onChange={(event) => setFormData((prev) => ({ ...prev, titulo: event.target.value }))}
                  placeholder="Ex.: Acesso ao sistema"
                />
              </label>
              <label className="form-group">
                <span>Categoria</span>
                <select
                  value={formData.categoriaId}
                  onChange={(event) => setFormData((prev) => ({ ...prev, categoriaId: event.target.value }))}
                >
                  <option value="">Selecione...</option>
                  {categorias.map((categoria) => (
                    <option key={categoria.id} value={categoria.id}>
                      {categoria.nome}
                    </option>
                  ))}
                </select>
              </label>
              <label className="form-group">
                <span>Prioridade</span>
                <select
                  value={formData.prioridade}
                  onChange={(event) =>
                    setFormData((prev) => ({ ...prev, prioridade: event.target.value as Prioridade }))
                  }
                >
                  <option value="BAIXA">Baixa</option>
                  <option value="MEDIA">Média</option>
                  <option value="ALTA">Alta</option>
                </select>
              </label>
              <label className="form-group">
                <span>Descrição</span>
                <textarea
                  value={formData.descricao}
                  onChange={(event) => setFormData((prev) => ({ ...prev, descricao: event.target.value }))}
                  placeholder="Descreva a solicitação"
                  rows={4}
                />
              </label>
              <div className="form-actions">
                <button type="button" className="secondary-button" onClick={() => { setIsFormOpen(false); setEditingId(null) }}>
                  Cancelar
                </button>
                <Button type="submit" disabled={isSaving}>
                  {editingId ? <Pencil aria-hidden="true" /> : <Plus aria-hidden="true" />}
                  {isSaving ? 'Salvando...' : editingId ? 'Salvar alterações' : 'Salvar'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {selectedSolicitacao && (
        <div className="modal-backdrop" onClick={() => setSelectedSolicitacao(null)}>
          <div className="modal-card answers-modal" role="dialog" aria-modal="true" onClick={(event) => event.stopPropagation()}>
            <div className="answers-header">
              <div>
                <div className="answers-badges">
                  <span className={`priority-badge ${selectedSolicitacao.prioridade.toLowerCase()}`}>
                    {prioridadeLabels[selectedSolicitacao.prioridade]} prioridade
                  </span>
                  <span className={`status-badge ${selectedSolicitacao.status.toLowerCase()}`}>
                    {statusLabels[selectedSolicitacao.status]}
                  </span>
                </div>
                <h2>{selectedSolicitacao.titulo}</h2>
              </div>
              <button type="button" className="modal-close" onClick={() => setSelectedSolicitacao(null)} aria-label="Fechar">
                <X aria-hidden="true" />
              </button>
            </div>
            <div className="answers-layout">
              <div className="answers-main">
                <p className="answers-description">{selectedSolicitacao.descricao}</p>
                <div className="answers-list">
                  {isLoadingRespostas && <p className="empty-state">Carregando respostas...</p>}
                  {!isLoadingRespostas && respostas.length === 0 && (
                    <p className="empty-state">Ainda não há respostas para esta solicitação.</p>
                  )}
                  {!isLoadingRespostas &&
                    respostas.map((resposta) => (
                      <div className="answer-item" key={resposta.id}>
                        <div className="answer-meta">
                          <strong>{resposta.autorNome}</strong>
                          <span>{formatDateTime(resposta.dataCriacao)}</span>
                        </div>
                        <p>{resposta.texto}</p>
                      </div>
                    ))}
                </div>

                {selectedSolicitacao.status !== 'RESOLVIDA' ? (
                  <form className="request-form answer-form" onSubmit={handleRespostaSubmit}>
                    <label className="form-group">
                      <span>Nova resposta</span>
                      <textarea
                        value={respostaTexto}
                        onChange={(event) => setRespostaTexto(event.target.value)}
                        placeholder="Escreva sua resposta"
                        rows={4}
                      />
                    </label>
                    <div className="form-actions">
                      {podeEditar(selectedSolicitacao) && (
                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => abrirEdicao(selectedSolicitacao)}
                        >
                          <Pencil aria-hidden="true" />
                          Editar
                        </button>
                      )}
                      {podeExcluir(selectedSolicitacao) && (
                        <button
                          type="button"
                          className="secondary-button danger"
                          onClick={() => excluirSolicitacao(selectedSolicitacao)}
                        >
                          <Trash2 aria-hidden="true" />
                          Excluir
                        </button>
                      )}
                      {podeFinalizar(selectedSolicitacao) && (
                        <button
                          type="button"
                          className="secondary-button success"
                          onClick={() => finalizarSolicitacao(selectedSolicitacao)}
                        >
                          <CheckCircle2 aria-hidden="true" />
                          Finalizar solicitação
                        </button>
                      )}
                      <Button type="submit" disabled={isSendingResposta}>
                        <Send aria-hidden="true" />
                        {isSendingResposta ? 'Enviando...' : 'Enviar resposta'}
                      </Button>
                    </div>
                  </form>
                ) : (
                  <div className="form-actions resolved-actions">
                    <p className="resolved-message">Solicitação resolvida. Novas respostas não podem ser enviadas.</p>
                    {podeExcluir(selectedSolicitacao) && (
                      <button
                        type="button"
                        className="secondary-button danger"
                        onClick={() => excluirSolicitacao(selectedSolicitacao)}
                      >
                        <Trash2 aria-hidden="true" />
                        Excluir solicitação
                      </button>
                    )}
                  </div>
                )}
              </div>

              <aside className="answers-details">
                <h3>Detalhes</h3>
                <dl>
                  <div><dt>Solicitante</dt><dd>{selectedSolicitacao.autorNome}</dd></div>
                  <div><dt>Departamento</dt><dd>{selectedSolicitacao.autorDepartamento ?? '—'}</dd></div>
                  <div><dt>Categoria</dt><dd>{selectedSolicitacao.categoriaNome}</dd></div>
                  <div><dt>Criada em</dt><dd>{formatDateTime(selectedSolicitacao.dataCriacao)}</dd></div>
                  {selectedSolicitacao.dataResolucao && (
                    <div><dt>Resolvida em</dt><dd>{formatDateTime(selectedSolicitacao.dataResolucao)}</dd></div>
                  )}
                </dl>
              </aside>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
