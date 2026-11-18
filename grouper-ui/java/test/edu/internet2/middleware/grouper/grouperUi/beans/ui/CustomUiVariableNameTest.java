package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiEngine;
import junit.textui.TestRunner;

/**
 * <pre>
 * Custom UI variables from request parameters named cu_* were copied into the variable map with
 * any name, and CustomUiContainer.gshRunScript() wrote each name into Groovy source that runs as
 * root.  A member of groupCanAssignVariables could send a parameter NAME such as
 *   cu_proof; java.lang.System.setProperty('...','executed'); //
 * and run that code as root.
 *
 * This test checks both ends without a full custom UI config:
 * - CustomUiVariableName.isUrlParameterVariableName (what UiV2CustomUi now calls for each request
 *   parameter) accepts cu_someName, ignores non cu_ parameters, and refuses hostile cu_ names
 * - CustomUiVariableName.appendScriptVariableDeclaration (the source builder gshRunScript now uses)
 *   writes the same declarations as before for valid names and nothing for hostile names
 * - CustomUiContainer.gshRunScript() itself fails closed on a hostile key (the injected code does
 *   not run), and still runs a script that reads valid String, boolean and Number variables
 * </pre>
 */
public class CustomUiVariableNameTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CustomUiVariableNameTest("testGshRunScriptRefusesHostileKey"));
  }

  /**
   *
   */
  public CustomUiVariableNameTest() {
    super();
  }

  /**
   * @param name
   */
  public CustomUiVariableNameTest(String name) {
    super(name);
  }

  /** system property the hostile name would set if its code ran */
  private static final String PROOF_PROPERTY = "grouper.customUiProof142";

  /** system property the valid script sets */
  private static final String VALID_PROPERTY = "grouper.customUiValid142";

  /** the hostile parameter name from the report (the name, not the value) */
  private static final String HOSTILE_NAME = "cu_proof; java.lang.System.setProperty('" + PROOF_PROPERTY + "','executed'); //";

  /** other hostile names: whitespace, quotes, newline, brackets, comment, non-ASCII, empty suffix */
  private static final String[] OTHER_HOSTILE_NAMES = new String[] {
    "cu_a b", "cu_a\"b", "cu_a'b", "cu_a\nprintln 'x'", "cu_a(b)", "cu_a[0]", "cu_a/*", "cu_a//",
    "cu_a.b", "cu_a-b", "cu_a=b", "cu_a;", "cu_\u00e9", "cu_a\u0000", "cu_"
  };

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    System.clearProperty(PROOF_PROPERTY);
    System.clearProperty(VALID_PROPERTY);
    super.tearDown();
  }

  /**
   * request parameter names: valid cu_ names are variables, non cu_ names are not, hostile cu_
   * names are refused
   */
  public void testUrlParameterVariableName() {

    assertTrue(CustomUiVariableName.isUrlParameterVariableName("cu_someName"));
    assertTrue(CustomUiVariableName.isUrlParameterVariableName("cu_some_Name_2"));
    assertTrue(CustomUiVariableName.isUrlParameterVariableName("cu_9"));

    // not custom UI variables at all: ignored as before
    assertFalse(CustomUiVariableName.isUrlParameterVariableName(null));
    assertFalse(CustomUiVariableName.isUrlParameterVariableName("groupId"));
    assertFalse(CustomUiVariableName.isUrlParameterVariableName("CU_someName"));
    assertFalse(CustomUiVariableName.isUrlParameterVariableName("xcu_someName; foo()"));

    assertUrlNameRefused(HOSTILE_NAME);
    for (String hostileName : OTHER_HOSTILE_NAMES) {
      assertUrlNameRefused(hostileName);
    }
  }

  /**
   * @param name
   */
  private static void assertUrlNameRefused(String name) {
    try {
      CustomUiVariableName.isUrlParameterVariableName(name);
      fail("Expected refusal of url parameter name: " + name);
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("Invalid custom UI variable parameter name"));
      // the caller's text does not reach the message as-is (logs / error screens)
      assertFalse(re.getMessage(), re.getMessage().contains(";"));
      assertFalse(re.getMessage(), re.getMessage().contains("\n"));
    }
  }

  /**
   * names written into the Groovy source: identifiers only
   */
  public void testScriptVariableName() {
    assertTrue(CustomUiVariableName.isValidScriptVariableName("cu_someName"));
    assertTrue(CustomUiVariableName.isValidScriptVariableName("someName"));
    assertTrue(CustomUiVariableName.isValidScriptVariableName("_x1"));
    assertFalse(CustomUiVariableName.isValidScriptVariableName(null));
    assertFalse(CustomUiVariableName.isValidScriptVariableName(""));
    assertFalse(CustomUiVariableName.isValidScriptVariableName("1x"));
    assertFalse(CustomUiVariableName.isValidScriptVariableName(HOSTILE_NAME));
    for (String hostileName : OTHER_HOSTILE_NAMES) {
      if (!"cu_".equals(hostileName)) {
        assertFalse(hostileName, CustomUiVariableName.isValidScriptVariableName(hostileName));
      }
    }
  }

  /**
   * the source builder writes the same declarations as before for valid names, and nothing for
   * a hostile name
   */
  public void testAppendScriptVariableDeclaration() {

    StringBuilder script = new StringBuilder();
    CustomUiVariableName.appendScriptVariableDeclaration(script, "cu_str", "x");
    CustomUiVariableName.appendScriptVariableDeclaration(script, "cu_bool", Boolean.TRUE);
    CustomUiVariableName.appendScriptVariableDeclaration(script, "cu_num", Long.valueOf(5));
    CustomUiVariableName.appendScriptVariableDeclaration(script, "cu_null", null);
    CustomUiVariableName.appendScriptVariableDeclaration(script, "request", new Object());

    assertEquals(
        "String cu_str = grouperGroovyRuntime.retrieveInputValueString(\"cu_str\");\n"
        + "boolean cu_bool = grouperGroovyRuntime.retrieveInputValueBoolean(\"cu_bool\");\n"
        + "Number cu_num = (Number)grouperGroovyRuntime.retrieveInputValueObject(\"cu_num\");\n"
        + "Object cu_null = null;\n",
        script.toString());

    StringBuilder hostileScript = new StringBuilder();
    try {
      CustomUiVariableName.appendScriptVariableDeclaration(hostileScript, HOSTILE_NAME, "x");
      fail("Expected refusal of hostile key");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("Invalid custom UI variable name"));
    }
    assertEquals("nothing is appended for a hostile key", "", hostileScript.toString());
  }

  /**
   * a container whose overrideMap does not need a UI request (the real one reads the servlet
   * request), with the given user query variables
   * @param variables
   * @return the container
   */
  private static CustomUiContainer containerWithVariables(Map<String, Object> variables) {
    CustomUiContainer customUiContainer = new CustomUiContainer() {

      @Override
      public Map<String, Object> overrideMap() {
        return new HashMap<String, Object>();
      }
    };
    CustomUiEngine customUiEngine = new CustomUiEngine();
    customUiEngine.userQueryVariables().putAll(variables);
    customUiContainer.setCustomUiEngine(customUiEngine);
    return customUiContainer;
  }

  /**
   * gshRunScript fails closed on a hostile key: the injected code does not run as root
   */
  public void testGshRunScriptRefusesHostileKey() {

    System.clearProperty(PROOF_PROPERTY);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("cu_ok", "fine");
    variables.put(HOSTILE_NAME, "x");
    CustomUiContainer customUiContainer = containerWithVariables(variables);

    try {
      customUiContainer.gshRunScript(null, null, null, "int harmless = 1;\n");
      fail("Expected gshRunScript to refuse the hostile variable name");
    } catch (RuntimeException re) {
      assertTrue("Expected the variable name check, got: " + re.getMessage(),
          re.getMessage() != null && re.getMessage().contains("Invalid custom UI variable name"));
    }

    assertNull("The injected code ran", System.getProperty(PROOF_PROPERTY));
  }

  /**
   * gshRunScript still runs a script that reads valid String, boolean and Number variables
   */
  public void testGshRunScriptValidNamesStillWork() {

    System.clearProperty(VALID_PROPERTY);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("cu_someName", "hello");
    variables.put("cu_someFlag", Boolean.TRUE);
    variables.put("cu_someCount", Long.valueOf(5));
    CustomUiContainer customUiContainer = containerWithVariables(variables);

    customUiContainer.gshRunScript(null, null, null,
        "System.setProperty(\"" + VALID_PROPERTY + "\", cu_someName + \",\" + cu_someFlag + \",\" + cu_someCount);\n");

    assertEquals("hello,true,5", System.getProperty(VALID_PROPERTY));
  }
}
