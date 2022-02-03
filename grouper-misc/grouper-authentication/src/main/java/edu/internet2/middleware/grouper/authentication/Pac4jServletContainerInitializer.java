package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.filter.CallbackFilterFacade;
import edu.internet2.middleware.grouper.authentication.filter.SecurityFilterFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class Pac4jServletContainerInitializer implements ServletContainerInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jServletContainerInitializer.class);
    private Timer timer = new Timer();
    private Map config;

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        LOGGER.info("initializing pac4j");

        CallbackFilterFacade callbackFilterFacade = new CallbackFilterFacade();
        FilterRegistration.Dynamic callbackFilter = ctx.addFilter("callbackFilter", callbackFilterFacade);
        callbackFilter.addMappingForUrlPatterns(null, false, "/*");

        SecurityFilterFacade securityFilterFacade = new SecurityFilterFacade();
        FilterRegistration.Dynamic securityFilter = ctx.addFilter("securityFilter", SecurityFilterFacade.class);
        securityFilter.addMappingForUrlPatterns(null, false, "/*");

        this.config = ConfigUtils.getBestGrouperConfiguration().propertiesMap(Pattern.compile("^external\\.authentication\\.([^.]+)$"));

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Map curConfig = ConfigUtils.getBestGrouperConfiguration().propertiesMap(Pattern.compile("^external\\.authentication\\.([^.]+)$"));
                if (!areEqual(config, curConfig)) {
                    config = curConfig;
                    callbackFilterFacade.initDelegate();
                    securityFilterFacade.initDelegate();
                    LOGGER.info("Pac4j External Authentication configuration reloaded");
                }
            }
        };

        int period = ConfigUtils.getBestGrouperConfiguration().propertyValueInt("external.authentication.config.reload.milliseconds", 60 * 1000);
        this.timer.schedule(timerTask, period, period);
    }

    private boolean areEqual(Map<String, String> first, Map<String, String> second) {
        if (first.size() != second.size()) {
            return false;
        }
        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }
}
