package fi.vm.yti.groupmanagement.model;

import java.util.List;

public class OrganizationWithUsers {

    public Organization organization;
    public List<UserWithRoles> users;
    public List<String> availableRoles;
    public List<OrganizationListItem> childOrganizations;
}
