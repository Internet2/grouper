/*
 * @author mchyzer
 * $Id: GrouperServiceLogicTest.java,v 1.2 2008-12-04 07:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import junit.textui.TestRunner;

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
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameTest;
import edu.internet2.middleware.grouper.attr.AttributeDefTest;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;


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
    TestRunner.run(new GrouperServiceLogicTest("testGetAttributeAssignmentsMembership"));
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
    
    GrouperWsVersion.assignCurrentClientVersion(GrouperWsVersion.v1_6_000);
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
    WsGroupSaveResults wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperWsVersion.v1_4_000, 
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

    wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperWsVersion.v1_4_000, 
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

    wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperWsVersion.v1_4_000, 
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
    
    wsGroupSaveResults = GrouperServiceLogic.groupSave(GrouperWsVersion.v1_4_000, 
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
        GrouperWsVersion.v1_6_000, AttributeAssignType.member, null, null, null, 
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
        GrouperWsVersion.v1_6_000, AttributeAssignType.imm_mem, null, null, null, 
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
    attributeDef.store();
    

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  

    
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.stem, null, null, null, 
        null, new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());

    assertEquals(stem.getName(), wsGetAttributeAssignmentsResults.getWsStems()[0].getName());

  }

  /**
   * test group attribute read
   */
  public void testGetAttributeAssignmentsGroup() {
    
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
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, null, null, null, 
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
    
    //#################################################
    //you must pass in an attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals("You must pass in an attributeAssignType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());

    //###############################################
    //assignments on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, true, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(2, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, null, null, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by attributeDef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef.getName(), null)}, null, 
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
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, null, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //#################################################
    //test security, valid query
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    
    //#################################################
    //test security, no results
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GrouperWsVersion.v1_6_000, AttributeAssignType.group, null, null, null, 
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
        GrouperWsVersion.v1_6_000, AttributeAssignType.any_mem, null, null, null, 
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
        GrouperWsVersion.v1_6_000, AttributeAssignType.attr_def, null, null, null, 
        null, null, null, null, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefAssignTo.getName(), null)} ,
        null, false, null, false, null, false, null, null);
  
    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[0];
    
    assertEquals(attributeAssign.getId(), wsAttributeAssign.getId());
  
    assertEquals(attributeDef.getName(), wsGetAttributeAssignmentsResults.getWsAttributeDefs()[1].getName());
    
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
        GrouperWsVersion.v1_6_000, AttributeAssignType.group_asgn, null, 
        new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef.getName(), null)}, 
        null, null, null, null, null, null, null, 
        null, false, null, false, null, false, null, null);
  
    assertTrue(!StringUtils.equals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode()));
    
    assertEquals(0, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
  
    //##################################################
    
    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
  }
  
  
}
