package br.ifsp.infrastructure.shared;

import org.springframework.stereotype.Service;
import br.ifsp.domain.services.IUuidGeneratorService;

import java.util.UUID;

@Service
public class UuidGeneratorService implements IUuidGeneratorService {
    public UUID generate() {
        return UUID.randomUUID();
    }
}
