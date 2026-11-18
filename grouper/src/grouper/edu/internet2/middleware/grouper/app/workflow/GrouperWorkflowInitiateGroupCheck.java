package edu.internet2.middleware.grouper.app.workflow;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;

/**
 * <pre>
 * Binds a workflow initiate request to the workflow config's own group.  The UI submit takes
 * the config (workflowAttributeAssignId) and the group (groupId) as two separate request
 * parameters and resolves both as root, so without this check a user allowed to initiate one
 * workflow could have a workflow instance written, as root, onto any other group.
 *
 * In its own class (not in the UI handler) so it can be unit tested, and so it ships as one
 * drop-in file for every release.
 * </pre>
 */
public class GrouperWorkflowInitiateGroupCheck {

  /**
   * no instances
   */
  private GrouperWorkflowInitiateGroupCheck() {
  }

  /**
   * is the group of the request the group the workflow config hangs off
   * @param config workflow config from the request
   * @param group group from the request
   * @return true if both are there and the group is the config's owner group
   */
  public static boolean groupMatchesConfig(GrouperWorkflowConfig config, Group group) {
    if (config == null || group == null) {
      return false;
    }
    Group ownerGroup = config.getOwnerGroup();
    if (ownerGroup == null) {
      return false;
    }
    return StringUtils.equals(ownerGroup.getId(), group.getId());
  }

  /**
   * throw if the group of the request is not the group the workflow config hangs off
   * @param config workflow config from the request
   * @param group group from the request
   */
  public static void assertGroupMatchesConfig(GrouperWorkflowConfig config, Group group) {
    if (!groupMatchesConfig(config, group)) {
      // ids only, no names, this can end up in a log
      throw new RuntimeException("Workflow config " + (config == null ? null : config.getAttributeAssignmentMarkerId())
          + " does not belong to group " + (group == null ? null : group.getId())
          + ", owner group is " + (config == null || config.getOwnerGroup() == null ? null : config.getOwnerGroup().getId()));
    }
  }
}
