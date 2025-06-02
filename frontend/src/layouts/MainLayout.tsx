import React, { useState, useEffect } from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Menu, X, User, LogOut, Home, Building, Calendar, LogIn } from 'lucide-react';

const MainLayout = () => {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const location = useLocation();
  
  // Fechar menu ao mudar de rota
  useEffect(() => {
    setIsMenuOpen(false);
  }, [location]);
  
  // Detectar scroll para mudar a aparência do header
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };
    
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);
  
  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <header 
        className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
          isScrolled ? 'bg-white shadow-md py-2' : 'bg-transparent py-4'
        }`}
      >
        <div className="container-custom flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center">
            <Building className="h-8 w-8 text-azul-colonial-600" />
            <span className="ml-2 text-2xl font-bold font-heading text-azul-colonial-800">
              AirbnBES
            </span>
          </Link>
          
          {/* Menu para desktop */}
          <nav className="hidden md:flex items-center space-x-6">
            <Link 
              to="/" 
              className="text-gray-700 hover:text-azul-colonial-600 font-medium transition-colors"
            >
              Início
            </Link>
            <Link 
              to="/propriedades" 
              className="text-gray-700 hover:text-azul-colonial-600 font-medium transition-colors"
            >
              Propriedades
            </Link>
            {isAuthenticated && (
              <Link 
                to="/meus-alugueis" 
                className="text-gray-700 hover:text-azul-colonial-600 font-medium transition-colors"
              >
                Meus Aluguéis
              </Link>
            )}
            {isAdmin && (
              <Link 
                to="/admin" 
                className="text-gray-700 hover:text-azul-colonial-600 font-medium transition-colors"
              >
                Painel Admin
              </Link>
            )}
            
            {isAuthenticated ? (
              <div className="relative group">
                <button className="flex items-center space-x-2 text-gray-700 hover:text-azul-colonial-600 font-medium transition-colors">
                  <User className="h-5 w-5" />
                  <span>{user?.name}</span>
                </button>
                <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-2 invisible group-hover:visible opacity-0 group-hover:opacity-100 transition-all duration-200 transform translate-y-2 group-hover:translate-y-0">
                  <button 
                    onClick={logout}
                    className="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    <LogOut className="h-4 w-4 mr-2" />
                    Sair
                  </button>
                </div>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link 
                  to="/login" 
                  className="px-4 py-2 text-azul-colonial-600 hover:text-azul-colonial-700 font-medium"
                >
                  Entrar
                </Link>
                <Link 
                  to="/cadastro" 
                  className="px-4 py-2 bg-azul-colonial-600 hover:bg-azul-colonial-700 text-white rounded-lg transition-colors"
                >
                  Cadastrar
                </Link>
              </div>
            )}
          </nav>
          
          {/* Botão de menu para mobile */}
          <button 
            className="md:hidden text-gray-700 focus:outline-none"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
      </header>
      
      {/* Menu mobile */}
      {isMenuOpen && (
        <div className="fixed inset-0 z-40 bg-white pt-16 md:hidden animate-fade-in">
          <div className="container-custom py-6 flex flex-col space-y-4">
            <Link 
              to="/" 
              className="flex items-center py-3 text-gray-700 hover:text-azul-colonial-600 font-medium"
            >
              <Home className="h-5 w-5 mr-3" />
              Início
            </Link>
            <Link 
              to="/propriedades" 
              className="flex items-center py-3 text-gray-700 hover:text-azul-colonial-600 font-medium"
            >
              <Building className="h-5 w-5 mr-3" />
              Propriedades
            </Link>
            {isAuthenticated && (
              <Link 
                to="/meus-alugueis" 
                className="flex items-center py-3 text-gray-700 hover:text-azul-colonial-600 font-medium"
              >
                <Calendar className="h-5 w-5 mr-3" />
                Meus Aluguéis
              </Link>
            )}
            {isAdmin && (
              <Link 
                to="/admin" 
                className="flex items-center py-3 text-gray-700 hover:text-azul-colonial-600 font-medium"
              >
                <User className="h-5 w-5 mr-3" />
                Painel Admin
              </Link>
            )}
            
            <div className="border-t border-gray-200 my-2 pt-4">
              {isAuthenticated ? (
                <>
                  <div className="flex items-center py-3">
                    <User className="h-5 w-5 mr-3 text-gray-700" />
                    <span className="font-medium">{user?.name}</span>
                  </div>
                  <button 
                    onClick={logout}
                    className="flex items-center py-3 text-gray-700 hover:text-azul-colonial-600 font-medium w-full"
                  >
                    <LogOut className="h-5 w-5 mr-3" />
                    Sair
                  </button>
                </>
              ) : (
                <div className="flex flex-col space-y-3">
                  <Link 
                    to="/login" 
                    className="flex items-center py-3 text-gray-700 hover:text-azul-colonial-600 font-medium"
                  >
                    <LogIn className="h-5 w-5 mr-3" />
                    Entrar
                  </Link>
                  <Link 
                    to="/cadastro" 
                    className="btn-primary w-full justify-center"
                  >
                    Cadastrar
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
      
      {/* Conteúdo principal */}
      <main className="flex-grow pt-16 page-transition">
        <Outlet />
      </main>
      
      {/* Footer */}
      <footer className="bg-gray-900 text-white py-10">
        <div className="container-custom">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div>
              <div className="flex items-center mb-4">
                <Building className="h-7 w-7 text-azul-colonial-400" />
                <span className="ml-2 text-xl font-bold font-heading">AirbnBES</span>
              </div>
              <p className="text-gray-400 mb-4">
                Plataforma de gerenciamento de aluguéis de propriedades, facilitando a conexão entre proprietários e inquilinos.
              </p>
            </div>
            
            <div>
              <h4 className="text-lg font-semibold mb-4">Links Rápidos</h4>
              <ul className="space-y-2">
                <li>
                  <Link to="/" className="text-gray-400 hover:text-white transition-colors">
                    Início
                  </Link>
                </li>
                <li>
                  <Link to="/propriedades" className="text-gray-400 hover:text-white transition-colors">
                    Propriedades
                  </Link>
                </li>
                <li>
                  <Link to="/login" className="text-gray-400 hover:text-white transition-colors">
                    Entrar
                  </Link>
                </li>
                <li>
                  <Link to="/cadastro" className="text-gray-400 hover:text-white transition-colors">
                    Cadastrar
                  </Link>
                </li>
              </ul>
            </div>
            
            <div>
              <h4 className="text-lg font-semibold mb-4">Contato</h4>
              <p className="text-gray-400 mb-2">contato@airbnbes.com.br</p>
              <p className="text-gray-400 mb-2">+55 (11) 1234-5678</p>
              <p className="text-gray-400">São Paulo, SP - Brasil</p>
            </div>
          </div>
          
          <div className="border-t border-gray-800 mt-8 pt-6 text-center text-gray-500">
            <p>&copy; {new Date().getFullYear()} AirbnBES. Todos os direitos reservados.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;