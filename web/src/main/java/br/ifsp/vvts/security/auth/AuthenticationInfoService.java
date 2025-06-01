package br.ifsp.vvts.security.auth;

import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationInfoService {
    public UUID getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            throw new IllegalStateException("Unauthorized user request.");
        var applicationUser = (UserEntity) authentication.getPrincipal();
        return applicationUser.getId();
    }
}