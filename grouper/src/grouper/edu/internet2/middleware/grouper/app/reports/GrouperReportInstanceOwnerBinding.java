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
package edu.internet2.middleware.grouper.app.reports;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Looks up a report instance by its attribute assign id only if it belongs to a given group or
 * folder.</p>
 *
 * <p>Why: the UI report detail and download screens (UiV2GrouperReport) take the group or folder
 * and the report instance id as two separate request parameters.  They checked VIEW on the
 * group or folder and the report config's viewer group, but never that the instance belonged to
 * that group or folder.  A report viewer could then pair a private group's report instance with
 * any group they can VIEW.  This class requires that the instance assignment and the report
 * config marker assignment it points to are both report assignments on exactly that owner.</p>
 *
 * <p>Every failure (blank, malformed, deleted, wrong attribute, other owner) returns null, so the
 * caller shows one generic message and nothing tells the user which check failed.  Must be called
 * in a session that can read the assignments (the UI calls it in a root callback after it resolved
 * the owner as the logged in user).</p>
 */
public class GrouperReportInstanceOwnerBinding {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperReportInstanceOwnerBinding.class);

  /**
   * no instances
   */
  private GrouperReportInstanceOwnerBinding() {
  }

  /**
   * @param attributeAssignId id of the report instance attribute assignment from the request
   * @param owner group or folder the request names (already checked for VIEW as the logged in user)
   * @return the report instance, or null if it does not exist or is not a report of this owner
   */
  public static GrouperReportInstance retrieveReportInstanceForOwner(String attributeAssignId, GrouperObject owner) {
    if (StringUtils.isBlank(attributeAssignId) || owner == null) {
      return null;
    }
    try {
      AttributeAssign instanceAssign = AttributeAssignFinder.findById(attributeAssignId, false);
      if (!isAssignOnOwner(instanceAssign, GrouperReportInstanceAttributeNames.retrieveAttributeDefNameBase(), owner)) {
        return null;
      }
      GrouperReportInstance reportInstance = GrouperReportInstanceService.getReportInstance(attributeAssignId);
      if (reportInstance == null) {
        return null;
      }
      // the instance points to its report config by the config marker assignment id: that must be on the same owner too
      String configMarkerAssignmentId = reportInstance.getReportInstanceConfigMarkerAssignmentId();
      if (StringUtils.isBlank(configMarkerAssignmentId)) {
        return null;
      }
      AttributeAssign configMarkerAssign = AttributeAssignFinder.findById(configMarkerAssignmentId, false);
      if (!isAssignOnOwner(configMarkerAssign, GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase(), owner)) {
        return null;
      }
      if (reportInstance.getGrouperReportConfigurationBean() == null) {
        return null;
      }
      return reportInstance;
    } catch (RuntimeException re) {
      // malformed or half deleted data: same answer as not found
      LOG.debug("report instance not found for owner: " + attributeAssignId, re);
      return null;
    }
  }

  /**
   * @param attributeAssign assignment or null
   * @param attributeDefName the expected attribute (report instance or report config marker)
   * @param owner group or folder
   * @return true if the assignment is that attribute assigned directly to that owner
   */
  static boolean isAssignOnOwner(AttributeAssign attributeAssign, AttributeDefName attributeDefName, GrouperObject owner) {
    if (attributeAssign == null || attributeDefName == null || owner == null) {
      return false;
    }
    if (!StringUtils.equals(attributeAssign.getAttributeDefNameId(), attributeDefName.getId())) {
      return false;
    }
    if (owner instanceof Group) {
      return attributeAssign.getAttributeAssignType() == AttributeAssignType.group
          && StringUtils.equals(attributeAssign.getOwnerGroupId(), ((Group)owner).getId());
    }
    if (owner instanceof Stem) {
      return attributeAssign.getAttributeAssignType() == AttributeAssignType.stem
          && StringUtils.equals(attributeAssign.getOwnerStemId(), ((Stem)owner).getId());
    }
    return false;
  }
}
