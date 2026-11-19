package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleService;
import edu.internet2.middleware.subject.Subject;

/**
 * User lifecycle policy part of the group edit screen.
 * <p>
 * As a page bean (groupEdit.jsp, jsp:useBean): when the group has a policy that the logged in
 * user cannot use, that policy is not in the list of choices, so without this nothing would be
 * selected and any save would submit no value. This bean lets the JSP show the assigned policy as
 * a read-only, checked choice, so saving keeps it and removing it (choosing None) is deliberate.
 * </p>
 * <p>
 * As a helper for UiV2Group.groupEditSubmit: validate the submitted policy before anything on the
 * group is saved, so a denied change shows a validation message and changes nothing.
 * </p>
 * Kept in its own class so the patch only adds a few lines to UiV2Group and groupEdit.jsp, which
 * differ in almost every release.
 */
public class GuiGroupUserLifecyclePolicyEdit {

  /** text key for the validation error (fallback text below if the key is not configured) */
  public static final String TEXT_KEY_NOT_ALLOWED = "userLifecyclePolicyNotAllowedError";

  /** text key for the help text under a read-only assigned policy */
  public static final String TEXT_KEY_READ_ONLY = "userLifecyclePolicyAssignedReadOnlyHelp";

  /** if the assigned policy cannot be used by the logged in user */
  private boolean assignedPolicyReadOnly = false;

  /** config id of the assigned policy, blank if none */
  private String assignedPolicyConfigId = "";

  /** name of the assigned policy (config id if the policy is not configured any more) */
  private String assignedPolicyName = "";

  /**
   * page bean: look at the group being edited and the logged in user
   */
  public GuiGroupUserLifecyclePolicyEdit() {

    GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
    GuiGroup guiGroup = groupContainer == null ? null : groupContainer.getGuiGroup();
    final Group group = guiGroup == null ? null : guiGroup.getGroup();
    if (group == null) {
      return;
    }

    this.assignedPolicyConfigId = StringUtils.defaultString(UserLifecycleService.retrieveExistingPolicyConfigId(group));
    if (StringUtils.isBlank(this.assignedPolicyConfigId)) {
      return;
    }

    // if the user can use the assigned policy, the normal list shows it checked
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    for (UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration : UserLifecycleService.retrieveUserLifecyclePolicies(loggedInSubject)) {
      if (StringUtils.equals(userLifecyclePolicyConfiguration.getConfigId(), this.assignedPolicyConfigId)) {
        return;
      }
    }

    this.assignedPolicyReadOnly = true;

    // name of the policy (the decision is to show it), read as root like the list of policies
    final String policyConfigId = this.assignedPolicyConfigId;
    String name = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        List<UserLifecyclePolicyConfiguration> all = UserLifecyclePolicyConfiguration.retrieveAllUserLifecyclePolicyConfigurations();
        for (UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration : all) {
          if (StringUtils.equals(userLifecyclePolicyConfiguration.getConfigId(), policyConfigId)) {
            return userLifecyclePolicyConfiguration.retrieveAttributeValueFromConfig("name", false);
          }
        }
        return null;
      }
    });
    this.assignedPolicyName = StringUtils.isBlank(name) ? policyConfigId : name;
  }

  /**
   * @return true if the group has a policy the logged in user cannot use (show it read-only)
   */
  public boolean isAssignedPolicyReadOnly() {
    return this.assignedPolicyReadOnly;
  }

  /**
   * @return config id of the assigned policy
   */
  public String getAssignedPolicyConfigId() {
    return this.assignedPolicyConfigId;
  }

  /**
   * @return name of the assigned policy
   */
  public String getAssignedPolicyName() {
    return this.assignedPolicyName;
  }

  /**
   * @return help text under a read-only assigned policy
   */
  public String getAssignedPolicyReadOnlyText() {
    return text(TEXT_KEY_READ_ONLY, "This policy is assigned to the group. You can keep it or remove it (None), but you are not allowed to assign it.");
  }

  /**
   * Check the submitted policy before the group edit saves anything. If not allowed, add a
   * validation message to the response.
   * @param group group being edited (caller checked ADMIN)
   * @param policyConfigId request parameter userLifecyclePolicy (null if not submitted)
   * @param loggedInSubject logged in subject
   * @return true if the save can continue, false if the caller should return
   */
  public static boolean validateGroupEditSubmit(Group group, String policyConfigId, Subject loggedInSubject) {

    if (UserLifecycleService.subjectCanSavePolicyConfigOnGroup(group, policyConfigId, loggedInSubject)) {
      return true;
    }

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
        "#ulp_none", text(TEXT_KEY_NOT_ALLOWED, "You are not allowed to assign that user lifecycle policy. Nothing was saved.")));
    return false;
  }

  /**
   * externalized text, or the default if the key is not configured (a patched site does not have
   * the new keys in its text properties)
   * @param key text key
   * @param defaultText fallback
   * @return the text
   */
  private static String text(String key, String defaultText) {
    String result = null;
    try {
      result = TextContainer.textOrNull(key);
    } catch (RuntimeException re) {
      // no text container (e.g. not in a request): use the default
      result = null;
    }
    return StringUtils.isBlank(result) ? defaultText : result;
  }
}
