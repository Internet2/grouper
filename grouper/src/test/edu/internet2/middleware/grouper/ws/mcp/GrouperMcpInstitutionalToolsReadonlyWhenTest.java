/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputType;

/**
 * which calls of a readwrite template match its mcpReadonlyWhen, so the AI agent in the UI runs
 * them without asking.  a wrong match runs a change without approval, so most of these check that
 * something does not match.  no database needed
 */
public class GrouperMcpInstitutionalToolsReadonlyWhenTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperMcpInstitutionalToolsReadonlyWhenTest.class);
  }

  /**
   * @param name
   */
  public GrouperMcpInstitutionalToolsReadonlyWhenTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the pairs a template with a get action and a confirm step would set */
  private static final String READONLY_WHEN = "gsh_input_action=get, gsh_input_confirm=false";

  /** input types, like a template with an action dropdown, a confirm flag, a count and a file */
  private Map<String, GshTemplateInputType> inputTypes;

  /** input defaults: confirm defaults to false */
  private Map<String, String> inputDefaults;

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
  }

  /**
   * @param json the call's inputs argument
   * @return whether the call matches READONLY_WHEN
   */
  private boolean readonly(String json) {
    return readonly(READONLY_WHEN, json);
  }

  /**
   * @param readonlyWhen the template's mcpReadonlyWhen
   * @param json the call's inputs argument, or null for none
   * @return whether the call matches
   */
  private boolean readonly(String readonlyWhen, String json) {
    try {
      JsonNode inputsNode = json == null ? null : objectMapper.readTree(json);
      return GrouperMcpInstitutionalTools.isReadonlyCall(readonlyWhen, this.inputTypes,
          this.inputDefaults, inputsNode);
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * a get, and a plan with confirm left out, set false, or blank, are reads.  confirm=true is not
   */
  public void testGetAndPlanAreReads() {
    assertTrue(readonly("{\"gsh_input_action\":\"get\",\"gsh_input_confirm\":\"true\"}"));
    assertTrue(readonly("{\"action\":\"create\"}"));
    assertTrue(readonly("{\"action\":\"create\",\"confirm\":\"false\"}"));
    assertTrue(readonly("{\"action\":\"create\",\"confirm\":false}"));
    assertTrue(readonly("{\"action\":\"create\",\"confirm\":\"\"}"));

    assertFalse(readonly("{\"action\":\"create\",\"confirm\":\"true\",\"confirmToken\":\"abc\"}"));
    assertFalse(readonly("{\"action\":\"delete\",\"confirm\":true}"));
  }

  /**
   * a boolean is compared as a boolean, the way the template reads it, so f and no are false and
   * t and yes are true
   */
  public void testBooleanComparedAsBoolean() {
    assertTrue(readonly("{\"action\":\"create\",\"confirm\":\"f\"}"));
    assertTrue(readonly("{\"action\":\"create\",\"confirm\":\"NO\"}"));

    assertFalse(readonly("{\"action\":\"create\",\"confirm\":\"t\"}"));
    assertFalse(readonly("{\"action\":\"create\",\"confirm\":\"yes\"}"));
  }

  /**
   * a value which does not convert, a JSON null, and a string which differs only in case or
   * spaces, do not match
   */
  public void testOddValuesDoNotMatch() {
    assertFalse(readonly("{\"action\":\"create\",\"confirm\":\"maybe\"}"));
    assertFalse(readonly("{\"action\":\"create\",\"confirm\":null}"));
    assertFalse(readonly("{\"action\":\"GET\",\"confirm\":\"true\"}"));
    assertFalse(readonly("{\"action\":\" get \",\"confirm\":\"true\"}"));
  }

  /**
   * an input given both with and without the prefix makes the call a write, whatever the values,
   * since which one the template gets depends on order
   */
  public void testInputGivenTwiceIsWrite() {
    assertFalse(readonly("{\"action\":\"get\",\"gsh_input_action\":\"get\"}"));
    assertFalse(readonly("{\"confirm\":\"false\",\"gsh_input_confirm\":\"true\"}"));
  }

  /**
   * a required input, which has no default, left out does not match
   */
  public void testNoDefaultLeftOutDoesNotMatch() {
    this.inputDefaults.clear();
    assertFalse(readonly("{\"action\":\"create\"}"));
    assertTrue(readonly("{\"action\":\"create\",\"confirm\":\"false\"}"));
  }

  /**
   * pairs naming an input the template does not have, a file input, a blank value, or with no
   * equals sign, never match.  a blank setting and no inputs match nothing
   */
  public void testUnreadablePairsDoNotMatch() {
    assertFalse(readonly("gsh_input_other=x", "{\"other\":\"x\"}"));
    assertFalse(readonly("gsh_input_upload=x", "{\"upload\":\"x\"}"));
    assertFalse(readonly("gsh_input_action=", "{\"action\":\"\"}"));
    assertFalse(readonly("gsh_input_action", "{\"action\":\"get\",\"confirm\":\"true\"}"));
    assertFalse(readonly("", "{\"action\":\"get\"}"));
    assertFalse(readonly("gsh_input_action=get", null));
  }

  /**
   * a pair may leave out the gsh_input_ prefix, and integers are compared as integers
   */
  public void testShortNamesAndIntegers() {
    assertTrue(readonly("action=get", "{\"gsh_input_action\":\"get\"}"));
    assertTrue(readonly("count=7", "{\"count\":\"07\"}"));
    assertFalse(readonly("count=7", "{\"count\":\"8\"}"));
  }

}
