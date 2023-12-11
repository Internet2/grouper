/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * schedule quartz daemon
 */
@DisallowConcurrentExecution
public class GrouperDaemonSchedulerCheck extends OtherJobBase {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonSchedulerCheck.class);

  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  private static Thread daemonSchedulerCheckThread = new Thread(new Runnable() {

    @Override
    public void run() {
      
      try {
        
        int rando3_8 = 3 + (int)Math.round((Math.random() * 5.0));

        // sleep a few minutes after daemon starts up
        Thread.sleep(1000 * 60 * rando3_8);

      } catch (InterruptedException ie) {
        return;
      } catch (Exception e) {
        LOG.error("error in scheduler check thread start", e);

        // continue
      }

      while (true) {
        try {

          int maxMinutesSinceSuccess = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.schedulerCheckDaemon.maxMinutesSinceSuccess", 35);
  
          // dont worry about it
          if (maxMinutesSinceSuccess <= 0) {
            return;
          }

          Thread.sleep(1000*60*maxMinutesSinceSuccess+1);
          
          runDaemonSchedulerCheckNowIfHasntRunRecently();
        } catch (InterruptedException ie) {
          return;
        } catch (Exception e) {
          LOG.error("error in scheduler check thread", e);
          // continue
        }
      }
      
      
      
    }
    
  });

  /**
   * if the thread isnt running, and is supposed to run, then start it
   */
  public static void startDaemonSchedulerCheckThreadIfNeeded() {
    int maxMinutesSinceSuccess = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.schedulerCheckDaemon.maxMinutesSinceSuccess", 35);
    
    // dont worry about it
    if (maxMinutesSinceSuccess <= 0) {
      return;
    }
    if (daemonSchedulerCheckThread.isAlive()) {
      return;
    }
    daemonSchedulerCheckThread.setDaemon(true);
    daemonSchedulerCheckThread.start();
  }
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    String jobName = "OTHER_JOB_schedulerCheckDaemon";
     
    GrouperLoader.runOnceByJobName(grouperSession, jobName);

