package fi.vm.yti.groupmanagement.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserWithRolesInOrganizations {

    public String email;
    public String firstName;
    public String lastName;
    public boolean superuser;
    public UUID userId;
    public LocalDateTime creationDateTime;
    public LocalDateTime removalDateTime;
    public List<OrganizationRoles> organizations;

    public UserWithRolesInOrganizations(final String email,
                                        final String firstName,
                                        final String lastName,
                                        final boolean superuser,
                                        final UUID userId,
                                        final LocalDateTime creationDateTime,
                                        final LocalDateTime removalDateTime,
                                        final List<OrganizationRoles> organizations) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.superuser = superuser;
        this.creationDateTime = creationDateTime;
        this.organizations = organizations;
        this.removalDateTime = removalDateTime;
        this.userId = userId;
    }

    public static class OrganizationRoles {

        public UUID id;
        public List<String> roles;

        public OrganizationRoles(final UUID id,
                                 final List<String> roles) {
            this.id = id;
            this.roles = roles;
        }
    }
}
