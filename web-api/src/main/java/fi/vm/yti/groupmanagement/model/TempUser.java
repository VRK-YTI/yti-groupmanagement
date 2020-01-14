package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TempUser {

    public String email;
    public UUID id;
    public String firstName;
    public String lastName;
    public String creationDateTime;
    public String tokenCreatedAt;
    public String tokenInvalidationAt;
    public String containerUri;
    public String tokenRole;
}
