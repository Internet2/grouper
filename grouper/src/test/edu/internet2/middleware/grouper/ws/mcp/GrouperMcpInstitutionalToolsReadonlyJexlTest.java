/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputType;

/**
 * the variables a readwrite template's mcpReadonlyJexl is evaluated with, which decide whether the
 * AI agent in the UI runs a call without asking.  a wrong value runs a change without approval, so
 * these check the values are the ones the template gets, and that a call which cannot be read is a
 * write.  the expression itself is evaluated in GrouperMcpInstitutionalToolsTest, which has config.
 * no database needed
 */
public class GrouperMcpInstitutionalToolsReadonlyJexlTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperMcpInstitutionalToolsReadonlyJexlTest.class);
  }

  /**
   * @param name
   */
  public GrouperMcpInstitutionalToolsReadonlyJexlTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** input types, like a template with an action dropdown, a confirm flag, a count and a file */
  private Map<String, GshTemplateInputType> inputTypes;

  /** input defaults: confirm defaults to false */
  private Map<String, String> inputDefaults;

  /** inputs configured not to trim */
  private Set<String> inputsNotTrimmed;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.inputTypes = new HashMap<String, GshTemplateInputType>();
    this.inputTypes.put("gsh_input_action", GshTemplateInputType.STRING);
    this.inputTypes.put("gsh_input_confirm", GshTemplateInputType.BOOLEAN);
    this.inputTypes.put("gsh_input_count", GshTemplateInputType.INTEGER);
    this.inputTypes.put("gsh_input_upload", GshTemplateInputType.FILE);
    this.inputDefaults = new HashMap<String, String>();
    this.inputDefaults.put("gsh_input_confirm", "false");
    this.inputsNotTrimmed = new HashSet<String>();
  }

  /**
   * @param json the call's inputs argument, or null for none
   * @return the variables, or null if the call cannot be read
   */
  private Map<String, Object> variables(String json) {
    try {
      JsonNode inputsNode = json == null ? null : objectMapper.readTree(json);
      return GrouperMcpInstitutionalTools.readonlyJexlVariables(this.inputTypes, this.inputDefaults,
          this.inputsNotTrimmed, inputsNode);
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * every input of the template is a variable by its full name, given with or without the prefix.
   * one the call leaves out takes its default, or is null without one
   */
  public void testEveryInputByFullName() {
    Map<String, Object> variables = variables("{\"action\":\"get\",\"gsh_input_count\":\"7\"}");
    assertEquals(4, variables.size());
    assertEquals("get", variables.get("gsh_input_action"));
    assertEquals(Integer.valueOf(7), variables.get("gsh_input_count"));
    assertEquals(Boolean.FALSE, variables.get("gsh_input_confirm"));
    assertTrue(variables.containsKey("gsh_input_upload"));
    assertNull(variables.get("gsh_input_upload"));

    // no inputs at all
    variables = variables(null);
    assertEquals(4, variables.size());
    assertNull(variables.get("gsh_input_action"));
    assertEquals(Boolean.FALSE, variables.get("gsh_input_confirm"));
  }

  /**
   * a value is converted to the input's type the way the template converts it, so f and no are
   * false and t and yes are true, and a blank value takes the default
   */
  public void testConvertedToType() {
    assertEquals(Boolean.FALSE, variables("{\"confirm\":\"f\"}").get("gsh_input_confirm"));
    assertEquals(Boolean.FALSE, variables("{\"confirm\":\"NO\"}").get("gsh_input_confirm"));
    assertEquals(Boolean.FALSE, variables("{\"confirm\":false}").get("gsh_input_confirm"));
    assertEquals(Boolean.FALSE, variables("{\"confirm\":\"\"}").get("gsh_input_confirm"));
    assertEquals(Boolean.TRUE, variables("{\"confirm\":\"t\"}").get("gsh_input_confirm"));
    assertEquals(Boolean.TRUE, variables("{\"confirm\":\"yes\"}").get("gsh_input_confirm"));
    assertEquals(Boolean.TRUE, variables("{\"confirm\":true}").get("gsh_input_confirm"));
    assertEquals(Integer.valueOf(7), variables("{\"count\":\"07\"}").get("gsh_input_count"));
  }

  /**
   * a value is trimmed the way the template trims it, unless the input turns trimming off.  case
   * is left alone
   */
  public void testTrimmedUnlessTurnedOff() {
    assertEquals("get", variables("{\"action\":\" get \"}").get("gsh_input_action"));
    assertEquals("GET", variables("{\"action\":\"GET\"}").get("gsh_input_action"));

    this.inputsNotTrimmed.add("gsh_input_action");
    assertEquals(" get ", variables("{\"action\":\" get \"}").get("gsh_input_action"));
  }

  /**
   * a value which does not convert, including a JSON null, which reads as the string null, makes
   * the call unreadable, so it is a write.  the template refuses it anyway
   */
  public void testValueWhichDoesNotConvertIsWrite() {
    assertNull(variables("{\"action\":\"get\",\"confirm\":\"maybe\"}"));
    assertNull(variables("{\"action\":\"get\",\"confirm\":null}"));
    assertNull(variables("{\"action\":\"get\",\"count\":\"seven\"}"));
  }

  /**
   * an input given both with and without the prefix makes the call a write, whatever the values,
   * since which one the template gets depends on order
   */
  public void testInputGivenTwiceIsWrite() {
    assertNull(variables("{\"action\":\"get\",\"gsh_input_action\":\"get\"}"));
    assertNull(variables("{\"confirm\":\"false\",\"gsh_input_confirm\":\"true\"}"));
  }

  /**
   * a required input, which has no default, left out is null
   */
  public void testNoDefaultLeftOutIsNull() {
    this.inputDefaults.clear();
    Map<String, Object> variables = variables("{\"action\":\"create\"}");
    assertTrue(variables.containsKey("gsh_input_confirm"));
    assertNull(variables.get("gsh_input_confirm"));
  }

  /**
   * an input the template does not have is not a variable, so an expression naming it is an
   * error, and a file input is null whatever was sent
   */
  public void testUnknownAndFileInputs() {
    Map<String, Object> variables = variables("{\"other\":\"x\",\"upload\":\"x\"}");
    assertFalse(variables.containsKey("gsh_input_other"));
    assertTrue(variables.containsKey("gsh_input_upload"));
    assertNull(variables.get("gsh_input_upload"));
  }

}
