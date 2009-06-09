/*
 * @author mchyzer
 * $Id: GrouperLoader.java,v 1.14 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.PlatformFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * main class to start the grouper loader
 */
public class GrouperLoader {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoader.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

    //printAllSupportDdlUtilsPlatforms();
    GrouperStartup.startup();
    
    //make sure properties file is there
    GrouperCheckConfig.checkResource("grouper-loader.properties");
    
    //make sure properties are there
    GrouperCheckConfig.checkConfigProperties("grouper-loader.properties", 
        "grouper-loader.example.properties");
    
    GrouperCheckConfig.checkGrouperLoaderConfigDbs();
    GrouperCheckConfig.checkGrouperLoaderConsumers();
    
    //this will find all schedulable groups, and schedule them
    GrouperLoaderType.scheduleLoads();
    
    scheduleMaintenanceJobs();
    scheduleChangeLogJobs();
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
   * group attribute name of type of the loader, must match one of the enums in GrouperLoaderType.
   * If there is a query, and it has "group_name" before "from", then defaults to SQL_GROUP_LIST
   * else defaults to SQL_SIMPLE
   */
  public static final String GROUPER_LOADER_TYPE = "grouperLoaderType";

  /**
   * groups to and with to restrict members (e.g. "and" with activeEmployees)
   */
  public static final String GROUPER_LOADER_AND_GROUPS = "grouperLoaderAndGroups";

  /**
   * If you want the group (if not used from anywhere) or members deleted when 
   * no longer in loader sql results, list the sql like name, e.g. stem1:stem2:%:%org
   */
  public static final String GROUPER_LOADER_GROUPS_LIKE = "grouperLoaderGroupsLike";

  /**
   * optional group information for a group list query: e.g. to specify the display name of the
   * group/stem when it is created
   */
  public static final String GROUPER_LOADER_GROUP_QUERY = "grouperLoaderGroupQuery";

  /**
   * types to add to loaded groups
   */
  public static final String GROUPER_LOADER_GROUP_TYPES = "grouperLoaderGroupTypes";

  /**
   * group attribute name of type of schedule, must match one of the enums in GrouperLoaderScheduleType.
   * defaults to START_TO_START_INTERVAL if grouperLoaderQuartzCron is blank, else defaults to
   * CRON
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
   * group attribute name of the interval in seconds for a schedule type like START_TO_START_INTERVAL.
   * defaults to 86400 (1 day)
   */
  public static final String GROUPER_LOADER_INTERVAL_SECONDS = "grouperLoaderIntervalSeconds";

  /**
   * group attribute name of priority of job, optional, if not there, will be 5.  More is better.
   * if the threadpool is full, then this priority will help the schedule pick which job should go next
   */
  public static final String GROUPER_LOADER_PRIORITY = "grouperLoaderPriority";

  /**
   * group attribute name of the db connection where this query comes from.
   * if the name is "grouper", then it will be the group db name.  defaults to "grouper" for sql type
   * loaders
   */
  public static final String GROUPER_LOADER_DB_NAME = "grouperLoaderDbName";
  
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

