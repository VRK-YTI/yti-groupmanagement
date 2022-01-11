package fi.vm.yti.groupmanagement;

import fi.vm.yti.groupmanagement.model.*;
import fi.vm.yti.groupmanagement.security.AuthorizationManager;
import fi.vm.yti.groupmanagement.service.FrontendService;
import fi.vm.yti.groupmanagement.service.PublicApiService;
import fi.vm.yti.security.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = { OrganizationTest.Initializer.class })
@Testcontainers
public class OrganizationTest {

    @Autowired
    FrontendService frontendService;

    @Autowired
    PublicApiService publicApiService;

    @MockBean
    AuthorizationManager authorizationManager;

    @Container
    public static PostgreSQLContainer postgreSQLContainer = GroupmanagementDatabaseContainer.getInstance();

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    private PublicApiUser user;

    @BeforeEach
    public void setup() {
        when(authorizationManager.canCreateOrganization()).thenReturn(true);
        when(authorizationManager.canViewOrganization(any(UUID.class))).thenReturn(true);
        when(authorizationManager.canEditOrganization(any(UUID.class))).thenReturn(true);

        user = publicApiService.getOrCreateUser("admin@example.com", "Admin", "Test");
    }

    @Test
    public void createAndFetchOrganization() {
        CreateOrganization org = getOrganization(user.getEmail());
        UUID organizationId = frontendService.createOrganization(org);

        assertNotNull(organizationId);

        OrganizationWithUsers organization = frontendService.getOrganization(organizationId);

        assertEquals(user.getEmail(), organization.users.get(0).user.email);
        assertEquals(org.nameFi, organization.organization.nameFi);
    }

    @Test
    public void updateOrganization() {
        CreateOrganization org = getOrganization(user.getEmail());
        UUID organizationId = frontendService.createOrganization(org);

        OrganizationWithUsers organization = frontendService.getOrganization(organizationId);
        organization.organization.descriptionFi = "New description";

        UpdateOrganization updateOrganization = new UpdateOrganization();
        updateOrganization.organization = organization.organization;

        updateOrganization.userRoles = getEmailRoles(organization.users);

        frontendService.updateOrganization(updateOrganization);

        OrganizationWithUsers organizationAfterUpdate = frontendService.getOrganization(organizationId);

        assertEquals("New description", organizationAfterUpdate.organization.descriptionFi);
    }

    @Test
    public void addChildOrganizations() {
        CreateOrganization parent = getOrganization(user.getEmail());
        UUID parentOrganizationId = frontendService.createOrganization(parent);

        CreateOrganization child = getOrganization(user.getEmail());
        child.parentId = parentOrganizationId;
        UUID childOrganizationId = frontendService.createOrganization(child);

        OrganizationWithUsers parentOrganizationWithChild = frontendService.getOrganization(parentOrganizationId);
        OrganizationWithUsers childOrganization = frontendService.getOrganization(childOrganizationId);

        assertEquals(1, parentOrganizationWithChild.childOrganizations.size());
        assertEquals(childOrganization.organization.parentId, parentOrganizationId);
    }

    @Test
    public void removeOrganization() {
        CreateOrganization parent = getOrganization(user.getEmail());
        UUID parentOrganizationId = frontendService.createOrganization(parent);

        CreateOrganization child = getOrganization(user.getEmail());
        child.parentId = parentOrganizationId;
        UUID childOrganizationId = frontendService.createOrganization(child);

        OrganizationWithUsers parentOrganization = frontendService.getOrganization(parentOrganizationId);
        parentOrganization.organization.removed = true;

        UpdateOrganization updateOrganization = new UpdateOrganization();
        updateOrganization.userRoles = getEmailRoles(parentOrganization.users);
        updateOrganization.organization = parentOrganization.organization;

        frontendService.updateOrganization(updateOrganization);

        // Also child organizations should be marked as removed
        OrganizationWithUsers childOrganization = frontendService.getOrganization(childOrganizationId);
        assertTrue(childOrganization.organization.removed);
    }

    @Test
    public void listOrganizations() {
        CreateOrganization organization = getOrganization(user.getEmail());
        UUID organizationId = frontendService.createOrganization(organization);

        CreateOrganization parent = getOrganization(user.getEmail());
        UUID parentOrganizationId = frontendService.createOrganization(parent);

        CreateOrganization child = getOrganization(user.getEmail());
        child.parentId = parentOrganizationId;
        UUID childOrganizationId = frontendService.createOrganization(child);

        List<OrganizationListItem> mainOrganizationList = frontendService.getOrganizationList();
        List<OrganizationListItem> organizationListWithChildOrganizations = frontendService.getOrganizationList(true);

        assertTrue(findOrganization(organizationId, mainOrganizationList).isPresent());
        assertTrue(findOrganization(parentOrganizationId, mainOrganizationList).isPresent());
        assertFalse(findOrganization(childOrganizationId, mainOrganizationList).isPresent());

        assertTrue(findOrganization(organizationId, organizationListWithChildOrganizations).isPresent());
        assertTrue(findOrganization(parentOrganizationId, organizationListWithChildOrganizations).isPresent());
        assertTrue(findOrganization(childOrganizationId, organizationListWithChildOrganizations).isPresent());
    }

    @Test
    public void organizationHierarchyDepth() {
        CreateOrganization parent = getOrganization(user.getEmail());
        UUID parentOrganizationId = frontendService.createOrganization(parent);

        CreateOrganization child = getOrganization(user.getEmail());
        child.parentId = parentOrganizationId;
        UUID childOrganizationId = frontendService.createOrganization(child);

        CreateOrganization child2 = getOrganization(user.getEmail());
        child2.parentId = childOrganizationId;

        // only one hierarchy level is allowed
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            frontendService.createOrganization(child2);
        });

        assertNotNull(exception);
    }

    private static CreateOrganization getOrganization(String email) {
        CreateOrganization org = new CreateOrganization();
        org.nameFi = "Test";
        org.descriptionFi = "Test description";
        org.adminUserEmails = Arrays.asList(email);
        org.url = "http://www.example.com";

        return org;
    }

    private static List<EmailRole> getEmailRoles(List<UserWithRoles> users) {

        return users.stream().map(user -> {
            return user.roles.stream().map(role -> {
                EmailRole emailRole = new EmailRole();
                emailRole.userEmail = user.user.email;
                emailRole.role = role;
                return emailRole;
            }).collect(Collectors.toList());
        }).reduce(new ArrayList<>(), (a, c) -> {
            a.addAll(c);
            return a;
        });
    }

    private static Optional<OrganizationListItem> findOrganization(UUID orgId, List<OrganizationListItem> organizationList) {
        return organizationList.stream()
                .filter(org -> org.getId().equals(orgId))
                .findFirst();
    }
}
