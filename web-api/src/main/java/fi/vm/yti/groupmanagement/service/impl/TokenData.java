package fi.vm.yti.groupmanagement.service.impl;

import java.util.Date;
import java.util.UUID;

public class TokenData {

    private UUID userId;
    private Date tokenCreatedAt;
    private Date tokenInvalidationAt;
    private String type;

    public TokenData(final UUID userId,
                     final Date tokenCreatedAt,
                     final Date tokenInvalidationAt,
                     final String type) {
        this.userId = userId;
        this.tokenCreatedAt = tokenCreatedAt;
        this.tokenInvalidationAt = tokenInvalidationAt;
        this.type = type;
    }

    public UUID getUserId() {
        return userId;
    }

    public Date getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public Date getTokenInvalidationAt() {
        return tokenInvalidationAt;
    }

    public String getType() {
        return type;
    }
}
