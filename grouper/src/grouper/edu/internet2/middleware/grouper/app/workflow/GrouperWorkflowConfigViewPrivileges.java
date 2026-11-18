package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * VIEW privileges that saving a workflow config changes on the workflow's own group.
 *
 * So that people who may initiate a workflow can find its group, saving a workflow config grants
 * VIEW on the group to the initiate state's allowedGroupId, or to EveryEntity when that is blank.
 * The UI saves the config in a root session, so this class:
 *
 * 1. computes the VIEW changes a save would make (computeChanges), and refuses them at save time
 *    unless the author is wheel/root or an ADMIN of the group (validateAuthor).  A workflow
 *    editor (workflow.editorsGroup) who is not a group admin can still save a workflow, as long
 *    as the save does not change who can VIEW the group.
 * 2. applies them (assignViewPrivileges, called from
 *    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig):
 *    - grants VIEW to the allowed group (or EveryEntity) only when it does not already have it
 *    - on edit, revokes VIEW from the previous allowed group only when allowedGroupId changed to
 *      a different group and no other workflow config on the group still uses it
 *    - never revokes EveryEntity VIEW: there is no record of whether a workflow granted it or the
 *      group had it before, so it is left for a group admin to change
 *    - resolves allowedGroupId as a group (uuid, then name), the same way
 *      GrouperWorkflowConfigValidator does, instead of SubjectFinder.findById, which failed for a
 *      group name
 *
 * Only uses APIs that exist from Grouper 2.6 on, so it is the same file in every release.
 * </pre>
 */
public class GrouperWorkflowConfigViewPrivileges {

  /**
   * VIEW privilege changes that one workflow config save makes on its owner group
   */
  public static class ViewPrivilegeChanges {

    /** grant VIEW to this subject, or null */
    private Subject grantTo;

    /** what the config calls the grantee (allowedGroupId as entered, or everyone) */
    private String grantToLabel;

    /** revoke VIEW from this subject, or null */
    private Subject revokeFrom;

    /** what the previous config called it (allowedGroupId as entered) */
    private String revokeFromLabel;

    /** allowedGroupId is set but no such group */
    private boolean allowedGroupNotFound;

    /**
     * grant VIEW to this subject, or null
     * @return the subject
     */
    public Subject getGrantTo() {
      return this.grantTo;
    }

    /**
     * label of the grantee
     * @return the label
     */
    public String getGrantToLabel() {
      return this.grantToLabel;
    }

    /**
     * revoke VIEW from this subject, or null
     * @return the subject
     */
    public Subject getRevokeFrom() {
      return this.revokeFrom;
    }

    /**
     * label of the subject VIEW is revoked from
     * @return the label
     */
    public String getRevokeFromLabel() {
      return this.revokeFromLabel;
    }

    /**
     * allowedGroupId is set but no such group
     * @return true if not found
     */
    public boolean isAllowedGroupNotFound() {
      return this.allowedGroupNotFound;
    }

    /**
     * @return true if the save does not change VIEW on the group
     */
    public boolean isEmpty() {
      return this.grantTo == null && this.revokeFrom == null;
    }
  }

  /**
   * label used for EveryEntity in messages
   */
  private static final String EVERY_ENTITY_LABEL = "everyone (EveryEntity)";

