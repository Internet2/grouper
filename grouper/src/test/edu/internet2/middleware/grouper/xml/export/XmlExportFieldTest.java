/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.TestField;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportFieldTest extends GrouperTest {

  /** grouperSession */
  private GrouperSession grouperSession;

  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    this.grouperSession = GrouperSession.startRootSession();
  }

 
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    super.tearDown();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    TestRunner.run(XmlExportFieldTest.class);
    //TestRunner.run(new XmlExportFieldTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportFieldTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportField xmlExportField = new XmlExportField();
    
    xmlExportField.setContextId("contextId");
    xmlExportField.setGroupTypeUuid("groupTypeUuid");
    xmlExportField.setHibernateVersionNumber(3L);
    xmlExportField.setName("name");
    xmlExportField.setNullable("nullable");
    xmlExportField.setReadPrivilege("readPrivilege");
    xmlExportField.setType("type");
    xmlExportField.setUuid("uuid");
    xmlExportField.setWritePrivilege("writePrivilege");
    
    String xml = xmlExportField.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportField = XmlExportField.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportField.getContextId());
    assertEquals("groupTypeUuid", xmlExportField.getGroupTypeUuid());
    assertEquals(3L, xmlExportField.getHibernateVersionNumber());
    assertEquals("name", xmlExportField.getName());
    assertEquals("nullable", xmlExportField.getNullable());
    assertEquals("readPrivilege", xmlExportField.getReadPrivilege());
    assertEquals("type", xmlExportField.getType());
    assertEquals("uuid", xmlExportField.getUuid());
    assertEquals("writePrivilege", xmlExportField.getWritePrivilege());
        
  }
  
  /**
   * 
   */
  public void testConvertToField() {
    Field field = TestField.exampleField();
    
    XmlExportField xmlExportField = field.xmlToExportField(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    field = xmlExportField.toField();
    
    assertEquals("contextId", field.getContextId());
    assertEquals("groupTypeUuid", field.getGroupTypeUuid());
    assertEquals(new Long(3L), field.getHibernateVersionNumber());
    assertEquals("name", field.getName());
    assertEquals(true, field.getIsNullable());
    assertEquals("readPrivilege", field.getReadPrivilege());
    assertEquals("type", field.getTypeString());
    assertEquals("uuid", field.getUuid());
    assertEquals("writePrivilege", field.getWritePrivilege());
    
  }
}
