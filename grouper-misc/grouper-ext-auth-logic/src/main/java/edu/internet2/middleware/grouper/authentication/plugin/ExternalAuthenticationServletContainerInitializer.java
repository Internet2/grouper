package edu.internet2.middleware.grouper.authentication.plugin;

import edu.internet2.middleware.grouper.authentication.plugin.filter.CallbackFilterDecorator;
import edu.internet2.middleware.grouper.authentication.plugin.filter.SecurityFilterDecorator;
import org.apache.commons.logging.Log;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class ExternalAuthenticationServletContainerInitializer implements ServletContainerInitializer {
    private static final Log log = GrouperAuthentication.getLogFactory().getInstance(ExternalAuthenticationServletContainerInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        log.info("Initializing plugin security filters for external authentication");
        CallbackFilterDecorator callbackFilterDecorator = new CallbackFilterDecorator();
        FilterRegistration.Dynamic callbackFilter = ctx.addFilter("callbackFilter", callbackFilterDecorator);
        callbackFilter.addMappingForUrlPatterns(null, false, "/*");

        SecurityFilterDecorator securityFilterDecorator = new SecurityFilterDecorator();
        FilterRegistration.Dynamic securityFilter = ctx.addFilter("securityFilter", securityFilterDecorator);
        securityFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
