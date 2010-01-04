/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportStemTest extends GrouperTest {

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

    TestRunner.run(XmlExportStemTest.class);
    //TestRunner.run(new XmlExportStemTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportStemTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportStem xmlExportStem = new XmlExportStem();
    
    xmlExportStem.setContextId("contextId");
    xmlExportStem.setCreateTime("createTime");
    xmlExportStem.setCreatorId("creatorId");
    xmlExportStem.setDescription("description");
    xmlExportStem.setDisplayExtension("displayExtension");
    xmlExportStem.setDisplayName("displayName");
    xmlExportStem.setExtension("extension");
    xmlExportStem.setHibernateVersionNumber(3L);
    xmlExportStem.setLastMembershipChange(4L);
    xmlExportStem.setModifierId("modifierId");
    xmlExportStem.setModifierTime("modifierTime");
    xmlExportStem.setName("name");
    xmlExportStem.setParentStem("parentStem");
    xmlExportStem.setUuid("uuid");
    
    String xml = xmlExportStem.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportStem = XmlExportStem.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportStem.getContextId());
    assertEquals("createTime", xmlExportStem.getCreateTime());
    assertEquals("creatorId", xmlExportStem.getCreatorId());
    assertEquals("description", xmlExportStem.getDescription());
    assertEquals("displayExtension", xmlExportStem.getDisplayExtension());
    assertEquals("displayName", xmlExportStem.getDisplayName());
    assertEquals("extension", xmlExportStem.getExtension());
    assertEquals(3L, xmlExportStem.getHibernateVersionNumber());
    assertEquals(4L, xmlExportStem.getLastMembershipChange());
    assertEquals("modifierId", xmlExportStem.getModifierId());
    assertEquals("modifierTime", xmlExportStem.getModifierTime());
    assertEquals("name", xmlExportStem.getName());
    assertEquals("parentStem", xmlExportStem.getParentStem());
    assertEquals("uuid", xmlExportStem.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToStem() {
    Stem stem = new Stem();
    stem.setContextId("contextId");
    stem.setCreateTimeLong(5L);
    stem.setCreatorUuid("creatorId");
    stem.setDescription("description");
    stem.setDisplayExtensionDb("displayExtension");
    stem.setDisplayNameDb("displayName");
    stem.setExtensionDb("extension");
    stem.setHibernateVersionNumber(3L);
    stem.setLastMembershipChangeDb(4L);
    stem.setModifierUuid("modifierId");
    stem.setModifyTimeLong(6L);
    stem.setName("name");
    stem.setParentUuid("parentUuid");
    stem.setUuid("uuid");
    
    XmlExportStem xmlExportStem = new XmlExportStem(new GrouperVersion(GrouperVersion.GROUPER_VERSION), stem);

    //now go back
    stem = xmlExportStem.toStem();
    
    assertEquals("contextId", stem.getContextId());
    assertEquals(5L, stem.getCreateTimeLong());
    assertEquals("creatorId", stem.getCreatorUuid());
    assertEquals("description", stem.getDescription());
    assertEquals("displayExtension", stem.getDisplayExtension());
    assertEquals("displayName", stem.getDisplayName());
    assertEquals("extension", stem.getExtension());
    assertEquals(new Long(3L), stem.getHibernateVersionNumber());
    assertEquals(new Long(4L), stem.getLastMembershipChangeDb());
    assertEquals("modifierId", stem.getModifierUuid());
    assertEquals(6L, stem.getModifyTimeLong());
    assertEquals("name", stem.getName());
    assertEquals("parentUuid", stem.getParentUuid());
    assertEquals("uuid", stem.getUuid());
    
  }
}
