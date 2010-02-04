/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import java.io.File;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
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
    
    Member groupAmember = MemberFinder.findBySubject(grouperSession, GroupFinder.findByName(grouperSession, "etc:b", true).toSubject(), false);
    assertEquals("5f2e81fc5aa14b8480dfafa5ab793b6c", groupAmember.getContextId());
    assertEquals("e39fb58df25f4fa89e7dfc6c0dc1ca5c", groupAmember.getSubjectId());

    Stem stemEtc = StemFinder.findByName(grouperSession, "etc", true);
    assertEquals("2010/02/01 03:12:42.219", GrouperUtil.dateStringValue(stemEtc.getCreateTimeLong()));
    
    Group groupA = GroupFinder.findByName(grouperSession, "etc:a", false);
    Group groupB = GroupFinder.findByName(grouperSession, "etc:b", false);
    
    assertNotNull(groupA);
    assertEquals("description", groupA.getDescription());

    GroupType groupTypeTest = GroupTypeFinder.find("test", true);
    
    assertEquals("4a24f4aaf32b41a9bc3963818b362654", groupTypeTest.getUuid());
    
    Field attrField = FieldFinder.find("attr", true);
    
    assertEquals(new Long(3L), attrField.getHibernateVersionNumber());
    
    assertFalse(groupA.hasType(groupTypeTest));
    assertTrue(groupB.hasType(groupTypeTest));
    
    assertTrue(groupA.hasComposite());
    
    assertEquals("value", groupB.getAttributeOrFieldValue("attr", false, true));
    
    AttributeDef attributeDef = AttributeDefFinder.findByName("etc:students", true);
    
    assertEquals("150bf6f24d52424abd0569193c3379cc", attributeDef.getUuid());
    
    
    
    
    GrouperSession.stopQuietly(grouperSession);
    

    
  }
  
  
}
