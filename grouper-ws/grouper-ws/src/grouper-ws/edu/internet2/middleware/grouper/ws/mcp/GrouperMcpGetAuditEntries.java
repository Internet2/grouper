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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * MCP tool handler for getting audit log entries from Grouper.
 * Supports filtering by audit type, group, stem, subject, date range, and paging.
 * Uses UserAuditQuery directly (same approach as the UI) for privilege checking:
 * group audits require admin on the group, stem audits require stemAdmin on the stem.
 *
 * @author mchyzer
 */
public class GrouperMcpGetAuditEntries {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGetAuditEntries.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  /**
   * return the MCP tool definition for audit_get
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "audit_get");
    tool.put("description",
        "Get audit log entries from Grouper. "
        + "Supports filtering by audit type, group, stem, subject, "
        + "actions performed by a specific subject, and date range. "
        + "Defaults to page size 50 to prevent returning too many results.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode auditTypeProp = objectMapper.createObjectNode();
    auditTypeProp.put("type", "string");
    auditTypeProp.put("description",
        "Audit type category to filter by. "
        + "Common values: group, stem, membership, privilege. "
        + "Other values: entity, member, groupType, groupField, groupComposite, "
        + "groupAttestation, stemAttestation, attributeDef, attributeDefName, "
        + "provisionerSync, gshTemplate, oauth, configurationFile. "
        + "Optional - if not specified, all audit types are returned for the entity.");
    properties.set("auditType", auditTypeProp);

    ObjectNode groupNameProp = objectMapper.createObjectNode();
    groupNameProp.put("type", "string");
    groupNameProp.put("description",
        "Filter to audit entries for this group "
        + "(e.g., 'stem1:stem2:groupName'). "
        + "Requires admin privilege on the group.");
    properties.set("groupName", groupNameProp);

    ObjectNode stemNameProp = objectMapper.createObjectNode();
    stemNameProp.put("type", "string");
    stemNameProp.put("description",
        "Filter to audit entries for this stem "
        + "(e.g., 'stem1:stem2'). "
        + "Requires stemAdmin privilege on the stem.");
    properties.set("stemName", stemNameProp);

    ObjectNode subjectIdProp = objectMapper.createObjectNode();
    subjectIdProp.put("type", "string");
    subjectIdProp.put("description",
        "Subject ID to get audit entries about.");
    properties.set("subjectId", subjectIdProp);

    ObjectNode subjectSourceIdProp = objectMapper.createObjectNode();
    subjectSourceIdProp.put("type", "string");
    subjectSourceIdProp.put("description",
        "Source ID for the subject to get audit entries about.");
    properties.set("subjectSourceId", subjectSourceIdProp);

    ObjectNode actionsPerformedBySubjectIdProp = objectMapper.createObjectNode();
    actionsPerformedBySubjectIdProp.put("type", "string");
    actionsPerformedBySubjectIdProp.put("description",
        "Subject ID of the user who performed the actions.");
    properties.set("actionsPerformedBySubjectId", actionsPerformedBySubjectIdProp);

    ObjectNode actionsPerformedBySubjectSourceIdProp = objectMapper.createObjectNode();
    actionsPerformedBySubjectSourceIdProp.put("type", "string");
    actionsPerformedBySubjectSourceIdProp.put("description",
        "Source ID for the subject who performed the actions.");
    properties.set("actionsPerformedBySubjectSourceId", actionsPerformedBySubjectSourceIdProp);

    ObjectNode fromDateProp = objectMapper.createObjectNode();
    fromDateProp.put("type", "string");
    fromDateProp.put("description",
        "Start date for the audit query, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/01/01 00:00:00.000').");
    properties.set("fromDate", fromDateProp);

    ObjectNode toDateProp = objectMapper.createObjectNode();
    toDateProp.put("type", "string");
    toDateProp.put("description",
        "End date for the audit query, "
        + "in format yyyy/MM/dd HH:mm:ss.SSS (e.g., '2025/12/31 23:59:59.000').");
    properties.set("toDate", toDateProp);

    ObjectNode pageSizeProp = objectMapper.createObjectNode();
    pageSizeProp.put("type", "integer");
    pageSizeProp.put("description",
        "Number of audit entries to return. Defaults to 50.");
    pageSizeProp.put("default", 50);
    properties.set("pageSize", pageSizeProp);

    inputSchema.set("properties", properties);

    // no required fields - all filters are optional

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the audit_get tool using UserAuditQuery directly (like the UI).
   * Group audits require admin privilege on the group.
   * Stem audits require stemAdmin privilege on the stem.
   * Subject/action audits require admin or being the subject.
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String auditType = arguments != null && arguments.has("auditType")
        ? arguments.get("auditType").asText() : null;
    String groupName = arguments != null && arguments.has("groupName")
        ? arguments.get("groupName").asText() : null;
    String stemName = arguments != null && arguments.has("stemName")
        ? arguments.get("stemName").asText() : null;
    String subjectId = arguments != null && arguments.has("subjectId")
        ? arguments.get("subjectId").asText() : null;
    String subjectSourceId = arguments != null && arguments.has("subjectSourceId")
        ? arguments.get("subjectSourceId").asText() : null;
    String actionsPerformedBySubjectId = arguments != null
        && arguments.has("actionsPerformedBySubjectId")
        ? arguments.get("actionsPerformedBySubjectId").asText() : null;
    String actionsPerformedBySubjectSourceId = arguments != null
        && arguments.has("actionsPerformedBySubjectSourceId")
        ? arguments.get("actionsPerformedBySubjectSourceId").asText() : null;
    String fromDateString = arguments != null && arguments.has("fromDate")
        ? arguments.get("fromDate").asText() : null;
    String toDateString = arguments != null && arguments.has("toDate")
        ? arguments.get("toDate").asText() : null;
    int pageSize = arguments != null && arguments.has("pageSize")
        ? arguments.get("pageSize").asInt() : 50;

    // require at least one filter
    if (StringUtils.isBlank(groupName) && StringUtils.isBlank(stemName)
        && StringUtils.isBlank(subjectId) && StringUtils.isBlank(actionsPerformedBySubjectId)
        && StringUtils.isBlank(auditType)) {
      return buildErrorResult("At least one filter is required: "
          + "groupName, stemName, subjectId, actionsPerformedBySubjectId, or auditType.");
    }

    try {

      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Subject authenticatedSubject = authUser.getSubject();
      boolean isAdmin = PrivilegeHelper.isWheelOrRoot(authenticatedSubject);

      UserAuditQuery query = new UserAuditQuery();

      // paging and sorting
      QueryOptions queryOptions = new QueryOptions();
      queryOptions.paging(QueryPaging.page(pageSize, 1, false));
      queryOptions.sortDesc("lastUpdatedDb");
      query.setQueryOptions(queryOptions);

      // date filters
      if (StringUtils.isNotBlank(fromDateString)) {
        query.setFromDate(GrouperServiceUtils.stringToTimestamp(fromDateString));
      }
      if (StringUtils.isNotBlank(toDateString)) {
        query.setToDate(GrouperServiceUtils.stringToTimestamp(toDateString));
      }

      // group filter: requires admin privilege on the group
      if (StringUtils.isNotBlank(groupName)) {
        Group group = GroupFinder.findByName(grouperSession, groupName, false);
        if (group == null) {
          return buildErrorResult("Group not found: " + groupName);
        }
        if (!isAdmin
            && !group.canHavePrivilege(authenticatedSubject,
                AccessPrivilege.ADMIN.getName(), false)) {
          return buildErrorResult(
              "Access denied: admin privilege on the group is required to view audit entries.");
        }
        query.addAuditTypeFieldValue("groupId", group.getId());
      }

      // stem filter: requires stemAdmin privilege on the stem
      if (StringUtils.isNotBlank(stemName)) {
        Stem stem = StemFinder.findByName(grouperSession, stemName, false);
        if (stem == null) {
          return buildErrorResult("Stem not found: " + stemName);
        }
        if (!isAdmin
            && !stem.canHavePrivilege(authenticatedSubject,
                NamingPrivilege.STEM_ADMIN.getName(), false)) {
          return buildErrorResult(
              "Access denied: stemAdmin privilege on the stem is required to view audit entries.");
        }
        query.addAuditTypeFieldValue("stemId", stem.getId());
      }

      // subject filter: audit entries about this subject
      if (StringUtils.isNotBlank(subjectId)) {
        Subject subject = StringUtils.isNotBlank(subjectSourceId)
            ? SubjectFinder.findByIdAndSource(subjectId, subjectSourceId, false)
            : SubjectFinder.findById(subjectId, false);
        if (subject == null) {
          return buildErrorResult("Subject not found: " + subjectId);
        }
        // allow if admin or looking at own audit entries
        if (!isAdmin && !subjectsEqual(authenticatedSubject, subject)) {
          return buildErrorResult(
              "Access denied: admin privilege or being the subject is required "
              + "to view audit entries about a subject.");
        }
        Member member = MemberFinder.findBySubject(grouperSession, subject, false);
        if (member == null) {
          return buildErrorResult("Member not found for subject: " + subjectId);
        }
        query.addAuditTypeFieldValue("memberId", member.getUuid());
      }

      // actions performed by filter
      if (StringUtils.isNotBlank(actionsPerformedBySubjectId)) {
        Subject performer = StringUtils.isNotBlank(actionsPerformedBySubjectSourceId)
            ? SubjectFinder.findByIdAndSource(actionsPerformedBySubjectId,
                actionsPerformedBySubjectSourceId, false)
            : SubjectFinder.findById(actionsPerformedBySubjectId, false);
        if (performer == null) {
          return buildErrorResult("Subject not found: " + actionsPerformedBySubjectId);
        }
        // allow if admin or looking at own actions
        if (!isAdmin && !subjectsEqual(authenticatedSubject, performer)) {
          return buildErrorResult(
              "Access denied: admin privilege or being the subject is required "
              + "to view actions performed by a subject.");
        }
        Member member = MemberFinder.findBySubject(grouperSession, performer, false);
        if (member == null) {
          return buildErrorResult(
              "Member not found for subject: " + actionsPerformedBySubjectId);
        }
        query.loggedInMember(member);
        query.actAsMember(member);
      }

      // audit type category filter
      if (StringUtils.isNotBlank(auditType)) {
        query.addAuditTypeCategory(auditType);
      }

      // if only auditType with no entity filter, require admin
      if (StringUtils.isBlank(groupName) && StringUtils.isBlank(stemName)
          && StringUtils.isBlank(subjectId) && StringUtils.isBlank(actionsPerformedBySubjectId)
          && !isAdmin) {
        return buildErrorResult(
            "Access denied: admin privilege is required to query audit entries by type only.");
      }

      // execute query
      List<AuditEntry> auditEntries = query.execute();

      if (GrouperUtil.length(auditEntries) == 0) {
        return buildSuccessResult("No audit entries found matching the criteria.");
      }

      // collect member UUIDs from audit entry fields for batch resolution
      Set<String> memberUuids = new HashSet<>();
      for (AuditEntry auditEntry : auditEntries) {
        collectMemberUuids(auditEntry, memberUuids);
        if (StringUtils.isNotBlank(auditEntry.getLoggedInMemberId())) {
          memberUuids.add(auditEntry.getLoggedInMemberId());
        }
      }

      // batch-resolve member UUIDs to subject info
      Map<String, String[]> memberUuidToSubjectInfo = resolveMemberUuids(
          grouperSession, memberUuids);

      ArrayNode entriesArray = objectMapper.createArrayNode();
      for (AuditEntry auditEntry : auditEntries) {
        entriesArray.add(convertAuditEntryToJson(auditEntry, memberUuidToSubjectInfo));
      }

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(entriesArray);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error getting audit entries", e);
      return buildErrorResult("Error getting audit entries: " + e.getMessage());
    }
  }

  /**
   * compare two subjects by id and source
   * @param a first subject
   * @param b second subject
   * @return true if same subject
   */
  private static boolean subjectsEqual(Subject a, Subject b) {
    return StringUtils.equals(a.getId(), b.getId())
        && StringUtils.equals(a.getSourceId(), b.getSourceId());
  }

