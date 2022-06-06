package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Class used to start up the OSGI framework
 *
 * @author jj
 */
public class FrameworkStarter {
    private final static FrameworkStarter frameworkStarter = new FrameworkStarter();

    private Framework framework;

    private FrameworkStarter(){}

    public static FrameworkStarter getInstance() {
        return frameworkStarter;
    }

    public void start() {
        Map<String, String> configMap = new HashMap<>();

        configMap.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);

        // if it caches modules, they might not ever get reloaded
        configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        //TODO: maybe make this more dynamic. currently we're very opinionated on what we export
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "javax.servlet");

        // set up cachedir
        String grouperOsgiCacheDir = GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.cache.rootdir", "/opt/grouper/grouperWebapp/WEB-INF/grouperFelixCache");
        configMap.put(Constants.FRAMEWORK_STORAGE, grouperOsgiCacheDir);

        // usually, this is a bad idea, but we have several classes that must be loaded from the framework classpath to work,
        // e.g., logging, configuration
        Set<String> packagesForBootDelegation = new HashSet<>();
        packagesForBootDelegation.add(LogFactory.class.getPackage().getName());
        packagesForBootDelegation.add(ConfigPropertiesCascadeBase.class.getPackage().getName());
        configMap.put(Constants.FRAMEWORK_BOOTDELEGATION, String.join(",", packagesForBootDelegation));

        try {
            FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
            framework = frameworkFactory.newFramework(configMap);
            framework.start();

            this.registerConfigurationServices();

            BundleStarter bundleStarter = new BundleStarter(framework.getBundleContext());
            bundleStarter.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerConfigurationServices() {
        BundleContext context = framework.getBundleContext();

        try {
            context.registerService(LogFactory.class, LogFactory.getFactory(), new Hashtable<>());
            context.registerService(ConfigPropertiesCascadeBase.class, GrouperConfig.retrieveConfig(), buildSimpleDictionary("type", "grouper"));
            context.registerService(ConfigPropertiesCascadeBase.class, GrouperHibernateConfig.retrieveConfig(), buildSimpleDictionary("type", "hibernate"));
            if (GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false)) {
                context.registerService(ConfigPropertiesCascadeBase.class, (ConfigPropertiesCascadeBase) Class.forName("edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi").getMethod("retrieveConfig").invoke(null), buildSimpleDictionary("type", "ui"));
            }
            if (GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws", false)) {
                context.registerService(ConfigPropertiesCascadeBase.class, (ConfigPropertiesCascadeBase) Class.forName("edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi").getMethod("retrieveConfig").invoke(null), buildSimpleDictionary("type", "ws"));
            }
            if (GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.daemon", false)) {
                context.registerService(ConfigPropertiesCascadeBase.class, (ConfigPropertiesCascadeBase) Class.forName("edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig").getMethod("retrieveConfig").invoke(null), buildSimpleDictionary("type", "daemon"));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("There is a problem with your configuration. Things will not work", e);
        }
    }

    private static Dictionary buildSimpleDictionary(String key, String value) {
        Map tMap = new HashMap();
        tMap.put(key, value);
        return FrameworkUtil.asDictionary(tMap);
    }

    public Framework getFramework() {
        return this.framework;
    }
}
