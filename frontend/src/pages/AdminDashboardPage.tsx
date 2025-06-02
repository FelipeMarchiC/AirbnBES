import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Users, Building, ArrowUpRight } from 'lucide-react';
import rentalService, { Rental } from '../services/rentalService';
import propertyService from '../services/propertyService';
import RentalStatusBadge from '../components/RentalStatusBadge';

const AdminDashboardPage = () => {
  const [recentRentals, setRecentRentals] = useState<Rental[]>([]);
  const [totalProperties, setTotalProperties] = useState(0);
  const [totalRentals, setTotalRentals] = useState(0);
  const [pendingRentals, setPendingRentals] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  // Formatador de moeda
  const formatCurrency = (value?: number) => {
    if (typeof value !== 'number') return 'R$ 0,00';
    return value.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  };

  // Formatar data
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(date);
  };

  // Buscar dados para o dashboard
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setIsLoading(true);

        // Buscar todas as propriedades
        const properties = await propertyService.getAllProperties();
        setTotalProperties(properties.length);

        // Buscar todos os aluguéis
        const rentals = await rentalService.getAllRentals();

        setTotalRentals(rentals.length);

        // Contar aluguéis pendentes (assumindo 'PENDING' ou 'PENDENTE')
        const pending = rentals.filter(r => r.state === 'PENDING' || r.state === 'PENDENTE');
        setPendingRentals(pending.length);

        // Aluguéis recentes (últimos 5)
        setRecentRentals(rentals.slice(0, 5));
      } catch (error) {
        console.error('Erro ao buscar dados do dashboard:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  // Componente de card para estatísticas
  const StatCard = ({ title, value, icon, color }: { 
    title: string; 
    value: string | number; 
    icon: React.ReactNode; 
    color: string; 
  }) => (
    <div className="bg-white rounded-xl shadow-md p-6">
      <div className="flex items-center">
        <div className={`flex items-center justify-center h-12 w-12 rounded-lg ${color} text-white mr-4`}>
          {icon}
        </div>
        <div>
          <h3 className="text-sm font-medium text-gray-500">{title}</h3>
          <p className="text-2xl font-bold text-gray-800">{value}</p>
        </div>
      </div>
    </div>
  );

  if (isLoading) {
    return (
      <div className="animate-pulse space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="bg-white rounded-xl shadow-md p-6">
              <div className="flex items-center">
                <div className="h-12 w-12 rounded-lg bg-gray-200 mr-4"></div>
                <div>
                  <div className="h-4 bg-gray-200 rounded w-20 mb-2"></div>
                  <div className="h-8 bg-gray-200 rounded w-16"></div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="h-6 bg-gray-200 rounded w-48 mb-6"></div>
          <div className="space-y-4">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex justify-between items-center pb-4 border-b border-gray-100">
                <div className="flex-1">
                  <div className="h-5 bg-gray-200 rounded w-1/3 mb-2"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/4"></div>
                </div>
                <div className="h-6 bg-gray-200 rounded w-24"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Cards de estatísticas */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard 
          title="Total de Propriedades" 
          value={totalProperties} 
          icon={<Building className="h-6 w-6" />} 
          color="bg-azul-colonial-600"
        />
        <StatCard 
          title="Total de Aluguéis" 
          value={totalRentals} 
          icon={<Calendar className="h-6 w-6" />} 
          color="bg-verde-mata-600"
        />
        <StatCard 
          title="Aluguéis Pendentes" 
          value={pendingRentals} 
          icon={<Users className="h-6 w-6" />} 
          color="bg-areia-600"
        />
      </div>

      {/* Aluguéis recentes */}
      <div className="bg-white rounded-xl shadow-md p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-lg font-semibold text-gray-800">Aluguéis Recentes</h2>
          <Link 
            to="/admin/alugueis" 
            className="text-sm text-azul-colonial-600 hover:text-azul-colonial-700 flex items-center"
          >
            Ver todos
            <ArrowUpRight className="h-4 w-4 ml-1" />
          </Link>
        </div>

        {recentRentals.length === 0 ? (
          <p className="text-gray-500 text-center py-4">Nenhum aluguel registrado.</p>
        ) : (
          <div className="space-y-4">
            {recentRentals.map((rental) => (
              <div 
                key={rental.id}
                className="flex justify-between items-center pb-4 border-b border-gray-100"
              >
                <div>
                  <p className="font-medium text-gray-800">
                    {rental.username}
                  </p>
                  <p className="text-sm text-gray-500">
                    {rental.propertyName} • {formatDate(rental.startDate)} a {formatDate(rental.endDate)}
                  </p>
                </div>
                <div className="flex items-center space-x-4">
                  <span className="text-azul-colonial-600 font-semibold">
                    {formatCurrency(rental.price)}
                  </span>
                  <RentalStatusBadge status={rental.state} />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Links rápidos */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Link 
          to="/admin/alugueis" 
          className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div className="flex items-center">
            <div className="flex items-center justify-center h-12 w-12 rounded-lg bg-areia-100 text-areia-600 mr-4">
              <Calendar className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-medium text-gray-800">Gerenciar Aluguéis</h3>
              <p className="text-sm text-gray-500">
                Confirmar, recusar ou cancelar solicitações de aluguel
              </p>
            </div>
            <ArrowUpRight className="h-5 w-5 ml-auto text-gray-400" />
          </div>
        </Link>

        <Link 
          to="/" 
          className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div className="flex items-center">
            <div className="flex items-center justify-center h-12 w-12 rounded-lg bg-azul-colonial-100 text-azul-colonial-600 mr-4">
              <Building className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-medium text-gray-800">Voltar ao Site</h3>
              <p className="text-sm text-gray-500">
                Visualizar o site como um usuário comum
              </p>
            </div>
            <ArrowUpRight className="h-5 w-5 ml-auto text-gray-400" />
          </div>
        </Link>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
