package fi.vm.yti.groupmanagement.security;

import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.YtiUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static fi.vm.yti.security.Role.ADMIN;

@Service
public class AuthorizationManager {

    private final AuthenticatedUserProvider userProvider;
    private final boolean fakeLoginAllowed;

    @Autowired
    AuthorizationManager(AuthenticatedUserProvider userProvider,
                         @Value("${fake.login.allowed:false}") boolean fakeLoginAllowed) {
        this.userProvider = userProvider;
        this.fakeLoginAllowed = fakeLoginAllowed;
    }

    public boolean canCreateOrganization() {
        return getUser().isSuperuser();
    }

    public boolean canEditOrganization(UUID organizationId) {
        return getUser().isSuperuser() || getUser().isInRole(ADMIN, organizationId);
    }

    public boolean canViewOrganization(UUID organizationId) {
        return this.canEditOrganization(organizationId);
    }

    public boolean canShowAuthenticationDetails() {
        return !getUser().isAnonymous() && !getUser().getEmail().endsWith("@localhost");
    }

    public boolean canBrowseUsers() {
        return this.fakeLoginAllowed || getUser().isSuperuser() || getUser().isInRoleInAnyOrganization(ADMIN);
    }

    public @NotNull YtiUser getUser() {
        return userProvider.getUser();
    }
}
