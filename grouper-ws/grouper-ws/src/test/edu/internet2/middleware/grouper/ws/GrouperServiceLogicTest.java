/*
 * @author mchyzer
 * $Id: GrouperServiceLogicTest.java,v 1.2 2008-12-04 07:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.soap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperServiceLogicTest extends TestCase {

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
    TestRunner.run(new GrouperServiceLogicTest("testGetGrouperPrivilegesLite"));
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RestClientSettings.resetData();
    
    //help test logins from session opened from resetData
    GrouperServiceUtils.testSession = GrouperSession.staticGrouperSession();

  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
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
   * 
   */
  private static final GrouperWsVersion GROUPER_VERSION = GrouperWsVersion.v1_4_000;
  
  /**
   * test grouper privileges
   * @throws Exception 
   */
  public void testGetGrouperPrivilegesLite() throws Exception {

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
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getSourceId(), subject.getSource().getId())
            ) {
          return;
        }
        
      } else if (Privilege.isNaming(privilege)) {

        if (StringUtils.equals(wsGrouperPrivilegeResult.getAllowed(), "T") 
            && StringUtils.equals(wsGrouperPrivilegeResult.getPrivilegeName(), privilege.getName())
            && StringUtils.equals(wsGrouperPrivilegeResult.getWsStem().getName(), ownerName)
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getId(), subject.getId())
            && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getSourceId(), subject.getSource().getId())
            ) {
          return;
        }
        
      } else {
        throw new RuntimeException("Not expecting privilege: " + privilege);
      }
    }

    assertFalse(GrouperUtil.toStringForLog(wsGrouperPrivilegeResults), false);
    
  }
  

  
}
