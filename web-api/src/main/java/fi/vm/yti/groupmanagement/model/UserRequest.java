package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

public class UserRequest {
    public int id;
    public String userEmail;
    public UUID userId;
    public UUID organizationId;
    public String roleName;
    public boolean sent;
}
