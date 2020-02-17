package fi.vm.yti.groupmanagement.model;

public class SystemCountModel {

    private long organizationCount;
    private long userCount;

    SystemCountModel() {
    }

    public SystemCountModel(final long organizationCount,
                            final long userCount) {
        this.organizationCount = organizationCount;
        this.userCount = userCount;
    }

    public long getOrganizationCount() {
        return organizationCount;
    }

    public void setOrganizationCount(final long organizationCount) {
        this.organizationCount = organizationCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(final long userCount) {
        this.userCount = userCount;
    }

    @Override
    public String toString() {
        return organizationCount + " organizations, " + userCount + " users";
    }
}
