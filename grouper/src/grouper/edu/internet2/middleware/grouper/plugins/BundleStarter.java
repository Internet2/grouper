package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        try {
            Path tmpDir = Files.createTempDirectory("grouper");
            bundleDirWithLastSlash = GrouperUtil.stripLastSlashIfExists(GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.jar.dir", tmpDir.toString())) + File.separator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
