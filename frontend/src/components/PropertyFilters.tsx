import React, { useState } from 'react';
import { Search, MapPin, DollarSign, Filter } from 'lucide-react';
import { PropertyFilters as FiltersType } from '../services/propertyService';

interface PropertyFiltersProps {
  onFilterChange: (filters: FiltersType) => void;
  isLoading?: boolean;
}

const PropertyFilters: React.FC<PropertyFiltersProps> = ({ 
  onFilterChange,
  isLoading = false
}) => {
  const [location, setLocation] = useState('');
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const filters: FiltersType = {
      location: location || undefined,
      minPrice,
      maxPrice
    };
    
    onFilterChange(filters);
  };
  
  const clearFilters = () => {
    setLocation('');
    setMinPrice(undefined);
    setMaxPrice(undefined);
    
    onFilterChange({});
  };
  
  return (
    <div className="bg-white rounded-xl shadow-md p-4 mb-6 animate-slide-up">
      {/* Versão para desktop */}
      <form onSubmit={handleSubmit} className="hidden md:block">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Localização */}
          <div className="form-group mb-0">
            <label htmlFor="location" className="form-label">
              Localização
            </label>
            <div className="relative">
              <MapPin className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
              <input
                type="text"
                id="location"
                placeholder="Cidade, estado ou região"
                className="input pl-10"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              />
            </div>
          </div>
          
          {/* Preço mínimo */}
          <div className="form-group mb-0">
            <label htmlFor="minPrice" className="form-label">
              Preço mínimo (R$)
            </label>
            <div className="relative">
              <DollarSign className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
              <input
                type="number"
                id="minPrice"
                placeholder="Mínimo"
                className="input pl-10"
                min="0"
                value={minPrice || ''}
                onChange={(e) => setMinPrice(e.target.value ? Number(e.target.value) : undefined)}
              />
            </div>
          </div>
          
          {/* Preço máximo */}
          <div className="form-group mb-0">
            <label htmlFor="maxPrice" className="form-label">
              Preço máximo (R$)
            </label>
            <div className="relative">
              <DollarSign className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
              <input
                type="number"
                id="maxPrice"
                placeholder="Máximo"
                className="input pl-10"
                min="0"
                value={maxPrice || ''}
                onChange={(e) => setMaxPrice(e.target.value ? Number(e.target.value) : undefined)}
              />
            </div>
          </div>
        </div>
        
        <div className="flex items-center justify-end mt-4 space-x-3">
          <button
            type="button"
            onClick={clearFilters}
            className="btn-outline"
            disabled={isLoading}
          >
            Limpar filtros
          </button>
          <button
            type="submit"
            className="btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Buscando...' : 'Buscar propriedades'}
          </button>
        </div>
      </form>
      
      {/* Versão para mobile */}
      <div className="md:hidden">
        <div className="flex items-center justify-between mb-3">
          <h3 className="font-semibold text-gray-800">Filtrar propriedades</h3>
          <button
            type="button"
            onClick={() => setIsFilterOpen(!isFilterOpen)}
            className="text-azul-colonial-600 hover:text-azul-colonial-700 focus:outline-none"
          >
            <Filter className="h-5 w-5" />
          </button>
        </div>
        
        {isFilterOpen && (
          <form onSubmit={handleSubmit} className="animate-slide-up">
            {/* Localização */}
            <div className="form-group">
              <label htmlFor="location-mobile" className="form-label">
                Localização
              </label>
              <div className="relative">
                <MapPin className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                <input
                  type="text"
                  id="location-mobile"
                  placeholder="Cidade, estado ou região"
                  className="input pl-10"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                />
              </div>
            </div>
            
            {/* Preço mínimo e máximo */}
            <div className="grid grid-cols-2 gap-3">
              <div className="form-group">
                <label htmlFor="minPrice-mobile" className="form-label">
                  Preço mínimo (R$)
                </label>
                <div className="relative">
                  <DollarSign className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                  <input
                    type="number"
                    id="minPrice-mobile"
                    placeholder="Mínimo"
                    className="input pl-10"
                    min="0"
                    value={minPrice || ''}
                    onChange={(e) => setMinPrice(e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label htmlFor="maxPrice-mobile" className="form-label">
                  Preço máximo (R$)
                </label>
                <div className="relative">
                  <DollarSign className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                  <input
                    type="number"
                    id="maxPrice-mobile"
                    placeholder="Máximo"
                    className="input pl-10"
                    min="0"
                    value={maxPrice || ''}
                    onChange={(e) => setMaxPrice(e.target.value ? Number(e.target.value) : undefined)}
                  />
                </div>
              </div>
            </div>
            
            <div className="flex items-center justify-between mt-4 space-x-3">
              <button
                type="button"
                onClick={clearFilters}
                className="btn-outline flex-1"
                disabled={isLoading}
              >
                Limpar
              </button>
              <button
                type="submit"
                className="btn-primary flex-1"
                disabled={isLoading}
              >
                {isLoading ? 'Buscando...' : 'Buscar'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default PropertyFilters;