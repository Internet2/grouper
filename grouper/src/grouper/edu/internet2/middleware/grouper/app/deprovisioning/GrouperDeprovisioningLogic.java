/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Map;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperDeprovisioningLogic {

  /**
   * 
   */
  public GrouperDeprovisioningLogic() {
  }

  /**
   * if user is allowed to deprovision
   * @param subject
   * @return true if allowed
   */
  public static boolean allowedToDeprovision(Subject subject) {
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }

    Map<String, GrouperDeprovisioningAffiliation> map =  GrouperDeprovisioningAffiliation.retrieveAffiliationsForUserManager(subject);

    return GrouperUtil.length(map) > 0;
  }
  
}
