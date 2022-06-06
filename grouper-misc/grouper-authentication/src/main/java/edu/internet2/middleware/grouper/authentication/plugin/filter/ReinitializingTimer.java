package edu.internet2.middleware.grouper.authentication.plugin.filter;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.ExternalAuthenticationServletContainerInitializer;
import edu.internet2.middleware.grouper.authentication.plugin.Pac4jConfigFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class ReinitializingTimer extends TimerTask {
    private static final Log LOGGER;
    static {
        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(Pac4jConfigFactory.class).getBundleContext();
            //TODO: figure out why this is weird
            ServiceReference<LogFactory> logfactoryReference = (ServiceReference<LogFactory>) bundleContext.getAllServiceReferences("org.apache.commons.logging.LogFactory", null)[0];
            LOGGER = bundleContext.getService(logfactoryReference).getInstance(ExternalAuthenticationServletContainerInitializer.class);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Map config;
    private final Reinitializable initTarget;

    public ReinitializingTimer(Reinitializable initTarget) {
        this.initTarget = initTarget;
        config = ConfigUtils.getBestGrouperConfiguration().propertiesMap(Pattern.compile("^external\\.authentication\\.([^.]+)$"));
    }

    protected static boolean areEqual(Map<String, String> first, Map<String, String> second) {
        if (first.size() != second.size()) {
            return false;
        }
        return first.entrySet().stream().allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }

    @Override
    public void run() {
        Map curConfig = ConfigUtils.getBestGrouperConfiguration().propertiesMap(Pattern.compile("^external\\.authentication\\.([^.]+)$"));
        if (!areEqual(config, curConfig)) {
            config = curConfig;
            initTarget.initDecorator();
            LOGGER.info("Pac4j External Authentication configuration reloaded");
        }
    }
}
