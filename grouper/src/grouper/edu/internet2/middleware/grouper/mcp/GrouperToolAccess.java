/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;

/**
 * whether a caller is allowed to run a tool in a given category.
 *
 * <p>this is an intersection and never a grant: the caller has to be in the Grouper group for the
 * category, and, if they arrived over OAuth, to have consented to the matching scope.  neither
 * half can stand in for the other.  if it were computed from the consent record alone, the
 * consent screen would become a way to hand yourself access you do not have.</p>
 *
 * <p>group membership is checked against Grouper rather than read from the token, so removing
 * somebody from the group takes effect within a minute even while they hold an unexpired token
 * (the answer is cached for sixty seconds on each node, see {@link GrouperMcpGroupMembership}).
 * membership of the wheel group does not grant any of this.</p>
 *
 * <p>it lives here rather than on the MCP servlet so that the UI is held to the same rule.  a UI
 * caller is not OAuth authenticated, so the consent half does not apply to them; the narrowing a
 * UI user chooses for their session is a separate check on top of this one.</p>
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

  /**
   * whether this caller may run a tool in this category
   * @param category what the tool needs to be allowed to do
   * @param authUser the caller
   * @return true if allowed
   */
  public static boolean isAllowed(GrouperToolCategory category, GrouperMcpAuthUser authUser) {

    if (category == null || authUser == null) {
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

    if (authUser.isOAuthAuthenticated()
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

    if (authUser.isOAuthAuthenticated() && !authUser.isConsentScopeReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to run sql
   */
  public static boolean isAllowedSqlReadonly(GrouperMcpAuthUser authUser) {

    if (!GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_SQL_READONLY)) {
      return false;
    }

    if (authUser.isOAuthAuthenticated() && !authUser.isConsentScopeSqlReadonly()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to read config and daemons
   */
  public static boolean isAllowedAdminReadonly(GrouperMcpAuthUser authUser) {

    if (!GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_ADMIN_READONLY)
        && !GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_ADMIN_READWRITE)) {
      return false;
    }

    if (authUser.isOAuthAuthenticated()
        && !authUser.isConsentScopeAdminReadonly() && !authUser.isConsentScopeAdminReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * @param authUser the caller
   * @return true if allowed to run daemon jobs
   */
  public static boolean isAllowedAdminReadwrite(GrouperMcpAuthUser authUser) {

    if (!GrouperMcpGroupMembership.isSubjectInGroup(authUser, GROUP_ADMIN_READWRITE)) {
      return false;
    }

    if (authUser.isOAuthAuthenticated() && !authUser.isConsentScopeAdminReadwrite()) {
      return false;
    }

    return true;
  }

}
