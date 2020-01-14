package fi.vm.yti.groupmanagement.dao;

import java.time.LocalDateTime;
import java.util.UUID;

public final class TempUserRow {

    TempUserDetails tempUser = new TempUserDetails();

    public TempUserRow(UUID userId) {
        this.tempUser.id = userId;
    }

    public TempUserRow(final UUID userId,
                       final String firstName,
                       final String lastName,
                       final String email,
                       final String tokenRole,
                       final String containerUri,
                       final LocalDateTime creationDateTime,
                       final LocalDateTime removalDateTime) {
        this.tempUser.id = userId;
        this.tempUser.firstName = firstName;
        this.tempUser.lastName = lastName;
        this.tempUser.email = email;
        this.tempUser.creationDateTime = creationDateTime;
        this.tempUser.removalDateTime = removalDateTime;
        this.tempUser.tokenRole = tokenRole;
        this.tempUser.containerUri = containerUri;
    }

    public TempUserRow(final UUID userId,
                       final String firstName,
                       final String lastName,
                       final String email,
                       final String tokenRole,
                       final String containerUri,
                       final LocalDateTime creationDateTime,
                       final LocalDateTime removalDateTime,
                       final LocalDateTime tokenCreatedAt,
                       final LocalDateTime tokenInvalidationAt) {
        this.tempUser.id = userId;
        this.tempUser.firstName = firstName;
        this.tempUser.lastName = lastName;
        this.tempUser.email = email;
        this.tempUser.creationDateTime = creationDateTime;
        this.tempUser.removalDateTime = removalDateTime;
        this.tempUser.tokenCreatedAt = tokenCreatedAt;
        this.tempUser.tokenInvalidationAt = tokenInvalidationAt;
        this.tempUser.tokenRole = tokenRole;
        this.tempUser.containerUri = containerUri;
    }

    public static final class TempUserDetails {

        UUID id;
        String firstName;
        String lastName;
        String email;
        LocalDateTime creationDateTime;
        LocalDateTime removalDateTime;
        LocalDateTime tokenCreatedAt;
        LocalDateTime tokenInvalidationAt;
        String tokenRole;
        String containerUri;
    }
}
