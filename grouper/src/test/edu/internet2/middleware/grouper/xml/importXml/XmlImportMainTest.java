/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import java.io.File;
import java.util.Set;

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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
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
    
    GrouperCheckConfig.checkGroups();

    Stem stem = StemFinder.findByName(grouperSession, "etc", true);
    AttributeDef studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
    studentsAttrDef.setAssignToAttributeDef(true);
    studentsAttrDef.store();
    studentsAttrDef.setAssignToAttributeDef(false);
    studentsAttrDef.store();
    studentsAttrDef.setAssignToStem(true);
    studentsAttrDef.store();
    String studentsAttrDefUuid = studentsAttrDef.getUuid();
    studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction");
    
    XmlImportMain xmlImportMain = new XmlImportMain();

    xmlImportMain.processXml(importfile);

    assertEquals(75,xmlImportMain.getTotalImportFileCount());
    
    //probably at least 2 to get started
    assertTrue(2 < xmlImportMain.getOriginalDbCount());

    assertsFor_v1_6_0(grouperSession, studentsAttrDefUuid);

    assertTrue(0 < xmlImportMain.getInsertCount());
    assertTrue(0 < xmlImportMain.getUpdateCount());

    //now, lets do it again
    xmlImportMain = new XmlImportMain();

    xmlImportMain.processXml(importfile);

    assertEquals(75,xmlImportMain.getTotalImportFileCount());
    
    //probably at least 2 to get started
    assertTrue(2 < xmlImportMain.getOriginalDbCount());

    assertsFor_v1_6_0(grouperSession, studentsAttrDefUuid);

    assertEquals(0, xmlImportMain.getInsertCount());
    assertEquals(0, xmlImportMain.getUpdateCount());
    assertEquals(75, xmlImportMain.getSkipCount());

    
    GrouperSession.stopQuietly(grouperSession);
    
  }

  /**
   * 
   * @param grouperSession
   * @param studentsAttrDefUuid 
   */
  private void assertsFor_v1_6_0(GrouperSession grouperSession, String studentsAttrDefUuid) {
    
    Member groupAmember = MemberFinder.findBySubject(grouperSession, GroupFinder.findByName(grouperSession, "etc:b", true).toSubject(), false);
    assertEquals("7117f54c035d481dbc88ef976c113b62", groupAmember.getContextId());
    assertEquals("e581c4bd8e0245ba933c51e7fe13308b", groupAmember.getSubjectId());

    Stem stemEtc = StemFinder.findByName(grouperSession, "etc", true);
    assertEquals("2010/02/06 17:44:01.938", GrouperUtil.dateStringValue(stemEtc.getCreateTimeLong()));
    
    Group groupA = GroupFinder.findByName(grouperSession, "etc:a", false);
    Group groupB = GroupFinder.findByName(grouperSession, "etc:b", false);
    
    assertNotNull(groupA);
    assertEquals("description", groupA.getDescription());

    GroupType groupTypeTest = GroupTypeFinder.find("test", true);
    
    assertEquals("f29c514f9a8947f38fe489c43c896756", groupTypeTest.getUuid());
    
    Field attrField = FieldFinder.find("attr", true);
    
    assertEquals(new Long(3L), attrField.getHibernateVersionNumber());
    
    assertFalse(groupA.hasType(groupTypeTest));
    assertTrue(groupB.hasType(groupTypeTest));
    
    assertTrue(groupA.hasComposite());
    
    assertEquals("value", groupB.getAttributeOrFieldValue("attr", false, true));
    
    Role userSharerRole = GrouperDAOFactory.getFactory().getRole().findByName("etc:userSharer", true);
    Role userReceiverRole = GrouperDAOFactory.getFactory().getRole().findByName("etc:userReceiver", true);
    
    assertTrue(userSharerRole.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThis().contains(userReceiverRole));
    
    AttributeDef studentsAttrDef = AttributeDefFinder.findByName("etc:students", true);
    assertEquals(studentsAttrDefUuid, studentsAttrDef.getUuid());
    assertTrue(studentsAttrDef.isAssignToGroup());
    assertFalse(studentsAttrDef.isAssignToStem());
    assertEquals(new Long(1), studentsAttrDef.getHibernateVersionNumber());

    AttributeDef studentsAttrDef2 = AttributeDefFinder.findByName("etc:students2", true);
    assertEquals("1e5403c5a1854589b9bc762caacda581", studentsAttrDef2.getUuid());
    assertTrue(studentsAttrDef2.isAssignToGroupAssn());

    Set<String> actions = studentsAttrDef.getAttributeDefActionDelegate().allowedActionStrings();
    assertEquals(3, actions.size());
    assertTrue(actions.contains("assign"));
    assertTrue(actions.contains("someAction"));
    assertTrue(actions.contains("someAction2"));
    
    AttributeAssignAction someAction = studentsAttrDef.getAttributeDefActionDelegate().findAction("someAction", true);
    @SuppressWarnings("unused")
    AttributeAssignAction someAction2 = studentsAttrDef.getAttributeDefActionDelegate().findAction("someAction2", true);
    
    assertTrue(someAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionNamesImpliedByThis().contains("someAction2"));
    
    AttributeDefName studentsAttrName2 = AttributeDefNameFinder.findByName("etc:studentsName2", true);
    assertEquals("f2c92493b8434e599218277410dfcfb4", studentsAttrName2.getId());

    AttributeDefName studentsAttrName = AttributeDefNameFinder.findByName("etc:studentsName", true);

    assertTrue(studentsAttrName.getAttributeDefNameSetDelegate().getAttributeDefNameNamesImpliedByThis().contains("etc:studentsName2"));

    assertTrue(groupB.getAttributeDelegate().hasAttribute(studentsAttrName));

    assertTrue(1 <= HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_attribute_assign_value"));
    
    //TODO do the values later when the API exists
//    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
//    attributeAssignValue.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
//    attributeAssignValue.setAttributeAssignId(attributeAssignResult.getAttributeAssign().getId());
//    attributeAssignValue.setValueString("string");
//    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);

    Set<AttributeDefScope> attributeDefScopes = GrouperDAOFactory.getFactory().getAttributeDefScope().findByAttributeDefId(studentsAttrDef.getUuid());
    assertEquals(1, attributeDefScopes.size());
    AttributeDefScope attributeDefScope = attributeDefScopes.iterator().next();
    assertEquals("whatever", attributeDefScope.getScopeString());

  }
  
}
