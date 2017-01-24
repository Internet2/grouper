/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.subject.Subject;


/**
 * admin container
 * @author mchyzer
 *
 */
public class AdminContainer {

  /**
   * if import from group
   * @return if from group
   */
  public boolean isSubjectApiDiagnosticsShow() {
    
    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.admin.subjectApiDiagnostics.show", true)) {
      return false;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return false;
    }
    
    String error = GrouperUiFilter.requireUiGroup("uiV2.admin.subjectApiDiagnostics.must.be.in.group", loggedInSubject);

    if (!StringUtils.isBlank(error)) {
      return false;
    }
    
    return true;
  }

}
