package fi.vm.yti.groupmanagement.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static fi.vm.yti.groupmanagement.util.RequestUtil.createLoginUrl;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
public class ShibbolethErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ShibbolethErrorController.class);
    @Value("${registration.url}")
    private String registrationUrl;

    private static boolean isSignUpMissing(@Nullable final String errorType,
                                           @Nullable final String statusCode,
                                           @Nullable final String statusCode2) {
        return "opensaml::FatalProfileException".equals(errorType) &&
            "urn:oasis:names:tc:SAML:2.0:status:RequestDenied".equals(statusCode2) ||
            "opensaml::FatalProfileException".equals(errorType) &&
                "urn:oasis:names:tc:SAML:2.0:status:Responder".equals(statusCode);
    }

    @RequestMapping(value = "/login-error", method = RequestMethod.GET, produces = TEXT_HTML_VALUE)
    String loginError(HttpServletRequest request,
                      @RequestParam(required = false) @Nullable final String now,
                      @RequestParam(required = false) @Nullable final String requestURL,
                      @RequestParam(required = false) @Nullable final String errorType,
                      @RequestParam(required = false) @Nullable final String errorText,
                      @RequestParam(name = "RelayState", required = false) @Nullable final String relayState,
                      @RequestParam(required = false) @Nullable final String entityID,
                      @RequestParam(required = false) @Nullable final String statusCode,
                      @RequestParam(required = false) @Nullable final String statusCode2,
                      final Map<String, Object> model) {
        logger.info("loginError, requestURL: " + requestURL + ", errorType: " + errorType + " and errorText: " + errorText);
        final boolean signUpMissing = isSignUpMissing(errorType, statusCode, statusCode2);
        model.put("missingSignUp", signUpMissing);
        model.put("genericError", !signUpMissing);
        model.put("registrationUrl", registrationUrl);
        if (relayState != null) {
            model.put("goBackUrl", createLoginUrl(request, relayState));
        }
        model.put("now", now);
        model.put("requestURL", requestURL);
        model.put("errorType", errorType);
        model.put("errorText", errorText);
        model.put("relayState", relayState);
        model.put("entityID", entityID);
        model.put("statusCode", statusCode);
        model.put("statusCode2", statusCode2);
        return "loginError";
    }
}
