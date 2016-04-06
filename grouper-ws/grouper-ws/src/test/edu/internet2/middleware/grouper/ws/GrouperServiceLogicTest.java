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
/*
 * @author mchyzer
 * $Id: GrouperServiceLogicTest.java,v 1.2 2008-12-04 07:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
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
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefNameTest;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefTest;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueResult;
import edu.internet2.middleware.grouper.attr.value.AttributeValueResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAutoSourceAdapter;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.messaging.GrouperMessageHibernate;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionAssignOperation;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitElLogic;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchEntry;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchResult.WsAssignAttributeBatchResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeDefNameInheritanceResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeDefNameInheritanceResults.WsAssignAttributeDefNameInheritanceResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesBatchResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesBatchResults.WsAssignAttributesBatchResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDef;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefAssignActionResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteLiteResult.WsAttributeDefDeleteLiteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteResults.WsAttributeDefDeleteResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefName;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameToSave;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveResults.WsAttributeDefSaveResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefToSave;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResult.WsDeleteMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResults.WsDeleteMemberResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults.WsFindAttributeDefNamesResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefsResults.WsFindAttributeDefsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignActionsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignActionsResults.WsGetAttributeAssignActionsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResults.WsGetGroupsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersResults.WsGetMembersResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDetail;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.coresoap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults.WsHasMemberResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembership;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMessage;
import edu.internet2.middleware.grouper.ws.coresoap.WsMessageAcknowledgeResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMessageResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMessageResults.WsMessageResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsPermissionAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.coresoap.WsStem;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsInheritanceSetRelation;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;


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
    TestRunner.run(new GrouperServiceLogicTest("testAcknowledgeMessages"));
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

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
  }

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));
  
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
   * make sure a set of groups is similar to another by group name including order
   * @param set1 expected set
   * @param groupResultArray received set
   */
  public void assertGroupSetsAndOrder(Set<Group> set1, WsGroup[] groupResultArray) {
    int set1length = GrouperUtil.length(set1);
    int set2length = GrouperUtil.length(groupResultArray);
    if (set1length != set2length) {
      fail("Expecting groups of size: " + set1length + " but received size: " + set2length + ", expecting: "
          + GrouperUtil.toStringForLog(set1, 200) + ", but received: " + GrouperUtil.toStringForLog(groupResultArray, 200));
    }
    
    if (set1length == 0) {
      return;
    }
    
    int i=0;
    for (Group group : set1) {
      if (!StringUtils.equals(group.getName(), groupResultArray[i].getName())) {
        fail("Expecting index of set: " + i + " to be: " + group.getName() + ", but received: "
            + groupResultArray[i].getName() + ", expecting: " 
            + GrouperUtil.toStringForLog(set1, 200) 
            + ", but received: " + GrouperUtil.toStringForLog(groupResultArray, 200));
      }
      i++;
    }
  }

  /**
   * test get subjects
   */
  public void testGetSubjects() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
    
    WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());

    //###############################################
    //valid query for subject
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetSubjectsResults wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, null, null, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());

    assertTrue(GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()) + "", GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()) >= 10);

    WsSubject wsSubject = wsGetSubjectsResults.getWsSubjects()[0];

    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());

    //###############################################
    //valid query for subject in group
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, wsGroupLookup, null, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    
    WsGroup wsGroup = wsGetSubjectsResults.getWsGroup();
    wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());

    //###############################################
    //valid query for subject in group with search field
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, wsGroupLookup, null, null, false, 
        new WsParam[]{new WsParam("SearchStringEnumZeroIndexed", "0")});

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    
    wsGroup = wsGetSubjectsResults.getWsGroup();
    wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());

    //###############################################
    //valid query for subject in group with type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, wsGroupLookup, WsMemberFilter.Immediate, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    
    wsGroup = wsGetSubjectsResults.getWsGroup();
    wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());


    //###############################################
    //valid query for subject in group with wrong type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, wsGroupLookup, WsMemberFilter.NonImmediate, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(0, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    

    //###############################################
    //valid query for subject in group
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, wsGroupLookup, null, Group.getDefaultList(), false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    
    wsGroup = wsGetSubjectsResults.getWsGroup();
    wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());

    //###############################################
    //valid query for subject in group with field
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, null, wsGroupLookup, null, FieldFinder.find("admins", true), false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(0, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));

    //###############################################
    //valid query for subject in group with source
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, new String[]{SubjectTestHelper.SUBJ0.getSourceId()}, wsGroupLookup, null, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    
    wsGroup = wsGetSubjectsResults.getWsGroup();
    wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());

    //###############################################
    //valid query for subject in group with wrong source
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, null, "test.", false, null, null, new String[]{group1.toSubject().getSourceId()}, wsGroupLookup, null, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(0, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    

    //###############################################
    //valid query for subject in group by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetSubjectsResults = GrouperServiceLogic.getSubjects(
        GROUPER_VERSION, new WsSubjectLookup[] {new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null)}, 
        null, false, null, null, null, wsGroupLookup, null, null, false, null);

    assertEquals(wsGetSubjectsResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetSubjectsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()));
    
    wsGroup = wsGetSubjectsResults.getWsGroup();
    wsSubject = wsGetSubjectsResults.getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());


  }

  /**
   * test find groups with TypeOfGroup
   */
  public void testFindGroups() {

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    Group testGroup = new GroupSave(grouperSession).assignName("test:group").save();
    Group testRole = new GroupSave(grouperSession).assignName("test:role").assignTypeOfGroup(TypeOfGroup.role).save();
    Group testEntity = new GroupSave(grouperSession).assignName("test:entity").assignTypeOfGroup(TypeOfGroup.entity).save();
    
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:sub:group2").save();
    Group testRole2 = new GroupSave(grouperSession).assignName("test:sub:role2").assignTypeOfGroup(TypeOfGroup.role).save();
    Group testEntity2 = new GroupSave(grouperSession).assignName("test:sub:entity2").assignTypeOfGroup(TypeOfGroup.entity).save();
    
    testGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    testGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    
    testRole.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.UPDATE);
    testEntity.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.VIEW);

    testGroup2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    testRole2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    testEntity2.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);

    WsQueryFilter wsQueryFilter = new WsQueryFilter();
    wsQueryFilter.setAscending("T");
    wsQueryFilter.setSortString("name");
    wsQueryFilter.setStemName(":");
    wsQueryFilter.setStemNameScope(StemScope.ONE_LEVEL.name());
    wsQueryFilter.setQueryFilterType(WsQueryFilterType.FIND_BY_STEM_NAME.name());
    
    WsFindGroupsResults wsFindGroupsResults = GrouperServiceLogic.findGroups(
        GROUPER_VERSION, wsQueryFilter, null, false, null, null);

    assertEquals(wsFindGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsFindGroupsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsFindGroupsResults.getGroupResults()));

    wsQueryFilter.setStemNameScope(StemScope.ALL_IN_SUBTREE.name());
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsQueryFilter.assignGrouperSession(null);
    
    WsGroup[] wsGroups = GrouperServiceLogic.findGroups(
        GROUPER_VERSION, wsQueryFilter, null, false, null, null).getGroupResults();
    
    assertTrue(GrouperUtil.length(wsGroups) >= 6);

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsQueryFilter.assignGrouperSession(null);
    wsQueryFilter.setStemName("test");
    wsQueryFilter.setTypeOfGroups("entity, role");
    wsGroups = GrouperServiceLogic.findGroups(
        GROUPER_VERSION, wsQueryFilter, null, false, null, null).getGroupResults();
    
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testRole, testEntity2, testRole2), wsGroups);    
    
    assertEquals("entity", wsGroups[0].getTypeOfGroup());
    
    //try again with previous version
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsQueryFilter.assignGrouperSession(null);

    GrouperVersion v2_0_000 = GrouperVersion.valueOfIgnoreCase("v2_0_000");
        
    wsGroups = GrouperServiceLogic.findGroups(
        v2_0_000, wsQueryFilter, null, false, null, null).getGroupResults();
   
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testRole, testEntity2, testRole2), wsGroups);    
    
    assertTrue(StringUtils.isBlank(wsGroups[0].getTypeOfGroup()));
    
    //try again with previous version
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsQueryFilter.assignGrouperSession(null);

    wsGroups = GrouperServiceLogic.findGroups(
        GROUPER_VERSION, null, null, false, null, new WsGroupLookup[]{new WsGroupLookup(null, null, testRole.getIdIndex().toString()),
            new WsGroupLookup(null, null, testRole2.getIdIndex().toString())}).getGroupResults();
   
    assertGroupSetsAndOrder(GrouperUtil.toSet(testRole, testRole2), wsGroups);    
    
    
  }
  
  
  /**
   * test get groups
   */
  public void testGetGroups() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group2").assignName("test:group2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
      
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group3").assignName("test:group3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group4 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group4").assignName("test:group4").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ0);    
    group3.addMember(SubjectTestHelper.SUBJ0);    
    group4.addMember(SubjectTestHelper.SUBJ1);    
    
    group1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    
    ChangeLogTempToEntity.convertRecords();
    
    group3.deleteMember(SubjectTestHelper.SUBJ0);    
    ChangeLogTempToEntity.convertRecords();
    
    WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
    WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0};
    
    //###############################################
    //valid query
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    WsGetGroupsResults wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, WsMemberFilter.Immediate, 
        null, true, true, null, null, null, null, null, null, null, null, null, null, null, null, null);

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    WsSubject wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ0.getId());
    
    assertEquals(2, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    WsGroup wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    WsGroup wsGroup2 = wsGetGroupsResults.getResults()[0].getWsGroups()[1];
    
    assertEquals(wsGroup1.getUuid(), group1.getUuid());
    assertEquals(wsGroup1.getName(), group1.getName());
    assertEquals(wsGroup1.getDisplayName(), group1.getDisplayName());
    assertEquals(wsGroup2.getUuid(), group2.getUuid());
    assertEquals(wsGroup2.getName(), group2.getName());
    assertEquals(wsGroup2.getDisplayName(), group2.getDisplayName());

    //###############################################
    //privilege query
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ2.getId(), null, null);
    wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0};
    
    wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, WsMemberFilter.Immediate, 
        null, true, true, null, null, "updaters", null, null, null, null, null, null, null, null, null, null);

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ2.getId());
    
    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    
    assertEquals(wsGroup1.getUuid(), group1.getUuid());
    assertEquals(wsGroup1.getName(), group1.getName());
    assertEquals(wsGroup1.getDisplayName(), group1.getDisplayName());

  }
  
  
  /**
   * test get groups using point in time
   */
  public void testGetGroupsPIT() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group2").assignName("test:group2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
      
    Group group3 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group3").assignName("test:group3").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group4 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group4").assignName("test:group4").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ0);    
    group3.addMember(SubjectTestHelper.SUBJ0);    
    group4.addMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
    
    group3.deleteMember(SubjectTestHelper.SUBJ0);
    group2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
    WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0};
    
    //###############################################
    //valid query
    WsGetGroupsResults wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, null, 
        null, false, true, null, null, null, null, null, null, null, null, null, null, null, null, new Timestamp(new Date().getTime()));

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    WsSubject wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ0.getId());
    
    assertEquals(3, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    WsGroup wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    WsGroup wsGroup2 = wsGetGroupsResults.getResults()[0].getWsGroups()[1];
    WsGroup wsGroup3 = wsGetGroupsResults.getResults()[0].getWsGroups()[2];
    
    assertEquals(wsGroup1.getUuid(), group1.getUuid());
    assertEquals(wsGroup1.getName(), group1.getName());
    assertEquals(wsGroup1.getExtension(), group1.getExtension());
    assertEquals(wsGroup2.getUuid(), group2.getUuid());
    assertEquals(wsGroup2.getName(), group2.getName());
    assertEquals(wsGroup2.getExtension(), group2.getExtension());
    assertEquals(wsGroup3.getUuid(), group3.getUuid());
    assertEquals(wsGroup3.getName(), group3.getName());
    assertEquals(wsGroup3.getExtension(), group3.getExtension());
    
    //###############################################
    //valid query - this time specify scope
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    Group group5 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test2:group5").assignName("test2:group5").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    Group group6 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test2:sub:group6").assignName("test2:sub:group6").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    group5.addMember(SubjectTestHelper.SUBJ0);
    group6.addMember(SubjectTestHelper.SUBJ0);
    ChangeLogTempToEntity.convertRecords();
    
    wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, null, 
        null, false, true, null, null, null, "test2:", null, null, null, null, null, null, null, null, new Timestamp(new Date().getTime()));

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ0.getId());
    
    assertEquals(2, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    wsGroup2 = wsGetGroupsResults.getResults()[0].getWsGroups()[1];
    
    assertEquals(wsGroup1.getUuid(), group5.getUuid());
    assertEquals(wsGroup1.getName(), group5.getName());
    assertEquals(wsGroup1.getExtension(), group5.getExtension());
    assertEquals(wsGroup2.getUuid(), group6.getUuid());
    assertEquals(wsGroup2.getName(), group6.getName());
    assertEquals(wsGroup2.getExtension(), group6.getExtension());
    
    //###############################################
    //valid query - this time specify stem scope of sub
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Stem stem = StemFinder.findByName(GrouperServiceUtils.testSession, "test2", true);

    wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, null, 
        null, false, true, null, null, null, null, new WsStemLookup(stem.getName(), null), StemScope.ALL_IN_SUBTREE, null, null, null, null, null, null, new Timestamp(new Date().getTime()));

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ0.getId());
    
    assertEquals(2, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    wsGroup2 = wsGetGroupsResults.getResults()[0].getWsGroups()[1];
    
    assertEquals(wsGroup1.getUuid(), group5.getUuid());
    assertEquals(wsGroup1.getName(), group5.getName());
    assertEquals(wsGroup1.getExtension(), group5.getExtension());
    assertEquals(wsGroup2.getUuid(), group6.getUuid());
    assertEquals(wsGroup2.getName(), group6.getName());
    assertEquals(wsGroup2.getExtension(), group6.getExtension());
    
    //###############################################
    //valid query - this time specify stem scope of one
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, null, 
        null, false, true, null, null, null, null, new WsStemLookup(stem.getName(), null), StemScope.ONE_LEVEL, null, null, null, null, null, null, new Timestamp(new Date().getTime()));

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ0.getId());
    
    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    
    assertEquals(wsGroup1.getUuid(), group5.getUuid());
    assertEquals(wsGroup1.getName(), group5.getName());
    assertEquals(wsGroup1.getExtension(), group5.getExtension());
    
    //###############################################
    //valid query - this time specify field
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    ChangeLogTempToEntity.convertRecords();

    wsGetGroupsResults = GrouperServiceLogic.getGroups(
        GROUPER_VERSION, wsSubjectLookups, null, 
        null, false, true, null, null, "admins", "test:", null, null, null, null, null, null, null, null, new Timestamp(new Date().getTime()));

    assertEquals(wsGetGroupsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsGetGroupsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()));
    wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
    assertEquals(wsSubject.getId(), SubjectTestHelper.SUBJ0.getId());
    
    assertEquals(1, GrouperUtil.length(wsGetGroupsResults.getResults()[0].getWsGroups()));
    wsGroup1 = wsGetGroupsResults.getResults()[0].getWsGroups()[0];
    
    assertEquals(wsGroup1.getUuid(), group1.getUuid());
    assertEquals(wsGroup1.getName(), group1.getName());
    assertEquals(wsGroup1.getExtension(), group1.getExtension());
  }
  
  /**
   * test has member
   */
  public void testHasMember() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);    
    
    group1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);

    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
    
    WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
    WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
    WsSubjectLookup wsSubjectLookup1 = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null);
    WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0, wsSubjectLookup1};

    //###############################################
    //valid query
    WsHasMemberResults wsHasMemberResults = GrouperServiceLogic.hasMember(
        GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, WsMemberFilter.Immediate, 
        null, null, true, true, null, null, null, null);
    
    assertEquals(wsHasMemberResults.getResultMetadata().getResultMessage(),
        WsHasMemberResultsCode.SUCCESS.name(), 
        wsHasMemberResults.getResultMetadata().getResultCode());
    
    WsGroup wsGroup = wsHasMemberResults.getWsGroup();
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    
    assertEquals(2, GrouperUtil.length(wsHasMemberResults.getResults()));
    WsSubject wsSubject1 = wsHasMemberResults.getResults()[0].getWsSubject();
    WsSubject wsSubject2 = wsHasMemberResults.getResults()[1].getWsSubject();
    
    if (wsSubject1.getId().equals(SubjectTestHelper.SUBJ0.getId())) {
      assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject2.getId());
      assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
      assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
    } else {
      assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject2.getId());
      assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject1.getId());
      assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
      assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
    }

  
    //###############################################
    //privilege query
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    WsSubjectLookup wsSubjectLookup2 = new WsSubjectLookup(SubjectTestHelper.SUBJ2.getId(), null, null);
    wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0, wsSubjectLookup2};
    
    wsHasMemberResults = GrouperServiceLogic.hasMember(  
        GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, WsMemberFilter.Immediate, 
        null, FieldFinder.find("updaters", true), true, true, null, null, null, null);
    
    assertEquals(wsHasMemberResults.getResultMetadata().getResultMessage(),
        WsHasMemberResultsCode.SUCCESS.name(), 
        wsHasMemberResults.getResultMetadata().getResultCode());
    
    wsGroup = wsHasMemberResults.getWsGroup();
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());

    assertEquals(2, GrouperUtil.length(wsHasMemberResults.getResults()));
    wsSubject1 = wsHasMemberResults.getResults()[0].getWsSubject();
    wsSubject2 = wsHasMemberResults.getResults()[1].getWsSubject();

    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject1.getId());
    assertEquals(SubjectTestHelper.SUBJ2.getId(), wsSubject2.getId());
    assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
    assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
  }
  
  /**
   * test has member using point in time
   */
  public void testHasMemberPIT() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add/delete member
    group1.addMember(SubjectTestHelper.SUBJ0);
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(SubjectTestHelper.SUBJ0);    
    ChangeLogTempToEntity.convertRecords();
    
    //###############################################
    //valid query
    {
      WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
      WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
      WsSubjectLookup wsSubjectLookup1 = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null);
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0, wsSubjectLookup1};
      
      WsHasMemberResults wsHasMemberResults = GrouperServiceLogic.hasMember(
          GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, WsMemberFilter.All, 
          null, null, false, true, null, null, null, new Timestamp(new Date().getTime()));
      
      assertEquals(wsHasMemberResults.getResultMetadata().getResultMessage(),
          WsHasMemberResultsCode.SUCCESS.name(), 
          wsHasMemberResults.getResultMetadata().getResultCode());
      
      WsGroup wsGroup = wsHasMemberResults.getWsGroup();
      assertEquals(group1.getUuid(), wsGroup.getUuid());
      assertEquals(group1.getName(), wsGroup.getName());
      
      assertEquals(2, GrouperUtil.length(wsHasMemberResults.getResults()));
      WsSubject wsSubject1 = wsHasMemberResults.getResults()[0].getWsSubject();
      WsSubject wsSubject2 = wsHasMemberResults.getResults()[1].getWsSubject();
      
      if (wsSubject1.getId().equals(SubjectTestHelper.SUBJ0.getId())) {
        assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject2.getId());
        assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
        assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
      } else {
        assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject2.getId());
        assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject1.getId());
        assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
        assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
      }
    }
    //###############################################
    //another valid query -- this time with the group deleted
    {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      group1.delete();
      ChangeLogTempToEntity.convertRecords();
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
      WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
      WsSubjectLookup wsSubjectLookup1 = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null);
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0, wsSubjectLookup1};
      
      WsHasMemberResults wsHasMemberResults = GrouperServiceLogic.hasMember(
          GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, WsMemberFilter.All, 
          null, null, false, true, null, null, null, new Timestamp(new Date().getTime()));
      
      assertEquals(wsHasMemberResults.getResultMetadata().getResultMessage(),
          WsHasMemberResultsCode.SUCCESS.name(), 
          wsHasMemberResults.getResultMetadata().getResultCode());
      
      WsGroup wsGroup = wsHasMemberResults.getWsGroup();
      assertEquals(group1.getUuid(), wsGroup.getUuid());
      assertEquals(group1.getName(), wsGroup.getName());
      
      assertEquals(2, GrouperUtil.length(wsHasMemberResults.getResults()));
      WsSubject wsSubject1 = wsHasMemberResults.getResults()[0].getWsSubject();
      WsSubject wsSubject2 = wsHasMemberResults.getResults()[1].getWsSubject();
      
      if (wsSubject1.getId().equals(SubjectTestHelper.SUBJ0.getId())) {
        assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject2.getId());
        assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
        assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
      } else {
        assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject2.getId());
        assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject1.getId());
        assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
        assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
      }
    }
    
    //###############################################
    //another valid query -- this time recreate the deleted group and find by name
    {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();
      
      // add/delete member
      group2.addMember(SubjectTestHelper.SUBJ1);
      ChangeLogTempToEntity.convertRecords();
      
      group2.deleteMember(SubjectTestHelper.SUBJ1);    
      ChangeLogTempToEntity.convertRecords();
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(group2.getName(), null);
      WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
      WsSubjectLookup wsSubjectLookup1 = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null);
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0, wsSubjectLookup1};
     
      WsHasMemberResults wsHasMemberResults = GrouperServiceLogic.hasMember(
          GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, WsMemberFilter.All, 
          null, null, false, true, null, null, null, new Timestamp(new Date().getTime()));
      
      assertEquals(wsHasMemberResults.getResultMetadata().getResultMessage(),
          WsHasMemberResultsCode.SUCCESS.name(), 
          wsHasMemberResults.getResultMetadata().getResultCode());
      
      WsGroup wsGroup = wsHasMemberResults.getWsGroup();
      assertEquals(group2.getUuid(), wsGroup.getUuid());
      assertEquals(group2.getName(), wsGroup.getName());
      
      assertEquals(2, GrouperUtil.length(wsHasMemberResults.getResults()));
      WsSubject wsSubject1 = wsHasMemberResults.getResults()[0].getWsSubject();
      WsSubject wsSubject2 = wsHasMemberResults.getResults()[1].getWsSubject();
      
      if (wsSubject1.getId().equals(SubjectTestHelper.SUBJ1.getId())) {
        assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject2.getId());
        assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
        assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
      } else {
        assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject1.getId());
        assertEquals(SubjectTestHelper.SUBJ1.getId(), wsSubject2.getId());
        assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER, wsHasMemberResults.getResults()[0].resultCode());
        assertEquals(WsHasMemberResultCode.IS_MEMBER, wsHasMemberResults.getResults()[1].resultCode());
      }
    }
    
    //###############################################
    //invalid query - bad group name
    {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();

      WsGroupLookup wsGroupLookup = new WsGroupLookup("test:bogus", null);
      WsSubjectLookup wsSubjectLookup0 = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
      WsSubjectLookup wsSubjectLookup1 = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null);
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup0, wsSubjectLookup1};
     
      WsHasMemberResults wsHasMemberResults = GrouperServiceLogic.hasMember(
          GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, WsMemberFilter.All, 
          null, null, false, true, null, null, null, new Timestamp(new Date().getTime()));
      
      assertEquals(wsHasMemberResults.getResultMetadata().getResultMessage(),
          WsHasMemberResultsCode.GROUP_NOT_FOUND.name(), 
          wsHasMemberResults.getResultMetadata().getResultCode());
    }
  }
  
  /**
   * test get members
   */
  public void testGetMembers() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);    
    
    group1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
    
    WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
    
    //###############################################
    //valid query
    WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
        GROUPER_VERSION, wsGroupLookups, WsMemberFilter.Immediate, null, 
        Group.getDefaultList(), true, true, null, null, null, null, null, null, null, null, null);

    assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetMembersResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()));
    assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
    
    WsGroup wsGroup = wsGetMembersResults.getResults()[0].getWsGroup();
    WsSubject wsSubject = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());

    //should work on permissions
    //###############################################
    //valid query
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    wsGetMembersResults = GrouperServiceLogic.getMembers(
        GROUPER_VERSION, wsGroupLookups, WsMemberFilter.Immediate, null, 
        FieldFinder.find("updaters", true), true, true, null, null, null, 
        null, null, null, null, null, null);

    assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetMembersResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()));
    assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
    
    wsGroup = wsGetMembersResults.getResults()[0].getWsGroup();
    wsSubject = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ2.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ2.getSourceId(), wsSubject.getSourceId());

  
  }

  /**
   * test get members using point in time
   */
  public void testGetMembersPIT() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();

    //###############################################
    //valid query
    {
      WsGroupLookup wsGroupLookup = new WsGroupLookup(null, group1.getUuid());
      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
      
      WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
          GROUPER_VERSION, wsGroupLookups, WsMemberFilter.All, null, 
          Group.getDefaultList(), false, true, null, null, null, null, new Timestamp(new Date().getTime()), null, null, null, null);
  
      assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
          WsGetMembersResultsCode.SUCCESS.name(), 
          wsGetMembersResults.getResultMetadata().getResultCode());
      
      assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()));
      assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
      
      WsGroup wsGroup = wsGetMembersResults.getResults()[0].getWsGroup();
      WsSubject wsSubject1 = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
      WsSubject wsSubject2 = wsGetMembersResults.getResults()[0].getWsSubjects()[1];
      
      assertEquals(group1.getUuid(), wsGroup.getUuid());
      assertEquals(group1.getName(), wsGroup.getName());
      assertFalse(wsSubject1.getId().equals(wsSubject2.getId()));
      assertTrue(SubjectTestHelper.SUBJ0.getId().equals(wsSubject1.getId()) || SubjectTestHelper.SUBJ0.getId().equals(wsSubject2.getId()));
      assertTrue(SubjectTestHelper.SUBJ1.getId().equals(wsSubject1.getId()) || SubjectTestHelper.SUBJ1.getId().equals(wsSubject2.getId()));
      assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject1.getSourceId());
      assertEquals(SubjectTestHelper.SUBJ1.getSourceId(), wsSubject2.getSourceId());
    }
    
    //###############################################
    //another valid query -- this time with the group deleted
    {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      group1.delete();
      ChangeLogTempToEntity.convertRecords();
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(null, group1.getUuid());
      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
  
      WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
          GROUPER_VERSION, wsGroupLookups, WsMemberFilter.All, null, 
          Group.getDefaultList(), false, true, null, null, null, null, new Timestamp(new Date().getTime()), null, null, null, null);
  
      assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
          WsGetMembersResultsCode.SUCCESS.name(), 
          wsGetMembersResults.getResultMetadata().getResultCode());
      
      assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()));
      assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
      
      WsGroup wsGroup = wsGetMembersResults.getResults()[0].getWsGroup();
      WsSubject wsSubject1 = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
      WsSubject wsSubject2 = wsGetMembersResults.getResults()[0].getWsSubjects()[1];
      
      assertEquals(group1.getUuid(), wsGroup.getUuid());
      assertEquals(group1.getName(), wsGroup.getName());
      assertFalse(wsSubject1.getId().equals(wsSubject2.getId()));
      assertTrue(SubjectTestHelper.SUBJ0.getId().equals(wsSubject1.getId()) || SubjectTestHelper.SUBJ0.getId().equals(wsSubject2.getId()));
      assertTrue(SubjectTestHelper.SUBJ1.getId().equals(wsSubject1.getId()) || SubjectTestHelper.SUBJ1.getId().equals(wsSubject2.getId()));
      assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject1.getSourceId());
      assertEquals(SubjectTestHelper.SUBJ1.getSourceId(), wsSubject2.getSourceId());
    }
    
    //###############################################
    //another valid query -- this time recreate the deleted group and find by name
    {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();
    
      // add member
      group2.addMember(SubjectTestHelper.SUBJ2);
      ChangeLogTempToEntity.convertRecords();
  
      WsGroupLookup wsGroupLookup = new WsGroupLookup(group2.getName(), null);
      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
      
      WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
          GROUPER_VERSION, wsGroupLookups, WsMemberFilter.All, null, 
          Group.getDefaultList(), false, true, null, null, null, null, new Timestamp(new Date().getTime()), null, null, null, null);
  
      assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
          WsGetMembersResultsCode.SUCCESS.name(), 
          wsGetMembersResults.getResultMetadata().getResultCode());
      
      assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()));
      
      WsGroup wsGroup1;
      WsGroup wsGroup2;
      WsSubject wsSubject1;
      WsSubject wsSubject2;
      WsSubject wsSubject3;
      
      if (GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()) == 1) {
        assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()[1].getWsSubjects()));
        wsGroup1 = wsGetMembersResults.getResults()[1].getWsGroup();
        wsGroup2 = wsGetMembersResults.getResults()[0].getWsGroup();
        wsSubject1 = wsGetMembersResults.getResults()[1].getWsSubjects()[0];
        wsSubject2 = wsGetMembersResults.getResults()[1].getWsSubjects()[1];
        wsSubject3 = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
      } else {
        assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
        assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()[1].getWsSubjects()));
        wsGroup1 = wsGetMembersResults.getResults()[0].getWsGroup();
        wsGroup2 = wsGetMembersResults.getResults()[1].getWsGroup();
        wsSubject1 = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
        wsSubject2 = wsGetMembersResults.getResults()[0].getWsSubjects()[1];
        wsSubject3 = wsGetMembersResults.getResults()[1].getWsSubjects()[0];
      }
      
      assertEquals(group1.getUuid(), wsGroup1.getUuid());
      assertEquals(group1.getName(), wsGroup1.getName());
      assertEquals(group2.getUuid(), wsGroup2.getUuid());
      assertEquals(group2.getName(), wsGroup2.getName());
      
      assertEquals(SubjectTestHelper.SUBJ2.getId(), wsSubject3.getId());
      assertFalse(wsSubject1.getId().equals(wsSubject2.getId()));
      assertTrue(SubjectTestHelper.SUBJ0.getId().equals(wsSubject1.getId()) || SubjectTestHelper.SUBJ0.getId().equals(wsSubject2.getId()));
      assertTrue(SubjectTestHelper.SUBJ1.getId().equals(wsSubject1.getId()) || SubjectTestHelper.SUBJ1.getId().equals(wsSubject2.getId()));      
    }
    
    //###############################################
    //invalid query - bad group name
    {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();

      WsGroupLookup wsGroupLookup = new WsGroupLookup("test:bogus", null);
      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
      
      WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
          GROUPER_VERSION, wsGroupLookups, WsMemberFilter.All, null, 
          Group.getDefaultList(), false, true, null, null, null, null, new Timestamp(new Date().getTime()), null, null, null, null);
  
      assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
          WsGetMembersResultsCode.PROBLEM_GETTING_MEMBERS.name(),
          wsGetMembersResults.getResultMetadata().getResultCode());
    }
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
        GROUPER_VERSION, AttributeAssignType.member, null, null, null, 
        null, null, new WsSubjectLookup[]{new WsSubjectLookup(member.getSubjectId(), member.getSubjectSourceId(), null)}, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

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
        GROUPER_VERSION, AttributeAssignType.imm_mem, null, null, null, 
        null, null, null, new WsMembershipLookup[]{new WsMembershipLookup(membership.getUuid())}, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

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
        GROUPER_VERSION, AttributeAssignType.stem, null, null, null, 
        null, new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

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
   * test attributeAssignActions read
   */
  public void testGetAttributeAssignActions() {
    AttributeDefName nameOfAttributeDef1 = AttributeDefNameTest
        .exampleAttributeDefNameDb("test", "testAttributeAssignDefName");

    final AttributeDef attributeDef1 = nameOfAttributeDef1.getAttributeDef();

    attributeDef1.setAssignToGroup(false);
    attributeDef1.setAssignToStem(true);
    attributeDef1.setValueType(AttributeDefValueType.timestamp);
    attributeDef1.getAttributeDefActionDelegate().addAction("read");
    attributeDef1.getAttributeDefActionDelegate().addAction("delete");
    attributeDef1.store();

    AttributeDefName nameOfAttributeDef2 = AttributeDefNameTest
        .exampleAttributeDefNameDb("test", "testAttributeDefName2");

    final AttributeDef attributeDef2 = nameOfAttributeDef2.getAttributeDef();

    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToStem(true);
    attributeDef2.setValueType(AttributeDefValueType.timestamp);
    attributeDef2.getAttributeDefActionDelegate().addAction("view");
    attributeDef2.getAttributeDefActionDelegate().addAction("edit");
    attributeDef2.getAttributeDefActionDelegate().addAction("remove");
    attributeDef2.getAttributeDefActionDelegate().addAction("delete");
    attributeDef2.store();

    // invalid query: no attribute def passed
    WsGetAttributeAssignActionsResults wsGetAttributeAssignActionsResults = GrouperServiceLogic
        .
        getAttributeAssignActions(GROUPER_VERSION, null, null, null, null);

    assertEquals("You need to pass in wsAttributeDefLookups",
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(),
        wsGetAttributeAssignActionsResults.getResultMetadata().getResultCode());

    // invalid query: wrong attribute def name passed
    WsAttributeDefLookup[] wsAttributeDefLookups = new WsAttributeDefLookup[] { new WsAttributeDefLookup(
        "wrongDefName", null) };
    wsGetAttributeAssignActionsResults = GrouperServiceLogic.
        getAttributeAssignActions(GROUPER_VERSION, wsAttributeDefLookups, null, null,
            null);

    assertEquals("Attribute Def not found",
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(),
        wsGetAttributeAssignActionsResults.getResultMetadata().getResultCode());

    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsAttributeDefLookups = new WsAttributeDefLookup[] { new WsAttributeDefLookup(
        attributeDef1.getName(), null) };
    wsGetAttributeAssignActionsResults = GrouperServiceLogic.
        getAttributeAssignActions(GROUPER_VERSION, wsAttributeDefLookups, null, null,
            null);

    assertEquals(wsGetAttributeAssignActionsResults.getResultMetadata()
        .getResultMessage(),
        WsGetAttributeAssignActionsResultsCode.SUCCESS.name(),
        wsGetAttributeAssignActionsResults.getResultMetadata().getResultCode());

    assertEquals(3, GrouperUtil.length(wsGetAttributeAssignActionsResults
        .getWsAttributeAssignActionTuples()));

    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsAttributeDefLookups = new WsAttributeDefLookup[] {
        new WsAttributeDefLookup(attributeDef1.getName(), null),
        new WsAttributeDefLookup(attributeDef2.getName(), null) };
    wsGetAttributeAssignActionsResults = GrouperServiceLogic.
        getAttributeAssignActions(GROUPER_VERSION, wsAttributeDefLookups, null, null,
            null);

    assertEquals(wsGetAttributeAssignActionsResults.getResultMetadata()
        .getResultMessage(),
        WsGetAttributeAssignActionsResultsCode.SUCCESS.name(),
        wsGetAttributeAssignActionsResults.getResultMetadata().getResultCode());

    assertEquals(8, GrouperUtil.length(wsGetAttributeAssignActionsResults
        .getWsAttributeAssignActionTuples()));

    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignActionsResults = GrouperServiceLogic.
        getAttributeAssignActions(GROUPER_VERSION, wsAttributeDefLookups, new String[] {
            "assign", "read", "edit" }, null, null);

    assertEquals(wsGetAttributeAssignActionsResults.getResultMetadata()
        .getResultMessage(),
        WsGetAttributeAssignActionsResultsCode.SUCCESS.name(),
        wsGetAttributeAssignActionsResults.getResultMetadata().getResultCode());

    assertEquals(4, GrouperUtil.length(wsGetAttributeAssignActionsResults
        .getWsAttributeAssignActionTuples()));

  }
  
  /**
   * test assign actions to attribute def 
   */
  public void testAssignAttributeDefActions() {
    AttributeDefName nameOfAttributeDef1 = AttributeDefNameTest
        .exampleAttributeDefNameDb("test", "testAttributeAssignDefName");

    final AttributeDef attributeDef1 = nameOfAttributeDef1.getAttributeDef();

    attributeDef1.setAssignToGroup(false);
    attributeDef1.setAssignToStem(true);
    attributeDef1.setValueType(AttributeDefValueType.timestamp);
    attributeDef1.getAttributeDefActionDelegate().addAction("read");
    attributeDef1.getAttributeDefActionDelegate().addAction("delete");
    attributeDef1.store();

    String[] actions = new String[] { "view", "read", "remove" };

    // invalid query: no attribute def passed
    WsAttributeDefAssignActionResults wsAttributeDefAssignActionResults = GrouperServiceLogic
        .
        assignAttributeDefActions(GROUPER_VERSION, null, actions, true, null, null, null);

    assertEquals("You need to pass in wsAttributeDefLookup",
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(),
        wsAttributeDefAssignActionResults.getResultMetadata().getResultCode());

    // invalid query: wrong attribute def name passed
    WsAttributeDefLookup wsAttributeDefLookup = new WsAttributeDefLookup("wrongDefName",
        null);
    wsAttributeDefAssignActionResults = GrouperServiceLogic.
        assignAttributeDefActions(GROUPER_VERSION, wsAttributeDefLookup, actions, true,
            null, null, null);

    assertEquals("Attribute Def not found",
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(),
        wsAttributeDefAssignActionResults.getResultMetadata().getResultCode());

    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsAttributeDefLookup = new WsAttributeDefLookup(attributeDef1.getName(), null);
    wsAttributeDefAssignActionResults = GrouperServiceLogic.
        assignAttributeDefActions(GROUPER_VERSION, wsAttributeDefLookup, actions, true,
            null, null, null);

    assertEquals(
        wsAttributeDefAssignActionResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignActionsResultsCode.SUCCESS.name(),
        wsAttributeDefAssignActionResults.getResultMetadata().getResultCode());

    assertEquals(3, GrouperUtil.length(wsAttributeDefAssignActionResults.getActions()));

    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsAttributeDefLookup = new WsAttributeDefLookup(attributeDef1.getName(), null);
    wsAttributeDefAssignActionResults = GrouperServiceLogic.
        assignAttributeDefActions(GROUPER_VERSION, wsAttributeDefLookup, actions, true,
            true, null, null);

    assertEquals(
        wsAttributeDefAssignActionResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignActionsResultsCode.SUCCESS.name(),
        wsAttributeDefAssignActionResults.getResultMetadata().getResultCode());

    assertEquals(8, GrouperUtil.length(wsAttributeDefAssignActionResults.getActions()));

    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsAttributeDefLookup = new WsAttributeDefLookup(attributeDef1.getName(), null);
    wsAttributeDefAssignActionResults = GrouperServiceLogic.
        assignAttributeDefActions(GROUPER_VERSION, wsAttributeDefLookup, actions, false,
            null, null, null);

    assertEquals(
        wsAttributeDefAssignActionResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignActionsResultsCode.SUCCESS.name(),
        wsAttributeDefAssignActionResults.getResultMetadata().getResultCode());

    assertEquals(3, GrouperUtil.length(wsAttributeDefAssignActionResults.getActions()));

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

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.customEl.limitName", assignOnAssignDefName.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.customEl.logicClass", PermissionLimitElLogic.class.getName());
    
    AttributeValueResult attributeValueResult = attributeAssignResult.getAttributeAssign()
      .getAttributeValueDelegate().assignValueString(assignOnAssignDefName.getName(), "amount < 50");
    
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = null;
    WsPermissionAssign wsPermissionAssign = null;
    
    //#################################################
    //you must pass in some criteria
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, null, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, false, null, PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS, null, false);

    //new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}
    
    assertEquals("You must pass in some criteria", WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    
    //#################################################
    //invalid attrdef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:abc", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, 
        false, null, null, null, false);

    assertEquals("bad attr def", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //invalid action, needs something else
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, null, null, null, 
        new String[]{"action"}, false, false, false, false, null, false, null, false, null, 
        null, null, null, false, null, null, null, false);

    assertEquals("This is ok: " + wsGetPermissionAssignmentsResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //valid query for role assignment
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:attrDefName", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, false, null, null, null, false);

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
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:attrDefName2", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, false, null, null, null, false);

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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, false, null, null, null, false);

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
        GROUPER_VERSION, null, null, new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, false, null, null, null, false);

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
        GROUPER_VERSION, null, null, null, new WsSubjectLookup[]{new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null)}, 
        null, false, false, false, false, null, false, null, false, null, null, null, null, false, null, null, null, false);

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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, true, false, false, false, null, false, null, false, null, null, null, null, 
        false, null, null, null, false);

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
    //valid query for everything

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, null, null, new WsSubjectLookup[]{new WsSubjectLookup(SubjectTestHelper.SUBJ1_ID, null, null)}, 
        null, false, true, false, false, null, false, null, false, null, null, null, null, 
        false, null, null, null, true);

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

    assertEquals(2, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeDefNames()));
