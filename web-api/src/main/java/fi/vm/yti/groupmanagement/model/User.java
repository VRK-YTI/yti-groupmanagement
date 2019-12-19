package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

public class User {

    public String email;
    public UUID id;
    public String firstName;
    public String lastName;
    public String creationDateTime;
    public boolean superuser;
    public String tokenCreatedAt;
    public String tokenInvalidationAt;
}