  /**
   * find the group an allowedGroupId refers to, by uuid then by name, as root (the same
   * resolution GrouperWorkflowConfigValidator uses)
   * @param allowedGroupId uuid or name of a group
   * @return the group or null if blank or not found
   */
  public static Group findAllowedGroup(final String allowedGroupId) {
    if (GrouperUtil.isBlank(allowedGroupId)) {
      return null;
    }
    return (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        Group group = GroupFinder.findByUuid(rootSession, allowedGroupId, false);
        if (group == null) {
          group = GroupFinder.findByName(rootSession, allowedGroupId, false);
        }
        return group;
      }
    });
  }

  /**
   * allowedGroupId of the initiate state
   * @param states
   * @return the value or null
   */
  private static String initiateAllowedGroupId(GrouperWorkflowApprovalStates states) {
    if (states == null) {
      return null;
    }
    GrouperWorkflowApprovalState initiateState = states.getStateByName(INITIATE_STATE);
    return initiateState == null ? null : initiateState.getAllowedGroupId();
  }

  /**
   * approval states of a config: the parsed states, else the json string
   * @param config
   * @return the states or null
   */
  private static GrouperWorkflowApprovalStates approvalStates(GrouperWorkflowConfig config) {
    if (config == null) {
      return null;
    }
    if (config.getWorkflowApprovalStates() != null) {
      return config.getWorkflowApprovalStates();
    }
    if (!GrouperUtil.isBlank(config.getWorkflowConfigApprovalsString())) {
      return GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(config.getWorkflowConfigApprovalsString());
    }
    return null;
  }

  /**
   * does the subject have VIEW on the group as an immediate privilege (call as root)
   * @param rootSession
   * @param group
   * @param subject
   * @return true if so
   */
  private static boolean hasImmediateView(GrouperSession rootSession, Group group, Subject subject) {
    Membership membership = MembershipFinder.findImmediateMembership(rootSession, group, subject,
        AccessPrivilege.VIEW.getField(), false);
    return membership != null;
  }

  /**
   * is the group the allowed group of another workflow config on the owner group
   * @param ownerGroup
   * @param workflowConfigId the config being saved, skipped
   * @param allowedGroup
   * @return true if another config uses it
   */
  private static boolean usedByOtherWorkflowConfig(Group ownerGroup, String workflowConfigId, Group allowedGroup) {
    List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(ownerGroup);
    for (GrouperWorkflowConfig otherConfig : GrouperUtil.nonNull(configs)) {
      if (GrouperUtil.equals(workflowConfigId, otherConfig.getWorkflowConfigId())) {
        continue;
      }
      Group otherAllowedGroup = findAllowedGroup(initiateAllowedGroupId(approvalStates(otherConfig)));
      if (otherAllowedGroup != null && GrouperUtil.equals(otherAllowedGroup.getId(), allowedGroup.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * compute the VIEW changes saving this config would make on its owner group.  Runs as root.
   * @param config the config being saved
   * @param previousStates approval states stored before this save, or null for a new config
   * @param ownerGroup the workflow's group
   * @return the changes, never null
   */
  public static ViewPrivilegeChanges computeChanges(final GrouperWorkflowConfig config,
      final GrouperWorkflowApprovalStates previousStates, final Group ownerGroup) {

    return (ViewPrivilegeChanges)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        ViewPrivilegeChanges changes = new ViewPrivilegeChanges();

        // who should be able to VIEW the group: the allowed group, or everyone when blank
        String allowedGroupId = initiateAllowedGroupId(approvalStates(config));
        Group allowedGroup = null;
        Subject grantee = null;
        if (GrouperUtil.isBlank(allowedGroupId)) {
          grantee = SubjectFinder.findAllSubject();
          changes.grantToLabel = EVERY_ENTITY_LABEL;
        } else {
          allowedGroup = findAllowedGroup(allowedGroupId);
          if (allowedGroup == null) {
            // the validator reports this, nothing to grant
            changes.allowedGroupNotFound = true;
          } else {
            grantee = allowedGroup.toSubject();
            changes.grantToLabel = allowedGroupId;
          }
        }
        if (grantee != null && !hasImmediateView(rootSession, ownerGroup, grantee)) {
          changes.grantTo = grantee;
        }

        // on edit, only revoke from a previous allowed group that is replaced by a different one
        // and not used by another workflow on this group.  EveryEntity VIEW is never revoked:
        // the group may have had it before any workflow, and there is no record of who granted it
        String previousAllowedGroupId = initiateAllowedGroupId(previousStates);
        if (!GrouperUtil.isBlank(previousAllowedGroupId)) {
          Group previousAllowedGroup = findAllowedGroup(previousAllowedGroupId);
          if (previousAllowedGroup != null
              && (allowedGroup == null || !GrouperUtil.equals(allowedGroup.getId(), previousAllowedGroup.getId()))
              && !usedByOtherWorkflowConfig(ownerGroup, config.getWorkflowConfigId(), previousAllowedGroup)) {
            Subject previousSubject = previousAllowedGroup.toSubject();
            if (hasImmediateView(rootSession, ownerGroup, previousSubject)) {
              changes.revokeFrom = previousSubject;
              changes.revokeFromLabel = previousAllowedGroupId;
            }
          }
        }
        return changes;
      }
    });
  }

  /**
   * save time check: a save that changes who can VIEW the group needs ADMIN on the group (or
   * wheel/root).  Messages are fixed English text, so no externalized text keys are needed.
   * @param config the config being saved (add or edit)
   * @param ownerGroup the workflow's group
   * @param author the logged in subject
   * @return the errors, empty if allowed
   */
  public static List<String> validateAuthor(final GrouperWorkflowConfig config, final Group ownerGroup, final Subject author) {

    final List<String> errors = new ArrayList<String>();

    if (author == null) {
      errors.add("A logged in user is required to save a workflow.");
      return errors;
    }

    if (PrivilegeHelper.isWheelOrRoot(author)) {
      return errors;
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        // group admins may change VIEW on their group anyway
        if (ownerGroup.hasAdmin(author)) {
          return null;
        }

        // what is stored now (null for a new config)
        GrouperWorkflowConfig previousConfig = GrouperUtil.isBlank(config.getWorkflowConfigId()) ? null
            : GrouperWorkflowConfigService.getWorkflowConfig(ownerGroup, config.getWorkflowConfigId());

        ViewPrivilegeChanges changes = computeChanges(config, approvalStates(previousConfig), ownerGroup);

        if (!changes.isEmpty()) {
          StringBuilder what = new StringBuilder();
          if (changes.getGrantTo() != null) {
            what.append("grant VIEW to ").append(changes.getGrantToLabel());
          }
          if (changes.getRevokeFrom() != null) {
            if (what.length() > 0) {
              what.append(" and ");
            }
            what.append("revoke VIEW from ").append(changes.getRevokeFromLabel());
          }
          errors.add("Saving this workflow would " + what + " on group " + ownerGroup.getName()
              + ", which needs ADMIN on the group.  Ask an admin of the group to save it, or to make that"
              + " privilege change first.");
        }
        return null;
      }
    });

    return errors;
  }

  /**
   * apply the VIEW changes of a save.  Called from saveOrUpdateGrouperWorkflowConfig in the
   * caller's session (the UI uses a root session after validateAuthor passed).
   * @param config the config being saved
   * @param previousStates approval states stored before this save, or null for a new config
   * @param ownerGroup the workflow's group
   */
  public static void assignViewPrivileges(GrouperWorkflowConfig config,
      GrouperWorkflowApprovalStates previousStates, Group ownerGroup) {

    ViewPrivilegeChanges changes = computeChanges(config, previousStates, ownerGroup);

    // the old code threw here too (SubjectFinder.findById(id, true)); the validator reports it first
    if (changes.isAllowedGroupNotFound()) {
      throw new RuntimeException("Workflow allowed group not found: " + initiateAllowedGroupId(approvalStates(config)));
    }

    if (changes.getRevokeFrom() != null) {
      ownerGroup.revokePriv(changes.getRevokeFrom(), AccessPrivilege.VIEW, false);
    }
    if (changes.getGrantTo() != null) {
      ownerGroup.grantPriv(changes.getGrantTo(), AccessPrivilege.VIEW, false);
    }
  }
}
