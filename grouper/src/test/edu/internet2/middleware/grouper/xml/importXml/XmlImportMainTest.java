/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import java.io.File;
import java.io.StringWriter;

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
import edu.internet2.middleware.grouper.attr.AttributeDefScopeType;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportMain;


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

    GrouperCheckConfig.checkGroups();
    
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
    
    Role userSharerRole = GrouperDAOFactory.getFactory().getRole().findByName("etc:userSharer", true);
    Role userReceiverRole = GrouperDAOFactory.getFactory().getRole().findByName("etc:userReceiver", true);
    
    assertTrue(userSharerRole.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThis().contains(userReceiverRole));
    
    
//    Stem stem = StemFinder.findByName(grouperSession, "etc", true);
//    AttributeDef studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
//    Role userSharerRole = stem.addChildRole("userSharer", "userSharer");
//    Role userReceiverRole = stem.addChildRole("userReceiver", "userReceiver");
//    userSharerRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(userReceiverRole);
//    AttributeAssignAction action = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction");
//    AttributeAssignAction action2 = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction2");
//    action.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);
//
//    studentsAttrDef.setAssignToGroup(true);
//    studentsAttrDef.store();
//
//    AttributeDef studentsAttrDef2 = stem.addChildAttributeDef("students2", AttributeDefType.attr);
//    studentsAttrDef2.setAssignToGroupAssn(true);
//    studentsAttrDef2.store();
//
//    
//    AttributeDefName studentsAttrName = stem.addChildAttributeDefName(studentsAttrDef, "studentsName", "studentsName");
//    AttributeDefName studentsAttrName2 = stem.addChildAttributeDefName(studentsAttrDef2, "studentsName2", "studentsName2");
//
//    studentsAttrName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(studentsAttrName2);
//    
//    AttributeAssignResult attributeAssignResult = groupB.getAttributeDelegate().assignAttribute(studentsAttrName);
//    attributeAssignResult.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrName2);
//
//    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
//    attributeAssignValue.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
//    attributeAssignValue.setAttributeAssignId(attributeAssignResult.getAttributeAssign().getId());
//    attributeAssignValue.setValueString("string");
//    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
//    
//    AttributeDefScope attributeDefScope = new AttributeDefScope();
//    attributeDefScope.setId(GrouperUuid.getUuid());
//    attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
//    attributeDefScope.setAttributeDefId(studentsAttrDef.getId());
//    attributeDefScope.setScopeString("whatever");
//    attributeDefScope.saveOrUpdate();
//    
//    StringWriter stringWriter = new StringWriter();
//    XmlExportMain xmlExportMain = new XmlExportMain();
//    xmlExportMain.writeAllTables(stringWriter);

    
    
    
    
    GrouperSession.stopQuietly(grouperSession);
    

    
  }
  
  
}
