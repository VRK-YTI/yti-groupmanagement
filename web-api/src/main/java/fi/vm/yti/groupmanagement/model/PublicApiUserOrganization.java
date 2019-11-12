package fi.vm.yti.groupmanagement.model;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PublicApiUserOrganization {

    private final UUID uuid;
    private final List<String> role;

    public PublicApiUserOrganization(final UUID uuid,
                                     final List<String> role) {
        this.uuid = uuid;
        this.role = Collections.unmodifiableList(role);
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<String> getRole() {
        return role;
    }
}
