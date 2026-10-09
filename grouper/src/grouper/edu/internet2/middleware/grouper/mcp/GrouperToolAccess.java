/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.subject.Subject;

/**
 * whether a caller is allowed to run a tool in a given category.
 *
 * <p>this is an intersection and never a grant: the caller has to be in the Grouper group for the
 * category, and, where a scope applies (see {@link GrouperMcpAuthUser#isConsentScopeEnforced()}),
 * to have the matching scope.  neither half can stand in for the other.  if it were computed from
 * the consent record alone, the consent screen would become a way to hand yourself access you do
 * not have.</p>
 *
 * <p>group membership is checked against Grouper rather than read from the token, so removing
 * somebody from the group takes effect within a minute even while they hold an unexpired token
 * (the answer is cached for sixty seconds on each node, see {@link GrouperMcpGroupMembership}).
 * membership of the wheel group does not grant any of this.</p>
 *
 * <p>the sql and admin readonly categories each have two tiers (GRP-7415).  the "all" tier is the
 * original group, and gets every tool and every external system in the category, but only for a
 * Grouper sysadmin: wheel for sql and admin readwrite, wheel or readonly sysadmin for admin
 * readonly.  being in the group without being a sysadmin gives nothing, so nobody can be handed
 * the whole of these categories by accident.  the "limited" tier is a second group with no
 * sysadmin requirement, whose members only get the tools and external systems the institution
 * has opened to them one at a time with a limitedAccessGroup: per tool with
 * grouper.mcp.tool.&lt;toolName&gt;.limitedAccessGroup, or, for tools which reach external systems,
 * per system with grouper.mcp.sql|ldap|adminExternalSystem.&lt;id&gt;.limitedAccessGroup.  a tool or
 * system with no limitedAccessGroup is not available to the limited tier at all, and a
 * limitedAccessGroup has to be in etc:mcp, which MCP itself can never change.</p>
 *
 * <p>the tiers fail closed: only a caller positively on the all tier is unrestricted.  a caller on
 * neither tier, or whose membership could not be looked up, gets nothing.</p>
 *
 * <p>admin readwrite also has a limited tier, but it is not opened with config.  it follows what
 * the UI allows a non sysadmin to run, {@link
 * edu.internet2.middleware.grouper.privs.PrivilegeHelper#canRunLoaderJob}: the loader job of a
 * group the caller can refresh in the UI, and nothing else, so provisioning and other daemon jobs
 * stay sysadmin only.  the admin_daemon_job_run tool checks that per job.</p>
 *
 * <p>it lives here rather than on the MCP servlet so that the UI is held to the same rule.  for an
 * OAuth caller the scope is what they consented to for that client; for the AI agent in the UI it
 * is the scope the user chose for the session, carried in the same flags.  either way it can only
 * narrow what group membership allows, tiers included.</p>
 */
public class GrouperToolAccess {

  /** config name of the group whose members may read */
  public static final String GROUP_READONLY = "grouper.mcp.users.readonly";

  /** config name of the group whose members may write */
  public static final String GROUP_READWRITE = "grouper.mcp.users.readwrite";

  /** config name of the group whose members may run sql */
  public static final String GROUP_SQL_READONLY = "grouper.mcp.users.canRunSqlReadonly";

  /** config name of the group whose members may read config and daemons */
  public static final String GROUP_ADMIN_READONLY = "grouper.mcp.users.adminReadonly";

  /** config name of the group whose members may run daemon jobs.  note the capital W, which
   * matches the property as it has always been spelled */
  public static final String GROUP_ADMIN_READWRITE = "grouper.mcp.users.adminReadWrite";

  /** config name of the group whose members may run sql, but only on the databases opened to
   * them with grouper.mcp.sql.&lt;id&gt;.limitedAccessGroup.  no sysadmin requirement */
  public static final String GROUP_SQL_READONLY_LIMITED =
      "grouper.mcp.users.canRunSqlReadonlyLimited";

  /** config name of the group whose members may use admin readonly tools, but only the tools and
   * external systems opened to them with a limitedAccessGroup.  no sysadmin requirement */
  public static final String GROUP_ADMIN_READONLY_LIMITED =
      "grouper.mcp.users.adminReadonlyLimited";

  /** config name of the group whose members may run the loader jobs of groups they could refresh
   * in the UI, and no other daemon job.  no sysadmin requirement */
  public static final String GROUP_ADMIN_READWRITE_LIMITED =
      "grouper.mcp.users.adminReadWriteLimited";

