import React from 'react';
import { RentalStatus } from '../services/rentalService';
import { CheckCircle, XCircle, AlertCircle, Clock } from 'lucide-react';

interface RentalStatusBadgeProps {
  status: RentalStatus;
  size?: 'sm' | 'md' | 'lg';
}

const RentalStatusBadge: React.FC<RentalStatusBadgeProps> = ({ 
  status, 
  size = 'md' 
}) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'PENDENTE':
        return {
          label: 'Pendente',
          bgColor: 'bg-areia-100',
          textColor: 'text-areia-800',
          icon: <Clock className={`${size === 'sm' ? 'h-3 w-3' : 'h-4 w-4'} mr-1`} />
        };
      case 'CONFIRMADO':
        return {
          label: 'Confirmado',
          bgColor: 'bg-verde-mata-100',
          textColor: 'text-verde-mata-800',
          icon: <CheckCircle className={`${size === 'sm' ? 'h-3 w-3' : 'h-4 w-4'} mr-1`} />
        };
      case 'RECUSADO':
        return {
          label: 'Recusado',
          bgColor: 'bg-terracota-100',
          textColor: 'text-terracota-800',
          icon: <XCircle className={`${size === 'sm' ? 'h-3 w-3' : 'h-4 w-4'} mr-1`} />
        };
      case 'CANCELADO':
        return {
          label: 'Cancelado',
          bgColor: 'bg-gray-100',
          textColor: 'text-gray-800',
          icon: <AlertCircle className={`${size === 'sm' ? 'h-3 w-3' : 'h-4 w-4'} mr-1`} />
        };
      default:
        return {
          label: status,
          bgColor: 'bg-gray-100',
          textColor: 'text-gray-800',
          icon: null
        };
    }
  };
  
  const { label, bgColor, textColor, icon } = getStatusConfig();
  
  return (
    <span className={`inline-flex items-center rounded-full ${bgColor} ${textColor} ${
      size === 'sm' ? 'px-2 py-0.5 text-xs' : 
      size === 'md' ? 'px-2.5 py-0.5 text-sm' : 
      'px-3 py-1 text-base'
    }`}>
      {icon}
      {label}
    </span>
  );
};

export default RentalStatusBadge;