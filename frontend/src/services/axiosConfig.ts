import api from './api';
import toast from 'react-hot-toast';

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('@AirbnBES:token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptador de respostas
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // O servidor respondeu com um status de erro
      const { status } = error.response;
      
      if (status === 401) {
        localStorage.removeItem('@AirbnBES:token');
        localStorage.removeItem('@AirbnBES:user');
        
        // Redirecionando para login
        if (window.location.pathname !== '/login') {
          toast.error('Sessão expirada. Por favor, faça login novamente.');
          window.location.href = '/login';
        }
      } else if (status === 403) {
        toast.error('Você não tem permissão para acessar este recurso.');
      } else if (status === 404) {
        toast.error('Recurso não encontrado.');
      } else if (status >= 500) {
        toast.error('Erro no servidor. Tente novamente mais tarde.');
      }
    } else if (error.request) {
      toast.error('Não foi possível conectar ao servidor. Verifique sua conexão.');
    } else {
      toast.error('Erro ao processar sua solicitação.');
    }
    
    return Promise.reject(error);
  }
);

export default api;