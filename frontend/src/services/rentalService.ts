import api from './api';
import { Property } from './propertyService';

// Tipos
export type RentalStatus = 'PENDENTE' | 'CONFIRMADO' | 'RECUSADO' | 'CANCELADO';

export type Rental = {
  price: number;
  propertyName: string;
  state: string;
  id: string;
  propertyId: string;
  tenantId: string;
  ownerId: string; // Adicionado ownerId
  startDate: string;
  endDate: string;
  totalPrice: number;
  status: RentalStatus;
  tenantName: string;
  property?: Property; // Propriedade aninhada
  createdAt: string;
  updatedAt: string;
};

export type CreateRentalData = {
  propertyId: string;
  startDate: string;
  endDate: string;
};

// Serviço de aluguel
const rentalService = {
  // Criar um novo aluguel (POST /rental)
  createRental: async (data: CreateRentalData): Promise<Rental> => {
    try {
      const response = await api.post('/rental', data);
      return response.data;
    } catch (error) {
      console.error('Erro ao criar aluguel:', error);
      throw error;
    }
  },

  // Confirmar um aluguel (PUT /rental/{id}/owner/confirm)
  confirmRental: async (rentalId: string): Promise<Rental> => {
    try {
      const response = await api.put(`/rental/${rentalId}/owner/confirm`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao confirmar aluguel ${rentalId}:`, error);
      throw error;
    }
  },

  // Recusar um aluguel (PUT /rental/{id}/owner/deny)
  denyRental: async (rentalId: string): Promise<Rental> => {
    try {
      const response = await api.put(`/rental/${rentalId}/owner/deny`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao recusar aluguel ${rentalId}:`, error);
      throw error;
    }
  },

  // Cancelar um aluguel como proprietário (PUT /rental/{id}/owner/cancel)
  cancelRentalAsOwner: async (rentalId: string, cancelDate?: string): Promise<Rental> => {
    try {
      const url = cancelDate
        ? `/rental/${rentalId}/owner/cancel?cancelDate=${cancelDate}`
        : `/rental/${rentalId}/owner/cancel`;
      const response = await api.put(url);
      return response.data;
    } catch (error) {
      console.error(`Erro ao cancelar aluguel ${rentalId} como proprietário:`, error);
      throw error;
    }
  },

  // Cancelar um aluguel como inquilino (PUT /rental/{id}/tenant/cancel)
  cancelRentalAsTenant: async (rentalId: string, reason: string): Promise<Rental> => {
    try {
      const response = await api.put(`/rental/${rentalId}/tenant/cancel`, { reason });
      return response.data;
    } catch (error) {
      console.error(`Erro ao cancelar aluguel ${rentalId} como inquilino:`, error);
      throw error;
    }
  },

  // Excluir um aluguel (DELETE /rental/{id})
  deleteRental: async (rentalId: string): Promise<void> => {
    try {
      await api.delete(`/rental/${rentalId}`);
    } catch (error) {
      console.error(`Erro ao excluir aluguel ${rentalId}:`, error);
      throw error;
    }
  },

  /**
   * Busca todos os aluguéis.
   * @param ownerId Opcional: Filtra os aluguéis por ID do proprietário.
   * @returns Uma Promise que resolve para um array de objetos Rental.
   */
  getAllRentals: async (ownerId?: string): Promise<Rental[]> => {
    try {
      const url = ownerId ? `/rental?ownerId=${ownerId}` : '/rental';
      const response = await api.get(url);
      return response.data;
    } catch (error) {
      console.error('Erro ao buscar aluguéis:', error);
      throw error;
    }
  },

  // Buscar histórico de aluguéis por propriedade (GET /rental/properties/{id})
  getRentalsByProperty: async (propertyId: string): Promise<Rental[]> => {
    try {
      const response = await api.get(`/rental/properties/${propertyId}`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao buscar aluguéis para propriedade ${propertyId}:`, error);
      throw error;
    }
  },

  // Buscar histórico de aluguéis por inquilino (GET /rental/tenants/{id})
  getRentalsByTenant: async (tenantId: string): Promise<Rental[]> => {
    try {
      const response = await api.get(`/rental/tenants/${tenantId}`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao buscar aluguéis para inquilino ${tenantId}:`, error);
      throw error;
    }
  }
};

export default rentalService;