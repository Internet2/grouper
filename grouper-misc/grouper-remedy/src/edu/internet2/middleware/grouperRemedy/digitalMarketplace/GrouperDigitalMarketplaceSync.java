/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

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
 * This is for quartz outside of grouper, e.g. in a service
 */
public class GrouperDigitalMarketplaceSync {

  /**
   * 
   */
  public GrouperDigitalMarketplaceSync() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    grouperDigitalMarketplaceSync();
  }

  /**
   * 
   */
  public static void grouperDigitalMarketplaceSync() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTimeNanos = System.nanoTime();

    try {
      debugMap.put("method", "grouperDigitalMarketplaceSync");
  
      String cronStringFull = GrouperClientConfig.retrieveConfig().propertyValueString("grouperDigitalMarketplace.fullSync.quartzCron");
  
      debugMap.put("cronStringFull", cronStringFull);
  
      if (!GrouperClientUtils.isBlank(cronStringFull)) {
        
        JobDetail jobDetail = JobBuilder.newJob(GrouperDigitalMarketplaceFullRefresh.class)
            .withIdentity("grouperDigitalMarketplaceFullSync")
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger_grouperDigitalMarketplaceFullSync")
            .withPriority(1)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronStringFull))
            .build();
  
        try {
          schedulerFactory().getScheduler().scheduleJob(jobDetail, GrouperClientUtils.toSet(trigger), true);
        } catch (Exception e) {
          throw new RuntimeException("Problem scheduling job: grouperDigitalMarketplaceFullSync, '" + cronStringFull + "'", e);
        }
        
        debugMap.put("scheduledFull", true);
  
      }
      
      
      String cronStringIncremental = GrouperClientConfig.retrieveConfig().propertyValueString("grouperDigitalMarketplace.incrementalSync.quartzCron");
  
      debugMap.put("cronStringIncremental", cronStringIncremental);
  
      if (!GrouperClientUtils.isBlank(cronStringIncremental)) {
  
        JobDetail jobDetail = JobBuilder.newJob(GrouperDigitalMarketplaceMessageConsumer.class)
            .withIdentity("grouperDigitalMarketplaceMessageConsumer")
            .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger_grouperDigitalMarketplaceMessageConsumer")
            .withPriority(1)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronStringIncremental))
            .build();
  
        try {
          schedulerFactory().getScheduler().scheduleJob(jobDetail, GrouperClientUtils.toSet(trigger), true);
        } catch (Exception e) {
          throw new RuntimeException("Problem scheduling job: grouperDigitalMarketplaceMessageConsumer, '" + cronStringIncremental + "'", e);
        }
        debugMap.put("scheduledIncremental", true);
  
      }
      
      //you must configure one of these
      if (GrouperClientUtils.isBlank(cronStringFull) && GrouperClientUtils.isBlank(cronStringIncremental)) {
        throw new RuntimeException("Did not configure grouper.client.properties grouperDigitalMarketplace.fullSync.quartzCron or grouperDigitalMarketplace.incrementalSync.quartzCron!");
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
      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTimeNanos);
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
