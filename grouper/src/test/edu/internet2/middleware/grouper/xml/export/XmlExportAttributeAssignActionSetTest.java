/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeAssignActionSetTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeAssignActionSetTest.class);
    //TestRunner.run(new XmlExportAttributeAssignActionSetTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeAssignActionSetTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = new XmlExportAttributeAssignActionSet();
    
    xmlExportAttributeAssignActionSet.setContextId("contextId");
    xmlExportAttributeAssignActionSet.setCreateTime("createTime");
    xmlExportAttributeAssignActionSet.setDepth(5L);
    xmlExportAttributeAssignActionSet.setIfHasAttributeAssignActionId("ifHasAttributeAssignActionId");
    xmlExportAttributeAssignActionSet.setHibernateVersionNumber(3L);
    xmlExportAttributeAssignActionSet.setModifierTime("modifierTime");
    xmlExportAttributeAssignActionSet.setParentAttributeAssignActionSetId("parentAttributeAssignActionSetId");
    xmlExportAttributeAssignActionSet.setThenHasAttributeAssignActionId("thenHasAttributeAssignActionSetId");
    xmlExportAttributeAssignActionSet.setType("type");
    xmlExportAttributeAssignActionSet.setUuid("uuid");
    
    String xml = xmlExportAttributeAssignActionSet.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeAssignActionSet = XmlExportAttributeAssignActionSet.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportAttributeAssignActionSet.getContextId());
    assertEquals("createTime", xmlExportAttributeAssignActionSet.getCreateTime());
    assertEquals(5L, xmlExportAttributeAssignActionSet.getDepth());
    assertEquals("ifHasAttributeAssignActionId", xmlExportAttributeAssignActionSet.getIfHasAttributeAssignActionId());
    assertEquals(3L, xmlExportAttributeAssignActionSet.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeAssignActionSet.getModifierTime());
    assertEquals("parentAttributeAssignActionSetId", xmlExportAttributeAssignActionSet.getParentAttributeAssignActionSetId());
    assertEquals("thenHasAttributeAssignActionSetId", xmlExportAttributeAssignActionSet.getThenHasAttributeAssignActionId());
    assertEquals("type", xmlExportAttributeAssignActionSet.getType());
    assertEquals("uuid", xmlExportAttributeAssignActionSet.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeAssignActionSet() {
    AttributeAssignActionSet attributeAssignActionSet = new AttributeAssignActionSet();
    attributeAssignActionSet.setContextId("contextId");
    attributeAssignActionSet.setCreatedOnDb(new Long(4L));
    attributeAssignActionSet.setDepth(5);
    attributeAssignActionSet.setIfHasAttrAssignActionId("ifHasAttributeAssignActionId");
    attributeAssignActionSet.setHibernateVersionNumber(3L);
    attributeAssignActionSet.setLastUpdatedDb(new Long(7L));
    attributeAssignActionSet.setParentAttrAssignActionSetId("parentAttributeAssignActionSetId");
    attributeAssignActionSet.setThenHasAttrAssignActionId("thenHasAttributeAssignActionSetId");
    attributeAssignActionSet.setType(AttributeAssignActionType.effective);
    attributeAssignActionSet.setId("id");
    
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = new XmlExportAttributeAssignActionSet(new GrouperVersion(GrouperVersion.GROUPER_VERSION), attributeAssignActionSet);

    //now go back
    attributeAssignActionSet = xmlExportAttributeAssignActionSet.toAttributeAssignActionSet();
    
    assertEquals("contextId", attributeAssignActionSet.getContextId());
    assertEquals(new Long(4L), attributeAssignActionSet.getCreatedOnDb());
    assertEquals(5, attributeAssignActionSet.getDepth());
    assertEquals(new Long(3L), attributeAssignActionSet.getHibernateVersionNumber());
    assertEquals("ifHasAttributeAssignActionId", attributeAssignActionSet.getIfHasAttrAssignActionId());
    assertEquals(new Long(7), attributeAssignActionSet.getLastUpdatedDb());
    assertEquals("id", attributeAssignActionSet.getId());
    assertEquals("parentAttributeAssignActionSetId", attributeAssignActionSet.getParentAttrAssignActionSetId());
    assertEquals("thenHasAttributeAssignActionSetId", attributeAssignActionSet.getThenHasAttrAssignActionId());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSet.getType());
    
  }
}
