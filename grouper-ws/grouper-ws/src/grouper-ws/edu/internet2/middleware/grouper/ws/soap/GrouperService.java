package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * All public methods in this class are available in the web service
 * as both SOAP and REST.
 * 
 * This is the class that Axis uses to generate the WSDL.  Also this is the 
 * class that request/response objects are generated from for REST (each param
 * is a field in the object, each method is an object).
 * 
 * Each method in this class has an outer try/catch that does error handling, 
 * it decodes enums and looks things up, then delegates to GrouperServiceLogic
 * for the real business logic.  In that class the Lite methods delegate to the
 * real methods
 * 
 * booleans can either be T, F, true, false (case-insensitive)
 * 
 * get wsdl from: http://localhost:8090/grouper/services/GrouperService?wsdl
 * 
 * generate client (after wsdl copied): C:\mchyzer\isc\dev\grouper\axisJar2&gt;wsdl2java -p edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 * 
 * @author mchyzer
 * </pre>
 */
public class GrouperService {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(GrouperService.class);

  /**
   * find a group or groups
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param queryFilterType findGroupType is the WsQueryFilterType enum for which 
   * type of find is happening:  e.g.
   * FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_GROUP_NAME_APPROXIMATE, 
   * FIND_BY_TYPE, AND, OR, MINUS;
   * @param groupName search by group name (must match exactly), cannot use other
   *            params with this
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
  public WsFindGroupsResults findGroupsLite(final String clientVersion,
      String queryFilterType, String groupName, String stemName, String stemNameScope,
      String groupUuid, String groupAttributeName, String groupAttributeValue,
      String groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      StemScope stemScope = StemScope.valueOfIgnoreCase(stemNameScope);
      WsQueryFilterType wsQueryFilterType = WsQueryFilterType.valueOfIgnoreCase(queryFilterType);

      GroupType groupType = GrouperServiceUtils.retrieveGroupType(groupTypeName);

      wsFindGroupsResults = GrouperServiceLogic.findGroupsLite(grouperWsVersion, wsQueryFilterType, 
          groupName, stemName, stemScope, groupUuid, groupAttributeName, groupAttributeValue,
          groupType,actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindGroupsResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
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
  public WsFindStemsResults findStems(final String clientVersion,
      WsStemQueryFilter wsStemQueryFilter, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params) {

    WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();

    try {

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsFindStemsResults = GrouperServiceLogic.findStems(grouperWsVersion, wsStemQueryFilter, 
          actAsSubjectLookup, params);
    } catch (Exception e) {
      wsFindStemsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindStemsResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsFindStemsResults;

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
  public WsFindGroupsResults findGroups(final String clientVersion,
      WsQueryFilter wsQueryFilter, 
      WsSubjectLookup actAsSubjectLookup, 
      String includeGroupDetail, WsParam[] params) {

    WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsFindGroupsResults = GrouperServiceLogic.findGroups(grouperWsVersion, wsQueryFilter, actAsSubjectLookup, 
          includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindGroupsResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsFindGroupsResults;

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
//  public WsGetMembershipsResults getMembershipsLite(final String clientVersion,
//      String groupName, String groupUuid, String membershipFilter,
//      String includeSubjectDetail, String actAsSubjectId, String actAsSubjectSourceId,
//      String actAsSubjectIdentifier, String fieldName, String subjectAttributeNames,
//      String includeGroupDetail, String paramName0, String paramValue0,
//      String paramName1, String paramValue1) {
//
//    // setup the group lookup
//    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
//
//    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
//        actAsSubjectSourceId, actAsSubjectIdentifier);
//
//    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
//
//    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
//
//    // pass through to the more comprehensive method
//    WsGetMembershipsResults wsGetMembershipsResults = getMemberships(clientVersion,
//        wsGroupLookup, membershipFilter, actAsSubjectLookup, fieldName,
//        includeSubjectDetail, subjectAttributeArray, includeGroupDetail,
//        params);
//
//    return wsGetMembershipsResults;
//  }

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
  public WsGetMembersLiteResult getMembersLite(final String clientVersion,
      String groupName, String groupUuid, String memberFilter, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsGetMembersLiteResult wsGetMembersLiteResult = new WsGetMembersLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);

      wsGetMembersLiteResult = GrouperServiceLogic.getMembersLite(grouperWsVersion, 
          groupName, groupUuid, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1);
    } catch (Exception e) {
      wsGetMembersLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetMembersLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGetMembersLiteResult;

  }

  /**
   * get members from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups are groups to query
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
  public WsGetMembersResults getMembers(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, String memberFilter,
      WsSubjectLookup actAsSubjectLookup, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params) {
  
    WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
  
    try {
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);
  
      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
  
      wsGetMembersResults = GrouperServiceLogic.getMembers(grouperWsVersion, wsGroupLookups, 
          wsMemberFilter, actAsSubjectLookup, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params);
    } catch (Exception e) {
      wsGetMembersResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetMembersResults.getResultMetadata());
  
    //this should be the first and only return, or else it is exiting too early
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
//  @SuppressWarnings("unchecked")
//  public WsGetMembershipsResults getMemberships(final String clientVersion,
//      WsGroupLookup wsGroupLookup, String membershipFilter,
//      WsSubjectLookup actAsSubjectLookup, String fieldName, String includeSubjectDetail,
//      String[] subjectAttributeNames, String includeGroupDetail, final WsParam[] params) {
//
//    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
//
//    GrouperSession session = null;
//    String theSummary = null;
//    try {
//
//      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
//          + wsGroupLookup + ", membershipFilter: " + membershipFilter
//          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
//          + actAsSubjectLookup + ", fieldName: " + fieldName
//          + ", subjectAttributeNames: "
//          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
//          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n";
//
//      //start session based on logged in user or the actAs passed in
//      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
//
//      //convert the options to a map for easy access, and validate them
//      @SuppressWarnings("unused")
//      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
//          params);
//
//      Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
//
//      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
//          includeGroupDetail, false, "includeGroupDetail");
//
//      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
//          includeSubjectDetail, false, "includeSubjectDetail");
//
//      //assign the group to the result to be descriptive
//      wsGetMembershipsResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetailBoolean));
//
//      WsMemberFilter wsMembershipFilter = GrouperServiceUtils
//          .convertMemberFilter(membershipFilter);
//
//      //get the field or null or invalid query exception
//      Field field = GrouperServiceUtils.retrieveField(fieldName);
//
//      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
//          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetailBoolean);
//
//      // lets get the members, cant be null
//      Set<Membership> memberships = wsMembershipFilter.getMemberships(group, field);
//
//      wsGetMembershipsResults.assignSubjectResult(memberships,
//          subjectAttributeNamesToRetrieve);
//
//      //see if all success
//      wsGetMembershipsResults.tallyResults(theSummary);
//
//    } catch (Exception e) {
//      wsGetMembershipsResults.assignResultCodeException(null, theSummary, e);
//    } finally {
//      GrouperSession.stopQuietly(session);
//    }
//
//    //set response headers
//    GrouperServiceUtils.addResponseHeaders(wsGetMembershipsResults.getResultMetadata());
//
//    return wsGetMembershipsResults;
//
//  }

  /**
   * get groups from members based on filter (accepts batch of members)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectLookups
   *            subjects to be examined to get groups
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetGroupsResults getGroups(final String clientVersion,
      WsSubjectLookup[] subjectLookups, String memberFilter, 
      WsSubjectLookup actAsSubjectLookup, String includeGroupDetail,
      String includeSubjectDetail, String[] subjectAttributeNames, 
      WsParam[] params) {
    
    WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      wsGetGroupsResults = GrouperServiceLogic.getGroups(grouperWsVersion, subjectLookups, 
          wsMemberFilter, actAsSubjectLookup, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params);
    } catch (Exception e) {
      wsGetGroupsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetGroupsResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGetGroupsResults;

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
  public WsHasMemberResults hasMember(final String clientVersion,
      WsGroupLookup wsGroupLookup, WsSubjectLookup[] subjectLookups,
      String memberFilter,
      WsSubjectLookup actAsSubjectLookup, String fieldName,
      final String includeGroupDetail, 
      String includeSubjectDetail, String[] subjectAttributeNames, 
      WsParam[] params) {

    WsHasMemberResults wsHasMemberResults = new WsHasMemberResults();

    try {

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsHasMemberResults = GrouperServiceLogic.hasMember(grouperWsVersion, wsGroupLookup,
          subjectLookups, wsMemberFilter, actAsSubjectLookup, field,
          includeGroupDetailBoolean, includeSubjectDetailBoolean,
          subjectAttributeNames, params);
    } catch (Exception e) {
      wsHasMemberResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsHasMemberResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsHasMemberResults;

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
  public WsStemDeleteLiteResult stemDeleteLite(final String clientVersion,
      String stemName, String stemUuid, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsStemDeleteLiteResult wsStemDeleteLiteResult = new WsStemDeleteLiteResult();

    try {

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsStemDeleteLiteResult = GrouperServiceLogic.stemDeleteLite(grouperWsVersion, stemName, stemUuid, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsStemDeleteLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemDeleteLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsStemDeleteLiteResult;
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
  public WsGroupDeleteLiteResult groupDeleteLite(final String clientVersion,
      String groupName, String groupUuid, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier,
      final String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsGroupDeleteLiteResult wsGroupDeleteLiteResult = new WsGroupDeleteLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsGroupDeleteLiteResult = GrouperServiceLogic.groupDeleteLite(grouperWsVersion, groupName,
          groupUuid, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsGroupDeleteLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupDeleteLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGroupDeleteLiteResult;

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
//  public WsViewOrEditAttributesResults viewOrEditAttributesLite(
//      final String clientVersion, String groupName, String groupUuid,
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

  /**
   * save a stem (insert or update).  Note you cannot currently move an existing group.
   * 
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupLookupUuid the uuid of the group to edit (mutually exclusive with groupLookupName)
   * @param groupLookupName the name of the group to edit (mutually exclusive with groupLookupUuid)
   * @param groupName
   *            to delete the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to delete the group (mutually exclusive with groupName)
   * @param description
   *            of the group, empty will be ignored
   * @param displayExtension
   *            display name of the group, empty will be ignored
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
   * @return the result of one member add
   */
  public WsGroupSaveLiteResult groupSaveLite(final String clientVersion,
      String groupLookupUuid, String groupLookupName, String groupUuid,String groupName, 
      String displayExtension,String description,  String saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsGroupSaveLiteResult wsGroupSaveLiteResult = new WsGroupSaveLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      wsGroupSaveLiteResult = GrouperServiceLogic.groupSaveLite(grouperWsVersion, groupLookupUuid,
          groupLookupName, groupUuid, groupName, displayExtension, description, saveModeEnum,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean,  
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsGroupSaveLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupSaveLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGroupSaveLiteResult;
  }

  
  /**
   * save a stem (insert or update).  Note you cannot move an existing stem.
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
  public WsStemSaveLiteResult stemSaveLite(final String clientVersion,
      String stemLookupUuid, String stemLookupName, String stemUuid, String stemName, 
      String displayExtension, String description, String saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsStemSaveLiteResult wsStemSaveLiteResult = new WsStemSaveLiteResult();

    try {

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      wsStemSaveLiteResult = GrouperServiceLogic.stemSaveLite(grouperWsVersion, stemLookupUuid,
          stemLookupName, stemUuid, stemName, displayExtension, description, saveModeEnum,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsStemSaveLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemSaveLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsStemSaveLiteResult;
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
//  @SuppressWarnings("unchecked")
//  public WsViewOrEditAttributesResults viewOrEditAttributes(final String clientVersion,
//      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
//      final WsSubjectLookup actAsSubjectLookup, final String txType,
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
//    GrouperTransactionType grouperTransactionType = null;
//    try {
//      grouperTransactionType = GrouperUtil.defaultIfNull(GrouperTransactionType
//          .valueOfIgnoreCase(txType), GrouperTransactionType.NONE);
//    } catch (Exception e) {
//      //a helpful exception will probably be in the getMessage()
//      wsViewOrEditAttributesResults
//          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
//      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
//          "Invalid txType: '" + txType + "', " + e.getMessage());
//      return wsViewOrEditAttributesResults;
//    }
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
  public WsGroupSaveResults groupSave(final String clientVersion,
      final WsGroupToSave[] wsGroupToSaves, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final String includeGroupDetail, final WsParam[] params) {

    WsGroupSaveResults wsGroupSaveResults = new WsGroupSaveResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      wsGroupSaveResults = GrouperServiceLogic.groupSave(grouperWsVersion, wsGroupToSaves,
          actAsSubjectLookup, grouperTransactionType, includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsGroupSaveResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupSaveResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGroupSaveResults;

    
  }

  /**
   * save a stem or many (insert or update).  Note, you cannot move an existing stem.
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
  public WsStemSaveResults stemSave(final String clientVersion,
      final WsStemToSave[] wsStemToSaves, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {

    WsStemSaveResults wsStemSaveResults = new WsStemSaveResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsStemSaveResults = GrouperServiceLogic.stemSave(grouperWsVersion, wsStemToSaves,
          actAsSubjectLookup, 
          grouperTransactionType, params);
    } catch (Exception e) {
      wsStemSaveResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemSaveResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsStemSaveResults;
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
  public WsStemDeleteResults stemDelete(final String clientVersion,
      final WsStemLookup[] wsStemLookups, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {

    WsStemDeleteResults wsStemDeleteResults = new WsStemDeleteResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsStemDeleteResults = GrouperServiceLogic.stemDelete(grouperWsVersion, wsStemLookups,
          actAsSubjectLookup, 
          grouperTransactionType, params);
    } catch (Exception e) {
      wsStemDeleteResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemDeleteResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsStemDeleteResults;

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
  public WsGroupDeleteResults groupDelete(final String clientVersion,
      final WsGroupLookup[] wsGroupLookups, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final String includeGroupDetail, final WsParam[] params) {

    WsGroupDeleteResults wsGroupDeleteResults = new WsGroupDeleteResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsGroupDeleteResults = GrouperServiceLogic.groupDelete(grouperWsVersion, wsGroupLookups,
          actAsSubjectLookup, 
          grouperTransactionType, includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsGroupDeleteResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupDeleteResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGroupDeleteResults;
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
//  @SuppressWarnings("unchecked")
//  public WsViewOrEditPrivilegesResults viewOrEditPrivileges(final String clientVersion,
//      final WsPrivilege[] privileges, final WsSubjectLookup actAsSubjectLookup,
//      final String txType, final WsParam[] params) {
//
//    final WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = new WsViewOrEditPrivilegesResults();
//
//    GrouperSession session = null;
//    String theSummary = null;
//    //    try {
//
//    theSummary = "clientVersion: " + clientVersion + ", privileges: "
//        + GrouperUtil.toStringForLog(privileges, 300) + ", actAsSubject: "
//        + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
//        + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
//
//    final String THE_SUMMARY = theSummary;
//
//    //start session based on logged in user or the actAs passed in
//    session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
//
//    final GrouperSession SESSION = session;
//
//    //convert tx type to object
//    final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
//        .convertTransactionType(txType);
//
//    //      //start a transaction (or not if none)
//    //      GrouperTransaction.callbackGrouperTransaction(grouperTransactionType,
//    //          new GrouperTransactionHandler() {
//    //
//    //            public Object callback(GrouperTransaction grouperTransaction)
//    //                throws GrouperDAOException {
//    //
//    //              //convert the options to a map for easy access, and validate them
//    //              @SuppressWarnings("unused")
//    //              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
//    //                  params);
//    //
//    //              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
//    //                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);
//    //
//    //              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
//    //
//    //              boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
//    //                  includeGroupDetail, false, "includeGroupDetail");
//    //
//    //              //assign the group to the result to be descriptive
//    //              wsViewOrEditPrivilegesResults.setWsGroupAssigned(new WsGroup(group,
//    //                  includeGroupDetailBoolean));
//    //
//    //              int resultIndex = 0;
//    //
//    //              boolean replaceAllExistingBoolean = GrouperServiceUtils.booleanValue(
//    //                  replaceAllExisting, false, "replaceAllExisting");
//    //
//    //              Set<Subject> newSubjects = new HashSet<Subject>();
//    //              wsViewOrEditPrivilegesResults.setResults(new WsAddMemberResult[subjectLength]);
//    //
//    //              //get the field or null or invalid query exception
//    //              Field field = GrouperServiceUtils.retrieveField(fieldName);
//    //
//    //              //get existing members if replacing
//    //              Set<Member> members = null;
//    //              if (replaceAllExistingBoolean) {
//    //                try {
//    //                  // see who is there
//    //                  members = field == null ? group.getImmediateMembers() : group
//    //                      .getImmediateMembers(field);
//    //                } catch (SchemaException se) {
//    //                  throw new WsInvalidQueryException(
//    //                      "Problem with getting existing members: " + fieldName + ".  "
//    //                          + ExceptionUtils.getFullStackTrace(se));
//    //                }
//    //              }
//    //
//    //              for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
//    //                WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
//    //                wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsAddMemberResult;
//    //                try {
//    //
//    //                  Subject subject = wsSubjectLookup.retrieveSubject();
//    //
//    //                  wsAddMemberResult.processSubject(wsSubjectLookup);
//    //
//    //                  if (subject == null) {
//    //                    continue;
//    //                  }
//    //
//    //                  // keep track
//    //                  if (replaceAllExistingBoolean) {
//    //                    newSubjects.add(subject);
//    //                  }
//    //
//    //                  try {
//    //                    if (field != null) {
//    //                      // dont fail if already a direct member
//    //                      group.addMember(subject, false);
//    //                    } else {
//    //                      group.addMember(subject, field, false);
//    //                    }
//    //                    wsAddMemberResult.assignResultCode(WsAddMemberResultCode.SUCCESS);
//    //
//    //                  } catch (InsufficientPrivilegeException ipe) {
//    //                    wsAddMemberResult
//    //                        .assignResultCode(WsAddMemberResultCode.INSUFFICIENT_PRIVILEGES);
//    //                  }
//    //                } catch (Exception e) {
//    //                  wsAddMemberResult.assignResultCodeException(e, wsSubjectLookup);
//    //                }
//    //              }
//    //
//    //              // after adding all these, see if we are removing:
//    //              if (replaceAllExistingBoolean) {
//    //
//    //                for (Member member : members) {
//    //                  Subject subject = null;
//    //                  try {
//    //                    subject = member.getSubject();
//    //
//    //                    if (!newSubjects.contains(subject)) {
//    //                      if (field == null) {
//    //                        group.deleteMember(subject);
//    //                      } else {
//    //                        group.deleteMember(subject, field);
//    //                      }
//    //                    }
//    //                  } catch (Exception e) {
//    //                    String theError = "Error deleting subject: " + subject
//    //                        + " from group: " + group + ", field: " + field + ", " + e
//    //                        + ".  ";
//    //                    wsViewOrEditPrivilegesResults.assignResultCodeException(
//    //                        WsAddMemberResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
//    //                  }
//    //                }
//    //              }
//    //              //see if any inner failures cause the whole tx to fail, and/or change the outer status
//    //              if (!wsViewOrEditPrivilegesResults.tallyResults(grouperTransactionType, THE_SUMMARY)) {
//    //                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
//    //              }
//    //                
//    //
//    //              return wsViewOrEditPrivilegesResults;
//    //
//    //            }
//    //
//    //          });
//    //    } catch (Exception e) {
//    //      wsViewOrEditPrivilegesResults.assignResultCodeException(null, theSummary, e);
//    //    } finally {
//    //      GrouperSession.stopQuietly(session);
//    //    }
//    //
//    //    //set response headers
//    //    GrouperServiceUtils.addResponseHeaders(wsViewOrEditPrivilegesResults.getResultMetadata().getSuccess(),
//    //        wsViewOrEditPrivilegesResults.getResultMetadata().getResultCode());
//    //
//    //    //this should be the first and only return, or else it is exiting too early
//    //    return wsViewOrEditPrivilegesResults;
//    //
//    //
//    //    
//    //    
//    //    GrouperTransactionType grouperTransactionType = null;
//    //
//    //    //convert the options to a map for easy access, and validate them
//    //    @SuppressWarnings("unused")
//    //    Map<String, String> paramMap = null;
//    //    try {
//    //      paramMap = GrouperServiceUtils.convertParamsToMap(params);
//    //    } catch (Exception e) {
//    //      wsViewOrEditPrivilegesResults
//    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
//    //      wsViewOrEditPrivilegesResults.getResultMetadata().setResultMessage("Invalid params: " + e.getMessage());
//    //      return wsViewOrEditPrivilegesResults;
//    //    }
//    //
//    //    try {
//    //      grouperTransactionType = GrouperUtil.defaultIfNull(GrouperTransactionType
//    //          .valueOfIgnoreCase(txType), GrouperTransactionType.NONE);
//    //    } catch (Exception e) {
//    //      //a helpful exception will probably be in the getMessage()
//    //      wsViewOrEditPrivilegesResults
//    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
//    //      wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("Invalid txType: '" + txType
//    //          + "', " + e.getMessage());
//    //      return wsViewOrEditPrivilegesResults;
//    //    }
//    //
//    //    int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
//    //    if (subjectLength == 0) {
//    //      wsViewOrEditPrivilegesResults
//    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
//    //      wsViewOrEditPrivilegesResults
//    //          .getResultMetadata().appendResultMessage("Subject length must be more than 1");
//    //      return wsViewOrEditPrivilegesResults;
//    //    }
//    //
//    //    // see if greater than the max (or default)
//    //    int maxSavePrivileges = GrouperWsConfig.getPropertyInt(
//    //        GrouperWsConfig.WS_VIEW_OR_EDIT_PRIVILEGES_SUBJECTS_MAX, 1000000);
//    //    if (subjectLength > maxSavePrivileges) {
//    //      wsViewOrEditPrivilegesResults
//    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
//    //      wsViewOrEditPrivilegesResults
//    //          .getResultMetadata().appendResultMessage("Subject length must be less than max: "
//    //              + maxSavePrivileges + " (sent in " + subjectLength + ")");
//    //      return wsViewOrEditPrivilegesResults;
//    //    }
//    //
//    //    // TODO make sure size of params and values the same
//    //
//    //    // assume success
//    //    wsViewOrEditPrivilegesResults
//    //        .assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
//    //    Subject actAsSubject = null;
//    //    try {
//    //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
//    //
//    //      if (actAsSubject == null) {
//    //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
//    //      }
//    //
//    //      // use this to be the user connected, or the user act-as
//    //      try {
//    //        session = GrouperSession.start(actAsSubject);
//    //      } catch (SessionException se) {
//    //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
//    //            se);
//    //      }
//    //      wsGroupLookup.retrieveGroupIfNeeded(session);
//    //      Group group = wsGroupLookup.retrieveGroup();
//    //
//    //      if (group == null) {
//    //        wsViewOrEditPrivilegesResults
//    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
//    //        wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("Cant find group: "
//    //            + wsGroupLookup + ".  ");
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //      List<Privilege> privilegesToAssign = new ArrayList<Privilege>();
//    //
//    //      List<Privilege> privilegesToRevoke = new ArrayList<Privilege>();
//    //
//    //      // process the privilege inputs, keep in lists, handle invalid
//    //      // queries
//    //      if (!GrouperServiceUtils.processPrivilegesHelper(adminAllowed, "adminAllowed",
//    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.ADMIN,
//    //          wsViewOrEditPrivilegesResults)) {
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //      if (!GrouperServiceUtils.processPrivilegesHelper(optinAllowed, "optinAllowed",
//    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.OPTIN,
//    //          wsViewOrEditPrivilegesResults)) {
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //      if (!GrouperServiceUtils.processPrivilegesHelper(optoutAllowed, "optoutAllowed",
//    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.OPTOUT,
//    //          wsViewOrEditPrivilegesResults)) {
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //      if (!GrouperServiceUtils.processPrivilegesHelper(readAllowed, "readAllowed",
//    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.READ,
//    //          wsViewOrEditPrivilegesResults)) {
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //      if (!GrouperServiceUtils.processPrivilegesHelper(updateAllowed, "updateAllowed",
//    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.UPDATE,
//    //          wsViewOrEditPrivilegesResults)) {
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //      if (!GrouperServiceUtils.processPrivilegesHelper(viewAllowed, "viewAllowed",
//    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.VIEW,
//    //          wsViewOrEditPrivilegesResults)) {
//    //        return wsViewOrEditPrivilegesResults;
//    //      }
//    //
//    //      int resultIndex = 0;
//    //
//    //      wsViewOrEditPrivilegesResults
//    //          .setResults(new WsViewOrEditPrivilegesResult[subjectLength]);
//    //
//    //      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
//    //        WsPrivilege wsPrivilege = new WsPrivilege();
//    //        WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = new WsViewOrEditPrivilegesResult();
//    //        wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsViewOrEditPrivilegesResult;
//    //        wsViewOrEditPrivilegesResult.setWsPrivilege(wsPrivilege);
//    //        try {
//    //          wsPrivilege.setSubjectId(wsSubjectLookup.getSubjectId());
//    //          wsPrivilege.setSubjectIdentifier(wsSubjectLookup
//    //              .getSubjectIdentifier());
//    //
//    //          Subject subject = wsSubjectLookup.retrieveSubject();
//    //
//    //          // make sure the subject is there
//    //          if (subject == null) {
//    //            // see why not
//    //            SubjectFindResult subjectFindResult = wsSubjectLookup
//    //                .retrieveSubjectFindResult();
//    //            String error = "Subject: " + wsSubjectLookup + " had problems: "
//    //                + subjectFindResult;
//    //            wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(error);
//    //            if (SubjectFindResult.SUBJECT_NOT_FOUND.equals(subjectFindResult)) {
//    //              wsViewOrEditPrivilegesResult
//    //                  .assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_NOT_FOUND);
//    //              continue;
//    //            }
//    //            if (SubjectFindResult.SUBJECT_DUPLICATE.equals(subjectFindResult)) {
//    //              wsViewOrEditPrivilegesResult
//    //                  .assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_DUPLICATE);
//    //              continue;
//    //            }
//    //            throw new NullPointerException(error);
//    //          }
//    //
//    //          // these will probably match, but just in case
//    //          if (StringUtils.isBlank(wsPrivilege.getSubjectId())) {
//    //            wsPrivilege.setSubjectId(subject.getId());
//    //          }
//    //
//    //          try {
//    //            // lets get all the privileges for the group and user
//    //            Set<AccessPrivilege> accessPrivileges = GrouperUtil.nonNull(group
//    //                .getPrivs(subject));
//    //
//    //            // TODO keep track of isRevokable? Also, can you remove
//    //            // a read priv? I tried and got exception
//    //
//    //            // see what we really need to do. At the end, the
//    //            // currentAccessPrivileges should be what it looks like
//    //            // afterward
//    //            // (add in assignments, remove revokes),
//    //            // the privilegestoAssign will be what to assign (take
//    //            // out what is already there)
//    //            Set<Privilege> currentPrivilegesSet = GrouperServiceUtils
//    //                .convertAccessPrivilegesToPrivileges(accessPrivileges);
//    //
//    //            List<Privilege> privilegesToAssignToThisSubject = new ArrayList<Privilege>(
//    //                privilegesToAssign);
//    //            List<Privilege> privilegesToRevokeFromThisSubject = new ArrayList<Privilege>(
//    //                privilegesToRevoke);
//    //
//    //            // dont assign ones already in there
//    //            privilegesToAssignToThisSubject.removeAll(currentPrivilegesSet);
//    //            // dont revoke ones not in there
//    //            privilegesToRevokeFromThisSubject.retainAll(currentPrivilegesSet);
//    //            // assign
//    //            for (Privilege privilegeToAssign : privilegesToAssignToThisSubject) {
//    //              group.grantPriv(subject, privilegeToAssign);
//    //            }
//    //            // revoke
//    //            for (Privilege privilegeToRevoke : privilegesToRevokeFromThisSubject) {
//    //              group.revokePriv(subject, privilegeToRevoke);
//    //            }
//    //            // reset the current privileges set to reflect the new
//    //            // state
//    //            currentPrivilegesSet.addAll(privilegesToAssignToThisSubject);
//    //            currentPrivilegesSet.removeAll(privilegesToRevokeFromThisSubject);
//    //
//    //            wsPrivilege.setAdminAllowed(GrouperServiceUtils
//    //                .booleanToStringOneChar(currentPrivilegesSet
//    //                    .contains(AccessPrivilege.ADMIN)));
//    //            wsPrivilege.setOptinAllowed(GrouperServiceUtils
//    //                .booleanToStringOneChar(currentPrivilegesSet
//    //                    .contains(AccessPrivilege.OPTIN)));
//    //            wsPrivilege.setOptoutAllowed(GrouperServiceUtils
//    //                .booleanToStringOneChar(currentPrivilegesSet
//    //                    .contains(AccessPrivilege.OPTOUT)));
//    //            wsPrivilege.setReadAllowed(GrouperServiceUtils
//    //                .booleanToStringOneChar(currentPrivilegesSet
//    //                    .contains(AccessPrivilege.READ)));
//    //            wsPrivilege.setViewAllowed(GrouperServiceUtils
//    //                .booleanToStringOneChar(currentPrivilegesSet
//    //                    .contains(AccessPrivilege.VIEW)));
//    //            wsPrivilege.setUpdateAllowed(GrouperServiceUtils
//    //                .booleanToStringOneChar(currentPrivilegesSet
//    //                    .contains(AccessPrivilege.UPDATE)));
//    //
//    //            wsViewOrEditPrivilegesResult
//    //                .assignResultCode(WsViewOrEditPrivilegesResultCode.SUCCESS);
//    //          } catch (InsufficientPrivilegeException ipe) {
//    //            wsViewOrEditPrivilegesResult
//    //                .assignResultCode(WsViewOrEditPrivilegesResultCode.INSUFFICIENT_PRIVILEGES);
//    //            wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(ExceptionUtils
//    //                .getFullStackTrace(ipe));
//    //          }
//    //        } catch (Exception e) {
//    //          wsViewOrEditPrivilegesResult.getResultMetadata().setResultCode("EXCEPTION");
//    //          wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(ExceptionUtils
//    //              .getFullStackTrace(e));
//    //          LOG.error(wsSubjectLookup + ", " + e, e);
//    //        }
//    //
//    //      }
//    //    } catch (RuntimeException re) {
//    //      wsViewOrEditPrivilegesResults
//    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.EXCEPTION);
//    //      String theError = "Problem with privileges for member and group: wsGroupLookup: "
//    //          + wsGroupLookup + ", subjectLookups: "
//    //          + GrouperUtil.toStringForLog(subjectLookups) + ", actAsSubject: "
//    //          + actAsSubject + ", admin: '" + adminAllowed + "', optin: '" + optinAllowed
//    //          + "', optout: '" + optoutAllowed + "', read: '" + readAllowed + "', update: '"
//    //          + updateAllowed + "', view: '" + viewAllowed + ".  ";
//    //      wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage(theError + "\n"
//    //          + ExceptionUtils.getFullStackTrace(re));
//    //      // this is sent back to the caller anyway, so just log, and not send
//    //      // back again
//    //      LOG.error(theError + ", wsViewOrEditPrivilegesResults: "
//    //          + GrouperUtil.toStringForLog(wsViewOrEditPrivilegesResults), re);
//    //    } finally {
//    //      if (session != null) {
//    //        try {
//    //          session.stop();
//    //        } catch (Exception e) {
//    //          LOG.error(e.getMessage(), e);
//    //        }
//    //      }
//    //    }
//    //
//    //    if (wsViewOrEditPrivilegesResults.getResults() != null) {
//    //      // check all entries
//    //      int successes = 0;
//    //      int failures = 0;
//    //      for (WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult : wsViewOrEditPrivilegesResults
//    //          .getResults()) {
//    //        boolean success = "T".equalsIgnoreCase(wsViewOrEditPrivilegesResult.getResultMetadata().getSuccess());
//    //        if (success) {
//    //          successes++;
//    //        } else {
//    //          failures++;
//    //        }
//    //      }
//    //      if (failures > 0) {
//    //        wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("There were " + successes
//    //            + " successes and " + failures
//    //            + " failures of user group privileges operations.   ");
//    //        wsViewOrEditPrivilegesResults
//    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.PROBLEM_WITH_MEMBERS);
//    //      } else {
//    //        wsViewOrEditPrivilegesResults
//    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
//    //      }
//    //    }
//    //    if (!"T".equalsIgnoreCase(wsViewOrEditPrivilegesResults.getResultMetadata().getSuccess())) {
//    //
//    //      LOG.error(wsViewOrEditPrivilegesResults.getResultMetadata().getResultMessage());
//    //    }
//    return wsViewOrEditPrivilegesResults;
//  }

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
  //  @SuppressWarnings("unchecked")
  //  public WsViewOrEditPrivilegesResults viewOrEditPrivileges(final String clientVersion,
  //      final WsPrivilege[] privileges, final WsSubjectLookup actAsSubjectLookup,
  //      final String txType, final WsParam[] params) {
  //
  //    final WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = new WsViewOrEditPrivilegesResults();
  //
  //    GrouperSession session = null;
  //    String theSummary = null;
  //    //    try {
  //
  //    theSummary = "clientVersion: " + clientVersion + ", privileges: "
  //        + GrouperUtil.toStringForLog(privileges, 300) + ", actAsSubject: "
  //        + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
  //        + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  //
  //    final String THE_SUMMARY = theSummary;
  //
  //    //start session based on logged in user or the actAs passed in
  //    session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  //
  //    final GrouperSession SESSION = session;
  //
  //    //convert tx type to object
  //    final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
  //        .convertTransactionType(txType);
  //
  //    //      //start a transaction (or not if none)
  //    //      GrouperTransaction.callbackGrouperTransaction(grouperTransactionType,
  //    //          new GrouperTransactionHandler() {
  //    //
  //    //            public Object callback(GrouperTransaction grouperTransaction)
  //    //                throws GrouperDAOException {
  //    //
  //    //              //convert the options to a map for easy access, and validate them
  //    //              @SuppressWarnings("unused")
  //    //              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
  //    //                  params);
  //    //
  //    //              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
  //    //                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);
  //    //
  //    //              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
  //    //
  //    //              boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
  //    //                  includeGroupDetail, false, "includeGroupDetail");
  //    //
  //    //              //assign the group to the result to be descriptive
  //    //              wsViewOrEditPrivilegesResults.setWsGroupAssigned(new WsGroup(group,
  //    //                  includeGroupDetailBoolean));
  //    //
  //    //              int resultIndex = 0;
  //    //
  //    //              boolean replaceAllExistingBoolean = GrouperServiceUtils.booleanValue(
  //    //                  replaceAllExisting, false, "replaceAllExisting");
  //    //
  //    //              Set<Subject> newSubjects = new HashSet<Subject>();
  //    //              wsViewOrEditPrivilegesResults.setResults(new WsAddMemberResult[subjectLength]);
  //    //
  //    //              //get the field or null or invalid query exception
  //    //              Field field = GrouperServiceUtils.retrieveField(fieldName);
  //    //
  //    //              //get existing members if replacing
  //    //              Set<Member> members = null;
  //    //              if (replaceAllExistingBoolean) {
  //    //                try {
  //    //                  // see who is there
  //    //                  members = field == null ? group.getImmediateMembers() : group
  //    //                      .getImmediateMembers(field);
  //    //                } catch (SchemaException se) {
  //    //                  throw new WsInvalidQueryException(
  //    //                      "Problem with getting existing members: " + fieldName + ".  "
  //    //                          + ExceptionUtils.getFullStackTrace(se));
  //    //                }
  //    //              }
  //    //
  //    //              for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
  //    //                WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
  //    //                wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsAddMemberResult;
  //    //                try {
  //    //
  //    //                  Subject subject = wsSubjectLookup.retrieveSubject();
  //    //
  //    //                  wsAddMemberResult.processSubject(wsSubjectLookup);
  //    //
  //    //                  if (subject == null) {
  //    //                    continue;
  //    //                  }
  //    //
  //    //                  // keep track
  //    //                  if (replaceAllExistingBoolean) {
  //    //                    newSubjects.add(subject);
  //    //                  }
  //    //
  //    //                  try {
  //    //                    if (field != null) {
  //    //                      // dont fail if already a direct member
  //    //                      group.addMember(subject, false);
  //    //                    } else {
  //    //                      group.addMember(subject, field, false);
  //    //                    }
  //    //                    wsAddMemberResult.assignResultCode(WsAddMemberResultCode.SUCCESS);
  //    //
  //    //                  } catch (InsufficientPrivilegeException ipe) {
  //    //                    wsAddMemberResult
  //    //                        .assignResultCode(WsAddMemberResultCode.INSUFFICIENT_PRIVILEGES);
  //    //                  }
  //    //                } catch (Exception e) {
  //    //                  wsAddMemberResult.assignResultCodeException(e, wsSubjectLookup);
  //    //                }
  //    //              }
  //    //
  //    //              // after adding all these, see if we are removing:
  //    //              if (replaceAllExistingBoolean) {
  //    //
  //    //                for (Member member : members) {
  //    //                  Subject subject = null;
  //    //                  try {
  //    //                    subject = member.getSubject();
  //    //
  //    //                    if (!newSubjects.contains(subject)) {
  //    //                      if (field == null) {
  //    //                        group.deleteMember(subject);
  //    //                      } else {
  //    //                        group.deleteMember(subject, field);
  //    //                      }
  //    //                    }
  //    //                  } catch (Exception e) {
  //    //                    String theError = "Error deleting subject: " + subject
  //    //                        + " from group: " + group + ", field: " + field + ", " + e
  //    //                        + ".  ";
  //    //                    wsViewOrEditPrivilegesResults.assignResultCodeException(
  //    //                        WsAddMemberResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
  //    //                  }
  //    //                }
  //    //              }
  //    //              //see if any inner failures cause the whole tx to fail, and/or change the outer status
  //    //              if (!wsViewOrEditPrivilegesResults.tallyResults(grouperTransactionType, THE_SUMMARY)) {
  //    //                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
  //    //              }
  //    //                
  //    //
  //    //              return wsViewOrEditPrivilegesResults;
  //    //
  //    //            }
  //    //
  //    //          });
  //    //    } catch (Exception e) {
  //    //      wsViewOrEditPrivilegesResults.assignResultCodeException(null, theSummary, e);
  //    //    } finally {
  //    //      GrouperSession.stopQuietly(session);
  //    //    }
  //    //
  //    //    //set response headers
  //    //    GrouperServiceUtils.addResponseHeaders(wsViewOrEditPrivilegesResults.getResultMetadata().getSuccess(),
  //    //        wsViewOrEditPrivilegesResults.getResultMetadata().getResultCode());
  //    //
  //    //    //this should be the first and only return, or else it is exiting too early
  //    //    return wsViewOrEditPrivilegesResults;
  //    //
  //    //
  //    //    
  //    //    
  //    //    GrouperTransactionType grouperTransactionType = null;
  //    //
  //    //    //convert the options to a map for easy access, and validate them
  //    //    @SuppressWarnings("unused")
  //    //    Map<String, String> paramMap = null;
  //    //    try {
  //    //      paramMap = GrouperServiceUtils.convertParamsToMap(params);
  //    //    } catch (Exception e) {
  //    //      wsViewOrEditPrivilegesResults
  //    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
  //    //      wsViewOrEditPrivilegesResults.getResultMetadata().setResultMessage("Invalid params: " + e.getMessage());
  //    //      return wsViewOrEditPrivilegesResults;
  //    //    }
  //    //
  //    //    try {
  //    //      grouperTransactionType = GrouperUtil.defaultIfNull(GrouperTransactionType
  //    //          .valueOfIgnoreCase(txType), GrouperTransactionType.NONE);
  //    //    } catch (Exception e) {
  //    //      //a helpful exception will probably be in the getMessage()
  //    //      wsViewOrEditPrivilegesResults
  //    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
  //    //      wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("Invalid txType: '" + txType
  //    //          + "', " + e.getMessage());
  //    //      return wsViewOrEditPrivilegesResults;
  //    //    }
  //    //
  //    //    int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
  //    //    if (subjectLength == 0) {
  //    //      wsViewOrEditPrivilegesResults
  //    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
  //    //      wsViewOrEditPrivilegesResults
  //    //          .getResultMetadata().appendResultMessage("Subject length must be more than 1");
  //    //      return wsViewOrEditPrivilegesResults;
  //    //    }
  //    //
  //    //    // see if greater than the max (or default)
  //    //    int maxSavePrivileges = GrouperWsConfig.getPropertyInt(
  //    //        GrouperWsConfig.WS_VIEW_OR_EDIT_PRIVILEGES_SUBJECTS_MAX, 1000000);
  //    //    if (subjectLength > maxSavePrivileges) {
  //    //      wsViewOrEditPrivilegesResults
  //    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
  //    //      wsViewOrEditPrivilegesResults
  //    //          .getResultMetadata().appendResultMessage("Subject length must be less than max: "
  //    //              + maxSavePrivileges + " (sent in " + subjectLength + ")");
  //    //      return wsViewOrEditPrivilegesResults;
  //    //    }
  //    //
  //    //    // TODO make sure size of params and values the same
  //    //
  //    //    // assume success
  //    //    wsViewOrEditPrivilegesResults
  //    //        .assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
  //    //    Subject actAsSubject = null;
  //    //    try {
  //    //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
  //    //
  //    //      if (actAsSubject == null) {
  //    //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
  //    //      }
  //    //
  //    //      // use this to be the user connected, or the user act-as
  //    //      try {
  //    //        session = GrouperSession.start(actAsSubject);
  //    //      } catch (SessionException se) {
  //    //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
  //    //            se);
  //    //      }
  //    //      wsGroupLookup.retrieveGroupIfNeeded(session);
  //    //      Group group = wsGroupLookup.retrieveGroup();
  //    //
  //    //      if (group == null) {
  //    //        wsViewOrEditPrivilegesResults
  //    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
  //    //        wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("Cant find group: "
  //    //            + wsGroupLookup + ".  ");
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //      List<Privilege> privilegesToAssign = new ArrayList<Privilege>();
  //    //
  //    //      List<Privilege> privilegesToRevoke = new ArrayList<Privilege>();
  //    //
  //    //      // process the privilege inputs, keep in lists, handle invalid
  //    //      // queries
  //    //      if (!GrouperServiceUtils.processPrivilegesHelper(adminAllowed, "adminAllowed",
  //    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.ADMIN,
  //    //          wsViewOrEditPrivilegesResults)) {
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //      if (!GrouperServiceUtils.processPrivilegesHelper(optinAllowed, "optinAllowed",
  //    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.OPTIN,
  //    //          wsViewOrEditPrivilegesResults)) {
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //      if (!GrouperServiceUtils.processPrivilegesHelper(optoutAllowed, "optoutAllowed",
  //    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.OPTOUT,
  //    //          wsViewOrEditPrivilegesResults)) {
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //      if (!GrouperServiceUtils.processPrivilegesHelper(readAllowed, "readAllowed",
  //    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.READ,
  //    //          wsViewOrEditPrivilegesResults)) {
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //      if (!GrouperServiceUtils.processPrivilegesHelper(updateAllowed, "updateAllowed",
  //    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.UPDATE,
  //    //          wsViewOrEditPrivilegesResults)) {
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //      if (!GrouperServiceUtils.processPrivilegesHelper(viewAllowed, "viewAllowed",
  //    //          privilegesToAssign, privilegesToRevoke, AccessPrivilege.VIEW,
  //    //          wsViewOrEditPrivilegesResults)) {
  //    //        return wsViewOrEditPrivilegesResults;
  //    //      }
  //    //
  //    //      int resultIndex = 0;
  //    //
  //    //      wsViewOrEditPrivilegesResults
  //    //          .setResults(new WsViewOrEditPrivilegesResult[subjectLength]);
  //    //
  //    //      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
  //    //        WsPrivilege wsPrivilege = new WsPrivilege();
  //    //        WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = new WsViewOrEditPrivilegesResult();
  //    //        wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsViewOrEditPrivilegesResult;
  //    //        wsViewOrEditPrivilegesResult.setWsPrivilege(wsPrivilege);
  //    //        try {
  //    //          wsPrivilege.setSubjectId(wsSubjectLookup.getSubjectId());
  //    //          wsPrivilege.setSubjectIdentifier(wsSubjectLookup
  //    //              .getSubjectIdentifier());
  //    //
  //    //          Subject subject = wsSubjectLookup.retrieveSubject();
  //    //
  //    //          // make sure the subject is there
  //    //          if (subject == null) {
  //    //            // see why not
  //    //            SubjectFindResult subjectFindResult = wsSubjectLookup
  //    //                .retrieveSubjectFindResult();
  //    //            String error = "Subject: " + wsSubjectLookup + " had problems: "
  //    //                + subjectFindResult;
  //    //            wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(error);
  //    //            if (SubjectFindResult.SUBJECT_NOT_FOUND.equals(subjectFindResult)) {
  //    //              wsViewOrEditPrivilegesResult
  //    //                  .assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_NOT_FOUND);
  //    //              continue;
  //    //            }
  //    //            if (SubjectFindResult.SUBJECT_DUPLICATE.equals(subjectFindResult)) {
  //    //              wsViewOrEditPrivilegesResult
  //    //                  .assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_DUPLICATE);
  //    //              continue;
  //    //            }
  //    //            throw new NullPointerException(error);
  //    //          }
  //    //
  //    //          // these will probably match, but just in case
  //    //          if (StringUtils.isBlank(wsPrivilege.getSubjectId())) {
  //    //            wsPrivilege.setSubjectId(subject.getId());
  //    //          }
  //    //
  //    //          try {
  //    //            // lets get all the privileges for the group and user
  //    //            Set<AccessPrivilege> accessPrivileges = GrouperUtil.nonNull(group
  //    //                .getPrivs(subject));
  //    //
  //    //            // TODO keep track of isRevokable? Also, can you remove
  //    //            // a read priv? I tried and got exception
  //    //
  //    //            // see what we really need to do. At the end, the
  //    //            // currentAccessPrivileges should be what it looks like
  //    //            // afterward
  //    //            // (add in assignments, remove revokes),
  //    //            // the privilegestoAssign will be what to assign (take
  //    //            // out what is already there)
  //    //            Set<Privilege> currentPrivilegesSet = GrouperServiceUtils
  //    //                .convertAccessPrivilegesToPrivileges(accessPrivileges);
  //    //
  //    //            List<Privilege> privilegesToAssignToThisSubject = new ArrayList<Privilege>(
  //    //                privilegesToAssign);
  //    //            List<Privilege> privilegesToRevokeFromThisSubject = new ArrayList<Privilege>(
  //    //                privilegesToRevoke);
  //    //
  //    //            // dont assign ones already in there
  //    //            privilegesToAssignToThisSubject.removeAll(currentPrivilegesSet);
  //    //            // dont revoke ones not in there
  //    //            privilegesToRevokeFromThisSubject.retainAll(currentPrivilegesSet);
  //    //            // assign
  //    //            for (Privilege privilegeToAssign : privilegesToAssignToThisSubject) {
  //    //              group.grantPriv(subject, privilegeToAssign);
  //    //            }
  //    //            // revoke
  //    //            for (Privilege privilegeToRevoke : privilegesToRevokeFromThisSubject) {
  //    //              group.revokePriv(subject, privilegeToRevoke);
  //    //            }
  //    //            // reset the current privileges set to reflect the new
  //    //            // state
  //    //            currentPrivilegesSet.addAll(privilegesToAssignToThisSubject);
  //    //            currentPrivilegesSet.removeAll(privilegesToRevokeFromThisSubject);
  //    //
  //    //            wsPrivilege.setAdminAllowed(GrouperServiceUtils
  //    //                .booleanToStringOneChar(currentPrivilegesSet
  //    //                    .contains(AccessPrivilege.ADMIN)));
  //    //            wsPrivilege.setOptinAllowed(GrouperServiceUtils
  //    //                .booleanToStringOneChar(currentPrivilegesSet
  //    //                    .contains(AccessPrivilege.OPTIN)));
  //    //            wsPrivilege.setOptoutAllowed(GrouperServiceUtils
  //    //                .booleanToStringOneChar(currentPrivilegesSet
  //    //                    .contains(AccessPrivilege.OPTOUT)));
  //    //            wsPrivilege.setReadAllowed(GrouperServiceUtils
  //    //                .booleanToStringOneChar(currentPrivilegesSet
  //    //                    .contains(AccessPrivilege.READ)));
  //    //            wsPrivilege.setViewAllowed(GrouperServiceUtils
  //    //                .booleanToStringOneChar(currentPrivilegesSet
  //    //                    .contains(AccessPrivilege.VIEW)));
  //    //            wsPrivilege.setUpdateAllowed(GrouperServiceUtils
  //    //                .booleanToStringOneChar(currentPrivilegesSet
  //    //                    .contains(AccessPrivilege.UPDATE)));
  //    //
  //    //            wsViewOrEditPrivilegesResult
  //    //                .assignResultCode(WsViewOrEditPrivilegesResultCode.SUCCESS);
  //    //          } catch (InsufficientPrivilegeException ipe) {
  //    //            wsViewOrEditPrivilegesResult
  //    //                .assignResultCode(WsViewOrEditPrivilegesResultCode.INSUFFICIENT_PRIVILEGES);
  //    //            wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(ExceptionUtils
  //    //                .getFullStackTrace(ipe));
  //    //          }
  //    //        } catch (Exception e) {
  //    //          wsViewOrEditPrivilegesResult.getResultMetadata().setResultCode("EXCEPTION");
  //    //          wsViewOrEditPrivilegesResult.getResultMetadata().setResultMessage(ExceptionUtils
  //    //              .getFullStackTrace(e));
  //    //          LOG.error(wsSubjectLookup + ", " + e, e);
  //    //        }
  //    //
  //    //      }
  //    //    } catch (RuntimeException re) {
  //    //      wsViewOrEditPrivilegesResults
  //    //          .assignResultCode(WsViewOrEditPrivilegesResultsCode.EXCEPTION);
  //    //      String theError = "Problem with privileges for member and group: wsGroupLookup: "
  //    //          + wsGroupLookup + ", subjectLookups: "
  //    //          + GrouperUtil.toStringForLog(subjectLookups) + ", actAsSubject: "
  //    //          + actAsSubject + ", admin: '" + adminAllowed + "', optin: '" + optinAllowed
  //    //          + "', optout: '" + optoutAllowed + "', read: '" + readAllowed + "', update: '"
  //    //          + updateAllowed + "', view: '" + viewAllowed + ".  ";
  //    //      wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage(theError + "\n"
  //    //          + ExceptionUtils.getFullStackTrace(re));
  //    //      // this is sent back to the caller anyway, so just log, and not send
  //    //      // back again
  //    //      LOG.error(theError + ", wsViewOrEditPrivilegesResults: "
  //    //          + GrouperUtil.toStringForLog(wsViewOrEditPrivilegesResults), re);
  //    //    } finally {
  //    //      if (session != null) {
  //    //        try {
  //    //          session.stop();
  //    //        } catch (Exception e) {
  //    //          LOG.error(e.getMessage(), e);
  //    //        }
  //    //      }
  //    //    }
  //    //
  //    //    if (wsViewOrEditPrivilegesResults.getResults() != null) {
  //    //      // check all entries
  //    //      int successes = 0;
  //    //      int failures = 0;
  //    //      for (WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult : wsViewOrEditPrivilegesResults
  //    //          .getResults()) {
  //    //        boolean success = "T".equalsIgnoreCase(wsViewOrEditPrivilegesResult.getResultMetadata().getSuccess());
  //    //        if (success) {
  //    //          successes++;
  //    //        } else {
  //    //          failures++;
  //    //        }
  //    //      }
  //    //      if (failures > 0) {
  //    //        wsViewOrEditPrivilegesResults.getResultMetadata().appendResultMessage("There were " + successes
  //    //            + " successes and " + failures
  //    //            + " failures of user group privileges operations.   ");
  //    //        wsViewOrEditPrivilegesResults
  //    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.PROBLEM_WITH_MEMBERS);
  //    //      } else {
  //    //        wsViewOrEditPrivilegesResults
  //    //            .assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
  //    //      }
  //    //    }
  //    //    if (!"T".equalsIgnoreCase(wsViewOrEditPrivilegesResults.getResultMetadata().getSuccess())) {
  //    //
  //    //      LOG.error(wsViewOrEditPrivilegesResults.getResultMetadata().getResultMessage());
  //    //    }
  //    return wsViewOrEditPrivilegesResults;
  //  }
  
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
     * of the group (certain list)
     * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
     * NONE (will finish as much as possible).  Generally the only values for this param that make sense
     * are NONE (or blank), and READ_WRITE_NEW.
     * @param includeGroupDetail T or F as to if the group detail should be returned
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param params optional: reserved for future use
     * @return the results
     * @see GrouperWsVersion
     */
    @SuppressWarnings("unchecked")
    public WsAddMemberResults addMember(final String clientVersion,
        final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
        final String replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
        final String fieldName, final String txType, final String includeGroupDetail,
        final String includeSubjectDetail, final String[] subjectAttributeNames,
        final WsParam[] params) {
  
      WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
  
      try {
  
        //convert tx type to object
        final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
            .convertTransactionType(txType);
  
        boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
            includeGroupDetail, false, "includeGroupDetail");
  
        boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
            includeSubjectDetail, false, "includeSubjectDetail");
  
        boolean replaceAllExistingBoolean = GrouperServiceUtils.booleanValue(
            replaceAllExisting, false, "replaceAllExisting");
  
        //get the field or null or invalid query exception
        Field field = GrouperServiceUtils.retrieveField(fieldName);
  
        GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
            clientVersion, true);
  
