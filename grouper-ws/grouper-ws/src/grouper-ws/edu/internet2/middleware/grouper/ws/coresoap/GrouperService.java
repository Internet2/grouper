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

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;

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
@SwaggerDefinition(
    consumes = {"application/json", "application/xml"},
    produces = {"application/json", "application/xml"},
    schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS}
)
@Api(value = "Grouper", description = "Integrate with the Grouper registry")
public class GrouperService {

//  public static void main(String[] args) throws Exception {
//    Class clazz = GrouperService.class;
//    //    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//    //    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
//    //    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File(filename)));
//    //    JavacTask javacTask = 
//    //        (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
//    //      Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
//    String methodIn = "clientVersion, String attributeAssignType, String wsAttributeDefNameName, String wsAttributeDefNameId, String attributeAssignOperation, String valueId, String valueSystem, String valueFormatted, String assignmentNotes, String assignmentEnabledTime, String assignmentDisabledTime, String delegatable, String attributeAssignValueOperation, String wsAttributeAssignId, String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier, String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId, String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier, String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, String wsOwnerAttributeAssignId, String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, String includeSubjectDetail, String subjectAttributeNames, String includeGroupDetail, String paramName0, String paramValue0, String paramName1, String paramValue1";
//    String[] methodSplit = methodIn.split(", String ");
//
//    for (Method method : clazz.getMethods()) {
//
//      if (!method.getName().endsWith("Lite")) {
//        continue;
//      }
//
//      Set<String> paramNames = new HashSet<String>();
//      for (Annotation annotation : method.getAnnotations()) {
//
//        //        @ApiImplicitParams({
//        //          @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
//        //              value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
//
//        if (annotation instanceof ApiImplicitParams) {
//          ApiImplicitParams apiImplicitParams = (ApiImplicitParams) annotation;
//          if (apiImplicitParams.value() == null) {
//            continue;
//          }
//          for (ApiImplicitParam apiImplicitParam : apiImplicitParams.value()) {
//            if (paramNames.contains(apiImplicitParam.name())) {
//              System.out.println("duplicateParamName: " + method.getName() + ": "
//                  + apiImplicitParam.name());
//            } else {
//              paramNames.add(apiImplicitParam.name());
//            }
//          }
//
//        }
//
//      }
//      //only print problematic methods and desired method
//      if ((method.getParameterCount() != paramNames.size() - 1)
//          && method.getName().equals("assignAttributesLite")) {
//
//        System.out.println("Method: " + method.getName() + ", params: "
//            + method.getParameterCount() + ", swaggerParams: " + paramNames.size());
//
//        for (String par : methodSplit) {
//          if (!paramNames.contains(par)) {
//            System.out.println("Swagger is missing: " + par);
//          }
//        }
//      }
//    }
//  }
  

    
  
  
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
  private static final Log LOG = GrouperUtil.getLog(GrouperService.class);

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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
   * @param typeOfGroups is the comma separated TypeOfGroups to find, e.g. group, role, entity
   * @param enabled enabled is A for all, T or null for enabled only, F for disabled
   * @return the groups, or no groups if none found
   */
  @POST
  @Path("/grouper-ws/servicesRest/vF_G_UPL/groups")
  @ApiOperation(httpMethod = "POST", value = "Find groups lite", nickname = "findGroupsLite", response = WsFindGroupsResultsWrapper.class,
  notes = "<b>Description</b>: Find groups search for groups based on name, attribute, parent stem, etc. Can build queries with group math (AND / OR / MINUS)"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Find+Groups'>wiki</a> and go to samples to see requests and responses") 
  @ApiResponses({@ApiResponse(code = 200, message = "SUCCESS", response = WsFindGroupsResultsWrapper.class),
                @ApiResponse(code = 400, message = "INVALID_QUERY", response = WsFindGroupsResultsWrapperError.class),
                @ApiResponse(code = 404, message = "STEM_NOT_FOUND", response = WsFindGroupsResultsWrapperError.class),
                @ApiResponse(code = 500, message = "EXCEPTION", response = WsFindGroupsResultsWrapperError.class)})
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "queryFilterType", dataType = "String", paramType = "form", 
    value = "findGroupType is the WsQueryFilterType enum for which type of find is happening: "
      + "e.g. FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, FIND_BY_APPROXIMATE_ATTRIBUTE, "
      + "FIND_BY_ATTRIBUTE,  FIND_BY_GROUP_NAME_APPROXIMATE, FIND_BY_TYPE, AND, OR, MINUS", 
      example = "FIND_BY_GROUP_UUID | FIND_BY_GROUP_NAME_EXACT | FIND_BY_STEM_NAME | FIND_BY_APPROXIMATE_ATTRIBUTE |"
      + " FIND_BY_ATTRIBUTE | FIND_BY_GROUP_NAME_APPROXIMATE | FIND_BY_TYPE | AND | OR | MINUS"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
        value = "groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
        value = "Will return groups only in this stem (by name)", example = "some:parent:folder:name"),
    @ApiImplicitParam(required = false, name = "stemNameScope", dataType = "String", paramType = "form", 
        value = "if searching by stem, ONE_LEVEL is for one level, ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE", 
        example = "ONE_LEVEL | ALL_IN_SUBTREE"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "groupAttributeName", dataType = "String", paramType = "form", 
    value = "This is the attribute name, or null for search all attributes.  This could be a legacy attribute or an attributeDefName of a string valued attribute", example = "some:attribute:name"),
    @ApiImplicitParam(required = false, name = "groupAttributeValue", dataType = "String", paramType = "form", 
    value = "The attribute value to filter on if querying by attribute and value", example = "someValue"),
    @ApiImplicitParam(required = false, name = "groupTypeName", dataType = "String", paramType = "form", 
    value = "not implemented", example = "NA"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging", example = "1"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
        value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
        example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "typeOfGroups", dataType = "String", paramType = "form", 
    value = "Comma separated type of groups can be an enum of TypeOfGroup, e.g. group, role, entity", example = "group|role|entity"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "enabled", dataType = "String", paramType = "form", 
    value = "enabled is A for all, T or null for enabled only, F for disabled", example = "A|T|F")   
  })
  public WsFindGroupsResults findGroupsLite(
      final String clientVersion,
      String queryFilterType, String groupName, String stemName, String stemNameScope,
      String groupUuid, String groupAttributeName, String groupAttributeValue,
      String groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1, String pageSize, 
      String pageNumber, String sortString, String ascending, String typeOfGroups,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved,
      String enabled) {

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
          pageSize, pageNumber, sortString, ascending, typeOfGroups,
          pageIsCursor, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrieved,
          enabled);
      
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
   * @param pointInTimeRetrieve true means retrieve point in time records
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
   * @return the members, or no members if none found
   */
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_MEL/members")
  @ApiOperation(httpMethod = "POST", value = "Get members lite", nickname = "getMembersLite", //response = .class,
  notes = "<b>Description</b>: Get members will retrieve subjects assigned to a group."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Members'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging", example = "1"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
    value = "If the member is part of a certain field membership of the group", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
    @ApiImplicitParam(required = false, name = "pointInTimeFrom", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "pointInTimeTo", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "memberFilter", dataType = "String", paramType = "form", 
    value = "can be All(default), Effective (non immediate), Immediate (direct),Composite (if composite group with group math (union, minus,etc)", example = "Effective"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
    value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
    example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). "
        + "Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "sourceIds", dataType = "String", paramType = "form", 
    value = "comma separated source ids or null for all", example = "schoolPerson, g:gsa"),
    @ApiImplicitParam(required = false, name = "pointInTimeRetrieve", dataType = "String", paramType = "form", 
    value = "true means retrieve point in time records", example = "T|F"),
  })
   
    
  public WsGetMembersLiteResult getMembersLite(final String clientVersion,
      String groupName, String groupUuid, String memberFilter, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, final String fieldName,
      String includeGroupDetail, 
      String includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds,
      String pointInTimeFrom, String pointInTimeTo, String pageSize, String pageNumber,
      String sortString, String ascending, String pointInTimeRetrieve,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {

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
      
      Boolean pointInTimeRetrieveBoolean = GrouperUtil.booleanValue(pointInTimeRetrieve, false);
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      wsGetMembersLiteResult = GrouperServiceLogic.getMembersLite(grouperWsVersion, 
          groupName, groupUuid, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1, sourceIds,
          pointInTimeFromTimestamp, pointInTimeToTimestamp,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          pointInTimeRetrieveBoolean, pageIsCursorBoolean, pageLastCursorField, 
          pageLastCursorFieldType, pageCursorFieldIncludesLastRetrievedBoolean);
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
   * @param pointInTimeRetrieve true means retrieve point in time records
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
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
      String sortString, String ascending, String pointInTimeRetrieve,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {
	  
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
      
      Boolean pointInTimeRetrieveBoolean = GrouperUtil.booleanValue(pointInTimeRetrieve, false);
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      wsGetMembersResults = GrouperServiceLogic.getMembers(grouperWsVersion, wsGroupLookups, 
          wsMemberFilter, actAsSubjectLookup, field, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params, sourceIds, 
          pointInTimeFromTimestamp, pointInTimeToTimestamp,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, pointInTimeRetrieveBoolean,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
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
      String pointInTimeFrom, String pointInTimeTo,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {
    
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
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);
      
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      wsGetGroupsResults = GrouperServiceLogic.getGroups(grouperWsVersion, subjectLookups, 
          wsMemberFilter, actAsSubjectLookup, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, params, fieldName, scope, wsStemLookup, 
          stemScopeEnum, enabled, pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          pointInTimeFromTimestamp, pointInTimeToTimestamp,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);
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
  @POST
  @Path("/grouper-ws/servicesRest/vS_T_DEL/stems")
  @ApiOperation(httpMethod = "POST", value = "Stem delete lite", nickname = "stemDeleteLite", //response = .class,
  notes = "<b>Description</b>: Stem delete will insert or update a stem's uuid, extension, display name, or description (with restrictions)"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Stem+Delete'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, stemUuid search by stem uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Id path in UI, stemName search by stem name (must match exactly), cannot use other params with this", example = "some:stem:name"),
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_R_DEL/groups")
  @ApiOperation(httpMethod = "POST", value = "Group delete lite", nickname = "groupDeleteLite", //response = .class,
  notes = "<b>Description</b>: Group delete will insert or update a group's uuid, extension, display name, or description (with restrictions)"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Group+Delete'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
  })
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
   * @param alternateName the alternate name of the group
   * @param disabledTime 
   * @param enabledTime 
   * @return the result of one member add
   */
  @POST
  @Path("/grouper-ws/servicesRest/vG_R_SAL/groups")
  @ApiOperation(httpMethod = "POST", value = "Group save lite", nickname = "groupSaveLite", //response = .class,
  notes = "<b>Description</b>: Group save will insert or update a group's uuid, extension, display name, or description (with restrictions)."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Group+Save'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "saveMode", dataType = "String", paramType = "form", 
    value = "if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)", example = "INSERT"),
    @ApiImplicitParam(required = false, name = "groupLookupUuid", dataType = "String", paramType = "form", 
    value = "the uuid of the group to edit (mutually exclusive with groupLookupName)", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "groupLookupName", dataType = "String", paramType = "form", 
    value = "the name of the group to edit (mutually exclusive with groupLookupUuid)", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "displayExtension", dataType = "String", paramType = "form", 
    value = "display name of the group, empty will be ignored", example = "My Group"),
    @ApiImplicitParam(required = false, name = "description", dataType = "String", paramType = "form", 
    value = "descirption of the group, empty will be ignored", example = ""),
    @ApiImplicitParam(required = false, name = "enabledTime", dataType = "String", paramType = "form", 
    value = "date this will be enabled (for future provisioning): yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "disabledTime", dataType = "String", paramType = "form", 
    value = "date this will be disabled (for future provisioning): yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "typeOfGroup1", dataType = "String", paramType = "form", 
    value = "type of group can be an enum of TypeOfGroup, e.g. group, role, entity", example = "entity"),
    @ApiImplicitParam(required = false, name = "alternateName", dataType = "String", paramType = "form", 
    value = "the alternate name of the group", example = "some:group:othername"),
  })
  public WsGroupSaveLiteResult groupSaveLite(final String clientVersion,
      String groupLookupUuid, String groupLookupName, String groupUuid,String groupName, 
      String displayExtension,String description,  String saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String typeOfGroup1, String alternateName,
      String disabledTime, String enabledTime) {

    WsGroupSaveLiteResult wsGroupSaveLiteResult = new WsGroupSaveLiteResult();
    GrouperVersion grouperWsVersion = null;
    try {
      
      boolean includeGroupDetailBoolean = GrouperServiceUtils.booleanValue(
          includeGroupDetail, false, "includeGroupDetail");

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);
      
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      TypeOfGroup typeOfGroup = TypeOfGroup.valueOfIgnoreCase(typeOfGroup1, false);
      Timestamp disabledTimestamp = GrouperServiceUtils.stringToTimestamp(disabledTime);
      Timestamp enabledTimestamp = GrouperServiceUtils.stringToTimestamp(enabledTime);
      
      wsGroupSaveLiteResult = GrouperServiceLogic.groupSaveLite(grouperWsVersion, groupLookupUuid,
          groupLookupName, groupUuid, groupName, displayExtension, description, saveModeEnum,
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean,  
          paramName0, paramValue0, paramName1, paramValue1, typeOfGroup, alternateName, disabledTimestamp,
          enabledTimestamp);
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
  @POST
  @Path("/grouper-ws/servicesRest/vS_T_SAL/stems")
  @ApiOperation(httpMethod = "POST", value = "Stem save lite", nickname = "stemSaveLite", //response = .class,
  notes = "<b>Description</b>: Stem save will insert or update a stem's uuid, extension, display name, or description (with restrictions)"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Stem+Save'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, stemUuid search by stem uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Id path in UI, stemName search by stem name (must match exactly), cannot use other params with this", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "displayExtension", dataType = "String", paramType = "form", 
    value = "display name of the stem", example = "My Folder"),
    @ApiImplicitParam(required = false, name = "description", dataType = "String", paramType = "form", 
    value = "descirption of the stem, empty will be ignored", example = ""),
    @ApiImplicitParam(required = false, name = "saveMode", dataType = "String", paramType = "form", 
    value = "if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)", example = "INSERT"),
    @ApiImplicitParam(required = false, name = "stemLookupUuid", dataType = "String", paramType = "form", 
    value = "the uuid of the stem to save (mutually exclusive with stemLookupName), null for insert", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "stemLookupName", dataType = "String", paramType = "form", 
    value = "the name of the stam to save (mutually exclusive with stemLookupUuid), null for insert", example = "some:stem:name"),
    
  })
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_GRL/groups")
  @ApiOperation(httpMethod = "POST", value = "Get groups lite", nickname = "getGroupsLite", //response = .class,
  notes = "<b>Description</b>: Get groups will get the groups that a subject is in"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Groups'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging", example = "1"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "enabled", dataType = "String", paramType = "form", 
    value = "enabled is A for all, T or null for enabled only, F for disabled", example = "A|T|F"),
    @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
    value = "If the member is added to a certain field membership of the group", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
    @ApiImplicitParam(required = false, name = "pointInTimeFrom", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "pointInTimeTo", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "memberFilter", dataType = "String", paramType = "form", 
    value = "can be All(default), Effective (non immediate), Immediate (direct),Composite (if composite group with group math (union, minus,etc)", example = "Effective"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). "
        + "Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "scope", dataType = "String", paramType = "form", 
    value = "is a DB pattern that will have % appended to it, or null for all", example = "school:whatever:parent"),
    @ApiImplicitParam(required = false, name = "stemScope", dataType = "String", paramType = "form", 
    value = "is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath. You must pass stemScope if you pass a stem", example = "ONE_LEVEL"),
    @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource to be found", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier to be found, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "subjectId to be found, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, stemUuid search by stem uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Id path in UI, stemName search by stem name (must match exactly), cannot use other params with this", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
    value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
    example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
  })
  public WsGetGroupsLiteResult getGroupsLite(final String clientVersion, String subjectId,
      String subjectSourceId, String subjectIdentifier, String memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String includeGroupDetail, 
      String includeSubjectDetail, 
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String fieldName, String scope, 
      String stemName, String stemUuid, String stemScope, String enabled, 
      String pageSize, String pageNumber, String sortString, String ascending,
      String pointInTimeFrom, String pointInTimeTo,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {

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
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      wsGetGroupsLiteResult = GrouperServiceLogic.getGroupsLite(grouperWsVersion, 
          subjectId, subjectSourceId, subjectIdentifier, wsMemberFilter, actAsSubjectId, 
          actAsSubjectSourceId, actAsSubjectIdentifier, includeGroupDetailBoolean, 
          includeSubjectDetailBoolean, subjectAttributeNames, paramName0, paramValue0, 
          paramName1, paramValue1, fieldName, scope, stemName, stemUuid, stemScopeEnum, enabled, 
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, 
          pointInTimeFromTimestamp,
          pointInTimeToTimestamp, pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_A_MEL/members")
  @ApiOperation(httpMethod = "POST", value = "Add member lite", nickname = "addMemberLite", //response = .class,
  notes = "<b>Description</b>: Add member will add or replace the membership of a group.  This affects only direct memberships, not indirect memberships.  If the user is already a member of the group it is still a success"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Add+Member'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of the person to be added", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of entity to be added, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "subjectId of entity to be added, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
    value = "If the member should be added to a certain field membership of the group", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
    @ApiImplicitParam(required = false, name = "enabledTime", dataType = "String", paramType = "form", 
    value = "date this membership will be enabled (for future provisioning): yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "disabledTime", dataType = "String", paramType = "form", 
    value = "date this membership will be disabled (for future provisioning): yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "addExternalSubjectIfNotFound", dataType = "String", paramType = "form", 
    value = "T or F (default F), if this is a search by id or identifier, with no source, or the external source,and the subject is not found, then add an external subject (if the user is allowed) defaults to false", example = "T"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName")
    
  })
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
    @POST
    @Path("/grouper-ws/servicesRest/vH_M_EML/members")
    @ApiOperation(httpMethod = "POST", value = "Has member lite", nickname = "hasMemberLite", //response = .class,
    notes = "<b>Description</b>: Has member will see if a group contains a subject as a member"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Has+Member'>wiki</a> and go to samples to see requests and responses") 
    @ApiImplicitParams({
      @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
          value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
      @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
          value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
      @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
      value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
          + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
      @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
      value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
          + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
      @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
      value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
          + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
      @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
      value = "Optional params for this request", example = "NA"),
      @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
      value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
      @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
      value = "If the group detail should be returned, default to false", example = "T|F"),
      @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
      value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
      @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
      value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
      @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
      value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
      @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
      value = "is if the Group.hasMember() method with field is to be called", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
      @ApiImplicitParam(required = false, name = "pointInTimeFrom", dataType = "String", paramType = "form", 
      value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
          + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
          + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
      @ApiImplicitParam(required = false, name = "pointInTimeTo", dataType = "String", paramType = "form", 
      value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
          + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
          + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
      @ApiImplicitParam(required = false, name = "memberFilter", dataType = "String", paramType = "form", 
      value = "can be All(default), Effective (non immediate), Immediate (direct),Composite (if composite group with group math (union, minus,etc)", example = "Effective"),
      @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
      value = "the Id of the subjectSource of the entitity to be found", example = "schoolPerson"),
      @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
      value = "subjectIdentifier of entity to be found, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
      @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
      value = "subjectId of entity to be found, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    })
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
  @POST
  @Path("/grouper-ws/servicesRest/vM_C_SUL/members")
  @ApiOperation(httpMethod = "POST", value = "Member change subject lite", nickname = "memberChangeSubjectLite", //response = .class,
  notes = "<b>Description</b>: \"Member change subject\" will change the subject that a member refers to. You would want to do this when a person or entity changes their id, or if they were loaded wrong in the system."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Member+change+subject'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "deleteOldMember", dataType = "String", paramType = "form", 
    value = "T/F or TRUE/FALSE (Case sensitive) true means delete subject that was changed, false means keep, defaults to true", example = "T"),
    @ApiImplicitParam(required = false, name = "oldSubjectSourceId", dataType = "String", paramType = "form", 
    value = "the old Id of the subjectSource of the person to be changed, recommended", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "oldSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "Old subjectIdentifier of entity to be changed, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "oldsubjectId", dataType = "String", paramType = "form", 
    value = "Old subjectId of entity to be changed, mutually exclusive with subjectIdentifier, one of the two is required (preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "newSubjectSourceId", dataType = "String", paramType = "form", 
    value = "the new Id of the subjectSource of the person to be changed, recommended", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "newSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "New subjectIdentifier of entity to be changed, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "newSubjectId", dataType = "String", paramType = "form", 
    value = "New subjectId of entity to be changed, mutually exclusive with subjectIdentifier, one of the two is required (preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vD_M_EML/members")
  @ApiOperation(httpMethod = "POST", value = "Delete member lite", nickname = "deleteMemberLite", //response = .class,
  notes = "<b>Description</b>: Delete member will delete or replace the membership of a group.  This affects only direct memberships, not indirect memberships.  If the user is in an indirect membership, this is still a success"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Delete+Member'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of the entitity to be deleted", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of entity to be deleted, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "subjectId of entity to be deleted, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
    value = "If the member should be deleted from a certain field membership of the group", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vF_S_EML/stems")
  @ApiOperation(httpMethod = "POST", value = "Find stems lite", nickname = "findStemsLite", //response = .class,
  notes = "<b>Description</b>: Find stems search for stems based on name, attribute, parent stem, etc. Can build queries with group math (AND / OR / MINUS)"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Find+Stems'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, stemUuid search by stem uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Id path in UI, stemName search by stem name (must match exactly), cannot use other params with this", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "stemAttributeName", dataType = "String", paramType = "form", 
    value = "if searching by attribute, this is name,or null for all attributes", example = "etc:attributes:someAttributeName"),
    @ApiImplicitParam(required = false, name = "stemAttributeValue", dataType = "String", paramType = "form", 
    value = "if searching by attribute, this is the value", example = "someValue"),
    @ApiImplicitParam(required = false, name = "parentStemName", dataType = "String", paramType = "form", 
    value = "will return stems in this stem. can be used with various query types", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "parentStemNameScope", dataType = "String", paramType = "form", 
    value = "if searching by stem, ONE_LEVEL is for one level,ALL_IN_SUBTREE will return all in sub tree. Required ifsearching by stem", example = "ONE_LEVEL"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "stemQueryFilterType", dataType = "String", paramType = "form", 
    value = "findStemType is the WsFindStemType enum for whichtype of find is happening: e.g.FIND_BY_STEM_UUID, FIND_BY_STEM_NAME, FIND_BY_PARENT_STEM_NAME, "
        + "FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_STEM_NAME_APPROXIMATEAND, OR, MINUS;", example = "FIND_BY_STEM_NAME"),
    
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_GPL/grouperPrivileges")
  @ApiOperation(httpMethod = "POST", value = "Get grouper privileges lite", nickname = "getGrouperPrivilegesLite", //response = .class,
  notes = "<b>Description</b>: \"Get grouper privileges\" will retrieve the privileges for a subject and or (group or stem)."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+grouper+privileges'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "privilegeName", dataType = "String", paramType = "form", 
    value = "Name of the privilege", example = "for groups: read, view, update, admin, optin, optout, groupAttrRead, groupAttrUpdate.  for stems: create, stemAttrRead, stemAdmin, stemView, stemAttrUpdate"),
    @ApiImplicitParam(required = false, name = "privilegeType", dataType = "String", paramType = "form", 
    value = "Type of privilege, (e.g. access for groups and naming for stems)", example = "access"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, stemUuid search by stem uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Id path in UI, stemName search by stem name (must match exactly), cannot use other params with this", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of the entity to get privileges of", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of entity to get privileges of, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "subjectId of entity to get privileges of, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
  })
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
   * @param privilegeName (e.g. for groups: read, view, update, admin, optin, optout, groupAttrRead, groupAttrUpdate.  e.g. for stems:
   * create, stemAttrRead, stemAdmin, stemView, stemAttrUpdate)
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_G_PRL/grouperPrivileges")
  @ApiOperation(httpMethod = "POST", value = "Assign grouper privileges lite", nickname = "assignGrouperPrivilegesLite", //response = .class,
  notes = "<b>Description</b>: Will assign privileges for a subject and (group or stem).  This affects only direct memberships, not indirect memberships.  If the user is already a member of the group it is still a success"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Add+or+remove+grouper+privileges'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of the entity to have privileges assigned", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of entity to have privileges assigned, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "subjectId of entity to have privileges assigned, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "Id path in UI, groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return.If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)", example = "lastName"),
    @ApiImplicitParam(required = false, name = "privilegeName", dataType = "String", paramType = "form", 
    value = "Name of the privilege", example = "for groups: read, view, update, admin, optin, optout, groupAttrRead, groupAttrUpdate.  for stems: create, stemAttrRead, stemAdmin, stemView, stemAttrUpdate"),
    @ApiImplicitParam(required = false, name = "privilegeType", dataType = "String", paramType = "form", 
    value = "Type of privilege, (e.g. access for groups and naming for stems)", example = "access"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, stemUuid search by stem uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Id path in UI, stemName search by stem name (must match exactly), cannot use other params with this", example = "some:stem:name"),
    @ApiImplicitParam(required = true, name = "allowed", dataType = "String", paramType = "form", 
    value = "T|F is this is allowing the privilege, or denying it", example = "T|F"),
  })
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item 
   * @param pageSizeForMember page size if paging in the members part
   * @param pageNumberForMember page number 1 indexed if paging in the members part
   * @param sortStringForMember must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   * @param ascendingForMember T or null for ascending, F for descending in the members part
   * @param pageIsCursorForMember true means cursor based paging
   * @param pageLastCursorFieldForMember field based on which paging needs to occur 
   * @param pageLastCursorFieldTypeForMember type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrievedForMember should the result has last retrieved item
   * @param pointInTimeRetrieve true means retrieve point in time records
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
   * @return the results
   */
  public WsGetMembershipsResults getMemberships(final String clientVersion,
      WsGroupLookup[] wsGroupLookups, WsSubjectLookup[] wsSubjectLookups, String wsMemberFilter,
      WsSubjectLookup actAsSubjectLookup, String fieldName, String includeSubjectDetail,
      String[] subjectAttributeNames, String includeGroupDetail, final WsParam[] params, 
      String[] sourceIds, String scope, 
      WsStemLookup wsStemLookup, String stemScope, String enabled, String[] membershipIds, 
      WsStemLookup[] wsOwnerStemLookups, WsAttributeDefLookup[] wsOwnerAttributeDefLookups, 
      String fieldType, String serviceRole, WsAttributeDefNameLookup serviceLookup, String pageSize, String pageNumber,
      String sortString, String ascending, 
      String pageSizeForMember, String pageNumberForMember,
      String sortStringForMember, String ascendingForMember,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved,
      String pageIsCursorForMember, String pageLastCursorFieldForMember, 
      String pageLastCursorFieldTypeForMember,
      String pageCursorFieldIncludesLastRetrievedForMember,
      String pointInTimeRetrieve,
      String pointInTimeFrom, String pointInTimeTo) {  
    
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
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      Integer pageSizeForMemberInteger = GrouperUtil.intObjectValue(pageSizeForMember, true);
      Integer pageNumberForMemberInteger = GrouperUtil.intObjectValue(pageNumberForMember, true);
      
      Boolean ascendingForMemberBoolean = GrouperUtil.booleanObjectValue(ascendingForMember);
      
      Boolean pageIsCursorForMemberBoolean = GrouperUtil.booleanValue(pageIsCursorForMember, false);
      Boolean pageCursorFieldIncludesLastRetrievedForMemberBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrievedForMember, false);

      Boolean pointInTimeRetrieveBoolean = GrouperUtil.booleanValue(pointInTimeRetrieve, false);
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      wsGetMembershipsResults = GrouperServiceLogic.getMemberships(grouperWsVersion, wsGroupLookups, 
          wsSubjectLookups, memberFilter, actAsSubjectLookup, field, includeSubjectDetailBoolean, 
          subjectAttributeNames, includeGroupDetailBoolean, params, sourceIds, scope, wsStemLookup, theStemScope, enabled, membershipIds,
          wsOwnerStemLookups, wsOwnerAttributeDefLookups, fieldTypeEnum, serviceRoleEnum, serviceLookup,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, 
          pageSizeForMemberInteger, pageNumberForMemberInteger, 
          sortStringForMember, ascendingForMemberBoolean,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean,
          pageIsCursorForMemberBoolean, pageLastCursorFieldForMember, pageLastCursorFieldTypeForMember,
          pageCursorFieldIncludesLastRetrievedForMemberBoolean,
          pointInTimeRetrieveBoolean,
          pointInTimeFromTimestamp, pointInTimeToTimestamp);

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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
   * @param pageSizeForMember page size if paging in the members part
   * @param pageNumberForMember page number 1 indexed if paging in the members part
   * @param sortStringForMember must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   * @param ascendingForMember T or null for ascending, F for descending in the members part
   * @param pageIsCursorForMember true means cursor based paging
   * @param pageLastCursorFieldForMember field based on which paging needs to occur 
   * @param pageLastCursorFieldTypeForMember type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrievedForMember should the result has last retrieved item
   * @param pointInTimeRetrieve true means retrieve point in time records
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
   * @return the memberships, or none if none found
   */
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_MSL/memberships")
  @ApiOperation(httpMethod = "POST", value = "Get memberships lite", nickname = "getMembershipsLite", //response = .class,
  notes = "<b>Description</b>: Get memberships will retrieve membership objects by group, by subject, or by id (or a combination)."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Memberships'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging", example = "1"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "enabled", dataType = "String", paramType = "form", 
    value = "enabled is A for all, T or null for enabled only, F for disabled", example = "A|T|F"),
    @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
    value = "If the member is added to a certain field membership of the group", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
    @ApiImplicitParam(required = false, name = "fieldType", dataType = "String", paramType = "form", 
    value = "is the type of field to look at", example = "list (default, memberships),access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)"),
    @ApiImplicitParam(required = false, name = "pointInTimeFrom", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "pointInTimeTo", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "sourceId", dataType = "String", paramType = "form", 
    value = "sourceId of subject to search for memberships, or null to not restrict", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "sourceIds", dataType = "String", paramType = "form", 
    value = "are comma separated sourceIds", example = "schoolPerson, g:gsa"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "Identifier of subject to search for memberships, or null to not restrict", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "Id of subject to search for memberships, or null to not restrict", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsMemberFilter", dataType = "String", paramType = "form", 
    value = "can be All(default), Effective (non immediate), Immediate (direct),Composite", example = "Effective"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). "
        + "Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "scope", dataType = "String", paramType = "form", 
    value = "is a sql like string which will have a percent % concatenated to the end for groupnames to search in (or stem names)", example = "someApp someGroupExtension"),
    @ApiImplicitParam(required = false, name = "stemName", dataType = "String", paramType = "form", 
    value = "Name of stem to limit the search to (in or under)", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "stemUuid", dataType = "String", paramType = "form", 
    value = "Id of stem to limit the search to (in or under)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "stemScope", dataType = "String", paramType = "form", 
    value = "is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath. You must pass stemScope if you pass a stem", example = "ONE_LEVEL"),
    @ApiImplicitParam(required = false, name = "membershipIds", dataType = "String", paramType = "form", 
    value = "comma separated list of membershipIds to retrieve", example = "a1b2, c3d4, e5f6"),
    @ApiImplicitParam(required = false, name = "ownerStemUuid", dataType = "String", paramType = "form", 
    value = "if looking for privileges on stems, put the stem uuid here", example = "abc123"),
    @ApiImplicitParam(required = false, name = "ownerStemName", dataType = "String", paramType = "form", 
    value = "if looking for privileges on stems, put the stem name to look for here", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "nameOfOwnerAttributeDef", dataType = "String", paramType = "form", 
    value = "if looking for privileges on attribute definitions, put the name of the attribute definition here", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "ownerAttributeDefUuid", dataType = "String", paramType = "form", 
    value = "if looking for privileges on attribute definitions, put the uuid of the attribute definition here", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "serviceRole", dataType = "String", paramType = "form", 
    value = "to filter attributes that a user has a certain role", example = "member"),
    @ApiImplicitParam(required = false, name = "serviceId", dataType = "String", paramType = "form", 
    value = "if filtering by users in a service, then this is the service to look in, mutually exclusive with serviceName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "serviceName", dataType = "String", paramType = "form", 
    value = "if filtering by users in a service, then this is the service to look in, mutually exclusive with serviceId", example = "a:b:c:myService"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
    value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
    example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageSizeForMember", dataType = "String", paramType = "form", 
    value = "Page size if paging in the members part", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumberForMember", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging for the mebers part", example = "1"),
    @ApiImplicitParam(required = false, name = "sortStringForMember", dataType = "String", paramType = "form", 
    value = "must be an hql query field, e.g.can sort on uuid, subjectId, sourceId, sourceString0, "
        + "sortString1, sortString2, sortString3, sortString4, name, descriptionin the members part", 
    example = "name | displayName | extension | displayExtensionForMember"),
    @ApiImplicitParam(required = false, name = "ascendingForMember", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending for members  part.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageIsCursorForMember", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging for members part", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldForMember", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging in members part", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldTypeForMember", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp, in members part", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrievedForMember", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
        @ApiImplicitParam(required = false, name = "pointInTimeRetrieve", dataType = "String", paramType = "form", 
    value = "true means retrieve point in time records", example = "T|F"),
        
  })
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
      String sortString, String ascending, 
      String pageSizeForMember, String pageNumberForMember,
      String sortStringForMember, String ascendingForMember,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved,
      String pageIsCursorForMember, String pageLastCursorFieldForMember, 
      String pageLastCursorFieldTypeForMember, String pageCursorFieldIncludesLastRetrievedForMember,
      String pointInTimeRetrieve,
      String pointInTimeFrom, String pointInTimeTo) {
  
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

      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      Integer pageSizeForMemberInteger = GrouperUtil.intObjectValue(pageSizeForMember, true);
      Integer pageNumberForMemberInteger = GrouperUtil.intObjectValue(pageNumberForMember, true);
      
      Boolean ascendingForMemberBoolean = GrouperUtil.booleanObjectValue(ascendingForMember);

      Boolean pageIsCursorForMemberBoolean = GrouperUtil.booleanValue(pageIsCursorForMember, false);
      Boolean pageCursorFieldIncludesLastRetrievedForMemberBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrievedForMember, false);
      
      Boolean pointInTimeRetrieveBoolean = GrouperUtil.booleanValue(pointInTimeRetrieve, false);
      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      wsGetMembershipsResults = GrouperServiceLogic.getMembershipsLite(grouperWsVersion, groupName,
          groupUuid, subjectId, sourceId, subjectIdentifier, memberFilter,includeSubjectDetailBoolean, 
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier, field, subjectAttributeNames, 
          includeGroupDetailBoolean, paramName0, paramValue1, paramName1, paramValue1, sourceIds, scope, 
          stemName, stemUuid, theStemScope, enabled, membershipIds, ownerStemName, ownerStemUuid, 
          nameOfOwnerAttributeDef, ownerAttributeDefUuid, fieldTypeEnum, serviceRoleEnum, serviceId, serviceName,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean, 
          pageSizeForMemberInteger, pageNumberForMemberInteger, 
          sortStringForMember, ascendingForMemberBoolean,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean,
          pageIsCursorForMemberBoolean, pageLastCursorFieldForMember, 
          pageLastCursorFieldTypeForMember,
          pageCursorFieldIncludesLastRetrievedForMemberBoolean,
          pointInTimeRetrieveBoolean,
          pointInTimeFromTimestamp, pointInTimeToTimestamp
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_SUL/subjects")
  @ApiOperation(httpMethod = "POST", value = "Get subjects lite", nickname = "getSubjectsLite", //response = .class,
  notes = "<b>Description</b>: Get subjects will retrieve subject objects by subject lookups (source (optional), id or identifier), or by search string (free-form string that sources can search on), and optionally a list of sources to narrow the search"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Subjects'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "groupUuid", dataType = "String", paramType = "form", 
    value = "Id in UI, groupUuid search by group uuid (must match exactly)", example = "abc123"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "sourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subject to be found", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "Identifier of the subject to be found, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "Id of subject to be found, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "fieldName", dataType = "String", paramType = "form", 
    value = "If the entity added to a certain field membership of the group", example = "members, optin, optout, read, admin, update, view, groupAttrRead, groupAttrUpdate"),
    @ApiImplicitParam(required = false, name = "searchString", dataType = "String", paramType = "form", 
    value = "free form string query to find a list of subjects (exact behavior depends on source)", example = "john smith"),
    @ApiImplicitParam(required = false, name = "sourceIds", dataType = "String", paramType = "form", 
    value = "are comma separated sourceIds for a searchString", example = "schoolPerson, g:gsa"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). "
        + "Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "groupName", dataType = "String", paramType = "form", 
    value = "groupName search by group name (must match exactly), cannot use other params with this", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "wsMemberFilter", dataType = "String", paramType = "form", 
    value = "can be All(default), Effective (non immediate), Immediate (direct),Composite (if composite group with group math (union, minus,etc)", example = "Effective"),
  })
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
   * @param theValue value assigned to an attribute that you are searching for
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_ABL/attributeAssignments")
  @ApiOperation(httpMethod = "POST", value = "Get attribute assignments lite", nickname = "getAttributeAssignmentsLite", //response = .class,
  notes = "<b>Description</b>: Get attribute assignments.  These attributes can be on groups, stems, members, memberships (immediate or any), or attribute definitions.  If you want to retrieve attribute assignments assigned to other attributes, then pass a flag to the assignment lookup to include assignments on the returned assignments."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Attribute+Assignments'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "enabled", dataType = "String", paramType = "form", 
    value = "enabled is A for all, T or null for enabled only, F for disabled", example = "A|T|F"),
    @ApiImplicitParam(required = false, name = "wsOwnerGroupName", dataType = "String", paramType = "form", 
    value = "is name of the group to look in", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "wsOwnerGroupId", dataType = "String", paramType = "form", 
    value = "is id of the group to look in", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerStemName", dataType = "String", paramType = "form", 
    value = "is name of the stem to look in", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "wsOwnerStemId", dataType = "String", paramType = "form", 
    value = "is id of the stem to look in", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefName", dataType = "String", paramType = "form", 
    value = "find assignments in this attribute def", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefId", dataType = "String", paramType = "form", 
    value = "find assignments in this attribute def", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameId", dataType = "String", paramType = "form", 
    value = "attribute def Uuid to assign to the owner, mutually exclusive with wsAttributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "attributeAssignType", dataType = "String", paramType = "form", 
    value = "Type of owner, from enum AttributeAssignType, e.g.group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn,stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn ", example = "group"),
    @ApiImplicitParam(required = false, name = "attributeAssignId", dataType = "String", paramType = "form", 
    value = "if you know the assign id you want, put it here", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerSubjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of subject to look in", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "wsOwnerSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of the subject to look in, mutually exclusive with wsOwnerSubjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "wsOwnerSubjectId", dataType = "String", paramType = "form", 
    value = "subjectId of subject to look in, mutually exclusive with wsOwnerSubjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnySubjectIdentifier", dataType = "String", paramType = "form", 
    value = "to query attributes in \"any\" membership which is on immediate or effective membership", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipId", dataType = "String", paramType = "form", 
    value = "to query attributes on immediate membership", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnyGroupName", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "this:group:name"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnyGroupId", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnySubjectId", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnySubjectSourceId", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "myInsitutionPeople"),
    @ApiImplicitParam(required = false, name = "wsOwnerAttributeDefName", dataType = "String", paramType = "form", 
    value = "to query attributes assigned on attribute def", example = "a:b:c:myAttributeName"),
    @ApiImplicitParam(required = false, name = "wsOwnerAttributeDefId", dataType = "String", paramType = "form", 
    value = "to query attributes assigned on attribute def", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "action", dataType = "String", paramType = "form", 
    value = "action to query, or none to query all actions", example = "action"),
    @ApiImplicitParam(required = false, name = "includeAssignmentsOnAssignments", dataType = "String", paramType = "form", 
    value = "if this is not querying assignments on assignments directly, but the assignmentsand assignments on those assignments should be returned, enter true. default to false.", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). "
        + "Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "attributeDefValueType", dataType = "String", paramType = "form", 
    value = "required if sending theValue, can be:floating, integer, memberId, string, timestamp", example = "integer"),
    @ApiImplicitParam(required = false, name = "theValue", dataType = "String", paramType = "form", 
    value = "value assigned to an attribute that you are searching for", example = "myValue"),
    @ApiImplicitParam(required = false, name = "includeAssignmentsFromAssignments", dataType = "String", paramType = "form", 
    value = "T|F if you are finding an assignment that is an assignmentOnAssignment,then get the assignment which tells you the owner as well", example = "T|F"),
    @ApiImplicitParam(required = false, name = "attributeDefType", dataType = "String", paramType = "form", 
    value = "null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm", example = "attr"),
    @ApiImplicitParam(required = false, name = "wsAssignAssignOwnerAttributeAssignId", dataType = "String", paramType = "form", 
    value = "if looking for assignments on assignments, this is the assignment the assignment is assigned to", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAssignAssignOwnerIdOfAttributeDef", dataType = "String", paramType = "form", 
    value = "if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAssignAssignOwnerNameOfAttributeDef", dataType = "String", paramType = "form", 
    value = "if looking for assignments on assignments, this is the attribute definition of the assignment the assignment is assigned to", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsAssignAssignOwnerIdOfAttributeDefName", dataType = "String", paramType = "form", 
    value = "if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAssignAssignOwnerNameOfAttributeDefName", dataType = "String", paramType = "form", 
    value = "if looking for assignments on assignments, this is the attribute def name of the assignment the assignment is assigned to", example = "a:b:c:myAttributeName"),
    @ApiImplicitParam(required = false, name = "wsAssignAssignOwnerAction", dataType = "String", paramType = "form", 
    value = "if looking for assignments on assignments, this is the action of the assignment the assignment is assigned to", example = "canLogin"),
 
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_ABL/attributeAssignActions")
  @ApiOperation(httpMethod = "POST", value = "Get attribute assign actions lite", nickname = "getAttributeAssignActionsLite", //response = .class,
  notes = "<b>Description</b>: Get attribute assign actions will give you the permission actions associated with a Permission Definition (AttributeDef).  This service is available in version v2.3.0+."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Attribute+Assign+Actions'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "action", dataType = "String", paramType = "form", 
    value = "action to query, or none to query all actions", example = "read"),
    @ApiImplicitParam(required = false, name = "wsNameOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find assignActions in this attribute def", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsIdOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find assignActions in this attribute def", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsIdIndexOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find assignActions in this attribute def", example = "10009"),
    
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_E_ATL/attributeAssignments")
  @ApiOperation(httpMethod = "POST", value = "Assign attributes lite", nickname = "assignAttributesLite", //response = .class,
  notes = "<b>Description</b>: Assign or remove attributes and values of attribute assignments.  These attributes can be on groups, stems, members, memberships (immediate or any), attribute definitions, or on assignments of attributes (one level deep)."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Assign+Attributes'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "delegatable", dataType = "String", paramType = "form", 
    value = "really only for permissions, if the assignee can delegate to someone else. TRUE|FALSE|GRANT. defaults to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "attributeAssignType", dataType = "String", paramType = "form", 
    value = "Type of owner, from enum AttributeAssignType, e.g.group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn,stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn ", example = "group"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameId", dataType = "String", paramType = "form", 
    value = "attribute def Uuid to assign to the owner, mutually exclusive with wsAttributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "attributeAssignOperation", dataType = "String", paramType = "form", 
    value = "operation to perform for attribute on owners, from enum AttributeAssignOperationassign_attr, add_attr, remove_attr", example = "add_attr"),
    @ApiImplicitParam(required = false, name = "attributeAssignValueOperation", dataType = "String", paramType = "form", 
    value = "operation to perform for attribute value on attributeassignments: assign_value, add_value, remove_value, replace_values", example = "assign_value"),
    @ApiImplicitParam(required = false, name = "wsAttributeAssignId", dataType = "String", paramType = "form", 
    value = "if you know the assign id you want, put id here", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerGroupName", dataType = "String", paramType = "form", 
    value = "is name of the group to look in", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "wsOwnerGroupId", dataType = "String", paramType = "form", 
    value = "is id of the group to look in", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerStemName", dataType = "String", paramType = "form", 
    value = "is name of the stem to look in", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "wsOwnerStemId", dataType = "String", paramType = "form", 
    value = "is id of the stem to look in", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). "
        + "Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "valueId", dataType = "String", paramType = "form", 
    value = "If removing, and id is specified, will only remove values with that id", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "valueSystem", dataType = "String", paramType = "form", 
    value = "is value to add, assign, remove, etc", example = "myValue"),
    @ApiImplicitParam(required = false, name = "valueFormatted", dataType = "String", paramType = "form", 
    value = "is value to add, assign, remove, etc though not implemented yet", example = "myValue"),
    @ApiImplicitParam(required = false, name = "assignmentNotes", dataType = "String", paramType = "form", 
    value = "notes on the assignment (optional)", example = ""),
    @ApiImplicitParam(required = false, name = "assignmentEnabledTime", dataType = "String", paramType = "form", 
    value = "enabled time, or null for enabled now", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "assignmentDisabledTime", dataType = "String", paramType = "form", 
    value = "disabled time, or null for not disabled", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "action", dataType = "String", paramType = "form", 
    value = "to assign, or rescind assign is the default if blank", example = "assign"),
    @ApiImplicitParam(required = false, name = "wsOwnerSubjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of subject to look in", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "wsOwnerSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of the subject to look in, mutually exclusive with wsOwnerSubjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "wsOwnerSubjectId", dataType = "String", paramType = "form", 
    value = "subjectId of subject to look in, mutually exclusive with wsOwnerSubjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipId", dataType = "String", paramType = "form", 
    value = "to query attributes on immediate membership", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnyGroupName", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "this:group:name"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnyGroupId", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnySubjectId", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnySubjectSourceId", dataType = "String", paramType = "form", 
    value = "to query attributes in 'any' membership which is on immediate or effective membership", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "wsOwnerAttributeDefName", dataType = "String", paramType = "form", 
    value = "to query attributes assigned on attribute def", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsOwnerAttributeDefId", dataType = "String", paramType = "form", 
    value = "to query attributes assigned on attribute def", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsOwnerAttributeAssignId", dataType = "String", paramType = "form", 
    value = "for assignment on assignment", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "wsOwnerMembershipAnySubjectIdentifier", dataType = "String", paramType = "form", 
    value = "to query attributes in \"any\" membership which is on immediate or effective membership", example = "12345678"),
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_PAL/permissionAssignments")
  @ApiOperation(httpMethod = "POST", value = "Get permission assignments lite", nickname = "getPermissionAssignmentsLite", //response = .class,
  notes = "<b>Description</b>: Get permission assignments.  These permissions can be on roles or subjects (note if assignment is assigned directly to a subject, it is in the context of a role)."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Permission+Assignments'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includePermissionDetail", dataType = "String", paramType = "form", 
    value = "T or F for if the permission details should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "action", dataType = "String", paramType = "form", 
    value = "to assign, or assign is the default if blank", example = "assign"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameId", dataType = "String", paramType = "form", 
    value = "attribute def Uuid to assign to the owner, mutually exclusive with wsAttributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefName", dataType = "String", paramType = "form", 
    value = "find assignments in this attribute def", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefId", dataType = "String", paramType = "form", 
    value = "find assignments in this attribute def", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "roleName", dataType = "String", paramType = "form", 
    value = "Id of role to look in", example = "a:b:c:powerUsers"),
    @ApiImplicitParam(required = false, name = "roleId", dataType = "String", paramType = "form", 
    value = "Name of role to look in", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource to look in", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "wsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "Identifier of subject to look in", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "wsSubjectId", dataType = "String", paramType = "form", 
    value = "Id of subject to look in", example = "12345678"),
    @ApiImplicitParam(required = false, name = "includeAttributeDefNames", dataType = "String", paramType = "form", 
    value = "T or F for if attributeDefName objects should be returned", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeAssignmentsOnAssignments", dataType = "String", paramType = "form", 
    value = "if this is not querying assignments on assignments directly, but the assignmentsand assignments on those assignments should be returned, enter true. default to false.", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "includeAttributeAssignments", dataType = "String", paramType = "form", 
    value = "T or F for if attributeDefName objects should be returned", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "enabled", dataType = "String", paramType = "form", 
    value = "enabled is A for all, T or null for enabled only, F for disabled", example = "A|T|F"),
    @ApiImplicitParam(required = false, name = "pointInTimeFrom", dataType = "String", paramType = "form", 
    value = "To query permissions at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "pointInTimeTo", dataType = "String", paramType = "form", 
    value = "To query permissions at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "immediateOnly", dataType = "String", paramType = "form", 
    value = "T of F (defaults to F) if we should filter out non immediate permissions", example = "T|F"),
    @ApiImplicitParam(required = false, name = "permissionType", dataType = "String", paramType = "form", 
    value = "are we looking for role permissions or subject permissions? fromenum PermissionType: role, or role_subject. defaults to role_subject permissions", example = "role"),
    @ApiImplicitParam(required = false, name = "permissionProcessor", dataType = "String", paramType = "form", 
    value = "if we should find the best answer, or process limits, etc. From the enumPermissionProcessor. example values are: FILTER_REDUNDANT_PERMISSIONS,FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS, "
        + "FILTER_REDUNDANT_PERMISSIONS_AND_ROLES,FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS, PROCESS_LIMITS", example = "PROCESS_LIMITS"),
    @ApiImplicitParam(required = false, name = "limitEnvVarName0", dataType = "String", paramType = "form", 
    value = "limitEnvVars if processing limits, pass in a set of limits. The name is thename of the variable, "
        + "and the value is the value. Note, you can typecast thevalues by putting a valid type in parens in front of the param name", example = "(int)amount"),
    @ApiImplicitParam(required = false, name = "limitEnvVarValue0", dataType = "String", paramType = "form", 
    value = "first limit env var value", example = "50"),
    @ApiImplicitParam(required = false, name = "limitEnvVarType0", dataType = "String", paramType = "form", 
    value = "first limit env var type", example = "int"),
    @ApiImplicitParam(required = false, name = "limitEnvVarName1", dataType = "String", paramType = "form", 
    value = "second limit env var name", example = "amount"),
    @ApiImplicitParam(required = false, name = "limitEnvVarValue1", dataType = "String", paramType = "form", 
    value = "second limit env var value", example = "50"),
    @ApiImplicitParam(required = false, name = "limitEnvVarType1", dataType = "String", paramType = "form", 
    value = "second limit env var type", example = "int"),
    @ApiImplicitParam(required = false, name = "includeLimits", dataType = "String", paramType = "form", 
    value = "T or F (default to F) for if limits should be returned with the results.Note that the attributeDefs, attributeDefNames, and attributeAssignments will be added to those lists", example = "T|F")
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_H_PRL/permissionAssignments")
  @ApiOperation(httpMethod = "POST", value = "Assign permissions lite", nickname = "assignPermissionsLite", //response = .class,
  notes = "<b>Description</b>: Assign or remove permissions.  These permissions can be on roles or subjects (in the context of a role)."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Assign+Permissions'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "includeGroupDetail", dataType = "String", paramType = "form", 
    value = "If the group detail should be returned, default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "permissionType", dataType = "String", paramType = "form", 
    value = "is role or role_subject from the PermissionType enum", example = "role"),
    @ApiImplicitParam(required = true, name = "permissionDefNameName", dataType = "String", paramType = "form", 
    value = "attribute def name to assign to the owner", example = "a:b:c:myPermissionDef"),
    @ApiImplicitParam(required = true, name = "permissionDefNameId", dataType = "String", paramType = "form", 
    value = "attribute def id to assign to the owner", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "permissionAssignmentOperation", dataType = "String", paramType = "form", 
    value = "operation to perform for permission on role or subject, from enum PermissionAssignOperation: assign_permission, remove_permission", example = "assign_permission"),
    @ApiImplicitParam(required = false, name = "assignmentNotes", dataType = "String", paramType = "form", 
    value = "notes on the assignment (optional)", example = ""),
    @ApiImplicitParam(required = false, name = "assignmentEnabledTime", dataType = "String", paramType = "form", 
    value = "enabled time, or null for enabled now", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "assignmentDisabledTime", dataType = "String", paramType = "form", 
    value = "disabled time, or null for not disabled", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "delegatable", dataType = "String", paramType = "form", 
    value = "really only for permissions, if the assignee can delegate to someone else. TRUE|FALSE|GRANT. defaults to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "wsAttributeAssignId", dataType = "String", paramType = "form", 
    value = "if you know the assign id you want, put id here. lookup to remove etc", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "roleName", dataType = "String", paramType = "form", 
    value = "is name of group to assign to for permissionType 'role'", example = "member"),
    @ApiImplicitParam(required = false, name = "roleId", dataType = "String", paramType = "form", 
    value = "is id of group to assign to for permissionType 'role'", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "subjectRoleName", dataType = "String", paramType = "form", 
    value = "is role name if assigning to subject, in the context of a role (for permissionType \"subject_role\")", example = "member"),
    @ApiImplicitParam(required = false, name = "subjectRoleId", dataType = "String", paramType = "form", 
    value = "is role id if assigning to subject, in the context of a role (for permissionType \"subject_role\")", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "subjectRoleSubjectSourceId", dataType = "String", paramType = "form", 
    value = "is subject source id if assigning to subject, in the context of a role (for permissionType \"subject_role\")", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectRoleSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "is subject identifier if assigning to subject, in the context of a role (for permissionType \"subject_role\")", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectRoleSubjectId", dataType = "String", paramType = "form", 
    value = "is subject id if assigning to subject, in the context of a role (for permissionType \"subject_role\")", example = "12345678"),
    @ApiImplicitParam(required = false, name = "action", dataType = "String", paramType = "form", 
    value = "to assign, or assign is the default if blank", example = "assign"),
    @ApiImplicitParam(required = false, name = "includeSubjectDetail", dataType = "String", paramType = "form", 
    value = "If the subject detail should be returned (anything more than ID), default to false", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectAttributeNames", dataType = "String", paramType = "form", 
    value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName"),
    @ApiImplicitParam(required = false, name = "disallowed", dataType = "String", paramType = "form", 
    value = "T or F if the permission is disallowed", example = "T"),
  })
  
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_D_ANL/attributeDefNames")
  @ApiOperation(httpMethod = "POST", value = "Assign attribute def name inheritance lite", nickname = "assignAttributeDefNameInheritanceLite", //response = .class,
  notes = "<b>Description</b>: Assign attribute definition name inheritance based on lookups by name or ID. This is new as of Grouper v2.1.  Note: attribute definition name inheritance is only used for permissions (e.g. if the permission names are an org chart there would be inheritance)"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Assign+Attribute+Definition+Name+Inheritance'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = true, name = "assign", dataType = "String", paramType = "form", 
    value = "T to assign, F to remove assingment", example = "F"),
    @ApiImplicitParam(required = true, name = "attributeDefNameUuid", dataType = "String", paramType = "form", 
    value = "Id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = true, name = "attributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = true, name = "relatedAttributeDefNameUuid", dataType = "String", paramType = "form", 
    value = "id of attribute def name to add or remove from inheritance from the container, mutually exclusive with relatedAttributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = true, name = "relatedAttributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attribute def name to add or remove from inheritance from the container, mutually exclusive with relatedAttributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef")
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_L_SAL/attributeDefs")
  @ApiOperation(httpMethod = "POST", value = "Attribute def save lite", nickname = "attributeDefSaveLite", //response = .class,
  notes = "<b>Description</b>: Add or edit attribute definitions based on name or ID. This is new as of Grouper v2.3.0"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Attribute+Definition+Save'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "attributeDefLookupUuid", dataType = "String", paramType = "form", 
    value = "to lookup the attributeDef (mutually exclusive with attributeDefName)", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "attributeDefLookupName", dataType = "String", paramType = "form", 
    value = "to lookup the attributeDef (mutually exclusive with attributeDefUuid)", example = "some:folder:attributes:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "createParentStemsIfNotExist", dataType = "String", paramType = "form", 
    value = "T or F (default F) if parent stems should be created if not exist", example = "T|F"),
    @ApiImplicitParam(required = false, name = "saveMode", dataType = "String", paramType = "form", 
    value = "if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)", example = "INSERT"),
    @ApiImplicitParam(required = false, name = "description", dataType = "String", paramType = "form", 
    value = "of the attributeDef, empty will be ignored", example = ""),
    @ApiImplicitParam(required = false, name = "valueType", dataType = "String", paramType = "form", 
    value = "what type of value on assignments: AttributeDefValueType: e.g. integer, timestamp, string, floating, marker, memberId", example = "marker"),
    @ApiImplicitParam(required = false, name = "multiValued", dataType = "String", paramType = "form", 
    value = "T or F, if has values, if can assign multiple values to one assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "multiAssignable", dataType = "String", paramType = "form", 
    value = "T of F for if can be assigned multiple times to one object", example = "T|F"),
    @ApiImplicitParam(required = false, name = "attributeDefType", dataType = "String", paramType = "form", 
    value = "null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm", example = "attr"),
    @ApiImplicitParam(required = false, name = "assignToStemAssignment", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to a stem assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToStem", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to a stem", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToMemberAssignment", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to a member assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToMember", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to a member", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToImmediateMembershipAssignment", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to an immediate membership assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToImmediateMembership", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to an immediate membership", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToGroupAssignment", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to a group assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToGroup", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to a group", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToEffectiveMembershipAssignment", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to an effective membership assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToEffectiveMembership", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to an effective membership", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToAttributeDefAssignment", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to an attribute def assignment", example = "T|F"),
    @ApiImplicitParam(required = false, name = "assignToAttributeDef", dataType = "String", paramType = "form", 
    value = "T|F if can assign this attribute to an attribute def", example = "T|F"),
    @ApiImplicitParam(required = false, name = "uuidOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find attribute defs associated with this attribute def uuid, mutually exclusive with nameOfAttributeDef", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "nameOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find attribute defs associated with this attribute def name, mutually exclusive with idOfAttributeDef", example = "a:b:c:myAttributeDef"),
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_I_DEL/attributeDefs")
  @ApiOperation(httpMethod = "POST", value = "Attribute def delete lite", nickname = "attributeDefDeleteLite", //response = .class,
  notes = "<b>Description</b>: Delete attribute definitions based on name or ID. This is new as of Grouper v2.3.0"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Attribute+Definition+Delete'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "wsNameOfAttributeDef", dataType = "String", paramType = "form", 
    value = "name of attribute def to be deleted", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsIdOfAttributeDef", dataType = "String", paramType = "form", 
    value = "Id of attribute def to be deleted", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsIdIndexOfAttributeDef", dataType = "String", paramType = "form", 
    value = "Id index of attribute def to be deleted", example = "10009"),
  })
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
   * @return the attribute defs, or no attribute def if none found
   */
  public WsFindAttributeDefsResults findAttributeDefs(final String clientVersion,
      String scope, String splitScope, WsAttributeDefLookup[] wsAttributeDefLookups,
      String privilegeName,
      String stemScope, String parentStemId, String findByUuidOrName,
      String pageSize, String pageNumber,
      String sortString, String ascending, 
      WsSubjectLookup actAsSubjectLookup,
      WsParam[] params, 
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {

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
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      StemScope stemScopeEnum = StemScope.valueOfIgnoreCase(stemScope);

      wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefs(
          grouperWsVersion,
          scope, splitScopeBoolean, wsAttributeDefLookups,
          privilegeName,
          stemScopeEnum, parentStemId, findByUuidOrNameBoolean,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          actAsSubjectLookup, params,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);

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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
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
  @POST
  @Path("/grouper-ws/servicesRest/vF_A_DSL/attributeDefs")
  @ApiOperation(httpMethod = "POST", value = "Find attribute defs lite", nickname = "findAttributeDefsLite", //response = .class,
  notes = "<b>Description</b>: Find attribute definitions based on name or ID or other criteria. This is new as of Grouper v2.3.0"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Find+Attribute+Definitions'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging", example = "1"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
    value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
    example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "privilegeName", dataType = "String", paramType = "form", 
    value = "Name of the privilege", example = "for groups: read, view, update, admin, optin, optout, groupAttrRead, groupAttrUpdate.  for stems: create, stemAttrRead, stemAdmin, stemView, stemAttrUpdate"),
    @ApiImplicitParam(required = false, name = "scope", dataType = "String", paramType = "form", 
    value = "search string with % as wildcards will search name, display name, description", example = "someApp someAttributeDefExtension"),
    @ApiImplicitParam(required = false, name = "splitScope", dataType = "String", paramType = "form", 
    value = "T or F, if T will split the scope by whitespace, and find attribute def names with each token.e.g. if you have a scope of \"pto permissions\", and split scope T, "
        + "it will returnschool:apps:pto_app:internal:the_permissions:whatever", example = "T|F"),
    @ApiImplicitParam(required = false, name = "uuidOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find attribute defs associated with this attribute def uuid, mutually exclusive with nameOfAttributeDef", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "nameOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find attribute defs associated with this attribute def name, mutually exclusive with idOfAttributeDef", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "idIndexOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find attribute defs associated with this attribute def id index", example = "10009"),
    @ApiImplicitParam(required = false, name = "stemScope", dataType = "String", paramType = "form", 
    value = "is if in this stem, or in any stem underneath. You must pass stemScope if you pass a stem", example = "this:stem:name"),
    @ApiImplicitParam(required = false, name = "parentStemId", dataType = "String", paramType = "form", 
    value = "will return attribute defs in this stem", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "findByUuidOrName", dataType = "String", paramType = "form", 
    value = "True for Uuid, false for name, defaults to name", example = "T|F"),
  })
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
      String paramValue0, String paramName1, String paramValue1,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {

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
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      StemScope stemScopeEnum = StemScope.valueOfIgnoreCase(stemScope);

      wsFindAttributeDefsResults = GrouperServiceLogic.findAttributeDefsLite(
          grouperWsVersion,
          scope, splitScopeBoolean, uuidOfAttributeDef, nameOfAttributeDef,
          idIndexOfAttributeDef, privilegeName,
          stemScopeEnum, parentStemId, findByUuidOrNameBoolean,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0,
          paramValue0, paramName1, paramValue1,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);

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
  @POST
  @Path("/grouper-ws/servicesRest/vA_J_DEL/attributeDefNames")
  @ApiOperation(httpMethod = "POST", value = "Attribute def name delete lite", nickname = "attributeDefNameDeleteLite", //response = .class,
  notes = "<b>Description</b>: Delete attribute definition names based on name or ID. This is new as of Grouper v2.1"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Attribute+Definition+Name+Delete'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = true, name = "attributeDefNameUuid", dataType = "String", paramType = "form", 
    value = "Id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = true, name = "attributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef"),
  })
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
  @POST
  @Path("/grouper-ws/servicesRest/vA_K_SAL/attributeDefNames")
  @ApiOperation(httpMethod = "POST", value = "Attribute def name save lite", nickname = "attributeDefNameSaveLite", //response = .class,
  notes = "<b>Description</b>: Add or edit attribute definition names based on name or ID. This is new as of Grouper v2.1"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Attribute+Definition+Name+Save'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "attributeDefNameLookupUuid", dataType = "String", paramType = "form", 
    value = "Id of attributeDefName to edit, which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameLookupName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "attributeDefNameLookupName", dataType = "String", paramType = "form", 
    value = "Lookup name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameLookupUuId", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "attributeDefLookupUuid", dataType = "String", paramType = "form", 
    value = "to lookup the attributeDef (mutually exclusive with attributeDefName)", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "attributeDefLookupName", dataType = "String", paramType = "form", 
    value = "to lookup the attributeDef (mutually exclusive with attributeDefUuid)", example = "some:folder:attributes:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "attributeDefNameUuid", dataType = "String", paramType = "form", 
    value = "Id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "attributeDefNameName", dataType = "String", paramType = "form", 
    value = "name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "saveMode", dataType = "String", paramType = "form", 
    value = "if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)", example = "INSERT"),
    @ApiImplicitParam(required = false, name = "displayExtension", dataType = "String", paramType = "form", 
    value = "display name of the attributeDefName, empty will be ignored", example = "My Attribute Name"),
    @ApiImplicitParam(required = false, name = "description", dataType = "String", paramType = "form", 
    value = "of the attributeDefName, empty will be ignored", example = ""),
    @ApiImplicitParam(required = false, name = "createParentStemsIfNotExist", dataType = "String", paramType = "form", 
    value = "T or F (default F) if parent stems should be created if not exist", example = "T|F"),
  })
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
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
      WsSubjectLookup wsSubjectLookup, String serviceRole,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {

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

      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);
      
      AttributeDefType attributeDefTypeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(AttributeDefType.class,attributeDefType, false);
      AttributeAssignType attributeAssignTypeEnum = GrouperServiceUtils.enumValueOfIgnoreCase(AttributeAssignType.class, attributeAssignType, false);
      WsInheritanceSetRelation wsInheritanceSetRelationEnum = WsInheritanceSetRelation.valueOfIgnoreCase(wsInheritanceSetRelation);

      ServiceRole serviceRoleEnum = GrouperServiceUtils.enumValueOfIgnoreCase(ServiceRole.class, serviceRole, false);
      
      wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNames(grouperWsVersion,
          scope, splitScopeBoolean, wsAttributeDefLookup,
          attributeAssignTypeEnum, attributeDefTypeEnum,
          wsAttributeDefNameLookups, pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          wsInheritanceSetRelationEnum, actAsSubjectLookup, params, wsSubjectLookup,
          serviceRoleEnum, pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);
  
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
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
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
  @POST
  @Path("/grouper-ws/servicesRest/vF_A_DNL/attributeDefNames")
  @ApiOperation(httpMethod = "POST", value = "Find attribute def names lite", nickname = "findAttributeDefNamesLite", //response = .class,
  notes = "<b>Description</b>: Find attribute definition names based on name, search filter, permission name inheritance, etc. This is new as of Grouper v2.1"
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Find+Attribute+Definition+Names'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageNumber", dataType = "String", paramType = "form", 
    value = "Page number 1 indexed if paging", example = "1"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
    value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
    example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "subjectSourceId", dataType = "String", paramType = "form", 
    value = "the Id of the subjectSource of the entity", example = "schoolPerson"),
    @ApiImplicitParam(required = false, name = "subjectIdentifier", dataType = "String", paramType = "form", 
    value = "subjectIdentifier of entity, mutually exclusive with subjectId, one of the two is required", example = "subjIdent0"),
    @ApiImplicitParam(required = false, name = "subjectId", dataType = "String", paramType = "form", 
    value = "subjectId of entity, mutually exclusive with subjectIdentifier, one of the two is required", example = "12345678"),
    @ApiImplicitParam(required = false, name = "wsInheritanceSetRelation", dataType = "String", paramType = "form", 
    value = "if there is one wsAttributeDefNameLookup, and this is specified, then findthe attribute def names which are related to the lookup by this relation,"
        + " e.g. IMPLIED_BY_THIS,IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE", example = "IMPLIED_BY_THIS"),
    @ApiImplicitParam(required = false, name = "scope", dataType = "String", paramType = "form", 
    value = "is a DB pattern that will have % appended to it, or null for all", example = "school:whatever:parent"),
    @ApiImplicitParam(required = false, name = "splitScope", dataType = "String", paramType = "form", 
    value = "T or F, if T will split the scope by whitespace, and find attribute def names with each token.e.g. if you have a scope of \"pto permissions\", "
        + "and split scope T, it will return school:apps:pto_app:internal:the_permissions:whatever", example = "T|F"),
    @ApiImplicitParam(required = false, name = "uuidOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find names associated with this attribute definition, mutually exclusive with nameOfAttributeDef", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "nameOfAttributeDef", dataType = "String", paramType = "form", 
    value = "find names associated with this attribute definition, mutually exclusive with idOfAttributeDef", example = "a:b:c:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "attributeAssignType", dataType = "String", paramType = "form", 
    value = "where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def,attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn ", example = "group"),
    @ApiImplicitParam(required = false, name = "attributeDefType", dataType = "String", paramType = "form", 
    value = "null for all, or specify an AttributeDefType e.g. attr, limit, service, type, limit, perm", example = "attr"),
    @ApiImplicitParam(required = true, name = "attributeDefNameUuid", dataType = "String", paramType = "form", 
    value = "to lookup an attribute def name by id, mutually exclusive with attributeDefNameName", example = "a1b2c3d4"),
    @ApiImplicitParam(required = true, name = "attributeDefNameName", dataType = "String", paramType = "form", 
    value = "to lookup an attribute def name by name, mutually exclusive with attributeDefNameName", example = "some:folder:attributes:nameOfMyAttributeDef"),
    @ApiImplicitParam(required = false, name = "serviceRole", dataType = "String", paramType = "form", 
    value = "to filter attributes that a user has a certain role", example = "member"),
  })
  public WsFindAttributeDefNamesResults findAttributeDefNamesLite(final String clientVersion,
      String scope, String splitScope, String uuidOfAttributeDef, String nameOfAttributeDef,
      String attributeAssignType, String attributeDefType, String attributeDefNameUuid, String attributeDefNameName,
      String pageSize, String pageNumber,
      String sortString, String ascending,
      String wsInheritanceSetRelation,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1,
      String subjectId, String subjectSourceId,
      String subjectIdentifier, String serviceRole,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved) {
        
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

      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);
      
      ServiceRole serviceRoleEnum = GrouperServiceUtils.enumValueOfIgnoreCase(ServiceRole.class, serviceRole, false);

      wsFindAttributeDefNamesResults = GrouperServiceLogic.findAttributeDefNamesLite(grouperWsVersion,
          scope, splitScopeBoolean, uuidOfAttributeDef, nameOfAttributeDef,
          attributeAssignTypeEnum, attributeDefTypeEnum, attributeDefNameUuid, attributeDefNameName,
          pageSizeInteger, pageNumberInteger, sortString, ascendingBoolean,
          wsInheritanceSetRelationEnum, actAsSubjectId, actAsSubjectSourceId,
          actAsSubjectIdentifier, paramName0,
          paramValue0, paramName1, paramValue1, subjectId, subjectSourceId,
          subjectIdentifier, serviceRoleEnum,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean);

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
   * @param exchangeType
   * @param queueArguments
   * @param autocreateObjects
   * @param messages
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message send call
   */
  public WsMessageResults sendMessage(final String clientVersion,
      String queueType, String queueOrTopicName, String messageSystemName,
      String routingKey, String exchangeType, Map<String, Object> queueArguments,
      String autocreateObjects, WsMessage[] messages,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {

    WsMessageResults wsSendMessageResults = new WsMessageResults();

    GrouperVersion grouperWsVersion = null;

    try {
      
      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      GrouperMessageQueueType messageQueueType = GrouperMessageQueueType
          .valueOfIgnoreCase(queueType, true);

      Boolean autocreateObjectsBoolean = GrouperUtil.booleanObjectValue(autocreateObjects);
      
      wsSendMessageResults = GrouperServiceLogic.sendMessage(grouperWsVersion,
          messageQueueType, queueOrTopicName, messageSystemName, routingKey, exchangeType,
          queueArguments, autocreateObjectsBoolean, messages, actAsSubjectLookup, params);

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
   * @param queueArguments
   * @param autocreateObjects
   * @param blockMillis - the millis to block waiting for messages, max of 20000 (optional)
   * @param maxMessagesToReceiveAtOnce - max number of messages to receive at once, though can't be more than the server maximum (optional)
   * @param actAsSubjectLookup
   * @param params
   * @return the results of message receive call
   */
  public WsMessageResults receiveMessage(final String clientVersion,
      String queueType, String queueOrTopicName, String messageSystemName,
      String routingKey, String exchangeType, Map<String, Object> queueArguments,
      final String autocreateObjects, final String blockMillis, final String maxMessagesToReceiveAtOnce,
      WsSubjectLookup actAsSubjectLookup, WsParam[] params) {

    WsMessageResults wsReceiveMessageResults = new WsMessageResults();

    GrouperVersion grouperWsVersion = null;

    try {

      grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      GrouperMessageQueueType messageQueueType = null;
      if (StringUtils.isNotBlank(queueType)) {
        messageQueueType = GrouperMessageQueueType.valueOfIgnoreCase(queueType, true);
      }

      Integer blockMillisInteger = GrouperUtil.intObjectValue(blockMillis, true);
      Integer maxMessagesToReceiveAtOnceInteger = GrouperUtil.intObjectValue(maxMessagesToReceiveAtOnce, true);
      
      Boolean autocreateObjectsBoolean = GrouperUtil.booleanObjectValue(autocreateObjects);

      wsReceiveMessageResults = GrouperServiceLogic.receiveMessage(grouperWsVersion,
          messageQueueType, queueOrTopicName, messageSystemName, routingKey, exchangeType, queueArguments,
          autocreateObjectsBoolean, blockMillisInteger, maxMessagesToReceiveAtOnceInteger, actAsSubjectLookup, params);

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
  
 
  /**
   * get audit entries
   * @param clientVersion
   * @param actAsSubjectId
   * @param actAsSubjectSourceId
   * @param actAsSubjectIdentifier
   * @param auditType
   * @param auditActionId
   * @param wsGroupName
   * @param wsGroupId
   * @param wsStemName
   * @param wsStemId
   * @param wsAttributeDefName
   * @param wsAttributeDefId
   * @param wsAttributeDefNameName
   * @param wsAttributeDefNameId
   * @param wsSubjectId
   * @param wsSubjectSourceId
   * @param wsSubjectIdentifier
   * @param actionsPerformedByWsSubjectId
   * @param actionsPerformedByWsSubjectSourceId
   * @param actionsPerformedByWsSubjectIdentifier
   * @param paramName0
   * @param paramValue0
   * @param paramName1
   * @param paramValue1
   * @param pageSize
   * @param sortString
   * @param ascending
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return audit entries result
   */
  @POST
  @Path("/grouper-ws/servicesRest/vG_E_AEL/audits")
  @ApiOperation(httpMethod = "POST", value = "Get audit entries lite", nickname = "getAuditEntriesLite", //response = .class,
  notes = "<b>Description</b>: Get audit entries for groups, stems, and subjects. Available in Grouper v2.5 or later."
      + "<br />See documentation on the <a href='https://spaces.at.internet2.edu/display/Grouper/Get+Audit+Entries'>wiki</a> and go to samples to see requests and responses") 
  @ApiImplicitParams({
    @ApiImplicitParam(required = true, name = "wsLiteObjectType", dataType = "String", paramType = "form", 
        value = "WsRestFindGroupsLiteRequest", example = "WsRestFindGroupsLiteRequest"),
    @ApiImplicitParam(required = false, name = "clientVersion", dataType = "String", paramType = "form", 
        value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001"),
    @ApiImplicitParam(required = false, name = "actAsSubjectId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectId to act as here.  Mutually exclusive with actAsSubjectIdentifier (actAsSubjectId is preferred)", example = "12345678"),
    @ApiImplicitParam(required = false, name = "actAsSubjectSourceId", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), "
        + "specify the subject source ID (get this from the UI or your Grouper admin)", example = "myInstitutionPeople"),
    @ApiImplicitParam(required = false, name = "actAsSubjectIdentifier", dataType = "String", paramType = "form", 
    value = "If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user "
        + "subjectIdentifier to act as here.  Mutually exclusive with actAsSubjectId (preferred)", example = "jsmith"),
    @ApiImplicitParam(required = false, name = "paramName0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue0", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramName1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "paramValue1", dataType = "String", paramType = "form", 
    value = "Optional params for this request", example = "NA"),
    @ApiImplicitParam(required = false, name = "pageSize", dataType = "String", paramType = "form", 
    value = "Page size if paging", example = "100"),
    @ApiImplicitParam(required = false, name = "pageIsCursor", dataType = "String", paramType = "form", 
    value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pageLastCursorField", dataType = "String", paramType = "form", 
    value = "Field that will be sent back for cursor based paging", example = "abc123"),
    @ApiImplicitParam(required = false, name = "pageLastCursorFieldType", dataType = "String", paramType = "form", 
    value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp"),
    @ApiImplicitParam(required = false, name = "pageCursorFieldIncludesLastRetrieved", dataType = "String", paramType = "form", 
    value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F"),
    @ApiImplicitParam(required = false, name = "pointInTimeFrom", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "pointInTimeTo", dataType = "String", paramType = "form", 
    value = "To query members at a certain point in time or time range in the past, set this valueand/or the value of pointInTimeTo. "
        + "This parameter specifies the start of the rangeof the point in time query. If this is specified but pointInTimeTo is not specified,then the point in time query range will be from the time specified to now."
        + "Format: yyyy/MM/dd HH:mm:ss.SSS", example = "1970/01/01 00:00:00.000"),
    @ApiImplicitParam(required = false, name = "ascending", dataType = "String", paramType = "form", 
    value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F"),
    @ApiImplicitParam(required = false, name = "sortString", dataType = "String", paramType = "form", 
    value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", 
    example = "name | displayName | extension | displayExtension"),
    @ApiImplicitParam(required = false, name = "wsGroupName", dataType = "String", paramType = "form", value = "fetch audit entries for this group", example = "some:group:name"),
    @ApiImplicitParam(required = false, name = "wsGroupId", dataType = "String", paramType = "form", value = "fetch audit entries for this group", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsStemName", dataType = "String", paramType = "form", value = "fetch audit entries for this stem", example = "some:stem:name"),
    @ApiImplicitParam(required = false, name = "wsStemId", dataType = "String", paramType = "form", value = "fetch audit entries for this stem", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefName", dataType = "String", paramType = "form", value = "fetch audit entries for attribute def", example = "some:other:myAttributeDef"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefId", dataType = "String", paramType = "form", value = "fetch audit entries for attribute def", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameId", dataType = "String", paramType = "form", value = "fetch audit entries for attribute def name", example = "a1b2c3d4"),
    @ApiImplicitParam(required = false, name = "wsAttributeDefNameName", dataType = "String", paramType = "form", value = "fetch audit entries for attribute def name", example = "some:other:myAttributeDefName"),
  })
  public WsGetAuditEntriesResults getAuditEntriesLite(final String clientVersion,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      String auditType, String auditActionId,
      String wsGroupName, String wsGroupId,
      String wsStemName, String wsStemId,
      String wsAttributeDefName, String wsAttributeDefId,
      String wsAttributeDefNameName, String wsAttributeDefNameId,
      String wsSubjectId, String wsSubjectSourceId, String wsSubjectIdentifier,
      String actionsPerformedByWsSubjectId, String actionsPerformedByWsSubjectSourceId, String actionsPerformedByWsSubjectIdentifier,
      String paramName0, String paramValue0, String paramName1, String paramValue1,
      String pageSize,
      String sortString, String ascending,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved,
      String pointInTimeFrom, String pointInTimeTo) {
    
    WsGetAuditEntriesResults results = new WsGetAuditEntriesResults();
    
    try {

      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);

      Timestamp pointInTimeFromTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeFrom);
      Timestamp pointInTimeToTimestamp = GrouperServiceUtils.stringToTimestamp(pointInTimeTo);
      
      results =  GrouperServiceLogic.getAuditEntriesLite(grouperWsVersion, 
          actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier,
          auditType, auditActionId,
          wsGroupName, wsGroupId, 
          wsStemName, wsStemId, wsAttributeDefName, wsAttributeDefId, 
          wsAttributeDefNameName, wsAttributeDefNameId, wsSubjectId, 
          wsSubjectSourceId, wsSubjectIdentifier, 
          actionsPerformedByWsSubjectId, actionsPerformedByWsSubjectSourceId, actionsPerformedByWsSubjectIdentifier,
          paramName0, paramValue0, paramName1,
          paramValue1, pageSizeInteger,
          sortString, ascendingBoolean,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean,
          pointInTimeFromTimestamp,
          pointInTimeToTimestamp);
     
    } catch (Exception e) {
      results.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(results.getResultMetadata(), this.soap);
    
    return results;
    
  }

  /**
   * get audit entries
   * @param clientVersion
   * @param actAsSubjectLookup
   * @param auditType
   * @param auditActionId
   * @param wsGroupLookup 
   * @param wsStemLookup 
   * @param wsAttributeDefLookup 
   * @param wsAttributeDefNameLookup 
   * @param wsSubjectLookup 
   * @param actionsPerformedByWsSubjectLookup 
   * @param wsOwnerGroupLookups
   * @param wsOwnerStemLookups
   * @param wsOwnerAttributeDefLookups
   * @param wsOwnerAttributeDefNameLookups
   * @param wsOwnerSubjectLookups
   * @param params
   * @param pageSize
   * @param sortString 
   * @param ascending 
   * @param pageIsCursor true means cursor based paging
   * @param pageLastCursorField field based on which paging needs to occur 
   * @param pageLastCursorFieldType type of last cursor field (eg: string, int, timestamp, etc)
   * @param pageCursorFieldIncludesLastRetrieved should the result has last retrieved item
   * @param fromDate
   * @param toDate
   * @return get audit entries
   */
  public WsGetAuditEntriesResults getAuditEntries(final String clientVersion,
      WsSubjectLookup actAsSubjectLookup,
      String auditType, String auditActionId,
      WsGroupLookup wsGroupLookup,
      WsStemLookup wsStemLookup,
      WsAttributeDefLookup wsAttributeDefLookup,
      WsAttributeDefNameLookup wsAttributeDefNameLookup,
      WsSubjectLookup wsSubjectLookup,
      WsSubjectLookup actionsPerformedByWsSubjectLookup,
      WsParam[] params,
      String pageSize,
      String sortString, String ascending,
      String pageIsCursor, String pageLastCursorField, String pageLastCursorFieldType,
      String pageCursorFieldIncludesLastRetrieved,
      String fromDate, String toDate) {
    
    
    WsGetAuditEntriesResults results = new WsGetAuditEntriesResults();
    
    try {
      
      GrouperVersion grouperWsVersion = GrouperVersion.valueOfIgnoreCase(
          clientVersion, true);

      Integer pageSizeInteger = GrouperUtil.intObjectValue(pageSize, true);
      
      Boolean ascendingBoolean = GrouperUtil.booleanObjectValue(ascending);
      
      Boolean pageIsCursorBoolean = GrouperUtil.booleanValue(pageIsCursor, false);
      Boolean pageCursorFieldIncludesLastRetrievedBoolean = GrouperUtil.booleanValue(pageCursorFieldIncludesLastRetrieved, false);
      
      Timestamp fromDateTimestamp = GrouperServiceUtils.stringToTimestamp(fromDate);
      Timestamp toDateTimestamp = GrouperServiceUtils.stringToTimestamp(toDate);
      
      results =  GrouperServiceLogic.getAuditEntries(grouperWsVersion,
          actAsSubjectLookup, auditType, auditActionId,
          wsGroupLookup, wsStemLookup, wsAttributeDefLookup, wsAttributeDefNameLookup,
          wsSubjectLookup, actionsPerformedByWsSubjectLookup, params, pageSizeInteger, sortString, ascendingBoolean,
          pageIsCursorBoolean, pageLastCursorField, pageLastCursorFieldType,
          pageCursorFieldIncludesLastRetrievedBoolean,
          fromDateTimestamp, toDateTimestamp);
     
    } catch (Exception e) {
      results.assignResultCodeException(null, null, e);
    }

    //set response headers
    GrouperServiceUtils.addResponseHeaders(results.getResultMetadata(), this.soap);
    
    return results;
  }
}
