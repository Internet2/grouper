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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import edu.internet2.middleware.grouper.util.GrouperUtil;
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

  /** this package */
  private static final String THIS_VERSION_PACKAGE = GrouperService.class.getPackage().getName();

  /** 
   * default
   */
  public GrouperService() {
    //nothin
  }
  
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
    
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findGroupsLite",
        new Object[]{clientVersion,
      queryFilterType, groupName, stemName, stemNameScope,
      groupUuid, groupAttributeName, groupAttributeValue,
      groupTypeName, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, includeGroupDetail, paramName0,
      paramValue0, paramName1, paramValue1, pageSize, pageNumber, sortString, ascending, typeOfGroups});
    
    return (WsFindGroupsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
  public WsFindStemsResults findStems(final String clientVersion,
      WsStemQueryFilter wsStemQueryFilter, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params, WsStemLookup[] wsStemLookups) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findStems",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsStemQueryFilter, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsStemLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName())});
    
    return (WsFindStemsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

  public WsFindGroupsResults findGroups(final String clientVersion,
      WsQueryFilter wsQueryFilter, 
      WsSubjectLookup actAsSubjectLookup, 
      String includeGroupDetail, WsParam[] params, WsGroupLookup[] wsGroupLookups) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findGroups",
        new Object[]{
      clientVersion,
      GrouperUtil.changeToVersion(wsQueryFilter, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsGroupLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsFindGroupsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getMembersLite",
        new Object[]{clientVersion,
      groupName, groupUuid, memberFilter, actAsSubjectId,
      actAsSubjectSourceId, actAsSubjectIdentifier, fieldName,
      includeGroupDetail, 
      includeSubjectDetail, subjectAttributeNames,
      paramName0, paramValue0,
      paramName1, paramValue1, sourceIds, pointInTimeFrom, pointInTimeTo});
    
    return (WsGetMembersLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
  public WsGetMembersResults getMembers(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, String memberFilter,
      WsSubjectLookup actAsSubjectLookup, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params, String[] sourceIds,
      String pointInTimeFrom, String pointInTimeTo) {
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getMembers",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsGroupLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      memberFilter,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      fieldName,
      includeGroupDetail, 
      includeSubjectDetail, 
      subjectAttributeNames,
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      sourceIds, pointInTimeFrom, pointInTimeTo});
    
    return (WsGetMembersResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  public WsGetGroupsResults getGroups(final String clientVersion,
      WsSubjectLookup[] subjectLookups, String memberFilter, 
      WsSubjectLookup actAsSubjectLookup, String includeGroupDetail,
      String includeSubjectDetail, String[] subjectAttributeNames, 
      WsParam[] params, String fieldName, String scope, 
      WsStemLookup wsStemLookup, String stemScope, String enabled, 
      String pageSize, String pageNumber, String sortString, String ascending,
      String pointInTimeFrom, String pointInTimeTo) {
    
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getGroups",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(subjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      memberFilter, 
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeGroupDetail,
      includeSubjectDetail, subjectAttributeNames, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      fieldName, scope, 
      GrouperUtil.changeToVersion(wsStemLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      stemScope, enabled, 
      pageSize, pageNumber, sortString, ascending, pointInTimeFrom, pointInTimeTo});
    
    return (WsGetGroupsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "hasMember",
        new Object[]{
      clientVersion,
      GrouperUtil.changeToVersion(wsGroupLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(subjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      memberFilter,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      fieldName,
      includeGroupDetail, 
      includeSubjectDetail, subjectAttributeNames, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      pointInTimeFrom, pointInTimeTo
    });
    
    return (WsHasMemberResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "stemDeleteLite",
        new Object[]{clientVersion,
      stemName, stemUuid, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0,  paramValue0,
      paramName1, paramValue1});
    
    return (WsStemDeleteLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "groupDeleteLite",
        new Object[]{clientVersion,
      groupName, groupUuid, actAsSubjectId,
      actAsSubjectSourceId, actAsSubjectIdentifier,
      includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsGroupDeleteLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

  }

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
   * @param typeOfGroup type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   * @return the result of one member add
   */
  public WsGroupSaveLiteResult groupSaveLite(final String clientVersion,
      String groupLookupUuid, String groupLookupName, String groupUuid,String groupName, 
      String displayExtension,String description,  String saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String typeOfGroup) {
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "groupSaveLite",
        new Object[]{clientVersion,
      groupLookupUuid, groupLookupName, groupUuid,groupName, 
      displayExtension,description,  saveMode,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1, typeOfGroup});
    
    return (WsGroupSaveLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "stemSaveLite",
        new Object[]{clientVersion,
      stemLookupUuid, stemLookupName, stemUuid, stemName, 
      displayExtension, description, saveMode,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsStemSaveLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

  public WsGroupSaveResults groupSave(final String clientVersion,
      final WsGroupToSave[] wsGroupToSaves, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final String includeGroupDetail, final WsParam[] params) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "groupSave",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsGroupToSaves, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsGroupSaveResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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

  public WsStemSaveResults stemSave(final String clientVersion,
      final WsStemToSave[] wsStemToSaves, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "stemSave",
        new Object[]{ clientVersion,
      GrouperUtil.changeToVersion(wsStemToSaves, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
    });
    
    return (WsStemSaveResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

  public WsStemDeleteResults stemDelete(final String clientVersion,
      final WsStemLookup[] wsStemLookups, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final WsParam[] params) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "stemDelete",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsStemLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsStemDeleteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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
  public WsGroupDeleteResults groupDelete(final String clientVersion,
      final WsGroupLookup[] wsGroupLookups, final WsSubjectLookup actAsSubjectLookup,
      final String txType, final String includeGroupDetail, final WsParam[] params) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "groupDelete",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsGroupLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsGroupDeleteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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
  public WsAddMemberResults addMember(final String clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final String replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
      final String fieldName, final String txType, final String includeGroupDetail,
      final String includeSubjectDetail, final String[] subjectAttributeNames,
      final WsParam[] params, final String disabledTime, 
      final String enabledTime, String addExternalSubjectIfNotFound) {
  
      Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
          GrouperServiceUtils.currentServiceClass(), "addMember",
          new Object[]{clientVersion,
        GrouperUtil.changeToVersion(wsGroupLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
        GrouperUtil.changeToVersion(subjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
        replaceAllExisting, 
        GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
        fieldName, txType, includeGroupDetail,
        includeSubjectDetail, subjectAttributeNames,
        GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
        disabledTime, 
        enabledTime,
        addExternalSubjectIfNotFound});
      
      return (WsAddMemberResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

  public WsDeleteMemberResults deleteMember(final String clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final WsSubjectLookup actAsSubjectLookup, final String fieldName,
      final String txType, final String includeGroupDetail, 
      final String includeSubjectDetail, final String[] subjectAttributeNames,
      final WsParam[] params) {

    
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "deleteMember",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsGroupLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(subjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      fieldName,
      txType, 
      includeGroupDetail, 
      includeSubjectDetail, 
      subjectAttributeNames,
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsDeleteMemberResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getGroupsLite",
        new Object[]{clientVersion, subjectId,
      subjectSourceId, subjectIdentifier, memberFilter,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, includeGroupDetail, 
      includeSubjectDetail, 
      subjectAttributeNames, paramName0, paramValue0,
      paramName1, paramValue1, fieldName, scope, 
      stemName, stemUuid, stemScope, enabled, 
      pageSize, pageNumber, sortString, ascending, pointInTimeFrom, pointInTimeTo});
    
    return (WsGetGroupsLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
    
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "addMemberLite",
        new Object[]{clientVersion,
      groupName, groupUuid, subjectId, subjectSourceId,
      subjectIdentifier, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, fieldName, includeGroupDetail,
      includeSubjectDetail, subjectAttributeNames, paramName0,
      paramValue0, paramName1, paramValue1, disabledTime, 
      enabledTime, addExternalSubjectIfNotFound});
    
    return (WsAddMemberLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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
  
      Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
          GrouperServiceUtils.currentServiceClass(), "hasMemberLite",
          new Object[]{ clientVersion, groupName,
        groupUuid, subjectId, subjectSourceId, subjectIdentifier,
        memberFilter,
        actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier,
        fieldName, includeGroupDetail, 
        includeSubjectDetail, subjectAttributeNames, paramName0,
        paramValue0, paramName1, paramValue1, pointInTimeFrom, pointInTimeTo});
      
      return (WsHasMemberLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "memberChangeSubjectLite",
        new Object[]{clientVersion, 
      oldSubjectId, oldSubjectSourceId, oldSubjectIdentifier,
      newSubjectId, newSubjectSourceId, newSubjectIdentifier,
      actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier,
      deleteOldMember, 
      includeSubjectDetail, subjectAttributeNames, paramName0,
      paramValue0, paramName1, paramValue1});
    
    return (WsMemberChangeSubjectLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

  public WsMemberChangeSubjectResults memberChangeSubject(final String clientVersion,
      WsMemberChangeSubject[] wsMemberChangeSubjects, final WsSubjectLookup actAsSubjectLookup,
      final String txType, 
      final String includeSubjectDetail, final String[] subjectAttributeNames,
      final WsParam[] params) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "memberChangeSubject",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsMemberChangeSubjects, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      includeSubjectDetail, 
      subjectAttributeNames,
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
    });
    
    return (WsMemberChangeSubjectResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "deleteMemberLite",
        new Object[]{clientVersion,
      groupName, groupUuid, subjectId, subjectSourceId,
      subjectIdentifier, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, fieldName,
      includeGroupDetail, includeSubjectDetail,
      subjectAttributeNames, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsDeleteMemberLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findStemsLite",
        new Object[]{clientVersion,
      stemQueryFilterType, stemName, parentStemName,
      parentStemNameScope, stemUuid, stemAttributeName,
      stemAttributeValue, actAsSubjectId,
      actAsSubjectSourceId, actAsSubjectIdentifier, paramName0,
      paramValue0, paramName1, paramValue1});
    
    return (WsFindStemsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getGrouperPrivilegesLite",
        new Object[]{ clientVersion, 
      subjectId, subjectSourceId, subjectIdentifier,
      groupName, groupUuid, 
      stemName, stemUuid, 
      privilegeType, privilegeName,
      actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier,
      includeSubjectDetail, subjectAttributeNames, 
      includeGroupDetail, paramName0,
      paramValue0, paramName1, paramValue1});
    
    return (WsGetGrouperPrivilegesLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignGrouperPrivilegesLite",
        new Object[]{clientVersion, 
      subjectId, subjectSourceId, subjectIdentifier,
      groupName, groupUuid, 
      stemName, stemUuid, 
      privilegeType, privilegeName, allowed,
      actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier,
      includeSubjectDetail, subjectAttributeNames, 
      includeGroupDetail, paramName0,
      paramValue0, paramName1, paramValue1});
    
    return (WsAssignGrouperPrivilegesLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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
  public WsGetMembershipsResults getMemberships(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, WsSubjectLookup[] wsSubjectLookups, String wsMemberFilter,
      WsSubjectLookup actAsSubjectLookup, String fieldName, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, final WsParam[] params, 
      String[] sourceIds, String scope, 
      WsStemLookup wsStemLookup, String stemScope, String enabled, String[] membershipIds, 
      WsStemLookup[] wsOwnerStemLookups, WsAttributeDefLookup[] wsOwnerAttributeDefLookups, String fieldType,
      String serviceRole, WsAttributeDefNameLookup serviceLookup, String pageSize, String pageNumber,
      String sortString, String ascending, String pageSizeForMember, String pageNumberForMember,
      String sortStringForMember, String ascendingForMember) {  
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getMemberships",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsGroupLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      wsMemberFilter,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      fieldName, 
      includeSubjectDetail,
      subjectAttributeNames, 
      includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      sourceIds, 
      scope, 
      GrouperUtil.changeToVersion(wsStemLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      stemScope, 
      enabled, 
      membershipIds, 
      GrouperUtil.changeToVersion(wsOwnerStemLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      fieldType, serviceRole, serviceLookup,
      pageSize, pageNumber, sortString, ascending, pageSizeForMember, pageNumberForMember, sortStringForMember, ascendingForMember});

    return (WsGetMembershipsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
      String stemUuid, String stemScope, String enabled, String membershipIds,
      String ownerStemName, String ownerStemUuid, String nameOfOwnerAttributeDef, 
      String ownerAttributeDefUuid, String fieldType,
      String serviceRole, String serviceId, String serviceName, String pageSize, String pageNumber,
      String sortString, String ascending, String pageSizeForMember, String pageNumberForMember,
      String sortStringForMember, String ascendingForMember) {
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getMembershipsLite",
        new Object[]{clientVersion,
      groupName, groupUuid, subjectId, sourceId, subjectIdentifier, 
      wsMemberFilter,
      includeSubjectDetail, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, fieldName, subjectAttributeNames,
      includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1, sourceIds, scope, stemName, 
      stemUuid, stemScope, enabled, membershipIds, 
      ownerStemName, 
      ownerStemUuid, nameOfOwnerAttributeDef, 
      ownerAttributeDefUuid, fieldType, serviceRole, serviceId, serviceName,
      pageSize, pageNumber, sortString, ascending, pageSizeForMember, pageNumberForMember, sortStringForMember, ascendingForMember});
    
    return (WsGetMembershipsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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

  public WsGetSubjectsResults getSubjects(final String clientVersion,
      WsSubjectLookup[] wsSubjectLookups, String searchString, String includeSubjectDetail, 
      String[] subjectAttributeNames, WsSubjectLookup actAsSubjectLookup, String[] sourceIds, 
      WsGroupLookup wsGroupLookup, String wsMemberFilter,
       String fieldName, 
      String includeGroupDetail, final WsParam[] params) {  
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getSubjects",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      searchString, 
      includeSubjectDetail, 
      subjectAttributeNames, 
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      sourceIds, 
      GrouperUtil.changeToVersion(wsGroupLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      wsMemberFilter,
      fieldName, 
      includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsGetSubjectsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getSubjectsLite",
        new Object[]{clientVersion,
      subjectId, sourceId, subjectIdentifier, searchString,
      includeSubjectDetail, subjectAttributeNames,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, sourceIds,
      groupName, groupUuid, wsMemberFilter,
      fieldName, includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsGetSubjectsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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
   *            optional: T or F (default), if the existing privilege assignments for this object should be
   *            replaced
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignGrouperPrivileges",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsGroupLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsStemLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      privilegeType, 
      privilegeNames,
      allowed,
      replaceAllExisting,
      txType,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeSubjectDetail, 
      subjectAttributeNames, 
      includeGroupDetail,  
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())});
    
    return (WsAssignGrouperPrivilegesResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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
      String enabled, String attributeDefValueType, String theValue, String includeAssignmentsFromAssignments,
      String attributeDefType, WsAttributeAssignLookup[] wsAssignAssignOwnerAttributeAssignLookups,
      WsAttributeDefLookup[] wsAssignAssignOwnerAttributeDefLookups, 
      WsAttributeDefNameLookup[] wsAssignAssignOwnerAttributeDefNameLookups,
      String[] wsAssignAssignOwnerActions) {  

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getAttributeAssignments",
        new Object[]{clientVersion, attributeAssignType,
      GrouperUtil.changeToVersion(wsAttributeAssignLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerGroupLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerStemLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerMembershipLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerMembershipAnyLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actions, 
      includeAssignmentsOnAssignments, 
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      enabled, attributeDefValueType, theValue, includeAssignmentsFromAssignments, attributeDefType,
      GrouperUtil.changeToVersion(wsAssignAssignOwnerAttributeAssignLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsAssignAssignOwnerAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()), 
      GrouperUtil.changeToVersion(wsAssignAssignOwnerAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      wsAssignAssignOwnerActions});
    
    return (WsGetAttributeAssignmentsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getAttributeAssignmentsLite",
        new Object[]{clientVersion, attributeAssignType,
      attributeAssignId,
      wsAttributeDefName, wsAttributeDefId, wsAttributeDefNameName, wsAttributeDefNameId,
      wsOwnerGroupName, wsOwnerGroupId, wsOwnerStemName, wsOwnerStemId, 
      wsOwnerSubjectId, wsOwnerSubjectSourceId, wsOwnerSubjectIdentifier,
      wsOwnerMembershipId, wsOwnerMembershipAnyGroupName, wsOwnerMembershipAnyGroupId,
      wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, wsOwnerMembershipAnySubjectIdentifier, 
      wsOwnerAttributeDefName, wsOwnerAttributeDefId, 
      action, 
      includeAssignmentsOnAssignments, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1, 
      enabled, attributeDefValueType, theValue, includeAssignmentsFromAssignments, attributeDefType,
      wsAssignAssignOwnerAttributeAssignId, 
      wsAssignAssignOwnerIdOfAttributeDef, wsAssignAssignOwnerNameOfAttributeDef,
      wsAssignAssignOwnerIdOfAttributeDefName, wsAssignAssignOwnerNameOfAttributeDefName, wsAssignAssignOwnerAction});
    
    return (WsGetAttributeAssignmentsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignAttributes",
        new Object[]{clientVersion, attributeAssignType,
      GrouperUtil.changeToVersion(wsAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      attributeAssignOperation,
      GrouperUtil.changeToVersion(values, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      assignmentNotes, assignmentEnabledTime,
      assignmentDisabledTime, delegatable,
      attributeAssignValueOperation,
      GrouperUtil.changeToVersion(wsAttributeAssignLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerGroupLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerStemLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerMembershipLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerMembershipAnyLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsOwnerAttributeAssignLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actions, 
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(attributeDefsToReplace, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actionsToReplace, attributeDefTypesToReplace});
    
    return (WsAssignAttributesResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  public WsAssignAttributesBatchResults assignAttributesBatch(
      final String clientVersion, final WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries,
      final WsSubjectLookup actAsSubjectLookup, final String includeSubjectDetail, String txType,
      final String[] subjectAttributeNames, final String includeGroupDetail, final WsParam[] params) {  
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignAttributesBatch",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsAssignAttributeBatchEntries, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeSubjectDetail, txType, subjectAttributeNames, includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()) });
    
    return (WsAssignAttributesBatchResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignAttributesLite",
        new Object[]{clientVersion, attributeAssignType,
      wsAttributeDefNameName, wsAttributeDefNameId,
      attributeAssignOperation,
      valueId, valueSystem, valueFormatted,
      assignmentNotes, assignmentEnabledTime,
      assignmentDisabledTime, delegatable,
      attributeAssignValueOperation,
      wsAttributeAssignId,
      wsOwnerGroupName, wsOwnerGroupId, wsOwnerStemName, wsOwnerStemId, 
      wsOwnerSubjectId, wsOwnerSubjectSourceId, wsOwnerSubjectIdentifier,
      wsOwnerMembershipId, wsOwnerMembershipAnyGroupName, wsOwnerMembershipAnyGroupId,
      wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, wsOwnerMembershipAnySubjectIdentifier,
      wsOwnerAttributeDefName, wsOwnerAttributeDefId, wsOwnerAttributeAssignId,
      action, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsAssignAttributesLiteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getPermissionAssignments",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(roleLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(wsSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actions, 
      includePermissionAssignDetail,
      includeAttributeDefNames, 
      includeAttributeAssignments,
      includeAssignmentsOnAssignments, 
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeSubjectDetail,
      subjectAttributeNames, 
      includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      enabled, pointInTimeFrom, pointInTimeTo, immediateOnly, permissionType, 
      permissionProcessor, 
      GrouperUtil.changeToVersion(limitEnvVars, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeLimits});
    
    return (WsGetPermissionAssignmentsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getPermissionAssignmentsLite",
        new Object[]{clientVersion,
      wsAttributeDefName, wsAttributeDefId, wsAttributeDefNameName, wsAttributeDefNameId,
      roleName, roleId,
      wsSubjectId, wsSubjectSourceId, wsSubjectIdentifier,
      action, includePermissionAssignDetail,
      includeAttributeDefNames, includeAttributeAssignments,
      includeAssignmentsOnAssignments, actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1, enabled, pointInTimeFrom, pointInTimeTo, immediateOnly,
      permissionType, permissionProcessor,
      limitEnvVarName0, limitEnvVarValue0,
      limitEnvVarType0, limitEnvVarName1,
      limitEnvVarValue1, limitEnvVarType1, includeLimits});
    
    return (WsGetPermissionAssignmentsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignPermissions",
        new Object[]{clientVersion, permissionType,
      GrouperUtil.changeToVersion(permissionDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      permissionAssignOperation,
      assignmentNotes, assignmentEnabledTime,
      assignmentDisabledTime, delegatable,
      GrouperUtil.changeToVersion(wsAttributeAssignLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(roleLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(subjectRoleLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actions,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(attributeDefsToReplace, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actionsToReplace, disallowed});
    
    return (WsAssignPermissionsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignPermissionsLite",
        new Object[]{clientVersion, permissionType,
      permissionDefNameName, permissionDefNameId,
      permissionAssignOperation,
      assignmentNotes, assignmentEnabledTime,
      assignmentDisabledTime, delegatable,
      wsAttributeAssignId,
      roleName, roleId,
      subjectRoleName, subjectRoleId,
      subjectRoleSubjectId, subjectRoleSubjectSourceId, subjectRoleSubjectIdentifier, 
      action, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeSubjectDetail,
      subjectAttributeNames, includeGroupDetail, paramName0, paramValue0,
      paramName1, paramValue1, disallowed});
    
    return (WsAssignPermissionsLiteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefNameSave",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsAttributeDefNameToSaves, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsAttributeDefNameSaveResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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
   * @param createParentStemsIfNotExist T or F (default is F) if parent stems should be created if not exist
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
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefNameSaveLite",
        new Object[]{clientVersion,
      attributeDefNameLookupUuid, attributeDefNameLookupName, attributeDefLookupUuid, 
      attributeDefLookupName, attributeDefNameUuid,attributeDefNameName, 
      displayExtension,description,  saveMode, createParentStemsIfNotExist,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsAttributeDefNameSaveLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
      String sortString, String ascending, 
      String wsInheritanceSetRelation, WsSubjectLookup actAsSubjectLookup, WsParam[] params,
      WsSubjectLookup wsSubjectLookup, String serviceRole) {

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findAttributeDefNames",
        new Object[]{
      clientVersion, scope, splitScope,
      GrouperUtil.changeToVersion(wsAttributeDefLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      attributeAssignType, attributeDefType,
      GrouperUtil.changeToVersion(wsAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      pageSize, pageNumber, sortString, ascending, wsInheritanceSetRelation,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      wsSubjectLookup, serviceRole
      });

    return (WsFindAttributeDefNamesResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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
   * @param serviceRole is the role you are looking for, admin or user, ServiceRole enum
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
      String subjectIdentifier, String serviceRole) {
        
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findAttributeDefNamesLite",
        new Object[]{clientVersion,
      scope, splitScope, uuidOfAttributeDef, nameOfAttributeDef,
      attributeAssignType, attributeDefType, attributeDefNameUuid, attributeDefNameName, 
      pageSize, pageNumber, sortString, ascending,
      wsInheritanceSetRelation,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0,
      paramValue0, paramName1, paramValue1, subjectId, subjectSourceId, subjectIdentifier, serviceRole});
    
    return (WsFindAttributeDefNamesResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignAttributeDefNameInheritance",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsAttributeDefNameLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(relatedWsAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      assign, replaceAllExisting,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType,
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
    });
    
    return (WsAssignAttributeDefNameInheritanceResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  }
  
  /**
   * assign or unassign attribute def name permission inheritance
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeDefNameUuid id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName
   * @param attributeDefNameName name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId
   * @param relatedAttributeDefNameUuid id of attribute def name to add or remove from inheritance from the container
   * @param relatedAttributeDefNameName name of attribute def name to add or remove from inheritance from the container
   * @param assign T to assign, or F to remove assignment
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
    
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignAttributeDefNameInheritanceLite",
        new Object[]{clientVersion, attributeDefNameUuid, attributeDefNameName, relatedAttributeDefNameUuid, relatedAttributeDefNameName,
      assign, actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, paramName0,
      paramValue0, paramName1, paramValue1
    });
    
    return (WsAssignAttributeDefNameInheritanceResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefNameDelete",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsAttributeDefNameLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsAttributeDefNameDeleteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefNameDeleteLite",
        new Object[]{clientVersion,
      attributeDefNameUuid, attributeDefNameName,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0, paramValue0,
      paramName1, paramValue1});
    
    return (WsAttributeDefNameDeleteLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
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
    
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "acknowledge",
        new Object[]{clientVersion, queueOrTopicName, messageSystemName, acknowledgeType, messageIds, anotherQueueOrTopicName, anotherQueueType,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });

    return (WsMessageAcknowledgeResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "assignAttributeDefActions",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsAttributeDefLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actions, assign, replaceAllExisting,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });
    
    return (WsAttributeDefAssignActionResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefSave",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsAttributeDefsToSave, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType,
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });
    
    return (WsAttributeDefSaveResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefSaveLite",
        new Object[]{clientVersion,
      attributeDefLookupUuid, attributeDefLookupName,
      uuidOfAttributeDef, nameOfAttributeDef,
      assignToAttributeDef, assignToAttributeDefAssignment,
      assignToEffectiveMembership, assignToEffectiveMembershipAssignment,
      assignToGroup, assignToGroupAssignment, 
      assignToImmediateMembership, assignToImmediateMembershipAssignment,
      assignToMember, assignToMemberAssignment,
      assignToStem, assignToStemAssignment,
      attributeDefType, multiAssignable,
      multiValued, valueType,
      description, saveMode, createParentStemsIfNotExist,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0, paramValue0,
      paramName1, paramValue1
      });
    
    return (WsAttributeDefSaveLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefDelete",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType,
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });
    
    return (WsAttributeDefDeleteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "attributeDefDeleteLite",
        new Object[]{clientVersion,
      wsNameOfAttributeDef, wsIdOfAttributeDef,
      wsIdIndexOfAttributeDef, actAsSubjectId,
      actAsSubjectSourceId, actAsSubjectIdentifier, paramName0,
      paramValue0,
      paramName1, paramValue1
      });
    
    return (WsAttributeDefDeleteLiteResult)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findAttributeDefs",
        new Object[]{clientVersion, scope, splitScope, 
      GrouperUtil.changeToVersion(wsAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      privilegeName,
      stemScope, parentStemId, findByUuidOrName,
      pageSize, pageNumber,
      sortString, ascending,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });
    
    return (WsFindAttributeDefsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findAttributeDefsLite",
        new Object[]{clientVersion,
      scope, splitScope, uuidOfAttributeDef,
      nameOfAttributeDef,
      idIndexOfAttributeDef, privilegeName,
      stemScope, parentStemId,
      findByUuidOrName,
      pageSize, pageNumber,
      sortString, ascending,
      actAsSubjectId, actAsSubjectSourceId,
      actAsSubjectIdentifier, paramName0,
      paramValue0, paramName1, paramValue1
      });
    
    return (WsFindAttributeDefsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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

    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getAttributeAssignActions",
        new Object[]{clientVersion, 
      GrouperUtil.changeToVersion(wsAttributeDefLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      actions, 
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });
    
    return (WsGetAttributeAssignActionsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "getAttributeAssignActionsLite",
        new Object[]{clientVersion, wsNameOfAttributeDef, wsIdOfAtttributeDef, wsIdIndexOfAtrrbuteDef, action,
        actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, 
        paramName0, paramValue0, paramName1, paramValue1
      });
    
    return (WsGetAttributeAssignActionsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
  }

  /**
   * @param clientVersion
   * @param queueOrTopicName
   * @param messageSystemName
   * @param blockMillis - the millis to block waiting for messages, max of 20000 (optional)
   * @param maxMessagesToReceiveAtOnce - max number of messages to receive at once, though can't be more than the server maximum (optional)
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message receive call
   */
  public WsMessageResults receiveMessage(final String clientVersion,
      String queueOrTopicName, String messageSystemName,
      final Integer blockMillis, final Integer maxMessagesToReceiveAtOnce,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "receiveMessage",
        new Object[]{clientVersion, queueOrTopicName, messageSystemName, blockMillis, maxMessagesToReceiveAtOnce,
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsMessageResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
  
  }

  /**
   * @param clientVersion
   * @param queueType - queue or topic (required)
   * @param queueOrTopicName
   * @param messageSystemName
   * @param messages
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message send call
   */
  public WsMessageResults sendMessage(final String clientVersion,
      String queueType, String queueOrTopicName, String messageSystemName,
      WsMessage[] messages,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "sendMessage",
        new Object[]{clientVersion, queueType, queueOrTopicName, messageSystemName,
      GrouperUtil.changeToVersion(messages, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsMessageResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "externalSubjectDelete",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsExternalSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsExternalSubjectDeleteResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);

  
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "externalSubjectSave",
        new Object[]{clientVersion,
      GrouperUtil.changeToVersion(wsExternalSubjectToSaves, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      txType, 
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      });
    
    return (WsExternalSubjectSaveResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
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
  
    Object result = GrouperUtil.callMethodWithMoreParams(GrouperUtil.newInstance(GrouperServiceUtils.currentServiceClass()), 
        GrouperServiceUtils.currentServiceClass(), "findExternalSubjects",
        new Object[]{
      clientVersion,
      GrouperUtil.changeToVersion(wsExternalSubjectLookups, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(actAsSubjectLookup, GrouperServiceUtils.currentServiceClass().getPackage().getName()),
      GrouperUtil.changeToVersion(params, GrouperServiceUtils.currentServiceClass().getPackage().getName())
      });
    
    return (WsFindExternalSubjectsResults)GrouperUtil.changeToVersion(result, THIS_VERSION_PACKAGE);
    
  }

}
