package edu.internet2.middleware.grouper.plugins.authentication.filter;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;

import java.util.Map;

public class FilterDecoratorUtils {
    protected static boolean isExternalAuthenticationEnabled() {
        return ConfigUtils.getBestGrouperConfiguration().propertyValueBoolean("grouper.is.extAuth.enabled", false);
    }

}