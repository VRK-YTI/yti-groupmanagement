package fi.vm.yti.groupmanagement.controller;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.yti.groupmanagement.model.PublicApiOrganization;
import fi.vm.yti.groupmanagement.model.PublicApiUser;
import fi.vm.yti.groupmanagement.model.PublicApiUserListItem;
import fi.vm.yti.groupmanagement.service.PublicApiService;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/public-api")
public class PublicApiController {

    private static final Logger logger = LoggerFactory.getLogger(PublicApiController.class);
    private final PublicApiService publicApiService;

    public PublicApiController(PublicApiService publicApiService) {
        this.publicApiService = publicApiService;
    }

    @RequestMapping(value = "/user", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public PublicApiUser getUserByEmail(@RequestBody final NewUser newUser) {
        logger.info("POST /user requested");

        if (newUser.email.isEmpty()) {
            throw new RuntimeException("Email is a mandatory parameter");
        }

        if (newUser.firstName != null && newUser.lastName != null) {
            return this.publicApiService.getOrCreateUser(newUser.email, newUser.firstName, newUser.lastName);
        } else {
            return this.publicApiService.getUserByEmail(newUser.email);
        }
    }

    @RequestMapping(value = "/user", method = GET, produces = APPLICATION_JSON_VALUE, params = "id")
    public PublicApiUser findUserById(@RequestParam @NotNull final UUID id) {

        logger.info("GET /user requested");

        PublicApiUser user = this.publicApiService.findUserById(id);

        if (user == null) {
            throw new UserNotFoundException(id);
        }

        return user;
    }

    @RequestMapping(value = "/users", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PublicApiUserListItem>> getUsers(@RequestHeader(value = "If-Modified-Since", required = false) final String ifModifiedSince) {
        logger.info("GET /users requested");

        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            List<PublicApiUserListItem> users = publicApiService.getModifiedUsers(ifModifiedSince);
            if (users.size() > 0) {
                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(users, HttpStatus.NOT_MODIFIED);
            }
        } else return new ResponseEntity<>(this.publicApiService.getUsers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/organizations", method = GET, produces = APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<List<PublicApiOrganization>> getOrganizations(@RequestParam(value = "onlyValid", required = false, defaultValue = "false") final boolean onlyValid,
                                                                        @RequestParam(value = "ifModifiedSince", required = false) final String ifModifiedSinceParam,
                                                                        @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
        logger.info("GET /organizations requested");

        if (ifModifiedSince == null && ifModifiedSinceParam != null && !ifModifiedSinceParam.isEmpty())
            ifModifiedSince = ifModifiedSinceParam;

        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            List<PublicApiOrganization> organizations = publicApiService.getModifiedOrganizations(ifModifiedSince, onlyValid);
            if (organizations.size() > 0) {
                return new ResponseEntity<>(organizations, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(organizations, HttpStatus.NOT_MODIFIED);
            }
        } else
            return new ResponseEntity<>(onlyValid ? publicApiService.getValidOrganizations() : publicApiService.getOrganizations(), HttpStatus.OK);
    }
}

class NewUser {

    public String email;
    public String firstName;
    public String lastName;
}