//    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
//    
//    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
//
//    hib3GrouperLoaderLog.setJobName(jobName);
//    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
//    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
//    hib3GrouperLoaderLog.store();
//    
//    OtherJobInput otherJobInput = new OtherJobInput();
//    otherJobInput.setJobName(jobName);
//    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
//    otherJobInput.setGrouperSession(grouperSession);
//    new GrouperDaemonSchedulerCheck().run(otherJobInput);
//    
//    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
//    hib3GrouperLoaderLog.store();
    
    
  }
  
  public static void runDaemonSchedulerCheckNowIfHasntRunRecently() {
    if (!Hib3GrouperLoaderLog.hasRecentDaemonSchedulerCheck()) {
      LOG.error("Scheduler check daemon did not run from quartz!!!!!  running by fallback thread");
      runDaemonStandalone();
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    boolean runNow = GrouperLoader.isJobRunningAsRunNow("OTHER_JOB_schedulerCheckDaemon");
    
    handleBlockedAndAcquiredStates(otherJobInput, runNow);
    handleErrorState(otherJobInput, runNow);
    handleMissingTriggers(otherJobInput, runNow);
    handleJobsWhereJvmDied(otherJobInput);

    LOG.info("GrouperDaemonSchedulerCheck finished successfully.");
    return null;
  }
  
  private void handleJobsWhereJvmDied(OtherJobInput otherJobInput) {

    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("schedulerCheckDaemon.handleJobsWhereJvmDied", true)) {
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping handleJobsWhereJvmDied.  ");
    }

    //  has status of STARTED or RUNNING 
    //  AND
    //    - last_updated is in the last 30 seconds
    //    OR
    //        - theres a fired triggers with job name that matches
    //        OR 
    //         - theres a fired triggers with trigger name that matches and the triggers table has a job name that matches
    //      - AND the triggers instance name matches a scheduler that is alive
    //  then the job is running

    // last checkin in last 50 seconds
    long lastCheckinTime = System.currentTimeMillis() - 50000;
    
    List<Object[]> jobIdsNamesStartedTimes = new GcDbAccess().sql("select id, job_name, gll.started_time from grouper_loader_log gll where status in ('STARTED', 'RUNNING') and last_updated < ?")
        .addBindVar(new Timestamp(System.currentTimeMillis() - (60*1000L))).selectList(Object[].class);

    if (GrouperUtil.length(jobIdsNamesStartedTimes) == 0) {
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("No running jobs last updated more than 1 minutes ago");
      return;
    }

    Set<MultiKey> idsAndJobNamesToFix = new HashSet<MultiKey>();
    Set<String> jobNamesRunning = new HashSet<String>();
    
    int batchSize = 300;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(jobIdsNamesStartedTimes, batchSize, false);
    
    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<Object[]> jobIdsNamesStartedTimesBatch = GrouperUtil.batchList(jobIdsNamesStartedTimes, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select id, gll.job_name from grouper_loader_log gll where ");
      
      GcDbAccess gcDbAccess = new GcDbAccess();
      
      boolean first = true;
      for (Object[] jobIdNameStartedTime : jobIdsNamesStartedTimesBatch) {
        
        if (!first) {
          sql.append(" or ");
        }
        
        sql.append(" ( gll.id != ? and gll.job_name = ? and gll.started_time > ? ) ");
        gcDbAccess.addBindVar(jobIdNameStartedTime[0]).addBindVar(jobIdNameStartedTime[1]).addBindVar(jobIdNameStartedTime[2]);
        
        first = false;
      }

      List<Object[]> idsAndJobNames = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
      for (Object[] idAndJobName : idsAndJobNames) {
        idsAndJobNamesToFix.add(new MultiKey(idAndJobName));
      }

      sql = new StringBuilder(" select gqft.job_name from grouper_qz_fired_triggers gqft, grouper_qz_scheduler_state gqss "
          + " where gqft.job_name in (" + GrouperClientUtils.appendQuestions(jobIdsNamesStartedTimesBatch.size()) + ") and gqft.instance_name = gqss.instance_name and gqss.last_checkin_time > " + lastCheckinTime + " "
          + " union "
          + " select gqft.job_name from grouper_qz_fired_triggers gqft, grouper_qz_triggers gqt, grouper_qz_scheduler_state gqss "
          + " where gqft.trigger_name = gqt.trigger_name and gqt.job_name in (" + GrouperClientUtils.appendQuestions(jobIdsNamesStartedTimesBatch.size()) + ") "
          + " and gqft.instance_name = gqss.instance_name and gqss.last_checkin_time > " + lastCheckinTime + " ");
      
      gcDbAccess = new GcDbAccess();
      
      for (Object[] jobIdNameStartedTime : jobIdsNamesStartedTimesBatch) {
        
        gcDbAccess.addBindVar(jobIdNameStartedTime[1]);
        
      }
      for (Object[] jobIdNameStartedTime : jobIdsNamesStartedTimesBatch) {
        
        gcDbAccess.addBindVar(jobIdNameStartedTime[1]);
        
      }
      
      List<String> jobNamesRunningBatch = gcDbAccess.sql(sql.toString()).selectList(String.class);
      jobNamesRunning.addAll(jobNamesRunningBatch);
    }

    for (Object[] jobIdNameStartedTime : jobIdsNamesStartedTimes) {
      
      if (!jobNamesRunning.contains(jobIdNameStartedTime[1])) {
        idsAndJobNamesToFix.add(new MultiKey(jobIdNameStartedTime[0], jobIdNameStartedTime[1]));
      }
    }
    
    int fixed = 0;

    otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Fixed jobs where jvm died: ");

    for (MultiKey idAndName : idsAndJobNamesToFix) {

      Hib3GrouperLoaderLog hib3GrouperLoaderLog = HibernateSession.byHqlStatic().createQuery("from Hib3GrouperLoaderLog theLoaderLog1 " +
          "where theLoaderLog1.id = :id and status in ('STARTED', 'RUNNING')")
          .setString("id", (String)idAndName.getKey(0)).uniqueResult(edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog.class);

      if (hib3GrouperLoaderLog == null) {
        continue;
      }
      
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(idAndName.getKey(1) + ", ");
      otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
      hib3GrouperLoaderLog.appendJobMessage(".  Marking job as error since its status was " + hib3GrouperLoaderLog.getStatus() + " and it is not detected as running.  The JVM abruptly stopped or died perhaps due to memory error.");
      hib3GrouperLoaderLog.setStatus("ERROR");
      hib3GrouperLoaderLog.store();
      
      fixed++;
      
    }

    otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(fixed + ".  ");
    
    otherJobInput.getHib3GrouperLoaderLog().store();
  }
  

  
  private void handleMissingTriggers(OtherJobInput otherJobInput, boolean runNow) {

    // either running now or every thirty minutes
    boolean run = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("schedulerCheckDaemon.handleMissingTriggers", true) 
        && (runNow || new Random().nextInt(100) < GrouperLoaderConfig.retrieveConfig().propertyValueInt("schedulerCheckDaemon.percentRunFixerJobs", 3));

    if (!run) {
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping handleMissingTriggers.  ");
      return;
    }
    
    List<String> badJobs = new ArrayList<String>();
        
    String sql = "select trigger_name from grouper_qz_triggers gqt where start_time < ? and "
        + " (gqt.trigger_type = 'CRON' and not exists (select 1 from grouper_qz_cron_triggers gqct where gqct.trigger_name = gqt.trigger_name)) "
        + " or (gqt.trigger_type = 'SIMPLE' and not exists (select 1 from grouper_qz_simple_triggers gqst where gqst.trigger_name = gqt.trigger_name))";

    List<String> triggerNamesMissingTriggers = new GcDbAccess().sql(sql).addBindVar(System.currentTimeMillis() - (30 * 1000)).selectList(String.class);

    if (GrouperUtil.length(triggerNamesMissingTriggers) > 0) {

      //  if you get something, wait 10 seconds
      try {
        Thread.sleep(11111);
      } catch (InterruptedException e) {
        // ignore
      }

      // run again just to make sure there's not some race condition happening
      List<String> triggerNames2 = new GcDbAccess().sql(sql).addBindVar(System.currentTimeMillis() - (40 * 1000)).selectList(String.class);

      for (String triggerName : triggerNamesMissingTriggers) {      
        if (triggerNames2.contains(triggerName)) {
          LOG.info("Trigger with name=" + triggerName + " is missing a cron or simple trigger entry, the generic entry will be removed");
          badJobs.add(triggerName);
          
          int fixed = new GcDbAccess().sql("delete from grouper_QZ_TRIGGERS where trigger_name = ?").addBindVar(triggerName).executeSql();

          otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(fixed);
        }
      }
      
    }
    otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Fixed " + badJobs.size() + " jobs with trigger entries without the child table entries in trigger cron or simple tables: " + badJobs.toString() + ". ");
    otherJobInput.getHib3GrouperLoaderLog().store();
  }
  
  private void handleBlockedAndAcquiredStates(OtherJobInput otherJobInput, boolean runNow) {

    // either running now or every thirty minutes
    boolean run = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("schedulerCheckDaemon.handleBlockedAndAcquiredStates", true) 
        && (runNow || new Random().nextInt(100) < GrouperLoaderConfig.retrieveConfig().propertyValueInt("schedulerCheckDaemon.percentRunFixerJobs", 3));

    if (!run) {
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping handleBlockedAndAcquiredStates.  ");
      return;
    }

    List<String> badJobs = new ArrayList<String>();
        
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.add(Calendar.MINUTE, -5);
    long millis = calendar.getTimeInMillis();

    String sql = "select trigger_name from grouper_QZ_TRIGGERS where (trigger_state = 'BLOCKED' or trigger_state = 'ACQUIRED') and next_fire_time < ? and trigger_name not in (select trigger_name from grouper_QZ_FIRED_TRIGGERS)";
    
    List<String> triggerNames = HibernateSession.bySqlStatic().listSelect(String.class, sql, GrouperUtil.toListObject(millis), HibUtils.listType(LongType.INSTANCE));
    
    if (triggerNames.size() > 0) {
      try {
        Thread.sleep(11111);
      } catch (InterruptedException e) {
        // ignore
      }
      
      // run again just to make sure there's not some race condition happening
      List<String> triggerNames2 = HibernateSession.bySqlStatic().listSelect(String.class, sql, GrouperUtil.toListObject(millis), HibUtils.listType(LongType.INSTANCE));
      
      for (String triggerName : triggerNames) {      
        if (triggerNames2.contains(triggerName)) {
          LOG.info("Trigger with name=" + triggerName + " is not being fired.  Updating trigger state.");
          badJobs.add(triggerName);
          
          int fixed = HibernateSession.bySqlStatic().executeSql("update grouper_QZ_TRIGGERS set trigger_state='WAITING' where trigger_name=? and (trigger_state='BLOCKED' or trigger_state='ACQUIRED')",
              GrouperUtil.toListObject(triggerName), HibUtils.listType(StringType.INSTANCE));

          otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(fixed);
        }
      }
    }
    
    otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Fixed " + badJobs.size() + " jobs stuck in BLOCKED or ACQUIRED states: " + badJobs.toString() + ". ");
    otherJobInput.getHib3GrouperLoaderLog().store();
  }
  

  private void handleErrorState(OtherJobInput otherJobInput, boolean runNow) {

    // either running now or every thirty minutes
    boolean run = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("schedulerCheckDaemon.handleErrorState", true) 
        && (runNow || new Random().nextInt(100) < GrouperLoaderConfig.retrieveConfig().propertyValueInt("schedulerCheckDaemon.percentRunFixerJobs", 3));

    if (!run) {
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping handleErrorState.  ");
      return;
    }

    List<String> badJobs = new ArrayList<String>();

    String sql = "select trigger_name from grouper_QZ_TRIGGERS where trigger_state = 'ERROR'";
    
    List<String> triggerNames = HibernateSession.bySqlStatic().listSelect(String.class, sql, null, null);
    
    for (String triggerName : triggerNames) {      
      LOG.info("Trigger with name=" + triggerName + " is not being fired.  Updating trigger state.");
      badJobs.add(triggerName);
        
      int fixed = HibernateSession.bySqlStatic().executeSql("update grouper_QZ_TRIGGERS set trigger_state='WAITING' where trigger_name=? and trigger_state='ERROR'",
          GrouperUtil.toListObject(triggerName), HibUtils.listType(StringType.INSTANCE));
      
      otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(fixed);
    }
    
    otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Fixed " + badJobs.size() + " jobs stuck in ERROR state: " + badJobs.toString() + ". ");
    otherJobInput.getHib3GrouperLoaderLog().store();
  }
}
