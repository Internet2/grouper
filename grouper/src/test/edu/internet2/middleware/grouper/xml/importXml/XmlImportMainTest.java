/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import java.io.File;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class XmlImportMainTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new XmlImportMainTest("testImport_v1_6_0"));
  }

  /**
   * 
   */
  public XmlImportMainTest() {
    super();
    
  }

  /**
   * @param name
   */
  public XmlImportMainTest(String name) {
    super(name);
    
  }

  /**
   * test an import
   */
  public void testImport_v1_6_0() {
    
    File importfile = GrouperUtil.fileFromResourceName("edu/internet2/middleware/grouper/xml/importXml/xmlImport_v1_6_0.xml");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    XmlImportMain xmlImportMain = new XmlImportMain();

    xmlImportMain.processXml(importfile);
    
    assertEquals(75,xmlImportMain.getTotalImportFileCount());
    
    //probably at least 2 to get started
    assertTrue(2 < xmlImportMain.getOriginalDbCount());
    
    Group groupA = GroupFinder.findByName(grouperSession, "etc:a", false);
    
    assertNotNull(groupA);
    assertEquals("description", groupA.getDescription());
    
    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  
}
