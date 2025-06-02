import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

const NotFoundPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="max-w-md w-full text-center">
        <h1 className="text-6xl font-bold text-azul-colonial-600 mb-4">404</h1>
        <h2 className="text-2xl font-semibold text-gray-800 mb-4">Página não encontrada</h2>
        <p className="text-gray-600 mb-8">
          A página que você está procurando não existe ou foi removida.
        </p>
        <Link 
          to="/" 
          className="inline-flex items-center btn-primary"
        >
          <ArrowLeft className="h-5 w-5 mr-2" />
          Voltar para a página inicial
        </Link>
      </div>
    </div>
  );
};

export default NotFoundPage;