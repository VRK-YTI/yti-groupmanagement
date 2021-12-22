package fi.vm.yti.groupmanagement;

import org.testcontainers.containers.PostgreSQLContainer;

public class GroupmanagementDatabaseContainer extends PostgreSQLContainer<GroupmanagementDatabaseContainer> {

    public GroupmanagementDatabaseContainer() {
        super("postgres:12.5");
    }

    private static GroupmanagementDatabaseContainer container;

    public static GroupmanagementDatabaseContainer getInstance() {
        if (container == null) {
            container = new GroupmanagementDatabaseContainer();
        }
        return container;
    }

    @Override
    public void stop() {
        // Container will be stopped by JVM
    }
}
