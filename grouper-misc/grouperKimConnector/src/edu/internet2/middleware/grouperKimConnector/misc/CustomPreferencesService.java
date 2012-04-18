/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
