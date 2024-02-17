package edu.internet2.middleware.grouper.authentication.plugin.filter;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.ExternalAuthenticationServletContainerInitializer;
import edu.internet2.middleware.grouper.authentication.plugin.GrouperAuthentication;
import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class ReinitializingTimer extends TimerTask {
    private static final Log LOGGER = GrouperAuthentication.getLogFactory().getInstance(ExternalAuthenticationServletContainerInitializer.class);

    private Map<String, String> config;
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
        Map<String, String> curConfig = ConfigUtils.getBestGrouperConfiguration().propertiesMap(Pattern.compile("^external\\.authentication\\.([^.]+)$"));
        if (!areEqual(config, curConfig)) {
            config = curConfig;
            initTarget.initDecorator();
            LOGGER.info("Pac4j External Authentication configuration reloaded");
        }
    }
}
