/*
 * @author mchyzer $Id: GrouperServiceLogic.java,v 1.6 2008-03-29 10:50:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperQuery;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.QueryFilter;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAttribute;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeEdit;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsPrivilege;
import edu.internet2.middleware.grouper.ws.soap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsStem;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.soap.WsSubject;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditAttributesResult;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditAttributesResults;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditPrivilegesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult.WsDeleteMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults.WsFindGroupsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults.WsFindStemsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult.WsGroupDeleteResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup.GroupFindResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult.WsGroupSaveResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults.WsGroupSaveResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResult.WsStemDeleteResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup.StemFindResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResult.WsStemSaveResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditAttributesResult.WsViewOrEditAttributesResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsViewOrEditAttributesResults.WsViewOrEditAttributesResultsCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * Meant to be delegate from GrouperService which has the same params (and names)
 * with enums translated (for Simple objects like Field) for each Javadoc viewing.
 */
public class GrouperServiceLogic {

  /**
   * logger 
   */
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
   * @return the results.  return the subject lookup only if there are problems retrieving the subject.
   * @see GrouperWsVersion
   */
  @SuppressWarnings("unchecked")
  public static WsAddMemberResults addMember(final GrouperWsVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final boolean replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
      final Field fieldName, GrouperTransactionType txType,
      final boolean includeGroupDetail, final boolean includeSubjectDetail,
      final String[] subjectAttributeNames, final WsParam[] params) {
    final WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();

    GrouperSession session = null;
    String theSummary = null;
    try {
      
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
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
          + GrouperUtil.toStringForLog(params, 100);

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

              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);

              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");

              String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
                  .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

              wsAddMemberResults
                  .setSubjectAttributeNames(subjectAttributeNamesToRetrieve);

              //assign the group to the result to be descriptive
              wsAddMemberResults
                  .setWsGroupAssigned(new WsGroup(group, wsGroupLookup, includeGroupDetail));

              int resultIndex = 0;

              Set<Subject> newSubjects = new HashSet<Subject>();
              wsAddMemberResults.setResults(new WsAddMemberResult[subjectLength]);

              //get existing members if replacing
              Set<Member> members = null;
              if (replaceAllExisting) {
                try {
                  // see who is there
                  members = fieldName == null ? group.getImmediateMembers() : group
                      .getImmediateMembers(fieldName);
                } catch (SchemaException se) {
                  throw new WsInvalidQueryException(
                      "Problem with getting existing members: " + fieldName + ".  "
                          + ExceptionUtils.getFullStackTrace(se));
                }
              }

              for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
                WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
                wsAddMemberResults.getResults()[resultIndex++] = wsAddMemberResult;
                try {

                  Subject subject = wsSubjectLookup.retrieveSubject();

                  wsAddMemberResult.processSubject(wsSubjectLookup,
                      subjectAttributeNamesToRetrieve);

                  if (subject == null) {
                    continue;
                  }

                  // keep track
                  if (replaceAllExisting) {
                    newSubjects.add(subject);
                  }

                  try {
                    if (fieldName == null) {
                      // dont fail if already a direct member
                      group.addMember(subject, false);
                    } else {
                      group.addMember(subject, fieldName, false);
                    }
                    wsAddMemberResult.assignResultCode(WsAddMemberResultCode.SUCCESS);

                  } catch (InsufficientPrivilegeException ipe) {
                    wsAddMemberResult
                        .assignResultCode(WsAddMemberResultCode.INSUFFICIENT_PRIVILEGES);
                  }
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

                    if (!newSubjects.contains(subject)) {
                      if (fieldName == null) {
                        group.deleteMember(subject);
                      } else {
                        group.deleteMember(subject, fieldName);
                      }
                    }
                  } catch (Exception e) {
                    String theError = "Error deleting subject: " + subject
                        + " from group: " + group + ", field: "
                        + GrouperServiceUtils.fieldName(fieldName) + ", " + e + ".  ";
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
   * @return the result of one member add
   */
  public static WsAddMemberLiteResult addMemberLite(
      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      Field fieldName, boolean includeGroupDetail, boolean includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsAddMemberResults wsAddMemberResults = addMember(clientVersion, wsGroupLookup,
        subjectLookups, false, actAsSubjectLookup, fieldName, null, includeGroupDetail,
        includeSubjectDetail, subjectAttributeArray, params);

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
  public static WsDeleteMemberResults deleteMember(final GrouperWsVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final WsSubjectLookup actAsSubjectLookup, final Field fieldName,
      GrouperTransactionType txType, final boolean includeGroupDetail, 
      final boolean includeSubjectDetail, final String[] subjectAttributeNames, 
      final WsParam[] params) {
  
    final WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 100) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName + ", txType: " + txType
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
  
              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);
  
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
  
                  Subject subject = wsSubjectLookup.retrieveSubject();
                  wsDeleteMemberResult.processSubject(wsSubjectLookup, subjectAttributeNames);
  
                  if (subject == null) {
                    continue;
                  }
  
                  try {
  
                    boolean hasImmediate = false;
                    boolean hasEffective = false;
                    if (fieldName == null) {
                      // dont fail if already a direct member
                      hasEffective = group.hasEffectiveMember(subject);
                      hasImmediate = group.hasImmediateMember(subject);
                      if (hasImmediate) {
                        group.deleteMember(subject);
                      }
                    } else {
                      // dont fail if already a direct member
                      hasEffective = group.hasEffectiveMember(subject, fieldName);
                      hasImmediate = group.hasImmediateMember(subject, fieldName);
                      if (hasImmediate) {
                        group.deleteMember(subject, fieldName);
                      }
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
  public static WsDeleteMemberLiteResult deleteMemberLite(final GrouperWsVersion clientVersion,
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
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
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
   * @return the groups, or no groups if none found
   */
  @SuppressWarnings("unchecked")
  public static WsFindGroupsResults findGroups(final GrouperWsVersion clientVersion,
      WsQueryFilter wsQueryFilter, 
      WsSubjectLookup actAsSubjectLookup, boolean includeGroupDetail, WsParam[] params) {
  
    final WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsQueryFilter: "
          + wsQueryFilter + "\n, includeGroupDetail: " + includeGroupDetail
          + ", actAsSubject: " + actAsSubjectLookup + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      wsQueryFilter.assignGrouperSession(session);
  
      //make sure filter is ok to use
      wsQueryFilter.validate();
  
      //run the query
      QueryFilter queryFilter = wsQueryFilter.retrieveQueryFilter();
      GrouperQuery grouperQuery = GrouperQuery.createQuery(session, queryFilter);
      Set<Group> groups = grouperQuery.getGroups();
  
      wsFindGroupsResults.assignGroupResult(groups, includeGroupDetail);
  
      wsFindGroupsResults.assignResultCode(WsFindGroupsResultsCode.SUCCESS);
      wsFindGroupsResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, theSummary, e);
    } finally {
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
   * @return the groups, or no groups if none found
   */
  public static WsFindGroupsResults findGroupsLite(final GrouperWsVersion clientVersion,
      WsQueryFilterType queryFilterType, String groupName, String stemName, StemScope stemNameScope,
      String groupUuid, String groupAttributeName, String groupAttributeValue,
      GroupType groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
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
  
    // pass through to the more comprehensive method
    WsFindGroupsResults wsFindGroupsResults = findGroups(clientVersion, wsQueryFilter,
        actAsSubjectLookup, includeGroupDetail, params);
  
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
   * @return the stems, or no stems if none found
   */
  @SuppressWarnings("unchecked")
  public static WsFindStemsResults findStems(final GrouperWsVersion clientVersion,
      WsStemQueryFilter wsStemQueryFilter, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params) {
  
    final WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsStemQueryFilter: "
          + wsStemQueryFilter + ", actAsSubject: " + actAsSubjectLookup
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      wsStemQueryFilter.assignGrouperSession(session);
  
      //make sure filter is ok to use
      wsStemQueryFilter.validate();
  
      //run the query
      QueryFilter queryFilter = wsStemQueryFilter.retrieveQueryFilter();
      GrouperQuery grouperQuery = GrouperQuery.createQuery(session, queryFilter);
      Set<Stem> stems = grouperQuery.getStems();
  
      wsFindStemsResults.assignStemResult(stems);
  
      wsFindStemsResults.assignResultCode(WsFindStemsResultsCode.SUCCESS);
      wsFindStemsResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindStemsResults.assignResultCodeException(null, theSummary, e);
    } finally {
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
  public static WsFindStemsResults findStemsLite(final GrouperWsVersion clientVersion,
      WsStemQueryFilterType stemQueryFilterType, String stemName, String parentStemName,
      StemScope parentStemNameScope, String stemUuid, String stemAttributeName,
      String stemAttributeValue, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
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
        actAsSubjectLookup, params);
  
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
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetGroupsResults getGroups(final GrouperWsVersion clientVersion,
      WsSubjectLookup[] subjectLookups, WsMemberFilter memberFilter, 
      WsSubjectLookup actAsSubjectLookup, boolean includeGroupDetail,
      boolean includeSubjectDetail, 
      String[] subjectAttributeNames, WsParam[] params) {
  
    final WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 200) 
          + "\nmemberFilter: " + memberFilter + ", includeGroupDetail: "
          + includeGroupDetail + ", actAsSubject: " + actAsSubjectLookup
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
          subjectLookups, GrouperWsConfig.WS_GET_GROUPS_SUBJECTS_MAX, 1000000);

      int resultIndex = 0;

      wsGetGroupsResults.setResults(new WsGetGroupsResult[subjectLength]);

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
        WsGetGroupsResult wsGetGroupsResult = new WsGetGroupsResult();
        wsGetGroupsResults.getResults()[resultIndex++] = wsGetGroupsResult;
        
        try {
          //init in case error
          wsGetGroupsResult.setWsSubject(new WsSubject(wsSubjectLookup));
          Subject subject = wsSubjectLookup.retrieveSubject("subjectLookup");
          wsGetGroupsResult.setWsSubject(new WsSubject(subject, subjectAttributeNames));
          Member member = GrouperServiceUtils.convertSubjectToMember(session, subject);
      
          Set<Group> groups = memberFilter.getGroups(member);
          wsGetGroupsResult.assignGroupResult(groups, includeGroupDetail);
        } catch (Exception e) {
          wsGetGroupsResult.assignResultCodeException(null, null,wsSubjectLookup,  e);
        }
        
      }
  
      wsGetGroupsResults.tallyResults(theSummary);
      
    } catch (Exception e) {
      wsGetGroupsResults.assignResultCodeException(null, theSummary, e);
    } finally {
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
   * @return the result of one member add
   */
  public static WsGetGroupsLiteResult getGroupsLite(final GrouperWsVersion clientVersion, String subjectId,
      String subjectSourceId, String subjectIdentifier, WsMemberFilter memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, 
      boolean includeSubjectDetail, 
      String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the subject lookup
    WsSubjectLookup subjectLookup = new WsSubjectLookup(subjectId, subjectSourceId,
        subjectIdentifier);
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[]{subjectLookup};
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGetGroupsResults wsGetGroupsResults = getGroups(clientVersion, subjectLookups,
        memberFilter, actAsSubjectLookup, includeGroupDetail, includeSubjectDetail,
        subjectAttributeArray, params);
  
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
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetMembersResults getMembers(
      final GrouperWsVersion clientVersion,
      WsGroupLookup[] wsGroupLookups, WsMemberFilter memberFilter,
      WsSubjectLookup actAsSubjectLookup, final Field fieldName,
      boolean includeGroupDetail, 
      boolean includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params) {
  
    WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups,200) + "\n, memberFilter: " 
          + memberFilter
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);

      int resultIndex = 0;
      
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
      
      int groupLookupsLength = GrouperUtil.length(wsGroupLookups);
      wsGetMembersResults.setResults(new WsGetMembersResult[groupLookupsLength]);
      
      for (WsGroupLookup wsGroupLookup : GrouperUtil.nonNull(wsGroupLookups)) {
        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResults.getResults()[resultIndex++] = wsGetMembersResult;
        
        try {
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
          Set<Member> members = memberFilter.getMembers(group, fieldName);
      
          wsGetMembersResult.assignSubjectResult(members, subjectAttributeNamesToRetrieve);
      
        } catch (Exception e) {
          wsGetMembersResult.assignResultCodeException(null, null, wsGroupLookup, e);
        }
        
      }
  
      wsGetMembersResults.tallyResults(theSummary);
      
      
    } catch (Exception e) {
      wsGetMembersResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetMembersResults;
  }

  /**
   * get memberships from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   * @param membershipFilter
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
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetMembershipsResults getMemberships(final GrouperWsVersion clientVersion,
      WsGroupLookup wsGroupLookup, String membershipFilter,
      WsSubjectLookup actAsSubjectLookup, Field fieldName, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params) {
  
    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", membershipFilter: " + membershipFilter
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n";
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
  
      //assign the group to the result to be descriptive
      wsGetMembershipsResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
  
      WsMemberFilter wsMembershipFilter = GrouperServiceUtils
          .convertMemberFilter(membershipFilter);
  
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      // lets get the members, cant be null
      Set<Membership> memberships = wsMembershipFilter.getMemberships(group, fieldName);
  
      wsGetMembershipsResults.assignSubjectResult(memberships,
          subjectAttributeNamesToRetrieve);
  
      //see if all success
      wsGetMembershipsResults.tallyResults(theSummary);
  
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
   * @param membershipFilter
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
   * @return the memberships, or none if none found
   */
  public static WsGetMembershipsResults getMembershipsLite(final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, String membershipFilter,
      boolean includeSubjectDetail, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, Field fieldName, String subjectAttributeNames,
      boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // pass through to the more comprehensive method
    WsGetMembershipsResults wsGetMembershipsResults = getMemberships(clientVersion,
        wsGroupLookup, membershipFilter, actAsSubjectLookup, fieldName,
        includeSubjectDetail, subjectAttributeArray, includeGroupDetail,
        params);
  
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
   * @return the members, or no members if none found
   */
  public static WsGetMembersLiteResult getMembersLite(
      final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, WsMemberFilter memberFilter, 
      String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, 
      final Field fieldName,
      boolean includeGroupDetail, 
      boolean includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // pass through to the more comprehensive method
    WsGetMembersResults wsGetMembersResults = getMembers(clientVersion, wsGroupLookups,
        memberFilter, actAsSubjectLookup, fieldName, 
        includeGroupDetail, includeSubjectDetail,
        subjectAttributeArray, params);
  
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
  public static WsGroupDeleteResults groupDelete(final GrouperWsVersion clientVersion,
      final WsGroupLookup[] wsGroupLookups, final WsSubjectLookup actAsSubjectLookup,
      final GrouperTransactionType txType, final boolean includeGroupDetail, final WsParam[] params) {
  
    final WsGroupDeleteResults wsGroupDeleteResults = new WsGroupDeleteResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
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
                  GrouperWsConfig.WS_GROUP_DELETE_MAX, 1000000);
  
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
              if (!wsGroupDeleteResults.tallyResults(txType, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsGroupDeleteResults.assignResultCodeException(null, theSummary, e);
    } finally {
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
  public static WsGroupDeleteResults groupDeleteLite(final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier,
      final boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGroupDeleteResults wsGroupDeleteResults = groupDelete(clientVersion,
        wsGroupLookups, actAsSubjectLookup, null, includeGroupDetail, params);
  
    return wsGroupDeleteResults;
  }

  /**
   * save a group or many (insert or update).  Note, you cannot rename an existing group.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param wsGroupsToSave
   *            groups to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGroupSaveResults groupSave(final GrouperWsVersion clientVersion,
      final WsGroupToSave[] wsGroupsToSave, final WsSubjectLookup actAsSubjectLookup,
      final GrouperTransactionType txType, final WsParam[] params) {
  
    GrouperSession session = null;
    int groupsSize = wsGroupsToSave == null ? 0 : wsGroupsToSave.length;
  
    WsGroupSaveResults wsGroupSaveResults = new WsGroupSaveResults();
  
    //convert the options to a map for easy access, and validate them
    @SuppressWarnings("unused")
    Map<String, String> paramMap = null;
    try {
      paramMap = GrouperServiceUtils.convertParamsToMap(params);
    } catch (Exception e) {
      wsGroupSaveResults.assignResultCode(WsGroupSaveResultsCode.INVALID_QUERY);
      wsGroupSaveResults.getResultMetadata().setResultMessage(
          "Invalid params: " + e.getMessage());
      return wsGroupSaveResults;
    }
  
    // see if greater than the max (or default)
    int maxSaveGroup = GrouperWsConfig.getPropertyInt(GrouperWsConfig.WS_GROUP_SAVE_MAX,
        1000000);
    if (groupsSize > maxSaveGroup) {
      wsGroupSaveResults.assignResultCode(WsGroupSaveResultsCode.INVALID_QUERY);
      wsGroupSaveResults.getResultMetadata().appendResultMessage(
          "Number of groups must be less than max: " + maxSaveGroup + " (sent in "
              + groupsSize + ")");
      return wsGroupSaveResults;
    }
  
    // assume success
    wsGroupSaveResults.assignResultCode(WsGroupSaveResultsCode.SUCCESS);
    Subject actAsSubject = null;
    // TODO have common try/catch
    try {
      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
  
      if (actAsSubject == null) {
        // TODO make this a result code
        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
      }
  
      // use this to be the user connected, or the user act-as
      try {
        session = GrouperSession.start(actAsSubject);
      } catch (SessionException se) {
        // TODO make this a result code
        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
            se);
      }
  
      int resultIndex = 0;
  
      wsGroupSaveResults.setResults(new WsGroupSaveResult[groupsSize]);
      for (WsGroupToSave wsGroupToSave : wsGroupsToSave) {
        WsGroupSaveResult wsGroupSaveResult = new WsGroupSaveResult();
        wsGroupSaveResults.getResults()[resultIndex++] = wsGroupSaveResult;
        Group group = null;
  
        try {
          wsGroupSaveResult.setGroupName(wsGroupToSave.getGroupName());
          // TODO change this to groupUuid
          wsGroupSaveResult.setGroupUuid(wsGroupToSave.getUuid());
  
          try {
            wsGroupToSave.validate();
          } catch (Exception e) {
            wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.INVALID_QUERY);
            wsGroupSaveResult.getResultMetadata().setResultMessage(
                ExceptionUtils.getFullStackTrace(e));
            continue;
          }
  
          group = wsGroupToSave.save(session);
  
          // these will probably match, but just in case
          if (StringUtils.isBlank(wsGroupSaveResult.getGroupName())) {
            wsGroupSaveResult.setGroupName(group.getName());
          }
          if (StringUtils.isBlank(wsGroupSaveResult.getGroupUuid())) {
            wsGroupSaveResult.setGroupUuid(group.getUuid());
          }
          //TODO use the WsResultCode
          wsGroupSaveResult.getResultMetadata().assignSuccess("T");
          wsGroupSaveResult.getResultMetadata().assignResultCode("SUCCESS");
          wsGroupSaveResult.getResultMetadata().setResultMessage(
              "Group '" + group.getName() + "' was saved.");
        } catch (InsufficientPrivilegeException ipe) {
          wsGroupSaveResult
              .assignResultCode(WsGroupSaveResultCode.INSUFFICIENT_PRIVILEGES);
          wsGroupSaveResult.getResultMetadata().setResultMessage(
              "Error: insufficient privileges to save group '"
                  + wsGroupToSave.getGroupName() + "'");
        } catch (StemNotFoundException snfe) {
          wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.STEM_NOT_FOUND);
          wsGroupSaveResult.getResultMetadata().setResultMessage(
              "Error: stem not found to save group '" + wsGroupToSave.getGroupName()
                  + "'");
        } catch (GroupNotFoundException gnfe) {
          wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.GROUP_NOT_FOUND);
          wsGroupSaveResult.getResultMetadata().setResultMessage(
              "Error: group not found to save group '" + wsGroupToSave.getGroupName()
                  + "'");
        } catch (Exception e) {
          // lump the rest in there, group_add_exception, etc
          wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.EXCEPTION);
          wsGroupSaveResult.getResultMetadata().setResultMessage(
              ExceptionUtils.getFullStackTrace(e));
          LOG.error(wsGroupToSave + ", " + e, e);
        }
      }
  
    } catch (RuntimeException re) {
      wsGroupSaveResults.assignResultCode(WsGroupSaveResultsCode.EXCEPTION);
      String theError = "Problem saving groups: wsGroupsToSave: "
          + GrouperUtil.toStringForLog(wsGroupsToSave) + ", actAsSubject: "
          + actAsSubject + ".  \n" + "";
      wsGroupSaveResults.getResultMetadata().appendResultMessage(theError);
      // this is sent back to the caller anyway, so just log, and not send
      // back again
      LOG.error(theError + ", wsGroupSaveResults: "
          + GrouperUtil.toStringForLog(wsGroupSaveResults), re);
    } finally {
      if (session != null) {
        try {
          session.stop();
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  
    if (wsGroupSaveResults.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsGroupSaveResult wsGroupSaveResult : wsGroupSaveResults.getResults()) {
        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
            : wsGroupSaveResult.getResultMetadata().getSuccess());
        if (success) {
          successes++;
        } else {
          failures++;
        }
      }
      if (failures > 0) {
        wsGroupSaveResults.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of saving groups.   ");
        wsGroupSaveResults
            .assignResultCode(WsGroupSaveResultsCode.PROBLEM_DELETING_GROUPS);
      } else {
        wsGroupSaveResults.assignResultCode(WsGroupSaveResultsCode.SUCCESS);
      }
    }
    if (!"T".equalsIgnoreCase(wsGroupSaveResults.getResultMetadata().getSuccess())) {
  
      LOG.error(wsGroupSaveResults.getResultMetadata().getResultMessage());
    }
    return wsGroupSaveResults;
  }

  /**
   * save a group (insert or update).  Note you cannot rename an existing group.
   * 
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to delete the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to delete the group (mutually exclusive with groupName)
   * @param description
   *            of the group, empty will be ignored
   * @param displayExtension
   *            display name of the group, empty will be ignored
   * @param saveMode if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param createStemsIfNotExist
   *            if the stems should be created if not exist
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
  public static WsGroupSaveResults groupSaveLite(final GrouperWsVersion clientVersion, String groupName,
      String groupUuid, String description, String displayExtension, String saveMode,
      String createStemsIfNotExist, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupToSave wsGroupToSave = new WsGroupToSave(groupUuid, description,
        displayExtension, saveMode, createStemsIfNotExist, groupName);
    WsGroupToSave[] wsGroupsToSave = new WsGroupToSave[] { wsGroupToSave };
  
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGroupSaveResults wsGroupSaveResults = groupSave(clientVersion, wsGroupsToSave,
        actAsSubjectLookup, null, params);
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
  public static WsHasMemberResults hasMember(final GrouperWsVersion clientVersion,
      WsGroupLookup wsGroupLookup, WsSubjectLookup[] subjectLookups,
      WsMemberFilter memberFilter,
      WsSubjectLookup actAsSubjectLookup, Field fieldName,
      final boolean includeGroupDetail, boolean includeSubjectDetail, 
      String[] subjectAttributeNames, WsParam[] params) {
  
    WsHasMemberResults wsHasMemberResults = new WsHasMemberResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 200)
          + "\n memberFilter: "
          + memberFilter + ", actAsSubject: " + actAsSubjectLookup + ", fieldName: "
          + fieldName + ", includeGroupDetail: " + includeGroupDetail 
          + ", includeSubjectDetail: " + includeSubjectDetail
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n," +
          		"params: " + GrouperUtil.toStringForLog(params, 100) + "\n";
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
  
      //assign the group to the result to be descriptive
      wsHasMemberResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
  
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(subjectLookups,
          GrouperWsConfig.WS_HAS_MEMBER_SUBJECTS_MAX, 1000000);
  
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
  
          boolean hasMember = memberFilter.hasMember(group, wsSubjectLookup
              .retrieveSubject(), fieldName);
          wsHasMemberResult.assignResultCode(hasMember ? WsHasMemberResultCode.IS_MEMBER
              : WsHasMemberResultCode.IS_NOT_MEMBER);
  
        } catch (Exception e) {
          wsHasMemberResult.assignResultCodeException(e, wsSubjectLookup);
        }
      }
  
      //see if all success
      wsHasMemberResults.tallyResults(theSummary);
  
    } catch (Exception e) {
      wsHasMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
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
   * @return the result of one member query
   */
  public static WsHasMemberLiteResult hasMemberLite(final GrouperWsVersion clientVersion, String groupName,
      String groupUuid, String subjectId, String subjectSourceId, String subjectIdentifier,
      WsMemberFilter memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      Field fieldName, final boolean includeGroupDetail, boolean includeSubjectDetail, String subjectAttributeNames, 
      String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsHasMemberResults wsHasMemberResults = hasMember(clientVersion, wsGroupLookup,
        subjectLookups, memberFilter,
        actAsSubjectLookup, fieldName, includeGroupDetail, 
        includeSubjectDetail, subjectAttributeArray, params);
  
    return new WsHasMemberLiteResult(wsHasMemberResults);
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
  public static WsStemDeleteResults stemDelete(final GrouperWsVersion clientVersion,
      final WsStemLookup[] wsStemLookups, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsStemDeleteResults wsStemDeleteResults = new WsStemDeleteResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
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
                  GrouperWsConfig.WS_STEM_DELETE_MAX, 1000000);
  
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
  public static WsStemDeleteResults stemDeleteLite(final GrouperWsVersion clientVersion,
      String stemName, String stemUuid, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the stem lookup
    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    WsStemLookup[] wsStemLookups = new WsStemLookup[] { wsStemLookup };
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemDeleteResults wsStemDeleteResults = stemDelete(clientVersion, wsStemLookups,
        actAsSubjectLookup, null, params);
  
    return wsStemDeleteResults;
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
  public static WsStemSaveResults stemSave(final GrouperWsVersion clientVersion,
      final WsStemToSave[] wsStemToSaves, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsStemSaveResults wsStemSaveResults = new WsStemSaveResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsStemToSaves: "
          + GrouperUtil.toStringForLog(wsStemSaveResults, 200) + "\n, actAsSubject: "
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
                  wsStemToSaves, GrouperWsConfig.WS_STEM_SAVE_MAX, 1000000);
  
              wsStemSaveResults.setResults(new WsStemSaveResult[wsStemsLength]);
  
              int resultIndex = 0;
  
              //loop through all stems and do the save
              for (WsStemToSave wsStemToSave : wsStemToSaves) {
                WsStemSaveResult wsStemSaveResult = new WsStemSaveResult();
                wsStemSaveResults.getResults()[resultIndex++] = wsStemSaveResult;
                wsStemSaveResult.setWsStemLookup(wsStemToSave.getWsStemLookup());
  
                try {
                  //make sure everything is in order
                  wsStemToSave.validate();
                  Stem stem = wsStemToSave.save(SESSION);
  
                  wsStemSaveResult.setWsStem(new WsStem(stem));
                  wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS);
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsStemSaveResult
                      .assignResultCode(WsStemSaveResultCode.INSUFFICIENT_PRIVILEGES);
                } catch (Exception e) {
                  wsStemSaveResult.assignResultCodeException(e, wsStemToSave);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsStemSaveResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsStemSaveResults.assignResultCodeException(null, theSummary, e);
    } finally {
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
   * @param createStemsIfNotExist
   *            if the stems should be created if not exist
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
  public static WsStemSaveResults stemSaveLite(final GrouperWsVersion clientVersion,
      String stemLookupUuid, String stemLookupName, String stemName, String stemUuid,
      String description, String displayExtension, String saveMode,
      String createStemsIfNotExist, String actAsSubjectId, String actAsSubjectSourceId,
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
  
    wsStemToSave.setSaveMode(saveMode);
  
    WsStemToSave[] wsStemsToSave = new WsStemToSave[] { wsStemToSave };
  
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemSaveResults wsStemSaveResults = stemSave(clientVersion, wsStemsToSave,
        actAsSubjectLookup, null, params);
  
    return wsStemSaveResults;
  }

  /**
   * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
   * just view.  
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups
   *            groups to save
   * @param wsAttributeEdits are the attributes to change or delete
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsViewOrEditAttributesResults viewOrEditAttributes(final GrouperWsVersion clientVersion,
      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
      final WsSubjectLookup actAsSubjectLookup, final GrouperTransactionType txType,
      final WsParam[] params) {
  
    GrouperSession session = null;
    int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;
  
    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();
  
    //convert the options to a map for easy access, and validate them
    @SuppressWarnings("unused")
    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
        params);
  
    // see if greater than the max (or default)
    int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
        GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
    if (groupsSize > maxAttributeGroup) {
      wsViewOrEditAttributesResults
          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
          "Number of groups must be less than max: " + maxAttributeGroup + " (sent in "
              + groupsSize + ")");
      return wsViewOrEditAttributesResults;
    }
  
    // TODO make sure size of params and values the same
  
    //lets validate the attribute edits
    boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
    if (!readOnly) {
      for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
        String errorMessage = wsAttributeEdit.validate();
        if (errorMessage != null) {
          wsViewOrEditAttributesResults
              .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
          wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
              errorMessage + ", " + wsAttributeEdit);
        }
      }
    }
  
    // assume success
    wsViewOrEditAttributesResults
        .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
    Subject actAsSubject = null;
    // TODO have common try/catch
    try {
      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
  
      if (actAsSubject == null) {
        // TODO make this a result code
        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
      }
  
      // use this to be the user connected, or the user act-as
      try {
        session = GrouperSession.start(actAsSubject);
      } catch (SessionException se) {
        // TODO make this a result code
        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
            se);
      }
  
      int resultIndex = 0;
  
      wsViewOrEditAttributesResults
          .setResults(new WsViewOrEditAttributesResult[groupsSize]);
      GROUP_LOOP: for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
        WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
        wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
        Group group = null;
  
        try {
          wsViewOrEditAttributesResult.setGroupName(wsGroupLookup.getGroupName());
          wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
  
          //get the group
          wsGroupLookup.retrieveGroupIfNeeded(session);
          group = wsGroupLookup.retrieveGroup();
          if (group == null) {
            wsViewOrEditAttributesResult
                .assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
                "Cant find group: '" + wsGroupLookup + "'.  ");
            continue;
          }
  
          group = wsGroupLookup.retrieveGroup();
  
          // these will probably match, but just in case
          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
            wsViewOrEditAttributesResult.setGroupName(group.getName());
          }
          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
            wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
          }
  
          //lets read them
          Map<String, String> attributeMap = GrouperUtil.nonNull(group.getAttributes());
  
          //see if we are updating
          if (!readOnly) {
            for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
              String attributeName = wsAttributeEdit.getName();
              try {
                //lets see if delete
                if (wsAttributeEdit.deleteBoolean()) {
                  //if its not there, dont bother
                  if (attributeMap.containsKey(attributeName)) {
                    group.deleteAttribute(attributeName);
                    //update map
                    attributeMap.remove(attributeName);
                  }
                } else {
                  String attributeValue = wsAttributeEdit.getValue();
                  //make sure it is different
                  if (!StringUtils
                      .equals(attributeValue, attributeMap.get(attributeName))) {
                    //it is update
                    group.setAttribute(attributeName, wsAttributeEdit.getValue());
                    attributeMap.put(attributeName, attributeValue);
                  }
                }
              } catch (AttributeNotFoundException anfe) {
                wsViewOrEditAttributesResult
                    .assignResultCode(WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
                wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
                    "Cant find attribute: " + attributeName);
                //go to next group
                continue GROUP_LOOP;
  
              }
            }
          }
          //now take the attributes and put them in the result
          if (attributeMap.size() > 0) {
            int attributeIndex = 0;
            WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
            wsViewOrEditAttributesResult.setAttributes(attributes);
            //lookup each from map and return
            for (String key : attributeMap.keySet()) {
              WsAttribute wsAttribute = new WsAttribute();
              attributes[attributeIndex++] = wsAttribute;
              wsAttribute.setName(key);
              wsAttribute.setValue(attributeMap.get(key));
            }
          }
          wsViewOrEditAttributesResult.getResultMetadata().assignSuccess("T");
          wsViewOrEditAttributesResult.getResultMetadata().assignResultCode("SUCCESS");
          if (readOnly) {
            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
                "Group '" + group.getName() + "' was queried.");
          } else {
            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
                "Group '" + group.getName() + "' had attributes edited.");
          }
        } catch (InsufficientPrivilegeException ipe) {
          wsViewOrEditAttributesResult
              .assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
              "Error: insufficient privileges to view/edit attributes '"
                  + wsGroupLookup.getGroupName() + "'");
        } catch (Exception e) {
          // lump the rest in there, group_add_exception, etc
          wsViewOrEditAttributesResult
              .assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
              ExceptionUtils.getFullStackTrace(e));
          LOG.error(wsGroupLookup + ", " + e, e);
        }
      }
  
    } catch (RuntimeException re) {
      wsViewOrEditAttributesResults
          .assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
      String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
          + GrouperUtil.toStringForLog(wsGroupLookups) + ", attributeEdits: "
          + GrouperUtil.toStringForLog(wsAttributeEdits) + ", actAsSubject: "
          + actAsSubject + ".  \n" + "";
      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(theError);
      // this is sent back to the caller anyway, so just log, and not send
      // back again
      LOG.error(theError + ", wsViewOrEditAttributesResults: "
          + GrouperUtil.toStringForLog(wsViewOrEditAttributesResults), re);
    } finally {
      if (session != null) {
        try {
          session.stop();
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  
    if (wsViewOrEditAttributesResults.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
          .getResults()) {
        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
            : wsGroupSaveResult.getResultMetadata().getSuccess());
        if (success) {
          successes++;
        } else {
          failures++;
        }
      }
      if (failures > 0) {
        wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of viewing/editing group attribues.   ");
        wsViewOrEditAttributesResults
            .assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
      } else {
        wsViewOrEditAttributesResults
            .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
      }
    }
    if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getResultMetadata()
        .getSuccess())) {
  
      LOG.error(wsViewOrEditAttributesResults.getResultMetadata().getResultMessage());
    }
    return wsViewOrEditAttributesResults;
  }

  /**
   * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
   * just view.  
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to delete the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to delete the group (mutually exclusive with groupName)
   * @param attributeName0 name of first attribute (optional)
   * @param attributeValue0 value of first attribute (optional)
   * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
   * @param attributeName1 name of second attribute (optional)
   * @param attributeValue1 value of second attribute (optional)
   * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
   * @param attributeName2 name of third attribute (optional)
   * @param attributeValue2 value of third attribute (optional)
   * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
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
  public static WsViewOrEditAttributesResults viewOrEditAttributesLite(
      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
      String attributeName0, String attributeValue0, String attributeDelete0,
      String attributeName1, String attributeValue1, String attributeDelete1,
      String attributeName2, String attributeValue2, String attributeDelete2,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      String paramName0, String paramValue0, String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
  
    //setup attributes
    List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
    if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
        || !StringUtils.isBlank(attributeDelete0)) {
      attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0,
          attributeDelete0));
    }
    if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
        || !StringUtils.isBlank(attributeDelete1)) {
      attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1,
          attributeDelete1));
    }
    if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
        || !StringUtils.isBlank(attributeDelete2)) {
      attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2,
          attributeDelete2));
    }
    //convert to array
    WsAttributeEdit[] wsAttributeEdits = GrouperUtil.toArray(attributeEditList,
        WsAttributeEdit.class);
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(
        clientVersion, wsGroupLookups, wsAttributeEdits, actAsSubjectLookup, null,
        params);
  
    return wsViewOrEditAttributesResults;
  }

  /**
   * If all privilege params are empty, then it is viewonly. If any are set,
   * then the privileges will be set (and returned)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   *            for group which is related to the privileges
   * @param subjectLookups
   *            subjects to be added to the group
   * @param privileges is the array of privileges.  Each "allowed" field in there is either
   *            T for allowed, F for not allowed, blank for unchanged
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsViewOrEditPrivilegesResults viewOrEditPrivileges(final GrouperWsVersion clientVersion,
      final WsPrivilege[] privileges, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = new WsViewOrEditPrivilegesResults();
  
    GrouperSession session = null;
    String theSummary = null;
    //    try {
  
    txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.READ_WRITE_OR_USE_EXISTING);
    @SuppressWarnings("unused")
    final GrouperTransactionType TX_TYPE = txType;
    
    theSummary = "clientVersion: " + clientVersion + ", privileges: "
        + GrouperUtil.toStringForLog(privileges, 300) + ", actAsSubject: "
        + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
        + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
    @SuppressWarnings("unused")
    final String THE_SUMMARY = theSummary;
  
    //start session based on logged in user or the actAs passed in
    session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
    @SuppressWarnings("unused")
    final GrouperSession SESSION = session;
  
    //      //start a transaction (or not if none)
    //      GrouperTransaction.callbackGrouperTransaction(grouperTransactionType,
    //          new GrouperTransactionHandler() {
    //
    //            public Object callback(GrouperTransaction grouperTransaction)
    //                throws GrouperDAOException {
    //
    //              //convert the options to a map for easy access, and validate them
    //              @SuppressWarnings("unused")
    //              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
    //                  params);
    //
    //              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
    //                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);
    //
    //              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
    //
    //              boolean includeGroupDetail = GrouperServiceUtils.booleanValue(
    //                  includeGroupDetail, false, "includeGroupDetail");
    //
    //              //assign the group to the result to be descriptive
    //              wsViewOrEditPrivilegesResults.setWsGroupAssigned(new WsGroup(group,
    //                  includeGroupDetail));
    //
    //              int resultIndex = 0;
    //
    //              boolean replaceAllExistingBoolean = GrouperServiceUtils.booleanValue(
    //                  replaceAllExisting, false, "replaceAllExisting");
    //
    //              Set<Subject> newSubjects = new HashSet<Subject>();
    //              wsViewOrEditPrivilegesResults.setResults(new WsAddMemberResult[subjectLength]);
    //
    //              //get the field or null or invalid query exception
    //              Field field = GrouperServiceUtils.retrieveField(fieldName);
    //
    //              //get existing members if replacing
    //              Set<Member> members = null;
    //              if (replaceAllExistingBoolean) {
    //                try {
    //                  // see who is there
    //                  members = field == null ? group.getImmediateMembers() : group
    //                      .getImmediateMembers(field);
    //                } catch (SchemaException se) {
    //                  throw new WsInvalidQueryException(
    //                      "Problem with getting existing members: " + fieldName + ".  "
    //                          + ExceptionUtils.getFullStackTrace(se));
    //                }
    //              }
    //
    //              for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
    //                WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
    //                wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsAddMemberResult;
    //                try {
    //
    //                  Subject subject = wsSubjectLookup.retrieveSubject();
    //
    //                  wsAddMemberResult.processSubject(wsSubjectLookup);
    //
    //                  if (subject == null) {
    //                    continue;
    //                  }
    //
    //                  // keep track
    //                  if (replaceAllExistingBoolean) {
    //                    newSubjects.add(subject);
    //                  }
    //
    //                  try {
    //                    if (field != null) {
    //                      // dont fail if already a direct member
    //                      group.addMember(subject, false);
    //                    } else {
    //                      group.addMember(subject, field, false);
    //                    }
    //                    wsAddMemberResult.assignResultCode(WsAddMemberResultCode.SUCCESS);
    //
    //                  } catch (InsufficientPrivilegeException ipe) {
    //                    wsAddMemberResult
    //                        .assignResultCode(WsAddMemberResultCode.INSUFFICIENT_PRIVILEGES);
    //                  }
    //                } catch (Exception e) {
    //                  wsAddMemberResult.assignResultCodeException(e, wsSubjectLookup);
    //                }
    //              }
    //
    //              // after adding all these, see if we are removing:
    //              if (replaceAllExistingBoolean) {
    //
    //                for (Member member : members) {
    //                  Subject subject = null;
    //                  try {
    //                    subject = member.getSubject();
    //
    //                    if (!newSubjects.contains(subject)) {
    //                      if (field == null) {
    //                        group.deleteMember(subject);
    //                      } else {
    //                        group.deleteMember(subject, field);
    //                      }
    //                    }
    //                  } catch (Exception e) {
    //                    String theError = "Error deleting subject: " + subject
    //                        + " from group: " + group + ", field: " + field + ", " + e
    //                        + ".  ";
    //                    wsViewOrEditPrivilegesResults.assignResultCodeException(
    //                        WsAddMemberResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
    //                  }
    //                }
    //              }
    //              //see if any inner failures cause the whole tx to fail, and/or change the outer status
    //              if (!wsViewOrEditPrivilegesResults.tallyResults(grouperTransactionType, THE_SUMMARY)) {
    //                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
    //              }
    //                
    //
    //              return wsViewOrEditPrivilegesResults;
    //
    //            }
    //
    //          });
    //    } catch (Exception e) {
    //      wsViewOrEditPrivilegesResults.assignResultCodeException(null, theSummary, e);
    //    } finally {
    //      GrouperSession.stopQuietly(session);
    //    }
    //
    //
    //    //this should be the first and only return, or else it is exiting too early
    //    return wsViewOrEditPrivilegesResults;
    //
    //
    //    
    //    
    //    GrouperTransactionType grouperTransactionType = null;
    //
    //    //convert the options to a map for easy access, and validate them
    //    @SuppressWarnings("unused")
    //    Map<String, String> paramMap = null;
    //    try {
    //      paramMap = GrouperServiceUtils.convertParamsToMap(params);
    //    } catch (Exception e) {
    //      wsViewOrEditPrivilegesResults
    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
    //      wsViewOrEditPrivilegesResults.getResultMetadata().setResultMessage("Invalid params: " + e.getMessage());
    //      return wsViewOrEditPrivilegesResults;
    //    }
    //
    //    try {
    //      grouperTransactionType = GrouperUtil.defaultIfNull(GrouperTransactionType
    //          .valueOfIgnoreCase(txType), GrouperTransactionType.NONE);
    //    } catch (Exception e) {
    //      //a helpful exception will probably be in the getMessage()
    //      wsViewOrEditPrivilegesResults
    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
    //      wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("Invalid txType: '" + txType
    //          + "', " + e.getMessage());
    //      return wsViewOrEditPrivilegesResults;
    //    }
    //
    //    int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
    //    if (subjectLength == 0) {
    //      wsViewOrEditPrivilegesResults
    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
    //      wsViewOrEditPrivilegesResults
    //          .getResultMetadata().appendResultMessage("Subject length must be more than 1");
    //      return wsViewOrEditPrivilegesResults;
    //    }
    //
    //    // see if greater than the max (or default)
    //    int maxSavePrivileges = GrouperWsConfig.getPropertyInt(
    //        GrouperWsConfig.WS_VIEW_OR_EDIT_PRIVILEGES_SUBJECTS_MAX, 1000000);
    //    if (subjectLength > maxSavePrivileges) {
    //      wsViewOrEditPrivilegesResults
    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
    //      wsViewOrEditPrivilegesResults
    //          .getResultMetadata().appendResultMessage("Subject length must be less than max: "
    //              + maxSavePrivileges + " (sent in " + subjectLength + ")");
    //      return wsViewOrEditPrivilegesResults;
    //    }
    //
    //    // TODO make sure size of params and values the same
    //
    //    // assume success
    //    wsViewOrEditPrivilegesResults
    //        .assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
    //    Subject actAsSubject = null;
    //    try {
    //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
    //
    //      if (actAsSubject == null) {
    //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
    //      }
    //
    //      // use this to be the user connected, or the user act-as
    //      try {
    //        session = GrouperSession.start(actAsSubject);
    //      } catch (SessionException se) {
    //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
    //            se);
    //      }
    //      wsGroupLookup.retrieveGroupIfNeeded(session);
    //      Group group = wsGroupLookup.retrieveGroup();
    //
    //      if (group == null) {
    //        wsViewOrEditPrivilegesResults
    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
    //        wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("Cant find group: "
    //            + wsGroupLookup + ".  ");
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //      List<Privilege> privilegesToAssign = new ArrayList<Privilege>();
    //
    //      List<Privilege> privilegesToRevoke = new ArrayList<Privilege>();
    //
    //      // process the privilege inputs, keep in lists, handle invalid
    //      // queries
    //      if (!GrouperServiceUtils.processPrivilegesHelper(adminAllowed, "adminAllowed",
    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.ADMIN,
    //          wsViewOrEditPrivilegesResults)) {
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //      if (!GrouperServiceUtils.processPrivilegesHelper(optinAllowed, "optinAllowed",
    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.OPTIN,
    //          wsViewOrEditPrivilegesResults)) {
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //      if (!GrouperServiceUtils.processPrivilegesHelper(optoutAllowed, "optoutAllowed",
    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.OPTOUT,
    //          wsViewOrEditPrivilegesResults)) {
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //      if (!GrouperServiceUtils.processPrivilegesHelper(readAllowed, "readAllowed",
    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.READ,
    //          wsViewOrEditPrivilegesResults)) {
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //      if (!GrouperServiceUtils.processPrivilegesHelper(updateAllowed, "updateAllowed",
    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.UPDATE,
    //          wsViewOrEditPrivilegesResults)) {
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //      if (!GrouperServiceUtils.processPrivilegesHelper(viewAllowed, "viewAllowed",
    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.VIEW,
    //          wsViewOrEditPrivilegesResults)) {
    //        return wsViewOrEditPrivilegesResults;
    //      }
    //
    //      int resultIndex = 0;
    //
    //      wsViewOrEditPrivilegesResults
    //          .setResults(new WsViewOrEditPrivilegesResult[subjectLength]);
    //
    //      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
    //        WsPrivilege wsPrivilege = new WsPrivilege();
    //        WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = new WsViewOrEditPrivilegesResult();
    //        wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsViewOrEditPrivilegesResult;
    //        wsViewOrEditPrivilegesResult.setWsPrivilege(wsPrivilege);
    //        try {
    //          wsPrivilege.setSubjectId(wsSubjectLookup.getSubjectId());
    //          wsPrivilege.setSubjectIdentifier(wsSubjectLookup
    //              .getSubjectIdentifier());
    //
    //          Subject subject = wsSubjectLookup.retrieveSubject();
    //
    //          // make sure the subject is there
    //          if (subject == null) {
    //            // see why not
    //            SubjectFindResult subjectFindResult = wsSubjectLookup
    //                .retrieveSubjectFindResult();
    //            String error = "Subject: " + wsSubjectLookup + " had problems: "
    //                + subjectFindResult;
    //            wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(error);
    //            if (SubjectFindResult.SUBJECT_NOT_FOUND.equals(subjectFindResult)) {
    //              wsViewOrEditPrivilegesResult
    //                  .assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_NOT_FOUND);
    //              continue;
    //            }
    //            if (SubjectFindResult.SUBJECT_DUPLICATE.equals(subjectFindResult)) {
    //              wsViewOrEditPrivilegesResult
    //                  .assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_DUPLICATE);
    //              continue;
    //            }
    //            throw new NullPointerException(error);
    //          }
    //
    //          // these will probably match, but just in case
    //          if (StringUtils.isBlank(wsPrivilege.getSubjectId())) {
    //            wsPrivilege.setSubjectId(subject.getId());
    //          }
    //
    //          try {
    //            // lets get all the privileges for the group and user
    //            Set<AccessPrivilege> accessPrivileges = GrouperUtil.nonNull(group
    //                .getPrivs(subject));
    //
    //            // TODO keep track of isRevokable? Also, can you remove
    //            // a read priv? I tried and got exception
    //
    //            // see what we really need to do. At the end, the
    //            // currentAccessPrivileges should be what it looks like
    //            // afterward
    //            // (add in assignments, remove revokes),
    //            // the privilegestoAssign will be what to assign (take
    //            // out what is already there)
    //            Set<Privilege> currentPrivilegesSet = GrouperServiceUtils
    //                .convertAccessPrivilegesToPrivileges(accessPrivileges);
    //
    //            List<Privilege> privilegesToAssignToThisSubject = new ArrayList<Privilege>(
    //                privilegesToAssign);
    //            List<Privilege> privilegesToRevokeFromThisSubject = new ArrayList<Privilege>(
    //                privilegesToRevoke);
    //
    //            // dont assign ones already in there
    //            privilegesToAssignToThisSubject.removeAll(currentPrivilegesSet);
    //            // dont revoke ones not in there
    //            privilegesToRevokeFromThisSubject.retainAll(currentPrivilegesSet);
    //            // assign
    //            for (Privilege privilegeToAssign : privilegesToAssignToThisSubject) {
    //              group.grantPriv(subject, privilegeToAssign);
    //            }
    //            // revoke
    //            for (Privilege privilegeToRevoke : privilegesToRevokeFromThisSubject) {
    //              group.revokePriv(subject, privilegeToRevoke);
    //            }
    //            // reset the current privileges set to reflect the new
    //            // state
    //            currentPrivilegesSet.addAll(privilegesToAssignToThisSubject);
    //            currentPrivilegesSet.removeAll(privilegesToRevokeFromThisSubject);
    //
    //            wsPrivilege.setAdminAllowed(GrouperServiceUtils
    //                .booleanToStringOneChar(currentPrivilegesSet
    //                    .contains(AccessPrivilege.ADMIN)));
    //            wsPrivilege.setOptinAllowed(GrouperServiceUtils
    //                .booleanToStringOneChar(currentPrivilegesSet
    //                    .contains(AccessPrivilege.OPTIN)));
    //            wsPrivilege.setOptoutAllowed(GrouperServiceUtils
    //                .booleanToStringOneChar(currentPrivilegesSet
    //                    .contains(AccessPrivilege.OPTOUT)));
    //            wsPrivilege.setReadAllowed(GrouperServiceUtils
    //                .booleanToStringOneChar(currentPrivilegesSet
    //                    .contains(AccessPrivilege.READ)));
    //            wsPrivilege.setViewAllowed(GrouperServiceUtils
    //                .booleanToStringOneChar(currentPrivilegesSet
    //                    .contains(AccessPrivilege.VIEW)));
    //            wsPrivilege.setUpdateAllowed(GrouperServiceUtils
    //                .booleanToStringOneChar(currentPrivilegesSet
    //                    .contains(AccessPrivilege.UPDATE)));
    //
    //            wsViewOrEditPrivilegesResult
    //                .assignResultCode(WsViewOrEditPrivilegesResultCode.SUCCESS);
    //          } catch (InsufficientPrivilegeException ipe) {
    //            wsViewOrEditPrivilegesResult
    //                .assignResultCode(WsViewOrEditPrivilegesResultCode.INSUFFICIENT_PRIVILEGES);
    //            wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(ExceptionUtils
    //                .getFullStackTrace(ipe));
    //          }
    //        } catch (Exception e) {
    //          wsViewOrEditPrivilegesResult.getResultMetadata().setResultCode("EXCEPTION");
    //          wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(ExceptionUtils
    //              .getFullStackTrace(e));
    //          LOG.error(wsSubjectLookup + ", " + e, e);
    //        }
    //
    //      }
    //    } catch (RuntimeException re) {
    //      wsViewOrEditPrivilegesResults
    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.EXCEPTION);
    //      String theError = "Problem with privileges for member and group: wsGroupLookup: "
    //          + wsGroupLookup + ", subjectLookups: "
    //          + GrouperUtil.toStringForLog(subjectLookups) + ", actAsSubject: "
    //          + actAsSubject + ", admin: '" + adminAllowed + "', optin: '" + optinAllowed
    //          + "', optout: '" + optoutAllowed + "', read: '" + readAllowed + "', update: '"
    //          + updateAllowed + "', view: '" + viewAllowed + ".  ";
    //      wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage(theError + "\n"
    //          + ExceptionUtils.getFullStackTrace(re));
    //      // this is sent back to the caller anyway, so just log, and not send
    //      // back again
    //      LOG.error(theError + ", wsViewOrEditPrivilegesResults: "
    //          + GrouperUtil.toStringForLog(wsViewOrEditPrivilegesResults), re);
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
    //    if (wsViewOrEditPrivilegesResults.getResults() != null) {
    //      // check all entries
    //      int successes = 0;
    //      int failures = 0;
    //      for (WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult : wsViewOrEditPrivilegesResults
    //          .getResults()) {
    //        boolean success = "T".equalsIgnoreCase(wsViewOrEditPrivilegesResult.getResultMetadata().getSuccess());
    //        if (success) {
    //          successes++;
    //        } else {
    //          failures++;
    //        }
    //      }
    //      if (failures > 0) {
    //        wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("There were " + successes
    //            + " successes and " + failures
    //            + " failures of user group privileges operations.   ");
    //        wsViewOrEditPrivilegesResults
    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.PROBLEM_WITH_MEMBERS);
    //      } else {
    //        wsViewOrEditPrivilegesResults
    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
    //      }
    //    }
    //    if (!"T".equalsIgnoreCase(wsViewOrEditPrivilegesResults.getResultMetadata().getSuccess())) {
    //
    //      LOG.error(wsViewOrEditPrivilegesResults.getResultMetadata().getResultMessage());
    //    }
    return wsViewOrEditPrivilegesResults;
  }

  /**
   * If all privilege params are empty, then it is viewonly. If any are set,
   * then the privileges will be set (and returned)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to assign (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to assign (mutually exclusive with subjectId)
   * @param adminAllowed
   *            T for allowed, F for not allowed, blank for unchanged
   * @param optinAllowed
   *            T for allowed, F for not allowed, blank for unchanged
   * @param optoutAllowed
   *            T for allowed, F for not allowed, blank for unchanged
   * @param readAllowed
   *            T for allowed, F for not allowed, blank for unchanged
   * @param viewAllowed
   *            T for allowed, F for not allowed, blank for unchanged
   * @param updateAllowed
   *            T for allowed, F for not allowed, blank for unchanged
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
  public static WsViewOrEditPrivilegesResults viewOrEditPrivilegesLite(
      final GrouperWsVersion clientVersion, String groupName, String groupUuid, String subjectId,
      String subjectSourceId, String subjectIdentifier, String adminAllowed,
      String optinAllowed, String optoutAllowed, String readAllowed,
      String updateAllowed, String viewAllowed, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    // setup the subject lookup
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, subjectSourceId,
        subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsPrivilege wsPrivilege = new WsPrivilege();
    wsPrivilege.setSubjectLookup(wsSubjectLookup);
    wsPrivilege.setWsGroupLookup(wsGroupLookup);
    wsPrivilege.setAdminAllowed(adminAllowed);
    wsPrivilege.setOptinAllowed(optinAllowed);
    wsPrivilege.setOptoutAllowed(optoutAllowed);
    wsPrivilege.setReadAllowed(readAllowed);
    wsPrivilege.setUpdateAllowed(updateAllowed);
    wsPrivilege.setViewAllowed(viewAllowed);
  
    WsPrivilege[] wsPrivileges = { wsPrivilege };
  
    WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = viewOrEditPrivileges(
        clientVersion, wsPrivileges, actAsSubjectLookup, null, params);
  
    return wsViewOrEditPrivilegesResults;
  
  }

}
