package edu.internet2.middleware.grouper.authentication.plugin;

import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import javax.servlet.ServletContainerInitializer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class GrouperAuthentication implements BundleActivator {
    private Map<String, ServiceReference> referenceMap = new HashMap<>();
    private final Map<String, ServiceRegistration<?>> registrationMap = new HashMap<>();

    private static final LogFactory LOG_FACTORY;
    static {
        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(GrouperAuthentication.class).getBundleContext();
            //TODO: figure out why this is weird
            // TODO: check if this can be checked
            @SuppressWarnings("unchecked")
            ServiceReference<LogFactory> logfactoryReference = (ServiceReference<LogFactory>) bundleContext.getAllServiceReferences("org.apache.commons.logging.LogFactory", null)[0];
            bundleContext.getServiceReference("org.apache.commons.logging.LogFactory");
            if (bundleContext.getService(logfactoryReference) != null) {
                LOG_FACTORY = bundleContext.getService(logfactoryReference);
            } else {
                LOG_FACTORY = LogFactory.getFactory();
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static LogFactory getLogFactory() {
        return LOG_FACTORY;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        ExternalAuthenticationServletContainerInitializer externalAuthenticationServletContainerInitializer = new ExternalAuthenticationServletContainerInitializer();
        ServiceRegistration easciRegistration = context.registerService(ServletContainerInitializer.class, externalAuthenticationServletContainerInitializer, new Hashtable<>());
        registrationMap.put(ExternalAuthenticationServletContainerInitializer.class.getCanonicalName(), easciRegistration);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration registration : registrationMap.values()) {
            registration.unregister();
        }
    }
}