//    assertEquals(attrDefName2.getId(), wsGetPermissionAssignmentsResults.getWsAttributeDefNames()[0].getUuid());
//    assertEquals(attrDefName2.getName(), wsGetPermissionAssignmentsResults.getWsAttributeDefNames()[0].getName());
//
//    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeAssigns()));
    

    
    //#################################################
    //valid query for attrdef and action and attr def names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, true, false, false, null, false, null, false, null, null, null, null, 
        false, null, null, null, false);

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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, true, false, null, false, null, false, null, null, null, 
        null, false, null, null, null, false);

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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, false, true, null, false, null, false, null, null, null, 
        null, false, null, null, null, false);

    assertEquals("need assignments to see assigns on assignments", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));

    
    //#################################################
    //valid query for attrdef and action and assign on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, true, true, null, false, null, false, null, 
        null, null, null, false, null, null, null, false);

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
    assertEquals("amount < 50", wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[1].getWsAttributeAssignValues()[0].getValueSystem());
    
    //#################################################
    //valid query for attrdef and action, and group detail 
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, false, false, null, false, null, true, null, null, 
        null, null, false, null, null, null, false);

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
   * test getting permission assignments using point in time
   */
  public void testGetPermissionAssignmentsPIT() {
    
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

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.customEl.limitName", assignOnAssignDefName.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.customEl.logicClass", PermissionLimitElLogic.class.getName());

    AttributeValueResult attributeValueResult = attributeAssignResult.getAttributeAssign()
      .getAttributeValueDelegate().assignValueString(assignOnAssignDefName.getName(), "amount < 50");

    ChangeLogTempToEntity.convertRecords();

    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = null;
    WsPermissionAssign wsPermissionAssign = null;
    
    //#################################################
    //you must pass in some criteria
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, null, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, new Timestamp(new Date().getTime()),
        false, null, null, null, false);

    //new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}
    
    assertEquals("You must pass in some criteria", WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    
    //#################################################
    //invalid attrdef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:abc", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, null, null, new Timestamp(new Date().getTime()), 
        false, null, null, null, false);

    assertEquals("bad attr def", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //invalid action, needs something else
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, null, null, null, 
        new String[]{"action"}, false, false, false, false, null, false, null, false, null, null, null, 
        new Timestamp(new Date().getTime()), false, null, null, null, false);

    assertEquals("need more than action", 
        WsGetAttributeAssignmentsResultsCode.EXCEPTION.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //valid query for role assignment
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:attrDefName", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, null, 
        null, null, new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup("top:attrDefName2", null)}, null, null, 
        null, false, false, false, false, null, false, null, false, 
        null, null, null, new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        null, false, false, false, false, null, false, null, false, null, 
        null, null, new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, null, null, new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, 
        null, false, false, false, false, null, false, null, false, 
        null, null, null, new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, null, null, null, new WsSubjectLookup[]{new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null)}, 
        null, false, false, false, false, null, false, null, 
        false, null, null, null, new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, true, false, false, false, null, 
        false, null, false, null, null, null, new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
    assertNull(wsPermissionAssign.getDetail().getImmediateMembership());
    assertNull(wsPermissionAssign.getDetail().getImmediatePermission());
    assertEquals(MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ1, true).getUuid(), wsPermissionAssign.getDetail().getMemberId());
    assertEquals("0", wsPermissionAssign.getDetail().getMembershipDepth());
    assertNull(wsPermissionAssign.getDetail().getPermissionDelegatable());
    assertEquals("If not a role assignment, this is not used, -1", "-1", wsPermissionAssign.getDetail().getRoleSetDepth());
    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsAttributeDefNames()));
    
    //#################################################
    //valid query for attrdef and action and attr def names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, true, false, 
        false, null, false, null, false, null, null, null, 
        new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, true, 
        false, null, false, null, false, null, null, null, 
        new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, 
        false, true, null, false, null, false, null, null, 
        null, new Timestamp(new Date().getTime()), false, null, null, null, false);

    assertEquals("need assignments to see assigns on assignments", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));

    
    //#################################################
    //valid query for attrdef and action and assign on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, true, true, 
        null, false, null, false, null, null, null, 
        new Timestamp(new Date().getTime()), false, null, null, null, false);

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
    assertNull(wsPermissionAssign.getEnabled());
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
    assertEquals("amount < 50", wsGetPermissionAssignmentsResults.getWsAttributeAssigns()[1].getWsAttributeAssignValues()[0].getValueSystem());
    
    //#################################################
    //invalid query for attrdef and action, and group detail 
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, 
        false, false, null, false, null, true, null, null, 
        null, new Timestamp(new Date().getTime()), false, null, null, null, false);

    assertEquals("group detail invalid for point in time queries", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    
    //#################################################
    //invalid query for attrdef and action, disabled permissions
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, false, 
        false, null, false, null, false, null, "A", null, 
        new Timestamp(new Date().getTime()), false, null, null, null, false);

    assertEquals("disabled permissions invalid for point in time queries", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
    
    //#################################################
    //invalid query for attrdef and action, disabled permissions
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(
        GROUPER_VERSION, new WsAttributeDefLookup[]{new WsAttributeDefLookup("top:attributeDef", null)}, null, null, null, 
        new String[]{"action2"}, false, false, 
        false, false, null, false, null, false, null, "F", 
        null, new Timestamp(new Date().getTime()), false, null, null, null, false);

    assertEquals("disabled permissions invalid for point in time queries", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetPermissionAssignmentsResults.getResultMetadata().getResultCode());

    assertEquals(0, GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns()));
  }

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

    String oldName = GrouperWsConfig.retrieveConfig().propertiesOverrideMap().get("ws.groupNameOfUsersWhoCanCheckAllPrivileges");
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.groupNameOfUsersWhoCanCheckAllPrivileges", whitelistGroup.getName());
    
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
      GrouperWsConfig.retrieveConfig().propertiesOverrideMap().remove("ws.groupNameOfUsersWhoCanCheckAllPrivileges");
    } else {
      GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.groupNameOfUsersWhoCanCheckAllPrivileges", oldName);
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
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, subjectWithAdminPriv, NamingPrivilege.STEM_ADMIN);
    assertHasPrivilege(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults(), stemName, SubjectFinder.findRootSubject(), NamingPrivilege.STEM_ADMIN);

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

    
  }


  /**
   * make sure this privilege is in the array
   * @param wsGrouperPrivilegeResults
   * @param ownerName
   * @param subject
   * @param privilege
   */
  public void assertHasPrivilege(WsGrouperPrivilegeResult[] wsGrouperPrivilegeResults, String ownerName, Subject subject, Privilege privilege) {
    for (WsGrouperPrivilegeResult wsGrouperPrivilegeResult : GrouperUtil.nonNull(wsGrouperPrivilegeResults, WsGrouperPrivilegeResult.class)) {
      if (Privilege.isAccess(privilege)) {
        
        if (StringUtils.equals(wsGrouperPrivilegeResult.getAllowed(), "T") 
            && StringUtils.equals(wsGrouperPrivilegeResult.getPrivilegeName(), privilege.getName())
            && StringUtils.equals(wsGrouperPrivilegeResult.getWsGroup().getName(), ownerName)
            && (StringUtils.equals(wsGrouperPrivilegeResult.getWsSubject().getId(), subject.getId())
            && StringUtils.equals(wsGrouperPrivilegeResult.getWsSubject().getSourceId(), subject.getSourceId())
            || (StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getId(), subject.getId())
                && StringUtils.equals(wsGrouperPrivilegeResult.getOwnerSubject().getSourceId(), subject.getSourceId())))) {
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

    fail("\nExpected user: " + subject.getSourceId() + " - " + subject.getId() + " to have " + privilege.getName() + " on " + ownerName + ", but these were the privs:\n" 
        + GrouperUtil.toStringForLog(wsGrouperPrivilegeResults));
    
  }
  
  /**
   * make sure this membership is in the array
   * @param wsMemberships
   * @param ownerName
   * @param subject
   * @param field
   */
  public void assertHasMembership(WsMembership[] wsMemberships, String ownerName, Subject subject, Field field) {
    for (WsMembership wsMembership : GrouperUtil.nonNull(wsMemberships, WsMembership.class)) {
      if (field.isGroupListField()) {
        
        if (StringUtils.equals(wsMembership.getListName(), field.getName())
            && StringUtils.equals(wsMembership.getGroupName(), ownerName)
            && StringUtils.equals(wsMembership.getSubjectId(), subject.getId())
            && StringUtils.equals(wsMembership.getSubjectSourceId(), subject.getSourceId())) {
          return;
        }
        
      } else if (field.isStemListField()) {

        if (StringUtils.equals(wsMembership.getListName(), field.getName())
            && StringUtils.equals(wsMembership.getOwnerStemName(), ownerName)
            && StringUtils.equals(wsMembership.getSubjectId(), subject.getId())
            && StringUtils.equals(wsMembership.getSubjectSourceId(), subject.getSourceId())) {
          return;
        }
        
      } else if (field.isAttributeDefListField()) {

        if (StringUtils.equals(wsMembership.getListName(), field.getName())
            && StringUtils.equals(wsMembership.getOwnerNameOfAttributeDef(), ownerName)
            && StringUtils.equals(wsMembership.getSubjectId(), subject.getId())
            && StringUtils.equals(wsMembership.getSubjectSourceId(), subject.getSourceId())) {
          return;
        }
        
      } else {
        throw new RuntimeException("Not expecting field: " + field);
      }
    }

    fail(GrouperUtil.toStringForLog(wsMemberships));
    
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
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
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
        GROUPER_VERSION, AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

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
        GROUPER_VERSION, null, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    assertEquals("You must pass in an attributeAssignType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());

    //###############################################
    //assignments on assignments
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, true, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(2, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.group, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, null, null, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by attributeDef
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.group, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef.getName(), null)}, null, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //###############################################
    //test by attributeDefName
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.group, null, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        null, null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    //#################################################
    //test security, valid query
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    
    //#################################################
    //test security, no results
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.group, null, null, null, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null), new WsGroupLookup(group2.getName(), null)}, 
        null, null, null, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);

    //cant read one of the groups
    assertEquals(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(0, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));

    
    //##################################################
    
    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
  }

  /**
   * see which services a subject is a user of.
   */
  public void testListServicesForUser() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDefName jiraService = null;
    AttributeDefName confluenceService = null;    
    AttributeDefName directoryService = null;    
    try {
      
      //create three services, one directly in, one hierarchical, one the user is not in
      AttributeDef jiraServiceDef = new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jiraServiceDefinition").assignToStem(true).save();
      
      jiraService = new AttributeDefNameSave(grouperSession, jiraServiceDef)
        .assignCreateParentStemsIfNotExist(true)
        .assignName("apps:jira:jiraService").assignDisplayExtension("Central IT production Jira issue tracker").save();
      
      //jira group
      Group jiraGroup = new GroupSave(grouperSession)
        .assignName("apps:jira:groups:admins").assignCreateParentStemsIfNotExist(true).save();
      
      jiraGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      jiraGroup.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.READ);
      jiraGroup.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
      
      jiraGroup.addMember(SubjectTestHelper.SUBJ0);
      jiraGroup.addMember(SubjectTestHelper.SUBJ1);
      
      //the jira group has the jira service tag
      Stem jiraStem = StemFinder.findByUuid(grouperSession, jiraGroup.getStemId(), true);
      jiraStem.getAttributeDelegate().assignAttribute(jiraService);
      
      AttributeDef confluenceServiceDef = new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:confluence:confluenceServiceDefinition").assignToStem(true).save();
      
      confluenceService = new AttributeDefNameSave(grouperSession, confluenceServiceDef)
        .assignCreateParentStemsIfNotExist(true)
        .assignName("apps:confluence:confluenceService").assignDisplayExtension("Central IT production Confluence wiki").save();
    
      Group confluenceGroup = new GroupSave(grouperSession)
        .assignName("apps:confluence:editors").assignCreateParentStemsIfNotExist(true).save();

      confluenceGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      confluenceGroup.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.READ);
      confluenceGroup.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
      confluenceGroup.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);

      confluenceGroup.addMember(SubjectTestHelper.SUBJ1);
      confluenceGroup.addMember(SubjectTestHelper.SUBJ2);

      //the confluence folder has the confluence service tag
      Stem confluenceFolder = StemFinder.findByName(grouperSession, "apps:confluence", true);
      confluenceFolder.getAttributeDelegate().assignAttribute(confluenceService);
      
      AttributeDef directoryServiceDef = new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:directory:directoryServiceDefinition").assignToStem(true).save();
      
      directoryService = new AttributeDefNameSave(grouperSession, directoryServiceDef)
        .assignCreateParentStemsIfNotExist(true)
        .assignName("apps:directory:directoryService").assignDisplayExtension("MySchool directory").save();
      
      Group directoryGroup = new GroupSave(grouperSession)
        .assignName("apps:directory:users").assignCreateParentStemsIfNotExist(true).save();

      directoryGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      directoryGroup.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.READ);
      directoryGroup.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.READ);
      directoryGroup.addMember(SubjectTestHelper.SUBJ2);
      directoryGroup.addMember(SubjectTestHelper.SUBJ3);

      //the confluence folder has the confluence service tag
      Stem directoryFolder = StemFinder.findByName(grouperSession, "apps:directory", true);
      directoryFolder.getAttributeDelegate().assignAttribute(directoryService);

      
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }

    // ##################### subject 5 can see that subject 0 and 1 are in the jira service...

    
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ5);

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null), ServiceRole.user);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 1, GrouperUtil.length(wsAttributeDefNames));
      assertEquals(jiraService.getId(), wsAttributeDefNames[0].getUuid());
      
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
    // ##################### subject 5 can see that subject 0 and 1 are in the jira service (LITE)...

    
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ5);

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNamesLite(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, null, null, null, null, null, null, null, SubjectTestHelper.SUBJ0_ID,null, null, ServiceRole.user);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 1, GrouperUtil.length(wsAttributeDefNames));
      assertEquals(jiraService.getId(), wsAttributeDefNames[0].getUuid());
      
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
    // ##################### subject 6 can see that subject 0 and 1 are in the jira service...

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ6);

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null), ServiceRole.user);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 1, GrouperUtil.length(wsAttributeDefNames));
      assertEquals(jiraService.getId(), wsAttributeDefNames[0].getUuid());

    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }

    // ##################### subject 7 cannot see that subject 0 and 1 are in the jira service...

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ7);

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null), ServiceRole.user);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 0, GrouperUtil.length(wsAttributeDefNames));
      //assertEquals(jiraService.getId(), wsAttributeDefNames[0].getUuid());
      
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
    // ##################### GrouperSystem can see that subject 0 and 1 are in the jira service...

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(SubjectTestHelper.SUBJ0_ID, null, null), ServiceRole.user);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 1, GrouperUtil.length(wsAttributeDefNames));
      assertEquals(jiraService.getId(), wsAttributeDefNames[0].getUuid());

    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
    // ##################### GrouperSystem can see that subject 7 is admin of the confluence service...

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(null, null, SubjectTestHelper.SUBJ7_IDENTIFIER), ServiceRole.admin);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 1, GrouperUtil.length(wsAttributeDefNames));
      assertEquals(confluenceService.getId(), wsAttributeDefNames[0].getUuid());
      
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
    // ##################### subject 8 can see that subject 7 is admin of the confluence service...

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ8);

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(null, null, SubjectTestHelper.SUBJ7_IDENTIFIER), ServiceRole.admin);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 1, GrouperUtil.length(wsAttributeDefNames));
      assertEquals(confluenceService.getId(), wsAttributeDefNames[0].getUuid());

    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
    // ##################### subject 9 cannot see that subject 7 is admin of the confluence service...

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ9);

    try {

      WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
          GROUPER_VERSION, "%", null, null, null, null, null, null, null, null, 
          null, null, null, null, new WsSubjectLookup(null, null, SubjectTestHelper.SUBJ7_IDENTIFIER), ServiceRole.admin);

      WsAttributeDefName[] wsAttributeDefNames = wsFindAttributeDefNamesResults.getAttributeDefNameResults();
      
      assertEquals(GrouperUtil.toStringForLog(wsAttributeDefNames), 0, GrouperUtil.length(wsAttributeDefNames));

    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    }
    
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
        GROUPER_VERSION, AttributeAssignType.any_mem, null, null, null, 
        null, null, null, null, 
        new WsMembershipAnyLookup[]{new WsMembershipAnyLookup(new WsGroupLookup(group1.getName(), group1.getUuid()), 
            new WsSubjectLookup(member.getSubjectId(), member.getSubjectSourceId(), null))},
        null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);
  
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
        GROUPER_VERSION, AttributeAssignType.attr_def, null, null, null, 
        null, null, null, null, null, new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefAssignTo.getName(), null)} ,
        null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);
  
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
        GROUPER_VERSION, AttributeAssignType.group_asgn, null, 
        new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDef.getName(), null)}, 
        null, null, null, null, null, null, null, 
        null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);
  
    assertEquals(WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
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
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, null, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.add_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.stem, null, AttributeAssignOperation.remove_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.remove_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, null, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, new WsAttributeAssignValue[]{wsAttributeAssignValue}, 
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
        GROUPER_VERSION, AttributeAssignType.group, null, AttributeAssignOperation.assign_attr, 
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
        GROUPER_VERSION, AttributeAssignType.group, 
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
        GROUPER_VERSION, AttributeAssignType.group, 
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
        GROUPER_VERSION, AttributeAssignType.group, 
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
        GROUPER_VERSION, AttributeAssignType.group, 
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
        GROUPER_VERSION, AttributeAssignType.group, 
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
    group.grantPriv(subjectWithNoAdminPriv, AccessPrivilege.GROUP_ATTR_READ, false);
    group.grantPriv(subjectWithAttrDefAdminPriv, AccessPrivilege.GROUP_ATTR_READ, false);
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
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId, null)}, 
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
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId, null)}, 
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
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId, null)}, 
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
        new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId, null)}, 
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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
        GROUPER_VERSION, AttributeAssignType.member, 
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
        GROUPER_VERSION, AttributeAssignType.imm_mem, 
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
        GROUPER_VERSION, AttributeAssignType.any_mem, 
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
        GROUPER_VERSION, AttributeAssignType.attr_def, 
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
        GROUPER_VERSION, AttributeAssignType.stem_asgn, 
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
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb(
        AttributeDefType.perm, "test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setMultiValued(false);
    attributeDef.setMultiAssignable(false);
    attributeDef.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").assignTypeOfGroup(TypeOfGroup.role).save();
  
    Role role = group;
    
    
    //Error case attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsAssignPermissionsResults wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, null, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        false, null, false, null, null, null, false);
  
    assertEquals("You must pass in a permissionType", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue("You must pass in a permissionType", 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("You need to pass in a permissionType"));
  
    //Error case lookups and names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Cant do defName and assign lookup", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("If you are passing in assign lookup ids to query, you cant specify attribute def names"));
  
    
    //cant pass in attr assign ids and owners
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup("abc")}, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Why is there more than one type of lookup?", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue("Why is there more than one type of lookup?", 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("Why is there more than one type of lookup?"));
  
    
    //Need to pass in permission assign operation
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        null, null, 
        null, null, null, null, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Need to pass in permissionAssignOperation", WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue("Need to pass in permissionAssignOperation: " + wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains("You need to pass in an permissionAssignOperation"));

    
    
    //Cant pass in attr def name ids when sending in attribute assign id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, new WsAttributeDefNameLookup[]{
            new WsAttributeDefNameLookup(attributeDefName.getName(),null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, new String[]{"a"}, null, 
        false, null, false, null, null, null, false);
    
      assertEquals("If you are passing in assign lookup ids to query, you cant specify attribute def names", 
          WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
          wsAssignPermissionsResults.getResultMetadata().getResultCode());
      assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
          wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "If you are passing in assign lookup ids to query, you cant specify attribute def names"));

      
          
    //Why is there more than one type of lookup?
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, null, new String[]{"a"}, null, 
        false, null, false, null, null, null, false);
    
      assertEquals("Why is there more than one type of lookup?", 
          WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
          wsAssignPermissionsResults.getResultMetadata().getResultCode());
      assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
          wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Why is there more than one type of lookup?"));

    
    //Cant pass in actions when sending in attribute assign id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, new String[]{"a"}, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Cant pass in actions when using attribute assign id lookup", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
          "Cant pass in actions when using attribute assign id lookup"));

    //Cant pass in assignmentNotes when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, "notes", 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Cant pass in assignmentNotes when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentNotes when deleting attributes"));

    //Cant pass in assignmentEnabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        new Timestamp(0), null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Cant pass in assignmentEnabledTime when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentEnabledTime when deleting attributes"));
    
    //Cant pass in assignmentDisabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        null, new Timestamp(0), null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Cant pass in assignmentDisabledTime when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentDisabledTime when deleting attributes"));
    

    //Cant pass in delegatable when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("Cant pass in delegatable when deleting attributes", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cant pass in delegatable when deleting attributes"));
    
    //lets assign by id (should ignore)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    

    
    //lets delete by id (should delete)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.remove_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("delete an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    


    //lets assign by id and assign notes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    String id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, "notes", 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
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
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        new Timestamp(123L), null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
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
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, new Timestamp(123L), null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
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
    attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, false);
    
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
        GROUPER_VERSION, PermissionType.role, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    assertEquals("F", wsAssignPermissionsResults.getWsAssignPermissionResults()[0]
         .getWsAttributeAssigns()[0].getDisallowed());

    //try in different version
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);

    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, null, null, 
        false, null, false, null, null, null, false);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    assertEquals("F", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getWsAttributeAssigns()[0].getDisallowed());

    
    
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
        GROUPER_VERSION, PermissionType.role_subject, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        null, new WsMembershipAnyLookup[]{wsMembershipAnyLookup}, null, null, 
        false, null, false, null, null, null, false);
    
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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
        GROUPER_VERSION, AttributeAssignType.stem, 
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

  /**
   * test add external member
   */
  public void testAddExternalMember() {
    
    //if no externals, add it
    try {
      
      SourceManager.getInstance().getSource(ExternalSubject.sourceId());
      
    } catch (SourceUnavailableException sue) {
      SourceManager.getInstance().loadSource(ExternalSubjectAutoSourceAdapter.instance());
    }
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    Subject subject = SubjectFinder.findByIdentifier("a@b.c", false);
    
    assertNull(subject);
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("test:groupAddExternal").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //Error case attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    GrouperVersion grouperVersion = GrouperVersion.valueOfIgnoreCase("v2_0_000");
    WsGroupLookup wsGroupLookup = new WsGroupLookup(group.getName(), null);
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(null, null, "a@b.c")};
    WsAddMemberResults wsAddMemberResults = GrouperServiceLogic.addMember(
        grouperVersion, wsGroupLookup, subjectLookups, false,
        null, null, null, false, false, null, null, null, null, false);

    assertEquals("Should not be success: " + wsAddMemberResults.getResultMetadata().getResultMessage() , 
        WsAddMemberResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAddMemberResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    subject = SubjectFinder.findByIdentifier("a@b.c", false);
    assertNull(subject);
    
    wsGroupLookup = new WsGroupLookup(group.getName(), null);
    subjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(null, null, "a@b.c")};
    
    //try again with param
    wsAddMemberResults = GrouperServiceLogic.addMember(
        grouperVersion, wsGroupLookup, subjectLookups, false,
        null, null, null, false, false, null, null, null, null, true);
    
    assertEquals("Should be success: " + wsAddMemberResults.getResultMetadata().getResultMessage() , 
        WsAddMemberResultsCode.SUCCESS.name(), 
        wsAddMemberResults.getResultMetadata().getResultCode());
  
    assertEquals("Should be success-created: " + wsAddMemberResults.getResults()[0].getResultMetadata().getResultMessage() , 
        WsAddMemberResultCode.SUCCESS_CREATED.name(), 
        wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode());
  
    //make sure the external member is a member
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    subject = SubjectFinder.findByIdentifier("a@b.c", true);
    
    assertTrue(group.hasMember(subject));
  }

  /**
   * test assign permissions
   */
  public void testAssignPermissionsAllowDeny() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb(
        AttributeDefType.perm, "test", "testAttributeAssignDefName");

    final AttributeDef attributeDef = attributeDefName.getAttributeDef();

    attributeDef.setMultiValued(false);
    attributeDef.setMultiAssignable(false);
    attributeDef.store();

    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").assignTypeOfGroup(TypeOfGroup.role).save();

    Role role = group;


    //lets assign by id (should ignore)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    WsAssignPermissionsResults wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, null, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, null, new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssign.getId())}, 
        null, null, null, null, 
        false, null, false, null, null, null, true);

    assertEquals("cant change the disallow", 
        WsGetAttributeAssignmentsResultsCode.INVALID_QUERY.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertTrue(wsAssignPermissionsResults.getResultMetadata().getResultMessage(), 
        wsAssignPermissionsResults.getResultMetadata().getResultMessage().contains(
            "Cannot change the disallowed property of an assignment"));

    //lets assign a new by group owner
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    role.getPermissionRoleDelegate().removeRolePermission(attributeDefName);

    wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(
        GROUPER_VERSION, PermissionType.role, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        new WsGroupLookup[]{new WsGroupLookup(role.getName(), null)}, null, null, null, 
        false, null, false, null, null, null, true);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    

    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getWsAttributeAssigns()[0].getDisallowed());
    
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
        GROUPER_VERSION, PermissionType.role_subject, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefName.getName(), null)}, 
        PermissionAssignOperation.assign_permission, null, 
        null, null, AttributeAssignDelegatable.TRUE, null, 
        null, new WsMembershipAnyLookup[]{wsMembershipAnyLookup}, null, null, 
        false, null, false, null, null, null, true);
    
    assertEquals("assign an existing attribute is ok", 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignPermissionsResults.getResultMetadata().getResultCode());
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getChanged());    
    
    assertEquals("T", wsAssignPermissionsResults.getWsAssignPermissionResults()[0].getWsAttributeAssigns()[0].getDisallowed());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    Member member4 = MemberFinder.findBySubject(GrouperServiceUtils.testSession, SubjectTestHelper.SUBJ4, false);
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
      .findAnyMembershipAttributeAssignments(null, null, null, GrouperUtil.toSet(new MultiKey(role.getId(), member4.getUuid())), null, null, false);
    assertEquals(1, attributeAssigns.size());
    assertNotNull(attributeAssigns.iterator().next().getId());
  
    assertTrue(attributeAssigns.iterator().next().isDisallowed());

  
    
  }

  /**
   * test find groups with TypeOfGroup
   */
  public void testAssignAttributeDefNameInheritance() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
  
    Stem testStem = new StemSave(GrouperServiceUtils.testSession).assignName("test1").save();
    Stem testSubStem = new StemSave(GrouperServiceUtils.testSession).assignName("test1:sub").save();
    Stem rootStem = StemFinder.findRootStem(GrouperServiceUtils.testSession);
    
    AttributeDef testAttributeDef = new AttributeDefSave(GrouperServiceUtils.testSession).assignName("test1:attributeDef1")
      .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
      .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName1 = new AttributeDefNameSave(GrouperServiceUtils.testSession, testAttributeDef)
      .assignName("test1:attributeDefName1").save();
    AttributeDefName testAttributeDefName2 = new AttributeDefNameSave(GrouperServiceUtils.testSession, testAttributeDef)
      .assignName("test2:attributeDefName2").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName testAttributeDefName3 = new AttributeDefNameSave(GrouperServiceUtils.testSession, testAttributeDef)
      .assignName("test2:attributeDefName3").assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName testAttributeDefName4 = new AttributeDefNameSave(GrouperServiceUtils.testSession, testAttributeDef)
      .assignName("test2:attributeDefName4").assignCreateParentStemsIfNotExist(true).save();
    
    testAttributeDefName1.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(testAttributeDefName2);
    
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, true);
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    //assign something
    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = GrouperServiceLogic.assignAttributeDefNameInheritance(
        GROUPER_VERSION, new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null), 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(null, testAttributeDefName2.getId()),
          new WsAttributeDefNameLookup(testAttributeDefName3.getName(), null)}, 
          true, null, null, null, null);
  
    assertEquals(wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultMessage(),
        WsAssignAttributeDefNameInheritanceResultsCode.SUCCESS.name(), 
        wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultCode());
    
    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    testAttributeDefName1 = AttributeDefNameFinder.findById(testAttributeDefName1.getId(), true);
    Set<AttributeDefName> attributeDefNames = testAttributeDefName1.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
    
    assertEquals(2, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(testAttributeDefName2));
    assertTrue(attributeDefNames.contains(testAttributeDefName3));

    //#########################
    // try to remove, not allowed

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    wsAssignAttributeDefNameInheritanceResults = GrouperServiceLogic.assignAttributeDefNameInheritance(
        GROUPER_VERSION, new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null), 
        new WsAttributeDefNameLookup[]{
          new WsAttributeDefNameLookup(testAttributeDefName3.getName(), null)}, 
          false, null, null, null, null);
    
    assertEquals("F", wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES", wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultCode());

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    testAttributeDefName1 = AttributeDefNameFinder.findById(testAttributeDefName1.getId(), true);
    attributeDefNames = testAttributeDefName1.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
    
    assertEquals(2, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(testAttributeDefName2));
    assertTrue(attributeDefNames.contains(testAttributeDefName3));

    //#########################
    // replace, allowed

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    wsAssignAttributeDefNameInheritanceResults = GrouperServiceLogic.assignAttributeDefNameInheritance(
        GROUPER_VERSION, new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null), 
        new WsAttributeDefNameLookup[]{
          new WsAttributeDefNameLookup(testAttributeDefName3.getName(), null),
          new WsAttributeDefNameLookup(testAttributeDefName4.getName(), null)}, 
          true, true, null, null, null);
    
    assertEquals("T", wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getSuccess());
    assertEquals("SUCCESS", wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultCode());

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    testAttributeDefName1 = AttributeDefNameFinder.findById(testAttributeDefName1.getId(), true);
    attributeDefNames = testAttributeDefName1.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
    
    assertEquals(2, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(testAttributeDefName3));
    assertTrue(attributeDefNames.contains(testAttributeDefName4));


    //#########################
    // try to remove, allowed

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    wsAssignAttributeDefNameInheritanceResults = GrouperServiceLogic.assignAttributeDefNameInheritance(
        GROUPER_VERSION, new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null), 
        new WsAttributeDefNameLookup[]{
          new WsAttributeDefNameLookup(testAttributeDefName3.getName(), null)}, 
          false, null, null, null, null);
    
    assertEquals("T", wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getSuccess());
    assertEquals("SUCCESS", wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultCode());

    GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    testAttributeDefName1 = AttributeDefNameFinder.findById(testAttributeDefName1.getId(), true);
    attributeDefNames = testAttributeDefName1.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
    
    assertEquals(1, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(testAttributeDefName4));
    
  }

  /**
   * test save groups
   */
  public void testAttributeDefNameSave() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    AttributeDef testAttributeDef = new AttributeDefSave(grouperSession).assignName("test1:attributeDef1")
      .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
      .assignToEffMembership(true).save();
    
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, true);
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, true);
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    WsAttributeDefNameToSave wsAttributeDefNameToSave1 = new WsAttributeDefNameToSave();
    WsAttributeDefName wsAttributeDefName1 = new WsAttributeDefName();
    wsAttributeDefName1.setAttributeDefName(testAttributeDef.getName());
    wsAttributeDefName1.setName("test1:testAttributeDefName1");
    wsAttributeDefName1.setDisplayExtension("Test Attribute Def Name 1");
    wsAttributeDefName1.setDescription("My Description");
    wsAttributeDefNameToSave1.setWsAttributeDefName(wsAttributeDefName1);
    
    WsAttributeDefNameToSave wsAttributeDefNameToSave2 = new WsAttributeDefNameToSave();
    WsAttributeDefName wsAttributeDefName2 = new WsAttributeDefName();
    wsAttributeDefName2.setAttributeDefName(testAttributeDef.getName());
    wsAttributeDefName2.setName("test1:testAttributeDefName2");
    wsAttributeDefName2.setDisplayExtension("Test Attribute Def Name 2");
    wsAttributeDefName2.setDescription("My Description2");
    wsAttributeDefNameToSave2.setWsAttributeDefName(wsAttributeDefName2);
    
    
    //save two attribute def names
    WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = GrouperServiceLogic.attributeDefNameSave(
        GROUPER_VERSION, new WsAttributeDefNameToSave[]{wsAttributeDefNameToSave1, wsAttributeDefNameToSave2}, null, null, null);
  
    assertEquals(wsAttributeDefNameSaveResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsAttributeDefNameSaveResults.getResultMetadata().getResultCode());
  
    assertEquals(2, GrouperUtil.length(wsAttributeDefNameSaveResults.getResults()));
    assertEquals(testAttributeDef.getId(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getAttributeDefId());
    assertEquals(wsAttributeDefName1.getName(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getName());
    assertEquals(wsAttributeDefName1.getDisplayExtension(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getDisplayExtension());
    assertEquals(wsAttributeDefName1.getDescription(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getDescription());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    AttributeDefName testAttributeDefName2 = AttributeDefNameFinder.findByName(wsAttributeDefName2.getName(), true);
    assertEquals(testAttributeDefName2.getDisplayExtension(), wsAttributeDefNameSaveResults.getResults()[1].getWsAttributeDefName().getDisplayExtension());
    assertEquals(testAttributeDefName2.getDescription(), wsAttributeDefNameSaveResults.getResults()[1].getWsAttributeDefName().getDescription());
    
    //#########################
    //try as subject0
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    wsAttributeDefNameToSave1 = new WsAttributeDefNameToSave();
    wsAttributeDefName1 = new WsAttributeDefName();
    wsAttributeDefName1.setAttributeDefName(testAttributeDef.getName());
    wsAttributeDefName1.setName("test1:testAttributeDefName1a");
    wsAttributeDefName1.setDisplayExtension("Test Attribute Def Name 1a");
    wsAttributeDefName1.setDescription("My Descriptiona");
    wsAttributeDefNameToSave1.setWsAttributeDefName(wsAttributeDefName1);
    
    wsAttributeDefNameToSave2 = new WsAttributeDefNameToSave();
    wsAttributeDefName2 = new WsAttributeDefName();
    wsAttributeDefName2.setAttributeDefName(testAttributeDef.getName());
    wsAttributeDefName2.setName("test1:testAttributeDefName2a");
    wsAttributeDefName2.setDisplayExtension("Test Attribute Def Name 2a");
    wsAttributeDefName2.setDescription("My Description2a");
    wsAttributeDefNameToSave2.setWsAttributeDefName(wsAttributeDefName2);
    
    
    //save two attribute def names
    wsAttributeDefNameSaveResults = GrouperServiceLogic.attributeDefNameSave(
        GROUPER_VERSION, new WsAttributeDefNameToSave[]{wsAttributeDefNameToSave1, wsAttributeDefNameToSave2}, null, null, null);
  
    assertEquals(wsAttributeDefNameSaveResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsAttributeDefNameSaveResults.getResultMetadata().getResultCode());
  
    assertEquals(2, GrouperUtil.length(wsAttributeDefNameSaveResults.getResults()));
    assertEquals(testAttributeDef.getId(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getAttributeDefId());
    assertEquals(wsAttributeDefName1.getName(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getName());
    assertEquals(wsAttributeDefName1.getDisplayExtension(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getDisplayExtension());
    assertEquals(wsAttributeDefName1.getDescription(), wsAttributeDefNameSaveResults.getResults()[0].getWsAttributeDefName().getDescription());

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    testAttributeDefName2 = AttributeDefNameFinder.findByName(wsAttributeDefName2.getName(), true);
    assertEquals(testAttributeDefName2.getDisplayExtension(), wsAttributeDefNameSaveResults.getResults()[1].getWsAttributeDefName().getDisplayExtension());
    assertEquals(testAttributeDefName2.getDescription(), wsAttributeDefNameSaveResults.getResults()[1].getWsAttributeDefName().getDescription());

    //#########################
    //try as subject1, not allowed
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    wsAttributeDefNameToSave1 = new WsAttributeDefNameToSave();
    wsAttributeDefName1 = new WsAttributeDefName();
    wsAttributeDefName1.setAttributeDefName(testAttributeDef.getName());
    wsAttributeDefName1.setName("test1:testAttributeDefName1b");
    wsAttributeDefName1.setDisplayExtension("Test Attribute Def Name 1b");
    wsAttributeDefName1.setDescription("My Descriptionb");
    wsAttributeDefNameToSave1.setWsAttributeDefName(wsAttributeDefName1);
    
    wsAttributeDefNameToSave2 = new WsAttributeDefNameToSave();
    wsAttributeDefName2 = new WsAttributeDefName();
    wsAttributeDefName2.setAttributeDefName(testAttributeDef.getName());
    wsAttributeDefName2.setName("test1:testAttributeDefName2b");
    wsAttributeDefName2.setDisplayExtension("Test Attribute Def Name 2b");
    wsAttributeDefName2.setDescription("My Description2b");
    wsAttributeDefNameToSave2.setWsAttributeDefName(wsAttributeDefName2);

    wsAttributeDefNameSaveResults = GrouperServiceLogic.attributeDefNameSave(
        GROUPER_VERSION, new WsAttributeDefNameToSave[]{wsAttributeDefNameToSave1, wsAttributeDefNameToSave2}, null, null, null);
    assertEquals("F", wsAttributeDefNameSaveResults.getResultMetadata().getSuccess());
    assertEquals("PROBLEM_SAVING_ATTRIBUTE_DEF_NAMES", wsAttributeDefNameSaveResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAttributeDefNameSaveResults.getResults()[0].getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES", wsAttributeDefNameSaveResults.getResults()[0].getResultMetadata().getResultCode());
    assertEquals("F", wsAttributeDefNameSaveResults.getResults()[1].getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES", wsAttributeDefNameSaveResults.getResults()[1].getResultMetadata().getResultCode());
    
    
    
  }

  /**
   * test delete groups
   */
  public void testAttributeDefNameDelete() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    AttributeDef testAttributeDef = new AttributeDefSave(grouperSession).assignName("test1:attributeDef1")
      .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
      .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName1 = new AttributeDefNameSave(grouperSession, 
        testAttributeDef).assignName("test1:attributeDefName1").save();
    AttributeDefName testAttributeDefName2 = new AttributeDefNameSave(grouperSession, 
        testAttributeDef).assignName("test1:attributeDefName2").save();
    
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, true);
    //if you cant view you cant delete
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, true);
    
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //save two attribute def names
    WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults = GrouperServiceLogic.attributeDefNameDelete(
        GROUPER_VERSION, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null),
            new WsAttributeDefNameLookup(null, testAttributeDefName2.getId())}, 
        null, null, null);
  
    assertEquals(wsAttributeDefNameDeleteResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsAttributeDefNameDeleteResults.getResultMetadata().getResultCode());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    testAttributeDefName1 = AttributeDefNameFinder.findByName(testAttributeDefName1.getName(), false);
    testAttributeDefName2 = AttributeDefNameFinder.findByName(testAttributeDefName2.getName(), false);
    assertNull(testAttributeDefName1);
    assertNull(testAttributeDefName2);
    
    //#########################
    //try as subject1
    
    grouperSession = GrouperSession.startRootSession();
    
    testAttributeDefName1 = new AttributeDefNameSave(grouperSession, 
        testAttributeDef).assignName("test1:attributeDefName1a").save();
    testAttributeDefName2 = new AttributeDefNameSave(grouperSession, 
        testAttributeDef).assignName("test1:attributeDefName2a").save();
    
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    wsAttributeDefNameDeleteResults = GrouperServiceLogic.attributeDefNameDelete(
        GROUPER_VERSION, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null),
            new WsAttributeDefNameLookup(null, testAttributeDefName2.getId())}, 
        null, null, null);

    grouperSession = GrouperSession.startRootSession();
    testAttributeDefName1 = AttributeDefNameFinder.findByName(testAttributeDefName1.getName(), true);
    testAttributeDefName2 = AttributeDefNameFinder.findByName(testAttributeDefName2.getName(), true);
    assertNotNull(testAttributeDefName1);
    assertNotNull(testAttributeDefName2);

    assertEquals("F", wsAttributeDefNameDeleteResults.getResultMetadata().getSuccess());
    assertEquals("PROBLEM_DELETING_ATTRIBUTE_DEF_NAMES", wsAttributeDefNameDeleteResults.getResultMetadata().getResultCode());
    assertEquals("F", wsAttributeDefNameDeleteResults.getResults()[0].getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES", wsAttributeDefNameDeleteResults.getResults()[0].getResultMetadata().getResultCode());
    assertEquals("F", wsAttributeDefNameDeleteResults.getResults()[1].getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES", wsAttributeDefNameDeleteResults.getResults()[1].getResultMetadata().getResultCode());
    
    
    
  }

  /**
   * test find attribute def names with TypeOfGroup
   */
  public void testFindAttributeDefNames() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("testabc1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("testabc1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    AttributeDef testAttributeDef = new AttributeDefSave(grouperSession).assignName("testabc1:attributeDef1")
      .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
      .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName1 = new AttributeDefNameSave(grouperSession, testAttributeDef).assignName("testabc1:attributeDefName1").save();
    AttributeDefName testAttributeDefName2 = new AttributeDefNameSave(grouperSession, testAttributeDef)
      .assignName("testabc2:attributeDefName2").assignCreateParentStemsIfNotExist(true).save();
    
    testAttributeDefName1.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(testAttributeDefName2);
    
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, true);
  
    //find no results
    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc3%", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(0, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(0, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
  
    //#############################################
    //find all results
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc%", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(2, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
    assertEquals(testAttributeDefName2.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[1].getName());
    
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
    
    //#############################################
    //no scope and no lookups is an exception
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertEquals("F", wsFindAttributeDefNamesResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY", wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
    
    //#############################################
    //find all results by attribute def
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, new WsAttributeDefLookup(testAttributeDef.getName(), null), null, null, 
        null, null, null, null, null, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(2, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
    assertEquals(testAttributeDefName2.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[1].getName());
    
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
    
    //#############################################
    //find all results by attribute def
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, new WsAttributeDefLookup(testAttributeDef.getName(), null), 
        null, null, null, null, null, null, null, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(2, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
    assertEquals(testAttributeDefName2.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[1].getName());
    
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
    
    //#############################################
    //find all results by attribute def name lookup
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, null, null, null, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null), 
            new WsAttributeDefNameLookup(null, testAttributeDefName2.getId())}, null, null, null, null, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(2, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
    assertEquals(testAttributeDefName2.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[1].getName());
    
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
  
    
    //#############################################
    //cant have two lookups and relationship
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, null, null, null, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null), 
            new WsAttributeDefNameLookup(null, testAttributeDefName2.getId())}, 
        null, null, null, null, WsInheritanceSetRelation.IMPLIED_BY_THIS, null, null, null, null);
    assertEquals("F", wsFindAttributeDefNamesResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY", wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
    
    //#############################################
    //cant have no lookups and relationship
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, null, null, null, 
        null, 
        null, null, null, null, WsInheritanceSetRelation.IMPLIED_BY_THIS, null, null, null, null);
    assertEquals("F", wsFindAttributeDefNamesResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY", wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
    
    //#############################################
    //find relationship of attribute def name
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, null, null, null, 
        new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(testAttributeDefName1.getName(), null)}, 
        null, null, null, null, WsInheritanceSetRelation.IMPLIED_BY_THIS, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName2.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
    
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
  
    //#############################################
    //if you pass a page number, you need a page size
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc%", null, null, null, null, null, null, 1, null, null, null, null, null, null, null);
    assertEquals("F", wsFindAttributeDefNamesResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY", wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    //#############################################
    //if you pass a split scope, you need a scope
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, true, null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertEquals("F", wsFindAttributeDefNamesResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY", wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    //#############################################
    //page by size 1, page number 2
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc%", null, null, null, null, null, 
        1, 2, "name", false, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
  
    //#############################################
    //page by size 1, page number 2, secure
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc%", null, null, null, null, null, 
        1, 2, "name", false, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
  
    //#############################################
    //page by size 1, page number 2, secure
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc%", null, null, null, null, null, 
        1, 2, "name", false, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
  
    //#############################################
    //page by size 1, page number 2, secure
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, "testabc%", null, null, null, null, null, 
        1, 2, "name", false, null, null, null, null, null);
  
    assertEquals(0, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(0, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
  
    //#############################################
    //paging and find by attribute def idIndex
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, new WsAttributeDefLookup(null, null, testAttributeDef.getIdIndex().toString()), null, null, null, 
        1, 2, "name", false, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
    
    //#############################################
    //paging and find by attribute def name idIndex
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(
        GROUPER_VERSION, null, null, null, null, null, new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(null, null, testAttributeDefName1.getIdIndex().toString())}, 
        1, 1, "name", false, null, null, null, null, null);
  
    assertEquals(wsFindAttributeDefNamesResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(), 
        wsFindAttributeDefNamesResults.getResultMetadata().getResultCode());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefNameResults()));
    assertEquals(testAttributeDefName1.getName(), wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0].getName());
  
    assertEquals(1, GrouperUtil.length(wsFindAttributeDefNamesResults.getAttributeDefs()));
    assertEquals(testAttributeDef.getName(), wsFindAttributeDefNamesResults.getAttributeDefs()[0].getName());
  
  }

  /**
   * test assign attributes batch
   */
  public void testAssignAttributesBatch() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    AttributeDefName attributeDefName3 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName2");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.setMultiAssignable(true);
    attributeDef.store();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    final AttributeDef attributeDef3 = attributeDefName3.getAttributeDef();
    
    attributeDef3.setValueType(AttributeDefValueType.string);
    attributeDef3.setAssignToGroup(false);
    attributeDef3.setAssignToGroupAssn(true);
    attributeDef3.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    @SuppressWarnings("unused")
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign2").assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //Error case attribute assign type
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(),null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    
    WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    WsAssignAttributesBatchResults wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);
        
    assertEquals("You must pass in an attributeAssignType", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    WsAssignAttributeBatchResult wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("You must pass in an attributeAssignType", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue("You must pass in an attributeAssignType", 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains("You need to pass in an attributeAssignType"));
  
    //Error case lookups and names
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(),null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setWsOwnerAttributeAssignLookup(new WsAttributeAssignLookup("abc"));
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant do defName and assign lookup", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant do defName and assign lookup", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Why is there more than one type of lookup?"));
  
    
    //Need to pass in attribute assign operation
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(),null));
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Need to pass in attributeAssignOperation", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Need to pass in attributeAssignOperation", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "You need to pass in an attributeAssignOperation"));

    
    

  
    //Need to do assign or delete by id if passing by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.stem.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("but this operation was passed attributeAssignType", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("but this operation was passed attributeAssignType", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "but this operation was passed attributeAssignType"));

    
    
  
    //cant pass in actions if using attribute assign ids
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    wsAssignAttributeBatchEntry.setAction("assign");
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in actions when using attribute assign id lookup", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in actions when using attribute assign id lookup", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in actions when using attribute assign id lookup"));

    
//  //Cant pass in values when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
    //we dont use value formatted yet
    wsAttributeAssignValue.setValueSystem("hey");

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in actions when using attribute assign id lookup", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in values when deleting attributes", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in values when deleting attributes"));


    //Cant pass in assignmentNotes when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    wsAssignAttributeBatchEntry.setAssignmentNotes("a");
        
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in assignmentNotes when deleting attributes", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in assignmentNotes when deleting attributes", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentNotes when deleting attributes"));

    //Cant pass in assignmentEnabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    wsAssignAttributeBatchEntry.setAssignmentEnabledTime("2012/01/01 01:01:01.000");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in assignmentEnabledTime when deleting attributes", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in assignmentEnabledTime when deleting attributes", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentEnabledTime when deleting attributes"));

    //Cant pass in assignmentDisabledTime when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    wsAssignAttributeBatchEntry.setAssignmentDisabledTime("2012/01/01 01:01:01.000");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in assignmentDisabledTime when deleting attributes", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in assignmentDisabledTime when deleting attributes", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in assignmentDisabledTime when deleting attributes"));

    //Cant pass in delegatable when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    wsAssignAttributeBatchEntry.setDelegatable("TRUE");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in delegatable when deleting attributes", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in delegatable when deleting attributes", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in delegatable when deleting attributes"));


    //Cant pass in attributeAssignValueOperation when deleting attributes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation("remove_value");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};
    
    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("Cant pass in attributeAssignValueOperation when deleting attributes", WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];
    
    assertEquals("Cant pass in attributeAssignValueOperation when deleting attributes", WsAssignAttributeBatchResultCode.INVALID_QUERY.name(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultCode());
    assertTrue(wsAssignAttributeBatchResult.getResultMetadata().getResultMessage(), 
        wsAssignAttributeBatchResult.getResultMetadata().getResultMessage().contains(
            "Cant pass in attributeAssignValueOperation when deleting attributes"));

    //lets assign by id (should ignore)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("assign an existing attribute is ok", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());

    //lets delete by id (should delete)
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("delete an existing attribute is ok", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getChanged());

    //lets assign by id and assign notes
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    String id = attributeAssign.getId();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setAssignmentNotes("notes");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("assign an existing attribute is ok", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getChanged());

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

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setAssignmentEnabledTime("2012/01/01 01:01:01.111");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign by id and assign enabled date", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals("2012/01/01 01:01:01.111", GrouperUtil.timestampToString(attributeAssign.getEnabledTime()));

    //lets assign by id and assign disabled date
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    id = attributeAssign.getId();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setAssignmentDisabledTime("2012/01/01 01:01:01.111");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign by id and assign disabled date", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    assertEquals("2012/01/01 01:01:01.111", GrouperUtil.timestampToString(attributeAssign.getDisabledTime()));

    //lets assign by id and assign delegatable
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    id = attributeAssign.getId();

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setDelegatable("TRUE");

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign by id and assign delegatable", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getChanged());

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

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());    
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation(AttributeAssignValueOperation.assign_value.name());    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");

    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value: " + wsAssignAttributesBatchResults.getResultMetadata().getResultMessage()
        + ", " + wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0].getResultMetadata().getResultMessage(), 
        WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    
    assertEquals(1, attributeAssign.getValueDelegate().getAttributeAssignValues().size());
    assertEquals(new Long(123), attributeAssign.getValueDelegate().getAttributeAssignValues().iterator().next().getValueInteger());
    
    //lets delete a value by id
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    id = attributeAssign.getId();

    AttributeAssignValueResult attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setId(attributeAssignValueResult.getAttributeAssignValue().getId());

    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation("remove_value");
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    List<Long> values =  attributeAssign.getValueDelegate().retrieveValuesInteger();
    
    assertEquals(0, GrouperUtil.nonNull(values).size());

    //lets delete a value by value
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    id = attributeAssign.getId();

    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();
    
    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation("remove_value");
    

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());

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

    id = attributeAssign.getId();

    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation("add_value");
    

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());
    assertEquals(1, wsAssignAttributeBatchResult.getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();
    
    assertEquals(1, GrouperUtil.nonNull(values).size());
    assertEquals(new Long(123L), values.iterator().next());


    //lets assign a value by value
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();

    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");
    id = attributeAssign.getId();

    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation("assign_value");
    

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getValuesChanged());
    assertEquals(1, wsAssignAttributeBatchResult.getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();
    
    assertEquals(1, GrouperUtil.nonNull(values).size());
    assertEquals(new Long(123L), values.iterator().next());
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();
  
    assertEquals(1, GrouperUtil.nonNull(values).size());
    assertEquals(123L, values.iterator().next().longValue());

    
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

    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsAttributeAssignLookup(new WsAttributeAssignLookup(attributeAssign.getId()));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation(AttributeAssignValueOperation.replace_values.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());

    assertEquals(2, wsAssignAttributeBatchResult.getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getDeleted());
    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
    assertEquals(id, attributeAssign.getId());
    values =  attributeAssign.getValueDelegate().retrieveValuesInteger();
  
    assertEquals(2, GrouperUtil.nonNull(values).size());
    Iterator<Long> iterator = values.iterator();
    assertEquals(123L, iterator.next().longValue());
    assertEquals(234L, iterator.next().longValue());
    
    //lets replace values by group owner
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssign = attributeAssignResult.getAttributeAssign();
    id = attributeAssign.getId();
    
    attributeAssignValueResult = attributeAssign.getValueDelegate().assignValue("123");

    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");

    wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("234");

    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(), null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation(AttributeAssignValueOperation.replace_values.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());

    assertEquals(2, wsAssignAttributeBatchResult.getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getDeleted());
    
    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsGroups()));
    assertEquals(group.getName(), wsAssignAttributesBatchResults.getWsGroups()[0].getName());
    

    
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

    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("123");

    wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("234");

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(), null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation(AttributeAssignValueOperation.assign_value.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("lets assign value", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());

    assertEquals(2, wsAssignAttributeBatchResult.getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getDeleted());
    
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssign = group.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();

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
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(), null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue, wsAttributeAssignValue2});
    wsAssignAttributeBatchEntry.setAttributeAssignValueOperation(AttributeAssignValueOperation.replace_values.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesBatchResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getChanged());
    
    assertEquals(3, wsAssignAttributeBatchResult.getWsAttributeAssignValueResults().length);
    assertEquals("123", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[0].getDeleted());
    assertEquals("234", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getChanged());
    assertEquals("F", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[1].getDeleted());
    assertEquals("345", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[2].getWsAttributeAssignValue().getValueSystem());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[2].getChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getWsAttributeAssignValueResults()[2].getDeleted());
  
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

    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(), null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.add_attr.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("assign attribute add a value is ok: " + wsAssignAttributesBatchResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getChanged());
    
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(2, attributeAssigns.size());
    
    //already assigned twice, remove
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    attributeAssignResult = group.getAttributeDelegate().addAttribute(attributeDefName);
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(), null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.remove_attr.name());

    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("remove attribute is ok: " + wsAssignAttributesBatchResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0];

    assertEquals("F", wsAssignAttributeBatchResult.getValuesChanged());
    assertEquals("T", wsAssignAttributeBatchResult.getChanged());

    assertEquals(2, GrouperUtil.length(wsAssignAttributeBatchResult.getWsAttributeAssigns()));
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(0, GrouperUtil.length(attributeAssigns));
    
    //assign a marker, and an attribute value on that marker
    //already assigned twice, remove
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.group.name());
    wsAssignAttributeBatchEntry.setWsOwnerGroupLookup(new WsGroupLookup(group.getName(), null));
    wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(), null));
    wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());

    wsAttributeAssignValue = new WsAttributeAssignValue();
    wsAttributeAssignValue.setValueSystem("hello1");

    WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry2 = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry2.setAttributeAssignType(AttributeAssignType.group_asgn.name());
    wsAssignAttributeBatchEntry2.setWsOwnerAttributeAssignLookup(new WsAttributeAssignLookup(null, "0"));
    wsAssignAttributeBatchEntry2.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName2.getName(), null));
    wsAssignAttributeBatchEntry2.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry2.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
    wsAssignAttributeBatchEntry2.setAttributeAssignValueOperation(AttributeAssignValueOperation.assign_value.name());

    wsAttributeAssignValue2 = new WsAttributeAssignValue();
    wsAttributeAssignValue2.setValueSystem("hello2");

    WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry3 = new WsAssignAttributeBatchEntry();
    wsAssignAttributeBatchEntry3.setAttributeAssignType(AttributeAssignType.group_asgn.name());
    wsAssignAttributeBatchEntry3.setWsOwnerAttributeAssignLookup(new WsAttributeAssignLookup(null, "0"));
    wsAssignAttributeBatchEntry3.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName3.getName(), null));
    wsAssignAttributeBatchEntry3.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
    wsAssignAttributeBatchEntry3.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue2});
    wsAssignAttributeBatchEntry3.setAttributeAssignValueOperation(AttributeAssignValueOperation.assign_value.name());
    
    wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry, wsAssignAttributeBatchEntry2, wsAssignAttributeBatchEntry3};

    wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
        GROUPER_VERSION, 
        wsAssignAttributeBatchEntries, null, false, null, null, false, null);

    assertEquals("assign attributes is ok: " + wsAssignAttributesBatchResults.getResultMetadata().getResultMessage(), 
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsAssignAttributesBatchResults.getResultMetadata().getResultCode());

    assertEquals(3, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));
    
    assertEquals("F", wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0].getValuesChanged());
    assertEquals("T", wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0].getChanged());

    assertEquals("T", wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[1].getValuesChanged());
    assertEquals("T", wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[1].getChanged());

    assertEquals("T", wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[2].getValuesChanged());
    assertEquals("T", wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[2].getChanged());

    assertEquals(1, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[0].getWsAttributeAssigns()));
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
    attributeAssigns = group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, GrouperUtil.length(attributeAssigns));

    attributeAssign = attributeAssigns.iterator().next();
    assertEquals("hello1", attributeAssign.getAttributeValueDelegate().retrieveValueString(attributeDefName2.getName()));
    assertEquals("hello2", attributeAssign.getAttributeValueDelegate().retrieveValueString(attributeDefName3.getName()));
    
  }

  /**
   * test find stems
   */
  public void testFindStems() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test:sub").save();
    
    WsFindStemsResults wsFindStemsResults = GrouperServiceLogic.findStems(
        GROUPER_VERSION, null, null, null, new WsStemLookup[]{new WsStemLookup(null, null, testStem.getIdIndex().toString()),
            new WsStemLookup(null, null, testSubStem.getIdIndex().toString())});
  
    assertEquals(wsFindStemsResults.getResultMetadata().getResultMessage(),
        WsGetGroupsResultsCode.SUCCESS.name(), 
        wsFindStemsResults.getResultMetadata().getResultCode());
  
    assertStemSetsAndOrder(GrouperUtil.toSet(testStem, testSubStem), wsFindStemsResults.getStemResults());    
    
    
  }

  /**
   * make sure a set of stems is similar to another by stem name including order
   * @param set1 expected set
   * @param stemResultArray received set
   */
  public void assertStemSetsAndOrder(Set<Stem> set1, WsStem[] stemResultArray) {
    int set1length = GrouperUtil.length(set1);
    int set2length = GrouperUtil.length(stemResultArray);
    if (set1length != set2length) {
      fail("Expecting stems of size: " + set1length + " but received size: " + set2length + ", expecting: "
          + GrouperUtil.toStringForLog(set1, 200) + ", but received: " + GrouperUtil.toStringForLog(stemResultArray, 200));
    }
    
    if (set1length == 0) {
      return;
    }
    
    int i=0;
    for (Stem stem : set1) {
      if (!StringUtils.equals(stem.getName(), stemResultArray[i].getName())) {
        fail("Expecting index of set: " + i + " to be: " + stem.getName() + ", but received: "
            + stemResultArray[i].getName() + ", expecting: " 
            + GrouperUtil.toStringForLog(set1, 200) 
            + ", but received: " + GrouperUtil.toStringForLog(stemResultArray, 200));
      }
      i++;
    }
  }

  /**
   * make sure a set of attributeDefNames is similar to another by attributeDefName name including order
   * @param set1 expected set
   * @param attributeDefNameResultArray received set
   */
  public void assertAttributeDefNameSetsAndOrder(Set<AttributeDefName> set1, WsAttributeDefName[] attributeDefNameResultArray) {
    int set1length = GrouperUtil.length(set1);
    int set2length = GrouperUtil.length(attributeDefNameResultArray);
    if (set1length != set2length) {
      fail("Expecting attributeDefNames of size: " + set1length + " but received size: " + set2length + ", expecting: "
          + GrouperUtil.toStringForLog(set1, 200) + ", but received: " + GrouperUtil.toStringForLog(attributeDefNameResultArray, 200));
    }
    
    if (set1length == 0) {
      return;
    }
    
    int i=0;
    for (AttributeDefName attributeDefName : set1) {
      if (!StringUtils.equals(attributeDefName.getName(), attributeDefNameResultArray[i].getName())) {
        fail("Expecting index of set: " + i + " to be: " + attributeDefName.getName() + ", but received: "
            + attributeDefNameResultArray[i].getName() + ", expecting: " 
            + GrouperUtil.toStringForLog(set1, 200) 
            + ", but received: " + GrouperUtil.toStringForLog(attributeDefNameResultArray, 200));
      }
      i++;
    }
  }

  /**
   * test get members
   */
  public void testGetMembersPaging() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);    
    group1.addMember(SubjectTestHelper.SUBJ2);    
    group1.addMember(SubjectTestHelper.SUBJ3);    
    group1.addMember(SubjectTestHelper.SUBJ4);    
    group1.addMember(SubjectTestHelper.SUBJ5);    
    ChangeLogTempToEntity.convertRecords();
    
    WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
    
    //###############################################
    //valid query
    WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
        GROUPER_VERSION, wsGroupLookups, WsMemberFilter.Immediate, null, 
        Group.getDefaultList(), true, true, null, null, null, null, null, 2, 1, null, null);
  
    assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
        WsGetMembersResultsCode.SUCCESS.name(), 
        wsGetMembersResults.getResultMetadata().getResultCode());
    
    assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()));
    assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
    
    WsGroup wsGroup = wsGetMembersResults.getResults()[0].getWsGroup();
    WsSubject wsSubject = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
    
    assertEquals(group1.getUuid(), wsGroup.getUuid());
    assertEquals(group1.getName(), wsGroup.getName());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), wsSubject.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), wsSubject.getSourceId());
  }

  /**
   * test get members using point in time
   */
  public void testGetMembersPITpaging() {
  
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
  
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    // add members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);    
    group1.addMember(SubjectTestHelper.SUBJ2);    
    group1.addMember(SubjectTestHelper.SUBJ3);    
    group1.addMember(SubjectTestHelper.SUBJ4);    
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(SubjectTestHelper.SUBJ1);    
    ChangeLogTempToEntity.convertRecords();
  
    //###############################################
    //valid query
    {
      WsGroupLookup wsGroupLookup = new WsGroupLookup(null, group1.getUuid());
      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
      
      WsGetMembersResults wsGetMembersResults = GrouperServiceLogic.getMembers(
          GROUPER_VERSION, wsGroupLookups, WsMemberFilter.All, null, 
          Group.getDefaultList(), false, true, null, null, null, null, new Timestamp(new Date().getTime()), 2, 2, null, null);
  
      assertEquals(wsGetMembersResults.getResultMetadata().getResultMessage(),
          WsGetMembersResultsCode.SUCCESS.name(), 
          wsGetMembersResults.getResultMetadata().getResultCode());
      
      assertEquals(1, GrouperUtil.length(wsGetMembersResults.getResults()));
      assertEquals(2, GrouperUtil.length(wsGetMembersResults.getResults()[0].getWsSubjects()));
      
      WsGroup wsGroup = wsGetMembersResults.getResults()[0].getWsGroup();
      WsSubject wsSubject1 = wsGetMembersResults.getResults()[0].getWsSubjects()[0];
      WsSubject wsSubject2 = wsGetMembersResults.getResults()[0].getWsSubjects()[1];
      
      assertEquals(group1.getUuid(), wsGroup.getUuid());
      assertEquals(group1.getName(), wsGroup.getName());
      assertFalse(wsSubject1.getId().equals(wsSubject2.getId()));
      assertEquals(SubjectTestHelper.SUBJ2.getId(), wsSubject1.getId());
      assertEquals(SubjectTestHelper.SUBJ3.getId(), wsSubject2.getId());
      assertEquals(SubjectTestHelper.SUBJ2.getSourceId(), wsSubject1.getSourceId());
      assertEquals(SubjectTestHelper.SUBJ3.getSourceId(), wsSubject2.getSourceId());
    }
    
  }

  /**
   * test membership attribute read
   */
  public void testGetAttributeAssignmentsMembershipBatch() {
  
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
  
    group1.addMember(SubjectTestHelper.SUBJ1);
    
    Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
    Membership membership1 = group1.getMemberships(FieldFinder.find("members", true), GrouperUtil.toSet(member1)).iterator().next();
    
    attributeAssignResult = membership1.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign1 = attributeAssignResult.getAttributeAssign();

    group1.addMember(SubjectTestHelper.SUBJ2);
    Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, true);
    
    Membership membership2 = group1.getMemberships(FieldFinder.find("members", true), GrouperUtil.toSet(member2)).iterator().next();

    //###############################################
    //valid query
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(
        GROUPER_VERSION, AttributeAssignType.imm_mem, null, null, null, 
        null, null, null, 
        new WsMembershipLookup[]{new WsMembershipLookup(membership.getUuid()), new WsMembershipLookup(membership1.getUuid()), 
            new WsMembershipLookup(membership2.getUuid())}, 
        null, null, null, false, null, false, null, false, null, null, null, null, false, null, null, null, null, null);
  
    assertEquals(wsGetAttributeAssignmentsResults.getResultMetadata().getResultMessage(),
        WsGetAttributeAssignmentsResultsCode.SUCCESS.name(), 
        wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode());
    
    assertEquals(2, GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
    
    for (int i=0;i<2;i++) {
      
      WsAttributeAssign wsAttributeAssign = wsGetAttributeAssignmentsResults.getWsAttributeAssigns()[1];
      
      assertTrue(StringUtils.equals(attributeAssign.getId(), wsAttributeAssign.getId())
          || StringUtils.equals(attributeAssign1.getId(), wsAttributeAssign.getId()));
    
      assertTrue(StringUtils.equals(membership.getImmediateMembershipId(), wsGetAttributeAssignmentsResults.getWsMemberships()[i].getImmediateMembershipId())
          || StringUtils.equals(membership1.getImmediateMembershipId(), wsGetAttributeAssignmentsResults.getWsMemberships()[i].getImmediateMembershipId()));
      assertTrue(StringUtils.equals(membership.getUuid(), wsGetAttributeAssignmentsResults.getWsMemberships()[i].getMembershipId())
          || StringUtils.equals(membership1.getUuid(), wsGetAttributeAssignmentsResults.getWsMemberships()[i].getMembershipId()));
      
      
    }
    
  
  }

  /**
     * test assign attributes batch immediate memberships
     */
    public void testAssignAttributesBatchImmMembership() {
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      
      AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
      
      final AttributeDef attributeDef = attributeDefName.getAttributeDef();
      
      attributeDef.setValueType(AttributeDefValueType.integer);
      attributeDef.setMultiValued(false);
      attributeDef.setMultiAssignable(false);
      attributeDef.setAssignToGroup(false);
      attributeDef.setAssignToImmMembership(true);
      attributeDef.store();
            
      Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();
    
      group.addMember(SubjectTestHelper.SUBJ0);
      
      Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, true);
      Membership membership = group.getMemberships(FieldFinder.find("members", true), GrouperUtil.toSet(member)).iterator().next();
      
      group.addMember(SubjectTestHelper.SUBJ1);
      
      Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
      Membership membership1 = group.getMemberships(FieldFinder.find("members", true), GrouperUtil.toSet(member1)).iterator().next();
      
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      
      WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry = new WsAssignAttributeBatchEntry();
      wsAssignAttributeBatchEntry.setWsOwnerMembershipLookup(new WsMembershipLookup(membership.getUuid()));
      wsAssignAttributeBatchEntry.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(),null));
      wsAssignAttributeBatchEntry.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
      wsAssignAttributeBatchEntry.setAttributeAssignValueOperation(AttributeAssignValueOperation.assign_value.name());
      wsAssignAttributeBatchEntry.setAttributeAssignType(AttributeAssignType.imm_mem.name());
      WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
      wsAttributeAssignValue.setValueSystem("3");
      wsAssignAttributeBatchEntry.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});
      
      WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry1 = new WsAssignAttributeBatchEntry();
      wsAssignAttributeBatchEntry1.setWsOwnerMembershipLookup(new WsMembershipLookup(membership1.getUuid()));
      wsAssignAttributeBatchEntry1.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup(attributeDefName.getName(),null));
      wsAssignAttributeBatchEntry1.setAttributeAssignOperation(AttributeAssignOperation.assign_attr.name());
      wsAssignAttributeBatchEntry1.setAttributeAssignValueOperation(AttributeAssignValueOperation.assign_value.name());
      wsAssignAttributeBatchEntry1.setAttributeAssignType(AttributeAssignType.imm_mem.name());
      WsAttributeAssignValue wsAttributeAssignValue1 = new WsAttributeAssignValue();
      wsAttributeAssignValue1.setValueSystem("5");
      wsAssignAttributeBatchEntry1.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue1});
      
      WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[] {wsAssignAttributeBatchEntry, wsAssignAttributeBatchEntry1};
      
      WsAssignAttributesBatchResults wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(
          GROUPER_VERSION, 
          wsAssignAttributeBatchEntries, null, false, null, null, false, null);

      GrouperServiceUtils.testSession = GrouperSession.startRootSession();

      assertEquals("assign an existing attribute is ok", WsAssignAttributesBatchResultsCode.SUCCESS.name(), 
          wsAssignAttributesBatchResults.getResultMetadata().getResultCode());
  
      assertEquals(new Long(3), membership.getAttributeValueDelegate().retrieveValueInteger(attributeDefName.getName()));
      assertEquals(new Long(5), membership1.getAttributeValueDelegate().retrieveValueInteger(attributeDefName.getName()));
      
      assertEquals(2, GrouperUtil.length(wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()));

      AttributeAssign attributeAssign = membership.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
      AttributeAssign attributeAssign1 = membership1.getAttributeDelegate().retrieveAssignments(attributeDefName).iterator().next();
      
      for (int i=0;i<2;i++) {
        WsAssignAttributeBatchResult wsAssignAttributeBatchResult = wsAssignAttributesBatchResults.getWsAssignAttributeBatchResultArray()[i];
        assertEquals("T", wsAssignAttributeBatchResult.getChanged());
        assertEquals("T", wsAssignAttributeBatchResult.getValuesChanged());
        assertTrue(StringUtils.equals(attributeAssign.getId(), wsAssignAttributeBatchResult.getWsAttributeAssigns()[0].getId())
            || StringUtils.equals(attributeAssign1.getId(), wsAssignAttributeBatchResult.getWsAttributeAssigns()[0].getId()));
      }
      
  
  
      
    }

    /**
     * test get memberships
     */
    public void testGetMemberships() {
    
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
      String attributeDefName = "test:testAttributeDef";
      String attributeDefName2 = "test2:testAttributeDef2";
      String subjectIdWithAdminPriv = "test.subject.0";
      String subjectIdWithUpdatePriv = "test.subject.1";
      String subjectIdNoPrivs = "test.subject.2";
      String subjectIdMember = "test.subject.3";
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      Stem stem = new StemSave(GrouperServiceUtils.testSession).assignName(stemName).assignCreateParentStemsIfNotExist(true).save();
      Stem stem2 = new StemSave(GrouperServiceUtils.testSession).assignName(stemName2).assignCreateParentStemsIfNotExist(true).save();
      AttributeDef attributeDef = new AttributeDefSave(GrouperServiceUtils.testSession).assignName(attributeDefName).assignCreateParentStemsIfNotExist(true).save();
      AttributeDef attributeDef2 = new AttributeDefSave(GrouperServiceUtils.testSession).assignName(attributeDefName2).assignCreateParentStemsIfNotExist(true).save();
      Group group = new GroupSave(GrouperServiceUtils.testSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
      Group group2 = new GroupSave(GrouperServiceUtils.testSession).assignName(groupName2).assignCreateParentStemsIfNotExist(true).save();
      Subject subjectWithAdminPriv = SubjectFinder.findByIdOrIdentifier(subjectIdWithAdminPriv, true);
      Subject subjectWithUpdatePriv = SubjectFinder.findByIdOrIdentifier(subjectIdWithUpdatePriv, true);
      Subject subjectNoPrivs = SubjectFinder.findByIdOrIdentifier(subjectIdNoPrivs, true);
      Subject subjectMember = SubjectFinder.findByIdOrIdentifier(subjectIdMember, true);
      group.grantPriv(subjectNoPrivs, AccessPrivilege.VIEW, false);
      group.grantPriv(subjectWithAdminPriv, AccessPrivilege.ADMIN, false);
      group.grantPriv(subjectWithUpdatePriv, AccessPrivilege.UPDATE, false);
      group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      group2.grantPriv(subjectNoPrivs, AccessPrivilege.VIEW, false);
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
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      attributeDef.getPrivilegeDelegate().grantPriv(subjectWithAdminPriv, AttributeDefPrivilege.ATTR_ADMIN, false);
      attributeDef.getPrivilegeDelegate().grantPriv(subjectNoPrivs, AttributeDefPrivilege.ATTR_VIEW, false);
      attributeDef.getPrivilegeDelegate().grantPriv(subjectWithUpdatePriv, AttributeDefPrivilege.ATTR_UPDATE, false);
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_VIEW, false);
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
      attributeDef2.getPrivilegeDelegate().grantPriv(subjectWithAdminPriv, AttributeDefPrivilege.ATTR_ADMIN, false);
      attributeDef2.getPrivilegeDelegate().grantPriv(subjectWithUpdatePriv, AttributeDefPrivilege.ATTR_UPDATE, false);
      attributeDef2.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_VIEW, false);
      attributeDef2.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
      attributeDef2.getPrivilegeDelegate().grantPriv(subjectNoPrivs, AttributeDefPrivilege.ATTR_VIEW, false);



      WsGetMembershipsResults wsGetMembershipsResults = null;
      
      //#################################
      //## test that an unprivileged user cannot see memberships on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, new WsSubjectLookup[]{new WsSubjectLookup(subjectMember.getId(), null, null)}, null, null, null,
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));
      
      //#################################
      //## test that a privileged user can see memberships on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, new WsSubjectLookup[]{new WsSubjectLookup(subjectMember.getId(), null, null)}, null, null, null,
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          2, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName, subjectMember, Group.getDefaultList());
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName2, subjectMember, Group.getDefaultList());

      //#################################
      //## test that an unprivileged user cannot see memberships on a group

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectMember.getId(), null, null)}, null, null, 
          null,
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      //#################################
      //## test that a privileged user can see memberships on a group

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectMember.getId(), null, null)}, null, null, 
          null,
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          2, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName, subjectMember, Group.getDefaultList());
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName2, subjectMember, Group.getDefaultList());


      
      //#################################
      //## test that an unprivileged user cannot see privileges on a stem

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, null, null, null, null,
          false, null, false, null, null, null, null, 
          null, null, null, new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));
      

      //#################################
      //## test that a privileged user can see privileges on a stem

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          null, null, null, null,
          false, null, false, null, null, null, null, null, null, null, 
          new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          3, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName, subjectWithAdminPriv, FieldFinder.find("stemmers", true));
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName, subjectWithUpdatePriv, FieldFinder.find("creators", true));
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName, SubjectFinder.findRootSubject(), FieldFinder.find("stemmers", true));

      //#################################
      //## test that a privileged user can see privileges on a stem, filter by field

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          null, null, null, FieldFinder.find("creators", true),
          false, null, false, null, null, null, null, null, null, null, 
          new WsStemLookup[]{new WsStemLookup(stem.getName(), null)}, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          1, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName, subjectWithUpdatePriv, FieldFinder.find("creators", true));


      //#################################
      //## test that a privileged user can see privs on a stem with field

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectWithUpdatePriv.getId(), null, null)}, null, null, 
          FieldFinder.find("creators", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          2, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName, subjectWithUpdatePriv, FieldFinder.find("creators", true));
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName2, subjectWithUpdatePriv, FieldFinder.find("creators", true));



      //#################################
      //## test that a privileged user can see stem privs on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectIdWithUpdatePriv, null, null)}, null, null, null,
          false, null, false, null, null, null, null, null, null, null, null, null, FieldType.NAMING, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          2, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName, subjectWithUpdatePriv, FieldFinder.find("creators", true));
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), stemName2, subjectWithUpdatePriv, FieldFinder.find("creators", true));

      //#################################
      //## test that an unprivileged user cannot see stem privs on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectIdWithUpdatePriv, null, null)}, null, null, null,
          false, null, false, null, null, null, null, null, null, null, null, null, FieldType.NAMING, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      //#################################
      //## test that a privileged user can see group privileges on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectWithUpdatePriv.getId(), null, null)}, null, null, 
          FieldFinder.find("updaters", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          2, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName, subjectWithUpdatePriv, FieldFinder.find("updaters", true));
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName2, subjectWithUpdatePriv, FieldFinder.find("updaters", true));

      //#################################
      //## test that an unprivileged user cannot see group privileges on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectWithUpdatePriv.getId(), null, null)}, null, null, 
          FieldFinder.find("updaters", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      //#################################
      //## test that a privileged user can see privileges on a group

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
          null, null, null, 
          FieldFinder.find("updaters", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          1, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), groupName, subjectWithUpdatePriv, FieldFinder.find("updaters", true));

      //#################################
      //## test that a unprivileged user cannot see privileges on a group

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, new WsGroupLookup[]{new WsGroupLookup(group.getName(), null)}, 
          null, null, null, 
          FieldFinder.find("updaters", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));



      
      //#################################
      //## test that a privileged user can see attrDef privileges on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectWithUpdatePriv.getId(), null, null)}, null, null, 
          FieldFinder.find("attrUpdaters", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          2, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), attributeDefName, subjectWithUpdatePriv, FieldFinder.find("attrUpdaters", true));
      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), attributeDefName2, subjectWithUpdatePriv, FieldFinder.find("attrUpdaters", true));

      //#################################
      //## test that an unprivileged user cannot see attrDef privileges on a subject

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          new WsSubjectLookup[]{new WsSubjectLookup(subjectWithUpdatePriv.getId(), null, null)}, null, null, 
          FieldFinder.find("attrUpdaters", true),
          false, null, false, null, null, null, null, null, null, null, null, null, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      //#################################
      //## test that a privileged user can see privileges on an attrDef

      GrouperServiceUtils.testSession = GrouperSession.start(subjectWithAdminPriv);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          null, null, null, 
          FieldFinder.find("attrUpdaters", true),
          false, null, false, null, null, null, null, null, null, null, null, 
          new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefName, null)}, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          1, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));

      assertHasMembership(wsGetMembershipsResults.getWsMemberships(), attributeDefName, subjectWithUpdatePriv, FieldFinder.find("attrUpdaters", true));

      //#################################
      //## test that a unprivileged user cannot see privileges on a group

      GrouperServiceUtils.testSession = GrouperSession.start(subjectNoPrivs);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(
          GROUPER_VERSION, null, 
          null, null, null, 
          FieldFinder.find("attrUpdaters", true),
          false, null, false, null, null, null, null, null, null, null, null, 
          new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefName, null)}, null, null, null);

      assertEquals(GrouperUtil.toStringForLog(wsGetMembershipsResults.getWsMemberships()), 
          0, GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()));


      
    }

    /**
     * test has member
     */
    public void testAddRemoveMemberUpdatePrivs() {

      GrouperServiceUtils.testSession = GrouperSession.startRootSession();

      Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();
      
      Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
          .assignGroupNameToEdit("test:group2").assignName("test:group2").assignCreateParentStemsIfNotExist(true)
          .assignDescription("description").save();

      group1.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group1.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      
      group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    
      ChangeLogTempToEntity.convertRecords();
      
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
      WsSubjectLookup wsSubjectLookup5 = new WsSubjectLookup(SubjectTestHelper.SUBJ5.getId(), null, null);
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup5};
    
      //###############################################
      //valid query, add member
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsAddMemberResults wsAddMemberResults = GrouperServiceLogic.addMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, false, 
            null, null, null, false, false, null, null, null, null, false);
        
        assertEquals(wsAddMemberResults.getResultMetadata().getResultMessage(),
            WsAddMemberResultsCode.SUCCESS.name(), 
            wsAddMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsAddMemberResults.getWsGroupAssigned();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsAddMemberResults.getResults()));
        WsSubject wsSubject5 = wsAddMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsAddMemberResultCode.SUCCESS.name(), wsAddMemberResults.getResults()[0].resultCode().name());
      }
      
      //###############################################
      //valid query, add member again
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsAddMemberResults wsAddMemberResults = GrouperServiceLogic.addMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, false, 
            null, null, null, false, false, null, null, null, null, false);
  
        assertEquals(wsAddMemberResults.getResultMetadata().getResultMessage(),
            WsAddMemberResultsCode.SUCCESS.name(), 
            wsAddMemberResults.getResultMetadata().getResultCode());
  
        WsGroup wsGroup = wsAddMemberResults.getWsGroupAssigned();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsAddMemberResults.getResults()));
        WsSubject wsSubject5 = wsAddMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsAddMemberResultCode.SUCCESS.name(), wsAddMemberResults.getResults()[0].resultCode().name());
      }
      
      //###############################################
      //valid query, remove member
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS.name(), wsDeleteMemberResults.getResults()[0].resultCode().name());
      }    

      //###############################################
      //valid query, remove member already removed
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResults()[0].resultCode().name());
      }

      //add subj5 to group2, and add group2 to group1
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      group2.addMember(SubjectTestHelper.SUBJ5, false);
      group1.addMember(group2.toSubject(), false);
      group1.addMember(SubjectTestHelper.SUBJ5, false);
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResults()[0].resultCode().name());
      }
      
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResults()[0].resultCode().name());
      }


    }

    /**
     * test has member
     */
    public void testAddRemoveMember() {
    
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    
      Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:group1").assignName("test:group1").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();

      Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
          .assignGroupNameToEdit("test:group2").assignName("test:group2").assignCreateParentStemsIfNotExist(true)
          .assignDescription("description").save();

      group1.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group1.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      
      group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      
      group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
      group1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    
      ChangeLogTempToEntity.convertRecords();
      
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(group1.getName(), group1.getUuid());
      WsSubjectLookup wsSubjectLookup5 = new WsSubjectLookup(SubjectTestHelper.SUBJ5.getId(), null, null);
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {wsSubjectLookup5};
    
      //###############################################
      //valid query, add member
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsAddMemberResults wsAddMemberResults = GrouperServiceLogic.addMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, false, 
            null, null, null, false, false, null, null, null, null, false);
        
        assertEquals(wsAddMemberResults.getResultMetadata().getResultMessage(),
            WsAddMemberResultsCode.SUCCESS.name(), 
            wsAddMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsAddMemberResults.getWsGroupAssigned();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsAddMemberResults.getResults()));
        WsSubject wsSubject5 = wsAddMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsAddMemberResultCode.SUCCESS.name(), wsAddMemberResults.getResults()[0].resultCode().name());
      }
      
      //###############################################
      //valid query, add member again
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsAddMemberResults wsAddMemberResults = GrouperServiceLogic.addMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, false, 
            null, null, null, false, false, null, null, null, null, false);
  
        assertEquals(wsAddMemberResults.getResultMetadata().getResultMessage(),
            WsAddMemberResultsCode.SUCCESS.name(), 
            wsAddMemberResults.getResultMetadata().getResultCode());
  
        WsGroup wsGroup = wsAddMemberResults.getWsGroupAssigned();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsAddMemberResults.getResults()));
        WsSubject wsSubject5 = wsAddMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsAddMemberResultCode.SUCCESS_ALREADY_EXISTED.name(), wsAddMemberResults.getResults()[0].resultCode().name());
      }
      
      //###############################################
      //valid query, remove member
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS.name(), wsDeleteMemberResults.getResults()[0].resultCode().name());
      }    

      //###############################################
      //valid query, remove member already removed
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS_WASNT_IMMEDIATE.name(), 
            wsDeleteMemberResults.getResults()[0].resultCode().name());
      }

      //add subj5 to group2, and add group2 to group1
      GrouperServiceUtils.testSession = GrouperSession.startRootSession();
      group2.addMember(SubjectTestHelper.SUBJ5, false);
      group1.addMember(group2.toSubject(), false);
      group1.addMember(SubjectTestHelper.SUBJ5, false);
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS_BUT_HAS_EFFECTIVE.name(), 
            wsDeleteMemberResults.getResults()[0].resultCode().name());
      }
      
      {
        GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

        WsDeleteMemberResults wsDeleteMemberResults = GrouperServiceLogic.deleteMember(
            GROUPER_VERSION, wsGroupLookup, wsSubjectLookups, null, 
            null, null, false, false, null, null);
        
        assertEquals(wsDeleteMemberResults.getResultMetadata().getResultMessage(),
            WsDeleteMemberResultsCode.SUCCESS.name(), 
            wsDeleteMemberResults.getResultMetadata().getResultCode());
        
        WsGroup wsGroup = wsDeleteMemberResults.getWsGroup();
        assertEquals(group1.getUuid(), wsGroup.getUuid());
        assertEquals(group1.getName(), wsGroup.getName());
        
        assertEquals(1, GrouperUtil.length(wsDeleteMemberResults.getResults()));
        WsSubject wsSubject5 = wsDeleteMemberResults.getResults()[0].getWsSubject();
        
        assertEquals(SubjectTestHelper.SUBJ5.getId(), wsSubject5.getId());
        assertEquals(WsDeleteMemberResultCode.SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE.name(), 
            wsDeleteMemberResults.getResults()[0].resultCode().name());
      }

      
    }
    
  /**
   * test delete attributeDefs
   */
  public void testAttributeDefDelete() {

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrRead", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem testStem = new StemSave(grouperSession).assignName("test1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);

    AttributeDef testAttributeDef1 = new AttributeDefSave(grouperSession)
        .assignName("test1:attributeDef1")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
        .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName1 = new AttributeDefNameSave(grouperSession,
        testAttributeDef1).assignName("test1:attributeDefName1").save();

    AttributeDef testAttributeDef2 = new AttributeDefSave(grouperSession)
        .assignName("test1:attributeDef2")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
        .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName2 = new AttributeDefNameSave(grouperSession,
        testAttributeDef2).assignName("test1:attributeDefName2").save();

    testAttributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_ADMIN, true);
    //if you cant view you cant delete
    testAttributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1,
        AttributeDefPrivilege.ATTR_VIEW, true);

    testAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_ADMIN, true);
    //if you cant view you cant delete
    testAttributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1,
        AttributeDefPrivilege.ATTR_VIEW, true);

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    WsAttributeDefLookup[] wsAttributeDefLookups = new WsAttributeDefLookup[] {
        new WsAttributeDefLookup(testAttributeDef1.getName(),
            testAttributeDef1.getUuid()),
        new WsAttributeDefLookup(testAttributeDef2.getName(),
            testAttributeDef2.getUuid()) };

    //delete two attribute defs
    WsAttributeDefDeleteResults attributeDefDeleteResults = GrouperServiceLogic
        .attributeDefDelete(GROUPER_VERSION, wsAttributeDefLookups, null, null, null);

    assertEquals(attributeDefDeleteResults.getResultMetadata().getResultMessage(),
        WsAttributeDefDeleteResultsCode.SUCCESS.name(),
        attributeDefDeleteResults.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    testAttributeDef1 = AttributeDefFinder.findByName("test1:attributeDef1", false);
    assertNull(testAttributeDef1);

    testAttributeDef2 = AttributeDefFinder.findByName("test1:attributeDef2", false);
    assertNull(testAttributeDef2);

    //#########################
    //try as subject1

    grouperSession = GrouperSession.startRootSession();

    testAttributeDef1 = new AttributeDefSave(grouperSession)
        .assignName("test1:attributeDef1")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
        .assignToEffMembership(true).save();
    testAttributeDefName1 = new AttributeDefNameSave(grouperSession,
        testAttributeDef1).assignName("test1:attributeDefName1").save();

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    attributeDefDeleteResults = GrouperServiceLogic.attributeDefDelete(GROUPER_VERSION,
        wsAttributeDefLookups, null, null, null);

    grouperSession = GrouperSession.startRootSession();
    testAttributeDef1 = AttributeDefFinder.findByName("test1:attributeDef1", false);
    assertNotNull(testAttributeDef1);

    assertEquals("F", attributeDefDeleteResults.getResultMetadata().getSuccess());
    assertEquals("PROBLEM_DELETING_ATTRIBUTE_DEFS",
        attributeDefDeleteResults.getResultMetadata().getResultCode());
    assertEquals("F",
        attributeDefDeleteResults.getResults()[0].getResultMetadata().getSuccess());
    assertEquals("EXCEPTION",
        attributeDefDeleteResults.getResults()[0].getResultMetadata().getResultCode());

  }

  /**
   * test delete attributeDef lite
   */
  public void testAttributeDefDeleteLite() {

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrRead", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem testStem = new StemSave(grouperSession).assignName("test1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);

    AttributeDef testAttributeDef = new AttributeDefSave(grouperSession)
        .assignName("test1:attributeDef1")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
        .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName1 = new AttributeDefNameSave(grouperSession,
        testAttributeDef).assignName("test1:attributeDefName1").save();

    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_ADMIN, true);

    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    //Delete attribute def
    WsAttributeDefDeleteLiteResult attributeDefDeleteLiteResult = GrouperServiceLogic
        .attributeDefDeleteLite(GROUPER_VERSION, testAttributeDef.getName(),
            testAttributeDef.getUuid(),
            null, null, null, null, null, null, null, null);

    assertEquals(attributeDefDeleteLiteResult.getResultMetadata().getResultMessage(),
        WsAttributeDefDeleteLiteResultCode.SUCCESS.name(),
        attributeDefDeleteLiteResult.getResultMetadata().getResultCode());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    testAttributeDef = AttributeDefFinder.findByName("test1:attributeDef1", false);
    assertNull(testAttributeDef);

  }

  /**
   * test find attribute defs
   */
  public void testFindAttributeDefs() {

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrRead", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem testStem = new StemSave(grouperSession).assignName("testabc1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("testabc1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);

    AttributeDef testAttributeDef = new AttributeDefSave(grouperSession)
        .assignName("testabc1:attributeDef1")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
        .assignToEffMembership(true).save();
    AttributeDefName testAttributeDefName1 = new AttributeDefNameSave(grouperSession,
        testAttributeDef).assignName("testabc1:attributeDefName1").save();
    AttributeDefName testAttributeDefName2 = new AttributeDefNameSave(grouperSession,
        testAttributeDef)
            .assignName("testabc2:attributeDefName2")
            .assignCreateParentStemsIfNotExist(true).save();

    testAttributeDefName1.getAttributeDefNameSetDelegate()
        .addToAttributeDefNameSet(testAttributeDefName2);

    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, true);
    testAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1,
        AttributeDefPrivilege.ATTR_ADMIN, true);

    WsAttributeDefLookup[] wsAttributeDefLookups = new WsAttributeDefLookup[] {
        new WsAttributeDefLookup(testAttributeDef.getName(),
            testAttributeDef.getUuid()) };

    //find no results
    WsFindAttributeDefsResults wsFindAttributeDefsResults = GrouperServiceLogic
        .findAttributeDefs(GROUPER_VERSION, "testabc3%", null, wsAttributeDefLookups,
            null, null, null, null, null, null, null, null, null, null);

    assertEquals(wsFindAttributeDefsResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefsResultsCode.SUCCESS.name(),
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    assertEquals(0,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));

    //find all results
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        null, null, null, null, null, null);

    assertEquals(wsFindAttributeDefsResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefsResultsCode.SUCCESS.name(),
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    assertEquals(1,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));
    assertEquals(testAttributeDef.getName(),
        wsFindAttributeDefsResults.getAttributeDefResults()[0].getName());

    //#############################################
    //no scope and no lookups is an exception
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, null, null, null, null, null, null, null, null, null, null, null,
        null, null);
    assertEquals("F", wsFindAttributeDefsResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY",
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    //#############################################
    //if you pass a page number, you need a page size
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        null, 1, null, null, null, null);
    assertEquals("F", wsFindAttributeDefsResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY",
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    //#############################################
    //if you pass a split scope, you need a scope
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, null, true, wsAttributeDefLookups, null, null, null, null, null,
        1, null, null, null, null);
    assertEquals("F", wsFindAttributeDefsResults.getResultMetadata().getSuccess());
    assertEquals("INVALID_QUERY",
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    //#############################################
    //page by size 1, page number 2
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    AttributeDef testAttributeDef1 = new AttributeDefSave(grouperSession)
        .assignName("testabc1:attributeDef2")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
        .assignToEffMembership(true).save();
    testAttributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, true);
    testAttributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1,
        AttributeDefPrivilege.ATTR_ADMIN, true);
    wsAttributeDefLookups = new WsAttributeDefLookup[] {
        new WsAttributeDefLookup(testAttributeDef.getName(), testAttributeDef.getUuid()),
        new WsAttributeDefLookup(testAttributeDef1.getName(),
            testAttributeDef1.getUuid()) };
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        1, 2, null, null, null, null);

    assertEquals(wsFindAttributeDefsResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(),
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    assertEquals(1,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));
    assertEquals(testAttributeDef1.getName(),
        wsFindAttributeDefsResults.getAttributeDefResults()[0].getName());

    //#############################################
    //page by size 1, page number 2, secure
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        1, 2, "name", false, null, null);

    assertEquals(wsFindAttributeDefsResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(),
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    assertEquals(1,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));
    assertEquals(testAttributeDef.getName(),
        wsFindAttributeDefsResults.getAttributeDefResults()[0].getName());

    //#############################################
    //page by size 1, page number 2, secure
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        1, 2, "name", false, null, null);

    assertEquals(wsFindAttributeDefsResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(),
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    assertEquals(1,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));
    assertEquals(testAttributeDef.getName(),
        wsFindAttributeDefsResults.getAttributeDefResults()[0].getName());

    //#############################################
    //page by size 1, page number 2, secure
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        1, 2, "name", false, null, null);

    assertEquals(0,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));

    //#############################################
    //paging and find by attribute def idIndex
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    wsAttributeDefLookups = new WsAttributeDefLookup[] {
        new WsAttributeDefLookup(null, null, testAttributeDef.getIdIndex().toString()),
        new WsAttributeDefLookup(null, null, testAttributeDef1.getIdIndex().toString()) };
    wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
        GROUPER_VERSION, "testabc%", null, wsAttributeDefLookups, null, null, null, null,
        1, 2, "name", false, null, null);

    assertEquals(wsFindAttributeDefsResults.getResultMetadata().getResultMessage(),
        WsFindAttributeDefNamesResultsCode.SUCCESS.name(),
        wsFindAttributeDefsResults.getResultMetadata().getResultCode());

    assertEquals(1,
        GrouperUtil.length(wsFindAttributeDefsResults.getAttributeDefResults()));
    assertEquals(testAttributeDef.getName(),
        wsFindAttributeDefsResults.getAttributeDefResults()[0].getName());

  }

  /**
   * test save attribute defs
   */
  public void testAttributeDefSave() {

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("attributeDefs.create.grant.all.attrRead", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem testStem = new StemSave(grouperSession).assignName("test1").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test1:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);

    WsAttributeDefToSave wsAttributeDefToSave1 = new WsAttributeDefToSave();

    WsAttributeDef wsAttributeDef1 = new WsAttributeDef();
    wsAttributeDef1.setName("test1:attributeDef1");
    wsAttributeDef1.setAttributeDefType("perm");
    wsAttributeDef1.setDescription("Perm Attribute Def");
    wsAttributeDef1.setAssignableTos(new String[] { "GROUP", "EFFECTIVE_MEMBERSHIP" });
    wsAttributeDef1.setValueType("marker");
    wsAttributeDefToSave1.setWsAttributeDef(wsAttributeDef1);
    //wsAttributeDefToSave1.setCreateParentStemsIfNotExist("T");

    WsAttributeDefToSave wsAttributeDefToSave2 = new WsAttributeDefToSave();

    WsAttributeDef wsAttributeDef2 = new WsAttributeDef();
    wsAttributeDef2.setAssignableTos(new String[] { "ATTRIBUTE_DEF" });
    wsAttributeDef2.setAttributeDefType("attr");
    wsAttributeDef2.setMultiAssignable("F");
    wsAttributeDef2.setMultiValued("F");
    wsAttributeDef2.setValueType("string");

    wsAttributeDef2.setName("test1:attributeDef2");
    wsAttributeDef2.setDescription("This is a description2");

    wsAttributeDefToSave2.setWsAttributeDef(wsAttributeDef2);

    //wsAttributeDefToSave2.setCreateParentStemsIfNotExist("T");

    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, true);

    //save two attribute defs
    WsAttributeDefSaveResults wsAttributeDefSaveResults = GrouperServiceLogic
        .attributeDefSave(GROUPER_VERSION,
            new WsAttributeDefToSave[] { wsAttributeDefToSave1, wsAttributeDefToSave2 },
            null, null, null);

    assertEquals(wsAttributeDefSaveResults.getResultMetadata().getResultMessage(),
        WsAttributeDefSaveResultsCode.SUCCESS.name(),
        wsAttributeDefSaveResults.getResultMetadata().getResultCode());

    assertEquals(2, GrouperUtil.length(wsAttributeDefSaveResults.getResults()));
    assertEquals("test1:attributeDef1",
        wsAttributeDefSaveResults.getResults()[0].getWsAttributeDef().getName());
    assertEquals("Perm Attribute Def",
        wsAttributeDefSaveResults.getResults()[0].getWsAttributeDef().getDescription());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    AttributeDef testAttributeDef2 = AttributeDefFinder.findByName("test1:attributeDef2",
        true);
    assertEquals(testAttributeDef2.getDescription(),
        wsAttributeDefSaveResults.getResults()[1].getWsAttributeDef().getDescription());

    //#########################
    //try as subject0
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    wsAttributeDefToSave1 = new WsAttributeDefToSave();

    wsAttributeDef1 = new WsAttributeDef();
    wsAttributeDef1.setName("test1:attributeDef1");
    wsAttributeDef1.setAttributeDefType("perm");
    wsAttributeDef1.setDescription("Perm Attribute Def");
    wsAttributeDef1.setAssignableTos(new String[] { "GROUP", "EFFECTIVE_MEMBERSHIP" });
    wsAttributeDef1.setValueType("marker");
    wsAttributeDefToSave1.setWsAttributeDef(wsAttributeDef1);
    //wsAttributeDefToSave1.setCreateParentStemsIfNotExist("T");

    wsAttributeDefToSave2 = new WsAttributeDefToSave();

    wsAttributeDef2 = new WsAttributeDef();
    wsAttributeDef2.setAssignableTos(new String[] { "ATTRIBUTE_DEF" });
    wsAttributeDef2.setAttributeDefType("attr");
    wsAttributeDef2.setMultiAssignable("F");
    wsAttributeDef2.setMultiValued("F");
    wsAttributeDef2.setValueType("string");

    wsAttributeDef2.setName("test1:attributeDef2");
    wsAttributeDef2.setDescription("This is a description2");

    wsAttributeDefToSave2.setWsAttributeDef(wsAttributeDef2);

    //save two attribute defs
    wsAttributeDefSaveResults = GrouperServiceLogic.attributeDefSave(GROUPER_VERSION,
        new WsAttributeDefToSave[] { wsAttributeDefToSave1, wsAttributeDefToSave2 }, null,
        null, null);

    assertEquals(wsAttributeDefSaveResults.getResultMetadata().getResultMessage(),
        WsAttributeDefSaveResultsCode.SUCCESS.name(),
        wsAttributeDefSaveResults.getResultMetadata().getResultCode());

    assertEquals(2, GrouperUtil.length(wsAttributeDefSaveResults.getResults()));

    assertEquals("test1:attributeDef1",
        wsAttributeDefSaveResults.getResults()[0].getWsAttributeDef().getName());
    assertEquals("Perm Attribute Def",
        wsAttributeDefSaveResults.getResults()[0].getWsAttributeDef().getDescription());

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    testAttributeDef2 = AttributeDefFinder.findByName("test1:attributeDef2", true);
    assertEquals(testAttributeDef2.getDescription(),
        wsAttributeDefSaveResults.getResults()[1].getWsAttributeDef().getDescription());

    //#########################
    //try as subject1, not allowed
    GrouperServiceUtils.testSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    wsAttributeDefToSave1 = new WsAttributeDefToSave();

    wsAttributeDef1 = new WsAttributeDef();
    wsAttributeDef1.setName("test1:attributeDef1");
    wsAttributeDef1.setAttributeDefType("perm");
    wsAttributeDef1.setDescription("Perm Attribute Def");
    wsAttributeDef1.setAssignableTos(new String[] { "GROUP", "EFFECTIVE_MEMBERSHIP" });
    wsAttributeDef1.setValueType("marker");
    wsAttributeDefToSave1.setWsAttributeDef(wsAttributeDef1);

    wsAttributeDefToSave2 = new WsAttributeDefToSave();

    wsAttributeDef2 = new WsAttributeDef();
    wsAttributeDef2.setAssignableTos(new String[] { "ATTRIBUTE_DEF" });
    wsAttributeDef2.setAttributeDefType("attr");
    wsAttributeDef2.setMultiAssignable("F");
    wsAttributeDef2.setMultiValued("F");
    wsAttributeDef2.setValueType("string");

    wsAttributeDef2.setName("test1:attributeDef2");
    wsAttributeDef2.setDescription("This is a description2");

    wsAttributeDefToSave2.setWsAttributeDef(wsAttributeDef2);

    wsAttributeDefSaveResults = GrouperServiceLogic.attributeDefSave(GROUPER_VERSION,
        new WsAttributeDefToSave[] { wsAttributeDefToSave1, wsAttributeDefToSave2 }, null,
        null, null);

    assertEquals("F", wsAttributeDefSaveResults.getResultMetadata().getSuccess());
    assertEquals("PROBLEM_SAVING_ATTRIBUTE_DEFS",
        wsAttributeDefSaveResults.getResultMetadata().getResultCode());
    assertEquals("F",
        wsAttributeDefSaveResults.getResults()[0].getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES",
        wsAttributeDefSaveResults.getResults()[0].getResultMetadata().getResultCode());
    assertEquals("F",
        wsAttributeDefSaveResults.getResults()[1].getResultMetadata().getSuccess());
    assertEquals("INSUFFICIENT_PRIVILEGES",
        wsAttributeDefSaveResults.getResults()[1].getResultMetadata().getResultCode());

  }
  
  /**
   * test send messages
   */
  public void testSendMessages() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.use.builtin.messaging", "true");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.default.name.of.messaging.system", "grouperBuiltinMessaging");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.system.grouperBuiltinMessaging.name", "grouperBuiltinMessaging");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.system.grouperBuiltinMessaging.class","edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem");
    
    GrouperBuiltinMessagingSystem.createQueue("abc");
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0);

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    WsMessage wsMessage = new WsMessage();
    wsMessage.setMessageBody("test message body");
    
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
   
    // happy path
    WsMessageResults wsMessageResults = GrouperServiceLogic.sendMessage(GROUPER_VERSION, GrouperMessageQueueType.queue, "abc", null, new WsMessage[] {wsMessage}, actAsSubjectLookup, null);

    assertEquals(wsMessageResults.getResultMetadata().getResultMessage(),
            WsMessageResultsCode.SUCCESS.name(), 
            wsMessageResults.getResultMetadata().getResultCode());
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GrouperMessageHibernate grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);
    
    // null/blank queueTopicName not allowed
    wsMessageResults = GrouperServiceLogic.sendMessage(GROUPER_VERSION, GrouperMessageQueueType.queue, null, null, new WsMessage[] {wsMessage}, actAsSubjectLookup, null);
    assertEquals(wsMessageResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(), 
        wsMessageResults.getResultMetadata().getResultCode());
  
    // there has to be at least one message
    wsMessageResults = GrouperServiceLogic.sendMessage(GROUPER_VERSION, GrouperMessageQueueType.queue, "abc", null, new WsMessage[] {}, actAsSubjectLookup, null);
    assertEquals(wsMessageResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(), 
        wsMessageResults.getResultMetadata().getResultCode());
    
  }
  
  /**
   * test receive messages 
   */
  public void testReceiveMessages() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.use.builtin.messaging", "true");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.default.name.of.messaging.system", "grouperBuiltinMessaging");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.system.grouperBuiltinMessaging.name", "grouperBuiltinMessaging");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.system.grouperBuiltinMessaging.class","edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem");
    
    GrouperBuiltinMessagingSystem.createQueue("abc");
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ1);

    GrouperSession.start(SubjectTestHelper.SUBJ0);
        
    GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueOrTopicName("abc")
        .addMessageBody("message body").assignQueueType(GrouperMessageQueueType.queue));
    
    
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null);
    // happy path
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    WsMessageResults wsMessageResults = GrouperServiceLogic.receiveMessage(GROUPER_VERSION, "abc", null, 0, 10, actAsSubjectLookup, null);
    assertTrue("test", wsMessageResults.getMessages().length > 0);
    
    // null/blank queueTopicName not allowed
    wsMessageResults = GrouperServiceLogic.receiveMessage(GROUPER_VERSION, null, null, 0, 10, actAsSubjectLookup, null);
    assertEquals(wsMessageResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(), 
        wsMessageResults.getResultMetadata().getResultCode());
    
  }
  
  /**
   * test acknowledge messages
   */
  public void testAcknowledgeMessages() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.use.builtin.messaging", "true");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.default.name.of.messaging.system", "grouperBuiltinMessaging");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.system.grouperBuiltinMessaging.name", "grouperBuiltinMessaging");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.system.grouperBuiltinMessaging.class","edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem");
    
    GrouperBuiltinMessagingSystem.createQueue("abc");
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0);

    GrouperSession.start(SubjectTestHelper.SUBJ0);
    GrouperMessagingEngine.send(new GrouperMessageSendParam().assignQueueOrTopicName("abc")
        .addMessageBody("message body").assignQueueType(GrouperMessageQueueType.queue));
    
    GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(1, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    GrouperMessage grouperMessage = grouperMessageReceiveResult.getGrouperMessages().iterator().next();

    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(SubjectTestHelper.SUBJ0.getId(), null, null);
    
    // happy path
    WsMessageAcknowledgeResults messageAcknowledgeResults = GrouperServiceLogic.acknowledge(GROUPER_VERSION, "abc", null, 
        GrouperMessageAcknowledgeType.mark_as_processed,
        new String[] {grouperMessage.getId()}, null, null, actAsSubjectLookup, null);
    
    assertEquals(grouperMessage.getId(), messageAcknowledgeResults.getMessageIds()[0]);
    
    // null/blank queueTopicName not allowed
    messageAcknowledgeResults = GrouperServiceLogic.acknowledge(GROUPER_VERSION, null, null, 
        GrouperMessageAcknowledgeType.mark_as_processed,
        new String[] {grouperMessage.getId()}, null, null, actAsSubjectLookup, null);
    
    assertEquals(messageAcknowledgeResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(),
        messageAcknowledgeResults.getResultMetadata().getResultCode());
  
    // there has to be at least one message
    messageAcknowledgeResults = GrouperServiceLogic.acknowledge(GROUPER_VERSION, "abc", null, 
        GrouperMessageAcknowledgeType.mark_as_processed,
        new String[] {}, null, null, actAsSubjectLookup, null);
    
    assertEquals(messageAcknowledgeResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(),
        messageAcknowledgeResults.getResultMetadata().getResultCode());
    
    // anotherQueueOrTopicName and anotherQueueType both has to be passed in
    messageAcknowledgeResults = GrouperServiceLogic.acknowledge(GROUPER_VERSION, "abc", null, 
        GrouperMessageAcknowledgeType.mark_as_processed,
        new String[] {grouperMessage.getId()}, null, GrouperMessageQueueType.queue, actAsSubjectLookup, null);
    
    assertEquals(messageAcknowledgeResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(),
        messageAcknowledgeResults.getResultMetadata().getResultCode());
    
    messageAcknowledgeResults = GrouperServiceLogic.acknowledge(GROUPER_VERSION, "abc", null, 
        GrouperMessageAcknowledgeType.mark_as_processed,
        new String[] {grouperMessage.getId()}, "def", null, actAsSubjectLookup, null);
    
    assertEquals(messageAcknowledgeResults.getResultMetadata().getResultMessage(),
        WsMessageResultsCode.INVALID_QUERY.name(),
        messageAcknowledgeResults.getResultMetadata().getResultCode());
  }
    
    
}
