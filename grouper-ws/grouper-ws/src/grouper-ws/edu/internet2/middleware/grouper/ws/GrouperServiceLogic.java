/*
 * @author mchyzer $Id: GrouperServiceLogic.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberSimpleResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * Meant to be delegate from GrouperService which has the same params
 * with enums translated for each Javadoc viewing.
 */
public class GrouperServiceLogic {

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
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params
   *            optional: reserved for future use
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @return the results.  return the subject lookup only if there are problems retrieving the subject.
   * @see GrouperWsVersion
   */
  @SuppressWarnings("unchecked")
  public static WsAddMemberResults addMember(final GrouperWsVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final boolean replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
      final Field fieldName, final GrouperTransactionType txType,
      final boolean includeGroupDetail, final boolean includeSubjectDetail,
      final String[] subjectAttributeNames, final WsParam[] params) {
    final WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();

    GrouperSession session = null;
    String theSummary = null;
    try {

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
                  .setWsGroupAssigned(new WsGroup(group, includeGroupDetail));

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
              if (!wsAddMemberResults.tallyResults(txType, THE_SUMMARY)) {
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
   * @param subjectSource is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to add (mutually exclusive with subjectId)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSource is source of act as subject to narrow the result and prevent
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
  public static WsAddMemberSimpleResult addMemberSimple(
      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
      String subjectId, String subjectSource, String subjectIdentifier,
      String actAsSubjectId, String actAsSubjectSource, String actAsSubjectIdentifier,
      Field fieldName, boolean includeGroupDetail, boolean includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSource, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
        actAsSubjectSource, actAsSubjectIdentifier);

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsAddMemberResults wsAddMemberResults = addMember(clientVersion, wsGroupLookup,
        subjectLookups, false, actAsSubjectLookup, fieldName, null, includeGroupDetail,
        includeSubjectDetail, subjectAttributeArray, params);

    WsAddMemberSimpleResult wsAddMemberSimpleResult = new WsAddMemberSimpleResult(
        wsAddMemberResults);
    return wsAddMemberSimpleResult;
  }

}
