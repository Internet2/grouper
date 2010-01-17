/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeAssignActionTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeAssignActionTest.class);
    //TestRunner.run(new XmlExportAttributeAssignActionTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeAssignActionTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = new XmlExportAttributeAssignAction();
    
    xmlExportAttributeAssignAction.setAttributeDefId("attributeDefId");
    xmlExportAttributeAssignAction.setContextId("contextId");
    xmlExportAttributeAssignAction.setCreateTime("createTime");
    xmlExportAttributeAssignAction.setHibernateVersionNumber(3L);
    xmlExportAttributeAssignAction.setModifierTime("modifierTime");
    xmlExportAttributeAssignAction.setName("name");
    xmlExportAttributeAssignAction.setUuid("uuid");
    
    String xml = xmlExportAttributeAssignAction.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeAssignAction = XmlExportAttributeAssignAction.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("attributeDefId", xmlExportAttributeAssignAction.getAttributeDefId());
    assertEquals("contextId", xmlExportAttributeAssignAction.getContextId());
    assertEquals("createTime", xmlExportAttributeAssignAction.getCreateTime());
    assertEquals(3L, xmlExportAttributeAssignAction.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeAssignAction.getModifierTime());
    assertEquals("name", xmlExportAttributeAssignAction.getName());
    assertEquals("uuid", xmlExportAttributeAssignAction.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToGroup() {
    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    attributeAssignAction.setAttributeDefId("attributeDefId");
    attributeAssignAction.setContextId("contextId");
    attributeAssignAction.setCreatedOnDb(new Long(4L));
    attributeAssignAction.setHibernateVersionNumber(3L);
    attributeAssignAction.setId("id");
    attributeAssignAction.setLastUpdatedDb(new Long(7L));
    attributeAssignAction.setNameDb("name");
    
    
    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = new XmlExportAttributeAssignAction(new GrouperVersion(GrouperVersion.GROUPER_VERSION), attributeAssignAction);

    //now go back
    attributeAssignAction = xmlExportAttributeAssignAction.toAttributeAssignAction();
    
    assertEquals("attributeDefId", attributeAssignAction.getAttributeDefId());
    assertEquals("contextId", attributeAssignAction.getContextId());
    assertEquals(new Long(4L), attributeAssignAction.getCreatedOnDb());
    assertEquals(new Long(3L), attributeAssignAction.getHibernateVersionNumber());
    assertEquals(new Long(7), attributeAssignAction.getLastUpdatedDb());
    assertEquals("id", attributeAssignAction.getId());
    assertEquals("name", attributeAssignAction.getName());
    
  }
}
