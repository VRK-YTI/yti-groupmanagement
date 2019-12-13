package fi.vm.yti.groupmanagement.filter;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fi.vm.yti.security.AuthenticatedUserProvider;

@Component
public class RequestLoggingInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Autowired
    private AuthenticatedUserProvider userProvider;

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        MDC.put("startTime", String.valueOf(System.currentTimeMillis()));
        LOG.debug("*** Start request logging ***");
        LOG.debug("Resource: {}", request.getRequestURI());
        LOG.debug("Method: {}", request.getMethod());
        if (!userProvider.getUser().isAnonymous()) {
            LOG.debug("User: {}", userProvider.getUser().getId());
        } else {
            LOG.debug("User: anonymous");
        }
        logQueryParameters(request);
        logRequestHeaders(request);
        return true;
    }

    private void logQueryParameters(final HttpServletRequest requestContext) {
        LOG.debug("*** Start query parameters section of request ***");
        requestContext.getParameterMap().keySet().forEach(parameterName -> {
            final String paramValue = requestContext.getParameter(parameterName);
            LOG.debug("Parameter: {}, Value: {}", parameterName, paramValue);
        });
        LOG.debug("*** End query parameters section of request ***");
    }

    private void logRequestHeaders(final HttpServletRequest requestContext) {
        LOG.debug("*** Start header section of request ***");
        LOG.debug("Method type: {}", requestContext.getMethod());
        final Enumeration<String> headerNames = requestContext.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final String headerValue;
            if ("Authorization".equalsIgnoreCase(headerName) || "cookie".equalsIgnoreCase(headerName)) {
                headerValue = "[PROTECTED]";
            } else {
                headerValue = requestContext.getHeader(headerName);
            }
            if ("User-Agent".equalsIgnoreCase(headerName)) {
                MDC.put("userAgent", headerValue);
            } else if ("Host".equalsIgnoreCase(headerName)) {
                MDC.put("host", headerValue);
            }
            LOG.debug("Header: {}, Value: {} ", headerName, headerValue);
        }
        LOG.debug("*** End header section of request ***");
    }

    @Override
    public void afterCompletion(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final Object handler,
                                final Exception ex) {
        final Long executionTime = getExecutionTime();
        if (executionTime == null) {
            return;
        }
        LOG.debug("Request execution time: {} ms", executionTime);
        LOG.debug("*** End request logging ***");
        logRequestInfo(request, response, executionTime);
        MDC.clear();
    }

    private Long getExecutionTime() {
        final String startTimeString = MDC.get("startTime");
        if (startTimeString != null && !startTimeString.isEmpty()) {
            final long startTime = Long.parseLong(startTimeString);
            return System.currentTimeMillis() - startTime;
        }
        return null;
    }

    private void logRequestInfo(final HttpServletRequest requestContext,
                                final HttpServletResponse responseContext,
                                final long executionTime) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Request: /");
        builder.append(requestContext.getMethod());
        builder.append(" ");
        builder.append(requestContext.getRequestURI());
        builder.append(", ");
        builder.append("Status: ");
        builder.append(responseContext.getStatus());
        builder.append(", ");
        builder.append("User-Agent: ");
        builder.append(MDC.get("userAgent"));
        builder.append(", ");
        builder.append("User: ");
        if (!userProvider.getUser().isAnonymous()) {
            builder.append(userProvider.getUser().getId());
        } else {
            builder.append("anonymous");
        }
        builder.append(", ");
        builder.append("Host: ");
        builder.append(MDC.get("host"));
        builder.append(", ");
        builder.append("Time: ");
        builder.append(executionTime);
        builder.append(" ms");
        LOG.info(builder.toString());
    }
}