  /** the property suffix, after the tool name or external system id, which names the group whose
   * members on a limited tier may use that tool or external system */
  public static final String LIMITED_ACCESS_GROUP_SUFFIX = ".limitedAccessGroup";

  /** config prefix for the per tool limited access group, e.g.
   * grouper.mcp.tool.admin_daemon_logs.limitedAccessGroup */
  public static final String TOOL_CONFIG_PREFIX = "grouper.mcp.tool.";

  /** config prefix of the sql external systems */
  public static final String SQL_CONFIG_PREFIX = "grouper.mcp.sql.";

  /** config prefix of the ldap external systems */
  public static final String LDAP_CONFIG_PREFIX = "grouper.mcp.ldap.";

  /** config prefix of the external systems admin_external_system_get looks users up in */
  public static final String ADMIN_EXTERNAL_SYSTEM_CONFIG_PREFIX =
      "grouper.mcp.adminExternalSystem.";

  /**
   * whether this caller may run this tool.  this is {@link #isAllowed(GrouperToolCategory,
   * GrouperMcpAuthUser)} plus, for a category with a limited tier, that the caller is either on its
   * all tier or on its limited tier with the tool opened to them.  a tool which reaches external
   * systems is opened per system rather than per tool, and running a daemon job is decided per job,
   * so those are allowed here and the tool itself only lists and acts on what is opened to the
   * caller (see {@link GrouperTool#limitedAccessCheckedByTool()} and
   * {@link #isExternalSystemAllowed})
   *
   * <p>this fails closed: only a caller positively on the all tier is unrestricted.  a caller on
   * neither tier, including one whose membership could not be looked up, gets nothing, even if the
   * category check let them in a moment ago and their membership has changed since.</p>
   * @param grouperTool the tool
   * @param category what this call needs to be allowed to do, which can depend on the arguments
   * @param authUser the caller
   * @return true if allowed
   */
  public static boolean isAllowed(GrouperTool grouperTool, GrouperToolCategory category,
      GrouperMcpAuthUser authUser) {

    if (grouperTool == null || !isAllowed(category, authUser)) {
      return false;
    }

    // categories with no limited tier are decided by the category check alone
    if (!hasLimitedTier(category)) {
      return true;
    }

    Subject subject = authUser.getSubject();

    if (isAllTier(category, subject)) {
      return true;
    }

    if (!isLimitedTier(category, subject)) {
      return false;
    }

    if (grouperTool.limitedAccessCheckedByTool()) {
      return true;
    }

    return isInLimitedAccessGroup(subject,
        TOOL_CONFIG_PREFIX + grouperTool.name() + LIMITED_ACCESS_GROUP_SUFFIX);
  }

  /**
   * whether this caller may use this external system from a tool in this category.  a caller on
   * the all tier may use every external system.  a caller on the limited tier may only use a
   * system whose limitedAccessGroup they are in.  anybody else may use none, so this fails closed
   * even if it is reached without the category having been checked first
   * @param category the category of the tool, sql or admin_readonly
   * @param configPrefix the config prefix of the kind of external system, e.g.
   * {@link #SQL_CONFIG_PREFIX}
   * @param externalSystemId the external system id
   * @param authUser the caller
   * @return true if allowed
   */
  public static boolean isExternalSystemAllowed(GrouperToolCategory category,
      String configPrefix, String externalSystemId, GrouperMcpAuthUser authUser) {

    Subject subject = authUser == null ? null : authUser.getSubject();

    // only the tiered categories reach external systems.  anything else asking is a mistake,
    // and is refused rather than allowed
    if (subject == null || !hasLimitedTier(category)) {
      return false;
    }

    if (isAllTier(category, subject)) {
      return true;
    }

    if (!isLimitedTier(category, subject) || StringUtils.isBlank(externalSystemId)) {
      return false;
    }

    return isInLimitedAccessGroup(subject,
        configPrefix + externalSystemId.trim() + LIMITED_ACCESS_GROUP_SUFFIX);
  }

  /**
   * whether this category has a limited tier: sql, admin readonly and admin readwrite
   * @param category the category
   * @return true if it has tiers
   */
  public static boolean hasLimitedTier(GrouperToolCategory category) {
    return category == GrouperToolCategory.sql
        || category == GrouperToolCategory.admin_readonly
        || category == GrouperToolCategory.admin_readwrite;
  }

