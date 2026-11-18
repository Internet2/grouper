package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Per item metadata rules for the programmatic provisioning save API
 * ({@link ProvisionableGroupSave}), the same rules the UI save handlers apply.</p>
 *
 * <p>ProvisionableGroupSave lets a non-wheel caller save provisioning settings on a group when the
 * caller may assign the target (groupAllowedToAssign and ADMIN on the group); it is reached from
 * GSH and, in 7.x, the MCP group_save tool.  It checked required, value type, unknown names and the
 * provisioner's validateMetadataInputForGroup, but none of the item settings below, so the caller
 * could store any value in an item the wheel administrator had locked.</p>
 *
 * <p>Rules, for callers that are not wheel or root (wheel, root and runAsRoot saves are not
 * checked, as before, so migrations and admin scripts keep working):</p>
 * <ul>
 *   <li><b>groupIdThatCanUpdate</b> (see {@link GrouperProvisioningMetadataItemAuthorization}),
 *   <b>canUpdate=false</b> on an existing assignment, and <b>canChange=false</b>
 *   when a value is stored: the item is locked.  Setting a locked item to a value other than the
 *   stored one is an error; a locked item the caller did not set keeps its stored value (or stays
 *   absent), also when replaceAllSettings is used.</li>
 *   <li><b>Options</b>: a dropdown, radio button or checkbox item with an option list only accepts
 *   those options ({@link GrouperProvisioningMetadataAllowedValues}).</li>
 *   <li><b>validateUniqueValue</b>: a group item value may not equal the value stored for the same
 *   item on another group of this provisioner.</li>
 * </ul>
 *
 * <p>Errors are RuntimeExceptions naming the item but not the value, like the other
 * ProvisionableGroupSave errors.</p>
 *
 * <p>Only uses APIs that exist in every release that has a non-wheel ProvisionableGroupSave
 * (4.22.3+, 6.1.1+, 7.0.2+), so the same file ships in every patch.</p>
 */
public class GrouperProvisioningMetadataApiSaveRules {

  /**
   * no instances
   */
  private GrouperProvisioningMetadataApiSaveRules() {
  }

  /**
   * whether the rules apply to this caller
   * @param subject caller (subject of the session that called save)
   * @param runAsRoot if the caller asked to run as root
   * @return true if the item rules must be enforced
   */
  public static boolean appliesTo(Subject subject, boolean runAsRoot) {
    if (runAsRoot) {
      return false;
    }
    return subject == null || !PrivilegeHelper.isWheelOrRoot(subject);
  }

  /**
   * whether a non-wheel caller may not change this item
   * @param metadataItem item
   * @param storedValue value stored now (null if none)
   * @param isInsert true if there are no provisioning settings on the object yet
   * @param subject caller
   * @return true if locked
   */
  public static boolean isLocked(GrouperProvisioningObjectMetadataItem metadataItem, Object storedValue,
      boolean isInsert, Subject subject) {
    if (!GrouperProvisioningMetadataItemAuthorization.canUpdate(metadataItem, subject)) {
      return true;
    }
    if (!isInsert && !metadataItem.isCanUpdate()) {
      return true;
    }
    if (!metadataItem.isCanChange() && storedValue != null) {
      return true;
    }
    return false;
  }

  /**
   * Enforce the item rules on a save.  Call after the values were converted and before the
   * provisioner's own validation.
   * @param grouperProvisioner initialized provisioner (for the uniqueness lookup)
   * @param metadataItems items for this object type (e.g. the showForGroup items)
   * @param callerMetadataMap what the caller assigned (not merged with stored values)
   * @param metadataNameValuesToSave converted values that will be stored, changed in place
   * @param storedMetadataNameValues values stored now, null if none
   * @param isInsert true if there are no provisioning settings on the object yet
   * @param subject caller
   * @param groupNameToSkip name of the group being saved (for the uniqueness check), or null to
   * skip the uniqueness check
   */
  public static void enforce(GrouperProvisioner grouperProvisioner,
      List<GrouperProvisioningObjectMetadataItem> metadataItems,
      Map<String, Object> callerMetadataMap,
      Map<String, Object> metadataNameValuesToSave,
      Map<String, Object> storedMetadataNameValues,
      boolean isInsert, Subject subject, String groupNameToSkip) {

    if (metadataItems == null || metadataNameValuesToSave == null) {
      return;
    }

    // all provisioned groups' stored values, read once and only if an item needs it
    Map<String, GrouperProvisioningObjectAttributes> allGroupAttributes = null;

    for (GrouperProvisioningObjectMetadataItem metadataItem : metadataItems) {

      if (metadataItem == null) {
        continue;
      }
      String name = metadataItem.getName();
      boolean callerSetIt = callerMetadataMap != null && callerMetadataMap.containsKey(name);
      Object callerValue = callerSetIt ? callerMetadataMap.get(name) : null;
      Object storedValue = storedMetadataNameValues == null ? null : storedMetadataNameValues.get(name);

      // locked items: refuse a change, otherwise keep what is stored
      if (isLocked(metadataItem, storedValue, isInsert, subject)) {
        if (callerSetIt && !GrouperUtil.equals(metadataNameValuesToSave.get(name), storedValue)) {
          throw new RuntimeException("'" + name + "' cannot be changed by this subject.");
        }
        if (storedValue != null) {
          metadataNameValuesToSave.put(name, storedValue);
        } else {
          metadataNameValuesToSave.remove(name);
        }
        continue;
      }

      // option lists of dropdown / radio button / checkbox items
      if (callerValue != null
          && GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(metadataItem, callerValue) != null) {
        throw new RuntimeException("'" + name + "' value is not one of the allowed options.");
      }

      // unique values across the provisioner's groups (same rule as the UI)
      Object valueToSave = metadataNameValuesToSave.get(name);
      if (groupNameToSkip != null && metadataItem.isValidateUniqueValue() && metadataItem.isShowForGroup()
          && valueToSave != null && !GrouperUtil.isBlank(valueToSave.toString())) {
        if (allGroupAttributes == null) {
          allGroupAttributes = grouperProvisioner.retrieveGrouperDao().retrieveAllProvisioningGroupAttributes();
        }
        for (String otherGroupName : GrouperUtil.nonNull(allGroupAttributes).keySet()) {
          if (groupNameToSkip.equals(otherGroupName)) {
            continue;
          }
          GrouperProvisioningObjectAttributes otherAttributes = allGroupAttributes.get(otherGroupName);
          Map<String, Object> otherValues = otherAttributes == null ? null : otherAttributes.getMetadataNameValues();
          Object otherValue = otherValues == null ? null : otherValues.get(name);
          if (otherValue != null && valueToSave.toString().equals(otherValue.toString())) {
            throw new RuntimeException("'" + name + "' value is already used by another group.");
          }
        }
      }
    }
  }
}
