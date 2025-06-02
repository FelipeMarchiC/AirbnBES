import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { Calendar, Search, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import rentalService, { Rental, RentalStatus } from '../services/rentalService'; // Importa os tipos e serviço corretos
import RentalStatusBadge from '../components/RentalStatusBadge';
import toast from 'react-hot-toast';

const AdminRentalsPage = () => {
  const [rentals, setRentals] = useState<Rental[]>([]);
  const [filteredRentals, setFilteredRentals] = useState<Rental[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<RentalStatus | 'TODOS'>('TODOS');
  const [searchQuery, setSearchQuery] = useState('');

  // Buscar todos os aluguéis
  useEffect(() => {
    const fetchAllRentals = async () => {
      try {
        setIsLoading(true);
        const data = await rentalService.getAllRentals();
        setRentals(data);
        setFilteredRentals(data);
      } catch (error) {
        console.error('Erro ao buscar aluguéis:', error);
        toast.error('Erro ao carregar aluguéis');
      } finally {
        setIsLoading(false);
      }
    };

    fetchAllRentals();
  }, []);

  // Aplicar filtros
  useEffect(() => {
    let result = rentals;

    // Filtrar por status
    if (statusFilter !== 'TODOS') {
      result = result.filter(rental => rental.status === statusFilter);
    }

    // Filtrar por busca
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(rental =>
        rental.tenantName.toLowerCase().includes(query) ||
        (rental.propertyName || '').toLowerCase().includes(query) || // Usa propertyName
        rental.id.toLowerCase().includes(query)
      );
    }

    setFilteredRentals(result);
  }, [rentals, statusFilter, searchQuery]);

  // Formatadores
  const formatCurrency = (value?: number) => {
    if (typeof value !== 'number') return 'R$ 0,00';
    return value.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  };

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'dd/MM/yyyy', { locale: ptBR });
  };

  // Ações de gestão
  const handleConfirmRental = async (rentalId: string) => {
    try {
      await rentalService.confirmRental(rentalId);
      toast.success('Aluguel confirmado com sucesso');

      setRentals(prevRentals =>
        prevRentals.map(rental =>
          rental.id === rentalId
            ? { ...rental, status: 'CONFIRMADO' }
            : rental
        )
      );
    } catch (error) {
      console.error('Erro ao confirmar aluguel:', error);
      toast.error('Erro ao confirmar aluguel');
    }
  };

  const handleDenyRental = async (rentalId: string) => {
    try {
      await rentalService.denyRental(rentalId);
      toast.success('Aluguel recusado com sucesso');

      setRentals(prevRentals =>
        prevRentals.map(rental =>
          rental.id === rentalId
            ? { ...rental, status: 'RECUSADO' }
            : rental
        )
      );
    } catch (error) {
      console.error('Erro ao recusar aluguel:', error);
      toast.error('Erro ao recusar aluguel');
    }
  };

  const handleCancelRental = async (rentalId: string) => {
    try {
      await rentalService.cancelRentalAsOwner(rentalId);
      toast.success('Aluguel cancelado com sucesso');

      setRentals(prevRentals =>
        prevRentals.map(rental =>
          rental.id === rentalId
            ? { ...rental, status: 'CANCELADO' }
            : rental
        )
      );
    } catch (error) {
      console.error('Erro ao cancelar aluguel:', error);
      toast.error('Erro ao cancelar aluguel');
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Filtros */}
      <div className="bg-white rounded-xl shadow-md p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Filtrar Aluguéis</h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Busca */}
          <div className="col-span-2">
            <label htmlFor="search" className="form-label">
              Buscar por inquilino ou propriedade
            </label>
            <div className="relative">
              <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
              <input
                type="text"
                id="search"
                placeholder="Digite para buscar..."
                className="input pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {/* Filtro de status */}
          <div>
            <label htmlFor="statusFilter" className="form-label">
              Status
            </label>
            <select
              id="statusFilter"
              className="select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as RentalStatus | 'TODOS')}
            >
              <option value="TODOS">Todos os status</option>
              <option value="PENDENTE">Pendente</option>
              <option value="CONFIRMADO">Confirmado</option>
              <option value="RECUSADO">Recusado</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>
        </div>
      </div>

      {/* Lista de aluguéis */}
      <div className="bg-white rounded-xl shadow-md p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-6">Gerenciar Aluguéis</h2>

        {isLoading ? (
          <div className="space-y-4">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="border border-gray-200 rounded-lg p-4 animate-pulse">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                  <div className="space-y-3 w-full">
                    <div className="h-5 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                    <div className="h-4 bg-gray-200 rounded w-1/3"></div>
                  </div>
                  <div className="mt-3 md:mt-0 flex space-x-2">
                    <div className="h-8 w-24 bg-gray-200 rounded"></div>
                    <div className="h-8 w-24 bg-gray-200 rounded"></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : filteredRentals.length === 0 ? (
          <div className="text-center py-8">
            <AlertCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-lg font-medium text-gray-600">Nenhum aluguel encontrado</p>
            <p className="text-gray-500">
              {searchQuery || statusFilter !== 'TODOS'
                ? 'Tente ajustar seus filtros para ver mais resultados.'
                : 'Não há aluguéis registrados no sistema.'}
            </p>

            {(searchQuery || statusFilter !== 'TODOS') && (
              <button
                onClick={() => {
                  setSearchQuery('');
                  setStatusFilter('TODOS');
                }}
                className="mt-4 btn-outline"
              >
                Limpar filtros
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            {filteredRentals.map((rental) => (
              <div
                key={rental.id}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                  <div>
                    <h3 className="font-semibold text-lg mb-2">
                      {/* Usa propertyName diretamente */}
                      {rental.propertyName || `Propriedade #${rental.propertyId}`}
                    </h3>
                    <p className="text-gray-600 mb-1">
                      <span className="font-medium">Inquilino:</span> {rental.tenantName}
                    </p>
                    <div className="flex items-center text-gray-600 mb-1">
                      <Calendar className="h-4 w-4 mr-1" />
                      <span>
                        {formatDate(rental.startDate)} a {formatDate(rental.endDate)}
                      </span>
                    </div>
                    <p className="text-gray-600 mb-2">
                      <span className="font-medium">Valor total:</span> {formatCurrency(rental.totalPrice)}
                    </p>
                    <div className="mb-3">
                      <RentalStatusBadge status={rental.status} />
                    </div>
                  </div>

                  <div className="flex flex-col md:flex-row space-y-2 md:space-y-0 md:space-x-2 mt-3 md:mt-0">
                    {rental.status === 'PENDENTE' && (
                      <>
                        <button
                          onClick={() => handleConfirmRental(rental.id)}
                          className="btn-success text-sm px-3 py-1.5 flex items-center justify-center"
                        >
                          <CheckCircle className="h-4 w-4 mr-1" />
                          Confirmar
                        </button>
                        <button
                          onClick={() => handleDenyRental(rental.id)}
                          className="btn-danger text-sm px-3 py-1.5 flex items-center justify-center"
                        >
                          <XCircle className="h-4 w-4 mr-1" />
                          Recusar
                        </button>
                      </>
                    )}

                    {rental.status === 'CONFIRMADO' && (
                      <button
                        onClick={() => handleCancelRental(rental.id)}
                        className="btn-outline text-terracota-600 border-terracota-600 hover:bg-terracota-50 text-sm px-3 py-1.5 flex items-center justify-center"
                      >
                        <AlertCircle className="h-4 w-4 mr-1" />
                        Cancelar
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminRentalsPage;