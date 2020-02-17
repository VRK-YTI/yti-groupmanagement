package fi.vm.yti.groupmanagement.dao;

import org.dalesbred.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.vm.yti.groupmanagement.model.SystemCountModel;

@Repository
public class SystemDao {

    private final Database db;

    @Autowired
    public SystemDao(final Database db) {
        this.db = db;
    }

    /**
     * Counts organizations and users. Entities marked as removed are not counted. In addition, it is required that a user is either
     * superuser or has a role in some non-removed organization.
     *
     * @return user and organization counts
     */
    public SystemCountModel countThings() {
        long organizations = db.findUniqueLong("select count(*) from organization where not removed");
        long users = db.findUniqueLong("select count(email) from \"user\" " +
            "left join (select distinct(user_id) from user_organization " +
            "           join organization on user_organization.organization_id = organization.id where not organization.removed) as organized " +
            "on \"user\".id = organized.user_id " +
            "where (\"user\".superuser = true or organized.user_id is not null) and \"user\".removed_at is null");
        return new SystemCountModel(organizations, users);
    }
}
