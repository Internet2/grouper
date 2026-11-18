/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.ws.mcp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSubjectScope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * MCP tool handler for adding members to a Grouper group.
 * Supports adding one or more subjects by subject ID or identifier.
 * Delegates to the WS addMember service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpAddMember {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAddMember.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for group_add_member
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "group_add_member");
    tool.put("description",
        "Add one or more subjects as members of a Grouper group. "
        + "Each subject is identified by subjectIdOrIdentifier "
        + "(and optionally sourceId and subjectIdType). Supports setting membership "
        + "enabled/disabled dates for future provisioning.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "The fully qualified group name to add members to "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("groupName", groupNameProp);

    ObjectNode subjectsProp = objectMapper.createObjectNode();
    subjectsProp.put("type", "array");
    ObjectNode subjectsItemsNode = objectMapper.createObjectNode();
    subjectsItemsNode.put("type", "object");
    ObjectNode subjectProperties = objectMapper.createObjectNode();

    GrouperMcpSubjectUtils.addSubjectArrayItemProperties(subjectProperties);

    subjectsItemsNode.set("properties", subjectProperties);
    subjectsProp.set("items", subjectsItemsNode);
    subjectsProp.put("description",
        "Array of subjects to add to the group. Each subject must have a "
        + "subjectIdOrIdentifier (and optionally sourceId and subjectIdType).");
    properties.set("subjects", subjectsProp);

    ObjectNode replaceAllExistingProp = objectMapper.createObjectNode();
    replaceAllExistingProp.put("type", "boolean");
    replaceAllExistingProp.put("description",
        "If true, replace all existing members of the group with the provided "
        + "subjects. Default is false (add to existing members).");
    replaceAllExistingProp.put("default", false);
    properties.set("replaceAllExisting", replaceAllExistingProp);

    ObjectNode fieldNameProp = objectMapper.createObjectNode();
    fieldNameProp.put("type", "string");
    fieldNameProp.put("description",
        "Field (list) name for the membership. "
        + "Defaults to 'members' (the standard membership list).");
    properties.set("fieldName", fieldNameProp);

    ObjectNode disabledTimeProp = objectMapper.createObjectNode();
    disabledTimeProp.put("type", "string");
    disabledTimeProp.put("description",
        "Date when this membership will be disabled, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/12/31 23:59:59.000'). "
        + "Used for time-limited memberships.");
    properties.set("disabledTime", disabledTimeProp);

    ObjectNode enabledTimeProp = objectMapper.createObjectNode();
    enabledTimeProp.put("type", "string");
    enabledTimeProp.put("description",
        "Date when this membership will be enabled, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/09/01 00:00:00.000'). "
        + "Used for future-dated provisioning.");
    properties.set("enabledTime", enabledTimeProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("groupName");
    required.add("subjects");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the group_add_member tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    boolean replaceAllExisting = arguments != null && arguments.has("replaceAllExisting")
        && arguments.get("replaceAllExisting").asBoolean(false);
    String fieldNameString = arguments != null && arguments.has("fieldName")
        ? arguments.get("fieldName").asText() : null;
    String disabledTimeString = arguments != null && arguments.has("disabledTime")
        ? arguments.get("disabledTime").asText() : null;
    String enabledTimeString = arguments != null && arguments.has("enabledTime")
        ? arguments.get("enabledTime").asText() : null;

    if (StringUtils.isBlank(groupName)) {
      return buildErrorResult("groupName is required.");
    }

    // protected-resource rules and read-write scope on the group the name really resolves to
    // (it can be an alternate, old, name), see GrouperMcpResolvedTargetGate.  the write below
    // uses that group's uuid, so the group checked is the group changed
    GrouperMcpResolvedTargetGate.Target groupTarget =
        GrouperMcpResolvedTargetGate.checkGroupByName(authUser, groupName, false);
    if (groupTarget.getError() != null) {
      return buildErrorResult(groupTarget.getError());
    }

    // parse subjects array
    if (arguments == null || !arguments.has("subjects")
        || !arguments.get("subjects").isArray()
        || arguments.get("subjects").size() == 0) {
      return buildErrorResult("subjects array is required and must not be empty.");
    }

    JsonNode subjectsArray = arguments.get("subjects");
    List<WsSubjectLookup> subjectLookupList = new ArrayList<WsSubjectLookup>();

    for (int i = 0; i < subjectsArray.size(); i++) {
      JsonNode subjectNode = subjectsArray.get(i);
      String subjectIdOrIdentifier = subjectNode.has("subjectIdOrIdentifier")
          ? subjectNode.get("subjectIdOrIdentifier").asText() : null;
      String subjectIdType = subjectNode.has("subjectIdType")
          ? subjectNode.get("subjectIdType").asText() : null;
      String sourceId = subjectNode.has("sourceId")
          ? subjectNode.get("sourceId").asText() : null;

      if (StringUtils.isBlank(subjectIdOrIdentifier)) {
        return buildErrorResult(
            "Each subject must have a subjectIdOrIdentifier "
            + "(subject at index " + i + " is missing it).");
      }

      // validate subjectIdType if provided
      String subjectIdTypeError = GrouperMcpSubjectUtils.validateSubjectIdType(subjectIdType);
      if (subjectIdTypeError != null) {
        return buildErrorResult(subjectIdTypeError + " (subject at index " + i + ").");
      }

      // the lookup the write will use.  the scope check resolves it (by value, id type and source)
      // and the lookup keeps that subject, so the subject checked is the subject added
      WsSubjectLookup subjectLookup = GrouperMcpSubjectUtils.createSubjectLookup(
          subjectIdOrIdentifier, subjectIdType, sourceId);

      // check readwrite scope restrictions on subject, by source and id
      String subjectError = GrouperMcpResolvedTargetGate.checkSubjectLookup(
          authUser, subjectLookup, subjectIdOrIdentifier, false);
      if (subjectError != null) {
        return buildErrorResult(subjectError);
      }

      subjectLookupList.add(subjectLookup);
    }

    WsSubjectLookup[] subjectLookups =
        subjectLookupList.toArray(new WsSubjectLookup[0]);

    try {

      // build the group lookup: by the uuid the gate checked, else by name (not found, the web
      // service logic reports that)
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      if (groupTarget.isFound()) {
        wsGroupLookup.setUuid(groupTarget.getUuid());
      } else {
        wsGroupLookup.setGroupName(groupName);
      }

      // convert fieldName string to Field
      Field field = GrouperServiceUtils.retrieveField(fieldNameString);

      // replaceAllExisting also removes every current member that is not in the request.  those
      // removals need the same subject scope check as an explicit group_delete_member, so when the
      // subject scope is limited, check them all before anything is changed
      if (replaceAllExisting && authUser.isConsentScopeEnforced()
          && authUser.isSubjectReadwriteScopeLimited()) {
        String replaceAllError = replaceAllExistingScopeError(authUser, groupTarget, field, subjectLookups);
        if (replaceAllError != null) {
          return buildErrorResult(replaceAllError);
        }
      }

      // convert timestamp strings
      Timestamp disabledTimestamp = GrouperServiceUtils.stringToTimestamp(disabledTimeString);
      Timestamp enabledTimestamp = GrouperServiceUtils.stringToTimestamp(enabledTimeString);

      // actAs is null: the logged-in user (from JWT, set on REMOTE_USER by the MCP servlet) is used
      // delegate to the WS service logic
      WsAddMemberResults wsResults = GrouperServiceLogic.addMember(
          GrouperVersion.currentVersion(),
          wsGroupLookup,
          subjectLookups,
          replaceAllExisting,
          null,   // actAsSubjectLookup - uses REMOTE_USER from the MCP JWT
          field,
          null,   // txType - default NONE
          false,  // includeGroupDetail
          false,  // includeSubjectDetail
          null,   // subjectAttributeNames
          null,   // params
          disabledTimestamp,
          enabledTimestamp,
          false   // addExternalSubjectIfNotFound
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsAddMemberResult[] memberResults = wsResults.getResults();
      if (GrouperUtil.length(memberResults) == 0) {
        return buildSuccessResult("No results returned.");
      }

      if (GrouperUtil.length(memberResults) == 1) {
        // single subject - return as object
        ObjectNode resultNode = convertMemberResultToJson(memberResults[0]);
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(resultNode);
        return buildSuccessResult(resultText);
      } else {
        // multiple subjects - return as array
        ArrayNode resultsArray = objectMapper.createArrayNode();
        for (WsAddMemberResult memberResult : memberResults) {
          resultsArray.add(convertMemberResultToJson(memberResult));
        }
        String resultText = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(resultsArray);
        return buildSuccessResult(resultText);
      }

    } catch (Exception e) {
      LOG.error("Error adding members to group: " + groupName, e);
      return buildErrorResult("Error adding members to group: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * check the members replaceAllExisting would remove against the subject scope.  The web service
   * logic removes every immediate member of the field whose subject is not in the request
   * (GrouperServiceLogic.addMember), so the same set is computed here, as root (a superset of what
   * the caller sees, which only makes this stricter), and every one of them has to be in scope.
   * Fails closed: a group that does not resolve, or a submitted subject that does not resolve (so
   * it is not known which members would be kept), is refused.
   * @param authUser the caller
   * @param groupTarget the gated group
   * @param field the field, null for the default members list
   * @param subjectLookups the subjects in the request, already resolved by the scope check
   * @return the error, or null if every removal is in scope
   */
  private static String replaceAllExistingScopeError(final GrouperMcpAuthUser authUser,
      final GrouperMcpResolvedTargetGate.Target groupTarget, final Field field,
      final WsSubjectLookup[] subjectLookups) {

    if (!groupTarget.isFound()) {
      return "Access denied: replaceAllExisting needs a group that exists.";
    }

    // the subjects that will be kept, by source and id, the same key the web service logic uses
    final Set<String> keptKeys = new HashSet<String>();
    for (WsSubjectLookup subjectLookup : subjectLookups) {
      Subject subject = null;
      try {
        subject = subjectLookup.retrieveSubject();
      } catch (Exception e) {
        LOG.debug("Could not resolve subject for replaceAllExisting check", e);
      }
      if (subject == null) {
        return "Access denied: with replaceAllExisting every subject in the request must resolve to "
            + "exactly one subject, so the members it would remove can be checked against your "
            + "consented read-write scope. Nothing was changed.";
      }
      keptKeys.add(GrouperOAuthSubjectScope.subjectKey(subject));
    }

    Integer outOfScopeCount = (Integer) GrouperSession.internal_callbackRootGrouperSession(
        new GrouperSessionHandler() {

          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            Group group = GroupFinder.findByUuid(grouperSession, groupTarget.getUuid(), false);
            if (group == null) {
              return -1;
            }
            Field theField = field == null ? Group.getDefaultList() : field;
            int count = 0;
            for (Member member : GrouperUtil.nonNull(group.getImmediateMembers(theField))) {
              String key = GrouperOAuthSubjectScope.subjectKey(member.getSubjectSourceId(), member.getSubjectId());
              if (keptKeys.contains(key)) {
                continue;
              }
              // this member would be removed: it has to be in the subject scope
              if (!authUser.isSubjectInReadwriteScope(member)) {
                count++;
              }
            }
            return count;
          }
        });

    if (outOfScopeCount == null || outOfScopeCount < 0) {
      return GrouperMcpResolvedTargetGate.GENERIC_DENIED_MESSAGE;
    }
    if (outOfScopeCount > 0) {
      // the subjects are not named: the caller may not be allowed to read the membership list
      return "Access denied: replaceAllExisting would remove " + outOfScopeCount
          + " existing member(s) outside your consented read-write subject scope. Nothing was changed.";
    }
    return null;
  }

  /**
   * convert a WsAddMemberResult to a clean JSON object for MCP consumption.
   * @param memberResult the WS add member result
   * @return clean JSON object
   */
  private static ObjectNode convertMemberResultToJson(WsAddMemberResult memberResult) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    // include the result code (SUCCESS, SUCCESS_CREATED, SUCCESS_ALREADY_EXISTED, etc.)
    if (memberResult.getResultMetadata() != null) {
      resultNode.put("resultCode",
          memberResult.getResultMetadata().getResultCode());
      resultNode.put("success",
          "T".equals(memberResult.getResultMetadata().getSuccess()));
    }

    // include subject info
    if (memberResult.getWsSubject() != null) {
      resultNode.put("subjectId", memberResult.getWsSubject().getId());
      if (StringUtils.isNotBlank(memberResult.getWsSubject().getName())) {
        resultNode.put("name", memberResult.getWsSubject().getName());
      }
      resultNode.put("sourceId", memberResult.getWsSubject().getSourceId());
    }

    return resultNode;
  }

  /**
   * build a successful MCP tool result
   */
  private static ObjectNode buildSuccessResult(String text) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", text);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", false);
    return result;
  }

  /**
   * build an error MCP tool result
   */
  private static ObjectNode buildErrorResult(String errorMessage) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", errorMessage);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", true);
    return result;
  }
}
