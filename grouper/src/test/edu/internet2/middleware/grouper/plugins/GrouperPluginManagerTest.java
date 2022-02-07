package edu.internet2.middleware.grouper.plugins;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderInput;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderOutput;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderService;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperPluginManagerTest extends GrouperTest {

  /**
   * 
   */
  public GrouperPluginManagerTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperPluginManagerTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperPluginManagerTest("testPlugin"));
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testPlugin() throws Exception {
    
    File felixCacheDir = null;
    File pluginDir = null;
    File pluginFile = null;
    
    try {
      // where is the plugin
      ClassLoader classLoader = this.getClass().getClassLoader();
      URL url = classLoader.getResource("edu/internet2/middleware/grouper/plugins/testImplementation/test-plugin-with-dependencies-0.0.1-SNAPSHOT.jar");
      byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
      pluginDir = new File(GrouperUtil.tmpDir(true) + "grouperTestPlugin_" + GrouperUtil.uniqueId());
      GrouperUtil.mkdirs(pluginDir);
      
      pluginFile = new File(pluginDir.getAbsolutePath() + File.separator + "test-plugin-with-dependencies-0.0.1-SNAPSHOT.jar");
      
      felixCacheDir = new File(GrouperUtil.tmpDir(true) + "grouperTestFelixCache_" + GrouperUtil.uniqueId());
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.felix.cache.rootdir", felixCacheDir.getAbsolutePath());

      GrouperUtil.mkdirs(felixCacheDir);

      Files.write(pluginFile.toPath(), bytes);
      
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.osgi.jar.dir", pluginDir.getAbsolutePath());
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperOsgiPlugin.somePluginConfigId.jarName", "test-plugin-with-dependencies-0.0.1-SNAPSHOT.jar");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperOsgiPlugin.somePluginConfigId.numberOfImplementations", "1");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperOsgiPlugin.somePluginConfigId.osgiImplementation.0.implementationClass", "edu.internet2.middleware.grouper.plugins.testImplementation.SamplePluginProviderServiceImpl");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperOsgiPlugin.somePluginConfigId.osgiImplementation.0.implementsInterface", SamplePluginProviderService.class.getName());
      
      SamplePluginProviderInput providerInput = new SamplePluginProviderInput();
      providerInput.setInput1("hey");
      
      SamplePluginProviderService someGrouperInterface = GrouperPluginManager.retrievePluginImplementation("test-plugin-with-dependencies-0.0.1-SNAPSHOT.jar", SamplePluginProviderService.class, "edu.internet2.middleware.grouper.plugins.testImplementation.SamplePluginProviderServiceImpl");
  
      SamplePluginProviderOutput providerOutput = someGrouperInterface.provide(providerInput);
      assertEquals("hey hey output", providerOutput.getOutput1());
      
      // dont worry about version
      someGrouperInterface = GrouperPluginManager.retrievePluginImplementation("test-plugin-with-dependencies", SamplePluginProviderService.class, "edu.internet2.middleware.grouper.plugins.testImplementation.SamplePluginProviderServiceImpl");
  
      providerOutput = someGrouperInterface.provide(providerInput);
      assertEquals("hey hey output", providerOutput.getOutput1());
      
      // or if there is only one implementation
      someGrouperInterface = GrouperPluginManager.retrievePluginImplementation("test-plugin-with-dependencies", SamplePluginProviderService.class);
  
      providerOutput = someGrouperInterface.provide(providerInput);
      assertEquals("hey hey output", providerOutput.getOutput1());
        
    } finally {
      try {
        GrouperUtil.deleteFile(pluginFile);
      } catch (Exception e) {
      }
      try {
        GrouperUtil.deleteFile(pluginDir);
      } catch (Exception e) {
      }
      GrouperPluginManager.shutdownIfStarted();
      try {
        FileUtils.deleteDirectory(felixCacheDir);
      } catch (Exception e) {
      }
    }
  }
}
