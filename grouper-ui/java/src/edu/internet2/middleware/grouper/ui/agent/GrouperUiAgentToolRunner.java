/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.agent;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.mcp.GrouperToolEntryPath;
import edu.internet2.middleware.grouper.mcp.GrouperToolException;
import edu.internet2.middleware.grouper.mcp.GrouperToolExecutor;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ws.GrouperWsRequestContext;
import edu.internet2.middleware.grouper.ws.GrouperWsSubjectResolver;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.subject.Subject;

/**
 * the UI's front door to the tool layer: runs the same tools MCP clients use, as the user logged
 * in to the UI, limited by the scope they chose for the session.
 *
 * <p>no OAuth, no token, no consent screen.  the user is already authenticated to the UI session,
 * and the client is Grouper itself, so there is no third party to consent to.  what the tool layer
 * needs is who is calling and what they allowed for this session, and this class fills in both from
 * the {@link GrouperUiAgentCaller}.  everything else -- the allow and deny config, the group
 * membership check, the scope check, running the tool as the user, and the audit row -- is the same
 * code an MCP call goes through, in {@link GrouperToolExecutor}.</p>
 *
 * <p>the scope is an intersection, never a grant: the user needs the MCP group membership for a
 * category and has to have allowed that category for the session.  it is read from the session on
 * every call, so a change on the screen takes effect on the next call.</p>
 *
 * <p>works from the caller rather than from the HTTP request, so it can be used from the
 * background thread the agent runs in.</p>
 */
public class GrouperUiAgentToolRunner {

  /**
   * run a tool as the caller.  a tool which ran and could not do what was asked, including being
   * refused by the scope or group checks, comes back as an ordinary result with isError set, so the
   * model can explain it
   *
   * @param caller who the agent is working for
   * @param toolName which tool
   * @param arguments as the model supplied them, may be null
   * @return the tool result
   * @throws GrouperToolException if there is no such tool, or the server broke
   */
  public static ObjectNode executeTool(GrouperUiAgentCaller caller, String toolName, JsonNode arguments) {

    GrouperMcpAuthUser authUser = retrieveAuthUser(caller);

    assignRequestContext(caller);
    try {
      return GrouperToolExecutor.executeTool(toolName, arguments, authUser);
    } finally {
      GrouperWsRequestContext.clearThreadLocals();
    }
  }

  /**
   * the definitions of every tool the caller's MCP group memberships allow, whatever scope they
   * chose for the session.  the agent offers this list so it stays the same when the user changes
   * the scope mid conversation: a changing tool list invalidates the provider's cache and the
   * model's earlier reasoning.  the session scope is still enforced on every call, by
   * {@link #executeTool(GrouperUiAgentCaller, String, JsonNode)}
   * @param caller who the agent is working for
   * @return the tool definitions, never null
   */
  public static ArrayNode retrieveToolDefinitionsForAllScopes(GrouperUiAgentCaller caller) {

    GrouperMcpAuthUser authUser = buildAuthUser(caller, allCategories());

    assignRequestContext(caller);
    try {
      return GrouperToolExecutor.retrieveToolDefinitions(authUser);
    } finally {
      GrouperWsRequestContext.clearThreadLocals();
    }
  }

  /**
   * the categories the caller's MCP group memberships allow, which are the only ones worth offering
   * on the scope screen.  choosing any other would do nothing
   * @param caller who the agent is working for
   * @return the categories, in the order they are declared
   */
  public static Set<GrouperToolCategory> retrieveCategoriesAllowedByMembership(GrouperUiAgentCaller caller) {

    GrouperMcpAuthUser authUser = membershipOnlyAuthUser(caller);

    Set<GrouperToolCategory> result = new LinkedHashSet<GrouperToolCategory>();
    for (GrouperToolCategory category : GrouperToolCategory.values()) {
      if (GrouperToolAccess.isAllowed(category, authUser)) {
        result.add(category);
      }
    }
    return result;
  }

  /**
   * @param caller who the agent is working for
   * @param category a tool category
   * @return true if the caller's MCP group memberships allow the category, whatever the session
   * scope says
   */
  public static boolean isAllowedByMembership(GrouperUiAgentCaller caller, GrouperToolCategory category) {
    return GrouperToolAccess.isAllowed(category, membershipOnlyAuthUser(caller));
  }

