/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.attr.AttributeDefScopeType;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


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

    //TestRunner.run(XmlExportMainTest.class);

    TestRunner.run(new XmlExportMainTest("testConvertToXml"));
    
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
    
    groupType.addAttribute(grouperSession, "attr", false);
    
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
    
    studentsAttrDef.setValueType(AttributeDefValueType.string);
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
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_type where audit_category = 'exportCategoryTest'");
    
    AuditType auditType = new AuditType("exportCategoryTest", "exportActionTest", null, "labelString01", "labelString02", "labelString03");
    GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditType);
    AuditTypeFinder.clearCache();

    StringWriter stringWriter = new StringWriter();
    XmlExportMain xmlExportMain = new XmlExportMain();
    xmlExportMain.writeAllTables(stringWriter, "a string");
    
    String xml = stringWriter.toString();
    
    System.out.println(xml);
    
    assertTrue(xml, xml.contains("<members>"));
    assertTrue(xml, xml.contains("<XmlExportMember>"));
    
    assertTrue(xml, xml.contains("<stems>"));
    assertTrue(xml, xml.contains("<XmlExportStem>"));

    assertTrue(xml, xml.contains("<groups>"));
    assertTrue(xml, xml.contains("<idIndex>"));
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

    stringWriter = new StringWriter();
    
    xmlExportMain.setIncludeComments(true);
    xmlExportMain.writeAllTables(stringWriter, "a string");

    xml = stringWriter.toString();
    
    //TODO comment this out
    //System.out.println(xml);
    
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
        
    
    //lets try to import this
    try {
      new XmlImportMain().processXml(xml); 
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * 
   */
  public void testConvertToXmlStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupA = new GroupSave(grouperSession).assignGroupNameToEdit("notExport:a").assignName("notExport:a")
       .assignDescription("description")
      .assignCreateParentStemsIfNotExist(true).save();
  
    Group groupB = new GroupSave(grouperSession).assignGroupNameToEdit("notExport:b").assignName("notExport:b")
      .assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignGroupNameToEdit("notExport:c").assignName("notExport:c")
      .assignCreateParentStemsIfNotExist(true).save();

    Group groupD = new GroupSave(grouperSession).assignGroupNameToEdit("yesExport:d").assignName("yesExport:d")
      .assignCreateParentStemsIfNotExist(true).save();

    Group groupE = new GroupSave(grouperSession).assignGroupNameToEdit("yesExportAlso:e").assignName("yesExportAlso:e")
      .assignCreateParentStemsIfNotExist(true).save();

    Group groupF = new GroupSave(grouperSession).assignGroupNameToEdit("yesExport:f").assignName("yesExport:f")
      .assignCreateParentStemsIfNotExist(true).save();

    Group groupG = new GroupSave(grouperSession).assignGroupNameToEdit("yesExport:g").assignName("yesExport:g")
      .assignCreateParentStemsIfNotExist(true).save();
    
    Group groupH = new GroupSave(grouperSession).assignGroupNameToEdit("yesExport:h").assignName("yesExport:h")
      .assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addCompositeMember(CompositeType.INTERSECTION, groupB, groupC);
    groupF.addCompositeMember(CompositeType.INTERSECTION, groupG, groupH);
    
    GroupType groupType = GroupType.createType(grouperSession, "test");
    
    groupType.addAttribute(grouperSession, "attr", false);
    
    groupB.addType(groupType);
    groupG.addType(groupType);
    
    groupB.setAttribute("attr", "valueB");
    groupG.setAttribute("attr", "valueG");
    groupB.store();
    groupG.store();
    
    groupE.addMember(SubjectTestHelper.SUBJ8);
    
    Membership membershipYes = groupE.getImmediateMembership(Group.getDefaultList(), SubjectTestHelper.SUBJ8, true, true);
    
    groupB.addMember(SubjectTestHelper.SUBJ9);

    Membership membershipNo = groupB.getImmediateMembership(Group.getDefaultList(), SubjectTestHelper.SUBJ9, true, true);

    Stem stem = StemFinder.findByName(grouperSession, "notExport", true);
    Stem stemYes = StemFinder.findByName(grouperSession, "yesExport", true);

    AttributeDef studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
    Role userSharerRole = stem.addChildRole("userSharer", "userSharer");
    Role userReceiverRole = stem.addChildRole("userReceiver", "userReceiver");
    userSharerRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(userReceiverRole);
    
    Role userSharerRoleYes = stemYes.addChildRole("userSharerYes", "userSharerYes");
    Role userReceiverRoleYes = stemYes.addChildRole("userReceiverYes", "userReceiverYes");
    userSharerRoleYes.getRoleInheritanceDelegate().addRoleToInheritFromThis(userReceiverRoleYes);

    
    AttributeAssignAction action = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction");
    AttributeAssignAction action2 = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction2");
    action.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);

    AttributeDef studentsAttrDefYes = stemYes.addChildAttributeDef("studentsYes", AttributeDefType.attr);
    
    AttributeDefName studentsAttrNameYes = stemYes.addChildAttributeDefName(studentsAttrDefYes, "studentsNameYes", "studentsNameYes");
    AttributeDefName studentsAttrNameYes2 = stemYes.addChildAttributeDefName(studentsAttrDefYes, "studentsNameYes2", "studentsNameYes2");

    AttributeAssignAction notAction = studentsAttrDef.getAttributeDefActionDelegate().addAction("notAction");
    AttributeAssignAction yesAction = studentsAttrDefYes.getAttributeDefActionDelegate().addAction("yesAction");
    AttributeAssignAction notAction2 = studentsAttrDef.getAttributeDefActionDelegate().addAction("notAction2");
    AttributeAssignAction yesAction2 = studentsAttrDefYes.getAttributeDefActionDelegate().addAction("yesAction2");
    
    notAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(notAction2);
    yesAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(yesAction2);
    
    
    studentsAttrDef.setAssignToGroup(true);
    studentsAttrDef.setAssignToStem(true);
    studentsAttrDef.setAssignToImmMembership(true);
    studentsAttrDef.setAssignToAttributeDef(true);
    studentsAttrDef.setValueType(AttributeDefValueType.string);
    studentsAttrDef.store();
  
    studentsAttrDefYes.setAssignToAttributeDef(true);
    studentsAttrDefYes.setAssignToAttributeDefAssn(true);
    studentsAttrDefYes.setAssignToEffMembership(true);
    studentsAttrDefYes.setAssignToEffMembershipAssn(true);
    studentsAttrDefYes.setAssignToGroup(true);
    studentsAttrDefYes.setAssignToGroupAssn(true);
    studentsAttrDefYes.setAssignToImmMembership(true);
    studentsAttrDefYes.setAssignToImmMembershipAssn(true);
    studentsAttrDefYes.setAssignToMember(true);
    studentsAttrDefYes.setAssignToMemberAssn(true);
    studentsAttrDefYes.setAssignToStem(true);
    studentsAttrDefYes.setAssignToStemAssn(true);
    
    studentsAttrDefYes.setValueType(AttributeDefValueType.string);

    studentsAttrDefYes.setAssignToGroup(true);
    studentsAttrDefYes.setAssignToStem(true);
    studentsAttrDefYes.setAssignToMember(true);
    studentsAttrDefYes.setAssignToImmMembership(true);
    studentsAttrDefYes.setAssignToAttributeDef(true);
    studentsAttrDefYes.store();
  
    AttributeDef studentsAttrDef2 = stem.addChildAttributeDef("students2", AttributeDefType.attr);
    studentsAttrDef2.setAssignToGroupAssn(true);
    studentsAttrDef2.store();
  
    AttributeDef studentsAttrDefYes2 = stemYes.addChildAttributeDef("studentsYes2", AttributeDefType.attr);
    studentsAttrDefYes2.setAssignToGroupAssn(true);
    studentsAttrDefYes2.store();
  
    
    AttributeDefName studentsAttrName = stem.addChildAttributeDefName(studentsAttrDef, "studentsName", "studentsName");
    AttributeDefName studentsAttrName2 = stem.addChildAttributeDefName(studentsAttrDef2, "studentsName2", "studentsName2");
  
    studentsAttrName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(studentsAttrName2);
    studentsAttrNameYes.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(studentsAttrNameYes2);
    
    AttributeAssignResult attributeAssignResult = groupB.getAttributeDelegate().assignAttribute(studentsAttrName);
    AttributeAssignResult attributeAssignResultAssn = attributeAssignResult.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrName2);
  
    AttributeAssignResult attributeAssignResultYes = groupD.getAttributeDelegate().assignAttribute(studentsAttrNameYes);
    AttributeAssignResult attributeAssignResultAssnYes = attributeAssignResultYes.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrNameYes2);

    AttributeAssignResult attributeAssignResultStem = stem.getAttributeDelegate().assignAttribute(studentsAttrName);
    AttributeAssignResult attributeAssignResultStemYes = stemYes.getAttributeDelegate().assignAttribute(studentsAttrNameYes);

    AttributeAssignResult attributeAssignResultAttrDef = studentsAttrDef.getAttributeDelegate().assignAttribute(studentsAttrName);
    AttributeAssignResult attributeAssignResultAttrDefYes = studentsAttrDefYes.getAttributeDelegate().assignAttribute(studentsAttrNameYes);
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    AttributeAssignResult attributeAssignResultMemberYes = member.getAttributeDelegate().assignAttribute(studentsAttrNameYes);

    
    attributeAssignResultYes.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrNameYes2);

    AttributeAssignResult attributeAssignResultMship = membershipNo.getAttributeDelegate().assignAttribute(studentsAttrName);
    AttributeAssignResult attributeAssignResultMshipYes = membershipYes.getAttributeDelegate().assignAttribute(studentsAttrNameYes);
    
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
    attributeAssignValue.setAttributeAssignId(attributeAssignResult.getAttributeAssign().getId());
    attributeAssignValue.setValueString("string");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
    
    AttributeAssignValue attributeAssignValueYes = new AttributeAssignValue();
    attributeAssignValueYes.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
    attributeAssignValueYes.setAttributeAssignId(attributeAssignResultYes.getAttributeAssign().getId());
    attributeAssignValueYes.setValueString("stringYes");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValueYes);
    
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setId(GrouperUuid.getUuid());
    attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
    attributeDefScope.setAttributeDefId(studentsAttrDef.getId());
    attributeDefScope.setScopeString("whatever");
    attributeDefScope.saveOrUpdate();
    
    AttributeDefScope attributeDefScopeYes = new AttributeDefScope();
    attributeDefScopeYes.setId(GrouperUuid.getUuid());
    attributeDefScopeYes.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
    attributeDefScopeYes.setAttributeDefId(studentsAttrDefYes.getId());
    attributeDefScopeYes.setScopeString("whateverYes");
    attributeDefScopeYes.saveOrUpdate();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_type where audit_category = 'exportCategoryTest'");
    
    AuditType auditType = new AuditType("exportCategoryTest", "exportActionTest", null, "labelString01", "labelString02", "labelString03");
    GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditType);
    AuditTypeFinder.clearCache();
  
    StringWriter stringWriter = new StringWriter();
    
    // TRY WITH NO AUDITS
    XmlExportMain xmlExportMain = new XmlExportMain();
    
    xmlExportMain.setIncludeAudits(false);
    
    xmlExportMain.writeAllTables(stringWriter, "a string");
    
    String xml = stringWriter.toString();
        
    assertTrue(xml.contains("XmlExportMembership"));
    assertFalse(xml.contains("XmlExportAuditEntry"));
    assertFalse(xml.contains("XmlExportAuditType"));
    
    //TRY WITH ONE STEM
    stringWriter = new StringWriter();
    xmlExportMain = new XmlExportMain();
    xmlExportMain.setIncludeComments(true);
    xmlExportMain.setIncludeAudits(false);
    xmlExportMain.addStem("yesExport");
    xmlExportMain.addStem("whatever");

    xmlExportMain.addObjectName(groupE.getName());
    
    xmlExportMain.writeAllTables(stringWriter, "a string");
    
    xml = stringWriter.toString();
    
    //System.out.println(GrouperUtil.indent(xml, true));
    
    assertTrue(xml.contains("folders=\"yesExport:%, whatever:%\"")); 
    assertTrue(xml.contains("objects=\"yesExportAlso:e\""));
    assertTrue(xml.contains("members=\"allWithoutUnecessaryGroups\""));
    
    assertTrue(xml.contains(groupD.getId()));
    assertTrue(xml.contains(groupE.getName()));
    assertFalse(xml.contains("<uuid>" + groupA.getParentStem().getUuid() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + groupD.getParentStem().getUuid() + "</uuid>"));
    assertFalse(groupA.getId(), xml.contains("<uuid>" + groupA.getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + groupD.getId() + "</uuid>"));

    assertFalse(xml.contains("<leftFactor>" + groupB.getUuid() + "</leftFactor>"));
    assertTrue(xml.contains("<leftFactor>" + groupG.getUuid() + "</leftFactor>"));
    
    assertFalse(xml.contains("<value>valueB</value>"));
    assertTrue(xml.contains("<value>valueG</value>"));
    
    assertFalse(xml.contains("<name>" + studentsAttrDef.getName() + "</name>"));
    assertTrue(xml.contains("<name>" + studentsAttrDefYes.getName() + "</name>"));

    assertFalse(xml.contains("<name>" + studentsAttrName.getName() + "</name>"));
    assertTrue(xml.contains("<name>" + studentsAttrNameYes.getName() + "</name>"));
    
    assertFalse(xml.contains("<ifHasRoleId>" + userSharerRole.getId() + "</ifHasRoleId>"));
    assertTrue(xml.contains("<ifHasRoleId>" + userSharerRoleYes.getId() + "</ifHasRoleId>"));
    
    assertFalse(xml.contains("<name>notAction</name>"));
    assertTrue(xml.contains("<name>yesAction</name>"));
    
    assertFalse(xml.contains("<ifHasAttributeAssignActionId>" + notAction.getId() + "</ifHasAttributeAssignActionId>"));
    assertTrue(xml.contains("<ifHasAttributeAssignActionId>" + yesAction.getId() + "</ifHasAttributeAssignActionId>"));
    
    assertFalse(xml.contains("<uuid>" + attributeAssignResult.getAttributeAssign().getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + attributeAssignResultYes.getAttributeAssign().getId() + "</uuid>"));
    
    assertFalse(xml.contains("<uuid>" + attributeAssignResultStem.getAttributeAssign().getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + attributeAssignResultStemYes.getAttributeAssign().getId() + "</uuid>"));
    
    assertFalse(xml.contains("<uuid>" + attributeAssignResultMemberYes.getAttributeAssign().getId() + "</uuid>"));
    
    assertFalse(xml.contains("<uuid>" + attributeAssignResultMship.getAttributeAssign().getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + attributeAssignResultMshipYes.getAttributeAssign().getId() + "</uuid>"));
    
    assertFalse(groupB.getUuid(), xml.contains("<ownerGroupId>" + groupB.getUuid() + "</ownerGroupId>"));
    assertTrue(groupE.getUuid(), xml.contains("<ownerGroupId>" + groupE.getUuid() + "</ownerGroupId>"));
    
    assertFalse(xml.contains("<uuid>" + attributeAssignResultAttrDef.getAttributeAssign().getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + attributeAssignResultAttrDefYes.getAttributeAssign().getId() + "</uuid>"));

    assertFalse(xml.contains("<uuid>" + attributeAssignResultAssn.getAttributeAssign().getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + attributeAssignResultAssnYes.getAttributeAssign().getId() + "</uuid>"));

    assertFalse(xml.contains("<uuid>" + attributeAssignValue.getId() + "</uuid>"));
    assertTrue(xml.contains("<uuid>" + attributeAssignValueYes.getId() + "</uuid>"));

    assertFalse(xml.contains("<ifHasAttributeDefNameId>" + studentsAttrName.getId() + "</ifHasAttributeDefNameId>"));
    assertTrue(xml.contains("<ifHasAttributeDefNameId>" + studentsAttrNameYes.getId() + "</ifHasAttributeDefNameId>"));

    assertFalse(xml.contains("<scopeString>whatever</scopeString>"));
    assertTrue(xml.contains("<scopeString>whateverYes</scopeString>"));

    
    
    assertTrue(xml.contains("yesExport"));
    assertFalse(xml.contains("notExport"));
    
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
        
    assertFalse(xml, xml.contains("<auditTypes>"));
    assertFalse(xml, xml.contains("<XmlExportAuditType>"));
  
    assertFalse(xml, xml.contains("<auditEntries>"));
    assertFalse(xml, xml.contains("<XmlExportAuditEntry>"));
  
    stringWriter = new StringWriter();
    
    xmlExportMain.setIncludeComments(true);
    xmlExportMain.writeAllTables(stringWriter, "a string");
  
    xml = stringWriter.toString();
    
    //TODO comment this out
    //System.out.println(xml);
    
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
        
    
    //lets try to import this
    new XmlImportMain().processXml(xml); 
  }
}
