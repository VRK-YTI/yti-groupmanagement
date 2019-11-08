package fi.vm.yti.groupmanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.yti.groupmanagement.model.PublicApiUserListItem;
import fi.vm.yti.groupmanagement.model.TokenModel;
import fi.vm.yti.groupmanagement.service.PrivateApiService;
import fi.vm.yti.security.YtiUser;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/private-api")
public class PrivateApiController {

    private static final Logger logger = LoggerFactory.getLogger(PrivateApiController.class);
    private final PrivateApiService privateApiService;

    public PrivateApiController(PrivateApiService PrivateApiService) {
        this.privateApiService = PrivateApiService;
    }

    @RequestMapping(value = "/users", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PublicApiUserListItem>> getUsers(@RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
        logger.info("GET /users requested");

        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            List<PublicApiUserListItem> users = this.privateApiService.getModifiedUsers(ifModifiedSince);
            if (users.size() > 0) {
                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(users, HttpStatus.NOT_MODIFIED);
            }
        } else return new ResponseEntity<>(this.privateApiService.getUsers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/validate", method = POST, produces = APPLICATION_JSON_VALUE)
    public YtiUser validateUserToken(@RequestBody TokenModel token) {
        logger.info("POST /validate requested");

        if (token != null && token.token != null && !token.token.isEmpty()) {
            return this.privateApiService.validateToken(token);
        } else {
            throw new RuntimeException("No token present in validation API!");
        }
    }
}
