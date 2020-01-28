package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

public class PrivateApiTempUserListItem {

    private final UUID id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String tokenRole;
    private final String containerUri;

    public PrivateApiTempUserListItem(final UUID id,
                                      final String email,
                                      final String firstName,
                                      final String lastName,
                                      final String tokenRole,
                                      final String containerUri) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tokenRole = tokenRole;
        this.containerUri = containerUri;
    }

    public UUID getId() {
        return id;
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

    public String getTokenRole() {
        return tokenRole;
    }

    public String getContainerUri() {
        return containerUri;
    }
}
