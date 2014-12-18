/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperInstaller;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;


/**
 *
 */
public class GrouperInstallerMergePatchFilesTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperInstallerMergePatchFilesTest("testMergeConfigs"));
  }

  /**
   * 
   */
  public GrouperInstallerMergePatchFilesTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperInstallerMergePatchFilesTest(String name) {
    super(name);
    
  }

  /**
   * test merge configs
   */
  public void testMergeConfigs() {

    String testFileDirName = GrouperInstallerUtils.propertiesValue("grouperInstaller.testFile.dir", true);
    
    testFileDirName = GrouperInstallerUtils.fileAddLastSlashIfNotExists(testFileDirName);
    
    String tmpDirName = GrouperInstallerUtils.propertiesValue("grouperInstaller.temp.dir", false);
    if (GrouperInstallerUtils.isBlank(tmpDirName)) {
      tmpDirName = "/tmp/";
    }

    tmpDirName = GrouperInstallerUtils.fileAddLastSlashIfNotExists(tmpDirName);
    
    GrouperInstallerUtils.mkdirs(new File(tmpDirName));

    File file1 = new File(testFileDirName + "patchProperties" + File.separator + "test1.properties");
    File file2 = new File(testFileDirName + "patchProperties" + File.separator + "test1dest.properties");
    File file2tmp = new File(tmpDirName + file2.getName());
    GrouperInstallerUtils.copyFile(file2, file2tmp);
    
    GrouperInstallerMergePatchFiles.mergePatchFiles(file1, file2tmp, true);

    Properties destProperties = GrouperInstallerUtils.propertiesFromFile(file2tmp);
    
    assertEquals(0, destProperties.size());
    
    file1 = new File(testFileDirName + "patchProperties" + File.separator + "test2.properties");
    file2 = new File(testFileDirName + "patchProperties" + File.separator + "test2dest.properties");
    file2tmp = new File(tmpDirName + file2.getName());
    GrouperInstallerUtils.copyFile(file2, file2tmp);
    
    GrouperInstallerMergePatchFiles.mergePatchFiles(file1, file2tmp, true);
    
    destProperties = GrouperInstallerUtils.propertiesFromFile(file2tmp);
    
    assertEquals(2, destProperties.size());
    assertEquals("applied", destProperties.getProperty("something.state"));
    assertEquals("whatever", destProperties.getProperty("something.date"));
        
    file1 = new File(testFileDirName + "patchProperties" + File.separator + "test3.properties");
    file2 = new File(testFileDirName + "patchProperties" + File.separator + "test3dest.properties");
    file2tmp = new File(tmpDirName + file2.getName());
    GrouperInstallerUtils.copyFile(file2, file2tmp);
    
    GrouperInstallerMergePatchFiles.mergePatchFiles(file1, file2tmp, true);
    
    destProperties = GrouperInstallerUtils.propertiesFromFile(file2tmp);
    
    assertEquals(8, destProperties.size());
    assertEquals("applied", destProperties.getProperty("something.state"));
    assertEquals("whatever", destProperties.getProperty("something.date"));
    assertEquals("applied", destProperties.getProperty("another.state"));
    assertEquals("whatever2", destProperties.getProperty("another.date"));
    assertEquals("applied", destProperties.getProperty("another2.state"));
    assertEquals("whatever3", destProperties.getProperty("another2.date"));
    assertEquals("applied", destProperties.getProperty("something5.state"));
    assertEquals("whatever10", destProperties.getProperty("something5.date"));    
    
  }
  
}
