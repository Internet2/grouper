package edu.internet2.middleware.grouper.authentication.plugin;

import edu.internet2.middleware.grouper.authentication.plugin.filter.CallbackFilterDecorator;
import edu.internet2.middleware.grouper.authentication.plugin.filter.SecurityFilterDecorator;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.j2ee.servlet.filter.PluginFilterDelegate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class ExternalAuthenticationServletContainerInitializer implements ServletContainerInitializer {
    private final Log log;

    public ExternalAuthenticationServletContainerInitializer(BundleContext bundleContext) {
        try {
            //TODO: figure out why this is weird
            ServiceReference<LogFactory> logfactoryReference = (ServiceReference<LogFactory>) bundleContext.getAllServiceReferences("org.apache.commons.logging.LogFactory", null)[0];
            log = bundleContext.getService(logfactoryReference).getInstance(ExternalAuthenticationServletContainerInitializer.class);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

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
