/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * <pre>
 * Enforces the option list of a provisioning metadata item on the server.
 *
 * A metadata item rendered as a dropdown, radio button or checkbox has a list of options
 * (GrouperProvisioningObjectMetadataItem.getKeysAndLabelsForDropdown()), either from the site
 * config (metadata.&lt;n&gt;.dropdownValues, or true/false for a boolean item) or built in by
 * the provisioner (e.g. the midPoint list of targets).  That list is what a wheel administrator
 * allows delegated users to send to the target, so a submitted value that is not one of the
 * option keys must be rejected, not only left out of the form.
 *
 * Rules:
 * - Only DROPDOWN, RADIOBUTTON and CHECKBOX items with a non-empty option list are restricted.
 *   Free-form items (TEXT, TEXTAREA, or a list with no options) accept anything, as before.
 * - Each non-blank submitted value (a single value, or every element of a multi-value
 *   submission) must equal the string form of the first key of some option, exactly
 *   (case-sensitive, not trimmed: the form only submits the keys it rendered).
 * - Blank values are not checked here; whether blank is allowed is the existing "required" check.
 *
 * This class only uses APIs that exist in every Grouper 5.x, 6.x and 7.x release, so it is the
 * same file everywhere.
 * </pre>
 */
public class GrouperProvisioningMetadataAllowedValues {

  /**
   * no instances
   */
  private GrouperProvisioningMetadataAllowedValues() {
  }

  /**
   * see if a metadata item restricts its values to its option list
   * @param metadataItem
   * @return true if the item is a dropdown, radio button or checkbox with at least one option
   */
  public static boolean isRestrictedToOptions(GrouperProvisioningObjectMetadataItem metadataItem) {
    if (metadataItem == null) {
      return false;
    }
    GrouperProvisioningObjectMetadataItemFormElementType formElementType = metadataItem.getFormElementType();
    if (formElementType != GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN
        && formElementType != GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON
        && formElementType != GrouperProvisioningObjectMetadataItemFormElementType.CHECKBOX) {
      return false;
    }
    List<MultiKey> keysAndLabels = metadataItem.getKeysAndLabelsForDropdown();
    return keysAndLabels != null && keysAndLabels.size() > 0;
  }

  /**
   * see if one submitted value is allowed for the item
   * @param metadataItem
   * @param value
   * @return true if the item is not restricted, the value is blank, or the value is an option key
   */
  public static boolean isAllowedValue(GrouperProvisioningObjectMetadataItem metadataItem, String value) {
    if (isBlank(value) || !isRestrictedToOptions(metadataItem)) {
      return true;
    }
    for (MultiKey keyAndLabel : metadataItem.getKeysAndLabelsForDropdown()) {
      if (keyAndLabel == null || keyAndLabel.size() == 0) {
        continue;
      }
      Object key = keyAndLabel.getKey(0);
      if (key != null && value.equals(String.valueOf(key))) {
        return true;
      }
    }
    return false;
  }

  /**
   * check a UI submission: a single value (dropdown, radio button) or the values of a checkbox
   * @param metadataItem
   * @param value single submitted value, or null
   * @param values multiple submitted values (checkbox), or null
   * @return the first submitted value that is not allowed, or null if all are allowed
   */
  public static String firstDisallowedValue(GrouperProvisioningObjectMetadataItem metadataItem, String value, String[] values) {
    if (!isRestrictedToOptions(metadataItem)) {
      return null;
    }
    if (!isAllowedValue(metadataItem, value)) {
      return value;
    }
    if (values != null) {
      // a multi-value submission is rejected as a whole if any element is not an option
      for (String oneValue : values) {
        if (!isAllowedValue(metadataItem, oneValue)) {
          return oneValue;
        }
      }
    }
    return null;
  }

  /**
   * check a value from an API caller (e.g. ProvisionableGroupSave): a scalar, a collection or an array
   * @param metadataItem
   * @param value
   * @return the first value (as a string) that is not allowed, or null if all are allowed
   */
  public static String firstDisallowedValue(GrouperProvisioningObjectMetadataItem metadataItem, Object value) {
    if (value == null || !isRestrictedToOptions(metadataItem)) {
      return null;
    }
    if (value instanceof Collection) {
      for (Object oneValue : (Collection<?>)value) {
        String disallowed = firstDisallowedValue(metadataItem, oneValue);
        if (disallowed != null) {
          return disallowed;
        }
      }
      return null;
    }
    if (value.getClass().isArray()) {
      int length = Array.getLength(value);
      for (int i = 0; i < length; i++) {
        String disallowed = firstDisallowedValue(metadataItem, Array.get(value, i));
        if (disallowed != null) {
          return disallowed;
        }
      }
      return null;
    }
    // booleans and numbers are compared by their string form, which is how the option keys are written
    String stringValue = String.valueOf(value);
    return isAllowedValue(metadataItem, stringValue) ? null : stringValue;
  }

  /**
   * blank check without a commons-lang / lang3 import (that import differs between 5.x and 6.x)
   * @param value
   * @return true if null or only whitespace
   */
  private static boolean isBlank(String value) {
    return value == null || value.trim().length() == 0;
  }
}
