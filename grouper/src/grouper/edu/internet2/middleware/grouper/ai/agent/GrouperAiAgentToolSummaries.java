/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpDocSearch;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpInstitutionalTools;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpRecipeTool;

/**
 * what each tool says about itself when the AI agent in the UI offers tools compact
 * ({@link GrouperAiAgentToolCompaction}): a one line summary with any rule a call could break while
 * still succeeding, and, for a tool whose description ends with what this institution configured,
 * where that part starts.
 *
 * <p>kept together rather than in each tool class so the summaries read alike and are reviewed
 * and trimmed together.  everything else about a tool, its parameters included, is in its full
 * definition, which tool_describe returns.  a tool with no summary here is offered in full, and
 * GrouperAiAgentToolCompactionTest checks that every tool has one.</p>
 */
public class GrouperAiAgentToolSummaries {

  /** summaries by tool name */
  private static final Map<String, String> SUMMARIES = new HashMap<String, String>();

  /** where the configured part of a description starts, by tool name */
  private static final Map<String, String> DESCRIPTIONS_KEPT_FROM = new HashMap<String, String>();

  static {

    // readonly

    summary("doc_search", "Search this institution's Grouper documentation, or read more of a "
        + "document a search found.");
    keptFrom("doc_search", GrouperMcpDocSearch.AVAILABLE_DOCUMENTATION_SOURCES);

    summary("attribute_def_name_find", "Find attribute definition names, by part of the name or "
        + "by the exact name.");

    summary("group_find", "Find groups by name, folder, uuid or attribute value.  "
        + "FIND_BY_GROUP_NAME_APPROXIMATE is the usual search.");

    summary("folder_find", "Find folders by name, uuid, parent folder or attribute value.  "
        + "FIND_BY_STEM_NAME_APPROXIMATE is the usual search.");

    summary("attribute_assignment_get", "Get the attributes assigned to groups, folders, members "
        + "or other owners, filtered by attribute name, owner and action.");

    summary("audit_get", "Get audit log entries, filtered by type, group, folder, subject, who "
        + "acted, and date range.  50 at a time by default.");

    summary("privilege_get", "Get the privileges on a group or a folder, optionally for one "
        + "subject or one privilege.  Give groupName or stemName, not both.");

    summary("entity_get_groups", "Find the groups a subject is a member of, or has a privilege "
        + "on.");

    summary("group_get_members", "Get the members of a group, 50 at a time by default.");

    summary("memberships_get", "Get memberships and privileges in detail, including start and "
        + "end dates, for groups, subjects, folders or attribute definitions, now or at a point in "
        + "time.");

    summary("entity_get", "Look up subjects, people and other entities, by id or identifier, or "
        + "search for them by name.  Give subjectIdOrIdentifier or searchString, not both.");

    summary("entity_whoami", "Who is logged in on this connection and what this login can do: "
        + "the subject, MCP access groups, scopes granted and tools available.  Use it to "
        + "troubleshoot an access denied error.");

    summary("group_has_member", "Check whether subjects are members of a group, now or at a "
        + "point in time.");

    summary("institutional_tools", "Run this institution's own tools (GSH templates): action "
        + "'schema' with a template's configId gives its inputs, then 'execute' runs it.");
    keptFrom("institutional_tools", GrouperMcpInstitutionalTools.AVAILABLE_TEMPLATES);

    summary(GrouperMcpRecipeTool.RECIPE_TOOL_NAME, "This institution's recipes: how it wants tasks "
        + "done, which is not always the tool which looks closest.  Read one with action 'get' "
        + "before acting when it covers the task.");
    keptFrom(GrouperMcpRecipeTool.RECIPE_TOOL_NAME, GrouperMcpRecipeTool.AVAILABLE_RECIPES);

    // readwrite

    summary("group_add_member", "Add subjects to a group.  replaceAllExisting true replaces "
        + "every member with the ones given.");

    summary("attribute_assignment_save", "Assign, add, remove or replace attributes and their "
        + "values on groups, folders, members, memberships and attribute definitions, including "
        + "assignments on an assignment (the _asgn types).  replace_attrs and replace_values "
        + "replace what is there.");

    summary("privilege_assign", "Grant (allowed true) or revoke (allowed false) one privilege on "
        + "a group or a folder for a subject.  Give groupName or stemName, not both.");

    summary("group_remove_member", "Remove subjects from a group.");

    summary("folder_delete", "Permanently delete a folder, which must be empty of groups and "
        + "folders.");

    summary("folder_save", "Create a folder, or change its display name and description.  Only "
        + "the fields given change.  It cannot rename or move a folder.");

    summary("group_delete", "Permanently delete a group.");

    summary("group_save", "Create or change a group, its types, composite, eligibility "
        + "requirements and provisioning, or rename or move it.  Only the fields given change.");

    // sql

    summary("sql_get_schema", "List the databases available to SQL (action "
        + "'listExternalSystems', start here), their tables ('listTables'), or one table's "
        + "columns ('tableInfo').");

    summary("sql_select", "Run a read only SELECT on one of the databases available to SQL, a "
        + "page at a time.  No LIMIT, OFFSET or trailing semicolon: use pageSize and pageNumber, "
        + "with an ORDER BY past page 1.");

    // admin

    summary("admin_external_system_get", "Look up a Grouper subject's user in an external system "
        + "such as Azure, Duo or Google: action 'listExternalSystems' first, then 'getUser'.");

    summary("admin_daemon_job_message", "Get the message of one daemon job run, by its id from "
        + "admin_daemon_logs.");

    summary("admin_daemon_logs", "Get the most recent daemon job runs, up to 100, by job name or "
        + "status, without their messages.  Give jobName or status.");

    summary("admin_config_search", "Search Grouper configuration properties by key and value, or "
        + "by key name with a regex.  Secret values are masked.");

    summary("admin_daemon_names", "Find daemon job names.  Each word is matched anywhere in the "
        + "name, all must match, and % is a wildcard.");

    summary("ldap", "Search an LDAP directory configured in Grouper: action "
        + "'listExternalSystems' first, then 'filter' with an LDAP filter.");

    summary("admin_daemon_job_run", "Start a daemon job now, by its exact name from "
        + "admin_daemon_names.  It runs on the daemon server; check it with admin_daemon_logs.");
  }

  /**
   * @param toolName the tool
   * @param summary its summary
   */
  private static void summary(String toolName, String summary) {
    SUMMARIES.put(toolName, summary);
  }

  /**
   * @param toolName the tool
   * @param keptFrom the text the configured part of its description starts with
   */
  private static void keptFrom(String toolName, String keptFrom) {
    DESCRIPTIONS_KEPT_FROM.put(toolName, keptFrom);
  }

  /**
   * @param toolName the tool
   * @return its summary, or null if none was written
   */
  public static String summary(String toolName) {
    return SUMMARIES.get(toolName);
  }

  /**
   * @param toolName the tool
   * @return the text the configured part of its description starts with, or null
   */
  public static String descriptionKeptFrom(String toolName) {
    return DESCRIPTIONS_KEPT_FROM.get(toolName);
  }

}