  /**
   * whether this subject is on the all tier of this category: in the category's group and the
   * Grouper sysadmin flavor it requires.  false for a category with no tiers.  no consent check,
   * so the UI can use this too
   * @param category the category
   * @param subject the caller
   * @return true if on the all tier
   */
  public static boolean isAllTier(GrouperToolCategory category, Subject subject) {

    if (category == null || subject == null) {
      return false;
    }

    switch (category) {
      case sql:
        return isSqlReadonlyAllTier(subject);
      case admin_readonly:
        return isAdminReadonlyAllTier(subject);
      case admin_readwrite:
        return isAdminReadwriteAllTier(subject);
      default:
        return false;
    }
  }

  /**
   * whether this subject is in the limited tier group of this category.  false for a category with
   * no tiers.  no consent check, so the UI can use this too
   * @param category the category
   * @param subject the caller
   * @return true if in the limited group
   */
  public static boolean isLimitedTier(GrouperToolCategory category, Subject subject) {

    if (category == null || subject == null) {
      return false;
    }

    switch (category) {
      case sql:
        return isSqlReadonlyLimitedTier(subject);
      case admin_readonly:
        return isAdminReadonlyLimitedTier(subject);
      case admin_readwrite:
        return isAdminReadwriteLimitedTier(subject);
      default:
        return false;
    }
  }

  /**
   * the folder every limitedAccessGroup has to be in, etc:mcp unless the folder for built in
   * objects is configured differently.  MCP refuses to change anything under that folder, so a
   * group in here cannot be used through MCP to widen who gets what
   * @return the folder name
   */
  public static String limitedAccessGroupFolderName() {
    return GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.rootStemForBuiltinObjects", "etc") + ":mcp";
  }

  /**
   * whether the subject is in the limitedAccessGroup named in this property.  the group has to be
   * in {@link #limitedAccessGroupFolderName()}; one anywhere else is ignored, with an error in the
   * log, so a misconfiguration denies rather than allows
   * @param subject the caller
   * @param propertyName the property naming the group
   * @return true if in the group
   */
  private static boolean isInLimitedAccessGroup(Subject subject, String propertyName) {
    return GrouperMcpGroupMembership.isSubjectInGroup(subject, propertyName,
        limitedAccessGroupFolderName());
  }

  /**
   * whether this caller may run a tool in this category.  for sql and admin readonly this is true
   * for either tier, see {@link #isAllowed(GrouperTool, GrouperToolCategory, GrouperMcpAuthUser)}
   * for the narrowing of the limited tier
   * @param category what the tool needs to be allowed to do
   * @param authUser the caller
   * @return true if allowed
   */
  public static boolean isAllowed(GrouperToolCategory category, GrouperMcpAuthUser authUser) {

    if (category == null || authUser == null || authUser.getSubject() == null) {
      return false;
    }

    switch (category) {

      case readonly:
        // being allowed to write implies being allowed to read
        return isAllowedReadonly(authUser);

      case readwrite:
        return isAllowedReadwrite(authUser);

      case sql:
        return isAllowedSqlReadonly(authUser);

      case admin_readonly:
        // being allowed to run daemon jobs implies being allowed to look at them
        return isAllowedAdminReadonly(authUser);

      case admin_readwrite:
        return isAllowedAdminReadwrite(authUser);

      default:
        // a category nobody has taught this method about is refused rather than allowed, so that
        // adding one without coming here fails closed
        return false;
    }
  }

