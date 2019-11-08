package fi.vm.yti.groupmanagement.model;

import java.util.List;
import java.util.UUID;

public final class PublicApiUserRequest {

    private final UUID organizationId;
    private final List<String> role;

    public PublicApiUserRequest(final UUID organizationId,
                                final List<String> role) {
        this.organizationId = organizationId;
        this.role = role;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public List<String> getRole() {
        return role;
    }
}
