package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;

/**
 * Masks the values of GSH template inputs that are configured with formElementType = password
 * before they are written anywhere other than the template itself: the GSH_TEMPLATE_EXEC audit
 * entry description, and the variable map that is put in exception text when an input showEl
 * script fails (which ends up in the UI / daemon logs).
 *
 * A template author who picks a password box for an input is collecting a secret (a new service
 * account password, an API token to store, ...).  The template still gets the real value; only the
 * copies made for audit and logging are masked.
 *
 * Only uses APIs that exist in every Grouper release with GSH templates (2.6 and up), so the same
 * class is shipped to every release.
 */
public class GshTemplateInputMask {

  /**
   * what a masked value looks like, same as masked passwords in the config screens
   */
  public static final String MASKED_VALUE = GrouperConfigHibernate.ESCAPED_PASSWORD;

  /**
   * no instances
   */
  private GshTemplateInputMask() {
  }

  /**
   * see if an input of this template is configured as a password form element
   * @param templateConfig template config (populated), may be null
   * @param inputName name of the input, e.g. gsh_input_newPassword
   * @return true if the value of that input must not be logged or audited
   */
  public static boolean isPasswordInput(GshTemplateConfig templateConfig, String inputName) {
    if (templateConfig == null || inputName == null) {
      return false;
    }
    if (templateConfig.getGshTemplateInputConfigs() == null) {
      return false;
    }
    for (GshTemplateInputConfig gshTemplateInputConfig : templateConfig.getGshTemplateInputConfigs()) {
      if (gshTemplateInputConfig == null) {
        continue;
      }
      // input names are matched exactly, same as GshTemplateValidationService matches them
      if (inputName.equals(gshTemplateInputConfig.getName())) {
        return gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.PASSWORD;
      }
    }
    return false;
  }

  /**
   * value of an input as it may be written to the audit or a log
   * @param templateConfig template config (populated), may be null
   * @param inputName name of the input
   * @param value value the user passed in
   * @return the value, or the masked value if the input is a password input and the value is not empty
   */
  public static String valueForLog(GshTemplateConfig templateConfig, String inputName, String value) {
    // an empty value tells nothing about the secret, and shows the input was not filled in
    if (value == null || value.length() == 0) {
      return value;
    }
    if (isPasswordInput(templateConfig, inputName)) {
      return MASKED_VALUE;
    }
    return value;
  }

  /**
   * copy of the showEl variable map with the values of password inputs masked, for exception text.
   * The map passed in is not changed (the template and the later showEl scripts still use it).
   * @param templateConfig template config (populated), may be null
   * @param variableMap map of variable name to value
   * @return the copy, or the same object if null
   */
  public static Map<String, Object> variableMapForLog(GshTemplateConfig templateConfig, Map<String, Object> variableMap) {
    if (variableMap == null) {
      return null;
    }
    Map<String, Object> result = new HashMap<String, Object>(variableMap);
    for (String key : variableMap.keySet()) {
      Object value = variableMap.get(key);
      if (value == null) {
        continue;
      }
      if (isPasswordInput(templateConfig, key)) {
        result.put(key, MASKED_VALUE);
      }
    }
    return result;
  }
}
