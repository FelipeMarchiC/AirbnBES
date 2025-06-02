import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import rentalService, { Rental } from '../services/rentalService';
import RentalList from '../components/RentalList';
import toast from 'react-hot-toast';

const UserRentalsPage = () => {
  const { user } = useAuth();
  const [rentals, setRentals] = useState<Rental[]>([]);
  const [activeRentals, setActiveRentals] = useState<Rental[]>([]);
  const [pastRentals, setPastRentals] = useState<Rental[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchUserRentals = async () => {
      if (!user) return;

      try {
        setIsLoading(true);
        const data = await rentalService.getRentalsByTenant(user.id);
        setRentals(data);

        const now = new Date();
        const active: Rental[] = [];
        const past: Rental[] = [];

        data.forEach(rental => {
          const mappedStatus = mapApiStatusToRentalStatus(rental.state);
          const endDate = new Date(rental.endDate);

          if (
            mappedStatus === 'PENDENTE' ||
            (mappedStatus === 'CONFIRMADO' && endDate >= now)
          ) {
            active.push({ ...rental, status: mappedStatus });
          } else {
            past.push({ ...rental, status: mappedStatus });
          }
        });

        setActiveRentals(active);
        setPastRentals(past);
      } catch (error) {
        console.error('Erro ao buscar aluguéis do usuário:', error);
        toast.error('Erro ao carregar seus aluguéis');
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserRentals();
  }, [user]);

  const mapApiStatusToRentalStatus = (apiStatus: string): Rental['status'] => {
    switch (apiStatus) {
      case 'PENDING':
        return 'PENDENTE';
      case 'CONFIRMED':
        return 'CONFIRMADO';
      case 'DENIED':
        return 'RECUSADO';
      case 'CANCELLED':
      case 'EXPIRED':
        return 'CANCELADO';
      default:
        return 'PENDENTE';
    }
  };

  const handleCancelRental = async (rentalId: string) => {
    try {
      await rentalService.cancelRentalAsTenant(rentalId, 'Cancelado pelo inquilino');
      toast.success('Aluguel cancelado com sucesso');

      setRentals(prevRentals =>
        prevRentals.map(rental =>
          rental.id === rentalId
            ? { ...rental, status: 'CANCELADO', state: 'CANCELLED' }
            : rental
        )
      );

      const canceledRental = activeRentals.find(r => r.id === rentalId);
      if (canceledRental) {
        setActiveRentals(prev => prev.filter(r => r.id !== rentalId));
        setPastRentals(prev => [...prev, { ...canceledRental, status: 'CANCELADO' }]);
      }
    } catch (error) {
      console.error('Erro ao cancelar aluguel:', error);
      toast.error('Erro ao cancelar aluguel');
    }
  };

  return (
    <div className="page-transition">
      <div className="bg-azul-colonial-700 text-white py-12">
        <div className="container-custom">
          <h1 className="text-3xl font-bold mb-4">Meus Aluguéis</h1>
          <p className="text-lg text-gray-100 max-w-2xl">
            Gerencie todos os seus aluguéis em um só lugar. Veja o status das suas solicitações,
            aluguéis confirmados e histórico de reservas.
          </p>
        </div>
      </div>

      <div className="container-custom py-8">
        <div className="space-y-8">
          <RentalList
            rentals={activeRentals}
            title="Aluguéis Ativos"
            emptyMessage="Você não possui aluguéis ativos no momento."
            showActions={true}
            onCancel={handleCancelRental}
            isLoading={isLoading}
          />

          <RentalList
            rentals={pastRentals}
            title="Histórico de Aluguéis"
            emptyMessage="Você não possui histórico de aluguéis."
            isLoading={isLoading}
          />
        </div>
      </div>
    </div>
  );
};

export default UserRentalsPage;