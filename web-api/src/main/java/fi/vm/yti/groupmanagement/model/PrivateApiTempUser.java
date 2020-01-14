package fi.vm.yti.groupmanagement.model;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PrivateApiTempUser {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String containerUri;
    private final String tokenRole;
    private final LocalDateTime creationDateTime;
    private final LocalDateTime removalDateTime;
    private LocalDateTime tokenCreatedAt;
    private LocalDateTime tokenInvalidationAt;

    public PrivateApiTempUser(final UUID id,
                              final String email,
                              final String firstName,
                              final String lastName,
                              final String containerUri,
                              final String tokenRole,
                              final LocalDateTime creationDateTime,
                              final LocalDateTime removalDateTime,
                              final LocalDateTime tokenCreatedAt,
                              final LocalDateTime tokenInvalidationAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.containerUri = containerUri;
        this.tokenRole = tokenRole;
        this.creationDateTime = creationDateTime;
        this.removalDateTime = removalDateTime;
        this.tokenCreatedAt = tokenCreatedAt;
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

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public LocalDateTime getRemovalDateTime() {
        return removalDateTime;
    }

    public UUID getId() {
        return id;
    }

    public String getContainerUri() {
        return containerUri;
    }

    public String getTokenRole() {
        return tokenRole;
    }

    public LocalDateTime getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public void setTokenCreatedAt(final LocalDateTime tokenCreatedAt) {
        this.tokenCreatedAt = tokenCreatedAt;
    }

    public LocalDateTime getTokenInvalidationAt() {
        return tokenInvalidationAt;
    }

    public void setTokenInvalidationAt(final LocalDateTime tokenInvalidationAt) {
        this.tokenInvalidationAt = tokenInvalidationAt;
    }
}
