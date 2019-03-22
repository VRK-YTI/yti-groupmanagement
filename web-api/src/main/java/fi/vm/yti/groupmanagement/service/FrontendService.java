package fi.vm.yti.groupmanagement.service;

import fi.vm.yti.groupmanagement.dao.FrontendDao;
import fi.vm.yti.groupmanagement.model.*;
import fi.vm.yti.groupmanagement.security.AuthorizationManager;
import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.Role;
import fi.vm.yti.security.YtiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fi.vm.yti.security.AuthorizationException.check;

@Service
public class FrontendService {

    private final FrontendDao frontendDao;
    private final AuthorizationManager authorizationManager;
    private final AuthenticatedUserProvider userProvider;
    private final EmailSenderService emailSenderService;

    @Autowired
    public FrontendService(FrontendDao frontendDao,
                           AuthorizationManager authorizationManager,
                           AuthenticatedUserProvider userProvider,
                           EmailSenderService emailSenderService) {
        this.frontendDao = frontendDao;
        this.authorizationManager = authorizationManager;
        this.userProvider = userProvider;
        this.emailSenderService = emailSenderService;
    }

    @Transactional
    public UUID createOrganization(CreateOrganization createOrganizationModel) {

        check(authorizationManager.canCreateOrganization());

        UUID id = UUID.randomUUID();

        Organization org = new Organization();
        org.id = id;
        org.url = createOrganizationModel.url;
        org.nameEn = createOrganizationModel.nameEn;
        org.nameFi = createOrganizationModel.nameFi;
        org.nameSv = createOrganizationModel.nameSv;
        org.descriptionEn = createOrganizationModel.descriptionEn;
        org.descriptionFi = createOrganizationModel.descriptionFi;
        org.descriptionSv = createOrganizationModel.descriptionSv;

        frontendDao.createOrganization(org);

        for (String adminUserEmail : createOrganizationModel.adminUserEmails) {
            frontendDao.addUserToRoleInOrganization(adminUserEmail, "ADMIN", id);
        }

        return id;
    }

    @Transactional
    public void updateOrganization(UpdateOrganization updateOrganization) {

        check(authorizationManager.canEditOrganization(updateOrganization.organization.id));

        Organization organization = updateOrganization.organization;
        UUID id = organization.id;
        frontendDao.updateOrganization(organization);
        frontendDao.clearUserRoles(id);

        for (EmailRole userRole : updateOrganization.userRoles) {
            frontendDao.addUserToRoleInOrganization(userRole.userEmail, userRole.role, id);
        }
    }

    @Transactional
    public List<OrganizationListItem> getOrganizationListOpt(Boolean showRemoved) {
        return frontendDao.getOrganizationListOpt(showRemoved);
    }

    @Transactional
    public List<OrganizationListItem> getOrganizationList() {
        return frontendDao.getOrganizationList();
    }

    @Transactional
    public OrganizationWithUsers getOrganization(UUID organizationId) {

        check(authorizationManager.canViewOrganization(organizationId));

        Organization organizationModel = frontendDao.getOrganization(organizationId);
        List<UserWithRoles> users = frontendDao.getOrganizationUsers(organizationId);
        List<String> availableRoles = frontendDao.getAvailableRoles();

        OrganizationWithUsers organizationWithUsers = new OrganizationWithUsers();
        organizationWithUsers.organization = organizationModel;
        organizationWithUsers.users = users;
        organizationWithUsers.availableRoles = availableRoles;

        return organizationWithUsers;
    }

    @Transactional
    public List<UserWithRolesInOrganizations> getUsersForOwnOrganizations() {

        YtiUser user = this.userProvider.getUser();

        if (user.isSuperuser()) {
            return frontendDao.getPublicUsers();
        } else {
            return frontendDao.getUsersForAdminOrganizations(user.getEmail());
        }
    }

    @Transactional
    public List<UserWithRolesInOrganizations> getUsers() {
        if (authorizationManager.canBrowseUsers()) {
            if(authorizationManager.canShowAuthenticationDetails()) {
                return frontendDao.getUsers();
            } else {
                return frontendDao.getPublicUsers();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public void removeUser(String email) {
        frontendDao.removeUser(email);
    }

    @Transactional
    public List<String> getAllRoles() {
        return frontendDao.getAllRoles();
    }

    @Transactional
    public List<UserRequestWithOrganization> getAllUserRequests() {

        YtiUser user = userProvider.getUser();

        if (user.isSuperuser()) {
            return frontendDao.getAllUserRequestsForOrganizations(null);
        } else {
            Set<UUID> organizations = user.getOrganizations(Role.ADMIN);

            if (organizations.isEmpty()) {
                return Collections.emptyList();
            } else {
                return frontendDao.getAllUserRequestsForOrganizations(organizations);
            }
        }
    }

    @Transactional
    public void addUserRequest(UserRequestModel request) {
        this.frontendDao.addUserRequest(request);
    }

    @Transactional
    public void declineUserRequest(int requestId) {

        UserRequest userRequest = this.frontendDao.getUserRequest(requestId);
        check(authorizationManager.canEditOrganization(userRequest.organizationId));

        this.frontendDao.deleteUserRequest(requestId);
    }

    @Transactional
    public void acceptUserRequest(int requestId) {

        UserRequest userRequest = this.frontendDao.getUserRequest(requestId);
        check(authorizationManager.canEditOrganization(userRequest.organizationId));

        this.frontendDao.deleteUserRequest(requestId);
        this.frontendDao.addUserToRoleInOrganization(userRequest.userEmail, userRequest.roleName, userRequest.organizationId);
        String name = this.frontendDao.getOrganizationNameFI(userRequest.organizationId);
        this.emailSenderService.sendEmailToUserOnAcceptance(userRequest.userEmail, name);
    }
}
