package fi.vm.yti.groupmanagement.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.groupmanagement.dao.PublicApiDao;
import fi.vm.yti.groupmanagement.model.PublicApiUserListItem;
import fi.vm.yti.groupmanagement.model.PublicApiUserRequest;
import fi.vm.yti.groupmanagement.model.TokenModel;
import fi.vm.yti.security.YtiUser;

@Service
public class PrivateApiService {

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
    public List<PublicApiUserListItem> getModifiedUsers(String ifModifiedSince) {
        return this.publicApiDao.getModifiedUsers(ifModifiedSince);
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
}
