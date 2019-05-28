package fi.vm.yti.groupmanagement.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
class DefaultController {

    private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);

    // The paths here match the routes in frontend
    // It's annoying to have to define these in two places, but it works for now.
    @GetMapping(
            value = {
                    "/",
                    "/newOrganization",
                    "/organization/{id:[\\d\\w-]+}",
                    "/users"
            },
            produces = "text/html; charset=UTF-8")
    @ResponseBody
    ClassPathResource defaultPage() {
        logger.info("GET defautPage requested");
        return new ClassPathResource("/static/index.html");
    }
}
