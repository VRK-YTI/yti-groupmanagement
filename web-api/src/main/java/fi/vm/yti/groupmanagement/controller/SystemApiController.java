package fi.vm.yti.groupmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.yti.groupmanagement.model.SystemCountModel;
import fi.vm.yti.groupmanagement.service.SystemApiService;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/system")
public class SystemApiController {

    private SystemApiService systemApiService;

    @Autowired
    public SystemApiController(SystemApiService systemApiService) {
        this.systemApiService = systemApiService;
    }

    @GetMapping(path = "counts", produces = APPLICATION_JSON_VALUE)
    SystemCountModel counts() {
        return systemApiService.countThings();
    }
}
