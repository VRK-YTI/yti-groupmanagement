package fi.vm.yti.groupmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {

    UserNotFoundException(final UUID userId) {
        super("User not found by id: " + userId);
    }
}
