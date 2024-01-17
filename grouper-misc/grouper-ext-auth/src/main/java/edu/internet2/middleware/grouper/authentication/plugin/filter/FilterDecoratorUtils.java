package edu.internet2.middleware.grouper.authentication.plugin.filter;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;

public class FilterDecoratorUtils {
    protected static boolean isExternalAuthenticationEnabled() {
        return ConfigUtils.getBestGrouperConfiguration().propertyValueBoolean("grouper.is.extAuth.enabled", false);
    }

}