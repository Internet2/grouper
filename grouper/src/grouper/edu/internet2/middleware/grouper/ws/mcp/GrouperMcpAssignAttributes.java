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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;

/**
 * MCP tool handler for assigning, adding, removing, or replacing attributes in Grouper.
 * Supports attribute operations on groups, stems, members, and other owner types.
 * Delegates to the WS assignAttributes service logic for consistency.
 *
 * @author mchyzer
 */
public class GrouperMcpAssignAttributes {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAssignAttributes.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for attribute_assignment_save
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    String rootStem = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.rootStemForBuiltinObjects", "etc");

    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "attribute_assignment_save");
    tool.put("description",
        "Assign, add, remove, or replace attributes on Grouper objects. "
        + "Supports attribute operations on owner types: groups, stems (folders), "
        + "members (users), memberships ('immediate only' or 'any'), and attribute definitions. Can include attribute values. "
        + "Use attributeAssignId to target a specific attribute assignment for "
        + "removal or value update (attributeDefNameName is not required when "
        + "attributeAssignId is provided). "
        + "Also supports assignment-on-assignment (e.g. group_asgn) to assign "
        + "name/value pair metadata on an existing attribute assignment. "
        + "For example, to configure attestation on a group: first assign the "
        + "marker attribute '" + rootStem + ":attribute:attestation:attestation' "
        + "to the group (attributeAssignType=group), then use the returned "
        + "attributeAssignId as ownerAttributeAssignId with attributeAssignType=group_asgn "
        + "to assign configuration attributes like '" + rootStem
        + ":attribute:attestation:attestationSendEmail' with values. "
        + "Built-in Grouper attributes use the root stem prefix '" + rootStem + "'.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode attributeAssignTypeProp = objectMapper.createObjectNode();
    attributeAssignTypeProp.put("type", "string");
    ArrayNode assignTypeEnum = objectMapper.createArrayNode();
    assignTypeEnum.add("group");
    assignTypeEnum.add("stem");
    assignTypeEnum.add("member");
    assignTypeEnum.add("imm_mem");
    assignTypeEnum.add("any_mem");
    assignTypeEnum.add("attr_def");
    assignTypeEnum.add("group_asgn");
    assignTypeEnum.add("stem_asgn");
    assignTypeEnum.add("mem_asgn");
    assignTypeEnum.add("imm_mem_asgn");
    assignTypeEnum.add("any_mem_asgn");
    assignTypeEnum.add("attr_def_asgn");
    attributeAssignTypeProp.set("enum", assignTypeEnum);
    attributeAssignTypeProp.put("description",
        "The type of object to assign the attribute on. "
        + "Use the '_asgn' variants (e.g. group_asgn) for assignment-on-assignment, "
        + "where the owner is an existing attribute assignment identified by ownerAttributeAssignId.");
    properties.set("attributeAssignType", attributeAssignTypeProp);

    ObjectNode attributeAssignOperationProp = objectMapper.createObjectNode();
    attributeAssignOperationProp.put("type", "string");
    ArrayNode assignOpEnum = objectMapper.createArrayNode();
    assignOpEnum.add("assign_attr");
    assignOpEnum.add("add_attr");
    assignOpEnum.add("remove_attr");
    assignOpEnum.add("replace_attrs");
    attributeAssignOperationProp.set("enum", assignOpEnum);
    attributeAssignOperationProp.put("description",
        "The operation to perform. "
        + "Defaults to add_attr if the attribute def is multi-assignable, otherwise assign_attr. "
        + "assign_attr (assign if not already assigned), "
        + "add_attr (add even if already assigned), "
        + "remove_attr (remove the assignment), "
        + "replace_attrs (replace all existing assignments with this one).");
    properties.set("attributeAssignOperation", attributeAssignOperationProp);

    ObjectNode attributeDefNameNameProp = objectMapper.createObjectNode();
    attributeDefNameNameProp.put("type", "string");
    attributeDefNameNameProp.put("description",
        "The attribute def name to assign "
        + "(e.g., 'etc:attribute:attrDefName').");
    properties.set("attributeDefNameName", attributeDefNameNameProp);

    ObjectNode ownerGroupNameProp = objectMapper.createObjectNode();
    ownerGroupNameProp.put("type", "string");
    ownerGroupNameProp.put("description",
        "The fully qualified group name to assign the attribute on "
        + "(e.g., 'stem1:stem2:groupName').");
    properties.set("ownerGroupName", ownerGroupNameProp);

    ObjectNode ownerStemNameProp = objectMapper.createObjectNode();
    ownerStemNameProp.put("type", "string");
    ownerStemNameProp.put("description",
        "The fully qualified stem name to assign the attribute on "
        + "(e.g., 'stem1:stem2').");
    properties.set("ownerStemName", ownerStemNameProp);

    ObjectNode ownerSubjectIdOrIdentifierProp = objectMapper.createObjectNode();
    ownerSubjectIdOrIdentifierProp.put("type", "string");
    ownerSubjectIdOrIdentifierProp.put("description",
        "The subject ID or identifier to assign the attribute on "
        + "(e.g., login ID, pennkey, eppn, or subject ID).");
    properties.set("ownerSubjectIdOrIdentifier", ownerSubjectIdOrIdentifierProp);

    ObjectNode ownerSubjectIdTypeProp = objectMapper.createObjectNode();
    ownerSubjectIdTypeProp.put("type", "string");
    ownerSubjectIdTypeProp.put("description",
        "How to interpret the owner subject value. Defaults to 'subjectIdOrIdentifier' which "
        + "tries both ID and identifier. Use 'subjectId' to look up by ID only, "
        + "or 'subjectIdentifier' to look up by identifier only.");
    ArrayNode ownerSubjectIdTypeEnum = objectMapper.createArrayNode();
    ownerSubjectIdTypeEnum.add(GrouperMcpSubjectUtils.SUBJECT_ID_TYPE_ID_OR_IDENTIFIER);
    ownerSubjectIdTypeEnum.add(GrouperMcpSubjectUtils.SUBJECT_ID_TYPE_ID);
    ownerSubjectIdTypeEnum.add(GrouperMcpSubjectUtils.SUBJECT_ID_TYPE_IDENTIFIER);
    ownerSubjectIdTypeProp.set("enum", ownerSubjectIdTypeEnum);
    ownerSubjectIdTypeProp.put("default", GrouperMcpSubjectUtils.SUBJECT_ID_TYPE_ID_OR_IDENTIFIER);
    properties.set("ownerSubjectIdType", ownerSubjectIdTypeProp);

    ObjectNode ownerSubjectSourceIdProp = objectMapper.createObjectNode();
    ownerSubjectSourceIdProp.put("type", "string");
    ownerSubjectSourceIdProp.put("description",
        "Optional source ID for the owner subject.");
    properties.set("ownerSubjectSourceId", ownerSubjectSourceIdProp);

    ObjectNode ownerAttributeAssignIdProp = objectMapper.createObjectNode();
    ownerAttributeAssignIdProp.put("type", "string");
    ownerAttributeAssignIdProp.put("description",
        "The UUID of an existing attribute assignment to assign metadata attributes on "
        + "(assignment-on-assignment). Use with an '_asgn' attributeAssignType "
        + "(e.g. group_asgn). The ID is returned as attributeAssignId when assigning "
        + "the initial attribute.");
    properties.set("ownerAttributeAssignId", ownerAttributeAssignIdProp);

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    actionProp.put("description",
        "The action name for the attribute assignment. "
        + "Defaults to 'assign' if not specified.");
    properties.set("action", actionProp);

    ObjectNode valuesProp = objectMapper.createObjectNode();
    valuesProp.put("type", "array");
    ObjectNode valuesItemsNode = objectMapper.createObjectNode();
    valuesItemsNode.put("type", "string");
    valuesProp.set("items", valuesItemsNode);
    valuesProp.put("description",
        "Array of string values to assign with the attribute.");
    properties.set("values", valuesProp);

    ObjectNode valueOperationProp = objectMapper.createObjectNode();
    valueOperationProp.put("type", "string");
    ArrayNode valueOpEnum = objectMapper.createArrayNode();
    valueOpEnum.add("assign_value");
    valueOpEnum.add("add_value");
    valueOpEnum.add("remove_value");
    valueOpEnum.add("replace_values");
    valueOperationProp.set("enum", valueOpEnum);
    valueOperationProp.put("description",
        "The operation to perform on the attribute values. "
        + "Defaults to replace_values for assign_attr (replaces existing values), "
        + "assign_value for other operations. "
        + "assign_value (set if not already set), "
        + "add_value (add even if already set), "
        + "remove_value (remove this value), "
        + "replace_values (replace all existing values with these).");
    properties.set("valueOperation", valueOperationProp);

    ObjectNode attributeAssignIdProp = objectMapper.createObjectNode();
    attributeAssignIdProp.put("type", "string");
    attributeAssignIdProp.put("description",
        "The UUID of a specific attribute assignment to operate on. "
        + "Use with remove_attr to remove a specific assignment by ID "
        + "(e.g. when there are multiple assignments of the same attribute). "
        + "The ID is returned by attribute_assignment_get as attributeAssignId.");
    properties.set("attributeAssignId", attributeAssignIdProp);

    ObjectNode assignmentNotesProp = objectMapper.createObjectNode();
    assignmentNotesProp.put("type", "string");
    assignmentNotesProp.put("description",
        "Optional notes for the attribute assignment.");
    properties.set("assignmentNotes", assignmentNotesProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("attributeAssignType");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the attribute_assignment_save tool by delegating to the WS service logic
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String attributeAssignTypeString = arguments != null && arguments.has("attributeAssignType")
        ? arguments.get("attributeAssignType").asText() : null;
    String attributeAssignOperationString = arguments != null && arguments.has("attributeAssignOperation")
        ? arguments.get("attributeAssignOperation").asText() : null;
    String attributeDefNameName = arguments != null && arguments.has("attributeDefNameName")
        ? arguments.get("attributeDefNameName").asText() : null;
    String ownerGroupName = arguments != null && arguments.has("ownerGroupName")
        ? arguments.get("ownerGroupName").asText() : null;
    String ownerStemName = arguments != null && arguments.has("ownerStemName")
        ? arguments.get("ownerStemName").asText() : null;
    String ownerSubjectIdOrIdentifier = arguments != null && arguments.has("ownerSubjectIdOrIdentifier")
        ? arguments.get("ownerSubjectIdOrIdentifier").asText() : null;
    String ownerSubjectIdType = arguments != null && arguments.has("ownerSubjectIdType")
        ? arguments.get("ownerSubjectIdType").asText() : null;
    String ownerSubjectSourceId = arguments != null && arguments.has("ownerSubjectSourceId")
        ? arguments.get("ownerSubjectSourceId").asText() : null;
    String ownerAttributeAssignId = arguments != null && arguments.has("ownerAttributeAssignId")
        ? arguments.get("ownerAttributeAssignId").asText() : null;
    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;
    JsonNode valuesArray = arguments != null && arguments.has("values")
        ? arguments.get("values") : null;
    String attributeAssignId = arguments != null && arguments.has("attributeAssignId")
        ? arguments.get("attributeAssignId").asText() : null;
    String valueOperationString = arguments != null && arguments.has("valueOperation")
        ? arguments.get("valueOperation").asText() : null;
    String assignmentNotes = arguments != null && arguments.has("assignmentNotes")
        ? arguments.get("assignmentNotes").asText() : null;

    if (StringUtils.isBlank(attributeAssignTypeString)) {
      return buildErrorResult("attributeAssignType is required.");
    }
    if (StringUtils.isBlank(attributeDefNameName) && StringUtils.isBlank(attributeAssignId)) {
      return buildErrorResult("attributeDefNameName is required (unless attributeAssignId is provided).");
    }
    if (StringUtils.isBlank(attributeAssignOperationString)) {
      // default based on whether the attribute def is multi-assignable
      if (StringUtils.isNotBlank(attributeDefNameName)) {
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(attributeDefNameName, false);
        if (attributeDefName != null && attributeDefName.getAttributeDef().isMultiAssignable()) {
          attributeAssignOperationString = "add_attr";
        } else {
          attributeAssignOperationString = "assign_attr";
        }
      } else {
        // no def name (operating by ID), default to assign_attr
        attributeAssignOperationString = "assign_attr";
      }
    }

    // block modifications to protected system groups and stems
    if (StringUtils.isNotBlank(ownerGroupName)
        && GrouperMcpProtectedResources.isProtectedGroupName(ownerGroupName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedGroupError(ownerGroupName));
    }
    if (StringUtils.isNotBlank(ownerStemName)
        && GrouperMcpProtectedResources.isProtectedStemName(ownerStemName)) {
      return buildErrorResult(
          GrouperMcpProtectedResources.buildProtectedStemError(ownerStemName));
    }

    // check readwrite scope restrictions (OAuth only)
    if (authUser.isOAuthAuthenticated()) {
      // if user has no group/folder scope, deny group/folder owners entirely
      if (!authUser.hasGroupOrFolderReadwriteScope()) {
        if (StringUtils.isNotBlank(ownerGroupName)) {
          return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
        }
        if (StringUtils.isNotBlank(ownerStemName)) {
          return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
        }
      }
      if (StringUtils.isNotBlank(ownerGroupName)
          && !authUser.isGroupInReadwriteScope(ownerGroupName)) {
        return buildErrorResult(
            authUser.buildReadwriteScopeDeniedError("group", ownerGroupName));
      }
      if (StringUtils.isNotBlank(ownerStemName)
          && !authUser.isStemInReadwriteScope(ownerStemName)) {
        return buildErrorResult(
            authUser.buildReadwriteScopeDeniedError("folder", ownerStemName));
      }
      // if user has no subject scope, deny subject owners entirely
      if (!authUser.hasSubjectReadwriteScope()) {
        if (StringUtils.isNotBlank(ownerSubjectIdOrIdentifier)) {
          return buildErrorResult("Access denied: your OAuth scope does not include subjects.");
        }
      }
      if (StringUtils.isNotBlank(ownerSubjectIdOrIdentifier)
          && !authUser.isSubjectInReadwriteScope(ownerSubjectIdOrIdentifier)) {
        return buildErrorResult(
            authUser.buildReadwriteScopeDeniedError("subject", ownerSubjectIdOrIdentifier));
      }
    }

    // for assignment-on-assignment, resolve the owner assignment and validate
    // protected resources and scope against the underlying owner (group/stem/subject)
    if (StringUtils.isNotBlank(ownerAttributeAssignId)) {
      try {
        // use root session for the lookup so we can always resolve the owner
        // for scope/protected-resource validation; the WS layer does its own
        // privilege check for the actual operation
        AttributeAssign ownerAssign = (AttributeAssign) GrouperSession.internal_callbackRootGrouperSession(
            new GrouperSessionHandler() {
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                return AttributeAssignFinder.findById(ownerAttributeAssignId, true);
              }
            });

        // check protected resources and scope on the underlying owner
        AttributeAssignType ownerType = ownerAssign.getAttributeAssignType();
        if (AttributeAssignType.group == ownerType) {
          Group ownerGroup = ownerAssign.getOwnerGroup();
          if (ownerGroup != null) {
            String groupName = ownerGroup.getName();
            if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
              return buildErrorResult(
                  GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
            }
            if (authUser.isOAuthAuthenticated()
                && !authUser.hasGroupOrFolderReadwriteScope()) {
              return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
            }
            if (authUser.isOAuthAuthenticated()
                && !authUser.isGroupInReadwriteScope(groupName)) {
              return buildErrorResult(
                  authUser.buildReadwriteScopeDeniedError("group", groupName));
            }
          }
        } else if (AttributeAssignType.stem == ownerType) {
          Stem ownerStem = ownerAssign.getOwnerStem();
          if (ownerStem != null) {
            String stemName = ownerStem.getName();
            if (GrouperMcpProtectedResources.isProtectedStemName(stemName)) {
              return buildErrorResult(
                  GrouperMcpProtectedResources.buildProtectedStemError(stemName));
            }
            if (authUser.isOAuthAuthenticated()
                && !authUser.hasGroupOrFolderReadwriteScope()) {
              return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
            }
            if (authUser.isOAuthAuthenticated()
                && !authUser.isStemInReadwriteScope(stemName)) {
              return buildErrorResult(
                  authUser.buildReadwriteScopeDeniedError("folder", stemName));
            }
          }
        } else if (AttributeAssignType.member == ownerType) {
          if (authUser.isOAuthAuthenticated() && ownerAssign.getOwnerMember() != null) {
            if (!authUser.hasSubjectReadwriteScope()) {
              return buildErrorResult("Access denied: your OAuth scope does not include subjects.");
            }
            String subjectId = ownerAssign.getOwnerMember().getSubjectId();
            if (!authUser.isSubjectInReadwriteScope(subjectId)) {
              return buildErrorResult(
                  authUser.buildReadwriteScopeDeniedError("subject", subjectId));
            }
          }
        } else if (AttributeAssignType.any_mem == ownerType) {
          // any_mem has both a group and a member; validate both
          Group ownerGroup = ownerAssign.getOwnerGroup();
          if (ownerGroup != null) {
            String groupName = ownerGroup.getName();
            if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
              return buildErrorResult(
                  GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
            }
            if (authUser.isOAuthAuthenticated()
                && !authUser.hasGroupOrFolderReadwriteScope()) {
              return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
            }
            if (authUser.isOAuthAuthenticated()
                && !authUser.isGroupInReadwriteScope(groupName)) {
              return buildErrorResult(
                  authUser.buildReadwriteScopeDeniedError("group", groupName));
            }
          }
          if (authUser.isOAuthAuthenticated() && ownerAssign.getOwnerMember() != null) {
            if (!authUser.hasSubjectReadwriteScope()) {
              return buildErrorResult("Access denied: your OAuth scope does not include subjects.");
            }
            String subjectId = ownerAssign.getOwnerMember().getSubjectId();
            if (!authUser.isSubjectInReadwriteScope(subjectId)) {
              return buildErrorResult(
                  authUser.buildReadwriteScopeDeniedError("subject", subjectId));
            }
          }
        } else if (AttributeAssignType.imm_mem == ownerType) {
          // imm_mem has an ownerMembershipId; get the membership's group and member
          Membership ownerMembership = ownerAssign.getOwnerImmediateMembership();
          if (ownerMembership != null) {
            try {
              Group ownerGroup = ownerMembership.getOwnerGroup();
              if (ownerGroup != null) {
                String groupName = ownerGroup.getName();
                if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
                  return buildErrorResult(
                      GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
                }
                if (authUser.isOAuthAuthenticated()
                    && !authUser.hasGroupOrFolderReadwriteScope()) {
                  return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
                }
                if (authUser.isOAuthAuthenticated()
                    && !authUser.isGroupInReadwriteScope(groupName)) {
                  return buildErrorResult(
                      authUser.buildReadwriteScopeDeniedError("group", groupName));
                }
              }
            } catch (Exception e) {
              // membership might not have a group owner (e.g. stem membership)
            }
            try {
              Member ownerMember = ownerMembership.getMember();
              if (authUser.isOAuthAuthenticated() && ownerMember != null) {
                if (!authUser.hasSubjectReadwriteScope()) {
                  return buildErrorResult("Access denied: your OAuth scope does not include subjects.");
                }
                String subjectId = ownerMember.getSubjectId();
                if (!authUser.isSubjectInReadwriteScope(subjectId)) {
                  return buildErrorResult(
                      authUser.buildReadwriteScopeDeniedError("subject", subjectId));
                }
              }
            } catch (Exception e) {
              // ignore if member not found
            }
          }
        } else if (AttributeAssignType.attr_def == ownerType) {
          // attr_def: validate the parent folder of the attribute def
          AttributeDef ownerAttrDef = ownerAssign.getOwnerAttributeDef();
          if (ownerAttrDef != null) {
            String parentStemName = ownerAttrDef.getParentStemName();
            if (StringUtils.isNotBlank(parentStemName)) {
              if (GrouperMcpProtectedResources.isProtectedStemName(parentStemName)) {
                return buildErrorResult(
                    GrouperMcpProtectedResources.buildProtectedStemError(parentStemName));
              }
              if (authUser.isOAuthAuthenticated()
                  && !authUser.hasGroupOrFolderReadwriteScope()) {
                return buildErrorResult("Access denied: your OAuth scope does not include groups or folders.");
              }
              if (authUser.isOAuthAuthenticated()
                  && !authUser.isStemInReadwriteScope(parentStemName)) {
                return buildErrorResult(
                    authUser.buildReadwriteScopeDeniedError("folder", parentStemName));
              }
            }
          }
        }
      } catch (Exception e) {
        return buildErrorResult("Could not resolve owner attribute assignment: "
            + ownerAttributeAssignId + ", " + e.getMessage());
      }
    }

    try {

      AttributeAssignType attrAssignType = AttributeAssignType.valueOfIgnoreCase(
          attributeAssignTypeString, false);
      AttributeAssignOperation attrAssignOp = AttributeAssignOperation.valueOfIgnoreCase(
          attributeAssignOperationString, false);

      // skip wsAttributeDefNameLookups when targeting by attributeAssignId
      // (WS layer does not allow both at the same time)
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
      if (StringUtils.isBlank(attributeAssignId) && StringUtils.isNotBlank(attributeDefNameName)) {
        wsAttributeDefNameLookups = new WsAttributeDefNameLookup[] {
            new WsAttributeDefNameLookup(attributeDefNameName, null)
        };
      }

      // skip owner lookups when targeting by attributeAssignId
      // (WS layer does not allow both at the same time)
      WsGroupLookup[] wsOwnerGroupLookups = null;
      WsStemLookup[] wsOwnerStemLookups = null;
      WsSubjectLookup[] wsOwnerSubjectLookups = null;
      WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups = null;

      if (StringUtils.isBlank(attributeAssignId)) {
        if (StringUtils.isNotBlank(ownerGroupName)) {
          WsGroupLookup gl = new WsGroupLookup();
          gl.setGroupName(ownerGroupName);
          wsOwnerGroupLookups = new WsGroupLookup[] { gl };
        }

        if (StringUtils.isNotBlank(ownerStemName)) {
          wsOwnerStemLookups = new WsStemLookup[] { new WsStemLookup(ownerStemName, null) };
        }

        if (StringUtils.isNotBlank(ownerSubjectIdOrIdentifier)) {
          wsOwnerSubjectLookups = new WsSubjectLookup[] {
              GrouperMcpSubjectUtils.createSubjectLookup(
                  ownerSubjectIdOrIdentifier, ownerSubjectIdType, ownerSubjectSourceId)
          };
        }

        if (StringUtils.isNotBlank(ownerAttributeAssignId)) {
          WsAttributeAssignLookup aal = new WsAttributeAssignLookup();
          aal.setUuid(ownerAttributeAssignId);
          wsOwnerAttributeAssignLookups = new WsAttributeAssignLookup[] { aal };
        }
      }

      WsAttributeAssignValue[] wsValues = null;
      if (valuesArray != null && valuesArray.isArray() && valuesArray.size() > 0) {
        wsValues = new WsAttributeAssignValue[valuesArray.size()];
        for (int i = 0; i < valuesArray.size(); i++) {
          wsValues[i] = new WsAttributeAssignValue();
          wsValues[i].setValueSystem(valuesArray.get(i).asText());
        }
      }

      String[] actions = null;
      if (StringUtils.isNotBlank(action)) {
        actions = new String[] { action };
      }

      // build wsAttributeAssignLookups for targeted operations (e.g. remove by ID)
      WsAttributeAssignLookup[] wsAttributeAssignLookups = null;
      if (StringUtils.isNotBlank(attributeAssignId)) {
        WsAttributeAssignLookup aal = new WsAttributeAssignLookup();
        aal.setUuid(attributeAssignId);
        wsAttributeAssignLookups = new WsAttributeAssignLookup[] { aal };
      }

      // determine value operation: use explicit parameter if provided,
      // otherwise default to replace_values for assign_attr (so existing values
      // get replaced instead of erroring), assign_value for other operations
      AttributeAssignValueOperation valueOperation = null;
      if (wsValues != null) {
        if (StringUtils.isNotBlank(valueOperationString)) {
          valueOperation = AttributeAssignValueOperation.valueOfIgnoreCase(
              valueOperationString, true);
        } else if (attrAssignOp == AttributeAssignOperation.assign_attr) {
          valueOperation = AttributeAssignValueOperation.replace_values;
        } else {
          valueOperation = AttributeAssignValueOperation.assign_value;
        }
      }

      WsAssignAttributesResults wsResults = GrouperServiceLogic.assignAttributes(
          GrouperVersion.currentVersion(),
          attrAssignType,
          wsAttributeDefNameLookups,
          attrAssignOp,
          wsValues,
          assignmentNotes,
          null, null,  // enabledTime, disabledTime
          null,   // delegatable
          valueOperation,   // attributeAssignValueOperation
          wsAttributeAssignLookups,   // wsAttributeAssignLookups
          wsOwnerGroupLookups,
          wsOwnerStemLookups,
          wsOwnerSubjectLookups,
          null,   // wsOwnerMembershipLookups
          null,   // wsOwnerMembershipAnyLookups
          null,   // wsOwnerAttributeDefLookups
          wsOwnerAttributeAssignLookups,
          actions,
          null,   // actAsSubjectLookup
          false, null,  // includeSubjectDetail
          false,  // includeGroupDetail
          null,   // params
          null, null, null  // replace*
      );

      // check for overall errors
      if (wsResults.getResultMetadata() != null
          && !"T".equals(wsResults.getResultMetadata().getSuccess())) {
        return buildErrorResult(wsResults.getResultMetadata().getResultMessage());
      }

      // build clean MCP-friendly result
      WsAssignAttributeResult[] assignResults = wsResults.getWsAttributeAssignResults();
      if (GrouperUtil.length(assignResults) == 0) {
        return buildSuccessResult("No results returned.");
      }

      ArrayNode resultsArray = objectMapper.createArrayNode();
      for (WsAssignAttributeResult assignResult : assignResults) {
        resultsArray.add(convertAssignAttributeResultToJson(assignResult));
      }
      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultsArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error assigning attributes", e);
      return buildErrorResult("Error assigning attributes: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * convert a WsAssignAttributeResult to a clean JSON object for MCP consumption.
   * @param assignResult the WS assign attribute result
   * @return clean JSON object
   */
  private static ObjectNode convertAssignAttributeResultToJson(WsAssignAttributeResult assignResult) {
    ObjectNode resultNode = objectMapper.createObjectNode();

    resultNode.put("changed", "T".equals(assignResult.getChanged()));

    WsAttributeAssign[] wsAttributeAssigns = assignResult.getWsAttributeAssigns();
    if (GrouperUtil.length(wsAttributeAssigns) > 0) {
      ArrayNode assignsArray = objectMapper.createArrayNode();
      for (WsAttributeAssign wsAttrAssign : wsAttributeAssigns) {
        ObjectNode assignNode = objectMapper.createObjectNode();
        if (StringUtils.isNotBlank(wsAttrAssign.getId())) {
          assignNode.put("attributeAssignId", wsAttrAssign.getId());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getAttributeAssignType())) {
          assignNode.put("attributeAssignType", wsAttrAssign.getAttributeAssignType());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getAttributeDefNameName())) {
          assignNode.put("attributeDefNameName", wsAttrAssign.getAttributeDefNameName());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getOwnerGroupName())) {
          assignNode.put("ownerGroupName", wsAttrAssign.getOwnerGroupName());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getOwnerStemName())) {
          assignNode.put("ownerStemName", wsAttrAssign.getOwnerStemName());
        }
        if (StringUtils.isNotBlank(wsAttrAssign.getAttributeAssignActionName())) {
          assignNode.put("action", wsAttrAssign.getAttributeAssignActionName());
        }
        assignsArray.add(assignNode);
      }
      resultNode.set("attributeAssigns", assignsArray);
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