  /**
   * collect member UUIDs from the labeled fields of an audit entry
   * @param entry the audit entry
   * @param memberUuids set to add member UUIDs to
   */
  private static void collectMemberUuids(AuditEntry entry, Set<String> memberUuids) {
    AuditType auditType = entry.getAuditType();
    if (auditType == null) {
      return;
    }
    for (int i = 1; i <= 8; i++) {
      String label = getStringLabel(auditType, i);
      if ("memberId".equals(label)) {
        String value = getStringField(entry, i);
        if (StringUtils.isNotBlank(value)) {
          memberUuids.add(value);
        }
      }
    }
  }

  /**
   * batch-resolve member UUIDs to subject info (subjectId, sourceId, description).
   * @param grouperSession the session
   * @param memberUuids the member UUIDs to resolve
   * @return map of member UUID to String[]{subjectId, sourceId, description}
   */
  private static Map<String, String[]> resolveMemberUuids(
      GrouperSession grouperSession, Set<String> memberUuids) {
    Map<String, String[]> memberUuidToSubjectInfo = new HashMap<>();
    for (String memberUuid : memberUuids) {
      try {
        Member member = MemberFinder.findByUuid(grouperSession, memberUuid, false);
        if (member != null) {
          String localSubjectId = member.getSubjectId();
          String sourceId = member.getSubjectSourceId();
          String description = null;
          try {
            Subject subject = member.getSubject();
            if (subject != null) {
              description = subject.getDescription();
            }
          } catch (Exception e) {
            LOG.debug("Could not resolve subject for member: " + memberUuid, e);
          }
          memberUuidToSubjectInfo.put(memberUuid,
              new String[] { localSubjectId, sourceId, description });
        }
      } catch (Exception e) {
        LOG.debug("Could not find member UUID: " + memberUuid, e);
      }
    }
    return memberUuidToSubjectInfo;
  }

