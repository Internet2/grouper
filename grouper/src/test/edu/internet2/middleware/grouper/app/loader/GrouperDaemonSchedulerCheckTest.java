/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 *
 */
public class GrouperDaemonSchedulerCheckTest extends GrouperTest {

  /**
   * 
   */
  public GrouperDaemonSchedulerCheckTest() {
  }

  /**
   * @param name
   */
  public GrouperDaemonSchedulerCheckTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testJob() {
    GrouperDaemonSchedulerCheck.runDaemonStandalone();
  }
}
