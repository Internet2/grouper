/**
 * @author Grouper - external system references feature
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystemUsage;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * GUI wrapper around a {@link GrouperExternalSystemUsage} that knows how to turn
 * the usage's link type into a UI operation link (or no link, when the reference
 * type has no view page).  Rendered in the References section of the external
 * system view details screen.
 */
public class GuiGrouperExternalSystemUsage {

  /** the underlying usage (reference) */
  private GrouperExternalSystemUsage grouperExternalSystemUsage;

  /**
   * @param grouperExternalSystemUsage1 the underlying usage
   */
  public GuiGrouperExternalSystemUsage(GrouperExternalSystemUsage grouperExternalSystemUsage1) {
    this.grouperExternalSystemUsage = grouperExternalSystemUsage1;
  }

  /**
   * @return the underlying usage
   */
  public GrouperExternalSystemUsage getGrouperExternalSystemUsage() {
    return this.grouperExternalSystemUsage;
  }

  /**
   * @return human readable type of the reference
   */
  public String getUsageType() {
    return this.grouperExternalSystemUsage.getUsageType();
  }

  /**
   * @return display name of the referencing object
   */
  public String getName() {
    return this.grouperExternalSystemUsage.getName();
  }

  /**
   * @return how the external system is used
   */
  public String getDescription() {
    return this.grouperExternalSystemUsage.getDescription();
  }

  /**
   * @return true if this reference should render as a link
   */
  public boolean isHasLink() {
    return this.getLinkOperation() != null;
  }

  /**
   * build the UI operation query string (without a leading question mark) for this
   * reference, or null when there is no view page for its type (render as text).
   * @return the operation string or null
   */
  public String getLinkOperation() {

    String linkType = this.grouperExternalSystemUsage.getLinkType();
    if (linkType == null) {
      return null;
    }

    String encodedName = GrouperUtil.escapeUrlEncode(this.grouperExternalSystemUsage.getName());

    if (GrouperExternalSystemUsage.LINK_TYPE_PROVISIONER.equals(linkType)) {
      return "operation=UiV2ProvisionerConfiguration.viewProvisionerConfigDetails&provisionerConfigId=" + encodedName;
    } else if (GrouperExternalSystemUsage.LINK_TYPE_GROUP.equals(linkType)) {
      return "operation=UiV2Group.viewGroup&groupName=" + encodedName;
    } else if (GrouperExternalSystemUsage.LINK_TYPE_STEM.equals(linkType)) {
      return "operation=UiV2Stem.viewStem&stemName=" + encodedName;
    }

    return null;
  }

  /**
   * convert a list of usages into gui wrappers
   * @param usages the underlying usages
   * @return the gui wrappers
   */
  public static List<GuiGrouperExternalSystemUsage> convertFromGrouperExternalSystemUsages(
      List<GrouperExternalSystemUsage> usages) {

    List<GuiGrouperExternalSystemUsage> result = new ArrayList<GuiGrouperExternalSystemUsage>();
    for (GrouperExternalSystemUsage usage : GrouperUtil.nonNull(usages)) {
      result.add(new GuiGrouperExternalSystemUsage(usage));
    }
    return result;
  }

}
