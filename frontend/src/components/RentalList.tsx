import React, { useState } from 'react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { Rental } from '../services/rentalService';
import RentalStatusBadge from './RentalStatusBadge';

interface RentalListProps {
  rentals: Rental[];
  title?: string;
  emptyMessage?: string;
  showActions?: boolean;
  onConfirm?: (rentalId: string) => void;
  onDeny?: (rentalId: string) => void;
  onCancel?: (rentalId: string) => void;
  isLoading?: boolean;
}

const RentalList: React.FC<RentalListProps> = ({
  rentals,
  title = 'Aluguéis',
  emptyMessage = 'Nenhum aluguel encontrado.',
  showActions = false,
  onConfirm,
  onDeny,
  onCancel,
  isLoading = false,
}) => {
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [rentalToCancelId, setRentalToCancelId] = useState<string | null>(null);

  const formatCurrency = (value: number) => {
    return value.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    });
  };

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'dd/MM/yyyy', { locale: ptBR });
  };

  const handleCancelClick = (rentalId: string) => {
    setRentalToCancelId(rentalId);
    setShowCancelConfirm(true);
  };

  const handleConfirmCancel = () => {
    if (rentalToCancelId && onCancel) {
      onCancel(rentalToCancelId);
    }
    setShowCancelConfirm(false);
    setRentalToCancelId(null);
  };

  const handleCloseCancelConfirm = () => {
    setShowCancelConfirm(false);
    setRentalToCancelId(null);
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl shadow-md p-6 animate-pulse">
        <div className="h-7 bg-gray-200 rounded w-1/4 mb-6"></div>
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="border border-gray-200 rounded-lg p-4">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                <div className="space-y-3 w-full">
                  <div className="h-5 bg-gray-200 rounded w-3/4"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/3"></div>
                </div>
                <div className="mt-3 md:mt-0">
                  <div className="h-8 bg-gray-200 rounded w-24"></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (rentals.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4">{title}</h2>
        <div className="text-center py-8">
          <p className="text-gray-500">{emptyMessage}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-md p-6">
      <h2 className="text-xl font-semibold mb-4">{title}</h2>
      <div className="space-y-4">
        {rentals.map((rental) => (
          <div
            key={rental.id}
            className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
          >
            <div className="flex flex-col md:flex-row md:items-center md:justify-between">
              <div>
                <h3 className="font-semibold text-lg mb-2">
                  {rental.propertyName || `Propriedade #${rental.propertyId}`}
                </h3>
                <p className="text-gray-600 mb-1">
                  <span className="font-medium">Inquilino:</span> {rental.tenantName}
                </p>
                <p className="text-gray-600 mb-1">
                  <span className="font-medium">Período:</span> {formatDate(rental.startDate)} a{' '}
                  {formatDate(rental.endDate)}
                </p>
                <p className="text-gray-600 mb-2">
                  <span className="font-medium">Valor total:</span> {formatCurrency(rental.price)}
                </p>
                <div className="mb-3">
                  <RentalStatusBadge status={rental.status} />
                </div>
              </div>

              {showActions && rental.status === 'PENDENTE' && (
                <div className="flex flex-col md:flex-row space-y-2 md:space-y-0 md:space-x-2 mt-3 md:mt-0">
                  {onConfirm && (
                    <button
                      onClick={() => onConfirm(rental.id)}
                      className="btn-success text-sm px-3 py-1.5"
                    >
                      Confirmar
                    </button>
                  )}
                  {onDeny && (
                    <button
                      onClick={() => onDeny(rental.id)}
                      className="btn-danger text-sm px-3 py-1.5"
                    >
                      Recusar
                    </button>
                  )}
                </div>
              )}

              {showActions && rental.status === 'CONFIRMADO' && onCancel && (
                <div className="mt-3 md:mt-0">
                  <button
                    onClick={() => handleCancelClick(rental.id)}
                    className="btn-outline text-terracota-600 border-terracota-600 hover:bg-terracota-50 text-sm px-3 py-1.5"
                  >
                    Cancelar
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {showCancelConfirm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-sm">
            <h3 className="text-lg font-semibold mb-4">Confirmar Cancelamento</h3>
            <p className="text-gray-700 mb-6">
              Tem certeza que deseja cancelar este aluguel? Esta ação não pode ser desfeita.
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={handleCloseCancelConfirm}
                className="btn-secondary px-4 py-2 text-sm"
              >
                Não, manter
              </button>
              <button
                onClick={handleConfirmCancel}
                className="btn-danger px-4 py-2 text-sm"
              >
                Sim, cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RentalList;