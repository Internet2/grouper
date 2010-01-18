/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.attr.AttributeDefScopeType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeDefScopeTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeDefScopeTest.class);
    //TestRunner.run(new XmlExportAttributeDefScopeTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeDefScopeTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeDefScope xmlExportAttributeDefScope = new XmlExportAttributeDefScope();
    
    xmlExportAttributeDefScope.setAttributeDefId("attributeDefId");
    xmlExportAttributeDefScope.setAttributeDefScopeType("attributeDefScopeType");
    xmlExportAttributeDefScope.setContextId("contextId");
    xmlExportAttributeDefScope.setCreateTime("createTime");
    xmlExportAttributeDefScope.setHibernateVersionNumber(3L);
    xmlExportAttributeDefScope.setModifierTime("modifierTime");
    xmlExportAttributeDefScope.setScopeString("scopeString");
    xmlExportAttributeDefScope.setScopeString2("scopeString2");
    xmlExportAttributeDefScope.setUuid("uuid");
    
    String xml = xmlExportAttributeDefScope.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeDefScope = XmlExportAttributeDefScope.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("attributeDefId", xmlExportAttributeDefScope.getAttributeDefId());
    assertEquals("attributeDefScopeType", xmlExportAttributeDefScope.getAttributeDefScopeType());
    assertEquals("contextId", xmlExportAttributeDefScope.getContextId());
    assertEquals("createTime", xmlExportAttributeDefScope.getCreateTime());
    assertEquals(3L, xmlExportAttributeDefScope.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeDefScope.getModifierTime());
    assertEquals("scopeString", xmlExportAttributeDefScope.getScopeString());
    assertEquals("scopeString2", xmlExportAttributeDefScope.getScopeString2());
    assertEquals("uuid", xmlExportAttributeDefScope.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeDefScope() {
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setAttributeDefId("attributeDefId");
    attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
    attributeDefScope.setContextId("contextId");
    attributeDefScope.setCreatedOnDb(new Long(4L));
    attributeDefScope.setHibernateVersionNumber(3L);
    attributeDefScope.setLastUpdatedDb(new Long(7L));
    attributeDefScope.setId("id");
    attributeDefScope.setScopeString("scopeString");
    attributeDefScope.setScopeString2("scopeString2");
    
    XmlExportAttributeDefScope xmlExportAttributeDefScope = new XmlExportAttributeDefScope(new GrouperVersion(GrouperVersion.GROUPER_VERSION), attributeDefScope);

    //now go back
    attributeDefScope = xmlExportAttributeDefScope.toAttributeDefScope();
    
    assertEquals("attributeDefId", attributeDefScope.getAttributeDefId());
    assertEquals(AttributeDefScopeType.attributeDefNameIdAssigned, attributeDefScope.getAttributeDefScopeType());
    assertEquals("contextId", attributeDefScope.getContextId());
    assertEquals(new Long(4L), attributeDefScope.getCreatedOnDb());
    assertEquals(new Long(3L), attributeDefScope.getHibernateVersionNumber());
    assertEquals(new Long(7), attributeDefScope.getLastUpdatedDb());
    assertEquals("id", attributeDefScope.getId());
    assertEquals("scopeString", attributeDefScope.getScopeString());
    assertEquals("scopeString2", attributeDefScope.getScopeString2());
    
  }
}
