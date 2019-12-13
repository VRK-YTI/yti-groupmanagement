package fi.vm.yti.groupmanagement.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class DefaultController {

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
        return new ClassPathResource("/static/index.html");
    }
}
