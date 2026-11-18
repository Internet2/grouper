/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.regex.Pattern;

/**
 * <pre>
 * Checks the names of custom UI variables before they are used.
 *
 * Custom UI variables come from two places: the custom UI config (variableToAssign, checked by
 * CustomUiEngine) and request parameters whose names start with cu_ (UiV2CustomUi, for members of
 * groupCanAssignVariables and for url parameters). CustomUiContainer.gshRunScript() writes each
 * variable name into the Groovy source it runs as root, e.g.
 *
 *   String cu_something = grouperGroovyRuntime.retrieveInputValueString("cu_something");
 *
 * so a name is code, not data. A request parameter name is chosen by the caller, and a name like
 * "cu_x; someCode(); //" would inject Groovy that runs in a root session.
 *
 * This class holds the one allowed grammar for each place:
 * - request parameter names must match ^cu_[A-Za-z0-9_]+$ (UiV2CustomUi refuses others)
 * - names written into Groovy source must be plain identifiers ^[A-Za-z_][A-Za-z0-9_]*$
 *   (checked again at the sink, so a future caller cannot reach the source builder with an
 *   unchecked name)
 *
 * It only uses the JDK so the same file works in every Grouper release that has custom UI.
 * </pre>
 */
public class CustomUiVariableName {

  /**
   * no instances
   */
  private CustomUiVariableName() {
  }

  /** prefix of request parameters that are custom UI variables */
  public static final String URL_PARAMETER_PREFIX = "cu_";

  /** allowed request parameter names for custom UI variables */
  public static final Pattern URL_PARAMETER_NAME_PATTERN = Pattern.compile("^cu_[A-Za-z0-9_]+$");

  /** allowed variable names that are written into Groovy source (plain Java/Groovy identifier) */
  public static final Pattern SCRIPT_VARIABLE_NAME_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

  // -------------------------------------------------------------------------------------------
  // request parameter names (UiV2CustomUi)
  // -------------------------------------------------------------------------------------------

  /**
   * see if a request parameter is a custom UI variable.  Parameters that do not start with cu_
   * are not custom UI variables (false).  Parameters that start with cu_ must match
   * ^cu_[A-Za-z0-9_]+$, else this throws, so a tampered request fails loudly instead of being
   * silently ignored (a tester with a typo in a url also sees why the variable did not apply).
   * @param parameterName request parameter name
   * @return true if this is a valid custom UI variable parameter, false if not a cu_ parameter
   * @throws RuntimeException if the name starts with cu_ but is not a valid variable name
   */
  public static boolean isUrlParameterVariableName(String parameterName) {
    if (parameterName == null || !parameterName.startsWith(URL_PARAMETER_PREFIX)) {
      return false;
    }
    if (!URL_PARAMETER_NAME_PATTERN.matcher(parameterName).matches()) {
      throw new RuntimeException("Invalid custom UI variable parameter name, it must match "
          + URL_PARAMETER_NAME_PATTERN.pattern() + ": '" + safeForMessage(parameterName) + "'");
    }
    return true;
  }

  // -------------------------------------------------------------------------------------------
  // names written into Groovy source (CustomUiContainer.gshRunScript)
  // -------------------------------------------------------------------------------------------

  /**
   * see if a name can be written into Groovy source as a variable name
   * @param variableName
   * @return true if a plain identifier
   */
  public static boolean isValidScriptVariableName(String variableName) {
    return variableName != null && SCRIPT_VARIABLE_NAME_PATTERN.matcher(variableName).matches();
  }

  /**
   * throw if a name cannot be written into Groovy source as a variable name
   * @param variableName
   * @throws RuntimeException if not a plain identifier
   */
  public static void assertValidScriptVariableName(String variableName) {
    if (!isValidScriptVariableName(variableName)) {
      throw new RuntimeException("Invalid custom UI variable name, it must match "
          + SCRIPT_VARIABLE_NAME_PATTERN.pattern() + ": '" + safeForMessage(variableName) + "'");
    }
  }

  /**
   * append the Groovy declaration of one custom UI variable to the script, after checking the
   * name.  The declarations are the same ones CustomUiContainer.gshRunScript() wrote before
   * this check was added; values of other types get no declaration (as before).
   * @param script the script being built
   * @param key variable name
   * @param value variable value (Number, Boolean, String, null, or something else)
   * @throws RuntimeException if the name is not a plain identifier (nothing is appended)
   */
  public static void appendScriptVariableDeclaration(StringBuilder script, String key, Object value) {

    // check first: the key is written into source both as an identifier and in a string literal
    assertValidScriptVariableName(key);

    if (value instanceof Number) {
      script.append("Number " + key + " = (Number)grouperGroovyRuntime.retrieveInputValueObject(\"" + key + "\");\n");
    } else if (value instanceof Boolean) {
      script.append("boolean " + key + " = grouperGroovyRuntime.retrieveInputValueBoolean(\"" + key + "\");\n");
    } else if (value instanceof String) {
      script.append("String " + key + " = grouperGroovyRuntime.retrieveInputValueString(\"" + key + "\");\n");
    } else if (value == null) {
      script.append("Object " + key + " = null;\n");
    }
  }

  /**
   * make a caller-supplied name safe to put in an exception message (logs, error screens):
   * keep letters, digits and underscore, replace anything else with '?', and cut it short
   * @param name
   * @return the safe version
   */
  static String safeForMessage(String name) {
    if (name == null) {
      return "null";
    }
    StringBuilder result = new StringBuilder();
    int max = Math.min(name.length(), 100);
    for (int i = 0; i < max; i++) {
      char c = name.charAt(i);
      if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
        result.append(c);
      } else {
        result.append('?');
      }
    }
    if (name.length() > max) {
      result.append("...");
    }
    return result.toString();
  }
}
