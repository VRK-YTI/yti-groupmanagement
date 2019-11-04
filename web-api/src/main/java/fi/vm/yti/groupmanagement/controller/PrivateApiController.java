package fi.vm.yti.groupmanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.yti.groupmanagement.model.PublicApiUserListItem;
import fi.vm.yti.groupmanagement.service.PrivateApiService;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/private-api")
public class PrivateApiController {

    private static final Logger logger = LoggerFactory.getLogger(PrivateApiController.class);
    private final PrivateApiService PrivateApiService;

    public PrivateApiController(PrivateApiService PrivateApiService) {
        this.PrivateApiService = PrivateApiService;
    }

    @RequestMapping(value = "/users", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PublicApiUserListItem>> getUsers(@RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
        logger.info("GET /users requested");

        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            List<PublicApiUserListItem> users = PrivateApiService.getModifiedUsers(ifModifiedSince);
            if (users.size() > 0) {
                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(users, HttpStatus.NOT_MODIFIED);
            }
        } else return new ResponseEntity<>(this.PrivateApiService.getUsers(), HttpStatus.OK);
    }
}
