import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin } from 'lucide-react';
import { Property } from '../services/propertyService';

interface PropertyCardProps {
  property: Property;
}

const PropertyCard: React.FC<PropertyCardProps> = ({ property }) => {
  
  const formatCurrency = (value?: number) => {
    if (typeof value !== 'number') return ''; 
    return value.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  };

  return (
    <div className="card group h-full flex flex-col shadow-md rounded-xl overflow-hidden">
      <Link to={`/propriedades/${property.id}`} className="relative block overflow-hidden">
        <div className="aspect-w-16 aspect-h-9 bg-gray-300 flex items-center justify-center">
          <img 
            src="https://cdn1.iconfinder.com/data/icons/basic-ui-elements-coloricon/21/39-512.png" 
            alt="Imagem padrão - imóvel não disponível"
            className="object-contain w-full h-full p-6"
          />
        </div>
        <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent text-white p-4">
          <p className="font-bold text-lg truncate">{property.name}</p>
        </div>
      </Link>

      <div className="p-4 flex-grow flex flex-col">
        <div className="flex items-center text-sm text-gray-500 mb-3">
          <MapPin className="h-4 w-4 mr-1 flex-shrink-0 text-blue-500" />
          <span className="truncate">{property.description || 'Localização não informada'}</span>
        </div>

        <div className="mt-auto pt-4 border-t border-gray-100 flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-500">Diária</p>
            <p className="font-bold text-lg text-blue-600">
              {formatCurrency(property.dailyRate)}
            </p>
          </div>
          <Link
            to={`/propriedades/${property.id}`}
            className="btn-primary"
          >
            Ver detalhes
          </Link>
        </div>
      </div>
    </div>
  );
};

export default PropertyCard;
