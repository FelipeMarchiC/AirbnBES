// AuthProvider.tsx
import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

// Tipos
type User = {
  id: string;
  name: string;
  email: string;
  role: 'ADMIN' | 'USER';
};

type AuthContextType = {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
};

// Contexto
const AuthContext = createContext<AuthContextType>({} as AuthContextType);

// Provider
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  

  // Verificar se o usuário está autenticado ao carregar a página
  useEffect(() => {
    const token = localStorage.getItem('@AirbnBES:token');
    const storedUser = localStorage.getItem('@AirbnBES:user');

    if (token && storedUser) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setUser(JSON.parse(storedUser));
    }

    setLoading(false);
  }, []);

  // Login
  const login = async (email: string, password: string) => {
    try {
      setLoading(true);

      // Chamada real à API
      const response = await api.post('/authenticate', {
        username: email, // o backend espera "username"
        password,
      });

      const { token } = response.data;

      // Decodificar o token JWT (payload base64 do segundo segmento)
      const payload = JSON.parse(atob(token.split('.')[1]));

      const user: User = {
        id: payload.id || '', // Certifique-se de que o payload contém o campo 'id'
        name: payload.name || 'Usuário',
        email: payload.sub, // O campo 'sub' normalmente representa o e-mail ou username
        role: payload.role === 'ADMIN' ? 'ADMIN' : 'USER',
      };
      

      // Armazenar token e usuário no localStorage
      localStorage.setItem('@AirbnBES:token', token);
      localStorage.setItem('@AirbnBES:user', JSON.stringify(user));
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      console.log('Payload decodificado:', JSON.parse(atob(token.split('.')[1])));

      setUser(user);
      toast.success('Login realizado com sucesso!');

      // Redirecionar com replace para evitar problemas de histórico
      if (user.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
      
    } catch (error: any) {
      toast.error('Credenciais inválidas. Tente novamente.');
      console.error('Erro no login:', error);
    } finally {
      setLoading(false);
    }
  };

  

  // Registro
  const register = async (_name: string, _email: string, _password: string) => {
    try {
      setLoading(true);

      // Simulação de API de registro
      await new Promise(resolve => setTimeout(resolve, 1000));

      toast.success('Cadastro realizado com sucesso!');
      navigate('/login', { replace: true });
    } catch (error) {
      toast.error('Erro ao criar conta. Tente novamente.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  

  // Logout
  const logout = () => {
    localStorage.removeItem('@AirbnBES:token');
    localStorage.removeItem('@AirbnBES:user');
    delete api.defaults.headers.common['Authorization'];
    setUser(null);
    navigate('/login', { replace: true });
    toast.success('Logout realizado com sucesso!');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isAdmin: user?.role === 'ADMIN',
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// Hook personalizado para usar o contexto
export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }

  return context;
};
