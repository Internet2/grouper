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
   * key is MultiKey(subjectId, subjectSourceId, groupPropertyName)
   */
  private static GrouperCache<MultiKey, Boolean> subjectInGroupCache =
      new GrouperCache<MultiKey, Boolean>(
          GrouperMcpGroupMembership.class.getName() + ".subjectInGroupCache",
          2000, false, 60, 60, false);

  /**
   * @param authUser the caller
   * @param groupPropertyName the grouper.properties key holding the group name
   * @return true if the caller is in that group.  false if the property is not set, so an
   * institution which has not configured a group has not thereby allowed everybody
   */
  public static boolean isSubjectInGroup(GrouperMcpAuthUser authUser, String groupPropertyName) {

    String groupName = GrouperConfig.retrieveConfig().propertyValueString(groupPropertyName);
    if (StringUtils.isBlank(groupName)) {
      return false;
    }

    Subject subject = authUser.getSubject();

    MultiKey cacheKey = new MultiKey(subject.getId(),
        StringUtils.defaultString(subject.getSourceId()), groupPropertyName);
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
