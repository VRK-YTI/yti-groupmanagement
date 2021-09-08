package fi.vm.yti.groupmanagement.controller;

import java.util.List;
import java.util.UUID;

import fi.vm.yti.groupmanagement.config.VersionInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.yti.groupmanagement.config.ApplicationProperties;
import fi.vm.yti.groupmanagement.config.ImpersonateProperties;
import fi.vm.yti.groupmanagement.model.ConfigurationModel;
import fi.vm.yti.groupmanagement.model.CreateOrganization;
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

    private final FrontendService frontendService;
    private final AuthenticatedUserProvider userProvider;
    private final ApplicationProperties applicationProperties;
    private final ImpersonateProperties impersonateProperties;
    private final AuthorizationManager authorizationManager;
    private final VersionInformation versionInformation;

    @Autowired
    public FrontendController(FrontendService frontendService,
                              AuthenticatedUserProvider userProvider,
                              ApplicationProperties applicationProperties,
                              ImpersonateProperties impersonateProperties,
                              final AuthorizationManager authorizationManager,
                              VersionInformation versionInformation) {
        this.frontendService = frontendService;
        this.userProvider = userProvider;
        this.applicationProperties = applicationProperties;
        this.impersonateProperties = impersonateProperties;
        this.authorizationManager = authorizationManager;
        this.versionInformation = versionInformation;
    }

    @RequestMapping(value = "/authenticated-user", method = GET, produces = APPLICATION_JSON_VALUE)
    public YtiUser getAuthenticatedUser() {
        return userProvider.getUser();
    }

    @RequestMapping(value = "/organizations/{showRemoved}", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<OrganizationListItem> getOrganizationsOpt(@PathVariable("showRemoved") Boolean showRemoved) {
        return this.frontendService.getOrganizationListOpt(showRemoved);
    }

    @RequestMapping(value = "/organizations", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<OrganizationListItem> getOrganizations() {
        return this.frontendService.getOrganizationList();
    }

    @RequestMapping(value = "/organization/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public OrganizationWithUsers getOrganization(@PathVariable("id") final UUID id) {
        return this.frontendService.getOrganization(id);
    }

    @RequestMapping(value = "/organization", method = POST, produces = APPLICATION_JSON_VALUE)
    public UUID createOrganization(@RequestBody final CreateOrganization createOrganization) {
        return this.frontendService.createOrganization(createOrganization);
    }

    @RequestMapping(value = "/organization", method = PUT, produces = APPLICATION_JSON_VALUE)
    public void updateOrganization(@RequestBody final UpdateOrganization updateOrganization) {
        this.frontendService.updateOrganization(updateOrganization);
    }

    @RequestMapping(value = "/roles", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<String> getAllRoles() {
        return this.frontendService.getAllRoles();
    }

    @RequestMapping(value = "/usersForOwnOrganizations", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserWithRolesInOrganizations> getUsersForOwnOrganizations() {
        return this.frontendService.getUsersForOwnOrganizations();
    }

    @RequestMapping(value = "/users", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserWithRolesInOrganizations> getUsers() {
        return this.frontendService.getUsers();
    }

    @RequestMapping(value = "/testUsers", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserWithRolesInOrganizations> getTestUsers() {
        return this.frontendService.getTestUsers();
    }

    @RequestMapping(value = "/removeuser/{email}/", method = POST)
    public Boolean removeUser(@PathVariable("email") final String email) {
        return this.frontendService.removeUser(email);
    }

    @RequestMapping(value = "/requests", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<UserRequestWithOrganization> getAllUserRequests() {
        return this.frontendService.getAllUserRequests();
    }

    @RequestMapping(value = "/request", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void addUserRequest(@RequestBody final UserRequestModel request) {
        this.frontendService.addUserRequest(request);
    }

    @RequestMapping(value = "/request/{id}", method = DELETE)
    public void declineUserRequest(@PathVariable("id") final int id) {
        this.frontendService.declineUserRequest(id);
    }

    @RequestMapping(value = "/request/{id}", method = POST)
    public void acceptUserRequest(@PathVariable("id") final int id) {
        this.frontendService.acceptUserRequest(id);
    }

    @RequestMapping(value = "/config", method = GET, produces = APPLICATION_JSON_VALUE)
    public ConfigurationModel getConfiguration() {
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
        if (userId != null) {
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
        if (userId != null) {
            return frontendService.deleteToken(userId);
        } else {
            throw new RuntimeException("User is not logged in, failing token creation.");
        }
    }

    @RequestMapping(value = "/version")
    public VersionInformation getVersion() {
        return this.versionInformation;
    }

    /** uncomment if email-sending loop needs to be triggered manually for local testing purposes.
     @RequestMapping(value = "/email", method = GET, produces = APPLICATION_JSON_VALUE)
     public void email() {
     logger.info("run email sending loop" );
     this.frontendService.sendEmails();
     }
     */
}
