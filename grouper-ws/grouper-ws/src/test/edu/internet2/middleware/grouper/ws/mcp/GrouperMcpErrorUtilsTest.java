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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpErrorUtils, which decides who sees full stack traces
 * in MCP tool error responses (grouper.mcp.users.canSeeStackTraces).
 * <p>Note the membership answer is cached for 60 seconds keyed on the subject, so each
 * test method uses a different subject to avoid one test seeing another test's answer.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpErrorUtilsTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpErrorUtilsTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpErrorUtilsTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperMcpErrorUtilsTest.class);
  }

  /**
   * the config property which holds the group name
   */
  private static final String CONFIG_KEY = "grouper.mcp.users.canSeeStackTraces";

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * create the group which the config points at
   * @param groupName the name of the group to create
   * @return the group
   */
  private Group createStackTraceGroup(String groupName) {
    return new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit(groupName)
        .assignName(groupName)
        .assignCreateParentStemsIfNotExist(true)
        .save();
  }

  /**
   * a member of the configured group gets the full stack trace appended
   */
  public void testStackTraceForMemberOfGroup() {

    String groupName = "test:mcpStackTraceGroupMember";
    Group group = createStackTraceGroup(groupName);
    group.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY, groupName);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

    RuntimeException runtimeException = new RuntimeException("something went wrong in the tool");

    String stackTrace = GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, runtimeException);

    assertTrue("member of the group should get a stack trace", stackTrace.length() > 0);
    assertTrue("stack trace should be separated from the message by a blank line",
        stackTrace.startsWith("\n\n"));
    assertTrue("stack trace should name the exception class, was: " + stackTrace,
        stackTrace.contains("java.lang.RuntimeException"));
    assertTrue("stack trace should have frames, was: " + stackTrace,
        stackTrace.contains("GrouperMcpErrorUtilsTest"));

    assertTrue(GrouperMcpErrorUtils.canSeeStackTraces(authUser));
  }

  /**
   * someone who is not in the configured group gets no stack trace
   */
  public void testNoStackTraceForNonMember() {

    String groupName = "test:mcpStackTraceGroupNonMember";
    createStackTraceGroup(groupName);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY, groupName);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ1);

    RuntimeException runtimeException = new RuntimeException("something went wrong in the tool");

    assertEquals("", GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, runtimeException));
    assertFalse(GrouperMcpErrorUtils.canSeeStackTraces(authUser));
  }

  /**
   * if the config is blank then nobody gets a stack trace, even a sysadmin
   */
  public void testNoStackTraceWhenConfigBlank() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY, "");

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ2);

    RuntimeException runtimeException = new RuntimeException("something went wrong in the tool");

    assertEquals("", GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, runtimeException));
    assertFalse(GrouperMcpErrorUtils.canSeeStackTraces(authUser));
  }

  /**
   * if the configured group does not exist then nobody gets a stack trace
   */
  public void testNoStackTraceWhenGroupDoesNotExist() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY,
        "test:mcpStackTraceGroupWhichDoesNotExist");

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ3);

    RuntimeException runtimeException = new RuntimeException("something went wrong in the tool");

    assertEquals("", GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, runtimeException));
    assertFalse(GrouperMcpErrorUtils.canSeeStackTraces(authUser));
  }

  /**
   * a null auth user or a null exception never produces a stack trace
   */
  public void testNoStackTraceForNulls() {

    String groupName = "test:mcpStackTraceGroupNulls";
    Group group = createStackTraceGroup(groupName);
    group.addMember(SubjectTestHelper.SUBJ4, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY, groupName);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ4);

    assertEquals("", GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, null));
    assertEquals("", GrouperMcpErrorUtils.stackTraceIfAllowed(null,
        new RuntimeException("something went wrong in the tool")));
    assertEquals("", GrouperMcpErrorUtils.stackTraceIfAllowed(null, null));

    assertFalse(GrouperMcpErrorUtils.canSeeStackTraces(null));
  }

  /**
   * outside of MCP the WS layer keeps its existing behavior, so a subject who is not in the
   * configured group still gets stack traces in WsResultMeta result messages.  this is what
   * keeps the change from tightening plain WS clients.
   */
  public void testAllowsStackTraceToClientOutsideMcp() {

    String groupName = "test:mcpStackTraceGroupOutsideMcp";
    createStackTraceGroup(groupName);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY, groupName);

    // not MCP, this is a plain WS request
    GrouperContext.deleteDefaultContext();
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.WS, false, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ5);
    try {
      assertTrue("WS clients are unaffected by the MCP group",
          GrouperServiceUtils.allowsStackTraceToClient());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * under MCP, whether the WS layer may put a stack trace in the result message follows
   * the same group as the MCP tool errors do
   */
  public void testAllowsStackTraceToClientUnderMcp() {

    String groupName = "test:mcpStackTraceGroupUnderMcp";
    Group group = createStackTraceGroup(groupName);
    group.addMember(SubjectTestHelper.SUBJ6, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(CONFIG_KEY, groupName);

    // setUp already established an MCP context

    GrouperSession memberSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
    try {
      assertTrue("member of the group gets the stack trace",
          GrouperServiceUtils.allowsStackTraceToClient());
    } finally {
      GrouperSession.stopQuietly(memberSession);
    }

    GrouperSession nonMemberSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
    try {
      assertFalse("non member does not get the stack trace",
          GrouperServiceUtils.allowsStackTraceToClient());
    } finally {
      GrouperSession.stopQuietly(nonMemberSession);
    }
  }
}
