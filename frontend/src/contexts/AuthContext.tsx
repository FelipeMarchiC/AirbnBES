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
  const register = async (name: string, email: string, password: string) => {
    try {
      setLoading(true);

      // Chamada real à API de registro
      const response = await api.post('/register', {
        name,
        email,
        password,
        lastname: name.split(' ').slice(1).join(' ') || '', // Assuming name might contain first and last, or just first. Adjust as needed.
      });

      // Se o registro for bem-sucedido, o backend pode retornar um ID ou uma mensagem.
      // Neste caso, a sua API de backend para registro retorna um UUID do usuário registrado.
      // Não há token de autenticação retornado no registro, apenas no login.
      console.log('Registro bem-sucedido:', response.data);

      toast.success('Cadastro realizado com sucesso! Agora você pode fazer login.');
      navigate('/login', { replace: true });
    } catch (error: any) {
      let errorMessage = 'Erro ao criar conta. Tente novamente.';
      if (error.response && error.response.status === 409) {
        errorMessage = 'Este e-mail já está cadastrado. Por favor, faça login ou use outro e-mail.';
      } else if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      }
      toast.error(errorMessage);
      console.error('Erro ao cadastrar:', error);
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