  /**
   * convert an AuditEntry to a clean JSON object for MCP consumption.
   * @param auditEntry the audit entry
   * @param memberUuidToSubjectInfo map of member UUID to String[]{subjectId, sourceId, description}
   * @return clean JSON object
   */
  private static ObjectNode convertAuditEntryToJson(AuditEntry auditEntry,
      Map<String, String[]> memberUuidToSubjectInfo) {
    ObjectNode entryNode = objectMapper.createObjectNode();
    entryNode.put("id", auditEntry.getId());

    AuditType auditType = auditEntry.getAuditType();
    if (auditType != null) {
      if (StringUtils.isNotBlank(auditType.getAuditCategory())) {
        entryNode.put("auditCategory", auditType.getAuditCategory());
      }
      if (StringUtils.isNotBlank(auditType.getActionName())) {
        entryNode.put("actionName", auditType.getActionName());
      }
    }

    if (auditEntry.getCreatedOn() != null) {
      synchronized (TIMESTAMP_FORMAT) {
        entryNode.put("timestamp", TIMESTAMP_FORMAT.format(auditEntry.getCreatedOn()));
      }
    }

    if (StringUtils.isNotBlank(auditEntry.getDescription())) {
      entryNode.put("description", auditEntry.getDescription());
    }

    // logged-in member who performed the action
    if (StringUtils.isNotBlank(auditEntry.getLoggedInMemberId())) {
      String[] subjectInfo = memberUuidToSubjectInfo.get(auditEntry.getLoggedInMemberId());
      if (subjectInfo != null) {
        entryNode.put("performedBySubjectId", subjectInfo[0]);
        entryNode.put("performedBySourceId", subjectInfo[1]);
      }
    }

    // labeled fields from the audit type
    if (auditType != null) {
      ObjectNode entriesObject = objectMapper.createObjectNode();
      addLabeledStringFields(entriesObject, auditEntry, auditType, memberUuidToSubjectInfo);
      addLabeledIntFields(entriesObject, auditEntry, auditType);
      if (entriesObject.size() > 0) {
        entryNode.set("entries", entriesObject);
      }
    }

    return entryNode;
  }

