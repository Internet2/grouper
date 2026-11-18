package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.COMPLETE_STATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Checks that the person saving a workflow config is allowed to use every group the config
 * names.  GrouperWorkflowConfigValidator only checks that those groups exist (as root), and the
 * daemon later acts on them as root, so without this check an admin of one group could write a
 * workflow whose completion adds people to any group, including the wheel group.
 *
 * Rules for a non-wheel author (wheel and root may reference anything):
 * - every group the workflow adds people to needs UPDATE or ADMIN: each assignToGroup (complete
 *   state action arg 0), or, when there is no assignToGroup, the workflow's own (owner) group,
 *   which is where the daemon adds the requester.  So a workflowEditors member who can only VIEW
 *   a group cannot build a workflow that adds people to it.
 * - approverGroupId and approverManagersOfGroupId: READ or ADMIN (they expose membership)
 * - allowedGroupId and workflowConfigViewersGroupId: VIEW (any group privilege)
 * - every reference is checked on every save, including ones unchanged from the stored config
 *   and the owner group.  Whoever controls the approvers of a workflow controls its
 *   membership adds, so keeping a sysadmin-made assignToGroup while changing the approvers is
 *   the same as adding it (2026-11-09).
 *
 * Also a daemon-time guard (isDaemonAssignAllowed) so a config that is already stored cannot
 * add anyone to the wheel, viewonly wheel or readonly wheel groups.
 *
 * Messages are fixed English text plus the reference the author typed, so no new externalized
 * text keys are needed (this class ships as a drop-in patch for older releases too).
 * </pre>
 */
public class GrouperWorkflowConfigAuthorValidator {

  /** reference kind: complete state assignToGroup action */
  private static final String KIND_ASSIGN_TO_GROUP = "assignToGroup";

  /** reference kind: approverGroupId of a state */
  private static final String KIND_APPROVER_GROUP = "approverGroupId";

  /** reference kind: approverManagersOfGroupId of a state */
  private static final String KIND_APPROVER_MANAGERS_OF_GROUP = "approverManagersOfGroupId";

  /** reference kind: allowedGroupId of the initiate state */
  private static final String KIND_ALLOWED_GROUP = "allowedGroupId";

  /** reference kind: workflow config viewers group */
  private static final String KIND_VIEWERS_GROUP = "workflowConfigViewersGroupId";

  /**
   * no instances
   */
  private GrouperWorkflowConfigAuthorValidator() {
  }

