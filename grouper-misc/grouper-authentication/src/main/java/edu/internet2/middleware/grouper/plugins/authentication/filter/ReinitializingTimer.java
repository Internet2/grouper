package edu.internet2.middleware.grouper.plugins.authentication.filter;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class ReinitializingTimer extends TimerTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReinitializingTimer.class);

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