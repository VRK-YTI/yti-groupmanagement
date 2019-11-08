package fi.vm.yti.groupmanagement.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static @NotNull String createLoginUrl(@NotNull final HttpServletRequest request,
                                                 @NotNull final String url) {
        return getRequestUrlExcludingPath(request) + "/Shibboleth.sso/Login?target=" + urlEncode(url);
    }

    private static @NotNull String getRequestUrlExcludingPath(@NotNull final HttpServletRequest req) {

        final String scheme = req.getScheme();
        final String serverName = req.getServerName();
        final int serverPort = req.getServerPort();

        final StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }

        return url.toString();
    }

    private static @NotNull String urlEncode(@NotNull final String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
