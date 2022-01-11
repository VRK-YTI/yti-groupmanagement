package fi.vm.yti.groupmanagement.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fi.vm.yti.groupmanagement.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.groupmanagement.dao.PublicApiDao;
import fi.vm.yti.security.YtiUser;

@Service
public class PrivateApiService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateApiService.class);

    private final PublicApiDao publicApiDao;

    @Autowired
    public PrivateApiService(PublicApiDao publicApiDao) {
        this.publicApiDao = publicApiDao;
    }

    @Transactional
    public List<PublicApiUserListItem> getUsers() {
        return this.publicApiDao.getAllUsers();
    }

    @Transactional
    public List<PrivateApiTempUserListItem> getTempUsers() {
        return this.publicApiDao.getAllTempUsers();
    }

    @Transactional
    public List<PublicApiUserListItem> getModifiedUsers(String ifModifiedSince) {
        return this.publicApiDao.getModifiedUsers(ifModifiedSince);
    }

    @Transactional
    public List<PrivateApiTempUserListItem> getModifiedTempUsers(String ifModifiedSince) {
        return this.publicApiDao.getModifiedTempUsers(ifModifiedSince);
    }

    @Transactional
    public @NotNull PublicApiUser getUserById(@NotNull UUID id) {
        return this.publicApiDao.getUserById(id);
    }

    @Transactional
    public @NotNull PublicApiUser getUserOrTempUserById(@NotNull UUID id) {
        return this.publicApiDao.getUserOrTempUserById(id);
    }

    @Transactional
    public @NotNull PublicApiUser getUserByEmail(@NotNull String email) {
        return this.publicApiDao.getUserByEmail(email);
    }

    @Transactional
    public @NotNull PublicApiUser getOrCreateUser(@NotNull final String email,
                                                  @NotNull final String firstName,
                                                  @NotNull final String lastName) {

        final PublicApiUser user = publicApiDao.findUserByEmail(email);

        if (user != null) {
            return user;
        } else {
            UUID id = UUID.randomUUID();
            logger.info("Creating new user with ID: " + id.toString());
            return publicApiDao.createUser(email, firstName, lastName, id);
        }
    }

    @Transactional
    public @NotNull List<PublicApiUser> getOrCreateTempUsers(final String containerUri,
                                                             final List<TempUser> tempUsers) {
        if (tempUsers != null) {
            tempUsers.forEach(tempUser -> {
                if (tempUser.containerUri == null) {
                    tempUser.containerUri = containerUri;
                }
                if (!tempUser.containerUri.equalsIgnoreCase(containerUri)) {
                    throw new RuntimeException("Temp user container uri: " + tempUser.containerUri + " does not match to: " + containerUri);
                }
            });
        }
        deleteRemovedUsers(containerUri, tempUsers);
        final List<PublicApiUser> tempUserResults = new ArrayList<>();
        if (tempUsers != null) {
            tempUsers.forEach(tempUser -> {
                final PublicApiUser user;
                if (tempUser.id != null) {
                    user = publicApiDao.findTempUserById(tempUser.id);
                } else {
                    user = null;
                }
                if (user != null) {
                    tempUserResults.add(user);
                } else {
                    UUID id = UUID.randomUUID();
                    tempUser.id = id;
                    logger.info("Creating new temp user with ID: " + id.toString());
                    tempUserResults.add(publicApiDao.createTempUser(tempUser));
                }
            });
        }
        return tempUserResults;
    }

    private void deleteRemovedUsers(final String containerUri,
                                    final List<TempUser> tempUsers) {
        final List<PrivateApiTempUserListItem> existingTempUsers = publicApiDao.getAllTempUsersForContainerUri(containerUri);
        final Set<PrivateApiTempUserListItem> usersToBeDeleted = new HashSet<>();
        existingTempUsers.forEach(currentUser -> {
            boolean match = false;
            if (tempUsers != null) {
                for (final TempUser tempUser : tempUsers) {
                    if (tempUser.id != null && tempUser.id.equals(currentUser.getId())) {
                        match = true;
                        break;
                    }
                }
            }
            if (!match) {
                usersToBeDeleted.add(currentUser);
            }
        });
        if (!usersToBeDeleted.isEmpty()) {
            usersToBeDeleted.forEach(userToBeDeleted -> {
                publicApiDao.removeTempUser(userToBeDeleted.getId());
            });
        }
    }

    @Transactional
    public void addUserRequest(final UUID userId,
                               final UUID organizationId,
                               final String role) {
        publicApiDao.addUserRequest(userId, organizationId, role);
    }

    @Transactional
    public List<PublicApiUserRequest> getUserRequests(final UUID userId) {
        return this.publicApiDao.getUserRequests(userId);
    }

    @Transactional
    public YtiUser validateToken(final TokenModel token) {
        return this.publicApiDao.validateToken(token);
    }

    @Transactional
    public PublicApiOrganization getParentOrganization(final UUID childOrganizationId) {
        return this.publicApiDao.getParentOrganization(childOrganizationId);
    }

    @Transactional
    public List<PublicApiOrganization> getChildOrganizations(UUID parentId) {
        return this.publicApiDao.getChildOrganizations(parentId);
    }
}
