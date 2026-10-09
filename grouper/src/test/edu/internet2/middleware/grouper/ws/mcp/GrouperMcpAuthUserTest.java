/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.mcp;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.mcp.GrouperToolEntryPath;

/**
 * when the consent scope limits a caller.  no database needed
 */
public class GrouperMcpAuthUserTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperMcpAuthUserTest.class);
  }

  /**
   * @param name
   */
  public GrouperMcpAuthUserTest(String name) {
    super(name);
  }

  /**
   * an MCP caller with web service credentials is limited by group membership only, an OAuth
   * caller by what they consented to, and the UI agent by the scope chosen for the session
   */
  public void testConsentScopeEnforced() {

    GrouperMcpAuthUser wsAuthnMcpUser = new GrouperMcpAuthUser(null);
    assertEquals(GrouperToolEntryPath.mcp, wsAuthnMcpUser.getEntryPath());
    assertFalse(wsAuthnMcpUser.isConsentScopeEnforced());

    GrouperMcpAuthUser oauthMcpUser = new GrouperMcpAuthUser(null);
    oauthMcpUser.setOAuthAuthenticated(true);
    assertTrue(oauthMcpUser.isConsentScopeEnforced());

    GrouperMcpAuthUser uiUser = new GrouperMcpAuthUser(null);
    uiUser.setEntryPath(GrouperToolEntryPath.ui);
    assertFalse(uiUser.isOAuthAuthenticated());
    assertTrue(uiUser.isConsentScopeEnforced());
  }

  /**
   * an unset entry path must not read as the UI, or an MCP caller would be judged by a session
   * scope nobody set
   */
  public void testEntryPathDefaultsToMcp() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(null);
    authUser.setEntryPath(null);
    assertEquals(GrouperToolEntryPath.mcp, authUser.getEntryPath());
  }

}
