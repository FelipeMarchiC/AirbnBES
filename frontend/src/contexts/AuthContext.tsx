import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

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

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  
  useEffect(() => {
    const token = localStorage.getItem('@AirbnBES:token');
    const storedUser = localStorage.getItem('@AirbnBES:user');

    if (token && storedUser) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setUser(JSON.parse(storedUser));
    }

    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    try {
      setLoading(true);

      const response = await api.post('/authenticate', {
        username: email,
        password,
      });

      const { token } = response.data;

      const payload = JSON.parse(atob(token.split('.')[1]));

      const user: User = {
        id: payload.id || '',
        name: payload.name || 'Usuário',
        email: payload.sub,
        role: payload.role === 'ADMIN' ? 'ADMIN' : 'USER',
      };
      
      localStorage.setItem('@AirbnBES:token', token);
      localStorage.setItem('@AirbnBES:user', JSON.stringify(user));
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;

      setUser(user);
      toast.success('Login realizado com sucesso!');

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

  
  const register = async (name: string, email: string, password: string) => {
    try {
      setLoading(true);

      const response = await api.post('/register', {
        name,
        email,
        password,
        lastname: name.split(' ').slice(1).join(' ') || '',
      });


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

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }

  return context;
};