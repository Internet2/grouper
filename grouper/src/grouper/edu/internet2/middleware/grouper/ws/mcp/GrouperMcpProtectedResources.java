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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
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
 *   <li>Folders an admin lists in <code>grouper.mcp.protectedFolders</code>,
 *       and everything under them.  MCP refuses every write to these for all
 *       users, sysadmins included, so the change has to be made in the UI</li>
 * </ul>
 *
 * <p>The etc stem and system group names are computed lazily on first access and
 * cached for the lifetime of the JVM (until restart).  This is acceptable
 * because the config properties that determine these names do not change
 * at runtime.  The configured protected folders are read from config on each
 * check (config is itself cached) so an admin can add a folder without a restart.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpProtectedResources {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpProtectedResources.class);

  /** config key listing folders (comma separated) that MCP must not write to */
  public static final String PROTECTED_FOLDERS_CONFIG = "grouper.mcp.protectedFolders";

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
      addConfigGroupIfPresent(names, "grouper.mcp.users.canRunSqlReadonlyLimited");
      addConfigGroupIfPresent(names, "grouper.mcp.users.adminReadonlyLimited");
      addConfigGroupIfPresent(names, "grouper.mcp.users.adminReadWriteLimited");
      addConfigGroupIfPresent(names, "grouper.mcp.users.canSeeStackTraces");

      // who may use the AI agent in the UI, so the agent cannot be used to add people to it
      addConfigGroupIfPresent(names, "grouper.ai.agent.users");

      // groups which raise AI agent limits, so the agent cannot be used to raise its own user's
      // limits by adding them to one
      for (String configId : GrouperUtil.nonNull(GrouperConfig.retrieveConfig()
          .propertyConfigIds(GrouperAiAgentSettings.LIMIT_OVERRIDE_PATTERN))) {
        addConfigGroupIfPresent(names, "grouper.ai.agent.limitOverride." + configId + ".groupName");
      }

      // other security groups
      addConfigGroupIfPresent(names, "security.show.all.folders.if.in.group");
      addConfigGroupIfPresent(names, "deprovisioning.admin.group");
      addConfigGroupIfPresent(names, "workflow.editorsGroup");

      // the loader editors group decides who can run loader jobs from MCP's limited admin
      // readwrite tier (GRP-7415).  it is UI config, read from the API, and usually in etc anyway
      try {
        String loaderEditorsGroup = GrouperUiConfigInApi.retrieveConfig()
            .propertyValueString("uiV2.loader.edit.if.in.group");
        if (StringUtils.isNotBlank(loaderEditorsGroup)) {
          names.add(loaderEditorsGroup);
        }
      } catch (Exception e) {
        // UI config may not be available in all environments
        LOG.debug("Could not read uiV2.loader.edit.if.in.group config: " + e.getMessage());
      }

      // the per tool and per external system limitedAccessGroups which open MCP to the limited
      // tiers do not need listing: they are required to be in etc:mcp, and everything under etc
      // is protected by isProtectedGroupName

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
    if (protectedGroupNames.contains(groupName)) {
      return true;
    }

    // check folders the admin configured as protected from MCP
    return protectedFolderForGroupName(groupName) != null;
  }

  /**
   * the folders configured in grouper.mcp.protectedFolders.  read on each call so a
   * config change takes effect without a restart (the config itself is cached)
   * @return the folder names, trailing colons removed, or an empty list if none configured
   */
  public static List<String> protectedFolderNames() {
    List<String> result = new ArrayList<String>();
    String value = GrouperConfig.retrieveConfig().propertyValueString(PROTECTED_FOLDERS_CONFIG);
    if (StringUtils.isBlank(value)) {
      return result;
    }
    for (String folderName : GrouperUtil.splitTrim(value, ",")) {
      // tolerate "app:payroll:" so it does not turn into a prefix that matches nothing
      folderName = StringUtils.stripEnd(StringUtils.trimToEmpty(folderName), ":");
      if (StringUtils.isNotBlank(folderName)) {
        result.add(folderName);
      }
    }
    return result;
  }

  /**
   * find the configured protected folder that a name is, or is under.  the match is
   * on whole folder names, so app:payrollX is not under app:payroll
   * @param folders the configured protected folders
   * @param name a group or stem name
   * @return the protected folder, or null if the name is not in one
   */
  private static String protectedFolderMatching(List<String> folders, String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    for (String folderName : folders) {
      if (name.equals(folderName) || name.startsWith(folderName + ":")) {
        return folderName;
      }
    }
    return null;
  }

  /**
   * find the configured protected folder a group is in.  the name is checked as given,
   * and if that does not match, the group is looked up so a name which is an alternate
   * (old) name of a group that was moved into a protected folder is also caught.
   * the lookup is only done when folders are configured, so there is no extra query otherwise
   * @param groupName the group name as the MCP client sent it
   * @return the protected folder, or null if the group is not in one
   */
  public static String protectedFolderForGroupName(final String groupName) {
    List<String> folders = protectedFolderNames();
    if (folders.isEmpty() || StringUtils.isBlank(groupName)) {
      return null;
    }
    String folderName = protectedFolderMatching(folders, groupName);
    if (folderName != null) {
      return folderName;
    }
    // look up as root: whether the caller can see the group has nothing to do with
    // whether it is protected.  findByName resolves alternate names too
    Group group = (Group) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GroupFinder.findByName(groupName, false);
      }
    });
    if (group == null || StringUtils.equals(group.getName(), groupName)) {
      return null;
    }
    return protectedFolderMatching(folders, group.getName());
  }

  /**
   * find the configured protected folder a stem is, or is in.  like
   * {@link #protectedFolderForGroupName(String)} this also resolves an alternate (old) name
   * @param stemName the stem name as the MCP client sent it
   * @return the protected folder, or null if the stem is not in one
   */
  public static String protectedFolderForStemName(final String stemName) {
    List<String> folders = protectedFolderNames();
    if (folders.isEmpty() || StringUtils.isBlank(stemName)) {
      return null;
    }
    String folderName = protectedFolderMatching(folders, stemName);
    if (folderName != null) {
      return folderName;
    }
    // look up as root, see protectedFolderForGroupName
    Stem stem = (Stem) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return StemFinder.findByName(stemName, false);
      }
    });
    if (stem == null || StringUtils.equals(stem.getName(), stemName)) {
      return null;
    }
    return protectedFolderMatching(folders, stem.getName());
  }

  /**
   * the first folder in a readwrite consent scope which MCP refuses to write to: the etc
   * folder or a folder in grouper.mcp.protectedFolders, or anything under them.  used by the
   * OAuth consent screen so a consent which could never be used is not granted.  a folder which
   * only contains a protected folder is not returned, the tools block the protected part on their own
   * @param folderNames the folder names picked for the consent
   * @return the protected folder name, or null if none
   */
  public static String firstProtectedScopeFolderName(List<String> folderNames) {
    for (String folderName : GrouperUtil.nonNull(folderNames)) {
      if (isProtectedStemName(folderName)) {
        return folderName;
      }
    }
    return null;
  }

  /**
   * the first group in a readwrite consent scope which MCP refuses to write to: a system
   * group, or a group under the etc folder or a folder in grouper.mcp.protectedFolders
   * @param groupNames the group names picked for the consent
   * @return the protected group name, or null if none
   */
  public static String firstProtectedScopeGroupName(List<String> groupNames) {
    for (String groupName : GrouperUtil.nonNull(groupNames)) {
      if (isProtectedGroupName(groupName)) {
        return groupName;
      }
    }
    return null;
  }

  /**
   * build the error for a write to a configured protected folder.  it names the folder
   * and sends the person to the UI, where the change can still be made
   * @param objectType "group" or "folder"
   * @param objectName the name the MCP client sent
   * @param protectedFolderName the configured protected folder it is in
   * @return the error message
   */
  static String buildProtectedFolderError(String objectType, String objectName, String protectedFolderName) {
    StringBuilder result = new StringBuilder();
    result.append("Cannot modify ").append(objectType).append(" '").append(objectName)
        .append("' via MCP: it is in the folder '").append(protectedFolderName)
        .append("', which an administrator has protected from all changes made through MCP (")
        .append(PROTECTED_FOLDERS_CONFIG).append("). Make this change in the Grouper UI instead");
    String uiUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.ui.url");
    if (StringUtils.isNotBlank(uiUrl)) {
      result.append(": ").append(StringUtils.stripEnd(uiUrl, "/"))
          .append("/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=")
          .append(GrouperUtil.escapeUrlEncode(protectedFolderName));
    } else {
      result.append(".");
    }
    return result.toString();
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

    if (stemName.equals(etcStemName) || stemName.startsWith(etcStemName + ":")) {
      return true;
    }

    // check folders the admin configured as protected from MCP
    return protectedFolderForStemName(stemName) != null;
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
    // a configured protected folder gets its own message which names the folder
    String protectedFolderName = protectedFolderForGroupName(groupName);
    if (protectedFolderName != null) {
      return buildProtectedFolderError("group", groupName, protectedFolderName);
    }
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
    // a configured protected folder gets its own message which names the folder
    String protectedFolderName = protectedFolderForStemName(stemName);
    if (protectedFolderName != null) {
      return buildProtectedFolderError("folder", stemName, protectedFolderName);
    }
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
