import { useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { 
  Building, 
  LogOut, 
  LayoutDashboard, 
  CalendarCheck, 
  Menu, 
  X, 
  ChevronRight,
  Home
} from 'lucide-react';

const AdminLayout = () => {
  const { logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  
  const isActive = (path: string) => location.pathname === path;
  
  const handleLogout = () => {
    logout();
    navigate('/login');
  };
  
  return (
    <div className="min-h-screen bg-gray-100 flex">
      {}
      <aside className="hidden md:flex flex-col w-64 bg-azul-colonial-800 text-white">
        {}
        <div className="p-6 border-b border-azul-colonial-700">
          <Link to="/" className="flex items-center">
            <Building className="h-8 w-8 text-white" />
            <span className="ml-2 text-xl font-bold font-heading">
              AirbnBES
            </span>
          </Link>
        </div>
        
        {}
        <nav className="flex-1 overflow-y-auto py-4 px-3">
          <Link 
            to="/admin" 
            className={`flex items-center px-3 py-3 rounded-lg mb-1 transition-colors ${
              isActive('/admin') 
                ? 'bg-azul-colonial-700 text-white' 
                : 'text-gray-300 hover:bg-azul-colonial-700 hover:text-white'
            }`}
          >
            <LayoutDashboard className="h-5 w-5 mr-3" />
            Dashboard
          </Link>
          
          <Link 
            to="/admin/alugueis" 
            className={`flex items-center px-3 py-3 rounded-lg mb-1 transition-colors ${
              isActive('/admin/alugueis') 
                ? 'bg-azul-colonial-700 text-white' 
                : 'text-gray-300 hover:bg-azul-colonial-700 hover:text-white'
            }`}
          >
            <CalendarCheck className="h-5 w-5 mr-3" />
            Gerenciar Aluguéis
          </Link>
          
          <Link 
            to="/" 
            className="flex items-center px-3 py-3 rounded-lg mb-1 text-gray-300 hover:bg-azul-colonial-700 hover:text-white transition-colors"
          >
            <Home className="h-5 w-5 mr-3" />
            Voltar ao Site
          </Link>
        </nav>
        
        {}
        <div className="p-4 border-t border-azul-colonial-700">
          <button 
            onClick={handleLogout}
            className="flex items-center w-full px-3 py-2 text-gray-300 hover:text-white hover:bg-azul-colonial-700 rounded-lg transition-colors"
          >
            <LogOut className="h-5 w-5 mr-3" />
            Sair
          </button>
        </div>
      </aside>
      
      {}
      <div className="flex-1 flex flex-col">
        {}
        <header className="md:hidden bg-azul-colonial-800 text-white shadow-md">
          <div className="flex items-center justify-between p-4">
            <div className="flex items-center">
              <button 
                className="text-white focus:outline-none mr-4"
                onClick={() => setSidebarOpen(!sidebarOpen)}
              >
                <Menu className="h-6 w-6" />
              </button>
              <span className="text-lg font-bold font-heading">Painel Admin</span>
            </div>
            
            <Link to="/" className="text-white">
              <Home className="h-6 w-6" />
            </Link>
          </div>
        </header>
        
        {}
        {sidebarOpen && (
          <div className="fixed inset-0 z-40 md:hidden">
            {}
            <div 
              className="fixed inset-0 bg-black opacity-30"
              onClick={() => setSidebarOpen(false)}
            ></div>
            
            {}
            <div className="fixed inset-y-0 left-0 w-64 bg-azul-colonial-800 text-white shadow-lg">
              <div className="flex items-center justify-between p-6 border-b border-azul-colonial-700">
                <Link to="/" className="flex items-center">
                  <Building className="h-7 w-7 text-white" />
                  <span className="ml-2 text-xl font-bold font-heading">
                    AirbnBES
                  </span>
                </Link>
                <button 
                  className="text-white focus:outline-none"
                  onClick={() => setSidebarOpen(false)}
                >
                  <X className="h-6 w-6" />
                </button>
              </div>
              
              <nav className="flex-1 overflow-y-auto py-4 px-3">
                <Link 
                  to="/admin" 
                  className={`flex items-center px-3 py-3 rounded-lg mb-1 transition-colors ${
                    isActive('/admin') 
                      ? 'bg-azul-colonial-700 text-white' 
                      : 'text-gray-300 hover:bg-azul-colonial-700 hover:text-white'
                  }`}
                  onClick={() => setSidebarOpen(false)}
                >
                  <LayoutDashboard className="h-5 w-5 mr-3" />
                  Dashboard
                </Link>
                
                <Link 
                  to="/admin/alugueis" 
                  className={`flex items-center px-3 py-3 rounded-lg mb-1 transition-colors ${
                    isActive('/admin/alugueis') 
                      ? 'bg-azul-colonial-700 text-white' 
                      : 'text-gray-300 hover:bg-azul-colonial-700 hover:text-white'
                  }`}
                  onClick={() => setSidebarOpen(false)}
                >
                  <CalendarCheck className="h-5 w-5 mr-3" />
                  Gerenciar Aluguéis
                </Link>
                
                <Link 
                  to="/" 
                  className="flex items-center px-3 py-3 rounded-lg mb-1 text-gray-300 hover:bg-azul-colonial-700 hover:text-white transition-colors"
                  onClick={() => setSidebarOpen(false)}
                >
                  <Home className="h-5 w-5 mr-3" />
                  Voltar ao Site
                </Link>
                
                <div className="border-t border-azul-colonial-700 mt-4 pt-4">
                  <button 
                    onClick={handleLogout}
                    className="flex items-center w-full px-3 py-2 text-gray-300 hover:text-white hover:bg-azul-colonial-700 rounded-lg transition-colors"
                  >
                    <LogOut className="h-5 w-5 mr-3" />
                    Sair
                  </button>
                </div>
              </nav>
            </div>
          </div>
        )}
        
        {}
        <header className="hidden md:block bg-white shadow-sm px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <h1 className="text-2xl font-semibold text-gray-800">
                {location.pathname === '/admin' && 'Dashboard'}
                {location.pathname === '/admin/alugueis' && 'Gerenciar Aluguéis'}
              </h1>
            </div>
          </div>
        </header>
        
        {}
        <main className="flex-1 p-6 overflow-y-auto">
          <div className="hidden md:flex items-center text-sm text-gray-500 mb-6">
            <Link to="/admin" className="hover:text-azul-colonial-600 transition-colors">
              Admin
            </Link>
            <ChevronRight className="h-4 w-4 mx-2" />
            <span className="font-medium text-gray-700">
              {location.pathname === '/admin' && 'Dashboard'}
              {location.pathname === '/admin/alugueis' && 'Gerenciar Aluguéis'}
            </span>
          </div>
          
          <div className="page-transition">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;