/**
 * Copyright 2014 Internet2
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.misc;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;


public class GrouperFailsafeTest extends GrouperTest {

  /**
   * 
   */
  public GrouperFailsafeTest() {

  }

  /**
   * 
   * @param name
   */
  public GrouperFailsafeTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    TestRunner.run(new GrouperFailsafeTest("testAssignFailedReturnsTrueOnlyOnTransition"));

  }

  /**
   * job name used for this test
   */
  private static final String JOB_NAME = "OTHER_JOB_grouperFailsafeTest";

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    new GcDbAccess().sql("delete from grouper_failsafe where name = ?").addBindVar(JOB_NAME).executeSql();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    new GcDbAccess().sql("delete from grouper_failsafe where name = ?").addBindVar(JOB_NAME).executeSql();
    super.tearDown();
  }

  /**
   * GRP-7071: assignFailed returns true only for the run that transitions the job into the failsafe
   * state, so callers can send one notification per failsafe episode instead of one per run.
   */
  public void testAssignFailedReturnsTrueOnlyOnTransition() {

    // first failsafe issue is a transition into the failsafe state
    assertTrue(GrouperFailsafe.assignFailed(JOB_NAME));
    assertTrue(GrouperFailsafe.isFailsafeIssue(JOB_NAME));

    // the failsafe is still tripped, so subsequent runs are not transitions and must not notify again
    assertFalse(GrouperFailsafe.assignFailed(JOB_NAME));
    assertFalse(GrouperFailsafe.assignFailed(JOB_NAME));

    // approving the failsafe by itself does not end the episode, the job still has not run clean
    GrouperFailsafe.assignApproveNextRun(JOB_NAME);
    assertFalse(GrouperFailsafe.assignFailed(JOB_NAME));

    // a successful run clears the failsafe state, so the next issue is a new episode and notifies
    GrouperFailsafe.assignSuccess(JOB_NAME);
    assertFalse(GrouperFailsafe.isFailsafeIssue(JOB_NAME));
    assertTrue(GrouperFailsafe.assignFailed(JOB_NAME));
    assertFalse(GrouperFailsafe.assignFailed(JOB_NAME));

    // removing the failure also re-arms the notification for the next episode
    GrouperFailsafe.removeFailure(JOB_NAME);
    assertFalse(GrouperFailsafe.isFailsafeIssue(JOB_NAME));
    assertTrue(GrouperFailsafe.assignFailed(JOB_NAME));
  }

}
