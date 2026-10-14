package edu.internet2.middleware.grouper.app.smartsheet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Compare for the Smartsheet provisioner. Only changes how seatType compares:
 * <ul>
 *   <li>the deprecated VIEWER seat equals CONTRIBUTOR</li>
 *   <li>unless smartsheetDowngradeProvisionalMembers is set, a PROVISIONAL_MEMBER in Smartsheet
 *       counts as matching a computed CONTRIBUTOR or GUEST seat: a user downgraded by the API can
 *       never be provisional again, so Grouper leaves provisional members to Smartsheet's own
 *       expiry. Treating them as equal (rather than skipping the call in the DAO) keeps the
 *       framework from re-trying the change on every sync.</li>
 * </ul>
 */
public class SmartsheetProvisioningCompare extends GrouperProvisioningCompare {

  @Override
  public boolean attributeValueEquals(String attributeName, Object grouperValue, Object targetValue,
      ProvisioningUpdatable grouperTargetUpdatable) {

    if (!StringUtils.equals(SmartsheetUser.ATTR_SEAT_TYPE, attributeName)) {
      return super.attributeValueEquals(attributeName, grouperValue, targetValue, grouperTargetUpdatable);
    }

    String grouperSeat = SmartsheetUser.normalizeSeatType(GrouperUtil.stringValue(grouperValue));
    String targetSeat = SmartsheetUser.normalizeSeatType(GrouperUtil.stringValue(targetValue));

    if (StringUtils.equals(grouperSeat, targetSeat)) {
      return true;
    }

    SmartsheetProvisionerConfiguration config = (SmartsheetProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    return isProvisionalLeftAlone(config, targetSeat, grouperSeat);
  }

  /**
   * @param config the provisioner config
   * @param currentSeat the seat in Smartsheet (normalized)
   * @param desiredSeat the computed seat (normalized)
   * @return true if this is a downgrade of a provisional member that Grouper must not make
   */
  static boolean isProvisionalLeftAlone(SmartsheetProvisionerConfiguration config, String currentSeat, String desiredSeat) {
    return !config.isSmartsheetDowngradeProvisionalMembers()
        && SmartsheetUser.SEAT_PROVISIONAL_MEMBER.equals(currentSeat)
        && (SmartsheetUser.SEAT_CONTRIBUTOR.equals(desiredSeat) || SmartsheetUser.SEAT_GUEST.equals(desiredSeat));
  }

}
