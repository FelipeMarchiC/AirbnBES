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
  ownerId: string;
  startDate: string;
  endDate: string;
  totalPrice: number;
  status: RentalStatus;
  tenantName: string;
  property?: Property;
  createdAt: string;
  updatedAt: string;
};

export type CreateRentalData = {
  propertyId: string;
  startDate: string;
  endDate: string;
};

const rentalService = {
  createRental: async (data: CreateRentalData): Promise<Rental> => {
    try {
      const response = await api.post('/rental', data);
      return response.data;
    } catch (error) {
      console.error('Erro ao criar aluguel:', error);
      throw error;
    }
  },

  confirmRental: async (rentalId: string): Promise<Rental> => {
    try {
      const response = await api.put(`/rental/${rentalId}/owner/confirm`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao confirmar aluguel ${rentalId}:`, error);
      throw error;
    }
  },

  denyRental: async (rentalId: string): Promise<Rental> => {
    try {
      const response = await api.put(`/rental/${rentalId}/owner/deny`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao recusar aluguel ${rentalId}:`, error);
      throw error;
    }
  },

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

  cancelRentalAsTenant: async (rentalId: string, reason: string): Promise<Rental> => {
    try {
      const response = await api.put(`/rental/${rentalId}/tenant/cancel`, { reason });
      return response.data;
    } catch (error) {
      console.error(`Erro ao cancelar aluguel ${rentalId} como inquilino:`, error);
      throw error;
    }
  },

  deleteRental: async (rentalId: string): Promise<void> => {
    try {
      await api.delete(`/rental/${rentalId}`);
    } catch (error) {
      console.error(`Erro ao excluir aluguel ${rentalId}:`, error);
      throw error;
    }
  },

  /**
  
   * @param ownerId Filtra os aluguéis por ID do proprietário.
   * @returns 
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

  getRentalsByProperty: async (propertyId: string): Promise<Rental[]> => {
    try {
      const response = await api.get(`/rental/properties/${propertyId}`);
      return response.data;
    } catch (error) {
      console.error(`Erro ao buscar aluguéis para propriedade ${propertyId}:`, error);
      throw error;
    }
  },

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