  /**
   * add labeled string fields (string01-string08) to the entries object
   */
  private static void addLabeledStringFields(ObjectNode entriesObject,
      AuditEntry entry, AuditType auditType,
      Map<String, String[]> memberUuidToSubjectInfo) {
    for (int i = 1; i <= 8; i++) {
      String label = getStringLabel(auditType, i);
      String value = getStringField(entry, i);
      if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(value)) {
        if ("memberId".equals(label)) {
          // resolve member UUID to subject info
          String[] subjectInfo = memberUuidToSubjectInfo.get(value);
          if (subjectInfo != null) {
            entriesObject.put("subjectId", subjectInfo[0]);
            entriesObject.put("sourceId", subjectInfo[1]);
            if (StringUtils.isNotBlank(subjectInfo[2])) {
              entriesObject.put("subjectDescription", subjectInfo[2]);
            }
          } else {
            entriesObject.put(label, value);
          }
        } else {
          entriesObject.put(label, value);
        }
      }
    }
  }

  /**
   * add labeled int fields (int01-int05) to the entries object
   */
  private static void addLabeledIntFields(ObjectNode entriesObject,
      AuditEntry entry, AuditType auditType) {
    for (int i = 1; i <= 5; i++) {
      String label = getIntLabel(auditType, i);
      Long value = getIntField(entry, i);
      if (StringUtils.isNotBlank(label) && value != null) {
        entriesObject.put(label, value);
      }
    }
  }

  /**
   * get the label for a string field on an AuditType
   */
  private static String getStringLabel(AuditType auditType, int index) {
    switch (index) {
      case 1: return auditType.getLabelString01();
      case 2: return auditType.getLabelString02();
      case 3: return auditType.getLabelString03();
      case 4: return auditType.getLabelString04();
      case 5: return auditType.getLabelString05();
      case 6: return auditType.getLabelString06();
      case 7: return auditType.getLabelString07();
      case 8: return auditType.getLabelString08();
      default: return null;
    }
  }

  /**
   * get the value of a string field on an AuditEntry
   */
  private static String getStringField(AuditEntry entry, int index) {
    switch (index) {
      case 1: return entry.getString01();
      case 2: return entry.getString02();
      case 3: return entry.getString03();
      case 4: return entry.getString04();
      case 5: return entry.getString05();
      case 6: return entry.getString06();
      case 7: return entry.getString07();
      case 8: return entry.getString08();
      default: return null;
    }
  }

  /**
   * get the label for an int field on an AuditType
   */
  private static String getIntLabel(AuditType auditType, int index) {
    switch (index) {
      case 1: return auditType.getLabelInt01();
      case 2: return auditType.getLabelInt02();
      case 3: return auditType.getLabelInt03();
      case 4: return auditType.getLabelInt04();
      case 5: return auditType.getLabelInt05();
      default: return null;
    }
  }

  /**
   * get the value of an int field on an AuditEntry
   */
  private static Long getIntField(AuditEntry entry, int index) {
    switch (index) {
      case 1: return entry.getInt01();
      case 2: return entry.getInt02();
      case 3: return entry.getInt03();
      case 4: return entry.getInt04();
      case 5: return entry.getInt05();
      default: return null;
    }
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