  /**
   * a caller the access checks judge on group membership alone: not OAuth and not marked as the UI,
   * so no scope applies.  for asking what the groups allow, never for running a tool.  it has no
   * member internal id, so building it looks nothing up and creates nothing, which matters since
   * the screen asks this just to draw itself
   * @param caller who the agent is working for
   * @return the caller
   */
  private static GrouperMcpAuthUser membershipOnlyAuthUser(GrouperUiAgentCaller caller) {
    return new GrouperMcpAuthUser(caller.getSubject());
  }

  /**
   * the caller the tool layer sees: the user logged in to the UI, marked as coming through the UI,
   * with the scope they chose for the session, read now
   * @param caller who the agent is working for
   * @return the caller
   */
  public static GrouperMcpAuthUser retrieveAuthUser(GrouperUiAgentCaller caller) {
    return buildAuthUser(caller, caller.getSessionContainer().getAiAgentToolScope());
  }

  /**
   * @return every category
   */
  private static Set<GrouperToolCategory> allCategories() {
    Set<GrouperToolCategory> allCategories = new HashSet<GrouperToolCategory>();
    for (GrouperToolCategory category : GrouperToolCategory.values()) {
      allCategories.add(category);
    }
    return allCategories;
  }

  /**
   * the member internal id the tool log and the usage table record the user by, creating the member
   * if needed.  for the turn, which is about to log tool calls and record usage.  looked up as root
   * because this is bookkeeping about the caller, not something the caller is asking to see
   * @param subject the user
   * @return the member internal id
   */
  public static Long retrieveMemberInternalId(final Subject subject) {
    return retrieveMemberInternalId(subject, true);
  }

  /**
   * the member internal id the tool log and the usage table record the user by
   * @param subject the user
   * @param createIfMissing true only on a path which is about to write for the user, i.e. a turn.
   * the screen and its checks pass false, so drawing the screen never creates anything
   * @return the member internal id, or null if there is no member and createIfMissing is false
   */
  public static Long retrieveMemberInternalId(final Subject subject, final boolean createIfMissing) {
    Member member = (Member)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
        return MemberFinder.findBySubject(rootGrouperSession, subject, createIfMissing);
      }
    });
    return member == null ? null : member.getInternalId();
  }

  /**
   * build the caller the tool layer sees from the UI caller and a session scope
   * @param caller who the agent is working for
   * @param scope the tool categories the user allowed for the session
   * @return the caller
   */
  static GrouperMcpAuthUser buildAuthUser(GrouperUiAgentCaller caller, Set<GrouperToolCategory> scope) {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(caller.getSubject());

    // so the tool log can tell this apart from an MCP client acting for the same user, and so the
    // session scope below is enforced (see GrouperMcpAuthUser.isConsentScopeEnforced)
    authUser.setEntryPath(GrouperToolEntryPath.ui);

    // looked up once per caller, not on every build: each tool call builds this more than once
    authUser.setMemberInternalId(caller.getMemberInternalId());

    // the session scope is carried in the same flags as an OAuth consent.  choosing readwrite also
    // allows reading, and admin readwrite also allows admin reading, the same as for OAuth
    authUser.setConsentScopeReadonly(scope.contains(GrouperToolCategory.readonly));
    authUser.setConsentScopeReadwrite(scope.contains(GrouperToolCategory.readwrite));
    authUser.setConsentScopeSqlReadonly(scope.contains(GrouperToolCategory.sql));
    authUser.setConsentScopeAdminReadonly(scope.contains(GrouperToolCategory.admin_readonly));
    authUser.setConsentScopeAdminReadwrite(scope.contains(GrouperToolCategory.admin_readwrite));

    // the session scope is by category only.  nothing narrows readwrite to particular groups,
    // folders or subjects, so consentReadwriteScopeRestricted stays false

    return authUser;
  }

  /**
   * fill in the request context the web service logic behind the tools reads.  who is logged in
   * is answered from the caller rather than from a web service login, and the address is the
   * socket address the UI saw, the same thing the UI hands to GSH templates.  the caller must
   * clear it in a finally
   * @param caller who the agent is working for
   */
  private static void assignRequestContext(GrouperUiAgentCaller caller) {

    final Subject subject = caller.getSubject();

    GrouperWsRequestContext.assignSubjectResolverForRequest(new GrouperWsSubjectResolver() {

      public Subject retrieveSubjectLoggedIn() {
        return subject;
      }
    });

    if (caller.getRemoteAddr() != null) {
      GrouperWsRequestContext.assignRemoteAddr(caller.getRemoteAddr());
    }
  }

}
