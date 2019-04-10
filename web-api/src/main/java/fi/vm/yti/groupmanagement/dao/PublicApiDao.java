package fi.vm.yti.groupmanagement.dao;

import fi.vm.yti.groupmanagement.model.*;
import org.apache.http.client.utils.DateUtils;
import org.dalesbred.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fi.vm.yti.groupmanagement.util.CollectionUtil.requireSingleOrNone;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;


@Repository
public class PublicApiDao {

    private final Database database;

    @Autowired
    public PublicApiDao(Database database) {
        this.database = database;
    }

    public @NotNull PublicApiUser createUser(@NotNull String email, @NotNull String firstName, @NotNull String lastName, UUID id) {
        this.database.update("INSERT INTO \"user\" (email, firstName, lastName, superuser, id) VALUES (?,?,?,?,?)",
                email, firstName, lastName, false, id);

        return requireNonNull(findUserByEmail(email));
    }

    public @NotNull PublicApiUser getUserByEmail(@NotNull  String email) {
        return requireNonNull(findUserByEmail(email));
    }

    public PublicApiUser getUserById(@NotNull UUID id) {
        return requireNonNull(findUserById(id));
    }

    public @Nullable PublicApiUser findUserByEmail(@NotNull String email) {
        return findUser("email", email);
    }

    public @Nullable PublicApiUser findUserById(@NotNull UUID id) {
        return findUser("id", id);
    }

    private @Nullable PublicApiUser findUser(@NotNull String whereColumn, @NotNull Object conditionValue) {

        List<UserRow> rows = database.findAll(UserRow.class,
                "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                        "FROM \"user\" u \n" +
                        "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                        "WHERE u." + whereColumn + " = ? \n" +
                        "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at", conditionValue);

        return requireSingleOrNone(rowsToAuthorizationUsers(rows));
    }

    /**
     * List all public users, ie users with @localhost as email domain
     * @return List of users
     */
    public List<PublicApiUserListItem> getPublicUsers() {
        return database.findAll(PublicApiUserListItem.class,
                "SELECT email, firstName, lastName, id FROM \"user\" WHERE removed_at IS NULL AND email like '%@localhost' ORDER BY lastname, firstname");
    }

    /**
     * List all users
     * @return List of users
     */
    public List<PublicApiUserListItem> getAllUsers() {
        return database.findAll(PublicApiUserListItem.class,
                "SELECT email, firstName, lastName, id FROM \"user\" WHERE removed_at IS NULL ORDER BY lastname, firstname");
    }


    public List<PublicApiUserListItem> getModifiedUsers(String ifModifiedSince) {

        Date date;
        try {
            date = DateUtils.parseDate(ifModifiedSince);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return database.findAll(PublicApiUserListItem.class,
                "SELECT email, firstName, lastName, id FROM \"user\" WHERE removed_at IS NULL AND created_at > ? ORDER BY lastname, firstname", date);
    }


    public List<PublicApiOrganization> rowsToOrganizations(List<OrganizationRow> rows) {
        return rows.stream().map(row -> {

            Map<String, String> prefLabel = new HashMap<>(3);
            Map<String, String> description = new HashMap<>(3);

            prefLabel.put("fi", row.nameFi);
            prefLabel.put("en", row.nameEn);
            prefLabel.put("sv", row.nameSv);

            description.put("fi", row.descriptionFi);
            description.put("en", row.descriptionEn);
            description.put("sv", row.descriptionSv);

            return new PublicApiOrganization(row.id, unmodifiableMap(prefLabel), unmodifiableMap(description), row.url, row.removed);

        }).collect(toList());
    }

    public @NotNull List<PublicApiOrganization> getOrganizations() {

        List<OrganizationRow> rows = database.findAll(OrganizationRow.class, "select id, name_en, name_sv, name_fi, description_en, description_sv, description_fi, url, removed from organization");

        return rowsToOrganizations(rows);
    }

    public @NotNull List<PublicApiOrganization> getValidOrganizations() {

        List<OrganizationRow> rows = database.findAll(OrganizationRow.class, "select id, name_en, name_sv, name_fi, description_en, description_sv, description_fi, url, removed from organization where removed = ?",false);

        return rowsToOrganizations(rows);
    }

    public @NotNull List<PublicApiOrganization> getModifiedOrganizations(String ifModifiedSince, boolean onlyValid) {

        Date date;
        String[] datePatterns = new String[] {
                DateUtils.PATTERN_ASCTIME,
                DateUtils.PATTERN_RFC1036,
                DateUtils.PATTERN_RFC1123,
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd",};

        try {
            date = DateUtils.parseDate(ifModifiedSince,datePatterns);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        List<OrganizationRow> rows = database.findAll(OrganizationRow.class, "select id, name_en, name_sv, name_fi, description_en, description_sv, description_fi, url, removed from organization where modified > ? AND removed = ?", date,!onlyValid);

        return rowsToOrganizations(rows);
    }

    public void addUserRequest(String email, UUID organizationId, String role) {
        database.update("INSERT INTO request (user_id, organization_id, role_name, sent) VALUES ((select id from \"user\" where email = ?),?,?,?)",
                email, organizationId, role, false);
    }

    private static PublicApiUser entryToAuthorizationUser(Map.Entry<UserRow.UserDetails, List<UserRow.OrganizationDetails>> entry) {

        UserRow.UserDetails user = entry.getKey();

        List<PublicApiUserOrganization> nonNullOrganizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new PublicApiUserOrganization(org.id, org.roles))
                .collect(toList());

        return new PublicApiUser(user.email, user.firstName, user.lastName, user.superuser, false, user.creationDateTime, user.id, user.removalDateTime, nonNullOrganizations);
    }

    private static List<PublicApiUser> rowsToAuthorizationUsers(List<UserRow> rows) {

        Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
                rows.stream().collect(
                        groupingBy(row -> row.user,
                                mapping(row -> row.organization, toList())));

        return grouped.entrySet().stream().map(PublicApiDao::entryToAuthorizationUser).collect(toList());
    }

    public List<PublicApiUserRequest> getUserRequests(String email) {
        return database.findAll(PublicApiUserRequest.class,
                "SELECT organization_id, array_agg(role_name)\n" +
                        "FROM request r \n" +
                        "LEFT JOIN \"user\" u on (u.id = r.user_id)" +
                        "WHERE u.email = ? \n" +
                        "GROUP BY r.organization_id", email);
    }

    public static final class OrganizationRow {

        public UUID id;
        public String url;
        public String nameEn;
        public String nameFi;
        public String nameSv;
        public String descriptionEn;
        public String descriptionFi;
        public String descriptionSv;
        public Boolean removed;
    }
}
