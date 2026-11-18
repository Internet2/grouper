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
package edu.internet2.middleware.grouper.grouperUi.beans.dojo;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Feature authorization for UI autocomplete (dojo combo) endpoints.
 * <p>
 * A combo endpoint (e.g. UiV2Admin.subjectApiDiagnosticsActAsCombo) is a separate URL that anyone
 * logged in to the UI can call directly, without first opening the page it belongs to.  Some combos
 * belong to a feature that is restricted (wheel, or members of a configured group) and search as
 * GrouperSystem, so the feature's check must also run on the combo itself.  DojoComboLogic.logic
 * calls validationError() before the combo's own initialValidationError, lookup or search; a
 * non-null result is returned to the browser as the only item, and nothing is looked up.
 * </p>
 * <p>
 * Combos are identified by the method that declares the anonymous DojoComboQueryLogic (Java
 * reflection: getEnclosingClass / getEnclosingMethod), not by the request URL, so the check cannot
 * be sidestepped by a different URL shape.  Combos not in the registry keep their current behavior.
 * </p>
 * <p>
 * Kept in its own class (only APIs present in every 5.x-7.x UI) so the same file ships in every
 * release; DojoComboLogic only gets a few lines.
 * </p>
 */
public class DojoComboAuthorization {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(DojoComboAuthorization.class);

  /**
   * a feature check for one combo
   */
  public static interface ComboFeatureCheck {

    /**
     * @return null if the logged-in user may use the combo, else the error to show
     */
    public String validationError();
  }

  /**
   * combo key (enclosing class name + "." + enclosing method name) to feature check
   */
  private static final Map<String, ComboFeatureCheck> comboKeyToCheck;

  static {
    Map<String, ComboFeatureCheck> theMap = new HashMap<String, ComboFeatureCheck>();

    // subject API diagnostics: same decision as UiV2Admin.subjectApiDiagnosticsAllowed() (wheel or
    // root, or member of uiV2.admin.subjectApiDiagnostics.must.be.in.group).  The combo searches
    // subjects as GrouperSystem, so it must not be open to every UI user.
    theMap.put("edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Admin.subjectApiDiagnosticsActAsCombo",
        new ComboFeatureCheck() {

          @Override
          public String validationError() {
            if (GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer().isSubjectApiDiagnosticsShow()) {
              return null;
            }
            return text("adminSubjectApiDiagnosticsErrorNotAllowed", "You are not allowed to use subject API diagnostics");
          }
        });

    comboKeyToCheck = Collections.unmodifiableMap(theMap);
  }

  /**
   * no instances
   */
  private DojoComboAuthorization() {
  }

  /**
   * key of the combo that declares this query logic class: enclosing class name + "." + enclosing
   * method name, or null if the class is not declared inside a method (e.g. a named class)
   * @param queryLogicClass
   * @return the key or null
   */
  public static String comboKey(Class<?> queryLogicClass) {
    if (queryLogicClass == null) {
      return null;
    }
    Method enclosingMethod = queryLogicClass.getEnclosingMethod();
    if (enclosingMethod == null) {
      return null;
    }
    return enclosingMethod.getDeclaringClass().getName() + "." + enclosingMethod.getName();
  }

  /**
   * @param comboKey
   * @return true if the combo needs a feature check
   */
  public static boolean isRegistered(String comboKey) {
    return comboKey != null && comboKeyToCheck.containsKey(comboKey);
  }

  /**
   * run the feature check for a combo key as the logged-in UI user
   * @param comboKey enclosing class name + "." + method name
   * @return null if allowed (or not registered), else the error text
   */
  public static String validationErrorForComboKey(String comboKey) {
    if (!isRegistered(comboKey)) {
      return null;
    }
    String error = null;
    try {
      error = comboKeyToCheck.get(comboKey).validationError();
    } catch (RuntimeException re) {
      // fail closed: a registered combo whose check cannot run is denied
      LOG.error("Error checking authorization for combo " + comboKey, re);
      error = text("adminSubjectApiDiagnosticsErrorNotAllowed", "You are not allowed to use this feature");
    }
    if (error != null) {
      Subject loggedInSubject = null;
      try {
        loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      } catch (RuntimeException re) {
        // only for the log message
      }
      LOG.warn("Denied combo " + comboKey + " for subject "
          + (loggedInSubject == null ? null : loggedInSubject.getSourceId() + "::" + loggedInSubject.getId()));
    }
    return error;
  }

  /**
   * feature check for the combo that declares this query logic, called by DojoComboLogic.logic
   * before any lookup or search
   * @param request
   * @param grouperSession session of the logged-in user
   * @param dojoComboQueryLogic
   * @return null if allowed, else the error text
   */
  public static String validationError(HttpServletRequest request, GrouperSession grouperSession,
      DojoComboQueryLogic<?> dojoComboQueryLogic) {
    if (dojoComboQueryLogic == null) {
      return null;
    }
    return validationErrorForComboKey(comboKey(dojoComboQueryLogic.getClass()));
  }

  /**
   * externalized text with a fallback (the text container needs a UI request)
   * @param key
   * @param defaultText
   * @return the text
   */
  private static String text(String key, String defaultText) {
    try {
      String text = TextContainer.retrieveFromRequest().getText().get(key);
      if (text != null && text.trim().length() > 0) {
        return text;
      }
    } catch (RuntimeException re) {
      // no UI request (e.g. unit test), use the default
    }
    return defaultText;
  }

}
