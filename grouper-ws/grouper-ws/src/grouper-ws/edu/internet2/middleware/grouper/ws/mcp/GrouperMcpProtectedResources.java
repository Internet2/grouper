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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Utility to determine whether a group name or stem name refers to a
 * protected system resource that MCP write tools should not modify.
 *
 * <p>Protected resources include:</p>
 * <ul>
 *   <li>The root stem for built-in objects (configured via
 *       <code>grouper.rootStemForBuiltinObjects</code>, default "etc")
 *       and everything under it</li>
 *   <li>Explicitly configured system groups referenced by config properties
 *       (wheel groups, MCP authorization groups, deprovisioning admin group,
 *       workflow editors group, WS client user group, etc.) &mdash; these
 *       are checked even if an admin has moved them outside the etc stem</li>
 * </ul>
 *
 * <p>The sets of protected names are computed lazily on first access and
 * cached for the lifetime of the JVM (until restart).  This is acceptable
 * because the config properties that determine these names do not change
 * at runtime.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpProtectedResources {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpProtectedResources.class);

  /** maximum number of sub-objects (groups + stems) before a stem rename is blocked */
  static final int MAX_SUB_OBJECTS_FOR_RENAME = 5;

  /** cached etc stem prefix, e.g., "etc" */
  private static volatile String etcStemName = null;

  /** cached set of explicitly protected group names (from config properties) */
  private static volatile Set<String> protectedGroupNames = null;

  /** lock object for lazy initialization */
  private static final Object INIT_LOCK = new Object();

  /**
   * lazy initialization of the cached etc stem name and protected group names.
   * uses double-checked locking for thread safety.
   */
  private static void initializeIfNeeded() {
    if (protectedGroupNames != null) {
      return;
    }
    synchronized (INIT_LOCK) {
      if (protectedGroupNames != null) {
        return;
      }

      etcStemName = GrouperConfig.retrieveConfig()
          .propertyValueString("grouper.rootStemForBuiltinObjects", "etc");

      Set<String> names = new HashSet<String>();

      // core sysadmin groups
      addConfigGroupIfPresent(names, "groups.wheel.group");
      addConfigGroupIfPresent(names, "groups.wheel.viewonly.group");
      addConfigGroupIfPresent(names, "groups.wheel.readonly.group");

      // MCP authorization groups
      addConfigGroupIfPresent(names, "grouper.mcp.users.readonly");
      addConfigGroupIfPresent(names, "grouper.mcp.users.readwrite");
      addConfigGroupIfPresent(names, "grouper.mcp.users.wsAuthnAllowed");
      addConfigGroupIfPresent(names, "grouper.mcp.users.canRunSqlReadonly");
      addConfigGroupIfPresent(names, "grouper.mcp.users.adminReadonly");
      addConfigGroupIfPresent(names, "grouper.mcp.users.adminReadWrite");

      // other security groups
      addConfigGroupIfPresent(names, "security.show.all.folders.if.in.group");
      addConfigGroupIfPresent(names, "deprovisioning.admin.group");
      addConfigGroupIfPresent(names, "workflow.editorsGroup");

      // WS client user group (from grouper-ws config, optional)
      try {
        String wsClientGroup = GrouperWsConfigInApi.retrieveConfig()
            .propertyValueString("ws.client.user.group.name");
        if (StringUtils.isNotBlank(wsClientGroup)) {
          names.add(wsClientGroup);
        }
      } catch (Exception e) {
        // WS config may not be available in all environments
        LOG.debug("Could not read ws.client.user.group.name config: " + e.getMessage());
      }

      protectedGroupNames = names;

      LOG.info("MCP protected resources initialized: etcStemName='" + etcStemName
          + "', protectedGroupNames=" + names.size() + " entries");
    }
  }

  /**
   * helper to read a config property value and add it to the set if not blank
   * @param names the set to add to
   * @param propertyName the config property key
   */
  private static void addConfigGroupIfPresent(Set<String> names, String propertyName) {
    String value = GrouperConfig.retrieveConfig().propertyValueString(propertyName);
    if (StringUtils.isNotBlank(value)) {
      names.add(value);
    }
  }

  /**
   * check if a group name refers to a protected system group that should not be
   * modified via MCP tools.
   *
   * <p>A group name is protected if:</p>
   * <ol>
   *   <li>It equals the etc stem name (e.g., "etc")</li>
   *   <li>It starts with the etc stem name followed by ":" (e.g., "etc:anything")</li>
   *   <li>It is in the set of explicitly configured protected group names</li>
   * </ol>
   *
   * @param groupName the fully qualified group name to check
   * @return true if the group is protected and should not be modified
   */
  public static boolean isProtectedGroupName(String groupName) {
    if (StringUtils.isBlank(groupName)) {
      return false;
    }
    initializeIfNeeded();

    // check if under (or is) the etc stem
    if (groupName.equals(etcStemName) || groupName.startsWith(etcStemName + ":")) {
      return true;
    }

    // check explicitly configured protected groups
    return protectedGroupNames.contains(groupName);
  }

  /**
   * check if a stem name refers to a protected system stem that should not be
   * modified via MCP tools.
   *
   * <p>A stem name is protected if:</p>
   * <ol>
   *   <li>It equals the etc stem name (e.g., "etc")</li>
   *   <li>It starts with the etc stem name followed by ":" (e.g., "etc:anything")</li>
   * </ol>
   *
   * @param stemName the fully qualified stem name to check
   * @return true if the stem is protected and should not be modified
   */
  public static boolean isProtectedStemName(String stemName) {
    if (StringUtils.isBlank(stemName)) {
      return false;
    }
    initializeIfNeeded();

    return stemName.equals(etcStemName) || stemName.startsWith(etcStemName + ":");
  }

  /**
   * check if a stem has too many sub-objects (child groups + child stems, recursively)
   * to be renamed via MCP. uses SQL count queries to avoid loading all objects.
   *
   * @param stemName the fully qualified stem name to check
   * @return true if the stem has more than {@link #MAX_SUB_OBJECTS_FOR_RENAME} sub-objects
   */
  public static boolean isStemTooLargeToRename(String stemName) {
    if (StringUtils.isBlank(stemName)) {
      return false;
    }

    try {
      // count child groups under this stem (SUB scope = all descendants)
      // groups whose name starts with "stemName:" are descendants
      String likePattern = stemName + ":%";

      long childGroupCount = new GcDbAccess()
          .sql("SELECT COUNT(*) FROM grouper_groups WHERE name LIKE ?")
          .addBindVar(likePattern)
          .select(Long.class);

      // if already over limit, no need to count stems
      if (childGroupCount > MAX_SUB_OBJECTS_FOR_RENAME) {
        return true;
      }

      long childStemCount = new GcDbAccess()
          .sql("SELECT COUNT(*) FROM grouper_stems WHERE name LIKE ?")
          .addBindVar(likePattern)
          .select(Long.class);

      long total = childGroupCount + childStemCount;
      return total > MAX_SUB_OBJECTS_FOR_RENAME;

    } catch (Exception e) {
      LOG.error("Error counting sub-objects for stem: " + stemName, e);
      // on error, be conservative and block the rename
      return true;
    }
  }

  /**
   * count the total number of sub-objects (child groups + child stems) under a stem
   * using SQL count queries
   * @param stemName the fully qualified stem name
   * @return the total count of child groups + child stems
   */
  public static long countStemSubObjects(String stemName) {
    if (StringUtils.isBlank(stemName)) {
      return 0;
    }
    try {
      String likePattern = stemName + ":%";

      long childGroupCount = new GcDbAccess()
          .sql("SELECT COUNT(*) FROM grouper_groups WHERE name LIKE ?")
          .addBindVar(likePattern)
          .select(Long.class);

      long childStemCount = new GcDbAccess()
          .sql("SELECT COUNT(*) FROM grouper_stems WHERE name LIKE ?")
          .addBindVar(likePattern)
          .select(Long.class);

      return childGroupCount + childStemCount;

    } catch (Exception e) {
      LOG.error("Error counting sub-objects for stem: " + stemName, e);
      return -1;
    }
  }

  /**
   * build an error message for attempting to modify a protected group
   * @param groupName the protected group name
   * @return the error message
   */
  public static String buildProtectedGroupError(String groupName) {
    initializeIfNeeded();
    return "Cannot modify protected system group: " + groupName
        + ". System groups and groups under the '" + etcStemName
        + "' stem are protected from modification via MCP.";
  }

  /**
   * build an error message for attempting to modify a protected stem
   * @param stemName the protected stem name
   * @return the error message
   */
  public static String buildProtectedStemError(String stemName) {
    initializeIfNeeded();
    return "Cannot modify protected system stem: " + stemName
        + ". The '" + etcStemName
        + "' stem and stems under it are protected from modification via MCP.";
  }

  /**
   * build an error message for attempting to rename a stem with too many sub-objects
   * @param stemName the stem name
   * @param count the number of sub-objects
   * @return the error message
   */
  public static String buildStemTooLargeError(String stemName, long count) {
    return "Cannot rename stem '" + stemName + "': it contains " + count
        + " sub-objects (limit is " + MAX_SUB_OBJECTS_FOR_RENAME
        + "). Stems with more than " + MAX_SUB_OBJECTS_FOR_RENAME
        + " child groups and stems cannot be renamed via MCP.";
  }

  /**
   * clear the cached state.  for unit testing only.
   */
  public static void clearCache() {
    synchronized (INIT_LOCK) {
      protectedGroupNames = null;
      etcStemName = null;
    }
  }
}
