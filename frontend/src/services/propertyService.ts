import api from './api';

export type Property = {
  dailyRate: number;
  id: string;
  name: string;
  description: string;
  location: string;
  price: number;
  maxGuests: number;
  ownerId: string;
  ownerName: string;
};

export type PropertyFilters = {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
};

const propertyService = {
  getAllProperties: async (): Promise<Property[]> => {
    try {
      const response = await api.get('/property');
      return response.data;
    } catch (error) {
      console.error('Erro ao buscar propriedades:', error);
      throw error;
    }
  },

  getPropertiesByLocation: async (location: string): Promise<Property[]> => {
    try {
      const response = await api.get(`property/location?location=${encodeURIComponent(location)}`);
      return response.data;
    } catch (error) {
      console.error('Erro ao buscar propriedades por localização:', error);
      throw error;
    }
  },

    getPropertyById: async (propertyId: string): Promise<Property> => {
    try {
      const response = await api.get(`/property/${propertyId}`);
      return response.data;
    } catch (error) {
      console.error('Erro ao buscar propriedade por ID:', error);
      throw error;
    }
  },

  getPropertiesByPriceRange: async (min: number, max: number): Promise<Property[]> => {
    try {
      const response = await api.get(`/property/price-range?min=${min}&max=${max}`);
      return response.data;
    } catch (error) {
      console.error('Erro ao buscar propriedades por faixa de preço:', error);
      throw error;
    }
  },

  filterProperties: async (filters: PropertyFilters): Promise<Property[]> => {
    try {
      if (filters.location && (filters.minPrice !== undefined && filters.maxPrice !== undefined)) {
        const locationFiltered = await propertyService.getPropertiesByLocation(filters.location);
        return locationFiltered.filter(p =>
          p.price >= (filters.minPrice || 0) && p.price <= (filters.maxPrice || Infinity)
        );
      } else if (filters.location) {
        return await propertyService.getPropertiesByLocation(filters.location);
      } else if (filters.minPrice !== undefined && filters.maxPrice !== undefined) {
        return await propertyService.getPropertiesByPriceRange(filters.minPrice, filters.maxPrice);
      }

      return await propertyService.getAllProperties();
    } catch (error) {
      console.error('Erro ao filtrar propriedades:', error);
      throw error;
    }
  }
};

export default propertyService;