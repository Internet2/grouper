/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperLoaderContainer {

  /**
   * 
   */
  public GrouperLoaderContainer() {
  }

  /**
   * 
   * @return if loader group
   */
  public boolean isLoaderGroup() {
    //TODO
    return false;
    
  }
  
  /**
   * show if grouper admin or loader group
   * @return true if shouldl show the loader menu item
   */
  public boolean isCanSeeLoader() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    String error = GrouperUiFilter.requireUiGroup("uiV2.loader.must.be.in.group", loggedInSubject);
    //null error means allow
    if (error == null) {
      return true;
    }
    
    if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.loaderTab.view.by.group.admins", true)) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
        return true;
      }
    }
    
    return false;
  }
  
}
