package fi.vm.yti.groupmanagement.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.yti.groupmanagement.config.ApplicationProperties;
import fi.vm.yti.groupmanagement.config.ImpersonateProperties;
import fi.vm.yti.groupmanagement.model.ConfigurationModel;
import fi.vm.yti.groupmanagement.model.CreateOrganization;
import fi.vm.yti.groupmanagement.model.EmailRole;
import fi.vm.yti.groupmanagement.model.OrganizationListItem;
import fi.vm.yti.groupmanagement.model.OrganizationWithUsers;
import fi.vm.yti.groupmanagement.model.TokenModel;
import fi.vm.yti.groupmanagement.model.UpdateOrganization;
import fi.vm.yti.groupmanagement.model.UserRequestModel;
import fi.vm.yti.groupmanagement.model.UserRequestWithOrganization;
import fi.vm.yti.groupmanagement.model.UserWithRolesInOrganizations;
import fi.vm.yti.groupmanagement.security.AuthorizationManager;
import fi.vm.yti.groupmanagement.service.FrontendService;
import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.YtiUser;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/api")
public class FrontendController {

    private static final Logger logger = LoggerFactory.getLogger(FrontendController.class);
    private final FrontendService frontendService;
    private final AuthenticatedUserProvider userProvider;
    private final ApplicationProperties applicationProperties;
    private final ImpersonateProperties impersonateProperties;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public FrontendController(FrontendService frontendService,
                              AuthenticatedUserProvider userProvider,
                              ApplicationProperties applicationProperties,
                              ImpersonateProperties impersonateProperties,
                              final AuthorizationManager authorizationManager) {
        this.frontendService = frontendService;
        this.userProvider = userProvider;
        this.applicationProperties = applicationProperties;
        this.impersonateProperties = impersonateProperties;
        this.authorizationManager = authorizationManager;
    }

    @RequestMapping(value = "/authenticated-user", method = GET, produces = APPLICATION_JSON_VALUE)
    public YtiUser getAuthenticatedUser() {
        logger.info("getAuthenticatedUser requested");
        return userProvider.getUser();
    }

