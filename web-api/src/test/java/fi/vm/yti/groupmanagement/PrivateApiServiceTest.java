package fi.vm.yti.groupmanagement;

import fi.vm.yti.groupmanagement.model.CreateOrganization;
import fi.vm.yti.groupmanagement.model.OrganizationWithUsers;
import fi.vm.yti.groupmanagement.model.PublicApiUser;
import fi.vm.yti.groupmanagement.model.PublicApiUserRequest;
import fi.vm.yti.groupmanagement.security.AuthorizationManager;
import fi.vm.yti.groupmanagement.service.FrontendService;
import fi.vm.yti.groupmanagement.service.PrivateApiService;
import fi.vm.yti.groupmanagement.service.PublicApiService;
import fi.vm.yti.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = { OrganizationTest.Initializer.class })
@Testcontainers
@Sql(scripts = {"/init_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/delete_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PrivateApiServiceTest {

    @Autowired
    PrivateApiService privateApiService;

    @Autowired
    PublicApiService publicApiService;

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

    private UUID orgId = UUID.fromString("7d3a3c00-5a6b-489b-a3ed-63bb58c26a63");

    @Test
    public void testCreateUserRequest() {
        PublicApiUser user = publicApiService.getOrCreateUser("test.user_1@example.com", "Test", "User");

        privateApiService.addUserRequest(user.getId(), orgId, Role.CODE_LIST_EDITOR.toString());

        var userRequests = privateApiService.getUserRequests(user.getId());

        assertEquals(1, userRequests.get(0).getRole().size());
    }

    @Test
    public void testCreateUserRequestMultiple() {
        PublicApiUser user = publicApiService.getOrCreateUser("test.user_2@example.com", "Test", "User");

        privateApiService.addUserRequest(user.getId(), orgId, Role.TERMINOLOGY_EDITOR.toString() + "," + Role.CODE_LIST_EDITOR.toString());

        var userRequests = privateApiService.getUserRequests(user.getId());

        assertEquals(2, userRequests.get(0).getRole().size());
    }

    @Test
    public void testInvalidRole() {
        PublicApiUser user = publicApiService.getOrCreateUser("test.user_3@example.com", "Test", "User");

        try {
            privateApiService.addUserRequest(user.getId(), orgId, Role.TERMINOLOGY_EDITOR.toString() + ",ADMIN");
        } catch (Exception e) {
            // expecting exception
        }

        var userRequests = privateApiService.getUserRequests(user.getId());

        assertEquals(0, userRequests.size());
    }
}
