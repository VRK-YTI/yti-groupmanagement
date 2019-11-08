package fi.vm.yti.groupmanagement.model;

import java.util.HashMap;
import java.util.UUID;

public class UserRequestWithOrganization {

    public final HashMap<String, String> organizationName = new HashMap<>(3);
    public Integer id;
    public String email;
    public UUID organizationId;
    public String role;
    public String firstName;
    public String lastName;
    public boolean sent = false;

    public UserRequestWithOrganization(final Integer id,
                                       final String userEmail,
                                       final UUID organizationId,
                                       final String roleName,
                                       final String firstName,
                                       final String lastName,
                                       final String orgNameFi,
                                       final String orgNameEn,
                                       final String orgNameSv,
                                       final Boolean sent) {

        organizationName.put("fi", orgNameFi);
        organizationName.put("en", orgNameEn);
        organizationName.put("sv", orgNameSv);

        this.id = id;
        this.email = userEmail;
        this.organizationId = organizationId;
        this.role = roleName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sent = sent;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
