/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.ws.mcp;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * test suite for MCP tool tests
 *
 * @author mchyzer
 */
public class AllMcpTests {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllMcpTests.suite());
  }

  /**
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.ws.mcp");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperMcpGetSubjectsTest.class);
    suite.addTestSuite(GrouperMcpHasMemberTest.class);
    suite.addTestSuite(GrouperMcpGetMembersLiteTest.class);
    suite.addTestSuite(GrouperMcpScopeTest.class);
    suite.addTestSuite(GrouperMcpFindGroupsTest.class);
    suite.addTestSuite(GrouperMcpFindStemsTest.class);
    suite.addTestSuite(GrouperMcpAdminSearchConfigsTest.class);
    suite.addTestSuite(GrouperMcpGetAuditEntriesTest.class);
    suite.addTestSuite(GrouperMcpSqlSelectTest.class);
    suite.addTestSuite(GrouperMcpSqlGetSchemaTest.class);
    //$JUnit-END$
    return suite;
  }
}
