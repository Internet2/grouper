/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.StringWriter;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
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
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;


/**
 *
 */
public class XmlExportMainTest extends GrouperTest {

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

    TestRunner.run(XmlExportMainTest.class);

  }
  
  /**
   * @param name
   */
  public XmlExportMainTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupA = new GroupSave(grouperSession).assignGroupNameToEdit("etc:a").assignName("etc:a")
       .assignDescription("description")
      .assignCreateParentStemsIfNotExist(true).save();

    Group groupB = new GroupSave(grouperSession).assignGroupNameToEdit("etc:b").assignName("etc:b")
      .assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignGroupNameToEdit("etc:c").assignName("etc:c")
      .assignCreateParentStemsIfNotExist(true).save();

    groupA.addCompositeMember(CompositeType.INTERSECTION, groupB, groupC);
    
    GroupType groupType = GroupType.createType(grouperSession, "test");
    
    groupType.addAttribute(grouperSession, "attr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
    
    groupB.addType(groupType);
    
    groupB.setAttribute("attr", "value");
    groupB.store();
    Stem stem = StemFinder.findByName(grouperSession, "etc", true);
    AttributeDef studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
    Role userSharerRole = stem.addChildRole("userSharer", "userSharer");
    Role userReceiverRole = stem.addChildRole("userReceiver", "userReceiver");
    userSharerRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(userReceiverRole);
    AttributeAssignAction action = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction");
    AttributeAssignAction action2 = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction2");
    action.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);

    studentsAttrDef.setAssignToGroup(true);
    studentsAttrDef.store();

    AttributeDef studentsAttrDef2 = stem.addChildAttributeDef("students2", AttributeDefType.attr);
    studentsAttrDef2.setAssignToGroupAssn(true);
    studentsAttrDef2.store();

    
    AttributeDefName studentsAttrName = stem.addChildAttributeDefName(studentsAttrDef, "studentsName", "studentsName");
    AttributeDefName studentsAttrName2 = stem.addChildAttributeDefName(studentsAttrDef2, "studentsName2", "studentsName2");

    studentsAttrName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(studentsAttrName2);
    
    AttributeAssignResult attributeAssignResult = groupB.getAttributeDelegate().assignAttribute(studentsAttrName);
    attributeAssignResult.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrName2);

    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
    attributeAssignValue.setAttributeAssignId(attributeAssignResult.getAttributeAssign().getId());
    attributeAssignValue.setValueString("string");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
    
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setId(GrouperUuid.getUuid());
    attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
    attributeDefScope.setAttributeDefId(studentsAttrDef.getId());
    attributeDefScope.setScopeString("whatever");
    attributeDefScope.saveOrUpdate();
    
    AuditType auditType = new AuditType("exportCategoryTest", "exportActionTest", null, "labelString01", "labelString02", "labelString03");
    GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditType);
    AuditTypeFinder.clearCache();

    StringWriter stringWriter = new StringWriter();
    XmlExportMain xmlExportMain = new XmlExportMain();
    xmlExportMain.writeAllTables(stringWriter);
    
    String xml = stringWriter.toString();
    
    //TODO comment this out
    System.out.println(xml);
    
    assertTrue(xml, xml.contains("<members>"));
    assertTrue(xml, xml.contains("<XmlExportMember>"));
    
    assertTrue(xml, xml.contains("<stems>"));
    assertTrue(xml, xml.contains("<XmlExportStem>"));

    assertTrue(xml, xml.contains("<groups>"));
    assertTrue(xml, xml.contains("<XmlExportGroup>"));
      
    assertTrue(xml, xml.contains("<groupTypes>"));
    assertTrue(xml, xml.contains("<XmlExportGroupType>"));
      
    assertTrue(xml, xml.contains("<fields>"));
    assertTrue(xml, xml.contains("<XmlExportField>"));

    assertTrue(xml, xml.contains("<groupTypeTuples>"));
    assertTrue(xml, xml.contains("<XmlExportGroupTypeTuple>"));

    assertTrue(xml, xml.contains("<composites>"));
    assertTrue(xml, xml.contains("<XmlExportComposite>"));

    assertTrue(xml, xml.contains("<attributes>"));
    assertTrue(xml, xml.contains("<XmlExportAttribute>"));

    assertTrue(xml, xml.contains("<attributeDefs>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDef>"));

    assertTrue(xml, xml.contains("<memberships>"));
    assertTrue(xml, xml.contains("<XmlExportMembership>"));

    assertTrue(xml, xml.contains("<attributeDefNames>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDefName>"));

    assertTrue(xml, xml.contains("<roleSets>"));
    assertTrue(xml, xml.contains("<XmlExportRoleSet>"));

    assertTrue(xml, xml.contains("<attributeAssignActions>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssignAction>"));

    assertTrue(xml, xml.contains("<attributeAssignActionSets>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssignActionSet>"));

    assertTrue(xml, xml.contains("<attributeAssigns>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssign>"));

    assertTrue(xml, xml.contains("<attributeAssignValues>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssignValue>"));
        
    assertTrue(xml, xml.contains("<attributeDefNameSets>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDefNameSet>"));
        
    assertTrue(xml, xml.contains("<attributeDefScopes>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDefScope>"));
        
    assertTrue(xml, xml.contains("<auditTypes>"));
    assertTrue(xml, xml.contains("<XmlExportAuditType>"));

    assertTrue(xml, xml.contains("<auditEntries>"));
    assertTrue(xml, xml.contains("<XmlExportAuditEntry>"));

    xmlExportMain.setIncludeComments(true);
    xmlExportMain.writeAllTables(stringWriter);

    xml = stringWriter.toString();
    
    //TODO comment this out
    System.out.println(xml);
    
    assertTrue(xml, xml.contains("<!--"));
    assertTrue(xml, xml.contains("<members>"));
    assertTrue(xml, xml.contains("<XmlExportMember>"));
    
    assertTrue(xml, xml.contains("<stems>"));
    assertTrue(xml, xml.contains("<XmlExportStem>"));

    assertTrue(xml, xml.contains("<groups>"));
    assertTrue(xml, xml.contains("<XmlExportGroup>"));
      
    assertTrue(xml, xml.contains("<groupTypes>"));
    assertTrue(xml, xml.contains("<XmlExportGroupType>"));
      
    assertTrue(xml, xml.contains("<fields>"));
    assertTrue(xml, xml.contains("<XmlExportField>"));

    assertTrue(xml, xml.contains("<groupTypeTuples>"));
    assertTrue(xml, xml.contains("<XmlExportGroupTypeTuple>"));

    assertTrue(xml, xml.contains("<composites>"));
    assertTrue(xml, xml.contains("<XmlExportComposite>"));

    assertTrue(xml, xml.contains("<attributes>"));
    assertTrue(xml, xml.contains("<XmlExportAttribute>"));

    assertTrue(xml, xml.contains("<attributeDefs>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDef>"));

    assertTrue(xml, xml.contains("<memberships>"));
    assertTrue(xml, xml.contains("<XmlExportMembership>"));

    assertTrue(xml, xml.contains("<attributeDefNames>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDefName>"));

    assertTrue(xml, xml.contains("<roleSets>"));
    assertTrue(xml, xml.contains("<XmlExportRoleSet>"));

    assertTrue(xml, xml.contains("<attributeAssignActions>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssignAction>"));

    assertTrue(xml, xml.contains("<attributeAssignActionSets>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssignActionSet>"));

    assertTrue(xml, xml.contains("<attributeAssigns>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssign>"));

    assertTrue(xml, xml.contains("<attributeAssignValues>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeAssignValue>"));
        
    assertTrue(xml, xml.contains("<attributeDefNameSets>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDefNameSet>"));
        
    assertTrue(xml, xml.contains("<attributeDefScopes>"));
    assertTrue(xml, xml.contains("<XmlExportAttributeDefScope>"));
        
  }
}
