/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class ProvisioningContainer {

  /**
   * 
   */
  public ProvisioningContainer() {
  }

  /**
   * if the user is a wheel group member or anyone or the member of the configured 
   * group
   * @return true if can see provisioning
   */
  public boolean isCanSeeProvisioning() {

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    String error = GrouperUiFilter.requireUiGroup("uiV2.provisioning.must.be.in.group", loggedInSubject);
    //null error means allow
    if (error == null) {
      return true;
    }
    return false;
  }

}
