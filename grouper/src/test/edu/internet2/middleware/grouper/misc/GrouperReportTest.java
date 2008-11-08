/*
 * @author mchyzer
 * $Id: GrouperReportTest.java,v 1.1 2008-11-08 03:42:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.grouper.GrouperTest;


/**
 *
 */
public class GrouperReportTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperReportTest(String name) {
    super(name);
  }

  /**
   * test the report
   */
  public void testReport() {
    GrouperReport.report(false, false);
    GrouperReport.report(true, true);
  }
  
}
