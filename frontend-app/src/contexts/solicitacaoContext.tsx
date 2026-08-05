import { createContext, useContext, useState } from 'react'
import type { User } from './userContext'

export interface Solicitacao {
  titulo: string
  descricao: string
  status: string
  dataCriacao: string
  dataResolucao: string | null
  autor: User
  categoriaId: number
}

interface SolicitacaoContextType {
  solicitacao: Solicitacao | null
  isLoading: boolean
  addSolicitacao: (solicitacao: Solicitacao) => Promise<string>
  updateSolicitacao: (solicitacao: Solicitacao) => Promise<string>
  deleteSolicitacao: (id: number) => Promise<string>
}

const SolicitacaoContext = createContext<SolicitacaoContextType | undefined>(undefined)

export const SolicitacaoProvider = ({ children }: { children: React.ReactNode }) => {
  const [solicitacao, setSolicitacao] = useState<Solicitacao | null>(() => {
    const savedSolicitacao = localStorage.getItem('solicitacao')
    return savedSolicitacao ? JSON.parse(savedSolicitacao) : null
  })
  const [isLoading] = useState(false)

  const addSolicitacao = async (novaSolicitacao: Solicitacao) => {
    setSolicitacao(novaSolicitacao)
    localStorage.setItem('solicitacao', JSON.stringify(novaSolicitacao))
    return 'Solicitação armazenada localmente.'
  }

  const updateSolicitacao = async (solicitacaoAtualizada: Solicitacao) => {
    setSolicitacao(solicitacaoAtualizada)
    localStorage.setItem('solicitacao', JSON.stringify(solicitacaoAtualizada))
    return 'Solicitação atualizada localmente.'
  }

  const deleteSolicitacao = async (_id: number) => {
    setSolicitacao(null)
    localStorage.removeItem('solicitacao')
    return 'Solicitação removida localmente.'
  }

  return (
    <SolicitacaoContext.Provider value={{ solicitacao, isLoading, addSolicitacao, updateSolicitacao, deleteSolicitacao }}>
      {children}
    </SolicitacaoContext.Provider>
  )
}

export const useSolicitacao = (): SolicitacaoContextType => {
  const context = useContext(SolicitacaoContext)
  if (!context) {
    throw new Error('useSolicitacao must be used within a SolicitacaoProvider')
  }
  return context
}
