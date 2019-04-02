package fi.vm.yti.groupmanagement.dao;

import fi.vm.yti.groupmanagement.model.*;
import org.dalesbred.Database;
import org.dalesbred.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

import static fi.vm.yti.groupmanagement.util.CollectionUtil.mapToList;
import static java.util.stream.Collectors.*;


@Repository
public class FrontendDao {

    private final Database db;

    @Autowired
    public FrontendDao(Database db) {
        this.db = db;
    }

    public List<UserWithRolesInOrganizations> getUsersForAdminOrganizations(String email) {

        List<UserRow> rows = db.findAll(UserRow.class,
                    "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                            "FROM \"user\" u \n" +
                            "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                            "WHERE uo.organization_id IN (SELECT organization_id FROM user_organization WHERE u.email = ? and role_name='ADMIN') \n" +
                            "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                            "ORDER BY u.lastName, u.firstName \n" +
                            "", email);

        Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
                rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            UserRow.UserDetails user = entry.getKey();

            List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                    .filter(org -> org.id != null)
                    .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                    .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public List<UserWithRolesInOrganizations> getUsers() {

        List<UserRow> rows = db.findAll(UserRow.class,
                "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                        "FROM \"user\" u \n" +
                        "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) WHERE u.removed_at IS NULL \n" +
                        "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                        "ORDER BY u.lastName, u.firstName \n" +
                        "");

        Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
                rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            UserRow.UserDetails user = entry.getKey();

            List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                    .filter(org -> org.id != null)
                    .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                    .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public List<UserWithRolesInOrganizations> getPublicUsers() {

        List<UserRow> rows = db.findAll(UserRow.class,
                "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                        "FROM \"user\" u \n" +
                        "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) WHERE u.removed_at IS NULL AND u.email like '%localhost'\n" +
                        "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                        "ORDER BY u.lastName, u.firstName \n" +
                        "");

        Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
                rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            UserRow.UserDetails user = entry.getKey();

            List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                    .filter(org -> org.id != null)
                    .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                    .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public void removeUser(String email) {
        db.update("UPDATE \"user\" SET email=?, firstname=?, lastname=?, removed_at=? WHERE email = ?",
                null, null, null, LocalDateTime.now(), email);
    }

    public @NotNull List<OrganizationListItem> getOrganizationListOpt(Boolean showRemoved) {
        List<OrganizationListItemRow> rows = db.findAll(OrganizationListItemRow.class, "SELECT id, name_en, name_fi, name_sv FROM organization WHERE removed = ? ORDER BY name_fi", showRemoved);
        return mapToList(rows, row -> new OrganizationListItem(row.id, row.nameFi, row.nameEn, row.nameSv));
    }

    public @NotNull List<OrganizationListItem> getOrganizationList() {
        List<OrganizationListItemRow> rows = db.findAll(OrganizationListItemRow.class, "SELECT id, name_en, name_fi, name_sv FROM organization ORDER BY name_fi");
        return mapToList(rows, row -> new OrganizationListItem(row.id, row.nameFi, row.nameEn, row.nameSv));
    }

    public @NotNull Organization getOrganization(UUID organizationId) {
        return db.findUnique(Organization.class,"SELECT id, name_en, name_fi, name_sv, description_en, description_fi, description_sv, url, removed FROM organization where id = ?", organizationId);
    }

    public @NotNull List<UserWithRoles> getOrganizationUsers(UUID organizationId) {

        List<UserRow> rows = db.findAll(UserRow.class,
                "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                        "FROM \"user\" u \n" +
                        "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                        "WHERE uo.organization_id = ? \n" +
                        "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                        "ORDER BY u.lastName, u.firstName, u.email", organizationId);

        return mapToList(rows, row -> {

            User user = new User();
            user.firstName = row.user.firstName;
            user.lastName = row.user.lastName;
            user.email = row.user.email;

            UserWithRoles result = new UserWithRoles();
            result.roles = row.organization.roles;
            result.user = user;

            return result;
        });
    }

    public @NotNull List<String> getAvailableRoles() {
        return db.findAll(String.class, "SELECT name from role");
    }

    public void createOrganization(Organization org) {

        db.update("INSERT INTO organization (id, name_en, name_fi, name_sv, description_en, description_fi, description_sv, url) VALUES (?,?,?,?,?,?,?,?)",
                org.id, org.nameEn, org.nameFi, org.nameSv, org.descriptionEn, org.descriptionFi, org.descriptionSv, org.url);
    }

    public void updateOrganization(Organization org) {

        db.update("UPDATE organization SET name_en=?, name_fi=?, name_sv=?, description_en=?, description_fi=?, description_sv=?, url=?, removed=?, modified=now() WHERE id = ?",
                org.nameEn, org.nameFi, org.nameSv, org.descriptionEn, org.descriptionFi, org.descriptionSv, org.url, org.removed, org.id);
    }

    public void addUserToRoleInOrganization(String userEmail, String role, UUID id) {
        db.update("INSERT INTO user_organization (user_id, organization_id, role_name) VALUES ((select id from \"user\" where email = ?), ?, ?)", userEmail, id, role);
        updateOrganizationModifiedStamp(id);
    }

    public void clearUserRoles(UUID id) {
        db.update("DELETE FROM user_organization uo where uo.organization_id = ?", id);
        updateOrganizationModifiedStamp(id);
    }

    public @NotNull List<String> getAllRoles() {
        return db.findAll(String.class,"SELECT name FROM role");
    }

    public String getOrganizationNameFI(UUID id ) { return db.findUnique(String.class,"SELECT name_fi FROM organization WHERE id=?", id);}

    public void addUserRequest(UserRequestModel userRequest) {
        db.update("INSERT INTO request (user_id, organization_id, role_name, sent) VALUES ((select id from \"user\" where email = ?),?,?,?)",
                userRequest.email, userRequest.organizationId, userRequest.role, false);
    }

    public @NotNull List<UserRequestWithOrganization> getAllUserRequestsForOrganizations(@Nullable Set<UUID> organizations) {

        QueryBuilder builder = new QueryBuilder(
                "SELECT r.id, us.email as user_email, r.organization_id, r.role_name, us.firstName, us.lastName, org.name_fi, org.name_en, org.name_sv, r.sent \n" +
                        "FROM request r\n" +
                        "LEFT JOIN \"user\" us ON (us.id = r.user_id)\n" +
                        "LEFT JOIN organization org ON (org.id = r.organization_id)\n");

        if (organizations != null) {
            builder.append("WHERE r.organization_id in (").appendPlaceholders(organizations).append(")");
        }

        return db.findAll(UserRequestWithOrganization.class, builder.build());
    }

    public void deleteUserRequest(int requestId) {
        db.update("DELETE FROM request WHERE id=?", requestId);
    }

    public @NotNull UserRequest getUserRequest(int requestId) {
        return db.findUnique(UserRequest.class,
                "SELECT r.id, u.email as user_email, r.organization_id, r.role_name, r.sent FROM request r \n" +
                        "LEFT JOIN \"user\" u on (u.id = r.user_id) \n" +
                        "WHERE r.id = ?", requestId);
    }

    void updateOrganizationModifiedStamp(UUID orgId) {
        db.update("UPDATE organization SET modified=now() WHERE id = ?", orgId);
    }

    public static class OrganizationListItemRow {

        public UUID id;
        public String nameFi;
        public String nameEn;
        public String nameSv;
    }
}
