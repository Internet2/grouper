package edu.internet2.middleware.grouper.plugin.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.servlet.ServletContainerInitializer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class TestPlugin implements BundleActivator {
    private Map<String, ServiceRegistration> registrationMap = new HashMap<>();

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        TestPluginServletContainerInitializer initializer = new TestPluginServletContainerInitializer();
        ServiceRegistration registration = bundleContext.registerService(ServletContainerInitializer.class, initializer, new Hashtable<>());
        registrationMap.put(TestPluginServletContainerInitializer.class.getCanonicalName(), registration);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        registrationMap.values().forEach(x -> { x.unregister(); } );
    }
}
