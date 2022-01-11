package fi.vm.yti.groupmanagement.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.groupmanagement.dao.FrontendDao;
import fi.vm.yti.groupmanagement.model.CreateOrganization;
import fi.vm.yti.groupmanagement.model.EmailRole;
import fi.vm.yti.groupmanagement.model.Organization;
import fi.vm.yti.groupmanagement.model.OrganizationListItem;
import fi.vm.yti.groupmanagement.model.OrganizationWithUsers;
import fi.vm.yti.groupmanagement.model.UpdateOrganization;
import fi.vm.yti.groupmanagement.model.UserRequest;
import fi.vm.yti.groupmanagement.model.UserRequestModel;
import fi.vm.yti.groupmanagement.model.UserRequestWithOrganization;
import fi.vm.yti.groupmanagement.model.UserWithRoles;
import fi.vm.yti.groupmanagement.model.UserWithRolesInOrganizations;
import fi.vm.yti.groupmanagement.security.AuthorizationManager;
import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.Role;
import fi.vm.yti.security.YtiUser;
import static fi.vm.yti.security.AuthorizationException.check;

@Service
public class FrontendService {

    private static final Logger logger = LoggerFactory.getLogger(FrontendService.class);
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
    public UUID createOrganization(final CreateOrganization createOrganizationModel) {

        // creating child organization is allowed for organization's admin user
        // main organizations can be created only by super users
        if (createOrganizationModel.parentId != null) {
            check(authorizationManager.canEditOrganization(createOrganizationModel.parentId));
        } else {
            check(authorizationManager.canCreateOrganization());
        }

        final UUID id = UUID.randomUUID();
        final Organization org = new Organization();

        if (createOrganizationModel.parentId != null) {
            OrganizationWithUsers parent = getOrganization(createOrganizationModel.parentId);

            if (parent.organization.parentId != null) {
                throw new IllegalArgumentException("Child organizations cannot have children") ;
            }
        }

        org.id = id;
        org.url = createOrganizationModel.url;
        org.nameEn = createOrganizationModel.nameEn;
        org.nameFi = createOrganizationModel.nameFi;
        org.nameSv = createOrganizationModel.nameSv;
        org.descriptionEn = createOrganizationModel.descriptionEn;
        org.descriptionFi = createOrganizationModel.descriptionFi;
        org.descriptionSv = createOrganizationModel.descriptionSv;
        org.parentId = createOrganizationModel.parentId;
        frontendDao.createOrganization(org);
        for (final String adminUserEmail : createOrganizationModel.adminUserEmails) {
            frontendDao.addUserToRoleInOrganization(adminUserEmail, "ADMIN", id);
        }
        final YtiUser user = userProvider.getUser();
        logger.info("Organization with ID: " + id.toString() + " created by user: " + user.getId());
        return id;
    }

    @Transactional
    public void updateOrganization(final UpdateOrganization updateOrganization) {
        check(authorizationManager.canEditOrganization(updateOrganization.organization.id));
        validateEmailRoles(updateOrganization.userRoles);
        final Organization organization = updateOrganization.organization;
        final UUID id = organization.id;
        frontendDao.updateOrganization(organization);

        // Mark also child organizations as removed
        if (organization.removed) {
            List<OrganizationListItem> childOrganizations = frontendDao.getChildOrganizations(id);

            for (OrganizationListItem orgListItem : childOrganizations) {
                Organization child = frontendDao.getOrganization(orgListItem.getId());
                child.removed = true;
                frontendDao.updateOrganization(child);
            }
        }

        logOrganizationUpdate(id, updateOrganization.userRoles);
        frontendDao.clearUserRoles(id);
        for (final EmailRole userRole : updateOrganization.userRoles) {
            frontendDao.addUserToRoleInOrganization(userRole.userEmail, userRole.role, id);
        }
    }

    private void logOrganizationUpdate(final UUID organizationId,
                                       final List<EmailRole> updatedEmailRoles) {
        final YtiUser user = userProvider.getUser();
        final Set<String> updatedUsers = new HashSet<>();
        updatedEmailRoles.forEach(emailRole -> updatedUsers.add(emailRole.userEmail));
        final List<UserWithRoles> existingOrganizationUsers = frontendDao.getOrganizationUsers(organizationId);
        final Map<String, UUID> existingUsers = new HashMap<>();
        existingOrganizationUsers.forEach(existingOrganizationUser -> existingUsers.put(existingOrganizationUser.user.email, existingOrganizationUser.user.id));
        final Set<String> addedUsers = new HashSet<>();
        final Set<UUID> removedUsers = new HashSet<>();
        existingUsers.keySet().forEach(email -> {
            if (!updatedUsers.contains(email)) {
                removedUsers.add(existingUsers.get(email));
            }
        });
        updatedUsers.forEach(email -> {
            if (!existingUsers.keySet().contains(email)) {
                addedUsers.add(email);
            }
        });
        final StringBuffer buffer = new StringBuffer();
        if (!addedUsers.isEmpty() || !removedUsers.isEmpty()) {
            buffer.append("Organization updated with ID: " + organizationId.toString() + " by user: " + user.getId());
            if (!addedUsers.isEmpty()) {
                buffer.append(" added users: ");
                int i = 0;
                for (final String email : addedUsers) {
                    final UUID userId = frontendDao.getUserIdForEmail(email);
                    if (userId != null) {
                        buffer.append(userId.toString());
                    }
                    i++;
                    if (i < addedUsers.size()) {
                        buffer.append(", ");
                    }
                }
            }
            if (!removedUsers.isEmpty()) {
                buffer.append(" removed users: ");
                int i = 0;
                for (final UUID userId : removedUsers) {
                    buffer.append(userId.toString());
                    i++;
                    if (i < removedUsers.size()) {
                        buffer.append(", ");
                    }
                }
            }
            logger.info(buffer.toString());
        } else {
            logger.info("Organization updated with ID: " + organizationId.toString() + " by user: " + user.getId());
        }
    }

