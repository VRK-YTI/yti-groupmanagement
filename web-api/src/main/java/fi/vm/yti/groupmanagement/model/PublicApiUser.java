package fi.vm.yti.groupmanagement.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PublicApiUser {

    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean superuser;
    private final boolean newlyCreated;
    private final LocalDateTime creationDateTime;
    private final List<PublicApiUserOrganization> organization;
    private final UUID id;
    private final LocalDateTime removalDateTime;
    private final LocalDateTime tokenCreatedAt;
    private final LocalDateTime tokenInvalidationAt;
    private final String containerUri;
    private final String tokenRole;

    public PublicApiUser(final String email,
                         final String firstName,
                         final String lastName,
                         final boolean superuser,
                         final boolean newlyCreated,
                         final LocalDateTime creationDateTime,
                         final UUID id,
                         final LocalDateTime removalDateTime,
                         final LocalDateTime tokenCreatedAt,
                         final LocalDateTime tokenInvalidationAt,
                         final String containerUri,
                         final String tokenRole,
                         final List<PublicApiUserOrganization> organization) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.superuser = superuser;
        this.newlyCreated = newlyCreated;
        this.creationDateTime = creationDateTime;
        this.organization = Collections.unmodifiableList(organization);
        this.id = id;
        this.removalDateTime = removalDateTime;
        this.tokenCreatedAt = tokenCreatedAt;
        this.containerUri = containerUri;
        this.tokenRole = tokenRole;
        this.tokenInvalidationAt = tokenInvalidationAt;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isSuperuser() {
        return superuser;
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public List<PublicApiUserOrganization> getOrganization() {
        return organization;
    }

    public LocalDateTime getRemovalDateTime() {
        return removalDateTime;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public LocalDateTime getTokenInvalidationAt() {
        return tokenInvalidationAt;
    }

    public String getContainerUri() {
        return containerUri;
    }

    public String getTokenRole() {
        return tokenRole;
    }
}
