package edu.internet2.middleware.grouper.app.serviceLifecycle;

import java.math.BigDecimal;
import java.sql.Timestamp;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class GrouperTimeDaemonTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTimeDaemonTest("testRun"));
  }

  public GrouperTimeDaemonTest(String name) {
    super(name);
  }

  public void testRun() {
    
    // do an insert
    new GcDbAccess().sql("delete from grouper_time");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_timeDaemon");

    Object[] row = new GcDbAccess()
        .sql("select time_label, the_utc_timestamp, this_tz_timestamp, utc_millis_since_1970, utc_micros_since_1970 from grouper_time")
        .select(Object[].class);
    
    assertEquals("now", row[0]);
    assertNotNull(row[1]);
    assertNotNull(row[2]);
    assertNotNull(row[3]);
    assertNotNull(row[4]);
    
    GrouperUtil.sleep(1000);

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_timeDaemon");

    Object[] row2 = new GcDbAccess()
        .sql("select time_label, the_utc_timestamp, this_tz_timestamp, utc_millis_since_1970, utc_micros_since_1970 from grouper_time")
        .select(Object[].class);
    
    assertEquals("now", row[0]);
    assertTrue(((Timestamp)row2[1]).getTime() > ((Timestamp)row[1]).getTime());
    assertTrue(((Timestamp)row2[2]).getTime() > ((Timestamp)row[2]).getTime());
    assertTrue(((BigDecimal)row2[3]).doubleValue() > ((BigDecimal)row[3]).doubleValue());
    assertTrue(((BigDecimal)row2[4]).doubleValue() > ((BigDecimal)row[4]).doubleValue());
    
  }

}
