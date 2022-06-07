package edu.internet2.middleware.grouper.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import net.sf.cglib.proxy.Enhancer;

/**
 * manage felix osgi plugins
 * @author mchyzer
 *
 */
public class GrouperPluginManager {

  /**
   * if we are initted from config
   */
  private static boolean initted = false;
  
  /**
   * jar name: my-jar-1.2.3.jar
   * to bundle
   */
  private static Map<String, Bundle> pluginJarNameToBundleMap = new HashMap<String, Bundle>();
  
  /**
   * jar name: my-jar-1.2.3.jar to implementations
   * to bundle
   */
  private static Map<String, Map<Class<?>, Set<String>>> pluginJarNameToInterfaceToImplementationClasses = new HashMap<String, Map<Class<?>, Set<String>>>();
  
  /**
   * main felix reference
   */
  private static Felix felix = null;

  /**
   * clear this on stop (for dev purposes)
   */
  private static String felixCacheDir = null;
  
  /**
   * if this dir was created then delete it
   */
  private static boolean felixCacheDirCreated = false;
  
  /**
   * init plugins if not initted
   */
  private static void initIfNotInitted() {
    if (initted) {
      return;
    }
    synchronized(GrouperPluginManager.class) {
      if (initted) {
        return;
      }
      
      // Create a configuration property map.
      Map<String, String> configMap = new HashMap<String, String>();
      
      //configMap.put("felix.log.level", "4");
      
      // if it caches modules, they might not ever get reloaded
      configMap.put("org.osgi.framework.storage.clean", "onFirstInit");
      
      
      Pattern pattern = Pattern.compile("^grouperOsgiPlugin\\.([^.]+)\\.jarName$");
      Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
      if (GrouperUtil.length(configIds) > 0) {
        
        Set<String> packagesForPlugins = new HashSet<String>();
        
        for (String configId: configIds) {
          
          try {
            Map<Class<?>, Set<String>> interfaceClassToImplementations = new HashMap<Class<?>, Set<String>>();
            
            //  grouperOsgiPlugin.<configId>.jarName = my-plugin-with-embedded-dependencies-1.2.3.jar
            //  grouperOsgiPlugin.<configId>.numberOfImplementations = 1
            String jarName = GrouperConfig.retrieveConfig().propertyValueString("grouperOsgiPlugin." + configId + ".jarName");
            int numberOfImplementations = GrouperConfig.retrieveConfig().propertyValueIntRequired("grouperOsgiPlugin." + configId + ".numberOfImplementations");

            pluginJarNameToInterfaceToImplementationClasses.put(jarName, interfaceClassToImplementations);

            for (int i=0;i<numberOfImplementations;i++) {
              //  grouperOsgiPlugin.<configId>.osgiImplementation.0.implementationClass = some.package.SomeClass
              //  grouperOsgiPlugin.<configId>.osgiImplementation.0.implementsInterface = edu.interent2.middleware.grouper.some.package.SomeInterface
              String implementationClassName = GrouperConfig.retrieveConfig().propertyValueString("grouperOsgiPlugin." + configId + ".osgiImplementation." + i + ".implementationClass");
              String implementsInterfaceName = GrouperConfig.retrieveConfig().propertyValueString("grouperOsgiPlugin." + configId + ".osgiImplementation." + i + ".implementsInterface");
              Class<?> implementsInterface = GrouperUtil.forName(implementsInterfaceName);
              
              Set<String> implementationClasses = interfaceClassToImplementations.get(implementsInterface);
              if (implementationClasses == null) {
                implementationClasses = new HashSet<String>();
                interfaceClassToImplementations.put(implementsInterface, implementationClasses);
                
                packagesForPlugins.add(implementsInterface.getPackage().getName());
              }
              implementationClasses.add(implementationClassName);
            }
            
          } catch (Exception e) {
            LOG.error("Problem with plugin: " + configId, e);
            // dont throw so we dont hose all plugins since one is hosed
          }
          
        }
      
      
        // declare which packages to send to modules
        configMap.put("org.osgi.framework.system.packages.extra", GrouperUtil.join(packagesForPlugins.iterator(), ","));

        String grouperFelixCacheDirDefault = "/opt/grouper/grouperWebapp/WEB-INF/grouperFelixCache";
        felixCacheDir = GrouperConfig.retrieveConfig().propertyValueString("grouper.felix.cache.rootdir", grouperFelixCacheDirDefault);
        
        if (StringUtils.equals(felixCacheDir, grouperFelixCacheDirDefault) && new File("/opt/grouper/grouperWebapp/WEB-INF").exists()
            && !new File(grouperFelixCacheDirDefault).exists()) {
          new File(grouperFelixCacheDirDefault).mkdir();
          felixCacheDirCreated = true;
        }

        configMap.put("felix.cache.rootdir", felixCacheDir);
            
        felix = new Felix(configMap);
        // Now start Felix instance.
        try {
          felix.init();
          felix.start();

        } catch (Exception e) {
          throw new RuntimeException("Cant init felix", e);
        }
        
        final BundleContext context = felix.getBundleContext();

        String bundleDirWithLastSlash = GrouperConfig.retrieveConfig().propertyValueString("grouper.osgi.jar.dir", "/opt/grouper/grouperWebapp/WEB-INF/grouperPlugins");
        bundleDirWithLastSlash = GrouperUtil.stripLastSlashIfExists(bundleDirWithLastSlash) + File.separator;
        
        // this would come from a configuration
        for (String pluginJarName : pluginJarNameToInterfaceToImplementationClasses.keySet()) {
          try {
            Bundle bundle = context.installBundle("file:" + bundleDirWithLastSlash + pluginJarName);
            bundle.start();
            pluginJarNameToBundleMap.put(pluginJarName, bundle);
          } catch (Exception e) {
            LOG.error("Problem installing plugin: " + pluginJarName, e);
          }
        }
      }
      initted = true;
      
    }
  }

