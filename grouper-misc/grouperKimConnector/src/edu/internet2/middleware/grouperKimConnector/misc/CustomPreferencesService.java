/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import org.kuali.rice.kew.preferences.Preferences;
import org.kuali.rice.kew.preferences.service.impl.PreferencesServiceImpl;
import org.kuali.rice.kew.util.KEWConstants;


/**
 * hide title on search results screen
 */
public class CustomPreferencesService extends PreferencesServiceImpl {

  /**
   * @see org.kuali.rice.kew.preferences.service.impl.PreferencesServiceImpl#getPreferences(java.lang.String)
   */
  @Override
  public Preferences getPreferences(String principalId) {

    Preferences preferences =  super.getPreferences(principalId);

    if (KEWConstants.PREFERENCES_YES_VAL.equals(preferences.getShowDocTitle())) {

      preferences.setShowDocTitle(KEWConstants.PREFERENCES_NO_VAL);
      preferences.setRequiresSave(true);

    }
        
    return preferences;
    
  }


}
