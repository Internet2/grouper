package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Checks provisioning metadata items whose value is the name of another Grouper group, e.g.
 * the Datadog <code>md_adminGroupName</code> and the TrueFoundry
 * <code>md_trueFoundryManagerGroupName</code> items.</p>
 *
 * <p>The provisioner reads the members of the named group as root and sends them to the target
 * (as team admins / managers).  So whoever saves the name decides which group's membership is
 * copied out of Grouper.  A delegated user (in groupAllowedToAssign and ADMIN of the provisioned
 * group) must therefore be able to READ the named group; otherwise they could copy, and observe
 * through the target or the provisioning details screen, the members of any private group.</p>
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>A blank value is always allowed (nothing is copied).</li>
 *   <li>Wheel and root may name any group.</li>
 *   <li>Anyone else needs READ on the named group.  A group that does not exist and a group the
 *   subject cannot read get the same message, so the check does not reveal which group names
 *   exist.</li>
 *   <li>No subject (no session) is not allowed past a non-blank value.</li>
 * </ul>
 *
 * <p>The subject is the one in the static Grouper session when the provisioner's
 * validateMetadataInputFor* method is called: the logged in user in the UI save handlers, and
 * the caller of ProvisionableGroupSave (which runs that validation in the caller's session).</p>
 *
 * <p>Only uses APIs that exist in every release that has the Datadog / TrueFoundry provisioners
 * (4.22.3+, 6.1.1+, 7.0.3+), so the same file ships in every patch.</p>
 */
public class GrouperProvisioningMetadataGroupReference {

  /**
   * message for a group that is missing or not readable (deliberately the same for both, and
   * without the submitted value, so nothing is reflected or revealed)
   */
  public static final String ERROR_MESSAGE = "Group not found, or you are not allowed to read it";

  /**
   * no instances
   */
  private GrouperProvisioningMetadataGroupReference() {
  }

  /**
   * whether the subject may name this group in a group-reference metadata item
   * @param subject subject saving the metadata, null means unknown (not allowed)
   * @param groupName value of the metadata item (a group system name)
   * @return true if allowed
   */
  public static boolean canReferenceGroup(final Subject subject, final String groupName) {
    if (GrouperUtil.isBlank(groupName)) {
      return true;
    }
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    // look the group up as root: the subject may not even have VIEW on it, and that must give
    // the same answer as a group that does not exist
    Boolean allowed = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        Group group = GroupFinder.findByName(rootSession, groupName, false);
        if (group == null) {
          return false;
        }
        return group.canHavePrivilege(subject, AccessPrivilege.READ.getName(), false);
      }
    });
    return allowed != null && allowed;
  }

  /**
   * subject of the static grouper session, or null if there is none
   * @return the subject
   */
  public static Subject subjectInSession() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    return grouperSession == null ? null : grouperSession.getSubject();
  }

  /**
   * add an error for each group-reference item in the input whose group the subject in the
   * static session may not reference
   * @param errorMessages map from the superclass validation (may be null), added to
   * @param nameToValueFromUsersInput metadata name to value being saved
   * @param groupReferenceMetadataNames names of the items whose value is a group name
   * @return the error map (null or empty if no errors)
   */
  public static Map<String, String> validate(Map<String, String> errorMessages,
      Map<String, Object> nameToValueFromUsersInput, Collection<String> groupReferenceMetadataNames) {
    if (nameToValueFromUsersInput == null || groupReferenceMetadataNames == null) {
      return errorMessages;
    }
    Subject subject = null;
    boolean subjectLookedUp = false;
    for (String metadataName : groupReferenceMetadataNames) {
      if (GrouperUtil.isBlank(metadataName) || !nameToValueFromUsersInput.containsKey(metadataName)) {
        continue;
      }
      Object value = nameToValueFromUsersInput.get(metadataName);
      String groupName = value == null ? null : value.toString();
      if (GrouperUtil.isBlank(groupName)) {
        continue;
      }
      if (!subjectLookedUp) {
        subject = subjectInSession();
        subjectLookedUp = true;
      }
      if (!canReferenceGroup(subject, groupName)) {
        if (errorMessages == null) {
          errorMessages = new LinkedHashMap<String, String>();
        }
        errorMessages.put(metadataName, ERROR_MESSAGE);
      }
    }
    return errorMessages;
  }
}
