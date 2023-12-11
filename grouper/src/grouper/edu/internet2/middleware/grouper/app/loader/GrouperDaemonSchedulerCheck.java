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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

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
    
    handleBlockedAndAcquiredStates(otherJobInput);
    handleErrorState(otherJobInput);
    handleMissingTriggers(otherJobInput);

    LOG.info("GrouperDaemonSchedulerCheck finished successfully.");
    return null;
  }
  
  private void handleMissingTriggers(OtherJobInput otherJobInput) {

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
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Fixed " + badJobs.size() + " jobs with trigger entries without the child table entries in trigger cron or simple tables: " + badJobs.toString() + ". ");
    otherJobInput.getHib3GrouperLoaderLog().store();
  }
  
  
  
  private void handleBlockedAndAcquiredStates(OtherJobInput otherJobInput) {

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
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Fixed " + badJobs.size() + " jobs stuck in BLOCKED or ACQUIRED states: " + badJobs.toString() + ". ");
    otherJobInput.getHib3GrouperLoaderLog().store();
  }
  

  private void handleErrorState(OtherJobInput otherJobInput) {

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
