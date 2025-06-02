import React, { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { LogIn, Building } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const LoginPage = () => {
  const { isAuthenticated, login, loading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});

  // Redirecionar se já estiver autenticado
  if (isAuthenticated) {
    return <Navigate to="/" />;
  }

  const validate = () => {
    const newErrors: { email?: string; password?: string } = {};

    if (!email) {
      newErrors.email = 'E-mail é obrigatório';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = 'E-mail inválido';
    }

    if (!password) {
      newErrors.password = 'Senha é obrigatória';
    }
    // Removida a verificação do tamanho da senha

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (validate()) {
      try {
        await login(email, password);
      } catch (error) {
        console.error('Erro ao fazer login:', error);
      }
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full bg-white rounded-xl shadow-md p-8 space-y-8 animate-fade-in">
        <div className="text-center">
          <Link to="/" className="inline-flex items-center justify-center">
            <Building className="h-10 w-10 text-azul-colonial-600" />
          </Link>
          <h2 className="mt-4 text-3xl font-bold text-gray-800">Bem-vindo de volta</h2>
          <p className="mt-2 text-gray-600">
            Entre com sua conta para acessar o AirbnBES
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label htmlFor="email" className="form-label">
                E-mail
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                className={`input ${errors.email ? 'border-terracota-500 focus:ring-terracota-500' : ''}`}
                placeholder="seu@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              {errors.email && (
                <p className="mt-1 text-sm text-terracota-600">{errors.email}</p>
              )}
            </div>

            <div>
              <div className="flex items-center justify-between">
                <label htmlFor="password" className="form-label">
                  Senha
                </label>
                {/* O botão "Esqueceu a senha?" foi removido daqui */}
              </div>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                className={`input ${errors.password ? 'border-terracota-500 focus:ring-terracota-500' : ''}`}
                placeholder="********"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              {errors.password && (
                <p className="mt-1 text-sm text-terracota-600">{errors.password}</p>
              )}
            </div>
          </div>

          <div>
            <button
              type="submit"
              className="btn-primary w-full py-3 flex items-center justify-center"
              disabled={loading}
            >
              {loading ? (
                <span className="flex items-center">
                  <svg
                    className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    ></path>
                  </svg>
                  Entrando...
                </span>
              ) : (
                <span className="flex items-center">
                  <LogIn className="h-5 w-5 mr-2" />
                  Entrar
                </span>
              )}
            </button>
          </div>
        </form>

        <div className="text-center mt-4">
          <p className="text-sm text-gray-600">
            Não tem uma conta?{' '}
            <Link to="/cadastro" className="font-medium text-azul-colonial-600 hover:text-azul-colonial-700">
              Cadastre-se
            </Link>
          </p>
        </div>

        <div className="text-center mt-8 border-t border-gray-200 pt-6">
          <p className="text-xs text-gray-500">
            Para testes, use:
          </p>
          <p className="text-xs text-gray-500 mt-1">
            <strong>Usuário regular:</strong> uriel@eustacio.com / bes
          </p>
          <p className="text-xs text-gray-500 mt-1">
            <strong>Administrador:</strong> roberto-abadia@gmail.com / bes
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;