package br.ifsp.vvts.rental.requests;

import br.ifsp.application.rental.update.tenant.ITenantUpdateRentalService;

import java.util.UUID;

public record PutRequest() {
    public ITenantUpdateRentalService.RequestModel toTenantUpdateRequestModel(UUID tenantId, UUID rentalId) {
        return new ITenantUpdateRentalService.RequestModel(tenantId, rentalId);
    }
}
