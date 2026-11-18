package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Enforces the per metadata item access groups of a provisioner:
 * <code>provisioner.&lt;configId&gt;.metadata.&lt;n&gt;.groupIdThatCanView</code> and
 * <code>provisioner.&lt;configId&gt;.metadata.&lt;n&gt;.groupIdThatCanUpdate</code>.</p>
 *
 * <p>These settings were loaded into {@link GrouperProvisioningObjectMetadataItem} since the
 * metadata feature was added, but nothing checked them, so a restricted item could be viewed and
 * changed by anyone who could edit provisioning on the object.  The UI calls this class at the
 * points where metadata items and values are handed to a JSP or read from a request.</p>
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>A blank setting means unrestricted (the behavior before this class existed).</li>
 *   <li>Wheel and root may always view and update.</li>
 *   <li>The setting holds a group id (uuid).  The group is looked up as root, so the user does
 *   not need VIEW on it.  An id that does not resolve fails closed (not allowed).</li>
 *   <li>A member of the update group may also view the item (the update setting is documented
 *   as "can view/edit").</li>
 *   <li>A null subject is never allowed past a configured setting.</li>
 * </ul>
 *
 * <p>This class is only for UI / user facing callers.  The provisioning engine runs as root and
 * must see every item, so do not call this from the engine.</p>
 *
 * <p>Only uses APIs that exist in Grouper 5.0 through 7.x, so the same file can ship in every
 * release's patch.</p>
 */
public class GrouperProvisioningMetadataItemAuthorization {

  /**
   * no instances
   */
  private GrouperProvisioningMetadataItemAuthorization() {
  }

  // ---------------------------------------------------------------------------------------------
  // single item checks
  // ---------------------------------------------------------------------------------------------

  /**
   * whether the subject may see this metadata item and its value
   * @param metadataItem item, null means not a configured item (unrestricted)
   * @param subject logged in subject
   * @return true if allowed
   */
  public static boolean canView(GrouperProvisioningObjectMetadataItem metadataItem, Subject subject) {
    if (metadataItem == null) {
      return true;
    }
    return canViewByGroupIds(metadataItem.getGroupIdThatCanView(), metadataItem.getGroupIdThatCanUpdate(), subject);
  }

  /**
   * whether the subject may change this metadata item
   * @param metadataItem item, null means not a configured item (unrestricted)
   * @param subject logged in subject
   * @return true if allowed
   */
  public static boolean canUpdate(GrouperProvisioningObjectMetadataItem metadataItem, Subject subject) {
    if (metadataItem == null) {
      return true;
    }
    return canUpdateByGroupId(metadataItem.getGroupIdThatCanUpdate(), subject);
  }

  /**
   * whether the subject may see a metadata value, looked up by provisioner config id and
   * metadata name from the provisioner config.  For callers that have a stored name / value map
   * but no initialized provisioner (e.g. the provisioner tables on the group and folder screens).
   * A name that is not a configured metadata item (e.g. built in md_grouper_ names) is
   * unrestricted.
   * @param provisionerConfigId provisioner config id (target name)
   * @param metadataName e.g. md_myRole
   * @param subject logged in subject
   * @return true if allowed
   */
  public static boolean canViewMetadataName(String provisionerConfigId, String metadataName, Subject subject) {
    if (GrouperUtil.isBlank(provisionerConfigId) || GrouperUtil.isBlank(metadataName)) {
      return true;
    }
    // same lookup as GrouperProvisioningConfiguration.retrieveConfigString
    int numberOfMetadata = GrouperUtil.intValue(configString(provisionerConfigId, "numberOfMetadata"), 0);
    for (int i = 0; i < numberOfMetadata; i++) {
      if (metadataName.equals(configString(provisionerConfigId, "metadata." + i + ".name"))) {
        return canViewByGroupIds(configString(provisionerConfigId, "metadata." + i + ".groupIdThatCanView"),
            configString(provisionerConfigId, "metadata." + i + ".groupIdThatCanUpdate"), subject);
      }
    }
    return true;
  }

  // ---------------------------------------------------------------------------------------------
  // list helpers for the UI handlers
  // ---------------------------------------------------------------------------------------------

  /**
   * items the subject may view, for read only screens
   * @param metadataItems items
   * @param subject logged in subject
   * @return new list, never null
   */
  public static List<GrouperProvisioningObjectMetadataItem> filterViewable(
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject) {
    List<GrouperProvisioningObjectMetadataItem> result = new ArrayList<GrouperProvisioningObjectMetadataItem>();
    if (metadataItems != null) {
      for (GrouperProvisioningObjectMetadataItem metadataItem : metadataItems) {
        if (canView(metadataItem, subject)) {
          result.add(metadataItem);
        }
      }
    }
    return result;
  }

