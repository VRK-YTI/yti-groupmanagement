package fi.vm.yti.groupmanagement.service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import fi.vm.yti.groupmanagement.service.impl.TokenData;

public interface TokenService {

    String generateToken(final UUID userId,
                         final Map<String, Object> claims,
                         final Date createdAt,
                         final Date invalidatedAt);

    TokenData getTokenData(final String token);
}