        wsAddMemberResults = GrouperServiceLogic.addMember(grouperWsVersion, wsGroupLookup,
            subjectLookups, replaceAllExistingBoolean, actAsSubjectLookup, field,
            grouperTransactionType, includeGroupDetailBoolean, includeSubjectDetailBoolean,
            subjectAttributeNames, params);
      } catch (Exception e) {
        wsAddMemberResults.assignResultCodeException(null, null, e);
      }
  
      //set response headers
      GrouperServiceUtils.addResponseHeaders(wsAddMemberResults.getResultMetadata());
  
      //this should be the first and only return, or else it is exiting too early
      return wsAddMemberResults;
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
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsDeleteMemberResults deleteMember(final String clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final WsSubjectLookup actAsSubjectLookup, final String fieldName,
      final String txType, final String includeGroupDetail, 
      final String includeSubjectDetail, final String[] subjectAttributeNames,
      final WsParam[] params) {

    
    WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsDeleteMemberResults = GrouperServiceLogic.deleteMember(grouperWsVersion, wsGroupLookup,
          subjectLookups, actAsSubjectLookup, field,
          grouperTransactionType, includeGroupDetailBoolean, includeSubjectDetailBoolean,
          subjectAttributeNames, params);
    } catch (Exception e) {
      wsDeleteMemberResults.assignResultCodeException(null, null, e);
    }

    //this should be the first and only return, or else it is exiting too early
    return wsDeleteMemberResults;
    
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
  public WsGetGroupsLiteResult getGroupsLite(final String clientVersion, String subjectId,
      String subjectSourceId, String subjectIdentifier, String memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, 
      String includeSubjectDetail, 
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsGetGroupsLiteResult wsGetGroupsLiteResult = new WsGetGroupsLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      wsGetGroupsLiteResult = GrouperServiceLogic.getGroupsLite(grouperWsVersion, 
          subjectId, subjectSourceId, subjectIdentifier, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1);
    } catch (Exception e) {
      wsGetGroupsLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetGroupsLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsGetGroupsLiteResult;

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
  public WsAddMemberLiteResult addMemberLite(final String clientVersion,
      String groupName, String groupUuid, String subjectId, String subjectSourceId,
      String subjectIdentifier, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String fieldName, String includeGroupDetail,
      String includeSubjectDetail, String subjectAttributeNames, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
    
    WsAddMemberLiteResult wsAddMemberLiteResult = new WsAddMemberLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsAddMemberLiteResult = GrouperServiceLogic.addMemberLite(grouperWsVersion, groupName,
          groupUuid, subjectId, subjectSourceId, subjectIdentifier, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1);
    } catch (Exception e) {
      wsAddMemberLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAddMemberLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsAddMemberLiteResult;
    
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
//  public WsViewOrEditPrivilegesResults viewOrEditPrivilegesLite(
//      final String clientVersion, String groupName, String groupUuid, String subjectId,
//      String subjectSourceId, String subjectIdentifier, String adminAllowed,
//      String optinAllowed, String optoutAllowed, String readAllowed,
//      String updateAllowed, String viewAllowed, String actAsSubjectId,
//      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
//      String paramValue0, String paramName1, String paramValue1) {
//
//    // setup the group lookup
//    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
//
//    // setup the subject lookup
//    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, subjectSourceId,
//        subjectIdentifier);
//    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
//        actAsSubjectSourceId, actAsSubjectIdentifier);
//
//    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
//
//    WsPrivilege wsPrivilege = new WsPrivilege();
//    wsPrivilege.setSubjectLookup(wsSubjectLookup);
//    wsPrivilege.setWsGroupLookup(wsGroupLookup);
//    wsPrivilege.setAdminAllowed(adminAllowed);
//    wsPrivilege.setOptinAllowed(optinAllowed);
//    wsPrivilege.setOptoutAllowed(optoutAllowed);
//    wsPrivilege.setReadAllowed(readAllowed);
//    wsPrivilege.setUpdateAllowed(updateAllowed);
//    wsPrivilege.setViewAllowed(viewAllowed);
//
//    WsPrivilege[] wsPrivileges = { wsPrivilege };
//
//    WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = viewOrEditPrivileges(
//        clientVersion, wsPrivileges, actAsSubjectLookup, null, params);
//
//    return wsViewOrEditPrivilegesResults;
//
//  }

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
  //  public WsViewOrEditPrivilegesResults viewOrEditPrivilegesLite(
  //      final String clientVersion, String groupName, String groupUuid, String subjectId,
  //      String subjectSourceId, String subjectIdentifier, String adminAllowed,
  //      String optinAllowed, String optoutAllowed, String readAllowed,
  //      String updateAllowed, String viewAllowed, String actAsSubjectId,
  //      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
  //      String paramValue0, String paramName1, String paramValue1) {
  //
  //    // setup the group lookup
  //    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  //
  //    // setup the subject lookup
  //    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, subjectSourceId,
  //        subjectIdentifier);
  //    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
  //        actAsSubjectSourceId, actAsSubjectIdentifier);
  //
  //    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  //
  //    WsPrivilege wsPrivilege = new WsPrivilege();
  //    wsPrivilege.setSubjectLookup(wsSubjectLookup);
  //    wsPrivilege.setWsGroupLookup(wsGroupLookup);
  //    wsPrivilege.setAdminAllowed(adminAllowed);
  //    wsPrivilege.setOptinAllowed(optinAllowed);
  //    wsPrivilege.setOptoutAllowed(optoutAllowed);
  //    wsPrivilege.setReadAllowed(readAllowed);
  //    wsPrivilege.setUpdateAllowed(updateAllowed);
  //    wsPrivilege.setViewAllowed(viewAllowed);
  //
  //    WsPrivilege[] wsPrivileges = { wsPrivilege };
  //
  //    WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = viewOrEditPrivileges(
  //        clientVersion, wsPrivileges, actAsSubjectLookup, null, params);
  //
  //    return wsViewOrEditPrivilegesResults;
  //
  //  }
  
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
     * @param subjectIdentifier
     *            to query (mutually exclusive with subjectId)
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
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
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent
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
    public WsHasMemberLiteResult hasMemberLite(final String clientVersion, String groupName,
        String groupUuid, String subjectId, String subjectSourceId, String subjectIdentifier,
        String memberFilter,
        String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
        String fieldName, final String includeGroupDetail, 
        String includeSubjectDetail, String subjectAttributeNames, String paramName0,
        String paramValue0, String paramName1, String paramValue1) {
  
      WsHasMemberLiteResult wsHasMemberLiteResult = new WsHasMemberLiteResult();
  
      try {
  
        WsMemberFilter wsMemberFilter = GrouperServiceUtils
          .convertMemberFilter(memberFilter);
  
        boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
            includeGroupDetail, false, "includeGroupDetail");
  
        boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
            includeSubjectDetail, false, "includeSubjectDetail");
  
        //get the field or null or invalid query exception
        Field field = GrouperServiceUtils.retrieveField(fieldName);
  
        GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
            clientVersion, true);
  
        wsHasMemberLiteResult = GrouperServiceLogic.hasMemberLite(grouperWsVersion, groupName,
            groupUuid, subjectId, subjectSourceId, subjectIdentifier, wsMemberFilter, actAsSubjectId, 
            actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
            includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
            paramName1, paramValue1);
      } catch (Exception e) {
        wsHasMemberLiteResult.assignResultCodeException(null, null, e);
      }
  
      //set response headers
      GrouperServiceUtils.addResponseHeaders(wsHasMemberLiteResult.getResultMetadata());
  
      //this should be the first and only return, or else it is exiting too early
      return wsHasMemberLiteResult;
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
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id) of the new subject
   * @param subjectAttributeNames are the additional subject attributes (data) to return of the new subject
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
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
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
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
  public WsMemberChangeSubjectLiteResult memberChangeSubjectLite(final String clientVersion, 
      String oldSubjectId, String oldSubjectSourceId, String oldSubjectIdentifier,
      String newSubjectId, String newSubjectSourceId, String newSubjectIdentifier,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      final String deleteOldMember, 
      String includeSubjectDetail, String subjectAttributeNames, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsMemberChangeSubjectLiteResult wsMemberChangeSubjectLiteResult = new WsMemberChangeSubjectLiteResult();

    try {

      boolean deleteOldMemberBoolean = GrouperServiceUtils.booleanValue(
          deleteOldMember, true, "deleteOldMember");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsMemberChangeSubjectLiteResult = GrouperServiceLogic.memberChangeSubjectLite(grouperWsVersion, oldSubjectId,
          oldSubjectSourceId, oldSubjectIdentifier, newSubjectId, newSubjectSourceId, newSubjectIdentifier, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, deleteOldMemberBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1);
    } catch (Exception e) {
      wsMemberChangeSubjectLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsMemberChangeSubjectLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsMemberChangeSubjectLiteResult;
  }

  /**
   * add member to a group (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsMemberChangeSubjects list of objects which describe a member change subject
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param params optional: reserved for future use
   * @return the results
   * @see GrouperWsVersion
   */
  @SuppressWarnings("unchecked")
  public WsMemberChangeSubjectResults memberChangeSubject(final String clientVersion,
      WsMemberChangeSubject[] wsMemberChangeSubjects, final WsSubjectLookup actAsSubjectLookup,
      final String txType, 
      final String includeSubjectDetail, final String[] subjectAttributeNames,
      final WsParam[] params) {

    WsMemberChangeSubjectResults wsMemberChangeSubjectResults = new WsMemberChangeSubjectResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsMemberChangeSubjectResults = GrouperServiceLogic.memberChangeSubject(grouperWsVersion, wsMemberChangeSubjects,
          actAsSubjectLookup, 
          grouperTransactionType, includeSubjectDetailBoolean,
          subjectAttributeNames, params);
    } catch (Exception e) {
      wsMemberChangeSubjectResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsMemberChangeSubjectResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsMemberChangeSubjectResults;
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
  public WsDeleteMemberLiteResult deleteMemberLite(final String clientVersion,
      String groupName, String groupUuid, String subjectId, String subjectSourceId,
      String subjectIdentifier, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, final String fieldName,
      final String includeGroupDetail, String includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsDeleteMemberLiteResult wsDeleteMemberLiteResult = new WsDeleteMemberLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsDeleteMemberLiteResult = GrouperServiceLogic.deleteMemberLite(grouperWsVersion, groupName,
          groupUuid, subjectId, subjectSourceId, subjectIdentifier, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1);
    } catch (Exception e) {
      wsDeleteMemberLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsDeleteMemberLiteResult.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsDeleteMemberLiteResult;
  }

  /**
   * find a stem or stems
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemQueryFilterType findStemType is the WsFindStemType enum for which 
   * type of find is happening:  e.g.
   * FIND_BY_STEM_UUID, FIND_BY_STEM_NAME, FIND_BY_PARENT_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE,  FIND_BY_STEM_NAME_APPROXIMATE
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
  public WsFindStemsResults findStemsLite(final String clientVersion,
      String stemQueryFilterType, String stemName, String parentStemName,
      String parentStemNameScope, String stemUuid, String stemAttributeName,
      String stemAttributeValue, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();

    try {

      GrouperWsVersion grouperWsVersion = GrouperWsVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      StemScope stemScope = StemScope.valueOfIgnoreCase(parentStemNameScope);
      WsStemQueryFilterType wsStemQueryFilterType = WsStemQueryFilterType.valueOfIgnoreCase(stemQueryFilterType);

      
      wsFindStemsResults = GrouperServiceLogic.findStemsLite(grouperWsVersion, wsStemQueryFilterType, 
          stemName, parentStemName, stemScope, stemUuid, stemAttributeName, stemAttributeValue,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsFindStemsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindStemsResults.getResultMetadata());

    //this should be the first and only return, or else it is exiting too early
    return wsFindStemsResults;

  }

}
