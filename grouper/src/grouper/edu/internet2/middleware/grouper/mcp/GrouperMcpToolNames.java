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
package edu.internet2.middleware.grouper.mcp;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The names of the MCP tools Grouper advertises.
 *
 * <p>This lives in the API module because two things outside grouper-ws need it and cannot see
 * that module's classes: the recipe configuration, which rejects a recipe pointing at a tool
 * which does not exist, and the recipes screen, which tells a deployer when a recipe that used
 * to work has stopped pointing anywhere.</p>
 *
 * <p>The list is kept honest from the grouper-ws side rather than by hoping somebody remembers
 * it: {@code addToolIfAllowed} refuses to advertise a tool which is not named here, so adding a
 * tool without registering it fails immediately rather than quietly making recipes about it
 * unwritable.  A unit test in grouper-ws checks the dispatch covers these names too.</p>
 *
 * <p>The dispatch in the servlet is a superset of this list: {@code sql_select_count} is still
 * answered for older clients but is not advertised, so it is not something a recipe should point
 * at.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpToolNames {

  /** every tool name Grouper advertises, in the order the servlet lists them */
  private static final Set<String> TOOL_NAMES;

  static {

    Set<String> toolNames = new LinkedHashSet<String>();

    // readonly
    toolNames.add("doc_search");
    toolNames.add("attribute_def_name_find");
    toolNames.add("group_find");
    toolNames.add("folder_find");
    toolNames.add("attribute_assignment_get");
    toolNames.add("audit_get");
    toolNames.add("privilege_get");
    toolNames.add("entity_get_groups");
    toolNames.add("group_get_members");
    toolNames.add("memberships_get");
    toolNames.add("entity_get");
    toolNames.add("group_has_member");
    toolNames.add("institutional_tools");
    toolNames.add("recipe");

    // readwrite
    toolNames.add("group_add_member");
    toolNames.add("attribute_assignment_save");
    toolNames.add("privilege_assign");
    toolNames.add("group_remove_member");
    toolNames.add("folder_delete");
    toolNames.add("group_delete");
    toolNames.add("group_save");

    // sql readonly
    toolNames.add("sql_get_schema");
    toolNames.add("sql_select");

    // admin
    toolNames.add("admin_external_system_get");
    toolNames.add("admin_daemon_job_message");
    toolNames.add("admin_daemon_logs");
    toolNames.add("admin_config_search");
    toolNames.add("admin_daemon_names");
    toolNames.add("ldap");
    toolNames.add("admin_daemon_job_run");

    TOOL_NAMES = Collections.unmodifiableSet(toolNames);
  }

  /**
   * every tool name Grouper advertises
   * @return the tool names, unmodifiable
   */
  public static Set<String> toolNames() {
    return TOOL_NAMES;
  }

  /**
   * whether a name is one of Grouper's MCP tools
   * @param toolName the name to check
   * @return true if Grouper advertises a tool by that name
   */
  public static boolean isToolName(String toolName) {
    return toolName != null && TOOL_NAMES.contains(toolName);
  }

}
