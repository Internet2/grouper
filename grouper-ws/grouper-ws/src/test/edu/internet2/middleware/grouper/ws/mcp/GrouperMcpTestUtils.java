/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.mcp;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.mcp.GrouperMcpGroupMembership;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * test helpers for the MCP access tiers (GRP-7415).
 *
 * <p>the sql and admin tools only let a caller who is positively on a tier use them, so a test
 * which calls a tool directly has to put its subject on one first.</p>
 */
public class GrouperMcpTestUtils {

  /** folder the test tier groups go in */
  private static final String TEST_TIER_STEM = "test:mcpTier";

  /**
   * put a subject on the all tier of these categories, the way a sysadmin in the MCP group for
   * the category is.  the wheel group and the category groups are pointed at test groups rather
   * than the real ones, so the subject does not also become a member of etc:sysadmingroup, which
   * would change what else they can see (e.g. stack traces in errors).  config overrides are
   * cleared by GrouperTest.setUp, so call this from setUp after super.setUp()
   * @param subject who to put on the all tier
   * @param categories sql, admin_readonly and/or admin_readwrite
   */
  public static void assignAllTier(final Subject subject,
      final GrouperToolCategory... categories) {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group",
        TEST_TIER_STEM + ":wheel");

    // the groups are created and filled as root, without disturbing the caller's session
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        addMember(rootSession, TEST_TIER_STEM + ":wheel", subject);

        for (GrouperToolCategory category : categories) {
          String groupName = TEST_TIER_STEM + ":" + category.name() + "All";
          GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
              allTierGroupConfigKey(category), groupName);
          addMember(rootSession, groupName, subject);
        }
        return null;
      }
    });

    clearCaches();
  }

  /**
   * clear the MCP membership cache and the wheel cache, so the next access check sees changes
   */
  public static void clearCaches() {
    GrouperMcpGroupMembership.clearCache();
    PrivilegeHelper.wheelMemberCacheClear();
  }

  /**
   * @param category a category with tiers
   * @return the config key of the group for its all tier
   */
  private static String allTierGroupConfigKey(GrouperToolCategory category) {
    switch (category) {
      case sql:
        return GrouperToolAccess.GROUP_SQL_READONLY;
      case admin_readonly:
        return GrouperToolAccess.GROUP_ADMIN_READONLY;
      case admin_readwrite:
        return GrouperToolAccess.GROUP_ADMIN_READWRITE;
      default:
        throw new IllegalArgumentException("Category has no tiers: " + category);
    }
  }

  /**
   * create the group if needed and add the subject
   * @param rootSession root session
   * @param groupName the group
   * @param subject who to add
   */
  private static void addMember(GrouperSession rootSession, String groupName, Subject subject) {
    Group group = new GroupSave(rootSession).assignName(groupName)
        .assignCreateParentStemsIfNotExist(true).save();
    group.addMember(subject, false);
  }

}
