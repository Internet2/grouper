/*
 * @author mchyzer
 * $Id: GrouperReportTest.java,v 1.2 2008-12-11 16:28:11 mchyzer Exp $
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
    @SuppressWarnings("unused")
    String report = GrouperReport.report(false, false);
    report = GrouperReport.report(true, true);
  }
  
}
