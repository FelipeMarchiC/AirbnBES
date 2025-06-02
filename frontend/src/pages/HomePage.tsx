import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Building, Calendar, CheckCircle } from 'lucide-react';
import propertyService, { Property } from '../services/propertyService';
import PropertyCard from '../components/PropertyCard';

const HomePage = () => {
  const [featuredProperties, setFeaturedProperties] = useState<Property[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchProperties = async () => {
      try {
        const properties = await propertyService.getAllProperties();
        setFeaturedProperties(properties.slice(0, 4));
      } catch (error) {
        console.error('Erro ao buscar propriedades em destaque:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProperties();
  }, []);

  return (
    <div className="page-transition">
      <section className="relative h-[70vh] min-h-[500px] bg-gradient-to-r from-azul-colonial-800 to-azul-colonial-600 text-white">
        <div className="absolute inset-0 overflow-hidden">
          <img
            src="https://images.pexels.com/photos/1643384/pexels-photo-1643384.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
            alt="Imagem de fundo"
            className="w-full h-full object-cover opacity-25"
          />
        </div>
        <div className="absolute inset-0 bg-gradient-to-r from-azul-colonial-900/80 to-azul-colonial-800/60"></div>

        <div className="container-custom relative h-full flex flex-col justify-center">
          <div className="max-w-2xl animate-slide-up">
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold mb-4">
              Simplifique o gerenciamento dos seus aluguéis
            </h1>
            <p className="text-xl mb-8 text-gray-100">
              AirbnBES conecta proprietários e inquilinos em uma plataforma completa para gerenciar aluguéis de propriedades com facilidade.
            </p>
            <div className="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4">
              <Link
                to="/propriedades"
                className="btn-primary text-lg py-3 px-6"
              >
                Encontrar propriedades
              </Link>

            </div>
          </div>
        </div>
      </section>

      <section className="py-16 bg-gray-50">
        <div className="container-custom">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-3xl font-bold text-gray-800">Propriedades em destaque</h2>
            <Link
              to="/propriedades"
              className="text-azul-colonial-600 hover:text-azul-colonial-700 font-medium"
            >
              Ver todas
            </Link>
          </div>

          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[1, 2, 3, 4].map((i) => (
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
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {featuredProperties.map((property) => (
                <PropertyCard key={property.id} property={property} />
              ))}
            </div>
          )}
        </div>
      </section>

      <section className="py-16 bg-white">
        <div className="container-custom">
          <h2 className="text-3xl font-bold text-gray-800 mb-12 text-center">Como funciona</h2>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="bg-azul-colonial-100 text-azul-colonial-600 rounded-full h-16 w-16 flex items-center justify-center mx-auto mb-4">
                <Search className="h-8 w-8" />
              </div>
              <h3 className="text-xl font-semibold mb-3">1. Encontre</h3>
              <p className="text-gray-600">
                Busque propriedades por localização ou faixa de preço. Encontre o imóvel perfeito para suas necessidades.
              </p>
            </div>

            <div className="text-center">
              <div className="bg-verde-mata-100 text-verde-mata-600 rounded-full h-16 w-16 flex items-center justify-center mx-auto mb-4">
                <Calendar className="h-8 w-8" />
              </div>
              <h3 className="text-xl font-semibold mb-3">2. Reserve</h3>
              <p className="text-gray-600">
                Solicite a reserva informando as datas desejadas. O proprietário receberá sua solicitação para análise.
              </p>
            </div>

            <div className="text-center">
              <div className="bg-areia-100 text-areia-600 rounded-full h-16 w-16 flex items-center justify-center mx-auto mb-4">
                <CheckCircle className="h-8 w-8" />
              </div>
              <h3 className="text-xl font-semibold mb-3">3. Confirme</h3>
              <p className="text-gray-600">
                Após a confirmação do proprietário, sua reserva está garantida. Gerencie seus aluguéis com facilidade na plataforma.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="py-16 bg-gradient-to-r from-areia-600 to-terracota-600 text-white">
        <div className="container-custom">
          <div className="max-w-3xl mx-auto text-center">
            <h2 className="text-3xl font-bold mb-6">Pronto para começar?</h2>
            <p className="text-xl mb-8">
              Crie sua conta agora e comece a gerenciar seus aluguéis de forma simples e eficiente.
            </p>
            <div className="flex flex-col sm:flex-row justify-center space-y-3 sm:space-y-0 sm:space-x-4">
              <Link
                to="/propriedades"
                className="btn-outline border-white text-terracota hover:bg-white/10 text-lg py-3 px-6"
              >
                Explorar propriedades
              </Link>

            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;