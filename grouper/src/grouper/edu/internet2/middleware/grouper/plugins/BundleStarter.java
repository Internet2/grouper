package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BundleStarter {
    private static final Log LOG = GrouperUtil.getLog(BundleStarter.class);
    private final BundleContext bundleContext;

    private final String bundleDirWithLastSlash;

    private final Map<String, Bundle> installedBundles;

    public BundleStarter(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.installedBundles = new HashMap<>();

        try {
            Path tmpDir = Files.createTempDirectory("grouper");
            bundleDirWithLastSlash = GrouperUtil.stripLastSlashIfExists(GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.jar.dir", tmpDir.toString())) + "/";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        Map<String, Map<Class<?>, Set<String>>> pluginJarNameToInterfaceToImplementationClasses = new HashMap<String, Map<Class<?>, Set<String>>>();

        // TODO: deprecate
        {
            Pattern pattern = Pattern.compile("^grouperOsgiPlugin\\.([^.]+)\\.jarName$");
            Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
            if (GrouperUtil.length(configIds) > 0) {
                for (String configId: configIds) {
                    LOG.warn("DEPRECATION WARNING: you are using older configuration in the namespace `grouperOsgiPlugin`; this will be removed in future versions. Update configuration to use `grouper.osgi.plugin`");
                    String jarName = GrouperConfig.retrieveConfig().propertyValueString("grouperOsgiPlugin." + configId + ".jarName");
                    Bundle bundle = null;
                    try {
                        bundle = bundleContext.installBundle("file:" + bundleDirWithLastSlash + jarName);
                        bundle.start();
                        installedBundles.put(configId, bundle);
                    } catch (BundleException e) {
                        LOG.error("Problem installing plugin: " + jarName, e);
                    }
                }
            }
        }

        Pattern pattern = Pattern.compile("^grouper\\.osgi\\.plugin\\.([^.]+)\\.location$");
        Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
        if (GrouperUtil.length(configIds) > 0) {
            for (String configId: configIds) {
                String location = GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.plugin." + configId + ".location");
                try {
                    Bundle bundle = bundleContext.installBundle(location);
                    bundle.start();
                    installedBundles.put(configId, bundle);
                } catch (BundleException e) {
                    LOG.error("Problem installing plugin: " + location, e);
                }
            }
        }
    }
}
