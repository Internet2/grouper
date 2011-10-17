package edu.internet2.middleware.grouper.ws.coresoap;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionAssignOperation;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
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

  /** 
   * default
   */
  public GrouperService() {
    //nothin
  }
  
  /** if soap */
  private boolean soap = true;
  
  /**
   * 
   * @param soap1
   */
  public GrouperService(boolean soap1) {
    this.soap = soap1;
  }
  
  
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
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  
   * If you pass true or false, must pass a sort string
   * @param typeOfGroups is the comma separated TypeOfGroups to find, e.g. group, role, entity
   * @return the groups, or no groups if none found
   */
  public WsFindGroupsResults findGroupsLite(final String clientVersion,
      String queryFilterType, String groupName, String stemName, String stemNameScope,
      String groupUuid, String groupAttributeName, String groupAttributeValue,
      String groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1, String pageSize, 
      String pageNumber, String sortString, String ascending, String typeOfGroups) {

    WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      StemScope stemScope = StemScope.valueOfIgnoreCase(stemNameScope);
      WsQueryFilterType wsQueryFilterType = WsQueryFilterType.valueOfIgnoreCase(queryFilterType);

      GroupType groupType = GrouperServiceUtils.retrieveGroupType(groupTypeName);

      wsFindGroupsResults = GrouperServiceLogic.findGroupsLite(grouperWsVersion, wsQueryFilterType, 
          groupName, stemName, stemScope, groupUuid, groupAttributeName, groupAttributeValue,
          groupType,actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1,
          pageSize, pageNumber, sortString, ascending, typeOfGroups);
      
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindGroupsResults.getResultMetadata(), this.soap);

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
   * @param wsStemLookups to pass in a list of uuids or names to lookup.  Note the stems are returned
   * in alphabetical order
   * @return the stems, or no stems if none found
   */
  @SuppressWarnings("unchecked")
  public WsFindStemsResults findStems(final String clientVersion,
      WsStemQueryFilter wsStemQueryFilter, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params, WsStemLookup[] wsStemLookups) {

    WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();

    try {

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsFindStemsResults = GrouperServiceLogic.findStems(grouperWsVersion, wsStemQueryFilter, 
          actAsSubjectLookup, params, wsStemLookups);
    } catch (Exception e) {
      wsFindStemsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindStemsResults.getResultMetadata(), this.soap);

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
   * @param wsGroupLookups if you want to just pass in a list of uuids and/or names.  Note the stems are returned
   * in alphabetical order
   * @return the groups, or no groups if none found
   */
  @SuppressWarnings("unchecked")
  public WsFindGroupsResults findGroups(final String clientVersion,
      WsQueryFilter wsQueryFilter, 
      WsSubjectLookup actAsSubjectLookup, 
      String includeGroupDetail, WsParam[] params, WsGroupLookup[] wsGroupLookups) {

    WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsFindGroupsResults = GrouperServiceLogic.findGroups(grouperWsVersion, wsQueryFilter, actAsSubjectLookup, 
          includeGroupDetailBoolean, params, wsGroupLookups);
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindGroupsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsFindGroupsResults;

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
   * @param sourceIds comma separated source ids or null for all
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
   * @return the members, or no members if none found
   */
  public WsGetMembersLiteResult getMembersLite(final String clientVersion,
      String groupName, String groupUuid, String memberFilter, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds,
      String pointInTimeFrom, String pointInTimeTo) {

    WsGetMembersLiteResult wsGetMembersLiteResult = new WsGetMembersLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
      
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);


      wsGetMembersLiteResult = GrouperServiceLogic.getMembersLite(grouperWsVersion, 
          groupName, groupUuid, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1, sourceIds, pointInTimeFromTimestamp, pointInTimeToTimestamp);
    } catch (Exception e) {
      wsGetMembersLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetMembersLiteResult.getResultMetadata(), this.soap);

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
   * @param sourceIds array of source ids or null if all
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetMembersResults getMembers(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, String memberFilter,
      WsSubjectLookup actAsSubjectLookup, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params, String[] sourceIds,
      String pointInTimeFrom, String pointInTimeTo) {
  
    WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
  
    try {
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);
  
      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
      
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
  
      wsGetMembersResults = GrouperServiceLogic.getMembers(grouperWsVersion, wsGroupLookups, 
          wsMemberFilter, actAsSubjectLookup, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params, sourceIds, 
          pointInTimeFromTimestamp, pointInTimeToTimestamp);
    } catch (Exception e) {
      wsGetMembersResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetMembersResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
    return wsGetMembersResults;
  
  
  }


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
   * @param fieldName is field name (list name) to search or blank for default list
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param wsStemLookup is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem
   * @param enabled is A for all, T or null for enabled only, F for disabled
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetGroupsResults getGroups(final String clientVersion,
      WsSubjectLookup[] subjectLookups, String memberFilter, 
      WsSubjectLookup actAsSubjectLookup, String includeGroupDetail,
      String includeSubjectDetail, String[] subjectAttributeNames, 
      WsParam[] params, String fieldName, String scope, 
      WsStemLookup wsStemLookup, String stemScope, String enabled, 
      String pageSize, String pageNumber, String sortString, String ascending,
      String pointInTimeFrom, String pointInTimeTo) {
    
    WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      StemScope stemScopeEnum = StemScope.valueOfIgnoreCase(stemScope);
      
      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      Integer pageNumberInteger = GrouperUtil.intObjectValue(pageNumber, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);
      
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      wsGetGroupsResults = GrouperServiceLogic.getGroups(grouperWsVersion, subjectLookups, 
          wsMemberFilter, actAsSubjectLookup, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params, fieldName, scope, wsStemLookup, 
          stemScopeEnum, enabled, pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          pointInTimeFromTimestamp, pointInTimeToTimestamp);
    } catch (Exception e) {
      wsGetGroupsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetGroupsResults.getResultMetadata(), this.soap);

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
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
   * @return the results
   */
  public WsHasMemberResults hasMember(final String clientVersion,
      WsGroupLookup wsGroupLookup, WsSubjectLookup[] subjectLookups,
      String memberFilter,
      WsSubjectLookup actAsSubjectLookup, String fieldName,
      final String includeGroupDetail, 
      String includeSubjectDetail, String[] subjectAttributeNames, 
      WsParam[] params,
      String pointInTimeFrom, String pointInTimeTo) {

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
      
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsHasMemberResults = GrouperServiceLogic.hasMember(grouperWsVersion, wsGroupLookup,
          subjectLookups, wsMemberFilter, actAsSubjectLookup, field,
          includeGroupDetailBoolean, includeSubjectDetailBoolean,
          subjectAttributeNames, params, pointInTimeFromTimestamp, pointInTimeToTimestamp);
    } catch (Exception e) {
      wsHasMemberResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsHasMemberResults.getResultMetadata(), this.soap);

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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsStemDeleteLiteResult = GrouperServiceLogic.stemDeleteLite(grouperWsVersion, stemName, stemUuid, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsStemDeleteLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemDeleteLiteResult.getResultMetadata(), this.soap);

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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsGroupDeleteLiteResult = GrouperServiceLogic.groupDeleteLite(grouperWsVersion, groupName,
          groupUuid, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsGroupDeleteLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupDeleteLiteResult.getResultMetadata(), this.soap);

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
    GrouperVersion grouperWsVersion = null;
    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      wsGroupSaveLiteResult = GrouperServiceLogic.groupSaveLite(grouperWsVersion, groupLookupUuid,
          groupLookupName, groupUuid, groupName, displayExtension, description, saveModeEnum,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean,  
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsGroupSaveLiteResult.assignResultCodeException(null, null, e, grouperWsVersion);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupSaveLiteResult.getResultMetadata(), this.soap);

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

    GrouperVersion grouperWsVersion = null;
    
    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      wsStemSaveLiteResult = GrouperServiceLogic.stemSaveLite(grouperWsVersion, stemLookupUuid,
          stemLookupName, stemUuid, stemName, displayExtension, description, saveModeEnum,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
          paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsStemSaveLiteResult.assignResultCodeException(null, null, e, grouperWsVersion);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemSaveLiteResult.getResultMetadata(), this.soap);

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
    GrouperVersion grouperWsVersion = null;
    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      wsGroupSaveResults = GrouperServiceLogic.groupSave(grouperWsVersion, wsGroupToSaves,
          actAsSubjectLookup, grouperTransactionType, includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsGroupSaveResults.assignResultCodeException(null, null, e, grouperWsVersion);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupSaveResults.getResultMetadata(), this.soap);

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
    GrouperVersion grouperWsVersion = null;
    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsStemSaveResults = GrouperServiceLogic.stemSave(grouperWsVersion, wsStemToSaves,
          actAsSubjectLookup, 
          grouperTransactionType, params);
    } catch (Exception e) {
      wsStemSaveResults.assignResultCodeException(null, null, e, grouperWsVersion);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemSaveResults.getResultMetadata(), this.soap);

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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsStemDeleteResults = GrouperServiceLogic.stemDelete(grouperWsVersion, wsStemLookups,
          actAsSubjectLookup, 
          grouperTransactionType, params);
    } catch (Exception e) {
      wsStemDeleteResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsStemDeleteResults.getResultMetadata(), this.soap);

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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      wsGroupDeleteResults = GrouperServiceLogic.groupDelete(grouperWsVersion, wsGroupLookups,
          actAsSubjectLookup, 
          grouperTransactionType, includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsGroupDeleteResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGroupDeleteResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsGroupDeleteResults;
  }

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
     * @param disabledTime date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS
     * @param enabledTime date this membership will be enabled (for future provisioning), yyyy/MM/dd HH:mm:ss.SSS
     * @param addExternalSubjectIfNotFound T or F, if this is a search by id or identifier, with no source, or the external source,
     * and the subject is not found, then add an external subject (if the user is allowed
     * @return the results
     * @see GrouperVersion
     */
    @SuppressWarnings("unchecked")
    public WsAddMemberResults addMember(final String clientVersion,
        final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
        final String replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
        final String fieldName, final String txType, final String includeGroupDetail,
        final String includeSubjectDetail, final String[] subjectAttributeNames,
        final WsParam[] params, final String disabledTime, 
        final String enabledTime, String addExternalSubjectIfNotFound) {
  
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
  
        boolean addExternalSubjectIfNotFoundBoolean = GrouperServiceUtils.booleanValue(
            addExternalSubjectIfNotFound, false, "addExternalSubjectIfNotFound");
        
        //get the field or null or invalid query exception
        Field field = GrouperServiceUtils.retrieveField(fieldName);
  
        GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
            clientVersion, true);
        
        Timestamp disabledTimestamp = GrouperServiceUtils.stringToTimestamp(disabledTime);
        Timestamp enabledTimestamp = GrouperServiceUtils.stringToTimestamp(enabledTime);
        
        
        wsAddMemberResults = GrouperServiceLogic.addMember(grouperWsVersion, wsGroupLookup,
            subjectLookups, replaceAllExistingBoolean, actAsSubjectLookup, field,
            grouperTransactionType, includeGroupDetailBoolean, includeSubjectDetailBoolean,
            subjectAttributeNames, params, disabledTimestamp, enabledTimestamp, addExternalSubjectIfNotFoundBoolean);
      } catch (Exception e) {
        wsAddMemberResults.assignResultCodeException(null, null, e);
      }
  
      //set response headers
      GrouperServiceUtils.addResponseHeaders(wsAddMemberResults.getResultMetadata(), this.soap);
  
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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsDeleteMemberResults = GrouperServiceLogic.deleteMember(grouperWsVersion, wsGroupLookup,
          subjectLookups, actAsSubjectLookup, field,
          grouperTransactionType, includeGroupDetailBoolean, includeSubjectDetailBoolean,
          subjectAttributeNames, params);
    } catch (Exception e) {
      wsDeleteMemberResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsDeleteMemberResults.getResultMetadata(), this.soap);

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
   * @param fieldName is field name (list name) to search or blank for default list
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param stemName is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemUuid is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem
   * @param enabled is A for all, T or blank for enabled only, F for disabled
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @param pointInTimeFrom 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   *            then the point in time query range will be from the time specified to now.  
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query members at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
   * @return the result of one member add
   */
  public WsGetGroupsLiteResult getGroupsLite(final String clientVersion, String subjectId,
      String subjectSourceId, String subjectIdentifier, String memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, 
      String includeSubjectDetail, 
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String fieldName, String scope, 
      String stemName, String stemUuid, String stemScope, String enabled, 
      String pageSize, String pageNumber, String sortString, String ascending,
      String pointInTimeFrom, String pointInTimeTo) {

    WsGetGroupsLiteResult wsGetGroupsLiteResult = new WsGetGroupsLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      WsMemberFilter wsMemberFilter = GrouperServiceUtils
        .convertMemberFilter(memberFilter);

      StemScope stemScopeEnum = StemScope.valueOfIgnoreCase(stemScope);
      
      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      Integer pageNumberInteger = GrouperUtil.intObjectValue(pageNumber, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);

      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      wsGetGroupsLiteResult = GrouperServiceLogic.getGroupsLite(grouperWsVersion, 
          subjectId, subjectSourceId, subjectIdentifier, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1, fieldName, scope, stemName, stemUuid, stemScopeEnum, enabled, 
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, pointInTimeFromTimestamp,
          pointInTimeToTimestamp);
    } catch (Exception e) {
      wsGetGroupsLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetGroupsLiteResult.getResultMetadata(), this.soap);

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
   * @param disabledTime date this membership will be disabled: yyyy/MM/dd HH:mm:ss.SSS
   * @param enabledTime date this membership will be enabled (for future provisioning): yyyy/MM/dd HH:mm:ss.SSS
   * @param addExternalSubjectIfNotFound T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   * @return the result of one member add
   */
  public WsAddMemberLiteResult addMemberLite(final String clientVersion,
      String groupName, String groupUuid, String subjectId, String subjectSourceId,
      String subjectIdentifier, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String fieldName, String includeGroupDetail,
      String includeSubjectDetail, String subjectAttributeNames, String paramName0,
      String paramValue0, String paramName1, String paramValue1, final String disabledTime, 
      final String enabledTime, String addExternalSubjectIfNotFound) {
    
    LOG.debug("entering addMemberLite");
    
    WsAddMemberLiteResult wsAddMemberLiteResult = new WsAddMemberLiteResult();

    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      boolean addExternalSubjectIfNotFoundBoolean = GrouperServiceUtils.booleanValue(
          addExternalSubjectIfNotFound, false, "addExternalSubjectIfNotFound");

      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      Timestamp disabledTimestamp = GrouperServiceUtils.stringToTimestamp(disabledTime);
      Timestamp enabledTimestamp = GrouperServiceUtils.stringToTimestamp(enabledTime);

      wsAddMemberLiteResult = GrouperServiceLogic.addMemberLite(grouperWsVersion, groupName,
          groupUuid, subjectId, subjectSourceId, subjectIdentifier, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1, disabledTimestamp, enabledTimestamp, addExternalSubjectIfNotFoundBoolean);

    } catch (Exception e) {
      wsAddMemberLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAddMemberLiteResult.getResultMetadata(), this.soap);

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
     * @param pointInTimeFrom 
     *            To query members at a certain point in time or time range in the past, set this value
     *            and/or the value of pointInTimeTo.  This parameter specifies the start of the range
     *            of the point in time query.  If this is specified but pointInTimeTo is not specified, 
     *            then the point in time query range will be from the time specified to now.  
     *            Format:  yyyy/MM/dd HH:mm:ss.SSS
     * @param pointInTimeTo 
     *            To query members at a certain point in time or time range in the past, set this value
     *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
     *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
     *            will be done at a single point in time rather than a range.  If this is specified but 
     *            pointInTimeFrom is not specified, then the point in time query range will be from the 
     *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
     * @return the result of one member query
     */
    public WsHasMemberLiteResult hasMemberLite(final String clientVersion, String groupName,
        String groupUuid, String subjectId, String subjectSourceId, String subjectIdentifier,
        String memberFilter,
        String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
        String fieldName, final String includeGroupDetail, 
        String includeSubjectDetail, String subjectAttributeNames, String paramName0,
        String paramValue0, String paramName1, String paramValue1,
        String pointInTimeFrom, String pointInTimeTo) {
  
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
  
        Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
        Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
        
        GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
            clientVersion, true);
  
        wsHasMemberLiteResult = GrouperServiceLogic.hasMemberLite(grouperWsVersion, groupName,
            groupUuid, subjectId, subjectSourceId, subjectIdentifier, wsMemberFilter, actAsSubjectId, 
            actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
            includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
            paramName1, paramValue1, pointInTimeFromTimestamp, pointInTimeToTimestamp);
      } catch (Exception e) {
        wsHasMemberLiteResult.assignResultCodeException(null, null, e);
      }
  
      //set response headers
      GrouperServiceUtils.addResponseHeaders(wsHasMemberLiteResult.getResultMetadata(), this.soap);
  
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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
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
    GrouperServiceUtils.addResponseHeaders(wsMemberChangeSubjectLiteResult.getResultMetadata(), this.soap);

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
   * @see GrouperVersion
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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsMemberChangeSubjectResults = GrouperServiceLogic.memberChangeSubject(grouperWsVersion, wsMemberChangeSubjects,
          actAsSubjectLookup, 
          grouperTransactionType, includeSubjectDetailBoolean,
          subjectAttributeNames, params);
    } catch (Exception e) {
      wsMemberChangeSubjectResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsMemberChangeSubjectResults.getResultMetadata(), this.soap);

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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
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
    GrouperServiceUtils.addResponseHeaders(wsDeleteMemberLiteResult.getResultMetadata(), this.soap);

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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
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
    GrouperServiceUtils.addResponseHeaders(wsFindStemsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsFindStemsResults;

  }
  
  /**
   * <pre>
   * see if a group has a member (if already a direct member, ignore)
   * GET
   * e.g. /grouperPrivileges/subjects/1234567/groups/aStem:aGroup
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
  public WsGetGrouperPrivilegesLiteResult getGrouperPrivilegesLite(String clientVersion, 
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String groupName, String groupUuid, 
      String stemName, String stemUuid, 
      String privilegeType, String privilegeName,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      String includeSubjectDetail, String subjectAttributeNames, 
      String includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = new WsGetGrouperPrivilegesLiteResult();
    try {

      PrivilegeType privilegeTypeEnum = PrivilegeType.valueOfIgnoreCase(privilegeType);
      
      Privilege privilege = privilegeTypeEnum == null ? null : privilegeTypeEnum.retrievePrivilege(privilegeName);

      //its ok to just pass in the name of the privilege, and not type
      if (privilegeTypeEnum == null && !GrouperUtil.isBlank(privilegeName)) {
        //better be unique
        for (PrivilegeType privilegeTypeEnumLocal : PrivilegeType.values()) {
          Privilege privilegeLocal = null;
          //dont worry if invalid
          try {
            privilegeLocal = privilegeTypeEnumLocal.retrievePrivilege(privilegeName);
          } catch (Exception e) {
            //empty
          }
          if (privilegeLocal != null) {
            if (privilegeTypeEnum != null) {
              throw new RuntimeException("Problem, two privilege types have the same named privilege: " 
                  + privilegeTypeEnumLocal + ", " + privilegeTypeEnum + ": " + privilege);
            }
            privilegeTypeEnum = privilegeTypeEnumLocal;
            privilege = privilegeLocal;
          }
        }
      }
      
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsGetGrouperPrivilegesLiteResult = GrouperServiceLogic.getGrouperPrivilegesLite(grouperWsVersion, subjectId, 
          subjectSourceId, subjectIdentifier, groupName, groupUuid, stemName, stemUuid, privilegeTypeEnum, 
          privilege, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsGetGrouperPrivilegesLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetGrouperPrivilegesLiteResult.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsGetGrouperPrivilegesLiteResult;
    
  }

  /**
   * <pre>
   * add a privilge to a stem or group (ok if already existed)
   * PUT
   * e.g. /grouperPrivileges/subjects/1234567/groups/aStem:aGroup
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
   * @param deleteOldMember T or F as to whether the old member should be deleted (if new member does exist).
   * This defaults to T if it is blank
   * @param privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeName (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param allowed T|F is this is allowing the privilege, or denying it
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
  public WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLite(String clientVersion, 
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String groupName, String groupUuid, 
      String stemName, String stemUuid, 
      String privilegeType, String privilegeName, String allowed,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      String includeSubjectDetail, String subjectAttributeNames, 
      String includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = 
      new WsAssignGrouperPrivilegesLiteResult();

    try {

      PrivilegeType privilegeTypeEnum = PrivilegeType.valueOfIgnoreCase(privilegeType);
      
      Privilege privilege = privilegeTypeEnum == null ? null : privilegeTypeEnum.retrievePrivilege(privilegeName);

      //its ok to just pass in the name of the privilege, and not type
      if (privilegeTypeEnum == null && !GrouperUtil.isBlank(privilegeName)) {
        //better be unique
        for (PrivilegeType privilegeTypeEnumLocal : PrivilegeType.values()) {
          Privilege privilegeLocal = null;
          //dont worry if invalid
          try {
            privilegeLocal = privilegeTypeEnumLocal.retrievePrivilege(privilegeName);
          } catch (Exception e) {
            //empty
          }
          if (privilegeLocal != null) {
            if (privilegeTypeEnum != null) {
              throw new RuntimeException("Problem, two privilege types have the same named privilege: " 
                  + privilegeTypeEnumLocal + ", " + privilegeTypeEnum + ": " + privilege);
            }
            privilegeTypeEnum = privilegeTypeEnumLocal;
            privilege = privilegeLocal;
          }
        }
      }
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      boolean allowedBoolean = GrouperServiceUtils.booleanValue(
          allowed, true, "allowed");
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignGrouperPrivilegesLiteResult = GrouperServiceLogic.assignGrouperPrivilegesLite(grouperWsVersion, subjectId, 
          subjectSourceId, subjectIdentifier, groupName, groupUuid, stemName, stemUuid, privilegeTypeEnum, 
          privilege, allowedBoolean, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsAssignGrouperPrivilegesLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignGrouperPrivilegesLiteResult.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsAssignGrouperPrivilegesLiteResult;
    
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
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetMembershipsResults getMemberships(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, WsSubjectLookup[] wsSubjectLookups, String wsMemberFilter,
      WsSubjectLookup actAsSubjectLookup, String fieldName, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, final WsParam[] params, 
      String[] sourceIds, String scope, 
      WsStemLookup wsStemLookup, String stemScope, String enabled, String[] membershipIds) {  
  
    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
  
    try {
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      WsMemberFilter memberFilter = GrouperServiceUtils
        .convertMemberFilter(wsMemberFilter);
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
      
      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
      
      StemScope theStemScope = StringUtils.isBlank(stemScope) ? null : StemScope.valueOfIgnoreCase(stemScope);

      //if its blank it is a placeholder for axis, just null it out
      if (wsStemLookup != null && wsStemLookup.blank()) {
        wsStemLookup = null;
      }
      
      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(grouperWsVersion, wsGroupLookups, 
          wsSubjectLookups, memberFilter, actAsSubjectLookup, field, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, params, sourceIds, scope, wsStemLookup, theStemScope, enabled, membershipIds);
    } catch (Exception e) {
      wsGetMembershipsResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetMembershipsResults.getResultMetadata(), this.soap);

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
   * @return the memberships, or none if none found
   */
  public WsGetMembershipsResults getMembershipsLite(final String clientVersion,
      String groupName, String groupUuid, String subjectId, String sourceId, String subjectIdentifier, 
      String wsMemberFilter,
      String includeSubjectDetail, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String fieldName, String subjectAttributeNames,
      String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds, String scope, String stemName, 
      String stemUuid, String stemScope, String enabled, String membershipIds) {
  
    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
    try {
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      WsMemberFilter memberFilter = GrouperServiceUtils
        .convertMemberFilter(wsMemberFilter);
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
      
      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
      
      StemScope theStemScope = StringUtils.isBlank(stemScope) ? null : StemScope.valueOfIgnoreCase(stemScope);
      
      wsGetMembershipsResults = GrouperServiceLogic.getMembershipsLite(grouperWsVersion, groupName,
          groupUuid, subjectId, sourceId, subjectIdentifier, memberFilter,includeSubjectDetailBoolean, 
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, field, subjectAttributeNames, 
          includeGroupDetailBoolean, paramName0, paramValue1, paramName1, paramValue1, sourceIds, scope, 
          stemName, stemUuid, theStemScope, enabled, membershipIds);
    } catch (Exception e) {
      wsGetMembershipsResults.assignResultCodeException(null, null, e);
      
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetMembershipsResults.getResultMetadata(), this.soap);
    
    return wsGetMembershipsResults;
  }

  /**
   * 
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
  public WsGetSubjectsResults getSubjects(final String clientVersion,
      WsSubjectLookup[] wsSubjectLookups, String searchString, String includeSubjectDetail, 
      String[] subjectAttributeNames, WsSubjectLookup actAsSubjectLookup, String[] sourceIds, 
      WsGroupLookup wsGroupLookup, String wsMemberFilter,
       String fieldName, 
      String includeGroupDetail, final WsParam[] params) {  
  
    WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
  
    try {
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      WsMemberFilter memberFilter = GrouperServiceUtils
        .convertMemberFilter(wsMemberFilter);
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
      
      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
      
      //if its blank it is a placeholder for axis, just null it out
      if (wsGroupLookup != null && wsGroupLookup.blank()) {
        wsGroupLookup = null;
      }
      
      wsGetSubjectsResults = GrouperServiceLogic.getSubjects(grouperWsVersion,
          wsSubjectLookups, searchString, includeSubjectDetailBoolean, subjectAttributeNames, 
          actAsSubjectLookup, sourceIds, wsGroupLookup, memberFilter, field, 
          includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsGetSubjectsResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetSubjectsResults.getResultMetadata(), this.soap);
  
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
  public WsGetSubjectsResults getSubjectsLite(final String clientVersion,
      String subjectId, String sourceId, String subjectIdentifier, String searchString,
      String includeSubjectDetail, String subjectAttributeNames,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String sourceIds,
      String groupName, String groupUuid, String wsMemberFilter,
      String fieldName, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
    try {
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      WsMemberFilter memberFilter = GrouperServiceUtils
        .convertMemberFilter(wsMemberFilter);
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
      
      //get the field or null or invalid query exception
      Field field = GrouperServiceUtils.retrieveField(fieldName);
      
      wsGetSubjectsResults = GrouperServiceLogic.getSubjectsLite(grouperWsVersion, subjectId, sourceId, subjectIdentifier, searchString, includeSubjectDetailBoolean, 
          subjectAttributeNames, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, sourceIds, groupName,
          groupUuid, memberFilter, field, includeGroupDetailBoolean, paramName0, paramValue1, paramName1, paramValue1);
    } catch (Exception e) {
      wsGetSubjectsResults.assignResultCodeException(null, null, e);
      
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetSubjectsResults.getResultMetadata(), this.soap);
    
    return wsGetSubjectsResults;
  }
      
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
  public WsAssignGrouperPrivilegesResults assignGrouperPrivileges(
      final String clientVersion, 
      final WsSubjectLookup[] wsSubjectLookups,
      final WsGroupLookup wsGroupLookup,
      final WsStemLookup wsStemLookup,
      final String privilegeType, final String[] privilegeNames,
      final String allowed,
      final String replaceAllExisting, final String txType,
      final WsSubjectLookup actAsSubjectLookup,
      final String includeSubjectDetail, final String[] subjectAttributeNames, 
      final String includeGroupDetail,  final WsParam[] params) {
  
    WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = new WsAssignGrouperPrivilegesResults();
    
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

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      PrivilegeType privilegeTypeEnum = PrivilegeType.valueOfIgnoreCase(privilegeType);
      
      Privilege[] privileges = new Privilege[GrouperUtil.length(privilegeNames)];
      
      for (int i=0;i<GrouperUtil.length(privilegeNames); i++) {

        String privilegeName = privilegeNames[i];

        Privilege privilege = privilegeTypeEnum == null ? null : privilegeTypeEnum.retrievePrivilege(privilegeName);

        //its ok to just pass in the name of the privilege, and not type
        if (privilegeTypeEnum == null && !GrouperUtil.isBlank(privilegeName)) {
          //better be unique
          for (PrivilegeType privilegeTypeEnumLocal : PrivilegeType.values()) {
            Privilege privilegeLocal = null;
            //dont worry if invalid
            try {
              privilegeLocal = privilegeTypeEnumLocal.retrievePrivilege(privilegeName);
            } catch (Exception e) {
              //empty
            }
            if (privilegeLocal != null) {
              if (privilegeTypeEnum != null) {
                throw new RuntimeException("Problem, two privilege types have the same named privilege: " 
                    + privilegeTypeEnumLocal + ", " + privilegeTypeEnum + ": " + privilege);
              }
              privilegeTypeEnum = privilegeTypeEnumLocal;
              privilege = privilegeLocal;
            }
          }
        }
        privileges[i] = privilege;
        
      }
      
      boolean allowedBoolean = GrouperServiceUtils.booleanValue(
          allowed, true, "allowed");
  

      
      wsAssignGrouperPrivilegesResults = GrouperServiceLogic.assignGrouperPrivileges(grouperWsVersion, 
          wsSubjectLookups, wsGroupLookup, wsStemLookup, privilegeTypeEnum, privileges, allowedBoolean, replaceAllExistingBoolean, grouperTransactionType, actAsSubjectLookup, 
          includeSubjectDetailBoolean, subjectAttributeNames, includeGroupDetailBoolean, params);
    } catch (Exception e) {
      wsAssignGrouperPrivilegesResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignGrouperPrivilegesResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
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
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetAttributeAssignmentsResults getAttributeAssignments(
      String clientVersion, String attributeAssignType,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsAttributeDefLookup[] wsAttributeDefLookups, WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups, WsSubjectLookup[] wsOwnerSubjectLookups, 
      WsMembershipLookup[] wsOwnerMembershipLookups, WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups, 
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups, 
      String[] actions, 
      String includeAssignmentsOnAssignments, WsSubjectLookup actAsSubjectLookup, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, final WsParam[] params, 
      String enabled) {  

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
  
    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      boolean includeAssignmentsOnAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAssignmentsOnAssignments, false, "includeAssignmentsOnAssignments");

      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.convertAttributeAssignType(attributeAssignType);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(grouperWsVersion, attributeAssignTypeEnum, wsAttributeAssignLookups, wsAttributeDefLookups, wsAttributeDefNameLookups, wsOwnerGroupLookups, wsOwnerStemLookups, wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups, wsOwnerAttributeDefLookups, actions, includeAssignmentsOnAssignmentsBoolean, actAsSubjectLookup, includeSubjectDetailBoolean, subjectAttributeNames, includeGroupDetailBoolean, params, enabled);

    } catch (Exception e) {
      wsGetAttributeAssignmentsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetAttributeAssignmentsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsGetAttributeAssignmentsResults; 
  
  }

  /**
   * get attributeAssignments based on inputs
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
   * @param actAsSubjectId 
   * @param actAsSubjectSourceId 
   * @param actAsSubjectIdentifier 
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
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetAttributeAssignmentsResults getAttributeAssignmentsLite(
      String clientVersion, String attributeAssignType,
      String attributeAssignId,
      String wsAttributeDefName, String wsAttributeDefId, String wsAttributeDefNameName, String wsAttributeDefNameId,
      String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, 
      String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier,
      String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId,
      String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier, 
      String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, 
      String action, 
      String includeAssignmentsOnAssignments, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeSubjectDetail,
      String subjectAttributeNames, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, 
      String enabled) {  

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
    
    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      boolean includeAssignmentsOnAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAssignmentsOnAssignments, false, "includeAssignmentsOnAssignments");

      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.convertAttributeAssignType(attributeAssignType);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignmentsLite(
          grouperWsVersion, attributeAssignTypeEnum, attributeAssignId, wsAttributeDefName, 
          wsAttributeDefId, wsAttributeDefNameName, wsAttributeDefNameId, wsOwnerGroupName, 
          wsOwnerGroupId, wsOwnerStemName, wsOwnerStemId, wsOwnerSubjectId, wsOwnerSubjectSourceId, 
          wsOwnerSubjectIdentifier, wsOwnerMembershipId, wsOwnerMembershipAnyGroupName, 
          wsOwnerMembershipAnyGroupId, wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, 
          wsOwnerMembershipAnySubjectIdentifier, wsOwnerAttributeDefName, wsOwnerAttributeDefId, 
          action, includeAssignmentsOnAssignmentsBoolean, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1, enabled );

    } catch (Exception e) {
      wsGetAttributeAssignmentsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetAttributeAssignmentsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
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
  public WsAssignAttributesResults assignAttributes(
      String clientVersion, String attributeAssignType,
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      String attributeAssignOperation,
      WsAttributeAssignValue[] values,
      String assignmentNotes, String assignmentEnabledTime,
      String assignmentDisabledTime, String delegatable,
      String attributeAssignValueOperation,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups, WsSubjectLookup[] wsOwnerSubjectLookups, 
      WsMembershipLookup[] wsOwnerMembershipLookups, WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups, 
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups, WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups,
      String[] actions, WsSubjectLookup actAsSubjectLookup, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, WsParam[] params,
      WsAttributeDefLookup[] attributeDefsToReplace, String[] actionsToReplace, String[] attributeDefTypesToReplace) {  
  
    WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
  
    try {
  
      AttributeAssignOperation attributeAssignOperationEnum = GrouperServiceUtils.convertAttributeAssignOperation(attributeAssignOperation);
      AttributeAssignValueOperation attributeAssignValueOperationEnum = GrouperServiceUtils.convertAttributeAssignValueOperation(attributeAssignValueOperation);

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.convertAttributeAssignType(attributeAssignType);
      
      Timestamp assignmentEnabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentEnabledTime);
      Timestamp assignmentDisabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentDisabledTime);
      
      AttributeAssignDelegatable attributeAssignDelegatableEnum = GrouperServiceUtils.convertAttributeAssignDelegatable(delegatable);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignAttributesResults = GrouperServiceLogic.assignAttributes(grouperWsVersion, attributeAssignTypeEnum, 
          wsAttributeDefNameLookups, attributeAssignOperationEnum, values, assignmentNotes, assignmentEnabledTimestamp, 
          assignmentDisabledTimestamp, attributeAssignDelegatableEnum, attributeAssignValueOperationEnum, wsAttributeAssignLookups, 
          wsOwnerGroupLookups, wsOwnerStemLookups, wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups, 
          wsOwnerAttributeDefLookups, wsOwnerAttributeAssignLookups, actions, actAsSubjectLookup, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, params, attributeDefsToReplace, 
          actionsToReplace, attributeDefTypesToReplace);
  
    } catch (Exception e) {
      wsAssignAttributesResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignAttributesResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
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
   * @param delegatable  really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
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
  public WsAssignAttributesLiteResults assignAttributesLite(
      String clientVersion, String attributeAssignType,
      String wsAttributeDefNameName, String wsAttributeDefNameId,
      String attributeAssignOperation,
      String valueId, String valueSystem, String valueFormatted,
      String assignmentNotes, String assignmentEnabledTime,
      String assignmentDisabledTime, String delegatable,
      String attributeAssignValueOperation,
      String wsAttributeAssignId,
      String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, 
      String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier,
      String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId,
      String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier,
      String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, String wsOwnerAttributeAssignId,
      String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, String includeSubjectDetail,
      String subjectAttributeNames, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {  
  
    WsAssignAttributesLiteResults wsAssignAttributesLiteResults = new WsAssignAttributesLiteResults();
    
    try {
  
      AttributeAssignOperation attributeAssignOperationEnum = GrouperServiceUtils.convertAttributeAssignOperation(attributeAssignOperation);
      AttributeAssignValueOperation attributeAssignValueOperationEnum = GrouperServiceUtils.convertAttributeAssignValueOperation(attributeAssignValueOperation);

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.convertAttributeAssignType(attributeAssignType);
      
      Timestamp assignmentEnabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentEnabledTime);
      Timestamp assignmentDisabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentDisabledTime);
      
      AttributeAssignDelegatable attributeAssignDelegatableEnum = GrouperServiceUtils.convertAttributeAssignDelegatable(delegatable);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignAttributesLiteResults = GrouperServiceLogic.assignAttributesLite(
          grouperWsVersion, attributeAssignTypeEnum, wsAttributeDefNameName, wsAttributeDefNameId, 
          attributeAssignOperationEnum, valueId, valueSystem, valueFormatted, assignmentNotes, assignmentEnabledTimestamp,
          assignmentDisabledTimestamp, attributeAssignDelegatableEnum, attributeAssignValueOperationEnum, wsAttributeAssignId, wsOwnerGroupName, 
          wsOwnerGroupId, wsOwnerStemName, wsOwnerStemId, wsOwnerSubjectId, wsOwnerSubjectSourceId, 
          wsOwnerSubjectIdentifier, wsOwnerMembershipId, wsOwnerMembershipAnyGroupName, 
          wsOwnerMembershipAnyGroupId, wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, 
          wsOwnerMembershipAnySubjectIdentifier, wsOwnerAttributeDefName, wsOwnerAttributeDefId, wsOwnerAttributeAssignId, 
          action, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, paramValue1 );
  
    } catch (Exception e) {
      wsAssignAttributesLiteResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignAttributesLiteResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
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
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query permissions at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
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
  public WsGetPermissionAssignmentsResults getPermissionAssignments(
      String clientVersion, 
      WsAttributeDefLookup[] wsAttributeDefLookups, WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      WsGroupLookup[] roleLookups, WsSubjectLookup[] wsSubjectLookups, 
      String[] actions, String includePermissionAssignDetail,
      String includeAttributeDefNames, String includeAttributeAssignments,
      String includeAssignmentsOnAssignments, WsSubjectLookup actAsSubjectLookup, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, WsParam[] params, 
      String enabled, String pointInTimeFrom, String pointInTimeTo, String immediateOnly,
      String permissionType, String permissionProcessor, WsPermissionEnvVar[] limitEnvVars, String includeLimits) {  
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new WsGetPermissionAssignmentsResults();
  
    try {
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      boolean includeAssignmentsOnAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAssignmentsOnAssignments, false, "includeAssignmentsOnAssignments");
  
      boolean includePermissionAssignDetailBoolean = GrouperServiceUtils.booleanValue(
          includePermissionAssignDetail, false, "includePermissionAssignDetail");
  
      boolean includeAttributeDefNamesBoolean = GrouperServiceUtils.booleanValue(
          includeAttributeDefNames, false, "includeAttributeDefNames");
  
      boolean includeAttributeAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAttributeAssignments, false, "includeAttributeAssignments");
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      boolean immediateOnlyBoolean = GrouperServiceUtils.booleanValue(
          immediateOnly, false, "immediateOnly");
      
      PermissionType permissionTypeEnum = PermissionType.valueOfIgnoreCase(permissionType, false);
      PermissionProcessor permissionProcessorEnum = PermissionProcessor.valueOfIgnoreCase(permissionProcessor, false);
      
      boolean includeLimitsBoolean = GrouperServiceUtils.booleanValue(
          includeLimits, false, "includeLimits");
      
      wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignments(grouperWsVersion, wsAttributeDefLookups, 
          wsAttributeDefNameLookups, roleLookups, wsSubjectLookups, actions, includePermissionAssignDetailBoolean, 
          includeAttributeDefNamesBoolean, includeAttributeAssignmentsBoolean, includeAssignmentsOnAssignmentsBoolean, 
          actAsSubjectLookup, includeSubjectDetailBoolean, subjectAttributeNames, includeGroupDetailBoolean, params, enabled,
          pointInTimeFromTimestamp, pointInTimeToTimestamp, immediateOnlyBoolean,
          permissionTypeEnum, permissionProcessorEnum, limitEnvVars, includeLimitsBoolean);
  
    } catch (Exception e) {
      wsGetPermissionAssignmentsResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetPermissionAssignmentsResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
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
   *            Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeTo 
   *            To query permissions at a certain point in time or time range in the past, set this value
   *            and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   *            of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   *            will be done at a single point in time rather than a range.  If this is specified but 
   *            pointInTimeFrom is not specified, then the point in time query range will be from the 
   *            minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS
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
  public WsGetPermissionAssignmentsResults getPermissionAssignmentsLite(
      String clientVersion, 
      String wsAttributeDefName, String wsAttributeDefId, String wsAttributeDefNameName, String wsAttributeDefNameId,
      String roleName, String roleId, 
      String wsSubjectId, String wsSubjectSourceId, String wsSubjectIdentifier,
      String action, String includePermissionAssignDetail,
      String includeAttributeDefNames, String includeAttributeAssignments,
      String includeAssignmentsOnAssignments, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeSubjectDetail,
      String subjectAttributeNames, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String enabled, String pointInTimeFrom, String pointInTimeTo,
      String immediateOnly,
      String permissionType, String permissionProcessor, 
      String limitEnvVarName0, String limitEnvVarValue0, 
      String limitEnvVarType0, String limitEnvVarName1, 
      String limitEnvVarValue1, String limitEnvVarType1, String includeLimits) {  
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new WsGetPermissionAssignmentsResults();
    
    try {
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      boolean includeAssignmentsOnAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAssignmentsOnAssignments, false, "includeAssignmentsOnAssignments");
  
      boolean includePermissionAssignDetailBoolean = GrouperServiceUtils.booleanValue(
          includePermissionAssignDetail, false, "includePermissionAssignDetail");
  
      boolean includeAttributeDefNamesBoolean = GrouperServiceUtils.booleanValue(
          includeAttributeDefNames, false, "includeAttributeDefNames");
  
      boolean includeAttributeAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAttributeAssignments, false, "includeAttributeAssignments");
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      boolean immediateOnlyBoolean = GrouperServiceUtils.booleanValue(
          immediateOnly, false, "immediateOnly");
      
      PermissionType permissionTypeEnum = PermissionType.valueOfIgnoreCase(permissionType, false);
      PermissionProcessor permissionProcessorEnum = PermissionProcessor.valueOfIgnoreCase(permissionProcessor, false);

      boolean includeLimitsBoolean = GrouperServiceUtils.booleanValue(
          includeLimits, false, "includeLimits");
      
      wsGetPermissionAssignmentsResults = GrouperServiceLogic.getPermissionAssignmentsLite(
          grouperWsVersion, wsAttributeDefName, 
          wsAttributeDefId, wsAttributeDefNameName, wsAttributeDefNameId, roleName, 
          roleId, wsSubjectId, wsSubjectSourceId, 
          wsSubjectIdentifier, action, includePermissionAssignDetailBoolean, 
          includeAttributeDefNamesBoolean, includeAttributeAssignmentsBoolean, 
          includeAssignmentsOnAssignmentsBoolean, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, 
          paramValue0, paramName1, paramValue1, enabled, pointInTimeFromTimestamp,
          pointInTimeToTimestamp, immediateOnlyBoolean, permissionTypeEnum, 
          permissionProcessorEnum, limitEnvVarName0, limitEnvVarValue0, 
          limitEnvVarType0, limitEnvVarName1, limitEnvVarValue1, limitEnvVarType1, includeLimitsBoolean);
  
    } catch (Exception e) {
      wsGetPermissionAssignmentsResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetPermissionAssignmentsResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
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
   * @param disallowed T or F if the permission is disallowed
   * @return the results
   */
  public WsAssignPermissionsResults assignPermissions(
      String clientVersion, String permissionType,
      WsAttributeDefNameLookup[] permissionDefNameLookups,
      String permissionAssignOperation,
      String assignmentNotes, String assignmentEnabledTime,
      String assignmentDisabledTime, String delegatable,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsGroupLookup[] roleLookups, 
      WsMembershipAnyLookup[] subjectRoleLookups, 
      String[] actions, WsSubjectLookup actAsSubjectLookup, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, WsParam[] params,
      WsAttributeDefLookup[] attributeDefsToReplace, String[] actionsToReplace, 
      String disallowed) {  
  
    WsAssignPermissionsResults wsAssignPermissionsResults = new WsAssignPermissionsResults();
  
    try {
  
      PermissionAssignOperation permissionAssignOperationEnum = GrouperServiceUtils.convertPermissionAssignOperation(permissionAssignOperation);
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      Boolean disallowedBoolean = GrouperServiceUtils.booleanObjectValue(
          disallowed, "disallowed");
  
      PermissionType permissionTypeEnum = GrouperServiceUtils.convertPermissionType(permissionType);
      Timestamp assignmentEnabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentEnabledTime);
      Timestamp assignmentDisabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentDisabledTime);
      
      AttributeAssignDelegatable attributeAssignDelegatableEnum = GrouperServiceUtils.convertAttributeAssignDelegatable(delegatable);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignPermissionsResults = GrouperServiceLogic.assignPermissions(grouperWsVersion, permissionTypeEnum, 
          permissionDefNameLookups, permissionAssignOperationEnum, assignmentNotes, assignmentEnabledTimestamp, 
          assignmentDisabledTimestamp, attributeAssignDelegatableEnum, wsAttributeAssignLookups, 
          roleLookups, subjectRoleLookups, actions, actAsSubjectLookup, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, params, attributeDefsToReplace, actionsToReplace,
          disallowedBoolean);
  
    } catch (Exception e) {
      wsAssignPermissionsResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignPermissionsResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
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
   * @param disallowed T or F if the permission is disallowed
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsAssignPermissionsLiteResults assignPermissionsLite(
      String clientVersion, String permissionType,
      String permissionDefNameName, String permissionDefNameId,
      String permissionAssignOperation,
      String assignmentNotes, String assignmentEnabledTime,
      String assignmentDisabledTime, String delegatable,
      String wsAttributeAssignId,
      String roleName, String roleId,
      String subjectRoleName, String subjectRoleId,
      String subjectRoleSubjectId, String subjectRoleSubjectSourceId, String subjectRoleSubjectIdentifier, 
      String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, String includeSubjectDetail,
      String subjectAttributeNames, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, 
      String disallowed) {  
  
    WsAssignPermissionsLiteResults wsAssignPermissionsLiteResults = new WsAssignPermissionsLiteResults();
    
    try {
  
      PermissionAssignOperation permissionAssignOperationEnum = GrouperServiceUtils.convertPermissionAssignOperation(permissionAssignOperation);
      
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      Boolean disallowedBoolean = GrouperServiceUtils.booleanObjectValue(
          disallowed, "disallowed");

      PermissionType permissionTypeEnum = GrouperServiceUtils.convertPermissionType(permissionType);
      Timestamp assignmentEnabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentEnabledTime);
      Timestamp assignmentDisabledTimestamp = GrouperServiceUtils.stringToTimestamp(assignmentDisabledTime);
      
      AttributeAssignDelegatable attributeAssignDelegatableEnum = GrouperServiceUtils.convertAttributeAssignDelegatable(delegatable);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignPermissionsLiteResults = GrouperServiceLogic.assignPermissionsLite(
          grouperWsVersion, permissionTypeEnum, permissionDefNameName, permissionDefNameId, 
          permissionAssignOperationEnum, assignmentNotes, assignmentEnabledTimestamp,
          assignmentDisabledTimestamp, attributeAssignDelegatableEnum, wsAttributeAssignId, roleName, 
          roleId, subjectRoleName, 
          subjectRoleId, subjectRoleSubjectId, subjectRoleSubjectSourceId, 
          subjectRoleSubjectIdentifier,  
          action, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, paramValue0, paramName1, 
          paramValue1,
          disallowedBoolean);
  
    } catch (Exception e) {
      wsAssignPermissionsLiteResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignPermissionsLiteResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
    return wsAssignPermissionsLiteResults; 
  
  }

}
