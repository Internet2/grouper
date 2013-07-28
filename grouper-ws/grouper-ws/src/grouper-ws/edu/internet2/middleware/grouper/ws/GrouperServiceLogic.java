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
  /*nerer
 * @author mchyzer $Id: GrouperServiceLogic.java,v 1.41 2009/12/30 07:07:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.permissions.PermissionAssignOperation;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.GrouperPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.NamingResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.*;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeDefNameInheritanceResults.WsAssignAttributeDefNameInheritanceResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResult.WsAssignGrouperPrivilegesResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResults.WsAssignGrouperPrivilegesResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResult.WsAttributeDefNameDeleteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup.AttributeDefNameFindResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResult.WsAttributeDefNameSaveResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResult.WsDeleteMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults.WsFindAttributeDefNamesResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindGroupsResults.WsFindGroupsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindStemsResults.WsFindStemsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult.WsGetGrouperPrivilegesLiteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults.WsGetMembershipsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetPermissionAssignmentsResults.WsGetPermissionAssignmentsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults.WsGetSubjectsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteResult.WsGroupDeleteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup.GroupFindResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResult.WsGroupSaveResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectResult.WsMemberChangeSubjectResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteResult.WsStemDeleteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup.StemFindResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveResult.WsStemSaveResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup.MemberFindResult;
import edu.internet2.middleware.grouper.ws.exceptions.WebServiceDoneException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsAssignAttributeLogic;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsInheritanceSetRelation;
import edu.internet2.middleware.grouper.ws.rest.subject.TooManyResultsWhenFilteringByGroupException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Meant to be delegate from GrouperService which has the same params (and names)
 * with enums translated (for Simple objects like Field) for each Javadoc viewing.
 */
public class GrouperServiceLogic {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(GrouperServiceLogic.class);