  /**
   * get the implementation of an interface from a plugin
   * @param <T>
   * @param moduleJarNameInput
   * @param theInterface
   * @return the instance of that plugin class
   */
  public static <T> T retrievePluginImplementation(String moduleJarNameInput, Class<T> theInterface) {
    return retrievePluginImplementation(moduleJarNameInput, theInterface, null);
  }
  
  /**
   * get the implementation of an interface from a plugin
   * @param <T>
   * @param moduleJarNameInput
   * @param theInterface
   * @param pluginClassName or null to use the only one listed (if multiple throw error)
   * @return the instance of that plugin class
   */
  public static <T> T retrievePluginImplementation(String moduleJarNameInput, Class<T> theInterface, String pluginClassName) {
    initIfNotInitted();
    
    String moduleFullName = moduleJarNameInput;
    Bundle bundle = pluginJarNameToBundleMap.get(moduleJarNameInput);
    
    // find one that starts with the module jar input (no version)
    if (bundle == null) {
      moduleFullName = null;
      for (String currentJarName : pluginJarNameToBundleMap.keySet()) {
        if (currentJarName.startsWith(moduleJarNameInput)) {
          if (moduleFullName != null) {
            throw new RuntimeException("There are multiple modules that start with: '" + moduleJarNameInput + "', " + moduleFullName + ", " + currentJarName);
          }
          moduleFullName = currentJarName;
        }
      }
      if (moduleFullName == null) {
        throw new RuntimeException("Cannot find module: '" + moduleJarNameInput + "'");
      }
      bundle = pluginJarNameToBundleMap.get(moduleFullName); 
    }

    Map<Class<?>, Set<String>> interfaceToImplementationClass = pluginJarNameToInterfaceToImplementationClasses.get(moduleFullName);

    // lets validate
    if (interfaceToImplementationClass == null) {
      throw new RuntimeException("Plugin '" + moduleFullName + "' is not configured");
    }

    // default classname to the only implementation
    if (StringUtils.isBlank(pluginClassName)) {
      
      Set<String> implementationClasses = interfaceToImplementationClass.get(theInterface);
      if (GrouperUtil.length(implementationClasses) == 1) {
        pluginClassName = implementationClasses.iterator().next();
      }
    }
    
    Set<String> implementationClasses = interfaceToImplementationClass.get(theInterface);
    if (!GrouperUtil.nonNull(implementationClasses).contains(pluginClassName)) {
      throw new RuntimeException("Plugin '" + moduleFullName + "' is not configured to implement '" + pluginClassName + "' for interface '" + theInterface + "'");
    }
    
    Class<?> clazz = null;
    try {
      clazz = bundle.loadClass(pluginClassName);
    } catch (Exception e) {
      throw new RuntimeException("Cannot load class '" + pluginClassName +  "' from bundle: " + (bundle == null ? null : bundle.getSymbolicName()) + ", " + moduleJarNameInput, e);
    }
    
    // this is an instance from the module, though from a different classloader
    Object providerImpl = null;
    
    try {
      providerImpl = clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Cannot instantiate class '" + pluginClassName +  "' from bundle: " + (bundle == null ? null : bundle.getSymbolicName()) + ", " + moduleJarNameInput, e);
    }

    // this converts that instance to the interface which is implements (but cant be typecast since different classloader)
    DynamicClassProxy providerServiceHandler = new DynamicClassProxy(providerImpl);
    
    @SuppressWarnings("unchecked")
    T providerService = (T)Enhancer.create(theInterface,providerServiceHandler);
    
    return providerService;
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPluginManager.class);

  /**
   * not sure where this would be called from, but heres the method
   */
  public synchronized static void shutdownIfStarted() {
    if (!initted) {
      return;
    }
    for (Bundle bundle : GrouperUtil.nonNull(pluginJarNameToBundleMap).values()) {
      try {
        bundle.stop();
      } catch (Exception e) {
        LOG.error("Problem stopping plugin: " + (bundle == null ? null : bundle.getSymbolicName()), e);
      }
    }
    try {
      felix.stop();
    } catch (Exception e) {
      LOG.error("Problem stopping felix", e);
    }
    // delete felix cache dir
    try {
      if (felixCacheDirCreated && !StringUtils.isBlank(felixCacheDir)) {
        FileUtils.deleteDirectory(new File(felixCacheDir));
      }
    } catch (Exception e) {
      LOG.error("Problem deleting felix cache dir", e);
    }
    
    initted = false;
  }
}