    scheduleLogCleanerJob();
    scheduleDailyReportJob();

  }

  /**
   * schedule change log jobs
   */
  public static void scheduleChangeLogJobs() {
    scheduleChangeLogTempToChangeLogJob();
    scheduleChangeLogConsumers();
  }
  
  /**
   * schedule maintenance job for moving records from change log to change log temp
   */
  public static void scheduleChangeLogTempToChangeLogJob() {

    String cronString = null;

    //this is a medium priority job
    int priority = 5;

    //schedule the log delete job
    try {
      
      if (!GrouperLoaderConfig.getPropertyBoolean("changeLog.changeLogTempToChangeLog.enable", false)) {
        LOG.warn("grouper-loader.properties key: changeLog.changeLogTempToChangeLog.enable is not " +
          "filled in or false so the change log temp to change log daemon will not run");
        return;
      }
      
      cronString = GrouperLoaderConfig.getPropertyString("changeLog.changeLogTempToChangeLog.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        cronString = "50 * * * * ?";
        
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG, null, GrouperLoaderJob.class);

      //schedule this job
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerChangeLog_grouperChangeLogTempToChangeLog");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
        hib3GrouploaderLog.setJobSchedulePriority(priority);
        hib3GrouploaderLog.setJobScheduleQuartzCron(cronString);
        hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
        hib3GrouploaderLog.setJobType(GrouperLoaderType.CHANGE_LOG.name());
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        hib3GrouploaderLog.store();
        
      } catch (Exception e2) {
        LOG.error("Problem logging to loader db log", e2);
      }
    }

  }

  /**
   * schedule change log consumer jobs
   */
  public static void scheduleChangeLogConsumers() {

    //changeLog.consumer.ldappc.class = 
    //changeLog.consumer.ldappc.quartz.cron
    
    //make sure sequences are ok
    Map<String, String> consumerMap = GrouperCheckConfig.retrievePropertiesKeys("grouper-loader.properties", 
        GrouperCheckConfig.grouperLoaderConsumerPattern);
    
    int index = 0;
    
    while (consumerMap.size() > 0) {
      
      //get one
      String consumerKey = consumerMap.keySet().iterator().next();
      //get the database name
      Matcher matcher = GrouperCheckConfig.grouperLoaderConsumerPattern.matcher(consumerKey);
      matcher.matches();
      String consumerName = matcher.group(1);
      boolean missingOne = false;
      //now find all 4 required keys
      String classKey = "changeLog.consumer." + consumerName + ".class";
      if (!consumerMap.containsKey(classKey)) {
        String error = "cannot find grouper-loader.properties key: " + classKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String cronKey = "changeLog.consumer." + consumerName + ".quartzCron";

      //check the classname
      Class<?> theClass = null;
      String className = consumerMap.get(classKey);
      String cronString = consumerMap.get(cronKey);
      
      String jobName = GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + consumerName;
      
      //this is a medium priority job
      int priority = 5;

      try {
        if (missingOne) {
          throw new RuntimeException("Cant find config param" );
        }
        
        theClass = GrouperUtil.forName(className);
        if (!ChangeLogConsumerBase.class.isAssignableFrom(theClass)) {
          throw new RuntimeException("not a subclass of ChangeLogConsumerBase");
        }

        //default to every minute on the minute, though add a couple of seconds for each one
        if (StringUtils.isBlank(cronString)) {
          cronString = ((index * 2) % 60) + " * * * * ?";
        }
        //at this point we have all the attributes and we know the required ones are there, and logged when 
        //forbidden ones are there
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

        //the name of the job must be unique, so use the group name since one job per group (at this point)
        JobDetail jobDetail = new JobDetail(jobName, null, GrouperLoaderJob.class);

        //schedule this job
        GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

        Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

        trigger.setName("triggerChangeLog_" + jobName);

        trigger.setPriority(priority);

        scheduler.scheduleJob(jobDetail, trigger);

      } catch (Exception e) {

        String errorMessage = "Could not schedule job: '" + jobName + "'";
        LOG.error(errorMessage, e);
        errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
        try {
          //lets enter a log entry so it shows up as error in the db
          Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
          hib3GrouploaderLog.setHost(GrouperUtil.hostname());
          hib3GrouploaderLog.setJobMessage(errorMessage);
          hib3GrouploaderLog.setJobName(jobName);
          hib3GrouploaderLog.setJobSchedulePriority(priority);
          hib3GrouploaderLog.setJobScheduleQuartzCron(cronString);
          hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
          hib3GrouploaderLog.setJobType(GrouperLoaderType.CHANGE_LOG.name());
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
          hib3GrouploaderLog.store();
          
        } catch (Exception e2) {
          LOG.error("Problem logging to loader db log", e2);
        }

      }
      
      consumerMap.remove(classKey);
      consumerMap.remove(cronKey);
      index++;
    }
      

  }


  /**
   * schedule maintenance job
   */
  public static void scheduleDailyReportJob() {

    String cronString = null;

    //this is a low priority job
    int priority = 1;

    //schedule the log delete job
    try {
      
      cronString = GrouperLoaderConfig.getPropertyString("daily.report.quartz.cron");

      if (StringUtils.isBlank(cronString)) {
        LOG.warn("grouper-loader.properties key: daily.report.quartz.cron is not " +
        		"filled in so the daily report will not run");
        return;
      }
      
      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.GROUPER_REPORT, null, GrouperLoaderJob.class);

      //schedule this job daily at 6am
      GrouperLoaderScheduleType grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON;

      Trigger trigger = grouperLoaderScheduleType.createTrigger(cronString, null);

      trigger.setName("triggerMaintenance_grouperReport");

      trigger.setPriority(priority);

      scheduler.scheduleJob(jobDetail, trigger);


    } catch (Exception e) {
      String errorMessage = "Could not schedule job: '" + GrouperLoaderType.GROUPER_REPORT + "'";
      LOG.error(errorMessage, e);
      errorMessage += "\n" + ExceptionUtils.getFullStackTrace(e);
      try {
        //lets enter a log entry so it shows up as error in the db
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobMessage(errorMessage);
        hib3GrouploaderLog.setJobName(GrouperLoaderType.GROUPER_REPORT);
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

  /**
   * schedule maintenance job
   */
  public static void scheduleLogCleanerJob() {
    
    //schedule daily anytime
    //6am daily: "0 0 6 * * ?"
    //every minute for testing: "0 * * * * ?"
    String cronString = "0 0 6 * * ?";
    
    //this is a low priority job
    int priority = 1;

    //schedule the log delete job
    try {

      //at this point we have all the attributes and we know the required ones are there, and logged when 
      //forbidden ones are there
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      //the name of the job must be unique, so use the group name since one job per group (at this point)
      JobDetail jobDetail = new JobDetail(GrouperLoaderType.MAINTENANCE_CLEAN_LOGS, null, GrouperLoaderJob.class);
      
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
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
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

  /**
   * @param group
   * @param grouperSession
   * @return status
   */
  public static String runJobOnceForGroup(GrouperSession grouperSession, Group group) {
    try {
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
      
      String grouperLoaderTypeString = GrouperLoaderType.attributeValueOrDefaultOrNull(group, GROUPER_LOADER_TYPE);
  
      if (!group.hasType(GroupTypeFinder.find("grouperLoader", true)) 
          || StringUtils.isBlank(grouperLoaderTypeString)) {
        throw new RuntimeException("Cant find grouper loader type of group: " + group.getName());
      }
      
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, true);
      
      hib3GrouperLoaderLog.setJobName(grouperLoaderType.name() + "__" + group.getName() + "__" + group.getUuid());
      hib3GrouperLoaderLog.setJobType(grouperLoaderTypeString);
  
      GrouperLoaderJob.runJob(hib3GrouperLoaderLog, group, grouperSession);
      
      return "loader ran successfully, inserted " + hib3GrouperLoaderLog.getInsertCount()
        + " memberships, deleted " + hib3GrouperLoaderLog.getDeleteCount() + " memberships, total membership count: "
        + hib3GrouperLoaderLog.getTotalCount();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param grouperSession
   * @param jobName
   * @return status
   */
  public static String runOnceByJobName(GrouperSession grouperSession, String jobName) {
    try {
  
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.typeForThisName(jobName);
      if (grouperLoaderType.equals(GrouperLoaderType.SQL_SIMPLE) || grouperLoaderType.equals(GrouperLoaderType.SQL_GROUP_LIST)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
      
        String grouperLoaderGroupUuid = jobName.substring(uuidIndexStart+2, jobName.length());
        Group group = GroupFinder.findByUuid(grouperSession, grouperLoaderGroupUuid, true);
        return runJobOnceForGroup(grouperSession, group);
      }
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
      hib3GrouperLoaderLog.setJobName(jobName);
      GrouperLoaderJob.runJob(hib3GrouperLoaderLog, null, grouperSession);
      
      return "loader ran successfully: " + hib3GrouperLoaderLog.getJobMessage();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
