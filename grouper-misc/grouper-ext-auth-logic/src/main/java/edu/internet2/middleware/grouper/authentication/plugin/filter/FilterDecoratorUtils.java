package edu.internet2.middleware.grouper.authentication.plugin.filter;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.GrouperAuthentication;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FilterDecoratorUtils {
    private static final Log log = GrouperAuthentication.getLogFactory().getInstance(FilterDecoratorUtils.class);

    private static final String EXCEPTION_MESSAGE_TOKEN = "%%exceptionMessage%%";

    private static final String DEFAULT_LOGIN_FAILURE_MESSAGE = "Login failed. Please try again or contact your system administrator.";

    public static boolean isExternalAuthenticationEnabled() {
        return ConfigUtils.getBestGrouperConfiguration().propertyValueBoolean("grouper.is.extAuth.enabled", false);
    }

    /**
     * Log the exception with its stacktrace and send a plain HTTP 500 with a short message, so that
     * the stacktrace is not presented to the user (CWE-209).  The message text may optionally contain
     * %%exceptionMessage%%, which is replaced with the exception message.
     */
    public static void handleLoginFailure(Exception e, HttpServletResponse response) throws IOException {
        log.error("External authentication failed", e);
        if (response.isCommitted()) {
            return;
        }
        String message = null;
        try {
            message = GrouperTextContainer.textOrNull("externalAuthenticationLoginFailed");
        } catch (Throwable textException) {
            // never let a text lookup problem (including a failed static initializer) bring back the stacktrace
            log.debug("Unable to look up externalAuthenticationLoginFailed text", textException);
        }
        if (message == null || message.trim().length() == 0) {
            message = DEFAULT_LOGIN_FAILURE_MESSAGE;
        }
        message = substituteExceptionMessage(message, e);
        response.resetBuffer();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    /**
     * replace the optional %%exceptionMessage%% token with the exception message
     */
    public static String substituteExceptionMessage(String message, Exception e) {
        if (!message.contains(EXCEPTION_MESSAGE_TOKEN)) {
            return message;
        }
        return message.replace(EXCEPTION_MESSAGE_TOKEN, e.getMessage() == null ? "" : e.getMessage());
    }

}