  /**
   * Check the author's privileges on every group the config references.  Runs its lookups in a
   * root session, and checks the privileges of the author explicitly, so it does not matter
   * which session the caller is in.
   * @param config the submitted config (approvals already parsed or as a json string)
   * @param ownerGroup group the workflow config hangs off
   * @param author logged in subject saving the config
   * @param previousConfig stored config on edit, or null on add; not used any more (every
   *   reference is checked on every save), kept so callers do not change
   * @return error messages, empty if ok
   */
  public static List<String> validateAuthor(final GrouperWorkflowConfig config, final Group ownerGroup,
      final Subject author, final GrouperWorkflowConfig previousConfig) {

    final List<String> errors = new ArrayList<String>();

    if (config == null) {
      return errors;
    }

    if (author == null) {
      errors.add("Workflow config cannot be saved without a logged in subject");
      return errors;
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        // wheel and root can reference anything, as before
        if (PrivilegeHelper.isWheelOrRoot(author)) {
          return null;
        }

        List<String[]> currentRefs = references(config);

        // with no assignToGroup the daemon adds the requester to the owner group (see
        // GrouperWorkflowDaemonJob.addSubjectToGroup), so that is a membership add too
        boolean hasAssignToGroup = false;
        for (String[] ref : currentRefs) {
          if (KIND_ASSIGN_TO_GROUP.equals(ref[0])) {
            hasAssignToGroup = true;
          }
        }
        if (!hasAssignToGroup && ownerGroup != null
            && !PrivilegeHelper.hasPrivilege(rootSession, ownerGroup, author, AccessPrivilege.UPDATE_PRIVILEGES)) {
          errors.add("You need UPDATE or ADMIN on this group to save a workflow that adds people to it: "
              + ownerGroup.getName());
        }

        // every reference, on every save: no exception for references unchanged from the stored
        // config, and none for the owner group (an owner group ADMIN passes every check anyway)
        for (String[] ref : currentRefs) {
          String kind = ref[0];
          String groupRef = ref[1];

          Group group = findGroup(rootSession, groupRef);

          // not found is reported by GrouperWorkflowConfigValidator
          if (group == null) {
            continue;
          }

          Set<Privilege> neededPrivileges = null;
          String neededLabel = null;
          if (KIND_ASSIGN_TO_GROUP.equals(kind)) {
            neededPrivileges = AccessPrivilege.UPDATE_PRIVILEGES;
            neededLabel = "UPDATE or ADMIN";
          } else if (KIND_APPROVER_GROUP.equals(kind) || KIND_APPROVER_MANAGERS_OF_GROUP.equals(kind)) {
            neededPrivileges = AccessPrivilege.READ_PRIVILEGES;
            neededLabel = "READ or ADMIN";
          } else {
            neededPrivileges = AccessPrivilege.VIEW_PRIVILEGES;
            neededLabel = "VIEW";
          }

          if (!PrivilegeHelper.hasPrivilege(rootSession, group, author, neededPrivileges)) {
            errors.add("You need " + neededLabel + " on the group in " + kind + " to use it in a workflow: " + groupRef);
          }
        }
        return null;
      }
    });

    return errors;
  }

  /**
   * Daemon-time guard: may the daemon add the requester to this group for this config?  There is
   * no stored author on a config, so this cannot re-check the author.  It refuses the wheel,
   * viewonly wheel and readonly wheel groups unless that group is the workflow's own group.
   * @param config workflow config of the instance
   * @param target assignToGroup group resolved by the daemon
   * @return true if allowed
   */
  public static boolean isDaemonAssignAllowed(GrouperWorkflowConfig config, Group target) {

    if (target == null) {
      return false;
    }

    Group ownerGroup = config == null ? null : config.getOwnerGroup();
    if (ownerGroup != null && StringUtils.equals(ownerGroup.getId(), target.getId())) {
      return true;
    }

    for (String wheelProperty : new String[] {"groups.wheel.group", "groups.wheel.viewonly.group", "groups.wheel.readonly.group"}) {
      String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString(wheelProperty);
      if (StringUtils.isNotBlank(wheelGroupName) && StringUtils.equals(StringUtils.trim(wheelGroupName), target.getName())) {
        return false;
      }
    }
    return true;
  }

  /**
   * find a group by uuid, then by name, the same way GrouperWorkflowConfigValidator does
   * @param grouperSession
   * @param groupRef
   * @return the group or null
   */
  private static Group findGroup(GrouperSession grouperSession, String groupRef) {
    Group group = GroupFinder.findByUuid(grouperSession, groupRef, false);
    if (group == null) {
      group = GroupFinder.findByName(grouperSession, groupRef, false);
    }
    return group;
  }

  /**
   * list the group references in a config as {kind, reference} pairs
   * @param config
   * @return the references, never null
   */
  private static List<String[]> references(GrouperWorkflowConfig config) {

    List<String[]> result = new ArrayList<String[]>();
    if (config == null) {
      return result;
    }

    addReference(result, KIND_VIEWERS_GROUP, config.getWorkflowConfigViewersGroupId());

    GrouperWorkflowApprovalStates approvalStates = config.getWorkflowApprovalStates();
    if (approvalStates == null && StringUtils.isNotBlank(config.getWorkflowConfigApprovalsString())) {
      try {
        approvalStates = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(config.getWorkflowConfigApprovalsString());
      } catch (RuntimeException re) {
        // unparseable approvals are reported by GrouperWorkflowConfigValidator
        approvalStates = null;
      }
    }
    if (approvalStates == null || approvalStates.getStates() == null) {
      return result;
    }

    for (GrouperWorkflowApprovalState state : approvalStates.getStates()) {
      if (state == null) {
        continue;
      }
      addReference(result, KIND_ALLOWED_GROUP, state.getAllowedGroupId());
      addReference(result, KIND_APPROVER_GROUP, state.getApproverGroupId());
      addReference(result, KIND_APPROVER_MANAGERS_OF_GROUP, state.getApproverManagersOfGroupId());

      // the daemon only acts on assignToGroup in the complete state
      if (COMPLETE_STATE.equals(state.getStateName()) && state.getActions() != null) {
        for (GrouperWorkflowApprovalAction action : state.getActions()) {
          if (action != null && KIND_ASSIGN_TO_GROUP.equals(action.getActionName())) {
            addReference(result, KIND_ASSIGN_TO_GROUP, action.getActionArg0());
          }
        }
      }
    }
    return result;
  }

  /**
   * add a reference if not blank
   * @param result
   * @param kind
   * @param groupRef
   */
  private static void addReference(List<String[]> result, String kind, String groupRef) {
    if (StringUtils.isNotBlank(groupRef)) {
      result.add(new String[] {kind, groupRef});
    }
  }
}
