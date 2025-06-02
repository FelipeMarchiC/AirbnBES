import axios from 'axios';

// Criando instância do axios com configuração base
const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;