/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.internet2.middleware.grouper.mcp.GrouperToolConfirmationSummariesTest;
import edu.internet2.middleware.grouper.mcp.GrouperToolExecutorPageSizeTest;
import edu.internet2.middleware.grouper.mcp.GrouperToolResultBoundTest;
import edu.internet2.middleware.grouper.ws.GrouperWsRequestContextTest;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUserTest;

/**
 * tests for the AI agent in the Grouper UI and the parts of the tool layer it added: the agent loop,
 * the provider clients, confirmation summaries, the row cap on list tools, the per request subject
 * resolver and the UI scope.  none of them need a database.  the usage and call log tables are
 * tested against the database in GrouperAiAgentUsageDbTest, which is in AllTests.  the MCP tool
 * tests that need grouper-ws are in AllMcpTests there
 */
public class AllAiAgentTests {

  /**
   * suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for the AI agent in the UI and the shared tool layer");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperAiAgentTest.class);
    suite.addTestSuite(GrouperAiAnthropicClientTest.class);
    suite.addTestSuite(GrouperAiOpenAiClientTest.class);
    suite.addTestSuite(GrouperToolConfirmationSummariesTest.class);
    suite.addTestSuite(GrouperToolExecutorPageSizeTest.class);
    suite.addTestSuite(GrouperToolResultBoundTest.class);
    suite.addTestSuite(GrouperWsRequestContextTest.class);
    suite.addTestSuite(GrouperMcpAuthUserTest.class);
    //$JUnit-END$
    return suite;
  }

}
