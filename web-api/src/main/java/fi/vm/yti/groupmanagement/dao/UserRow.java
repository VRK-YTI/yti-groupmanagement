package fi.vm.yti.groupmanagement.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class UserRow {

    UserDetails user = new UserDetails();
    OrganizationDetails organization = new OrganizationDetails();

    public UserRow(final UUID userId) {

        this.user.id = userId;
    }

    public UserRow(final String email,
                   final String firstName,
                   final String lastName,
                   final boolean superuser,
                   final UUID organizationId,
                   final LocalDateTime creationDateTime,
                   final UUID userId,
                   final LocalDateTime removalDateTime,
                   final List<String> roles) {

        this.user.email = email;
        this.user.firstName = firstName;
        this.user.lastName = lastName;
        this.user.superuser = superuser;
        this.user.id = userId;
        this.organization.id = organizationId;
        this.user.creationDateTime = creationDateTime;
        this.user.removalDateTime = removalDateTime;
        this.organization.roles = roles;
    }

    public UserRow(final String email,
                   final String firstName,
                   final String lastName,
                   final boolean superuser,
                   final UUID organizationId,
                   final LocalDateTime creationDateTime,
                   final UUID userId,
                   final LocalDateTime removalDateTime,
                   final LocalDateTime tokenCreatedAt,
                   final LocalDateTime tokenInvalidationAt,
                   final List<String> roles) {

        this.user.email = email;
        this.user.firstName = firstName;
        this.user.lastName = lastName;
        this.user.superuser = superuser;
        this.user.id = userId;
        this.organization.id = organizationId;
        this.user.creationDateTime = creationDateTime;
        this.user.removalDateTime = removalDateTime;
        this.user.tokenCreatedAt = tokenCreatedAt;
        this.user.tokenInvalidationAt = tokenInvalidationAt;
        this.organization.roles = roles;
    }

    public static final class UserDetails {

        String email;
        String firstName;
        String lastName;
        LocalDateTime creationDateTime;
        boolean superuser;
        LocalDateTime removalDateTime;
        LocalDateTime tokenCreatedAt;
        LocalDateTime tokenInvalidationAt;
        UUID id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserDetails user = (UserDetails) o;

            return email.equals(user.email);
        }

        @Override
        public int hashCode() {
            // Removed user has no emai so use base-class hashCode as fallback
            if(email != null){
                return email.hashCode();
            } else if(id != null){
                // all should have existing id
                return id.hashCode();
            } else {
                // Faalback, user base class hash, but this case should be never reached and
                // it is just to ensure that false data does not crash request
                return id.hashCode();
            }
        }
    }

    public static final class OrganizationDetails {

        UUID id;
        List<String> roles;
    }
}
