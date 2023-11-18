/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;

import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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

  public void testRunIfNotRunRecently() {
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");
    
    GrouperDaemonSchedulerCheck.runDaemonSchedulerCheckNowIfHasntRunRecently();

    int count = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");

    assertEquals(1, count);

    GrouperDaemonSchedulerCheck.runDaemonSchedulerCheckNowIfHasntRunRecently();

    count = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");

    assertEquals(1, count);

    // lets make it 20 minutes ago, still wont run
    
    count = HibernateSession.bySqlStatic().executeSql(
        "update grouper_loader_log set started_time = ?, ended_time = ? where job_name = 'OTHER_JOB_schedulerCheckDaemon'",
        GrouperUtil.toListObject(new Timestamp(System.currentTimeMillis() - 1000*60*20), new Timestamp(System.currentTimeMillis() - 1000*60*20)),
        HibUtils.listType(TimestampType.INSTANCE, TimestampType.INSTANCE));
    
    assertEquals(1, count);

    GrouperDaemonSchedulerCheck.runDaemonSchedulerCheckNowIfHasntRunRecently();

    count = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");

    assertEquals(1, count);

    // lets make it not a success, and recent, still shouldnt run

    count = HibernateSession.bySqlStatic().executeSql(
        "update grouper_loader_log set status = 'SUCCESS2', started_time = ?, ended_time = ? where job_name = 'OTHER_JOB_schedulerCheckDaemon'",
        GrouperUtil.toListObject(new Timestamp(System.currentTimeMillis() - 1000*60*10), new Timestamp(System.currentTimeMillis() - 1000*60*10)),
        HibUtils.listType(TimestampType.INSTANCE, TimestampType.INSTANCE));
    
    assertEquals(1, count);

    GrouperDaemonSchedulerCheck.runDaemonSchedulerCheckNowIfHasntRunRecently();

    count = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");

    assertEquals(1, count);

    // lets make it 20 minutes ago, will run
    
    count = HibernateSession.bySqlStatic().executeSql(
        "update grouper_loader_log set started_time = ?, ended_time = ? where job_name = 'OTHER_JOB_schedulerCheckDaemon'",
        GrouperUtil.toListObject(new Timestamp(System.currentTimeMillis() - 1000*60*20), new Timestamp(System.currentTimeMillis() - 1000*60*20)),
        HibUtils.listType(TimestampType.INSTANCE, TimestampType.INSTANCE));
    
    assertEquals(1, count);

    GrouperDaemonSchedulerCheck.runDaemonSchedulerCheckNowIfHasntRunRecently();

    count = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");

    assertEquals(2, count);

    // delete the non success
    
    count = HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon' and status = 'SUCCESS2'");
    assertEquals(1, count);

    // make the success 40 minutes ago, should run
    count = HibernateSession.bySqlStatic().executeSql(
        "update grouper_loader_log set started_time = ?, ended_time = ? where job_name = 'OTHER_JOB_schedulerCheckDaemon'",
        GrouperUtil.toListObject(new Timestamp(System.currentTimeMillis() - 1000*60*40), new Timestamp(System.currentTimeMillis() - 1000*60*40)),
        HibUtils.listType(TimestampType.INSTANCE, TimestampType.INSTANCE));
    
    assertEquals(1, count);

    GrouperDaemonSchedulerCheck.runDaemonSchedulerCheckNowIfHasntRunRecently();

    count = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_schedulerCheckDaemon'");

    assertEquals(2, count);
    
  }
  
  /**
   * 
   */
  public void testJob() {
    GrouperDaemonSchedulerCheck.runDaemonStandalone();
  }
}