    @RequestMapping(value = "/organizations/{showRemoved}", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<OrganizationListItem> getOrganizationsOpt(@PathVariable("showRemoved") Boolean showRemoved) {
        logger.info("getOrganizations/{showRemoved} requested");
        return this.frontendService.getOrganizationListOpt(showRemoved);
    }

    @RequestMapping(value = "/organizations", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<OrganizationListItem> getOrganizations() {
        logger.info("getOrganizations requested");
        return this.frontendService.getOrganizationList();
    }

    @RequestMapping(value = "/organization/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public OrganizationWithUsers getOrganization(@PathVariable("id") final UUID id) {
        logger.info("getOrganization requested with id: " + id.toString());
        return this.frontendService.getOrganization(id);
    }

    @RequestMapping(value = "/organization", method = POST, produces = APPLICATION_JSON_VALUE)
    public UUID createOrganization(@RequestBody final CreateOrganization createOrganization) {
        logger.info("createOrganization requested with descriptionFI: " + createOrganization.descriptionFi + ", " +
            "nameFI: " + createOrganization.nameFi + " and adminUserEmails: {}", createOrganization.adminUserEmails.toString());
        return this.frontendService.createOrganization(createOrganization);
    }

    @RequestMapping(value = "/organization", method = PUT, produces = APPLICATION_JSON_VALUE)
    public void updateOrganization(@RequestBody final UpdateOrganization updateOrganization) {
        final List<EmailRole> userRoles = updateOrganization.userRoles;
        final List<String> emailRoles = new ArrayList<>();
        for (final EmailRole item : userRoles) {
            emailRoles.add(new String(item.userEmail + ": " + item.role));
        }
        logger.info("updateOrganization requested with id: " + updateOrganization.organization.id + " , nameFI: " + updateOrganization.organization.nameFi + ", userRoles: {}", emailRoles);
        this.frontendService.updateOrganization(updateOrganization);
    }

    @RequestMapping(value = "/roles", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<String> getAllRoles() {
        logger.info("getAllRoles requested");
        return this.frontendService.getAllRoles();
    }

    @RequestMapping(value = "/usersForOwnOrganizations", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserWithRolesInOrganizations> getUsersForOwnOrganizations() {
        logger.info("getUsersForOwnOrganizations requested");
        return this.frontendService.getUsersForOwnOrganizations();
    }

    @RequestMapping(value = "/users", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserWithRolesInOrganizations> getUsers() {
        logger.info("getUsers requested");
        return this.frontendService.getUsers();
    }

    @RequestMapping(value = "/testUsers", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserWithRolesInOrganizations> getTestUsers() {
        logger.info("getTestUsers requested");
        return this.frontendService.getTestUsers();
    }

    @RequestMapping(value = "/removeuser/{email}/", method = POST)
    public Boolean removeUser(@PathVariable("email") final String email) {
        return this.frontendService.removeUser(email);
    }

    @RequestMapping(value = "/requests", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserRequestWithOrganization> getAllUserRequests() {
        logger.info("getAllUserRequests requested");
        return this.frontendService.getAllUserRequests();
    }

    @RequestMapping(value = "/request", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void addUserRequest(@RequestBody final UserRequestModel request) {
        logger.info("addUserRequest requested for email: " + request.email + ", role: " + request.role + " for organization id: " + request.organizationId);
        this.frontendService.addUserRequest(request);
    }

    @RequestMapping(value = "/request/{id}", method = DELETE)
    public void declineUserRequest(@PathVariable("id") final int id) {
        logger.info("declineUserRequest requested with request id: " + id);
        this.frontendService.declineUserRequest(id);
    }

    @RequestMapping(value = "/request/{id}", method = POST)
    public void acceptUserRequest(@PathVariable("id") final int id) {
        logger.info("acceptUserRequest requested with request id: " + id);
        this.frontendService.acceptUserRequest(id);
    }

    @RequestMapping(value = "/config", method = GET, produces = APPLICATION_JSON_VALUE)
    public ConfigurationModel getConfiguration() {
        logger.info("getConfiguration requested");

        final ConfigurationModel model = new ConfigurationModel();
        model.codeListUrl = this.applicationProperties.getCodeListUrl();
        model.dataModelUrl = this.applicationProperties.getDataModelUrl();
        model.terminologyUrl = this.applicationProperties.getTerminologyUrl();
        model.commentsUrl = this.applicationProperties.getCommentsUrl();
        model.dev = this.applicationProperties.getDevMode();
        model.env = this.applicationProperties.getEnv();
        model.fakeLoginAllowed = this.applicationProperties.isFakeLoginAllowed();
        model.impersonateAllowed = impersonateProperties.isAllowed();
        model.messagingEnabled = this.applicationProperties.isMessagingEnabled();

        return model;
    }

    @RequestMapping(value = "/token", method = POST, produces = APPLICATION_JSON_VALUE)
    public TokenModel createToken() {
        final UUID userId = authorizationManager.getUser().getId();
//        final UUID userId = UUID.fromString("4ce70937-6fa4-49af-a229-b5f10328adb8");
        if (userId != null) {
            logger.info("Fetching token for user: " + userId.toString());
            final String token = frontendService.createToken(userId);
            final TokenModel model = new TokenModel();
            model.token = token;
            return model;
        } else {
            throw new RuntimeException("User is not logged in, failing token creation.");
        }
    }

    @RequestMapping(value = "/token", method = DELETE)
    public Boolean deleteToken() {
        final UUID userId = authorizationManager.getUser().getId();
//        final UUID userId = UUID.fromString("4ce70937-6fa4-49af-a229-b5f10328adb8");
        if (userId != null) {
            logger.info("Deleting token for user: " + userId.toString());
            return frontendService.deleteToken(userId);
        } else {
            throw new RuntimeException("User is not logged in, failing token creation.");
        }
    }

    /** uncomment if email-sending loop needs to be triggered manually for local testing purposes.
     @RequestMapping(value = "/email", method = GET, produces = APPLICATION_JSON_VALUE)
     public void email() {
     logger.info("run email sending loop" );
     this.frontendService.sendEmails();
     }
     */
}
