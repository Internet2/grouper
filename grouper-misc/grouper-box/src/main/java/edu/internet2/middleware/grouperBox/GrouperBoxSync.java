/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperBoxSync {

  /**
   * 
   */
  public GrouperBoxSync() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    grouperBoxSync();
  }

  /**
   * 
   */
  public static void grouperBoxSync() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTimeNanos = System.nanoTime();

    try {
      debugMap.put("method", "grouperBoxSync");
  
      String cronStringFull = GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.fullSync.quartzCron");
  
      debugMap.put("cronStringFull", cronStringFull);
  
      if (!GrouperClientUtils.isBlank(cronStringFull)) {
        
        JobDetail jobDetail = JobBuilder.newJob(GrouperBoxFullRefresh.class)
            .withIdentity("grouperBoxFullSync")
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger_grouperBoxFullSync")
            .withPriority(1)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronStringFull))
            .build();
  
        try {
          schedulerFactory().getScheduler().scheduleJob(jobDetail, GrouperClientUtils.toSet(trigger), true);
        } catch (Exception e) {
          throw new RuntimeException("Problem scheduling job: grouperBoxFullSync, '" + cronStringFull + "'", e);
        }
        
        debugMap.put("scheduledFull", true);
  
      }
      
      
      String cronStringIncremental = GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.incrementalSync.quartzCron");
  
      debugMap.put("cronStringIncremental", cronStringIncremental);
  
      if (!GrouperClientUtils.isBlank(cronStringIncremental)) {
  
        JobDetail jobDetail = JobBuilder.newJob(GrouperBoxMessageConsumer.class)
            .withIdentity("grouperBoxMessageConsumer")
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger_grouperBoxMessageConsumer")
            .withPriority(1)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronStringIncremental))
            .build();
  
        try {
          schedulerFactory().getScheduler().scheduleJob(jobDetail, GrouperClientUtils.toSet(trigger), true);
        } catch (Exception e) {
          throw new RuntimeException("Problem scheduling job: grouperBoxMessageConsumer, '" + cronStringIncremental + "'", e);
        }
        debugMap.put("scheduledIncremental", true);
  
      }
      
      //you must configure one of these
      if (GrouperClientUtils.isBlank(cronStringFull) && GrouperClientUtils.isBlank(cronStringIncremental)) {
        throw new RuntimeException("Did not configure grouper.client.properties grouperBox.fullSync.quartzCron or grouperBox.incrementalSync.quartzCron!");
      }
      
      // delay starting the scheduler until the end to make sure things that need to be unscheduled are taken care of first?
      try {
        schedulerFactory().getScheduler().start();
      } catch (SchedulerException e) {
        throw new RuntimeException(e);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
    }
  }
  
  
  /**
   * scheduler factory singleton
   */
  private static SchedulerFactory schedulerFactory = null;

  /**
   * lazy load (and start the scheduler) the scheduler factory
   * @return the scheduler factory
   */
  public static SchedulerFactory schedulerFactory() {
    if (schedulerFactory == null) {
      
      Properties props = new Properties();
      for (String key : GrouperClientConfig.retrieveConfig().propertyNames()) {
        if (key.startsWith("org.quartz.")) {
          String value = GrouperClientConfig.retrieveConfig().propertyValueString(key);
          if (value == null) {
            value = "";
          }
          props.put(key, value);
        }
      }
      
      try {
        schedulerFactory = new StdSchedulerFactory(props);
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }

}