  /**
   * items for an edit form: items the subject cannot view are removed, and items the subject may
   * view but not update are marked read only.  Read only only affects how the form renders; the
   * save handlers enforce update with {@link #filterUpdatable(List, Subject)} and the
   * preserveRestrictedValues methods.
   * @param metadataItems items
   * @param subject logged in subject
   * @return new list, never null
   */
  public static List<GrouperProvisioningObjectMetadataItem> restrictForEditForm(
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject) {
    List<GrouperProvisioningObjectMetadataItem> result = filterViewable(metadataItems, subject);
    for (GrouperProvisioningObjectMetadataItem metadataItem : result) {
      if (!canUpdate(metadataItem, subject)) {
        metadataItem.setReadOnly(true);
      }
    }
    return result;
  }

  /**
   * items whose value a save handler may read from the request.  Restricted items are left out so
   * a submitted value is ignored and a required item the user cannot see does not fail validation.
   * @param metadataItems items
   * @param subject logged in subject
   * @return new list, never null
   */
  public static List<GrouperProvisioningObjectMetadataItem> filterUpdatable(
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject) {
    List<GrouperProvisioningObjectMetadataItem> result = new ArrayList<GrouperProvisioningObjectMetadataItem>();
    if (metadataItems != null) {
      for (GrouperProvisioningObjectMetadataItem metadataItem : metadataItems) {
        if (canUpdate(metadataItem, subject)) {
          result.add(metadataItem);
        }
      }
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------------
  // save helpers: put back stored values of items the subject may not update
  // ---------------------------------------------------------------------------------------------

  /**
   * The save handlers replace the whole stored metadata map with the map built from the request.
   * For every item the subject may not update, put the stored value back (or remove the key if
   * nothing was stored), so a restricted value is neither changed nor dropped when the user saves
   * other items.
   * @param metadataNameValuesToSave map that will be saved, changed in place
   * @param databaseMetadataNameValues stored values, null if nothing stored
   * @param metadataItems items for this object type (all of them, not only the updatable ones)
   * @param subject logged in subject
   */
  public static void preserveRestrictedValues(Map<String, Object> metadataNameValuesToSave,
      Map<String, Object> databaseMetadataNameValues,
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject) {
    if (metadataNameValuesToSave == null || metadataItems == null) {
      return;
    }
    for (GrouperProvisioningObjectMetadataItem metadataItem : metadataItems) {
      if (metadataItem == null || canUpdate(metadataItem, subject)) {
        continue;
      }
      String name = metadataItem.getName();
      if (databaseMetadataNameValues != null && databaseMetadataNameValues.containsKey(name)) {
        metadataNameValuesToSave.put(name, databaseMetadataNameValues.get(name));
      } else {
        metadataNameValuesToSave.remove(name);
      }
    }
  }

  /**
   * {@link #preserveRestrictedValues(Map, Map, List, Subject)} for a group or folder save, reads
   * the stored values as root
   * @param metadataNameValuesToSave map that will be saved, changed in place
   * @param metadataItems items for this object type
   * @param subject logged in subject
   * @param groupOrFolder group or stem
   * @param targetName provisioner config id
   */
  public static void preserveRestrictedValuesOnGroupOrFolder(Map<String, Object> metadataNameValuesToSave,
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject,
      final GrouperObject groupOrFolder, final String targetName) {
    if (!hasRestrictedItem(metadataItems, subject)) {
      return;
    }
    GrouperProvisioningAttributeValue stored = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GrouperProvisioningService.getProvisioningAttributeValue(groupOrFolder, targetName);
      }
    });
    preserveRestrictedValues(metadataNameValuesToSave, stored == null ? null : stored.getMetadataNameValues(), metadataItems, subject);
  }

  /**
   * {@link #preserveRestrictedValues(Map, Map, List, Subject)} for a subject (member) save, reads
   * the stored values as root
   * @param metadataNameValuesToSave map that will be saved, changed in place
   * @param metadataItems items for this object type
   * @param subject logged in subject
   * @param provisionedSubject subject whose provisioning settings are saved
   * @param targetName provisioner config id
   */
  public static void preserveRestrictedValuesOnSubject(Map<String, Object> metadataNameValuesToSave,
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject,
      final Subject provisionedSubject, final String targetName) {
    if (!hasRestrictedItem(metadataItems, subject)) {
      return;
    }
    GrouperProvisioningAttributeValue stored = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Member member = MemberFinder.findBySubject(grouperSession, provisionedSubject, false);
        return member == null ? null : GrouperProvisioningService.getProvisioningAttributeValue(member, targetName);
      }
    });
    preserveRestrictedValues(metadataNameValuesToSave, stored == null ? null : stored.getMetadataNameValues(), metadataItems, subject);
  }

  /**
   * {@link #preserveRestrictedValues(Map, Map, List, Subject)} for a membership save, reads the
   * stored values as root
   * @param metadataNameValuesToSave map that will be saved, changed in place
   * @param metadataItems items for this object type
   * @param subject logged in subject
   * @param group group of the membership
   * @param provisionedSubject member subject of the membership
   * @param targetName provisioner config id
   */
  public static void preserveRestrictedValuesOnMembership(Map<String, Object> metadataNameValuesToSave,
      List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject,
      final Group group, final Subject provisionedSubject, final String targetName) {
    if (!hasRestrictedItem(metadataItems, subject)) {
      return;
    }
    GrouperProvisioningAttributeValue stored = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Member member = MemberFinder.findBySubject(grouperSession, provisionedSubject, false);
        return member == null ? null : GrouperProvisioningService.getProvisioningAttributeValue(group, member, targetName);
      }
    });
    preserveRestrictedValues(metadataNameValuesToSave, stored == null ? null : stored.getMetadataNameValues(), metadataItems, subject);
  }

  // ---------------------------------------------------------------------------------------------
  // internals
  // ---------------------------------------------------------------------------------------------

  /**
   * whether any item is not updatable by the subject (skip the stored value read when none is)
   * @param metadataItems items
   * @param subject logged in subject
   * @return true if at least one item is restricted for the subject
   */
  private static boolean hasRestrictedItem(List<GrouperProvisioningObjectMetadataItem> metadataItems, Subject subject) {
    if (metadataItems != null) {
      for (GrouperProvisioningObjectMetadataItem metadataItem : metadataItems) {
        if (metadataItem != null && !canUpdate(metadataItem, subject)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * view rule: no view or update group configured, or in either group (update implies view)
   * @param groupIdThatCanView view group id or blank
   * @param groupIdThatCanUpdate update group id or blank
   * @param subject logged in subject
   * @return true if allowed
   */
  private static boolean canViewByGroupIds(String groupIdThatCanView, String groupIdThatCanUpdate, Subject subject) {
    if (GrouperUtil.isBlank(groupIdThatCanView)) {
      // no view restriction.  An update group alone restricts update only, not view.
      return true;
    }
    if (isWheelOrRoot(subject)) {
      return true;
    }
    if (isMemberOfGroupId(groupIdThatCanView, subject)) {
      return true;
    }
    // members of the update group can view too
    return !GrouperUtil.isBlank(groupIdThatCanUpdate) && isMemberOfGroupId(groupIdThatCanUpdate, subject);
  }

  /**
   * update rule: no update group configured, or in it
   * @param groupIdThatCanUpdate update group id or blank
   * @param subject logged in subject
   * @return true if allowed
   */
  private static boolean canUpdateByGroupId(String groupIdThatCanUpdate, Subject subject) {
    if (GrouperUtil.isBlank(groupIdThatCanUpdate)) {
      return true;
    }
    if (isWheelOrRoot(subject)) {
      return true;
    }
    return isMemberOfGroupId(groupIdThatCanUpdate, subject);
  }

  /**
   * @param subject subject or null
   * @return true if wheel or root, false for null
   */
  private static boolean isWheelOrRoot(Subject subject) {
    return subject != null && PrivilegeHelper.isWheelOrRoot(subject);
  }

  /**
   * whether the subject is an effective member of the group with this id.  Looked up as root so
   * the user does not need VIEW on the access group.  An id that does not resolve fails closed.
   * @param groupId group uuid
   * @param subject subject or null
   * @return true if member
   */
  private static boolean isMemberOfGroupId(final String groupId, final Subject subject) {
    if (subject == null || GrouperUtil.isBlank(groupId)) {
      return false;
    }
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group group = GroupFinder.findByUuid(grouperSession, groupId.trim(), false);
        if (group == null) {
          return false;
        }
        return group.hasMember(subject);
      }
    });
  }

  /**
   * provisioner config value, with the provisionerDefault fallback, same as
   * GrouperProvisioningConfiguration.retrieveConfigString
   * @param provisionerConfigId provisioner config id
   * @param configSuffix e.g. metadata.0.name
   * @return value or null
   */
  private static String configString(String provisionerConfigId, String configSuffix) {
    String value = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + provisionerConfigId + "." + configSuffix);
    if (!GrouperUtil.isBlank(value)) {
      return value;
    }
    value = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisionerDefault." + configSuffix);
    if (!GrouperUtil.isBlank(value)) {
      return value;
    }
    return null;
  }

}
