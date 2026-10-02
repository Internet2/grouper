/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

/**
 * is this caller in the group an institution configured for a kind of access.
 *
 * <p>membership is checked against Grouper rather than read from a token, so that taking somebody
 * out of the group stops them immediately rather than when their token expires.  the answer is
 * cached for sixty seconds, which keeps that close to immediate without asking the database on
 * every call.</p>
 */
public class GrouperMcpGroupMembership {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperMcpGroupMembership.class);

  /**
   * key is MultiKey(subjectId, subjectSourceId, groupPropertyName, requiredFolderName)
   */
  private static GrouperCache<MultiKey, Boolean> subjectInGroupCache =
      new GrouperCache<MultiKey, Boolean>(
          GrouperMcpGroupMembership.class.getName() + ".subjectInGroupCache",
          2000, false, 60, 60, false);

  /**
   * clear the membership cache on this node, e.g. in tests after changing who is in a group
   */
  public static void clearCache() {
    subjectInGroupCache.clear();
  }

  /**
   * @param authUser the caller
   * @param groupPropertyName the grouper.properties key holding the group name
   * @return true if the caller is in that group.  false if the property is not set, so an
   * institution which has not configured a group has not thereby allowed everybody
   */
  public static boolean isSubjectInGroup(GrouperMcpAuthUser authUser, String groupPropertyName) {
    return isSubjectInGroup(authUser == null ? null : authUser.getSubject(), groupPropertyName);
  }

  /**
   * @param subject the caller.  the UI has a subject rather than an MCP caller
   * @param groupPropertyName the grouper.properties key holding the group name
   * @return true if the subject is in that group.  false if the property is not set, so an
   * institution which has not configured a group has not thereby allowed everybody
   */
  public static boolean isSubjectInGroup(Subject subject, String groupPropertyName) {
    return isSubjectInGroup(subject, groupPropertyName, null);
  }

  /**
   * @param subject the caller.  the UI has a subject rather than an MCP caller
   * @param groupPropertyName the grouper.properties key holding the group name
   * @param requiredFolderName if not null, the group has to be in this folder or below it.  this is
   * checked on the group's current name, so a group moved out of the folder does not still count
   * because it can be found by its old name.  a group anywhere else is ignored, with an error in
   * the log, so a misconfiguration denies rather than allows
   * @return true if the subject is in that group.  false if the property is not set, so an
   * institution which has not configured a group has not thereby allowed everybody
   */
  public static boolean isSubjectInGroup(Subject subject, String groupPropertyName,
      String requiredFolderName) {

    if (subject == null) {
      return false;
    }

    String groupName = GrouperConfig.retrieveConfig().propertyValueString(groupPropertyName);
    if (StringUtils.isBlank(groupName)) {
      return false;
    }

    MultiKey cacheKey = new MultiKey(subject.getId(),
        StringUtils.defaultString(subject.getSourceId()), groupPropertyName,
        StringUtils.defaultString(requiredFolderName));
    Boolean cachedResult = subjectInGroupCache.get(cacheKey);
    if (cachedResult != null) {
      return cachedResult;
    }

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group == null) {
        subjectInGroupCache.put(cacheKey, false);
        return false;
      }
      if (requiredFolderName != null && !group.getName().startsWith(requiredFolderName + ":")) {
        LOG.error("MCP access group in " + groupPropertyName + " is '" + group.getName()
            + "', which is not in folder " + requiredFolderName + ", so it is ignored and grants "
            + "nothing.  Move the group into " + requiredFolderName + ", where MCP cannot change it.");
        subjectInGroupCache.put(cacheKey, false);
        return false;
      }
      boolean isMember = group.hasMember(subject);
      subjectInGroupCache.put(cacheKey, isMember);
      return isMember;
    } catch (Exception e) {
      // if we cannot tell, do not grant
      LOG.error("Error checking group membership for MCP access: " + groupPropertyName, e);
      return false;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
