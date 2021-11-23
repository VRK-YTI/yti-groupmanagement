package fi.vm.yti.groupmanagement.dao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.apache.http.client.utils.DateUtils;
import org.dalesbred.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.vm.yti.groupmanagement.model.PrivateApiTempUserListItem;
import fi.vm.yti.groupmanagement.model.PublicApiOrganization;
import fi.vm.yti.groupmanagement.model.PublicApiUser;
import fi.vm.yti.groupmanagement.model.PublicApiUserListItem;
import fi.vm.yti.groupmanagement.model.PublicApiUserOrganization;
import fi.vm.yti.groupmanagement.model.PublicApiUserRequest;
import fi.vm.yti.groupmanagement.model.TempUser;
import fi.vm.yti.groupmanagement.model.TokenModel;
import fi.vm.yti.groupmanagement.service.impl.TokenData;
import fi.vm.yti.groupmanagement.service.impl.TokenServiceImpl;
import fi.vm.yti.security.Role;
import fi.vm.yti.security.YtiUser;
import static fi.vm.yti.groupmanagement.util.CollectionUtil.requireSingleOrNone;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

@Repository
public class PublicApiDao {

    private static final Logger logger = LoggerFactory.getLogger(PublicApiDao.class);
    private final Database database;
    private final TokenServiceImpl tokenService;

    @Autowired
    public PublicApiDao(final Database database,
                        final TokenServiceImpl tokenService) {
        this.database = database;
        this.tokenService = tokenService;
    }

    private static PublicApiUser entryToAuthorizationUser(final Map.Entry<UserRow.UserDetails, List<UserRow.OrganizationDetails>> entry) {

        final UserRow.UserDetails user = entry.getKey();

        final List<PublicApiUserOrganization> nonNullOrganizations = entry.getValue().stream()
            .filter(org -> org.id != null)
            .map(org -> new PublicApiUserOrganization(org.id, org.roles))
            .collect(toList());

        return new PublicApiUser(user.email, user.firstName, user.lastName, user.superuser, false, user.creationDateTime, user.id, user.removalDateTime, user.tokenCreatedAt, user.tokenInvalidationAt, null, null, nonNullOrganizations);
    }

    private static List<PublicApiUser> rowsToAuthorizationUsers(List<UserRow> rows) {

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(
                groupingBy(row -> row.user,
                    mapping(row -> row.organization, toList())));

        return grouped.entrySet().stream().map(PublicApiDao::entryToAuthorizationUser).collect(toList());
    }

    public @NotNull PublicApiUser createUser(@NotNull final String email,
                                             @NotNull final String firstName,
                                             @NotNull final String lastName,
                                             final UUID id) {
        this.database.update("INSERT INTO \"user\" (email, firstName, lastName, superuser, id) VALUES (?,?,?,?,?)",
            email, firstName, lastName, false, id);

        return requireNonNull(findUserByEmail(email));
    }

    public @NotNull PublicApiUser createTempUser(final TempUser tempUser) {
        this.database.update("INSERT INTO tempuser (id, email, firstname, lastname, token_role, container_uri) VALUES (?,?,?,?,?,?)",
            tempUser.id, tempUser.email, tempUser.firstName, tempUser.lastName, "MEMBER", tempUser.containerUri);

        return requireNonNull(findTempUserById(tempUser.id));
    }

    public @NotNull PublicApiUser getUserByEmail(@NotNull final String email) {
        return requireNonNull(findUserByEmail(email));
    }

    public PublicApiUser getUserById(@NotNull final UUID id) {
        return requireNonNull(findUserById(id));
    }

    public PublicApiUser getUserOrTempUserById(@NotNull final UUID id) {
        PublicApiUser publicApiUser = findUserById(id);
        if (publicApiUser == null) {
            publicApiUser = findTempUserById(id);
        }
        return requireNonNull(publicApiUser);
    }

    public @Nullable PublicApiUser findUserByEmail(@NotNull final String email) {
        return findUser("email", email);
    }

