package fi.vm.yti.groupmanagement.dao;

import fi.vm.yti.groupmanagement.model.*;
import fi.vm.yti.groupmanagement.service.impl.TokenServiceImpl;

import org.dalesbred.Database;
import org.dalesbred.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static fi.vm.yti.groupmanagement.util.CollectionUtil.mapToList;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class FrontendDao {

    private static final Logger logger = LoggerFactory.getLogger(FrontendDao.class);

    private final Database db;
    private final TokenServiceImpl tokenService;

    @Autowired
    public FrontendDao(final Database db,
                       final TokenServiceImpl tokenService) {
        this.db = db;
        this.tokenService = tokenService;
    }

    public List<UserWithRolesInOrganizations> getUsersForAdminOrganizations(final String email) {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                "WHERE uo.organization_id IN (SELECT organization_id FROM user_organization WHERE u.email = ? and role_name='ADMIN') \n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName \n" +
                "", email);

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            final UserRow.UserDetails user = entry.getKey();

            final List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public List<UserWithRolesInOrganizations> getUsers() {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) WHERE u.removed_at IS NULL \n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName \n" +
                "");

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            final UserRow.UserDetails user = entry.getKey();

            final List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public List<UserWithRolesInOrganizations> getPublicUsers() {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "LEFT JOIN user_organization uo ON (uo.user_id = u.id) WHERE u.removed_at IS NULL AND u.email like '%localhost'\n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName \n" +
                "");

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            final UserRow.UserDetails user = entry.getKey();

            final List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public boolean removeUser(String email) {
        db.update("DELETE FROM user_organization uo USING \"user\" u WHERE uo.user_id = u.id AND u.email = ?", email);
        final int modifiedRows = db.update("UPDATE \"user\" SET email=?, firstname=?, lastname=?, removed_at=? WHERE email = ?",
            null, null, null, LocalDateTime.now(), email);
        if (modifiedRows > 0) {
            return true;
        } else {
            return false;
        }
    }

    public @NotNull List<OrganizationListItem> getOrganizationListOpt(Boolean showRemoved) {
        final List<OrganizationListItemRow> rows = db.findAll(OrganizationListItemRow.class, "SELECT id, name_en, name_fi, name_sv FROM organization WHERE removed = ? ORDER BY name_fi", showRemoved);
        return mapToList(rows, row -> new OrganizationListItem(row.id, row.nameFi, row.nameEn, row.nameSv));
    }

    public @NotNull List<OrganizationListItem> getOrganizationList() {
        final List<OrganizationListItemRow> rows = db.findAll(OrganizationListItemRow.class, "SELECT id, name_en, name_fi, name_sv FROM organization ORDER BY name_fi");
        return mapToList(rows, row -> new OrganizationListItem(row.id, row.nameFi, row.nameEn, row.nameSv));
    }

    public @NotNull Organization getOrganization(UUID organizationId) {
        return db.findUnique(Organization.class, "SELECT id, name_en, name_fi, name_sv, description_en, description_fi, description_sv, url, removed FROM organization where id = ?", organizationId);
    }

    public @NotNull List<UserWithRoles> getOrganizationUsers(UUID organizationId) {

        final List<UserRow> rows = db.findAll(UserRow.class,
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

    public void createOrganization(final Organization org) {

        db.update("INSERT INTO organization (id, name_en, name_fi, name_sv, description_en, description_fi, description_sv, url) VALUES (?,?,?,?,?,?,?,?)",
            org.id, org.nameEn, org.nameFi, org.nameSv, org.descriptionEn, org.descriptionFi, org.descriptionSv, org.url);
    }

    public void updateOrganization(final Organization org) {

        db.update("UPDATE organization SET name_en=?, name_fi=?, name_sv=?, description_en=?, description_fi=?, description_sv=?, url=?, removed=?, modified=now() WHERE id = ?",
            org.nameEn, org.nameFi, org.nameSv, org.descriptionEn, org.descriptionFi, org.descriptionSv, org.url, org.removed, org.id);
    }

    public void addUserToRoleInOrganization(final String userEmail,
                                            final String role,
                                            final UUID id) {

        db.update("INSERT INTO user_organization (user_id, organization_id, role_name) VALUES ((select id from \"user\" where email = ?), ?, ?)", userEmail, id, role);
        updateOrganizationModifiedStamp(id);
    }

    public void clearUserRoles(UUID id) {

        db.update("DELETE FROM user_organization uo where uo.organization_id = ?", id);
        updateOrganizationModifiedStamp(id);
    }

    public @NotNull List<String> getAllRoles() {
        return db.findAll(String.class, "SELECT name FROM role");
    }

    public String getOrganizationNameFI(final UUID id) {
        return db.findUnique(String.class, "SELECT name_fi FROM organization WHERE id=?", id);
    }

    public void addUserRequest(UserRequestModel userRequest) {
        logger.info("AddUserRequest 1");
        // Log new user id
        db.update("INSERT INTO request (user_id, organization_id, role_name, sent) VALUES ((select id from \"user\" where email = ?),?,?,?)",
            userRequest.email, userRequest.organizationId, userRequest.role, false);
        // Log newly created  user id
        logger.info("AddUserRequest 2");
        @NotNull List<UUID> userIds = db.findAll(UUID.class, "SELECT id from user where email = ?", userRequest.email);
        if (userIds != null && userIds.size() == 1) {
            logger.info("AddNewUser id:" + userIds.get(0).toString());
        }
    }

    public @NotNull List<UserRequestWithOrganization> getAllUserRequestsForOrganizations(@Nullable Set<UUID> organizations) {

        final QueryBuilder builder = new QueryBuilder(
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
            "SELECT r.id, u.email as user_email, r.user_id, r.organization_id, r.role_name, r.sent FROM request r \n" +
                "LEFT JOIN \"user\" u on (u.id = r.user_id) \n" +
                "WHERE r.id = ?", requestId);
    }

    public String createToken(final UUID userId) {

        // TODO: Check if db would work with just plain Date instead of LocalDateTime
        final LocalDateTime createdAtLocalDateTime = now();
        final LocalDateTime invalidatedAtLocalDateTime = createdAtLocalDateTime.plusMonths(6);
        final Date createdAt = Date.from(createdAtLocalDateTime.atZone(ZoneId.of("UTC")).toInstant());
        final Date invalidatedAt = Date.from(invalidatedAtLocalDateTime.atZone(ZoneId.of("UTC")).toInstant());
        final int success = db.update("UPDATE \"user\" SET token_created_at = ?, token_invalidation_at = ? WHERE id = ?", createdAtLocalDateTime, invalidatedAtLocalDateTime, userId);
        if (success == 1) {
            final Map<String, Object> claims = new HashMap<>();
            return tokenService.generateToken(userId, claims, createdAt, invalidatedAt);
        } else {
            throw new RuntimeException("No user found with ID: " + userId.toString());
        }
    }

    public boolean deleteToken(final UUID userId) {

        final int success = db.update("UPDATE \"user\" SET token_created_at = NULL, token_invalidation_at = NULL WHERE id = ?", userId);
        return success == 1;
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
