package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BundleStarter {
    private static final Log LOG = GrouperUtil.getLog(BundleStarter.class);
    private final BundleContext bundleContext;

    private final String bundleDirWithLastSlash;

    public BundleStarter(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        bundleDirWithLastSlash = GrouperUtil.stripLastSlashIfExists(GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.jar.dir", "/opt/grouper/grouperWebapp/WEB-INF/grouperPlugins")) + File.separator;
    }

    public void start() {
        Map<String, Map<Class<?>, Set<String>>> pluginJarNameToInterfaceToImplementationClasses = new HashMap<String, Map<Class<?>, Set<String>>>();

        Pattern pattern = Pattern.compile("^grouperOsgiPlugin\\.([^.]+)\\.jarName$");
        Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
        if (GrouperUtil.length(configIds) > 0) {
            for (String configId: configIds) {
                String jarName = GrouperConfig.retrieveConfig().propertyValueString("grouperOsgiPlugin." + configId + ".jarName");
                try {
                    Bundle bundle = bundleContext.installBundle("file:" + bundleDirWithLastSlash + jarName);
                    bundle.start();
                } catch (BundleException e) {
                    LOG.error("Problem installing plugin: " + jarName, e);
                }
            }
        }
    }
}
