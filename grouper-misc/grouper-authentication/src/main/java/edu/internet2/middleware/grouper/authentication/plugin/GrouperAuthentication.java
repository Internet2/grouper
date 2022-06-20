package edu.internet2.middleware.grouper.authentication.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import javax.servlet.ServletContainerInitializer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class GrouperAuthentication implements BundleActivator {
    private Map<String, ServiceReference> referenceMap = new HashMap<>();
    private Map<String, ServiceRegistration> registrationMap = new HashMap<>();

    @Override
    public void start(BundleContext context) throws Exception {
        ExternalAuthenticationServletContainerInitializer externalAuthenticationServletContainerInitializer = new ExternalAuthenticationServletContainerInitializer(context);
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