  /**
   * @param authUser the caller
   * @return true if allowed to read
   */
  public static boolean isAllowedReadonly(GrouperMcpAuthUser authUser) {

    if (!GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_READONLY)
        && !GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_READWRITE)) {
      return false;
    }

    if (authUser.isConsentScopeEnforced()
        && !authUser.isConsentScopeReadonly() && !authUser.isConsentScopeReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to write
   */
  public static boolean isAllowedReadwrite(GrouperMcpAuthUser authUser) {

    if (!GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_READWRITE)) {
      return false;
    }

    if (authUser.isConsentScopeEnforced() && !authUser.isConsentScopeReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to run sql, on either tier
   */
  public static boolean isAllowedSqlReadonly(GrouperMcpAuthUser authUser) {

    Subject subject = authUser.getSubject();
    if (!isSqlReadonlyAllTier(subject) && !isSqlReadonlyLimitedTier(subject)) {
      return false;
    }

    if (authUser.isConsentScopeEnforced() && !authUser.isConsentScopeSqlReadonly()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to read config and daemons, on either tier
   */
  public static boolean isAllowedAdminReadonly(GrouperMcpAuthUser authUser) {

    Subject subject = authUser.getSubject();
    if (!isAdminReadonlyAllTier(subject) && !isAdminReadonlyLimitedTier(subject)) {
      return false;
    }

    if (authUser.isConsentScopeEnforced()
        && !authUser.isConsentScopeAdminReadonly() && !authUser.isConsentScopeAdminReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to run daemon jobs, on either tier
   */
  public static boolean isAllowedAdminReadwrite(GrouperMcpAuthUser authUser) {

    Subject subject = authUser.getSubject();
    if (!isAdminReadwriteAllTier(subject) && !isAdminReadwriteLimitedTier(subject)) {
      return false;
    }

    if (authUser.isConsentScopeEnforced() && !authUser.isConsentScopeAdminReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * the all tier of sql: in the sql group and a sysadmin.  sql reaches whole databases with no row
   * level security, so it takes full wheel, not readonly sysadmin.  no consent check, so the UI
   * can use this too
   * @param subject the caller
   * @return true if on the all tier
   */
  public static boolean isSqlReadonlyAllTier(Subject subject) {
    return subject != null
        && GrouperMcpGroupMembership.isSubjectInGroup(subject, GROUP_SQL_READONLY)
        && PrivilegeHelper.isWheelOrRoot(subject);
  }

  /**
   * the limited tier of sql: in the limited sql group.  no sysadmin requirement, since this tier
   * only reaches the databases opened to the caller.  the group has to be in
   * {@link #limitedAccessGroupFolderName()} like every limitedAccessGroup, so MCP cannot be used to change it
   * @param subject the caller
   * @return true if in the limited group
   */
  public static boolean isSqlReadonlyLimitedTier(Subject subject) {
    return subject != null
        && GrouperMcpGroupMembership.isSubjectInGroup(subject, GROUP_SQL_READONLY_LIMITED,
            limitedAccessGroupFolderName());
  }

  /**
   * the all tier of admin readonly: in the admin readonly group and a sysadmin or readonly
   * sysadmin, or on the all tier of admin readwrite, since being allowed to run daemon jobs
   * implies being allowed to look at them.  no consent check, so the UI can use this too
   * @param subject the caller
   * @return true if on the all tier
   */
  public static boolean isAdminReadonlyAllTier(Subject subject) {
    if (subject == null) {
      return false;
    }
    if (GrouperMcpGroupMembership.isSubjectInGroup(subject, GROUP_ADMIN_READONLY)
        && PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject)) {
      return true;
    }
    return isAdminReadwriteAllTier(subject);
  }

  /**
   * the limited tier of admin readonly: in the limited admin readonly group.  no sysadmin
   * requirement, since this tier only reaches the tools and systems opened to the caller.  the group has to
   * be in {@link #limitedAccessGroupFolderName()}
   * @param subject the caller
   * @return true if in the limited group
   */
  public static boolean isAdminReadonlyLimitedTier(Subject subject) {
    return subject != null
        && GrouperMcpGroupMembership.isSubjectInGroup(subject, GROUP_ADMIN_READONLY_LIMITED,
            limitedAccessGroupFolderName());
  }

  /**
   * the limited tier of admin readwrite: in the limited admin readwrite group.  no sysadmin
   * requirement, since this tier can only run the loader jobs the caller could refresh in the UI.  the
   * group has to be in {@link #limitedAccessGroupFolderName()}
   * @param subject the caller
   * @return true if in the limited group
   */
  public static boolean isAdminReadwriteLimitedTier(Subject subject) {
    return subject != null
        && GrouperMcpGroupMembership.isSubjectInGroup(subject, GROUP_ADMIN_READWRITE_LIMITED,
            limitedAccessGroupFolderName());
  }

  /**
   * the all tier of admin readwrite: in the admin readwrite group and a sysadmin.  no consent
   * check, so the UI can use this too
   * @param subject the caller
   * @return true if allowed
   */
  public static boolean isAdminReadwriteAllTier(Subject subject) {
    return subject != null
        && GrouperMcpGroupMembership.isSubjectInGroup(subject, GROUP_ADMIN_READWRITE)
        && PrivilegeHelper.isWheelOrRoot(subject);
  }

}