    @Transactional
    public List<OrganizationListItem> getOrganizationListOpt(final Boolean showRemoved) {
        return frontendDao.getMainOrganizationListOpt(showRemoved);
    }

    @Transactional
    public List<OrganizationListItem> getOrganizationList() {
        return getOrganizationList(false);
    }

    @Transactional
    public List<OrganizationListItem> getOrganizationList(boolean includeChildOrganizations) {
        return frontendDao.getOrganizationList(includeChildOrganizations);
    }

    @Transactional
    public OrganizationWithUsers getOrganization(UUID organizationId) {

        check(authorizationManager.canViewOrganization(organizationId));

        final Organization organizationModel = frontendDao.getOrganization(organizationId);
        final List<UserWithRoles> users = frontendDao.getOrganizationUsers(organizationId);
        final List<String> availableRoles = frontendDao.getAvailableRoles();
        final List<OrganizationListItem> childOrganizations = frontendDao.getChildOrganizations(organizationId);

        final OrganizationWithUsers organizationWithUsers = new OrganizationWithUsers();
        organizationWithUsers.organization = organizationModel;
        organizationWithUsers.users = users;
        organizationWithUsers.availableRoles = availableRoles;
        organizationWithUsers.childOrganizations = childOrganizations;

        return organizationWithUsers;
    }

    @Transactional
    public List<UserWithRolesInOrganizations> getUsersForOwnOrganizations() {

        final YtiUser user = this.userProvider.getUser();

        if (user.isSuperuser()) {
            return frontendDao.getUsers();
        } else {
            return frontendDao.getUsersForAdminOrganizations(user.getEmail());
        }
    }

    @Transactional
    public List<UserWithRolesInOrganizations> getUsers() {

        if (authorizationManager.canBrowseUsers()) {
            if (authorizationManager.canShowAuthenticationDetails()) {
                return frontendDao.getUsers();
            } else {
                return frontendDao.getPublicUsers();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public List<UserWithRolesInOrganizations> getTestUsers() {

        if (authorizationManager.canBrowseUsers()) {
            return frontendDao.getPublicUsers();
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public boolean removeUser(final String email) {

        final YtiUser user = userProvider.getUser();
        if (user.isSuperuser() && !user.getEmail().equals(email)) {
            final UUID removedUserId = frontendDao.getUserIdForEmail(email);
            if (removedUserId != null) {
                logger.info("Removing user: " + removedUserId + " by user: " + user.getId());
                return frontendDao.removeUser(email);
            }
        }
        return false;
    }

    @Transactional
    public List<String> getAllRoles() {
        return frontendDao.getAllRoles();
    }

    @Transactional
    public List<UserRequestWithOrganization> getAllUserRequests() {

        final YtiUser user = userProvider.getUser();

        if (user.isSuperuser()) {
            return frontendDao.getAllUserRequestsForOrganizations(null);
        } else {
            final Set<UUID> organizations = user.getOrganizations(Role.ADMIN);

            if (organizations.isEmpty()) {
                return Collections.emptyList();
            } else {
                return frontendDao.getAllUserRequestsForOrganizations(organizations);
            }
        }
    }

    @Transactional
    public void addUserRequest(final UserRequestModel request) {
        this.frontendDao.addUserRequest(request);
    }

    @Transactional
    public void declineUserRequest(final int requestId) {

        final UserRequest userRequest = this.frontendDao.getUserRequest(requestId);
        check(authorizationManager.canEditOrganization(userRequest.organizationId));
        final YtiUser user = userProvider.getUser();
        logger.info("User organization request declibed by user: " + user.getId() + " for user: " + userRequest.userId + " to organization: " + userRequest.organizationId);
        this.frontendDao.deleteUserRequest(requestId);
    }

    @Transactional
    public void acceptUserRequest(final int requestId) {

        final UserRequest userRequest = this.frontendDao.getUserRequest(requestId);
        check(authorizationManager.canEditOrganization(userRequest.organizationId));
        final YtiUser user = userProvider.getUser();
        logger.info("User organization request accepted by user: " + user.getId() + " for user: " + userRequest.userId + " to organization: " + userRequest.organizationId);
        this.frontendDao.deleteUserRequest(requestId);
        this.frontendDao.addUserToRoleInOrganization(userRequest.userEmail, userRequest.roleName, userRequest.organizationId);
        final String name = this.frontendDao.getOrganizationNameFI(userRequest.organizationId);
        this.emailSenderService.sendEmailToUserOnAcceptance(userRequest.userEmail, userRequest.userId, name);
    }

    @Transactional
    public String createToken(final UUID userId) {
        return this.frontendDao.createToken(userId);
    }

    @Transactional
    public boolean deleteToken(final UUID userId) {
        return this.frontendDao.deleteToken(userId);
    }

    private void validateEmailRoles(final List<EmailRole> emailRoles) {
        boolean hasAdmin = false;
        for (final EmailRole emailRole : emailRoles) {
            try {
                final Role role = Role.valueOf(emailRole.role);
                if (role.equals(Role.ADMIN)) {
                    hasAdmin = true;
                }
            } catch (final Exception e) {
                throw new RuntimeException("Role not valid: " + emailRole.role);
            }
        }
        if (!hasAdmin) {
            throw new RuntimeException("Organization needs to have at least one user with ADMIN role.");
        }
    }

    /** uncomment if you need to trigger email-sending manually
     public void sendEmails(){
     this.emailSenderService.sendEmailsToAdmins();
     }
     */
}
