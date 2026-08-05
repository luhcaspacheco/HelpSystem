import { createContext, useContext, useState } from 'react'
import api, { type ApiResponse } from '@/services/api'

export interface User {
  id?: number
  nome: string
  email: string
  depId?: number
  tipo?: string
  admin: boolean
  ativo?: boolean
}

interface UserContextType {
  user: User | null
  token?: string | null
  addUser: (nome: string, email: string, senha: string, depId: number) => Promise<string>
  removeUser: (userid: string) => Promise<string>
  login: (email: string, password: string) => Promise<string>
  logout: () => Promise<string>
  isLoading: boolean
}

type LoginResponse = {
  token: string
  usuario: User
}

const UserContext = createContext<UserContextType | undefined>(undefined)

export const UserProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('usuario') || localStorage.getItem('user')
    return savedUser ? JSON.parse(savedUser) : null
  })

  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('token')
  })

  const [isLoading, setIsLoading] = useState(false)

  const login = async (email: string, password: string) => {
    setIsLoading(true)

    try {
      const response = await api.post<ApiResponse<LoginResponse>>('/login', {
        email,
        senha: password
      })

      const responseData = response.data

      if (!responseData.sucesso) {
        return responseData.mensagem
      }

      const userData = responseData.dado.usuario
      const authToken = responseData.dado.token

      setUser(userData)
      setToken(authToken)
      localStorage.setItem('usuario', JSON.stringify(userData))
      localStorage.setItem('token', authToken)

      return responseData.mensagem
    } catch (error) {
      console.error('Erro ao fazer login:', error)
      return 'Erro ao fazer login. Verifique o e-mail e a senha.'
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    setIsLoading(true)

    try {
      if (token) {
        await api.post<ApiResponse<null>>('/logout', {})
      }

      return 'Logout realizado com sucesso.'
    } catch (error) {
      console.error('Erro ao fazer logout:', error)
      return 'Sessão local encerrada. O backend não confirmou o logout.'
    } finally {
      setIsLoading(false)
      localStorage.removeItem('usuario')
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      setUser(null)
      setToken(null)
    }
  }

  const addUser = async (nome: string, email: string, senha: string, depId: number) => {
    setIsLoading(true)

    try {
      const response = await api.post<ApiResponse<User>>('/usuarios', {
        nome,
        email,
        senha,
        departamentoId: depId
      })

      return response.data.mensagem
    } catch (error) {
      console.error('Erro ao cadastrar usuário:', error)
      return 'Erro ao cadastrar usuário. Verifique os dados e tente novamente.'
    } finally {
      setIsLoading(false)
    }
  }

  const removeUser = async (userid: string) => {
    try {
      const response = await api.delete<ApiResponse<User>>(`/usuarios/${userid}`)
      return response.data.mensagem
    } catch (error) {
      console.error('Erro ao remover usuário:', error)
      return 'Erro ao remover usuário.'
    }
  }

  return (
    <UserContext.Provider value={{ user, token, login, logout, addUser, removeUser, isLoading }}>
      {children}
    </UserContext.Provider>
  )
}

export const useUser = (): UserContextType => {
  const context = useContext(UserContext)

  if (!context) {
    throw new Error('useUser must be used within a UserProvider')
  }

  return context
}
