/*
 * @author mchyzer
 * $Id: GrouperServiceLogicTest.java,v 1.2 2008-12-04 07:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameTest;
import edu.internet2.middleware.grouper.attr.AttributeDefTest;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueResult;
import edu.internet2.middleware.grouper.attr.value.AttributeValueResult;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionAssignOperation;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap.WsPermissionAssign;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperServiceLogicTest extends GrouperTest {

  /**
   * 
   */
  public GrouperServiceLogicTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperServiceLogicTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(GrouperServiceLogicTest.class);
    TestRunner.run(new GrouperServiceLogicTest("testAssignAttributes"));
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();
    
    //help test logins from session opened from resetData
    GrouperServiceUtils.testSession = GrouperSession.staticGrouperSession();

    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");

    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    GrouperWsVersionUtils.assignCurrentClientVersion(GrouperVersion.valueOfIgnoreCase("v1_6_000"), new StringBuilder());
  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();

    //clear out
    GrouperServiceUtils.testSession = null;
  }

  /**
   * @throws SessionException 
   * 
   */
  public void testSaveGroupDetailInsert() throws SessionException {
    
    WsGroupToSave leftGroupToSave = new WsGroupToSave();
    leftGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());
    WsGroupLookup leftWsGroupLookup = new WsGroupLookup();
    leftWsGroupLookup.setGroupName("aStem:aGroupLeft");
    leftGroupToSave.setWsGroupLookup(leftWsGroupLookup);
    WsGroup leftWsGroup = new WsGroup();
    leftWsGroup.setDescription("some group");
    leftWsGroup.setDisplayExtension("aGroupLeft");
    leftWsGroup.setName("aStem:aGroupLeft");
    leftGroupToSave.setWsGroup(leftWsGroup);
    
    WsGroupToSave rightGroupToSave = new WsGroupToSave();
    rightGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());
    WsGroupLookup rightWsGroupLookup = new WsGroupLookup();
    rightWsGroupLookup.setGroupName("aStem:aGroupRight");
    rightGroupToSave.setWsGroupLookup(rightWsGroupLookup);
    WsGroup rightWsGroup = new WsGroup();
    rightWsGroup.setDescription("some group");
    rightWsGroup.setDisplayExtension("aGroupRight");
    rightWsGroup.setName("aStem:aGroupRight");
    rightGroupToSave.setWsGroup(rightWsGroup);

    
    
    
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setSaveMode(SaveMode.INSERT.name());
    WsGroupLookup wsGroupLookup = new WsGroupLookup();
    wsGroupLookup.setGroupName("aStem:aGroupInsert");
    wsGroupToSave.setWsGroupLookup(wsGroupLookup);
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription("some group");
    wsGroup.setDisplayExtension("aGroupInsert");
    wsGroup.setName("aStem:aGroupInsert");
    
    WsGroupDetail wsGroupDetail = new WsGroupDetail();
    wsGroupDetail.setTypeNames(new String[]{"aType", "aType2"});

    wsGroupDetail.setAttributeNames(new String[]{"attr_1", "attr2_1"});
    wsGroupDetail.setAttributeValues(new String[]{"val_1", "val2_1"});
    
    wsGroup.setDetail(wsGroupDetail);
    wsGroupToSave.setWsGroup(wsGroup);
    
    wsGroupDetail.setHasComposite("T");
    wsGroupDetail.setCompositeType("UNION");
    wsGroupDetail.setLeftGroup(leftWsGroup);
    wsGroupDetail.setRightGroup(rightWsGroup);
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(SubjectFinder.findRootSubject().getId(), null, null);
    WsGroupSaveResults wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperVersion.valueOfIgnoreCase("v1_4_000"), 
        new WsGroupToSave[]{leftGroupToSave, rightGroupToSave, wsGroupToSave}, 
        actAsSubjectLookup, GrouperTransactionType.NONE, true, null);
    
    if (!StringUtils.equals("T", wsGroupSaveResults.getResultMetadata().getSuccess())) {
      int index = 0;
      for (WsGroupSaveResult wsGroupSaveResult : GrouperUtil.nonNull(wsGroupSaveResults.getResults(), WsGroupSaveResult.class)) {
        if (!StringUtils.equals("T", wsGroupSaveResult.getResultMetadata().getSuccess())) {
          System.out.println("Error on index: " + index + ", " + wsGroupSaveResult.getResultMetadata().getResultMessage());
        }
        index++;
      }
    }
    
    
    assertEquals(wsGroupSaveResults.getResultMetadata().getResultMessage(), "T", 
        wsGroupSaveResults.getResultMetadata().getSuccess());
    
    WsGroupSaveResult[] wsGroupSaveResultsArray = wsGroupSaveResults.getResults();
    WsGroup wsGroupResult = wsGroupSaveResultsArray[2].getWsGroup();
    WsGroupDetail wsGroupDetailResult = wsGroupResult.getDetail();
    assertEquals(2, wsGroupDetailResult.getAttributeNames().length);
    assertEquals("attr2_1", wsGroupDetailResult.getAttributeNames()[0]);
    assertEquals("attr_1", wsGroupDetailResult.getAttributeNames()[1]);

    assertEquals(2, wsGroupDetailResult.getAttributeValues().length);
    assertEquals("val2_1", wsGroupDetailResult.getAttributeValues()[0]);
    assertEquals("val_1", wsGroupDetailResult.getAttributeValues()[1]);
    
    assertEquals(2, wsGroupDetailResult.getTypeNames().length);
    assertEquals("aType", wsGroupDetailResult.getTypeNames()[0]);
    assertEquals("aType2", wsGroupDetailResult.getTypeNames()[1]);
    
    assertEquals("T", wsGroupDetailResult.getHasComposite());
    assertEquals("F", wsGroupDetailResult.getIsCompositeFactor());
    assertEquals("union", wsGroupDetailResult.getCompositeType());
    assertEquals("aStem:aGroupLeft", wsGroupDetailResult.getLeftGroup().getName());
    assertEquals("aStem:aGroupRight", wsGroupDetailResult.getRightGroup().getName());
    
    //######################################
    //now lets mix things up a little bit
    
    //make new lookups since stuff is stored in there
    leftWsGroupLookup = new WsGroupLookup();
    leftWsGroupLookup.setGroupName("aStem:aGroupLeft");
    leftGroupToSave.setWsGroupLookup(leftWsGroupLookup);
    
    rightWsGroupLookup = new WsGroupLookup();
    rightWsGroupLookup.setGroupName("aStem:aGroupRight");
    rightGroupToSave.setWsGroupLookup(rightWsGroupLookup);
    
    wsGroupLookup = new WsGroupLookup();
    wsGroupLookup.setGroupName("aStem:aGroupInsert");
    wsGroupToSave.setWsGroupLookup(wsGroupLookup);
    
    wsGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());

    wsGroupDetail.setTypeNames(new String[]{"aType", "aType3"});

    wsGroupDetail.setAttributeNames(new String[]{"attr_1", "attr3_1"});
    wsGroupDetail.setAttributeValues(new String[]{"val_1", "val3_1"});
    
    wsGroupDetail.setHasComposite("T");
    wsGroupDetail.setCompositeType("COMPLEMENT");
    wsGroupDetail.setLeftGroup(rightWsGroup);
    wsGroupDetail.setRightGroup(leftWsGroup);

    //this was probably closed by last call
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperVersion.valueOfIgnoreCase("v1_4_000"), 
        new WsGroupToSave[]{leftGroupToSave, rightGroupToSave, wsGroupToSave}, 
        actAsSubjectLookup, GrouperTransactionType.NONE, true, null);
    
    if (!StringUtils.equals("T", wsGroupSaveResults.getResultMetadata().getSuccess())) {
      int index = 0;
      for (WsGroupSaveResult wsGroupSaveResult : GrouperUtil.nonNull(wsGroupSaveResults.getResults(), WsGroupSaveResult.class)) {
        if (!StringUtils.equals("T", wsGroupSaveResult.getResultMetadata().getSuccess())) {
          System.out.println("Error on index: " + index + ", " + wsGroupSaveResult.getResultMetadata().getResultMessage());
        }
        index++;
      }
    }
    
    assertEquals(wsGroupSaveResults.getResultMetadata().getResultMessage(), "T", 
        wsGroupSaveResults.getResultMetadata().getSuccess());
    
    wsGroupSaveResultsArray = wsGroupSaveResults.getResults();
    wsGroupResult = wsGroupSaveResultsArray[2].getWsGroup();
    wsGroupDetailResult = wsGroupResult.getDetail();
    assertEquals(2, wsGroupDetailResult.getAttributeNames().length);
    assertEquals("attr3_1", wsGroupDetailResult.getAttributeNames()[0]);
    assertEquals("attr_1", wsGroupDetailResult.getAttributeNames()[1]);

    assertEquals(2, wsGroupDetailResult.getAttributeValues().length);
    assertEquals("val3_1", wsGroupDetailResult.getAttributeValues()[0]);
    assertEquals("val_1", wsGroupDetailResult.getAttributeValues()[1]);
    
    assertEquals(2, wsGroupDetailResult.getTypeNames().length);
    assertEquals("aType", wsGroupDetailResult.getTypeNames()[0]);
    assertEquals("aType3", wsGroupDetailResult.getTypeNames()[1]);
    
    assertEquals("T", wsGroupDetailResult.getHasComposite());
    assertEquals("F", wsGroupDetailResult.getIsCompositeFactor());
    assertEquals("complement", wsGroupDetailResult.getCompositeType());
    assertEquals("aStem:aGroupRight", wsGroupDetailResult.getLeftGroup().getName());
    assertEquals("aStem:aGroupLeft", wsGroupDetailResult.getRightGroup().getName());

    
    //######################################
    //now lets remove all that stuff
    
    //make new lookups since stuff is stored in there
    leftWsGroupLookup = new WsGroupLookup();
    leftWsGroupLookup.setGroupName("aStem:aGroupLeft");
    leftGroupToSave.setWsGroupLookup(leftWsGroupLookup);
    
    rightWsGroupLookup = new WsGroupLookup();
    rightWsGroupLookup.setGroupName("aStem:aGroupRight");
    rightGroupToSave.setWsGroupLookup(rightWsGroupLookup);
    
    wsGroupLookup = new WsGroupLookup();
    wsGroupLookup.setGroupName("aStem:aGroupInsert");
    wsGroupToSave.setWsGroupLookup(wsGroupLookup);
    
    wsGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());

    wsGroupDetail.setTypeNames(null);

    wsGroupDetail.setAttributeNames(null);
    wsGroupDetail.setAttributeValues(null);
    
    wsGroupDetail.setHasComposite("F");
    wsGroupDetail.setCompositeType(null);
    wsGroupDetail.setLeftGroup(null);
    wsGroupDetail.setRightGroup(null);
    
    //this was probably closed by last call
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperVersion.valueOfIgnoreCase("v1_4_000"), 
        new WsGroupToSave[]{leftGroupToSave, rightGroupToSave, wsGroupToSave}, 
        actAsSubjectLookup, GrouperTransactionType.NONE, true, null);
    
    if (!StringUtils.equals("T", wsGroupSaveResults.getResultMetadata().getSuccess())) {
      int index = 0;
      for (WsGroupSaveResult wsGroupSaveResult : GrouperUtil.nonNull(wsGroupSaveResults.getResults(), WsGroupSaveResult.class)) {
        if (!StringUtils.equals("T", wsGroupSaveResult.getResultMetadata().getSuccess())) {
          System.out.println("Error on index: " + index + ", " + wsGroupSaveResult.getResultMetadata().getResultMessage());
        }
        index++;
      }
    }
    
    assertEquals(wsGroupSaveResults.getResultMetadata().getResultMessage(), "T", 
        wsGroupSaveResults.getResultMetadata().getSuccess());
    
    wsGroupSaveResultsArray = wsGroupSaveResults.getResults();
    wsGroupResult = wsGroupSaveResultsArray[2].getWsGroup();
    wsGroupDetailResult = wsGroupResult.getDetail();
    assertEquals(0, GrouperUtil.length(wsGroupDetailResult.getAttributeNames()));

    assertEquals(0, GrouperUtil.length(wsGroupDetailResult.getAttributeValues()));
    
    assertEquals(0, GrouperUtil.length(wsGroupDetailResult.getTypeNames()));
    
    assertEquals("F", wsGroupDetailResult.getHasComposite());
    assertEquals("F", wsGroupDetailResult.getIsCompositeFactor());
    assertTrue(StringUtils.isBlank(wsGroupDetailResult.getCompositeType()));
    assertNull(wsGroupDetailResult.getLeftGroup());
    assertNull(wsGroupDetailResult.getRightGroup());

    //#######################
    //lets do it again...
    
    leftGroupToSave = new WsGroupToSave();
    leftGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());
    leftWsGroupLookup = new WsGroupLookup();
    leftWsGroupLookup.setGroupName("aStem:aGroupLeft");
    leftGroupToSave.setWsGroupLookup(leftWsGroupLookup);
    leftWsGroup = new WsGroup();
    leftWsGroup.setDescription("some group");
    leftWsGroup.setDisplayExtension("aGroupLeft");
    leftWsGroup.setName("aStem:aGroupLeft");
    leftGroupToSave.setWsGroup(leftWsGroup);
    
    rightGroupToSave = new WsGroupToSave();
    rightGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());
    rightWsGroupLookup = new WsGroupLookup();
    rightWsGroupLookup.setGroupName("aStem:aGroupRight");
    rightGroupToSave.setWsGroupLookup(rightWsGroupLookup);
    rightWsGroup = new WsGroup();
    rightWsGroup.setDescription("some group");
    rightWsGroup.setDisplayExtension("aGroupRight");
    rightWsGroup.setName("aStem:aGroupRight");
    rightGroupToSave.setWsGroup(rightWsGroup);

    
    
    
    wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setSaveMode(SaveMode.INSERT_OR_UPDATE.name());
    wsGroupLookup = new WsGroupLookup();
    wsGroupLookup.setGroupName("aStem:aGroupInsert");
    wsGroupToSave.setWsGroupLookup(wsGroupLookup);
    wsGroup = new WsGroup();
    wsGroup.setDescription("some group");
    wsGroup.setDisplayExtension("aGroupInsert");
    wsGroup.setName("aStem:aGroupInsert");
    
    wsGroupDetail = new WsGroupDetail();
    wsGroupDetail.setTypeNames(new String[]{"aType", "aType2"});

    wsGroupDetail.setAttributeNames(new String[]{"attr_1", "attr2_1"});
    wsGroupDetail.setAttributeValues(new String[]{"val_1", "val2_1"});
    
    wsGroup.setDetail(wsGroupDetail);
    wsGroupToSave.setWsGroup(wsGroup);
    
    wsGroupDetail.setHasComposite("T");
    wsGroupDetail.setCompositeType("UNION");
    wsGroupDetail.setLeftGroup(leftWsGroup);
    wsGroupDetail.setRightGroup(rightWsGroup);
    actAsSubjectLookup = new WsSubjectLookup(SubjectFinder.findRootSubject().getId(), null, null);

    //this was probably closed by last call
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperVersion.valueOfIgnoreCase("v1_4_000"), 
        new WsGroupToSave[]{leftGroupToSave, rightGroupToSave, wsGroupToSave}, 
        actAsSubjectLookup, GrouperTransactionType.NONE, true, null);
    
    if (!StringUtils.equals("T", wsGroupSaveResults.getResultMetadata().getSuccess())) {
      int index = 0;
      for (WsGroupSaveResult wsGroupSaveResult : GrouperUtil.nonNull(wsGroupSaveResults.getResults(), WsGroupSaveResult.class)) {
        if (!StringUtils.equals("T", wsGroupSaveResult.getResultMetadata().getSuccess())) {
          System.out.println("Error on index: " + index + ", " + wsGroupSaveResult.getResultMetadata().getResultMessage());
        }
        index++;
      }
    }
    
    assertEquals(wsGroupSaveResults.getResultMetadata().getResultMessage(), "T", 
        wsGroupSaveResults.getResultMetadata().getSuccess());
    
    wsGroupSaveResultsArray = wsGroupSaveResults.getResults();
    wsGroupResult = wsGroupSaveResultsArray[2].getWsGroup();
    wsGroupDetailResult = wsGroupResult.getDetail();
    assertEquals(2, wsGroupDetailResult.getAttributeNames().length);
    assertEquals("attr2_1", wsGroupDetailResult.getAttributeNames()[0]);
    assertEquals("attr_1", wsGroupDetailResult.getAttributeNames()[1]);

    assertEquals(2, wsGroupDetailResult.getAttributeValues().length);
    assertEquals("val2_1", wsGroupDetailResult.getAttributeValues()[0]);
    assertEquals("val_1", wsGroupDetailResult.getAttributeValues()[1]);
    
    assertEquals(2, wsGroupDetailResult.getTypeNames().length);
    assertEquals("aType", wsGroupDetailResult.getTypeNames()[0]);
    assertEquals("aType2", wsGroupDetailResult.getTypeNames()[1]);
    
    assertEquals("T", wsGroupDetailResult.getHasComposite());
    assertEquals("F", wsGroupDetailResult.getIsCompositeFactor());
    assertEquals("union", wsGroupDetailResult.getCompositeType());
    assertEquals("aStem:aGroupLeft", wsGroupDetailResult.getLeftGroup().getName());
    assertEquals("aStem:aGroupRight", wsGroupDetailResult.getRightGroup().getName());

    
  }
  
  /**
   * test member attribute read
   */
  public void testGetAttributeAssignmentsMember() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    Member member = MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ0, true);

    //test subject 0 can read
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

  
    
    AttributeAssignResult attributeAssignResult = member.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.member, null, null, null, 
        null, null, new WsSubjectLookup[]{new WsSubjectLookup(member.getSubjectId(), member.getSubjectSourceId(), null)}, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());

    assertEquals(SubjectTestHelper.SUBJ0_ID, wsGetAttributeAssignmentsResults.getWsSubjects()[0].getId());

  }
  
  /**
   * test membership attribute read
   */
  public void testGetAttributeAssignmentsMembership() {

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
      
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.imm_mem, null, null, null, 
        null, null, null, new WsMembershipLookup[]{new WsMembershipLookup(membership.getUuid())}, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());

    assertEquals(membership.getImmediateMembershipId(), wsGetAttributeAssignmentsResults.getWsMemberships()[0].getImmediateMembershipId());
    assertEquals(membership.getUuid(), wsGetAttributeAssignmentsResults.getWsMemberships()[0].getMembershipId());

  }
  
  /**
   * test stem attribute read
   */
  public void testGetAttributeAssignmentsStem() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.timestamp);
    attributeDef.store();
    

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  

    
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    Timestamp now = new Timestamp(System.currentTimeMillis());
    
    attributeAssign.getValueDelegate().assignValueTimestamp(now);
    
    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, null, null, null, 
        null, new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());

    assertEquals(stem.getName(), wsGetAttributeAssignmentsResults.getWsStems()[0].getName());

    WsAttributeAssignValue[] wsAttributeAssignValues = wsAttributeAssign.getWsAttributeAssignValues();
    
    assertEquals(1, GrouperUtil.length(wsAttributeAssignValues));
    
    assertEquals(GrouperServiceUtils.dateToString(now), wsAttributeAssignValues[0].getValueSystem());

  }

  /**
   * test getting permission assignments
   */
  public void testGetPermissionAssignments() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem top = StemFinder.findRootStem(grouperSession).addChildStem("top", "top display name");

    //parent implies child
    Role role = top.addChildRole("role", "role");
    Role role2 = top.addChildRole("role2", "role2");
        
    ((Group)role).addMember(SubjectTestHelper.SUBJ0);    
    ((Group)role2).addMember(SubjectTestHelper.SUBJ1);    
    
    AttributeDef attributeDef = top.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attrDefName = top.addChildAttributeDefName(attributeDef, "attrDefName", "attrDefName");
    AttributeDefName attrDefName2 = top.addChildAttributeDefName(attributeDef, "attrDefName2", "attrDefName2");

    attributeDef.getAttributeDefActionDelegate().addAction("action");
    attributeDef.getAttributeDefActionDelegate().addAction("action2");
    
    //subject 0 has a "role" permission of attributeDefName with "action" in 
    //subject 1 has a "role_subject" permission of attributeDefName2 with action2
    
    role.getPermissionRoleDelegate().assignRolePermission("action", attrDefName);
    AttributeAssignResult attributeAssignResult = role2.getPermissionRoleDelegate()
      .assignSubjectRolePermission("action2", attrDefName2, SubjectTestHelper.SUBJ1);

    AttributeDef assignOnAssignDef = top.addChildAttributeDef("assignOnAssignDef", AttributeDefType.limit);
    assignOnAssignDef.setAssignToGroupAssn(true);
    assignOnAssignDef.setAssignToEffMembershipAssn(true);
    assignOnAssignDef.setValueType(AttributeDefValueType.string);
    assignOnAssignDef.store();
    AttributeDefName assignOnAssignDefName = top.addChildAttributeDefName(
        assignOnAssignDef, "assignOnAssignDefName", "assignOnAssignDefName");

    AttributeValueResult attributeValueResult = attributeAssignResult.getAttributeAssign()
      .getAttributeValueDelegate().assignValueString(assignOnAssignDefName.getName(), "hey");
    
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = null;
    WsPermissionAssign wsPermissionAssign = null;
    
    //#################################################
    //you must pass in some criteria
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, null, null, null, 
        null, false, false, false, false, null, false, null, false, null, null);

    //new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}
    
    assertEquals("You must pass in some criteria", WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    
    //#################################################
    //invalid attrdef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:abc", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null);

    assertEquals("bad attr def", 
        WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //invalid action, needs something else
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, null, null, null, 
        new String[]{"action"}, false, false, false, false, null, false, null, false, null, null);

    assertEquals("need more than action", 
        WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //valid query for role assignment
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:attrDefName", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role", wsPermissionAssign.getPermissionType());
    assertEquals(role.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ0_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());
    
    //#################################################
    //valid query for role subject assignment
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:attrDefName2", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());
    
    //#################################################
    //valid query for attrdef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        null, false, false, false, false, null, false, null, false, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(2, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role", wsPermissionAssign.getPermissionType());
    assertEquals(role.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ0_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[1];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());
    
    //#################################################
    //valid query for lookup by role
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, null, new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, 
        null, false, false, false, false, null, false, null, false, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role", wsPermissionAssign.getPermissionType());
    assertEquals(role.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ0_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());
    
    //#################################################
    //valid query for lookup by subject
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, null, null, new WsSubjectLookup[]{new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null)}, 
        null, false, false, false, false, null, false, null, false, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role", wsPermissionAssign.getPermissionType());
    assertEquals(role.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ0_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());
    
    
    //#################################################
    //valid query for attrdef and action
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, true, false, false, false, null, false, null, false, null, null);

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNotNull(wsPermissionAssign.getDetail());
    assertEquals("0", wsPermissionAssign.getDetail().getActionDepth());
    assertNotNull(wsPermissionAssign.getDetail().getActionId());
    assertNull(wsPermissionAssign.getDetail().getAssignmentNotes());
    assertEquals("0", wsPermissionAssign.getDetail().getAttributeDefNameSetDepth());
    assertNull(wsPermissionAssign.getDetail().getDisabledTime());
    assertNull(wsPermissionAssign.getDetail().getEnabledTime());
    assertEquals("T", wsPermissionAssign.getDetail().getImmediateMembership());
    assertEquals("T", wsPermissionAssign.getDetail().getImmediatePermission());
    assertEquals(MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ1, true).getUuid(), wsPermissionAssign.getDetail().getMemberId());
    assertEquals("0", wsPermissionAssign.getDetail().getMembershipDepth());
    assertEquals("FALSE", wsPermissionAssign.getDetail().getPermissionDelegatable());
    assertEquals("If not a role assignment, this is not used, -1", "-1", wsPermissionAssign.getDetail().getRoleSetDepth());
    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeDefNames()));
    
    //#################################################
    //valid query for attrdef and action and attr def names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, true, false, false, null, false, null, false, null, null);

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeDefNames()));
    assertEquals(attrDefName2.getId(), wsGetPermissionAssignmentsResults.getWsAttributeDefNames()[0].getUuid());
    assertEquals(attrDefName2.getName(), wsGetPermissionAssignmentsResults.getWsAttributeDefNames()[0].getName());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeAssigns()));

    //#################################################
    //valid query for attrdef and action and attr def names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, true, false, null, false, null, false, null, null);

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeAssigns()));
    assertEquals("action2", wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[0].getAttributeAssignActionName());
    
    //#################################################
    //invalid query for attrdef and action and assign on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, false, true, null, false, null, false, null, null);

    assertEquals("need assignments to see assigns on assignments", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));

    
    //#################################################
    //valid query for attrdef and action and assign on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, true, true, null, false, null, false, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());

    assertEquals(2, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeAssigns()));
    assertEquals("action2", wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[0].getAttributeAssignActionName());
    assertEquals("assign", wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[1].getAttributeAssignActionName());
    assertEquals(attributeValueResult.getAttributeAssignResult().getAttributeAssign().getId(), wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[1].getId());
    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[1].getWsAttributeAssignValues()));
    assertEquals("hey", wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[1].getWsAttributeAssignValues()[0].getValueSystem());
    
    //#################################################
    //valid query for attrdef and action, and group detail 
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, false, false, null, false, null, true, null, null);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    wsPermissionAssign = wsGetPermissionAssignmentsResults.getWsPermissionAssigns()[0];
    
    assertEquals("action2", wsPermissionAssign.getAction());
    assertEquals(attributeDef.getId(), wsPermissionAssign.getAttributeDefId());
    assertEquals(attributeDef.getName(), wsPermissionAssign.getAttributeDefName());
    assertEquals(attrDefName2.getId(), wsPermissionAssign.getAttributeDefNameId());
    assertEquals(attrDefName2.getName(), wsPermissionAssign.getAttributeDefNameName());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getAttributeAssignId()));
    assertEquals("T", wsPermissionAssign.getEnabled());
    assertTrue(!StringUtils.isBlank(wsPermissionAssign.getMembershipId()));
    assertEquals("role_subject", wsPermissionAssign.getPermissionType());
    assertEquals(role2.getId(), wsPermissionAssign.getRoleId());
    assertEquals(role2.getName(), wsPermissionAssign.getRoleName());
    assertEquals("jdbc", wsPermissionAssign.getSourceId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, wsPermissionAssign.getSubjectId());
    assertNull(wsPermissionAssign.getDetail());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsGroups()));
    assertEquals("F", wsGetPermissionAssignmentsResults.getWsGroups()[0].getDetail().getIsCompositeFactor());
  }

  /** 
   * 
   */
  private final static GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase("v1_6_000");
  
  /**
   * test grouper privileges
   */
  public void testGetGrouperPrivilegesLite() {

//    groupName = "test:testPrivilegeIssue";
//    subjectIdWithAdminPriv = "test.subject.0";
//    subjectIdWithUpdatePriv = "test.subject.1";
//    subjectIdNoPrivs = "test.subject.2";
//    subjectIdMember = "test.subject.3";
//    grouperSession = GrouperSession.startRootSession();
//    group = new GroupSave(grouperSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
//    subjectWithAdminPriv = SubjectFinder.findByIdOrIdentifier(subjectIdWithAdminPriv, true);
//    subjectWithUpdatePriv = SubjectFinder.findByIdOrIdentifier(subjectIdWithUpdatePriv, true);
//    subjectNoPrivs = SubjectFinder.findByIdOrIdentifier(subjectIdNoPrivs, true);
//    subjectMember = SubjectFinder.findByIdOrIdentifier(subjectIdMember, true);
//    group.grantPriv(subjectWithAdminPriv, AccessPrivilege.ADMIN);
//    group.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE);
//    group.addMember(subjectMember);

    String groupName = "test:testPrivilegeIssue";
    String groupName2 = "test:testPrivilegeIssue2";
    String stemName = "test";
    String stemName2 = "test2";
    String subjectIdWithAdminPriv = "test.subject.0";
    String subjectIdWithUpdatePriv = "test.subject.1";
    String subjectIdNoPrivs = "test.subject.2";
    String subjectIdMember = "test.subject.3";
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(GrouperServiceUtils.testSession).assignName(stemName).assignCreateParentStemsIfNotExist(true).save();
    Stem stem2 = new StemSave(GrouperServiceUtils.testSession).assignName(stemName2).assignCreateParentStemsIfNotExist(true).save();
    Group group = new GroupSave(GrouperServiceUtils.testSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(GrouperServiceUtils.testSession).assignName(groupName2).assignCreateParentStemsIfNotExist(true).save();
    Subject subjectWithAdminPriv = SubjectFinder.findByIdOrIdentifier(subjectIdWithAdminPriv, true);
    Subject subjectWithUpdatePriv = SubjectFinder.findByIdOrIdentifier(subjectIdWithUpdatePriv, true);
    Subject subjectNoPrivs = SubjectFinder.findByIdOrIdentifier(subjectIdNoPrivs, true);
    Subject subjectMember = SubjectFinder.findByIdOrIdentifier(subjectIdMember, true);
    group.grantPriv(subjectWithAdminPriv, AccessPrivilege.ADMIN, false);
    group.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    group2.grantPriv(subjectWithAdminPriv, AccessPrivilege.ADMIN, false);
    group2.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE, false);
    group2.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group2.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    stem.grantPriv(subjectWithAdminPriv, NamingPrivilege.STEM);
    stem.grantPriv(subjectWithUpdatePriv, NamingPrivilege.CREATE);
    stem2.grantPriv(subjectWithAdminPriv, NamingPrivilege.STEM);
    stem2.grantPriv(subjectWithUpdatePriv, NamingPrivilege.CREATE);
    group.addMember(subjectMember);
    group2.addMember(subjectMember);

    Group whitelistGroup = new GroupSave(GrouperServiceUtils.testSession).assignName("test:whitelistGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    whitelistGroup.addMember(subjectNoPrivs);
    
    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = null;
    
    //#################################
    //## test that an unprivileged user cannot see privileges on a group

    GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithAdminPriv.getId(), null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that an unprivileged user cannot see privileges on a group

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithUpdatePriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithAdminPriv.getId(), null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that an unprivileged user in the whitelist can see privileges

    String oldName = GrouperWsConfig.getProperties().getProperty("ws.groupNameOfUsersWhoCanCheckAllPrivileges");
    GrouperWsConfig.getProperties().put("ws.groupNameOfUsersWhoCanCheckAllPrivileges", whitelistGroup.getName());
    
    GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithAdminPriv.getId(), null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        3, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //group.grantPriv(subjectWithAdminPriv, AccessPrivilege.ADMIN);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithAdminPriv, AccessPrivilege.ADMIN);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    if (oldName == null) {
      GrouperWsConfig.getProperties().remove("ws.groupNameOfUsersWhoCanCheckAllPrivileges");
    } else {
      GrouperWsConfig.getProperties().put("ws.groupNameOfUsersWhoCanCheckAllPrivileges", oldName);
    }

    //#################################
    //## test that an unprivileged user cannot see privileges on a group

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithUpdatePriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithAdminPriv.getId(), null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that a privileged user can see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithUpdatePriv.getId(), null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        3, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //group.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    //#################################
    //## test that an unprivileged user cannot see privileges on a group

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithUpdatePriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, null, null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that a privileged user can see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, null, null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);
    
    //there are a bunch of privs here...
    assertTrue(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        4 <= GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //group.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    //group.grantPriv(subjectWithAdminPriv, AccessPrivilege.ADMIN);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithAdminPriv, AccessPrivilege.ADMIN);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);


    //#################################
    //## test that a privileged user can see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithUpdatePriv.getId(), null, null, groupName, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        3, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //group.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    //group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    //#################################
    //## test that an unprivileged user cant see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithUpdatePriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithAdminPriv.getId(), null, null, null, 
        null, stemName, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that a privileged user can see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectWithUpdatePriv.getId(), null, null, null, 
        null, stemName, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        1, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //stem.grantPriv(subjectWithAdminPriv, NamingPrivilege.STEM);
    //stem.grantPriv(subjectWithUpdatePriv, NamingPrivilege.CREATE);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, subjectWithUpdatePriv, NamingPrivilege.CREATE);

    //#################################
    //## test that an unprivileged user cant see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithUpdatePriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, null, null, null, null, 
        null, stemName, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that a privileged user can see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, null, null, null, null, 
        null, stemName, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        3, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //stem.grantPriv(subjectWithAdminPriv, NamingPrivilege.STEM);
    //stem.grantPriv(subjectWithUpdatePriv, NamingPrivilege.CREATE);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, subjectWithUpdatePriv, NamingPrivilege.CREATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, subjectWithAdminPriv, NamingPrivilege.STEM);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, SubjectFinder.findRootSubject(), NamingPrivilege.STEM);

    //#################################
    //## test that an unprivileged user cant see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithUpdatePriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectIdWithAdminPriv, null, null, null, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //#################################
    //## test that a privileged user can see privileges

    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

    wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(
        GROUPER_VERSION, subjectIdWithUpdatePriv, null, null, null, 
        null, null, null, null, null, null, null, null, false, null, false, null, null, null, null);

    assertTrue(GrouperUtil.toStringForLog(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()), 
        10 <= GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    //stem.grantPriv(subjectWithAdminPriv, NamingPrivilege.STEM);
    //stem.grantPriv(subjectWithUpdatePriv, NamingPrivilege.CREATE);

    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, subjectWithUpdatePriv, NamingPrivilege.CREATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithUpdatePriv, AccessPrivilege.VIEW);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName, subjectWithUpdatePriv, AccessPrivilege.READ);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName2, subjectWithUpdatePriv, NamingPrivilege.CREATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName2, subjectWithUpdatePriv, AccessPrivilege.UPDATE);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName2, subjectWithUpdatePriv, AccessPrivilege.VIEW);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), groupName2, subjectWithUpdatePriv, AccessPrivilege.READ);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, subjectWithAdminPriv, NamingPrivilege.STEM);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, SubjectFinder.findRootSubject(), NamingPrivilege.STEM);

    
  }

  /**
   * make sure this privilege is in the array
   * @param wsGrouperPrivilegeResults
   * @param ownerName
   * @param subject
   * @param privilege
   */
  public static void assertHasPrivilege(WsGrouperPrivilegeResult[] wsGrouperPrivilegeResults, String ownerName, Subject subject, Privilege privilege) {
    for (WsGrouperPrivilegeResult wsGrouperPrivilegeResult : GrouperUtil.nonNull(wsGrouperPrivilegeResults, WsGrouperPrivilegeResult.class)) {
      if (Privilege.isAccess(privilege)) {
        
        if (StringUtils.equals(wsGrouperPrivilegeResult.getAllowed(), "T") 
            && StringUtils.equals(wsGrouperPrivilegeResult.getPrivilegeName(), privilege.getName())
            && StringUtils.equals(wsGrouperPrivilegeResult.getWsGroup().getName(), ownerName)
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getId(), subject.getId())
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getSourceId(), subject.getSourceId())) {
          return;
        }
        
      } else if (Privilege.isNaming(privilege)) {

        if (StringUtils.equals(wsGrouperPrivilegeResult.getAllowed(), "T") 
            && StringUtils.equals(wsGrouperPrivilegeResult.getPrivilegeName(), privilege.getName())
            && StringUtils.equals(wsGrouperPrivilegeResult.getWsStem().getName(), ownerName)
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getId(), subject.getId())
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getSourceId(), subject.getSourceId())) {
          return;
        }
        
      } else {
        throw new RuntimeException("Not expecting privilege: " + privilege);
      }
    }

    assertFalse(GrouperUtil.toStringForLog(wsGrouperPrivilegeResults), false);
    
  }
  

  
  /**
   * test group attribute read
   */
  public void testGetAttributeAssignmentsGroup() {
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign2").assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //test subject 0 can view and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    @SuppressWarnings("unused")
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();

    attributeAssign.getValueDelegate().addValueInteger(5L);
    attributeAssign.getValueDelegate().addValueInteger(15L);
    attributeAssign.getValueDelegate().addValueInteger(5L);
    
    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getAttributeAssignActionId(), wsAttributeAssign.getAttributeAssignActionId());
    assertEquals("assign", wsAttributeAssign.getAttributeAssignActionName());
    assertEquals("immediate", wsAttributeAssign.getAttributeAssignActionType());
    assertEquals("group", wsAttributeAssign.getAttributeAssignType());
    assertEquals(attributeAssign.getAttributeDefName().getAttributeDefId(), wsAttributeAssign.getAttributeDefId());
    assertEquals("test:testAttributeAssignDefNameDef", wsAttributeAssign.getAttributeDefName());
    assertEquals(attributeAssign.getAttributeDefNameId(), wsAttributeAssign.getAttributeDefNameId());
    assertEquals("test:testAttributeAssignDefName", wsAttributeAssign.getAttributeDefNameName());
    assertEquals(GrouperServiceUtils.dateToString(attributeAssign.getCreatedOn()), wsAttributeAssign.getCreatedOn());
    assertEquals(GrouperServiceUtils.dateToString(attributeAssign.getDisabledTime()), wsAttributeAssign.getDisabledTime());
    assertEquals("T", wsAttributeAssign.getEnabled());
    assertEquals(GrouperServiceUtils.dateToString(attributeAssign.getEnabledTime()), wsAttributeAssign.getEnabledTime());
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());
    assertEquals(GrouperServiceUtils.dateToString(attributeAssign.getLastUpdated()), wsAttributeAssign.getLastUpdated());
    assertEquals(attributeAssign.getNotes(), wsAttributeAssign.getNotes());
    assertEquals(null, wsAttributeAssign.getOwnerAttributeAssignId());
    assertEquals(null, wsAttributeAssign.getOwnerAttributeDefId());
    assertEquals(null, wsAttributeAssign.getOwnerAttributeDefName());
    assertEquals(attributeAssign.getOwnerGroupId(), wsAttributeAssign.getOwnerGroupId());
    assertEquals("test:groupTestAttrAssign", wsAttributeAssign.getOwnerGroupName());
    assertEquals(null, wsAttributeAssign.getOwnerMemberId());
    assertEquals(null, wsAttributeAssign.getOwnerMembershipId());
    assertEquals(null, wsAttributeAssign.getOwnerMemberSourceId());
    assertEquals(null, wsAttributeAssign.getOwnerMemberSubjectId());
    assertEquals(null, wsAttributeAssign.getOwnerStemId());
    assertEquals(null, wsAttributeAssign.getOwnerStemName());
    
    assertEquals(group.getName(), wsGetAttributeAssignmentsResults.getWsGroups()[0].getName());
    
    WsAttributeAssignValue[] wsAttributeAssignValues = wsAttributeAssign.getWsAttributeAssignValues();
    
    assertEquals(3, GrouperUtil.length(wsAttributeAssignValues));
    
    assertEquals("15", wsAttributeAssignValues[0].getValueSystem());
    assertEquals("5", wsAttributeAssignValues[1].getValueSystem());
    assertEquals("5", wsAttributeAssignValues[2].getValueSystem());
    
    //#################################################
    //you must pass in an attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals("You must pass in an attributeAssignType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());

    //###############################################
    //assignments on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, true, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(2, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, null, null, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by attributeDef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef.getName(), null)}, null, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by attributeDefName
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //#################################################
    //test security, valid query
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    
    //#################################################
    //test security, no results
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null), new WsGroupLookup(group2.getName(), null)}, 
        null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(0, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    
    //##################################################
    
    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
  }

  /**
   * test membership attribute read
   */
  public void testGetAttributeAssignmentsAnyMembership() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign").assignName("test:anyMembershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign2").assignName("test:anyMembershipTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //add one group to another to make effective membership and add attribute to that membership
    group1.addMember(group2.toSubject());
    group2.addMember(SubjectTestHelper.SUBJ0);
    
    Member member = MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ0, false);
    
    Membership membership = (Membership)MembershipFinder.findMemberships(GrouperUtil.toSet(group1.getId()), 
        GrouperUtil.toSet(member.getUuid()), null, null, FieldFinder.find("members", true), null, null, null, null, null).iterator().next()[0];
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegateEffMship().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.any_mem, null, null, null, 
        null, null, null, null, 
        new WsMembershipAnyLookup[]{new WsMembershipAnyLookup(new WsGroupLookup(group1.getName(), group1.getUuid()), 
            new WsSubjectLookup(member.getSubjectId(), member.getSubjectSourceId(), null))},
        null, null, false, null, false, null, false, null, null);
  
    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());
    
    assertEquals(group1.getName(), wsGetAttributeAssignmentsResults.getWsGroups()[0].getName());
    
    assertEquals(SubjectTestHelper.SUBJ0_ID, wsGetAttributeAssignmentsResults.getWsSubjects()[0].getId());
  
  }

  /**
   * test attribute def attribute read
   */
  public void testGetAttributeAssignmentsAttributeDef() {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDef attributeDefAssignTo = AttributeDefTest.exampleAttributeDefDb("test", "testAttributeDefAssignTo");
    
    
    AttributeAssignResult attributeAssignResult = attributeDefAssignTo.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.attr_def, null, null, null, 
        null, null, null, null, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefAssignTo.getName(), null)} ,
        null, false, null, false, null, false, null, null);
  
    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());
  
    assertEquals(attributeDefAssignTo.getName(), wsGetAttributeAssignmentsResults.getWsAttributeDefs()[1].getName());
    
  }

  /**
   * test group attribute read
   */
  public void testGetAttributeAssignmentsOnAssignmentsOnGroup() {
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign2").assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //test subject 0 can view and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    @SuppressWarnings("unused")
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
  
    
  
    //###############################################
    //test by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group_asgn, null, 
        new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef.getName(), null)}, 
        null, null, null, null, null, null, null, 
        null, false, null, false, null, false, null, null);
  
    assertTrue(!StringUtils.equals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode()));
    
    assertEquals(0, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
  
    //##################################################
    
    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
  }
  
  /**
   * test assign attributes
   */
  public void testAssignAttributes() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.setMultiAssignable(true);
    attributeDef.store();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    @SuppressWarnings("unused")
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign2").assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //Error case attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, AttributeAssignOperation.assign_attr, null, 
        null, null, null, null, null, null, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals("You must pass in an attributeAssignType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue("You must pass in an attributeAssignType", 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains("You need to pass in an attributeAssignType"));

    //Error case lookups and names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        AttributeAssignOperation.assign_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant do defName and assign lookup", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains("If you are passing in assign lookup ids to query, you cant specify attribute def names"));

    
    //cant pass in attr assign ids and owners
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Why is there more than one type of lookup?", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue("Why is there more than one type of lookup?", 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains("Why is there more than one type of lookup?"));

    
    //Need to pass in attribute assign operation
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, null, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Need to pass in attributeAssignOperation", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue("Need to pass in attributeAssignOperation", 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains("You need to pass in an attributeAssignOperation"));
    
    //Need to do assign or delete by id if passing by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.add_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("attributeAssignOperation must be assign_attr", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "attributeAssignOperation must be assign_attr or remove_attr"));


    //Need to do assign or delete by id if passing by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, null, AttributeAssignOperation.remove_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("but this operation was passed attributeAssignType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "but this operation was passed attributeAssignType"));

    //cant pass in actions if using attribute assign ids
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, new String[]{"assign"}, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in actions when using attribute assign id lookup", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in actions when using attribute assign id lookup"));

    
    //Cant pass in values when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
    //we dont use value formatted yet
    wsAttributeAssignValue.setValueSystem("hey");
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
        new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in values when deleting attributes", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in values when deleting attributes"));

    
    //Cant pass in assignmentNotes when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
        null, 
        "a", null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in assignmentNotes when deleting attributes", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentNotes when deleting attributes"));

    //Cant pass in assignmentEnabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
        null, 
        null, new Timestamp(0), null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in assignmentEnabledTime when deleting attributes", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentEnabledTime when deleting attributes"));

    //Cant pass in assignmentDisabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
        null, 
        null, null, new Timestamp(0), null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in assignmentDisabledTime when deleting attributes", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentDisabledTime when deleting attributes"));

    //Cant pass in delegatable when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, AttributeAssignDelegatable.TRUE, null, 
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in delegatable when deleting attributes", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in delegatable when deleting attributes"));

    //Cant pass in attributeAssignValueOperation when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, AttributeAssignValueOperation.remove_value, 
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("Cant pass in attributeAssignValueOperation when deleting attributes", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        wsAssignAttributesResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in attributeAssignValueOperation when deleting attributes"));

    
    //lets assign by id (should ignore)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok", WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    
    //lets delete by id (should delete)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, null, 
        null, null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals("delete an existing attribute is ok", WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    assertFalse("should be deleted", group.getAttributeDelegate().hasAttribute(attributeDefName));
    
    //lets assign by id and assign notes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    String id = attributeAssign.getId();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
        "notes", null, null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok", WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals("notes", attributeAssign.getNotes());

    //lets assign by id and assign enabled date
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    id = attributeAssign.getId();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
        null, new Timestamp(123L), null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals(123L, attributeAssign.getEnabledTime().getTime());

    //lets assign by id and assign disabled date
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    id = attributeAssign.getId();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
        null, null, new Timestamp(123L), null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals(123L, attributeAssign.getDisabledTime().getTime());

    //lets assign by id and assign delegatable
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    id = attributeAssign.getId();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
        null, null, null, AttributeAssignDelegatable.TRUE, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals(AttributeAssignDelegatable.TRUE, attributeAssign.getAttributeAssignDelegatable());

    //lets assign value
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
        null, null, null, null, AttributeAssignValueOperation.assign_value, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    List<Long> values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(1, values.size());
    assertEquals(123L, values.iterator().next().longValue());
    
    //lets delete a value by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    AttributeAssignValueResult attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setId(attributeAssignValueResult.getAttributeAssignValue().getId());
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
        null, null, null, null, AttributeAssignValueOperation.remove_value, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(0, GrouperUtil.nonNull(values).size());
    
    
    //lets delete a value by value
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
        null, null, null, null, AttributeAssignValueOperation.remove_value, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(0, GrouperUtil.nonNull(values).size());
    
    
    //lets add a value by value
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
        null, null, null, null, AttributeAssignValueOperation.add_value, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(1, wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getDeleted());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(2, GrouperUtil.nonNull(values).size());
    Iterator<Long> iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    assertEquals(123L, iterator.next().longValue());
    
    
    //lets assign a value by value
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
        null, null, null, null, AttributeAssignValueOperation.assign_value, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(1, GrouperUtil.nonNull(values).size());
    iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    
    
    //lets replace values
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    WsAttributeAssignValue wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("234");
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, 
        new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2}, 
        null, null, null, null, AttributeAssignValueOperation.assign_value, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("replace an existing attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(2, wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getDeleted());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(2, GrouperUtil.nonNull(values).size());
    iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    assertEquals(234L, iterator.next().longValue());
    
    
    //lets replace values but by group owner
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("234");
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2}, 
        null, null, null, null, AttributeAssignValueOperation.assign_value, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(2, wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getDeleted());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(2, GrouperUtil.nonNull(values).size());
    iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    assertEquals(234L, iterator.next().longValue());
    
    
    //lets assign a new by group owner
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    //attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    //attributeAssign = attributeAssignResult.getAttributeAssign();
    //attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    //id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("234");
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2}, 
        null, null, null, null, AttributeAssignValueOperation.assign_value, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(2, wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getDeleted());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    //assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(2, GrouperUtil.nonNull(values).size());
    iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    assertEquals(234L, iterator.next().longValue());
    
    //already assigned, assign again
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("345");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("234");
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2}, 
        null, null, null, null, AttributeAssignValueOperation.replace_values, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(3, wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[1].getDeleted());
    assertEquals("345", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[2].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[2].getChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[2].getDeleted());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, attributeAssigns.size());
    attributeAssign = attributeAssigns.iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();

    assertEquals(2, GrouperUtil.nonNull(values).size());
    iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    assertEquals(234L, iterator.next().longValue());
    
    //already assigned, add
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("345");
    id = attributeAssign.getId();
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.add_attr, 
        null, 
        null, null, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(2, attributeAssigns.size());
    
    //already assigned twice, remove
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().addAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("remove an attribute is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    assertEquals(2, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssigns()));
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(0, GrouperUtil.length(attributeAssigns));
    
    
    /*
     *  test security -- make sure privileges are checked when deleting an assignment
     */
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.add_attr, 
        null, 
        null, null, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    String attributeAssignId = attributeAssigns.iterator().next().getId();

    Subject subjectWithGroupAdminPriv = SubjectFinder.findByIdOrIdentifier("test.subject.0", true);
    Subject subjectWithAttrDefAdminPriv = SubjectFinder.findByIdOrIdentifier("test.subject.1", true);
    Subject subjectWithBothAdminPriv = SubjectFinder.findByIdOrIdentifier("test.subject.2", true);
    Subject subjectWithNoAdminPriv = SubjectFinder.findByIdOrIdentifier("test.subject.3", true);

    group.grantPriv(subjectWithGroupAdminPriv, AccessPrivilege.ADMIN, false);
    group.grantPriv(subjectWithBothAdminPriv, AccessPrivilege.ADMIN, false);
    group.grantPriv(subjectWithNoAdminPriv, AccessPrivilege.READ, false);
    group.grantPriv(subjectWithAttrDefAdminPriv, AccessPrivilege.READ, false);
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(subjectWithAttrDefAdminPriv, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(subjectWithBothAdminPriv, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(subjectWithNoAdminPriv, AttributeDefPrivilege.ATTR_READ, false);
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(subjectWithGroupAdminPriv, AttributeDefPrivilege.ATTR_READ, false);
    
    // 1.  Test removing assignment with no privileges
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithNoAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    
    // 2.  Test removing assignment with no privileges - specify attributeAssignId
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithNoAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        null, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, 
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId)}, 
        null, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    
    // 3.  Test removing assignment with group privileges only
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithGroupAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    
    // 4.  Test removing assignment with group privileges only - specify attributeAssignId
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithGroupAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        null, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, 
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId)}, 
        null, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    
    // 5.  Test removing assignment with attributeDef privileges only
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAttrDefAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    
    // 6.  Test removing assignment with attributeDef privileges only - specify attributeAssignId
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAttrDefAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        null, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, 
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId)}, 
        null, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));
    
    // 7.  Test removing assignment with appropriate privileges - specify attributeAssignId
    GrouperServiceUtils.testSession = GrouperSession.start(subjectWithBothAdminPriv);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GROUPER_VERSION, AttributeAssignType.group, 
        null, 
        AttributeAssignOperation.remove_attr, 
        null, 
        null, null, null, null, null, 
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId)}, 
        null, 
        null, null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(0, GrouperUtil.length(attributeAssigns));
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesStem() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.timestamp);
    attributeDef.store();
    

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  

    
    
    
    //lets assign to a stem
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    //group.getAttributeDelegate().removeAttribute(attributeDefName);
    //AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    //AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesMember() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    
    Member member = MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ0, true);
    
    
    //lets assign to a stem
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    //group.getAttributeDelegate().removeAttribute(attributeDefName);
    //AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    //AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.member, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        null, 
        null, null, null, null, null, null, 
        null, 
        null, 
        new WsSubjectLookup[]{new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), SubjectTestHelper.SUBJ0.getSourceId(), null)}, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = member.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesMembership() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
      
    
    
    //lets assign to a stem
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    //group.getAttributeDelegate().removeAttribute(attributeDefName);
    //AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    //AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.imm_mem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        null, 
        null, null, null, null, null, null, 
        null, 
        null, 
        null, new WsMembershipLookup[]{new WsMembershipLookup(membership.getUuid())}, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = membership.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesAnyMembership() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    group1.addMember(SubjectTestHelper.SUBJ0);

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ0, true);
    @SuppressWarnings("unused")
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
    
    
    //lets assign to a member
    //group.getAttributeDelegate().removeAttribute(attributeDefName);
    //AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    //AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.any_mem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        null, 
        null, null, null, null, null, null, 
        null, 
        null, 
        null, null, 
        new WsMembershipAnyLookup[]{new WsMembershipAnyLookup(new WsGroupLookup(group1.getName(), null), 
            new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), SubjectTestHelper.SUBJ0.getSourceId(), null))}, 
        null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = new GroupMember(group1, member).getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesAttributeDef() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDef attributeDefAssignTo = AttributeDefTest.exampleAttributeDefDb("test", "testAttributeDefAssignTo");
    
    
    //lets assign to a attribute def
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    //group.getAttributeDelegate().removeAttribute(attributeDefName);
    //AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    //AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.attr_def, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        null, 
        null, null, null, null, null, null, 
        null, 
        null, 
        null, null, 
        null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefAssignTo.getName(), null)}, 
        null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = attributeDefAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesAttrAssign() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setValueType(AttributeDefValueType.timestamp);
    attributeDef.store();
    
  
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    
    //lets assign to a stem
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    //group.getAttributeDelegate().removeAttribute(attributeDefName);
    //AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    //AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem_asgn, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.assign_attr, 
        null, 
        null, null, null, null, null, null, 
        null, 
        null, 
        null, null, 
        null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    assertEquals("F", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesResults.getWsAttributeAssignResults()[0].getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign2 = attributeAssign.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign2.getAttributeDefName().getName());
    
    
  }

  /**
   * test assign permissions
   */
  public void testAssignPermissions() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAttributeDefType(AttributeDefType.perm);
    attributeDef.setMultiValued(true);
    attributeDef.setMultiAssignable(true);
    attributeDef.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").assignTypeOfGroup(TypeOfGroup.role).save();
  
    Role role = group;
    
    
    //Error case attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsAssignPermissionsResults wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, null, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        false, null, false, null, null, null);
  
    assertEquals("You must pass in a permissionType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue("You must pass in a permissionType", 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("You need to pass in a permissionType"));
  
    //Error case lookups and names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Cant do defName and assign lookup", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("If you are passing in assign lookup ids to query, you cant specify attribute def names"));
  
    
    //cant pass in attr assign ids and owners
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Why is there more than one type of lookup?", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue("Why is there more than one type of lookup?", 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("Why is there more than one type of lookup?"));
  
    
    //Need to pass in permission assign operation
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        null, null, 
        null, null, null, null, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Need to pass in permissionAssignOperation", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue("Need to pass in permissionAssignOperation: " + wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("You need to pass in an permissionAssignOperation"));

    
    
    //Cant pass in attr def name ids when sending in attribute assign id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, new WsAttributeDefNameLookup[]{
            new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, new String[]{"a"}, null, 
        false, null, false, null, null, null);
    
      assertEquals("If you are passing in assign lookup ids to query, you cant specify attribute def names", 
          WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
          wsAssignPermissionsResults.getResultMetadata().getResultCode());
      assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
          wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "If you are passing in assign lookup ids to query, you cant specify attribute def names"));

      
          
    //Why is there more than one type of lookup?
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, new String[]{"a"}, null, 
        false, null, false, null, null, null);
    
      assertEquals("Why is there more than one type of lookup?", 
          WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
          wsAssignPermissionsResults.getResultMetadata().getResultCode());
      assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
          wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Why is there more than one type of lookup?"));

    
    //Cant pass in actions when sending in attribute assign id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, new String[]{"a"}, null, 
        false, null, false, null, null, null);
    
    assertEquals("Cant pass in actions when using attribute assign id lookup", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
          "Cant pass in actions when using attribute assign id lookup"));

    //Cant pass in assignmentNotes when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, "notes", 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Cant pass in assignmentNotes when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentNotes when deleting attributes"));

    //Cant pass in assignmentEnabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        new Timestamp(0), null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Cant pass in assignmentEnabledTime when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentEnabledTime when deleting attributes"));
    
    //Cant pass in assignmentDisabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        null, new Timestamp(0), null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Cant pass in assignmentDisabledTime when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentDisabledTime when deleting attributes"));
    

    //Cant pass in delegatable when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("Cant pass in delegatable when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in delegatable when deleting attributes"));
    
    //lets assign by id (should ignore)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    

    
    //lets delete by id (should delete)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("delete an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    


    //lets assign by id and assign notes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    String id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, "notes", 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    assertEquals("notes", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getWsAttributeAssigns()[0].getNotes());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals("notes", attributeAssign.getNotes());
    
    
    //lets assign by id and assign enabled
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        new Timestamp(123L), null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals(new Timestamp(123L), attributeAssign.getEnabledTime());
    
    
    //lets assign by id and assign disabled
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, new Timestamp(123L), null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals(new Timestamp(123L), attributeAssign.getDisabledTime());
    
    //lets assign by id and assign delegatable
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals(AttributeAssignDelegatable.TRUE, attributeAssign.getAttributeAssignDelegatable());
    
    
    //lets assign a new by group owner
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);


    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertNotNull(attributeAssign.getId());

    //lets assign a new by group owner and subject
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);

    WsMembershipAnyLookup wsMembershipAnyLookup = new WsMembershipAnyLookup(
        new WsGroupLookup(role.getName(), null), new WsSubjectLookup(SubjectTestHelper.SUBJ4_ID, null, null));

    //member must be in role to assign a permission
    role.addMember(SubjectTestHelper.SUBJ4, false);
    attributeDef.setAssignToEffMembership(true); 
    attributeDef.store();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), PermissionType.role_subject, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        null, new WsMembershipAnyLookup[]{wsMembershipAnyLookup}, null, null, 
        false, null, false, null, null, null);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    Member member4 = MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ4, false);
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
      .findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(role.getId(), member4.getUuid())), null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertNotNull(attributeAssigns.iterator().next().getId());

    

    
  }

  /**
   * test assign attributes
   */
  public void testAssignAttributesStemReplace() {
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.timestamp);
    attributeDef.store();
    
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName2");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToStem(true);
    attributeDef2.setValueType(AttributeDefValueType.timestamp);
    attributeDef2.getAttributeDefActionDelegate().configureActionList("a,b");
    attributeDef2.store();
    
  
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
  
    
    
    
    //lets assign to a stem
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    //###################### SIMPLE REPLACE
    for (AttributeAssign attributeAssign : stem.getAttributeDelegate().retrieveAssignments()) {
      attributeAssign.delete();
    }
        
    WsAssignAttributesResults wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.replace_attrs, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    WsAssignAttributeResult wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
    assertEquals("F", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());
    
    assertEquals(0, GrouperUtil.length(wsAssignAttributeResult.getWsAttributeAssignValueResults()));  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments()));

    //####################### REPLACE EXISTING
        
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        AttributeAssignOperation.replace_attrs, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, null, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(1, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
    
    assertEquals("F", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("F", wsAssignAttributeResult.getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = stem.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments()));
    
    //####################### REPLACE WITH ANOTHER
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName2.getName(), null)}, 
        AttributeAssignOperation.replace_attrs, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, new String[]{"a"}, null, false, null, false, null, null, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(2, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("F", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[1];
    
    assertEquals("test:testAttributeAssignDefName", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("T", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    assertEquals(0, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName)));
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2)));
    attributeAssign = stem.getAttributeDelegate().retrieveAssignments(attributeDefName2).iterator().next();
    assertEquals(attributeDefName2.getName(), attributeAssign.getAttributeDefName().getName());
    
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments()));

    
    //####################### REPLACE WITH ATTRIBUTE DEFS
    
    stem.getAttributeDelegate().assignAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName2.getName(), null)}, 
        AttributeAssignOperation.replace_attrs, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, new String[]{"b"}, null, false, null, false, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef2.getName(), null)}, null, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(2, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("T", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[1];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("F", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName)));
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2)));
    attributeAssign = (AttributeAssign)GrouperUtil.get(stem.getAttributeDelegate().retrieveAssignments(attributeDefName), 0);
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    attributeAssign = (AttributeAssign)GrouperUtil.get(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2), 0);
    assertEquals(attributeDefName2.getName(), attributeAssign.getAttributeDefName().getName());
    assertEquals("b", attributeAssign.getAttributeAssignAction().getName());
    
    assertEquals(2, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments()));

    //####################### REPLACE WITH ACTIONS
    
    stem.getAttributeDelegate().assignAttribute(attributeDefName);
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName2.getName(), null)}, 
        AttributeAssignOperation.replace_attrs, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, new String[]{"a"}, null, false, null, false, null, null, new String[]{"a", "b"}, null);
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(2, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("F", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[1];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("T", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName)));
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2)));
    attributeAssign = (AttributeAssign)GrouperUtil.get(stem.getAttributeDelegate().retrieveAssignments(attributeDefName), 0);
    assertEquals(attributeDefName.getName(), attributeAssign.getAttributeDefName().getName());
    attributeAssign = (AttributeAssign)GrouperUtil.get(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2), 0);
    assertEquals(attributeDefName2.getName(), attributeAssign.getAttributeDefName().getName());
    assertEquals("a", attributeAssign.getAttributeAssignAction().getName());
    
    assertEquals(2, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments()));

    
    //####################### REPLACE WITH TYPE
    
    stem.getAttributeDelegate().assignAttribute(attributeDefName);
    
    assertEquals(2, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments()));
    
    wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(
        GrouperVersion.valueOfIgnoreCase("v1_6_000"), AttributeAssignType.stem, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName2.getName(), null)}, 
        AttributeAssignOperation.replace_attrs, 
        null, 
        null, null, null, null, null, null, 
        null, 
        new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, 
        null, null, 
        null, null, null, new String[]{"b"}, null, false, null, false, null, null, null, new String[]{AttributeDefType.attr.name()});
    
    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesResults.getResultMetadata().getResultCode());
    assertEquals(3, GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[0];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("T", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());
    
    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[1];
    
    assertEquals("test:testAttributeAssignDefName2", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("F", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());

    wsAssignAttributeResult = wsAssignAttributesResults.getWsAttributeAssignResults()[2];
    
    assertEquals("test:testAttributeAssignDefName", wsAssignAttributeResult.getWsAttributeAssigns()[0].getAttributeDefNameName());
    assertEquals("T", wsAssignAttributeResult.getDeleted());
    assertEquals("F", wsAssignAttributeResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeResult.getChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    assertEquals(0, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName)));
    assertEquals(1, GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2)));
    attributeAssign = (AttributeAssign)GrouperUtil.get(stem.getAttributeDelegate().retrieveAssignments(attributeDefName2), 0);
    assertEquals(attributeDefName2.getName(), attributeAssign.getAttributeDefName().getName());
    assertEquals("b", attributeAssign.getAttributeAssignAction().getName());
    
    
  }

  
}
