import { useState, useEffect } from 'react';
import propertyService, { Property, PropertyFilters as PropertyFiltersType } from '../services/propertyService';
import PropertyCard from '../components/PropertyCard';
import PropertyFilters from '../components/PropertyFilters';

const PropertyListPage = () => {
  const [properties, setProperties] = useState<Property[]>([]);
  const [filteredProperties, setFilteredProperties] = useState<Property[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeFilters, setActiveFilters] = useState<PropertyFiltersType>({});

  useEffect(() => {
    const fetchProperties = async () => {
      try {
        setIsLoading(true);
        const data = await propertyService.getAllProperties();
        setProperties(data);
        setFilteredProperties(data);
      } catch (error) {
        console.error('Erro ao buscar propriedades:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProperties();
  }, []);

  const handleFilterChange = async (filters: PropertyFiltersType) => {
    setActiveFilters(filters);
    setIsLoading(true);

    try {
      let results: Property[];

      if (Object.keys(filters).length === 0 || (
          !filters.state && !filters.city &&
          filters.minPrice === undefined && filters.maxPrice === undefined
      )) {
        results = properties;
      } else {
        results = properties.filter(property => {
          let matches = true;

          if (filters.state) {
            matches = matches && property.state.toLowerCase() === filters.state.toLowerCase();
          }

          if (filters.city) {
            matches = matches && property.city.toLowerCase().includes(filters.city.toLowerCase());
          }

          if (filters.minPrice !== undefined) {
            matches = matches && property.dailyRate >= filters.minPrice;
          }

          if (filters.maxPrice !== undefined) {
            matches = matches && property.dailyRate <= filters.maxPrice;
          }
          return matches;
        });
      }

      setFilteredProperties(results);
    } catch (error) {
      console.error('Erro ao filtrar propriedades:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="page-transition">
      <div className="bg-azul-colonial-700 text-white py-12">
        <div className="container-custom">
          <h1 className="text-3xl font-bold mb-4">Encontre a propriedade ideal</h1>
          <p className="text-lg text-gray-100 max-w-2xl">
            Explore nossa seleção de propriedades e encontre a melhor opção para seu próximo aluguel.
            Use os filtros abaixo para refinar sua busca.
          </p>
        </div>
      </div>

      <div className="container-custom py-8">
        <PropertyFilters onFilterChange={handleFilterChange} isLoading={isLoading} />

        <div>
          {Object.keys(activeFilters).length > 0 && (
              <div className="mb-4 flex flex-wrap items-center text-sm text-gray-500">
                <span className="mr-2">Filtros ativos:</span>
                {activeFilters.state && (
                    <span className="bg-azul-colonial-100 text-azul-colonial-700 px-2 py-1 rounded-full mr-2 mb-2">
                    Estado: {activeFilters.state}
                  </span>
                )}
                {activeFilters.city && (
                    <span className="bg-azul-colonial-100 text-azul-colonial-700 px-2 py-1 rounded-full mr-2 mb-2">
                    Cidade: {activeFilters.city}
                  </span>
                )}
                {activeFilters.minPrice !== undefined && (
                    <span className="bg-azul-colonial-100 text-azul-colonial-700 px-2 py-1 rounded-full mr-2 mb-2">
                    Preço mínimo: R$ {activeFilters.minPrice}
                  </span>
                )}
                {activeFilters.maxPrice !== undefined && (
                    <span className="bg-azul-colonial-100 text-azul-colonial-700 px-2 py-1 rounded-full mr-2 mb-2">
                    Preço máximo: R$ {activeFilters.maxPrice}
                  </span>
                )}
              </div>
          )}

          {isLoading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
                    <div key={i} className="bg-white rounded-xl shadow-md overflow-hidden animate-pulse">
                      <div className="h-48 bg-gray-300"></div>
                      <div className="p-4">
                        <div className="h-5 bg-gray-300 rounded w-3/4 mb-2"></div>
                        <div className="h-4 bg-gray-300 rounded w-1/2 mb-4"></div>
                        <div className="flex justify-between mb-4">
                          <div className="h-4 bg-gray-300 rounded w-1/4"></div>
                          <div className="h-4 bg-gray-300 rounded w-1/4"></div>
                          <div className="h-4 bg-gray-300 rounded w-1/4"></div>
                        </div>
                        <div className="h-8 bg-gray-300 rounded w-full mt-4"></div>
                      </div>
                    </div>
                ))}
              </div>
          ) : filteredProperties.length === 0 ? (
              <div className="text-center py-12">
                <h3 className="text-xl font-semibold text-gray-700 mb-2">Nenhuma propriedade encontrada</h3>
                <p className="text-gray-500">
                  Tente ajustar seus filtros ou buscar por outros termos.
                </p>
                <button
                    onClick={() => handleFilterChange({})}
                    className="mt-4 btn-outline"
                >
                  Limpar todos os filtros
                </button>
              </div>
          ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredProperties.map((property) => (
                    <PropertyCard key={property.id} property={property} />
                ))}
              </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PropertyListPage;