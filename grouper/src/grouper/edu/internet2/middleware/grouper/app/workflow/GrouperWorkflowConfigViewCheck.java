package edu.internet2.middleware.grouper.app.workflow;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Per-workflow view authorization.  A workflow config has a viewers group ("people who can view
 * this workflow and instances of this workflow").  GrouperWorkflowConfigService.canSubjectViewWorkflow
 * answers "can view SOME workflow on this group" (right for menu links), but the UI used it to
 * list every config on the group, and the config details screen checked only VIEW on the group.
 *
 * This answers the question for one specific config: people who can configure workflows on the
 * group (wheel or root, group admins, the workflow editors group), else members of that config's
 * viewers group.  Call it as root (the viewers group lookup needs it, as the UI does today).
 *
 * In its own class so one version ships to every release and can be unit tested.
 * </pre>
 */
public class GrouperWorkflowConfigViewCheck {

  /**
   * no instances
   */
  private GrouperWorkflowConfigViewCheck() {
  }

  /**
   * can the subject view this specific workflow config on this group
   * @param group owner group of the config (the config was loaded from this group)
   * @param workflowConfig
   * @param subject
   * @return true if allowed
   */
  public static boolean canSubjectViewWorkflowConfig(Group group, GrouperWorkflowConfig workflowConfig, Subject subject) {
    if (workflowConfig == null || subject == null) {
      return false;
    }
    if (GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, subject)) {
      return true;
    }
    return canSubjectViewWorkflowConfig(workflowConfig, subject);
  }

  /**
   * can a subject who cannot configure workflows on the group view this config (viewers group)
   * @param workflowConfig
   * @param subject
   * @return true if in the config's own viewers group
   */
  public static boolean canSubjectViewWorkflowConfig(GrouperWorkflowConfig workflowConfig, Subject subject) {
    if (workflowConfig == null || subject == null) {
      return false;
    }
    return workflowConfig.isSubjectInViewersGroup(subject);
  }
}
