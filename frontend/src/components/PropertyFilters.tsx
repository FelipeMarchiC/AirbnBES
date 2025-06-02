import React, { useState, useEffect } from 'react';
import { PropertyFilters as PropertyFiltersType } from '../services/propertyService';

interface PropertyFiltersProps {
  onFilterChange: (filters: PropertyFiltersType) => void;
  isLoading: boolean;
}

const PropertyFilters: React.FC<PropertyFiltersProps> = ({ onFilterChange, isLoading }) => {
  const [state, setState] = useState<string>('');
  const [city, setCity] = useState<string>('');
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');

  // Example list of Brazilian states - you might fetch this from an API
  const brazilianStates = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'BR', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  const handleApplyFilters = () => {
    const filters: PropertyFiltersType = {};
    if (state) filters.state = state;
    if (city) filters.city = city;
    if (minPrice) filters.minPrice = parseFloat(minPrice);
    if (maxPrice) filters.maxPrice = parseFloat(maxPrice);
    onFilterChange(filters);
  };

  const handleClearFilters = () => {
    setState('');
    setCity('');
    setMinPrice('');
    setMaxPrice('');
    onFilterChange({}); // Clear all filters
  };

  return (
    <div className="bg-white rounded-xl shadow-md p-6 mb-8">
      <h2 className="text-2xl font-semibold mb-6 text-azul-colonial-800">Filtrar Propriedades</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div>
          <label htmlFor="state" className="block text-sm font-medium text-gray-700 mb-1">Estado</label>
          <select
            id="state"
            className="form-select w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-azul-colonial-500 focus:border-azul-colonial-500 sm:text-sm"
            value={state}
            onChange={(e) => setState(e.target.value)}
            disabled={isLoading}
          >
            <option value="">Todos os Estados</option>
            {brazilianStates.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-1">Cidade</label>
          <input
            type="text"
            id="city"
            className="form-input w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-azul-colonial-500 focus:border-azul-colonial-500 sm:text-sm"
            placeholder="Ex: São Paulo"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            disabled={isLoading}
          />
        </div>
        <div>
          <label htmlFor="minPrice" className="block text-sm font-medium text-gray-700 mb-1">Preço Mínimo</label>
          <input
            type="number"
            id="minPrice"
            className="form-input w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-azul-colonial-500 focus:border-azul-colonial-500 sm:text-sm"
            placeholder="R$ 0"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            disabled={isLoading}
          />
        </div>
        <div>
          <label htmlFor="maxPrice" className="block text-sm font-medium text-gray-700 mb-1">Preço Máximo</label>
          <input
            type="number"
            id="maxPrice"
            className="form-input w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-azul-colonial-500 focus:border-azul-colonial-500 sm:text-sm"
            placeholder="R$ 10000"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            disabled={isLoading}
          />
        </div>
      </div>
      <div className="flex justify-end space-x-3">
        <button
          onClick={handleClearFilters}
          className="btn-outline"
          disabled={isLoading}
        >
          Limpar Filtros
        </button>
        <button
          onClick={handleApplyFilters}
          className="btn-primary"
          disabled={isLoading}
        >
          Aplicar Filtros
        </button>
      </div>
    </div>
  );
};

export default PropertyFilters;