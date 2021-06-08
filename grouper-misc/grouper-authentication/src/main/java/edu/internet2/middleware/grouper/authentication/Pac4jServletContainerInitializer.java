package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.filter.CallbackFilterFascade;
import edu.internet2.middleware.grouper.authentication.filter.SecurityFilterFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class Pac4jServletContainerInitializer implements ServletContainerInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jServletContainerInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        LOGGER.info("initializing pac4j");

        FilterRegistration.Dynamic callbackFilter = ctx.addFilter("callbackFilter", CallbackFilterFascade.class);
        callbackFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic securityFilter = ctx.addFilter("securityFilter", SecurityFilterFacade.class);
        securityFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
