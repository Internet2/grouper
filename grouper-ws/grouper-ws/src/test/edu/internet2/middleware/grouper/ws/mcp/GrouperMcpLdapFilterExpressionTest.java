package edu.internet2.middleware.grouper.ws.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * The MCP ldap tool must treat its filter argument as an LDAP filter only.  The LDAP session
 * evaluates a filter that starts with ${ as an unrestricted Grouper expression, so the tool
 * refuses that syntax before it looks at the external system or opens a connection.
 * Security fix (GRP-7583).
 */
public class GrouperMcpLdapFilterExpressionTest extends GrouperTest {

  /** group the expression payloads would create if they ran */
  private static final String PROOF_GROUP = "test:mcpLdapFilterExpressionTest:proof";

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpLdapFilterExpressionTest("testExpressionFilterRefused"));
  }

  /**
   * @param name
   */
  public GrouperMcpLdapFilterExpressionTest(String name) {
    super(name);
  }

  /**
   * build the tool arguments for a filter action
   * @param filter
   * @return the arguments
   */
  private static ObjectNode filterArguments(String filter) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "filter");
    arguments.put("externalSystemId", "personLdap");
    arguments.put("baseDn", "dc=example,dc=edu");
    arguments.put("filter", filter);
    return arguments;
  }

  /**
   * expression payloads (plain, leading whitespace, multiline, no closing brace) are refused
   * with an error result, and the payload has no side effect.  The caller is null here: the
   * refusal must come before the caller / external system checks and any LDAP connection
   */
  public void testExpressionFilterRefused() {

    String gsh = "GrouperSession.startRootSession(); new GroupSave().assignName(\\\"" + PROOF_GROUP
        + "\\\").assignCreateParentStemsIfNotExist(true).save();";
    String[] payloads = new String[] {
        "${var proof = grouperUtil.gshRunScript(\"" + gsh + "\"); \"(objectClass=*)\"}",
        "   ${grouperUtil.gshRunScript(\"" + gsh + "\"); \"(objectClass=*)\"}  ",
        "\n\t${var x = 1;\n grouperUtil.gshRunScript(\"" + gsh + "\");\n \"(objectClass=*)\"}",
        "${java.lang.System.getProperty('user.home')",
    };

    for (String payload : payloads) {
      ObjectNode result = GrouperMcpLdapSearch.execute(filterArguments(payload), null);
      assertTrue("refused: " + result, result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("expression message: " + text, text.contains("Expression syntax"));
    }

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      assertNull("payload must not have run", GroupFinder.findByName(grouperSession, PROOF_GROUP, false));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the check matches what LdaptiveSessionImpl would evaluate (trimmed, starts with ${) and
   * leaves real LDAP filters alone, including ones with a $ or braces inside
   */
  public void testFilterIsExpression() {
    assertTrue(GrouperMcpLdapSearch.filterIsExpression("${x}"));
    assertTrue(GrouperMcpLdapSearch.filterIsExpression("  ${x}"));
    assertTrue(GrouperMcpLdapSearch.filterIsExpression("\r\n${x}\n"));
    assertTrue(GrouperMcpLdapSearch.filterIsExpression("${x"));

    assertFalse(GrouperMcpLdapSearch.filterIsExpression(null));
    assertFalse(GrouperMcpLdapSearch.filterIsExpression(""));
    assertFalse(GrouperMcpLdapSearch.filterIsExpression("(uid=jsmith)"));
    assertFalse(GrouperMcpLdapSearch.filterIsExpression("(&(objectClass=person)(sn=Smith))"));
    assertFalse(GrouperMcpLdapSearch.filterIsExpression("(cn=price${x})"));
    assertFalse(GrouperMcpLdapSearch.filterIsExpression("$(uid=a)"));
  }

  /**
   * a plain filter is not refused as an expression (it goes on to the normal checks, which
   * fail here for other reasons: no caller and no configured external system)
   */
  public void testPlainFilterNotRefusedAsExpression() {
    ObjectNode result = GrouperMcpLdapSearch.execute(filterArguments("(uid=jsmith)"), null);
    String text = result.get("content").get(0).get("text").asText();
    assertFalse("not the expression refusal: " + text, text.contains("Expression syntax"));
  }
}
