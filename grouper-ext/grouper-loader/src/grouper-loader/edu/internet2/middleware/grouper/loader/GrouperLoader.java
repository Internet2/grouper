/*
 * @author mchyzer
 * $Id: GrouperLoader.java,v 1.5 2008-05-14 05:39:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.util.Arrays;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.PlatformFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.loader.db.Hib3GrouploaderLog;
import edu.internet2.middleware.grouper.loader.util.GrouperDdlUtils;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;



/**
 * main class to start the grouper loader
 */
public class GrouperLoader {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperLoader.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    //printAllSupportDdlUtilsPlatforms();
    
    //figures out ddl
    GrouperDdlUtils.bootstrap();

    //this will find all schedulable groups, and schedule them
    GrouperLoaderType.scheduleLoads();
    
    scheduleMaintenanceJobs();
  }

  /**
   * print out all ddlutils platforms
   */
  public static void printAllSupportDdlUtilsPlatforms() {
    String[] platforms = PlatformFactory.getSupportedPlatforms();
    Arrays.sort(platforms);
    for (String platform : platforms) {
      System.out.print(platform + ", ");
    }
  }
  
  
  /**
   * group attribute name of type of the loader, must match one of the enums in GrouperLoaderType
   */
  public static final String GROUPER_LOADER_TYPE = "grouperLoaderType";

  /**
   * job param of group name of the loader
   */
  public static final String GROUPER_LOADER_GROUP_NAME = "grouperLoaderGroupName";

  /**
   * groups to and with to restrict members (e.g. "and" with activeEmployees)
   */
  public static final String GROUPER_LOADER_AND_GROUPS = "grouperLoaderAndGroups";

  /**
   * group attribute name of type of schedule, must match one of the enums in GrouperLoaderScheduleType
   */
  public static final String GROUPER_LOADER_SCHEDULE_TYPE = "grouperLoaderScheduleType";

  /**
   * group attribute name of query, must have the required columns for the grouperLoaderType
   */
  public static final String GROUPER_LOADER_QUERY = "grouperLoaderQuery";

  /**
   * group attribute name of quartz cron-like string to describe when the job should run
   */
  public static final String GROUPER_LOADER_QUARTZ_CRON = "grouperLoaderQuartzCron";

  /**
   * group attribute name of the interval in seconds for a schedule type like START_TO_START_INTERVAL
   */
  public static final String GROUPER_LOADER_INTERVAL_SECONDS = "grouperLoaderIntervalSeconds";

  /**
   * group attribute name of priority of job, optional, if not there, will be 5.  More is better.
   * if the threadpool is full, then this priority will help the schedule pick which job should go next
   */
  public static final String GROUPER_LOADER_PRIORITY = "grouperLoaderPriority";

  /**
   * group attribute name of the db connection where this query comes from.
   * if the name is "grouper", then it will be the group db name
   */
  public static final String GROUPER_LOADER_DB_NAME = "grouperLoaderDbName";
  
  /**
   * group uuid in the job context attributes
   */
  public static final String GROUPER_LOADER_GROUP_UUID = "grouperLoaderGroupUuid";
  
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
      schedulerFactory = new StdSchedulerFactory();
      try {
        schedulerFactory.getScheduler().start();
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }
  
  /**
   * schedule maintenance jobs
   */
  public static void scheduleMaintenanceJobs() {
    
    //schedule daily anytime
    //6am daily: "0 0 6 * * ?"
    //every minute for testing: "0 * * * * ?"
    String cronString = "0 * * * * ?";
    
    //this is a low priority job
    int priority = 1;

    //schedule the log delete job
    try {

      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.MAINTENANCE_CLEAN_LOGS, null, GrouperLoaderJob.class);

      //set data for the job to execute
      jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_GROUP_NAME, null);
      jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_GROUP_UUID, null);
      jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_TYPE, GrouperLoaderType.MAINTENANCE.name());
      jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_DB_NAME, "grouper");
      jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_QUERY, null);
      jobDetail.getJobDataMap().put(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, 
          GrouperLoaderScheduleType.CRON.name());
      
      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;
      
      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);
      
      trigger.setName("triggerMaintenance_cleanLogs");
      
      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);

      
    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.MAINTENANCE_CLEAN_LOGS + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouploaderLog hib3GrouploaderLog = new Hib3GrouploaderLog();
        hib3GrouploaderLog.setHost(GrouperLoaderUtils.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.MAINTENANCE_CLEAN_LOGS);
        hib3GrouploaderLog.setJobSchedulePriority(priority);
        hib3GrouploaderLog.setJobScheduleQuartzCron(cronString);
        hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
        hib3GrouploaderLog.setJobType(GrouperLoaderType.MAINTENANCE.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }

  }
  
}
