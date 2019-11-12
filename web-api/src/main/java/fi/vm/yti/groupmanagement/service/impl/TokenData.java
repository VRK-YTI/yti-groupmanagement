package fi.vm.yti.groupmanagement.service.impl;

import java.util.Date;
import java.util.UUID;

public class TokenData {

    private UUID userId;
    private Date tokenCreatedAt;
    private Date tokenInvalicationAt;

    public TokenData(final UUID userId,
                     final Date tokenCreatedAt,
                     final Date tokenInvalicationAt) {
        this.userId = userId;
        this.tokenCreatedAt = tokenCreatedAt;
        this.tokenInvalicationAt = tokenInvalicationAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public Date getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public Date getTokenInvalidationAt() {
        return tokenInvalicationAt;
    }
}
