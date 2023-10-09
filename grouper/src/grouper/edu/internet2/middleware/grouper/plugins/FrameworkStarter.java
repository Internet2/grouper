package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
    private final static Log LOG = GrouperUtil.getLog(FrameworkStarter.class);

    private final static FrameworkStarter frameworkStarter = new FrameworkStarter();

    private Framework framework;

    private FrameworkStarter(){}

    public static FrameworkStarter getInstance() {
        return frameworkStarter;
    }

    public void start() {
      if (!GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.osgi.enable", false)) {
        return;
      }
        Map<String, String> configMap = new HashMap<>();

        configMap.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);

        // if it caches modules, they might not ever get reloaded
        configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        Set<String> frameworkSystemPackagesExtra = new HashSet<>();
        // TODO: add any needed system packages here
        if (null != GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.framework.system.packages.extra")) {
            LOG.warn("You are setting a value for `grouper.osgi.framework.system.packages.extra`. This generally not needed and should not be used unless there is a good reason to do so");
            frameworkSystemPackagesExtra.addAll(Arrays.asList(GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.framework.system.packages.extra", "").split(",")));
        }
        if (!frameworkSystemPackagesExtra.isEmpty()) {
            configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",", frameworkSystemPackagesExtra));
        }

        try {
            // set up cachedir
            Path cacheDir = Files.createTempDirectory("osgi-cache");
            String grouperOsgiCacheDir = GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.cache.rootdir", cacheDir.toString());
            configMap.put(Constants.FRAMEWORK_STORAGE, grouperOsgiCacheDir);
        } catch (IOException e) {
            throw new RuntimeException("problem with setting up osgi cache directory", e);
        }

        // usually, this is a bad idea, but we have several classes that must be loaded from the framework classpath to work,
        // e.g., logging, configuration
        Set<String> packagesForBootDelegation = new HashSet<>();
        if (null != GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.framework.boot.delegation")) {
            LOG.warn("You are setting a value for `grouper.osgi.framework.boot.delegation`. This is generally not needed adn should not be used unless there is a good reason to do so");
            packagesForBootDelegation.addAll(Arrays.asList(GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.framework.boot.delegation").split(",")));
        } else {
            packagesForBootDelegation.add("org.osgi.*");
            packagesForBootDelegation.add("javax.*");
            packagesForBootDelegation.add("org.apache.commons.logging");
            packagesForBootDelegation.add("edu.internet2.middleware.grouper.*");
            packagesForBootDelegation.add("edu.internet2.middleware.grouperClient.*");
        }
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

    /**
     * register various Grouper services
     */
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
    
//    public void stop() {
//      if (!started) {
//        return;
//      }
//      started = false;
//      try {
//        FrameworkStarter.getInstance().getFramework().waitForStop(20000);
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }
//
//    }
    
}