  /**
   * add member to a group (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   *            group to add the members to
   * @param subjectLookups
   *            subjects to be added to the group
   * @param replaceAllExisting
   *            optional: T or F (default), if the existing groups should be
   *            replaced
   * @param actAsSubjectLookup
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list).  by fieldName
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params
   *            optional: reserved for future use
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param disabledTime date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS
   * @param enabledTime date this membership will be enabled (for future provisioning), yyyy/MM/dd HH:mm:ss.SSS
   * @param addExternalSubjectIfNotFound T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   * @return the results.  return the subject lookup only if there are problems retrieving the subject.
   * @see GrouperVersion
   */
  @SuppressWarnings("unchecked")
  public static WsAddMemberResults addMember(final GrouperVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final boolean replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
      Field fieldName, GrouperTransactionType txType,
      final boolean includeGroupDetail, final boolean includeSubjectDetail,
      final String[] subjectAttributeNames, final WsParam[] params, final Timestamp disabledTime, 
      final Timestamp enabledTime, final boolean addExternalSubjectIfNotFound  ) {
    final WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();

    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAddMemberResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 100) + "\n, replaceAllExisting: "
          + replaceAllExisting + ", actAsSubject: " + actAsSubjectLookup
          + ", fieldName: " + GrouperServiceUtils.fieldName(fieldName) + ", txType: "
          + txType + ", includeGroupDetail: " + includeGroupDetail
          + ", includeSubjectDetail: " + includeSubjectDetail
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames, 50) + "\n, params: "
          + GrouperUtil.toStringForLog(params, 100) + "\n, disabledDate: " + disabledTime
          + ", enabledDate: " + enabledTime;

      final String THE_SUMMARY = theSummary;
      
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      final GrouperSession SESSION = session;

      final Field FIELD_CALCULATED = fieldName == null ? Group.getDefaultList() : fieldName;
      
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);

              int subjectLength = GrouperUtil.length(subjectLookups);

              final Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");

              String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
                  .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

              wsAddMemberResults
                  .setSubjectAttributeNames(subjectAttributeNamesToRetrieve);

              //assign the group to the result to be descriptive
              wsAddMemberResults
                  .setWsGroupAssigned(new WsGroup(group, wsGroupLookup, includeGroupDetail));

              int resultIndex = 0;

              Set<MultiKey> newSubjects = new HashSet<MultiKey>();
              
              if (subjectLength > 0) {
                wsAddMemberResults.setResults(new WsAddMemberResult[subjectLength]);
              }

              //get existing members if replacing
              Set<Member> members = null;
              if (replaceAllExisting) {
                try {
                  // see who is there
                  members = group.getImmediateMembers(FIELD_CALCULATED);
                } catch (SchemaException se) {
                  throw new WsInvalidQueryException(
                      "Problem with getting existing members: " + FIELD_CALCULATED + ".  "
                          + ExceptionUtils.getFullStackTrace(se));
                }
              }

              for (final WsSubjectLookup wsSubjectLookup : GrouperUtil.nonNull(subjectLookups, WsSubjectLookup.class)) {
                final WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
                wsAddMemberResults.getResults()[resultIndex++] = wsAddMemberResult;
                try {

                  final Subject subject = wsSubjectLookup.retrieveSubject(addExternalSubjectIfNotFound);

                  wsAddMemberResult.processSubject(wsSubjectLookup,
                      subjectAttributeNamesToRetrieve);

                  if (subject == null) {
                    continue;
                  }

                  // keep track
                  if (replaceAllExisting) {
                    newSubjects.add(new MultiKey(subject.getId(), subject.getSource().getId()));
                  }
                  HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                    
                    public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {
                      try {
                        boolean didntAlreadyExist = false;

                        didntAlreadyExist = group.addMember(subject, FIELD_CALCULATED, false);

                        final boolean dealWithDates = enabledTime != null || disabledTime != null;
                        if (dealWithDates) {
                          //get the membership
                          Membership membership = group.getImmediateMembership(FIELD_CALCULATED, subject, true, true);

                          boolean needsUpdate = false;
                          if (!GrouperUtil.equals(disabledTime, membership.getDisabledTime())) {
                            membership.setDisabledTime(disabledTime);
                            needsUpdate = true;
                          }
                          if (!GrouperUtil.equals(enabledTime, membership.getEnabledTime())) {
                            membership.setEnabledTime(enabledTime);
                            needsUpdate = true;
                          }
                          if (needsUpdate) {
                            membership.update();
                          }
                        }

                        wsAddMemberResult.assignResultCode(GrouperWsVersionUtils.addMemberSuccessResultCode(didntAlreadyExist, wsSubjectLookup.retrieveSubjectFindResult()));

                      } catch (InsufficientPrivilegeException ipe) {
                        wsAddMemberResult
                            .assignResultCode(WsAddMemberResultCode.INSUFFICIENT_PRIVILEGES);
                      }
                      return null;
                    }
                  });
                  
                } catch (Exception e) {
                  wsAddMemberResult.assignResultCodeException(e, wsSubjectLookup);
                }
              }

              // after adding all these, see if we are removing:
              if (replaceAllExisting) {

                for (Member member : members) {
                  Subject subject = null;
                  try {
                    subject = member.getSubject();

                    if (!newSubjects.contains(new MultiKey(subject.getId(), subject.getSource().getId()))) {
                      group.deleteMember(subject, FIELD_CALCULATED);
                    }
                  } catch (Exception e) {
                    String theError = "Error deleting subject: " + subject
                        + " from group: " + group + ", field: "
                        + GrouperServiceUtils.fieldName(FIELD_CALCULATED) + ", " + e + ".  ";
                    wsAddMemberResults.assignResultCodeException(
                        WsAddMemberResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
                  }
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsAddMemberResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }

              return wsAddMemberResults;

            }

          });
    } catch (Exception e) {
      wsAddMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }

    //this should be the first and only return, or else it is exiting too early
    return wsAddMemberResults;
  }

  /**
   * add member to a group (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to add (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to add (mutually exclusive with subjectId)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   *  of the group (certain list)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param disabledTime date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS
   * @param enabledTime date this membership will be enabled (for future provisioning), yyyy/MM/dd HH:mm:ss.SSS
   * @param addExternalSubjectIfNotFound T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   * @return the result of one member add
   */
  public static WsAddMemberLiteResult addMemberLite(
      final GrouperVersion clientVersion, String groupName, String groupUuid,
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      Field fieldName, boolean includeGroupDetail, boolean includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1, final Timestamp disabledTime, 
      final Timestamp enabledTime, final boolean addExternalSubjectIfNotFound) {

    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);


    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsAddMemberResults wsAddMemberResults = addMember(clientVersion, wsGroupLookup,
        subjectLookups, false, actAsSubjectLookup, fieldName, null, includeGroupDetail,
        includeSubjectDetail, subjectAttributeArray, params, disabledTime, enabledTime, 
        addExternalSubjectIfNotFound);

    WsAddMemberLiteResult wsAddMemberLiteResult = new WsAddMemberLiteResult(
        wsAddMemberResults);
    return wsAddMemberLiteResult;
  }

  /**
   * remove member(s) from a group (if not already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   * @param subjectLookups
   *            subjects to be deleted to the group
   * @param actAsSubjectLookup
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsDeleteMemberResults deleteMember(final GrouperVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final WsSubjectLookup actAsSubjectLookup, final Field fieldName,
      GrouperTransactionType txType, final boolean includeGroupDetail, 
      final boolean includeSubjectDetail, String[] subjectAttributeNames, 
      final WsParam[] params) {
  
    final WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsDeleteMemberResults.getResponseMetadata().warnings());
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 100) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName + ", txType: " + txType
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
      
      final String THE_SUMMARY = theSummary;
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsDeleteMemberResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000, "subjectLookups");
  
              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
  
              //assign the group to the result to be descriptive
              wsDeleteMemberResults.setWsGroup(new WsGroup(group, wsGroupLookup,
                  includeGroupDetail));
  
              wsDeleteMemberResults.setResults(new WsDeleteMemberResult[subjectLength]);
  
              int resultIndex = 0;
  
              //loop through all subjects and do the delete
              for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
                WsDeleteMemberResult wsDeleteMemberResult = new WsDeleteMemberResult();
                wsDeleteMemberResults.getResults()[resultIndex++] = wsDeleteMemberResult;
                try {
  
                  //NOTE: deal with member here so unresolvables can be removed
                  Member member = wsSubjectLookup.retrieveMember();
                  wsDeleteMemberResult.processSubject(wsSubjectLookup, subjectAttributeNamesToRetrieve, false);
  
                  try {
  
                    boolean hasImmediate = false;
                    boolean hasEffective = false;
                    if (member != null) {
                      if (fieldName == null) {
                        // dont fail if already a direct member
                        hasEffective = member.isEffectiveMember(group);
                        hasImmediate = member.isImmediateMember(group);
                        if (hasImmediate) {
                          group.deleteMember(member);
                        }
                      } else {
                        // dont fail if already a direct member
                        hasEffective = member.isEffectiveMember(group, fieldName);
                        hasImmediate = member.isImmediateMember(group, fieldName);
                        if (hasImmediate) {
                          group.deleteMember(member, fieldName);
                        }
                      }
                    }
                    if (LOG.isDebugEnabled()) {
                      LOG.debug("deleteMember: " + group.getName() + ", " + member.getSubjectSourceId() 
                          + ", " + member.getSubjectId() + ", eff? " + hasEffective + ", imm? " + hasImmediate);
                    }
  
                    //assign one of 4 success codes
                    wsDeleteMemberResult.assignResultCodeSuccess(hasImmediate,
                        hasEffective);
  
                  } catch (InsufficientPrivilegeException ipe) {
                    wsDeleteMemberResult
                        .assignResultCode(WsDeleteMemberResultCode.INSUFFICIENT_PRIVILEGES);
                  }
                } catch (Exception e) {
                  wsDeleteMemberResult.assignResultCodeException(e, wsSubjectLookup);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsDeleteMemberResults
                  .tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsDeleteMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsDeleteMemberResults;
  }

  /**
   * delete member to a group (if not already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to lookup the subject (mutually exclusive with
   *            subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to lookup the subject (mutually exclusive with subjectId)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member delete
   */
  public static WsDeleteMemberLiteResult deleteMemberLite(final GrouperVersion clientVersion,
      String groupName, String groupUuid, String subjectId, String subjectSourceId,
      String subjectIdentifier, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, final Field fieldName,
      final boolean includeGroupDetail, boolean includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsDeleteMemberResults wsDeleteMemberResults = deleteMember(clientVersion,
        wsGroupLookup, subjectLookups, actAsSubjectLookup, fieldName, null,
        includeGroupDetail, includeSubjectDetail, subjectAttributeArray, params);
  
    return new WsDeleteMemberLiteResult(wsDeleteMemberResults);
  }

  /**
   * find a group or groups
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsQueryFilter is the filter properties that can search by
   * name, uuid, attribute, type, and can do group math on multiple operations, etc
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @param wsGroupLookups if you want to just pass in a list of uuids and/or names.  Note the groups are returned
   * in alphabetical order
   * @return the groups, or no groups if none found
   */
  @SuppressWarnings("unchecked")
  public static WsFindGroupsResults findGroups(final GrouperVersion clientVersion,
      WsQueryFilter wsQueryFilter, 
      WsSubjectLookup actAsSubjectLookup, boolean includeGroupDetail, WsParam[] params, WsGroupLookup[] wsGroupLookups) {
  
    final WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsFindGroupsResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion + ", wsQueryFilter: "
          + wsQueryFilter + "\n, includeGroupDetail: " + includeGroupDetail
          + ", actAsSubject: " + actAsSubjectLookup + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n, wsGroupLookups: " + GrouperUtil.toStringForLog(wsGroupLookups, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      Set<Group> groups = new TreeSet<Group>();
      
      if (wsQueryFilter != null) {
        wsQueryFilter.assignGrouperSession(session);
    
        //make sure filter is ok to use
        wsQueryFilter.validate();
    
        //run the query
        QueryFilter queryFilter = wsQueryFilter.retrieveQueryFilter();
        GrouperQuery grouperQuery = GrouperQuery.createQuery(session, queryFilter);
        groups.addAll(grouperQuery.getGroups());
      }
      
      //we could do this in fewer queries if we like...
      for (WsGroupLookup wsGroupLookup : GrouperUtil.nonNull(wsGroupLookups, WsGroupLookup.class)) {
        wsGroupLookup.retrieveGroupIfNeeded(session);
        Group group = wsGroupLookup.retrieveGroup();
        if (group != null) {
          groups.add(group);
        }
      }
      
      wsFindGroupsResults.assignGroupResult(groups, includeGroupDetail);
  
      wsFindGroupsResults.assignResultCode(WsFindGroupsResultsCode.SUCCESS);
      
      wsFindGroupsResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsFindGroupsResults;
  }

  /**
   * find a group or groups
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param queryFilterType findGroupType is the WsQueryFilterType enum for which 
   * type of find is happening:  e.g.
   * FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE,  FIND_BY_GROUP_NAME_APPROXIMATE,
   * FIND_BY_TYPE, AND, OR, MINUS;
   * @param groupName search by group name (context in query type)
   * @param stemName
   *            will return groups in this stem.  can be used with various query types
   * @param stemNameScope
   *            if searching by stem, ONE_LEVEL is for one level,
   *            ALL_IN_SUBTREE will return all in sub tree. Required if
   *            searching by stem
   * @param groupUuid
   *            search by group uuid (must match exactly), cannot use other
   *            params with this
   * @param groupAttributeName if searching by attribute, this is name,
   * or null for all attributes
   * @param groupAttributeValue if searching by attribute, this is the value
   * @param groupTypeName if searching by type, this is the type.  not yet implemented
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId
   *            optional to narrow the act as subject search to a particular source 
   * @param includeGroupDetail T or F as for if group detail should be included
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  
   * If you pass true or false, must pass a sort string
   * @param typeOfGroups is comma separated TypeOfGroups to find, e.g. group, role, entity
   * @return the groups, or no groups if none found
   */
  public static WsFindGroupsResults findGroupsLite(final GrouperVersion clientVersion,
      WsQueryFilterType queryFilterType, String groupName, String stemName, StemScope stemNameScope,
      String groupUuid, String groupAttributeName, String groupAttributeValue,
      GroupType groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1, String pageSize, 
      String pageNumber, String sortString, String ascending, String typeOfGroups) {
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsQueryFilter wsQueryFilter = new WsQueryFilter();
    wsQueryFilter.setQueryFilterType(queryFilterType == null ? null : queryFilterType.name());
    wsQueryFilter.setGroupName(groupName);
    wsQueryFilter.setStemName(stemName);
    wsQueryFilter.setStemNameScope(stemNameScope == null ? null : stemNameScope.name());
    wsQueryFilter.setGroupUuid(groupUuid);
    wsQueryFilter.setGroupAttributeName(groupAttributeName);
    wsQueryFilter.setGroupAttributeValue(groupAttributeValue);
    wsQueryFilter.setGroupTypeName(groupTypeName == null ? null : groupTypeName.getName());
    wsQueryFilter.setPageSize(pageSize);
    wsQueryFilter.setPageNumber(pageNumber);
    wsQueryFilter.setSortString(sortString);
    wsQueryFilter.setAscending(ascending);
    wsQueryFilter.setTypeOfGroups(typeOfGroups);
    
    // pass through to the more comprehensive method
    WsFindGroupsResults wsFindGroupsResults = findGroups(clientVersion, wsQueryFilter,
        actAsSubjectLookup, includeGroupDetail, params, null);
  
    return wsFindGroupsResults;
  }

  /**
   * find a stem or stems
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsStemQueryFilter is the filter properties that can search by
   * name, uuid, approximate attribute, and can do group math on multiple operations, etc
   * @param includeStemDetail T or F as to if the stem detail should be
   * included (defaults to F)
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @param wsStemLookups to pass in a list of uuids or names to lookup.  Note the stems are returned
   * in alphabetical order
   * @return the stems, or no stems if none found
   */
  @SuppressWarnings("unchecked")
  public static WsFindStemsResults findStems(final GrouperVersion clientVersion,
      WsStemQueryFilter wsStemQueryFilter, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params, WsStemLookup[] wsStemLookups) {
  
    final WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsFindStemsResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion + ", wsStemQueryFilter: "
          + wsStemQueryFilter + ", actAsSubject: " + actAsSubjectLookup
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n, wsStemLookups: " + GrouperUtil.toStringForLog(wsStemLookups, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      //keep these ordered for testing
      Set<Stem> stems = new TreeSet<Stem>();
      
      if (wsStemQueryFilter != null) {

        wsStemQueryFilter.assignGrouperSession(session);
        
        //make sure filter is ok to use
        wsStemQueryFilter.validate();
    
        //run the query
        QueryFilter queryFilter = wsStemQueryFilter.retrieveQueryFilter();
        GrouperQuery grouperQuery = GrouperQuery.createQuery(session, queryFilter);
        stems.addAll(grouperQuery.getStems());
        
      }

      //we could do this in fewer queries if we like...
      for (WsStemLookup wsStemLookup : GrouperUtil.nonNull(wsStemLookups, WsStemLookup.class)) {
        wsStemLookup.retrieveStemIfNeeded(session, false);
        Stem stem = wsStemLookup.retrieveStem();
        stems.add(stem);
      }

      wsFindStemsResults.assignStemResult(stems);
  
      wsFindStemsResults.assignResultCode(WsFindStemsResultsCode.SUCCESS);
      wsFindStemsResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindStemsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsFindStemsResults;
  }

  /**
   * find a stem or stems
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemQueryFilterType findStemType is the WsFindStemType enum for which 
   * type of find is happening:  e.g.
   * FIND_BY_STEM_UUID, FIND_BY_STEM_NAME_EXACT, FIND_BY_PARENT_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, 
   * AND, OR, MINUS;
   * @param stemName search by stem name (must match exactly), cannot use other
   *            params with this
   * @param parentStemName
   *            will return stems in this stem.  can be used with various query types
   * @param parentStemNameScope
   *            if searching by stem, ONE_LEVEL is for one level,
   *            ALL_IN_SUBTREE will return all in sub tree. Required if
   *            searching by stem
   * @param stemUuid
   *            search by stem uuid (must match exactly), cannot use other
   *            params with this
   * @param stemAttributeName if searching by attribute, this is name,
   * or null for all attributes
   * @param stemAttributeValue if searching by attribute, this is the value
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId
   *            optional to narrow the act as subject search to a particular source 
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the stems, or no stems if none found
   */
  public static WsFindStemsResults findStemsLite(final GrouperVersion clientVersion,
      WsStemQueryFilterType stemQueryFilterType, String stemName, String parentStemName,
      StemScope parentStemNameScope, String stemUuid, String stemAttributeName,
      String stemAttributeValue, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, 
        paramName1, paramValue1);
  
    WsStemQueryFilter wsStemQueryFilter = new WsStemQueryFilter();
    wsStemQueryFilter.setStemQueryFilterType(stemQueryFilterType == null ? null : stemQueryFilterType.name());
    wsStemQueryFilter.setParentStemName(parentStemName);
    wsStemQueryFilter.setParentStemNameScope(parentStemNameScope == null ? null : parentStemNameScope.name());
    wsStemQueryFilter.setStemAttributeName(stemAttributeName);
    wsStemQueryFilter.setStemAttributeValue(stemAttributeValue);
    wsStemQueryFilter.setStemName(stemName);
    wsStemQueryFilter.setStemUuid(stemUuid);
  
    // pass through to the more comprehensive method
    WsFindStemsResults wsFindStemsResults = findStems(clientVersion, wsStemQueryFilter,
        actAsSubjectLookup, params, null);
  
    return wsFindStemsResults;
  }

  /**
   * get groups from members based on filter (accepts batch of members)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectLookup
   * @param subjectLookups
   *            subjects to be examined to see if in group
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @param fieldName is field name (list name) to search or blank for default list
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param wsStemLookup is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param enabled is A for all, T or null for enabled only, F for disabled
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetGroupsResults getGroups(final GrouperVersion clientVersion,
      WsSubjectLookup[] subjectLookups, WsMemberFilter memberFilter, 
      WsSubjectLookup actAsSubjectLookup, boolean includeGroupDetail,
      boolean includeSubjectDetail, 
      String[] subjectAttributeNames, WsParam[] params, String fieldName, String scope, 
      WsStemLookup wsStemLookup, StemScope stemScope, String enabled, 
      Integer pageSize, Integer pageNumber, String sortString, Boolean ascending,
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
  
    final WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
    boolean usePIT = pointInTimeFrom != null || pointInTimeTo != null;

    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetGroupsResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 200) 
          + "\nmemberFilter: " + memberFilter + ", includeGroupDetail: "
          + includeGroupDetail + ", actAsSubject: " + actAsSubjectLookup
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n fieldName1: " + fieldName + "\n, scope: " + scope
          + ", wsStemLookup: " + wsStemLookup + "\n, stemScope: " + stemScope + ", enabled: " + enabled
          + ", pageSize: " + pageSize + ", pageNumber: " + pageNumber + ", sortString: " + sortString
          + ", ascending: " + ascending
          + "\n, pointInTimeFrom: " + pointInTimeFrom + ", pointInTimeTo: " + pointInTimeTo;
  
      subjectAttributeNames = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

      wsGetGroupsResults.setSubjectAttributeNames(subjectAttributeNames);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
          subjectLookups, GrouperWsConfig.WS_GET_GROUPS_SUBJECTS_MAX, 1000000, "subjectLookups");

      int resultIndex = 0;

      Boolean enabledBoolean = null;
      if (StringUtils.equalsIgnoreCase("A", enabled)) {
        enabledBoolean = null;
      } else {
        enabledBoolean = GrouperServiceUtils.booleanValue(enabled, true, "enabled");
      }
      
      wsGetGroupsResults.setResults(new WsGetGroupsResult[subjectLength]);

      //convert the options to a map for easy access, and validate them
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(params);
      if (StringUtils.isBlank(fieldName)) {
        fieldName = paramMap.get("fieldName");
      }
      Field field = null;
      if (!StringUtils.isBlank(fieldName)) {
        field = GrouperServiceUtils.retrieveField(fieldName);
        theSummary += ", field: " + field.getName();
      }
      
      if (usePIT && memberFilter != null && memberFilter.getMembershipType() != null) {
        throw new WsInvalidQueryException("Cannot specify a member filter for point in time queries.");
      }
      
      if (usePIT && includeGroupDetail) {
        throw new WsInvalidQueryException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      if (usePIT && (enabledBoolean == null || !enabledBoolean)) {
        throw new WsInvalidQueryException("Cannot search for disabled memberships for point in time queries.");
      }
      
      if (usePIT && sortString != null && !sortString.equals("name")) {
        throw new WsInvalidQueryException("Can only sort by name for point in time queries.");
      }
        
      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
        WsGetGroupsResult wsGetGroupsResult = new WsGetGroupsResult();
        wsGetGroupsResults.getResults()[resultIndex++] = wsGetGroupsResult;
        
        try {
          //init in case error
          wsGetGroupsResult.setWsSubject(new WsSubject(wsSubjectLookup));
          Subject subject = wsSubjectLookup.retrieveSubject("subjectLookup");
          wsGetGroupsResult.setWsSubject(new WsSubject(subject, subjectAttributeNames, wsSubjectLookup));
          Member member = MemberFinder.internal_findBySubject(subject, null, false);
          Set<Group> groups = null;
          Set<PITGroup> pitGroups = null;
          if (member == null) {
            groups = new HashSet<Group>();
            pitGroups = new HashSet<PITGroup>();
          } else {
            if (field == null) {
              field = Group.getDefaultList();
            }
            
            Stem stem = null;
            if (wsStemLookup != null) {
              wsStemLookup.retrieveStemIfNeeded(session, true);
              stem = wsStemLookup.retrieveStem();
            }

            //if supposed to have stem but cant find, then dont get any groups
            QueryOptions queryOptions = null;
            Scope stemDotScope = null;
            if (wsStemLookup == null || stem != null ) {
              
              if (pageSize != null || pageNumber != null || !StringUtils.isBlank(sortString) || ascending != null) {
                queryOptions = new QueryOptions();
                if ((pageSize == null) != (pageNumber == null)) {
                  throw new RuntimeException("If you pass page size, you must pass page number and vice versa");
                }
                if (pageSize != null) {
                  queryOptions.paging(new QueryPaging(pageSize, pageNumber, false));
                }
                if (!StringUtils.isBlank(sortString)) {
                  if (ascending == null) {
                    ascending = true;
                  }
                  queryOptions.sort(new QuerySort(sortString, ascending));
                }
              }
              
              if (stemScope != null) {
                stemDotScope = stemScope.convertToScope();
              }
              
            }
            
            if (!usePIT) {
              groups = memberFilter.getGroups(member, field, scope, stem, stemDotScope, queryOptions, enabledBoolean);
            } else {
              pitGroups = PITMember.getGroups(member.getUuid(), field.getUuid(), scope, stem, stemDotScope, pointInTimeFrom, pointInTimeTo, queryOptions);
            }
          }
          
          if (!usePIT) {
            wsGetGroupsResult.assignGroupResult(groups, includeGroupDetail);
          } else {
            wsGetGroupsResult.assignGroupResult(pitGroups);
          }
        } catch (Exception e) {
          wsGetGroupsResult.assignResultCodeException(null, null,wsSubjectLookup,  e);
        }
        
      }
  
      wsGetGroupsResults.tallyResults(theSummary);
      
    } catch (Exception e) {
      wsGetGroupsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetGroupsResults;
  
  }

  /**
   * get groups for a subject based on filter
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectId
   *            to add (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to add (mutually exclusive with subjectId)
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param subjectIdentifier
   *            to query (mutually exclusive with subjectId)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param fieldName is field name (list name) to search or blank for default list
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param stemName is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemUuid is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param enabled is A for all, T or null for enabled only, F for disabled
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @return the result of one member add
   */
  public static WsGetGroupsLiteResult getGroupsLite(final GrouperVersion clientVersion, String subjectId,
      String subjectSourceId, String subjectIdentifier, WsMemberFilter memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, 
      boolean includeSubjectDetail, 
      String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String fieldName, String scope, 
      String stemName, String stemUuid, StemScope stemScope, String enabled, 
      Integer pageSize, Integer pageNumber, String sortString, Boolean ascending,
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
  
    // setup the subject lookup
    WsSubjectLookup subjectLookup = new WsSubjectLookup(subjectId, subjectSourceId,
        subjectIdentifier);
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[]{subjectLookup};
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    
    WsGetGroupsResults wsGetGroupsResults = getGroups(clientVersion, subjectLookups,
        memberFilter, actAsSubjectLookup, includeGroupDetail, includeSubjectDetail,
        subjectAttributeArray, params, fieldName, scope, wsStemLookup, stemScope, enabled, pageSize, pageNumber, sortString, ascending,
        pointInTimeFrom, pointInTimeTo);
  
    return new WsGetGroupsLiteResult(wsGetGroupsResults);
  
  }

  /**
   * get members from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups are groups to check members for
   * @param memberFilter
   *            must be one of All, Effective, Immediate, Composite
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param sourceIds are source ids of members to retrieve
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * @param ascending T or null for ascending, F for descending.  
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetMembersResults getMembers(
      final GrouperVersion clientVersion,
      WsGroupLookup[] wsGroupLookups, WsMemberFilter memberFilter,
      WsSubjectLookup actAsSubjectLookup, final Field fieldName,
      boolean includeGroupDetail, 
      boolean includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params, String[] sourceIds, Timestamp pointInTimeFrom, Timestamp pointInTimeTo,
      Integer pageSize, Integer pageNumber,
      String sortString, Boolean ascending) {
  
    WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
  
    GrouperSession session = null;
    String theSummary = null;
    ArrayList<WsGetMembersResult> results = new ArrayList<WsGetMembersResult>();
    
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetMembersResults.getResponseMetadata().warnings());

      boolean hasSources = GrouperUtil.length(sourceIds) > 0;
      boolean usePIT = pointInTimeFrom != null || pointInTimeTo != null;

      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups,200) + "\n, memberFilter: " 
          + memberFilter
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + 
          "\n, sourceIds: " + GrouperUtil.toStringForLog(sourceIds) + 
          "\n, pointInTimeFrom: " + pointInTimeFrom + ", pointInTimeTo: " + pointInTimeTo
          + ", pageSize: " + pageSize + ", pageNumber: " + pageNumber 
          + ", sortString: " + sortString + ", ascending: " + ascending ;

      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      String fieldId = fieldName == null ? Group.getDefaultList().getUuid() : fieldName.getUuid();

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);

      Set<Source> sources = null;
      if (hasSources) {
        sources = GrouperUtil.convertSources(sourceIds);
      }
      
      if (usePIT && memberFilter != null && memberFilter.getMembershipType() != null) {
        throw new WsInvalidQueryException("Cannot specify a member filter for point in time queries.");
      }
      
      if (usePIT && includeGroupDetail) {
        throw new WsInvalidQueryException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
      wsGetMembersResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
            
      if ((pageNumber != null) && (pageSize == null) ) {
        
        throw new WsInvalidQueryException(
          "If you pass in pageNumber you need to pass in pageSize");
      
      }
      
      for (WsGroupLookup wsGroupLookup : GrouperUtil.nonNull(wsGroupLookups, WsGroupLookup.class)) {
        
        QueryOptions queryOptions = null;
        if (pageSize != null || pageNumber != null || !StringUtils.isBlank(sortString) || ascending != null) {
          queryOptions = new QueryOptions();
          boolean hasPaging = false;
          if (pageNumber != null || pageSize != null) {
            //default page number to 1
            if (pageNumber == null) {
              pageNumber = 1;
            }
            queryOptions.paging(pageSize, pageNumber, false);
            hasPaging = true;
          }
          if (hasPaging || ascending != null || !StringUtils.isBlank(sortString)) {
            //default sort string to subjectId
            if (StringUtils.isBlank(sortString)) {
              sortString = "sourceId,subjectId";
            }
            if (ascending == null || ascending) {
              queryOptions.sortAsc(sortString);
            } else {
              queryOptions.sortDesc(sortString);
            }
          }
        }


        
        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        results.add(wsGetMembersResult);
        
        try {
          if (!usePIT) {
            Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
  
            //init in case error
            wsGetMembersResult.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
            
            if (group == null) {
  
              wsGetMembersResult
                  .assignResultCode(GroupFindResult
                      .convertToGetMembersCodeStatic(wsGroupLookup
                          .retrieveGroupFindResult()));
              wsGetMembersResult.getResultMetadata().setResultMessage(
                  "Problem with group: '" + wsGroupLookup + "'.  ");
              //should we short circuit if transactional?
              continue;
            }
            
            // lets get the members, cant be null
            Set<Member> members = memberFilter.getMembers(group, fieldName, sources, queryOptions);
            Member.resolveSubjects(members, true);
            wsGetMembersResult.assignSubjectResult(members, subjectAttributeNamesToRetrieve, includeSubjectDetail);
          } else {            
            Set<PITGroup> pitGroups = wsGroupLookup.retrievePITGroupsIfNeeded("wsGroupLookup", pointInTimeFrom, pointInTimeTo);
            if (pitGroups == null || pitGroups.size() == 0) {
              wsGetMembersResult.setWsGroup(new WsGroup(null, wsGroupLookup, includeGroupDetail));

              wsGetMembersResult
                  .assignResultCode(GroupFindResult
                      .convertToGetMembersCodeStatic(wsGroupLookup
                          .retrieveGroupFindResult()));
              wsGetMembersResult.getResultMetadata().setResultMessage(
                  "Problem with group: '" + wsGroupLookup + "'.  ");
              //should we short circuit if transactional?
              continue;
            }
            
            Iterator<PITGroup> pitGroupsIter = pitGroups.iterator();
            while (pitGroupsIter.hasNext()) {
              PITGroup pitGroup = pitGroupsIter.next();
              wsGetMembersResult.setWsGroup(new WsGroup(pitGroup));

              // lets get the members, cant be null
              Set<Member> members = pitGroup.getMembers(fieldId, pointInTimeFrom, pointInTimeTo, sources, queryOptions);
          
              wsGetMembersResult.assignSubjectResult(members, subjectAttributeNamesToRetrieve, includeSubjectDetail);
              
              if (pitGroupsIter.hasNext()) {
                wsGetMembersResult = new WsGetMembersResult();
                results.add(wsGetMembersResult);
              }
            }
          }
        } catch (Exception e) {
          wsGetMembersResult.assignResultCodeException(null, null, wsGroupLookup, e);
        }
        
      }
      
      wsGetMembersResults.setResults(results.toArray(new WsGetMembersResult[0]));
      wsGetMembersResults.tallyResults(theSummary);
      
    } catch (Exception e) {
      wsGetMembersResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetMembersResults;
  }

  /**
   * get memberships from groups and or subjects based on a filter (all, immediate only,
   * effective only, composite, nonimmediate).
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups are groups to look in
   * @param wsSubjectLookups are subjects to look in
   * @param wsMemberFilter
   *            must be one of All, Effective, Immediate, Composite, NonImmediate
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param fieldName is if the memberships should be retrieved from a certain field membership
   * of the group (certain list)
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param sourceIds are sources to look in for memberships, or null if all
   * @param scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @param wsStemLookup is the stem to look in for memberships
   * @param stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @param membershipIds are the ids to search for if they are known
   * @param serviceRole to filter attributes that a user has a certain role
   * @param serviceLookup if filtering by users in a service, then this is the service to look in
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetMembershipsResults getMemberships(final GrouperVersion clientVersion,
      WsGroupLookup[] wsGroupLookups, WsSubjectLookup[] wsSubjectLookups, WsMemberFilter wsMemberFilter,
      WsSubjectLookup actAsSubjectLookup, Field fieldName, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params, 
      String[] sourceIds, String scope, 
      WsStemLookup wsStemLookup, StemScope stemScope, String enabled, String[] membershipIds,
      ServiceRole serviceRole, WsAttributeDefNameLookup serviceLookup
      ) {  

    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetMembershipsResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups, 200) + ", wsMemberFilter: " + wsMemberFilter
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsSubjectLookups: "
          + GrouperUtil.toStringForLog(wsSubjectLookups, 200) + "\n, sourceIds: " + GrouperUtil.toStringForLog(sourceIds, 100)
          + "\n, scope: " + scope + ", wsStemLookup: " + wsStemLookup + ", stemScope: " + stemScope + ", enabled: " + enabled
          + "\n, serviceRole: " + serviceRole + ", serviceLookup: " + serviceLookup
          + "\n, membershipIds: " + GrouperUtil.toStringForLog(membershipIds, 200);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //TODO lookup all groups and subjects before and after
      
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);

      MembershipFinder membershipFinder = new MembershipFinder();
      
      
      MembershipType membershipType = null;
      if (wsMemberFilter != null) {
        membershipType = wsMemberFilter.getMembershipType();
        membershipFinder.assignMembershipType(membershipType);
      }
      
      //get all the groups
      //we could probably batch these to get better performance.  And we dont even have to lookup uuids
      boolean groupsOk = GrouperUtil.length(wsGroupLookups) == 0;

      if (GrouperUtil.length(wsGroupLookups) > 0) {
        groupsOk = false;
        int groupCount = 0;
        for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
          
          if (wsGroupLookup == null) {
            continue;
          }
          groupCount++;
          wsGroupLookup.retrieveGroupIfNeeded(session, "group");
          Group group = wsGroupLookup.retrieveGroup();
          if (group == null) {
            throw new GroupNotFoundException("Could not find group: " + wsGroupLookup);
          }
          membershipFinder.addGroupId(group.getUuid());
          groupsOk = groupsOk || !StringUtils.isBlank(group.getUuid());
        }
        groupsOk = groupCount == 0 || groupsOk;
      }

      //get all the members
      boolean membersOk = GrouperUtil.length(wsSubjectLookups) == 0;
      if (GrouperUtil.length(wsSubjectLookups) > 0) {
        membersOk = false;
        int subjectCount = 0;
        for (WsSubjectLookup wsSubjectLookup : wsSubjectLookups) {
          if (wsSubjectLookup == null) {
            continue;
          }
          subjectCount++;
          Member member = wsSubjectLookup.retrieveMember();
          if (member == null) {
            //cant find, thats ok
            if (MemberFindResult.MEMBER_NOT_FOUND.equals(wsSubjectLookup.retrieveMemberFindResult())) {
              continue;
            }
            //problem
            throw new RuntimeException("Problem with subject: " + wsSubjectLookup + ", " + wsSubjectLookup.retrieveMemberFindResult());
          }
          membershipFinder.addMemberId(member.getUuid());
          membersOk = membersOk || !StringUtils.isBlank(member.getUuid());
        }
        membersOk = subjectCount == 0 || membersOk;
      }

      if (StringUtils.isBlank(enabled)) {
        membershipFinder.assignEnabled(true);
      } else {
        //if A leave at null
        if (!StringUtils.equalsIgnoreCase("A", enabled)) {
          membershipFinder.assignEnabled(GrouperUtil.booleanValue(enabled));
        }
      }
      
      Stem stem = null;
      if (wsStemLookup != null) {
        wsStemLookup.retrieveStemIfNeeded(session, true);
        stem = wsStemLookup.retrieveStem();
        membershipFinder.assignStem(stem);
      }
      
      //if filtering by stem, and stem not found, then dont find any memberships
      if ((wsStemLookup == null || stem != null) && membersOk && groupsOk) {
        Set<Source> sources = GrouperUtil.convertSources(sourceIds);
        
        for (Source source : GrouperUtil.nonNull(sources)) {
          membershipFinder.addSource(source);
        }
        
        Set<String> membershipIdSet = null;
        if (GrouperUtil.length(membershipIds) > 0) {
          for (String membershipId : membershipIds) { 
            membershipFinder.addMembershipId(membershipId);
          }
        }
        
        membershipFinder.assignField(fieldName);
        
        membershipFinder.assignStemScope(stemScope == null ? null : stemScope.convertToScope());
        
        membershipFinder.assignServiceRole(serviceRole);
        
        if (serviceLookup != null && serviceLookup.hasData()) {
          serviceLookup.retrieveAttributeDefNameIfNeeded(session, "serviceLookup");
          membershipFinder.assignServiceId(serviceLookup.retrieveAttributeDefName().getId());
        }
        
        
        // lets get the members, cant be null
        Set<Object[]> membershipObjects = membershipFinder.findMembershipsGroupsMembers();
        
        
        Membership.resolveSubjects(membershipObjects);
        
        //calculate and return the results
        wsGetMembershipsResults.assignResult(membershipObjects, includeGroupDetail, includeSubjectDetail, subjectAttributeNames);
      }
      wsGetMembershipsResults.assignResultCode(WsGetMembershipsResultsCode.SUCCESS);
      
      wsGetMembershipsResults.getResultMetadata().setResultMessage(
          "Found " + GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()) 
          + " results involving " + GrouperUtil.length(wsGetMembershipsResults.getWsGroups())
          + " groups and " + GrouperUtil.length(wsGetMembershipsResults.getWsSubjects()) + " subjects");

        
    } catch (Exception e) {
      wsGetMembershipsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetMembershipsResults;
  
  }

  /**
   * get memberships from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId to search for memberships in or null to not restrict
   * @param sourceId of subject to search for memberships, or null to not restrict
   * @param subjectIdentifier of subject to search for memberships, or null to not restrict
   * @param wsMemberFilter
   *            must be one of All, Effective, Immediate, Composite
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param sourceIds are comma separated sourceIds
   * @param scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @param stemName to limit the search to a stem (in or under)
   * @param stemUuid to limit the search to a stem (in or under)
   * @param stemScope to specify if we are searching in or under the stem
   * @param enabled A for all, null or T for enabled only, F for disabled only
   * @param membershipIds comma separated list of membershipIds to retrieve
   * @param serviceRole to filter attributes that a user has a certain role
   * @param serviceId if filtering by users in a service, then this is the service to look in, mutually exclusive with serviceName
   * @param serviceName if filtering by users in a service, then this is the service to look in, mutually exclusive with serviceId
   * @return the memberships, or none if none found
   */
  public static WsGetMembershipsResults getMembershipsLite(final GrouperVersion clientVersion,
      String groupName, String groupUuid, String subjectId, String sourceId, String subjectIdentifier, 
      WsMemberFilter wsMemberFilter,
      boolean includeSubjectDetail, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, Field fieldName, String subjectAttributeNames,
      boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds, String scope, String stemName, 
      String stemUuid, StemScope stemScope, String enabled, String membershipIds, ServiceRole serviceRole,
      String serviceId, String serviceName) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = null;
    
    if (StringUtils.isNotBlank(groupName) || StringUtils.isNotBlank(groupUuid)) {
      wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    }
  
    WsSubjectLookup wsSubjectLookup = WsSubjectLookup.createIfNeeded(subjectId, sourceId, subjectIdentifier);
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsAttributeDefNameLookup serviceLookup = WsAttributeDefNameLookup.createIfNeeded(serviceId, serviceName);
    
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // pass through to the more comprehensive method
    WsGroupLookup[] wsGroupLookups = wsGroupLookup == null ? null : new WsGroupLookup[]{wsGroupLookup};
    WsSubjectLookup[] wsSubjectLookups = wsSubjectLookup == null ? null : new WsSubjectLookup[]{wsSubjectLookup};
    
    String[] sourceIdArray = GrouperUtil.splitTrim(sourceIds, ",");

    String[] membershipIdArray = GrouperUtil.splitTrim(membershipIds, ",");

    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    
    WsGetMembershipsResults wsGetMembershipsResults = getMemberships(clientVersion,
        wsGroupLookups, wsSubjectLookups, wsMemberFilter, actAsSubjectLookup, fieldName,
        includeSubjectDetail, subjectAttributeArray, includeGroupDetail,
        params, sourceIdArray, scope, wsStemLookup, stemScope, enabled, membershipIdArray,
        serviceRole, serviceLookup);
  
    return wsGetMembershipsResults;
  }

  /**
   * get members from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param memberFilter
   *            must be one of All, Effective, Immediate, Composite
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is the source to use to lookup the subject (if applicable) 
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param sourceIds comma separated of sources to get members from
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @param ascending T or null for ascending, F for descending.  
   * @return the members, or no members if none found
   */
  public static WsGetMembersLiteResult getMembersLite(
      final GrouperVersion clientVersion,
      String groupName, String groupUuid, WsMemberFilter memberFilter, 
      String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, 
      final Field fieldName,
      boolean includeGroupDetail, 
      boolean includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds,
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo,
      Integer pageSize, Integer pageNumber,
      String sortString, Boolean ascending) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    String[] sourceIdArray = GrouperUtil.splitTrim(sourceIds, ",");
    
    boolean usePIT = pointInTimeFrom != null || pointInTimeTo != null;

    // pass through to the more comprehensive method
    WsGetMembersResults wsGetMembersResults = getMembers(clientVersion, wsGroupLookups,
        memberFilter, actAsSubjectLookup, fieldName, 
        includeGroupDetail, includeSubjectDetail,
        subjectAttributeArray, params, sourceIdArray, pointInTimeFrom, pointInTimeTo,
        pageSize, pageNumber,
        sortString, ascending);
  
    if (usePIT && wsGetMembersResults.getResults() != null && wsGetMembersResults.getResults().length > 1) {
      WsGetMembersResult[] lastResult = { wsGetMembersResults.getResults()[wsGetMembersResults.getResults().length - 1] };
      wsGetMembersResults.setResults(lastResult);
    }
    
    return new WsGetMembersLiteResult(wsGetMembersResults);
  }

  /**
   * delete a group or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups
   *            groups to delete
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGroupDeleteResults groupDelete(final GrouperVersion clientVersion,
      final WsGroupLookup[] wsGroupLookups, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final boolean includeGroupDetail, final WsParam[] params) {
  
    final WsGroupDeleteResults wsGroupDeleteResults = new WsGroupDeleteResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGroupDeleteResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", includeGroupDetail: "
          + includeGroupDetail + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int groupsSize = GrouperServiceUtils.arrayLengthAtLeastOne(wsGroupLookups,
                  GrouperWsConfig.WS_GROUP_DELETE_MAX, 1000000, "groupLookups");
  
              wsGroupDeleteResults.setResults(new WsGroupDeleteResult[groupsSize]);
  
              int resultIndex = 0;
  
              //loop through all groups and do the delete
              for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
  
                WsGroupDeleteResult wsGroupDeleteResult = new WsGroupDeleteResult(
                    wsGroupLookup);
                wsGroupDeleteResults.getResults()[resultIndex++] = wsGroupDeleteResult;
  
                wsGroupLookup.retrieveGroupIfNeeded(SESSION);
                Group group = wsGroupLookup.retrieveGroup();
  
                if (group == null) {
  
                  wsGroupDeleteResult
                      .assignResultCode(GroupFindResult
                          .convertToGroupDeleteCodeStatic(wsGroupLookup
                              .retrieveGroupFindResult()));
                  wsGroupDeleteResult.getResultMetadata().setResultMessage(
                      "Cant find group: '" + wsGroupLookup + "'.  ");
                  //should we short circuit if transactional?
                  continue;
                }
  
                //make each group failsafe
                try {
                  wsGroupDeleteResult.assignGroup(group, wsGroupLookup, includeGroupDetail);
  
                  //if there was already a problem, then dont continue
                  if (!GrouperUtil.booleanValue(wsGroupDeleteResult.getResultMetadata()
                      .getSuccess(), true)) {
                    continue;
                  }
  
                  group.delete();
  
                  wsGroupDeleteResult.assignResultCode(WsGroupDeleteResultCode.SUCCESS);
                  wsGroupDeleteResult.getResultMetadata().setResultMessage(
                      "Group '" + group.getName() + "' was deleted.");
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsGroupDeleteResult
                      .assignResultCode(WsGroupDeleteResultCode.INSUFFICIENT_PRIVILEGES);
                } catch (Exception e) {
                  wsGroupDeleteResult.assignResultCodeException(e, wsGroupLookup);
                }
              }
  
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsGroupDeleteResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsGroupDeleteResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsGroupDeleteResults;
  
  }

  /**
   * delete a group or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to delete the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to delete the group (mutually exclusive with groupName)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsGroupDeleteLiteResult groupDeleteLite(final GrouperVersion clientVersion,
      String groupName, String groupUuid, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier,
      final boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGroupDeleteResults wsGroupDeleteResults = groupDelete(clientVersion,
        wsGroupLookups, actAsSubjectLookup, null, includeGroupDetail, params);
  
    return new WsGroupDeleteLiteResult(wsGroupDeleteResults);
  }

  /**
   * save a group or many (insert or update).  Note, you cannot rename an existing group.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param wsGroupToSaves
   *            groups to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGroupSaveResults groupSave(final GrouperVersion clientVersion,
      final WsGroupToSave[] wsGroupToSaves, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final boolean includeGroupDetail,  final WsParam[] params) {

    final WsGroupSaveResults wsGroupSaveResults = new WsGroupSaveResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGroupSaveResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupToSaves: "
          + GrouperUtil.toStringForLog(wsGroupToSaves, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int wsGroupsLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsGroupToSaves, GrouperWsConfig.WS_GROUP_SAVE_MAX, 1000000, "groupsToSave");
  
              wsGroupSaveResults.setResults(new WsGroupSaveResult[wsGroupsLength]);
  
              int resultIndex = 0;
  
              //loop through all stems and do the save
              for (WsGroupToSave wsGroupToSave : wsGroupToSaves) {
                final WsGroupSaveResult wsGroupSaveResult = new WsGroupSaveResult(wsGroupToSave.getWsGroupLookup());
                wsGroupSaveResults.getResults()[resultIndex++] = wsGroupSaveResult;
                final WsGroupToSave WS_GROUP_TO_SAVE = wsGroupToSave;
                try {
                  //this should be autonomous, so that within one group, it is transactional
                  HibernateSession.callbackHibernateSession(
                      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                    public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {
                      //make sure everything is in order
                      WS_GROUP_TO_SAVE.validate();
                      Group group = WS_GROUP_TO_SAVE.save(SESSION);
                      SaveResultType saveResultType = WS_GROUP_TO_SAVE.saveResultType();
                      wsGroupSaveResult.setWsGroup(new WsGroup(group, 
                          WS_GROUP_TO_SAVE.getWsGroupLookup(), includeGroupDetail));
                      
                      if (saveResultType == SaveResultType.INSERT) {
                        wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.SUCCESS_INSERTED, clientVersion);
                      } else if (saveResultType == SaveResultType.UPDATE) {
                        wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.SUCCESS_UPDATED, clientVersion);
                      } else if (saveResultType == SaveResultType.NO_CHANGE) {
                        wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.SUCCESS_NO_CHANGES_NEEDED, clientVersion);
                      } else {
                        throw new RuntimeException("Invalid saveType: " + saveResultType);
                      }

                      return null;
                    }

                  });
  
                } catch (Exception e) {
                  wsGroupSaveResult.assignResultCodeException(e, wsGroupToSave, clientVersion);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsGroupSaveResults.tallyResults(TX_TYPE, THE_SUMMARY, clientVersion)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsGroupSaveResults.assignResultCodeException(null, theSummary, e, clientVersion);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsGroupSaveResults;
  }

  /**
   * see if a group has members based on filter (accepts batch of members)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   *            for the group to see if the members are in there
   * @param subjectLookups
   *            subjects to be examined to see if in group
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param fieldName
   *            is if the Group.hasMember() method with field is to be called
   *            (e.g. admins, optouts, optins, etc from Field table in DB)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsHasMemberResults hasMember(final GrouperVersion clientVersion,
      WsGroupLookup wsGroupLookup, WsSubjectLookup[] subjectLookups,
      WsMemberFilter memberFilter,
      WsSubjectLookup actAsSubjectLookup, Field fieldName,
      final boolean includeGroupDetail, boolean includeSubjectDetail, 
      String[] subjectAttributeNames, WsParam[] params,
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
  
    WsHasMemberResults wsHasMemberResults = new WsHasMemberResults();
    boolean usePIT = pointInTimeFrom != null || pointInTimeTo != null;

    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsHasMemberResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 200)
          + "\n memberFilter: "
          + memberFilter + ", actAsSubject: " + actAsSubjectLookup + ", fieldName: "
          + fieldName + ", includeGroupDetail: " + includeGroupDetail 
          + ", includeSubjectDetail: " + includeSubjectDetail
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n," +
          		"params: " + GrouperUtil.toStringForLog(params, 100) + "\n,"
          + "pointInTimeFrom: " + pointInTimeFrom + ", pointInTimeTo: " + pointInTimeTo;

  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      String fieldId = fieldName == null ? Group.getDefaultList().getUuid() : fieldName.getUuid();

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
      
      if (usePIT && memberFilter != null && memberFilter.getMembershipType() != null) {
        throw new WsInvalidQueryException("Cannot specify a member filter for point in time queries.");
      }
      
      if (usePIT && includeGroupDetail) {
        throw new WsInvalidQueryException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      Group group = null;
      PITGroup pitGroup = null;
  
      if (!usePIT) {
        group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
    
        //assign the group to the result to be descriptive
        wsHasMemberResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
      } else {
        Set<PITGroup> pitGroups = wsGroupLookup.retrievePITGroupsIfNeeded("wsGroupLookup", pointInTimeFrom, pointInTimeTo);
        pitGroup = pitGroups.toArray(new PITGroup[0])[pitGroups.size() - 1];
        
        //assign the group to the result to be descriptive
        wsHasMemberResults.setWsGroup(new WsGroup(pitGroup));
      }
  
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsHasMemberResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(subjectLookups,
          GrouperWsConfig.WS_HAS_MEMBER_SUBJECTS_MAX, 1000000, "subjectLookups");
  
      wsHasMemberResults.setResults(new WsHasMemberResult[subjectLength]);
  
      int resultIndex = 0;
  
      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
        WsHasMemberResult wsHasMemberResult = null;
        try {
          wsHasMemberResult = new WsHasMemberResult(wsSubjectLookup,
              subjectAttributeNamesToRetrieve);
          wsHasMemberResults.getResults()[resultIndex++] = wsHasMemberResult;
  
          //see if subject found
          if (!GrouperUtil.booleanValue(wsHasMemberResult.getResultMetadata()
              .getSuccess(), true)) {
            continue;
          }
  
          if (StringUtils.equals(wsHasMemberResult.getResultMetadata().getResultCode(), 
              WsHasMemberResultCode.SUBJECT_NOT_FOUND.name())) {

            wsHasMemberResult.assignResultCode(WsHasMemberResultCode.IS_NOT_MEMBER);
            wsHasMemberResult.getResultMetadata().setResultCode2(WsHasMemberResultCode.SUBJECT_NOT_FOUND.name());

          } else {

            boolean hasMember;

            if (!usePIT) {
              hasMember = memberFilter.hasMember(group, wsSubjectLookup.retrieveSubject(), fieldName);
            } else {
              hasMember = pitGroup.hasMember(wsSubjectLookup.retrieveSubject(), fieldId, pointInTimeFrom, pointInTimeTo, null);
            }
            
            wsHasMemberResult.assignResultCode(hasMember ? WsHasMemberResultCode.IS_MEMBER
                : WsHasMemberResultCode.IS_NOT_MEMBER);

          }
          
        } catch (Exception e) {
          wsHasMemberResult.assignResultCodeException(e, wsSubjectLookup);
        }
      }
  
      //see if all success
      wsHasMemberResults.tallyResults(theSummary);
  
    } catch (Exception e) {
      wsHasMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsHasMemberResults;
  
  }

  /**
   * see if a group has a member (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to query (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName
   *            is if the Group.hasMember() method with field is to be called
   *            (e.g. admins, optouts, optins, etc from Field table in DB)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param subjectIdentifier
   *            to query (mutually exclusive with subjectId)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @return the result of one member query
   */
  public static WsHasMemberLiteResult hasMemberLite(final GrouperVersion clientVersion, String groupName,
      String groupUuid, String subjectId, String subjectSourceId, String subjectIdentifier,
      WsMemberFilter memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      Field fieldName, final boolean includeGroupDetail, boolean includeSubjectDetail, String subjectAttributeNames, 
      String paramName0,
      String paramValue0, String paramName1, String paramValue1,
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsHasMemberResults wsHasMemberResults = hasMember(clientVersion, wsGroupLookup,
        subjectLookups, memberFilter,
        actAsSubjectLookup, fieldName, includeGroupDetail, 
        includeSubjectDetail, subjectAttributeArray, params, pointInTimeFrom, pointInTimeTo);
  
    return new WsHasMemberLiteResult(wsHasMemberResults);
  }

  /**
   * see if a group has a member (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param oldSubjectId subject id of old member object.  This is the preferred way to look up the 
   * old subject, but subjectIdentifier could also be used
   * @param oldSubjectSourceId source id of old member object (optional)
   * @param oldSubjectIdentifier subject identifier of old member object.  It is preferred to lookup the 
   * old subject by id, but if identifier is used, that is ok instead (as long as subject is resolvable).
   * @param newSubjectId preferred way to identify the new subject id
   * @param newSubjectSourceId preferres way to identify the new subject id
   * @param newSubjectIdentifier subjectId is the preferred way to lookup the new subject, but identifier is
   * ok to use instead
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param deleteOldMember T or F as to whether the old member should be deleted (if new member does exist).
   * This defaults to T if it is blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member query
   */
  public static WsMemberChangeSubjectLiteResult memberChangeSubjectLite(final GrouperVersion clientVersion, 
      String oldSubjectId, String oldSubjectSourceId, String oldSubjectIdentifier,
      String newSubjectId, String newSubjectSourceId, String newSubjectIdentifier,      
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      boolean deleteOldMember, 
      boolean includeSubjectDetail, String subjectAttributeNames, 
      String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsMemberChangeSubject wsMemberChangeSubject = new WsMemberChangeSubject();
    
    WsSubjectLookup oldSubjectLookup = new WsSubjectLookup(oldSubjectId, oldSubjectSourceId, oldSubjectIdentifier);
    WsSubjectLookup newSubjectLookup = new WsSubjectLookup(newSubjectId, newSubjectSourceId, newSubjectIdentifier);
    
    wsMemberChangeSubject.assignDeleteOldMemberBoolean(deleteOldMember);
    wsMemberChangeSubject.setOldSubjectLookup(oldSubjectLookup);
    wsMemberChangeSubject.setNewSubjectLookup(newSubjectLookup);
    
    WsMemberChangeSubject[] wsMemberChangeSubjects = {wsMemberChangeSubject};
    
    // setup the subject lookup
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsMemberChangeSubjectResults wsMemberChangeSubjectResults = memberChangeSubject(clientVersion, 
        wsMemberChangeSubjects,
        actAsSubjectLookup, null, includeSubjectDetail, subjectAttributeArray, params);
  
    return new WsMemberChangeSubjectLiteResult(wsMemberChangeSubjectResults);
  }

  /**
   * change the subject in a member or some members
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsMemberChangeSubjects objects that describe one member renaming
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsMemberChangeSubjectResults memberChangeSubject(final GrouperVersion clientVersion,
      final WsMemberChangeSubject[] wsMemberChangeSubjects,
      final WsSubjectLookup actAsSubjectLookup, GrouperTransactionType txType, 
      final boolean includeSubjectDetail, 
      final String[] subjectAttributeNames, final WsParam[] params) {
    final WsMemberChangeSubjectResults wsMemberChangeSubjectResults = new WsMemberChangeSubjectResults();
    
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsMemberChangeSubjectResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsMemberChangeSubject: "
          + GrouperUtil.toStringForLog(wsMemberChangeSubjects, 500) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
      
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
      wsMemberChangeSubjectResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int membersToChangeLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsMemberChangeSubjects, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000, "subjectLookups");
  
              wsMemberChangeSubjectResults.setResults(new WsMemberChangeSubjectResult[membersToChangeLength]);
  
              int resultIndex = 0;
  
              //loop through all subjects and do the delete
              for (WsMemberChangeSubject wsMemberChangeSubject : wsMemberChangeSubjects) {
                WsMemberChangeSubjectResult wsMemberChangeSubjectResult = new WsMemberChangeSubjectResult();
                wsMemberChangeSubjectResults.getResults()[resultIndex++] = wsMemberChangeSubjectResult;
                try {
  
                  Member oldMember = wsMemberChangeSubject.getOldSubjectLookup().retrieveMember();
                  wsMemberChangeSubjectResult.processMemberOld(wsMemberChangeSubject.getOldSubjectLookup(), 
                      subjectAttributeNamesToRetrieve, includeSubjectDetail);
                  if (oldMember == null) {
                    continue;
                  }
                  Subject newSubject = wsMemberChangeSubject.getNewSubjectLookup().retrieveSubject();
                  
                  wsMemberChangeSubjectResult.processSubjectNew(wsMemberChangeSubject.getNewSubjectLookup(), 
                      subjectAttributeNamesToRetrieve);
  
                  //make sure we have the right data, if not, then keep going
                  if (newSubject == null) {
                    continue;
                  }

                  boolean deleteOldMember = wsMemberChangeSubject.retrieveDeleteOldMemberBoolean();
                  
                  oldMember.changeSubject(newSubject, deleteOldMember);
                  
                  //assign one of 4 success codes
                  wsMemberChangeSubjectResult.assignResultCode(WsMemberChangeSubjectResultCode.SUCCESS);
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsMemberChangeSubjectResult
                      .assignResultCode(WsMemberChangeSubjectResultCode.INSUFFICIENT_PRIVILEGES);
                } catch (Exception e) {
                  wsMemberChangeSubjectResult.assignResultCodeException(e, wsMemberChangeSubject);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsMemberChangeSubjectResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsMemberChangeSubjectResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsMemberChangeSubjectResults;

  }
  
  /**
   * delete a stem or many (if doesnt exist, ignore)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemName name of stem to delete (mutually exclusive with uuid)
   * @param stemUuid uuid of stem to delete (mutually exclusive with name)
   * 
   * @param wsStemLookups stem lookups of stems to delete (specify name or uuid)
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsStemDeleteResults stemDelete(final GrouperVersion clientVersion,
      final WsStemLookup[] wsStemLookups, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsStemDeleteResults wsStemDeleteResults = new WsStemDeleteResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsStemDeleteResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsStemLookups: "
          + GrouperUtil.toStringForLog(wsStemLookups, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int stemsSize = GrouperServiceUtils.arrayLengthAtLeastOne(wsStemLookups,
                  GrouperWsConfig.WS_STEM_DELETE_MAX, 1000000, "stemLookups");
  
              wsStemDeleteResults.setResults(new WsStemDeleteResult[stemsSize]);
  
              int resultIndex = 0;
  
              //loop through all groups and do the delete
              for (WsStemLookup wsStemLookup : wsStemLookups) {
  
                WsStemDeleteResult wsStemDeleteResult = new WsStemDeleteResult(
                    wsStemLookup);
                wsStemDeleteResults.getResults()[resultIndex++] = wsStemDeleteResult;
  
                wsStemLookup.retrieveStemIfNeeded(SESSION, true);
                Stem stem = wsStemLookup.retrieveStem();
  
                if (stem == null) {
                  wsStemDeleteResult.assignResultCode(StemFindResult
                      .convertToDeleteCodeStatic(wsStemLookup.retrieveStemFindResult()));
                  wsStemDeleteResult.getResultMetadata().setResultMessage(
                      "Cant find stem: '" + wsStemLookup + "'.  ");
                  continue;
                }
  
                //make each stem failsafe
                try {
                  wsStemDeleteResult.setWsStem(new WsStem(stem));
                  stem.delete();
  
                  wsStemDeleteResult.assignResultCode(WsStemDeleteResultCode.SUCCESS);
                  wsStemDeleteResult.getResultMetadata().setResultMessage(
                      "Stem '" + stem.getName() + "' was deleted.");
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsStemDeleteResult
                      .assignResultCode(WsStemDeleteResultCode.INSUFFICIENT_PRIVILEGES);
                  wsStemDeleteResult.getResultMetadata().setResultMessage(
                      "Error: insufficient privileges to delete stem '" + stem.getName()
                          + "'");
                } catch (Exception e) {
                  wsStemDeleteResult.assignResultCodeException(e, wsStemLookup);
                }
  
              }
  
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsStemDeleteResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsStemDeleteResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsStemDeleteResults;
  
  }

  /**
   * delete a stem or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemName
   *            to delete the stem (mutually exclusive with stemUuid)
   * @param stemUuid
   *            to delete the stem (mutually exclusive with stemName)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsStemDeleteLiteResult stemDeleteLite(final GrouperVersion clientVersion,
      String stemName, String stemUuid, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the stem lookup
    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    WsStemLookup[] wsStemLookups = new WsStemLookup[] { wsStemLookup };
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemDeleteResults wsStemDeleteResults = stemDelete(clientVersion, wsStemLookups,
        actAsSubjectLookup, null, params);
  
    return new WsStemDeleteLiteResult(wsStemDeleteResults);
  }

  /**
   * save a stem or many (insert or update).  Note, you cannot rename an existing stem.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param wsStemToSaves
   *            stems to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsStemSaveResults stemSave(final GrouperVersion clientVersion,
      final WsStemToSave[] wsStemToSaves, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsStemSaveResults wsStemSaveResults = new WsStemSaveResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsStemSaveResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsStemToSaves: "
          + GrouperUtil.toStringForLog(wsStemToSaves, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int wsStemsLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsStemToSaves, GrouperWsConfig.WS_STEM_SAVE_MAX, 1000000, "stemsToSave");
  
              wsStemSaveResults.setResults(new WsStemSaveResult[wsStemsLength]);
  
              int resultIndex = 0;
  
              //loop through all stems and do the save
              for (WsStemToSave wsStemToSave : wsStemToSaves) {
                WsStemSaveResult wsStemSaveResult = new WsStemSaveResult(wsStemToSave.getWsStemLookup());
                wsStemSaveResults.getResults()[resultIndex++] = wsStemSaveResult;
  
                try {
                  //make sure everything is in order
                  wsStemToSave.validate();
                  Stem stem = wsStemToSave.save(SESSION);
  
                  wsStemSaveResult.setWsStem(new WsStem(stem));
  
                  SaveResultType saveResultType = wsStemToSave.saveResultType();
                  
                  if (saveResultType == SaveResultType.INSERT) {
                    wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS_INSERTED, clientVersion);
                  } else if (saveResultType == SaveResultType.UPDATE) {
                    wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS_UPDATED, clientVersion);
                  } else if (saveResultType == SaveResultType.NO_CHANGE) {
                    wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS_NO_CHANGES_NEEDED, clientVersion);
                  } else {
                    throw new RuntimeException("Invalid saveResultType: " + saveResultType);
                  }
                    
                } catch (Exception e) {
                  wsStemSaveResult.assignResultCodeException(e, wsStemToSave, clientVersion);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsStemSaveResults.tallyResults(TX_TYPE, THE_SUMMARY, clientVersion)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsStemSaveResults.assignResultCodeException(null, theSummary, e, clientVersion);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsStemSaveResults;
  }

  /**
   * save a stem (insert or update).  Note you cannot rename an existing stem.
   * 
   * @param stemLookupUuid the uuid of the stem to save (mutually exclusive with stemLookupName), null for insert
   * @param stemLookupName the name of the stam to save (mutually exclusive with stemLookupUuid), null for insert
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Stem#saveStem(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param stemName data of stem to save
   * @param stemUuid uuid data of stem to save
   * @param description of the stem
   * @param displayExtension of the stem
   * @param saveMode if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsStemSaveLiteResult stemSaveLite(final GrouperVersion clientVersion,
      String stemLookupUuid, String stemLookupName, String stemUuid, String stemName, 
      String displayExtension, String description, SaveMode saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the stem lookup
    WsStemToSave wsStemToSave = new WsStemToSave();
  
    WsStem wsStem = new WsStem();
    wsStem.setDescription(description);
    wsStem.setDisplayExtension(displayExtension);
    wsStem.setName(stemName);
    wsStem.setUuid(stemUuid);
  
    wsStemToSave.setWsStem(wsStem);
  
    WsStemLookup wsStemLookup = new WsStemLookup(stemLookupName, stemLookupUuid);
    wsStemToSave.setWsStemLookup(wsStemLookup);
  
    wsStemToSave.setSaveMode(saveMode == null ? null : saveMode.name());
  
    WsStemToSave[] wsStemsToSave = new WsStemToSave[] { wsStemToSave };
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemSaveResults wsStemSaveResults = stemSave(clientVersion, wsStemsToSave,
        actAsSubjectLookup, null, params);
  
    return new WsStemSaveLiteResult(wsStemSaveResults);
  }

  /**
   * save a group (insert or update).  Note you cannot rename an existing group.
   * 
   * @param groupLookupUuid the uuid of the group to save (mutually exclusive with groupLookupName), null for insert
   * @param groupLookupName the name of the stam to save (mutually exclusive with groupLookupUuid), null for insert
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param groupName data of group to save
   * @param groupUuid uuid data of group to save
   * @param description of the group
   * @param displayExtension of the group
   * @param saveMode if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param typeOfGroup type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   * @return the result of one member add
   */
  public static WsGroupSaveLiteResult groupSaveLite(final GrouperVersion clientVersion,
      String groupLookupUuid, String groupLookupName, String groupUuid, String groupName, 
      String displayExtension, String description, SaveMode saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, TypeOfGroup typeOfGroup) {
  
    // setup the group lookup
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
  
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription(description);
    wsGroup.setDisplayExtension(displayExtension);
    wsGroup.setName(groupName);
    wsGroup.setUuid(groupUuid);
    wsGroup.setTypeOfGroup(typeOfGroup == null ? null : typeOfGroup.name());
  
    wsGroupToSave.setWsGroup(wsGroup);
  
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupLookupName, groupLookupUuid);
    wsGroupToSave.setWsGroupLookup(wsGroupLookup);
  
    wsGroupToSave.setSaveMode(saveMode == null ? null : saveMode.name());
  
    WsGroupToSave[] wsGroupsToSave = new WsGroupToSave[] { wsGroupToSave };
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGroupSaveResults wsGroupSaveResults = groupSave(clientVersion, wsGroupsToSave,
        actAsSubjectLookup, null, includeGroupDetail, params);
  
    return new WsGroupSaveLiteResult(wsGroupSaveResults);
  }

  /**
   * <pre>
   * get grouper privileges for a group or folder
   * e.g. /grouperPrivileges/subjects/1234567/groups/aStem:aGroup/types/access/names/update
   * e.g. /grouperPrivileges/subjects/sources/someSource/subjectId/1234567/stems/aStem1:aStem2/
   * </pre>
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectId subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
   * @param subjectSourceId source id of subject object (optional)
   * @param subjectIdentifier subject identifier of subject.  Mutuallyexclusive with subjectId
   * @param groupName if this is a group privilege.  mutually exclusive with groupUuid
   * @param groupUuid if this is a group privilege.  mutually exclusive with groupName
   * @param stemName if this is a stem privilege.  mutually exclusive with stemUuid
   * @param stemUuid if this is a stem privilege.  mutually exclusive with stemName
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeName (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param includeGroupDetail T or F as for if group detail should be included
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member query
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public static WsGetGrouperPrivilegesLiteResult getGrouperPrivilegesLite(final GrouperVersion clientVersion, 
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String groupName, String groupUuid, 
      String stemName, String stemUuid, 
      PrivilegeType privilegeType, Privilege privilegeName,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      boolean includeSubjectDetail, String subjectAttributeNames, 
      boolean includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = 
      new WsGetGrouperPrivilegesLiteResult();
      
    GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetGrouperPrivilegesLiteResult.getResponseMetadata().warnings());

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsSubjectLookup subjectLookup = new WsSubjectLookup(subjectId, 
        subjectSourceId, subjectIdentifier);

    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);

    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    
    // setup the subject lookup
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);


    WsParam[] params = GrouperServiceUtils.params(paramName0, 
        paramValue0, paramValue1, paramValue1);

    GrouperSession session = null;
    String theSummary = null;
    
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsSubject: "
          + subjectLookup + ", group: " +  wsGroupLookup + ", stem: " + wsStemLookup 
          + ", privilege: " + (privilegeType == null ? null : privilegeType.getPrivilegeName()) 
          + "-" + (privilegeName == null ? null : privilegeName.getName())
          + ", actAsSubject: "
          + actAsSubjectLookup 
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
        
      subjectAttributeArray = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeArray, includeSubjectDetail);

      wsGetGrouperPrivilegesLiteResult.setSubjectAttributeNames(subjectAttributeArray);
      
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      boolean hasGroup = wsGroupLookup.hasData();
      boolean hasStem = wsStemLookup.hasData();

      //cant have both
      if (hasGroup && hasStem) {
        throw new WsInvalidQueryException("Cant pass both group and stem.  Pass one or the other");
      }
      
    
      boolean hasSubject = !subjectLookup.blank();
      boolean hasPrivilege = privilegeName != null;
      
      //lets try to assign privilege type if not assigned
      if (privilegeType == null && hasPrivilege) {
        if (Privilege.isAccess(privilegeName)) {
          privilegeType = PrivilegeType.ACCESS;
        } else if (Privilege.isNaming(privilegeName)) {
          privilegeType = PrivilegeType.NAMING;
        } else {
          throw new RuntimeException("Unexpected privilege, cant find type: " + privilegeName);
        }
      }
      
      if (privilegeType == null) {
        if (hasGroup) {
          privilegeType = PrivilegeType.ACCESS;
        } else if (hasStem) {
          privilegeType = PrivilegeType.NAMING;
        }
      }
      
      boolean groupPrivilege = PrivilegeType.ACCESS.equals(privilegeType);
      boolean stemPrivilege = PrivilegeType.NAMING.equals(privilegeType);

      //make sure the privilege type matches
      if (privilegeType != null) {
          
        if (hasGroup && !groupPrivilege) {
            throw new WsInvalidQueryException("If you are querying a group, you need to pass in an " +
              "access privilege type: '" + privilegeType + "', e.g. admin|view|read|optin|optout|update");
          }

        if (hasStem && !stemPrivilege) {
          throw new WsInvalidQueryException("If you are querying a stem, you need to pass in a " +
              "naming privilege type: '" + privilegeType + "', e.g. stem|create");
        }
      }

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
    
      
      Subject subject = null;
      
      if (hasSubject) {
        subject = subjectLookup.retrieveSubject();
        //need to check to see status      
        wsGetGrouperPrivilegesLiteResult.processSubject(subjectLookup, subjectAttributeArray);
        
        if (subject == null) {
          throw new WebServiceDoneException();
        }
      } else {
        if (!hasGroup && !hasStem) {
          //what are we filtering by???
          throw new RuntimeException("Not enough information in the query, pass in at least a subject or group or stem");
        }
      }
      
      AccessResolver accessResolver = session.getAccessResolver();
      NamingResolver namingResolver = session.getNamingResolver();
      
      Set<GrouperPrivilege> privileges = new TreeSet<GrouperPrivilege>();
      if (hasGroup) {
        
        Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
        
        //handle bad stuff
        if (group == null) {
          GroupFindResult groupFindResult = wsGroupLookup.retrieveGroupFindResult();
          if (groupFindResult == GroupFindResult.GROUP_NOT_FOUND) {
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.GROUP_NOT_FOUND);
            throw new WebServiceDoneException();
          }
          throw new RuntimeException(groupFindResult == null ? null : groupFindResult.toString());
        }

        if (subject != null) {

          privileges.addAll(GrouperUtil.nonNull(group.getPrivs(subject)));
        } else {
          Set<Subject> subjects = new HashSet<Subject>();
          if (privilegeName == null || AccessPrivilege.ADMIN.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.ADMIN)));
          } 
          if (privilegeName == null || AccessPrivilege.OPTIN.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.OPTIN)));
          } 
          if (privilegeName == null || AccessPrivilege.OPTOUT.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.OPTOUT)));
          } 
          if (privilegeName == null || AccessPrivilege.READ.equals(privilegeName)) {
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.READ)));
          } 
          if (privilegeName == null || AccessPrivilege.UPDATE.equals(privilegeName)) {
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.UPDATE)));
          } 
          if (privilegeName == null || AccessPrivilege.VIEW.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.VIEW)));
          } 
          //make it a little more efficient, note subjects dont have equals and hashcode, cant just use set
          SubjectHelper.removeDuplicates(subjects);
          //add privs
          for (Subject current : subjects) {
            privileges.addAll(accessResolver.getPrivileges(group, current));
          }
        }
      } else if (hasStem) {

        wsStemLookup.retrieveStemIfNeeded(session, true);
        Stem stem = wsStemLookup.retrieveStem();
        //handle bad stuff
        if (stem == null) {
          StemFindResult stemFindResult = wsStemLookup.retrieveStemFindResult();
          if (stemFindResult == StemFindResult.STEM_NOT_FOUND) {
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.STEM_NOT_FOUND);
            throw new WebServiceDoneException();
          }
          throw new RuntimeException(stemFindResult == null ? null : stemFindResult.toString());
        }

        if (subject != null) {

          privileges.addAll(GrouperUtil.nonNull(stem.getPrivs(subject)));
        } else {
          Set<Subject> subjects = new HashSet<Subject>();
          if (privilegeName == null || NamingPrivilege.CREATE.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(namingResolver.getSubjectsWithPrivilege(stem, NamingPrivilege.CREATE)));
          } 
          if (privilegeName == null || NamingPrivilege.STEM.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(namingResolver.getSubjectsWithPrivilege(stem, NamingPrivilege.STEM)));
          } 
          //make it a little more efficient, note subjects dont have equals and hashcode, cant just use set
          SubjectHelper.removeDuplicates(subjects);
          //add privs
          for (Subject current : subjects) {
            privileges.addAll(namingResolver.getPrivileges(stem, current));
          }
        }
      } else {

        //if group privilege, then 
        Member member = subjectLookup.retrieveMember();
        //if there is no member record, then there is nothing
        if (member != null) {
          //this means no group or stem, but has subject
          if (groupPrivilege || privilegeType == null) {

            Set<Group> groups = new HashSet<Group>();
            if (privilegeName == null || AccessPrivilege.ADMIN.equals(privilegeName)) { 
              groups.addAll(member.hasAdmin());
            } 
            if (privilegeName == null || AccessPrivilege.OPTIN.equals(privilegeName)) { 
              groups.addAll(member.hasOptin());
            } 
            if (privilegeName == null || AccessPrivilege.OPTOUT.equals(privilegeName)) { 
              groups.addAll(member.hasOptout());
            } 
            if (privilegeName == null || AccessPrivilege.READ.equals(privilegeName)) { 
              groups.addAll(member.hasRead());
            } 
            if (privilegeName == null || AccessPrivilege.UPDATE.equals(privilegeName)) { 
              groups.addAll(member.hasUpdate());
            } 
            if (privilegeName == null || AccessPrivilege.VIEW.equals(privilegeName)) { 
              groups.addAll(member.hasView());
            } 
            //from there lets get the privilege
            for (Group group : groups) {
              privileges.addAll(GrouperUtil.nonNull(group.getPrivs(subject)));
            }
          }
          //this means no group or stem, but has subject
          if (stemPrivilege || privilegeType == null) {

            Set<Stem> stems = new HashSet<Stem>();
            if (privilegeName == null || NamingPrivilege.CREATE.equals(privilegeName)) { 
              stems.addAll(member.hasCreate());
            } 
            if (privilegeName == null || NamingPrivilege.STEM.equals(privilegeName)) { 
              stems.addAll(member.hasStem());
            } 
            //from there lets get the privilege
            for (Stem stem : stems) {
              privileges.addAll(GrouperUtil.nonNull(stem.getPrivs(subject)));
            }
          }

        }
        }

        //see if we need to remove, if specifying privs, and this doesnt match
        Iterator<? extends GrouperPrivilege> iterator = privileges.iterator();
        while (iterator.hasNext()) {
          GrouperPrivilege current = iterator.next();
          if (privilegeName != null && !StringUtils.equals(privilegeName.getName(), current.getName())){
            iterator.remove();
          }          
        }

        removePrivsNotAllowedToSee(privileges);

        WsGrouperPrivilegeResult[] privilegeResults = new WsGrouperPrivilegeResult[privileges.size()];
        if (privileges.size() > 0) {
          
          wsGetGrouperPrivilegesLiteResult.setPrivilegeResults(privilegeResults);
        }
        
        int i=0;
        
        //init subjects
        PrivilegeHelper.resolveSubjects(privileges, true);
        
        for (GrouperPrivilege grouperPrivilege : privileges) {
          
          WsGrouperPrivilegeResult wsGrouperPrivilegeResult = new WsGrouperPrivilegeResult();
          privilegeResults[i] = wsGrouperPrivilegeResult;
          
          wsGrouperPrivilegeResult.setAllowed("T");
          Subject owner = grouperPrivilege.getOwner();
          wsGrouperPrivilegeResult.setOwnerSubject(owner == null ? null : new WsSubject(owner, subjectAttributeArray, null));

          String thePrivilegeName = grouperPrivilege.getName();
          wsGrouperPrivilegeResult.setPrivilegeName(thePrivilegeName);
          wsGrouperPrivilegeResult.setPrivilegeType(grouperPrivilege.getType());
          
          wsGrouperPrivilegeResult.setRevokable(grouperPrivilege.isRevokable() ? "T" : "F");

          GrouperAPI groupOrStem = grouperPrivilege.getGrouperApi();
          if (groupOrStem instanceof Group) {
            Group group = (Group)groupOrStem;
            wsGrouperPrivilegeResult.setWsGroup(new WsGroup(group, null, includeGroupDetail));
          } else if (groupOrStem instanceof Stem) {
            Stem stem = (Stem)groupOrStem;
            wsGrouperPrivilegeResult.setWsStem(new WsStem(stem));
          }

          //note, if there is a subejct lookup, it should match here
          //make sure they match
          Subject privilegeSubject = grouperPrivilege.getSubject(); 
          
          //(granted we should check source too, but it might not be available
        if (subject != null) {
          if (!StringUtils.equals(privilegeSubject.getId(), subject.getId())) {
            throw new RuntimeException("These subjects should be equal: " 
                + GrouperUtil.subjectToString(privilegeSubject) + ", " 
                + GrouperUtil.subjectToString(subject));
          }
        }

        //only pass in the subject lookup if it wasnt null
        wsGrouperPrivilegeResult.setWsSubject(new WsSubject(privilegeSubject, subjectAttributeArray, 
            subject != null ? subjectLookup : null));
          
          i++;
        }

        //if the privilege was queried, and group/stem and subject... then it should be one alswer
        if (privilegeName != null && (wsGroupLookup.hasData() ^ wsStemLookup.hasData()) &&
            subject != null && (privileges.size() == 1 || privileges.size() == 0)) {
          
          if (privileges.size() == 1) {
            
            //assign one of 2 success codes
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUCCESS_ALLOWED);
          } else {
            
            //assign one of 2 success codes
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUCCESS_NOT_ALLOWED);
          }
          
          
        } else {
        
          //assign success and the real privs are in the XML
          wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUCCESS);
        }
      
    } catch (WebServiceDoneException wsde) {
      //ignore this
    } catch (InsufficientPrivilegeException ipe) {
      wsGetGrouperPrivilegesLiteResult
          .assignResultCode(WsGetGrouperPrivilegesLiteResultCode.INSUFFICIENT_PRIVILEGES);
    } catch (Exception e) {
      wsGetGrouperPrivilegesLiteResult.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetGrouperPrivilegesLiteResult;

    
  }

  /**
   * remove privileges not allowed to see
   * @param privileges
   */
  public static void removePrivsNotAllowedToSee(Set<GrouperPrivilege> privileges) {
    
    int originalNumberOfPrivileges = GrouperUtil.length(privileges);
    
    if (privileges != null) {
      
      //subject who is making the query
      final Subject grouperSessionSubject = GrouperSession.staticGrouperSession().getSubject();
      
      //if this change breaks an app, and you need a quick fix, you can whitelist users
      final String groupNameOfUsersWhoCanCheckAllPrivileges = GrouperWsConfig.retrieveConfig().propertyValueString("ws.groupNameOfUsersWhoCanCheckAllPrivileges");
      
      //if there is a whitelist to preserve old broken behavior
      if (!StringUtils.isBlank(groupNameOfUsersWhoCanCheckAllPrivileges)) {
        
        //do this as root since the user who is allowed might not be able to read the whitelist group...
        boolean done = (Boolean)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession1) throws GrouperSessionException {
            
            Group groupOfUsersWhoCanCheckAllPrivileges = GroupFinder.findByName(grouperSession1, groupNameOfUsersWhoCanCheckAllPrivileges, false);
            
            if (groupOfUsersWhoCanCheckAllPrivileges != null) {
              
              //if the subject in the grouper session is in the whitelist group, then allow the query without filtering privileges
              if (groupOfUsersWhoCanCheckAllPrivileges.hasMember(grouperSessionSubject)) {
                return true;
              }
              
            } else {
              
              //it is misconfigured, just keep going, but filter privileges based on calling user
              LOG.error("Why is ws.groupNameOfUsersWhoCanCheckAllPrivileges: " + groupNameOfUsersWhoCanCheckAllPrivileges + ", not found????");
            }
            return false;
          }
        });
        
        //this means the calling user is in the whitelist for the old bad logic...
        if (done) {
          return;
        }
      }
      
      //map of group name to if the user is allowed to see privileges
      Map<String, Boolean> groupPrivilegeCache = new HashMap<String, Boolean>();

      //map of stem name to if the user is allowed to see privileges
      Map<String, Boolean> stemPrivilegeCache = new HashMap<String, Boolean>();
          
      Iterator<GrouperPrivilege> iterator = privileges.iterator();
      
      while (iterator.hasNext()) {
        GrouperPrivilege grouperPrivilege = iterator.next();
        
        GrouperAPI grouperApi = grouperPrivilege.getGrouperApi();
        if (grouperApi instanceof Group) {
          
          Group group = (Group)grouperApi;
          String groupName = group.getName();
          
          //check the cache
          Boolean allowed = groupPrivilegeCache.get(groupName);
          if (allowed == null) {
            //not in cache
            //see if allowed
            allowed = group.hasAdmin(grouperSessionSubject);
            
            //add back to cache
            groupPrivilegeCache.put(group.getName(), allowed);
            
          }
          
          if (!allowed) {
            iterator.remove();
          }
          
        } else if (grouperApi instanceof Stem) {

          Stem stem = (Stem)grouperApi;
          String stemName = stem.getName();
          
          //check the cache
          Boolean allowed = stemPrivilegeCache.get(stemName);
          if (allowed == null) {
            //not in cache
            //see if allowed
            allowed = stem.hasStem(grouperSessionSubject);
            
            //add back to cache
            stemPrivilegeCache.put(stem.getName(), allowed);
            
          }
          
          if (!allowed) {
            iterator.remove();
          }

        } else {
          //this should never happen
          throw new RuntimeException("Not expecting GrouperAPI of type: " + grouperApi.getClass() + ", " + grouperApi);
        }
        
      }
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("removePrivsNotAllowedToSee() from " + originalNumberOfPrivileges + " to " + GrouperUtil.length(privileges) + " privileges");
    }
    
  }

  //  /**
  //   * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
  //   * just view.  
  //   * 
  //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
  //   * @param wsGroupLookups
  //   *            groups to save
  //   * @param wsAttributeEdits are the attributes to change or delete
  //   * @param actAsSubjectLookup
  //   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
  //   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
  //   * are NONE (or blank), and READ_WRITE_NEW.
  //   * @param params optional: reserved for future use
  //   * @return the results
  //   */
  //  @SuppressWarnings("unchecked")
  //  public static WsViewOrEditAttributesResults viewOrEditAttributes(final GrouperWsVersion clientVersion,
  //      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
  //      final WsSubjectLookup actAsSubjectLookup, final GrouperTransactionType txType,
  //      final WsParam[] params) {
  //  
  //    GrouperSession session = null;
  //    int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;
  //  
  //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();
  //  
  //    //convert the options to a map for easy access, and validate them
  //    @SuppressWarnings("unused")
  //    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
  //        params);
  //  
  //    // see if greater than the max (or default)
  //    int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
  //        GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
  //    if (groupsSize > maxAttributeGroup) {
  //      wsViewOrEditAttributesResults
  //          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
  //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
  //          "Number of groups must be less than max: " + maxAttributeGroup + " (sent in "
  //              + groupsSize + ")");
  //      return wsViewOrEditAttributesResults;
  //    }
  //  
  //    // TODO make sure size of params and values the same
  //  
  //    //lets validate the attribute edits
  //    boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
  //    if (!readOnly) {
  //      for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
  //        String errorMessage = wsAttributeEdit.validate();
  //        if (errorMessage != null) {
  //          wsViewOrEditAttributesResults
  //              .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
  //          wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
  //              errorMessage + ", " + wsAttributeEdit);
  //        }
  //      }
  //    }
  //  
  //    // assume success
  //    wsViewOrEditAttributesResults
  //        .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
  //    Subject actAsSubject = null;
  //    // TODO have common try/catch
  //    try {
  //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
  //  
  //      if (actAsSubject == null) {
  //        // TODO make this a result code
  //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
  //      }
  //  
  //      // use this to be the user connected, or the user act-as
  //      try {
  //        session = GrouperSession.start(actAsSubject);
  //      } catch (SessionException se) {
  //        // TODO make this a result code
  //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
  //            se);
  //      }
  //  
  //      int resultIndex = 0;
  //  
  //      wsViewOrEditAttributesResults
  //          .setResults(new WsViewOrEditAttributesResult[groupsSize]);
  //      GROUP_LOOP: for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
  //        WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
  //        wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
  //        Group group = null;
  //  
  //        try {
  //          wsViewOrEditAttributesResult.setGroupName(wsGroupLookup.getGroupName());
  //          wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
  //  
  //          //get the group
  //          wsGroupLookup.retrieveGroupIfNeeded(session);
  //          group = wsGroupLookup.retrieveGroup();
  //          if (group == null) {
  //            wsViewOrEditAttributesResult
  //                .assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
  //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                "Cant find group: '" + wsGroupLookup + "'.  ");
  //            continue;
  //          }
  //  
  //          group = wsGroupLookup.retrieveGroup();
  //  
  //          // these will probably match, but just in case
  //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
  //            wsViewOrEditAttributesResult.setGroupName(group.getName());
  //          }
  //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
  //            wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
  //          }
  //  
  //          //lets read them
  //          Map<String, String> attributeMap = GrouperUtil.nonNull(group.getAttributes());
  //  
  //          //see if we are updating
  //          if (!readOnly) {
  //            for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
  //              String attributeName = wsAttributeEdit.getName();
  //              try {
  //                //lets see if delete
  //                if (wsAttributeEdit.deleteBoolean()) {
  //                  //if its not there, dont bother
  //                  if (attributeMap.containsKey(attributeName)) {
  //                    group.deleteAttribute(attributeName);
  //                    //update map
  //                    attributeMap.remove(attributeName);
  //                  }
  //                } else {
  //                  String attributeValue = wsAttributeEdit.getValue();
  //                  //make sure it is different
  //                  if (!StringUtils
  //                      .equals(attributeValue, attributeMap.get(attributeName))) {
  //                    //it is update
  //                    group.setAttribute(attributeName, wsAttributeEdit.getValue());
  //                    attributeMap.put(attributeName, attributeValue);
  //                  }
  //                }
  //              } catch (AttributeNotFoundException anfe) {
  //                wsViewOrEditAttributesResult
  //                    .assignResultCode(WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
  //                wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                    "Cant find attribute: " + attributeName);
  //                //go to next group
  //                continue GROUP_LOOP;
  //  
  //              }
  //            }
  //          }
  //          //now take the attributes and put them in the result
  //          if (attributeMap.size() > 0) {
  //            int attributeIndex = 0;
  //            WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
  //            wsViewOrEditAttributesResult.setAttributes(attributes);
  //            //lookup each from map and return
  //            for (String key : attributeMap.keySet()) {
  //              WsAttribute wsAttribute = new WsAttribute();
  //              attributes[attributeIndex++] = wsAttribute;
  //              wsAttribute.setName(key);
  //              wsAttribute.setValue(attributeMap.get(key));
  //            }
  //          }
  //          wsViewOrEditAttributesResult.getResultMetadata().assignSuccess("T");
  //          wsViewOrEditAttributesResult.getResultMetadata().assignResultCode("SUCCESS");
  //          if (readOnly) {
  //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                "Group '" + group.getName() + "' was queried.");
  //          } else {
  //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                "Group '" + group.getName() + "' had attributes edited.");
  //          }
  //        } catch (InsufficientPrivilegeException ipe) {
  //          wsViewOrEditAttributesResult
  //              .assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
  //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //              "Error: insufficient privileges to view/edit attributes '"
  //                  + wsGroupLookup.getGroupName() + "'");
  //        } catch (Exception e) {
  //          // lump the rest in there, group_add_exception, etc
  //          wsViewOrEditAttributesResult
  //              .assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
  //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //              ExceptionUtils.getFullStackTrace(e));
  //          LOG.error(wsGroupLookup + ", " + e, e);
  //        }
  //      }
  //  
  //    } catch (RuntimeException re) {
  //      wsViewOrEditAttributesResults
  //          .assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
  //      String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
  //          + GrouperUtil.toStringForLog(wsGroupLookups) + ", attributeEdits: "
  //          + GrouperUtil.toStringForLog(wsAttributeEdits) + ", actAsSubject: "
  //          + actAsSubject + ".  \n" + "";
  //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(theError);
  //      // this is sent back to the caller anyway, so just log, and not send
  //      // back again
  //      LOG.error(theError + ", wsViewOrEditAttributesResults: "
  //          + GrouperUtil.toStringForLog(wsViewOrEditAttributesResults), re);
  //    } finally {
  //      if (session != null) {
  //        try {
  //          session.stop();
  //        } catch (Exception e) {
  //          LOG.error(e.getMessage(), e);
  //        }
  //      }
  //    }
  //  
  //    if (wsViewOrEditAttributesResults.getResults() != null) {
  //      // check all entries
  //      int successes = 0;
  //      int failures = 0;
  //      for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
  //          .getResults()) {
  //        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
  //            : wsGroupSaveResult.getResultMetadata().getSuccess());
  //        if (success) {
  //          successes++;
  //        } else {
  //          failures++;
  //        }
  //      }
  //      if (failures > 0) {
  //        wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
  //            "There were " + successes + " successes and " + failures
  //                + " failures of viewing/editing group attribues.   ");
  //        wsViewOrEditAttributesResults
  //            .assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
  //      } else {
  //        wsViewOrEditAttributesResults
  //            .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
  //      }
  //    }
  //    if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getResultMetadata()
  //        .getSuccess())) {
  //  
  //      LOG.error(wsViewOrEditAttributesResults.getResultMetadata().getResultMessage());
  //    }
  //    return wsViewOrEditAttributesResults;
  //  }
  //
  //  /**
  //   * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
  //   * just view.  
  //   * 
  //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
  //   * @param groupName
  //   *            to delete the group (mutually exclusive with groupUuid)
  //   * @param groupUuid
  //   *            to delete the group (mutually exclusive with groupName)
  //   * @param attributeName0 name of first attribute (optional)
  //   * @param attributeValue0 value of first attribute (optional)
  //   * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
  //   * @param attributeName1 name of second attribute (optional)
  //   * @param attributeValue1 value of second attribute (optional)
  //   * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
  //   * @param attributeName2 name of third attribute (optional)
  //   * @param attributeValue2 value of third attribute (optional)
  //   * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
  //   * @param actAsSubjectId
  //   *            optional: is the subject id of subject to act as (if
  //   *            proxying). Only pass one of actAsSubjectId or
  //   *            actAsSubjectIdentifer
  //   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
  //   * duplicates
  //   * @param actAsSubjectIdentifier
  //   *            optional: is the subject identifier of subject to act as (if
  //   *            proxying). Only pass one of actAsSubjectId or
  //   *            actAsSubjectIdentifer
  //   * @param paramName0
  //   *            reserved for future use
  //   * @param paramValue0
  //   *            reserved for future use
  //   * @param paramName1
  //   *            reserved for future use
  //   * @param paramValue1
  //   *            reserved for future use
  //   * @return the result of one member add
  //   */
  //  public static WsViewOrEditAttributesResults viewOrEditAttributesLite(
  //      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
  //      String attributeName0, String attributeValue0, String attributeDelete0,
  //      String attributeName1, String attributeValue1, String attributeDelete1,
  //      String attributeName2, String attributeValue2, String attributeDelete2,
  //      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
  //      String paramName0, String paramValue0, String paramName1, String paramValue1) {
  //  
  //    // setup the group lookup
  //    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  //    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
  //  
  //    //setup attributes
  //    List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
  //    if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
  //        || !StringUtils.isBlank(attributeDelete0)) {
  //      attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0,
  //          attributeDelete0));
  //    }
  //    if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
  //        || !StringUtils.isBlank(attributeDelete1)) {
  //      attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1,
  //          attributeDelete1));
  //    }
  //    if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
  //        || !StringUtils.isBlank(attributeDelete2)) {
  //      attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2,
  //          attributeDelete2));
  //    }
  //    //convert to array
  //    WsAttributeEdit[] wsAttributeEdits = GrouperUtil.toArray(attributeEditList,
  //        WsAttributeEdit.class);
  //    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
  //        actAsSubjectSourceId, actAsSubjectIdentifier);
  //  
  //    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  //  
  //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(
  //        clientVersion, wsGroupLookups, wsAttributeEdits, actAsSubjectLookup, null,
  //        params);
  //  
  //    return wsViewOrEditAttributesResults;
  //  }
  //
    
    /**
     * <pre>
     * assign a privilege for a user/group/type/name combo
     * e.g. /grouperPrivileges/subjects/1234567/groups/aStem:aGroup/types/access/names/update
     * e.g. /grouperPrivileges/subjects/sources/someSource/subjectId/1234567/stems/aStem1:aStem2/
     * </pre>
     * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param subjectId subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
     * @param subjectSourceId source id of subject object (optional)
     * @param subjectIdentifier subject identifier of subject.  Mutuallyexclusive with subjectId
     * @param groupName if this is a group privilege.  mutually exclusive with groupUuid
     * @param groupUuid if this is a group privilege.  mutually exclusive with groupName
     * @param stemName if this is a stem privilege.  mutually exclusive with stemUuid
     * @param stemUuid if this is a stem privilege.  mutually exclusive with stemName
     * @param actAsSubjectId
     *            optional: is the subject id of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
     * duplicates
     * @param actAsSubjectIdentifier
     *            optional: is the subject identifier of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param privilegeType (e.g. "access" for groups and "naming" for stems)
     * @param privilegeName (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
     * stem, create)
     * @param allowed is T to allow this privilege, F to deny this privilege
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
     * @param includeGroupDetail T or F as for if group detail should be included
     * @param paramName0
     *            reserved for future use
     * @param paramValue0
     *            reserved for future use
     * @param paramName1
     *            reserved for future use
     * @param paramValue1
     *            reserved for future use
     * @return the result of one member query
     */
    public static WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLite(
        final GrouperVersion clientVersion, 
        String subjectId, String subjectSourceId, String subjectIdentifier,
        String groupName, String groupUuid, 
        String stemName, String stemUuid, 
        PrivilegeType privilegeType, Privilege privilegeName,
        boolean allowed,
        String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
        boolean includeSubjectDetail, String subjectAttributeNames, 
        boolean includeGroupDetail, String paramName0,
        String paramValue0, String paramName1, String paramValue1) {

      // setup the group lookup
      WsGroupLookup wsGroupLookup = null;
      
      if (!StringUtils.isBlank(groupName) || !StringUtils.isBlank(groupUuid)) {
        wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
      }

      WsStemLookup wsStemLookup = null;
      
      if (!StringUtils.isBlank(stemName) || !StringUtils.isBlank(stemUuid)) {
        wsStemLookup = new WsStemLookup(stemName, stemUuid);
      }

      // setup the subject lookup
      WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
      subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
      WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
          actAsSubjectSourceId, actAsSubjectIdentifier);


      WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName1, paramValue1);

      Privilege[] privileges = new Privilege[]{privilegeName};
      
      String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

      WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = assignGrouperPrivileges(clientVersion, 
          subjectLookups, wsGroupLookup, wsStemLookup, privilegeType, privileges, allowed, false, null, actAsSubjectLookup, 
          includeSubjectDetail, subjectAttributeArray, includeGroupDetail, params);

      WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = new WsAssignGrouperPrivilegesLiteResult(
          wsAssignGrouperPrivilegesResults);

      return wsAssignGrouperPrivilegesLiteResult;
    }

    /**
     * get subjects from searching by id or identifier or search string.  Can filter by subjects which
     * are members in a group.
     * 
     * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param wsSubjectLookups are subjects to look in
     * @param searchString free form string query to find a list of subjects (exact behavior depends on source)
     * @param wsMemberFilter
     *            must be one of All, Effective, Immediate, Composite, NonImmediate
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param actAsSubjectLookup
     * @param fieldName is if the memberships should be retrieved from a certain field membership
     * of the group (certain list)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent
     * @param includeGroupDetail T or F as to if the group detail should be returned
     * @param params optional: reserved for future use
     * @param sourceIds are sources to look in for memberships, or null if all
     * @param wsGroupLookup specify a group if the subjects must be in the group (limit of number of subjects
     * found in list is much lower e.g. 1000)
     * @return the results
     */
    @SuppressWarnings("unchecked")
    public static WsGetSubjectsResults getSubjects(final GrouperVersion clientVersion,
        WsSubjectLookup[] wsSubjectLookups, String searchString, boolean includeSubjectDetail,
        String[] subjectAttributeNames, WsSubjectLookup actAsSubjectLookup, 
        String[] sourceIds, WsGroupLookup wsGroupLookup, WsMemberFilter wsMemberFilter,
        Field fieldName, boolean includeGroupDetail, final WsParam[] params) {  
    
      WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
    
      GrouperSession session = null;
      String theSummary = null;
      try {
    
        GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetSubjectsResults.getResponseMetadata().warnings());

        theSummary = "clientVersion: " + clientVersion + ", wsSubjectLookups: "
            + GrouperUtil.toStringForLog(wsSubjectLookups, 200) + ", searchString: '" + searchString + "'" 
            + ", wsMemberFilter: " + wsMemberFilter
            + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
            + actAsSubjectLookup + ", fieldName: " + fieldName  + ", wsGroupLookup: " + wsGroupLookup
            + ", subjectAttributeNames: "
            + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
            + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsSubjectLookups: "
            + GrouperUtil.toStringForLog(wsSubjectLookups, 200) + "\n, sourceIds: " + GrouperUtil.toStringForLog(sourceIds, 100);
    
        //start session based on logged in user or the actAs passed in
        session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
    
        //convert the options to a map for easy access, and validate them
        @SuppressWarnings("unused")
        Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
            params);
        
        MembershipType membershipType = null;
        if (wsMemberFilter != null) {
          membershipType = wsMemberFilter.getMembershipType();
        }
        
        Group group = null;
        if (wsGroupLookup != null && wsGroupLookup.hasData()) {
          wsGroupLookup.retrieveGroupIfNeeded(session, "getSubjects group is not valid");
          group = wsGroupLookup.retrieveGroup();            
          wsGetSubjectsResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
        }
        
        boolean filteringByGroup = group != null;
        
        //get all the members
        Set<Subject> resultSubjects = new HashSet<Subject>();
        Set<WsSubject> resultWsSubjects = new TreeSet<WsSubject>();

        String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

        wsGetSubjectsResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);

        //we need to keep track of the lookups if doing group filtering and specifying the users by id or identifier
        //multikey of source id to subject id
        Map<MultiKey, WsSubjectLookup> subjectLookupMap = null;
        
        //find members by id or identifier
        if (GrouperUtil.length(wsSubjectLookups) > 0 && !wsSubjectLookups[0].blank()) {
          
          subjectLookupMap = new HashMap<MultiKey, WsSubjectLookup>();
          
          for (WsSubjectLookup wsSubjectLookup : wsSubjectLookups) {
            if (wsSubjectLookup == null) {
              continue;
            }
            
            Subject subject = wsSubjectLookup.retrieveSubject();
            
            //normally we will keep the subjects not found, but not if filtering by group, no subject, or result
            if (subject == null && filteringByGroup) {
              continue; 
            }
            
            if (subject != null) {
              subjectLookupMap.put(SubjectHelper.convertToMultiKey(subject), wsSubjectLookup);
            }
            
            //keep track here if not filtering by group
            if (!filteringByGroup) {
              WsSubject wsSubject = new WsSubject(subject, subjectAttributeNamesToRetrieve, wsSubjectLookup);
              
              resultWsSubjects.add(wsSubject);
            }
            
            if (subject == null) {

              continue;
            }
            resultSubjects.add(subject);
          }
        }
        
        //free form search
        if (!StringUtils.isBlank(searchString)) {
          
          //if filtering by stem, and stem not found, then dont find any memberships
          Set<Source> sources = GrouperUtil.convertSources(sourceIds);
          
          Set<Subject> subjects = SubjectFinder.findPage(searchString, sources).getResults();
          
          for (Subject subject: GrouperUtil.nonNull(subjects)) {
            resultSubjects.add(subject);
            
            //keep track here if not filtering by group
            if (!filteringByGroup) {
              resultWsSubjects.add(new WsSubject(subject, subjectAttributeNamesToRetrieve, null));
            }
              
          }
          
        }
        
        int resultSubjectsLengthPreGroup = GrouperUtil.length(resultSubjects);
        if (filteringByGroup && resultSubjectsLengthPreGroup > 0) {
          //we have a list of subjects, lets see if they are too large
          if (resultSubjectsLengthPreGroup > GrouperWsConfig.retrieveConfig().propertyValueInt("ws.get.subjects.max.filter.by.group", 1000)) {
            throw new TooManyResultsWhenFilteringByGroupException();
          }
          
          //lets filter by group
          Set<Member> members = MemberFinder.findBySubjectsInGroup(session, resultSubjects, group, fieldName, membershipType);
          
          resultSubjects = null;
          
          if (GrouperUtil.length(members) > 0) {
            resultSubjects = new HashSet<Subject>();
            for (Member member : members) {
              Subject subject = member.getSubject();
              
              WsSubjectLookup wsSubjectLookup = null;
              if (subjectLookupMap != null) {
                
                wsSubjectLookup = subjectLookupMap.get(SubjectHelper.convertToMultiKey(subject));
                
              }
              
              WsSubject wsSubject = new WsSubject(subject, subjectAttributeNamesToRetrieve, wsSubjectLookup);
              resultWsSubjects.add(wsSubject);
            }
          }
        }
        
        //calculate and return the results
        if (GrouperUtil.length(resultWsSubjects) > 0) {
          
          wsGetSubjectsResults.setWsSubjects(GrouperUtil.toArray(resultWsSubjects, WsSubject.class));
        }
        
        wsGetSubjectsResults.assignResultCode(WsGetSubjectsResultsCode.SUCCESS);
        
        wsGetSubjectsResults.getResultMetadata().setResultMessage(
            "Queried " + GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()) + " subjects");
          
      } catch (Exception e) {
        wsGetSubjectsResults.assignResultCodeException(null, theSummary, e);
      } finally {
        GrouperSession.stopQuietly(session);
      }

      return wsGetSubjectsResults;
    
    }

    /**
     * get subjects from searching by id or identifier or search string.  Can filter by subjects which
     * are members in a group.
     * 
     * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param wsSubjectLookups are subjects to look in
     * @param subjectId to find a subject by id
     * @param sourceId to find a subject by id or identifier
     * @param subjectIdentifier to find a subject by identifier
     * @param searchString free form string query to find a list of subjects (exact behavior depends on source)
     * @param wsMemberFilter
     *            must be one of All, Effective, Immediate, Composite, NonImmediate or null (all)
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param actAsSubjectId
     *            optional: is the subject id of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
     * duplicates
     * @param actAsSubjectIdentifier
     *            optional: is the subject identifier of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param fieldName is if the memberships should be retrieved from a certain field membership
     * of the group (certain list)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
     * if multiple
     * @param includeGroupDetail T or F as to if the group detail should be returned
     * @param paramName0
     *            reserved for future use
     * @param paramValue0
     *            reserved for future use
     * @param paramName1
     *            reserved for future use
     * @param paramValue1
     *            reserved for future use
     * @param sourceIds are comma separated sourceIds for a searchString
     * @param groupName specify a group if the subjects must be in the group (limit of number of subjects
     * found in list is much lower e.g. 1000)
     * @param groupUuid specify a group if the subjects must be in the group (limit of number of subjects
     * found in list is much lower e.g. 1000)
     * @return the results or none if none found
     */
    public static WsGetSubjectsResults getSubjectsLite(final GrouperVersion clientVersion,
        String subjectId, String sourceId, String subjectIdentifier, String searchString,
        boolean includeSubjectDetail, String subjectAttributeNames,
        String actAsSubjectId, String actAsSubjectSourceId,
        String actAsSubjectIdentifier, String sourceIds,
        String groupName, String groupUuid, WsMemberFilter wsMemberFilter,
        Field fieldName, boolean includeGroupDetail, String paramName0, String paramValue0,
        String paramName1, String paramValue1) {
    
      // setup the group lookup
      WsGroupLookup wsGroupLookup = null;
      
      if (StringUtils.isNotBlank(groupName) || StringUtils.isNotBlank(groupUuid)) {
        wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
      }
    
      WsSubjectLookup wsSubjectLookup = WsSubjectLookup.createIfNeeded(subjectId, sourceId, subjectIdentifier);
      
      WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
          actAsSubjectSourceId, actAsSubjectIdentifier);
    
      WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
    
      String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
      // pass through to the more comprehensive method
      WsSubjectLookup[] wsSubjectLookups = wsSubjectLookup == null ? null : new WsSubjectLookup[]{wsSubjectLookup};
      
      String[] sourceIdArray = GrouperUtil.splitTrim(sourceIds, ",");
      
      WsGetSubjectsResults wsGetSubjectsResults = getSubjects(clientVersion,
          wsSubjectLookups, searchString, includeSubjectDetail, subjectAttributeArray, actAsSubjectLookup, sourceIdArray, wsGroupLookup, wsMemberFilter, fieldName,
          includeGroupDetail,
          params);
    
      return wsGetSubjectsResults;
    }

    //  /**
    //   * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
    //   * just view.  
    //   * 
    //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
    //   * @param wsGroupLookups
    //   *            groups to save
    //   * @param wsAttributeEdits are the attributes to change or delete
    //   * @param actAsSubjectLookup
    //   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
    //   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
    //   * are NONE (or blank), and READ_WRITE_NEW.
    //   * @param params optional: reserved for future use
    //   * @return the results
    //   */
    //  @SuppressWarnings("unchecked")
    //  public static WsViewOrEditAttributesResults viewOrEditAttributes(final GrouperWsVersion clientVersion,
    //      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
    //      final WsSubjectLookup actAsSubjectLookup, final GrouperTransactionType txType,
    //      final WsParam[] params) {
    //  
    //    GrouperSession session = null;
    //    int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;
    //  
    //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();
    //  
    //    //convert the options to a map for easy access, and validate them
    //    @SuppressWarnings("unused")
    //    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
    //        params);
    //  
    //    // see if greater than the max (or default)
    //    int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
    //        GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
    //    if (groupsSize > maxAttributeGroup) {
    //      wsViewOrEditAttributesResults
    //          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
    //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
    //          "Number of groups must be less than max: " + maxAttributeGroup + " (sent in "
    //              + groupsSize + ")");
    //      return wsViewOrEditAttributesResults;
    //    }
    //  
    //    // TODO make sure size of params and values the same
    //  
    //    //lets validate the attribute edits
    //    boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
    //    if (!readOnly) {
    //      for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
    //        String errorMessage = wsAttributeEdit.validate();
    //        if (errorMessage != null) {
    //          wsViewOrEditAttributesResults
    //              .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
    //          wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
    //              errorMessage + ", " + wsAttributeEdit);
    //        }
    //      }
    //    }
    //  
    //    // assume success
    //    wsViewOrEditAttributesResults
    //        .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
    //    Subject actAsSubject = null;
    //    // TODO have common try/catch
    //    try {
    //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
    //  
    //      if (actAsSubject == null) {
    //        // TODO make this a result code
    //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
    //      }
    //  
    //      // use this to be the user connected, or the user act-as
    //      try {
    //        session = GrouperSession.start(actAsSubject);
    //      } catch (SessionException se) {
    //        // TODO make this a result code
    //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
    //            se);
    //      }
    //  
    //      int resultIndex = 0;
    //  
    //      wsViewOrEditAttributesResults
    //          .setResults(new WsViewOrEditAttributesResult[groupsSize]);
    //      GROUP_LOOP: for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
    //        WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
    //        wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
    //        Group group = null;
    //  
    //        try {
    //          wsViewOrEditAttributesResult.setGroupName(wsGroupLookup.getGroupName());
    //          wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
    //  
    //          //get the group
    //          wsGroupLookup.retrieveGroupIfNeeded(session);
    //          group = wsGroupLookup.retrieveGroup();
    //          if (group == null) {
    //            wsViewOrEditAttributesResult
    //                .assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
    //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                "Cant find group: '" + wsGroupLookup + "'.  ");
    //            continue;
    //          }
    //  
    //          group = wsGroupLookup.retrieveGroup();
    //  
    //          // these will probably match, but just in case
    //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
    //            wsViewOrEditAttributesResult.setGroupName(group.getName());
    //          }
    //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
    //            wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
    //          }
    //  
    //          //lets read them
    //          Map<String, String> attributeMap = GrouperUtil.nonNull(group.getAttributes());
    //  
    //          //see if we are updating
    //          if (!readOnly) {
    //            for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
    //              String attributeName = wsAttributeEdit.getName();
    //              try {
    //                //lets see if delete
    //                if (wsAttributeEdit.deleteBoolean()) {
    //                  //if its not there, dont bother
    //                  if (attributeMap.containsKey(attributeName)) {
    //                    group.deleteAttribute(attributeName);
    //                    //update map
    //                    attributeMap.remove(attributeName);
    //                  }
    //                } else {
    //                  String attributeValue = wsAttributeEdit.getValue();
    //                  //make sure it is different
    //                  if (!StringUtils
    //                      .equals(attributeValue, attributeMap.get(attributeName))) {
    //                    //it is update
    //                    group.setAttribute(attributeName, wsAttributeEdit.getValue());
    //                    attributeMap.put(attributeName, attributeValue);
    //                  }
    //                }
    //              } catch (AttributeNotFoundException anfe) {
    //                wsViewOrEditAttributesResult
    //                    .assignResultCode(WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
    //                wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                    "Cant find attribute: " + attributeName);
    //                //go to next group
    //                continue GROUP_LOOP;
    //  
    //              }
    //            }
    //          }
    //          //now take the attributes and put them in the result
    //          if (attributeMap.size() > 0) {
    //            int attributeIndex = 0;
    //            WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
    //            wsViewOrEditAttributesResult.setAttributes(attributes);
    //            //lookup each from map and return
    //            for (String key : attributeMap.keySet()) {
    //              WsAttribute wsAttribute = new WsAttribute();
    //              attributes[attributeIndex++] = wsAttribute;
    //              wsAttribute.setName(key);
    //              wsAttribute.setValue(attributeMap.get(key));
    //            }
    //          }
    //          wsViewOrEditAttributesResult.getResultMetadata().assignSuccess("T");
    //          wsViewOrEditAttributesResult.getResultMetadata().assignResultCode("SUCCESS");
    //          if (readOnly) {
    //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                "Group '" + group.getName() + "' was queried.");
    //          } else {
    //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                "Group '" + group.getName() + "' had attributes edited.");
    //          }
    //        } catch (InsufficientPrivilegeException ipe) {
    //          wsViewOrEditAttributesResult
    //              .assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
    //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //              "Error: insufficient privileges to view/edit attributes '"
    //                  + wsGroupLookup.getGroupName() + "'");
    //        } catch (Exception e) {
    //          // lump the rest in there, group_add_exception, etc
    //          wsViewOrEditAttributesResult
    //              .assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
    //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //              ExceptionUtils.getFullStackTrace(e));
    //          LOG.error(wsGroupLookup + ", " + e, e);
    //        }
    //      }
    //  
    //    } catch (RuntimeException re) {
    //      wsViewOrEditAttributesResults
    //          .assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
    //      String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
    //          + GrouperUtil.toStringForLog(wsGroupLookups) + ", attributeEdits: "
    //          + GrouperUtil.toStringForLog(wsAttributeEdits) + ", actAsSubject: "
    //          + actAsSubject + ".  \n" + "";
    //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(theError);
    //      // this is sent back to the caller anyway, so just log, and not send
    //      // back again
    //      LOG.error(theError + ", wsViewOrEditAttributesResults: "
    //          + GrouperUtil.toStringForLog(wsViewOrEditAttributesResults), re);
    //    } finally {
    //      if (session != null) {
    //        try {
    //          session.stop();
    //        } catch (Exception e) {
    //          LOG.error(e.getMessage(), e);
    //        }
    //      }
    //    }
    //  
    //    if (wsViewOrEditAttributesResults.getResults() != null) {
    //      // check all entries
    //      int successes = 0;
    //      int failures = 0;
    //      for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
    //          .getResults()) {
    //        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
    //            : wsGroupSaveResult.getResultMetadata().getSuccess());
    //        if (success) {
    //          successes++;
    //        } else {
    //          failures++;
    //        }
    //      }
    //      if (failures > 0) {
    //        wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
    //            "There were " + successes + " successes and " + failures
    //                + " failures of viewing/editing group attribues.   ");
    //        wsViewOrEditAttributesResults
    //            .assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
    //      } else {
    //        wsViewOrEditAttributesResults
    //            .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
    //      }
    //    }
    //    if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getResultMetadata()
    //        .getSuccess())) {
    //  
    //      LOG.error(wsViewOrEditAttributesResults.getResultMetadata().getResultMessage());
    //    }
    //    return wsViewOrEditAttributesResults;
    //  }
    //
    //  /**
    //   * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
    //   * just view.  
    //   * 
    //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
    //   * @param groupName
    //   *            to delete the group (mutually exclusive with groupUuid)
    //   * @param groupUuid
    //   *            to delete the group (mutually exclusive with groupName)
    //   * @param attributeName0 name of first attribute (optional)
    //   * @param attributeValue0 value of first attribute (optional)
    //   * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
    //   * @param attributeName1 name of second attribute (optional)
    //   * @param attributeValue1 value of second attribute (optional)
    //   * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
    //   * @param attributeName2 name of third attribute (optional)
    //   * @param attributeValue2 value of third attribute (optional)
    //   * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
    //   * @param actAsSubjectId
    //   *            optional: is the subject id of subject to act as (if
    //   *            proxying). Only pass one of actAsSubjectId or
    //   *            actAsSubjectIdentifer
    //   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
    //   * duplicates
    //   * @param actAsSubjectIdentifier
    //   *            optional: is the subject identifier of subject to act as (if
    //   *            proxying). Only pass one of actAsSubjectId or
    //   *            actAsSubjectIdentifer
    //   * @param paramName0
    //   *            reserved for future use
    //   * @param paramValue0
    //   *            reserved for future use
    //   * @param paramName1
    //   *            reserved for future use
    //   * @param paramValue1
    //   *            reserved for future use
    //   * @return the result of one member add
    //   */
    //  public static WsViewOrEditAttributesResults viewOrEditAttributesLite(
    //      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
    //      String attributeName0, String attributeValue0, String attributeDelete0,
    //      String attributeName1, String attributeValue1, String attributeDelete1,
    //      String attributeName2, String attributeValue2, String attributeDelete2,
    //      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
    //      String paramName0, String paramValue0, String paramName1, String paramValue1) {
    //  
    //    // setup the group lookup
    //    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    //    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
    //  
    //    //setup attributes
    //    List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
    //    if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
    //        || !StringUtils.isBlank(attributeDelete0)) {
    //      attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0,
    //          attributeDelete0));
    //    }
    //    if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
    //        || !StringUtils.isBlank(attributeDelete1)) {
    //      attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1,
    //          attributeDelete1));
    //    }
    //    if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
    //        || !StringUtils.isBlank(attributeDelete2)) {
    //      attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2,
    //          attributeDelete2));
    //    }
    //    //convert to array
    //    WsAttributeEdit[] wsAttributeEdits = GrouperUtil.toArray(attributeEditList,
    //        WsAttributeEdit.class);
    //    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
    //        actAsSubjectSourceId, actAsSubjectIdentifier);
    //  
    //    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
    //  
    //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(
    //        clientVersion, wsGroupLookups, wsAttributeEdits, actAsSubjectLookup, null,
    //        params);
    //  
    //    return wsViewOrEditAttributesResults;
    //  }
    //
      
  /**
   * <pre>
   * assign a privilege for a user/group/type/name combo
   * e.g. POST /grouperPrivileges
   * </pre>
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsSubjectLookups are the subjects to assign the privileges to, looked up by subjectId or identifier
   * @param wsGroupLookup if this is a group privilege, this is the group
   * @param wsStemLookup if this is a stem privilege, this is the stem
   * @param replaceAllExisting
   *            optional: T or F (default), If replaceAllExisting is T, 
   *            then allowed must be set to T.  This will assign the provided 
   *            privilege(s) to the provided subject(s), and remove it from all other 
   *            subjects who are assigned. If F or blank, assign or remove  
   *            (depending on value provided in 'allowed') the provided privilege(s) 
   *            from the provided subject(s)
   * @param actAsSubjectLookup optional: is the subject to act as (if proxying).
   * @param privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeNames (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param allowed is T to allow this privilege, F to deny this privilege
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param includeGroupDetail T or F as for if group detail should be included
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params
   *            optional: reserved for future use
   * @return the result of one member query
   */
  public static WsAssignGrouperPrivilegesResults assignGrouperPrivileges(
      final GrouperVersion clientVersion, 
      final WsSubjectLookup[] wsSubjectLookups,
      final WsGroupLookup wsGroupLookup,
      final WsStemLookup wsStemLookup,
      final PrivilegeType privilegeType, final Privilege[] privilegeNames,
      final boolean allowed,
      final boolean replaceAllExisting, GrouperTransactionType txType,
      final WsSubjectLookup actAsSubjectLookup,
      final boolean includeSubjectDetail, final String[] subjectAttributeNames, 
      final boolean includeGroupDetail,  final WsParam[] params) {

    final WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = 
      new WsAssignGrouperPrivilegesResults();

    GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAssignGrouperPrivilegesResults.getResponseMetadata().warnings());
    
    GrouperSession session = null;
    String theSummary = null;
    
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsSubjects: " + GrouperUtil.toStringForLog(wsSubjectLookups, 100)
          + ", group: " +  wsGroupLookup + ", stem: " + wsStemLookup 
          + ", privilege: " + privilegeType.name() + "-" + GrouperUtil.toStringForLog(privilegeNames)
          + ", allowed? " + allowed + ", actAsSubject: "
          + actAsSubjectLookup + ", replaceAllExisting: " + replaceAllExisting
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
        
      final String[] subjectAttributeArray = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

      wsAssignGrouperPrivilegesResults.setSubjectAttributeNames(subjectAttributeArray);
        
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      if (wsGroupLookup != null && wsGroupLookup.hasData() && wsStemLookup != null && wsStemLookup.hasData()) {
        throw new WsInvalidQueryException("Cant pass both group and stem.  Pass one or the other");
      }
      if ((wsGroupLookup == null || !wsGroupLookup.hasData()) && (wsStemLookup == null || !wsStemLookup.hasData())) {
        throw new WsInvalidQueryException("Cant pass neither group nor stem.  Pass one or the other");
      }
      if (GrouperUtil.length(privilegeNames) == 0) {
        throw new WsInvalidQueryException("Need to pass in a privilege name");
      }
      
      
      if (txType == null) {
        txType = GrouperTransactionType.NONE;
      }
      
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
    
      final GrouperSession SESSION = session;
      final GrouperTransactionType TX_TYPE = txType;
      
      final String THE_SUMMARY = theSummary;
      
      final List<WsAssignGrouperPrivilegesResult> wsAssignGrouperPrivilegesResultList = new ArrayList<WsAssignGrouperPrivilegesResult>();
      
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              Group group = null;
              
              //see if group and retrieve
              if (wsGroupLookup != null && wsGroupLookup.hasData()) {
                
                if (!privilegeType.equals(PrivilegeType.ACCESS)) {
                  throw new WsInvalidQueryException("If you are querying a group, you need to pass in an " +
                      "access privilege type: '" + privilegeType + "'");
                }
      
                group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
                
                wsAssignGrouperPrivilegesResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
              }
              
              Stem stem = null;
              
              //see if stem and retrieve
              if (wsStemLookup != null && wsStemLookup.hasData()) {
                  
                wsStemLookup.retrieveStemIfNeeded(SESSION, true);
                stem = wsStemLookup.retrieveStem();
                if (stem != null) {
                  wsAssignGrouperPrivilegesResults.setWsStem(new WsStem(stem));
                } else {
                  wsAssignGrouperPrivilegesResults.setWsStem(new WsStem(wsStemLookup));
                }
              }
              
              //loop through all the privileges
              for (Privilege privilege : privilegeNames) {
                
                if (privilege == null) {
                  throw new WsInvalidQueryException("privilege cannot be null");
                }
                
                //get existing members if replacing
                Map<MultiKey, Subject> existingSubjectMap = null;
                if (replaceAllExisting) {
                  existingSubjectMap = new HashMap<MultiKey, Subject>();
                  Set<Subject> subjects = null;
                  try {
                    
                    if (group != null) {
                      
                      subjects = GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(group, privilege);
                    } else if (stem != null) {
                      subjects = GrouperSession.staticGrouperSession().getNamingResolver().getSubjectsWithPrivilege(stem, privilege);
                    } 
                    //add to map, note, might not be revokable
                    for (Subject subject : GrouperUtil.nonNull(subjects)) {
                      existingSubjectMap.put(SubjectHelper.convertToMultiKey(subject), subject);
                    }

                  } catch (SchemaException se) {
                    throw new WsInvalidQueryException(
                        "Problem with getting existing subjects", se);
                  }
                }

                Set<MultiKey> newSubjects = replaceAllExisting ? new HashSet<MultiKey>() : null;

                for (WsSubjectLookup subjectLookup : wsSubjectLookups) {
                
                  Subject subject = subjectLookup.retrieveSubject();
                  
                  WsAssignGrouperPrivilegesResult wsAssignGrouperPrivilegesResult = new WsAssignGrouperPrivilegesResult();
                  wsAssignGrouperPrivilegesResultList.add(wsAssignGrouperPrivilegesResult);
                  wsAssignGrouperPrivilegesResult.processSubject(subjectLookup, subjectAttributeArray);
            
                  //need to check to see status
            
                  if (subject != null) {
            
                    // keep track
                    if (replaceAllExisting) {
                      newSubjects.add(SubjectHelper.convertToMultiKey(subject));
                    }

                    boolean privilegeDidntAlreadyExist = false;
                    boolean privilegeStillExists = false;
                    
                    //handle group privileges
                    if (group != null) {
                                            
                      if (allowed) {
                        privilegeDidntAlreadyExist = group.grantPriv(subject, privilege, false);
                      } else {
                        privilegeDidntAlreadyExist = group.revokePriv(subject, privilege, false);
                        Set<AccessPrivilege> privileges = group.getPrivs(subject);
                        
                        for (AccessPrivilege accessPrivilege : GrouperUtil.nonNull(privileges)) {
                          if (StringUtils.equals(accessPrivilege.getName(), privilege.getName())) {
                            privilegeStillExists = true;
                          }
                        }
                      }
                      
                    } else if (stem != null) {
            
                      if (allowed) {
                        privilegeDidntAlreadyExist = stem.grantPriv(subject, privilege, false);
                      } else {
                        privilegeDidntAlreadyExist = stem.revokePriv(subject, privilege, false);
                        Set<NamingPrivilege> privileges = stem.getPrivs(subject);
                        
                        for (NamingPrivilege namingPrivilege : GrouperUtil.nonNull(privileges)) {
                          if (StringUtils.equals(namingPrivilege.getName(), privilege.getName())) {
                            privilegeStillExists = true;
                          }
                        }
                      }
                      
                    }
                    
                    String thePrivilegeName = privilege.getName();
                    wsAssignGrouperPrivilegesResult.setPrivilegeName(thePrivilegeName);
                    wsAssignGrouperPrivilegesResult.setPrivilegeType(privilegeType.getPrivilegeName());
                    
                    wsAssignGrouperPrivilegesResult.setWsSubject(new WsSubject(subject, subjectAttributeArray, subjectLookup));
                      
                    //assign one of 6 success codes
                    //setup the resultcode
                    if (allowed) {
                      if (!privilegeDidntAlreadyExist) {
                        wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_ALLOWED_ALREADY_EXISTED);
                      } else {
                        wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_ALLOWED);
                      }
                    } else {
                      if (!privilegeDidntAlreadyExist) {
                        if (privilegeStillExists) {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED_DIDNT_EXIST_BUT_EXISTS_EFFECTIVE);
                        } else {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED_DIDNT_EXIST);
                        }
                      } else {
                        if (privilegeStillExists) {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED_EXISTS_EFFECTIVE);
                        } else {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED);
                        }
                      }
                    }
                  }
                }
                
                //remove ones not added
                if (replaceAllExisting) {
                  for (MultiKey subjectKey : existingSubjectMap.keySet()) {
                    if (newSubjects.contains(subjectKey)) {
                      continue;
                    }
                    Subject subject = existingSubjectMap.get(subjectKey);
                    try {
                      //note, no exception if already revoked since might not be immediate
                      if (group != null) {
                        group.revokePriv(subject, privilege, false);
                      } else if (stem != null) {
                        stem.revokePriv(subject, privilege, false);
                      }
                    } catch (Exception e) {
                      String theError = "Error removing subject: " + subject
                          + " owner: " + (group == null ? stem : group) + ", privilege: "
                          + privilege + ", " + e + ".  ";
                      wsAssignGrouperPrivilegesResults.assignResultCodeException(
                          WsAssignGrouperPrivilegesResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
                    }

                  }
                  
                }
                
              }
              //assign results
              wsAssignGrouperPrivilegesResults.setResults(GrouperUtil.toArray(wsAssignGrouperPrivilegesResultList, WsAssignGrouperPrivilegesResult.class));
              
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsAssignGrouperPrivilegesResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }

              return null;
            }
      });
            
    } catch (InsufficientPrivilegeException ipe) {
      wsAssignGrouperPrivilegesResults
          .assignResultCode(WsAssignGrouperPrivilegesResults.WsAssignGrouperPrivilegesResultsCode.INSUFFICIENT_PRIVILEGES);
    } catch (Exception e) {
      wsAssignGrouperPrivilegesResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsAssignGrouperPrivilegesResults;
  }
  
  /**
   * get attributeAssignments from groups etc based on inputs
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, NOT: group_asgn, NOT: mem_asgn, 
   * NOT: stem_asgn, NOT: any_mem_asgn, NOT: imm_mem_asgn, NOT: attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeAssignLookups if you know the assign ids you want, put them here
   * @param wsOwnerGroupLookups are groups to look in
   * @param wsOwnerSubjectLookups are subjects to look in
   * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
   * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)
   * @param wsOwnerStemLookups are stems to look in
   * @param wsOwnerMembershipLookups to query attributes on immediate memberships
   * @param wsOwnerMembershipAnyLookups to query attributes in "any" memberships which are on immediate or effective memberships
   * @param wsOwnerAttributeDefLookups to query attributes assigned on attribute defs
   * @param actions to query, or none to query all actions
   * @param includeAssignmentsOnAssignments if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @param attributeDefValueType required if sending theValue, can be:
   * floating, integer, memberId, string, timestamp
   * @param theValue value if you are passing in one attributeDefNameLookup
   * @param includeAssignmentsFromAssignments T|F if you are finding an assignment that is an assignmentOnAssignment,
   * then get the assignment which tells you the owner as well
   * @param attributeDefType null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
   * @param wsAssignAssignOwnerAttributeAssignLookups if looking for assignments on assignments, this is the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerAttributeDefLookups if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerAttributeDefNameLookups if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerActions if looking for assignments on assignments, this are the actions of the assignment the assignment is assigned to
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetAttributeAssignmentsResults getAttributeAssignments(
      final GrouperVersion clientVersion, AttributeAssignType attributeAssignType,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsAttributeDefLookup[] wsAttributeDefLookups, WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups, WsSubjectLookup[] wsOwnerSubjectLookups, 
      WsMembershipLookup[] wsOwnerMembershipLookups, WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups, 
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups, 
      String[] actions, 
      boolean includeAssignmentsOnAssignments, WsSubjectLookup actAsSubjectLookup, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params, 
      String enabled, AttributeDefValueType attributeDefValueType, Object theValue, boolean includeAssignmentsFromAssignments, 
      AttributeDefType attributeDefType, WsAttributeAssignLookup[] wsAssignAssignOwnerAttributeAssignLookups,
      WsAttributeDefLookup[] wsAssignAssignOwnerAttributeDefLookups, 
      WsAttributeDefNameLookup[] wsAssignAssignOwnerAttributeDefNameLookups,
      String[] wsAssignAssignOwnerActions) {  

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetAttributeAssignmentsResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion+ ", attributeAssignType: " + attributeAssignType 
          + ", wsAttributeDefLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefLookups, 200) 
          + ", wsAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsAttributeAssignLookups, 200)
          + ", wsAttributeDefNameLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameLookups, 200) + ", wsOwnerStemLookups: "
          + GrouperUtil.toStringForLog(wsOwnerStemLookups, 200) + ", wsOwnerGroupLookups: "
          + GrouperUtil.toStringForLog(wsOwnerGroupLookups, 200) + ", wsOwnerMembershipLookups: "
          + GrouperUtil.toStringForLog(wsOwnerMembershipLookups, 200) 
          + ", wsOwnerMembershipAnyLookups: " + GrouperUtil.toStringForLog(wsOwnerMembershipAnyLookups, 200)
          + ", wsOwnerAttributeDefLookups: " + GrouperUtil.toStringForLog(wsOwnerAttributeDefLookups, 200)
          + ", actions: " + GrouperUtil.toStringForLog(actions, 200)
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsOwnerSubjectLookups: "
          + GrouperUtil.toStringForLog(wsOwnerSubjectLookups, 200) 
          + ", enabled: " + enabled + ", attributeDefValueType: " + attributeDefValueType
          + ", theValue: " + theValue + ", includeAssignmentsFromAssignments: " + includeAssignmentsFromAssignments
          + ", attributeDefType: " + attributeDefType + ", wsAssignAssignOwnerAttributeAssignLookups: "
          + GrouperUtil.toStringForLog(wsAssignAssignOwnerAttributeAssignLookups, 200)
          + ", wsAssignAssignOwnerAttributeDefLookups: " + GrouperUtil.toStringForLog(wsAssignAssignOwnerAttributeDefLookups, 200)
          + ", wsAssignAssignOwnerAttributeDefNameLookups: " + GrouperUtil.toStringForLog(wsAssignAssignOwnerAttributeDefNameLookups, 200)
          + ", wsAssignAssignOwnerActions: " + GrouperUtil.toStringForLog(wsAssignAssignOwnerActions, 200);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsGetAttributeAssignmentsResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);


      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
      
      //this is for error checking
      
      int[] lookupCount = new int[]{0};

      StringBuilder errorMessage = new StringBuilder();

      if (attributeAssignType == null) {
        throw new WsInvalidQueryException("You need to pass in an attributeAssignType");
      }
      
      //if true, return no results
      boolean notFound = false;
      
      //get the attributeAssignids to retrieve
      Set<String> attributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsAttributeAssignLookups, errorMessage);
      
      if (!GrouperServiceUtils.nullArray(wsAttributeAssignLookups) && GrouperUtil.length(attributeAssignIds) == 0) {
        notFound = true;
      }
      
      //get the attributedefs to retrieve
      Set<String> attributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsAttributeDefLookups, errorMessage, null, false, null, null);

      if (!GrouperServiceUtils.nullArray(wsAttributeDefLookups) && GrouperUtil.length(attributeDefIds) == 0) {
        notFound = true;
      }
      
      //get the attributeDefNames to retrieve
      Set<String> attributeDefNameIds = WsAttributeDefNameLookup.convertToAttributeDefNameIds(session, wsAttributeDefNameLookups, errorMessage, null, false, null, null);
      
      if (!GrouperServiceUtils.nullArray(wsAttributeDefNameLookups) && GrouperUtil.length(attributeDefNameIds) == 0) {
        notFound = true;
      }

      //get all the owner groups
      Set<String> ownerGroupIds = WsGroupLookup.convertToGroupIds(session, wsOwnerGroupLookups, errorMessage, null, false, null, null, lookupCount);
      
      if (!GrouperServiceUtils.nullArray(wsOwnerGroupLookups) && GrouperUtil.length(ownerGroupIds) == 0) {
        notFound = true;
      }

      //get all the owner stems
      Set<String> ownerStemIds = WsStemLookup.convertToStemIds(session, wsOwnerStemLookups, errorMessage, lookupCount);
      
      if (!GrouperServiceUtils.nullArray(wsOwnerStemLookups) && GrouperUtil.length(ownerStemIds) == 0) {
        notFound = true;
      }

      //get all the owner member ids
      Set<String> ownerMemberIds = WsSubjectLookup.convertToMemberIds(session, wsOwnerSubjectLookups, errorMessage, lookupCount);
      
      if (!GrouperServiceUtils.nullArray(wsOwnerSubjectLookups) && GrouperUtil.length(ownerMemberIds) == 0) {
        notFound = true;
      }

      //get all the owner membership ids
      Set<String> ownerMembershipIds = WsMembershipLookup.convertToMembershipIds(session, wsOwnerMembershipLookups, errorMessage, lookupCount);
      
      if (!GrouperServiceUtils.nullArray(wsOwnerMembershipLookups) && GrouperUtil.length(ownerMembershipIds) == 0) {
        notFound = true;
      }

      //get all the owner membership any ids
      Set<MultiKey> ownerGroupMemberIds = WsMembershipAnyLookup.convertToGroupMemberIds(session, wsOwnerMembershipAnyLookups, errorMessage, null, lookupCount);
      
      if (!GrouperServiceUtils.nullArray(wsOwnerMembershipAnyLookups) && GrouperUtil.length(ownerGroupMemberIds) == 0) {
        notFound = true;
      }

      //get all the owner attributeDef ids
      Set<String> ownerAttributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsOwnerAttributeDefLookups, errorMessage, null, false, null, null, lookupCount);

      if (!GrouperServiceUtils.nullArray(wsOwnerAttributeDefLookups) && GrouperUtil.length(ownerAttributeDefIds) == 0) {
        notFound = true;
      }

      //get owner attribute assign ids
      Set<String> ownerAttributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsAssignAssignOwnerAttributeAssignLookups, errorMessage, lookupCount);

      if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerAttributeAssignLookups) && GrouperUtil.length(ownerAttributeAssignIds) == 0) {
        notFound = true;
      }

      //get the attributedefs to retrieve
      Set<String> assignAssignOwnerAttributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsAssignAssignOwnerAttributeDefLookups, errorMessage, null, false, null, null);
      
      if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerAttributeDefLookups) && GrouperUtil.length(assignAssignOwnerAttributeDefIds) == 0) {
        notFound = true;
      }

      //get the attributeDefNames to retrieve
      Set<String> assignAssignOwnerAttributeDefNameIds = WsAttributeDefNameLookup.convertToAttributeDefNameIds(session, wsAssignAssignOwnerAttributeDefNameLookups, errorMessage, null, false, null, null);
      
      if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerAttributeDefNameLookups) && GrouperUtil.length(assignAssignOwnerAttributeDefNameIds) == 0) {
        notFound = true;
      }

      if (lookupCount[0] > 1) {
        throw new WsInvalidQueryException("Why is there more than one type of lookup?  ");
      }
      
      Set<AttributeAssign> results = null;
      
      Boolean enabledBoolean = true;
      if (!StringUtils.isBlank(enabled)) {
        if (StringUtils.equalsIgnoreCase("A", enabled)) {
          enabledBoolean = null;
        } else {
          enabledBoolean = GrouperUtil.booleanValue(enabled);
        }
      }
      
      Collection<String> actionsCollection = GrouperUtil.toSet(actions);
      
      if (actionsCollection == null || actionsCollection.size() == 0 
          || (actionsCollection.size() == 1 && StringUtils.isBlank(actionsCollection.iterator().next()))) {
        actionsCollection = null;
      }
      
      Collection<String> ownerActionsCollection = GrouperUtil.toSet(wsAssignAssignOwnerActions);
      
      if (ownerActionsCollection == null || ownerActionsCollection.size() == 0 
          || (ownerActionsCollection.size() == 1 && StringUtils.isBlank(ownerActionsCollection.iterator().next()))) {
        ownerActionsCollection = null;
      }
      
      switch(attributeAssignType) {
        case group:
        case stem:
        case member:
        case imm_mem:
        case any_mem:
        case attr_def:
          if (includeAssignmentsFromAssignments) {
            throw new WsInvalidQueryException("Only assignment on assignment queries can include includeAssignmentsFromAssignments.  ");
          }
          if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerAttributeAssignLookups)) {
            throw new WsInvalidQueryException("Only assignment on assignment queries can include wsAssignAssignOwnerAttributeAssignLookups.  ");
          }
          if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerAttributeDefLookups)) {
            throw new WsInvalidQueryException("Only assignment on assignment queries can include wsAssignAssignOwnerAttributeDefLookups.  ");
          }
          if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerAttributeDefNameLookups)) {
            throw new WsInvalidQueryException("Only assignment on assignment queries can include wsAssignAssignOwnerAttributeDefNameLookups.  ");
          }
          if (!GrouperServiceUtils.nullArray(wsAssignAssignOwnerActions)) {
            throw new WsInvalidQueryException("Only assignment on assignment queries can include wsAssignAssignOwnerActions.  ");
          }
          
          break;
        case group_asgn:
        case stem_asgn:
        case mem_asgn:
        case any_mem_asgn:
        case attr_def_asgn:
        case imm_mem_asgn:
          if (includeAssignmentsOnAssignments) {
            throw new WsInvalidQueryException("Only non assignment on assignment queries can include includeAssignmentsOnAssignments.  ");
          }
          
          break;
        default: 
          throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
      }
      
      //if couldnt find one of the lookups...
      if (notFound) {
        results = new HashSet<AttributeAssign>();
      } else {

        switch(attributeAssignType) {
          case group:
            
            //if there is a lookup and its not about groups, then there is a problem
            if (lookupCount[0] > 0 && GrouperUtil.length(wsOwnerGroupLookups) == 0) {
              throw new WsInvalidQueryException("Group calls can only have group owner lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerGroupIds, actionsCollection, 
                enabledBoolean, includeAssignmentsOnAssignments, attributeDefType, attributeDefValueType, 
                theValue);
            
            break;  
          case stem:
            
            //if there is a lookup and its not about stems, then there is a problem
            if (lookupCount[0] > 0 && GrouperUtil.length(wsOwnerStemLookups) == 0) {
              throw new WsInvalidQueryException("Stem calls can only have stem owner lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerStemIds, actionsCollection, 
                enabledBoolean, includeAssignmentsOnAssignments, attributeDefType, attributeDefValueType, theValue);
            
            break;  
          case member:
            
            //if there is a lookup and its not about subjects, then there is a problem
            if (lookupCount[0] > 0 && GrouperUtil.length(wsOwnerSubjectLookups) == 0) {
              throw new WsInvalidQueryException("Subject calls can only have subject owner lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerMemberIds, actionsCollection, 
                enabledBoolean, includeAssignmentsOnAssignments, attributeDefType, attributeDefValueType, theValue);
            
            break;  
          case imm_mem:
            
            //if there is a lookup and its not about memberships, then there is a problem
            if (lookupCount[0] > 0 && GrouperUtil.length(wsOwnerMembershipLookups) == 0) {
              throw new WsInvalidQueryException("Membership calls can only have membership owner lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerMembershipIds, actionsCollection, 
                enabledBoolean, includeAssignmentsOnAssignments, attributeDefType, attributeDefValueType, theValue);
            
            break;  
          case any_mem:
            
            //if there is a lookup and its not about memberships, then there is a problem
            if (lookupCount[0] > 0 && GrouperUtil.length(wsOwnerMembershipAnyLookups) == 0) {
              throw new WsInvalidQueryException("MembershipAny calls can only have membershipAny owner lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerGroupMemberIds, actionsCollection, 
                enabledBoolean, includeAssignmentsOnAssignments, attributeDefType, attributeDefValueType, 
                theValue);
            
            break;  
          case attr_def:
            
            //if there is a lookup and its not about attr def, then there is a problem
            if (lookupCount[0] > 0 && GrouperUtil.length(wsOwnerAttributeDefLookups) == 0) {
              throw new WsInvalidQueryException("attributeDef calls can only have attributeDef owner lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerAttributeDefIds, actionsCollection, 
                enabledBoolean, includeAssignmentsOnAssignments, attributeDefType, attributeDefValueType, theValue);
            
            break;  
          case group_asgn:
            
            //if there is a lookup and its not about group or assignment, then there is a problem
            if (lookupCount[0] != (GrouperUtil.length(ownerAttributeAssignIds) > 0 ? 1 : 0) + (GrouperUtil.length(ownerGroupIds) > 0 ? 1 : 0)) {
              throw new WsInvalidQueryException("group_asgn calls can only have attribute assign owner lookups and/or group lookups.  ");
            }
            
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerGroupIds, actionsCollection, 
                enabledBoolean, attributeDefType, attributeDefValueType, theValue, includeAssignmentsFromAssignments, 
                ownerAttributeAssignIds, assignAssignOwnerAttributeDefIds, assignAssignOwnerAttributeDefNameIds, ownerActionsCollection, true);
  
            break;
            
          case stem_asgn:
            
            //if there is a lookup and its not about attr def, then there is a problem
            if (lookupCount[0] != (GrouperUtil.length(ownerAttributeAssignIds) > 0 ? 1 : 0) + (GrouperUtil.length(ownerStemIds) > 0 ? 1 : 0)) {
              throw new WsInvalidQueryException("stem_asgn calls can only have attribute assign owner lookups and/or stem lookups.  ");
            }
  
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignmentsOnAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerStemIds, actionsCollection, 
                enabledBoolean, attributeDefType, attributeDefValueType, theValue, includeAssignmentsFromAssignments, 
                ownerAttributeAssignIds, assignAssignOwnerAttributeDefIds, assignAssignOwnerAttributeDefNameIds, ownerActionsCollection, true);
  
            break;
            
          case mem_asgn:
            
            //if there is a lookup and its not about attr def, then there is a problem
            if (lookupCount[0] != (GrouperUtil.length(ownerAttributeAssignIds) > 0 ? 1 : 0) + (GrouperUtil.length(ownerMemberIds) > 0 ? 1 : 0)) {
              throw new WsInvalidQueryException("mem_asgn calls can only have attribute assign owner lookups and/or member lookups.  ");
            }
  
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerMemberIds, actionsCollection, 
                enabledBoolean, attributeDefType, attributeDefValueType, theValue, includeAssignmentsFromAssignments, 
                ownerAttributeAssignIds, assignAssignOwnerAttributeDefIds, assignAssignOwnerAttributeDefNameIds, ownerActionsCollection, true);
  
            break;
            
          case any_mem_asgn:
            
            //if there is a lookup and its not about attr def, then there is a problem
            if (lookupCount[0] != (GrouperUtil.length(ownerAttributeAssignIds) > 0 ? 1 : 0) + (GrouperUtil.length(ownerGroupMemberIds) > 0 ? 1 : 0)) {
              throw new WsInvalidQueryException("any_mem_asgn calls can only have attribute assign owner lookups and/or any_mem lookups.  ");
            }
  
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerGroupMemberIds, actionsCollection, 
                enabledBoolean, attributeDefType, attributeDefValueType, theValue, includeAssignmentsFromAssignments, 
                ownerAttributeAssignIds, assignAssignOwnerAttributeDefIds, assignAssignOwnerAttributeDefNameIds, ownerActionsCollection, true);
  
            break;
            
          case attr_def_asgn:
            
            //if there is a lookup and its not about attr def, then there is a problem
            if (lookupCount[0] != (GrouperUtil.length(ownerAttributeAssignIds) > 0 ? 1 : 0) + (GrouperUtil.length(ownerAttributeDefIds) > 0 ? 1 : 0)) {
              throw new WsInvalidQueryException("attr_def_asgn calls can only have attribute assign owner lookups and/or owner attribute def lookups.  ");
            }
  
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerAttributeDefIds, actionsCollection, 
                enabledBoolean, attributeDefType, attributeDefValueType, theValue, includeAssignmentsFromAssignments, 
                ownerAttributeAssignIds, assignAssignOwnerAttributeDefIds, assignAssignOwnerAttributeDefNameIds, ownerActionsCollection, true);
  
            break;
            
          case imm_mem_asgn:
            
            //if there is a lookup and its not about attr def, then there is a problem
            if (lookupCount[0] != (GrouperUtil.length(ownerAttributeAssignIds) > 0 ? 1 : 0) + (GrouperUtil.length(ownerMembershipIds) > 0 ? 1 : 0)) {
              throw new WsInvalidQueryException("imm_mem_asgn calls can only have attribute assign owner lookups and/or owner immediate membership lookups.  ");
            }
  
            results = GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(
                attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerMembershipIds, actionsCollection, 
                enabledBoolean, attributeDefType, attributeDefValueType, theValue, includeAssignmentsFromAssignments, 
                ownerAttributeAssignIds, assignAssignOwnerAttributeDefIds, assignAssignOwnerAttributeDefNameIds, ownerActionsCollection, true);
  
            break;
            
          default: 
            throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
        }
      }
      
      wsGetAttributeAssignmentsResults.assignResult(results, subjectAttributeNames);
      
      wsGetAttributeAssignmentsResults.fillInAttributeDefNames(attributeDefNameIds);
      wsGetAttributeAssignmentsResults.fillInAttributeDefs(attributeDefIds);
      
      Set<String> allGroupIds = new HashSet<String>(GrouperUtil.nonNull(ownerGroupIds));
      Set<String> extraMemberIds = new HashSet<String>();
      for (MultiKey multiKey : GrouperUtil.nonNull(ownerGroupMemberIds)) {
        allGroupIds.add((String)multiKey.getKey(0));
        extraMemberIds.add((String)multiKey.getKey(1));
      }
      
      
      wsGetAttributeAssignmentsResults.fillInGroups(ownerGroupIds, includeGroupDetail);
      wsGetAttributeAssignmentsResults.fillInStems(ownerStemIds);
      wsGetAttributeAssignmentsResults.fillInSubjects(wsOwnerSubjectLookups, extraMemberIds, 
          includeSubjectDetail, subjectAttributeNamesToRetrieve);
      wsGetAttributeAssignmentsResults.fillInMemberships(ownerMembershipIds);
      
      //sort after all the data is there
      wsGetAttributeAssignmentsResults.sortResults();
      
      if (errorMessage.length() > 0) {
        wsGetAttributeAssignmentsResults.assignResultCode(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY);
        wsGetAttributeAssignmentsResults.getResultMetadata().appendResultMessage(errorMessage.toString());
      } else {
        wsGetAttributeAssignmentsResults.assignResultCode(WsGetAttributeAssignmentsResultsCode.SUCCESS);
      }
      
      wsGetAttributeAssignmentsResults.getResultMetadata().appendResultMessage(
          ", Found " + GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns())
          + " results.  ");

        
    } catch (Exception e) {
      wsGetAttributeAssignmentsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetAttributeAssignmentsResults; 
  }

    
  /**
   * get attributeAssignments from group and or subject based on inputs
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, NOT: group_asgn, NOT: mem_asgn, 
   * NOT: stem_asgn, NOT: any_mem_asgn, NOT: imm_mem_asgn, NOT: attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeAssignId if you know the assign id you want, put it here
   * @param wsAttributeDefName find assignments in this attribute def (optional)
   * @param wsAttributeDefId find assignments in this attribute def (optional)
   * @param wsAttributeDefNameName find assignments in this attribute def name (optional)
   * @param wsAttributeDefNameId find assignments in this attribute def name (optional)
   * @param wsOwnerGroupName is group name to look in
   * @param wsOwnerGroupId is group id to look in
   * @param wsOwnerStemName is stem to look in
   * @param wsOwnerStemId is stem to look in
   * @param wsOwnerSubjectId is subject to look in
   * @param wsOwnerSubjectSourceId is subject to look in
   * @param wsOwnerSubjectIdentifier is subject to look in
   * @param wsOwnerMembershipId to query attributes on immediate membership
   * @param wsOwnerMembershipAnyGroupName to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupId  to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectId to query attributes in "any" membership which is on immediate or effective membership 
   * @param wsOwnerMembershipAnySubjectSourceId to query attributes in "any" membership which is on immediate or effective membership 
   * @param wsOwnerMembershipAnySubjectIdentifier to query attributes in "any" membership which is on immediate or effective membership 
   * @param wsOwnerAttributeDefName to query attributes assigned on attribute def
   * @param wsOwnerAttributeDefId to query attributes assigned on attribute def
   * @param action to query, or none to query all actions
   * @param includeAssignmentsOnAssignments if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param actAsSubjectId act as this subject
   * @param actAsSubjectSourceId act as this subject
   * @param actAsSubjectIdentifier act as this subject
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return (comma separated)
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @param attributeDefValueType required if sending theValue, can be:
   * floating, integer, memberId, string, timestamp
   * @param theValue value if you are passing in one attributeDefNameLookup
   * @param includeAssignmentsFromAssignments T|F if you are finding an assignment that is an assignmentOnAssignment,
   * then get the assignment which tells you the owner as well
   * @param attributeDefType null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm
   * @param wsAssignAssignOwnerAttributeAssignId if looking for assignments on assignments, this is the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerIdOfAttributeDef if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerNameOfAttributeDef if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerIdOfAttributeDefName if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerNameOfAttributeDefName if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to
   * @param wsAssignAssignOwnerAction if looking for assignments on assignments, this is the action of the assignment the assignment is assigned to
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetAttributeAssignmentsResults getAttributeAssignmentsLite(
      final GrouperVersion clientVersion, AttributeAssignType attributeAssignType,
      String attributeAssignId,
      String wsAttributeDefName, String wsAttributeDefId, String wsAttributeDefNameName, String wsAttributeDefNameId,
      String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, 
      String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier,
      String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId,
      String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier, 
      String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, 
      String action, 
      boolean includeAssignmentsOnAssignments, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeSubjectDetail,
      String subjectAttributeNames, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, 
      String enabled, AttributeDefValueType attributeDefValueType, Object theValue, boolean includeAssignmentsFromAssignments, 
      AttributeDefType attributeDefType, String wsAssignAssignOwnerAttributeAssignId, 
      String wsAssignAssignOwnerIdOfAttributeDef, String wsAssignAssignOwnerNameOfAttributeDef,
      String wsAssignAssignOwnerIdOfAttributeDefName, String wsAssignAssignOwnerNameOfAttributeDefName, String wsAssignAssignOwnerAction) {  

    WsAttributeAssignLookup[] attributeAssignLookups = null;
    
    if (!StringUtils.isBlank(attributeAssignId)) {
      attributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId)};
    }
    
    WsAttributeDefLookup[] wsAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefName) || !StringUtils.isBlank(wsAttributeDefId)) {
      wsAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsAttributeDefName, wsAttributeDefId)};
    }
    
    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefNameName) || !StringUtils.isBlank(wsAttributeDefNameId)) {
      wsAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId )};
    }
    
    WsGroupLookup[] wsOwnerGroupLookups = null;
    if (!StringUtils.isBlank(wsOwnerGroupName) || !StringUtils.isBlank(wsOwnerGroupId)) {
      wsOwnerGroupLookups = new WsGroupLookup[]{new WsGroupLookup(wsOwnerGroupName, wsOwnerGroupId)};
    }
    
    WsStemLookup[] wsOwnerStemLookups = null;
    if (!StringUtils.isBlank(wsOwnerStemName) || !StringUtils.isBlank(wsOwnerStemId)) {
      wsOwnerStemLookups = new WsStemLookup[]{new WsStemLookup(wsOwnerStemName, wsOwnerStemId)};
    }
    
    WsSubjectLookup[] wsOwnerSubjectLookups = null;
    if (!StringUtils.isBlank(wsOwnerSubjectId) || !StringUtils.isBlank(wsOwnerSubjectSourceId) || !StringUtils.isBlank(wsOwnerSubjectIdentifier)) {
      wsOwnerSubjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(wsOwnerSubjectId, wsOwnerSubjectSourceId, wsOwnerSubjectIdentifier)};
    }
    
    WsMembershipLookup[] wsOwnerMembershipLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipId)) {
      wsOwnerMembershipLookups = new WsMembershipLookup[]{new WsMembershipLookup(wsOwnerMembershipId)};
    }
    
    WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipAnyGroupName) || !StringUtils.isBlank(wsOwnerMembershipAnyGroupId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectId) || !StringUtils.isBlank(wsOwnerMembershipAnySubjectSourceId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectIdentifier)) {
      wsOwnerMembershipAnyLookups = new WsMembershipAnyLookup[]{
          new WsMembershipAnyLookup(new WsGroupLookup(wsOwnerMembershipAnyGroupName,wsOwnerMembershipAnyGroupId ),
              new WsSubjectLookup(wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, wsOwnerMembershipAnySubjectIdentifier))};
    }
    
    WsAttributeDefLookup[] wsOwnerAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsOwnerAttributeDefName) || !StringUtils.isBlank(wsOwnerAttributeDefId)) {
      wsOwnerAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsOwnerAttributeDefName, wsOwnerAttributeDefId)}; 
    }
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
    
    String[] actions = null;
    if (!StringUtils.isBlank(action)) {
      actions = new String[]{action};
    }
    
    WsAttributeAssignLookup[] ownerAttributeAssignLookups = null;
    
    if (!StringUtils.isBlank(wsAssignAssignOwnerAttributeAssignId)) {
      ownerAttributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(wsAssignAssignOwnerAttributeAssignId)};
    }
    
    WsAttributeDefLookup[] wsAssignAssignOwnerAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsAssignAssignOwnerNameOfAttributeDef) || !StringUtils.isBlank(wsAssignAssignOwnerIdOfAttributeDef)) {
      wsAssignAssignOwnerAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsAssignAssignOwnerNameOfAttributeDef, wsAssignAssignOwnerIdOfAttributeDef)};
    }
    
    WsAttributeDefNameLookup[] wsAssignAssignOwnerAttributeDefNameLookups = null;
    if (!StringUtils.isBlank(wsAssignAssignOwnerNameOfAttributeDefName) || !StringUtils.isBlank(wsAssignAssignOwnerIdOfAttributeDefName)) {
      wsAssignAssignOwnerAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(wsAssignAssignOwnerNameOfAttributeDefName,wsAssignAssignOwnerIdOfAttributeDefName )};
    }
    
    String[] ownerActions = null;
    if (!StringUtils.isBlank(wsAssignAssignOwnerAction)) {
      ownerActions = new String[]{wsAssignAssignOwnerAction};
    }
    

    
    
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = getAttributeAssignments(clientVersion, attributeAssignType, 
        attributeAssignLookups, wsAttributeDefLookups, wsAttributeDefNameLookups, wsOwnerGroupLookups, wsOwnerStemLookups, 
        wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups, wsOwnerAttributeDefLookups, actions, 
        includeAssignmentsOnAssignments, actAsSubjectLookup, includeSubjectDetail, subjectAttributeArray, includeGroupDetail, 
        params, enabled, attributeDefValueType, theValue,  includeAssignmentsFromAssignments, attributeDefType, 
        ownerAttributeAssignLookups, wsAssignAssignOwnerAttributeDefLookups, wsAssignAssignOwnerAttributeDefNameLookups, ownerActions);
    
    return wsGetAttributeAssignmentsResults; 
  
  }

  /**
   * assign attributes and values to owner objects (groups, stems, etc)
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeAssignLookups if you know the assign ids you want, put them here
   * @param wsOwnerGroupLookups are groups to look in
   * @param wsOwnerSubjectLookups are subjects to look in
   * @param wsAttributeDefNameLookups attribute def names to assign to the owners
   * @param attributeAssignOperation operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param values are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   * @param assignmentNotes notes on the assignment (optional)
   * @param assignmentEnabledTime enabled time, or null for enabled now
   * @param assignmentDisabledTime disabled time, or null for not disabled
   * @param delegatable really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param attributeAssignValueOperation operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param wsOwnerStemLookups are stems to look in
   * @param wsOwnerMembershipLookups to query attributes on immediate memberships
   * @param wsOwnerMembershipAnyLookups to query attributes in "any" memberships which are on immediate or effective memberships
   * @param wsOwnerAttributeDefLookups to query attributes assigned on attribute defs
   * @param wsOwnerAttributeAssignLookups for assignment on assignment
   * @param actions to assign, or "assign" is the default if blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param wsAttributeAssignLookups lookups to remove etc
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param attributeDefsToReplace if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @param actionsToReplace if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   * @param attributeDefTypesToReplace if replacing attributeDefNames, then these are the
   * related attributeDefTypes, if blank, then just do all
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignAttributesResults assignAttributes(
      final GrouperVersion clientVersion, AttributeAssignType attributeAssignType,
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      AttributeAssignOperation attributeAssignOperation,
      WsAttributeAssignValue[] values,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups, WsSubjectLookup[] wsOwnerSubjectLookups, 
      WsMembershipLookup[] wsOwnerMembershipLookups, WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups, 
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups, WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups,
      String[] actions, WsSubjectLookup actAsSubjectLookup, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params,
      WsAttributeDefLookup[] attributeDefsToReplace, String[] actionsToReplace, String[] attributeDefTypesToReplace) {  

    WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAssignAttributesResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion+ ", attributeAssignType: " + attributeAssignType 
          + ", attributeAssignOperation: " + attributeAssignOperation
          + ", attributeAssignValues: " + GrouperUtil.toStringForLog(values, 200)
          + ", attributeAssignValueOperation: " + attributeAssignValueOperation
          + ", wsOwnerAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsOwnerAttributeAssignLookups, 200)
          + ", wsAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsAttributeAssignLookups, 200)
          + ", wsAttributeDefNameLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameLookups, 200) + ", wsOwnerStemLookups: "
          + GrouperUtil.toStringForLog(wsOwnerStemLookups, 200) + ", wsOwnerGroupLookups: "
          + GrouperUtil.toStringForLog(wsOwnerGroupLookups, 200) + ", wsOwnerMembershipLookups: "
          + GrouperUtil.toStringForLog(wsOwnerMembershipLookups, 200) 
          + ", wsOwnerMembershipAnyLookups: " + GrouperUtil.toStringForLog(wsOwnerMembershipAnyLookups, 200)
          + ", wsOwnerAttributeDefLookups: " + GrouperUtil.toStringForLog(wsOwnerAttributeDefLookups, 200)
          + ", actions: " + GrouperUtil.toStringForLog(actions, 200)
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsOwnerSubjectLookups: "
          + GrouperUtil.toStringForLog(wsOwnerSubjectLookups, 200) 
          + "\n, attributeDefsToReplace: " + GrouperUtil.toStringForLog(attributeDefsToReplace, 200)
          + "\n, actionsToReplace: " + GrouperUtil.toStringForLog(actionsToReplace, 200)
          + "\n, attributeDefTypesToReplace: " + GrouperUtil.toStringForLog(attributeDefTypesToReplace, 200);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      if (attributeAssignType == null) {
        throw new WsInvalidQueryException("You need to pass in an attributeAssignType.  ");
      }
      
      if (attributeAssignOperation == null) {
        throw new WsInvalidQueryException("You need to pass in an attributeAssignOperation.  ");
      }
      
      WsAssignAttributeLogic.assignAttributesHelper(attributeAssignType, wsAttributeDefNameLookups,
          attributeAssignOperation, values, assignmentNotes, assignmentEnabledTime,
          assignmentDisabledTime, delegatable, attributeAssignValueOperation,
          wsAttributeAssignLookups, wsOwnerGroupLookups, wsOwnerStemLookups,
          wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups,
          wsOwnerAttributeDefLookups, wsOwnerAttributeAssignLookups, actions,
          includeSubjectDetail, subjectAttributeNames, includeGroupDetail,
          wsAssignAttributesResults, session, params, null, null, 
          attributeDefsToReplace, actionsToReplace, attributeDefTypesToReplace, false, true, null);

        
    } catch (Exception e) {
      wsAssignAttributesResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsAssignAttributesResults; 
  
  }

  /**
   * assign attributes and values to owner objects (groups, stems, etc)
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeAssignId if you know the assign id you want, put id here
   * @param wsOwnerGroupName is group to look in
   * @param wsOwnerGroupId is group to look in
   * @param wsOwnerSubjectId is subject to look in
   * @param wsOwnerSubjectSourceId is subject to look in
   * @param wsOwnerSubjectIdentifier is subject to look in
   * @param wsAttributeDefNameName attribute def name to assign to the owner
   * @param wsAttributeDefNameId attribute def name to assign to the owner
   * @param attributeAssignOperation operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param valueId If removing, and id is specified, will
   * only remove values with that id.
   * @param valueSystem is value to add, assign, remove, etc
   * @param valueFormatted is value to add, assign, remove, etc though not implemented yet
   * @param assignmentNotes notes on the assignment (optional)
   * @param assignmentEnabledTime enabled time, or null for enabled now
   * @param assignmentDisabledTime disabled time, or null for not disabled
   * @param delegatable really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param attributeAssignValueOperation operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param wsOwnerStemName is stem to look in
   * @param wsOwnerStemId is stem to look in
   * @param wsOwnerMembershipId to query attributes on immediate membership
   * @param wsOwnerMembershipAnyGroupName to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupId to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectId to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectSourceId to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectIdentifier to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerAttributeDefName to query attributes assigned on attribute def
   * @param wsOwnerAttributeDefId to query attributes assigned on attribute def
   * @param wsOwnerAttributeAssignId for assignment on assignment
   * @param action to assign, or "assign" is the default if blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId act as this subject
   * @param actAsSubjectSourceId act as this subject
   * @param actAsSubjectIdentifier act as this subject
   * @param wsAttributeAssignLookups lookups to remove etc
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignAttributesLiteResults assignAttributesLite(
      GrouperVersion clientVersion, AttributeAssignType attributeAssignType,
      String wsAttributeDefNameName, String wsAttributeDefNameId,
      AttributeAssignOperation attributeAssignOperation,
      String valueId, String valueSystem, String valueFormatted,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation,
      String wsAttributeAssignId,
      String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, 
      String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier,
      String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId,
      String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier,
      String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, String wsOwnerAttributeAssignId,
      String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, boolean includeSubjectDetail,
      String subjectAttributeNames, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {  
  
    String[] attributeDefTypesToReplace = null;
    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
    WsAttributeDefLookup[] attributeDefsToReplace = null; 

    if (!StringUtils.isBlank(wsAttributeDefNameName) || !StringUtils.isBlank(wsAttributeDefNameId)) {
      wsAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId )};
      
      attributeDefTypesToReplace = WsAssignAttributeLogic.retrieveAttributeDefTypesForReplace(
          wsAttributeDefNameName, wsAttributeDefNameId, attributeAssignOperation);
      
      attributeDefsToReplace = WsAssignAttributeLogic.retrieveAttributeDefsForReplace(
          wsAttributeDefNameName, wsAttributeDefNameId, attributeAssignOperation);
    }
    
    
    WsAttributeAssignValue[] wsAttributeAssignValues = null;
    if (!StringUtils.isBlank(valueId) || !StringUtils.isBlank(valueSystem) || !StringUtils.isBlank(valueFormatted)) {
      WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
      wsAttributeAssignValue.setId(valueId);
      wsAttributeAssignValue.setValueSystem(valueSystem);
      wsAttributeAssignValue.setValueFormatted(valueFormatted);
      wsAttributeAssignValues = new WsAttributeAssignValue[]{wsAttributeAssignValue};
    }
    
    WsAttributeAssignLookup[] attributeAssignLookups = null;
    
    if (!StringUtils.isBlank(wsAttributeAssignId)) {
      attributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(wsAttributeAssignId)};
    }
    
    WsGroupLookup[] wsOwnerGroupLookups = null;
    if (!StringUtils.isBlank(wsOwnerGroupName) || !StringUtils.isBlank(wsOwnerGroupId)) {
      wsOwnerGroupLookups = new WsGroupLookup[]{new WsGroupLookup(wsOwnerGroupName, wsOwnerGroupId)};
    }
    
    WsStemLookup[] wsOwnerStemLookups = null;
    if (!StringUtils.isBlank(wsOwnerStemName) || !StringUtils.isBlank(wsOwnerStemId)) {
      wsOwnerStemLookups = new WsStemLookup[]{new WsStemLookup(wsOwnerStemName, wsOwnerStemId)};
    }
    
    WsSubjectLookup[] wsOwnerSubjectLookups = null;
    if (!StringUtils.isBlank(wsOwnerSubjectId) || !StringUtils.isBlank(wsOwnerSubjectSourceId) || !StringUtils.isBlank(wsOwnerSubjectIdentifier)) {
      wsOwnerSubjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(wsOwnerSubjectId, wsOwnerSubjectSourceId, wsOwnerSubjectIdentifier)};
    }
    
    WsMembershipLookup[] wsOwnerMembershipLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipId)) {
      wsOwnerMembershipLookups = new WsMembershipLookup[]{new WsMembershipLookup(wsOwnerMembershipId)};
    }
    
    WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipAnyGroupName) || !StringUtils.isBlank(wsOwnerMembershipAnyGroupId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectId) || !StringUtils.isBlank(wsOwnerMembershipAnySubjectSourceId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectIdentifier)) {
      wsOwnerMembershipAnyLookups = new WsMembershipAnyLookup[]{
          new WsMembershipAnyLookup(new WsGroupLookup(wsOwnerMembershipAnyGroupName,wsOwnerMembershipAnyGroupId ),
              new WsSubjectLookup(wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, wsOwnerMembershipAnySubjectIdentifier))};
    }
    
    WsAttributeDefLookup[] wsOwnerAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsOwnerAttributeDefName) || !StringUtils.isBlank(wsOwnerAttributeDefId)) {
      wsOwnerAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsOwnerAttributeDefName, wsOwnerAttributeDefId)}; 
    }
    
    WsAttributeAssignLookup[] ownerAttributeAssignLookups = null;
    if (!StringUtils.isBlank(wsOwnerAttributeAssignId)) {
      ownerAttributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(wsOwnerAttributeAssignId)};
    }
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
    
    String[] actions = null;
    if (!StringUtils.isBlank(action)) {
      actions = new String[]{action};
    }
    
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    String[] actionsToReplace = new String[]{GrouperUtil.defaultIfBlank(action, AttributeDef.ACTION_DEFAULT)};
    
    WsAssignAttributesResults wsAssignAttributesResults = assignAttributes(clientVersion, attributeAssignType, 
        wsAttributeDefNameLookups, attributeAssignOperation, wsAttributeAssignValues, assignmentNotes, assignmentEnabledTime,
        assignmentDisabledTime, delegatable, attributeAssignValueOperation, attributeAssignLookups, wsOwnerGroupLookups, wsOwnerStemLookups, 
        wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups, wsOwnerAttributeDefLookups, ownerAttributeAssignLookups, actions, 
        actAsSubjectLookup, includeSubjectDetail, subjectAttributeArray, includeGroupDetail, 
        params, attributeDefsToReplace, actionsToReplace, attributeDefTypesToReplace );
    
    WsAssignAttributesLiteResults wsAssignAttributesLiteResults = new WsAssignAttributesLiteResults(wsAssignAttributesResults);
    
    return wsAssignAttributesLiteResults; 

  }

  /**
   * get permissionAssignments from roles etc based on inputs
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param roleLookups are roles to look in
   * @param wsSubjectLookups are subjects to look in
   * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
   * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)
   * @param actions to query, or none to query all actions
   * @param includeAttributeDefNames T or F for if attributeDefName objects should be returned
   * @param includeAttributeAssignments T or F for it attribute assignments should be returned
   * @param includeAssignmentsOnAssignments if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param includePermissionAssignDetail T or F for if the permission details should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @param pointInTimeFrom 
   *            To query permissions at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query permissions at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @param immediateOnly T of F (defaults to F) if we should filter out non immediate permissions
   * @param permissionType are we looking for role permissions or subject permissions?  from
   * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
   * @param permissionProcessor if we should find the best answer, or process limits, etc.  From the enum
   * PermissionProcessor.  example values are: FILTER_REDUNDANT_PERMISSIONS, 
   * FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS, PROCESS_LIMITS
   * @param limitEnvVars limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param includeLimits T or F (default to F) for if limits should be returned with the results.
   * Note that the attributeDefs, attributeDefNames, and attributeAssignments will be added to those lists
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetPermissionAssignmentsResults getPermissionAssignments(
      GrouperVersion clientVersion, 
      WsAttributeDefLookup[] wsAttributeDefLookups, WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      WsGroupLookup[] roleLookups, WsSubjectLookup[] wsSubjectLookups, 
      String[] actions, boolean includePermissionAssignDetail,
      boolean includeAttributeDefNames, boolean includeAttributeAssignments,
      boolean includeAssignmentsOnAssignments, WsSubjectLookup actAsSubjectLookup, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, WsParam[] params, 
      String enabled, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, boolean immediateOnly,
      PermissionType permissionType, PermissionProcessor permissionProcessor, WsPermissionEnvVar[] limitEnvVars,
      boolean includeLimits) {  
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new WsGetPermissionAssignmentsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    boolean usePIT = pointInTimeFrom != null || pointInTimeTo != null;

    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsGetPermissionAssignmentsResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion 
          + ", wsAttributeDefLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefLookups, 200) 
          + ", wsAttributeDefNameLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameLookups, 200) + ", roleLookups: "
          + GrouperUtil.toStringForLog(roleLookups, 200) 
          + ", actions: " + GrouperUtil.toStringForLog(actions, 200)
          + ", includePermissionAssignDetail: " + includePermissionAssignDetail
          + ", includeAttributeDefNames: " + includeAttributeDefNames
          + ", includeAttributeAssignments: " + includeAttributeAssignments
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsSubjectLookups: "
          + GrouperUtil.toStringForLog(wsSubjectLookups, 200) 
          + ", enabled: " + enabled
          + "\n pointInTimeFrom: " + pointInTimeFrom + ", pointInTimeTo: " + pointInTimeTo
          + "\n immediateOnly: " + immediateOnly + ", permissionType: " + permissionType
          + ", permissionProcessor: " + permissionProcessor + "\n limitEnvVars: "
          + GrouperUtil.toStringForLog(limitEnvVars, 100) + "\n includeLimits: " + includeLimits;

      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsGetPermissionAssignmentsResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      if (includeAssignmentsOnAssignments && !includeAttributeAssignments) {
        throw new WsInvalidQueryException("If you want assignments on assignment, then you have to have includeAttributeAssignments = T");
      }
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
      
      Map<String, Object> limitEnvMap = GrouperServiceUtils.convertLimitsToMap(
          limitEnvVars);
      
      //role_subject permission type is the default
      if (permissionType == null) {
        permissionType = PermissionType.role_subject;
      }
      
      //this is for error checking
      
      StringBuilder errorMessage = new StringBuilder();
  
      //get the attributedefs to retrieve
      Set<String> attributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsAttributeDefLookups, errorMessage, AttributeDefType.perm, usePIT, pointInTimeFrom, pointInTimeTo);
      
      //get the attributeDefNames to retrieve
      Set<String> attributeDefNameIds = WsAttributeDefNameLookup.convertToAttributeDefNameIds(session, wsAttributeDefNameLookups, errorMessage, AttributeDefType.perm, usePIT, pointInTimeFrom, pointInTimeTo);
      
      //if you sent some in, and none are remaining, then that is bad...
      if (GrouperUtil.length(attributeDefNameIds) == 0 && GrouperUtil.length(wsAttributeDefNameLookups) > 0) {
        throw new WsInvalidQueryException("Cant find any attribute def names from lookups!");
      }
      
      //get all the owner groups
      //the point in time tables do not have typeOfGroup.... permissions only exist on roles so hopefully this doesn't matter..
      TypeOfGroup typeOfGroup = usePIT ? null : TypeOfGroup.role;
      Set<String> roleIds = WsGroupLookup.convertToGroupIds(session, roleLookups, errorMessage, typeOfGroup, usePIT, pointInTimeFrom, pointInTimeTo);
      
      //get all the member ids
      Set<String> memberIds = WsSubjectLookup.convertToMemberIds(session, wsSubjectLookups, errorMessage);
            
      Boolean enabledBoolean = true;
      if (!StringUtils.isBlank(enabled)) {
        if (StringUtils.equalsIgnoreCase("A", enabled)) {
          enabledBoolean = null;
        } else {
          enabledBoolean = GrouperUtil.booleanValue(enabled);
        }
      }
      
      Collection<String> actionsCollection = GrouperUtil.toSet(actions);
      
      if (actionsCollection == null || actionsCollection.size() == 0 
          || (actionsCollection.size() == 1 && StringUtils.isBlank(actionsCollection.iterator().next()))) {
        actionsCollection = null;
      }
      
      if (usePIT && includeGroupDetail) {
        throw new WsInvalidQueryException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      if (usePIT && (enabledBoolean == null || !enabledBoolean)) {
        throw new WsInvalidQueryException("Cannot search for disabled memberships for point in time queries.");
      }
      
      if (usePIT && includeLimits) {
        throw new WsInvalidQueryException("Cannot search for disabled memberships for point in time queries.");
      }
      
      PermissionFinder permissionFinder = new PermissionFinder().assignPermissionDefIds(attributeDefIds)
        .assignPermissionNameIds(attributeDefNameIds).assignRoleIds(roleIds)
        .assignActions(actionsCollection).assignEnabled(enabledBoolean)
        .assignMemberIds(memberIds).assignImmediateOnly(immediateOnly)
        .assignLimitEnvVars(limitEnvMap).assignPermissionType(permissionType)
        .assignPermissionProcessor(permissionProcessor).assignPointInTimeFrom(pointInTimeFrom)
        .assignPointInTimeTo(pointInTimeTo);

      Set<PermissionEntry> results = null;
      Map<PermissionEntry, Set<PermissionLimitBean>> permissionLimitMap = null;
      if (errorMessage.length() == 0) {

        if (includeLimits) {
          permissionLimitMap = permissionFinder.findPermissionsAndLimits();
          results = permissionLimitMap.keySet();
        } else {
          results = permissionFinder.findPermissions();
        }
  
        permissionLimitMap = GrouperUtil.nonNull(permissionLimitMap);
  
        wsGetPermissionAssignmentsResults.assignResult(results, permissionLimitMap, 
            subjectAttributeNames, includePermissionAssignDetail);
  
        if (includeAttributeAssignments) {
          wsGetPermissionAssignmentsResults.fillInAttributeAssigns(usePIT, pointInTimeFrom, pointInTimeTo,
              includeAssignmentsOnAssignments, enabledBoolean);
        }
        
        //get the limit attribute assigns to fill in other objects
        for (Set<PermissionLimitBean> permissionLimitBeanSet : permissionLimitMap.values()) {
          for (PermissionLimitBean permissionLimitBean : GrouperUtil.nonNull(permissionLimitBeanSet)) {
            AttributeAssign attributeAssign = permissionLimitBean.getLimitAssign();
            if (!attributeDefNameIds.contains(attributeAssign.getAttributeDefNameId())) {
              attributeDefNameIds.add(attributeAssign.getAttributeDefNameId());
              attributeDefIds.add(attributeAssign.getAttributeDef().getId());
            }
            if (!StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
              roleIds.add(attributeAssign.getOwnerGroupId());
            }
            //TODO add subjects?
          }
        }
        
        if (includeAttributeDefNames) {
          wsGetPermissionAssignmentsResults.fillInAttributeDefNames(usePIT, attributeDefNameIds);
        }
  
        
        wsGetPermissionAssignmentsResults.fillInAttributeDefs(usePIT, attributeDefIds);
        
        wsGetPermissionAssignmentsResults.fillInGroups(usePIT, roleIds, includeGroupDetail);
        wsGetPermissionAssignmentsResults.fillInSubjects(wsSubjectLookups, null, 
            includeSubjectDetail, subjectAttributeNamesToRetrieve);
        
        //sort after all the data is there
        wsGetPermissionAssignmentsResults.sortResults();
      }
      
      if (errorMessage.length() > 0) {
        wsGetPermissionAssignmentsResults.assignResultCode(WsGetPermissionAssignmentsResultsCode.INVALID_QUERY);
        wsGetPermissionAssignmentsResults.getResultMetadata().appendResultMessage(errorMessage.toString());
      } else {
        wsGetPermissionAssignmentsResults.assignResultCode(WsGetPermissionAssignmentsResultsCode.SUCCESS);
      }
      
      wsGetPermissionAssignmentsResults.getResultMetadata().appendResultMessage(
          ", Found " + GrouperUtil.length(wsGetPermissionAssignmentsResults.getWsPermissionAssigns())
          + " results.  ");
  
        
    } catch (Exception e) {
      wsGetPermissionAssignmentsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetPermissionAssignmentsResults; 
  }

  /**
   * get permissionAssignments from role etc based on inputs
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param roleName is role to look in
   * @param roleId is role to look in
   * @param wsAttributeDefName find assignments in this attribute def (optional)
   * @param wsAttributeDefId find assignments in this attribute def (optional)
   * @param wsAttributeDefNameName find assignments in this attribute def name (optional)
   * @param wsAttributeDefNameId find assignments in this attribute def name (optional)
   * @param wsSubjectId is subject to look in
   * @param wsSubjectSourceId is subject to look in
   * @param wsSubjectIdentifier is subject to look in
   * @param action to query, or none to query all actions
   * @param includeAttributeDefNames T or F for if attributeDefName objects should be returned
   * @param includeAttributeAssignments T or F for it attribute assignments should be returned
   * @param includeAssignmentsOnAssignments if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param includePermissionAssignDetail T or F for if the permission details should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId act as this subject (if allowed)
   * @param actAsSubjectSourceId act as this subject (if allowed)
   * @param actAsSubjectIdentifier act as this subject (if allowed)
   * @param subjectAttributeNames are the additional subject attributes (data) to return (comma separated)
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @param pointInTimeFrom 
   *            To query permissions at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   * @param pointInTimeTo 
   *            To query permissions at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.
   * @param immediateOnly T of F (defaults to F) if we should filter out non immediate permissions
   * @param permissionType are we looking for role permissions or subject permissions?  from
   * enum PermissionType: role, or role_subject.  defaults to role_subject permissions
   * @param permissionProcessor if we should find the best answer, or process limits, etc.  From the enum
   * PermissionProcessor.  example values are: FILTER_REDUNDANT_PERMISSIONS, 
   * FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS, FILTER_REDUNDANT_PERMISSIONS_AND_ROLES,
   * FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS, PROCESS_LIMITS
   * @param limitEnvVarName0 limitEnvVars if processing limits, pass in a set of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (int)amount, value: 50
   * @param limitEnvVarValue0 first limit env var value
   * @param limitEnvVarType0 first limit env var type
   * @param limitEnvVarName1 second limit env var name
   * @param limitEnvVarValue1 second limit env var value
   * @param limitEnvVarType1 second limit env var type
   * @param includeLimits T or F (default to F) for if limits should be returned with the results.
   * Note that the attributeDefs, attributeDefNames, and attributeAssignments will be added to those lists
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetPermissionAssignmentsResults getPermissionAssignmentsLite(
      GrouperVersion clientVersion, 
      String wsAttributeDefName, String wsAttributeDefId, String wsAttributeDefNameName, String wsAttributeDefNameId,
      String roleName, String roleId, 
      String wsSubjectId, String wsSubjectSourceId, String wsSubjectIdentifier,
      String action, boolean includePermissionAssignDetail,
      boolean includeAttributeDefNames, boolean includeAttributeAssignments,
      boolean includeAssignmentsOnAssignments, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeSubjectDetail,
      String subjectAttributeNames, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String enabled, Timestamp pointInTimeFrom, Timestamp pointInTimeTo,
      boolean immediateOnly,
      PermissionType permissionType, PermissionProcessor permissionProcessor, 
      String limitEnvVarName0, String limitEnvVarValue0, 
      String limitEnvVarType0, String limitEnvVarName1, String limitEnvVarValue1, String limitEnvVarType1, 
      boolean includeLimits) {  
    
    WsAttributeDefLookup[] wsAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefName) || !StringUtils.isBlank(wsAttributeDefId)) {
      wsAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsAttributeDefName, wsAttributeDefId)};
    }
    
    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefNameName) || !StringUtils.isBlank(wsAttributeDefNameId)) {
      wsAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId )};
    }
    
    WsGroupLookup[] roleLookups = null;
    if (!StringUtils.isBlank(roleName) || !StringUtils.isBlank(roleId)) {
      roleLookups = new WsGroupLookup[]{new WsGroupLookup(roleName, roleId)};
    }
    
    WsSubjectLookup[] wsSubjectLookups = null;
    if (!StringUtils.isBlank(wsSubjectId) || !StringUtils.isBlank(wsSubjectSourceId) || !StringUtils.isBlank(wsSubjectIdentifier)) {
      wsSubjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(wsSubjectId, wsSubjectSourceId, wsSubjectIdentifier)};
    }

    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

    String[] actions = null;
    if (!StringUtils.isBlank(action)) {
      actions = new String[]{action};
    }

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    WsPermissionEnvVar[] limitEnvVars = GrouperServiceUtils.limitEnvVars(limitEnvVarName0, 
        limitEnvVarValue0, limitEnvVarType0, limitEnvVarName1, limitEnvVarValue1, limitEnvVarType1);

    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = getPermissionAssignments(clientVersion, 
        wsAttributeDefLookups, wsAttributeDefNameLookups, roleLookups, 
        wsSubjectLookups, actions, includePermissionAssignDetail, includeAttributeDefNames, includeAttributeAssignments, 
        includeAssignmentsOnAssignments, 
        actAsSubjectLookup, includeSubjectDetail, subjectAttributeArray, includeGroupDetail, 
        params, enabled, pointInTimeFrom, pointInTimeTo, immediateOnly, permissionType, 
        permissionProcessor, limitEnvVars, includeLimits);

    return wsGetPermissionAssignmentsResults;
  }

  /**
   * assign permissions to roles or subjects (in the context of a role)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param permissionType is role or role_subject from the PermissionType enum
   * @param roleLookups are groups to assign to for permissionType "role"
   * @param subjectRoleLookups are subjects to assign to, in the context of a role (for permissionType "subject_role")
   * @param permissionDefNameLookups attribute def names to assign to the owners (required)
   * @param permissionAssignOperation operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   * @param assignmentNotes notes on the assignment (optional)
   * @param assignmentEnabledTime enabled time, or null for enabled now
   * @param assignmentDisabledTime disabled time, or null for not disabled
   * @param delegatable if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param actions to assign, or "assign" is the default if blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param wsAttributeAssignLookups lookups to remove etc
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param attributeDefsToReplace if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @param actionsToReplace if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   * @param disallowed is disallowed
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignPermissionsResults assignPermissions(
      GrouperVersion clientVersion, PermissionType permissionType,
      WsAttributeDefNameLookup[] permissionDefNameLookups,
      PermissionAssignOperation permissionAssignOperation,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsGroupLookup[] roleLookups, 
      WsMembershipAnyLookup[] subjectRoleLookups, 
      String[] actions, WsSubjectLookup actAsSubjectLookup, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, WsParam[] params,
      WsAttributeDefLookup[] attributeDefsToReplace, String[] actionsToReplace, 
      Boolean disallowed) {  
  
    WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAssignAttributesResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion+ ", permissionType: " + permissionType 
          + ", permissionAssignOperation: " + permissionAssignOperation
          + ", wsAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsAttributeAssignLookups, 200)
          + ", permissionNameLookups: "
          + GrouperUtil.toStringForLog(permissionDefNameLookups, 200) 
          + ", roleLookups: " + GrouperUtil.toStringForLog(roleLookups, 200)
          + ", subjectRoleLookups: " + GrouperUtil.toStringForLog(subjectRoleLookups, 200)
          + ", actions: " + GrouperUtil.toStringForLog(actions, 200)
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n, attributeDefsToReplace: " + GrouperUtil.toStringForLog(attributeDefsToReplace, 200)
          + "\n, actionsToReplace: " + GrouperUtil.toStringForLog(actionsToReplace, 200)
          + "\n, disallowed: " + disallowed;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      if (permissionType == null) {
        throw new WsInvalidQueryException("You need to pass in a permissionType.  ");
      }
      
      if (permissionAssignOperation == null) {
        throw new WsInvalidQueryException("You need to pass in an permissionAssignOperation.  ");
      }
      
      AttributeAssignType attributeAssignType = permissionType.convertToAttributeAssignType();
      
      AttributeAssignOperation attributeAssignOperation = permissionAssignOperation.convertToAttributeAssignOperation();
      
      String[] attributeDefTypesToReplace = permissionAssignOperation == PermissionAssignOperation.replace_permissions 
        ? new String[]{AttributeDefType.perm.name()} : null;
      
      WsAssignAttributeLogic.assignAttributesHelper(attributeAssignType, permissionDefNameLookups, 
          attributeAssignOperation, null, assignmentNotes, assignmentEnabledTime, assignmentDisabledTime, 
          delegatable, null, wsAttributeAssignLookups, roleLookups, null, null, null, subjectRoleLookups, 
          null,null, actions, includeSubjectDetail, subjectAttributeNames, includeGroupDetail, 
          wsAssignAttributesResults, session, params, TypeOfGroup.role, AttributeDefType.perm,
          attributeDefsToReplace, actionsToReplace, attributeDefTypesToReplace, disallowed, true, null);
      
    } catch (Exception e) {
      wsAssignAttributesResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
    WsAssignPermissionsResults wsAssignPermissionsResults = new WsAssignPermissionsResults(wsAssignAttributesResults);
    return wsAssignPermissionsResults; 
  
  }

  /**
   * assign permissions to role or subject (in the context of a role)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param permissionType is role or role_subject from the PermissionType enum
   * @param permissionDefNameName attribute def name to assign to the owner (required)
   * @param permissionDefNameId attribute def name to assign to the owner (required)
   * @param roleName is group to assign to for permissionType "role"
   * @param roleId is group to assign to for permissionType "role"
   * @param permissionAssignOperation operation to perform for permission on role or subject, from enum PermissionAssignOperation
   * assign_permission, remove_permission
   * @param assignmentNotes notes on the assignment (optional)
   * @param assignmentEnabledTime enabled time, or null for enabled now
   * @param assignmentDisabledTime disabled time, or null for not disabled
   * @param delegatable if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param action to assign, or "assign" is the default if blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param wsAttributeAssignId lookup to remove etc
   * @param subjectRoleName is role name if assigning to subject, in the context of a role (for permissionType "subject_role")
   * @param subjectRoleId is role id if assigning to subject, in the context of a role (for permissionType "subject_role")
   * @param subjectRoleSubjectId is subject id if assigning to subject, in the context of a role (for permissionType "subject_role")
   * @param subjectRoleSubjectSourceId  is subject source id if assigning to subject, in the context of a role (for permissionType "subject_role")
   * @param subjectRoleSubjectIdentifier  is subject identifier if assigning to subject, in the context of a role (for permissionType "subject_role")
   * @param actAsSubjectId if acting as someone else
   * @param actAsSubjectSourceId if acting as someone else
   * @param actAsSubjectIdentifier if acting as someone else
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0 optional: reserved for future use
   * @param paramValue0 optional: reserved for future use
   * @param paramName1 optional: reserved for future use
   * @param paramValue1 optional: reserved for future use
   * @param disallowed if the assignment is a disallow
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignPermissionsLiteResults assignPermissionsLite(
      GrouperVersion clientVersion, PermissionType permissionType,
      String permissionDefNameName, String permissionDefNameId,
      PermissionAssignOperation permissionAssignOperation,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      String wsAttributeAssignId,
      String roleName, String roleId,
      String subjectRoleName, String subjectRoleId,
      String subjectRoleSubjectId, String subjectRoleSubjectSourceId, String subjectRoleSubjectIdentifier, 
      String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, boolean includeSubjectDetail,
      String subjectAttributeNames, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, Boolean disallowed) {  

    WsAttributeDefLookup[] attributeDefsToReplace = null; 

    WsAttributeDefNameLookup[] permissionDefNameLookups = null;
    if (!StringUtils.isBlank(permissionDefNameName) || !StringUtils.isBlank(permissionDefNameId)) {
      permissionDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(permissionDefNameName,permissionDefNameId )};

      if (permissionAssignOperation != null) {
        AttributeAssignOperation attributeAssignOperation = permissionAssignOperation.convertToAttributeAssignOperation();
        
        attributeDefsToReplace = WsAssignAttributeLogic.retrieveAttributeDefsForReplace(
            permissionDefNameName, permissionDefNameId, attributeAssignOperation);

      }

    }
    
    WsAttributeAssignLookup[] attributeAssignLookups = null;
    
    if (!StringUtils.isBlank(wsAttributeAssignId)) {
      attributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(wsAttributeAssignId)};
    }
    
    WsGroupLookup[] roleLookups = null;
    if (!StringUtils.isBlank(roleName) || !StringUtils.isBlank(roleId)) {
      roleLookups = new WsGroupLookup[]{new WsGroupLookup(roleName, roleId)};
    }
    
    WsMembershipAnyLookup[] subjectRoleLookups = null;
    if (!StringUtils.isBlank(subjectRoleName) || !StringUtils.isBlank(subjectRoleId)
        || !StringUtils.isBlank(subjectRoleSubjectId) || !StringUtils.isBlank(subjectRoleSubjectSourceId)
        || !StringUtils.isBlank(subjectRoleSubjectIdentifier)) {
      subjectRoleLookups = new WsMembershipAnyLookup[]{
          new WsMembershipAnyLookup(new WsGroupLookup(subjectRoleName,subjectRoleId ),
              new WsSubjectLookup(subjectRoleSubjectId, subjectRoleSubjectSourceId, subjectRoleSubjectIdentifier))};
    }
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
    
    String[] actions = null;
    if (!StringUtils.isBlank(action)) {
      actions = new String[]{action};
    }
    
    String[] actionsToReplace = new String[]{GrouperUtil.defaultIfBlank(action, AttributeDef.ACTION_DEFAULT)};

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    WsAssignPermissionsResults wsAssignPermissionsResults = assignPermissions(clientVersion, permissionType, 
        permissionDefNameLookups, permissionAssignOperation, assignmentNotes, assignmentEnabledTime,
        assignmentDisabledTime, delegatable, attributeAssignLookups, roleLookups, subjectRoleLookups, 
        actions, 
        actAsSubjectLookup, includeSubjectDetail, subjectAttributeArray, includeGroupDetail, 
        params, attributeDefsToReplace, actionsToReplace, disallowed);
    
    WsAssignPermissionsLiteResults wsAssignPermissionsLiteResults = new WsAssignPermissionsLiteResults(wsAssignPermissionsResults);
    
    return wsAssignPermissionsLiteResults; 
  }
  
  /**
   * assign or unassign attribute def name permission inheritance
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeDefNameLookup attributeDefName which is the container for the inherited attribute def names
   * @param relatedWsAttributeDefNameLookups one or many attribute def names to add or remove from inheritance from the container
   * @param assign T to assign, or F to remove assignment
   * @param replaceAllExisting T if assigning, if this list should replace all existing immediately inherited attribute def names
   * @param actAsSubjectLookup if searching as someone else, pass in that subject here, note the caller must
   * be allowed to act as that other subject
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the result
   */
  public static WsAssignAttributeDefNameInheritanceResults assignAttributeDefNameInheritance(final GrouperVersion clientVersion,
      final WsAttributeDefNameLookup wsAttributeDefNameLookup, final WsAttributeDefNameLookup[] relatedWsAttributeDefNameLookups,
      final boolean assign,
      final Boolean replaceAllExisting, final WsSubjectLookup actAsSubjectLookup, GrouperTransactionType txType, 
      final WsParam[] params) {
    
    final WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = new WsAssignAttributeDefNameInheritanceResults();
    
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAssignAttributeDefNameInheritanceResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      theSummary = "clientVersion: " + clientVersion + ", wsAttributeDefNameLookup: "
          + wsAttributeDefNameLookup + ", relatedWsAttributeDefNameLookups: " 
          + GrouperUtil.toStringForLog(relatedWsAttributeDefNameLookups, 200) 
          + "\n, assign: " + assign + ", replaceAllExisting: " + replaceAllExisting + ", actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);

      if ((wsAttributeDefNameLookup == null || wsAttributeDefNameLookup.blank())) {
        throw new WsInvalidQueryException(
            "You need to pass in wsAttributeDefNameLookup");
      }

      if (GrouperUtil.length(relatedWsAttributeDefNameLookups) == 0) {
        throw new WsInvalidQueryException(
            "You need to pass in at last one relatedWsAttributeDefNameLookup");
      }
      
      if (!assign && replaceAllExisting != null) {
        throw new WsInvalidQueryException(
            "If you are unassigning, you cannot pass in replaceAllExisting");
      }
      
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);

      wsAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(session);
      if (!wsAttributeDefNameLookup.retrieveAttributeDefNameFindResult().isSuccess()) {
        throw new WsInvalidQueryException(
          "Cannot find wsAttributeDefNameLookup: " + wsAttributeDefNameLookup);
        
      }
      final AttributeDefName attributeDefName = wsAttributeDefNameLookup.retrieveAttributeDefName();
      
      int relatedWsAttributeDefNameLookupsLength = GrouperServiceUtils.arrayLengthAtLeastOne(
          relatedWsAttributeDefNameLookups, GrouperWsConfig.WS_ATTRIBUTE_DEF_NAME_DELETE_MAX, 1000000, "assignAttributeDefNameInheritance");
      
      if (relatedWsAttributeDefNameLookupsLength < GrouperUtil.length(relatedWsAttributeDefNameLookups)) {
        throw new WsInvalidQueryException(
          "Too many relatedWsAttributeDefNameLookups: " 
            + GrouperUtil.length(relatedWsAttributeDefNameLookups) + " > " + relatedWsAttributeDefNameLookupsLength);
      }
      
      Set<AttributeDefName> relatedAttributeDefNames = new LinkedHashSet<AttributeDefName>();
      
      final boolean[] success = new boolean[]{true};
      StringBuilder resultErrors = new StringBuilder();
      
      //loop through all stems and do the save
      for (WsAttributeDefNameLookup relatedAttributeDefNameLookup : relatedWsAttributeDefNameLookups) {
        relatedAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(session);
        AttributeDefNameFindResult attributeDefNameFindResult = relatedAttributeDefNameLookup.retrieveAttributeDefNameFindResult();
        AttributeDefName relatedAttributeDefName = relatedAttributeDefNameLookup.retrieveAttributeDefName();
        if (attributeDefNameFindResult.isSuccess() && relatedAttributeDefName != null) {
          relatedAttributeDefNames.add(relatedAttributeDefName);
        } else {
          success[0] = false;
          resultErrors.append("Problem with relatedAttributeDefNameLookup: " 
              + relatedAttributeDefNameLookup + ": result: " + attributeDefNameFindResult + ", ");
          if (txType == GrouperTransactionType.READ_WRITE_NEW || txType == GrouperTransactionType.READ_WRITE_OR_USE_EXISTING) {
            throw new WsInvalidQueryException(
                "Transactional query and " + resultErrors);
            
          }
        }
      }
      
      //lets get the adds and removes ready to go
      final Set<AttributeDefName> adds = new LinkedHashSet<AttributeDefName>();
      final Set<AttributeDefName> removes = new LinkedHashSet<AttributeDefName>();
      
      final int[] addSuccesses = new int[]{0};
      final int[] removeSuccesses = new int[]{0};
      final int[] addNoops = new int[]{0};
      final int[] removeNoops = new int[]{0};
      
      Set<AttributeDefName> currentList = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
      int currentListSize = currentList.size();
      int relatedAttributeDefNamesSize = relatedAttributeDefNames.size();
      if (!assign) {
        removes.addAll(relatedAttributeDefNames);
        removes.retainAll(currentList);
        removeNoops[0] += relatedAttributeDefNamesSize - removes.size();
      } else if (assign && (replaceAllExisting == null || !replaceAllExisting)) {
        adds.addAll(relatedAttributeDefNames);
        adds.removeAll(currentList);
        addNoops[0] += relatedAttributeDefNamesSize - adds.size();
      } else {
        adds.addAll(relatedAttributeDefNames);
        //assigning and replacing all existing
        //get current list
        adds.removeAll(currentList);
        
        addNoops[0] += relatedAttributeDefNamesSize - adds.size();
        removes.addAll(new LinkedHashSet<AttributeDefName>(currentList));
        removes.removeAll(relatedAttributeDefNames);
        removeNoops[0] += currentListSize - removes.size();
      }
      
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              for (AttributeDefName add : adds) {
                final AttributeDefName ADD = add;
                //this should be autonomous, so that within one operational, it is transactional
                HibernateSession.callbackHibernateSession(
                    GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    
                      try {
                        boolean wasAdded = attributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(ADD);
                        if (wasAdded) {
                          addSuccesses[0]++;
                        } else {
                          addNoops[0]++;
                        }
                      } catch (Exception e) {
                        wsAssignAttributeDefNameInheritanceResults.assignResultCodeException(null, "Problem with add: " + ADD, e);
                        success[0] = false;
                      }
                      return null;
                    }
                  });
              }
              for (AttributeDefName remove : removes) {
                final AttributeDefName REMOVE = remove;
                //this should be autonomous, so that within one operational, it is transactional
                HibernateSession.callbackHibernateSession(
                    GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    
                      try {
                        boolean wasRemoved = attributeDefName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(REMOVE);
                        if (wasRemoved) {
                          removeSuccesses[0]++;
                        } else {
                          removeNoops[0]++;
                        }
                      } catch (Exception e) {
                        wsAssignAttributeDefNameInheritanceResults.assignResultCodeException(null, "Problem with remove: " + REMOVE, e);
                        success[0] = false;
                      }
                      return null;
                    }
                  });
              }

              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!success[0]) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
                if (TX_TYPE == GrouperTransactionType.READ_WRITE_NEW || TX_TYPE == GrouperTransactionType.READ_WRITE_OR_USE_EXISTING) {
                  addSuccesses[0] = 0;
                  removeSuccesses[0] = 0;
                }
              }
  
              return null;
            }
          });
      
      if (StringUtils.defaultString(wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultMessage()).length() > 0) {
        wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(", ");
      }
      wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(
          "Had " + addSuccesses[0] + " successful adds, " + addNoops[0] 
          + " adds which already existed, " + removeSuccesses[0] + " successful removes, and " 
          + removeNoops[0] + " removes which didnt exist.  ");

      if (success[0]) {
        wsAssignAttributeDefNameInheritanceResults.assignResultCode(WsAssignAttributeDefNameInheritanceResultsCode.SUCCESS);
      
      } else {
        wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(resultErrors.toString());
        if (StringUtils.isBlank(wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultCode())) {
          wsAssignAttributeDefNameInheritanceResults.assignResultCode(WsAssignAttributeDefNameInheritanceResultsCode.EXCEPTION);
        }
        if (resultErrors.length() > 0) {
          
          wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(",  ");
          wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(resultErrors.toString());

        }
        wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(",  ");
        wsAssignAttributeDefNameInheritanceResults.getResultMetadata().appendResultMessage(theSummary);
      }
    } catch (Exception e) {
      wsAssignAttributeDefNameInheritanceResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsAssignAttributeDefNameInheritanceResults;
  }
  
  /**
   * assign or unassign attribute def name permission inheritance
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeDefNameUuid id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName
   * @param attributeDefNameName name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId
   * @param relatedAttributeDefNameUuid id of attribute def name to add or remove from inheritance from the container
   * @param relatedAttributeDefNameName name of attribute def name to add or remove from inheritance from the container
   * @param assign T to assign, or F to remove assignment
   * @param replaceAllExisting T if assigning, if this list should replace all existing immediately inherited attribute def names
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId
   *            optional to narrow the act as subject search to a particular source 
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result
   */
  public static WsAssignAttributeDefNameInheritanceResults assignAttributeDefNameInheritanceLite(GrouperVersion clientVersion,
      String attributeDefNameUuid, String attributeDefNameName, String relatedAttributeDefNameUuid, String relatedAttributeDefNameName,
      boolean assign,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
    
    WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup(attributeDefNameName, attributeDefNameUuid);
    WsAttributeDefNameLookup relatedWsAttributeDefNameLookup = new WsAttributeDefNameLookup(relatedAttributeDefNameName, relatedAttributeDefNameUuid);
    WsAttributeDefNameLookup[] relatedWsAttributeDefNameLookups = new WsAttributeDefNameLookup[] { relatedWsAttributeDefNameLookup };
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = assignAttributeDefNameInheritance(clientVersion,
        wsAttributeDefNameLookup, relatedWsAttributeDefNameLookups, assign, false, actAsSubjectLookup, null, params);
  
    return wsAssignAttributeDefNameInheritanceResults;
  }
    
  /**
   * delete an AttributeDefName or many.  
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeDefNameLookups
   *            AttributeDefNames to delete
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  public static WsAttributeDefNameDeleteResults attributeDefNameDelete(final GrouperVersion clientVersion,
      final WsAttributeDefNameLookup[] wsAttributeDefNameLookups, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
    final WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults = new WsAttributeDefNameDeleteResults();
    
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAttributeDefNameDeleteResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameLookups, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);

      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int attributeDefNamesSize = GrouperServiceUtils.arrayLengthAtLeastOne(wsAttributeDefNameLookups,
                  GrouperWsConfig.WS_ATTRIBUTE_DEF_NAME_DELETE_MAX, 1000000, "attributeDefNameLookups");
  
              wsAttributeDefNameDeleteResults.setResults(new WsAttributeDefNameDeleteResult[attributeDefNamesSize]);
  
              int resultIndex = 0;
  
              //loop through all groups and do the delete
              for (WsAttributeDefNameLookup wsAttributeDefNameLookup : wsAttributeDefNameLookups) {
  
                WsAttributeDefNameDeleteResult wsAttributeDefNameDeleteResult = new WsAttributeDefNameDeleteResult(
                    wsAttributeDefNameLookup);
                wsAttributeDefNameDeleteResults.getResults()[resultIndex++] = wsAttributeDefNameDeleteResult;
  
                wsAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(SESSION);
                AttributeDefName attributeDefName = wsAttributeDefNameLookup.retrieveAttributeDefName();
  
                if (attributeDefName == null) {
  
                  wsAttributeDefNameDeleteResult
                      .assignResultCode(AttributeDefNameFindResult
                          .convertToAttributeDefNameDeleteCodeStatic(wsAttributeDefNameLookup
                              .retrieveAttributeDefNameFindResult()));
                  wsAttributeDefNameDeleteResult.getResultMetadata().setResultMessage(
                      "Cant find attributeDefName: '" + wsAttributeDefNameLookup + "'.  ");
                  //should we short circuit if transactional?
                  continue;
                }
  
                //make each attribute def name failsafe
                try {
                  wsAttributeDefNameDeleteResult.assignAttributeDefName(attributeDefName, wsAttributeDefNameLookup);
  
                  //if there was already a problem, then dont continue
                  if (!GrouperUtil.booleanValue(wsAttributeDefNameDeleteResult.getResultMetadata()
                      .getSuccess(), true)) {
                    continue;
                  }
  
                  attributeDefName.delete();
  
                  wsAttributeDefNameDeleteResult.assignResultCode(WsAttributeDefNameDeleteResultCode.SUCCESS);
                  wsAttributeDefNameDeleteResult.getResultMetadata().setResultMessage(
                      "AttributeDefName '" + attributeDefName.getName() + "' was deleted.");
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsAttributeDefNameDeleteResult
                      .assignResultCode(WsAttributeDefNameDeleteResultCode.INSUFFICIENT_PRIVILEGES);
                } catch (Exception e) {
                  wsAttributeDefNameDeleteResult.assignResultCodeException(e, wsAttributeDefNameLookup);
                }
              }
  
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsAttributeDefNameDeleteResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsAttributeDefNameDeleteResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefNameDeleteResults;
  }
  
  /**
   * delete an AttributeDefName
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeDefNameUuid the uuid of the attributeDefName to delete (mutually exclusive with attributeDefNameName)
   * @param attributeDefNameName the name of the attributeDefName to delete (mutually exclusive with attributeDefNameUuid)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param paramName0 reserved for future use
   * @param paramValue0 reserved for future use
   * @param paramName1 reserved for future use
   * @param paramValue1 reserved for future use
   * @param typeOfGroup type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   * @return the result of one member add
   */
  public static WsAttributeDefNameDeleteLiteResult attributeDefNameDeleteLite(final GrouperVersion clientVersion,
      final String attributeDefNameUuid, final String attributeDefNameName,
      final String actAsSubjectId, final String actAsSubjectSourceId,
      final String actAsSubjectIdentifier, final String paramName0, final String paramValue0,
      final String paramName1, final String paramValue1) {
    
    // setup the group lookup
    WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup(attributeDefNameName, attributeDefNameUuid);
    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = new WsAttributeDefNameLookup[] { wsAttributeDefNameLookup };
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults = attributeDefNameDelete(clientVersion,
        wsAttributeDefNameLookups, actAsSubjectLookup, null, params);
  
    return new WsAttributeDefNameDeleteLiteResult(wsAttributeDefNameDeleteResults);
  }

  /**
   * save an AttributeDefName or many (insert or update).  Note, you cannot rename an existing AttributeDefName.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link AttributeDefNameSave#save()}
   * @param wsAttributeDefNameToSaves
   *            AttributeDefNames to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  public static WsAttributeDefNameSaveResults attributeDefNameSave(final GrouperVersion clientVersion,
      final WsAttributeDefNameToSave[] wsAttributeDefNameToSaves, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
    
    final WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = new WsAttributeDefNameSaveResults();
    
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAttributeDefNameSaveResults.getResponseMetadata().warnings());

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsAttributeDefNameToSaves: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameToSaves, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int wsAttributeDefNamesLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsAttributeDefNameToSaves, GrouperWsConfig.WS_ATTRIBUTE_DEF_NAME_SAVE_MAX, 1000000, "attributeDefNamesToSave");
  
              wsAttributeDefNameSaveResults.setResults(new WsAttributeDefNameSaveResult[wsAttributeDefNamesLength]);
  
              int resultIndex = 0;
  
              //loop through all stems and do the save
              for (WsAttributeDefNameToSave wsAttributeDefNameToSave : wsAttributeDefNameToSaves) {
                final WsAttributeDefNameSaveResult wsAttributeDefNameSaveResult = 
                  new WsAttributeDefNameSaveResult(wsAttributeDefNameToSave.getWsAttributeDefNameLookup());
                wsAttributeDefNameSaveResults.getResults()[resultIndex++] = wsAttributeDefNameSaveResult;
                final WsAttributeDefNameToSave WS_ATTRIBUTE_DEF_NAME_TO_SAVE = wsAttributeDefNameToSave;
                try {
                  //this should be autonomous, so that within one attribute def name, it is transactional
                  HibernateSession.callbackHibernateSession(
                      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                    public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {
                      //make sure everything is in order
                      WS_ATTRIBUTE_DEF_NAME_TO_SAVE.validate();
                      AttributeDefName attributeDefName = WS_ATTRIBUTE_DEF_NAME_TO_SAVE.save(SESSION);
                      SaveResultType saveResultType = WS_ATTRIBUTE_DEF_NAME_TO_SAVE.saveResultType();
                      wsAttributeDefNameSaveResult.setWsAttributeDefName(new WsAttributeDefName(attributeDefName, 
                          WS_ATTRIBUTE_DEF_NAME_TO_SAVE.getWsAttributeDefNameLookup()));
                      
                      if (saveResultType == SaveResultType.INSERT) {
                        wsAttributeDefNameSaveResult.assignResultCode(WsAttributeDefNameSaveResultCode.SUCCESS_INSERTED, clientVersion);
                      } else if (saveResultType == SaveResultType.UPDATE) {
                        wsAttributeDefNameSaveResult.assignResultCode(WsAttributeDefNameSaveResultCode.SUCCESS_UPDATED, clientVersion);
                      } else if (saveResultType == SaveResultType.NO_CHANGE) {
                        wsAttributeDefNameSaveResult.assignResultCode(WsAttributeDefNameSaveResultCode.SUCCESS_NO_CHANGES_NEEDED, clientVersion);
                      } else {
                        throw new RuntimeException("Invalid saveType: " + saveResultType);
                      }

                      return null;
                    }

                  });
  
                } catch (Exception e) {
                  wsAttributeDefNameSaveResult.assignResultCodeException(e, wsAttributeDefNameToSave, clientVersion);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsAttributeDefNameSaveResults.tallyResults(TX_TYPE, THE_SUMMARY, clientVersion)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsAttributeDefNameSaveResults.assignResultCodeException(null, theSummary, e, clientVersion);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefNameSaveResults;

  }
  
  /**
   * save an AttributeDefName (insert or update).  Note you cannot currently move an existing AttributeDefName.
   * 
   * @see {@link AttributeDefNameSave#save()}
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeDefNameLookupUuid the uuid of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupName)
   * @param attributeDefNameLookupName the name of the attributeDefName to edit (mutually exclusive with attributeDefNameLookupUuid)
   * @param attributeDefLookupName
   *            to lookup the attributeDef (mutually exclusive with attributeDefUuid)
   * @param attributeDefLookupUuid
   *            to lookup the attributeDef (mutually exclusive with attributeDefName)
   * @param attributeDefNameName
   *            to lookup the attributeDefName (mutually exclusive with attributeDefNameUuid)
   * @param attributeDefNameUuid
   *            to lookup the attributeDefName (mutually exclusive with attributeDefNameName)
   * @param description
   *            of the attributeDefName, empty will be ignored
   * @param displayExtension
   *            display name of the attributeDefName, empty will be ignored
   * @param saveMode if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param createParentStemsIfNotExist T or F (default F) if parent stems should be created if not exist
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsAttributeDefNameSaveLiteResult attributeDefNameSaveLite(final GrouperVersion clientVersion,
      String attributeDefNameLookupUuid, String attributeDefNameLookupName, String attributeDefLookupUuid, 
      String attributeDefLookupName, String attributeDefNameUuid,String attributeDefNameName, 
      String displayExtension,String description,  SaveMode saveMode, Boolean createParentStemsIfNotExist,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
    // setup the group lookup
    WsAttributeDefNameToSave wsAttributeDefNameToSave = new WsAttributeDefNameToSave();
  
    WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
    wsAttributeDefName.setDescription(description);
    wsAttributeDefName.setDisplayExtension(displayExtension);
    wsAttributeDefName.setName(attributeDefNameName);
    wsAttributeDefName.setUuid(attributeDefNameUuid);
    wsAttributeDefName.setAttributeDefId(attributeDefLookupUuid);
    wsAttributeDefName.setAttributeDefName(attributeDefLookupName);
    
    wsAttributeDefNameToSave.setWsAttributeDefName(wsAttributeDefName);
    wsAttributeDefNameToSave.setCreateParentStemsIfNotExist(createParentStemsIfNotExist == null ? null : (createParentStemsIfNotExist ? "T" : "F"));
    WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup(attributeDefNameLookupName, attributeDefNameLookupUuid);
    wsAttributeDefNameToSave.setWsAttributeDefNameLookup(wsAttributeDefNameLookup);
  
    wsAttributeDefNameToSave.setSaveMode(saveMode == null ? null : saveMode.name());
  
    WsAttributeDefNameToSave[] wsAttributeDefNameToSaves = new WsAttributeDefNameToSave[] { wsAttributeDefNameToSave };
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = attributeDefNameSave(clientVersion, wsAttributeDefNameToSaves,
        actAsSubjectLookup, null, params);
  
    return new WsAttributeDefNameSaveLiteResult(wsAttributeDefNameSaveResults);
  }
  
  /**
   * find an attribute def name or attribute def names.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param scope search string with % as wildcards will search name, display name, description
   * @param splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param wsAttributeDefLookup find names associated with this attribute definition
   * @param attributeAssignType where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @param attributeDefType type of attribute definition, e.g. attr, domain, limit, perm, type
   * @param actAsSubjectLookup if searching as someone else, pass in that subject here, note the caller must
   * be allowed to act as that other subject
   * @param params optional: reserved for future use
   * @param wsAttributeDefNameLookups if you want to just pass in a list of uuids and/or names.
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, F for descending.  
   * @param wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @param wsSubjectLookup subject if looking for privileges or service role
   * @param serviceRole to filter attributes that a user has a certain role
   * @return the attribute def names, or no attribute def names if none found
   */
  public static WsFindAttributeDefNamesResults findAttributeDefNames(final GrouperVersion clientVersion,
      String scope, Boolean splitScope, WsAttributeDefLookup wsAttributeDefLookup,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType,
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups, Integer pageSize, Integer pageNumber,
      String sortString, Boolean ascending, 
      WsInheritanceSetRelation wsInheritanceSetRelation, WsSubjectLookup actAsSubjectLookup, WsParam[] params,
      WsSubjectLookup wsSubjectLookup, ServiceRole serviceRole) {

    final WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = new WsFindAttributeDefNamesResults();
    
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsFindAttributeDefNamesResults.getResponseMetadata().warnings());

      theSummary = "clientVersion: " + clientVersion + ", scope: "
          + scope + ", splitScope: " + splitScope
          + ", wsAttributeDefLookup: " + wsAttributeDefLookup
          + ", attributeAssignType: " + attributeAssignType
          + ", attributeDefType: " + attributeAssignType
          + "\nwsAttributeDefNameLookups: " + GrouperUtil.toStringForLog(wsAttributeDefNameLookups)
          + "\nwsInheritanceSetRelation: " + wsInheritanceSetRelation
          + ", pageSize: " + pageSize + ", pageNumber: " + pageNumber 
          + ", sortString: " + sortString + ", ascending: " + ascending 
          + ", actAsSubject: " + actAsSubjectLookup + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n, wsSubjectLookup: " + wsSubjectLookup + ", serviceRole: " + serviceRole;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      final Set<AttributeDefName> attributeDefNames = new LinkedHashSet<AttributeDefName>();
      
      boolean hasScopeQuery = false;
      
      if (!StringUtils.isBlank(scope) || (wsAttributeDefLookup != null && !wsAttributeDefLookup.blank())) {
        hasScopeQuery = true;
      }
      
      boolean hasLookupQuery = false;
      
      if (GrouperUtil.length(wsAttributeDefNameLookups) > 0) {
        hasLookupQuery = true;
      }
      
      if (!hasLookupQuery && !hasScopeQuery) {

        throw new WsInvalidQueryException(
            "You need to pass in either a scope query or attribute def lookup, or attribute def name lookups.");

      }
      
      if (hasLookupQuery && hasScopeQuery) {

        throw new WsInvalidQueryException(
            "You need to pass in either a scope query or attribute def lookup, or attribute def name lookups.");

      }

      if (splitScope != null && StringUtils.isBlank(scope)) {

        throw new WsInvalidQueryException(
          "If you pass in a splitScope, then you need to pass in a scope");

      }

      if (wsInheritanceSetRelation != null && GrouperUtil.length(wsAttributeDefNameLookups) != 1) {

        throw new WsInvalidQueryException(
          "If you pass in a wsInheritanceSetRelation, then you need to pass in one and only one wsAttributeDefNameLookup");

      }

      if ((pageNumber != null) && (pageSize == null) ) {

        throw new WsInvalidQueryException(
          "If you pass in pageNumber you need to pass in pageSize");

      }

      if (wsSubjectLookup != null && !wsSubjectLookup.blank() && (serviceRole == null )) {

        throw new WsInvalidQueryException(
          "If you pass in wsSubjectLookup you need to pass in serviceRole");

      }

      if (wsSubjectLookup != null && wsSubjectLookup.blank() && (serviceRole != null )) {

        throw new WsInvalidQueryException(
          "If you pass in serviceRole you need to pass in wsSubjectLookup");

      }

      if (hasScopeQuery) {

        String attributeDefId = null;
        if (wsAttributeDefLookup != null && !wsAttributeDefLookup.blank()) {
          wsAttributeDefLookup.retrieveAttributeDefIfNeeded(session);
          attributeDefId = wsAttributeDefLookup.retrieveAttributeDef().getId();
        }
        
        QueryOptions queryOptions = null;
        if (pageSize != null || pageNumber != null || !StringUtils.isBlank(sortString) || ascending != null) {
          queryOptions = new QueryOptions();
          if (pageNumber != null || pageSize != null) {
            //default page number to 1
            if (pageNumber == null) {
              pageNumber = 1;
            }
            queryOptions.paging(pageSize, pageNumber, false);
          }
          if (ascending != null || !StringUtils.isBlank(sortString)) {
            //default sort string to displayName
            if (StringUtils.isBlank(sortString)) {
              sortString = "displayName";
            }
            if (ascending == null || ascending) {
              queryOptions.sortAsc(sortString);
            } else {
              queryOptions.sortDesc(sortString);
            }
          }
        }
        
        Subject subject = wsSubjectLookup == null ? null : wsSubjectLookup.retrieveSubject();
        
        attributeDefNames.addAll(GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSecure(
            scope, true, session, attributeDefId, subject, AttributeDefPrivilege.VIEW_PRIVILEGES, 
            queryOptions, attributeAssignType, attributeDefType, serviceRole, false));
      }
      
      if (hasLookupQuery) {
        //if we are looking at inheritance sets
        if (wsInheritanceSetRelation != null) {
          wsAttributeDefNameLookups[0].retrieveAttributeDefNameIfNeeded(session);
          AttributeDefName attributeDefName = wsAttributeDefNameLookups[0].retrieveAttributeDefName();
          //we better be able to find the parent...
          if (attributeDefName == null) {
            throw new WsInvalidQueryException(
              "Cant find attribute def name from lookup: " + wsAttributeDefNameLookups[0]);
          }
          
          //get the related attribute def names
          attributeDefNames.addAll(wsInheritanceSetRelation.relatedAttributeDefNames(attributeDefName));
          
        } else {
          //we could do this in fewer queries if we like...
          for (WsAttributeDefNameLookup wsAttributeDefNameLookup : GrouperUtil.nonNull(wsAttributeDefNameLookups, WsAttributeDefNameLookup.class)) {
            wsAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(session);
            AttributeDefName attributeDefName = wsAttributeDefNameLookup.retrieveAttributeDefName();
            if (attributeDefName != null) {
              attributeDefNames.add(attributeDefName);
            }
          }
        }        
      }
      
      if (serviceRole != null) {
        
        //if service role is not equal to null, then we are searching for services
        GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            wsFindAttributeDefNamesResults.assignAttributeDefNameResult(attributeDefNames);
            return null;
          }
        });
        
      } else {
        wsFindAttributeDefNamesResults.assignAttributeDefNameResult(attributeDefNames);
      }
  
      wsFindAttributeDefNamesResults.assignResultCode(WsFindAttributeDefNamesResultsCode.SUCCESS);
      
      wsFindAttributeDefNamesResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindAttributeDefNamesResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersionUtils.removeCurrentClientVersion(true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsFindAttributeDefNamesResults;
  }
    
  /**
   * find an attribute def name or attribute def names.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param scope search string with % as wildcards will search name, display name, description
   * @param splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param uuidOfAttributeDef find names associated with this attribute definition, mutually exclusive with nameOfAttributeDef
   * @param nameOfAttributeDef find names associated with this attribute definition, mutually exclusive with idOfAttributeDef
   * @param wsAttributeDefLookup find names associated with this attribute definition
   * @param attributeAssignType where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @param attributeDefType type of attribute definition, e.g. attr, domain, limit, perm, type
   * @param attributeDefNameUuid to lookup an attribute def name by id, mutually exclusive with attributeDefNameName
   * @param attributeDefNameName to lookup an attribute def name by name, mutually exclusive with attributeDefNameId
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, F for descending.  
   * @param wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId
   *            optional to narrow the act as subject search to a particular source 
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param subjectId subject id if looking for privileges or service role
   * @param subjectSourceId subject source id if looking for privileges or service role
   * @param subjectIdentifier subject identifier if looking for privileges or service role
   * @param serviceRole to filter attributes that a user has a certain role
   * @return the attribute def names, or no attribute def names if none found
   */
  public static WsFindAttributeDefNamesResults findAttributeDefNamesLite(final GrouperVersion clientVersion,
      String scope, Boolean splitScope, String uuidOfAttributeDef, String nameOfAttributeDef,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType, String attributeDefNameUuid, String attributeDefNameName,
      Integer pageSize, Integer pageNumber,
      String sortString, Boolean ascending, WsInheritanceSetRelation wsInheritanceSetRelation,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1, String subjectId, String subjectSourceId,
      String subjectIdentifier, ServiceRole serviceRole) {

    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);


    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);

    WsAttributeDefLookup wsAttributeDefLookup = null;

    if (!StringUtils.isBlank(nameOfAttributeDef) || !StringUtils.isBlank(uuidOfAttributeDef)) {
      wsAttributeDefLookup = new WsAttributeDefLookup(nameOfAttributeDef, uuidOfAttributeDef);
    }

    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;

    if (!StringUtils.isBlank(attributeDefNameName) || !StringUtils.isBlank(attributeDefNameUuid)) {
      wsAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(attributeDefNameName, attributeDefNameUuid)};
    }

    WsSubjectLookup wsSubjectLookup = WsSubjectLookup.createIfNeeded(subjectId,
        subjectSourceId, subjectIdentifier);

    // pass through to the more comprehensive method
    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = findAttributeDefNames(clientVersion, 
        scope, splitScope, wsAttributeDefLookup, attributeAssignType, attributeDefType, wsAttributeDefNameLookups, 
        pageSize, pageNumber, sortString, ascending, wsInheritanceSetRelation, 
        actAsSubjectLookup, params, wsSubjectLookup, serviceRole);
  
    return wsFindAttributeDefNamesResults;

  }

  /**
   * assign attributes and values to owner objects (groups, stems, etc), doing multiple operations in one batch
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param wsAssignAttributeBatchEntries batch of attribute assignments
   * @param actAsSubjectLookup
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignAttributesBatchResults assignAttributesBatch(
      final GrouperVersion clientVersion, final WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries,
      final WsSubjectLookup actAsSubjectLookup, final boolean includeSubjectDetail, GrouperTransactionType txType,
      final String[] subjectAttributeNames, final boolean includeGroupDetail, final WsParam[] params) {  
  
    final WsAssignAttributesBatchResults wsAssignAttributesBatchResults = new WsAssignAttributesBatchResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersionUtils.assignCurrentClientVersion(clientVersion, wsAssignAttributesBatchResults.getResponseMetadata().warnings());
  
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;

      theSummary = "clientVersion: " + clientVersion 
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) 
          + "\n, wsAssignAttributeBatchEntries: "
          + WsAssignAttributeBatchEntry.toString(wsAssignAttributeBatchEntries, 200);
      
      final String THE_SUMMARY = theSummary;
      
      if (GrouperUtil.length(wsAssignAttributeBatchEntries) == 0) {
        throw new WsInvalidQueryException(
            "You must pass in at least one WsAssignAttributeBatchEntry");
      }
      
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
      final GrouperSession SESSION = session;
      
      //there should be as many results as there were batch entries
      wsAssignAttributesBatchResults.setWsAssignAttributeBatchResultArray(new WsAssignAttributeBatchResult[GrouperUtil.length(wsAssignAttributeBatchEntries)]);
      
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              int index = 0;
              String[] attributeAssignIds = new String[GrouperUtil.length(wsAssignAttributeBatchEntries)];
              for (WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry : wsAssignAttributeBatchEntries) {

                String theError = null;
                Exception exception = null;
                WsAssignAttributesResults tempResults = null;
                
                //keep track for back references
                String attributeAssignId = null;
                
                try {
                  AttributeAssignType attributeAssignType = GrouperServiceUtils.convertAttributeAssignType(
                     wsAssignAttributeBatchEntry.getAttributeAssignType());
                  
                  WsAttributeDefNameLookup[] wsAttributeDefNameLookups = 
                    new WsAttributeDefNameLookup[]{wsAssignAttributeBatchEntry.getWsAttributeDefNameLookup()};
                  
                  AttributeAssignOperation attributeAssignOperation = GrouperServiceUtils.convertAttributeAssignOperation(
                      wsAssignAttributeBatchEntry.getAttributeAssignOperation());
                  
                  if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
                    throw new WsInvalidQueryException("You cannot relace attributes in a batch operation.  ");
                  }
                  
                  AttributeAssignValueOperation attributeAssignValueOperation = GrouperServiceUtils.
                    convertAttributeAssignValueOperation(wsAssignAttributeBatchEntry.getAttributeAssignValueOperation());
  
                  if (attributeAssignType == null) {
                    throw new WsInvalidQueryException("You need to pass in an attributeAssignType.  ");
                  }
                  
                  if (attributeAssignOperation == null) {
                    throw new WsInvalidQueryException("You need to pass in an attributeAssignOperation.  ");
                  }
  
                  Timestamp assignmentEnabledTime = GrouperServiceUtils.stringToTimestamp(
                      wsAssignAttributeBatchEntry.getAssignmentEnabledTime());
                  
                  Timestamp assignmentDisabledTime = GrouperServiceUtils.stringToTimestamp(
                      wsAssignAttributeBatchEntry.getAssignmentDisabledTime());
  
                  AttributeAssignDelegatable attributeAssignDelegatable = GrouperServiceUtils
                    .convertAttributeAssignDelegatable(wsAssignAttributeBatchEntry.getDelegatable());
  
                  WsAttributeAssignLookup[] wsAttributeAssignLookups = new WsAttributeAssignLookup[]{
                      wsAssignAttributeBatchEntry.getWsAttributeAssignLookup()};
                  
                  WsGroupLookup[] wsOwnerGroupLookups = new WsGroupLookup[]{wsAssignAttributeBatchEntry.getWsOwnerGroupLookup()};
  
                  WsStemLookup[] wsOwnerStemLookups = new WsStemLookup[]{wsAssignAttributeBatchEntry.getWsOwnerStemLookup()};
                  
                  WsSubjectLookup[] wsOwnerSubjectLookups = new WsSubjectLookup[]{wsAssignAttributeBatchEntry.getWsOwnerSubjectLookup()};
                  
                  WsMembershipLookup[] wsOwnerMembershipLookups = new WsMembershipLookup[]{
                      wsAssignAttributeBatchEntry.getWsOwnerMembershipLookup()};
                  
                  WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups = new WsMembershipAnyLookup[]{wsAssignAttributeBatchEntry.getWsOwnerMembershipAnyLookup()};
  
                  WsAttributeDefLookup[] wsOwnerAttributeDefLookups = new WsAttributeDefLookup[]{wsAssignAttributeBatchEntry.getWsOwnerAttributeDefLookup()};
                  
                  WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups = new WsAttributeAssignLookup[]{wsAssignAttributeBatchEntry.getWsOwnerAttributeAssignLookup()};
                  
                  String[] actions = new String[]{wsAssignAttributeBatchEntry.getAction()};
                  
                  String assignmentNotes = wsAssignAttributeBatchEntry.getAssignmentNotes();
                  WsAttributeAssignValue[] values = wsAssignAttributeBatchEntry.getValues();
                  
                  tempResults = new WsAssignAttributesResults();
                  
                  WsAssignAttributeLogic.assignAttributesHelper(attributeAssignType, wsAttributeDefNameLookups,
                      attributeAssignOperation, values, 
                      assignmentNotes, assignmentEnabledTime,
                      assignmentDisabledTime, attributeAssignDelegatable, attributeAssignValueOperation,
                      wsAttributeAssignLookups, wsOwnerGroupLookups, wsOwnerStemLookups,
                      wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups,
                      wsOwnerAttributeDefLookups, wsOwnerAttributeAssignLookups, actions,
                      includeSubjectDetail, subjectAttributeNames, includeGroupDetail,
                      tempResults, SESSION, params, null, null, 
                      null, null, null, false, false, attributeAssignIds);
                  
                  //keep track of id's 
                  if (GrouperUtil.length(tempResults.getWsAttributeAssignResults()) == 1) {
                    
                    WsAssignAttributeResult wsAssignAttributeResult = tempResults.getWsAttributeAssignResults()[0];
                    if (wsAssignAttributeResult != null) {
                      
                      if (GrouperUtil.length(wsAssignAttributeResult.getWsAttributeAssigns()) == 1) {
                        
                        WsAttributeAssign wsAttributeAssign = wsAssignAttributeResult.getWsAttributeAssigns()[0];
                        attributeAssignId = wsAttributeAssign.getId();
                      }
                    }
                  }
                  
                } catch (Exception e) {
                  exception = e;
                  theError = "Error assigning attribute: " + wsAssignAttributeBatchEntry + ", " + e + ".  ";
 
                }
                //lets collate the results, note, we can make this more efficient later as far as resolving objects
                wsAssignAttributesBatchResults.addResult(tempResults, theError, exception, index);

                //no need to continue if one failed
                if (exception != null && TX_TYPE.isTransactional()) {
                  grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
                  break;
                }
                
                attributeAssignIds[index] = attributeAssignId;
                index++;
                
              }
              
              if (!wsAssignAttributesBatchResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }

              wsAssignAttributesBatchResults.sortResults();

              return null;
            }
      });
        
    } catch (Exception e) {
      wsAssignAttributesBatchResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
    
    
    return wsAssignAttributesBatchResults; 
  
  }
        
}