    public @Nullable PublicApiUser findUserById(@NotNull final UUID id) {
        return findUser("id", id);
    }

    private @Nullable PublicApiUser findUser(@NotNull final String whereColumn,
                                             @NotNull final Object conditionValue) {

        final List<UserRow> rows = database.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, u.token_created_at, u.token_invalidation_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                "WHERE u." + whereColumn + " = ? \n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, u.token_created_at, u.token_invalidation_at", conditionValue);

        return requireSingleOrNone(rowsToAuthorizationUsers(rows));
    }

    public @Nullable PublicApiUser findTempUserById(@NotNull final UUID id) {
        final List<TempUserRow> rows = database.findAll(TempUserRow.class,
            "SELECT id, firstname, lastname, email, token_role, container_uri, created_at, removed_at, token_created_at, token_invalidation_at FROM tempuser WHERE id = ?", id);

        if (rows.size() == 1) {
            final TempUserRow row = rows.get(0);
            final List<PublicApiUserOrganization> organizations = new ArrayList<>();
            return new PublicApiUser(row.tempUser.email, row.tempUser.firstName, row.tempUser.lastName, false, false, row.tempUser.creationDateTime, row.tempUser.id, row.tempUser.removalDateTime, row.tempUser.tokenCreatedAt, row.tempUser.tokenInvalidationAt, row.tempUser.containerUri, row.tempUser.tokenRole, organizations);
        } else {
            throw new RuntimeException();
        }
    }

    public List<PublicApiUserListItem> getPublicUsers() {
        return database.findAll(PublicApiUserListItem.class,
            "SELECT email, firstName, lastName, id FROM \"user\" WHERE removed_at IS NULL AND email like '%@localhost' ORDER BY lastname, firstname");
    }

    public List<PublicApiUserListItem> getAllUsers() {
        return database.findAll(PublicApiUserListItem.class,
            "SELECT email, firstName, lastName, id FROM \"user\" WHERE removed_at IS NULL ORDER BY lastname, firstname");
    }

    public List<PrivateApiTempUserListItem> getAllTempUsers() {
        return database.findAll(PrivateApiTempUserListItem.class,
            "SELECT id, email, firstname, lastname, token_role, container_uri FROM tempuser ORDER BY lastname, firstname");
    }

    public List<PrivateApiTempUserListItem> getAllTempUsersForContainerUri(final String containerUri) {
        return database.findAll(PrivateApiTempUserListItem.class,
            "SELECT id, email, firstname, lastname, token_role, container_uri FROM tempuser WHERE container_uri = ? ORDER BY lastname, firstname", containerUri);
    }

    public List<PublicApiUserListItem> getModifiedUsers(final String ifModifiedSince) {

        final Date date;
        try {
            date = DateUtils.parseDate(ifModifiedSince);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return database.findAll(PublicApiUserListItem.class,
            "SELECT email, firstName, lastName, id FROM \"user\" WHERE removed_at IS NULL AND created_at > ? ORDER BY lastname, firstname", date);
    }

    public List<PrivateApiTempUserListItem> getModifiedTempUsers(final String ifModifiedSince) {

        final Date date;
        try {
            date = DateUtils.parseDate(ifModifiedSince);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return database.findAll(PrivateApiTempUserListItem.class,
            "SELECT email, firstName, lastName, token_role, container_uri, id FROM tempuser WHERE removed_at IS NULL AND created_at > ? ORDER BY lastname, firstname", date);
    }

    public YtiUser validateToken(final TokenModel tokenModel) {

        final String token = tokenModel.token;
        final TokenData tokenData = tokenService.getTokenData(token);
        if (tokenData != null) {
            if ("tempuser".equalsIgnoreCase(tokenData.getType())) {
                final PublicApiUser user = findTempUserById(tokenData.getUserId());
                if (user != null) {
                    final LocalDateTime createdAtFromUser = user.getTokenCreatedAt();
                    final LocalDateTime invalidationAtFromUser = user.getTokenInvalidationAt();
                    final Date createdAtFromToken = tokenData.getTokenCreatedAt();
                    final Date invalidationAtFromToken = tokenData.getTokenInvalidationAt();
                    if (createdAtFromUser != null && invalidationAtFromUser != null && createdAtFromToken != null && invalidationAtFromToken != null) {
                        final Instant createdAtFromUserInstant = createdAtFromUser.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
                        final Instant invalidationAtFromUserInstant = invalidationAtFromUser.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
                        final Instant createdAtFromTokenInstant = createdAtFromToken.toInstant().truncatedTo(ChronoUnit.SECONDS);
                        final Instant invalidationAtFromTokenInstant = invalidationAtFromToken.toInstant().truncatedTo(ChronoUnit.SECONDS);
                        if (createdAtFromUserInstant.equals(createdAtFromTokenInstant) && invalidationAtFromUserInstant.equals(invalidationAtFromTokenInstant)) {
                            return new YtiUser(user.getEmail(), user.getFirstName(), user.getLastName(), user.getId(), false, false, createdAtFromUser, invalidationAtFromUser, emptyMap(), user.getContainerUri(), user.getTokenRole());
                        } else {
                            logger.debug("Token validation failed with timestamp info:");
                            logger.debug("createdAtFromUserInstant: " + createdAtFromUserInstant.toString());
                            logger.debug("createdAtFromToken: " + createdAtFromTokenInstant.toString());
                            logger.debug("invalidationAtFromUserInstant: " + invalidationAtFromUserInstant.toString());
                            logger.debug("invalidationAtFromToken: " + invalidationAtFromTokenInstant.toString());
                        }
                    } else {
                        logger.info("Timestamps in either token or user in database are not present with user: " + tokenData.getUserId());
                    }
                }
            } else {
                final PublicApiUser user = findUserById(tokenData.getUserId());
                if (user != null) {
                    final LocalDateTime createdAtFromUser = user.getTokenCreatedAt();
                    final LocalDateTime invalidationAtFromUser = user.getTokenInvalidationAt();
                    final Date createdAtFromToken = tokenData.getTokenCreatedAt();
                    final Date invalidationAtFromToken = tokenData.getTokenInvalidationAt();
                    if (createdAtFromUser != null && invalidationAtFromUser != null && createdAtFromToken != null && invalidationAtFromToken != null) {
                        final Instant createdAtFromUserInstant = createdAtFromUser.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
                        final Instant invalidationAtFromUserInstant = invalidationAtFromUser.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
                        final Instant createdAtFromTokenInstant = createdAtFromToken.toInstant().truncatedTo(ChronoUnit.SECONDS);
                        final Instant invalidationAtFromTokenInstant = invalidationAtFromToken.toInstant().truncatedTo(ChronoUnit.SECONDS);
                        if (createdAtFromUserInstant.equals(createdAtFromTokenInstant) && invalidationAtFromUserInstant.equals(invalidationAtFromTokenInstant)) {
                            final Map<UUID, Set<Role>> rolesInOrganizations = new HashMap<>();
                            user.getOrganization().forEach(organization -> {
                                final Set<Role> roles = new HashSet<>();
                                final List<String> rolesFromOrganization = organization.getRole();
                                rolesFromOrganization.forEach(role -> roles.add(Role.valueOf(role)));
                                rolesInOrganizations.put(organization.getUuid(), roles);
                            });
                            return new YtiUser(user.getEmail(), user.getFirstName(), user.getLastName(), user.getId(), user.isSuperuser(), user.isNewlyCreated(), createdAtFromUser, invalidationAtFromUser, rolesInOrganizations, null, null);
                        } else {
                            logger.debug("Token validation failed with timestamp info:");
                            logger.debug("createdAtFromUserInstant: " + createdAtFromUserInstant.toString());
                            logger.debug("createdAtFromToken: " + createdAtFromTokenInstant.toString());
                            logger.debug("invalidationAtFromUserInstant: " + invalidationAtFromUserInstant.toString());
                            logger.debug("invalidationAtFromToken: " + invalidationAtFromTokenInstant.toString());
                        }
                    } else {
                        logger.info("Timestamps in either token or user in database are not present with user: " + tokenData.getUserId());
                    }
                } else {
                    logger.info("Token user not found from database with ID: " + tokenData.getUserId());
                }
            }
        } else {
            logger.info("Token does not have user information!");
        }
        return null;
    }

    public List<PublicApiOrganization> rowsToOrganizations(final List<OrganizationRow> rows) {
        return rows.stream().map(row -> {

            final Map<String, String> prefLabel = new HashMap<>(3);
            final Map<String, String> description = new HashMap<>(3);

            prefLabel.put("fi", row.nameFi);
            prefLabel.put("en", row.nameEn);
            prefLabel.put("sv", row.nameSv);

            description.put("fi", row.descriptionFi);
            description.put("en", row.descriptionEn);
            description.put("sv", row.descriptionSv);

            return new PublicApiOrganization(row.id, unmodifiableMap(prefLabel), unmodifiableMap(description), row.url, row.removed, row.parentId);

        }).collect(toList());
    }

    public @NotNull List<PublicApiOrganization> getOrganizations() {

        final List<OrganizationRow> rows = database.findAll(OrganizationRow.class, "select id, name_en, name_sv, name_fi, description_en, description_sv, description_fi, url, removed, parent_id from organization");

        return rowsToOrganizations(rows);
    }

    public @NotNull List<PublicApiOrganization> getValidOrganizations() {

        final List<OrganizationRow> rows = database.findAll(OrganizationRow.class, "select id, name_en, name_sv, name_fi, description_en, description_sv, description_fi, url, removed, parent_id from organization where removed = ?", false);

        return rowsToOrganizations(rows);
    }

    public @NotNull List<PublicApiOrganization> getModifiedOrganizations(final String ifModifiedSince,
                                                                         final boolean onlyValid) {

        final Date date;
        final String[] datePatterns = new String[]{
            DateUtils.PATTERN_ASCTIME,
            DateUtils.PATTERN_RFC1036,
            DateUtils.PATTERN_RFC1123,
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd", };

        try {
            date = DateUtils.parseDate(ifModifiedSince, datePatterns);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        final List<OrganizationRow> rows = database.findAll(OrganizationRow.class, "select id, name_en, name_sv, name_fi, description_en, description_sv, description_fi, url, removed, parent_id from organization where modified > ? AND removed = ?", date, !onlyValid);

        return rowsToOrganizations(rows);
    }

    public void addUserRequest(final UUID userId,
                               final UUID organizationId,
                               final String role) {
        database.update("INSERT INTO request (user_id, organization_id, role_name, sent) VALUES (?,?,?,?)",
            userId, organizationId, role, false);
    }

    public List<PublicApiUserRequest> getUserRequests(final UUID userId) {
        return database.findAll(PublicApiUserRequest.class,
            "SELECT organization_id, array_agg(role_name)\n" +
                "FROM request r \n" +
                "WHERE r.user_id = ? \n" +
                "GROUP BY r.organization_id", userId);
    }

    public boolean removeTempUser(final UUID id) {
        final int modifiedRows = database.update("UPDATE tempuser SET email=?, firstname=?, lastname=?, token_created_at=?, token_invalidation_at=?, removed_at=? WHERE id = ?",
            null, null, null, null, null, LocalDateTime.now(), id);
        if (modifiedRows > 0) {
            return true;
        } else {
            return false;
        }
    }

    public PublicApiOrganization getParentOrganization(final UUID childOrganizationId) {
        OrganizationRow parent = database.findUniqueOrNull(OrganizationRow.class, "select child.id, parent.id, " +
                "parent.name_fi, parent.name_en, parent.name_sv, parent.description_en, parent.description_sv, parent.description_fi, " +
                "parent.removed from organization child " +
                "join organization parent on child.parent_id = parent.id where child.id = ?", childOrganizationId);

        if (parent != null) {
            return rowsToOrganizations(Arrays.asList(parent)).get(0);
        }

        return null;
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
        public UUID parentId;
    }
}
