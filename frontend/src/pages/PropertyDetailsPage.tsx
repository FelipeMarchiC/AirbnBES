import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { format, addDays, differenceInDays } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { 
  MapPin, 
  Calendar, 
  DollarSign,
  Home,
  ChevronRight,
  ArrowLeft
} from 'lucide-react';
import toast from 'react-hot-toast';
import propertyService, { Property } from '../services/propertyService';
import rentalService, { Rental } from '../services/rentalService';
import { useAuth } from '../contexts/AuthContext';
import RentalStatusBadge from '../components/RentalStatusBadge';

const PropertyDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [property, setProperty] = useState<Property | null>(null);
  const [propertyRentals, setPropertyRentals] = useState<Rental[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [totalPrice, setTotalPrice] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const formatCurrency = (value?: number) => {
    if (typeof value !== 'number') return 'R$ 0,00';
    return value.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return format(date, 'dd/MM/yyyy', { locale: ptBR });
  };

  useEffect(() => {
    const fetchPropertyDetails = async () => {
      if (!id) return;

      try {
        setIsLoading(true);
        const propertyData = await propertyService.getPropertyById(id);

        if (propertyData) {
          setProperty(propertyData);
          const rentalsData = await rentalService.getRentalsByProperty(id);
          setPropertyRentals(rentalsData);
        }
      } catch (error) {
        console.error('Erro ao buscar detalhes da propriedade:', error);
        toast.error('Erro ao carregar detalhes da propriedade');
      } finally {
        setIsLoading(false);
      }
    };

    fetchPropertyDetails();
  }, [id]);

  useEffect(() => {
    if (property && startDate && endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);

      if (start && end && start <= end) {
        const days = differenceInDays(end, start) + 1;
        setTotalPrice(property.dailyRate * days);
      } else {
        setTotalPrice(0);
      }
    } else {
      setTotalPrice(0);
    }
  }, [property, startDate, endDate]);

  const isDateReserved = (date: Date) => {
    return propertyRentals.some(rental => {
      if (rental.status !== 'CONFIRMADO') return false;
      const rentalStart = new Date(rental.startDate);
      const rentalEnd = new Date(rental.endDate);
      return date >= rentalStart && date <= rentalEnd;
    });
  };

  const validateDateRange = () => {
    if (!startDate || !endDate) return false;

    const start = new Date(startDate);
    const end = new Date(endDate);
    if (start > end) return false;

    let currentDate = new Date(start);
    while (currentDate <= end) {
      if (isDateReserved(currentDate)) {
        return false;
      }
      currentDate = addDays(currentDate, 1);
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!isAuthenticated) {
      toast.error('Você precisa estar logado para alugar uma propriedade');
      navigate('/login');
      return;
    }

    if (!validateDateRange()) {
      toast.error('Verifique as datas selecionadas. Pode haver conflito com reservas existentes.');
      return;
    }

    if (!property || !startDate || !endDate) {
      toast.error('Preencha todas as informações');
      return;
    }

    setIsSubmitting(true);

    try {
      await rentalService.createRental({
        propertyId: property.id,
        startDate,
        endDate,
      });

      toast.success('Solicitação de aluguel enviada com sucesso!');
      setStartDate('');
      setEndDate('');
      navigate('/meus-alugueis');
    } catch (error) {
      console.error('Erro ao solicitar aluguel:', error);
      toast.error('Erro ao enviar solicitação. Tente novamente.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="container-custom py-12">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
          <div className="h-64 bg-gray-200 rounded-xl mb-8"></div>
        </div>
      </div>
    );
  }

  if (!property) {
    return (
      <div className="container-custom py-12 text-center">
        <h2 className="text-2xl font-semibold mb-4">Propriedade não encontrada</h2>
        <p className="text-gray-600 mb-6">
          A propriedade que você está procurando não existe ou foi removida.
        </p>
        <Link to="/propriedades" className="btn-primary">
          Ver outras propriedades
        </Link>
      </div>
    );
  }

  return (
    <div className="page-transition">
      <div className="bg-gray-100 border-b border-gray-200">
        <div className="container-custom py-4">
          <div className="flex items-center text-sm">
            <Link to="/" className="text-gray-500 hover:text-azul-colonial-600 transition-colors">
              <Home className="h-4 w-4" />
            </Link>
            <ChevronRight className="h-4 w-4 mx-2 text-gray-400" />
            <Link to="/propriedades" className="text-gray-500 hover:text-azul-colonial-600 transition-colors">
              Propriedades
            </Link>
            <ChevronRight className="h-4 w-4 mx-2 text-gray-400" />
            <span className="text-gray-700 font-medium truncate">
              {property.name}
            </span>
          </div>
        </div>
      </div>

      <div className="container-custom py-8">
        <Link 
          to="/propriedades" 
          className="inline-flex items-center text-azul-colonial-600 hover:text-azul-colonial-700 mb-6"
        >
          <ArrowLeft className="h-4 w-4 mr-1" />
          Voltar para propriedades
        </Link>

        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            {property.name}
          </h1>
          <div className="flex items-center text-gray-600">
            <MapPin className="h-5 w-5 mr-1 text-azul-colonial-500" />
            <span>{property.location}</span>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">Descrição</h2>
          <p className="text-gray-700">
            {property.description}
          </p>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">Disponibilidade</h2>
          {propertyRentals.filter(r => r.status === 'CONFIRMADO').length > 0 ? (
            <div>
              <p className="text-gray-700 mb-4">
                Esta propriedade já está reservada nos seguintes períodos:
              </p>
              <div className="space-y-2">
                {propertyRentals
                  .filter(r => r.status === 'CONFIRMADO')
                  .map(rental => (
                    <div 
                      key={rental.id}
                      className="bg-gray-50 p-3 rounded-lg flex items-center justify-between"
                    >
                      <div className="flex items-center">
                        <Calendar className="h-5 w-5 mr-2 text-azul-colonial-500" />
                        <span>
                          {formatDate(rental.startDate)} a {formatDate(rental.endDate)}
                        </span>
                      </div>
                      <RentalStatusBadge status={rental.status} size="sm" />
                    </div>
                  ))}
              </div>
            </div>
          ) : (
            <p className="text-gray-700">
              Esta propriedade está completamente disponível para reservas.
            </p>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-md p-6 sticky top-24 max-w-sm">
          <div className="flex items-center mb-4">
            <DollarSign className="h-6 w-6 text-azul-colonial-600 mr-2" />
            <h2 className="text-xl font-semibold">
              {formatCurrency(property.dailyRate)} / noite
            </h2>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="startDate" className="block text-sm font-medium text-gray-700">
                Data de início
              </label>
              <input
                id="startDate"
                type="date"
                className="input-primary"
                value={startDate}
                min={format(new Date(), 'yyyy-MM-dd')}
                onChange={e => setStartDate(e.target.value)}
                required
              />
            </div>

            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-gray-700">
                Data de término
              </label>
              <input
                id="endDate"
                type="date"
                className="input-primary"
                value={endDate}
                min={startDate || format(new Date(), 'yyyy-MM-dd')}
                onChange={e => setEndDate(e.target.value)}
                required
              />
            </div>

            <div className="text-lg font-semibold text-gray-900">
              Total: {formatCurrency(totalPrice)}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary w-full"
            >
              {isSubmitting ? 'Enviando...' : 'Solicitar reserva'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PropertyDetailsPage;
