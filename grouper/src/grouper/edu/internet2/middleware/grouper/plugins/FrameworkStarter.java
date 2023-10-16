package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class used to start up the OSGI framework
 *
 * @author jj
 */
public class FrameworkStarter {
    private final static Log LOG = GrouperUtil.getLog(FrameworkStarter.class);

    private final static FrameworkStarter frameworkStarter = new FrameworkStarter();

    // properties
    public final static String GROUPER_OSGI_ENABLE = "grouper.osgi.enable";
    public final static String GROUPER_OSGI_SECURITY_ENABLE = "grouper.osgi.security.enable";
    public final static String GROUPER_OSGI_FRAMEWORK_TRUST_REPOSITORIES = "grouper.osgi.framework.trust.repositories";
    public final static String GROUPER_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "grouper.osgi.framework.system.packages.extra";
    public final static String GROUPER_OSGI_CACHE_ROOTDIR = "grouper.osgi.cache.rootdir";
    public final static String GROUPER_OSGI_FRAMEWORK_BOOT_DELEGATION = "grouper.osgi.framework.boot.delegation";

    private Framework framework;

    private FrameworkStarter(){}

    public static FrameworkStarter getInstance() {
        return frameworkStarter;
    }

    public void start() {
      if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_OSGI_ENABLE, false)) {
        return;
      }
        Map<String, String> configMap = new HashMap<>();

        // setup osgi security if enabled
        if (GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_OSGI_SECURITY_ENABLE, false)) {
            setupSecurity(configMap);
        }

        configMap.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);

        // if it caches modules, they might not ever get reloaded
        configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        Set<String> frameworkSystemPackagesExtra = new HashSet<>();
        // TODO: add any needed system packages here
        if (null != GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA)) {
            LOG.warn("You are setting a value for `grouper.osgi.framework.system.packages.extra`. This generally not needed and should not be used unless there is a good reason to do so");
            frameworkSystemPackagesExtra.addAll(Arrays.asList(GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, "").split(",")));
        }
        if (!frameworkSystemPackagesExtra.isEmpty()) {
            configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",", frameworkSystemPackagesExtra));
        }

        try {
            // set up cachedir
            Path cacheDir = Files.createTempDirectory("osgi-cache");
            String grouperOsgiCacheDir = GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_CACHE_ROOTDIR, cacheDir.toString());
            configMap.put(Constants.FRAMEWORK_STORAGE, grouperOsgiCacheDir);
        } catch (IOException e) {
            throw new RuntimeException("problem with setting up osgi cache directory", e);
        }

        // usually, this is a bad idea, but we have several classes that must be loaded from the framework classpath to work,
        // e.g., logging, configuration
        Set<String> packagesForBootDelegation = new HashSet<>();
        if (null != GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_FRAMEWORK_BOOT_DELEGATION)) {
            LOG.warn("You are setting a value for `grouper.osgi.framework.boot.delegation`. This is generally not needed adn should not be used unless there is a good reason to do so");
            packagesForBootDelegation.addAll(Arrays.asList(GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_FRAMEWORK_BOOT_DELEGATION).split(",")));
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

            // install security plugin, if needed
            // TODO: currently, we have a custom version of the plugin that fixes a problem with allowing plugins that are signed with untrusted certificates. this should change
            if (GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_OSGI_SECURITY_ENABLE, false)) {
                framework.getBundleContext().installBundle(FrameworkStarter.class.getResource("/plugins/org.apache.felix.framework.security.jar").toString()).start();
            }

            BundleStarter bundleStarter = new BundleStarter(framework.getBundleContext());
            bundleStarter.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    // setup up osgi security cofiguration. This includes setting up a jvm security policy and setting up a truststore
    private static void setupSecurity(Map<String, String> configMap) {
        // TODO: this seems very dangerous, but is also going to be deprecated. look into options, though for now there should be a bit of manual trust as a buffer
        System.setProperty("java.security.policy", FrameworkStarter.class.getResource("/plugins/all.policy").toString());

        configMap.put(Constants.FRAMEWORK_SECURITY, Constants.FRAMEWORK_SECURITY_OSGI);

        // setup trust repositories
        // TODO: look into making this more dynamic. Currently, according to spec, the only required way of setting the trust stores is to set a property with java.io.File.pathSeparator (`:` on unix, `;` on windows) separated list of keystores, with each implementation having the option to providing other mechanisms
        Pattern pattern = Pattern.compile("^grouper\\.osgi\\.truststore\\.([^.]+)\\.certificate$");
        Set<String> trustCertificatesAliases = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
        if (GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_FRAMEWORK_TRUST_REPOSITORIES) != null) {
            configMap.put(Constants.FRAMEWORK_TRUST_REPOSITORIES, GrouperConfig.retrieveConfig().propertyValueString(GROUPER_OSGI_FRAMEWORK_TRUST_REPOSITORIES));
        } else if (!trustCertificatesAliases.isEmpty()) {
            // we have certificates defined in configuration
            try {
                KeyStore keyStore = KeyStore.getInstance("JKS");
                String keyStorePassword = "changeme";
                keyStore.load(null, keyStorePassword.toCharArray());

                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                for (String alias: trustCertificatesAliases) {
                    Certificate certificate = certificateFactory.generateCertificate(new ReaderInputStream(new StringReader(GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.truststore." + alias + ".certificate"))));
                    keyStore.setCertificateEntry(alias, certificate);
                }

                Path keystorePath = Files.createTempFile("keystore", ".jks");
                try (OutputStream os = new FileOutputStream(keystorePath.toFile())) {
                    keyStore.store(os, keyStorePassword.toCharArray());
                    configMap.put(Constants.FRAMEWORK_TRUST_REPOSITORIES, keystorePath.toString());
                }
            } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
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

    public synchronized void stop() throws BundleException {
        if (this.framework != null) {
            this.framework.stop();
            this.framework = null;
        }
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
