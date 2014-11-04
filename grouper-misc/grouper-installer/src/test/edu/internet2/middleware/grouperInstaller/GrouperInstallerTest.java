/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperInstaller;

import java.io.File;

import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;
import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperInstallerTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperInstallerTest("testEhcacheMerge"));
  }

  /**
   * 
   */
  public GrouperInstallerTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperInstallerTest(String name) {
    super(name);
    
  }
  
  /**
   * 
   */
  public void testEhcacheMerge() {
    
    String testFileDir = GrouperInstallerUtils.propertiesValue("grouperInstaller.testFile.dir", true);
    
    if (!testFileDir.endsWith("/") && !testFileDir.endsWith("\\")) {
      testFileDir += File.separator;
    }

    String tmpDir = GrouperInstallerUtils.propertiesValue("grouperInstaller.temp.dir", false);
    if (GrouperInstallerUtils.isBlank(tmpDir)) {
      tmpDir = "/tmp/";
    }

    if (!tmpDir.endsWith("/") && !tmpDir.endsWith("\\")) {
      tmpDir += File.separator;
    }
    
    GrouperInstallerUtils.mkdirs(new File(tmpDir));
    
    File ehcacheExampleFile = new File(tmpDir + "ehcache.example.xml");

    GrouperInstallerUtils.copyFile(new File(testFileDir + "ehcache.example.xml"), ehcacheExampleFile);
    
    File ehcacheNewFile = new File(tmpDir + "ehcache.new.xml");

    GrouperInstallerUtils.copyFile(new File(testFileDir + "ehcache.new.xml"), ehcacheNewFile);
    
    File ehcacheFile = new File(tmpDir + "ehcache.xml");

    GrouperInstallerUtils.copyFile(new File(testFileDir + "ehcache.xml"), ehcacheFile);

    GrouperInstaller.mergeEhcacheXmlFiles(ehcacheNewFile,
        ehcacheExampleFile, ehcacheFile);

    
    
  }
  
}
