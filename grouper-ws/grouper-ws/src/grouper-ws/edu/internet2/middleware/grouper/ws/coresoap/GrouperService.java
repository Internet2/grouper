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
package edu.internet2.middleware.grouper.ws.coresoap;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionAssignOperation;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsInheritanceSetRelation;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;

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
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @param ascending T or null for ascending, F for descending.  
   * @return the members, or no members if none found
   */
  public WsGetMembersLiteResult getMembersLite(final String clientVersion,
      String groupName, String groupUuid, String memberFilter, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds,
      String pointInTimeFrom, String pointInTimeTo, String pageSize, String pageNumber,
      String sortString, String ascending ) {

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

      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      Integer pageNumberInteger = GrouperUtil.intObjectValue(pageNumber, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);

      wsGetMembersLiteResult = GrouperServiceLogic.getMembersLite(grouperWsVersion, 
          groupName, groupUuid, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1, sourceIds, pointInTimeFromTimestamp, pointInTimeToTimestamp,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean);
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
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @param ascending T or null for ascending, F for descending.  
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetMembersResults getMembers(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, String memberFilter,
      WsSubjectLookup actAsSubjectLookup, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params, String[] sourceIds,
      String pointInTimeFrom, String pointInTimeTo, String pageSize, String pageNumber,
      String sortString, String ascending ) {
	  
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
  
      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      Integer pageNumberInteger = GrouperUtil.intObjectValue(pageNumber, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);

      wsGetMembersResults = GrouperServiceLogic.getMembers(grouperWsVersion, wsGroupLookups, 
          wsMemberFilter, actAsSubjectLookup, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params, sourceIds, 
          pointInTimeFromTimestamp, pointInTimeToTimestamp,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean);
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
   * @param typeOfGroup1 type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   * @return the result of one member add
   */
  public WsGroupSaveLiteResult groupSaveLite(final String clientVersion,
      String groupLookupUuid, String groupLookupName, String groupUuid,String groupName, 
      String displayExtension,String description,  String saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String typeOfGroup1) {

    WsGroupSaveLiteResult wsGroupSaveLiteResult = new WsGroupSaveLiteResult();
    GrouperVersion grouperWsVersion = null;
    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      TypeOfGroup typeOfGroup = TypeOfGroup.valueOfIgnoreCase(typeOfGroup1, false);
      
      wsGroupSaveLiteResult = GrouperServiceLogic.groupSaveLite(grouperWsVersion, groupLookupUuid,
          groupLookupName, groupUuid, groupName, displayExtension, description, saveModeEnum,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean,  
          paramName0, paramValue0, paramName1, paramValue1, typeOfGroup);
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
   * @param wsOwnerStemLookups are the stem lookups if looking for stem privileges
   * @param wsOwnerAttributeDefLookups are the attribute definition lookups if looking for attribute definition privileges
   * @param fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   * @param serviceRole to filter attributes that a user has a certain role
   * @param serviceLookup if filtering by users in a service, then this is the service to look in
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending T or null for ascending, F for descending.  
   * @param pageSizeForMember page size if paging in the members part
   * @param pageNumberForMember page number 1 indexed if paging in the members part
   * @param sortStringForMember must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   * @param ascendingForMember T or null for ascending, F for descending in the members part
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsGetMembershipsResults getMemberships(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, WsSubjectLookup[] wsSubjectLookups, String wsMemberFilter,
      WsSubjectLookup actAsSubjectLookup, String fieldName, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, final WsParam[] params, 
      String[] sourceIds, String scope, 
      WsStemLookup wsStemLookup, String stemScope, String enabled, String[] membershipIds, 
      WsStemLookup[] wsOwnerStemLookups, WsAttributeDefLookup[] wsOwnerAttributeDefLookups, 
      String fieldType, String serviceRole, WsAttributeDefNameLookup serviceLookup, String pageSize, String pageNumber,
      String sortString, String ascending, String pageSizeForMember, String pageNumberForMember,
      String sortStringForMember, String ascendingForMember) {  
    
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
      
      FieldType fieldTypeEnum = GrouperServiceUtils.retrieveFieldType(fieldType);
      
      StemScope theStemScope = StringUtils.isBlank(stemScope) ? null : StemScope.valueOfIgnoreCase(stemScope);

      //if its blank it is a placeholder for axis, just null it out
      if (wsStemLookup != null && wsStemLookup.blank()) {
        wsStemLookup = null;
      }

      ServiceRole serviceRoleEnum = ServiceRole.valueOfIgnoreCase(serviceRole, false);

      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      Integer pageNumberInteger = GrouperUtil.intObjectValue(pageNumber, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);

      Integer pageSizeForMemberInteger = GrouperUtil.intObjectValue(pageSizeForMember, true);
      Integer pageNumberForMemberInteger = GrouperUtil.intObjectValue(pageNumberForMember, true);
      
      Boolean ascendingForMemberBoolean = GrouperUtil.booleanObjectValue(ascendingForMember);

      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(grouperWsVersion, wsGroupLookups, 
          wsSubjectLookups, memberFilter, actAsSubjectLookup, field, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, params, sourceIds, scope, wsStemLookup, theStemScope, enabled, membershipIds,
          wsOwnerStemLookups, wsOwnerAttributeDefLookups, fieldTypeEnum, serviceRoleEnum, serviceLookup,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, pageSizeForMemberInteger, pageNumberForMemberInteger, 
          sortStringForMember, ascendingForMemberBoolean);

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
   * @param ownerStemName if looking for privileges on stems, put the stem name to look for here
   * @param ownerStemUuid if looking for privileges on stems, put the stem uuid here
   * @param nameOfOwnerAttributeDef if looking for privileges on attribute definitions, put the name of the attribute definition here
   * @param ownerAttributeDefUuid if looking for privileges on attribute definitions, put the uuid of the attribute definition here
   * @param fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   * @param serviceRole to filter attributes that a user has a certain role
   * @param serviceId if filtering by users in a service, then this is the service to look in, mutually exclusive with serviceName
   * @param serviceName if filtering by users in a service, then this is the service to look in, mutually exclusive with serviceId
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending T or null for ascending, F for descending.  
   * @param pageSizeForMember page size if paging in the members part
   * @param pageNumberForMember page number 1 indexed if paging in the members part
   * @param sortStringForMember must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   * @param ascendingForMember T or null for ascending, F for descending in the members part
   * @return the memberships, or none if none found
   */
  public WsGetMembershipsResults getMembershipsLite(final String clientVersion,
      String groupName, String groupUuid, String subjectId, String sourceId, String subjectIdentifier, 
      String wsMemberFilter,
      String includeSubjectDetail, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String fieldName, String subjectAttributeNames,
      String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds, String scope, String stemName, 
      String stemUuid, String stemScope, String enabled, String membershipIds, String ownerStemName, String ownerStemUuid, String nameOfOwnerAttributeDef, 
      String ownerAttributeDefUuid, String fieldType, String serviceRole, 
      String serviceId, String serviceName, String pageSize, String pageNumber,
      String sortString, String ascending, String pageSizeForMember, String pageNumberForMember,
      String sortStringForMember, String ascendingForMember) {
  
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
      FieldType fieldTypeEnum = GrouperServiceUtils.retrieveFieldType(fieldType);
      
      StemScope theStemScope = StringUtils.isBlank(stemScope) ? null : StemScope.valueOfIgnoreCase(stemScope);

      ServiceRole serviceRoleEnum = ServiceRole.valueOfIgnoreCase(serviceRole, false);
      
      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      Integer pageNumberInteger = GrouperUtil.intObjectValue(pageNumber, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);

      Integer pageSizeForMemberInteger = GrouperUtil.intObjectValue(pageSizeForMember, true);
      Integer pageNumberForMemberInteger = GrouperUtil.intObjectValue(pageNumberForMember, true);
      
      Boolean ascendingForMemberBoolean = GrouperUtil.booleanObjectValue(ascendingForMember);

      wsGetMembershipsResults = GrouperServiceLogic.getMembershipsLite(grouperWsVersion, groupName,
          groupUuid, subjectId, sourceId, subjectIdentifier, memberFilter,includeSubjectDetailBoolean, 
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, field, subjectAttributeNames, 
          includeGroupDetailBoolean, paramName0, paramValue1, paramName1, paramValue1, sourceIds, scope, 
          stemName, stemUuid, theStemScope, enabled, membershipIds, ownerStemName, ownerStemUuid, 
          nameOfOwnerAttributeDef, ownerAttributeDefUuid, fieldTypeEnum, serviceRoleEnum, serviceId, serviceName,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, pageSizeForMemberInteger, pageNumberForMemberInteger, 
          sortStringForMember, ascendingForMemberBoolean
          );

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
      String enabled, String attributeDefValueType, String theValue, 
      String includeAssignmentsFromAssignments, String attributeDefType,
      WsAttributeAssignLookup[] wsAssignAssignOwnerAttributeAssignLookups,
      WsAttributeDefLookup[] wsAssignAssignOwnerAttributeDefLookups, 
      WsAttributeDefNameLookup[] wsAssignAssignOwnerAttributeDefNameLookups,
      String[] wsAssignAssignOwnerActions) {  

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
  
    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      boolean includeAssignmentsOnAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAssignmentsOnAssignments, false, "includeAssignmentsOnAssignments");

      boolean includeAssignmentsFromAssignmentsBoolean = GrouperServiceUtils.booleanValue(
              includeAssignmentsFromAssignments, false, "includeAssignmentsFromAssignments");

      AttributeDefValueType attributeDefValueTypeEnum = GrouperServiceUtils.convertAttributeDefValueType(attributeDefValueType);
      AttributeDefType attributeDefTypeEnum = GrouperServiceUtils.convertAttributeDefType(attributeDefType);

      if (!StringUtils.isBlank(theValue) && attributeDefValueTypeEnum == null) {
        throw new WsInvalidQueryException("If you are sending a value then you need to send attributeDefValueType!");
      }
      
      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.convertAttributeAssignType(attributeAssignType);
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsGetAttributeAssignmentsResults = GrouperServiceLogic.getAttributeAssignments(grouperWsVersion, 
    		  attributeAssignTypeEnum, wsAttributeAssignLookups, wsAttributeDefLookups, wsAttributeDefNameLookups, 
    		  wsOwnerGroupLookups, wsOwnerStemLookups, wsOwnerSubjectLookups, wsOwnerMembershipLookups, 
    		  wsOwnerMembershipAnyLookups, wsOwnerAttributeDefLookups, actions, 
    		  includeAssignmentsOnAssignmentsBoolean, actAsSubjectLookup, 
    		  includeSubjectDetailBoolean, subjectAttributeNames, includeGroupDetailBoolean, params, enabled,
    		  attributeDefValueTypeEnum, theValue, includeAssignmentsFromAssignmentsBoolean, attributeDefTypeEnum,
    		  wsAssignAssignOwnerAttributeAssignLookups,
          wsAssignAssignOwnerAttributeDefLookups, 
          wsAssignAssignOwnerAttributeDefNameLookups,
          wsAssignAssignOwnerActions);

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
   * @param attributeDefValueType required if sending theValue, can be:
   * floating, integer, memberId, string, timestamp
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
      String enabled, String attributeDefValueType, String theValue, String includeAssignmentsFromAssignments, String attributeDefType,
      String wsAssignAssignOwnerAttributeAssignId, 
      String wsAssignAssignOwnerIdOfAttributeDef, String wsAssignAssignOwnerNameOfAttributeDef,
      String wsAssignAssignOwnerIdOfAttributeDefName, String wsAssignAssignOwnerNameOfAttributeDefName, String wsAssignAssignOwnerAction) {  

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
    
    try {

      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");

      boolean includeAssignmentsOnAssignmentsBoolean = GrouperServiceUtils.booleanValue(
          includeAssignmentsOnAssignments, false, "includeAssignmentsOnAssignments");

      boolean includeAssignmentsFromAssignmentsBoolean = GrouperServiceUtils.booleanValue(
              includeAssignmentsFromAssignments, false, "includeAssignmentsFromAssignments");

      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.convertAttributeAssignType(attributeAssignType);
      
      AttributeDefValueType attributeDefValueTypeEnum = GrouperServiceUtils.convertAttributeDefValueType(attributeDefValueType);

      AttributeDefType attributeDefTypeEnum = GrouperServiceUtils.convertAttributeDefType(attributeDefType);

      if (!StringUtils.isBlank(theValue) && attributeDefValueTypeEnum == null) {
        throw new WsInvalidQueryException("If you are sending a value then you need to send attributeDefValueType!");
      }
      
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
          subjectAttributeNames, includeGroupDetailBoolean, paramName0, 
          paramValue0, paramName1, paramValue1, enabled, attributeDefValueTypeEnum, 
          theValue, includeAssignmentsFromAssignmentsBoolean, attributeDefTypeEnum,
          wsAssignAssignOwnerAttributeAssignId, 
          wsAssignAssignOwnerIdOfAttributeDef, wsAssignAssignOwnerNameOfAttributeDef,
          wsAssignAssignOwnerIdOfAttributeDefName, wsAssignAssignOwnerNameOfAttributeDefName, wsAssignAssignOwnerAction);

    } catch (Exception e) {
      wsGetAttributeAssignmentsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetAttributeAssignmentsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsGetAttributeAssignmentsResults; 
  }
  

  /**
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeDefLookup is the attribute definition to be modified
   * @param actions to assign
   * @param assign T to assign, or F to remove assignment
   * @param replaceAllExisting T if assigning, if this list should replace all existing actions
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @return the results
   */
  public WsAttributeDefAssignActionResults assignAttributeDefActions(
      String clientVersion, WsAttributeDefLookup wsAttributeDefLookup,
      String[] actions, String assign, String replaceAllExisting,
      WsSubjectLookup actAsSubjectLookup, final WsParam[] params) {

    WsAttributeDefAssignActionResults wsAttributeDefAssignActionResults = new WsAttributeDefAssignActionResults();

    try {
      boolean assignBoolean = GrouperServiceUtils.booleanValue(assign, "assign");

      Boolean replaceAllExistingBoolean = GrouperServiceUtils.booleanObjectValue(
          replaceAllExisting, "replaceAllExisting");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(clientVersion,
          true);

      wsAttributeDefAssignActionResults = GrouperServiceLogic.assignAttributeDefActions(
          grouperWsVersion,
          wsAttributeDefLookup, actions, assignBoolean, replaceAllExistingBoolean,
          actAsSubjectLookup, params);

    } catch (Exception e) {
      wsAttributeDefAssignActionResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsAttributeDefAssignActionResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefAssignActionResults;

  }
  
  /**
   * get attributeAssignActions from based on inputs
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeDefLookups find assignments in these attribute defs
   * @param actions to query, or none to query all actions
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @return the results
   */
  public WsGetAttributeAssignActionsResults getAttributeAssignActions(
      String clientVersion, WsAttributeDefLookup[] wsAttributeDefLookups,
      String[] actions,
      WsSubjectLookup actAsSubjectLookup, final WsParam[] params) {

    WsGetAttributeAssignActionsResults wsGetAttributeAssignActionsResults = new WsGetAttributeAssignActionsResults();

    try {

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(clientVersion,
          true);

      wsGetAttributeAssignActionsResults = GrouperServiceLogic.getAttributeAssignActions(
          grouperWsVersion,
          wsAttributeDefLookups, actions, actAsSubjectLookup, params);

    } catch (Exception e) {
      wsGetAttributeAssignActionsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsGetAttributeAssignActionsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsGetAttributeAssignActionsResults;
  }
  
  
  /**
   * get attributeAssignActions based on inputs
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsNameOfAttributeDef find assignActions in this attribute def
   * @param wsIdOfAtttributeDef find assignments in this attribute def (optional)
   * @param wsIdIndexOfAtrrbuteDef find assignments in this attribute def (optional)
   * @param action to query, or none to query all actions
   * @param actAsSubjectId 
   * @param actAsSubjectSourceId 
   * @param actAsSubjectIdentifier 
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
  public WsGetAttributeAssignActionsResults getAttributeAssignActionsLite(
		  String clientVersion, String wsNameOfAttributeDef, String wsIdOfAtttributeDef, String wsIdIndexOfAtrrbuteDef,
	      String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, 
	      String paramName0, String paramValue0, String paramName1, String paramValue1) {
	  
    WsGetAttributeAssignActionsResults wsGetAttributeAssignActionsResults = new WsGetAttributeAssignActionsResults();

    try {
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(clientVersion, true);

      wsGetAttributeAssignActionsResults = GrouperServiceLogic.getAttributeAssignActionsLite(grouperWsVersion,
    		  wsNameOfAttributeDef, wsIdOfAtttributeDef, wsIdIndexOfAtrrbuteDef, action, actAsSubjectId, 
    		  actAsSubjectSourceId, actAsSubjectIdentifier, paramName0, paramValue0, paramName1, paramValue1);

    } catch (Exception e) {
    	wsGetAttributeAssignActionsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsGetAttributeAssignActionsResults.getResultMetadata(), this.soap);

    return wsGetAttributeAssignActionsResults;
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
  public WsAssignAttributesBatchResults assignAttributesBatch(
      final String clientVersion, final WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries,
      final WsSubjectLookup actAsSubjectLookup, final String includeSubjectDetail, String txType,
      final String[] subjectAttributeNames, final String includeGroupDetail, final WsParam[] params) {  
  
    WsAssignAttributesBatchResults wsAssignAttributesBatchResults = new WsAssignAttributesBatchResults();
  
    try {
  
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");
  
      boolean includeSubjectDetailBoolean = GrouperServiceUtils.booleanValue(
          includeSubjectDetail, false, "includeSubjectDetail");
  
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
        .convertTransactionType(txType);

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignAttributesBatchResults = GrouperServiceLogic.assignAttributesBatch(grouperWsVersion, 
          wsAssignAttributeBatchEntries, actAsSubjectLookup, includeSubjectDetailBoolean, 
          grouperTransactionType, subjectAttributeNames, includeGroupDetailBoolean, params
          );
  
    } catch (Exception e) {
      wsAssignAttributesBatchResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignAttributesBatchResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
    return wsAssignAttributesBatchResults; 
  
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
  public WsAssignAttributeDefNameInheritanceResults assignAttributeDefNameInheritance(final String clientVersion,
      WsAttributeDefNameLookup wsAttributeDefNameLookup, WsAttributeDefNameLookup[] relatedWsAttributeDefNameLookups,
      String assign,
      String replaceAllExisting, final WsSubjectLookup actAsSubjectLookup, final String txType, 
      final WsParam[] params) {
    
    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = new WsAssignAttributeDefNameInheritanceResults();
    
    try {
  
      boolean assignBoolean = GrouperServiceUtils.booleanValue(
          assign, "assign");

      Boolean replaceAllExistingBoolean = GrouperServiceUtils.booleanObjectValue(
          replaceAllExisting, "replaceAllExisting");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
        .convertTransactionType(txType);

      wsAssignAttributeDefNameInheritanceResults = GrouperServiceLogic.assignAttributeDefNameInheritance(
          grouperWsVersion, wsAttributeDefNameLookup, relatedWsAttributeDefNameLookups, assignBoolean, replaceAllExistingBoolean, 
          actAsSubjectLookup, grouperTransactionType, params);
  
    } catch (Exception e) {
      wsAssignAttributeDefNameInheritanceResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignAttributeDefNameInheritanceResults.getResultMetadata(), this.soap);
  
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
  public WsAssignAttributeDefNameInheritanceResults assignAttributeDefNameInheritanceLite(final String clientVersion,
      String attributeDefNameUuid, String attributeDefNameName, String relatedAttributeDefNameUuid, String relatedAttributeDefNameName,
      String assign,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = new WsAssignAttributeDefNameInheritanceResults();
    
    try {
  
      boolean assignBoolean = GrouperServiceUtils.booleanValue(
          assign, "assign");

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAssignAttributeDefNameInheritanceResults = GrouperServiceLogic.assignAttributeDefNameInheritanceLite(grouperWsVersion,
          attributeDefNameUuid, attributeDefNameName, relatedAttributeDefNameUuid, relatedAttributeDefNameName,
          assignBoolean, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, paramName0,
          paramValue0, paramName1, paramValue1);
  
    } catch (Exception e) {
      wsAssignAttributeDefNameInheritanceResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAssignAttributeDefNameInheritanceResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
    return wsAssignAttributeDefNameInheritanceResults; 

  }
  
  /**
   * save an AttributeDef or many (insert or update).  Note, you cannot rename an existing AttributeDef.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link AttributeDefSave#save()}
   * @param wsAttributeDefsToSave AttributeDefs to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  public WsAttributeDefSaveResults attributeDefSave(final String clientVersion,
      final WsAttributeDefToSave[] wsAttributeDefsToSave,
      final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {

    WsAttributeDefSaveResults wsAttributeDefSaveResults = new WsAttributeDefSaveResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      wsAttributeDefSaveResults = GrouperServiceLogic.attributeDefSave(grouperWsVersion,
          wsAttributeDefsToSave, actAsSubjectLookup, grouperTransactionType, params);

    } catch (Exception e) {
      wsAttributeDefSaveResults
          .assignResultCodeException(null, null, e, grouperWsVersion);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAttributeDefSaveResults.getResultMetadata(),
        this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefSaveResults;

  }

  /**
   * save an AttributeDef (insert or update).  Note you cannot currently move an existing AttributeDef.
   * 
   * @see {@link AttributeDefSave#save()}
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeDefLookupUuid to lookup the attributeDef (mutually exclusive with attributeDefName)
   * @param attributeDefLookupName to lookup the attributeDef (mutually exclusive with attributeDefUuid)
   * @param uuidOfAttributeDef the uuid of the attributeDef to edit
   * @param nameOfAttributeDef the name of the attributeDef to edit
   * @param assignToAttributeDef 
   * @param assignToAttributeDefAssignment
   * @param assignToEffectiveMembership
   * @param assignToEffectiveMembershipAssignment
   * @param assignToGroup
   * @param assignToGroupAssignment
   * @param assignToImmediateMembership
   * @param assignToImmediateMembershipAssignment
   * @param assignToMember
   * @param assignToMemberAssignment
   * @param assignToStem
   * @param assignToStemAssignment
   * @param attributeDefType type of attribute def, from enum AttributeDefType, e.g. attr, domain, type, limit, perm
   * @param multiAssignable  T of F for if can be assigned multiple times to one object
   * @param multiValued T or F, if has values, if can assign multiple values to one assignment
   * @param valueType what type of value on assignments: AttributeDefValueType: e.g. integer, timestamp, string, floating, marker, memberId
   * @param description of the attributeDef, empty will be ignored
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
  public WsAttributeDefSaveLiteResult attributeDefSaveLite(final String clientVersion,
      String attributeDefLookupUuid, String attributeDefLookupName,
      String uuidOfAttributeDef, String nameOfAttributeDef,
      String assignToAttributeDef, String assignToAttributeDefAssignment,
      String assignToEffectiveMembership, String assignToEffectiveMembershipAssignment,
      String assignToGroup, String assignToGroupAssignment, 
      String assignToImmediateMembership, String assignToImmediateMembershipAssignment,
      String assignToMember, String assignToMemberAssignment,
      String assignToStem, String assignToStemAssignment,
      String attributeDefType, String multiAssignable,
      String multiValued, String valueType,
      String description, String saveMode, String createParentStemsIfNotExist,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    WsAttributeDefSaveLiteResult wsAttributeDefSaveLiteResult = new WsAttributeDefSaveLiteResult();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      Boolean createParentStemsIfNotExistBoolean = GrouperServiceUtils
          .booleanObjectValue(
              createParentStemsIfNotExist, "createParentStemsIfNotExist");

      SaveMode saveModeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(SaveMode.class,
          saveMode, false);
      
      wsAttributeDefSaveLiteResult = GrouperServiceLogic.attributeDefSaveLite(
          grouperWsVersion,
          attributeDefLookupUuid, attributeDefLookupName, uuidOfAttributeDef,
          nameOfAttributeDef, 
          GrouperUtil.booleanObjectValue(assignToAttributeDef), 
          GrouperUtil.booleanObjectValue(assignToAttributeDefAssignment), 
          GrouperUtil.booleanObjectValue(assignToEffectiveMembership), 
          GrouperUtil.booleanObjectValue(assignToEffectiveMembershipAssignment), 
          GrouperUtil.booleanObjectValue(assignToGroup), 
          GrouperUtil.booleanObjectValue(assignToGroupAssignment), 
          GrouperUtil.booleanObjectValue(assignToImmediateMembership),
          GrouperUtil.booleanObjectValue(assignToImmediateMembershipAssignment), 
          GrouperUtil.booleanObjectValue(assignToMember), 
          GrouperUtil.booleanObjectValue(assignToMemberAssignment),
          GrouperUtil.booleanObjectValue(assignToStem), 
          GrouperUtil.booleanObjectValue(assignToStemAssignment),
          attributeDefType, multiAssignable, multiValued,
          valueType, description, saveModeEnum, createParentStemsIfNotExistBoolean,
          actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0, paramValue0,
          paramName1, paramValue1);

    } catch (Exception e) {
      wsAttributeDefSaveLiteResult.assignResultCodeException(null, null, e,
          grouperWsVersion);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsAttributeDefSaveLiteResult.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefSaveLiteResult;

  }

  /**
   * delete an attribute def or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeDefLookups
   *            attributeDefs to delete
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsAttributeDefDeleteResults attributeDefDelete(final String clientVersion,
      final WsAttributeDefLookup[] wsAttributeDefLookups,
      final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {

    WsAttributeDefDeleteResults wsAttributeDefDeleteResults = new WsAttributeDefDeleteResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsAttributeDefDeleteResults = GrouperServiceLogic.attributeDefDelete(
          grouperWsVersion, wsAttributeDefLookups,
          grouperTransactionType, actAsSubjectLookup,
          params);
    } catch (Exception e) {
      wsAttributeDefDeleteResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsAttributeDefDeleteResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefDeleteResults;
  }

  /**
   * delete an attribute def
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsNameOfAttributeDef name of attribute def to be deleted
   * @param wsIdOfAttributeDef id of attribute def to be deleted.
   * @param wsIdIndexOfAttributeDef idIndex of attribute def to be deleted.
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
  public WsAttributeDefDeleteLiteResult attributeDefDeleteLite(
      final String clientVersion,
      String wsNameOfAttributeDef, String wsIdOfAttributeDef,
      String wsIdIndexOfAttributeDef, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
      String paramValue0,
      String paramName1, String paramValue1) {

    WsAttributeDefDeleteLiteResult wsAttributeDefDeleteLiteResult = new WsAttributeDefDeleteLiteResult();

    try {

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsAttributeDefDeleteLiteResult = GrouperServiceLogic.attributeDefDeleteLite(
          grouperWsVersion, wsNameOfAttributeDef, wsIdOfAttributeDef,
          wsIdIndexOfAttributeDef, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0, paramValue0, paramName1, paramValue1);
    } catch (Exception e) {
      wsAttributeDefDeleteLiteResult.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsAttributeDefDeleteLiteResult.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefDeleteLiteResult;

  }

  /**
   * find an attribute def or attribute defs.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param scope search string with % as wildcards will search name, display name, description
   * @param splitScope T or F, if T will split the scope by whitespace, and find attribute defs with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param wsAttributeDefLookups find attributeDefs associated with these attribute defs lookups
   * @param privilegeName privilegeName or null. null will default to ATTR_VIEW
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param parentStemId search in this stem
   * @param findByUuidOrName
   * @param actAsSubjectLookup if searching as someone else, pass in that subject here, note the caller must
   * be allowed to act as that other subject
   * @param params optional: reserved for future use
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, F for descending.  
   * @return the attribute defs, or no attribute def if none found
   */
  public WsFindAttributeDefsResults findAttributeDefs(final String clientVersion,
      String scope, String splitScope, WsAttributeDefLookup[] wsAttributeDefLookups,
      String privilegeName,
      String stemScope, String parentStemId, String findByUuidOrName,
      String pageSize, String pageNumber,
      String sortString, String ascending, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params) {

    WsFindAttributeDefsResults wsFindAttributeDefsResults = new WsFindAttributeDefsResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      Boolean splitScopeBoolean = GrouperServiceUtils.booleanObjectValue(
          splitScope, "splitScope");

      Boolean ascendingBoolean = GrouperServiceUtils.booleanObjectValue(
          ascending, "ascending");

      boolean findByUuidOrNameBoolean = GrouperServiceUtils.booleanValue(
          findByUuidOrName, false, "findByUuidOrName");

      Integer pageSizeInteger = GrouperServiceUtils.integerValue(pageSize, "pageSize");
      Integer pageNumberInteger = GrouperServiceUtils.integerValue(pageNumber,
          "pageNumber");

      StemScope stemScopeEnum = StemScope.valueOfIgnoreCase(stemScope);

      wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
          grouperWsVersion,
          scope, splitScopeBoolean, wsAttributeDefLookups,
          privilegeName,
          stemScopeEnum, parentStemId, findByUuidOrNameBoolean,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          actAsSubjectLookup, params);

    } catch (Exception e) {
      wsFindAttributeDefsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsFindAttributeDefsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsFindAttributeDefsResults;

  }

  /**
   * find an attribute def name attribute defs.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param scope search string with % as wildcards will search name, display name, description
   * @param splitScope T or F, if T will split the scope by whitespace, and find attribute defs with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param uuidOfAttributeDef find attribute defs associated with this attribute def uuid, mutually exclusive with nameOfAttributeDef
   * @param nameOfAttributeDef find attribute defs associated with this attribute def name, mutually exclusive with idOfAttributeDef
   * @param idIndexOfAttributeDef find attribute defs associated with this attribute def id index
   * @param privilegeName privilegeName or null. null will default to ATTR_VIEW
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param parentStemId search in this stem
   * @param findByUuidOrName
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, F for descending.  
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
   * @return the attribute defs, or no attribute defs if none found
   */
  public WsFindAttributeDefsResults findAttributeDefsLite(final String clientVersion,
      String scope, String splitScope, String uuidOfAttributeDef,
      String nameOfAttributeDef,
      String idIndexOfAttributeDef, String privilegeName,
      String stemScope, String parentStemId,
      String findByUuidOrName,
      String pageSize, String pageNumber,
      String sortString, String ascending,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1
      ) {

    WsFindAttributeDefsResults wsFindAttributeDefsResults = new WsFindAttributeDefsResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      Boolean splitScopeBoolean = GrouperServiceUtils.booleanObjectValue(
          splitScope, "splitScope");

      Boolean ascendingBoolean = GrouperServiceUtils.booleanObjectValue(
          splitScope, "ascending");

      boolean findByUuidOrNameBoolean = GrouperServiceUtils.booleanValue(
          findByUuidOrName, false, "findByUuidOrName");

      Integer pageSizeInteger = GrouperServiceUtils.integerValue(pageSize, "pageSize");
      Integer pageNumberInteger = GrouperServiceUtils.integerValue(pageNumber,
          "pageNumber");

      StemScope stemScopeEnum = StemScope.valueOfIgnoreCase(stemScope);

      wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefsLite(
          grouperWsVersion,
          scope, splitScopeBoolean, uuidOfAttributeDef, nameOfAttributeDef,
          idIndexOfAttributeDef, privilegeName,
          stemScopeEnum, parentStemId, findByUuidOrNameBoolean,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0,
          paramValue0, paramName1, paramValue1);

    } catch (Exception e) {
      wsFindAttributeDefsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(
        wsFindAttributeDefsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsFindAttributeDefsResults;

  }

  /**
   * delete an AttributeDefName or many.  Note, you cannot rename an existing AttributeDefName.
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
  public WsAttributeDefNameDeleteResults attributeDefNameDelete(final String clientVersion,
      final WsAttributeDefNameLookup[] wsAttributeDefNameLookups, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {
  
    WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults = new WsAttributeDefNameDeleteResults();
    
    try {
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
        .convertTransactionType(txType);

      wsAttributeDefNameDeleteResults = GrouperServiceLogic.attributeDefNameDelete(grouperWsVersion,
          wsAttributeDefNameLookups, actAsSubjectLookup,
          grouperTransactionType, params);
  
    } catch (Exception e) {
      wsAttributeDefNameDeleteResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAttributeDefNameDeleteResults.getResultMetadata(), this.soap);
  
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
  public WsAttributeDefNameDeleteLiteResult attributeDefNameDeleteLite(final String clientVersion,
      String attributeDefNameUuid, String attributeDefNameName,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
    WsAttributeDefNameDeleteLiteResult wsAttributeDefNameDeleteLiteResult = new WsAttributeDefNameDeleteLiteResult();
    
    try {
  
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      wsAttributeDefNameDeleteLiteResult = GrouperServiceLogic.attributeDefNameDeleteLite(grouperWsVersion,
          attributeDefNameUuid, attributeDefNameName, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0, paramValue0, paramName1, paramValue1);
  
    } catch (Exception e) {
      wsAttributeDefNameDeleteLiteResult.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAttributeDefNameDeleteLiteResult.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefNameDeleteLiteResult; 

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
  public WsAttributeDefNameSaveResults attributeDefNameSave(final String clientVersion,
      final WsAttributeDefNameToSave[] wsAttributeDefNameToSaves, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {
  
    WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = new WsAttributeDefNameSaveResults();
    
    GrouperVersion grouperWsVersion = null;
    
    try {
  
      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
        .convertTransactionType(txType);

      wsAttributeDefNameSaveResults = GrouperServiceLogic.attributeDefNameSave(grouperWsVersion,
          wsAttributeDefNameToSaves, actAsSubjectLookup, grouperTransactionType, params);
  
    } catch (Exception e) {
      wsAttributeDefNameSaveResults.assignResultCodeException(null, null, e, grouperWsVersion);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAttributeDefNameSaveResults.getResultMetadata(), this.soap);
  
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
  public WsAttributeDefNameSaveLiteResult attributeDefNameSaveLite(final String clientVersion,
      String attributeDefNameLookupUuid, String attributeDefNameLookupName, String attributeDefLookupUuid, 
      String attributeDefLookupName, String attributeDefNameUuid,String attributeDefNameName, 
      String displayExtension,String description,  String saveMode, String createParentStemsIfNotExist,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
    
    WsAttributeDefNameSaveLiteResult wsAttributeDefNameSaveLiteResult = new WsAttributeDefNameSaveLiteResult();
    
    GrouperVersion grouperWsVersion = null;
    
    try {
  
      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      Boolean createParentStemsIfNotExistBoolean = GrouperServiceUtils.booleanObjectValue(
          createParentStemsIfNotExist, "createParentStemsIfNotExist");

      SaveMode saveModeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(SaveMode.class, saveMode, false);

      wsAttributeDefNameSaveLiteResult = GrouperServiceLogic.attributeDefNameSaveLite(grouperWsVersion,
          attributeDefNameLookupUuid, attributeDefNameLookupName, attributeDefLookupUuid, 
          attributeDefLookupName, attributeDefNameUuid, attributeDefNameName, 
          displayExtension, description, saveModeEnum, createParentStemsIfNotExistBoolean,
          actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0, paramValue0,
          paramName1, paramValue1);
  
    } catch (Exception e) {
      wsAttributeDefNameSaveLiteResult.assignResultCodeException(null, null, e, grouperWsVersion);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsAttributeDefNameSaveLiteResult.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
    return wsAttributeDefNameSaveLiteResult; 

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
   * @param pageSize page size if paging on a sort filter or parent
   * @param pageNumber page number 1 indexed if paging on a sort filter or parent
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
  public WsFindAttributeDefNamesResults findAttributeDefNames(final String clientVersion,
      String scope, String splitScope, WsAttributeDefLookup wsAttributeDefLookup,
      String attributeAssignType, String attributeDefType,
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups, 
      String pageSize, String pageNumber,
      String sortString, String ascending, String wsInheritanceSetRelation, WsSubjectLookup actAsSubjectLookup, WsParam[] params,
      WsSubjectLookup wsSubjectLookup, String serviceRole) {

    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = new WsFindAttributeDefNamesResults();

    GrouperVersion grouperWsVersion = null;

    try {
  
      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      Boolean splitScopeBoolean = GrouperServiceUtils.booleanObjectValue(
          splitScope, "splitScope");

      Boolean ascendingBoolean = GrouperServiceUtils.booleanObjectValue(
          ascending, "ascending");
      Integer pageSizeInteger = GrouperServiceUtils.integerValue(pageSize, "pageSize");
      Integer pageNumberInteger = GrouperServiceUtils.integerValue(pageNumber, "pageNumber");

      AttributeDefType attributeDefTypeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(AttributeDefType.class,attributeDefType, false);
      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(AttributeAssignType.class, attributeAssignType, false);
      WsInheritanceSetRelation wsInheritanceSetRelationEnum = WsInheritanceSetRelation.valueOfIgnoreCase(wsInheritanceSetRelation);

      ServiceRole serviceRoleEnum = GrouperServiceUtils.enumValueOfIgnoreCase(ServiceRole.class, serviceRole, false);
      
      wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(grouperWsVersion,
          scope, splitScopeBoolean, wsAttributeDefLookup,
          attributeAssignTypeEnum, attributeDefTypeEnum,
          wsAttributeDefNameLookups, pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          wsInheritanceSetRelationEnum, actAsSubjectLookup, params, wsSubjectLookup, serviceRoleEnum);
  
    } catch (Exception e) {
      wsFindAttributeDefNamesResults.assignResultCodeException(null, null, e);
    }
  
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindAttributeDefNamesResults.getResultMetadata(), this.soap);
  
    //this should be the first and only return, or else it is exiting too early
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
   * @param pageSize page size if paging on a sort filter or parent
   * @param pageNumber page number 1 indexed if paging on a sort filter or parent
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
  public WsFindAttributeDefNamesResults findAttributeDefNamesLite(final String clientVersion,
      String scope, String splitScope, String uuidOfAttributeDef, String nameOfAttributeDef,
      String attributeAssignType, String attributeDefType, String attributeDefNameUuid, String attributeDefNameName,
      String pageSize, String pageNumber,
      String sortString, String ascending, String wsInheritanceSetRelation,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1,
      String subjectId, String subjectSourceId,
      String subjectIdentifier, String serviceRole 
      ) {
        
    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = new WsFindAttributeDefNamesResults();
    
    GrouperVersion grouperWsVersion = null;
    
    try {
  
      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
  
      Boolean splitScopeBoolean = GrouperServiceUtils.booleanObjectValue(
          splitScope, "splitScope");

      AttributeDefType attributeDefTypeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(AttributeDefType.class,attributeDefType, false);
      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(AttributeAssignType.class, attributeAssignType, false);
      WsInheritanceSetRelation wsInheritanceSetRelationEnum = WsInheritanceSetRelation.valueOfIgnoreCase(wsInheritanceSetRelation);

      Boolean ascendingBoolean = GrouperServiceUtils.booleanObjectValue(
          splitScope, "ascending");
      Integer pageSizeInteger = GrouperServiceUtils.integerValue(pageSize, "pageSize");
      Integer pageNumberInteger = GrouperServiceUtils.integerValue(pageNumber, "pageNumber");

      ServiceRole serviceRoleEnum = GrouperServiceUtils.enumValueOfIgnoreCase(ServiceRole.class, serviceRole, false);

      wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNamesLite(grouperWsVersion,
          scope, splitScopeBoolean, uuidOfAttributeDef, nameOfAttributeDef,
          attributeAssignTypeEnum, attributeDefTypeEnum, attributeDefNameUuid, attributeDefNameName,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          wsInheritanceSetRelationEnum, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0,
          paramValue0, paramName1, paramValue1, subjectId, subjectSourceId,
          subjectIdentifier, serviceRoleEnum);

    } catch (Exception e) {
      wsFindAttributeDefNamesResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindAttributeDefNamesResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsFindAttributeDefNamesResults; 

  }
  
  /**
   * @param clientVersion
   * @param queueType - queue or topic (required)
   * @param queueOrTopicName
   * @param messageSystemName
   * @param routingKey
   * @param messages
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message send call
   */
  public WsMessageResults sendMessage(final String clientVersion,
      String queueType, String queueOrTopicName, String messageSystemName,
      String routingKey, WsMessage[] messages,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {

    WsMessageResults wsSendMessageResults = new WsMessageResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      GrouperMessageQueueType messageQueueType = GrouperMessageQueueType
          .valueOfIgnoreCase(queueType, true);

      wsSendMessageResults = GrouperServiceLogic.sendMessage(grouperWsVersion,
          messageQueueType,
          queueOrTopicName, messageSystemName, routingKey, messages, actAsSubjectLookup, params);

    } catch (Exception e) {
      wsSendMessageResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsSendMessageResults.getResultMetadata(), this.soap);

    return wsSendMessageResults;
  }

  /**
   * @param clientVersion
   * @param queueOrTopicName
   * @param messageSystemName
   * @param routingKey
   * @param blockMillis - the millis to block waiting for messages, max of 20000 (optional)
   * @param maxMessagesToReceiveAtOnce - max number of messages to receive at once, though can't be more than the server maximum (optional)
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message receive call
   */
  public WsMessageResults receiveMessage(final String clientVersion,
      String queueOrTopicName, String messageSystemName, String routingKey,
      final Integer blockMillis, final Integer maxMessagesToReceiveAtOnce,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {

    WsMessageResults wsReceiveMessageResults = new WsMessageResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsReceiveMessageResults = GrouperServiceLogic.receiveMessage(grouperWsVersion,
          queueOrTopicName, messageSystemName, routingKey,
          blockMillis, maxMessagesToReceiveAtOnce, actAsSubjectLookup, params);

    } catch (Exception e) {
      wsReceiveMessageResults.assignResultCodeException(null, null, e);
    }
    
    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsReceiveMessageResults.getResultMetadata(), this.soap);

    return wsReceiveMessageResults;
  }

  /**
   * @param clientVersion
   * @param queueOrTopicName
   * @param messageSystemName
   * @param acknowledgeType specify what to do with the messages (required)
   * @param messageIds - messageIds to be marked as processed (required)
   * @param anotherQueueOrTopicName - required if acknowledgeType is SEND_TO_ANOTHER_TOPIC_OR_QUEUE
   * @param anotherQueueType - required if acknowledgeType is SEND_TO_ANOTHER_TOPIC_OR_QUEUE
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message receive call
   */
  public WsMessageAcknowledgeResults acknowledge(final String clientVersion,
      String queueOrTopicName, String messageSystemName, String acknowledgeType,
      String[] messageIds, String anotherQueueOrTopicName, String anotherQueueType,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {

    WsMessageAcknowledgeResults wsMessageResults = new WsMessageAcknowledgeResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      GrouperMessageAcknowledgeType messageAcknowledgeType = GrouperMessageAcknowledgeType
          .valueOfIgnoreCase(acknowledgeType, true);
      
      GrouperMessageQueueType messageQueueType = null;
      if (anotherQueueType != null) {
        messageQueueType = GrouperMessageQueueType
            .valueOfIgnoreCase(anotherQueueType, true);
      }
     

      wsMessageResults = GrouperServiceLogic.acknowledge(grouperWsVersion,
          queueOrTopicName, messageSystemName,
          messageAcknowledgeType, messageIds, anotherQueueOrTopicName, messageQueueType,
          actAsSubjectLookup, params);

    } catch (Exception e) {
      wsMessageResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsMessageResults.getResultMetadata(), this.soap);

    return wsMessageResults;
  }

  /**
   * delete an external subject or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsExternalSubjectLookups
   *            groups to delete
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsExternalSubjectDeleteResults externalSubjectDelete(final String clientVersion,
      final WsExternalSubjectLookup[] wsExternalSubjectLookups, final WsSubjectLookup actAsSubjectLookup,
      String txType, final WsParam[] params) {
  
    WsExternalSubjectDeleteResults wsExternalSubjectDeleteResults = new WsExternalSubjectDeleteResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsExternalSubjectDeleteResults = GrouperServiceLogic.externalSubjectDelete(grouperWsVersion, 
          wsExternalSubjectLookups, actAsSubjectLookup, 
          grouperTransactionType, params);
    } catch (Exception e) {
      wsExternalSubjectDeleteResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsExternalSubjectDeleteResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsExternalSubjectDeleteResults;
  
  }

  /**
   * save an external subject (insert or update).
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsExternalSubjectToSaves
   *            external subjects to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @since 2.3.0.patch
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public WsExternalSubjectSaveResults externalSubjectSave(final String clientVersion,
      final WsExternalSubjectToSave[] wsExternalSubjectToSaves, final WsSubjectLookup actAsSubjectLookup,
      String txType, final WsParam[] params) {
  
    WsExternalSubjectSaveResults wsExternalSubjectSaveResults = new WsExternalSubjectSaveResults();

    try {

      //convert tx type to object
      final GrouperTransactionType grouperTransactionType = GrouperServiceUtils
          .convertTransactionType(txType);

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsExternalSubjectSaveResults = GrouperServiceLogic.externalSubjectSave(grouperWsVersion, wsExternalSubjectToSaves,
          actAsSubjectLookup, 
          grouperTransactionType, params);
    } catch (Exception e) {
      wsExternalSubjectSaveResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsExternalSubjectSaveResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsExternalSubjectSaveResults;
  }

  /**
   * find a external subjects
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @param wsExternalSubjectLookups if you want to just pass in a list of uuids and/or names
   * @return the external subjects, or no external subjects if none found
   */
  @SuppressWarnings("unchecked")
  public WsFindExternalSubjectsResults findExternalSubjects(final String clientVersion,
      WsExternalSubjectLookup[] wsExternalSubjectLookups,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {
  
    WsFindExternalSubjectsResults wsFindExternalSubjectsResults = new WsFindExternalSubjectsResults();

    try {

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      wsFindExternalSubjectsResults = GrouperServiceLogic.findExternalSubjects(grouperWsVersion, wsExternalSubjectLookups, 
          actAsSubjectLookup, 
          params);
    } catch (Exception e) {
      wsFindExternalSubjectsResults.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(wsFindExternalSubjectsResults.getResultMetadata(), this.soap);

    //this should be the first and only return, or else it is exiting too early
    return wsFindExternalSubjectsResults;
  }

}
