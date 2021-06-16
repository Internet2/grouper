package edu.internet2.middleware.grouper.authentication.filter;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;

import javax.servlet.http.HttpServletRequest;

public class FilterFascadeUtils {
    protected static String getRequestPathInContext(HttpServletRequest request) {
        return request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
    }

    protected static boolean isExternalAuthenticationEnabled() {
        return ConfigUtils.getBestGrouperConfiguration().propertyValueBoolean("external.authentication.enabled", false);
    }

    protected static boolean isRunUi() {
        return GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);
    }
}
